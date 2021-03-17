/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project;

import com.intel.missioncontrol.project.serialization.DeserializationContext;

public class Waypoint extends AbstractWaypoint {

    public Waypoint() {
        super();
    }

    public Waypoint(IWaypoint source) {
        super(source);
    }

    public Waypoint(DeserializationContext context) {
        super(context);
    }

}
