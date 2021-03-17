/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone;

import com.intel.missioncontrol.Localizable;

public enum GnssState implements Localizable {
    UNKNOWN,
    NO_FIX,
    GPS,
    RTK_FLOAT,
    RTK_FIXED
}
