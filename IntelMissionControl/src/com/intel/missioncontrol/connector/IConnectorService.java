/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.connector;

import com.intel.missioncontrol.ui.navbar.connection.model.RtkStatisticsData;
import eu.mavinci.desktop.gui.doublepanel.ntripclient.NtripConnectionSettings;

import io.reactivex.Observable;
import java.util.UUID;

public interface IConnectorService {

    UUID createNtripConnector(RtkStatisticsData statisticsData, NtripConnectionSettings connectionSetting);

    void closeConnector(UUID connectorId);

    Observable<Integer> getPackageSourceFor(UUID connectorId);
}
