/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.airmap.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import gov.nasa.worldwind.geom.LatLon;
import org.junit.Test;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class GeoJsonTest {

    static final String testPoint =
            "{ \"type\": \"Point\", \n" +
            "    \"coordinates\": [30, 10]\n" +
            "}";

    static final String testLineString =
            "{ \"type\": \"LineString\", \n" +
            "    \"coordinates\": [\n" +
            "        [30, 10], [10, 30], [40, 40]\n" +
            "    ]\n" +
            "}";

    static final String testPoly =
            "{ \"type\": \"Polygon\", \n" +
            "    \"coordinates\": [\n" +
            "        [[30, 10], [40, 40], [20, 40], [10, 20], [30, 10]]\n" +
            "    ]\n" +
            "}";

    static final String testMultiPoly =
            "{ \"type\": \"MultiPolygon\", \n" +
            "    \"coordinates\": [\n" +
            "        [\n" +
            "            [[30, 20], [45, 40], [10, 40], [30, 20]]\n" +
            "        ], \n" +
            "        [\n" +
            "            [[15, 5], [40, 10], [10, 20], [5, 10], [15, 5]]\n" +
            "        ]\n" +
            "    ]\n" +
            "}";


    @Test
    public void testDecode() {
        GsonBuilder gb = new GsonBuilder();

        Loader.registerGeoJsonAdapter(gb);
        Gson gson = gb.create();

        GeoJson.Geometry pt = gson.fromJson(testPoint, GeoJson.Geometry.class);
        assertEquals(pt.getClass(), GeoJson.PointGeom.class);

        GeoJson.Geometry ls = gson.fromJson(testLineString, GeoJson.Geometry.class);
        assertEquals(ls.getClass(), GeoJson.LineStringGeom.class);

        GeoJson.Geometry po = gson.fromJson(testPoly, GeoJson.Geometry.class);
        assertEquals(po.getClass(), GeoJson.PolygonGeom.class);

        GeoJson.Geometry mpo = gson.fromJson(testMultiPoly, GeoJson.Geometry.class);
        assertEquals(mpo.getClass(), GeoJson.MultiPolygonGeom.class);
    }


    @Test
    public void testLatLon() {
        GsonBuilder gb = new GsonBuilder();
        Loader.registerGeoJsonAdapter(gb);
        Gson gson = gb.create();


        LatLon latLon = gson.fromJson("[-20, 10]", LatLon.class);
        assertNotNull(latLon);
        assertEquals(latLon.getLatitude().getDegrees(), 10.0, 0.01);
        assertEquals(latLon.getLongitude().getDegrees(), -20.0, 0.01);

        String llArray = "[[-20, 10],[20, 10],[-20,-10]]";
        Type type = new TypeToken<Collection<LatLon>>() {}.getType();

        Collection<LatLon> c = gson.fromJson(llArray, type);
        assertEquals(3, c.size());
        assertTrue(c.contains(latLon));

        {
            String bad = "[190, 95]";
            LatLon f = gson.fromJson(bad, LatLon.class);
        }

        Exception except = null;
        try {

            String bad = "[190z, 95]";
            LatLon f = gson.fromJson(bad, LatLon.class);
        } catch (Exception e) {
            except = e;
        }

        assertNotNull(except);
    }


    @Test
    public void testMap() {
        String str =
                "{\n" +
                "    \"foo\": [2,10],\n" +
                "    \"bar\": [10,3],\n" +
                "    \"baz\": [-20,10]\n" +
                "}";

        GsonBuilder gb = new GsonBuilder();
        Loader.registerGeoJsonAdapter(gb);
        Gson gson = gb.create();

        Type type = new TypeToken<Map<String, LatLon>>() {}.getType();
        Map<String, LatLon> map = gson.fromJson(str, type);

        int size = map.size();
    }
}
