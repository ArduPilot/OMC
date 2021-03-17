/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.airtraffic;

import com.intel.missioncontrol.airtraffic.dto.AirtrafficObject;
import org.asyncfx.beans.property.AsyncListProperty;

/**
 * IAirTrafficManager has the main job to provide the @see relevantTrafficProperty() that is a collection of all
 * airtraffic relevant in the user's context.
 */
public interface IAirTrafficManager {
    AsyncListProperty<AirtrafficObject> relevantTrafficProperty();
}
