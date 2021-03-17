/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.flightplanning.three;

import com.jme3.math.Vector3f;

public class LinePoint {
    public Vector3f p;
    public Vector3f normal;
    public double curving; // 1 means not curved, mess them one inner corner, larger 1 outer corner
}
