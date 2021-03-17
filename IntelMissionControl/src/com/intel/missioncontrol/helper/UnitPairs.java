/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.helper;

public class UnitPairs {

    private final String stringValue;
    private final double doubleValue;

    public UnitPairs(String stringValue, double doubleValue) {
        this.stringValue = stringValue;
        this.doubleValue = doubleValue;
    }

    public String getStringValue() {
        return this.stringValue;
    }

    public double getDoubleValue() {
        return this.doubleValue;
    }

    public String format(String format) {
        return String.format(format, doubleValue, stringValue);
    }
}
