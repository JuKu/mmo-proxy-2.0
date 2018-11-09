package com.jukusoft.mmo.proxy.frontend.handler;

import com.jukusoft.mmo.engine.shared.messages.PublicKeyResponse;
import com.jukusoft.mmo.proxy.frontend.network.ConnState;
import com.jukusoft.mmo.proxy.frontend.network.Connection;
import com.jukusoft.mmo.proxy.frontend.network.GSConnectionManager;
import com.jukusoft.mmo.proxy.frontend.network.MessageListener;
import com.jukusoft.mmo.proxy.frontend.utils.EncryptionUtils;
import com.jukusoft.vertx.connection.stream.BufferStream;
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
        //convert public key to byte array
        //byte[] array = EncryptionUtils.convertPublicKeyToByteArray(publicKey);

        PublicKeyResponse response = new PublicKeyResponse(publicKey);

        //send public key to client
        clientConn.send(response);
    }

}
