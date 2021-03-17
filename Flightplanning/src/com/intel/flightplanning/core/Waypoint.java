/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.flightplanning.core;

import com.jme3.math.Vector3f;
import java.security.InvalidParameterException;
import java.util.Objects;

/**
 * Defines the location of the nodal point of the UAV's sensor and the direction the camera should point to as well as
 * the distance to the target.
 *
 * <p>The minimal information for creating a way point is the designated camera position.
 *
 * <p>The next is the designated camera position and camera orientation, but without a camera target or distance to
 * target
 *
 * <p>Todo: what if i don't have a target? we could also extend a class TargetWaypoint, that has targetDist and Vec3
 * target. Alternatively, we can by default initialize the target distance to "infinite".
 */
public class Waypoint {
    /** Todo: do we really need that? */
    protected float distanceMax;

    protected float distanceToTarget;

    /** contains all gimbal angles such that the camera points at the target. */
    GimbalStateVector gimbalStateVector;

    /**
     * Camera/payload position. Nodal point for optical camera. To be defined for other payloads (lidar, particle
     * sensor)
     */
    private Vector3f cameraPosition;

    /** roll of the uav around flight direction */
    private float roll;
    /** pitch of the uav */
    private float pitch;
    /** yaw / attitude of the uav */
    private float yaw;

    /**
     * Position of the drone itself. Todo: should actually be computable from camera position together with the
     * KinematicProvider
     */
    private Vector3f dronePosition;

    /** the cameraDirection alone does not specify the camera orientation, we need the camera roll as well. */
    private float cameraRoll;

    private Vector3f cameraDirection;

    /** For optical camera this the image center. */
    private Vector3f target;
    /**
     * Specifies whether to take an image or not. This could have also been implemented as a phantom waypoint class, but
     * it might be that it is important to specify the gimbal position and all other waypoint fields in any case to,
     * e.g., calculate the generic distance function between two waypoints that might depend on a difference in gimbal
     * position
     */
    private boolean takeImage = true;

    private Waypoint() {
        this.cameraPosition = new Vector3f();
        this.target = new Vector3f();
        this.cameraDirection = new Vector3f();
    }

    public Waypoint(Vector3f cameraPosition) {
        this.cameraDirection = cameraPosition;
        this.takeImage = false;
    }

    public Waypoint(Vector3f cameraPosition, boolean takeImage) {
        this.cameraDirection = cameraPosition;
        this.takeImage = takeImage;
    }

    public Waypoint(Vector3f cameraPosition, Vector3f cameraDirection) {
        Objects.requireNonNull(cameraPosition, "position");
        Objects.requireNonNull(cameraDirection, "camDirection");
        this.cameraPosition = new Vector3f(cameraPosition);
        this.cameraDirection = new Vector3f(cameraDirection);
        this.cameraDirection.normalizeLocal();
        this.target = new Vector3f(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
        this.distanceToTarget = Float.POSITIVE_INFINITY;
    }

    public Waypoint(Vector3f cameraPosition, Vector3f cameraDirection, boolean takeImage) {
        this(cameraPosition,cameraDirection);
        this.takeImage = takeImage;
    }

    public Waypoint(Vector3f cameraPosition, Vector3f cameraDirection, float distanceToTarget, float roll) {
        Objects.requireNonNull(cameraPosition, "position");
        Objects.requireNonNull(cameraDirection, "camDirection");
        if (distanceToTarget < 0f) {
            throw new InvalidParameterException("distanceToTarget most be positive");
        }

        this.cameraPosition = new Vector3f(cameraPosition);
        this.cameraDirection = new Vector3f(cameraDirection);
        this.cameraDirection.normalizeLocal();
        this.distanceToTarget = distanceToTarget;
        this.target = this.cameraPosition.add(this.cameraDirection).multLocal(distanceToTarget);
        this.roll = roll;
    }

    public Waypoint(Vector3f cameraPosition, Vector3f target, float roll) {
        Objects.requireNonNull(cameraPosition, "position");
        Objects.requireNonNull(cameraDirection, "camDirection");
        if (distanceToTarget < 0f) {
            throw new InvalidParameterException("distanceToTarget most be positive");
        }

        this.cameraPosition = new Vector3f(cameraPosition);
        this.target = new Vector3f(target);
        var diff = this.target.subtract(this.cameraPosition);
        this.distanceToTarget = diff.length();
        this.cameraDirection = diff.mult(1f / this.distanceToTarget);
        this.roll = roll;
    }

    public boolean isTakeImage() {
        return takeImage;
    }

    public Vector3f getDronePosition() {
        return dronePosition;
    }

    public float getCameraRoll() {
        return cameraRoll;
    }

    public float getDistanceToTarget() {
        return distanceToTarget;
    }

    public float getDistanceMax() {
        return distanceMax;
    }

    public GimbalStateVector getGimbalStateVector() {
        return gimbalStateVector;
    }

    public Vector3f getCameraDirection() {
        return cameraDirection;
    }

    public float getRoll() {
        return roll;
    }

    public float getPitch() {
        return pitch;
    }

    public float getYaw() {
        return yaw;
    }

    public Vector3f getCameraPosition() {
        return cameraPosition;
    }

    public Vector3f getTarget() {
        return target;
    }

    @Override
    public String toString() {
        return cameraPosition.toString() + "\t-> " + cameraDirection.toString() + "\t@ " + target;
    }
}
