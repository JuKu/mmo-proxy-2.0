package com.jukusoft.mmo.proxy.frontend.network;

import com.jukusoft.vertx.connection.stream.BufferStream;
import io.vertx.core.buffer.Buffer;

public class Connection {

    protected final BufferStream bufferStream;

    public Connection (BufferStream bufferStream) {
        this.bufferStream = bufferStream;
    }

    public void send (Buffer buffer) {
        this.bufferStream.write(buffer);
    }

    /**
    * close connection
    */
    public void close () {
        //
    }

}
