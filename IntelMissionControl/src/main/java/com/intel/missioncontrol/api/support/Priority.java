/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.api.support;

import eu.mavinci.core.obfuscation.IKeepAll;

public enum Priority implements IKeepAll {
    CRITICAL,
    HIGH,
    MEDIUM,
    LOW,
    UNDEFINED
}
