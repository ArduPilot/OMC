/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.wwext;

import gov.nasa.worldwind.geom.Position;

public class SurfaceFilledCircleWithUserData extends SurfacePolygonWithUserData {

    double radius;
    Position center;

    public static final int NUMBER_OF_SEGMENTS = 32;

    public SurfaceFilledCircleWithUserData(Object userData, Position center, double radius) {
        setUserData(userData);
        this.center = center;
        this.radius = radius;
        setOuterBoundary(CircleWithUserData.makeCircle(center, radius));
    }

}
