package com.jukusoft.mmo.proxy.frontend.database;

public class RegionMeta {

    protected final int regionID;
    protected final int instanceID;

    public RegionMeta (int regionID, int instanceID) {
        this.regionID = regionID;
        this.instanceID = instanceID;
    }

    public int getRegionID() {
        return regionID;
    }

    public int getInstanceID() {
        return instanceID;
    }

}
