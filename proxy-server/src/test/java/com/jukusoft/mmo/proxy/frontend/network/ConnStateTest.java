package com.jukusoft.mmo.proxy.frontend.network;

import com.jukusoft.mmo.engine.shared.config.Config;
import com.jukusoft.mmo.proxy.frontend.login.User;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ConnStateTest {

    @Test
    public void testConstructor () {
        ConnState state = new ConnState();

        //check initial values
        assertEquals(false, state.isLoggedIn());
        assertEquals(-1, state.getUserID());
        assertEquals("", state.getUsername());
        assertNull(state.getUser());
        assertEquals(false, state.isGameMaster());
        assertEquals(-1, state.getCID());
    }

    @Test
    public void testSetLoggedIn () {
        Config.set("Security", "gamemasterGroup", "gamemaster");

        ConnState state = new ConnState();

        List<String> groups = new ArrayList<>();
        state.setLoggedIn(new User(2, "test", groups));

        assertEquals(true, state.isLoggedIn());
        assertEquals(2, state.getUserID());
        assertEquals("test", state.getUsername());
        assertEquals(false, state.isGameMaster());

        //cleanUp config
        Config.clear();
    }

    @Test
    public void testSetLoggedInGameMaster () {
        Config.set("Security", "gamemasterGroup", "gamemaster");

        ConnState state = new ConnState();

        List<String> groups = new ArrayList<>();
        groups.add("gamemaster");
        state.setLoggedIn(new User(2, "test", groups));

        assertEquals(true, state.isLoggedIn());
        assertEquals(2, state.getUserID());
        assertEquals("test", state.getUsername());
        assertEquals(true, state.isGameMaster());

        //cleanUp config
        Config.clear();
    }

    @Test
    public void testRetryLogin () {
        Config.set("Security", "maxLoginRetries", "3");

        ConnState state = new ConnState();
        assertEquals(true, state.retryLogin());
        assertEquals(true, state.retryLogin());
        assertEquals(true, state.retryLogin());
        assertEquals(false, state.retryLogin());

        for (int i = 0; i < 10; i++) {
            assertEquals(false, state.retryLogin());
        }
    }

    @Test
    public void testSetAndGetCID () {
        ConnState state = new ConnState();

        assertEquals(-1, state.getCID());

        state.setCID(2);
        assertEquals(2, state.getCID());
    }

    @Test
    public void testCleanUp () {
        ConnState state = new ConnState();
        state.cleanUp();
    }

}
