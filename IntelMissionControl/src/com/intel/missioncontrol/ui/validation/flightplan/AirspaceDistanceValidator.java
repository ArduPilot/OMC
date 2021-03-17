/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.validation.flightplan;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.intel.missioncontrol.beans.property.IQuantityStyleProvider;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.map.elevation.IElevationModel;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.QuantityFormat;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.measure.UnitInfo;
import com.intel.missioncontrol.mission.FlightPlan;
import com.intel.missioncontrol.settings.AirspacesProvidersSettings;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.OperationLevel;
import com.intel.missioncontrol.ui.controls.AdaptiveQuantityFormat;
import com.intel.missioncontrol.ui.sidepane.planning.StartPlanningViewModel;
import com.intel.missioncontrol.ui.validation.IResolveAction;
import com.intel.missioncontrol.ui.validation.SimpleResolveAction;
import com.intel.missioncontrol.ui.validation.ValidationMessageCategory;
import eu.mavinci.airspace.GolfUpperBoundAirspace;
import eu.mavinci.flightplan.computation.FPsim;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;

/** check A-09: min distance to any restricted airspace is smaller than 0 (Airmap) */
@SuppressWarnings("FieldCanBeLocal")
public class AirspaceDistanceValidator extends OnFlightplanRecomputedValidator {

    public interface Factory {
        AirspaceDistanceValidator create(FlightPlan flightPlan);
    }

    private final String className = AirspaceDistanceValidator.class.getName();
    private final ChangeListener<Number> maxAltASLChangeListener;
    private final ChangeListener<Boolean> maxAltASLChangeListenerBool;
    private final ILanguageHelper languageHelper;
    private final IElevationModel elevationModel;
    private final GeneralSettings generalSettings;
    private final AirspacesProvidersSettings settings;
    private final QuantityFormat quantityFormat;

    @Inject
    public AirspaceDistanceValidator(
            ILanguageHelper languageHelper,
            IElevationModel elevationModel,
            GeneralSettings generalSettings,
            AirspacesProvidersSettings settings,
            @Assisted FlightPlan flightplan) {
        super(flightplan, generalSettings);
        this.languageHelper = languageHelper;
        this.elevationModel = elevationModel;
        this.generalSettings = generalSettings;
        this.settings = settings;
        this.quantityFormat = new AdaptiveQuantityFormat(generalSettings);
        maxAltASLChangeListener = (a, b, c) -> invalidate();
        maxAltASLChangeListenerBool = (a, b, c) -> invalidate();
        settings.maxAltitudeAboveSeaLevelProperty().addListener(new WeakChangeListener<>(maxAltASLChangeListener));
        settings.maxAltitudeAboveGroundProperty().addListener(new WeakChangeListener<>(maxAltASLChangeListener));
        settings.useAirspaceDataForPlanningProperty()
            .addListener(new WeakChangeListener<>(maxAltASLChangeListenerBool));
    }

    @Override
    protected boolean onInvalidated(FlightPlan flightplan) {
        FPsim.SimResultData simResult = flightplan.getLegacyFlightplan().getFPsim().getSimResult();
        if (simResult == null) {
            return false;
        }

        IResolveAction resolveAirmapLaanc =
            new SimpleResolveAction(
                languageHelper.getString(className + ".approveAirmapLAANC"),
                () -> StartPlanningViewModel.airmapLaancApprove(flightplan, elevationModel));

        IResolveAction[] allResolveActions =
            generalSettings.getOperationLevel() == OperationLevel.DEBUG
                ? new IResolveAction[] {resolveAirmapLaanc}
                : new IResolveAction[] {};

        if (settings.isUseAirspaceDataForPlanning()
                && simResult.lowestAirspace != null
                && simResult.minDistanceToFloor <= 0
                && !(simResult.lowestAirspace instanceof GolfUpperBoundAirspace)) {
            addWarning(
                languageHelper.getString(
                    className + ".tooCloseToAirspace",
                    quantityFormat.format(
                        Quantity.of(-simResult.minDistanceToFloor, Unit.METER), UnitInfo.LOCALIZED_LENGTH),
                    simResult.lowestAirspace.getName().replaceAll("\\r\\n|\\r|\\n", " ").replaceAll("\\)\\(", ") (")),
                ValidationMessageCategory.NORMAL,
                allResolveActions);
        }

        if (simResult.minMaxDistanceToMSL.max > settings.maxAltitudeAboveSeaLevelProperty().doubleValue()) {
            addWarning(
                languageHelper.getString(
                    className + ".tooHeightOverMSL",
                    quantityFormat.format(
                        Quantity.of(simResult.minMaxDistanceToMSL.max, Unit.METER), UnitInfo.LOCALIZED_LENGTH),
                    quantityFormat.format(
                        Quantity.of(settings.maxAltitudeAboveSeaLevelProperty().doubleValue(), Unit.METER),
                        UnitInfo.LOCALIZED_LENGTH)),
                ValidationMessageCategory.NORMAL,
                allResolveActions);
        }

        if (simResult.minMaxDistanceToGround.max > settings.maxAltitudeAboveGroundProperty().doubleValue()) {
            addWarning(
                languageHelper.getString(
                    className + ".tooHeightOverGround",
                    quantityFormat.format(
                        Quantity.of(simResult.minMaxDistanceToGround.max, Unit.METER), UnitInfo.LOCALIZED_LENGTH),
                    quantityFormat.format(
                        Quantity.of(settings.maxAltitudeAboveGroundProperty().doubleValue(), Unit.METER),
                        UnitInfo.LOCALIZED_LENGTH)),
                ValidationMessageCategory.NORMAL,
                allResolveActions);
        }

        return true;
    }

}
