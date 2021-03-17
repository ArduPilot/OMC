/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.validation.flightplan;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.map.elevation.IElevationModel;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.QuantityFormat;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.measure.UnitInfo;
import com.intel.missioncontrol.mission.FlightPlan;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.ui.controls.AdaptiveQuantityFormat;
import com.intel.missioncontrol.ui.validation.IResolveAction;
import com.intel.missioncontrol.ui.validation.SimpleResolveAction;
import com.intel.missioncontrol.ui.validation.ValidationMessageCategory;
import gov.nasa.worldwind.geom.Sector;

/** check A-**: check if mission safety altitude is reasonable compare to terrain */
@SuppressWarnings("FieldCanBeLocal")
public class SafetyAltValidator extends OnFlightplanChangedValidator {

    public interface Factory {
        SafetyAltValidator create(FlightPlan flightPlan);
    }

    private final String className = SafetyAltValidator.class.getName();
    private final ILanguageHelper languageHelper;
    private final IElevationModel elevationModel;
    private final QuantityFormat quantityFormat;

    @Inject
    public SafetyAltValidator(
            ILanguageHelper languageHelper,
            IElevationModel elevationModel,
            GeneralSettings generalSettings,
            @Assisted FlightPlan flightplan) {
        super(flightplan, generalSettings);
        this.languageHelper = languageHelper;
        this.elevationModel = elevationModel;
        this.quantityFormat = new AdaptiveQuantityFormat(generalSettings);
    }

    @Override
    protected boolean onInvalidated(FlightPlan flightplan) {
        var hardwareConfiguration = flightplan.getLegacyFlightplan().getHardwareConfiguration();
        if (!hardwareConfiguration.getPlatformDescription().areEmergencyActionsSettable()) {
            return true;
        }

        Sector picAreaSector = flightplan.getLegacyFlightplan().getPicAreaSector();
        if (picAreaSector == null) {
            return true;
        }

        double minActualSafeAltitude =
            elevationModel.getMaxElevation(picAreaSector).max
                + hardwareConfiguration
                    .getPlatformDescription()
                    .getMinGroundDistance()
                    .convertTo(Unit.METER)
                    .getValue()
                    .doubleValue()
                - elevationModel.getMaxElevation(picAreaSector).min;
        double currentSafetyAltitude = flightplan.safetyAltitudeProperty().get();

        if (currentSafetyAltitude < minActualSafeAltitude) {
            IResolveAction increaseAltitude =
                new SimpleResolveAction(
                    languageHelper.getString(className + ".increaseAltitude"),
                    () ->
                        flightplan
                            .safetyAltitudeProperty()
                            .setValue(minActualSafeAltitude + IElevationModel.TINY_GROUND_ELEVATION));
            IResolveAction setToAuto =
                new SimpleResolveAction(
                    languageHelper.getString(className + ".setToAuto"),
                    () -> {
                        flightplan.autoComputeSafetyHeightProperty().set(true);
                        if (!flightplan.getLegacyFlightplan().willAutoRecalc()) {
                            flightplan.doFlightplanCalculation();
                        }
                    });

            IResolveAction[] allResolveActions = new IResolveAction[] {increaseAltitude, setToAuto};

            addWarning(
                languageHelper.getString(
                    className + ".safetyAltTooLow",
                    quantityFormat.format(Quantity.of(minActualSafeAltitude, Unit.METER), UnitInfo.LOCALIZED_LENGTH),
                    quantityFormat.format(Quantity.of(currentSafetyAltitude, Unit.METER), UnitInfo.LOCALIZED_LENGTH)),
                ValidationMessageCategory.NORMAL,
                allResolveActions);
        }

        return true;
    }

}
