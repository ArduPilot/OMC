/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.flightplanning.core;

import com.intel.flightplanning.core.Goal;
import com.intel.flightplanning.core.rasterizer.RasterizerCorridor;
import com.intel.flightplanning.core.terrain.MockElevationModel;
import com.intel.flightplanning.core.vehicle.UAV;
import com.intel.flightplanning.geometry2d.Polygon;
import com.intel.flightplanning.sensor.Camera;
import com.intel.flightplanning.sensor.CameraUtils;
import com.jme3.math.Vector2f;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;

class RasterizerCorridorTest {


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
        var corners = new ArrayList<Vector2f>();
        corners.add(new Vector2f(0,1));
        corners.add(new Vector2f(0,10));
        corners.add(new Vector2f(10,20));
        aoi.setCornerPoints(corners);
        var cam = new Camera();
        cam.setFocalLength(1);
        cam.setSensorHeightCm(1);
        cam.setSensorHeightPx(100);
        cam.setSensorWidthCm(1);
        cam.setSensorWidthPx(100);
        cam.setCcdHeight(1);
        cam.setCcdWidth(1);
        var uav = new UAV();
        var elev = new MockElevationModel();
        var g  = mockGoal();
        var p = CameraUtils.calculatePhotoProperties(cam, 100f);
p.setSizeParallelFlightEff((float) p.getSizeParallelFlight());
        var r = new RasterizerCorridor();
        var fp = r.rasterize(aoi,1,3,10,cam,uav,elev,g,p);
        System.out.println(fp);
    }
}