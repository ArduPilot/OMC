/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone.connection;

import org.asyncfx.concurrent.Future;

interface IConnector<T> {
    IConnectionItem getConnectionItem();

    Future<T> connectAsync();

    Future<Void> disconnectAsync();
}
