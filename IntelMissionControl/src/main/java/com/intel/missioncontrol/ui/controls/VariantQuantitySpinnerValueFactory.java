/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.controls;

import com.intel.missioncontrol.measure.AngleStyle;
import com.intel.missioncontrol.measure.Dimension;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.QuantityArithmetic;
import com.intel.missioncontrol.measure.SystemOfMeasurement;
import com.intel.missioncontrol.measure.TimeStyle;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.measure.VariantQuantity;
import com.intel.missioncontrol.measure.property.IQuantityStyleProvider;
import com.intel.missioncontrol.measure.property.VariantQuantityProperty;
import java.util.Map;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.SpinnerValueFactory;
import org.asyncfx.collections.ArrayMap;

public class VariantQuantitySpinnerValueFactory extends SpinnerValueFactory<VariantQuantity>
        implements IQuantityStyleProvider {

    public static class FactorySettings<Q extends Quantity<Q>> {
        private final Dimension dimension;
        private final VariantQuantity min;
        private final VariantQuantity max;
        private final double amountToStepBy;
        private final boolean wrapAround;
        private final int significantDigits;
        private final int maxFractionDigits;

        public FactorySettings(
                Class<Q> dimensionClass,
                Quantity<Q> min,
                Quantity<Q> max,
                double amountToStepBy,
                boolean wrapAround,
                int significantDigits,
                int maxFractionDigits) {
            this.dimension = Unit.getDimension(dimensionClass);
            this.min = min.toVariant();
            this.max = max.toVariant();
            this.amountToStepBy = amountToStepBy;
            this.wrapAround = wrapAround;
            this.significantDigits = significantDigits;
            this.maxFractionDigits = maxFractionDigits;
        }

        public Dimension getDimension() {
            return dimension;
        }

        public VariantQuantity getMax() {
            return max;
        }

        public VariantQuantity getMin() {
            return min;
        }

        public double getAmountToStepBy() {
            return amountToStepBy;
        }

        public boolean isWrapAround() {
            return wrapAround;
        }

        public int getSignificantDigits() {
            return significantDigits;
        }

        public int getMaxFractionDigits() {
            return maxFractionDigits;
        }
    }

    private final IQuantityStyleProvider quantityStyleProvider;
    private final Map<Dimension, FactorySettings<?>> settingsMap = new ArrayMap<>();

    public VariantQuantitySpinnerValueFactory(
            IQuantityStyleProvider quantityStyleProvider,
            QuantityArithmetic arithmetic,
            VariantQuantityProperty sourceProperty,
            FactorySettings<?>... factorySettings) {
        this.quantityStyleProvider = quantityStyleProvider;
        var converter = new VariantQuantityConverter(quantityStyleProvider, arithmetic);
        for (var unitInfo : sourceProperty.getUnitInfo()) {
            FactorySettings<?> settings = null;
            for (var s : factorySettings) {
                if (unitInfo.getDimension() == s.getDimension()) {
                    settings = s;
                    break;
                }
            }

            if (settings == null) {
                throw new IllegalArgumentException(
                    "No spinner settings specified for source property dimension " + unitInfo.getDimension());
            }

            converter.addRule(unitInfo, settings.getSignificantDigits(), settings.getMaxFractionDigits());
            settingsMap.put(unitInfo.getDimension(), settings);
        }

        setConverter(converter);

        valueProperty().bindBidirectional(sourceProperty);
        valueProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue == null) {
                        return;
                    }

                    var settings = settingsMap.get(newValue.getDimension());
                    converter.setImplicitUnit(newValue.getUnit());
                    boolean wrapAround = isWrapAround();
                    if (newValue.compareTo(settings.getMin()) < 0) {
                        valueProperty().set(wrapAround ? settings.getMax() : settings.getMin());
                    } else if (newValue.compareTo(settings.getMax()) > 0) {
                        valueProperty().set(wrapAround ? settings.getMin() : settings.getMax());
                    } else {
                        valueProperty().set(newValue);
                    }
                });
    }

    @Override
    public ObservableValue<SystemOfMeasurement> systemOfMeasurementProperty() {
        return quantityStyleProvider.systemOfMeasurementProperty();
    }

    @Override
    public ObservableValue<AngleStyle> angleStyleProperty() {
        return quantityStyleProvider.angleStyleProperty();
    }

    @Override
    public ObservableValue<TimeStyle> timeStyleProperty() {
        return quantityStyleProvider.timeStyleProperty();
    }

    @Override
    public void decrement(int arg0) {
        VariantQuantity oldValue = valueProperty().get();
        if (oldValue != null) {
            var settings = settingsMap.get(oldValue.getDimension());
            double step = settings.getAmountToStepBy();
            double value = oldValue.getValue().doubleValue();
            double floor = step * Math.floor(value / step);
            double ceil = step * Math.ceil(value / step);
            double dfloor = Math.abs(value - floor);
            double dceil = Math.abs(value - ceil);

            VariantQuantity newValue;
            if (dfloor <= dceil) {
                if (dfloor > 1E-2 * step) {
                    newValue = VariantQuantity.of(floor, oldValue.getUnit());
                } else {
                    newValue = VariantQuantity.of(floor - (arg0 * step), oldValue.getUnit());
                }
            } else {
                newValue = VariantQuantity.of(floor, oldValue.getUnit());
            }

            boolean wrapAround = isWrapAround();
            if (newValue.compareTo(settings.getMin()) < 0) {
                valueProperty().set(wrapAround ? settings.getMax() : settings.getMin());
            } else if (newValue.compareTo(settings.getMax()) > 0) {
                valueProperty().set(wrapAround ? settings.getMin() : settings.getMax());
            } else {
                valueProperty().set(newValue);
            }
        }
    }

    @Override
    public void increment(int arg0) {
        VariantQuantity oldValue = valueProperty().get();
        if (oldValue != null) {
            var settings = settingsMap.get(oldValue.getDimension());
            double step = settings.getAmountToStepBy();
            double value = oldValue.getValue().doubleValue();
            double floor = step * Math.floor(value / step);
            double ceil = step * Math.ceil(value / step);
            double dfloor = Math.abs(value - floor);
            double dceil = Math.abs(value - ceil);

            VariantQuantity newValue;
            if (dfloor >= dceil) {
                if (dceil > 1E-2 * step) {
                    newValue = VariantQuantity.of(ceil, oldValue.getUnit());
                } else {
                    newValue = VariantQuantity.of(ceil + (arg0 * step), oldValue.getUnit());
                }
            } else {
                newValue = VariantQuantity.of(ceil, oldValue.getUnit());
            }

            boolean wrapAround = isWrapAround();
            if (newValue.compareTo(settings.getMin()) < 0) {
                valueProperty().set(wrapAround ? settings.getMax() : settings.getMin());
            } else if (newValue.compareTo(settings.getMax()) > 0) {
                valueProperty().set(wrapAround ? settings.getMin() : settings.getMax());
            } else {
                valueProperty().set(newValue);
            }
        }
    }

}
