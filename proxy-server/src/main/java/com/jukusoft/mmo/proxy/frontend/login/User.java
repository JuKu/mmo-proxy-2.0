package com.jukusoft.mmo.proxy.frontend.login;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class User {

    //data from database
    protected final int userID;
    protected final String username;

    //ldap groups for permissions
    protected final List<String> groups;

    public User (int userID, String username, List<String> groups) {
        this.userID = userID;
        this.username = username;
        this.groups = groups;
    }

    public int getUserID() {
        return userID;
    }

    public String getUsername() {
        return username;
    }

    public List<String> listGroups() {
        return Collections.unmodifiableList(this.groups);
    }

}
