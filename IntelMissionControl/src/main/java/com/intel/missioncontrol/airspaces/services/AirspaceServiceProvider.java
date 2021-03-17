/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.airspaces.services;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.intel.missioncontrol.airspaces.AirspaceProvider;
import com.intel.missioncontrol.settings.AirspacesProvidersSettings;
import com.intel.missioncontrol.settings.ISettingsManager;

public class AirspaceServiceProvider implements Provider<LocationAwareAirspaceService> {

    @Inject
    private ISettingsManager settingsManager;

    @Inject
    @Named("AirMap2AirspaceService")
    private Provider<LocationAwareAirspaceService> airMap2ServiceProvider;

    @Inject
    @Named("BundledAirspaceService")
    private Provider<LocationAwareAirspaceService> bundledServiceProvider;

    @Override
    public LocationAwareAirspaceService get() {
        switch (getAirspaceProvider()) {
            case AIRMAP:
            case AIRMAP2:
                return airMap2ServiceProvider.get();
            default:
                return bundledServiceProvider.get();
        }
    }

    private AirspaceProvider getAirspaceProvider() {
        AirspacesProvidersSettings settings = settingsManager.getSection(AirspacesProvidersSettings.class);
        return settings.getAirspaceProvider();
    }

}
