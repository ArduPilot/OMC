/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.geometry;

/**
 * Yaw, pitch and roll angles in earth frame, see https://en.wikipedia.org/wiki/Flight_dynamics_(fixed-wing_aircraft)
 */
public class EulerAngles implements IOrientation {
    private final double yawDeg;
    private final double pitchDeg;
    private final double rollDeg;

    public static final EulerAngles ZERO = new EulerAngles(0, 0, 0);

    public EulerAngles(double[] yawPitchRollDeg) {
        if (yawPitchRollDeg.length != 3) {
            throw new IllegalArgumentException("yawPitchRollDeg must be a 3 element array");
        }

        this.yawDeg = yawPitchRollDeg[0];
        this.pitchDeg = yawPitchRollDeg[1];
        this.rollDeg = yawPitchRollDeg[2];
    }

    public EulerAngles(float[] yawPitchRollDeg) {
        if (yawPitchRollDeg.length != 3) {
            throw new IllegalArgumentException("yawPitchRollDeg must be a 3 element array");
        }

        this.yawDeg = yawPitchRollDeg[0];
        this.pitchDeg = yawPitchRollDeg[1];
        this.rollDeg = yawPitchRollDeg[2];
    }

    public EulerAngles(double yawDeg, double pitchDeg, double rollDeg) {
        this.yawDeg = yawDeg;
        this.pitchDeg = pitchDeg;
        this.rollDeg = rollDeg;
    }

    public static EulerAngles fromRadians(double yawRad, double pitchRad, double rollRad) {
        return new EulerAngles(yawRad * 180.0 / Math.PI, pitchRad * 180.0 / Math.PI, rollRad * 180.0 / Math.PI);
    }

    public double getYawDeg() {
        return yawDeg;
    }

    public double getPitchDeg() {
        return pitchDeg;
    }

    public double getRollDeg() {
        return rollDeg;
    }

    public double getYawRad() {
        return yawDeg * Math.PI / 180.0;
    }

    public double getPitchRad() {
        return pitchDeg * Math.PI / 180.0;
    }

    public double getRollRad() {
        return rollDeg * Math.PI / 180.0;
    }

    @Override
    public String toString() {
        return "EulerAngles{" + "yawDeg=" + yawDeg + ", pitchDeg=" + pitchDeg + ", rollDeg=" + rollDeg + '}';
    }
}
