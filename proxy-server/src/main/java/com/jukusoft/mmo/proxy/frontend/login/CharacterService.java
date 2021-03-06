package com.jukusoft.mmo.proxy.frontend.login;

import com.jukusoft.mmo.engine.shared.data.CharacterSlot;
import com.jukusoft.mmo.engine.shared.logger.Log;
import com.jukusoft.mmo.engine.shared.messages.CreateCharacterResponse;
import com.jukusoft.mmo.proxy.frontend.database.Database;
import com.jukusoft.mmo.proxy.frontend.database.RegionMeta;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class CharacterService implements ICharacterService {

    protected static final String SELECT_MY_CHARACTERS = "SELECT * FROM `{prefix}proxy_characters` WHERE `userID` = ?; ";
    protected static final String SELECT_CHARACTER_NAMES = "SELECT * FROM `{prefix}proxy_characters` WHERE `name` = ?; ";
    protected static final String INSERT_CHARACTER = "INSERT INTO `{prefix}proxy_characters` (" +
            "   `cid`, `userID`, `name`, `data`, `current_regionID`, `instanceID`, `shardID`, `activated`" +
            ") VALUES (" +
            "   NULL, ?, ?, ?, ?, ?, ?, '1'" +
            "); ";
    protected static final String CHECK_CID_BELONGS_TO_USER = "SELECT * FROM `{prefix}proxy_characters` WHERE `userID` = ? AND `cid` = ?; ";
    //protected static final String SELECT_CURRENT_REGION = "SELECT * FROM `{prefix}proxy_characters` LEFT JOIN `{prefix}regions` ON `{prefix}characters`.`current_regionID` = `{prefix}regions`.`regionID` WHERE `cid` = ?; ";
    protected static final String SELECT_CURRENT_REGION = "SELECT * FROM `{prefix}proxy_characters` WHERE `cid` = ?; ";

    public static final String LOG_TAG = "CharacterService";

    protected Pattern usernameValidatorPattern = Pattern.compile("[A-Za-z0-9_]+");

    public CharacterService() {
        //
    }

    @Override
    public List<CharacterSlot> listSlotsOfUser(int userID) {
        try (Connection conn = Database.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(Database.replacePrefix(SELECT_MY_CHARACTERS))) {
                //select characters
                stmt.setInt(1, userID);

                try (ResultSet rs = stmt.executeQuery()) {
                    //create new empty list
                    List<CharacterSlot> slots = new ArrayList<>();

                    while (rs.next()) {
                        int cid = rs.getInt("cid");
                        String name = rs.getString("name");
                        String data = rs.getString("data");//json data
                        int current_regionID = rs.getInt("current_regionID");
                        int instanceID = rs.getInt("instanceID");
                        int shardID = rs.getInt("shardID");

                        //create character slot from json
                        CharacterSlot slot = CharacterSlot.createFromJson(cid, name, new JsonObject(data));

                        //add slot to list
                        slots.add(slot);
                    }

                    return slots;
                }
            }
        } catch (SQLException e) {
            Log.w(LOG_TAG, "SQLException while try to get character slots.", e);
            return new ArrayList<>();
        }
    }

    @Override
    public void createCharacter(CharacterSlot character, int userID, Handler<CreateCharacterResponse.CREATE_CHARACTER_RESULT> handler) {
        //first, check if character name already exists
        String name = character.getName();

        //check, if name is valide
        if (!this.usernameValidatorPattern.matcher(name).matches()) {
            //character name is not valide
            handler.handle(CreateCharacterResponse.CREATE_CHARACTER_RESULT.INVALIDE_NAME);

            return;
        }

        //check, if character name already exists
        if (this.existsCharacterName(name)) {
            //character name already exists
            handler.handle(CreateCharacterResponse.CREATE_CHARACTER_RESULT.DUPLICATE_NAME);

            return;
        }

        //create character
        try {
            this.create(character, userID);
        } catch (SQLException e) {
            Log.w(LOG_TAG, "SQLException while trying to create character.", e);
            handler.handle(CreateCharacterResponse.CREATE_CHARACTER_RESULT.SERVER_ERROR);

            return;
        }

        handler.handle(CreateCharacterResponse.CREATE_CHARACTER_RESULT.SUCCESS);
    }

    @Override
    public boolean checkCIDBelongsToPlayer(int cid, int userID) {
        try (Connection conn = Database.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(Database.replacePrefix(CHECK_CID_BELONGS_TO_USER))) {
                //select characters
                stmt.setInt(1, userID);
                stmt.setInt(2, cid);

                try (ResultSet rs = stmt.executeQuery()) {
                    //return, if row exists
                    return rs.next();
                }
            }
        } catch (SQLException e) {
            Log.w(LOG_TAG, "SQLException while check if character (cid: " + cid + ") belongs to player (userID: " + userID + ").", e);
            return false;
        }
    }

    @Override
    public void getCurrentRegionOfCharacter(int cid, Handler<RegionMeta> handler) {
        Log.v(LOG_TAG, "try to get current region of cid '" + cid + "'...");

        try (Connection conn = Database.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(Database.replacePrefix(SELECT_CURRENT_REGION))) {
                //select character
                stmt.setInt(1, cid);

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        int regionID = rs.getInt("current_regionID");
                        int instanceID = rs.getInt("instanceID");

                        RegionMeta region = new RegionMeta(regionID, instanceID);
                        handler.handle(region);

                        return;
                    }
                }
            }
        } catch (SQLException e) {
            Log.w(LOG_TAG, "SQLException while get current region of character: ", e);
            handler.handle(null);
        }
    }

    protected boolean existsCharacterName (String name) {
        try (Connection conn = Database.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(Database.replacePrefix(SELECT_CHARACTER_NAMES))) {
                //select
                stmt.setString(1, name);

                try (ResultSet rs = stmt.executeQuery()) {
                    //return if select statement has one or more results (rows)
                    return rs.next();
                }
            }
        } catch (SQLException e) {
            Log.w(LOG_TAG, "SQLException while trying to check if character name already exists.", e);
            return true;
        }
    }

    protected void create (CharacterSlot character, int userID) throws SQLException {
        //create character in database
        try (Connection conn = Database.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(Database.replacePrefix(INSERT_CHARACTER))) {
                stmt.setInt(1, userID);
                stmt.setString(2, character.getName());
                stmt.setString(3, character.toJson().encode());

                //TODO: load start regionID & instanceID from global settings

                //region & instance id (and shardID), -1 so it will be set from proxy server automatically
                stmt.setInt(4, 1);
                stmt.setInt(5, 1);
                stmt.setInt(6, 1);

                stmt.executeUpdate();
            }
        }
    }

}
