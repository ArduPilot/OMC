/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.worldwind.layers.aircraft;

import com.intel.missioncontrol.beans.property.AsyncBooleanProperty;
import com.intel.missioncontrol.beans.property.PropertyMetadata;
import com.intel.missioncontrol.beans.property.SimpleAsyncBooleanProperty;
import com.intel.missioncontrol.settings.ISettings;
import com.intel.missioncontrol.settings.SettingsMetadata;

@SettingsMetadata(section = "aircraftLayers")
public class AircraftLayerVisibilitySettings implements ISettings {

    private final AsyncBooleanProperty model3D =
        new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().initialValue(true).create());

    private final AsyncBooleanProperty track =
        new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().initialValue(true).create());

    private final AsyncBooleanProperty boundingBox =
        new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().initialValue(true).create());

    private final AsyncBooleanProperty coveragePreview =
        new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().initialValue(true).create());

    private final AsyncBooleanProperty imageAreaPreview =
        new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().initialValue(false).create());

    private final AsyncBooleanProperty cameraFieldOfView =
        new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().initialValue(false).create());

    private final AsyncBooleanProperty groundStation =
        new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().initialValue(false).create());

    private final AsyncBooleanProperty startingPosition =
        new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().initialValue(false).create());

    private final AsyncBooleanProperty flightPlan =
        new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().initialValue(false).create());

    public AsyncBooleanProperty model3DProperty() {
        return model3D;
    }

    public AsyncBooleanProperty trackProperty() {
        return track;
    }

    public AsyncBooleanProperty boundingBoxProperty() {
        return boundingBox;
    }

    public AsyncBooleanProperty coveragePreviewProperty() {
        return coveragePreview;
    }

    public AsyncBooleanProperty imageAreaPreviewProperty() {
        return imageAreaPreview;
    }

    public AsyncBooleanProperty cameraFieldOfViewProperty() {
        return cameraFieldOfView;
    }

    public AsyncBooleanProperty groundStationProperty() {
        return groundStation;
    }

    public AsyncBooleanProperty flightPlanProperty() {
        return flightPlan;
    }

    public AsyncBooleanProperty startingPositionProperty() {
        return startingPosition;
    }

    public boolean isBoundingBox() {
        return boundingBox.get();
    }

    public boolean isCameraFieldOfView() {
        return cameraFieldOfView.get();
    }

    public boolean isCoveragePreview() {
        return coveragePreview.get();
    }

    public boolean isFlightPlan() {
        return flightPlan.get();
    }

    public boolean isGroundStation() {
        return groundStation.get();
    }

    public boolean isModel3D() {
        return model3D.get();
    }

    public boolean isTrack() {
        return track.get();
    }

    public Boolean getStartingPosition() {
        return startingPosition.get();
    }

}
