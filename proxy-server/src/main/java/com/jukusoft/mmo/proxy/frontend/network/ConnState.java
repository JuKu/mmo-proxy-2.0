package com.jukusoft.mmo.proxy.frontend.network;

import com.jukusoft.mmo.engine.shared.config.Config;
import com.jukusoft.mmo.proxy.frontend.login.User;

public class ConnState {

    protected boolean isLoggedIn = false;
    protected User user = null;
    protected int cid = 0;

    //how often has user tried to login?
    protected int loginRetries = 0;

    public ConnState () {
        //
    }

    public boolean isLoggedIn () {
        return this.isLoggedIn;
    }

    public void setLoggedIn (User user) {
        this.isLoggedIn = true;
        this.user = user;
    }

    public int getUserID() {
        if (this.user == null) {
            return -1;
        }

        return this.user.getUserID();
    }

    public String getUsername() {
        if (this.user == null) {
            return "";
        }

        return this.user.getUsername();
    }

    public User getUser () {
        return this.user;
    }

    public boolean retryLogin () {
        if (this.loginRetries >= Config.getInt("Security", "maxLoginRetries")) {
            //user isn't allowed to login again
            return false;
        }

        //user is allowed to login again
        this.loginRetries++;
        return true;
    }

    public boolean isGameMaster () {
        //TODO: add code here

        return false;
    }

    public int getCID() {
        return this.cid;
    }

    public void setCID(int cid) {
        this.cid = cid;
    }

    public void cleanUp () {
        //
    }

}
