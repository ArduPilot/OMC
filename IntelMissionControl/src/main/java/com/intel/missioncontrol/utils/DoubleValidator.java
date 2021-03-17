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

import java.text.DecimalFormat;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class DoubleValidator extends SpinnerValidator<Double> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DoubleValidator.class);

    private static final double DEFAULT_VALUE = 0.0;
    private static final String DEFAULT_STRING_VALUE = "0.0";
    public static final double DEFAULT_AMOUNT_TO_STEP_BY = 1.0;
    public static final Pattern VALUE_PATTERN = Pattern.compile("^-?\\d*\\.?\\d*$");

    private StringConverter<Double> stringConverter;
    private SpinnerValueFactory<Double> valueFactory;
    private Predicate<Double> rangePredicate = value -> value != null && value >= minValue && value <= maxValue;

    private DecimalFormat formatter = new DecimalFormat(DEFAULT_STRING_VALUE);

    public DoubleValidator(Double minValue, Double maxValue) {
        this(DEFAULT_VALUE, minValue, maxValue);
    }

    public DoubleValidator(Double defaultValue, Double minValue, Double maxValue) {
        this(defaultValue, minValue, maxValue, DEFAULT_AMOUNT_TO_STEP_BY);
    }

    public DoubleValidator(Double defaultValue, Double minValue, Double maxValue, Double amountToStepBy) {
        super(defaultValue, minValue, maxValue, amountToStepBy);

        formatter.setMaximumFractionDigits(50);
    }

    protected TextFormatter.Change applyTextFilter(TextFormatter.Change textFormatter) {
        TextFormatter.Change result = null;
        String newText = textFormatter.getControlNewText();
        if (VALUE_PATTERN.matcher(newText).matches() && isSignificantString(newText)) {
            try {
                Double testValue = Double.valueOf(newText);
                if (rangePredicate.test(testValue)) {
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
    public StringConverter<Double> getStringConverter() {
        if (stringConverter == null) {
            stringConverter =
                new StringConverter<Double>() {
                    @Override
                    public String toString(Double object) {
                        if (rangePredicate.test(object)) {
                            getValueFactory().setValue(object);
                        }

                        return formatter.format(getValueFactory().getValue());
                    }

                    @Override
                    public Double fromString(String string) {
                        Double preResult = null;
                        if (isSignificantString(string)) {
                            try {
                                preResult = Double.valueOf(string);
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
    public SpinnerValueFactory<Double> getValueFactory() {
        if (valueFactory == null) {
            SpinnerValueFactory.DoubleSpinnerValueFactory valueFactory =
                new SpinnerValueFactory.DoubleSpinnerValueFactory(minValue, maxValue, defaultValue, amountToStepBy);
            valueFactory.setConverter(getStringConverter());
            valueFactory
                .maxProperty()
                .addListener(
                    (observable, oldValue, newValue) -> {
                        maxValue = newValue.doubleValue();
                    });
            valueFactory
                .minProperty()
                .addListener(
                    (observable, oldValue, newValue) -> {
                        minValue = newValue.doubleValue();
                    });

            this.valueFactory = valueFactory;
        }

        return valueFactory;
    }
}
