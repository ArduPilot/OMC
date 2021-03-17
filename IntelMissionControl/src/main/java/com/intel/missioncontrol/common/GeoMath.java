/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.common;

import com.intel.missioncontrol.measure.Dimension;
import com.intel.missioncontrol.measure.Dimension.Angle;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.measure.VariantQuantity;

public class GeoMath {

    private enum ExcessOption {
        SATURATE,
        WRAP_AROUND
    }

    private static final Quantity<Angle> MAX_LAT = Quantity.of(Math.PI / 2, Unit.RADIAN);
    private static final Quantity<Angle> MIN_LAT = Quantity.of(-Math.PI / 2, Unit.RADIAN);
    private static final Quantity<Angle> MAX_LON = Quantity.of(Math.PI, Unit.RADIAN);
    private static final Quantity<Angle> MIN_LON = Quantity.of(-Math.PI, Unit.RADIAN);

    public static VariantQuantity addLat(VariantQuantity a, VariantQuantity b) {
        return addAngles(a, b, MIN_LAT, MAX_LAT, ExcessOption.SATURATE);
    }

    public static VariantQuantity addLon(VariantQuantity a, VariantQuantity b) {
        return addAngles(a, b, MIN_LON, MAX_LON, ExcessOption.WRAP_AROUND);
    }

    public static VariantQuantity subtractLat(VariantQuantity a, VariantQuantity b) {
        return addAngles(a, b.negate(), MIN_LAT, MAX_LAT, ExcessOption.SATURATE);
    }

    public static VariantQuantity subtractLon(VariantQuantity a, VariantQuantity b) {
        return addAngles(a, b.negate(), MIN_LON, MAX_LON, ExcessOption.WRAP_AROUND);
    }

    @SuppressWarnings("unchecked")
    private static VariantQuantity addAngles(
            VariantQuantity a, VariantQuantity b, Quantity<Angle> min, Quantity<Angle> max, ExcessOption excessOption) {
        Dimension dimA = a.getDimension();
        Dimension dimB = b.getDimension();

        if (dimA == Dimension.ANGLE && dimB == Dimension.ANGLE) {
            return addAnglesConstrained(a.convertTo(Unit.RADIAN), b.convertTo(Unit.RADIAN), min, max, excessOption)
                .convertTo((Unit<Angle>)a.getUnit())
                .toVariant();
        } else if (dimA == Dimension.ANGLE && dimB == Dimension.LENGTH) {
            return addAnglesConstrained(a.convertTo(Unit.RADIAN), getApproxAngleFromLength(b), min, max, excessOption)
                .convertTo((Unit<Angle>)a.getUnit())
                .toVariant();
        } else if (dimA == Dimension.LENGTH && dimB == Dimension.ANGLE) {
            return addAnglesConstrained(getApproxAngleFromLength(a), b.convertTo(Unit.RADIAN), min, max, excessOption)
                .convertTo((Unit<Angle>)b.getUnit())
                .toVariant();
        } else if (dimA != Dimension.ANGLE && dimA != Dimension.LENGTH) {
            throw new IllegalArgumentException("Unsupported dimension: a = " + dimA);
        } else if (dimB != Dimension.LENGTH) {
            throw new IllegalArgumentException("Unsupported dimension: b = " + dimB);
        }

        throw new IllegalArgumentException("Unsupported dimensions: a = " + dimA + ", b = " + dimB);
    }

    private static Quantity<Angle> addAnglesConstrained(
            Quantity<Angle> a, Quantity<Angle> b, Quantity<Angle> min, Quantity<Angle> max, ExcessOption excessOption) {
        Quantity<Angle> res = a.add(b);
        double overflow = res.subtract(min).getValue().doubleValue();
        if (overflow < 0) {
            if (excessOption == ExcessOption.SATURATE) {
                return min;
            }

            return max.add(overflow);
        }

        overflow = res.subtract(max).getValue().doubleValue();
        if (overflow > 0) {
            if (excessOption == ExcessOption.SATURATE) {
                return max;
            }

            return min.add(overflow);
        }

        return res;
    }

    private static Quantity<Angle> getApproxAngleFromLength(VariantQuantity length) {
        return Quantity.of(length.convertTo(Unit.METER).getValue().doubleValue() * (1. / 111_111.), Unit.DEGREE)
            .convertTo(Unit.RADIAN);
    }

}
