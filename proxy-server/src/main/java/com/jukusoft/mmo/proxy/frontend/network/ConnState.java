package com.jukusoft.mmo.proxy.frontend.network;

public class ConnState {

    protected boolean isLoggedIn = false;
    protected int userID = -1;
    protected String username = "";

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

    public void cleanUp () {
        //
    }

}
