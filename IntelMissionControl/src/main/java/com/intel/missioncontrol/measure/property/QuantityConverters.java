/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.measure.property;

import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.Unit;
import org.asyncfx.beans.binding.BidirectionalValueConverter;

public final class QuantityConverters {

    private QuantityConverters() {}

    public static <Q extends Quantity<Q>> BidirectionalValueConverter<Number, Quantity<Q>> numberToQuantity(
            Unit<Q> unit) {
        return new BidirectionalValueConverter<>() {
            @Override
            public Number convertBack(Quantity<Q> value) {
                return value.convertTo(unit).getValue();
            }

            @Override
            public Quantity<Q> convert(Number value) {
                return Quantity.of(value.doubleValue(), unit);
            }
        };
    }

    public static <Q extends Quantity<Q>> BidirectionalValueConverter<Quantity<Q>, Number> quantityToNumber(
            Unit<Q> unit) {
        return new BidirectionalValueConverter<>() {
            @Override
            public Number convert(Quantity<Q> value) {
                return value.convertTo(unit).getValue();
            }

            @Override
            public Quantity<Q> convertBack(Number value) {
                return Quantity.of(value, unit);
            }
        };
    }

}
