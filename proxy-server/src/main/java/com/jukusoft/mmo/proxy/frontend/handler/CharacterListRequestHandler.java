package com.jukusoft.mmo.proxy.frontend.handler;

import com.jukusoft.mmo.engine.shared.data.CharacterSlot;
import com.jukusoft.mmo.engine.shared.logger.Log;
import com.jukusoft.mmo.engine.shared.memory.Pools;
import com.jukusoft.mmo.engine.shared.messages.CharacterListResponse;
import com.jukusoft.mmo.proxy.frontend.login.CharacterService;
import com.jukusoft.mmo.proxy.frontend.login.ICharacterService;
import com.jukusoft.mmo.proxy.frontend.network.ConnState;
import com.jukusoft.mmo.proxy.frontend.network.Connection;
import com.jukusoft.mmo.proxy.frontend.network.GSConnectionManager;
import com.jukusoft.mmo.proxy.frontend.network.MessageListener;
import com.jukusoft.vertx.serializer.annotations.MessageType;
import com.jukusoft.vertx.serializer.annotations.ProtocolVersion;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

@MessageType(type = 0x01, extendedType = 0x04)
@ProtocolVersion(1)
public class CharacterListRequestHandler implements MessageListener {

    protected static final String LOG_TAG = "Login";

    protected ICharacterService characterService = null;

    public CharacterListRequestHandler () {
        this.characterService = new CharacterService();
    }

    @Override
    public void onMessage(Buffer buffer, ConnState state, Connection clientConn, GSConnectionManager gsConn) throws Exception {
        Log.i(LOG_TAG, "received character list request.");

        //ignore this request, if user isn't logged in
        if (!state.isLoggedIn()) {
            Log.d(LOG_TAG, "Cannot send character slots, because user isnt logged in.");
            return;
        }

        //send response to client
        Log.i(LOG_TAG, "[user: " + state.getUsername() + "] send character slots to client...");
        CharacterListResponse response = Pools.get(CharacterListResponse.class);

        JsonObject json = new JsonObject();
        JsonArray array = new JsonArray();

        for (CharacterSlot slot : this.characterService.listSlotsOfUser(state.getUserID())) {
            JsonObject json1 = slot.toJson();
            array.add(json1);
        }

        json.put("slots", array);

        //convert json object to string
        response.jsonStr = json.encode();

        clientConn.send(response);
    }

}
