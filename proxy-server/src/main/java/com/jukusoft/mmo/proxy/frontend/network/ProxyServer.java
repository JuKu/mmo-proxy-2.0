package com.jukusoft.mmo.proxy.frontend.network;

import com.jukusoft.mmo.engine.shared.config.Config;
import com.jukusoft.mmo.engine.shared.logger.Log;
import com.jukusoft.mmo.proxy.frontend.Const;
import com.jukusoft.vertx.connection.stream.BufferStream;
import com.jukusoft.vertx.serializer.annotations.MessageType;
import com.jukusoft.vertx.serializer.annotations.ProtocolVersion;
import com.jukusoft.vertx.serializer.exceptions.NoMessageTypeException;
import com.jukusoft.vertx.serializer.exceptions.NoProtocolVersionException;
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

    protected static final String MESSAGE_TAG = "Message";
    protected static final String CONNECT_TAG = "Connect";

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

        Log.i(CONNECT_TAG, "new client connection: " + socket.remoteAddress().host() + ":" + socket.remoteAddress().port() + "");

        //TODO: check ip blacklist / firewall

        final Connection connection = new Connection(socket, bufferStream);
        final ConnState state = new ConnState();
        final GSConnectionManager gsConnectionManager = new GSConnectionManager(bufferStream, this.vertx, state);

        bufferStream.handler(buffer -> {
            //get type
            byte type = buffer.getByte(0);
            byte extendedType = buffer.getByte(1);

            //check, if type has to be handled from proxy server itself
            if (Const.PROXY_HANDLER_TYPES[type]) {
                if (this.listeners[extendedType] != null) {
                    try {
                        //call message handler
                        this.listeners[extendedType].onMessage(buffer, state, connection, gsConnectionManager);
                    } catch (Exception e) {
                        Log.w(MESSAGE_TAG, "[" + socket.remoteAddress().host() + ":" + socket.remoteAddress().port() + "] Exception while handle message (" + ByteUtils.byteToHex(type) + ", " + ByteUtils.byteToHex(extendedType) + "): ", e);
                    }
                } else {
                    Log.w(MESSAGE_TAG, "no message listener registered for type " + ByteUtils.byteToHex(type) + ", extendedType " + ByteUtils.byteToHex(extendedType) + ", drop message.");
                }
            } else {
                //check login first
                if (!state.isLoggedIn()) {
                    Log.w(MESSAGE_TAG, "[" + socket.remoteAddress().host() + ":" + socket.remoteAddress().port() + "] drop message with type " + ByteUtils.byteToHex(type) + ", because user isn't logged in.");
                    return;
                }

                //redirect message to active game server (region)
                gsConnectionManager.sendToActiveRegion(buffer);
            }
        });

        bufferStream.endHandler(v -> {
            Log.i(CONNECT_TAG, "connection closed: " + socket.remoteAddress().host() + ":" + socket.remoteAddress().port() + "");

            connection.close();

            //close all connections to game server
            gsConnectionManager.closeAllConnections();

            //cleanUp memory of connection state
            state.cleanUp();
        });

        //resume reading data
        bufferStream.resume();
    }

    protected void addMessageListener (byte type, byte extendedType, MessageListener listener) {
        if (type != 0x01) {
            throw new UnsupportedOperationException("Only type 0x01 is allowed for handling on proxy server.");
        }

        this.listeners[extendedType] = listener;
    }

    public void addMessageListener (MessageListener listener) {
        //check, if listener has required annotations
        if (listener.getClass().getAnnotation(MessageType.class) == null) {
            throw new NoMessageTypeException("No message type annotation was found in class '" + listener.getClass().getCanonicalName() + "'!");
        }

        if (listener.getClass().getAnnotation(ProtocolVersion.class) == null) {
            throw new NoProtocolVersionException("No protocol version annotation was found in class '" + listener.getClass().getCanonicalName() + "'!");
        }

        MessageType msgType = listener.getClass().getAnnotation(MessageType.class);

        if (msgType.type() == 0x00) {
            throw new IllegalStateException("message type cannot 0x00, please correct annotation @MessageType in class '" + listener.getClass().getCanonicalName()+ "'!");
        }

        ProtocolVersion version = listener.getClass().getAnnotation(ProtocolVersion.class);

        //add message listener with protocol version check
        this.addMessageListener(msgType.type(), msgType.extendedType(), (buffer, state, conn, gsConn) -> {
            if (buffer.getShort(2) != version.value()) {
                throw new IllegalStateException("received message protocol version " + buffer.getShort(2) + " isn't supported by version " + version.value() + "!");
            }

            listener.onMessage(buffer, state, conn, gsConn);
        });
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
