/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.geotiff;

import eu.mavinci.core.obfuscation.IKeepAll;

public enum GeoTiffType implements IKeepAll {
    ELEVATION,
    IMAGERY,
    UNKNOWN
}
