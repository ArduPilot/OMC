/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.ntripclient;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.closeTo;
import static org.junit.Assert.assertThat;

import eu.mavinci.desktop.gui.doublepanel.ntripclient.RtcmParser.IStatisticEntryProvider;
import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.TestScheduler;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import org.junit.Ignore;
import org.junit.Test;

public class RtcmStatisticsFrequencyComputationTest {

    private static final int PACKAGE_TYPE = 1004;

    private Deque<Long> generateNotFixedRateTimestamps() {
        return LongStream.iterate(1, prev -> (prev % 5) + 1)
            .limit(50)
            .map(i -> i * 10_000)
            .boxed()
            .collect(Collectors.toCollection(ArrayDeque::new));
    }

    @Test
    public void frequencyIsInfinite_whenNoPackageReceived() throws Exception {
        RtkPackageSampler rtkPackageSampler = new RtkPackageSampler(0L);
        IStatisticEntryProvider statisticEntryProvider = rtkPackageSampler.createStatisticsProviderOnlyFor(PACKAGE_TYPE);
        RtcmParser parser = new RtcmParser(statisticEntryProvider);

        assertThat(parser.getStatistics().get(PACKAGE_TYPE).estimateRate(), is(Double.POSITIVE_INFINITY));
    }

    @Test
    public void frequencyIsInfinite_whenOnlyOnePackageReceived() throws Exception {
        RtkPackageSampler rtkPackageSampler = new RtkPackageSampler(100_000L, 100_000L);
        IStatisticEntryProvider statisticEntryProvider = rtkPackageSampler.createStatisticsProviderOnlyFor(PACKAGE_TYPE);

        RtcmParser parser = new RtcmParser(statisticEntryProvider);

        rtkPackageSampler.timestamps().forEach(t -> parser.updatePackageStatistics(PACKAGE_TYPE));

        assertThat(parser.getStatistics().get(PACKAGE_TYPE).estimateRate(), is(Double.POSITIVE_INFINITY));
    }

    @Test
    public void frequencyIsAverageRate_whenPackagesReceived_lessThanLimit() throws Exception {
        RtkPackageSampler rtkPackageSampler = new RtkPackageSampler(120_000L, 100_000L, 120_000L);
        IStatisticEntryProvider statisticEntryProvider = rtkPackageSampler.createStatisticsProviderOnlyFor(PACKAGE_TYPE);
        RtcmParser parser = new RtcmParser(statisticEntryProvider);

        rtkPackageSampler.timestamps().forEach(t -> parser.updatePackageStatistics(PACKAGE_TYPE));

        assertThat(parser.getStatistics().get(PACKAGE_TYPE).estimateRate(),
            closeTo(rtkPackageSampler.packageAverageFrequencyRequestedWithinLastPackage(), 0.1));
    }

    @Test
    public void computeFrequency_withFixedRateArrival() throws Exception {
        RtkPackageSampler rtkPackageSampler = new RtkPackageSampler(180_000L, 100_000L, 120_000L, 140_000L, 160_000L, 180_000L);
        IStatisticEntryProvider statisticEntryProvider = rtkPackageSampler.createStatisticsProviderOnlyFor(PACKAGE_TYPE);
        RtcmParser parser = new RtcmParser(statisticEntryProvider);

        rtkPackageSampler.timestamps().forEach(t -> parser.updatePackageStatistics(PACKAGE_TYPE));

        assertThat(parser.getStatistics().get(PACKAGE_TYPE).estimateRate(),
            closeTo(rtkPackageSampler.packageAverageFrequencyRequestedWithinLastPackage(), 0.1));
    }

    @Test
    public void computeFrequency_whenLastPackageLagBehind() throws Exception {
        RtkPackageSampler rtkPackageSampler = new RtkPackageSampler(300_000L, 100_000L, 120_000L, 140_000L, 160_000L, 300_000L);
        IStatisticEntryProvider statisticEntryProvider = rtkPackageSampler.createStatisticsProviderOnlyFor(PACKAGE_TYPE);
        RtcmParser parser = new RtcmParser(statisticEntryProvider);

        rtkPackageSampler.timestamps().forEach(t -> parser.updatePackageStatistics(1004));

        assertThat(parser.getStatistics().get(PACKAGE_TYPE).estimateRate(),
            closeTo(rtkPackageSampler.packageAverageFrequencyRequestedWithinLastPackage(), 0.1));
    }

    @Test
    @Ignore
    public void computeFrequency_whenConsumeLotsOfPackages() throws Exception {
        Deque<Long> timestamps = generateNotFixedRateTimestamps();
        RtkPackageSampler rtkPackageSampler = new RtkPackageSampler(timestamps.peekLast(), timestamps);

        IStatisticEntryProvider statisticEntryProvider = rtkPackageSampler.createStatisticsProviderOnlyFor(PACKAGE_TYPE);
        RtcmParser parser = new RtcmParser(statisticEntryProvider);

        rtkPackageSampler.timestamps().forEach(t -> parser.updatePackageStatistics(PACKAGE_TYPE));

        assertThat(parser.getStatistics().get(PACKAGE_TYPE).estimateRate(),
            closeTo(rtkPackageSampler.packageAverageFrequencyRequestedWithinLastPackage(), 0.1));
    }

    @Test
    @Ignore
    public void computeFrequency_whenConsumeLotsOfPackages_andCurrentTimeIsNotEqual_lastPackageTime() throws Exception {
        Deque<Long> timestamps = generateNotFixedRateTimestamps();
        final long currentTime = timestamps.peekLast() + 50_000L;

        RtkPackageSampler rtkPackageSampler = new RtkPackageSampler(currentTime, timestamps);
        IStatisticEntryProvider statisticEntryProvider = rtkPackageSampler.createStatisticsProviderOnlyFor(PACKAGE_TYPE);
        RtcmParser parser = new RtcmParser(statisticEntryProvider);

        rtkPackageSampler.timestamps().forEach(t -> parser.updatePackageStatistics(PACKAGE_TYPE));

        assertThat(parser.getStatistics().get(PACKAGE_TYPE).estimateRate(),
            closeTo(rtkPackageSampler.packageAverageFrequencyRequestedWithinCurrentTime(), 0.1));
    }
}
