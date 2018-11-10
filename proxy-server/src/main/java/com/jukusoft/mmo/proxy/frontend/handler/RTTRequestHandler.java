package com.jukusoft.mmo.proxy.frontend.handler;

import com.jukusoft.mmo.proxy.frontend.network.ConnState;
import com.jukusoft.mmo.proxy.frontend.network.Connection;
import com.jukusoft.mmo.proxy.frontend.network.GSConnectionManager;
import com.jukusoft.mmo.proxy.frontend.network.MessageListener;
import com.jukusoft.vertx.serializer.annotations.MessageType;
import com.jukusoft.vertx.serializer.annotations.ProtocolVersion;
import io.vertx.core.buffer.Buffer;

/**
* handler for RTT (round trip time) messages to detect ping
*/
@MessageType(type = 0x01, extendedType = 0x02)
@ProtocolVersion(1)
public class RTTRequestHandler implements MessageListener {

    @Override
    public void onMessage(Buffer buffer, ConnState state, Connection clientConn, GSConnectionManager gsConn) {
        //send RTT response
        Buffer msg = Buffer.buffer(4);
        buffer.appendByte((byte) 0x01);
        buffer.appendByte((byte) 0x02);
        buffer.appendShort((short) 1);

        //send message to client
        clientConn.sendRaw(buffer);
    }

}
