/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.settings;

import eu.mavinci.desktop.rs232.Rs232Params;

/** @deprecated use {@link Rs232Params.Parity} */
@Deprecated
public enum Parity {
    NONE,
    EVEN,
    ODD,
    MARK,
    SPACE
}
