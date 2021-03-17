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
import eu.mavinci.core.plane.AirplaneCacheEmptyException;
import eu.mavinci.geo.CountryDetector;
import eu.mavinci.plane.IAirplane;
import gov.nasa.worldwind.geom.LatLon;

/** check B-03 drone is in forbidden country. */
public class RestrictedCountryPlaneValidator extends AirplaneValidatorBase {

    public interface Factory {
        RestrictedCountryPlaneValidator create(FlightplanAirplanePair flightplanAirplanePair);
    }

    private final String className = RestrictedCountryPlaneValidator.class.getName();

    @Inject
    public RestrictedCountryPlaneValidator(
            ILanguageHelper languageHelper,
            IQuantityStyleProvider quantityStyleProvider,
            @Assisted FlightplanAirplanePair flightplanAirplanePair) {
        super(languageHelper, quantityStyleProvider, flightplanAirplanePair);
        addPositionOrientationListener();
        // setOkMessage(languageHelper.getString(className+".okMessage"));//dont show it by intention
    }

    @Override
    protected boolean onInvalidated(FlightplanAirplanePair flightplanAirplanePair) {
        final IAirplane plane = flightplanAirplanePair.getPlane();

        try {
            LatLon pos = plane.getAirplaneCache().getCurLatLon();

            if (!CountryDetector.instance.allowProceed(pos)) {
                addWarning(
                    languageHelper.getString(
                        className + ".restricted", "" + CountryDetector.instance.getFirstCountry(pos)),
                    ValidationMessageCategory.BLOCKING);
            }
        } catch (AirplaneCacheEmptyException e) {
            // expected
            return false;
        }

        return true;
    }

}
