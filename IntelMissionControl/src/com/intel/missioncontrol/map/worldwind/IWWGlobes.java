/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.worldwind;

import gov.nasa.worldwind.globes.Globe;

public interface IWWGlobes {

    Globe getActiveGlobe();

    Globe getDefaultGlobe();

    Globe getFlatGlobe();

}
