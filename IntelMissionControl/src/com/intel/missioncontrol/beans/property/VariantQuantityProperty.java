/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.beans.property;

import com.intel.missioncontrol.collections.ArrayMap;
import com.intel.missioncontrol.common.Expect;
import com.intel.missioncontrol.measure.Dimension;
import com.intel.missioncontrol.measure.SystemOfMeasurement;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.measure.UnitInfo;
import com.intel.missioncontrol.measure.VariantQuantity;
import java.util.Collection;
import java.util.Map;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;

@SuppressWarnings("FieldCanBeLocal")
public abstract class VariantQuantityProperty extends ObjectPropertyBase<VariantQuantity> {

    private final Map<Dimension, UnitInfo<?>> unitInfo = new ArrayMap<>();
    private final ChangeListener<SystemOfMeasurement> systemOfMeasurementChanged;
    private SystemOfMeasurement currentSystemOfMeasurement;

    protected VariantQuantityProperty(
            IQuantityStyleProvider quantityStyleProvider, UnitInfo<?>[] unitInfo, VariantQuantity initialValue) {
        super(null);

        Expect.notNull(quantityStyleProvider, "quantityStyleProvider");
        Expect.notNullOrEmpty(unitInfo, "unitInfo");

        for (var info : unitInfo) {
            this.unitInfo.put(info.getDimension(), info);
        }

        systemOfMeasurementChanged =
            (observable, oldValue, newValue) -> {
                currentSystemOfMeasurement = newValue;

                if (isBound()) {
                    return;
                }

                VariantQuantity oldQuantity = get();
                if (oldQuantity == null) {
                    return;
                }

                Unit<?> newUnit = this.unitInfo.get(oldQuantity.getDimension()).getPreferredUnit(newValue);
                set(oldQuantity.convertTo(newUnit).toVariant());
            };

        quantityStyleProvider
            .systemOfMeasurementProperty()
            .addListener(new WeakChangeListener<>(systemOfMeasurementChanged));

        currentSystemOfMeasurement = quantityStyleProvider.getSystemOfMeasurement();

        set(initialValue);
    }

    public Collection<UnitInfo<?>> getUnitInfo() {
        return unitInfo.values();
    }

    @Override
    public void set(VariantQuantity newValue) {
        if (newValue == null) {
            super.set(null);
        } else {
            UnitInfo<?> unitInfo = this.unitInfo.get(newValue.getDimension());
            boolean found = false;
            for (Unit<?> allowedUnit : unitInfo.getAllowedUnits(currentSystemOfMeasurement)) {
                if (newValue.getUnit() == allowedUnit) {
                    found = true;
                    break;
                }
            }

            super.set(
                found
                    ? newValue
                    : newValue.convertTo(unitInfo.getPreferredUnit(currentSystemOfMeasurement)).toVariant());
        }
    }

}
