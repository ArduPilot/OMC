/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.validation.flightplan;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.intel.missioncontrol.hardware.IPlatformDescription;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.measure.property.IQuantityStyleProvider;
import com.intel.missioncontrol.mission.FlightPlan;
import com.intel.missioncontrol.ui.validation.IResolveAction;
import com.intel.missioncontrol.ui.validation.SimpleResolveAction;
import com.intel.missioncontrol.ui.validation.ValidationMessageCategory;
import eu.mavinci.core.flightplan.AltitudeAdjustModes;
import eu.mavinci.core.helper.MinMaxPair;
import eu.mavinci.flightplan.Flightplan;

/** check A-03: GSD variation not within tolerance */
public class GsdVariationToleranceValidator extends OnFlightplanRecomputedValidator {

    public interface Factory {
        GsdVariationToleranceValidator create(FlightPlan flightPlan);
    }

    private final String className = GsdVariationToleranceValidator.class.getName();
    private ILanguageHelper languageHelper;

    @Inject
    public GsdVariationToleranceValidator(
            ILanguageHelper languageHelper,
            IQuantityStyleProvider quantityStyleProvider,
            @Assisted FlightPlan flightplan) {
        super(flightplan, quantityStyleProvider);
        this.languageHelper = languageHelper;
        setOkMessage(languageHelper.getString(className + ".okMessage"));
    }

    @Override
    protected boolean onInvalidated(final FlightPlan flightplan) {
        if (flightplan.isSimulatedTimeValidProperty().get()) {
            Flightplan legacyFlightplan = flightplan.getLegacyFlightplan();
            MinMaxPair gsdMissmatchRange = legacyFlightplan.getFPcoverage().getGsdMissmatchRange();
            if (gsdMissmatchRange == null || !gsdMissmatchRange.isValid()) {
                return false;
            }

            double gsdTolerance = legacyFlightplan.getPhotoSettings().getGsdTolerance();
            double gsdVariation =
                Math.max(
                        Math.max(gsdMissmatchRange.min, gsdMissmatchRange.max),
                        Math.max(1 / gsdMissmatchRange.min, 1 / gsdMissmatchRange.max))
                    - 1;

            if (gsdVariation > gsdTolerance) {
                IPlatformDescription platformDescription =
                    legacyFlightplan.getHardwareConfiguration().getPlatformDescription();
                final AltitudeAdjustModes altAdjustMode =
                    (platformDescription.isInCopterMode() || platformDescription.getAPtype().canLinearClimbOnLine())
                        ? AltitudeAdjustModes.FOLLOW_TERRAIN
                        : AltitudeAdjustModes.STEPS_ON_LINE;
                IResolveAction[] actions;

                IResolveAction accept =
                    new SimpleResolveAction(
                        languageHelper.getString(className + ".accept"),
                        () -> legacyFlightplan.getPhotoSettings().setGsdTolerance(gsdVariation + 0.005));

                if (legacyFlightplan.getPhotoSettings().getAltitudeAdjustMode() != altAdjustMode) {
                    IResolveAction adjustMode =
                        new SimpleResolveAction(
                            languageHelper.getString(className + ".changeTerrainMode"),
                            () -> legacyFlightplan.getPhotoSettings().setAltitudeAdjustMode(altAdjustMode));
                    actions = new IResolveAction[] {adjustMode, accept};
                } else {
                    actions = new IResolveAction[] {accept};
                }

                addWarning(
                    languageHelper.getString(
                        className + ".wrongGsd", Math.round(gsdVariation * 100), Math.round(gsdTolerance * 100)),
                    ValidationMessageCategory.NOTICE,
                    actions);
            }
        }

        return true;
    }

}
