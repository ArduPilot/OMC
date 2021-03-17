/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.hardware.kinematics;

import com.intel.missioncontrol.geometry.Arc;
import com.intel.missioncontrol.geometry.Vec3;
import com.intel.missioncontrol.project.hardware.SnapPointId;

/**
 * A segment of a kinematic chain, defining a snap point at an offset and rotation about an axis, relative to a previous
 * segment.
 *
 * <p>If the rotation angle is controllable, controllableSegmentIndex is non-negative and corresponds to an entry of a
 * gimbal state vector. Allowed rotation angles are limited to the given constraint arc, which msut have zero length if
 * the segment is not controllable.
 */
public class KinematicSegment {
    private final SnapPointId snapPointId;
    private final Vec3 offset;
    private final Vec3 rotationAxis;
    private final int controllableSegmentIndex;
    private final Arc constraintArc;
    private final KinematicSegment previous;

    public static final KinematicSegment BODY_ORIGIN =
        new KinematicSegment(SnapPointId.BODY_ORIGIN, Vec3.zero(), new Vec3(0, 0, 1), -1, new Arc(0.0, 0.0), null);

    public KinematicSegment(
            SnapPointId snapPointId,
            Vec3 offset,
            Vec3 rotationAxis,
            int controllableSegmentIndex,
            Arc constraintArc,
            KinematicSegment previous) {
        this.snapPointId = snapPointId;
        this.offset = offset;
        this.rotationAxis = rotationAxis;
        this.controllableSegmentIndex = controllableSegmentIndex;
        this.constraintArc = constraintArc;
        this.previous = previous;

        if (snapPointId == null) {
            throw new IllegalArgumentException("Snap point ID must not be null");
        }

        if ((rotationAxis == null || rotationAxis.length() == 0.0)
                && (controllableSegmentIndex >= 0
                    || constraintArc.getNormalizedStartAngleRad() != 0.0
                    || constraintArc.getNormalizedEndAngleRad() != 0.0)) {
            throw new IllegalArgumentException(
                "Non-zero rotation axis must be given for kinematic segments specifying a rotation angle");
        }

        if (constraintArc.getArcLengthRad() > 0.0 && controllableSegmentIndex < 0) {
            throw new IllegalArgumentException(
                "An controllable segment index must be given for kinematic segments specifying a constraint arc with length > 0");
        }

        if (controllableSegmentIndex >= 0 && !constraintArc.contains(0.0)) {
            throw new IllegalArgumentException(
                "Controllable segments must include zero angle to be used as the default angle.");
        }
    }

    AffineTransform toAffineTransform(double angleDeg) {
        double a = constraintArc.limitToArcDeg(angleDeg);
        return AffineTransform.fromAxisAngleDeg(rotationAxis, a).chain(AffineTransform.fromOffset(offset));
    }

    public KinematicSegment getPrevious() {
        return previous;
    }

    /**
     * the zero-based index in the angle array corresponding to this segment, or -1 if not a controllable part of the
     * kinematic chain.
     */
    int getControllableSegmentIndex() {
        return controllableSegmentIndex;
    }

    /** ID of the snap point at the end of this segment. */
    public SnapPointId getSnapPointId() {
        return snapPointId;
    }

    /** The arc describing the joint angles constraints about the rotation axis. */
    Arc getConstraintArc() {
        return constraintArc;
    }

    @Override
    public String toString() {
        return "KinematicSegment{"
            + "snapPointId="
            + snapPointId
            + ", offset="
            + offset
            + ", rotationAxis="
            + rotationAxis
            + ", controllableSegmentIndex="
            + controllableSegmentIndex
            + ", constraintArc="
            + constraintArc
            + ", previous="
            + previous
            + '}';
    }
}
