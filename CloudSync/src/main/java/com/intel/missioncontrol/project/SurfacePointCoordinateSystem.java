/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project;

import com.intel.missioncontrol.common.GeoMath;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;

public class SurfacePointCoordinateSystem extends CoordinateSystem<Position> {
    private final Matrix toGlobeMatrix;
    private final Matrix toLocalMatrix;

    SurfacePointCoordinateSystem(LatLon position) {
        super(GeoMath.geodeticToEcef(position.latitude, position.longitude, 0));
        toGlobeMatrix = GeoMath.computeEllipsoidalOrientationAtPosition(position.latitude, position.longitude, 0);
        toLocalMatrix = toGlobeMatrix.getInverse();
    }

    @Override
    public Position convertToParent(Vec4 localPoint) {
        return GeoMath.EcefToGeodetic(localPoint.transformBy4(toGlobeMatrix));
    }

    @Override
    public Vec4 convertToLocal(Position position) {
        return GeoMath.geodeticToEcef(position.latitude, position.longitude, position.elevation)
            .transformBy4(toLocalMatrix);
    }
}
