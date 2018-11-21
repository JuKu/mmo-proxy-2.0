package com.jukusoft.mmo.proxy.frontend.database;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RegionMetaTest {

    @Test
    public void testConstructor () {
        new RegionMeta(10, 20);
    }

    @Test
    public void testGetter () {
        RegionMeta region = new RegionMeta(10, 20);
        assertEquals(10, region.getRegionID());
        assertEquals(20, region.getInstanceID());
    }

}
