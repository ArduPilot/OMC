/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.worldwind;

import com.intel.missioncontrol.map.IMapModel;
import gov.nasa.worldwind.Model;

public interface IWWMapModel extends IMapModel {

    Model getWWModel();

}
