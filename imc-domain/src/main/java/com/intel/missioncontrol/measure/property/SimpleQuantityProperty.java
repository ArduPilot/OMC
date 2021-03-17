/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.measure.property;

import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.SystemOfMeasurement;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.measure.UnitInfo;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import org.checkerframework.checker.nullness.qual.Nullable;

@SuppressWarnings("FieldCanBeLocal")
public class SimpleQuantityProperty<Q extends Quantity<Q>> extends ObjectPropertyBase<Quantity<Q>>
        implements QuantityProperty<Q> {

    private static final Object DEFAULT_BEAN = null;
    private static final String DEFAULT_NAME = "";

    private final Object bean;
    private final String name;
    private final UnitInfo<Q> unitInfo;
    private final ChangeListener<SystemOfMeasurement> systemOfMeasurementChanged;
    private SystemOfMeasurement currentSystemOfMeasurement;

    public SimpleQuantityProperty(IQuantityStyleProvider quantityStyleProvider, UnitInfo<Q> unitInfo) {
        this(DEFAULT_BEAN, DEFAULT_NAME, quantityStyleProvider, unitInfo, null);
    }

    public SimpleQuantityProperty(
            IQuantityStyleProvider quantityStyleProvider, UnitInfo<Q> unitInfo, Quantity<Q> initialValue) {
        this(DEFAULT_BEAN, DEFAULT_NAME, quantityStyleProvider, unitInfo, initialValue);
    }

    public SimpleQuantityProperty(
            @Nullable Object bean,
            @Nullable String name,
            IQuantityStyleProvider quantityStyleProvider,
            UnitInfo<Q> unitInfo) {
        this(bean, name, quantityStyleProvider, unitInfo, null);
    }

    public SimpleQuantityProperty(
            @Nullable Object bean,
            @Nullable String name,
            IQuantityStyleProvider quantityStyleProvider,
            UnitInfo<Q> unitInfo,
            @Nullable Quantity<Q> initialValue) {
        super(null);
        this.bean = bean;
        this.name = (name == null) ? DEFAULT_NAME : name;
        this.unitInfo = unitInfo;
        this.systemOfMeasurementChanged =
            (observable, oldValue, newValue) -> {
                currentSystemOfMeasurement = newValue;

                if (isBound()) {
                    return;
                }

                Quantity<Q> oldQuantity = get();
                if (oldQuantity == null) {
                    return;
                }

                set(oldQuantity);
            };

        quantityStyleProvider
            .systemOfMeasurementProperty()
            .addListener(new WeakChangeListener<>(systemOfMeasurementChanged));

        currentSystemOfMeasurement = quantityStyleProvider.getSystemOfMeasurement();

        set(initialValue);
    }

    @Override
    public Object getBean() {
        return bean;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public UnitInfo<Q> getUnitInfo() {
        return this.unitInfo;
    }

    @Override
    public void set(Quantity<Q> newValue) {
        if (newValue == null) {
            super.set(null);
        } else {
            boolean found = false;
            for (Unit<?> allowedUnit : unitInfo.getAllowedUnits(currentSystemOfMeasurement)) {
                if (newValue.getUnit() == allowedUnit) {
                    found = true;
                    break;
                }
            }

            super.set(found ? newValue : newValue.convertTo(unitInfo.getPreferredUnit(currentSystemOfMeasurement)));
        }
    }

}
