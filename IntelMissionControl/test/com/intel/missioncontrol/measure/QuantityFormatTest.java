/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.measure;

import com.intel.missioncontrol.helper.DoubleHelper;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import org.junit.Assert;
import org.junit.Test;

public class QuantityFormatTest {

    @Test
    public void formatLength() {
        var decimalSeparator = new DecimalFormatSymbols(Locale.getDefault()).getDecimalSeparator();
        var format = new QuantityFormat();
        var quantity0 = format.parse("1m", Dimension.LENGTH);
        Assert.assertSame(quantity0.getUnit(), Unit.METER);
        Assert.assertTrue(DoubleHelper.areClose(quantity0.getValue().doubleValue(), 1));

        var quantity1 = format.parse("1" + decimalSeparator + "M", Dimension.LENGTH);
        Assert.assertSame(quantity1.getUnit(), Unit.METER);
        Assert.assertTrue(DoubleHelper.areClose(quantity1.getValue().doubleValue(), 1));

        var quantity2 = format.parse(decimalSeparator + "5Mm", Dimension.LENGTH, Dimension.AREA, Dimension.TIME);
        Assert.assertSame(quantity2.getUnit(), Unit.MILLIMETER);
        Assert.assertTrue(DoubleHelper.areClose(quantity2.getValue().doubleValue(), 0.5));

        try {
            format.parse("1", Dimension.LENGTH);
            Assert.fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            format.parse("m1", Dimension.LENGTH);
            Assert.fail();
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void formatAnglesDegDecimalMin() {
        var decimalSeparator = new DecimalFormatSymbols(Locale.getDefault()).getDecimalSeparator();
        var format = new QuantityFormat();
        format.setAngleStyle(AngleStyle.DEGREE_DECIMAL_MINUTE);

        var quantity0 = format.parse("90° 59′", Dimension.ANGLE);
        Assert.assertEquals("90° 59′", format.format(quantity0));

        var quantity8 = format.parse("5", Unit.DEGREE);
        Assert.assertEquals(quantity8, Quantity.of(5, Unit.DEGREE));

        var quantity3 = format.parse(decimalSeparator + "5° 1'", Dimension.ANGLE, Dimension.AREA, Dimension.TIME);
        Assert.assertSame(quantity3.getUnit(), Unit.DEGREE);
        Assert.assertEquals("0° 31′", format.format(quantity3));
    }

    @Test
    public void formatAnglesDMS() {
        var decimalSeparator = new DecimalFormatSymbols(Locale.getDefault()).getDecimalSeparator();
        var format = new QuantityFormat();
        format.setAngleStyle(AngleStyle.DEGREE_MINUTE_SECOND);

        var quantity0 = format.parse("90° 59′ 0″", Dimension.ANGLE);
        Assert.assertEquals("90° 59′ 0″", format.format(quantity0));

        var quantity3 = format.parse(decimalSeparator + "5° 1'", Dimension.ANGLE, Dimension.AREA, Dimension.TIME);
        Assert.assertSame(quantity3.getUnit(), Unit.DEGREE);
        format.setAngleStyle(AngleStyle.DEGREE_MINUTE_SECOND);
        Assert.assertEquals("0° 31′ 0″", format.format(quantity3));

        var quantity4 = format.parse(decimalSeparator + "5deg 1min 10 sec", Dimension.ANGLE, Dimension.AREA, Dimension.TIME);
        Assert.assertSame(quantity4.getUnit(), Unit.DEGREE);
        Assert.assertEquals("0° 31′ 10″", format.format(quantity4));

        var quantity6 = format.parse("5° 0′ 0″", Dimension.ANGLE);
        Assert.assertSame(quantity6.getUnit(), Unit.DEGREE);
        Assert.assertEquals("5° 0′ 0″", format.format(quantity6));

        var quantity7 = format.parse("5° 0′ 0″", Unit.DEGREE);
        String text = format.format(quantity7);
        Assert.assertEquals(Quantity.of(5, Unit.DEGREE), format.parse(text, Dimension.ANGLE).convertTo(Unit.DEGREE));

        var quantity5 = format.parse("10 sec", Dimension.ANGLE);
        Assert.assertSame(quantity5.getUnit(), Unit.ARCSECOND);
        format.setAngleStyle(AngleStyle.DEGREE_MINUTE_SECOND);
        Assert.assertEquals("0° 0′ 10″", format.format(quantity5));
    }

    @Test
    public void parseExpression() {
        var decimalSeparator = new DecimalFormatSymbols(Locale.getDefault()).getDecimalSeparator();
        var format = new QuantityFormat();

        var quantity0 = format.parse("1m + 1m", Unit.METER);
        Assert.assertEquals(quantity0.getValue().doubleValue(), 2, 0.0001);
        Assert.assertEquals(quantity0.getUnit(), Unit.METER);

        var quantity1 = format.parse("1m + 1m + 1", Unit.METER);
        Assert.assertEquals(quantity1.getValue().doubleValue(), 3, 0.0001);
        Assert.assertEquals(quantity1.getUnit(), Unit.METER);

        var quantity2 = format.parse("1m + 1m -50cm", Unit.METER);
        Assert.assertEquals(quantity2.getValue().doubleValue(), 1.5, 0.0001);
        Assert.assertEquals(quantity2.getUnit(), Unit.METER);

        var quantity3 = format.parse("1m - 1m -50cm", Unit.METER);
        Assert.assertEquals(quantity3.getValue().doubleValue(), -0.5, 0.0001);
        Assert.assertEquals(quantity3.getUnit(), Unit.METER);

        var quantity4 = format.parse("1m -( 1m -50cm)", Unit.METER);
        Assert.assertEquals(quantity4.getValue().doubleValue(), 0.5, 0.0001);
        Assert.assertEquals(quantity4.getUnit(), Unit.METER);

        var quantity5 = format.parse("1m * 2 -50cm", Unit.METER);
        Assert.assertEquals(quantity5.getValue().doubleValue(), 1.5, 0.0001);
        Assert.assertEquals(quantity5.getUnit(), Unit.METER);

        var quantity6 = format.parse("(50 / 2) * 3", Unit.METER);
        Assert.assertEquals(quantity6.getValue().doubleValue(), 75, 0.0001);
        Assert.assertEquals(quantity6.getUnit(), Unit.METER);

        var quantity7 = format.parse("(50cm / 2) * 0" + decimalSeparator + "5", Unit.METER);
        Assert.assertEquals(quantity7.getValue().doubleValue(), 12.5, 0.0001);
        Assert.assertEquals(quantity7.getUnit(), Unit.CENTIMETER);

        var quantity8 = format.parse("(50°/2)", Dimension.ANGLE);
        Assert.assertEquals(quantity8.getValue().doubleValue(), 25, 0.0001);
        Assert.assertEquals(quantity8.getUnit(), Unit.DEGREE);

        var quantity9 = format.parse("((((50°/2))))", Dimension.ANGLE);
        Assert.assertEquals(quantity9.getValue().doubleValue(), 25, 0.0001);
        Assert.assertEquals(quantity9.getUnit(), Unit.DEGREE);

        var quantity10 = format.parse("4000-5 m", Dimension.LENGTH);
        Assert.assertEquals(quantity10.getValue().doubleValue(), 3995, 0.0001);
        Assert.assertEquals(quantity10.getUnit(), Unit.METER);

        var quantity11 = format.parse("(100+200)/2m", Dimension.LENGTH);
        Assert.assertEquals(quantity11.getValue().doubleValue(), 150, 0.0001);
        Assert.assertEquals(quantity11.getUnit(), Unit.METER);

        var quantity12 = format.parse("(112+123)/2", Unit.METER);
        Assert.assertEquals(quantity12.getValue().doubleValue(), 117.5, 0.0001);
        Assert.assertEquals(quantity12.getUnit(), Unit.METER);

        var quantity13 = format.parse("35+(5*12" + decimalSeparator + "5)m", Unit.METER);
        Assert.assertEquals(quantity13.getValue().doubleValue(), 97.5, 0.0001);
        Assert.assertEquals(quantity13.getUnit(), Unit.METER);

        var quantity14 = format.parse("35+(5*12" + decimalSeparator + "5°)", Dimension.LENGTH, Dimension.ANGLE);
        Assert.assertEquals(quantity14.getValue().doubleValue(), 97.5, 0.0001);
        Assert.assertEquals(quantity14.getUnit(), Unit.DEGREE);

        // TODO: Doesn't work yet:
        // QuantityFormat will greedily treat "/" as an operator, instead of a part of the unit symbol.
        //
        //var quantity15 = format.parse("0 m/s+0" + decimalSeparator + "5", Dimension.SPEED);
        //Assert.assertEquals(quantity15.getValue().doubleValue(), 0.5, 0.0001);
        //Assert.assertEquals(quantity15.getUnit(), Unit.METER_PER_SECOND);

        try {
            format.parse("1m*1m", Dimension.LENGTH);
            Assert.fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            format.parse("50m + 15°", Dimension.LENGTH, Dimension.ANGLE);
            Assert.fail();
        } catch (IllegalArgumentException e) {
        }
    }

}
