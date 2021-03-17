/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.hardware;

import com.intel.missioncontrol.INotificationObject;
import com.intel.missioncontrol.measure.Dimension.Length;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.Dimension.Time;
import eu.mavinci.core.flightplan.camera.LensTypes;

public interface ILensDescription extends INotificationObject {

    String ID_PROPERTY = "id";
    String NAME_PROPERTY = "name";
    String MAX_TIME_VARIATION_PROPERTY = "maxTimeVariation";
    String IS_LENS_MANUAL_PROPERTY = "isLensManual";
    String IS_LENS_APERTURE_NOT_AVAILABLE_PROPERTY = "isApertureNotAvailable";
    String LENS_TYPE_PROPERTY = "lensType";
    String FOCAL_LENGTH_PROPERTY = "focalLength";
    String MIN_REP_TIME_PROPERTY = "minRepTime";

    String getId();

    String getName();

    Quantity<Time> getMaxTimeVariation();

    boolean isLensManual();

    boolean  isLensApertureNotAvailable();

    LensTypes getLensType();

    Quantity<Length> getFocalLength();

    Quantity<Time> getMinRepTime();

    default IMutableLensDescription asMutable() {
        return (IMutableLensDescription)this;
    }

}
