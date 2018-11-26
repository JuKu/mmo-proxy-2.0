package com.jukusoft.mmo.proxy.frontend.handler;

import com.jukusoft.mmo.engine.shared.config.Config;
import com.jukusoft.mmo.engine.shared.logger.Log;
import com.jukusoft.mmo.engine.shared.memory.Pools;
import com.jukusoft.mmo.engine.shared.messages.EnterGameWorldRequest;
import com.jukusoft.mmo.engine.shared.messages.EnterGameWorldResponse;
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

@MessageType(type = 0x01, extendedType = 0x06)
@ProtocolVersion(1)
public class EnterGameWorldRequestHandler implements MessageListener {

    protected static final String LOG_TAG = "Login";
    protected final ICharacterService characterService;

    public EnterGameWorldRequestHandler () {
        this.characterService = new CharacterService();
    }

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

        //create response
        EnterGameWorldResponse response = Pools.get(EnterGameWorldResponse.class);

        //check, if character belongs to player OR user has gamemaster permission
        if (!this.characterService.checkCIDBelongsToPlayer(cid, state.getUserID()) && !state.isGameMaster()) {
            //character doesn't belongs to this player and user isn't allowed to play all players

            //send error back to client
            response.setResult(EnterGameWorldResponse.RESULT_CODE.CHARACTER_DOESNT_BELONGS_TO_USER);
            clientConn.send(response);
            return;
        }

        //set CID
        state.setCID(cid);

        //check, if user is allowed to play
        if (!state.getUser().hasGroup(Config.get("Security", "playersGroup"))) {
            //send error back to client
            response.setResult(EnterGameWorldResponse.RESULT_CODE.NO_PERMISSION_TO_PLAY);
            clientConn.send(response);
            return;
        }

        //get current region of character
        this.characterService.getCurrentRegionOfCharacter(cid, res -> {
            long regionID = res.getRegionID();
            int instanceID = res.getInstanceID();

            //open connection to region server
            gsConn.requestJoin(regionID, instanceID, success -> {
                if (!success) {
                    Log.w(LOG_TAG, "player with cid '" + cid + "' couldn't join region " + regionID + ", instanceID: " + instanceID + ", maybe zonekeeper is down?");

                    //send error message back to client
                    response.setResult(EnterGameWorldResponse.RESULT_CODE.SERVER_ERROR);
                    clientConn.send(response);

                    return;
                }

                Log.v(LOG_TAG, "player with cid '" + cid + "' joined region " + regionID + ", instanceID: " + instanceID + ".");

                //send message back to client so client can show loading screen now
                response.setResult(EnterGameWorldResponse.RESULT_CODE.SUCCESS);
                clientConn.send(response);
            });
        });
    }

}
