/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.measure;

public class DoubleVariantQuantity implements VariantQuantity {

    private final double baseValue;
    private final double value;
    private final Unit<?> unit;

    DoubleVariantQuantity(double baseValue, Unit<?> unit) {
        this.baseValue = baseValue;
        this.value = unit.convertFromBase(baseValue);
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
    public Unit getUnit() {
        return unit;
    }

    @Override
    public Dimension getDimension() {
        return unit.getDimension();
    }

    @Override
    public boolean within(VariantQuantity lower, VariantQuantity upper) {
        throw new UnsupportedOperationException("Operation not supported for variant quantity.");
    }

    @Override
    public VariantQuantity add(VariantQuantity quantity) {
        checkDimension(unit.getDimension(), quantity.getDimension());
        if (unit == quantity.getUnit()) {
            return new DoubleVariantQuantity(baseValue + quantity.getBaseValue().doubleValue(), unit);
        }

        return new DoubleVariantQuantity(baseValue + quantity.convertTo(unit).getBaseValue().doubleValue(), unit);
    }

    @Override
    public VariantQuantity add(Number value) {
        return new DoubleVariantQuantity(baseValue + unit.convertToBase(value.doubleValue()), unit);
    }

    @Override
    public VariantQuantity subtract(VariantQuantity quantity) {
        checkDimension(unit.getDimension(), quantity.getDimension());
        if (unit == quantity.getUnit()) {
            return new DoubleVariantQuantity(baseValue - quantity.getBaseValue().doubleValue(), unit);
        }

        return new DoubleVariantQuantity(baseValue - quantity.convertTo(unit).getBaseValue().doubleValue(), unit);
    }

    @Override
    public VariantQuantity subtract(Number value) {
        return new DoubleVariantQuantity(baseValue - unit.convertToBase(value.doubleValue()), unit);
    }

    @Override
    public VariantQuantity multiply(Number scalar) {
        return new DoubleVariantQuantity(baseValue * scalar.doubleValue(), unit);
    }

    @Override
    public VariantQuantity divide(Number scalar) {
        return new DoubleVariantQuantity(baseValue / scalar.doubleValue(), unit);
    }

    @Override
    public VariantQuantity negate() {
        return new DoubleVariantQuantity(-baseValue, unit);
    }

    @Override
    public <Q extends Quantity<Q>> Quantity<Q> convertTo(Unit<Q> unit) {
        checkDimension(this.unit.getDimension(), unit.getDimension());
        return new DoubleQuantity<>(baseValue, unit);
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

        return DoubleHelper.areClose(quantity.getBaseValue().doubleValue(), baseValue);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((unit == null) ? 0 : unit.hashCode());
        long temp;
        temp = Double.doubleToLongBits(value);
        result = prime * result + (int)(temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public int compareTo(VariantQuantity quantity) {
        checkDimension(unit.getDimension(), quantity.getDimension());
        double otherValue = quantity.getBaseValue().doubleValue();

        if (DoubleHelper.areClose(baseValue, otherValue)) {
            return 0;
        } else if (baseValue > otherValue) {
            return 1;
        }

        return -1;
    }

    private void checkDimension(Dimension expected, Dimension actual) {
        if (expected != actual) {
            throw new IllegalArgumentException(
                "Incompatible dimensions. Expected: " + expected + ", Actual: " + actual);
        }
    }

}
