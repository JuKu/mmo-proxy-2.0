package com.jukusoft.mmo.proxy.frontend.network;

import io.vertx.core.buffer.Buffer;

@FunctionalInterface
public interface MessageListener {

    /**
    * this message is called, if a message was received
     *
     * @param buffer message buffer
     * @param state current connection state, e.q. if user is logged in
     * @param clientConn connection to client
     * @param gsConn game server connection manager
     *
     * @throws Exception if handler cannot handle this message correctly
    */
    public void onMessage (Buffer buffer, ConnState state, Connection clientConn, GSConnectionManager gsConn) throws Exception;

}
