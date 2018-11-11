package com.jukusoft.mmo.proxy.frontend.handler;

import com.jukusoft.mmo.engine.shared.logger.Log;
import com.jukusoft.mmo.engine.shared.memory.Pools;
import com.jukusoft.mmo.engine.shared.messages.LoginRequest;
import com.jukusoft.mmo.engine.shared.messages.LoginResponse;
import com.jukusoft.mmo.engine.shared.utils.EncryptionUtils;
import com.jukusoft.mmo.proxy.frontend.ldap.LDAPLogin;
import com.jukusoft.mmo.proxy.frontend.network.ConnState;
import com.jukusoft.mmo.proxy.frontend.network.Connection;
import com.jukusoft.mmo.proxy.frontend.network.GSConnectionManager;
import com.jukusoft.mmo.proxy.frontend.network.MessageListener;
import com.jukusoft.vertx.serializer.Serializer;
import com.jukusoft.vertx.serializer.annotations.MessageType;
import com.jukusoft.vertx.serializer.annotations.ProtocolVersion;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;

import java.security.PrivateKey;

@MessageType(type = 0x01, extendedType = 0x03)
@ProtocolVersion(1)
public class LoginRequestHandler implements MessageListener {

    public static final String LOG_TAG = "Login";

    protected final LDAPLogin ldapLogin;
    protected final PrivateKey privateKey;

    public LoginRequestHandler (PrivateKey privateKey) {
        this.ldapLogin = new LDAPLogin();
        this.privateKey = privateKey;
    }

    @Override
    public void onMessage(Buffer buffer, ConnState state, Connection clientConn, GSConnectionManager gsConn) throws Exception {
        LoginRequest req = Serializer.unserialize(buffer, LoginRequest.class);
        Log.i(LOG_TAG, "login request received.");

        //check, if user is allowed to login again or has reached max login retries
        if (!state.retryLogin()) {
            Log.w(LOG_TAG, "[" + clientConn.host() + ":" + clientConn.port() + "] user has tried to login too often, close connection now.");
            clientConn.close();

            //TODO: block user temporary in database

            return;
        }

        LoginResponse response = Pools.get(LoginResponse.class);

        String username = null;
        String password = null;

        try {
            //decrypt user credentials
            String jsonStr = EncryptionUtils.decrypt(privateKey, req.encryptedData);
            JsonObject json = new JsonObject(jsonStr);

            //get username and password
            username = json.getString("username");
            password = json.getString("password");
        } catch (Exception e) {
            Log.w(LOG_TAG, "[" + clientConn.host() + ":" + clientConn.port() + "] Exception while decrypting login credentials: ", e);

            //internal server error
            response.userID = -1;
            clientConn.send(response);
            return;
        }

        //try to login
        int userID = this.ldapLogin.login(username, password, clientConn.host());

        if (userID > 0) {
            //login successfully
            Log.i(LOG_TAG, "login successful for user '" + username + "'.");

            //update state
            state.setLoggedIn(userID, username);

            response.userID = userID;
            response.username = "n/a";
        } else {
            //wrong credentials
            Log.i(LOG_TAG, "login failed for user '" + username + "', because credentials are wrong.");
            response.userID = 0;
            response.username = "Guest";
        }

        clientConn.send(response);
    }

}
