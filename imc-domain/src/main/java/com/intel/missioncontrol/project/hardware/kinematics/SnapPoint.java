/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.hardware.kinematics;

import com.intel.missioncontrol.project.hardware.SnapPointId;

/**
 * A node in a tree of 3D snap points, including a transformation that converts a point in the snap point's coordinate
 * system to the previous snap point's system.
 *
 * <p>information about how obtain a local coordinate system from the previous snap point
 */
public class SnapPoint {
    private final SnapPointId id;
    private final SnapPoint previous;
    private final AffineTransform transformToPrevious;

    SnapPoint(SnapPointId id, SnapPoint previous, AffineTransform transformToPrevious) {
        this.id = id;
        this.previous = previous;
        this.transformToPrevious = transformToPrevious;
    }

    SnapPointId getId() {
        return id;
    }

    SnapPoint getPrevious() {
        return previous;
    }

    AffineTransform getTransformToPrevious() {
        return transformToPrevious;
    }
}
