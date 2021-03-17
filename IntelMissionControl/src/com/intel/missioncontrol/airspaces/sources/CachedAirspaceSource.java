/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.airspaces.sources;

import eu.mavinci.airspace.IAirspace;

import gov.nasa.worldwind.geom.Sector;
import java.util.List;

public interface CachedAirspaceSource {
    List<IAirspace> getCachedAirspacesWithin(Sector boundingBox);
}
