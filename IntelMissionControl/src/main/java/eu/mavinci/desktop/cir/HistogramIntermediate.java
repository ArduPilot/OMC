/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.cir;

public class HistogramIntermediate extends Histogram {

    static final double minHistRel = -10;
    static final double maxHistRel = 10;

    public HistogramIntermediate() {
        super(minHistRel, maxHistRel, 0.01, false);
    }
}
