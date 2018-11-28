package com.jukusoft.mmo.proxy.frontend.loadbalancer;

import com.hazelcast.core.*;
import com.jukusoft.mmo.engine.shared.logger.Log;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;

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

    //config
    protected LoadBalancingStrategy strategy = LoadBalancingStrategy.ROUND_ROBIN;

    //hazelcast
    protected final IMap<String,String> assignedRegions;
    protected final IList<String> gsServerList;
    protected final IAtomicLong nextIndex;
    protected final ILock nextIDLock;

    /**
    * default constructor
    */
    public RegionKeeper (Vertx vertx, HazelcastInstance hazelcastInstance) {
        Objects.requireNonNull(vertx);
        Objects.requireNonNull(hazelcastInstance);

        this.vertx = vertx;
        this.hazelcastInstance = hazelcastInstance;

        this.assignedRegions = hazelcastInstance.getMap("assigned-regions");
        this.gsServerList = hazelcastInstance.getList("gs-servers-list");
        this.nextIndex = hazelcastInstance.getAtomicLong("region-next-id");
        this.nextIDLock = hazelcastInstance.getLock("region-nextID-lock");
    }

    public void start () {
        //register message consumers
        Log.v(LOG_TAG, "register message consumer for region loadbalancer.");
        this.getMessageConsumer = this.vertx.eventBus().consumer(EVENT_BUS_ADDRESS, this::handleGetRequest);
    }

    protected void handleGetRequest (Message<String> msg) {
        Log.d(LOG_TAG, "RegionKeeper::get() called.");

        // convert request to json object and get parameters
        JsonObject request = new JsonObject(msg.body());
        long regionID = request.getLong("regionID");
        int instanceID = request.getInteger("instanceID");
        String regionToken = regionID + "-" + instanceID;

        JsonObject response = new JsonObject();

        String serverStr = "";

        //check if region is already running
        if (this.assignedRegions.containsKey(regionToken)) {
            //region is running

            // extract ip and port
            serverStr = this.assignedRegions.get(regionToken);

            Log.v(LOG_TAG, "region " + regionToken + " is already running.");
        } else {
            //server isn't running --> assign to a game server
            Log.d(LOG_TAG, "region " + regionToken + " isn't running yet, select server for region now.");

            //check if any game server is running
            if (this.gsServerList.isEmpty()) {
                Log.w(LOG_TAG, "cannot assign region, all gameservers are currently down.");

                //send error
                response.put("error", "all gameservers are currently down.");
                msg.reply(response.encode());
            }

            //choose gameserver
            serverStr = this.getGSServer(regionToken);
        }

        String array[] = serverStr.split(":");
        String ip = array[0];
        int port = Integer.parseInt(array[1]);

        //send answer back to server which requested this region
        response.put("ip", ip);
        response.put("port", port);
        response.put("error", "none");
        msg.reply(response.encode());
    }

    protected String getGSServer (String regionToken) {
        switch (this.strategy) {
            case ROUND_ROBIN:
                int nOfServers = this.gsServerList.size();

                //get next index and increment (mod server count)
                this.nextIDLock.lock();
                int index = (int) nextIndex.get() % nOfServers;
                nextIndex.set(index + 1);
                this.nextIDLock.unlock();

                return this.gsServerList.get(index);

            default:
                throw new UnsupportedOperationException("loadbalancing strategy " + this.strategy.name() + "isn't implemented yet.");
        }
    }

    public void shutdown () {
        //unregister message consumers
        this.getMessageConsumer.unregister();
    }

}
