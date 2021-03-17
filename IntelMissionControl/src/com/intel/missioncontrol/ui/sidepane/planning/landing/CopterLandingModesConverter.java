/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.planning.landing;

import com.intel.missioncontrol.helper.ILanguageHelper;
import eu.mavinci.core.flightplan.LandingModes;
import javafx.util.StringConverter;

public class CopterLandingModesConverter extends StringConverter<LandingModes> {

    private final ILanguageHelper languageHelper;
    private static final String PREFIX = "eu.mavinci.core.flightplan.LandingModes.Copter";

    public CopterLandingModesConverter(ILanguageHelper languageHelper) {
        this.languageHelper = languageHelper;
    }

    @Override
    public String toString(LandingModes object) {
        return languageHelper.toFriendlyName(PREFIX, object);
    }

    @Override
    public LandingModes fromString(String string) {
        return languageHelper.fromFriendlyName(LandingModes.class, PREFIX, string);
    }

}
