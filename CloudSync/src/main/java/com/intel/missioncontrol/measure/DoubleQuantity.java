/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.measure;

public class DoubleQuantity<Q extends Quantity<Q>> implements Quantity<Q> {

    private final double baseValue;
    private final double value;
    private final Unit<Q> unit;

    DoubleQuantity(double baseValue, Unit<Q> unit) {
        this.baseValue = baseValue;
        this.value = unit.convertFromBase(baseValue);
        this.unit = unit;
    }

    DoubleQuantity(double baseValue, double value, Unit<Q> unit) {
        this.baseValue = baseValue;
        this.value = value;
        this.unit = unit;
    }

    @Override
    public Number getValue() {
        return this.value;
    }

    @Override
    public Number getBaseValue() {
        return this.baseValue;
    }

    @Override
    public Unit<Q> getUnit() {
        return this.unit;
    }

    @Override
    public Dimension getDimension() {
        return this.unit.getDimension();
    }

    @Override
    public boolean within(Quantity<Q> lower, Quantity<Q> upper) {
        return compareTo(lower) >= 0 && compareTo(upper) <= 0;
    }

    @Override
    public Quantity<Q> add(Quantity<Q> quantity) {
        return new DoubleQuantity<>(baseValue + quantity.getBaseValue().doubleValue(), unit);
    }

    @Override
    public Quantity<Q> add(Number value) {
        return new DoubleQuantity<>(baseValue + unit.convertToBase(value.doubleValue()), unit);
    }

    @Override
    public Quantity<Q> subtract(Quantity<Q> quantity) {
        return new DoubleQuantity<>(baseValue - quantity.getBaseValue().doubleValue(), unit);
    }

    @Override
    public Quantity<Q> subtract(Number value) {
        return new DoubleQuantity<>(baseValue - unit.convertToBase(value.doubleValue()), unit);
    }

    @Override
    public Quantity<Q> multiply(Number scalar) {
        return new DoubleQuantity<>(baseValue * scalar.doubleValue(), unit);
    }

    @Override
    public Quantity<Q> divide(Number scalar) {
        return new DoubleQuantity<>(baseValue / scalar.doubleValue(), unit);
    }

    @Override
    public Quantity<Q> negate() {
        return new DoubleQuantity<>(-baseValue, unit);
    }

    @Override
    public Quantity<Q> convertTo(Unit<Q> unit) {
        if (this.unit == unit) {
            return this;
        }

        return new DoubleQuantity<>(baseValue, unit);
    }

    @Override
    public VariantQuantity toVariant() {
        return new DoubleVariantQuantity(baseValue, unit);
    }

    @Override
    public String toString() {
        QuantityFormat format = new QuantityFormat();
        format.setMaximumFractionDigits(16);
        return format.format(this);
    }

    @Override
    public boolean equals(Object value) {
        return equals(value, true);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object value, boolean exactMatch) {
        if (!(value instanceof Quantity<?>)) {
            return false;
        }

        Quantity<?> quantity = (Quantity<?>)value;
        if (quantity.getDimension() != getDimension()) {
            return false;
        }

        if (exactMatch && quantity.getUnit() != getUnit()) {
            return false;
        }

        return compareTo((Quantity<Q>)quantity) == 0;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + unit.hashCode();
        long temp;
        temp = Double.doubleToLongBits(value);
        result = prime * result + (int)(temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public int compareTo(Quantity<Q> quantity) {
        double otherValue = quantity.getBaseValue().doubleValue();

        if (DoubleHelper.areClose(baseValue, otherValue)) {
            return 0;
        } else if (baseValue > otherValue) {
            return 1;
        }

        return -1;
    }

}
