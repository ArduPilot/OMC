/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.validation.airplane;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.intel.missioncontrol.beans.property.IQuantityStyleProvider;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.ui.validation.ValidationMessageCategory;
import eu.mavinci.core.helper.StringHelper;
import eu.mavinci.core.plane.AirplaneCacheEmptyException;
import eu.mavinci.plane.IAirplane;

/** check B-12 Battery is almost empty / going low. */
public class BatteryValidator extends AirplaneValidatorBase {

    public interface Factory {
        BatteryValidator create(FlightplanAirplanePair flightplanAirplanePair);
    }

    private final String className = BatteryValidator.class.getName();

    @Inject
    public BatteryValidator(
            ILanguageHelper languageHelper,
            IQuantityStyleProvider quantityStyleProvider,
            @Assisted FlightplanAirplanePair flightplanAirplanePair) {
        super(languageHelper, quantityStyleProvider, flightplanAirplanePair);
        addPlaneInfoListener();
        addPlaneHealthListener();
        setOkMessage(languageHelper.getString(className + ".okMessage"));
    }

    @Override
    protected boolean onInvalidated(FlightplanAirplanePair flightplanAirplanePair) {
        final IAirplane plane = flightplanAirplanePair.getPlane();
        try {
            float percentage = plane.getAirplaneCache().getMainBatteryPercentage();
            int col = plane.getAirplaneCache().getMainBatteryColor();

            if (col == 1) {
                // orange level
                addWarning(
                    languageHelper.getString(
                        className + ".orangeLevel", StringHelper.ratioToPercent(percentage / 100, 0, false)),
                    ValidationMessageCategory.NORMAL);
            } else if (col == 2) {
                // red level
                addWarning(
                    languageHelper.getString(
                        className + ".redLevel", StringHelper.ratioToPercent(percentage / 100, 0, false)),
                    ValidationMessageCategory.BLOCKING);
            }

        } catch (AirplaneCacheEmptyException e) {
            // expected
            return false;
        }

        return true;
    }

}
