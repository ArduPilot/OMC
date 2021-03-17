/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.connection.model;

import eu.mavinci.desktop.gui.doublepanel.ntripclient.IRtkClient;
import eu.mavinci.desktop.gui.doublepanel.ntripclient.IRtkStatisticListener;

public interface IRtkStatisticFactory {

    IRtkStatisticListener createStatisticListener(RtkStatisticsData rtkStatisticsData, IRtkClient client);
}
