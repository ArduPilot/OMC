/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.settings;

import com.airmap.airmapsdk.networking.services.MappingService;
import com.intel.missioncontrol.airmap.AirMap2Source;
import com.intel.missioncontrol.airmap.layer.AirMapTileLoader2;
import com.intel.missioncontrol.airspaces.AirspaceProvider;
import eu.mavinci.airspace.EAirspaceManager;
import java.util.HashMap;
import org.asyncfx.beans.AsyncObservable;
import org.asyncfx.beans.property.AsyncBooleanProperty;
import org.asyncfx.beans.property.AsyncDoubleProperty;
import org.asyncfx.beans.property.AsyncIntegerProperty;
import org.asyncfx.beans.property.AsyncListProperty;
import org.asyncfx.beans.property.AsyncObjectProperty;
import org.asyncfx.beans.property.AsyncStringProperty;
import org.asyncfx.beans.property.PropertyMetadata;
import org.asyncfx.beans.property.SimpleAsyncBooleanProperty;
import org.asyncfx.beans.property.SimpleAsyncDoubleProperty;
import org.asyncfx.beans.property.SimpleAsyncIntegerProperty;
import org.asyncfx.beans.property.SimpleAsyncListProperty;
import org.asyncfx.beans.property.SimpleAsyncObjectProperty;
import org.asyncfx.beans.property.SimpleAsyncStringProperty;
import org.asyncfx.collections.AsyncObservableList;
import org.asyncfx.collections.FXAsyncCollections;
import org.asyncfx.collections.LockedList;

@SettingsMetadata(section = "airspaces")
public class AirspacesProvidersSettings implements ISettings {

    private static final AirspaceProvider FAILBACK_AIRSPACE_PROVIDER = AirspaceProvider.LOCAL_BUNDLE;

    private final AsyncDoubleProperty acceptedElevation =
        new SimpleAsyncDoubleProperty(this, new PropertyMetadata.Builder<Number>().initialValue(1_000_000).create());

