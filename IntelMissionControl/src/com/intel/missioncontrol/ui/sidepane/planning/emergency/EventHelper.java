/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.planning.emergency;

import com.intel.missioncontrol.helper.Expect;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.ui.EnumConverter;
import eu.mavinci.core.flightplan.CEvent;
import eu.mavinci.core.plane.AirplaneEventActions;
import org.apache.commons.lang3.StringUtils;

public class EventHelper {

    private static final String KEY_NAME_PREFIX = "eu.mavinci.core.flightplan.CEventList.";
    private static final String ARROW = " -> ";

    private final ILanguageHelper languageHelper;
    private final EnumConverter<AirplaneEventActions> actionConverter;

    public EventHelper(ILanguageHelper languageHelper) {
        Expect.notNull(languageHelper, "languageHelper");
        this.languageHelper = languageHelper;
        this.actionConverter = new EnumConverter<>(languageHelper, AirplaneEventActions.class);
    }

    public String getEventDescription(CEvent event) {
        if (event == null) {
            return "";
        }

        String name = extractName(event);
        String action = extractAction(event);

        return name + ARROW + action;
    }

    private String extractName(CEvent event) {
        String name = event.getName();

        if (StringUtils.isBlank(name)) {
            return "";
        }

        return languageHelper.getString(KEY_NAME_PREFIX + name);
    }

    private String extractAction(CEvent event) {
        AirplaneEventActions action = event.getAction();

        if (action == null) {
            return "";
        }

        return actionConverter.toString(action);
    }

}
