/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.credits;

import com.google.inject.Inject;
import com.intel.missioncontrol.airspaces.AirspaceProvider;
import com.intel.missioncontrol.beans.property.AsyncListProperty;
import com.intel.missioncontrol.beans.property.PropertyMetadata;
import com.intel.missioncontrol.beans.property.SimpleAsyncListProperty;
import com.intel.missioncontrol.collections.AsyncObservableList;
import com.intel.missioncontrol.collections.FXAsyncCollections;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.settings.AirspacesProvidersSettings;

public class AirspacesProvidersCredits implements IMapCreditsSource {

    private final AsyncListProperty<MapCreditViewModel> mapCredits =
        new SimpleAsyncListProperty<>(
            this,
            new PropertyMetadata.Builder<AsyncObservableList<MapCreditViewModel>>()
                .initialValue(FXAsyncCollections.observableArrayList())
                .create());

    @Inject
    public AirspacesProvidersCredits(
            AirspacesProvidersSettings airspacesProvidersSettings,
            IMapCreditsManager mapCreditsManager,
            ILanguageHelper languageHelper) {
        mapCreditsManager.register(this);
        if (airspacesProvidersSettings.getAirspaceProvider() == AirspaceProvider.AIRMAP2
                || airspacesProvidersSettings.getAirspaceProvider() == AirspaceProvider.AIRMAP) {
            // add airmap credits
            mapCredits.setAll(
                new MapCreditViewModel(
                    languageHelper.getString(
                        "com.intel.missioncontrol.map.layers.copyrights.airspaces.provider.airmap.name"),
                    languageHelper.getString(
                        "com.intel.missioncontrol.map.layers.copyrights.airspaces.provider.airmap.url")));
        } else {
            mapCredits.clear();
        }
    }

    @Override
    public AsyncListProperty<MapCreditViewModel> mapCreditsProperty() {
        return mapCredits;
    }
}
