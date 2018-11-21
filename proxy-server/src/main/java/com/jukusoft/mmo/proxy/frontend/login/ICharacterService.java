package com.jukusoft.mmo.proxy.frontend.login;

import com.jukusoft.mmo.engine.shared.data.CharacterSlot;
import com.jukusoft.mmo.engine.shared.messages.CreateCharacterResponse;
import com.jukusoft.mmo.proxy.frontend.database.RegionMeta;
import io.vertx.core.Handler;

import java.util.List;

public interface ICharacterService {

    public List<CharacterSlot> listSlotsOfUser(int userID);

    public void createCharacter (CharacterSlot character, int userID, Handler<CreateCharacterResponse.CREATE_CHARACTER_RESULT> handler);

    public boolean checkCIDBelongsToPlayer(int cid, int userID);

    public void getCurrentRegionOfCharacter (int cid, Handler<RegionMeta> handler);

}
