/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.airmap.data;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.mercator.MercatorSector;
import gov.nasa.worldwind.util.Level;
import gov.nasa.worldwind.util.LevelSet;
import gov.nasa.worldwind.util.Tile;
import gov.nasa.worldwind.util.TileUrlBuilder;
import org.junit.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collection;

import static org.junit.Assert.assertNotNull;

public class AirSpaceTest {


    public static Reader loadTestData() {
        InputStream is = AirSpaceTest.class.getResourceAsStream( "/airmap/airmap_response.json");
        Reader reader = new InputStreamReader(is, Charset.forName("utf-8"));
        return reader;
    }

    @Test
    public void testTile() {
        AVList params = new AVListImpl();

        params.setValue(AVKey.TILE_WIDTH, 256);
        params.setValue(AVKey.TILE_HEIGHT, 256);
        params.setValue(AVKey.DATA_CACHE_NAME, "Earth/OSM-Mercator/OpenStreetMap Cycle");
        params.setValue(AVKey.SERVICE, "http://b.andy.sandbox.cloudmade.com/tiles/cycle/");
        params.setValue(AVKey.DATASET_NAME, "*");
        params.setValue(AVKey.FORMAT_SUFFIX, ".png");
        params.setValue(AVKey.NUM_LEVELS, 16);

        params.setValue(AVKey.NUM_EMPTY_LEVELS, 0);
        params.setValue(AVKey.LEVEL_ZERO_TILE_DELTA, new LatLon(Angle
                .fromDegrees(22.5d), Angle.fromDegrees(45d)));
        params.setValue(AVKey.SECTOR, new MercatorSector(-1.0, 1.0,
                Angle.NEG180, Angle.POS180));
        params.setValue(AVKey.TILE_URL_BUILDER, new URLBuilder());


        params.setValue(AVKey.INACTIVE_LEVELS, "0,1,2,3,4");
        LevelSet l = new LevelSet(params);

        LatLon sjc = LatLon.fromDegrees(37.283345775, -121.916691995);
        Level lastLevel = l.getLastLevel();

        Sector sjcSector = lastLevel.computeSectorForPosition(sjc.getLatitude(), sjc.getLongitude(), l.getTileOrigin());

        Tile t;

    }

    private static class URLBuilder implements TileUrlBuilder
    {
        public URL getURL(Tile tile, String imageFormat)
                throws MalformedURLException
        {
            return new URL(tile.getLevel().getService()
                    + (tile.getLevelNumber() + 3) +"/"+ tile.getColumn()+"/"+ ((1 << (tile.getLevelNumber()) + 3) - 1 - tile.getRow()) + ".png");
        }
    }


    @Test
    public void loadTest() {

        Gson loader = Loader.createLoader();

        Type listType = new TypeToken<AirMapResponse<Collection<AirSpaceObject>>>() {}.getType();

        AirMapResponse<Collection<AirSpaceObject>> resp =  loader.fromJson(loadTestData(), listType);

        assertNotNull(resp.data);




        assertNotNull(resp);
    }
}
