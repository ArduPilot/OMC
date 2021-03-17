/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.sunangles;

public enum RelevantSunElevation {
    custom(Double.NaN),
    darkness(-25.),
    astronomicaldawn(-18),
    nauticaldawn(-12),
    civildawn(-6),
    sunrise(-0.833);

    public final double thresholdDeg;

    RelevantSunElevation(double thresholdDeg) {
        this.thresholdDeg = thresholdDeg;
    }
}
