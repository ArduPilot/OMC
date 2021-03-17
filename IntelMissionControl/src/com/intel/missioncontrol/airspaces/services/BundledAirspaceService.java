/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.airspaces.services;

import com.google.inject.Inject;
import com.intel.missioncontrol.airspaces.cache.airspace.AirspacesGeometryIndex;
import com.intel.missioncontrol.airspaces.sources.AirspaceSource;
import com.intel.missioncontrol.airspaces.sources.OpenAirAirspacesSource;
import com.intel.missioncontrol.common.IPathProvider;
import eu.mavinci.airspace.IAirspace;
import gov.nasa.worldwind.geom.Sector;
import java.util.List;

public class BundledAirspaceService implements LocationAwareAirspaceService, SourceAwareAirspaceService {
    private OpenAirAirspacesSource bundledAirspacesSource;

    final IPathProvider pathProvider;
    final AirspacesGeometryIndex geometryIndex;

    @Inject
    public BundledAirspaceService(IPathProvider pathProvider, AirspacesGeometryIndex geometryIndex) {
        this.pathProvider = pathProvider;
        this.geometryIndex = geometryIndex;
    }

    @Override
    public List<IAirspace> getAirspacesWithin(Sector boundingBox, int bufferInMeters) {
        return bundledAirspacesSource.getAirspacesWithin(boundingBox);
    }

    @Override
    public AirspaceSource getSource() {
        // lazy loading to speed up initial program startup... since this class is now also loaded even if airmap is
        // selected
        if (bundledAirspacesSource == null) {
            bundledAirspacesSource =
                new OpenAirAirspacesSource(pathProvider.getLocalAirspacesFolder().toFile(), geometryIndex);
        }

        return bundledAirspacesSource;
    }
}
