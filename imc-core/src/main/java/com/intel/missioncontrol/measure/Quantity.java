/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.measure;

public interface Quantity<Q extends Quantity<Q>> extends Comparable<Quantity<Q>> {

    /** Gets the value of this quantity, expressed in the quantity's unit. */
    Number getValue();

    /** Gets the value of this quantity, expressed in the base quantity's unit (e.g. meters for dimension length). */
    Number getBaseValue();

    Unit<Q> getUnit();

    Dimension getDimension();

    boolean within(Quantity<Q> lower, Quantity<Q> upper);

    Quantity<Q> convertTo(Unit<Q> unit);

    Quantity<Q> add(Quantity<Q> quantity);

    Quantity<Q> add(Number value);

    Quantity<Q> subtract(Quantity<Q> quantity);

    Quantity<Q> subtract(Number value);

    Quantity<Q> multiply(Number scalar);

    Quantity<Q> divide(Number scalar);

    Quantity<Q> negate();

    VariantQuantity toVariant();

    /**
     * Returns whether this quantity is equal to another quantity.
     *
     * @param exactMatch Determines whether the value and unit must be an exact match. If false, only the base value of
     *     both quantities is compared for equality; the units may be different.
     */
    boolean equals(Object value, boolean exactMatch);

    static <Q extends Quantity<Q>> Quantity<Q> of(Number value, Unit<Q> unit) {
        if (unit.getImplClass() == DoubleQuantity.class) {
            return new DoubleQuantity<>(unit.convertToBase(value.doubleValue()), value.doubleValue(), unit);
        } else if (unit.getImplClass() == LongBaseQuantity.class) {
            return new LongBaseQuantity<>((long)unit.convertToBase(value.doubleValue()), value.longValue(), unit);
        }

        throw new IllegalArgumentException("unit");
    }

}
