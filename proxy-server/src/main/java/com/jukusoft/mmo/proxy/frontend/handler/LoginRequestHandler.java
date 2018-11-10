package com.jukusoft.mmo.proxy.frontend.handler;

import com.jukusoft.mmo.proxy.frontend.network.ConnState;
import com.jukusoft.mmo.proxy.frontend.network.Connection;
import com.jukusoft.mmo.proxy.frontend.network.GSConnectionManager;
import com.jukusoft.mmo.proxy.frontend.network.MessageListener;
import com.jukusoft.vertx.serializer.annotations.MessageType;
import com.jukusoft.vertx.serializer.annotations.ProtocolVersion;
import io.vertx.core.buffer.Buffer;

@MessageType(type = 0x01, extendedType = 0x03)
@ProtocolVersion(1)
public class LoginRequestHandler implements MessageListener {

    @Override
    public void onMessage(Buffer buffer, ConnState state, Connection clientConn, GSConnectionManager gsConn) throws Exception {
        //
    }

}
