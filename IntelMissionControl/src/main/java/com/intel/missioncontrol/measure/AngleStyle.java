/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.measure;

import com.intel.missioncontrol.Localizable;
import eu.mavinci.core.obfuscation.IKeepAll;

@Localizable
public enum AngleStyle implements IKeepAll {
    DECIMAL_DEGREES,
    DEGREE_DECIMAL_MINUTE,
    DEGREE_MINUTE_SECOND
}
