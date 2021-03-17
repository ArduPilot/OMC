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
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import org.asyncfx.beans.property.AsyncObjectPropertyBase;

public abstract class AsyncQuantityProperty<Q extends Quantity<Q>> extends AsyncObjectPropertyBase<Quantity<Q>> {

    private Unit<Q> preferredUnit;

    protected AsyncQuantityProperty(QuantityPropertyMetadata<Q> metadata) {
        super(metadata);

        IQuantityStyleProvider quantityStyleProvider = metadata.getQuantityStyleProvider();
        if (quantityStyleProvider == null) {
            throw new IllegalArgumentException(
                "Metadata must provide a " + IQuantityStyleProvider.class.getSimpleName() + " value.");
        }

        UnitInfo<Q> unitInfo = metadata.getUnitInfo();
        if (unitInfo == null) {
            throw new IllegalArgumentException("Metadata must provide a " + UnitInfo.class.getSimpleName() + " value.");
        }

        quantityStyleProvider
            .systemOfMeasurementProperty()
            .addListener(new WeakChangeListener<SystemOfMeasurement>(this::systemOfMeasurementChanged));

        preferredUnit = unitInfo.getPreferredUnit(quantityStyleProvider.getSystemOfMeasurement());
    }

    @Override
    public QuantityPropertyMetadata<Q> getMetadata() {
        return (QuantityPropertyMetadata<Q>)super.getMetadata();
    }

    public UnitInfo<Q> getUnitInfo() {
        return getMetadata().getUnitInfo();
    }

    @Override
    public Quantity<Q> get() {
        Quantity<Q> value = super.get();
        if (value == null) {
            return null;
        }

        return value.convertTo(preferredUnit);
    }

    @Override
    public void set(Quantity<Q> newValue) {
        if (newValue == null) {
            super.set(null);
        } else {
            super.set(newValue.convertTo(preferredUnit));
        }
    }

    private void systemOfMeasurementChanged(
            ObservableValue<? extends SystemOfMeasurement> observable,
            SystemOfMeasurement oldValue,
            SystemOfMeasurement newValue) {
        QuantityPropertyMetadata<Q> metadata = getMetadata();
        preferredUnit = metadata.getUnitInfo().getPreferredUnit(newValue);

        if (isBound()) {
            return;
        }

        Quantity<Q> oldQuantity = get();
        if (oldQuantity == null) {
            return;
        }

        final Quantity<Q> value = oldQuantity.convertTo(metadata.getUnitInfo().getPreferredUnit(newValue));
        metadata.getDispatcher().run(() -> set(value));
    }

}
