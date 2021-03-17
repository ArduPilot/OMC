/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.flightplanning.geometry2d;

import com.jme3.math.Vector2f;
import java.util.List;

public abstract class Geometry2D {
    public abstract List<Vector2f> getCornerPoints();
}
