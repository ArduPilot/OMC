/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.ntripclient;

import eu.mavinci.desktop.gui.doublepanel.ntripclient.RtcmParser.IStatisticEntryProvider;
import eu.mavinci.desktop.gui.doublepanel.ntripclient.RtcmParser.StatisticEntry;
import java.time.Clock;
import java.util.Map;

class StatisticEntryProviderMock implements IStatisticEntryProvider {

    private final Map<Integer, Clock> clocks;

    StatisticEntryProviderMock(Map<Integer, Clock> clocks) {
        this.clocks = clocks;
    }

    @Override
    public StatisticEntry createEntryFor(int packageType) {
        return new StatisticEntry(clocks.getOrDefault(packageType, Clock.systemDefaultZone()));
    }
}
