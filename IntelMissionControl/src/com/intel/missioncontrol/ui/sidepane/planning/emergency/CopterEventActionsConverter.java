/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.planning.emergency;

import com.intel.missioncontrol.helper.ILanguageHelper;
import eu.mavinci.core.plane.AirplaneEventActions;
import javafx.util.StringConverter;

public class CopterEventActionsConverter extends StringConverter<AirplaneEventActions> {

    private final ILanguageHelper languageHelper;
    private static final String PREFIX = "eu.mavinci.core.plane.AirplaneEventActions.Copter";

    public CopterEventActionsConverter(ILanguageHelper languageHelper) {
        this.languageHelper = languageHelper;
    }

    @Override
    public String toString(AirplaneEventActions object) {
        return languageHelper.toFriendlyName(PREFIX, object);
    }

    @Override
    public AirplaneEventActions fromString(String string) {
        return languageHelper.fromFriendlyName(AirplaneEventActions.class, PREFIX, string);
    }

}
