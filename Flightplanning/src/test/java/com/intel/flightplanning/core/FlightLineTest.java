/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.flightplanning.core;

import com.intel.flightplanning.core.terrain.MockElevationHelper;
import com.intel.flightplanning.core.terrain.MockElevationModel;
import com.intel.flightplanning.geometry2d.Geometry2D;
import com.intel.flightplanning.geometry2d.Polygon;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class FlightLineTest {

    @Test
    public void intersectionTest() {
        Vector3f start = new Vector3f(0, 0, 0);
        Vector3f direction = new Vector3f(1, 0, 0);
        Vector3f end = new Vector3f(100, 0, 0);
        FlightLine fl = new FlightLine(start, direction, end);

        ArrayList<Vector2f> poly =
            new ArrayList<Vector2f>(
                Arrays.asList(
                    new Vector2f(0, 0),
                    new Vector2f(10, 0),
                    new Vector2f(10, 10),
                    new Vector2f(0, 10),
                    new Vector2f(0, 0)));
        ArrayList<List<Vector2f>> polyList = new ArrayList<>();
        polyList.add(poly);
        Geometry2D aoi = new Polygon();

        List<MinMaxPair> m = fl.getFlightlineIntersectionWithPolygonsHorizontal(polyList, 0, 1);
        System.out.println(m.get(0));
    }

    @Test
    public void adaptationTest() {
        var wp = new ArrayList<Waypoint>();
        wp.add(new Waypoint(new Vector3f(0f, 0f, 0f), new Vector3f(0f, 0f, -1f)));
        wp.add(new Waypoint(new Vector3f(1f, 0f, 0f), new Vector3f(0f, 0f, -1f)));
        wp.add(new Waypoint(new Vector3f(2f, 0f, 0f), new Vector3f(0f, 0f, -1f)));
        wp.add(new Waypoint(new Vector3f(3f, 0f, 0f), new Vector3f(0f, 0f, -1f)));

        var fl = new FlightLine(wp);
        var newFl = FlightLine.adaptToTerrain(fl, new MockElevationModel(), new MockElevationHelper(), 10f);
        newFl.getWaypoints().forEach(System.out::println);
    }

}
