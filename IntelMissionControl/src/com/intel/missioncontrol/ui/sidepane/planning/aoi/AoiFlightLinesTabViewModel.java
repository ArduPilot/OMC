/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.planning.aoi;

import com.google.inject.Inject;
import com.intel.missioncontrol.geometry.AreaOfInterest;
import com.intel.missioncontrol.measure.Dimension.Angle;
import com.intel.missioncontrol.measure.Dimension.Length;
import com.intel.missioncontrol.measure.Dimension.Percentage;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.beans.binding.QuantityBindings;
import com.intel.missioncontrol.beans.property.QuantityProperty;
import com.intel.missioncontrol.beans.property.SimpleQuantityProperty;
import com.intel.missioncontrol.measure.UnitInfo;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.ui.ViewModelBase;
import com.intel.missioncontrol.ui.scope.planning.PlanningScope;
import de.saxsys.mvvmfx.InjectScope;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleDoubleProperty;

public class AoiFlightLinesTabViewModel extends ViewModelBase<AreaOfInterest> {

    @InjectScope
    private PlanningScope planningScope;

    private final QuantityProperty<Angle> cameraTiltDegreesQuantity;
    private final Property<Number> cameraTiltDegrees = new SimpleDoubleProperty(0.0);

    private final QuantityProperty<Angle> cameraPitchOffsetDegreesQuantity;
    private final Property<Number> cameraPitchOffsetDegrees = new SimpleDoubleProperty(0.0);

    private final QuantityProperty<Angle> cameraRollDegreesQuantity;
    private final Property<Number> cameraRollDegrees = new SimpleDoubleProperty(0.0);

    private final QuantityProperty<Angle> cameraRollOffsetDegreesQuantity;
    private final Property<Number> cameraRollOffsetDegrees = new SimpleDoubleProperty(0.0);

    private final QuantityProperty<Angle> maxYawRollChangeQuantity;
    private final Property<Number> maxYawRollChange = new SimpleDoubleProperty(0.0);

    private final QuantityProperty<Angle> maxPitchChangeQuantity;
    private final Property<Number> maxPitchChange = new SimpleDoubleProperty(0.0);

    private final QuantityProperty<Percentage> overlapInFlightQuantity;
    private final Property<Number> overlapInFlight = new SimpleDoubleProperty(0.0);

    private final QuantityProperty<Percentage> overlapInFlightMinQuantity;
    private final Property<Number> overlapInFlightMin = new SimpleDoubleProperty(0.0);

    private final QuantityProperty<Percentage> overlapParallelQuantity;
    private final Property<Number> overlapParallel = new SimpleDoubleProperty(0.0);

    private final QuantityProperty<Length> minObjectDistanceQuantity;
    private final DoubleProperty minObjectDistance = new SimpleDoubleProperty(0.0);

    private final QuantityProperty<Length> maxObjectDistanceQuantity;
    private final DoubleProperty maxObjectDistance = new SimpleDoubleProperty(0.0);

    private final QuantityProperty<Length> minGroundDistanceQuantity;
    private final DoubleProperty minGroundDistance = new SimpleDoubleProperty(0.0);

    private final QuantityProperty<Angle> cameraPitchOffsetLineBeginDegreesQuantity;
    private final DoubleProperty cameraPitchOffsetLineBeginDegrees = new SimpleDoubleProperty(0.0);

    private AreaOfInterest areaOfInterest;

    @Inject
    public AoiFlightLinesTabViewModel(ISettingsManager settingsManager) {
        final GeneralSettings generalSettings = settingsManager.getSection(GeneralSettings.class);

        cameraTiltDegreesQuantity =
            new SimpleQuantityProperty<>(generalSettings, UnitInfo.ANGLE_DEGREES, Quantity.of(0.0, Unit.DEGREE));
        QuantityBindings.bindBidirectional(cameraTiltDegreesQuantity, cameraTiltDegrees, Unit.DEGREE);

        cameraPitchOffsetDegreesQuantity =
            new SimpleQuantityProperty<>(generalSettings, UnitInfo.ANGLE_DEGREES, Quantity.of(0.0, Unit.DEGREE));
        QuantityBindings.bindBidirectional(cameraPitchOffsetDegreesQuantity, cameraPitchOffsetDegrees, Unit.DEGREE);

        cameraRollDegreesQuantity =
            new SimpleQuantityProperty<>(generalSettings, UnitInfo.ANGLE_DEGREES, Quantity.of(0.0, Unit.DEGREE));
        QuantityBindings.bindBidirectional(cameraRollDegreesQuantity, cameraRollDegrees, Unit.DEGREE);

        cameraRollOffsetDegreesQuantity =
            new SimpleQuantityProperty<>(generalSettings, UnitInfo.ANGLE_DEGREES, Quantity.of(0.0, Unit.DEGREE));
        QuantityBindings.bindBidirectional(cameraRollOffsetDegreesQuantity, cameraRollOffsetDegrees, Unit.DEGREE);

        maxPitchChangeQuantity =
            new SimpleQuantityProperty<>(generalSettings, UnitInfo.ANGLE_DEGREES, Quantity.of(0.0, Unit.DEGREE));
        QuantityBindings.bindBidirectional(maxPitchChangeQuantity, maxPitchChange, Unit.DEGREE);

        maxYawRollChangeQuantity =
            new SimpleQuantityProperty<>(generalSettings, UnitInfo.ANGLE_DEGREES, Quantity.of(0.0, Unit.DEGREE));
        QuantityBindings.bindBidirectional(maxYawRollChangeQuantity, maxYawRollChange, Unit.DEGREE);

        cameraPitchOffsetLineBeginDegreesQuantity =
        new SimpleQuantityProperty<>(generalSettings, UnitInfo.ANGLE_DEGREES, Quantity.of(0.0, Unit.DEGREE));
        QuantityBindings.bindBidirectional(cameraPitchOffsetLineBeginDegreesQuantity, cameraPitchOffsetLineBeginDegrees, Unit.DEGREE);

        overlapInFlightQuantity =
            new SimpleQuantityProperty<>(generalSettings, UnitInfo.PERCENTAGE, Quantity.of(0.0, Unit.PERCENTAGE));
        QuantityBindings.bindBidirectional(overlapInFlightQuantity, overlapInFlight, Unit.PERCENTAGE);

        overlapInFlightMinQuantity =
            new SimpleQuantityProperty<>(generalSettings, UnitInfo.PERCENTAGE, Quantity.of(0.0, Unit.PERCENTAGE));
        QuantityBindings.bindBidirectional(overlapInFlightMinQuantity, overlapInFlightMin, Unit.PERCENTAGE);

        overlapParallelQuantity =
            new SimpleQuantityProperty<>(generalSettings, UnitInfo.PERCENTAGE, Quantity.of(0.0, Unit.PERCENTAGE));
        QuantityBindings.bindBidirectional(overlapParallelQuantity, overlapParallel, Unit.PERCENTAGE);

        minObjectDistanceQuantity =
            new SimpleQuantityProperty<>(generalSettings, UnitInfo.LOCALIZED_LENGTH, Quantity.of(0.0, Unit.METER));
        QuantityBindings.bindBidirectional(minObjectDistanceQuantity, minObjectDistance, Unit.METER);

        maxObjectDistanceQuantity =
            new SimpleQuantityProperty<>(generalSettings, UnitInfo.LOCALIZED_LENGTH, Quantity.of(0.0, Unit.METER));
        QuantityBindings.bindBidirectional(maxObjectDistanceQuantity, maxObjectDistance, Unit.METER);

        minGroundDistanceQuantity =
            new SimpleQuantityProperty<>(generalSettings, UnitInfo.LOCALIZED_LENGTH, Quantity.of(0.0, Unit.METER));
        QuantityBindings.bindBidirectional(minGroundDistanceQuantity, minGroundDistance, Unit.METER);
    }

