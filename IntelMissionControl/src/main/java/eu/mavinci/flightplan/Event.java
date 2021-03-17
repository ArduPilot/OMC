/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.flightplan;

import com.intel.missioncontrol.StaticInjector;
import com.intel.missioncontrol.helper.ILanguageHelper;
import eu.mavinci.core.flightplan.CEvent;
import eu.mavinci.core.flightplan.IFlightplanRelatedObject;
import eu.mavinci.core.helper.StringHelper;
import eu.mavinci.core.plane.AirplaneEventActions;

public class Event extends CEvent {

    public static final String KEY = "eu.mavinci.flightplan.Event";
    public static final String KEY_LEVEL = KEY + ".level";
    public static final String KEY_DELAY = KEY + ".delay";
    public static final String KEY_recover = KEY + ".recover";
    public static final String KEY_action = KEY + ".action";
    public static final String KEY_actionNames = "AirplaneEventActions";
    public static final String KEY_name = KEY + ".name";

    public static final String KEY_TO_STRING = KEY + ".toString";

    private static final ILanguageHelper languageHelper = StaticInjector.getInstance(ILanguageHelper.class);

    protected Event(
            EventList parent,
            String name,
            AirplaneEventActions action,
            int delay,
            boolean hasLevel,
            int level,
            boolean recover) {
        super(parent, name, action, delay, hasLevel, level, recover);
    }

    public Event(EventList parent, String name) {
        super(parent, name);
    }

    public static Object getActionI18N(AirplaneEventActions action) {
        return languageHelper.getString(KEY_actionNames + "." + action.name());
    }

    @Override
    public String toString() {
        if (delay == -1) {
            if (getAction() == AirplaneEventActions.ignore) {
                return languageHelper.getString(
                    KEY_TO_STRING + ".perfomed.ignore",
                    getNameI18n(),
                    getActionI18N(getAction()),
                    StringHelper.secToShortDHMS(getDelay()),
                    languageHelper.getString(KEY_recover + "." + isRecover()));
            } else {
                return languageHelper.getString(
                    KEY_TO_STRING + ".perfomed",
                    getNameI18n(),
                    getActionI18N(getAction()),
                    StringHelper.secToShortDHMS(getDelay()),
                    languageHelper.getString(KEY_recover + "." + isRecover()));
            }
        } else {
            if (getAction() == AirplaneEventActions.ignore) {
                return languageHelper.getString(
                    KEY_TO_STRING + ".ignore",
                    getNameI18n(),
                    getActionI18N(getAction()),
                    StringHelper.secToShortDHMS(getDelay()),
                    languageHelper.getString(KEY_recover + "." + isRecover()));
            } else {
                return languageHelper.getString(
                    KEY_TO_STRING,
                    getNameI18n(),
                    getActionI18N(getAction()),
                    StringHelper.secToShortDHMS(getDelay()),
                    languageHelper.getString(KEY_recover + "." + isRecover()));
            }
        }
    }

    @Override
    public EventList getParent() {
        return (EventList)super.getParent();
    }

    public String getNameI18n() {
        return languageHelper.getString(KEY_name + "." + getName());
    }

    @Override
    public IFlightplanRelatedObject getCopy() {
        return new Event(getParent(), getName(), getAction(), getDelay(), hasLevel(), getLevel(), isRecover());
    }

}
