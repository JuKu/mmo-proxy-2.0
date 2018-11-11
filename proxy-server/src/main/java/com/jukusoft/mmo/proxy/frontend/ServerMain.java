package com.jukusoft.mmo.proxy.frontend;

import com.hazelcast.config.CacheSimpleConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.jukusoft.mmo.engine.shared.config.Config;
import com.jukusoft.mmo.engine.shared.logger.Log;
import com.jukusoft.mmo.engine.shared.logger.LogWriter;
import com.jukusoft.mmo.engine.shared.messages.LoginRequest;
import com.jukusoft.mmo.engine.shared.messages.LoginResponse;
import com.jukusoft.mmo.engine.shared.messages.PublicKeyRequest;
import com.jukusoft.mmo.engine.shared.messages.PublicKeyResponse;
import com.jukusoft.mmo.engine.shared.utils.Utils;
import com.jukusoft.mmo.engine.shared.version.Version;
import com.jukusoft.mmo.proxy.frontend.database.Database;
import com.jukusoft.mmo.proxy.frontend.database.DatabaseUpgrader;
import com.jukusoft.mmo.proxy.frontend.database.MySQLConfig;
import com.jukusoft.mmo.proxy.frontend.handler.CharacterListRequestHandler;
import com.jukusoft.mmo.proxy.frontend.handler.LoginRequestHandler;
import com.jukusoft.mmo.proxy.frontend.handler.PublicKeyRequestHandler;
import com.jukusoft.mmo.proxy.frontend.handler.RTTRequestHandler;
import com.jukusoft.mmo.proxy.frontend.log.HzLogger;
import com.jukusoft.mmo.proxy.frontend.network.ProxyServer;
import com.jukusoft.mmo.proxy.frontend.utils.EncryptionUtils;
import com.jukusoft.mmo.proxy.frontend.utils.HazelcastFactory;
import com.jukusoft.vertx.serializer.TypeLookup;
import io.vertx.core.Vertx;
import org.apache.log4j.BasicConfigurator;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerMain {

    protected static final String VERSION_TAG = "Version";
    protected static final String CONFIG_TAG = "Config";
    protected static final String HAZELCAST_TAG = "Hazelcast";
    protected static final String NETWORK_TAG = "Network";
    protected static final String DATABASE_TAG = "Database";

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
        //configure log4j for netty and hide all netty logs
        BasicConfigurator.configure(new DummyAppender());

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

        Log.i(DATABASE_TAG, "initialize MySQL config...");

        //load mysql config
        MySQLConfig mySQLConfig = new MySQLConfig();
        mySQLConfig.load();

        Log.i(DATABASE_TAG, "execute database upgrader...");

        //create or upgrade database schema
        DatabaseUpgrader databaseUpgrader = new DatabaseUpgrader(mySQLConfig);
        databaseUpgrader.migrate();
        databaseUpgrader.printInfo(DATABASE_TAG);

        Log.i(DATABASE_TAG, "initialize database connection...");

        //initialize database
        Database.init(mySQLConfig);

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

        //start proxy server
        ProxyServer proxyServer = new ProxyServer(vertx);
        String host = Config.get("Proxy", "host");
        int port = Config.getInt("Proxy", "port");
        Log.i(NETWORK_TAG, "start proxy server on host " + host + " on port " + port + ".");

        AtomicBoolean started = new AtomicBoolean(false);
        AtomicBoolean error = new AtomicBoolean(false);

        //register message types
        TypeLookup.register(PublicKeyRequest.class);
        TypeLookup.register(PublicKeyResponse.class);
        TypeLookup.register(LoginRequest.class);
        TypeLookup.register(LoginResponse.class);

        //register handlers
        proxyServer.addMessageListener(new PublicKeyRequestHandler(keyPair.getPublic()));
        proxyServer.addMessageListener(new RTTRequestHandler());
        proxyServer.addMessageListener(new LoginRequestHandler(keyPair.getPrivate()));
        proxyServer.addMessageListener(new CharacterListRequestHandler());

        proxyServer.start(host, port, res -> {
            started.set(true);

            if (res.succeeded()) {
                Log.i(NETWORK_TAG, "proxy server started successfully.");
                error.set(false);
            } else {
                Log.e(NETWORK_TAG, "Couldn't start proxy server: ", res.cause());
                error.set(true);
            }
        });

        //wait while server is starting
        while (!started.get()) {
            Thread.sleep(100);
        }

        Log.i(NETWORK_TAG, "proxy server is running yet.");

        if (!error.get()) {
            Utils.printSection("Running");

            //wait
            Thread thread = Thread.currentThread();
            thread.setName("main");

            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            Log.i("CLI", "command line input is accepted yet. Quit server with 'quit' and ENTER.");

            while (!Thread.interrupted()) {
                //read line
                String line = reader.readLine();

                if (line.equals("quit") || line.equals("exit")) {
                    break;
                }

                System.out.println("Unsupported command: " + line);
            }
        }

        /**
        * shutdown process
        */

        //list currently active threads
        Set<Thread> threadSet = Thread.getAllStackTraces().keySet();

        //print log
        Utils.printSection("Shutdown");
        Log.i("Shutdown", "Shutdown now.");

        //shutdown vertx
        vertxManager.shutdown();

        //shutdown logger and write all remaining logs to file
        Log.shutdown();

        //wait 200ms, so logs can be written to file
        Thread.sleep(500);

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
