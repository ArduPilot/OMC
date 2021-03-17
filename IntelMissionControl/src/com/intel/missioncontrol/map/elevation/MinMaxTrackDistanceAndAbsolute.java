/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.elevation;

import eu.mavinci.core.helper.MinMaxPair;

public class MinMaxTrackDistanceAndAbsolute {
    public final MinMaxPair minMaxDistanceToGround;
    public final MinMaxPair minMaxGroundHeight;

    public MinMaxTrackDistanceAndAbsolute(MinMaxPair minMaxDistanceToGround, MinMaxPair minMaxGroundHeight) {
        this.minMaxDistanceToGround = minMaxDistanceToGround;
        this.minMaxGroundHeight = minMaxGroundHeight;
    }

    public MinMaxTrackDistanceAndAbsolute() {
        this.minMaxDistanceToGround = new MinMaxPair();
        this.minMaxGroundHeight = new MinMaxPair();
    }
}
