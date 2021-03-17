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

import java.text.ParseException;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/** @author Vladimir Iordanov */
public class DurationTimeSpinnerValidator extends SpinnerValidator<Integer> {

    private static final String DEFAULT_STRING_VALUE = "00:00:00";
    public static final int DEFAULT_VALUE = 0;
    public static final int DEFAULT_TO_STEP_BY = 1;

    private static final Logger LOGGER = LoggerFactory.getLogger(DurationTimeSpinnerValidator.class);
    public static final Pattern TIME_VALUE_PATTERN = Pattern.compile("(([01]?[0-9]|2[0-3]):[0-5][0-9]):[0-5][0-9]");

    private StringConverter<Integer> stringConverter;
    private SpinnerValueFactory<Integer> valueFactory;

    private final DurationConverter durationConverter = new DurationConverter();

    private Predicate<Integer> valueValidator = value -> value != null && value >= minValue && value <= maxValue;

    public DurationTimeSpinnerValidator(Integer minValue, Integer maxValue) {
        super(DEFAULT_VALUE, minValue, maxValue, DEFAULT_TO_STEP_BY);
    }

    protected TextFormatter.Change applyTextFilter(TextFormatter.Change textChange) {
        TextFormatter.Change result = null;
        String newText = textChange.getControlNewText();
        if (TIME_VALUE_PATTERN.matcher(newText).matches()) {
            String valueToParse =
                textChange.getControlText().isEmpty() ? DEFAULT_STRING_VALUE : textChange.getControlText();
            try {
                int parsedValue = durationConverter.parseToSeconds(valueToParse);
                if (valueValidator.test(parsedValue)) {
                    result = textChange.clone();
                }
            } catch (ParseException e) {
                // Should never happen because of TextFormatter
                LOGGER.error("Error on parsing " + valueToParse, e);
            }
        }

        return result;
    }

    @Override
    public StringConverter<Integer> getStringConverter() {
        if (stringConverter == null) {
            stringConverter =
                new StringConverter<Integer>() {

                    @Override
                    public String toString(Integer object) {
                        if (valueValidator.test(object)) {
                            getValueFactory().setValue(object);
                        }

                        return getValueFactory().getValue().toString();
                    }

                    @Override
                    public Integer fromString(String string) {
                        try {
                            Integer tmp = durationConverter.parseToSeconds(string);
                            if (valueValidator.test(tmp)) {
                                getValueFactory().setValue(tmp);
                            }
                        } catch (ParseException e) {
                            // Should never happen because of TextFormatter
                            LOGGER.error("Error on parsing " + string, e);
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
                new SpinnerValueFactory.IntegerSpinnerValueFactory(minValue, maxValue, defaultValue);
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
