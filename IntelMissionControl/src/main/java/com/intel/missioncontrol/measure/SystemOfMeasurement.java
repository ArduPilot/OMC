/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.measure;

import com.intel.missioncontrol.Localizable;
import eu.mavinci.core.obfuscation.IKeepAll;

@Localizable
public enum SystemOfMeasurement implements IKeepAll {
    METRIC,
    IMPERIAL,
    ICAO
}
