/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.utils;

import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextFormatter;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Predicate;
import java.util.regex.Pattern;

public class IntegerValidator extends SpinnerValidator<Integer> {

    private static final Logger LOGGER = LoggerFactory.getLogger(IntegerValidator.class);

    public static final int DEFAULT_VALUE = 0;
    public static final int DEFAULT_MIN_DIGITS = 1;
    public static final int DEFAULT_MAX_DIGITS = 10;
    public static final int DEFAULT_AMOUNT_TO_STEP_BY = 1;
    public static final String VALUE_PATTERN_STR = "^-?[0-9]{%d,%d}";

    public final Pattern valuePattern;

    private StringConverter<Integer> stringConverter;
    private SpinnerValueFactory<Integer> valueFactory;
    private Predicate<Integer> rangePredicate = value -> value != null && value >= minValue && value <= maxValue;

    public IntegerValidator(int minValue, int maxValue) {
        this(DEFAULT_VALUE, minValue, maxValue);
    }

    public IntegerValidator(int defaultValue, int minValue, int maxValue) {
        this(defaultValue, minValue, maxValue, DEFAULT_MIN_DIGITS, DEFAULT_MAX_DIGITS, DEFAULT_AMOUNT_TO_STEP_BY);
    }

    public IntegerValidator(int defaultValue, int minValue, int maxValue, int minDigits, int maxDigits) {
        this(defaultValue, minValue, maxValue, minDigits, maxDigits, DEFAULT_AMOUNT_TO_STEP_BY);
    }

    public IntegerValidator(
            Integer defaultValue,
            Integer minValue,
            Integer maxValue,
            Integer minDigits,
            Integer maxDigits,
            Integer amountToStepBy) {
        super(defaultValue, minValue, maxValue, amountToStepBy);

        valuePattern = Pattern.compile(String.format(VALUE_PATTERN_STR, minDigits, maxDigits));
    }

    protected TextFormatter.Change applyTextFilter(TextFormatter.Change textFormatter) {
        TextFormatter.Change result = null;
        String newText = textFormatter.getControlNewText();
        if (valuePattern.matcher(newText).matches()) {
            try {
                Integer parsedValue = Integer.valueOf(newText);
                if (rangePredicate.test(parsedValue)) {
                    result = textFormatter.clone();
                }
            } catch (NumberFormatException e) {
                // Should never happen because of pattern validator
                LOGGER.warn("Could not parse string " + newText, e);
            }
        }

        textFormatter.setCaretPosition(0);
        return result;
    }

    @Override
    public StringConverter<Integer> getStringConverter() {
        if (stringConverter == null) {
            stringConverter =
                new StringConverter<Integer>() {

                    @Override
                    public String toString(Integer object) {
                        if (rangePredicate.test(object)) {
                            getValueFactory().setValue(object);
                        }

                        return getValueFactory().getValue().toString();
                    }

                    @Override
                    public Integer fromString(String string) {
                        if (isSignificantString(string)) {
                            try {
                                Integer preResult = Integer.valueOf(string);
                                if (rangePredicate.test(preResult)) {
                                    getValueFactory().setValue(preResult);
                                }
                            } catch (NumberFormatException e) {
                                // Should never happen because of TextFormatter
                                LOGGER.error("Error on parsing " + string, e);
                            }
                        }

                        return getValueFactory().getValue();
                    }
                };
        }

        return stringConverter;
    }

    @Override
    public SpinnerValueFactory<Integer> getValueFactory() {
        if (valueFactory == null) {
            SpinnerValueFactory.IntegerSpinnerValueFactory valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(minValue, maxValue, defaultValue, amountToStepBy);
            valueFactory.setConverter(getStringConverter());
            valueFactory
                .maxProperty()
                .addListener(
                    (observable, oldValue, newValue) -> {
                        maxValue = newValue.intValue();
                    });
            valueFactory
                .minProperty()
                .addListener(
                    (observable, oldValue, newValue) -> {
                        minValue = newValue.intValue();
                    });

            this.valueFactory = valueFactory;
        }

        return valueFactory;
    }
}
