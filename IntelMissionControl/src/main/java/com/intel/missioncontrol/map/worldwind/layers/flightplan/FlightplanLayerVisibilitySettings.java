/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.worldwind.layers.flightplan;

import org.asyncfx.beans.property.AsyncBooleanProperty;
import org.asyncfx.beans.property.PropertyMetadata;
import org.asyncfx.beans.property.SimpleAsyncBooleanProperty;
import com.intel.missioncontrol.settings.ISettings;
import com.intel.missioncontrol.settings.SettingsMetadata;

@SettingsMetadata(section = "flightplanVisibility")
public class FlightplanLayerVisibilitySettings implements ISettings {

    private final AsyncBooleanProperty aoiVisible =
        new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().initialValue(true).create());

    private final AsyncBooleanProperty waypointVisible =
        new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().initialValue(true).create());

    private final AsyncBooleanProperty flightLineVisible =
        new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().initialValue(true).create());

    private final AsyncBooleanProperty startLandVisible =
        new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().initialValue(true).create());

    private final AsyncBooleanProperty coveragePreviewVisible =
        new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().initialValue(true).create());

    private final AsyncBooleanProperty showVoxels =
        new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().initialValue(false).create());

    private final AsyncBooleanProperty showVoxelsDilated =
        new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().initialValue(false).create());

    private final AsyncBooleanProperty showCamPreview =
        new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().initialValue(false).create());

    private final AsyncBooleanProperty showCurrentFlightplan =
        new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().initialValue(true).create());

    private final AsyncBooleanProperty showOtherFlightplans =
        new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().initialValue(false).create());

    public final AsyncBooleanProperty aoiVisibleProperty() {
        return aoiVisible;
    }

    public final AsyncBooleanProperty waypointVisibleProperty() {
        return waypointVisible;
    }

    public final AsyncBooleanProperty flightLineVisibleProperty() {
        return flightLineVisible;
    }

    public final AsyncBooleanProperty startLandVisibleProperty() {
        return startLandVisible;
    }

    public final AsyncBooleanProperty coveragePreviewVisibleProperty() {
        return coveragePreviewVisible;
    }

    public AsyncBooleanProperty showCamPreviewProperty() {
        return showCamPreview;
    }

    public AsyncBooleanProperty showVoxelsDilatedProperty() {
        return showVoxelsDilated;
    }

    public AsyncBooleanProperty showVoxelsProperty() {
        return showVoxels;
    }

    public AsyncBooleanProperty showCurrentFlightplanProperty() {
        return showCurrentFlightplan;
    }

    public AsyncBooleanProperty showOtherFlightplansProperty() {
        return showOtherFlightplans;
    }

    public boolean isAoiVisible() {
        return aoiVisible.get();
    }

    public boolean isCoveragePreviewVisible() {
        return coveragePreviewVisible.get();
    }

    public boolean isFlightLineVisible() {
        return flightLineVisible.get();
    }

    public boolean isShowCamPreview() {
        return showCamPreview.get();
    }

    public boolean isShowCurrentFlightplan() {
        return showCurrentFlightplan.get();
    }

    public boolean isShowVoxels() {
        return showVoxels.get();
    }

    public boolean isShowOtherFlightplans() {
        return showOtherFlightplans.get();
    }

    public boolean isShowVoxelsDilated() {
        return showVoxelsDilated.get();
    }

    public boolean isStartLandVisible() {
        return startLandVisible.get();
    }

    public boolean isWaypointVisible() {
        return waypointVisible.get();
    }

}
