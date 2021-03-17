/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.hardware.kinematics;

import com.intel.missioncontrol.NotImplementedException;
import com.intel.missioncontrol.geometry.Arc;
import com.intel.missioncontrol.geometry.EulerAngles;
import com.intel.missioncontrol.geometry.IOrientation;
import com.intel.missioncontrol.geometry.Mat3;
import com.intel.missioncontrol.geometry.Mat4;
import com.intel.missioncontrol.geometry.Vec3;
import com.intel.missioncontrol.geometry.Vec4;
import java.util.Collections;

/**
 * Affine transformation (limited to 3d offset, rotation and scaling), following the axis conventions of the body frame
 * in https://en.wikipedia.org/wiki/Flight_dynamics_(fixed-wing_aircraft)
 */
public class AffineTransform {

    // 4x4 matrix for affine transformation
    private final Mat4 affineMatrix;

    private final boolean isScaling;

    static final AffineTransform IDENTITY = new AffineTransform(Mat4.identity(), false);

    private AffineTransform(Mat4 affineMatrix, boolean isScaling) {
        this.affineMatrix = affineMatrix;
        this.isScaling = isScaling;
    }

    /**
     * Returns a transform to associate with geometry that is rotated by the given euler angles with respect to its
     * reference frame. Applying the transform to a point in the geometry's local frame will transform it into the
     * reference frame.
     */
    public static AffineTransform fromYawPitchRollDeg(double yawDeg, double pitchDeg, double rollDeg) {
        double roll = Double.isNaN(rollDeg) ? 0.0 : rollDeg * Math.PI / 180.0;
        double pitch = Double.isNaN(pitchDeg) ? 0.0 : pitchDeg * Math.PI / 180.0;
        double yaw = Double.isNaN(yawDeg) ? 0.0 : yawDeg * Math.PI / 180.0;

        // axes definition differs compared to Mat4 implementation
        return new AffineTransform(
            Mat4.fromRotationX(roll)
                .multiplyInplace(Mat4.fromRotationY(pitch))
                .multiplyInplace(Mat4.fromRotationZ(yaw)),
            false);
    }

    public static AffineTransform fromOffset(Vec3 offset) {
        return fromOffset(offset.x, offset.y, offset.z);
    }

    public static AffineTransform fromOffset(float[] offset) {
        if (offset.length != 3) {
            throw new IllegalArgumentException("offset must be a 3 element vector");
        }

        return fromOffset(offset[0], offset[1], offset[2]);
    }

    public static AffineTransform fromOffset(double x, double y, double z) {
        return new AffineTransform(Mat4.fromTranslation(x, y, z).transposeInplace(), false);
    }

    public static AffineTransform fromScale(double s) {
        return new AffineTransform(Mat4.fromScale(s), true);
    }

    public static AffineTransform fromOrientation(IOrientation orientation) {
        return fromYawPitchRollDeg(orientation.getYawDeg(), orientation.getPitchDeg(), orientation.getRollDeg());
    }

    public static AffineTransform fromAxisAngleDeg(Vec3 rotationAxis, double angleDeg) {
        return new AffineTransform(
            Mat4.fromAxisAngle(rotationAxis, angleDeg * Math.PI / 180.0).transposeInplace(), false);
    }

    public Vec3 transformPoint(Vec3 point) {
        return point.transform(affineMatrix.transpose());
    }

    /** return the 4x4 forward transform matrix. */
    public Mat4 getTransformMatrix() {
        return affineMatrix;
    }

    /** Return the given AffineTransform chained after the current one (pre-multiplies other.) */
    public AffineTransform chain(AffineTransform other) {
        if (this == IDENTITY) {
            return other;
        }

        if (other == IDENTITY) {
            return this;
        }

        return new AffineTransform(other.affineMatrix.multiply(affineMatrix), isScaling || other.isScaling);
    }

    /** return an AffineTransform that is the inverse (=backward transform) of the current. */
    public AffineTransform inverse() {
        if (equals(IDENTITY)) {
            return IDENTITY;
        }

        try {
            return new AffineTransform(affineMatrix.invert(), isScaling);
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("Malformed transformation: Not invertible");
        }
    }

    public enum GimbalLockMode {
        NAN,
        YAW_ZERO,
        ROLL_ZERO
    }

