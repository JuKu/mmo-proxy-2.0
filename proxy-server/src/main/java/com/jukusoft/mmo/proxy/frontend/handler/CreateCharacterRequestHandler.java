package com.jukusoft.mmo.proxy.frontend.handler;

import com.jukusoft.mmo.engine.shared.data.CharacterSlot;
import com.jukusoft.mmo.engine.shared.logger.Log;
import com.jukusoft.mmo.engine.shared.memory.Pools;
import com.jukusoft.mmo.engine.shared.messages.CreateCharacterRequest;
import com.jukusoft.mmo.engine.shared.messages.CreateCharacterResponse;
import com.jukusoft.mmo.proxy.frontend.login.CharacterService;
import com.jukusoft.mmo.proxy.frontend.login.ICharacterService;
import com.jukusoft.mmo.proxy.frontend.network.ConnState;
import com.jukusoft.mmo.proxy.frontend.network.Connection;
import com.jukusoft.mmo.proxy.frontend.network.GSConnectionManager;
import com.jukusoft.mmo.proxy.frontend.network.MessageListener;
import com.jukusoft.vertx.serializer.Serializer;
import com.jukusoft.vertx.serializer.annotations.MessageType;
import com.jukusoft.vertx.serializer.annotations.ProtocolVersion;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;

@MessageType(type = 0x01, extendedType = 0x05)
@ProtocolVersion(1)
public class CreateCharacterRequestHandler implements MessageListener {

    protected static final String LOG_TAG = "Login";

    protected ICharacterService characterService = null;

    public CreateCharacterRequestHandler () {
        this.characterService = new CharacterService();
    }

    @Override
    public void onMessage(Buffer buffer, ConnState state, Connection clientConn, GSConnectionManager gsConn) throws Exception {
        Log.i(LOG_TAG, "received create character request.");

        //first check, if user is logged in
        //ignore this request, if user isn't logged in
        if (!state.isLoggedIn()) {
            Log.d(LOG_TAG, "Cannot create character, because user isnt logged in.");
            return;
        }

        //unserialize message
        CreateCharacterRequest request = Serializer.unserialize(buffer, CreateCharacterRequest.class);

        //convert json string to json object
        JsonObject json = new JsonObject(request.jsonStr);
        String name = json.getString("name");

        //convert json object to character
        CharacterSlot slot = CharacterSlot.createFromJson(Integer.MAX_VALUE, name, json);

        //try to create character
        this.characterService.createCharacter(slot, state.getUserID(), resultCode -> {
            Log.i(LOG_TAG, "send create character result code: " + resultCode.name());

            //send response back to client
            CreateCharacterResponse response = Pools.get(CreateCharacterResponse.class);
            response.setResult(resultCode);
            clientConn.send(response);
        });
    }

}
