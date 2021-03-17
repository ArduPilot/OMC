/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.flightplan;

public abstract class CPhoto extends ReentryPoint implements IFlightplanStatement {

    protected boolean powerOn;
    protected boolean triggerOnlyOnWaypoints;

    protected double distance = DEFAULT_DISTANCE;
    protected double distanceMax = DEFAULT_DISTANCE;

    public static final double DEFAULT_DISTANCE = 1000; // in cm -> 10m
    public static final boolean DEFAULT_POWER_ON = true;

    public static final double MAX_DISTANCE = 100000; // 1km in cm

    public boolean isPowerOn() {
        return powerOn;
    }

    public void setPowerOn(boolean powerOn) {
        if (this.powerOn != powerOn) {
            this.powerOn = powerOn;
            informChangeListener();
        }
    }

    public boolean isTriggerOnlyOnWaypoints() {
        return triggerOnlyOnWaypoints;
    }

    public void setTriggerOnlyOnWaypoints(boolean triggerOnlyOnWaypoints) {
        if (this.triggerOnlyOnWaypoints != triggerOnlyOnWaypoints) {
            this.triggerOnlyOnWaypoints = triggerOnlyOnWaypoints;
            informChangeListener();
        }
    }

    public double getDistanceMaxInCm() {
        return distanceMax;
    }

    /**
     * Maximal Distance between two pictures in cm
     *
     * @param distance
     */
    public void setDistanceMaxInCm(double distanceMax) {
        if (distanceMax > MAX_DISTANCE) {
            distanceMax = MAX_DISTANCE;
        }
        // normalize -0.0==+0.0 for primitives but NOT for Double or Float objects...
        if (distanceMax <= 0) {
            distanceMax = 0;
        }

        if (this.distanceMax != distanceMax) {
            this.distanceMax = distanceMax;
            informChangeListener();
        }
    }

    public double getDistanceInCm() {
        return distance;
    }

    /**
     * Distance between two pictures in cm
     *
     * @param distance
     */
    public void setDistanceInCm(double distance) {
        if (distance > MAX_DISTANCE) {
            distance = MAX_DISTANCE;
        }

        if (distance < 0) {
            distance = 0;
        }

        if (this.distance != distance) {
            this.distance = distance;
            informChangeListener();
        }
    }

    protected CPhoto(boolean powerOn, double distance, double distanceMax, int id) {
        super(id);
        setPowerOn(powerOn);
        setDistanceInCm(distance);
        setDistanceMaxInCm(distanceMax);
    }

    protected CPhoto(boolean powerOn, double distance, double distanceMax, int id, IFlightplanContainer parent) {
        super(parent, id);
        setPowerOn(powerOn);
        setDistanceInCm(distance);
        setDistanceMaxInCm(distanceMax);
    }

    protected CPhoto(IFlightplanContainer parent) {
        super(parent);
        setPowerOn(DEFAULT_POWER_ON);
        setDistanceInCm(DEFAULT_DISTANCE);
        setDistanceMaxInCm(DEFAULT_DISTANCE);
    }

    protected CPhoto(boolean powerOn, double distance, double distanceMax, IFlightplanContainer parent) {
        super(parent);
        setPowerOn(powerOn);
        setDistanceInCm(distance);
        setDistanceMaxInCm(distanceMax);
    }

    public String toString() {
        return "Photo"; // TODO better name
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (o == null) {
            return false;
        }

        if (o instanceof CPhoto) {
            CPhoto fp = (CPhoto)o;
            return id == fp.id
                && powerOn == fp.powerOn
                && distance == fp.distance
                && distanceMax == fp.distanceMax
                && triggerOnlyOnWaypoints == fp.triggerOnlyOnWaypoints;
        }

        return false;
    }

}
