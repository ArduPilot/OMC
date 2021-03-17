/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.hardware.kinematics.provider;

import com.intel.missioncontrol.geometry.Arc;
import com.intel.missioncontrol.geometry.Vec3;
import com.intel.missioncontrol.project.hardware.IGimbalSegmentDescription;
import com.intel.missioncontrol.project.hardware.IGnssAntennaDescription;
import com.intel.missioncontrol.project.hardware.IPayloadSnapPointDescription;
import com.intel.missioncontrol.project.hardware.ISnapPointDescription;
import com.intel.missioncontrol.project.hardware.SnapPointId;
import com.intel.missioncontrol.project.hardware.kinematics.KinematicSegment;

class KinematicSegmentFactory {

    private static final Vec3 X_AXIS = new Vec3(1, 0, 0);
    private static final Vec3 Z_AXIS = new Vec3(0, 0, 1);

    static KinematicSegment createFromGimbalSegment(
            IGimbalSegmentDescription gimbalSegment, int angleIndex, KinematicSegment previous) {
        Vec3 offset = gimbalSegment.getOffset();
        Vec3 rotationAxis = gimbalSegment.getRotationAxis();
        Arc constraintArc = Arc.fromAnglesDeg(-gimbalSegment.getCcwLimitDeg(), gimbalSegment.getCwLimitDeg());

        return new KinematicSegment(
            gimbalSegment.getSnapPointId(), offset, rotationAxis, angleIndex, constraintArc, previous);
    }

    /** Creates a kinematic segment for a snap point with constant offset. */
    static KinematicSegment createFromSnapPointDescription(
            ISnapPointDescription snapPointDescription, KinematicSegment previous) {
        Vec3 offset = snapPointDescription.getOffset();

        return new KinematicSegment(snapPointDescription.getId(), offset, X_AXIS, -1, new Arc(0, 0), previous);
    }

    /**
     * Creates two consecutive kinematic segments for a payload snap point, one for the optical axis rotation with
     * respect to the x axis (= default optical axis), and one for the rotation angle about the optical axis itself.
     */
    static KinematicSegment createFromPayloadSnapPoint(
            IPayloadSnapPointDescription payloadSnapPoint, KinematicSegment previous) {
        Vec3 offset = payloadSnapPoint.getOffset();
        Vec3 opticalAxis = payloadSnapPoint.getOpticalAxis().normalize();

        if (!opticalAxis.equals(X_AXIS)) {
            Vec3 cr = opticalAxis.equals(X_AXIS) ? Z_AXIS : X_AXIS.cross(opticalAxis);
            Vec3 rotationAxis = cr.normalize();
            double angleRad = opticalAxis.equals(X_AXIS) ? 0.0f : (float)Math.asin(cr.length());

            previous =
                new KinematicSegment(
                    payloadSnapPoint.getId(), offset, rotationAxis, -1, new Arc(angleRad, angleRad), previous);
        }

        double angleAboutOpticalAxisRad = payloadSnapPoint.getAngle() * Math.PI / 180.0;

        return new KinematicSegment(
            payloadSnapPoint.getId(),
            offset,
            opticalAxis,
            -1,
            new Arc(angleAboutOpticalAxisRad, angleAboutOpticalAxisRad),
            previous);
    }

    static KinematicSegment createFromGnssAntenna(IGnssAntennaDescription gnssAntenna, KinematicSegment previous) {
        Vec3 offset = gnssAntenna.getOffset();

        Vec3 axis = new Vec3(0, 0, -1); // not used, end of kinematic chain

        return new KinematicSegment(SnapPointId.GNSS_ANTENNA, offset, axis, -1, new Arc(0.0, 0.0), previous);
    }
}
