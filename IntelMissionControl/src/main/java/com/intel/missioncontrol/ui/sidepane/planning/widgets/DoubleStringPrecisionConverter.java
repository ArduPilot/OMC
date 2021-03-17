/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.planning.widgets;

import com.google.common.base.Strings;
import javafx.util.StringConverter;

import java.text.DecimalFormat;
import java.text.ParseException;

/** Double to String converter able to set the number of fractional digits */
public class DoubleStringPrecisionConverter extends StringConverter<Double> {

    private final DecimalFormat format;

    public DoubleStringPrecisionConverter(int fractionDigits) {
        if (fractionDigits < 0) {
            fractionDigits = 0;
        }

        String fractionPattern = "";

        if (fractionDigits > 0) {
            fractionPattern = "." + Strings.repeat("0", fractionDigits);
        }

        format = new DecimalFormat("0" + fractionPattern);
    }

    @Override
    public String toString(Double value) {
        if (value == null) {
            return "";
        }

        return format.format(value);
    }

    @Override
    public Double fromString(String value) {
        try {
            if (value == null) {
                return null;
            }

            value = value.trim();

            if (value.length() < 1) {
                return null;
            }

            return format.parse(value).doubleValue();
        } catch (ParseException ex) {
            throw new RuntimeException(ex);
        }
    }

}
