/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.planning.aoi;

import com.intel.missioncontrol.beans.property.IQuantityStyleProvider;
import com.intel.missioncontrol.geometry.AreaOfInterest;
import com.intel.missioncontrol.helper.Expect;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.QuantityFormat;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.measure.UnitInfo;
import com.intel.missioncontrol.ui.controls.AdaptiveQuantityFormat;
import eu.mavinci.core.flightplan.PlanType;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;

public class AoiSummaryFactory {

    private static final String KEY_WIDTH = "widthWidget.label";
    private static final String KEY_LENGTH = "length.label";
    private static final String KEY_HEIGHT = "radiusHeightWidget.labelHeight";
    private static final String KEY_RADIUS = "radiusHeightWidget.labelRadius";
    private static final String KEY_SURFACE = "aoiSectionView.labelSurface";
    private static final String KEY_WAYPOINTS_LENGTH = "aoiSectionView.MANUAL.length";
    private static final String KEY_WAYPOINTS_NUMBER = "aoiSectionView.MANUAL.number";
    private static final String KEY_HEIGHT_WINDMILL = "radiusHeightWidget.labelHeightWindmill";
    private static final String KEY_RADIUS_WINDMILL = "radiusHeightWidget.labelRadiusWindmill";

    private final Map<PlanType, Consumer<AoiSummaryScope>> SUMMARY_BUILDERS = new EnumMap<>(PlanType.class);

    private final IQuantityStyleProvider quantityStyleProvider;
    private final ILanguageHelper languageHelper;
    private final QuantityFormat quantityFormat;

    public AoiSummaryFactory(IQuantityStyleProvider quantityStyleProvider, ILanguageHelper languageHelper) {
        Expect.notNull(languageHelper, "languageHelper");
        this.quantityStyleProvider = quantityStyleProvider;
        this.languageHelper = languageHelper;
        this.quantityFormat = new AdaptiveQuantityFormat(quantityStyleProvider);
        initBuilders();
    }

    private void initBuilders() {
        SUMMARY_BUILDERS.put(PlanType.POLYGON, this::buildPolygon);
        SUMMARY_BUILDERS.put(PlanType.POINT_OF_INTEREST, this::buildPoi);
        SUMMARY_BUILDERS.put(PlanType.PANORAMA, this::buildPanorama);
        SUMMARY_BUILDERS.put(PlanType.CITY, this::buildPolygon);
        SUMMARY_BUILDERS.put(PlanType.SPIRAL, this::buildSpiral);
        SUMMARY_BUILDERS.put(PlanType.SEARCH, this::buildSquareSpiral);
        SUMMARY_BUILDERS.put(PlanType.TOWER, this::buildTower);
        SUMMARY_BUILDERS.put(PlanType.BUILDING, this::buildBuilding);
        SUMMARY_BUILDERS.put(PlanType.FACADE, this::buildFacade);
        SUMMARY_BUILDERS.put(PlanType.CORRIDOR, this::buildCorridor);
        SUMMARY_BUILDERS.put(PlanType.MANUAL, this::buildWaypoints);
        SUMMARY_BUILDERS.put(PlanType.TARGET_POINTS, this::buildWaypoints);
        SUMMARY_BUILDERS.put(PlanType.COPTER3D, this::buildObjectSurface);
        SUMMARY_BUILDERS.put(PlanType.GEOFENCE_CIRC, this::buildGeofence);
        SUMMARY_BUILDERS.put(PlanType.GEOFENCE_POLY, this::buildGeofence);
        SUMMARY_BUILDERS.put(PlanType.NO_FLY_ZONE_CIRC, this::buildNoFlyZone);
        SUMMARY_BUILDERS.put(PlanType.NO_FLY_ZONE_POLY, this::buildNoFlyZone);
        SUMMARY_BUILDERS.put(PlanType.WINDMILL, this::buildWindmill);
    }

    private void buildPolygon(AoiSummaryScope summary) {
        putAreaSurface(summary, summary.getAreaOfInterest());
    }

    private void buildPoi(AoiSummaryScope summary) {
        // putAreaSurface(summary, summary.getAreaOfInterest());
    }

    private void buildPanorama(AoiSummaryScope summary) {
        // putAreaSurface(summary, summary.getAreaOfInterest());
    }

    private void buildSpiral(AoiSummaryScope summary) {
        AreaOfInterest areaOfInterest = summary.getAreaOfInterest();
        putAreaSurface(summary, areaOfInterest);
        // putRadius(summary, areaOfInterest);
    }

    private void buildSquareSpiral(AoiSummaryScope summary) {
        AreaOfInterest areaOfInterest = summary.getAreaOfInterest();
        putAreaSurface(summary, areaOfInterest);
        // putRadius(summary, areaOfInterest);
    }

    private void buildTower(AoiSummaryScope summary) {
        AreaOfInterest areaOfInterest = summary.getAreaOfInterest();
        putAreaSurface(summary, areaOfInterest);
        // putHeight(summary, areaOfInterest);
        // putRadius(summary, areaOfInterest);
    }

    private void buildFacade(AoiSummaryScope summary) {
        AreaOfInterest areaOfInterest = summary.getAreaOfInterest();
        putAreaSurface(summary, areaOfInterest);
        putLength(summary, areaOfInterest);
    }

