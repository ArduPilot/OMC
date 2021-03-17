/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.elevation;

import eu.mavinci.core.helper.MinMaxPair;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import java.util.LinkedList;

public class ElevationList {
    public final LinkedList<Position> p = new LinkedList<Position>();
    public final MinMaxPair minMaxElev = new MinMaxPair();

    void add(LatLon latLon, double elev) {
        p.add(new Position(latLon, elev));
        minMaxElev.update(elev);
    }

    void add(ElevationList other) {
        for (Position pos : other.p) {
            p.add(pos);
        }

        minMaxElev.enlarge(other.minMaxElev);
    }

    @Override
    public String toString() {
        return p + " minMaxElev:" + minMaxElev;
    }
}
