/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.flightplanning.validators;

import com.intel.flightplanning.core.Waypoint;
import com.intel.flightplanning.core.annotations.NeedsRework;
import com.jme3.collision.Collidable;
import java.util.List;

public class CollissionCheck {
    @NeedsRework
    public List<Waypoint> problematicCollissionWaypoints(Collidable coll) {
        return null;
    }
}
