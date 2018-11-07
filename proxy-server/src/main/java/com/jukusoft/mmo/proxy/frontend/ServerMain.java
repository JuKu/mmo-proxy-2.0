package com.jukusoft.mmo.proxy.frontend;

import com.jukusoft.mmo.engine.shared.config.Config;
import com.jukusoft.mmo.engine.shared.logger.Log;
import com.jukusoft.mmo.engine.shared.utils.Utils;
import com.jukusoft.mmo.engine.shared.version.Version;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerMain {

    protected static final String VERSION_TAG = "Version";

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

        Log.i("Startup", "Started Game Engine.");

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

}
