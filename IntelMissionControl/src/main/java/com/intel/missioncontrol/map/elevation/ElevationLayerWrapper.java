/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.elevation;

import com.intel.missioncontrol.measure.Dimension;
import com.intel.missioncontrol.measure.property.AsyncQuantityProperty;
import eu.mavinci.desktop.gui.wwext.WWFactory;
import gov.nasa.worldwind.globes.ElevationModel;
import java.time.Instant;
import org.apache.commons.lang3.NotImplementedException;
import org.asyncfx.beans.property.AsyncBooleanProperty;
import org.asyncfx.beans.property.AsyncDoubleProperty;
import org.asyncfx.beans.property.AsyncObjectProperty;
import org.asyncfx.beans.property.AsyncStringProperty;
import org.asyncfx.beans.property.PropertyMetadata;
import org.asyncfx.beans.property.ReadOnlyAsyncObjectProperty;
import org.asyncfx.beans.property.SimpleAsyncBooleanProperty;
import org.asyncfx.beans.property.SimpleAsyncStringProperty;

public class ElevationLayerWrapper implements IElevationLayer {
    private final ElevationModel elevationModel;
    private final AsyncBooleanProperty enabled;
    private final AsyncStringProperty name;

    public ElevationLayerWrapper(ElevationModel elevationModel) {
        enabled =
            new SimpleAsyncBooleanProperty(
                this, new PropertyMetadata.Builder<Boolean>().initialValue(elevationModel.isEnabled()).create());
        enabled.addListener((observable, oldValue, newValue) -> elevationModel.setEnabled(newValue));

        name =
            new SimpleAsyncStringProperty(
                this, new PropertyMetadata.Builder<String>().initialValue(elevationModel.getName()).create());
        name.addListener((observable, oldValue, newValue) -> elevationModel.setName(newValue));

        this.elevationModel = elevationModel;
    }

    @Override
    public AsyncQuantityProperty<Dimension.Length> shiftProperty() {
        throw new NotImplementedException("shiftProperty");
    }

    @Override
    public AsyncBooleanProperty enabledProperty() {
        return enabled;
    }

    @Override
    public AsyncStringProperty nameProperty() {
        return name;
    }

    @Override
    public AsyncQuantityProperty<Dimension.Storage> diskUsageProperty() {
        throw new NotImplementedException("diskUsageProperty");
    }

    @Override
    public ReadOnlyAsyncObjectProperty<Instant> sourceModifyedDateProperty() {
        throw new NotImplementedException("sourceModifyedDateProperty");
    }

    @Override
    public AsyncObjectProperty<ElevationModelShiftWrapper.ShiftType> elevationModelShiftTypeProperty() {
        throw new NotImplementedException("elevationModelShiftTypeProperty");
    }

    @Override
    public AsyncDoubleProperty importProgressProperty() {
        throw new NotImplementedException("importProgressProperty");
    }

    @Override
    public void autoDetectManualOffset() {
        throw new NotImplementedException("autoDetectManualOffset");
    }

    @Override
    public ElevationModel getElevationModel() {
        return elevationModel;
    }

    @Override
    public void dropCache() {
        WWFactory.dropDataCache(elevationModel);
    }
}
