package com.jukusoft.mmo.proxy.frontend.network;

import com.jukusoft.vertx.connection.stream.BufferStream;
import com.jukusoft.vertx.serializer.SerializableObject;
import com.jukusoft.vertx.serializer.Serializer;
import io.vertx.core.buffer.Buffer;

public class Connection {

    protected final BufferStream bufferStream;

    public Connection (BufferStream bufferStream) {
        this.bufferStream = bufferStream;
    }

    protected void send (Buffer buffer) {
        this.bufferStream.write(buffer);
    }

    public void send (SerializableObject msg) {
        this.send(Serializer.serialize(msg));
    }

    /**
    * close connection
    */
    public void close () {
        //
    }

}
