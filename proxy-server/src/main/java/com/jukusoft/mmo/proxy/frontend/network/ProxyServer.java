package com.jukusoft.mmo.proxy.frontend.network;

import com.jukusoft.mmo.engine.shared.config.Config;
import com.jukusoft.vertx.connection.clientserver.ClientConnectionImpl;
import com.jukusoft.vertx.connection.clientserver.Server;
import com.jukusoft.vertx.connection.stream.BufferStream;
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

        /*final ClientConnectionImpl conn = new ClientConnectionImpl(socket, bufferStream, this);

        bufferStream.handler(buffer -> this.messageReceived(buffer, conn));

        bufferStream.endHandler(v -> conn.handleClose());

        //call client handler
        this.clientHandler.handle(conn);*/

        //TODO: create connection state and register handlers

        //resume reading data
        bufferStream.resume();
    }

    public void addMessageListener (byte type, MessageListener listener) {
        //
    }

    public void removeMessageListener (byte type) {
        //
    }

    public void setDefaultListener (MessageListener listener) {
        //
    }

    /**
    * shutdown tcp server
    */
    public void shutdown () {
        //
    }

}
