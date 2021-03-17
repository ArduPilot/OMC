/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone;

import com.intel.missioncontrol.Localizable;
import eu.mavinci.core.obfuscation.IKeepAll;

@Localizable
public enum ArmedState implements IKeepAll {
    UNKNOWN,
    DISARMED,
    ARMED
}
