/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.helper;

import java.awt.Font;

public class FontHelper {

    private static Font baseFont = new Font("Intel Clear", Font.PLAIN, (int)Math.round(ScaleHelper.emsToPixels(1)));

    public static Font getBaseFont(double emsScale) {
        return getBaseFont(emsScale, Font.PLAIN);
    }

    public static Font getBaseFont(double emsScale, int attributes) {
        return baseFont.deriveFont(attributes, (int)Math.round(ScaleHelper.emsToPixels(emsScale)));
    }

}
