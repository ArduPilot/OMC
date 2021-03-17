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
import com.intel.missioncontrol.measure.UnitInfo;
import com.intel.missioncontrol.measure.property.QuantityBindings;
import com.intel.missioncontrol.measure.property.QuantityProperty;
import com.intel.missioncontrol.measure.property.SimpleQuantityProperty;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.ui.ViewModelBase;
import com.intel.missioncontrol.ui.scope.planning.PlanningScope;
import de.saxsys.mvvmfx.InjectScope;

public class AoiFlightLinesTabViewModel extends ViewModelBase<AreaOfInterest> {

    @InjectScope
    private PlanningScope planningScope;

    private final QuantityProperty<Angle> cameraTilt;
    private final QuantityProperty<Angle> cameraPitchOffset;
    private final QuantityProperty<Angle> cameraRoll;
    private final QuantityProperty<Angle> cameraRollOffset;
    private final QuantityProperty<Angle> maxYawRollChange;
    private final QuantityProperty<Angle> maxPitchChange;
    private final QuantityProperty<Percentage> overlapInFlight;
    private final QuantityProperty<Percentage> overlapInFlightMin;
    private final QuantityProperty<Percentage> overlapParallel;
    private final QuantityProperty<Length> minObjectDistance;
    private final QuantityProperty<Length> maxObjectDistance;
    private final QuantityProperty<Length> minGroundDistance;
    private final QuantityProperty<Angle> cameraPitchOffsetLineBegin;

    private AreaOfInterest areaOfInterest;

    @Inject
    public AoiFlightLinesTabViewModel(ISettingsManager settingsManager) {
        final GeneralSettings generalSettings = settingsManager.getSection(GeneralSettings.class);

        cameraTilt =
            new SimpleQuantityProperty<>(generalSettings, UnitInfo.ANGLE_DEGREES, Quantity.of(0.0, Unit.DEGREE));

        cameraPitchOffset =
            new SimpleQuantityProperty<>(generalSettings, UnitInfo.ANGLE_DEGREES, Quantity.of(0.0, Unit.DEGREE));

        cameraRoll =
            new SimpleQuantityProperty<>(generalSettings, UnitInfo.ANGLE_DEGREES, Quantity.of(0.0, Unit.DEGREE));

        cameraRollOffset =
            new SimpleQuantityProperty<>(generalSettings, UnitInfo.ANGLE_DEGREES, Quantity.of(0.0, Unit.DEGREE));

        maxPitchChange =
            new SimpleQuantityProperty<>(generalSettings, UnitInfo.ANGLE_DEGREES, Quantity.of(0.0, Unit.DEGREE));

        maxYawRollChange =
            new SimpleQuantityProperty<>(generalSettings, UnitInfo.ANGLE_DEGREES, Quantity.of(0.0, Unit.DEGREE));

        cameraPitchOffsetLineBegin =
            new SimpleQuantityProperty<>(generalSettings, UnitInfo.ANGLE_DEGREES, Quantity.of(0.0, Unit.DEGREE));

        overlapInFlight =
            new SimpleQuantityProperty<>(generalSettings, UnitInfo.PERCENTAGE, Quantity.of(0.0, Unit.PERCENTAGE));

        overlapInFlightMin =
            new SimpleQuantityProperty<>(generalSettings, UnitInfo.PERCENTAGE, Quantity.of(0.0, Unit.PERCENTAGE));

        overlapParallel =
            new SimpleQuantityProperty<>(generalSettings, UnitInfo.PERCENTAGE, Quantity.of(0.0, Unit.PERCENTAGE));

        minObjectDistance =
            new SimpleQuantityProperty<>(generalSettings, UnitInfo.LOCALIZED_LENGTH, Quantity.of(0.0, Unit.METER));

        maxObjectDistance =
            new SimpleQuantityProperty<>(generalSettings, UnitInfo.LOCALIZED_LENGTH, Quantity.of(0.0, Unit.METER));

        minGroundDistance =
            new SimpleQuantityProperty<>(generalSettings, UnitInfo.LOCALIZED_LENGTH, Quantity.of(0.0, Unit.METER));
    }

