/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone.connection;

/** A service providing IConnectionListeners. */
public interface IConnectionListenerService {
    IConnectionListener getConnectionListener();
}
