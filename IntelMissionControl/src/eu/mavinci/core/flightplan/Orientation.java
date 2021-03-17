/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.flightplan;

import eu.mavinci.core.helper.MinMaxPair;
import gov.nasa.worldwind.geom.Angle;

public class Orientation {

    public static final Angle UNDEFINED = Angle.fromDegrees(Double.MIN_VALUE);
    /*
     * all double values in degrees
     */

    private Angle roll = UNDEFINED;
    private Angle pitch = UNDEFINED;
    private Angle yaw = UNDEFINED;

    public boolean isRollDefined() {
        if (roll == UNDEFINED) {
            return false;
        }

        return true;
    }

    public double getRoll() {
        return roll.getDegrees();
    }

    public static double norm(double angle) {
        while (angle >= 180) {
            angle -= 360;
        }

        while (angle < -180) {
            angle += 360;
        }

        return angle;
    }

    public boolean setRoll(double roll) {
        double norm = norm(roll);
        if (norm == this.roll.getDegrees()) {
            return false;
        }

        this.roll = Angle.fromDegrees(norm);
        return true;
    }

    public boolean isPitchDefined() {
        if (pitch == UNDEFINED) {
            return false;
        }

        return true;
    }

    public double getPitch() {
        return pitch.getDegrees();
    }

    public boolean setPitch(double pitch) {
        // do not normalize pitch in order to allow backward looking and upward looking
        if (pitch == this.pitch.getDegrees()) {
            return false;
        }

        this.pitch = Angle.fromDegrees(pitch);
        return true;
    }

    public boolean isYawDefined() {
        if (yaw == UNDEFINED) {
            return false;
        }

        return true;
    }

    public double getYaw() {
        return yaw.getDegrees() < 0 ? yaw.getDegrees() + 360 : yaw.getDegrees();
    }

    public boolean setYaw(double yaw) {
        double norm = norm(yaw);
        if (norm == this.yaw.getDegrees()) {
            return false;
        }

        this.yaw = Angle.fromDegrees(norm);
        return true;
    }

    public Orientation() {}

    public Orientation(double roll, double pitch, double yaw) {
        setRoll(roll);
        setPitch(pitch);
        setYaw(yaw);
    }

    public Orientation(Orientation source) {
        this.pitch = source.pitch;
        this.roll = source.roll;
        this.yaw = source.yaw;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof Orientation) {
            // System.out.println( this + " <-> " + obj);
            if (roll.equals(((Orientation)obj).roll)
                    && pitch.equals(((Orientation)obj).pitch)
                    && yaw.equals(((Orientation)obj).yaw)) {
                return true;
            }
        }

        return false;
    }

    public Orientation substract(Orientation o) {
        double roll = this.roll.degrees - o.roll.degrees;
        double pitch = this.pitch.degrees - o.pitch.degrees;
        double yaw = this.yaw.degrees - o.yaw.degrees;
        return new Orientation(roll, pitch, yaw);
    }

    public double getMaxAbsDegrees() {
        MinMaxPair minMaxPair = new MinMaxPair(roll.degrees, pitch.degrees, yaw.degrees);
        return minMaxPair.absMax();
    }

    @Override
    public String toString() {
        return "roll:" + roll + " pitch:" + pitch + " yaw:" + yaw;
    }

    @Override
    public Orientation clone() {
        return new Orientation(this);
    }

    public double getRollDiffRad(Orientation other) {
        double d = roll.getRadians() - other.roll.getRadians();
        while (d <= -Math.PI) d += Math.PI;
        while (d > Math.PI) d -= Math.PI;
        return d;
    }

    public double getPitchDiffRad(Orientation other) {
        double d = Math.toRadians(pitch.getRadians()) - Math.toRadians(other.pitch.getRadians());
        while (d <= -Math.PI) d += Math.PI;
        while (d > Math.PI) d -= Math.PI;
        return d;
    }

    public double getYawDiffRad(Orientation other) {
        double d = yaw.getRadians() - other.yaw.getRadians();
        while (d <= -Math.PI) d += Math.PI;
        while (d > Math.PI) d -= Math.PI;
        return d;
    }
}
