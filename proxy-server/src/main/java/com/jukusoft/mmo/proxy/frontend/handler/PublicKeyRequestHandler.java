package com.jukusoft.mmo.proxy.frontend.handler;

import com.jukusoft.mmo.engine.shared.logger.Log;
import com.jukusoft.mmo.engine.shared.messages.PublicKeyResponse;
import com.jukusoft.mmo.proxy.frontend.network.ConnState;
import com.jukusoft.mmo.proxy.frontend.network.Connection;
import com.jukusoft.mmo.proxy.frontend.network.GSConnectionManager;
import com.jukusoft.mmo.proxy.frontend.network.MessageListener;
import com.jukusoft.vertx.serializer.annotations.MessageType;
import com.jukusoft.vertx.serializer.annotations.ProtocolVersion;
import io.vertx.core.buffer.Buffer;

import java.security.PublicKey;

@MessageType(type = 0x01, extendedType = 0x01)
@ProtocolVersion(1)
public class PublicKeyRequestHandler implements MessageListener {

    protected final PublicKey publicKey;

    public PublicKeyRequestHandler (PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    @Override
    public void onMessage(Buffer buffer, ConnState state, Connection clientConn, GSConnectionManager gsConn) throws Exception {
        //log event
        Log.d("PublicKey", "received public key request.");

        //create new message
        PublicKeyResponse response = new PublicKeyResponse(publicKey);

        //log event
        Log.d("PublicKey", "send public key request.");

        //send public key to client
        clientConn.send(response);
    }

}
