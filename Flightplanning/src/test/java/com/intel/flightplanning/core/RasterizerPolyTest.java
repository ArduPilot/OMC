/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.flightplanning.core;

import com.intel.flightplanning.core.Goal;
import com.intel.flightplanning.core.rasterizer.RasterizerPoly;
import com.intel.flightplanning.core.terrain.MockElevationModel;
import com.intel.flightplanning.core.vehicle.UAV;
import com.intel.flightplanning.geometry2d.Polygon;
import com.intel.flightplanning.sensor.Camera;
import com.intel.flightplanning.sensor.CameraUtils;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;

class RasterizerPolyTest {

    @Test
    void calculateBoundingBox() {}

    @Test
    void isValidAOI() {}

    @Test
    void isValidSizeAOI() {}

    public static Goal mockGoal() {
        var gb = new Goal.Builder();
        gb.setTargetGSD(0.1f);
        gb.setTargetAlt(100);
        gb.setOverlapInFlight(0.8f);
        return gb.createGoal();
    }

    @Test
    void rasterizeTest() {
        var poly = new ArrayList<Vector2f>();
        var aoi = new Polygon();
        var cam = new Camera();
        cam.setFocalLength(1);
        cam.setSensorHeightCm(1);
        cam.setSensorHeightPx(100);
        cam.setSensorWidthCm(1);
        cam.setSensorWidthPx(100);
        cam.setCcdHeight(1);
        cam.setCcdWidth(1);
        var av = new UAV();
        var elev = new MockElevationModel();
        var g  = mockGoal();

        Quaternion roll180 = new Quaternion();
        roll180.fromAngleAxis( FastMath.PI /4 , new Vector3f(1,0,0) );
        /* The rotation is applied: The object rolls by 180 degrees. */

        var p = CameraUtils.calculatePhotoProperties(cam, 100f, roll180);




        RasterizerPoly.rasterize(aoi,cam, av, elev, g, p);
    }
}