    @Override
    public void initializeViewModel(AreaOfInterest areaOfInterest) {
        super.initializeViewModel(areaOfInterest);

        this.areaOfInterest = areaOfInterest;

        QuantityBindings.bindBidirectional(cameraTilt, areaOfInterest.cameraTiltDegreesProperty(), Unit.DEGREE);
        QuantityBindings.bindBidirectional(
            cameraPitchOffset, areaOfInterest.cameraPitchOffsetDegreesProperty(), Unit.DEGREE);

        QuantityBindings.bindBidirectional(cameraRoll, areaOfInterest.cameraRollDegreesProperty(), Unit.DEGREE);
        QuantityBindings.bindBidirectional(
            cameraRollOffset, areaOfInterest.cameraRollOffsetDegreesProperty(), Unit.DEGREE);

        QuantityBindings.bindBidirectional(overlapInFlight, areaOfInterest.overlapInFlightProperty(), Unit.PERCENTAGE);
        QuantityBindings.bindBidirectional(
            overlapInFlightMin, areaOfInterest.overlapInFlightMinProperty(), Unit.PERCENTAGE);
        QuantityBindings.bindBidirectional(overlapParallel, areaOfInterest.overlapParallelProperty(), Unit.PERCENTAGE);

        QuantityBindings.bindBidirectional(minObjectDistance, areaOfInterest.minObjectDistanceProperty(), Unit.METER);
        QuantityBindings.bindBidirectional(maxObjectDistance, areaOfInterest.maxObjectDistanceProperty(), Unit.METER);
        QuantityBindings.bindBidirectional(minGroundDistance, areaOfInterest.minGroundDistanceProperty(), Unit.METER);

        QuantityBindings.bindBidirectional(maxYawRollChange, areaOfInterest.maxYawRollChangeProperty(), Unit.DEGREE);
        QuantityBindings.bindBidirectional(maxPitchChange, areaOfInterest.maxPitchChangeProperty(), Unit.DEGREE);
        QuantityBindings.bindBidirectional(
            cameraPitchOffsetLineBegin, areaOfInterest.cameraPitchOffsetLineBeginDegreesProperty(), Unit.DEGREE);
    }

    public QuantityProperty<Angle> cameraTiltProperty() {
        return cameraTilt;
    }

    public QuantityProperty<Angle> cameraPitchOffsetProperty() {
        return cameraPitchOffset;
    }

    public QuantityProperty<Angle> cameraRollProperty() {
        return cameraRoll;
    }

    public QuantityProperty<Angle> cameraRollOffsetProperty() {
        return cameraRollOffset;
    }

    public QuantityProperty<Percentage> overlapInFlightProperty() {
        return overlapInFlight;
    }

    public QuantityProperty<Percentage> overlapInFlightMinProperty() {
        return overlapInFlightMin;
    }

    public QuantityProperty<Percentage> overlapParallelProperty() {
        return overlapParallel;
    }

    public QuantityProperty<Length> minObjectDistanceProperty() {
        return minObjectDistance;
    }

    public QuantityProperty<Length> maxObjectDistanceProperty() {
        return maxObjectDistance;
    }

    public QuantityProperty<Length> minGroundDistanceProperty() {
        return minGroundDistance;
    }

    public QuantityProperty<Angle> maxPitchChangeProperty() {
        return maxPitchChange;
    }

    public QuantityProperty<Angle> maxYawRollChangeProperty() {
        return maxYawRollChange;
    }

    public QuantityProperty<Angle> cameraPitchOffsetLineBeginProperty() {
        return cameraPitchOffsetLineBegin;
    }

    public boolean isCopter() {
        return planningScope.getSelectedHardwareConfiguration().getPlatformDescription().isInCopterMode();
    }

    public AreaOfInterest getAreaOfInterest() {
        return areaOfInterest;
    }

}
