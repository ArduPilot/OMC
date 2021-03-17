/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.utils;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextFormatter;
import javafx.util.StringConverter;

import java.util.Set;

public abstract class SpinnerValidator<T extends Number> {

    private static final Set<String> INSIGNIFICANT_STRINGS = ImmutableSet.of(".", "-", "-.");

    protected T minValue;
    protected T maxValue;
    protected T defaultValue;
    protected T amountToStepBy;
    protected TextFormatter<T> textFormatter;

    public SpinnerValidator(T defaultValue, T minValue, T maxValue, T amountToStepBy) {
        this.defaultValue = defaultValue;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.amountToStepBy = amountToStepBy;
    }

    public TextFormatter<T> getTextFormatter() {
        if (textFormatter == null) {
            textFormatter = new TextFormatter<>(getStringConverter(), defaultValue, this::applyTextFilter);
        }

        return textFormatter;
    }

    public abstract StringConverter<T> getStringConverter();

    public abstract SpinnerValueFactory<T> getValueFactory();

    protected abstract TextFormatter.Change applyTextFilter(TextFormatter.Change textChange);

    protected boolean isSignificantString(String string) {
        return !(Strings.isNullOrEmpty(string) || INSIGNIFICANT_STRINGS.contains(string));
    }
}
