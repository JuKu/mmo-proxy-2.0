package com.jukusoft.mmo.proxy.frontend.network;

import com.jukusoft.mmo.engine.shared.config.Config;

public class ConnState {

    protected boolean isLoggedIn = false;
    protected int userID = -1;
    protected String username = "";

    //how often has user tried to login?
    protected int loginRetries = 0;

    public ConnState () {
        //
    }

    public boolean isLoggedIn () {
        return this.isLoggedIn;
    }

    public void setLoggedIn (int userID, String username) {
        this.isLoggedIn = true;
        this.userID = userID;
        this.username = username;
    }

    public int getUserID() {
        return this.userID;
    }

    public String getUsername() {
        return this.username;
    }

    public boolean retryLogin () {
        if (this.loginRetries > Config.getInt("Security", "maxLoginRetries")) {
            //user isn't allowed to login again
            return false;
        }

        //user is allowed to login again
        this.loginRetries++;
        return true;
    }

    public void cleanUp () {
        //
    }

}
