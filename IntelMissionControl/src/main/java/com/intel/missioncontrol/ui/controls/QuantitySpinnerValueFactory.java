/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.controls;

import com.intel.missioncontrol.helper.Expect;
import com.intel.missioncontrol.measure.AngleStyle;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.SystemOfMeasurement;
import com.intel.missioncontrol.measure.TimeStyle;
import com.intel.missioncontrol.measure.UnitInfo;
import com.intel.missioncontrol.measure.property.IQuantityStyleProvider;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.SpinnerValueFactory;

public class QuantitySpinnerValueFactory<Q extends Quantity<Q>> extends SpinnerValueFactory<Quantity<Q>>
        implements IQuantityStyleProvider {

    private final IQuantityStyleProvider quantityStyleProvider;
    private final ObjectProperty<Quantity<Q>> min;
    private final ObjectProperty<Quantity<Q>> max;
    private final DoubleProperty amountToStepBy;
    private boolean isUpdating;

    public QuantitySpinnerValueFactory(
            IQuantityStyleProvider quantityStyleProvider, UnitInfo<Q> unitInfo, int maxFractionDigits) {
        this(
            quantityStyleProvider,
            unitInfo,
            maxFractionDigits,
            Quantity.of(-Double.MAX_VALUE, unitInfo.getPreferredUnit(SystemOfMeasurement.METRIC)),
            Quantity.of(Double.MAX_VALUE, unitInfo.getPreferredUnit(SystemOfMeasurement.METRIC)),
            1.0);
    }

    public QuantitySpinnerValueFactory(
            IQuantityStyleProvider quantityStyleProvider,
            UnitInfo<Q> unitInfo,
            int significantDigits,
            int maxFractionDigits) {
        this(
            quantityStyleProvider,
            unitInfo,
            significantDigits,
            maxFractionDigits,
            Quantity.of(-Double.MAX_VALUE, unitInfo.getPreferredUnit(SystemOfMeasurement.METRIC)),
            Quantity.of(Double.MAX_VALUE, unitInfo.getPreferredUnit(SystemOfMeasurement.METRIC)),
            1.0,
            false);
    }

    public QuantitySpinnerValueFactory(
            IQuantityStyleProvider quantityStyleProvider,
            UnitInfo<Q> unitInfo,
            int maxFractionDigits,
            Quantity<Q> min,
            Quantity<Q> max) {
        this(quantityStyleProvider, unitInfo, maxFractionDigits, min, max, 1.0, false);
    }

    public QuantitySpinnerValueFactory(
            IQuantityStyleProvider quantityStyleProvider,
            UnitInfo<Q> unitInfo,
            int significantDigits,
            int maxFractionDigits,
            Quantity<Q> min,
            Quantity<Q> max) {
        this(quantityStyleProvider, unitInfo, significantDigits, maxFractionDigits, min, max, 1.0, false);
    }

    public QuantitySpinnerValueFactory(
            IQuantityStyleProvider quantityStyleProvider,
            UnitInfo<Q> unitInfo,
            int maxFractionDigits,
            Quantity<Q> min,
            Quantity<Q> max,
            double amountToStepBy) {
        this(quantityStyleProvider, unitInfo, maxFractionDigits, min, max, amountToStepBy, false);
    }

    public QuantitySpinnerValueFactory(
            IQuantityStyleProvider quantityStyleProvider,
            UnitInfo<Q> unitInfo,
            int significantDigits,
            int maxFractionDigits,
            Quantity<Q> min,
            Quantity<Q> max,
            double amountToStepBy) {
        this(quantityStyleProvider, unitInfo, significantDigits, maxFractionDigits, min, max, amountToStepBy, false);
    }

    public QuantitySpinnerValueFactory(
            IQuantityStyleProvider quantityStyleProvider,
            UnitInfo<Q> unitInfo,
            int maxFractionDigits,
            Quantity<Q> min,
            Quantity<Q> max,
            boolean wrapAround) {
        this(quantityStyleProvider, unitInfo, maxFractionDigits, min, max, 1.0, wrapAround);
    }

    public QuantitySpinnerValueFactory(
            IQuantityStyleProvider quantityStyleProvider,
            UnitInfo<Q> unitInfo,
            int significantDigits,
            int maxFractionDigits,
            Quantity<Q> min,
            Quantity<Q> max,
            double amountToStepBy,
            boolean wrapAround) {
        this(quantityStyleProvider, unitInfo, maxFractionDigits, min, max, amountToStepBy, wrapAround);
        setConverter(new QuantityConverter<>(quantityStyleProvider, unitInfo, significantDigits, maxFractionDigits));
    }

    public QuantitySpinnerValueFactory(
            IQuantityStyleProvider quantityStyleProvider,
            UnitInfo<Q> unitInfo,
            int maxFractionDigits,
            Quantity<Q> min,
            Quantity<Q> max,
            double amountToStepBy,
            boolean wrapAround) {
        this.quantityStyleProvider = quantityStyleProvider;
        setConverter(new QuantityConverter<>(quantityStyleProvider, unitInfo, maxFractionDigits));
        setWrapAround(wrapAround);

        this.min =
            new SimpleObjectProperty<>(this, "min", min) {
                @Override
                protected void invalidated() {
                    final Quantity<Q> newMin = get();

                    Quantity<Q> currentValue = QuantitySpinnerValueFactory.this.getValue();
                    if (currentValue == null) {
                        return;
                    }

                    if (newMin.compareTo(getMax()) > 0) {
                        if (!minProperty().isBound()) {
                            setMin(getMax());
                        }

                        return;
                    }

                    if (currentValue.compareTo(newMin) < 0) {
                        if (!minProperty().isBound()) {
                            QuantitySpinnerValueFactory.this.setValue(newMin);
                        }
                    }
                }
            };

        this.max =
            new SimpleObjectProperty<>(this, "max", max) {
                @Override
                protected void invalidated() {
                    final Quantity<Q> newMax = get();

                    Quantity<Q> currentValue = QuantitySpinnerValueFactory.this.getValue();
                    if (currentValue == null) {
                        return;
                    }

                    if (newMax.compareTo(getMin()) < 0) {
                        if (!maxProperty().isBound()) {
                            setMax(getMin());
                        }

                        return;
                    }

                    if (currentValue.compareTo(newMax) > 0) {
                        if (!maxProperty().isBound()) {
                            QuantitySpinnerValueFactory.this.setValue(newMax);
                        }
                    }
                }
            };

        this.amountToStepBy = new SimpleDoubleProperty(this, "amountToStepBy", amountToStepBy);

        valueProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    if (isUpdating) {
                        return;
                    }

                    try {
                        isUpdating = true;
                        if (newValue == null) {
                            return;
                        }

                        final Quantity<Q> minValue = getMin();
                        final Quantity<Q> maxValue = getMax();
                        if (!this.minProperty().isBound() && !this.maxProperty().isBound()) {
                            if (minValue != null && newValue.compareTo(minValue) < 0) {
                                setValue(minValue.convertTo(newValue.getUnit()));
                            } else if (maxValue != null && newValue.compareTo(maxValue) > 0) {
                                setValue(maxValue.convertTo(newValue.getUnit()));
                            }
                        }
                    } finally {
                        isUpdating = false;
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

    public ObjectProperty<Quantity<Q>> minProperty() {
        return this.min;
    }

    public Quantity<Q> getMin() {
        return this.min.get();
    }

    public void setMin(Quantity<Q> min) {
        Expect.notNull(min, "min");
        this.min.set(min);
    }

    public ObjectProperty<Quantity<Q>> maxProperty() {
        return this.max;
    }

    public Quantity<Q> getMax() {
        return this.max.get();
    }

    public void setMax(Quantity<Q> max) {
        Expect.notNull(max, "max");
        this.max.set(max);
    }

    public void setAmountToStepBy(double value) {
        this.amountToStepBy.set(value);
    }

    public double getAmountToStepBy() {
        return this.amountToStepBy.get();
    }

    public DoubleProperty amountToStepByProperty() {
        return this.amountToStepBy;
    }

    @Override
    public void decrement(int arg0) {
        Quantity<Q> oldValue = valueProperty().get();
        if (oldValue != null) {
            double step = getAmountToStepBy();
            double value = oldValue.getValue().doubleValue();
            double floor = step * Math.floor(value / step);
            double ceil = step * Math.ceil(value / step);
            double dfloor = Math.abs(value - floor);
            double dceil = Math.abs(value - ceil);

            Quantity<Q> newValue;
            if (dfloor <= dceil) {
                if (dfloor > 1E-2 * step) {
                    newValue = Quantity.of(floor, oldValue.getUnit());
                } else {
                    newValue = Quantity.of(floor - (arg0 * step), oldValue.getUnit());
                }
            } else {
                newValue = Quantity.of(floor, oldValue.getUnit());
            }

            boolean wrapAround = isWrapAround();
            if (newValue.compareTo(getMin()) < 0) {
                valueProperty().set((wrapAround ? getMax() : getMin()).convertTo(newValue.getUnit()));
            } else if (newValue.compareTo(getMax()) > 0) {
                valueProperty().set((wrapAround ? getMin() : getMax()).convertTo(newValue.getUnit()));
            } else {
                valueProperty().set(newValue);
            }
        }
    }

    @Override
    public void increment(int arg0) {
        Quantity<Q> oldValue = valueProperty().get();
        if (oldValue != null) {
            double step = getAmountToStepBy();
            double value = oldValue.getValue().doubleValue();
            double floor = step * Math.floor(value / step);
            double ceil = step * Math.ceil(value / step);
            double dfloor = Math.abs(value - floor);
            double dceil = Math.abs(value - ceil);

            Quantity<Q> newValue;
            if (dfloor >= dceil) {
                if (dceil > 1E-2 * step) {
                    newValue = Quantity.of(ceil, oldValue.getUnit());
                } else {
                    newValue = Quantity.of(ceil + (arg0 * step), oldValue.getUnit());
                }
            } else {
                newValue = Quantity.of(ceil, oldValue.getUnit());
            }

            boolean wrapAround = isWrapAround();
            if (newValue.compareTo(getMin()) < 0) {
                valueProperty().set((wrapAround ? getMax() : getMin()).convertTo(newValue.getUnit()));
            } else if (newValue.compareTo(getMax()) > 0) {
                valueProperty().set((wrapAround ? getMin() : getMax()).convertTo(newValue.getUnit()));
            } else {
                valueProperty().set(newValue);
            }
        }
    }

}
