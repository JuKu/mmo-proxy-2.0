package com.jukusoft.mmo.proxy.frontend.network;

import com.jukusoft.mmo.engine.shared.logger.Log;
import com.jukusoft.vertx.connection.stream.BufferStream;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;

/**
* manages gameserver connections for a single player (one player - 1..* game server connections)
*/
public class GSConnectionManager {

    protected static final String LOG_TAG = "GSConn";

    protected final BufferStream streamToClient;

    /**
    * default constructor
     *
     * @param streamToClient buffer stream which sends buffer to client
    */
    public GSConnectionManager(BufferStream streamToClient) {
        this.streamToClient = streamToClient;
    }

    public void requestJoin (long regionID, int instanceID, Handler<Boolean> handler) {
        Log.d(LOG_TAG, "try to join region " + regionID + " on instanceID: " + instanceID + "...");

        //TODO: find free shard

        //TODO: connect to region server

        //TODO: send cluster login data to region server to authentificate connection

        //TODO: send gs join message to region server with user, character & permissions data
    }

    /**
    * open game server connection to a specific region
    */
    protected void open (long regionID, int instanceID, int shardID) {
        //
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
        //
    }

    /**
    * close all connections to game server
    */
    public void closeAllConnections () {
        //
    }

}
