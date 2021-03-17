/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.common;

import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.measure.VariantQuantity;
import org.junit.Assert;
import org.junit.Test;

public class GeoMathTest {

    @Test
    public void addAndSubtractLatitudes() {
        var res = GeoMath.addLat(Quantity.of(15, Unit.DEGREE).toVariant(), Quantity.of(20, Unit.DEGREE).toVariant());
        assertEqualsApprox(res, 35);

        res = GeoMath.addLat(Quantity.of(80, Unit.DEGREE).toVariant(), Quantity.of(20, Unit.DEGREE).toVariant());
        assertEqualsApprox(res, 90);

        res = GeoMath.addLat(Quantity.of(-80, Unit.DEGREE).toVariant(), Quantity.of(-20, Unit.DEGREE).toVariant());
        assertEqualsApprox(res, -90);

        res = GeoMath.subtractLat(Quantity.of(15, Unit.DEGREE).toVariant(), Quantity.of(20, Unit.DEGREE).toVariant());
        assertEqualsApprox(res, -5);

        res = GeoMath.subtractLat(Quantity.of(80, Unit.DEGREE).toVariant(), Quantity.of(20, Unit.DEGREE).toVariant());
        assertEqualsApprox(res, 60);

        res = GeoMath.subtractLat(Quantity.of(-80, Unit.DEGREE).toVariant(), Quantity.of(20, Unit.DEGREE).toVariant());
        assertEqualsApprox(res, -90);

        res = GeoMath.addLat(Quantity.of(15, Unit.DEGREE).toVariant(), Quantity.of(111, Unit.KILOMETER).toVariant());
        assertEqualsApprox(res, 16);
    }

    @Test
    public void addAndSubtractLongitudes() {
        var res = GeoMath.addLon(Quantity.of(170, Unit.DEGREE).toVariant(), Quantity.of(20, Unit.DEGREE).toVariant());
        assertEqualsApprox(res, -170);

        res = GeoMath.subtractLon(Quantity.of(-170, Unit.DEGREE).toVariant(), Quantity.of(20, Unit.DEGREE).toVariant());
        assertEqualsApprox(res, 170);
    }

    @Test
    public void testInvalidArguments() {
        try {
            GeoMath.addLon(Quantity.of(1, Unit.METER).toVariant(), Quantity.of(1, Unit.METER).toVariant());
            Assert.fail();
        } catch (IllegalArgumentException expected) {
        }

        try {
            GeoMath.subtractLat(Quantity.of(1, Unit.SQUARE_FOOT).toVariant(), Quantity.of(1, Unit.DEGREE).toVariant());
            Assert.fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    private void assertEqualsApprox(VariantQuantity a, double b) {
        Assert.assertTrue(Math.abs(a.getValue().doubleValue() - b) < 0.001);
    }

}