    private final AsyncBooleanProperty useAirspaceDataForPlanning =
        new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().initialValue(true).create());

    private final AsyncDoubleProperty searchFreeDistance =
        new SimpleAsyncDoubleProperty(this, new PropertyMetadata.Builder<Number>().initialValue(4_000).create());

    private final AsyncDoubleProperty regularSearchZoneDistance =
        new SimpleAsyncDoubleProperty(this, new PropertyMetadata.Builder<Number>().initialValue(10_000).create());

    private final AsyncDoubleProperty bigSearchZoneDistance =
        new SimpleAsyncDoubleProperty(this, new PropertyMetadata.Builder<Number>().initialValue(20_000).create());

    private final AsyncStringProperty cacheDirectory =
        new SimpleAsyncStringProperty(
            this, new PropertyMetadata.Builder<String>().initialValue("airspaces/cache").create());

    private final AsyncIntegerProperty airmapAirspacesEntriesCachedInMemory =
        new SimpleAsyncIntegerProperty(this, new PropertyMetadata.Builder<Number>().initialValue(1000).create());

    private final AsyncIntegerProperty airmapAirspacesPersistentCacheSizeInMb =
        new SimpleAsyncIntegerProperty(this, new PropertyMetadata.Builder<Number>().initialValue(1024).create());

    private final AsyncIntegerProperty airmapCachedAirspacesEntriesExpirationPeriodInDays =
        new SimpleAsyncIntegerProperty(this, new PropertyMetadata.Builder<Number>().initialValue(60).create());

    private final AsyncIntegerProperty airmapSearchZonesPersistentCacheSizeInMb =
        new SimpleAsyncIntegerProperty(this, new PropertyMetadata.Builder<Number>().initialValue(32).create());

    private final AsyncIntegerProperty airmapSearchZonesEntriesCachedInMemory =
        new SimpleAsyncIntegerProperty(this, new PropertyMetadata.Builder<Number>().initialValue(1000).create());

    private final AsyncIntegerProperty airmapSearchZonesExpirationPeriodInHours =
        new SimpleAsyncIntegerProperty(this, new PropertyMetadata.Builder<Number>().initialValue(24).create());

    private final AsyncDoubleProperty maxAltitudeAboveGround =
        new SimpleAsyncDoubleProperty(this, new PropertyMetadata.Builder<Number>().initialValue(100).create()); // meter

    private final AsyncDoubleProperty maxAltitudeAboveSeaLevel =
        new SimpleAsyncDoubleProperty(
            this, new PropertyMetadata.Builder<Number>().initialValue(4_000).create()); // meter

    private final AsyncDoubleProperty minimumTimeLanding =
        new SimpleAsyncDoubleProperty(
            this, new PropertyMetadata.Builder<Number>().initialValue(30).create()); // minutes

    private final AsyncDoubleProperty minimumHorizontalDistance =
        new SimpleAsyncDoubleProperty(
            this, new PropertyMetadata.Builder<Number>().initialValue(3_000).create()); // meter

    private final AsyncDoubleProperty minimumVerticalDistance =
        new SimpleAsyncDoubleProperty(this, new PropertyMetadata.Builder<Number>().initialValue(300).create()); // meter

    private final AsyncObjectProperty<AirspaceProvider> airspaceProvider =
        new SimpleAsyncObjectProperty<>(
            this, new PropertyMetadata.Builder<AirspaceProvider>().initialValue(AirspaceProvider.AIRMAP2).create());

    private final AsyncBooleanProperty showProhibitedFlightZonesOnly =
        new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().initialValue(false).create());

    private final AsyncListProperty<AirspacesTypeEnabledSettings> airspacesEnabled =
        new SimpleAsyncListProperty<>(
            this,
            new PropertyMetadata.Builder<AsyncObservableList<AirspacesTypeEnabledSettings>>()
                .initialValue(
                    FXAsyncCollections.observableArrayList(
                        param -> new AsyncObservable[] {param.nameProperty(), param.enabledProperty()}))
                .create());

    private final transient HashMap<MappingService.AirMapAirspaceType, AsyncBooleanProperty> airspacesEnabledHash =
        new HashMap<>();

    public AirspacesProvidersSettings() {
        maxAltitudeAboveGroundProperty()
            .addListener(
                (observable, oldValue, newValue) ->
                    EAirspaceManager.setGolfMeters(getMaxAltitudeAboveGround(), getMaxAltitudeAboveSeaLevel()));
        maxAltitudeAboveSeaLevelProperty()
            .addListener(
                (observable, oldValue, newValue) ->
                    EAirspaceManager.setGolfMeters(getMaxAltitudeAboveGround(), getMaxAltitudeAboveSeaLevel()));
        EAirspaceManager.setGolfMeters(getMaxAltitudeAboveGround(), getMaxAltitudeAboveSeaLevel());
    }

    @Override
    public void onLoaded() {
        if (getAirspaceProvider() == AirspaceProvider.AIRMAP) {
            airspaceProvider.set(AirspaceProvider.AIRMAP2);
        }

        // make sure we have a fast and unique lookup table of boolean properties which is also persistently
        // stored/restored
        for (MappingService.AirMapAirspaceType entry : AirMapTileLoader2.getAirmapSearchTypes()) {
            AirspacesTypeEnabledSettings found = null;
            try (LockedList<AirspacesTypeEnabledSettings> list = airspacesEnabled.lock()) {
                for (AirspacesTypeEnabledSettings set : list) {
                    if (set.getName().equals(entry.toString())) {
                        found = set;
                        break;
                    }
                }

                if (found == null) {
                    found = new AirspacesTypeEnabledSettings(entry.toString(), entry.isDefaultEnabled());
                    list.add(found);
                }
            }

            airspacesEnabledHash.put(entry, found.enabledProperty());
        }

        AirMap2Source.getInstance().bindToSettings(this);
    }

    public AsyncListProperty<AirspacesTypeEnabledSettings> airspacesEnabledProperty() {
        return airspacesEnabled;
    }

    public AsyncDoubleProperty acceptedElevationProperty() {
        return acceptedElevation;
    }

    public AsyncDoubleProperty regularSearchZoneDistanceProperty() {
        return regularSearchZoneDistance;
    }

    public AsyncDoubleProperty searchFreeDistanceProperty() {
        return searchFreeDistance;
    }

    public AsyncDoubleProperty bigSearchZoneDistanceProperty() {
        return bigSearchZoneDistance;
    }

    public AsyncStringProperty cacheDirectory() {
        return cacheDirectory;
    }

    public AsyncIntegerProperty airmapAirspacesPersistentCacheSizeProperty() {
        return airmapAirspacesPersistentCacheSizeInMb;
    }

    public AsyncIntegerProperty airmapAirspacesCachedInMemoryProperty() {
        return airmapAirspacesEntriesCachedInMemory;
    }

    public AsyncIntegerProperty airmapCachedAirspacesEntriesExpirationPeriodInDaysProperty() {
        return airmapCachedAirspacesEntriesExpirationPeriodInDays;
    }

    public AsyncIntegerProperty airmapSearchZonesPersistentCacheSizeProperty() {
        return airmapSearchZonesPersistentCacheSizeInMb;
    }

    public AsyncIntegerProperty airmapSearchZonesCachedInMemoryProperty() {
        return airmapSearchZonesEntriesCachedInMemory;
    }

    public AsyncIntegerProperty airmapSearchZonesExpirationPeriodProperty() {
        return airmapSearchZonesExpirationPeriodInHours;
    }

    public AsyncDoubleProperty maxAltitudeAboveGroundProperty() {
        return maxAltitudeAboveGround;
    }

    public AsyncDoubleProperty maxAltitudeAboveSeaLevelProperty() {
        return maxAltitudeAboveSeaLevel;
    }

    public double getMaxAltitudeAboveGround() {
        return maxAltitudeAboveGround.get();
    }

    public double getMaxAltitudeAboveSeaLevel() {
        return maxAltitudeAboveSeaLevel.get();
    }

    public AsyncDoubleProperty minimumTimeLandingProperty() {
        return minimumTimeLanding;
    }

    public AsyncDoubleProperty minimumHorizontalDistanceProperty() {
        return minimumHorizontalDistance;
    }

    public AsyncDoubleProperty minimumVerticalDistanceProperty() {
        return minimumVerticalDistance;
    }

    public AsyncObjectProperty<AirspaceProvider> airspaceProviderProperty() {
        return airspaceProvider;
    }

    public AirspaceProvider getAirspaceProvider() {
        return airspaceProvider.getValue() != null ? airspaceProvider.getValue() : FAILBACK_AIRSPACE_PROVIDER;
    }

    public boolean showProhibitedFlightZonesOnly() {
        return showProhibitedFlightZonesOnly.getValue();
    }

    public AsyncBooleanProperty showAirspaceTypeProperty(MappingService.AirMapAirspaceType type) {
        return airspacesEnabledHash.get(type);
    }

    public boolean isUseAirspaceDataForPlanning() {
        return useAirspaceDataForPlanning.get();
    }

    public AsyncBooleanProperty useAirspaceDataForPlanningProperty() {
        return useAirspaceDataForPlanning;
    }

}
