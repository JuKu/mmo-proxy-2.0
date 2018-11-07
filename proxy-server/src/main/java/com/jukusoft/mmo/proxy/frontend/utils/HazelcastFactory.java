package com.jukusoft.mmo.proxy.frontend.utils;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.jukusoft.mmo.engine.shared.config.Config;

public class HazelcastFactory {

    protected HazelcastFactory() {
        //
    }

    public static HazelcastInstance getHazelcastInstance(String ip, int port, String user, String password) {
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.getGroupConfig().setName(user).setPassword(password);
        clientConfig.getNetworkConfig().addAddress(new String[]{ip + ":" + port});
        HazelcastInstance hazelcastInstance = HazelcastClient.newHazelcastClient(clientConfig);
        return hazelcastInstance;
    }

    public static HazelcastInstance createHzInstanceFromConfig () {
        String ip = Config.get("Hazelcast", "ip");
        int port = Config.getInt("Hazelcast", "port");
        String user = Config.get("Hazelcast", "user");
        String password = Config.get("Hazelcast", "password");

        return getHazelcastInstance(ip, port, user, password);
    }

}
