/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.airspaces.services;

import eu.mavinci.airspace.IAirspace;

import gov.nasa.worldwind.geom.Sector;
import java.util.List;

public interface LocationAwareAirspaceService {
    List<IAirspace> getAirspacesWithin(Sector boundingBox, int bufferInMeters);
}
