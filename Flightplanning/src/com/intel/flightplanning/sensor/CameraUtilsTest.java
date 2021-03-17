/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.flightplanning.sensor;

import static com.intel.flightplanning.sensor.CameraUtils.calculatePhotoProperties;
import static com.intel.flightplanning.sensor.CameraUtils.lineIntersection;

import com.jme3.math.FastMath;
import com.jme3.math.Plane;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;

class CameraUtilsTest {

    public static Camera mockCamera() {
        var cam = new Camera();
        cam.setFocalLength(55e-3f);
        cam.setSensorHeightCm(1);
        cam.setSensorHeightPx(1000);
        cam.setSensorWidthCm(1);
        cam.setSensorWidthPx(1000);
        cam.setCcdHeight(1);
        cam.setCcdWidth(1);

        return cam;
    }

    @org.junit.jupiter.api.Test
    void lineIntersectionTest() {

        Plane p = new Plane(new Vector3f(0f,0f,1f),0f);
        Ray ray = new Ray(new Vector3f(0,0,100),new Vector3f(0,0,-1));
        var intersec = lineIntersection(p.getNormal(),new Vector3f(), ray.origin, ray.direction);
    }

    @org.junit.jupiter.api.Test
    void calculatePhotoPropertiesTestRPY() {
        var cam = mockCamera();

        var res = calculatePhotoProperties(cam,10f,0f,0f,0f );
        System.out.println(res);
    }

    @org.junit.jupiter.api.Test
    void calculatePhotoPropertiesTest() {

var cam = mockCamera();

        Quaternion roll180 = new Quaternion();
        roll180.fromAngleAxis( FastMath.PI /4 , new Vector3f(1,0,0) );
        /* The rotation is applied: The object rolls by 180 degrees. */

        var foo = calculatePhotoProperties(cam, 100.0f,roll180);
        System.out.println(foo);
    }
}
