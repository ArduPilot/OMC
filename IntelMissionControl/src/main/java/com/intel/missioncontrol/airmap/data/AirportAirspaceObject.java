/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.airmap.data;

import java.util.List;

public class AirportAirspaceObject extends AirSpaceBase {
    public List<Rules> rules;

    public static class Rules {
        public String name;
        public GeoJson.Geometry geometry;
        public double min_circle_radius;
    }

    /**
     * useful for small airspaces without property boundary
     */
    public GeoJson.Geometry getAirspaceRule() {
        return (rules != null && rules.size() > 0) ? rules.get(0).geometry : null;
    }
}
