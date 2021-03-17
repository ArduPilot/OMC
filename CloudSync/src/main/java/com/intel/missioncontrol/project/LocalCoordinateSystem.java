/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Vec4;

public class LocalCoordinateSystem extends CoordinateSystem<Vec4> {

    private final Matrix toParentMatrix;
    private final Matrix toLocalMatrix;

    LocalCoordinateSystem(Vec4 origin, double roll, double pitch, double yaw) {
        super(origin);
        toParentMatrix =
            Matrix.fromTranslation(origin)
                .multiply(
                    Matrix.fromRotationXYZ(Angle.fromDegrees(roll), Angle.fromDegrees(pitch), Angle.fromDegrees(yaw)));
        toLocalMatrix = toParentMatrix.getInverse();
    }

    @Override
    public Vec4 convertToParent(Vec4 localPoint) {
        return localPoint.transformBy4(toParentMatrix);
    }

    @Override
    public Vec4 convertToLocal(Vec4 point) {
        return point.transformBy4(toLocalMatrix);
    }
}
