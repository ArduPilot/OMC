/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.kml;

import com.intel.missioncontrol.collections.AsyncObservableList;
import com.intel.missioncontrol.map.ILayer;

public interface IKmlManager {
    AsyncObservableList<ILayer> imageryLayersProperty();
}
