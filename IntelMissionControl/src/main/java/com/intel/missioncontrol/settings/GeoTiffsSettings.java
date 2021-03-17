/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.settings;

import org.asyncfx.beans.AsyncObservable;
import org.asyncfx.beans.property.AsyncListProperty;
import org.asyncfx.beans.property.PropertyMetadata;
import org.asyncfx.beans.property.SimpleAsyncListProperty;
import org.asyncfx.collections.AsyncObservableList;
import org.asyncfx.collections.FXAsyncCollections;

@SettingsMetadata(section = "geoTiffsLoaded")
public class GeoTiffsSettings implements ISettings {

    private AsyncListProperty<GeoTiffSettings> geoTiffs =
        new SimpleAsyncListProperty<>(
            this,
            new PropertyMetadata.Builder<AsyncObservableList<GeoTiffSettings>>()
                .initialValue(
                    FXAsyncCollections.observableArrayList(
                        geoTiff ->
                            new AsyncObservable[] {
                                geoTiff.enabledProperty(),
                                geoTiff.cacheNameProperty(),
                                geoTiff.manualElevationShiftProperty(),
                                geoTiff.elevationModelShiftTypeProperty()
                            }))
                .create());

    public AsyncListProperty<GeoTiffSettings> geoTiffsProperty() {
        return geoTiffs;
    }

}
