/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.connection.viewmodel;

import com.intel.missioncontrol.ui.ViewModelBase;
import com.intel.missioncontrol.ui.navbar.connection.RtkStatistic;
import com.intel.missioncontrol.ui.navbar.connection.scope.RtkConnectionScope;
import com.intel.missioncontrol.ui.navbar.connection.view.StatisticData;
import de.saxsys.mvvmfx.InjectScope;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ListProperty;

public class RtkStatisticsViewModel extends ViewModelBase {

    @InjectScope
    RtkConnectionScope rtkConnectionScope;

    public ListProperty<RtkStatistic> detailedStatisticsItemsProperty() {
        return rtkConnectionScope.detailedStatisticsItemsProperty();
    }

    public BooleanBinding isConnectedProperty() {
        return rtkConnectionScope.isConnectedBinding();
    }

    public ListProperty<StatisticData> statisticDataProperty() {
        return rtkConnectionScope.statisticDataProperty();
    }
}
