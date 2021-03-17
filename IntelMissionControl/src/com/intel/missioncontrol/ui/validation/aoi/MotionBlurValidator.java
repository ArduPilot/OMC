/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.validation.aoi;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.intel.missioncontrol.beans.property.IQuantityStyleProvider;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.ui.validation.IResolveAction;
import com.intel.missioncontrol.ui.validation.SimpleResolveAction;
import com.intel.missioncontrol.ui.validation.ValidationMessageCategory;
import eu.mavinci.desktop.gui.doublepanel.calculator.AltitudeGsdCalculator;
import eu.mavinci.flightplan.Flightplan;
import eu.mavinci.flightplan.PicArea;
import java.util.LinkedList;

/** check A-05: drone speed vs. GSD -> blur/shutter speed */
public class MotionBlurValidator extends OnPicAreaChangedValidator {

    public interface Factory {
        MotionBlurValidator create(PicArea picArea);
    }

    private final String className = MotionBlurValidator.class.getName();
    private final ILanguageHelper languageHelper;

    @Inject
    public MotionBlurValidator(
            IQuantityStyleProvider quantityStyleProvider, ILanguageHelper languageHelper, @Assisted PicArea picArea) {
        super(picArea, quantityStyleProvider);
        this.languageHelper = languageHelper;
        setOkMessage(languageHelper.getString(className + ".okMessage"));
    }

    @Override
    protected boolean onInvalidated(PicArea picArea) {
        Flightplan flightplan = picArea.getFlightplan();
        if (flightplan != null
                && flightplan.getPhotoSettings().getMaxGroundSpeedAutomatic().isAutomaticallyAdjusting()) {
            return true;
        }

        if (flightplan != null && flightplan.getPhotoSettings().isStoppingAtWaypoints()) {
            return true;
        }

        if (!picArea.wasRecalconce()) {
            return false;
        }

        if (picArea.getMotionBlurrEst() >= picArea.getGsd() * 1.1) {
            LinkedList<IResolveAction> actions = new LinkedList<>();
            actions.add(
                new SimpleResolveAction(
                    languageHelper.getString(className + ".increaseGsd"),
                    () -> picArea.setGsd(picArea.getMotionBlurrEst())));

            if (flightplan != null && flightplan.getHardwareConfiguration().getPlatformDescription().isInCopterMode()) {
                actions.add(
                    new SimpleResolveAction(
                        languageHelper.getString(className + ".reduceSpeed"),
                        () -> {
                            // compute max possible plane speed
                            double maxSpeed = flightplan.getPhotoSettings().getMaxGroundSpeedMPSec();
                            AltitudeGsdCalculator calc = picArea.getAltitudeGsdCalculator();
                            maxSpeed = Math.min(maxSpeed, calc.computeMaxGroundSpeedMpS());
                            flightplan.getPhotoSettings().setMaxGroundSpeedMPSec(maxSpeed);
                        }));

                // we could ALSO suggest to switch to automatic speed modes... BUT we can only add up to two resolve
                // actions.. that would be a third one..
                // flightplan.getPhotoSettings().setMaxGroundSpeedAutomatic(FlightplanSpeedModes.AUTOMATIC_CONSTANT);
            }

            addWarning(
                languageHelper.getString(
                    className + ".motionBlur",
                    formatLength(picArea.getGsd()),
                    formatLength(picArea.getMotionBlurrEst())),
                ValidationMessageCategory.NOTICE,
                actions.toArray(new IResolveAction[0]));
        }

        return true;
    }

}
