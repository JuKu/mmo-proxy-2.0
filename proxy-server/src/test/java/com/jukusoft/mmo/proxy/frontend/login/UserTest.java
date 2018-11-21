package com.jukusoft.mmo.proxy.frontend.login;

import org.junit.Test;

import java.util.ArrayList;

public class UserTest {

    @Test
    public void testConstructor () {
        new User(1, "test", new ArrayList<>());
    }

    @Test (expected = IllegalArgumentException.class)
    public void testNegativeUserIDConstructor () {
        new User(-1, "test", new ArrayList<>());
    }

    @Test (expected = NullPointerException.class)
    public void testNullUsernameConstructor () {
        new User(1, null, new ArrayList<>());
    }

    @Test (expected = IllegalArgumentException.class)
    public void testEmptyUsernameConstructor () {
        new User(1, "", new ArrayList<>());
    }

}
