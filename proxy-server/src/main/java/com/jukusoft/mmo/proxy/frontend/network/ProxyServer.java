package com.jukusoft.mmo.proxy.frontend.network;

import com.jukusoft.mmo.engine.shared.config.Config;
import com.jukusoft.mmo.engine.shared.logger.Log;
import com.jukusoft.mmo.proxy.frontend.Const;
import com.jukusoft.vertx.connection.clientserver.ClientConnectionImpl;
import com.jukusoft.vertx.connection.clientserver.Server;
import com.jukusoft.vertx.connection.stream.BufferStream;
import com.jukusoft.vertx.serializer.utils.ByteUtils;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetServerOptions;
import io.vertx.core.net.NetSocket;

import java.util.ArrayList;
import java.util.List;

public class ProxyServer {

    //vert.x instance
    protected final Vertx vertx;

    //vert.x tcp server options
    protected NetServerOptions options = new NetServerOptions();

    //instance of vert.x tcp servers
    protected List<NetServer> servers = new ArrayList<>();

    //message listeners for client - proxy messages with type 0x01
    protected MessageListener[] listeners = new MessageListener[256];

    /**
    * default constructor
     *
     * @param vertx vertx instance
    */
    public ProxyServer (Vertx vertx) {
        this.vertx = vertx;
    }

    public void start(String host, int port, Handler<AsyncResult<ProxyServer>> listenHandler) {
        int serverThreads = Config.getInt("Proxy", "serverThreads");

        //Scaling - sharing TCP servers, see https://vertx.io/docs/vertx-core/java/#_scaling_sharing_tcp_servers
        for (int i = 0; i < serverThreads; i++) {
            //create new tcp server
            NetServer server = this.vertx.createNetServer(options);

            //set connect handler
            server.connectHandler(this::connectHandler);

            //start server
            server.listen(port, host);

            servers.add(server);
        }

        //call handler
        listenHandler.handle(Future.succeededFuture(this));
    }

    protected void connectHandler (NetSocket socket) {
        //create buffer stream
        BufferStream bufferStream = new BufferStream(socket, socket);

        //pause reading data
        bufferStream.pause();

        //TODO: check ip blacklist / firewall

        final Connection connection = new Connection(bufferStream);
        final ConnState state = new ConnState();
        final GSConnectionManager gsConnectionManager = new GSConnectionManager(bufferStream);

        bufferStream.handler(buffer -> {
            //get type
            byte type = buffer.getByte(0);
            byte extendedType = buffer.getByte(1);

            //check, if type has to be handled from proxy server itself
            if (Const.PROXY_HANDLER_TYPES[type]) {
                if (this.listeners[extendedType] == null) {
                    try {
                        //call message handler
                        this.listeners[extendedType].onMessage(buffer, state, gsConnectionManager);
                    } catch (Exception e) {
                        Log.w("Message", "[" + socket.remoteAddress().host() + ":" + socket.remoteAddress().port() + "] Exception while handle message (" + ByteUtils.byteToHex(type) + ", " + ByteUtils.byteToHex(extendedType) + "): ", e);
                    }
                } else {
                    Log.w("Message Handler", "no message listener registered for type " + ByteUtils.byteToHex(type) + ", extendedType " + ByteUtils.byteToHex(extendedType) + ", drop message.");
                }
            } else {
                //check login first
                if (!state.isLoggedIn()) {
                    Log.w("Message", "[" + socket.remoteAddress().host() + ":" + socket.remoteAddress().port() + "] drop message with type " + ByteUtils.byteToHex(type) + ", because user isn't logged in.");
                    return;
                }

                //redirect message to active game server (region)
                gsConnectionManager.sendToActiveRegion(buffer);
            }
        });

        bufferStream.endHandler(v -> {
            connection.close();

            //close all connections to game server
            gsConnectionManager.closeAllConnections();

            //cleanUp memory of connection state
            state.cleanUp();
        });

        //resume reading data
        bufferStream.resume();
    }

    public void addMessageListener (byte type, byte extendedType, MessageListener listener) {
        if (type != 0x01) {
            throw new UnsupportedOperationException("Only type 0x01 is allowed for handling on proxy server.");
        }

        this.listeners[extendedType] = listener;
    }

    public void removeMessageListener (byte type, byte extendedType) {
        if (type != 0x01) {
            throw new UnsupportedOperationException("Only type 0x01 is allowed for handling on proxy server.");
        }

        this.listeners[extendedType] = null;
    }

    /**
    * shutdown tcp server
    */
    public void shutdown () {
        for (NetServer server : servers) {
            //shutdown tcp server
            server.close();
        }

        if (this.vertx != null) {
            this.vertx.close();
        }
    }

}
