package com.jukusoft.mmo.proxy.frontend;

public class Const {

    public static final boolean[] PROXY_HANDLER_TYPES = new boolean[256];

    static {
        for (int i = 0; i < PROXY_HANDLER_TYPES.length; i++) {
            PROXY_HANDLER_TYPES[i] = false;
        }

        //only type 0x01 has to be handled from proxy server
        PROXY_HANDLER_TYPES[0x01] = true;
    }

}
