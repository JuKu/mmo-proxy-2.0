package com.jukusoft.mmo.proxy.frontend.network;

import com.jukusoft.mmo.engine.shared.config.Config;
import com.jukusoft.mmo.proxy.frontend.login.User;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class ConnStateTest {

    @Test
    public void testConstructor () {
        ConnState state = new ConnState();

        //check initial values
        assertEquals(false, state.isLoggedIn());
        assertEquals(-1, state.getUserID());
        assertEquals("", state.getUsername());
    }

    @Test
    public void testSetLoggedIn () {
        ConnState state = new ConnState();
        state.setLoggedIn(new User(1, "test", new ArrayList<>()));

        assertEquals(true, state.isLoggedIn());
        assertEquals(2, state.getUserID());
        assertEquals("test", state.getUsername());
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
    public void testCleanUp () {
        ConnState state = new ConnState();
        state.cleanUp();
    }

}
