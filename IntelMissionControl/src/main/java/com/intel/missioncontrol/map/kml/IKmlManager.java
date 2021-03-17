/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.kml;

import com.intel.missioncontrol.map.ILayer;
import org.asyncfx.collections.AsyncObservableList;

public interface IKmlManager {
    AsyncObservableList<ILayer> imageryLayersProperty();
}
