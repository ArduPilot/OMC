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
import eu.mavinci.plane.IAirplane;

/** Check if a writeable UAV connection exist */
public class UavConnectedValidator extends AirplaneValidatorBase {

    public interface Factory {
        UavConnectedValidator create(FlightplanAirplanePair flightplanAirplanePair);
    }

    private final String className = UavConnectedValidator.class.getName();

    @Inject
    public UavConnectedValidator(
            ILanguageHelper languageHelper,
            IQuantityStyleProvider quantityStyleProvider,
            @Assisted FlightplanAirplanePair flightplanAirplanePair) {
        super(languageHelper, quantityStyleProvider, flightplanAirplanePair);
        addConnectionStateListener();
        setOkMessage(languageHelper.getString(className + ".okMessage"));
    }

    @Override
    protected boolean onInvalidated(FlightplanAirplanePair flightplanAirplanePair) {
        final IAirplane plane = flightplanAirplanePair.getPlane();
        if (!plane.isWriteable()) {
            addWarning(
                languageHelper.getString(className + ".notConnectedToActualUAV"), ValidationMessageCategory.BLOCKING);
        }

        return true;
    }

}
