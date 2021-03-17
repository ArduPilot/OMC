/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.ntripclient;

import com.google.common.collect.ImmutableMap.Builder;
import eu.mavinci.desktop.gui.doublepanel.ntripclient.RtcmParser.IStatisticEntryProvider;
import java.time.Clock;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Map;

public class RtkPackageSampler {

    private final long currentTime;
    private final Deque<Long> timestamps;

    public RtkPackageSampler(long currentTime, Long... timestamps) {
        this(currentTime, packageArrivalTimestamps(timestamps));
    }

    public RtkPackageSampler(long currentTime, Deque<Long> timestamps) {
        this.currentTime = currentTime;
        this.timestamps = timestamps;
    }

    private static Deque<Long> packageArrivalTimestamps(Long... timestamps) {
        return new ArrayDeque<>(Arrays.asList(timestamps));
    }

    private Clock packageTypeClock() {
        List<Long> times = new ArrayList<>(timestamps);
        times.add(currentTime);
        return new StatisticClock(times);
    }

    public IStatisticEntryProvider createStatisticsProviderOnlyFor(int packageType) {
        Map<Integer, Clock> packageTypeClock = new Builder<Integer, Clock>()
            .put(packageType, packageTypeClock())
            .build();

        return new StatisticEntryProviderMock(packageTypeClock);
    }

    public double packageAverageFrequencyRequestedWithinLastPackage() {
        return (timestamps.peekLast() - timestamps.peekFirst()) / (timestamps.size() - 1) / 1000.0;
    }

    public double packageAverageFrequencyRequestedWithinCurrentTime() {
        return (currentTime - timestamps.peekFirst()) / (timestamps.size()) / 1000.0;
    }

    public Deque<Long> timestamps() {
        return timestamps;
    }
}
