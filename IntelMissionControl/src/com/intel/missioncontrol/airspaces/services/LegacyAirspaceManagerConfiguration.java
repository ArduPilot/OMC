/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.airspaces.services;

import com.google.inject.Inject;
import eu.mavinci.airspace.EAirspaceManager;

public class LegacyAirspaceManagerConfiguration {
    @Inject
    public LegacyAirspaceManagerConfiguration(LocationAwareAirspaceService airspaceService) {
        if (airspaceService instanceof SourceAwareAirspaceService) {
            EAirspaceManager.install(((SourceAwareAirspaceService)airspaceService).getSource());
        }
    }
}