    /**
     * Returns the 3d offset component of this transform.
     */
    public Vec3 getOffset() {
        if (isScaling) {
            throw new NotImplementedException("Not implemented for scaling transforms");
        }

        Vec4 c = affineMatrix.column(3);
        return new Vec3(c.x, c.y, c.z);
    }

    /**
     * Get the rotational distance, 0°..180°, between the rotational parts of the current and given transform. The
     * rotational distance effectively compares yaw, pitch and roll angles at the same time while not being affected by
     * gimbal lock.
     */
    public double calculateRotationalDistanceDeg(AffineTransform other) {
        if (isScaling || other.isScaling) {
            throw new NotImplementedException("Not implemented for scaling transforms");
        }

        Mat3 m1 = Mat3.fromMat4(affineMatrix);
        Mat3 m2 = Mat3.fromMat4(other.affineMatrix);

        // http://www.boris-belousov.net/2016/12/01/quat-dist/

        double arg = ((m1.multiply(m2.transpose())).trace() - 1.0) / 2.0;
        if (arg < -1.0) {
            arg = -1.0;
        }

        if (arg > 1.0) {
            arg = 1.0;
        }

        double phiDeg = (180.0 / Math.PI) * Math.acos(arg);
        return phiDeg;
    }

    public double calculateRotationalDistanceDeg(AffineTransform other, boolean ignoreYaw) {
        if (!ignoreYaw) {
            return calculateRotationalDistanceDeg(other);
        }

        // there's probably a direct way of calculating the rotational distance while ignoring yaw.
        // for now, just run an optimization with all possible yaw angles and take the shortest distance.
        // use multiple starting yaw angles to ensure global minimum is found.

        double targetResolutionDeg = 1e-3;

        Arc constraintArc = Arc.fromAnglesDeg(0.0, 360.0 - targetResolutionDeg);

        GriddedAngularOptimizer optimizer = new GriddedAngularOptimizer(Collections.singletonList(constraintArc));

        GriddedAngularOptimizer.Result res =
            optimizer.optimize(
                stateVector -> {
                    double yawDeg = stateVector[0];
                    return calculateRotationalDistanceDeg(
                        AffineTransform.fromYawPitchRollDeg(yawDeg, 0, 0).chain(other));
                },
                targetResolutionDeg);

        return res != null ? res.getCost() : Double.NaN;
    }

    public EulerAngles getEulerAngles(GimbalLockMode gimbalLockMode) {
        // https://www.geometrictools.com/Documentation/EulerAngles.pdf
        // XYZ rotations
        // X: Roll rotation
        // Y: Pitch rotation
        // Z: Yaw rotation

        if (isScaling) {
            throw new NotImplementedException("Not implemented for scaling transforms");
        }

        Mat4 m = affineMatrix;

        double xRad;
        double yRad;
        double zRad;

        double tolerance = 1e-6;

        if (m.m13 < (1.0 - tolerance)) {
            if (m.m13 > (-1.0 + tolerance)) {
                xRad = Math.atan2(-m.m23, m.m33);
                yRad = Math.asin(m.m13);
                zRad = Math.atan2(-m.m12, m.m11);
            } else {
                yRad = -Math.PI / 2.0;
                switch (gimbalLockMode) {
                case NAN:
                    xRad = Double.NaN;
                    zRad = Double.NaN;
                    break;
                case YAW_ZERO:
                    xRad = -Math.atan2(m.m21, m.m22);
                    zRad = 0.0;
                    break;
                case ROLL_ZERO:
                    xRad = 0.0;
                    zRad = Math.atan2(m.m21, m.m22);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid gimbal lock mode");
                }
            }
        } else {
            yRad = Math.PI / 2.0;
            switch (gimbalLockMode) {
            case NAN:
                xRad = Double.NaN;
                zRad = Double.NaN;
                break;
            case YAW_ZERO:
                xRad = Math.atan2(m.m21, m.m22);
                zRad = 0.0;
                break;
            case ROLL_ZERO:
                xRad = 0.0;
                zRad = Math.atan2(m.m21, m.m22);
                break;
            default:
                throw new IllegalArgumentException("Invalid gimbal lock mode");
            }
        }

        return EulerAngles.fromRadians(-zRad, -yRad, -xRad);
    }

}
