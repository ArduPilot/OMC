/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.beans.property;

import com.intel.missioncontrol.collections.ArrayMap;
import com.intel.missioncontrol.measure.Dimension;
import com.intel.missioncontrol.measure.SystemOfMeasurement;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.measure.UnitInfo;
import com.intel.missioncontrol.measure.VariantQuantity;
import java.util.Collection;
import java.util.Map;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;

public abstract class AsyncVariantQuantityProperty extends AsyncObjectPropertyBase<VariantQuantity> {

    private final Map<Dimension, UnitInfo<?>> unitInfo = new ArrayMap<>();

    protected AsyncVariantQuantityProperty(VariantQuantityPropertyMetadata metadata) {
        super(metadata);

        for (var info : metadata.getUnitInfo()) {
            this.unitInfo.put(info.getDimension(), info);
        }

        IQuantityStyleProvider quantityStyleProvider = metadata.getQuantityStyleProvider();
        if (quantityStyleProvider != null) {
            quantityStyleProvider
                .systemOfMeasurementProperty()
                .addListener(new WeakChangeListener<SystemOfMeasurement>(this::systemOfMeasurementChanged));
        }
    }

    public Collection<UnitInfo<?>> getUnitInfo() {
        return unitInfo.values();
    }

    private void systemOfMeasurementChanged(
            ObservableValue<? extends SystemOfMeasurement> observable,
            SystemOfMeasurement oldValue,
            SystemOfMeasurement newValue) {
        VariantQuantity oldQuantity = get();
        if (oldQuantity == null) {
            return;
        }

        final Unit<?> newUnit = this.unitInfo.get(oldQuantity.getDimension()).getPreferredUnit(newValue);
        final VariantQuantity value = oldQuantity.convertTo(newUnit).toVariant();
        getMetadata().getExecutor().execute(() -> set(value));
    }

}
