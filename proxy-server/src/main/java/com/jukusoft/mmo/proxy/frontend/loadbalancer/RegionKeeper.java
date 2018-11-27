package com.jukusoft.mmo.proxy.frontend.loadbalancer;

import com.hazelcast.core.HazelcastInstance;
import com.jukusoft.mmo.engine.shared.logger.Log;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;

import java.util.Objects;

/**
 * the region keeper is responsible for assign regions to game servers, if the region isn't already running.
 * This class is also a load balancer for regions.
*/
public class RegionKeeper {

    protected static final String LOG_TAG = "RegionLB";
    protected static final String EVENT_BUS_ADDRESS = "region-manager::get()";

    protected final Vertx vertx;
    protected final HazelcastInstance hazelcastInstance;

    protected MessageConsumer<String> getMessageConsumer = null;

    /**
    * default constructor
    */
    public RegionKeeper (Vertx vertx, HazelcastInstance hazelcastInstance) {
        Objects.requireNonNull(vertx);
        Objects.requireNonNull(hazelcastInstance);

        this.vertx = vertx;
        this.hazelcastInstance = hazelcastInstance;
    }

    public void start () {
        //register message consumers
        Log.v(LOG_TAG, "register message consumer for region loadbalancer.");
        this.getMessageConsumer = this.vertx.eventBus().consumer(EVENT_BUS_ADDRESS, this::handleGetRequest);
    }

    protected void handleGetRequest (Message<String> msg) {
        Log.d(LOG_TAG, "RegionKeeper::get() called.");
    }

    public void shutdown () {
        //unregister message consumers
        this.getMessageConsumer.unregister();
    }

}
