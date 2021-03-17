/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.hardware.kinematics;

import com.intel.missioncontrol.geometry.Arc;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

/** Gimbal state vector consisting of joint angles for an IPayloadConfiguration. */
public class GimbalStateVector implements IGimbalStateVector {

    public static final GimbalStateVector NO_GIMBAL = new GimbalStateVector(new double[0]);

    private double[] anglesDeg;

    GimbalStateVector(double[] anglesDeg) {
        this.anglesDeg = anglesDeg;
    }

    static GimbalStateVector fromAnglesRad(double[] anglesRad) {
        return new GimbalStateVector(Arrays.stream(anglesRad).map(a -> (180.0 / Math.PI) * a).toArray());
    }

    double[] getAnglesDeg() {
        return anglesDeg;
    }

    /**
     * Returns the root mean square distance between corresponding joint angles of the current and given state vector,
     * in degrees.
     */
    public double getDistanceMeasure(IGimbalStateVector other) {
        if (!(other instanceof GimbalStateVector)) {
            throw new IllegalArgumentException("other");
        }

        GimbalStateVector o = (GimbalStateVector)other;

        if (o.anglesDeg.length != anglesDeg.length) {
            throw new IllegalArgumentException("Incompatible GimbalStateVector");
        }

        double sum = 0.0;
        for (int i = 0; i < anglesDeg.length; i++) {
            // TODO: use distance within constraint arc
            double d = Arc.undirectedDistanceDeg(anglesDeg[i], o.anglesDeg[i]);
            sum += d * d;
        }

        return Math.sqrt(sum);
    }

    @Override
    public String toString() {
        return "GimbalStateVector{"
            + Arrays.stream(anglesDeg)
                .mapToObj(a -> String.format(Locale.ROOT, "%.2f°", a))
                .collect(Collectors.joining(", "))
            + "}";
    }
}
