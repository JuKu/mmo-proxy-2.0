package com.jukusoft.mmo.proxy.frontend;

import com.hazelcast.config.CacheSimpleConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.jukusoft.mmo.engine.shared.config.Config;
import com.jukusoft.mmo.engine.shared.logger.Log;
import com.jukusoft.mmo.engine.shared.logger.LogWriter;
import com.jukusoft.mmo.engine.shared.utils.Utils;
import com.jukusoft.mmo.engine.shared.version.Version;
import com.jukusoft.mmo.proxy.frontend.log.HzLogger;
import com.jukusoft.mmo.proxy.frontend.utils.EncryptionUtils;
import com.jukusoft.mmo.proxy.frontend.utils.HazelcastFactory;
import io.vertx.core.Vertx;

import java.io.File;
import java.io.IOException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerMain {

    protected static final String VERSION_TAG = "Version";
    protected static final String CONFIG_TAG = "Config";
    protected static final String HAZELCAST_TAG = "Hazelcast";

    public static void main (String[] args) {
        //start game
        try {
            start(args);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    protected static void start (String[] args) throws Exception {
        //load logger config
        try {
            Config.load(new File("./config/logger.cfg"));

            //initialize logger
            Log.init();
        } catch (IOException e) {
            Logger.getAnonymousLogger().log(Level.SEVERE, "Couldn't initialize config and logger!", e);
            System.exit(0);
        }

        Log.i("Startup", "Started Proxy Server.");

        //set global version
        Version version = new Version(ServerMain.class);
        Version.setInstance(new Version(ServerMain.class));

        //print proxy server version information
        Utils.printSection("Game Version");
        Log.i(VERSION_TAG, "Version: " + version.getVersion());
        Log.i(VERSION_TAG, "Build: " + version.getRevision());
        Log.i(VERSION_TAG, "Build JDK: " + version.getBuildJdk());
        Log.i(VERSION_TAG, "Build Time: " + version.getBuildTime());
        Log.i(VERSION_TAG, "Vendor ID: " + (!version.getVendor().equals("n/a") ? version.getVendor() : version.getVendorID()));

        //print java version
        Utils.printSection("Java Version");
        Log.i("Java", "Java Vendor: " + System.getProperty("java.vendor"));
        Log.i("Java", "Java Vendor URL: " + System.getProperty("java.vendor.url"));
        Log.i("Java", "Java Version: " + System.getProperty("java.version"));

        //load all config files
        Utils.printSection("Configuration & Init");
        Log.i(CONFIG_TAG, "load configs in directory 'config/'...");
        Config.loadDir(new File("./config/"));

        //overrides config with params
        for (String param : args) {
            if (param.startsWith("-Config:")) {
                param = param.substring(8);
                String[] array = param.split("=");

                if (array.length < 2) {
                    throw new IllegalArgumentException("invalide parameter, -Config parameters requires a '=' to set config value.");
                }

                String[] array1 = array[0].split("\\.");

                if (array1.length < 2) {
                    throw new IllegalArgumentException("invalide parameter, -Config parameters requires a '.' in option key to use section (current key: '" + array[0] + "').");
                }

                Log.d(CONFIG_TAG, "set value '" + array[0] + "' = '" + array[1] + "' manually.");

                //set config value
                Config.set(array1[0], array1[1], array[1]);
            }
        }

        Log.i(HAZELCAST_TAG, "create new hazelcast instance...");
        HazelcastInstance hazelcastInstance = createHazelcastInstance();

        Log.i(HAZELCAST_TAG, "hazelcast started successfully.");

        //create and attach hazelcast logger
        Log.i("Logging", "enable hazelcast cluster logging...");
        HzLogger hzLogger = new HzLogger(hazelcastInstance);
        LogWriter.attachListener(hzLogger);

        //generate RSA key pair
        Log.i("Security", "generate RSA key pair...");
        KeyPair keyPair = null;

        try {
            keyPair = EncryptionUtils.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            Log.e("Security", "NoSuchAlgorithmException: ", e);
            Log.shutdown();
            Thread.sleep(5000);
            System.exit(1);
        }

        //create vert.x instance
        Log.i("Vertx", "Create vertx.io instance...");
        VertxManager vertxManager = new VertxManager();
        vertxManager.init(hazelcastInstance);
        Vertx vertx = vertxManager.getVertx();

        //TODO: add code here

        //list currently active threads
        Set<Thread> threadSet = Thread.getAllStackTraces().keySet();

        //print log
        Utils.printSection("Shutdown");
        Log.i("Shutdown", "Shutdown now.");

        //shutdown logger and write all remaining logs to file
        Log.shutdown();

        //wait 200ms, so logs can be written to file
        Thread.sleep(200);

        //check, if there are other active threads, except the main thread
        if (threadSet.size() > 1) {
            System.err.println("Shutdown: waiting for active threads:");

            for (Thread thread : threadSet) {
                System.err.println(" - " + thread.getName());
            }

            //wait 3 seconds, then force shutdown
            Thread.sleep(2000);
        }

        System.err.println("shutdown JVM now.");

        //force JVM shutdown
        if (Config.forceExit) {
            System.exit(0);
        }
    }

    public static HazelcastInstance createHazelcastInstance () {
        if (Config.getBool(HAZELCAST_TAG, "standalone")) {
            //create an new hazelcast instance
            com.hazelcast.config.Config config = new com.hazelcast.config.Config();

            //disable hazelcast logging
            config.setProperty("hazelcast.logging.type", "none");

            CacheSimpleConfig cacheConfig = new CacheSimpleConfig();
            config.getCacheConfigs().put("session-cache", cacheConfig);

            return Hazelcast.newHazelcastInstance(config);
        } else {
            return HazelcastFactory.createHzInstanceFromConfig();
        }
    }

}
