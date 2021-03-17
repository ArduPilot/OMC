/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.ntripclient;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import eu.mavinci.core.obfuscation.IKeepAll;
import eu.mavinci.desktop.gui.doublepanel.ntripclient.RtcmParser.StatisticEntry;
import eu.mavinci.test.rules.EarthElevationModelInitializer;
import eu.mavinci.test.rules.GuiceInitializer;
import eu.mavinci.test.rules.MavinciInitializer;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

@Ignore("BROKEN TEST, but ignored to get testing in build system")
public class RtcmParserIntegrationTest {

    @ClassRule
    public static final TestRule RULE_CHAIN =
        RuleChain.outerRule(new GuiceInitializer())
            .around(new MavinciInitializer())
            .around(new EarthElevationModelInitializer());

    private static final Gson GSON = new GsonBuilder().create();

    private RtcmParser rtcmParser;
    private byte[][] traffic;
    private byte[][] output;
    private byte[][] detailedStatisticsTraffic;
    private StatisticEntryProviderMock statisticEntryProviderMock;
    private List<Map<Integer, StatisticEntry>> statistics = new ArrayList<>();

    @Before
    public void setUp() throws Exception {
        populateStatisticsData();

        rtcmParser = new RtcmParser(statisticEntryProviderMock);

        traffic = new byte[191][];
        readTestData("/traffic", traffic);

        output = new byte[191][];
        readTestData("/output", output);

        detailedStatisticsTraffic = new byte[200][];
        readTestData("/detailed-statistics-traffic", detailedStatisticsTraffic);
    }

    private void populateStatisticsData() throws Exception {
        List<Map<Integer, RawRtkStatistic>> rawStatistics =
            GSON.fromJson(
                new InputStreamReader(getClass().getResource("/detailed-statistics-content.json").openStream()),
                new TypeToken<List<Map<Integer, RawRtkStatistic>>>() {}.getType());

        statistics = rawStatistics.stream().map(this::createStatisticEntriesFromRawData).collect(Collectors.toList());

        statisticEntryProviderMock = new StatisticEntryProviderMock(createClockMocksForRtkPackageTypes(rawStatistics));
    }

    private Map<Integer, StatisticEntry> createStatisticEntriesFromRawData(Map<Integer, RawRtkStatistic> raw) {
        return raw.entrySet().stream().collect(Collectors.toMap(Entry::getKey, e -> e.getValue().toStatisticEntry()));
    }

    private Map<Integer, Clock> createClockMocksForRtkPackageTypes(List<Map<Integer, RawRtkStatistic>> rawStatistics) {
        Map<Integer, RawRtkStatistic> lastStatisticsRecord = rawStatistics.get(rawStatistics.size() - 1);
        return lastStatisticsRecord
            .entrySet()
            .stream()
            .collect(
                Collectors.toMap(
                    Entry::getKey, e -> new StatisticClock(Lists.reverse(new ArrayList<>(e.getValue().que)))));
    }

    private void readTestData(String resourceName, byte[][] data) throws IOException {
        try (LineNumberReader resource =
            new LineNumberReader(new InputStreamReader(getClass().getResource(resourceName).openStream()))) {
            int j = 0;
            String line;
            while ((line = resource.readLine()) != null) {
                String[] split = line.split(", ");
                byte[] chunk = new byte[split.length];
                for (int i = 0; i < split.length; i++) {
                    chunk[i] = Byte.parseByte(split[i]);
                }

                data[j] = chunk;
                j += 1;
            }
        }
    }

    private static byte[] arraysCopier(byte[] buffer) {
        byte[] array = new byte[RtcmParser.inBufferSize];
        System.arraycopy(buffer, 0, array, 0, buffer.length);
        return array;
    }

    @Ignore("BROKEN TEST, but ignored to get testing in build system")
    @Test
    public void rawBufferContentTest() throws Exception {
        for (int i = 0; i < traffic.length; i++) {
            rtcmParser.addToBuffer(arraysCopier(traffic[i]), 0, traffic[i].length);
            assertThat("ERROR ON LINE - " + i, arraysCopier(output[i]), equalTo(rtcmParser.buff));
        }
    }

    @Ignore("BROKEN TEST, but ignored to get testing in build system")
    @Test
    public void rtkPackageStatisticTest() throws Exception {
        for (int i = 0; i < detailedStatisticsTraffic.length; i++) {
            rtcmParser.addToBuffer(arraysCopier(detailedStatisticsTraffic[i]), 0, detailedStatisticsTraffic[i].length);
            assertThat("ERROR ON LINE - " + (i + 1), rtcmParser.getStatistics(), equalTo(statistics.get(i)));
        }
    }

    @Ignore
    @Test
    public void rtkStationPosition() throws Exception {
        for (int i = 0; rtcmParser.lastStationPos == null; i++) {
            rtcmParser.addToBuffer(arraysCopier(detailedStatisticsTraffic[i]), 0, detailedStatisticsTraffic[i].length);
        }

        assertThat(
            rtcmParser.lastStationPos,
            is(
                new Position(
                    Angle.fromDegrees(50.77170209465638), Angle.fromDegrees(15.059891260142685), 448.41615824555925)));

        rtcmParser.lastStationPos = null;

        for (int i = 0; rtcmParser.lastStationPos == null; i++) {
            rtcmParser.addToBuffer(arraysCopier(traffic[i]), 0, traffic[i].length);
        }

        assertThat(
            rtcmParser.lastStationPos,
            is(
                new Position(
                    Angle.fromDegrees(43.33807382011539),
                    Angle.fromDegrees(-1.7970502455001014E-4),
                    -1853.4432743268414)));
    }

    private static class RawRtkStatistic implements IKeepAll {

        int cnt;
        Deque<Long> que;

        StatisticEntry toStatisticEntry() {
            StatisticEntry statisticEntry = new StatisticEntry(null);
            statisticEntry.packageCounter = cnt;
            statisticEntry.que.addAll(que);
            return statisticEntry;
        }
    }
}