    @Override
    public void initializeViewModel(AreaOfInterest areaOfInterest) {
        super.initializeViewModel(areaOfInterest);

        this.areaOfInterest = areaOfInterest;

        cameraTiltDegrees.bindBidirectional(areaOfInterest.cameraTiltDegreesProperty());
        cameraPitchOffsetDegrees.bindBidirectional(areaOfInterest.cameraPitchOffsetDegreesProperty());

        cameraRollDegrees.bindBidirectional(areaOfInterest.cameraRollDegreesProperty());
        cameraRollOffsetDegrees.bindBidirectional(areaOfInterest.cameraRollOffsetDegreesProperty());

        overlapInFlight.bindBidirectional(areaOfInterest.overlapInFlightProperty());
        overlapInFlightMin.bindBidirectional(areaOfInterest.overlapInFlightMinProperty());
        overlapParallel.bindBidirectional(areaOfInterest.overlapParallelProperty());

        minObjectDistance.bindBidirectional(areaOfInterest.minObjectDistanceProperty());
        maxObjectDistance.bindBidirectional(areaOfInterest.maxObjectDistanceProperty());
        minGroundDistance.bindBidirectional(areaOfInterest.minGroundDistanceProperty());

        maxYawRollChange.bindBidirectional(areaOfInterest.maxYawRollChangeProperty());
        maxPitchChange.bindBidirectional(areaOfInterest.maxPitchChangeProperty());
        cameraPitchOffsetLineBeginDegrees.bindBidirectional(areaOfInterest.cameraPitchOffsetLineBeginDegreesProperty());
    }

    public QuantityProperty<Angle> cameraTiltDegreesQuantityProperty() {
        return cameraTiltDegreesQuantity;
    }

    public QuantityProperty<Angle> cameraPitchOffsetDegreesQuantityProperty() {
        return cameraPitchOffsetDegreesQuantity;
    }

    public QuantityProperty<Angle> cameraRollDegreesQuantityProperty() {
        return cameraRollDegreesQuantity;
    }

    public QuantityProperty<Angle> cameraRollOffsetDegreesQuantityProperty() {
        return cameraRollOffsetDegreesQuantity;
    }

    public QuantityProperty<Percentage> overlapInFlightQuantityProperty() {
        return overlapInFlightQuantity;
    }

    public QuantityProperty<Percentage> overlapInFlightMinQuantityProperty() {
        return overlapInFlightMinQuantity;
    }

    public QuantityProperty<Percentage> overlapParallelQuantityProperty() {
        return overlapParallelQuantity;
    }

    public QuantityProperty<Length> minObjectDistanceQuantity() {
        return minObjectDistanceQuantity;
    }

    public QuantityProperty<Length> maxObjectDistanceQuantityProperty() {
        return maxObjectDistanceQuantity;
    }

    public QuantityProperty<Length> minGroundDistanceQuantityProperty() {
        return minGroundDistanceQuantity;
    }

    public QuantityProperty<Angle> maxPitchChangeQuantityProperty() {
        return maxPitchChangeQuantity;
    }

    public QuantityProperty<Angle> maxYawRollChangeQuantityProperty() {
        return maxYawRollChangeQuantity;
    }

    public QuantityProperty<Angle> cameraPitchOffsetLineBeginDegreesQuantityProperty() {
        return cameraPitchOffsetLineBeginDegreesQuantity;
    }

    public boolean isCopter() {
        return planningScope.getSelectedHardwareConfiguration().getPlatformDescription().isInCopterMode();
    }

    public AreaOfInterest getAreaOfInterest() {
        return areaOfInterest;
    }

}
