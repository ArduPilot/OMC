/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.analysis;

import eu.mavinci.core.obfuscation.IKeepAll;

public enum ImageChannel implements IKeepAll {
    RGB("rgb"),
    R("r"),
    G("g"),
    B("b"),
    RE("re"),
    IR("ir"),
    THERMAL("thermal");

    private String stringName;

    ImageChannel(String stringName) {
        this.stringName = stringName;
    }

    public String getStringName() {
        return stringName;
    }

    public static ImageChannel fromStringName(String stringName) {
        for (ImageChannel imageChannel : values()) {
            if (imageChannel.stringName.equalsIgnoreCase(stringName)) {
                return imageChannel;
            }
        }

        return null;
    }
}
