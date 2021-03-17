/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.flightplan;

import com.intel.missioncontrol.StaticInjector;
import com.intel.missioncontrol.helper.ILanguageHelper;
import eu.mavinci.core.flightplan.CPhoto;
import eu.mavinci.core.flightplan.IFlightplanContainer;
import eu.mavinci.core.flightplan.IFlightplanRelatedObject;
import eu.mavinci.core.helper.StringHelper;

public class Photo extends CPhoto {

    public static final String KEY = "eu.mavinci.flightplan.Photo";
    public static final String KEY_ON = KEY + ".on";
    public static final String KEY_OFF = KEY + ".off";
    public static final String KEY_Distance = KEY + ".Distance";
    public static final String KEY_DistanceMax = KEY + ".DistanceMax";
    public static final String KEY_TO_STRING = KEY + ".toString";
    public static final String KEY_WP_MODE = KEY + ".wpMode";

    private static final ILanguageHelper languageHelper = StaticInjector.getInstance(ILanguageHelper.class);

    public Photo(boolean powerOn, double distance, double distanceMax, IFlightplanContainer parent) {
        super(powerOn, distance, distanceMax, parent);
    }

    public Photo(IFlightplanContainer parent) {
        super(parent);
    }

    public Photo(boolean powerOn, double distance, double distanceMax, int id) {
        super(powerOn, distance, distanceMax, id);
    }

    public Photo(boolean powerOn, double distance, double distanceMax, int id, IFlightplanContainer parent) {
        super(powerOn, distance, distanceMax, id, parent);
    }

    public Photo(Photo source) {
        super(source.powerOn, source.distance, source.distanceMax, source.id);
        this.triggerOnlyOnWaypoints = source.triggerOnlyOnWaypoints;
    }

    public String toString() {
        if (powerOn) {
            return languageHelper.getString(
                KEY_TO_STRING,
                languageHelper.getString(KEY_ON),
                StringHelper.lengthToIngName(distance / 100., -3, false),
                StringHelper.lengthToIngName(distanceMax / 100., -3, false));
        } else {
            return languageHelper.getString(
                KEY_TO_STRING,
                languageHelper.getString(KEY_OFF),
                StringHelper.lengthToIngName(distance / 100., -3, false),
                StringHelper.lengthToIngName(distanceMax / 100., -3, false));
        }
    }

    @Override
    public IFlightplanRelatedObject getCopy() {
        return new Photo(this);
    }
}
