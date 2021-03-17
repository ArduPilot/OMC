/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.planning.widgets;

import com.google.inject.Inject;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.beans.binding.QuantityBindings;
import com.intel.missioncontrol.beans.property.IQuantityStyleProvider;
import com.intel.missioncontrol.beans.property.PropertyPath;
import com.intel.missioncontrol.beans.property.PropertyPathStore;
import com.intel.missioncontrol.beans.property.QuantityProperty;
import com.intel.missioncontrol.beans.property.SimpleQuantityProperty;
import com.intel.missioncontrol.flightplantemplate.AreasOfInterestType;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.measure.Dimension.Length;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.measure.UnitInfo;
import com.intel.missioncontrol.mission.FlightPlan;
import com.intel.missioncontrol.mission.Mission;
import de.saxsys.mvvmfx.ViewModel;
import eu.mavinci.core.flightplan.AltitudeAdjustModes;
import eu.mavinci.core.flightplan.PlanType;
import eu.mavinci.core.helper.MinMaxPair;
import eu.mavinci.flightplan.Flightplan;
import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;

public class GsdWidgetViewModel implements ViewModel {

    public static final String LABEL_ALT_DISTANCE = "gsdWidget.labelAltDistance";
    public static final String LABEL_ALT_DISTANCE_2D = "gsdWidget.labelAltDistance2d";
    public static final String LABEL_ALT_DISTANCE_3D = "gsdWidget.labelAltDistance3d";
    public static final String LABEL_GSD = "gsdWidget.labelGsd";
    public static final String LABEL_GSD_AVG = "gsdWidget.labelGsdAvg";

    private final QuantityProperty<Length> gsdQuantity;
    private final DoubleProperty gsd = new SimpleDoubleProperty();

    private final QuantityProperty<Length> altitudeQuantity;
    private final DoubleProperty altitude = new SimpleDoubleProperty(0.0);

    private final ObjectProperty<PlanType> aoiType = new SimpleObjectProperty<>();
    private final InvalidationListener propertyChangeListener = observable -> calculateGSDInfo();
    private final ReadOnlyObjectProperty<AltitudeAdjustModes> currentAltMode;

    private final StringProperty lblAltDistanceProperty = new SimpleStringProperty();
    private final StringProperty lblGsdProperty = new SimpleStringProperty();

    private final ILanguageHelper languageHelper;
    private final PropertyPathStore propertyPathStore = new PropertyPathStore();
    private final ChangeListener<AltitudeAdjustModes> altitudeAdjustModesChangeListener;

    public Quantity<Length> getGsdLowerEndOfRange() {
        return gsdLowerEndOfRangeQuantitiy.get();
    }

    public QuantityProperty<Length> gsdLowerEndOfRangeProperty() {
        return gsdLowerEndOfRangeQuantitiy;
    }

    public Quantity<Length> getGsdUpperEndOfRange() {
        return gsdUpperEndOfRangeQuantitiy.get();
    }

    public QuantityProperty<Length> gsdUpperEndOfRangeProperty() {
        return gsdUpperEndOfRangeQuantitiy;
    }

    private final IApplicationContext applicationContext;
    private final DoubleProperty gsdLowerEndOfRange = new SimpleDoubleProperty();
    private final DoubleProperty gsdUpperEndOfRange = new SimpleDoubleProperty();
    private final QuantityProperty<Length> gsdLowerEndOfRangeQuantitiy;
    private final QuantityProperty<Length> gsdUpperEndOfRangeQuantitiy;

    public BooleanProperty gsdOutSideToleranceProperty() {
        return gsdOutSideTolerance;
    }

    private final BooleanProperty gsdOutSideTolerance = new SimpleBooleanProperty(false);

