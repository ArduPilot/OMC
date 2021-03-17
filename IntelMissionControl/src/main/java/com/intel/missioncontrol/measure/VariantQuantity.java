/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.measure;

public interface VariantQuantity extends Comparable<VariantQuantity> {

    /** Gets the value of this quantity, expressed in the quantity's unit. */
    Number getValue();

    /** Gets the value of this quantity, expressed in the base quantity's unit (e.g. meters for dimension length). */
    Number getBaseValue();

    Unit<?> getUnit();

    Dimension getDimension();

    boolean within(VariantQuantity lower, VariantQuantity upper);

    <Q extends Quantity<Q>> Quantity<Q> convertTo(Unit<Q> unit);

    VariantQuantity add(VariantQuantity quantity);

    VariantQuantity add(Number value);

    VariantQuantity subtract(VariantQuantity quantity);

    VariantQuantity subtract(Number value);

    VariantQuantity multiply(Number scalar);

    VariantQuantity divide(Number scalar);

    VariantQuantity negate();

    /**
     * Returns whether this quantity is equal to another quantity.
     *
     * @param exactMatch Determines whether the value and unit must be an exact match. If false, only the base value of
     *     both quantities is compared for equality; the units may be different.
     */
    boolean equals(Object value, boolean exactMatch);

    static <Q extends VariantQuantity> VariantQuantity of(Number value, Unit<?> unit) {
        if (unit.getImplClass() == DoubleQuantity.class) {
            return new DoubleVariantQuantity(unit.convertToBase(value.doubleValue()), unit);
        } else if (unit.getImplClass() == LongBaseQuantity.class) {
            return new LongBaseVariantQuantity((long)unit.convertToBase(value.doubleValue()), unit);
        }

        throw new IllegalArgumentException("unit");
    }

}
