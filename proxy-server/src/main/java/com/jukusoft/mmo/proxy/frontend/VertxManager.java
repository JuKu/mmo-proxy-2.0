package com.jukusoft.mmo.proxy.frontend;

import com.hazelcast.core.HazelcastInstance;
import com.jukusoft.mmo.engine.shared.config.Config;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;

public class VertxManager {

    //vert.x options
    protected VertxOptions vertxOptions = null;

    //instance of vert.x
    protected Vertx vertx = null;

    //vert.x cluster manager
    protected ClusterManager clusterManager = null;

    //flag, if vertx is initialized
    protected boolean initialized = false;

    protected static final String PROXY_TAG = "Proxy";

    /**
    * default constructor
    */
    public VertxManager() {
        //
    }

    public void init (HazelcastInstance hazelcastInstance) {
        //create new vert.x cluster manager
        this.clusterManager = new HazelcastClusterManager(hazelcastInstance);

        //create new vertx.io options
        this.vertxOptions = new VertxOptions();

        //set thread count
        vertxOptions.setEventLoopPoolSize(Config.getInt(PROXY_TAG, "eventThreads"));
        vertxOptions.setWorkerPoolSize(Config.getInt(PROXY_TAG, "workerThreads"));

        //set thread pool timeouts
        vertxOptions.setMaxEventLoopExecuteTime(Config.getInt(PROXY_TAG, "maxEventLoopExecuteTime"));
        vertxOptions.setMaxWorkerExecuteTime(Config.getInt(PROXY_TAG, "maxWorkerExecuteTime"));

        //use clustered mode of vert.x
        this.vertxOptions.setClustered(true);

        //set cluster manager
        this.vertxOptions.setClusterManager(this.clusterManager);

        //set high availability flag
        this.vertxOptions.setHAEnabled(true);

        //create clustered vertx. instance
        Vertx.clusteredVertx(this.vertxOptions, res -> {
            if (res.succeeded()) {
                initialized = true;
                this.vertx = res.result();
            } else {
                // failed!
                System.exit(1);
            }
        });

        //wait while clustered vertx is initialized
        while (!this.initialized) {
            Thread.yield();
        }
    }

    public void shutdown () {
        this.vertx.close();
    }

    public Vertx getVertx () {
        return this.vertx;
    }

}