    @Inject
    public GsdWidgetViewModel(
            IQuantityStyleProvider quantityStyleProvider,
            IApplicationContext applicationContext,
            ILanguageHelper languageHelper) {
        this.languageHelper = languageHelper;
        this.applicationContext = applicationContext;

        gsdQuantity =
            new SimpleQuantityProperty<>(
                this, "gsd", quantityStyleProvider, new UnitInfo<>(Unit.CENTIMETER, Unit.INCH, Unit.INCH));
        QuantityBindings.bindBidirectional(gsdQuantity, gsd, Unit.METER);

        altitudeQuantity =
            new SimpleQuantityProperty<>(
                this, "altitude", quantityStyleProvider, UnitInfo.LOCALIZED_LENGTH, Quantity.of(0.0, Unit.METER));
        QuantityBindings.bindBidirectional(altitudeQuantity, altitude, Unit.METER);

        gsdLowerEndOfRangeQuantitiy =
            new SimpleQuantityProperty<>(
                this,
                "gsdLowerEndOfRange",
                quantityStyleProvider,
                new UnitInfo<>(Unit.CENTIMETER, Unit.INCH, Unit.INCH));
        QuantityBindings.bindBidirectional(gsdLowerEndOfRangeQuantitiy, gsdLowerEndOfRange, Unit.METER);

        gsdUpperEndOfRangeQuantitiy =
            new SimpleQuantityProperty<>(
                this,
                "gsdUpperEndOfRange",
                quantityStyleProvider,
                new UnitInfo<>(Unit.CENTIMETER, Unit.INCH, Unit.INCH));
        QuantityBindings.bindBidirectional(gsdUpperEndOfRangeQuantitiy, gsdUpperEndOfRange, Unit.METER);

        gsdProperty().addListener((obj, oldV, newV) -> calculateGSDInfo());
        propertyPathStore
            .from(applicationContext.currentMissionProperty())
            .select(Mission::currentFlightPlanProperty)
            .selectReadOnlyObject(FlightPlan::gsdMismatchRangeProperty)
            .addListener((obj, oldV, newV) -> calculateGSDInfo());

        propertyPathStore
            .from(applicationContext.currentMissionProperty())
            .select(Mission::currentFlightPlanProperty)
            .selectReadOnlyDouble(FlightPlan::gsdToleranceProperty)
            .addListener((obj, oldV, newV) -> calculateGSDInfo());

        calculateGSDInfo();

        currentAltMode =
            PropertyPath.from(applicationContext.currentMissionProperty())
                .select(Mission::currentFlightPlanProperty)
                .selectReadOnlyObject(FlightPlan::currentAltModeProperty);
        altitudeAdjustModesChangeListener = (obj, oldV, newV) -> lblsChanged(applicationContext, aoiType.getValue());
        currentAltMode.addListener(new WeakChangeListener<>(altitudeAdjustModesChangeListener));

        aoiTypeProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    lblsChanged(applicationContext, newValue);
                    calculateGSDInfo();
                });

        lblsChanged(applicationContext, aoiType.getValue());
    }

    public DoubleProperty gsdProperty() {
        return gsd;
    }

    public DoubleProperty altitudeProperty() {
        return altitude;
    }

    public QuantityProperty<Length> gsdQuantityProperty() {
        return gsdQuantity;
    }

    public QuantityProperty<Length> altitudeQuantityProperty() {
        return altitudeQuantity;
    }

    public ObjectProperty<PlanType> aoiTypeProperty() {
        return aoiType;
    }

    private String lblsChanged(IApplicationContext applicationContext, PlanType aoiType) {
        if (aoiType == null) {
            lblAltDistanceProperty.setValue(languageHelper.getString(LABEL_ALT_DISTANCE));
        } else if (AreasOfInterestType.AOI_3D.contains(aoiType)) {
            lblAltDistanceProperty.setValue(languageHelper.getString(LABEL_ALT_DISTANCE_3D));
            lblGsdProperty.setValue(languageHelper.getString(LABEL_GSD));
        } else {
            boolean isFollowTerrain = AltitudeAdjustModes.FOLLOW_TERRAIN.equals(currentAltMode.get());
            if (applicationContext.getCurrentMission() == null
                    || applicationContext.getCurrentMission().getCurrentFlightPlan() == null
                    || applicationContext.getCurrentMission().getCurrentFlightPlan().getLegacyFlightplan() == null) {
                lblAltDistanceProperty.setValue(languageHelper.getString(LABEL_ALT_DISTANCE_2D));
            } else {
                lblAltDistanceProperty.setValue(
                    languageHelper.getString(LABEL_ALT_DISTANCE_2D + "." + isFollowTerrain));
                lblGsdProperty.setValue(languageHelper.getString(isFollowTerrain ? LABEL_GSD : LABEL_GSD_AVG));
            }
        }

        return lblAltDistanceProperty.getValue();
    }

    public StringProperty lblAltDistanceProperty() {
        return lblAltDistanceProperty;
    }

    public StringProperty lblGsdProperty() {
        return lblGsdProperty;
    }

    protected boolean calculateGSDInfo() {
        if (aoiType.get() == null || !aoiType.get().supportsCoverageComputation()) {
            gsdOutSideToleranceProperty().set(false);
            return false;
        }

        var mission = applicationContext.getCurrentMission();
        if (mission == null) {
            gsdOutSideToleranceProperty().set(false);
            return false;
        }

        var flightplan = mission.getCurrentFlightPlan();
        if (flightplan == null) {
            gsdOutSideToleranceProperty().set(false);
            return false;
        }

        Flightplan legacyFlightplan = flightplan.getLegacyFlightplan();
        MinMaxPair gsdMismatchRange = flightplan.gsdMismatchRangeProperty().get();

        if (gsdMismatchRange == null || !gsdMismatchRange.isValid()) {
            gsdOutSideToleranceProperty().set(false);
            return false;
        }

        double gsdTolerance = legacyFlightplan.getPhotoSettings().getGsdTolerance();
        double gsdVariation =
            Math.max(
                    Math.max(gsdMismatchRange.min, gsdMismatchRange.max),
                    Math.max(1 / gsdMismatchRange.min, 1 / gsdMismatchRange.max))
                - 1;

        if (gsdVariation > gsdTolerance) {
            gsdLowerEndOfRange.set(gsdMismatchRange.min * gsdProperty().get());
            gsdUpperEndOfRange.set(gsdMismatchRange.max * gsdProperty().get());
            gsdOutSideToleranceProperty().set(true);
        } else {
            gsdOutSideToleranceProperty().set(false);
        }

        return true;
    }

}
