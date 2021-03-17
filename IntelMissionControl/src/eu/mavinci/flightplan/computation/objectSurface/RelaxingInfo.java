/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.flightplan.computation.objectSurface;

import java.util.LinkedList;

public class RelaxingInfo {
    double forceSumAbs;
    double forceSumX;
    double forceSumY;
    double forceSumZ;

    int neighbourCount;
    LinkedList<Integer> neighbours = new LinkedList<>();
    boolean isDeleted;

    void reset() {
        if (isDeleted) return;
        neighbours.clear();
        forceSumAbs = 0;
        forceSumX = 0;
        forceSumY = 0;
        forceSumZ = 0;
        neighbourCount = 0;
    }
}
