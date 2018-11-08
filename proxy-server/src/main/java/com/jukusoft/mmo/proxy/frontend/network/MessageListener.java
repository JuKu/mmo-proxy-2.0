package com.jukusoft.mmo.proxy.frontend.network;

import io.vertx.core.buffer.Buffer;

@FunctionalInterface
public interface MessageListener {

    /**
    * this message is called, if a message was received
     *
     * @param buffer message buffer
     * @param state current connection state, e.q. if user is logged in
    */
    public void onMessage (Buffer buffer, ConnState state);

}