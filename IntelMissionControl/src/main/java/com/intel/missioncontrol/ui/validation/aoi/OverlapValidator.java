/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.validation.aoi;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.intel.missioncontrol.hardware.IPlatformDescription;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.measure.property.IQuantityStyleProvider;
import com.intel.missioncontrol.ui.validation.IResolveAction;
import com.intel.missioncontrol.ui.validation.SimpleResolveAction;
import com.intel.missioncontrol.ui.validation.ValidationMessageCategory;
import eu.mavinci.flightplan.Flightplan;
import eu.mavinci.flightplan.PicArea;
import java.util.LinkedList;

/** check A-01: requested overlap is impossible to achieve with this drone */
public class OverlapValidator extends OnPicAreaChangedValidator {

    public interface Factory {
        OverlapValidator create(PicArea picArea);
    }

    private final String className = OverlapValidator.class.getName();
    private ILanguageHelper languageHelper;

    @Inject
    public OverlapValidator(
            ILanguageHelper languageHelper, IQuantityStyleProvider quantityStyleProvider, @Assisted PicArea picArea) {
        super(picArea, quantityStyleProvider);
        this.languageHelper = languageHelper;
        setOkMessage(languageHelper.getString(className + ".okMessage"));
    }

    @Override
    protected boolean onInvalidated(PicArea picArea) {
        Flightplan flightplan = picArea.getFlightplan();
        if (flightplan == null) {
            return true;
        }

        var simResult = flightplan.getFPsim().getSimResult();

        if (simResult == null || simResult.simulatedTimeValid) {
            if (flightplan.getPhotoSettings().getMaxGroundSpeedAutomatic().isAutomaticallyAdjusting()) {
                return true;
            }

            if (picArea.isCheckOverlapPossibleWarning()) {
                LinkedList<IResolveAction> actions = new LinkedList<>();
                actions.add(
                    new SimpleResolveAction(
                        languageHelper.getString(className + ".reduceOverlap"), picArea::reduceOverlap));

                IPlatformDescription platformDesc = flightplan.getHardwareConfiguration().getPlatformDescription();

                if (platformDesc.isInCopterMode()) {
                    actions.add(
                        new SimpleResolveAction(
                            languageHelper.getString(className + ".reduceSpeed"),
                            () -> {
                                // compute max possible plane speed
                                double maxPossiblePlaneSpeedMpS =
                                    picArea.getSizeInFlightEff() / flightplan.getPhotoSettings().getMinTimeInterval();
                                flightplan
                                    .getPhotoSettings()
                                    .setMaxGroundSpeedMPSec(
                                        Math.min(
                                            flightplan.getPhotoSettings().getMaxGroundSpeedMPSec(),
                                            maxPossiblePlaneSpeedMpS));
                            }));
                }
                // we could ALSO suggest to switch to automatic speed modes... BUT we can only add up to two resolve
                // actions.. that would be a third one..
                // picArea.getFlightplan().getPhotoSettings().setMaxGroundSpeedAutomatic(FlightplanSpeedModes.AUTOMATIC_CONSTANT);

                addWarning(
                    languageHelper.getString(
                        className + ".cameraTooSlow",
                        Math.round(picArea.getOverlapInFlight()),
                        Math.round(picArea.getOverlapInFlightMaxPossible())),
                    ValidationMessageCategory.NOTICE,
                    actions.toArray(new IResolveAction[actions.size()]));
            }
        }

        return true;
    }

}
