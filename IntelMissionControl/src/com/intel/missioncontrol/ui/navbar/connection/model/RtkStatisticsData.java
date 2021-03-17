/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.connection.model;

import com.intel.missioncontrol.ui.navbar.connection.ConnectionState;
import com.intel.missioncontrol.ui.navbar.connection.RtkStatistic;
import com.intel.missioncontrol.ui.navbar.connection.view.StatisticData;
import com.intel.missioncontrol.ui.navbar.connection.viewmodel.RtkStatisticData;

import java.util.List;

public interface RtkStatisticsData {
    List<RtkStatistic> getDetailedStatisticsItems();

    List<StatisticData> getStatisticsDataItems();

    void updateConnectionState(ConnectionState connectionState);

    void setData(RtkStatisticData data);

    RtkStatisticData getData();
}
