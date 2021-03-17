/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.elevation;

import gov.nasa.worldwind.geom.LatLon;

public interface IEgmModel {

    double getEGM96Offset(LatLon latLon);
}
