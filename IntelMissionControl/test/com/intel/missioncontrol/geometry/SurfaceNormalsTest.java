/*
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.geometry;

import eu.mavinci.flightplan.computation.PointShiftingHelper;
import gov.nasa.worldwind.geom.Vec4;
import org.junit.Assert;
import org.junit.Test;

public class SurfaceNormalsTest {
    @Test
    public void getNormalsToSomeSurface() {
        java.util.Vector<Vec4> points = new java.util.Vector<>();

        points.add(new Vec4(209.04324475326575, 159.4777208524756, -22.889339584132635, 1.0));
        points.add(new Vec4(198.43669365288224, 170.12930114241317, -22.130365927452395, 1.0));
        points.add(new Vec4(198.43665091216099, 159.47775994194672, -22.130227359052107, 1.0));
        points.add(new Vec4(198.43663225322962, 148.82622080249712, -21.370995049392164, 1.0));
        points.add(new Vec4(209.0432855822146, 170.12924643233418, -23.013633254619734, 1.0));
        points.add(new Vec4(209.04320491256658, 148.82619262160733, -23.588208264496664, 1.0));
        points.add(new Vec4(219.64986962685362, 170.12918990990147, -24.196316158844482, 1.0));
        points.add(new Vec4(219.6498294415651, 159.477680017706, -24.046466255129463, 1.0));
        points.add(new Vec4(219.64979315642267, 148.82616807194427, -24.468661757981852, 1.0));

        Vec4 normal = PointShiftingHelper.getNormal(points, Vec4.UNIT_Z);
        System.out.println("Normal: " + normal);
        double diff = Math.acos((normal).dot3(Vec4.UNIT_Z)) * 180 / Math.PI;
        System.out.println("Angle diff from ortho: " + diff);
        Assert.assertTrue(diff < 50);
    }
}
