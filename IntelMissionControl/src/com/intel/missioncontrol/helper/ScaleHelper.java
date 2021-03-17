/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.helper;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

public class ScaleHelper {

    private static final double FONT_SIZE = 12;
    private static double FONT_SIZE_SCALED = 12;

    private static double SCALE_FACTOR = 1;
    private static DoubleProperty scaleProp = new SimpleDoubleProperty(SCALE_FACTOR);

    public static double getScaleFactor() {
        return SCALE_FACTOR;
    }

    public static final double emsToPixels(double ems) {
        return ems * FONT_SIZE_SCALED;
    }

    public static final double pixelsToEms(double pixels) {
        return pixels / FONT_SIZE_SCALED;
    }

    public static final double scalePixels(double pixels) {
        return pixels * SCALE_FACTOR;
    }

    public static final int scalePixelsAsInt(double pixels) {
        return (int)Math.round(scalePixels(pixels));
    }

    public static final int scalePixelsAsIntMin1(double x) {
        return Math.max(1, scalePixelsAsInt(x));
    }

    public static final Insets scaleInsets(Insets insets) {
        return new Insets(
            scalePixelsAsIntMin1(insets.top),
            scalePixelsAsIntMin1(insets.left),
            scalePixelsAsIntMin1(insets.bottom),
            scalePixelsAsIntMin1(insets.right));
    }

    public static final Dimension scaleDimension(Dimension d) {
        return new Dimension(scalePixelsAsIntMin1(d.getWidth()), scalePixelsAsIntMin1(d.getHeight()));
    }

    public static final Point scalePoint(Point p) {
        return new Point(scalePixelsAsIntMin1(p.getX()), scalePixelsAsIntMin1(p.getY()));
    }

    public static void setScaleFactor(double scaleFactor) {
        SCALE_FACTOR = scaleFactor;
        FONT_SIZE_SCALED = FONT_SIZE * scaleFactor;

        //there are things which are listening to the scale factor change and use FONT_SIZE_SCALED
        scaleProp.setValue(scaleFactor);
    }

    public static ReadOnlyDoubleProperty scalePropProperty() {
        return scaleProp;
    }
}
