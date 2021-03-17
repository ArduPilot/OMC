/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.flightplanning.geometry2d;

import com.jme3.math.Vector2f;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** A single position on a 2D map */
public class Point extends Geometry2D {
    Vector2f point;

    @Override
    public List<Vector2f> getCornerPoints() {
        return new ArrayList<Vector2f>(Collections.singleton(point));
    }
}
