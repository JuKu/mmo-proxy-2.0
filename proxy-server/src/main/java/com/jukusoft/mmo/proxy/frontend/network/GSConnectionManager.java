package com.jukusoft.mmo.proxy.frontend.network;

import com.jukusoft.vertx.connection.stream.BufferStream;
import io.vertx.core.buffer.Buffer;

/**
* manages gameserver connections for a single player (one player - 1..* game server connections)
*/
public class GSConnectionManager {

    protected final BufferStream streamToClient;

    /**
    * default constructor
     *
     * @param streamToClient buffer stream which sends buffer to client
    */
    public GSConnectionManager(BufferStream streamToClient) {
        this.streamToClient = streamToClient;
    }

    /**
    * open game server connection to a specific region
    */
    public void open (long regionID, int instanceID, int shardID) {
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
