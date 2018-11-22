package com.jukusoft.mmo.proxy.frontend.network;

import com.jukusoft.mmo.engine.shared.logger.Log;
import com.jukusoft.vertx.connection.clientserver.TCPClient;
import com.jukusoft.vertx.connection.stream.BufferStream;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

/**
* manages gameserver connections for a single player (one player - 1..* game server connections)
*/
public class GSConnectionManager {

    protected static final String LOG_TAG = "GSConn";

    protected final BufferStream streamToClient;
    protected final Vertx vertx;
    protected DeliveryOptions deliveryOptions = new DeliveryOptions();

    protected TCPClient currentConn = null;

    /**
    * default constructor
     *
     * @param streamToClient buffer stream which sends buffer to client
    */
    public GSConnectionManager(BufferStream streamToClient, Vertx vertx) {
        this.streamToClient = streamToClient;
        this.vertx = vertx;

        //set send timeout of 3 seconds
        this.deliveryOptions.setSendTimeout(3000);
    }

    public void requestJoin (long regionID, int instanceID, Handler<Boolean> handler) {
        Log.d(LOG_TAG, "try to join region " + regionID + " on instanceID: " + instanceID + "...");

        //find free shard

        //request region server
        JsonObject json = new JsonObject();
        json.put("regionID", regionID);
        json.put("instanceID", instanceID);
        this.vertx.eventBus().send("region-manager", json.encode(), this.deliveryOptions, (Handler<AsyncResult<Message<String>>>) res -> {
            if (res.succeeded()) {
                Message<String> msg = res.result();
                JsonObject response = new JsonObject(msg.body());

                //get ip and port
                String ip = response.getString("ip");
                int port = response.getInteger("port");

                //open connection to region server
                this.open(ip, port);

                //TODO: send gs join message to region server with user, character & permissions data
            } else {
                //get region server failed (maybe caused by timeout)
                handler.handle(false);
            }
        });
    }

    /**
    * open game server connection to a specific region
    */
    protected void open (String ip, int port) {
        //TODO: connect to region server

        //TODO: send cluster login data to region server to authentificate connection
    }

    /**
    * send message to one of the opened region connections
     *
     * @param regionID regionID
     * @param instanceID instanceID
     * @param shardID shardID
    */
    public void send (long regionID, int instanceID, int shardID, Buffer buffer) {
        throw new UnsupportedOperationException("operation isn't supported yet.");
    }

    public void sendToActiveRegion (Buffer buffer) {
        if (this.currentConn == null) {
            throw new IllegalStateException("currently no region connection is active.");
        }

        this.currentConn.sendRaw(buffer);
    }

    /**
    * close all connections to game server
    */
    public void closeAllConnections () {
        if (this.currentConn != null) {
            this.currentConn.disconnect();
        }
    }

}
