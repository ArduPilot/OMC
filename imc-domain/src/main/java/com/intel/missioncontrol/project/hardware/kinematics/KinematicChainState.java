/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.hardware.kinematics;

import com.intel.missioncontrol.project.hardware.SnapPointId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Kinematic model of a drone gimbal, including specific joint angles. */
public class KinematicChainState {
    private final KinematicChain chain;
    private final GimbalStateVector gimbalStateVector;

    private final Map<SnapPointId, SnapPoint> snapPoints = new HashMap<>();

    /** angles correspond to kinematic chain segments, in-order. */
    public KinematicChainState(KinematicChain chain, GimbalStateVector gimbalStateVector) {
        this.chain = chain;
        this.gimbalStateVector = gimbalStateVector;

        long n = chain.getGimbalStateVectorLength();

        if (gimbalStateVector.getAnglesDeg().length != n) {
            throw new IllegalArgumentException("anglesDeg array length must match number of kinematic segments");
        }
    }

    private SnapPoint getOrCreateSnapPointFromId(SnapPointId snapPointId) {
        SnapPoint res = snapPoints.get(snapPointId);
        if (res != null) {
            return res;
        }

        // create:
        KinematicSegment kinematicSegment = chain.getKinematicSegmentMap().get(snapPointId);
        if (kinematicSegment == null) {
            throw new IllegalArgumentException("Error: Unknown snap point ID " + snapPointId + " requested");
        }

        KinematicSegment previousSegment = kinematicSegment.getPrevious();
        SnapPoint previousSnapPoint =
            previousSegment == null ? null : getOrCreateSnapPointFromId(previousSegment.getSnapPointId());

        AffineTransform transformToPrevious;

        int i = kinematicSegment.getControllableSegmentIndex();
        double angleDeg =
            i >= 0
                ? gimbalStateVector.getAnglesDeg()[i]
                : kinematicSegment.getConstraintArc().getNormalizedStartAngleDeg();
        transformToPrevious = kinematicSegment.toAffineTransform(angleDeg);

        SnapPoint snapPoint = new SnapPoint(snapPointId, previousSnapPoint, transformToPrevious);
        snapPoints.put(snapPointId, snapPoint);
        return snapPoint;
    }

    /**
     * Creates a transform that allows for conversion of positions in the coordinate system of snapPointId1 into
     * positions in the coordinate system of snapPointId2.
     */
    public AffineTransform getTransformPath(SnapPointId snapPointId1, SnapPointId snapPointId2) {
        AffineTransform t1ToBody = transformFromSnapPointIdToBodyFrame(snapPointId1);
        AffineTransform t2ToBody = transformFromSnapPointIdToBodyFrame(snapPointId2);
        return t1ToBody.chain(t2ToBody.inverse());
    }

    private AffineTransform transformFromSnapPointIdToBodyFrame(SnapPointId snapPointId) {
        if (snapPointId.equals(SnapPointId.BODY_ORIGIN)) {
            return AffineTransform.IDENTITY;
        }

        // trace back from p:
        SnapPoint p = getOrCreateSnapPointFromId(snapPointId);
        AffineTransform transform = AffineTransform.IDENTITY;
        List<SnapPoint> visited = new ArrayList<>();
        do {
            if (visited.contains(p)) {
                throw new IllegalArgumentException(
                    "Cyclic transform path detected at snap point " + p.getId() + ", check platform description.");
            }

            visited.add(p);

            AffineTransform transformToPrevious = p.getTransformToPrevious();
            transform = transform.chain(transformToPrevious);
            p = p.getPrevious();
        } while (p != null);

        return transform;
    }
}
