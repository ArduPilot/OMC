/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.validation.flightplan;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.intel.missioncontrol.beans.property.IQuantityStyleProvider;
import com.intel.missioncontrol.helper.Ensure;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.mission.FlightPlan;
import com.intel.missioncontrol.ui.validation.ValidationMessageCategory;
import eu.mavinci.geo.Country;
import eu.mavinci.geo.CountryDetector;

/** check A-08: flight is within restricted country */
public class RestrictedCountryValidator extends OnFlightplanChangedValidator {

    public interface Factory {
        RestrictedCountryValidator create(FlightPlan flightPlan);
    }

    private final String className = RestrictedCountryValidator.class.getName();
    private ILanguageHelper languageHelper;

    @Inject
    public RestrictedCountryValidator(
            ILanguageHelper languageHelper,
            IQuantityStyleProvider quantityStyleProvider,
            @Assisted FlightPlan flightplan) {
        super(flightplan, quantityStyleProvider);
        this.languageHelper = languageHelper;
    }

    @Override
    protected boolean onInvalidated(FlightPlan flightplan) {
        if (!CountryDetector.instance.allowProceed(flightplan.getSector())) {
            Country country = CountryDetector.instance.getFirstCountry(flightplan.getSector());
            Ensure.notNull(country, "country");
            addWarning(
                languageHelper.getString(className + ".restricted", "" + country.name),
                ValidationMessageCategory.BLOCKING);
        }

        return true;
    }

}
