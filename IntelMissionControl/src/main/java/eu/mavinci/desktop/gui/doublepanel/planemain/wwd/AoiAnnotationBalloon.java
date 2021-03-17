/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.planemain.wwd;

import com.intel.missioncontrol.helper.ScaleHelper;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.AnnotationAttributes;
import java.awt.Color;
import java.awt.Font;
import java.awt.Point;

/** @author João Santarém */
public class AoiAnnotationBalloon extends GlobeBaloonAnnotation {

    public AoiAnnotationBalloon() {
        this("", Position.ZERO);
    }

    public AoiAnnotationBalloon(String text, Position position) {
        super(text, position);

        AnnotationAttributes attributes = new AnnotationAttributes();
        attributes.setLeaderGapWidth(20);
        attributes.setAdjustWidthToText(AVKey.SIZE_FIXED);
        attributes.setCornerRadius(5);
        attributes.setTextAlign(AVKey.LEFT);
        attributes.setBackgroundColor(Color.LIGHT_GRAY);
        attributes.setLeader(AVKey.SHAPE_TRIANGLE);
        attributes.setDrawOffset(new Point(0, 10));
        attributes.setOpacity(0.9);

        Font font = new Font(attributes.getFont().getName(), Font.PLAIN, (int)ScaleHelper.emsToPixels(1));
        attributes.setFont(font);

        getAttributes().setDefaults(attributes);
        getAttributes().setFont(font);

        setAltitudeMode(WorldWind.ABSOLUTE);
        setAlwaysOnTop(true);
    }
}
