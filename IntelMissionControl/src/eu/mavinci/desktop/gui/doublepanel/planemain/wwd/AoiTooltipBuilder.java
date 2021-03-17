/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.planemain.wwd;

import com.intel.missioncontrol.helper.Expect;
import com.intel.missioncontrol.helper.ILanguageHelper;
import eu.mavinci.core.flightplan.PlanType;
import eu.mavinci.core.helper.StringHelper;
import eu.mavinci.flightplan.PicArea;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class AoiTooltipBuilder {

    public static final String AOI_PREFIX = "eu.mavinci.core.flightplan.PlanType.";

    private static final String RADIUS_LABEL = "radiusHeightWidget.labelRadius";
    private static final String HEIGHT_LABEL = "radiusHeightWidget.labelHeight";
    private static final String LENGTH_LABEL = "length.label";
    private static final String WIDTH_LABEL = "widthWidget.label";
    private static final String SURFACE_LABEL = "aoiSectionView.labelSurface";

    private final Map<PlanType, BiConsumer<PicArea, StringBuilder>> tooltipBuilders = new EnumMap<>(PlanType.class);

    private final ILanguageHelper languageHelper;

    public AoiTooltipBuilder(ILanguageHelper languageHelper) {
        Expect.notNull(languageHelper, "languageHelper");
        this.languageHelper = languageHelper;
        initTooltipBuilders();
    }

    private void initTooltipBuilders() {
        tooltipBuilders.put(PlanType.POLYGON, this::appendPolygonText);
        tooltipBuilders.put(PlanType.POINT_OF_INTEREST, this::appendPoiText);
        tooltipBuilders.put(PlanType.PANORAMA, this::appendPanoramaText);
        tooltipBuilders.put(PlanType.CITY, this::appendPolygonText);
        tooltipBuilders.put(PlanType.BUILDING, this::appendBuildingText);
        tooltipBuilders.put(PlanType.FACADE, this::appendBuildingText);
        tooltipBuilders.put(PlanType.CORRIDOR, this::appendCorridorText);
        tooltipBuilders.put(PlanType.SPIRAL, this::appendSpiralText);
        tooltipBuilders.put(PlanType.SEARCH, this::appendSquareSpiralText);
        tooltipBuilders.put(PlanType.TOWER, this::appendTowerText);
        tooltipBuilders.put(PlanType.WINDMILL, this::appendTowerText);
        tooltipBuilders.put(PlanType.NO_FLY_ZONE_POLY, this::appendPolygonText);
        tooltipBuilders.put(PlanType.NO_FLY_ZONE_CIRC, this::appendCircleText);
        tooltipBuilders.put(PlanType.GEOFENCE_POLY, this::appendPolygonText);
        tooltipBuilders.put(PlanType.GEOFENCE_CIRC, this::appendCircleText);
    }

    public String getTooltipText(PicArea picArea) {
        if (picArea == null) {
            return "";
        }

        PlanType planType = picArea.getPlanType();

        if (planType == null) {
            return "";
        }

        BiConsumer<PicArea, StringBuilder> toolTipBuilder = tooltipBuilders.get(planType);

        if (toolTipBuilder == null) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        toolTipBuilder.accept(picArea, builder);

        return builder.toString();
    }

    private void appendPolygonText(PicArea picArea, StringBuilder builder) {
        appendTitle(picArea, builder);
        appendSurfaceArea(picArea, builder);
    }

    private void appendPoiText(PicArea picArea, StringBuilder builder) {
        appendTitle(picArea, builder);
        appendHeight(picArea, builder);
    }

    private void appendPanoramaText(PicArea picArea, StringBuilder builder) {
        appendTitle(picArea, builder);
        appendHeight(picArea, builder);
    }

    private void appendBuildingText(PicArea picArea, StringBuilder builder) {
        appendTitle(picArea, builder);
        appendSurfaceArea(picArea, builder);
        appendHeight(picArea, builder);
    }

    private void appendCorridorText(PicArea picArea, StringBuilder builder) {
        appendTitle(picArea, builder);
        appendSurfaceArea(picArea, builder);
        appendLength(picArea, builder);
        appendWidth(picArea, builder);
    }

    private void appendCircleText(PicArea picArea, StringBuilder builder) {
        appendTitle(picArea, builder);
        appendSurfaceArea(picArea, builder);
        appendRadius(picArea, builder);
    }

    private void appendSpiralText(PicArea picArea, StringBuilder builder) {
        appendTitle(picArea, builder);
        appendSurfaceArea(picArea, builder);
        appendRadius(picArea, builder);
    }

    private void appendSquareSpiralText(PicArea picArea, StringBuilder builder) {
        appendTitle(picArea, builder);
        appendSurfaceArea(picArea, builder);
        appendRadius(picArea, builder);
    }

    private void appendTowerText(PicArea picArea, StringBuilder builder) {
        appendTitle(picArea, builder);
        appendSurfaceArea(picArea, builder);
        appendRadius(picArea, builder);
        appendHeight(picArea, builder);
    }

    private void appendTitle(PicArea picArea, StringBuilder builder) {
        builder.append("<b>").append(picArea.getName()).append("</b>");
    }

    private void appendSurfaceArea(PicArea picArea, StringBuilder builder) {
        String areaString = StringHelper.areaToIngName(picArea.getArea(), -3, false);
        appendValue(picArea, builder, SURFACE_LABEL, areaString);
    }

    private void appendHeight(PicArea picArea, StringBuilder builder) {
        appendDoubleLinearValue(picArea, builder, HEIGHT_LABEL, picArea.getObjectHeight());
    }

    private void appendLength(PicArea picArea, StringBuilder builder) {
        appendDoubleLinearValue(picArea, builder, LENGTH_LABEL, picArea.getLength());
    }

    private void appendWidth(PicArea picArea, StringBuilder builder) {
        appendDoubleLinearValue(picArea, builder, WIDTH_LABEL, picArea.getCorridorWidthInMeter());
    }

    private void appendRadius(PicArea picArea, StringBuilder builder) {
        // radius is stored in getCorridorWidthInMeter
        appendDoubleLinearValue(picArea, builder, RADIUS_LABEL, picArea.getCorridorWidthInMeter());
    }

    private void appendDoubleLinearValue(PicArea picArea, StringBuilder builder, String key, double value) {
        String valueString = StringHelper.lengthToIngName(value, -3, false);
        appendValue(picArea, builder, key, valueString);
    }

    private void appendValue(PicArea picArea, StringBuilder builder, String key, String value) {
        String label = this.languageHelper.getString(key);

        builder.append("<br>").append(label).append(" ").append(value);
    }

}
