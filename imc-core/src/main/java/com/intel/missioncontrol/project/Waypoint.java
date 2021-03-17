/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project;

import com.intel.missioncontrol.serialization.CompositeDeserializationContext;

public class Waypoint extends AbstractWaypoint {

    public Waypoint() {
        super();
    }

    public Waypoint(Waypoint source) {
        super(source);
    }

    public Waypoint(WaypointSnapshot source) {
        super(source);
    }

    public Waypoint(CompositeDeserializationContext context) {
        super(context);
    }

}
