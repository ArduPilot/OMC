/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.helper;

import java.awt.Color;

public class ColorHelper {
    public static Color setAlpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    public static Color removeAlpha(Color color) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue());
    }

    public static Color scaleAlpha(Color color, double alphaScale) {
        return new Color(
            color.getRed(), color.getGreen(), color.getBlue(), (int)Math.round(color.getAlpha() * alphaScale));
    }

    public static Color fadeColor(Color color1, Color color2, double pos) {
        return new Color(
            (int)MathHelper.intoRange(color1.getRed() + pos * (color2.getRed() - color1.getRed()), 0, 255),
            (int)MathHelper.intoRange(color1.getGreen() + pos * (color2.getGreen() - color1.getGreen()), 0, 255),
            (int)MathHelper.intoRange(color1.getBlue() + pos * (color2.getBlue() - color1.getBlue()), 0, 255));
    }

    public static Color scaleAlphaToShadow(Color color) {
        return scaleAlpha(color, SHADOW_ALPHA_SCALE);
    }

    public static final double SHADOW_ALPHA_SCALE = 0.5;

    public static final Color MAVINCI_LIGHTBLUE = new Color(27, 117, 188);
    public static final Color MAVINCI_DARKBLUE = new Color(0, 74, 128);
    public static final Color TRANSPARENT = new Color(0, 0, 0, 0);
    public static final Color HIDDEN_FILE_TEXT = Color.gray;

    public static Color invertColor(Color c) {
        return new Color(255 - c.getRed(), 255 - c.getGreen(), 255 - c.getBlue(), c.getAlpha());
    }
}
