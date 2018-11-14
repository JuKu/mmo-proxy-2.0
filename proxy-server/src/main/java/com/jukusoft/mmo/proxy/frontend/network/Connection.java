package com.jukusoft.mmo.proxy.frontend.network;

import com.jukusoft.vertx.connection.stream.BufferStream;
import com.jukusoft.vertx.serializer.SerializableObject;
import com.jukusoft.vertx.serializer.Serializer;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;

public class Connection {

    protected final NetSocket socket;
    protected final BufferStream bufferStream;

    public Connection (NetSocket socket, BufferStream bufferStream) {
        this.socket = socket;
        this.bufferStream = bufferStream;
    }

    public void sendRaw (Buffer buffer) {
        this.bufferStream.write(buffer);
    }

    public void send (SerializableObject msg) {
        this.sendRaw(Serializer.serialize(msg));
    }

    public String host () {
        return this.socket.remoteAddress().host();
    }

    public int port () {
        return this.socket.remoteAddress().port();
    }

    /**
    * close connection
    */
    public void close () {
        this.socket.close();
    }

}
