/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.hardware;

public interface IConnectionProperties {
    String getDroneType();
    double getLinkLostTimeoutSeconds();
}
