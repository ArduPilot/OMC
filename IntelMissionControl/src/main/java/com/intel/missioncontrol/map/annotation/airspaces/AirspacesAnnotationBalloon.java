/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.annotation.airspaces;

import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.helper.ScaleHelper;
import eu.mavinci.airspace.IAirspace;
import eu.mavinci.desktop.gui.doublepanel.planemain.wwd.GlobeBaloonAnnotation;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.Annotation;
import gov.nasa.worldwind.render.AnnotationAttributes;
import gov.nasa.worldwind.render.AnnotationFlowLayout;
import gov.nasa.worldwind.render.ScreenAnnotation;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Point;
import org.apache.commons.lang3.StringUtils;

class AirspacesAnnotationBalloon extends GlobeBaloonAnnotation {
    private static final Insets BALOON_INSETS = new Insets(8, 10, 8, 10);
    private static final int BALOON_OPACITY = 1;

    private static final Color BALOON_BACKGROUND_COLOR = new Color(255, 255, 255);
    private static final Color TITLE_TEXT_COLOR = new Color(183, 0, 0);
    private static final Color ALTITUDE_TEXT_COLOR = new Color(0, 0, 0);

    private final ILanguageHelper languageHelper;

    AirspacesAnnotationBalloon(Position position, IAirspace airspace, ILanguageHelper languageHelper) {
        super("", position);
        this.languageHelper = languageHelper;

        AnnotationAttributes attributes = new AnnotationAttributes();
        attributes.setCornerRadius(5);
        attributes.setBorderWidth(0);
        attributes.setAdjustWidthToText(AVKey.SIZE_FIT_TEXT);
        attributes.setLeaderGapWidth(20);
        attributes.setDrawOffset(new Point(0, 0));
        attributes.setSize(new Dimension(0, 0));
        attributes.setLeader(AVKey.SHAPE_TRIANGLE);
        attributes.setBackgroundColor(BALOON_BACKGROUND_COLOR);
        attributes.setInsets(BALOON_INSETS);
        attributes.setOpacity(BALOON_OPACITY);
        getAttributes().setDefaults(attributes);

        setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
        setAlwaysOnTop(true);

        setLayout(new AnnotationFlowLayout(AVKey.VERTICAL, AVKey.LEFT, 1, 4));

        Annotation airspaceNameAnnotation = textAirspaceTitleAnnotation();
        airspaceNameAnnotation.setText(airspaceTitle(airspace));

        Annotation airspaceTypeAnnotation = textAirspaceTitleAnnotation();
        airspaceTypeAnnotation.setText(airspaceType(airspace));

        Annotation floorAltitudeAnnotation = createAltitudeAnnotation();
        floorAltitudeAnnotation.setText(floorAltitudeText(airspace));

        if (StringUtils.isNotBlank(airspace.getName())) {
            addChild(airspaceNameAnnotation);
        }

        addChild(airspaceTypeAnnotation);
        addChild(floorAltitudeAnnotation);
    }

    private Annotation textAirspaceTitleAnnotation() {
        Annotation annotation = new ScreenAnnotation("", new java.awt.Point());
        annotation.setPickEnabled(false);

        Font nameFont = annotation.getAttributes().getFont();
        nameFont = new Font(nameFont.getName(), Font.BOLD, (int)ScaleHelper.emsToPixels(1));

        AnnotationAttributes attributes = new AnnotationAttributes();
        attributes.setTextColor(TITLE_TEXT_COLOR);
        attributes.setFont(nameFont);
        attributes.setAdjustWidthToText(AVKey.SIZE_FIT_TEXT);
        attributes.setBorderWidth(0);
        attributes.setCornerRadius(0);
        attributes.setSize(new Dimension(350, 0));
        attributes.setDrawOffset(new java.awt.Point(0, 0));
        attributes.setInsets(new java.awt.Insets(0, 0, 0, 0));
        attributes.setEffect(AVKey.TEXT_EFFECT_NONE);

        annotation.getAttributes().setDefaults(attributes);

        return annotation;
    }

    private String airspaceTitle(IAirspace airspace) {
        return String.format("%s", airspace.getName());
    }

    private String airspaceType(IAirspace airspace) {
        return String.format("(%s)", languageHelper.getString(airspace.getType().getLocalizationKey()));
    }

    private Annotation createAltitudeAnnotation() {
        Annotation annotation = new ScreenAnnotation("", new java.awt.Point());
        annotation.setPickEnabled(false);

        AnnotationAttributes attributes = new AnnotationAttributes();
        attributes.setTextColor(ALTITUDE_TEXT_COLOR);
        attributes.setBorderWidth(0);
        attributes.setCornerRadius(0);
        attributes.setDrawOffset(new java.awt.Point(0, 0));
        attributes.setInsets(new java.awt.Insets(0, 0, 0, 0));
        attributes.setEffect(AVKey.TEXT_EFFECT_NONE);

        annotation.getAttributes().setDefaults(attributes);

        return annotation;
    }

    private String floorAltitudeText(IAirspace airspace) {
        Double groundRef = airspace.getFloorReferenceGround();
        Double seaLevelRef = airspace.getFloorReferenceSeaLevel();
        if (groundRef == null && seaLevelRef == null) {
            return "";
        }

        return String.format(
            "<b>%s</b>" + (groundRef == null ? "%s%s" : "<br>%s %s") + (seaLevelRef == null ? "%s%s" : "<br>%s %s"),
            translate("altitude.floor.caption"),
            (groundRef == null ? "" : translate("altitude.floor.groundReference")),
            altitudeToString(groundRef),
            (seaLevelRef == null ? "" : translate("altitude.floor.seaLevelReference")),
            altitudeToString(seaLevelRef));
    }

    private String ceilingAltitudeText(IAirspace airspace) {
        Double ceilingRef = airspace.getCeilingReferenceGroundOrSeaLevel();
        if (ceilingRef == null) {
            return "";
        }

        return String.format(
            "<b>%s</b><br>%s %s<br>%s %s",
            translate("altitude.ceiling.caption"),
            translate("altitude.ceiling.groundReference"),
            altitudeToString(ceilingRef),
            translate("altitude.ceiling.seaLevelReference"),
            altitudeToString(ceilingRef));
    }

    private String translate(String shortKey) {
        return languageHelper.getString("com.intel.missioncontrol.map.layers.airspaces.baloon." + shortKey);
    }

    private String altitudeToString(Double altitude) {
        if (altitude == null) {
            return "";
        }

        return String.format("%d %s", altitude.intValue(), translate("altitude.reference.unit"));
    }
}
