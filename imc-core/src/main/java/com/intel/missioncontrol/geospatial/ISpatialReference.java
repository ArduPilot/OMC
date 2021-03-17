/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.geospatial;

import com.intel.missioncontrol.geometry.Vec4;
import com.intel.missioncontrol.serialization.CompositeSerializable;

public interface ISpatialReference extends CompositeSerializable {

    String getEpsg();

    Vec4 fromWgs84(Position pos);

    Position toWgs84(Vec4 vec);

}
