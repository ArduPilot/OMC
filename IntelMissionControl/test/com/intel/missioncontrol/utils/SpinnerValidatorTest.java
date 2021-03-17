/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.utils;

import org.junit.Ignore;
import org.junit.Test;

import java.util.function.UnaryOperator;

import javafx.scene.control.TextFormatter;
import javafx.util.StringConverter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * @author Vladimir Iordanov
 */
@Ignore
public class SpinnerValidatorTest {

    @Test
    public void testDoubleStringConverter_fromString() {
        DoubleValidator spinnerValidator = new DoubleValidator(0d, 100d);
        StringConverter<Double> stringConverter = spinnerValidator.getStringConverter();

        double val = stringConverter.fromString("1.23456");
        assertEquals(1.23456d, val, 0);

        val = stringConverter.fromString("123");
        // Should return the previous valid value because 123 is out of range
        assertEquals(1.23456d, val, 0);

        val = stringConverter.fromString("100");
        assertEquals(100d, val, 0);

        val = stringConverter.fromString("-100");
        assertEquals(100d, val, 0);

        val = stringConverter.fromString("ert");
        assertEquals(100d, val, 0);

        val = stringConverter.fromString("5.0");
        assertEquals(5d, val, 0);

        val = stringConverter.fromString("-");
        assertEquals(5d, val, 0);

        val = stringConverter.fromString(".");
        assertEquals(5d, val, 0);

        val = stringConverter.fromString("0.");
        assertEquals(0d, val, 0);

        val = stringConverter.fromString(".0");
        assertEquals(0d, val, 0);

        val = stringConverter.fromString("55.12323423423423412312312323");
        assertEquals(55.12323423423423412312312323d, val, 0);

        val = stringConverter.fromString("55.23");
        assertEquals(55.23d, val, 0);

        val = stringConverter.fromString(null);
        assertEquals(55.23d, val, 0);
    }

    @Test
    public void testDoubleStringConverter_toString() {
        DoubleValidator spinnerValidator = new DoubleValidator(0d, 100d);
        StringConverter<Double> stringConverter = spinnerValidator.getStringConverter();

        String val = stringConverter.toString(22.456);
        assertEquals("22.456", val);

        val = stringConverter.toString(200d);
        assertEquals("22.456", val);

        val = stringConverter.toString(null);
        assertEquals("22.456", val);

        val = stringConverter.toString(33.12345678901234d);
        assertEquals("33.12345678901234", val);

        val = stringConverter.toString(32.45d);
        assertEquals("32.45", val);

        val = stringConverter.toString(-23.81d);
        assertEquals("32.45", val);
    }

    @Test
    @Ignore("Need to add PowerMock to instantiate final class TextFormatter.Change")
    public void testDoubleTextFormatter() {
        DoubleValidator spinnerValidator = new DoubleValidator(0d, 100d);
        TextFormatter<Double> doubleTextFormatter = spinnerValidator.getTextFormatter();
        UnaryOperator<TextFormatter.Change> unaryOperator = doubleTextFormatter.getFilter();
        TextFormatter.Change change = mock(TextFormatter.Change.class);

        unaryOperator.apply(change);
    }
}
