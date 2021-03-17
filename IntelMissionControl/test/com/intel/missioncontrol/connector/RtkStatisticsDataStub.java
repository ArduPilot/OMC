/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.connector;

import com.intel.missioncontrol.ui.navbar.connection.ConnectionState;
import com.intel.missioncontrol.ui.navbar.connection.RtkStatistic;
import com.intel.missioncontrol.ui.navbar.connection.model.RtkStatisticsData;
import com.intel.missioncontrol.ui.navbar.connection.view.StatisticData;
import com.intel.missioncontrol.ui.navbar.connection.viewmodel.RtkStatisticData;
import java.util.ArrayList;
import java.util.List;

class RtkStatisticsDataStub implements RtkStatisticsData {
    private final List<RtkStatistic> statistics = new ArrayList<>();
    private final List<StatisticData> statisticsData = new ArrayList<>();

    @Override
    public List<RtkStatistic> getDetailedStatisticsItems() {
        return statistics;
    }

    @Override
    public List<StatisticData> getStatisticsDataItems() {
        return statisticsData;
    }

    @Override
    public void updateConnectionState(ConnectionState connectionState) {

    }

    @Override
    public void setData(RtkStatisticData data) {

    }

    @Override
    public RtkStatisticData getData() {
        return null;
    }
}