    private void buildBuilding(AoiSummaryScope summary) {
        AreaOfInterest areaOfInterest = summary.getAreaOfInterest();
        putAreaSurface(summary, areaOfInterest);
        // putHeight(summary, areaOfInterest);
    }

    private void buildCorridor(AoiSummaryScope summary) {
        AreaOfInterest areaOfInterest = summary.getAreaOfInterest();
        putAreaSurface(summary, areaOfInterest);
        putLength(summary, areaOfInterest);
        // putWidth(summary, areaOfInterest);
    }

    private void buildWaypoints(AoiSummaryScope summary) {
        AreaOfInterest areaOfInterest = summary.getAreaOfInterest();
        putAreaSurface(summary, areaOfInterest);

        /*summary.getKeyValues()
            .put(
                languageHelper.getString(KEY_WAYPOINTS_NUMBER),
                Bindings.createStringBinding(
                    () -> String.valueOf(areaOfInterest.cornerListProperty().size()),
                    areaOfInterest.cornerListProperty()));

        // TODO how to get waypoints length ???
        summary.getKeyValues().put(languageHelper.getString(KEY_WAYPOINTS_LENGTH), new ReadOnlyStringWrapper("0 m"));
        */
    }

    private void buildObjectSurface(AoiSummaryScope summary) {
        AreaOfInterest areaOfInterest = summary.getAreaOfInterest();
        putAreaSurface(summary, areaOfInterest);
    }

    private void buildGeofence(AoiSummaryScope summary) {
        // AreaOfInterest areaOfInterest = summary.getAreaOfInterest();
        // putAreaSurface(summary, areaOfInterest);
    }

    private void buildNoFlyZone(AoiSummaryScope summary) {
        // AreaOfInterest areaOfInterest = summary.getAreaOfInterest();
        // putAreaSurface(summary, areaOfInterest);
    }

    private void buildWindmill(AoiSummaryScope summary) {
        AreaOfInterest areaOfInterest = summary.getAreaOfInterest();
        putAreaSurface(summary, areaOfInterest);
        // putHeight(summary, areaOfInterest);
        // putRadius(summary, areaOfInterest);
    }

    private void putAreaSurface(AoiSummaryScope summary, AreaOfInterest areaOfInterest) {
        summary.getKeyValues()
            .put(
                languageHelper.getString(KEY_SURFACE),
                Bindings.createStringBinding(
                    () -> getReadableSurface(areaOfInterest),
                    areaOfInterest.surfaceAreaProperty(),
                    quantityStyleProvider.systemOfMeasurementProperty()));
    }

    private void putRadius(AoiSummaryScope summary, AreaOfInterest areaOfInterest) {
        String radius = (areaOfInterest.getType() == PlanType.WINDMILL) ? KEY_RADIUS_WINDMILL : KEY_RADIUS;
        summary.getKeyValues()
            .put(languageHelper.getString(radius), readableLengthObservable(areaOfInterest.widthProperty()));
    }

    private void putHeight(AoiSummaryScope summary, AreaOfInterest areaOfInterest) {
        String height = (areaOfInterest.getType() == PlanType.WINDMILL) ? KEY_HEIGHT_WINDMILL : KEY_HEIGHT;
        summary.getKeyValues()
            .put(languageHelper.getString(height), readableLengthObservable(areaOfInterest.heightProperty()));
    }

    private void putLength(AoiSummaryScope summary, AreaOfInterest areaOfInterest) {
        summary.getKeyValues()
            .put(languageHelper.getString(KEY_LENGTH), readableLengthObservable(areaOfInterest.lengthProperty()));
    }

    private void putWidth(AoiSummaryScope summary, AreaOfInterest areaOfInterest) {
        summary.getKeyValues()
            .put(languageHelper.getString(KEY_WIDTH), readableLengthObservable(areaOfInterest.widthProperty()));
    }

    private ObservableValue<String> readableLengthObservable(ObservableValue<Number> value) {
        return Bindings.createStringBinding(
            () -> getReadableLength(value.getValue()), value, quantityStyleProvider.systemOfMeasurementProperty());
    }

    private String getReadableLength(Number value) {
        if (value == null) {
            return "0";
        }

        return quantityFormat.format(Quantity.of(value.doubleValue(), Unit.METER), UnitInfo.LOCALIZED_LENGTH);
    }

    private String getReadableSurface(AreaOfInterest areaOfInterest) {
        return quantityFormat.format(
            Quantity.of(areaOfInterest.getSurfaceArea(), Unit.SQUARE_METER), UnitInfo.LOCALIZED_AREA);
    }

    public AoiSummaryScope getAoiSummary(AreaOfInterest areaOfInterest) {
        final AoiSummaryScope summary = new AoiSummaryScope(areaOfInterest);
        final PlanType areaOfInterestName = areaOfInterest.getType();
        Consumer<AoiSummaryScope> builder = SUMMARY_BUILDERS.get(areaOfInterestName);

        if (builder == null) {
            throw new UnsupportedOperationException(String.format("Area not supported: %s", areaOfInterestName));
        }

        builder.accept(summary);

        return summary;
    }

}
