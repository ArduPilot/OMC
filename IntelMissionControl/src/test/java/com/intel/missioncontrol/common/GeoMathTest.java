/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.common;

import com.intel.missioncontrol.TestBase;
import com.intel.missioncontrol.geospatial.GeoMath;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.measure.VariantQuantity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class GeoMathTest extends TestBase {

    @Test
    void addAndSubtractLatitudes() {
        var res = com.intel.missioncontrol.geospatial.GeoMath.addLat(Quantity.of(15, Unit.DEGREE).toVariant(), Quantity.of(20, Unit.DEGREE).toVariant());
        assertEqualsApprox(res, 35);

        res = com.intel.missioncontrol.geospatial.GeoMath.addLat(Quantity.of(80, Unit.DEGREE).toVariant(), Quantity.of(20, Unit.DEGREE).toVariant());
        assertEqualsApprox(res, 90);

        res = com.intel.missioncontrol.geospatial.GeoMath.addLat(Quantity.of(-80, Unit.DEGREE).toVariant(), Quantity.of(-20, Unit.DEGREE).toVariant());
        assertEqualsApprox(res, -90);

        res = com.intel.missioncontrol.geospatial.GeoMath.subtractLat(Quantity.of(15, Unit.DEGREE).toVariant(), Quantity.of(20, Unit.DEGREE).toVariant());
        assertEqualsApprox(res, -5);

        res = com.intel.missioncontrol.geospatial.GeoMath.subtractLat(Quantity.of(80, Unit.DEGREE).toVariant(), Quantity.of(20, Unit.DEGREE).toVariant());
        assertEqualsApprox(res, 60);

        res = com.intel.missioncontrol.geospatial.GeoMath.subtractLat(Quantity.of(-80, Unit.DEGREE).toVariant(), Quantity.of(20, Unit.DEGREE).toVariant());
        assertEqualsApprox(res, -90);

        res = com.intel.missioncontrol.geospatial.GeoMath.addLat(Quantity.of(15, Unit.DEGREE).toVariant(), Quantity.of(111, Unit.KILOMETER).toVariant());
        assertEqualsApprox(res, 16);
    }

    @Test
    void addAndSubtractLongitudes() {
        var res = com.intel.missioncontrol.geospatial.GeoMath.addLon(Quantity.of(170, Unit.DEGREE).toVariant(), Quantity.of(20, Unit.DEGREE).toVariant());
        assertEqualsApprox(res, -170);

        res = com.intel.missioncontrol.geospatial.GeoMath.subtractLon(Quantity.of(-170, Unit.DEGREE).toVariant(), Quantity.of(20, Unit.DEGREE).toVariant());
        assertEqualsApprox(res, 170);
    }

    @Test
    void testInvalidArguments() {
        try {
            com.intel.missioncontrol.geospatial.GeoMath.addLon(Quantity.of(1, Unit.METER).toVariant(), Quantity.of(1, Unit.METER).toVariant());
            Assertions.fail();
        } catch (IllegalArgumentException expected) {
        }

        try {
            GeoMath.subtractLat(Quantity.of(1, Unit.SQUARE_FOOT).toVariant(), Quantity.of(1, Unit.DEGREE).toVariant());
            Assertions.fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    private void assertEqualsApprox(VariantQuantity a, double b) {
        Assertions.assertTrue(Math.abs(a.getValue().doubleValue() - b) < 0.001);
    }

}
