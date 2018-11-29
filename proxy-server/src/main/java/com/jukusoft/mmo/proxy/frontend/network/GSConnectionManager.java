package com.jukusoft.mmo.proxy.frontend.network;

import com.jukusoft.mmo.engine.shared.logger.Log;
import com.jukusoft.mmo.proxy.frontend.Const;
import com.jukusoft.vertx.connection.clientserver.MessageHandler;
import com.jukusoft.vertx.connection.clientserver.RemoteConnection;
import com.jukusoft.vertx.connection.clientserver.ServerData;
import com.jukusoft.vertx.connection.clientserver.TCPClient;
import com.jukusoft.vertx.connection.stream.BufferStream;
import com.jukusoft.vertx.serializer.utils.ByteUtils;
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
    protected boolean authentificated = false;

    /**
    * default constructor
     *
     * @param streamToClient buffer stream which sends buffer to client
     * @param vertx singleton vertx instance
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
        this.vertx.eventBus().send("region-manager::get()", json.encode(), this.deliveryOptions, (Handler<AsyncResult<Message<String>>>) res -> {
            if (res.succeeded()) {
                Log.v(LOG_TAG, "got correct region from zonekeeper for region " + regionID + ", instanceID " + instanceID + ".");

                Message<String> msg = res.result();
                JsonObject response = new JsonObject(msg.body());

                //check, if an error has occured
                if (!response.getString("error").equals("none")) {
                    Log.w(LOG_TAG, "region-manager::get() returned error: " + response.getString("error"));
                    handler.handle(false);

                    return;
                }

                //get ip and port
                String ip = response.getString("ip");
                int port = response.getInteger("port");

                //open connection to region server
                this.open(ip, port, result -> {
                    //TODO: send gs join message to region server with user, character & permissions data

                    handler.handle(result);
                });
            } else {
                Log.w(LOG_TAG, "region-manager::get() has failed. Cause: ", res.cause());

                //get region server failed (maybe caused by timeout)
                handler.handle(false);
            }
        });
    }

    /**
    * open game server connection to a specific region
     *
     * @param ip gameserver ip
     * @param port gameserver port
     * @param connectHandler handler which is called if connection was established or has failed
    */
    protected void open (String ip, int port, Handler<Boolean> connectHandler) {
        // close old connection first, if neccessary
        if (this.currentConn != null) {
            this.currentConn.disconnect();
            this.currentConn = null;
        }

        // reset auth flag
        this.authentificated = false;

        //connect to region server
        this.currentConn = new TCPClient();
        this.currentConn.init(this.vertx);

        //set custom message handler to redirect messages from region server directly to client
        this.currentConn.setCustomMessageHandler((buffer, conn) -> {
            //get type
            byte type = buffer.getByte(0);
            byte extendedType = buffer.getByte(1);

            //check, if type has to be handled from proxy server itself
            if (Const.PROXY_HANDLER_TYPES[type]) {
                //TODO: add code here

                throw new UnsupportedOperationException("proxy server types aren't supported yet.");
            } else {
                //redirect message to client
                this.streamToClient.write(buffer);
            }
        });

        this.currentConn.connect(new ServerData(ip, port), res -> {
            if (!res.succeeded()) {
                Log.w(LOG_TAG, "Couldn't connect to region server " + ip + ":" + port);

                //TODO: send error back to client
                connectHandler.handle(false);

                //TODO: notify load balancer that server is down

                return;
            }

            Log.i(LOG_TAG, "GSConn established successfully to gs " + ip + ":" + port);

            //TODO: send cluster login data to region server to authentificate connection

            connectHandler.handle(true);
        });
    }

    /**
    * send message to one of the opened region connections
     *
     * @param regionID regionID
     * @param instanceID instanceID
     * @param shardID shardID
     * @param buffer message
    */
    public void send (long regionID, int instanceID, int shardID, Buffer buffer) {
        throw new UnsupportedOperationException("operation isn't supported yet.");
    }

    public void sendToActiveRegion (Buffer buffer) {
        if (this.currentConn == null) {
            throw new IllegalStateException("currently no region connection is active.");
        }

        if (!this.authentificated) {
            Log.w(LOG_TAG, "region connection isn't authentificated yet.");
            throw new IllegalStateException("region connection isn't authentificated yet.");
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
