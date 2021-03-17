/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.hardware.kinematics;

import com.intel.missioncontrol.geometry.Arc;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ArcTest {

    @Test
    void Arc_Angles_Are_Consistent() {
        double tolerance = 1e-9;

        Arc arc;

        arc = Arc.fromAnglesDeg(-10, 10);
        Assertions.assertEquals(20, arc.getArcLengthDeg(), tolerance);

        Assertions.assertEquals(-10, arc.getNormalizedStartAngleDeg(), tolerance);
        Assertions.assertEquals(10, arc.getNormalizedEndAngleDeg(), tolerance);

        Assertions.assertEquals(-10, arc.getMinAngleDeg(), tolerance);
        Assertions.assertEquals(10, arc.getMaxAngleDeg(), tolerance);

        arc = Arc.fromAnglesDeg(10, -10);
        Assertions.assertEquals(340, arc.getArcLengthDeg(), tolerance);

        Assertions.assertEquals(10, arc.getNormalizedStartAngleDeg(), tolerance);
        Assertions.assertEquals(-10, arc.getNormalizedEndAngleDeg(), tolerance);

        Assertions.assertEquals(10, arc.getMinAngleDeg(), tolerance);
        Assertions.assertEquals(350, arc.getMaxAngleDeg(), tolerance);
    }

    @Test
    void DistanceWithinArcRad_Works() {
        double tolerance = 1e-9;

        Arc arc;

        arc = Arc.fromAnglesDeg(-10, 10); // 20° arc
        Assertions.assertEquals(5, arc.distanceWithinArcDeg(-2, 3), tolerance);
        Assertions.assertEquals(5, arc.distanceWithinArcDeg(3, -2), tolerance);

        arc = Arc.fromAnglesDeg(10, -10); // 340° arc
        Assertions.assertEquals(360 - 12 - 13, arc.distanceWithinArcDeg(-12, 13), tolerance);
        Assertions.assertEquals(360 - 12 - 13, arc.distanceWithinArcDeg(13, -12), tolerance);
    }
}
