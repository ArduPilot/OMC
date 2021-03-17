/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.elevation;

import com.intel.missioncontrol.measure.Dimension;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.property.AsyncQuantityProperty;
import gov.nasa.worldwind.globes.ElevationModel;
import java.time.Instant;
import org.asyncfx.beans.property.AsyncBooleanProperty;
import org.asyncfx.beans.property.AsyncDoubleProperty;
import org.asyncfx.beans.property.AsyncObjectProperty;
import org.asyncfx.beans.property.AsyncStringProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncObjectProperty;

public interface IElevationLayer {

    AsyncQuantityProperty<Dimension.Length> shiftProperty();

    default Quantity<Dimension.Length> getShift() {
        return shiftProperty().get();
    }

    default void setShift(Quantity<Dimension.Length> value) {
        shiftProperty().set(value);
    }

    AsyncBooleanProperty enabledProperty();

    AsyncStringProperty nameProperty();

    AsyncQuantityProperty<Dimension.Storage> diskUsageProperty();

    ReadOnlyAsyncObjectProperty<Instant> sourceModifyedDateProperty();

    AsyncObjectProperty<ElevationModelShiftWrapper.ShiftType> elevationModelShiftTypeProperty();

    AsyncDoubleProperty importProgressProperty();

    void autoDetectManualOffset();

    ElevationModel getElevationModel();

    void dropCache();

}
