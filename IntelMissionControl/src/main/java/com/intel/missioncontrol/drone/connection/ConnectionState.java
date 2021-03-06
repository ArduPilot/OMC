/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone.connection;

import com.intel.missioncontrol.Localizable;

@Localizable
public enum ConnectionState {
    NOT_CONNECTED,
    CONNECTING,
    CONNECTED
}
