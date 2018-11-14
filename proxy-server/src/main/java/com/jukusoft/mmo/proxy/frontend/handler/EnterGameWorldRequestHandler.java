package com.jukusoft.mmo.proxy.frontend.handler;

import com.jukusoft.mmo.engine.shared.logger.Log;
import com.jukusoft.mmo.engine.shared.messages.EnterGameWorldRequest;
import com.jukusoft.mmo.proxy.frontend.network.ConnState;
import com.jukusoft.mmo.proxy.frontend.network.Connection;
import com.jukusoft.mmo.proxy.frontend.network.GSConnectionManager;
import com.jukusoft.mmo.proxy.frontend.network.MessageListener;
import com.jukusoft.vertx.serializer.Serializer;
import com.jukusoft.vertx.serializer.annotations.MessageType;
import com.jukusoft.vertx.serializer.annotations.ProtocolVersion;
import io.vertx.core.buffer.Buffer;

@MessageType(type = 0x01, extendedType = 0x06)
@ProtocolVersion(1)
public class EnterGameWorldRequestHandler implements MessageListener {

    protected static final String LOG_TAG = "Login";

    @Override
    public void onMessage(Buffer buffer, ConnState state, Connection clientConn, GSConnectionManager gsConn) throws Exception {
        Log.i(LOG_TAG, "enter game world request received.");

        //ignore this request, if user isn't logged in
        if (!state.isLoggedIn()) {
            Log.w(LOG_TAG, "Player cannot enter game world, because user isnt logged in.");
            return;
        }

        EnterGameWorldRequest request = Serializer.unserialize(buffer, EnterGameWorldRequest.class);
        int cid = request.cid;

        Log.i(LOG_TAG, "try to enter game world with cid " + cid + " from userID " + state.getUserID() + "...");

        //TODO: check, if character belongs to player OR user has gamemaster permission

        //TODO: get User permissions from DB

        //TODO: add code here
    }

}
