/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui;

import javafx.css.Size;
import javafx.css.SizeUnits;

public class Convert {

    @SuppressWarnings("unused")
    public static double emsToPixels(String value) {
        if (value.endsWith("em")) {
            value = value.substring(0, value.length() - 2);
        }

        return new Size(Double.parseDouble(value), SizeUnits.EM).pixels();
    }

}
