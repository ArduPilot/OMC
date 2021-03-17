/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.cir;

public class HistogramRelative extends Histogram {

    static final double minHistRel = 0.01;
    static final double maxHistRel = 10;

    public HistogramRelative() {
        super(minHistRel, maxHistRel, 0.01, false);
    }
}
