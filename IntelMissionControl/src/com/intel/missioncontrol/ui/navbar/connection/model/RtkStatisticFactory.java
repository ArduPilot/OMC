/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.connection.model;

import com.google.inject.Inject;
import com.intel.missioncontrol.concurrent.Dispatcher;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.ui.navbar.connection.ConnectionState;
import com.intel.missioncontrol.ui.navbar.connection.RtkStatistic;
import com.intel.missioncontrol.ui.navbar.connection.view.StatisticData;
import com.intel.missioncontrol.ui.navbar.connection.viewmodel.RtkStatisticData;
import eu.mavinci.core.helper.StringHelper;
import eu.mavinci.core.plane.CAirplaneCache;
import eu.mavinci.desktop.gui.doublepanel.ntripclient.IRtkClient;
import eu.mavinci.desktop.gui.doublepanel.ntripclient.IRtkStatisticListener;
import eu.mavinci.desktop.gui.doublepanel.ntripclient.NtripConnectionState;
import eu.mavinci.desktop.gui.doublepanel.ntripclient.RtcmParser;
import eu.mavinci.desktop.main.debug.Debug;
import gov.nasa.worldwind.geom.Position;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class RtkStatisticFactory implements IRtkStatisticFactory {
    private static final int STATUS_ROW = 0;
    private static final int PACKAGE_STATUS_ROW = 1;
    private static final int TIME_ROW = 2;
    private static final int VOLUME_IN_ROW = 3;
    private static final int VOLUME_OUT_ROW = 4;
    private static final int SKIPPED_ROW = 5;
    private static final int BASE_ID_ROW = 6;
    private static final int BASE_ANTENNA_ROW = 7;
    private static final int BASELINE_ROW = 8;
    private static final int BASE_POSITION_ROW = 9;

    private final Map<Integer, String> descriptions = new HashMap<>();
    private final Map<Integer, StatusCalculator> rates = new HashMap<>();

    private final ILanguageHelper languageHelper;

    private static class PackageRates implements StatusCalculator {
        private final double minRate;
        private final double maxRate;

        PackageRates(double minRate, double maxRate) {
            this.minRate = minRate;
            this.maxRate = maxRate;
        }

        @Override
        public RtkStatistic.Status fixRateCheck(double rate) {
            return isInsideRateRange(rate) ? RtkStatistic.Status.GOOD : RtkStatistic.Status.BAD;
        }

        private boolean isInsideRateRange(double rate) {
            return rate >= minRate && rate <= maxRate;
        }
    }

    private interface StatusCalculator {
        RtkStatistic.Status fixRateCheck(double rate);
    }

    @Inject
    public RtkStatisticFactory(ILanguageHelper languageHelper) {
        this.languageHelper = languageHelper;

        descriptions.put(1004, languageHelper.getString("com.intel.missioncontrol.RtkStatistic.gps"));

        String stationCoordinate = languageHelper.getString("com.intel.missioncontrol.RtkStatistic.stationCoordinate");
        descriptions.put(1005, stationCoordinate);
        descriptions.put(1006, stationCoordinate);

        String antennaDescription =
            languageHelper.getString("com.intel.missioncontrol.RtkStatistic.antennaDescription");
        descriptions.put(1007, antennaDescription);
        descriptions.put(1008, antennaDescription);

        descriptions.put(1012, languageHelper.getString("com.intel.missioncontrol.RtkStatistic.glonass"));
        descriptions.put(1029, languageHelper.getString("com.intel.missioncontrol.RtkStatistic.unicodeTextString"));

        rates.put(1004, new PackageRates(0.25, 1.5));
        rates.put(1012, new PackageRates(0.25, 1.5));
        rates.put(1005, new PackageRates(0.5, 15));
        rates.put(1006, new PackageRates(0.5, 15));
        rates.put(1007, new PackageRates(0.5, 15));
        rates.put(1008, new PackageRates(0.5, 15));
    }

    @Override
    public IRtkStatisticListener createStatisticListener(RtkStatisticsData rtkStatisticsData, IRtkClient client) {
        return new IRtkStatisticListener() {
            @Override
            public void connectionStateChanged(NtripConnectionState conState, int msecUnitlReconnect) {
                switch (conState) {
                case connected:
                    rtkStatisticsData.updateConnectionState(ConnectionState.CONNECTED);
                    client.getStatistics().clear();
                    break;
                case unconnected:
                    Dispatcher.postToUI(
                        () -> rtkStatisticsData.updateConnectionState(ConnectionState.NOT_CONNECTED));
                    break;
                default:
                    Dispatcher.postToUI(
                        () -> rtkStatisticsData.updateConnectionState(ConnectionState.CONNECTED));
                    break;
                }
            }

            @Override
            public void timerTickWhileConnected(long msecConnected, long byteTransferredIn, long byteTransferredOut) {
                rtkStatisticsData.setData(new RtkStatisticData());
                List<RtkStatistic> updatedRtkDetailedStatistics = getUpdatedRtkDetailedStatistics(client);
                rtkStatisticsData.getDetailedStatisticsItems().clear();
                rtkStatisticsData.getDetailedStatisticsItems().addAll(updatedRtkDetailedStatistics);
                RtcmParser parser = client.getParser();
                List<StatisticData> statisticsDataItems = rtkStatisticsData.getStatisticsDataItems();
                Position posRover = client.getLastPosWGS84();
                Position posBase = parser.lastStationPos;
                Dispatcher.postToUI(
                    () -> {
                        try {
                            updateStatus(statisticsDataItems);
                            updatePackageStatus(statisticsDataItems);
                            updateTime(msecConnected, statisticsDataItems);
                            updateVolumeIn(byteTransferredIn, statisticsDataItems);
                            updateVolumeOut(byteTransferredOut, statisticsDataItems);
                            updateSkipped(parser.getBytesSkipped(), statisticsDataItems);
                            updateLastStationId(statisticsDataItems, parser.lastStationID);
                            updateAntennaId(statisticsDataItems, parser.antennaID);
                            updateBaseline(statisticsDataItems, posRover, posBase);
                            updateBasePosition(statisticsDataItems, posBase);
                        } catch (Exception e) {
                            Debug.getLog().log(Level.INFO, "ERROR", e);
                        }
                    });
            }

            @Override
            public void packageReceived(byte[] msg, int type) {}
        };
    }

    private Function<Map.Entry<Integer, RtcmParser.StatisticEntry>, RtkStatistic> mapToRtkStatistic =
        e -> {
            RtcmParser.StatisticEntry statisticEntry = e.getValue();
            int count = statisticEntry.getReceivedPackagesNumber();
            double estimateRate = statisticEntry.estimateRate();
            RtkStatistic.Status status =
                rates.getOrDefault(e.getKey(), r -> RtkStatistic.Status.GOOD).fixRateCheck(estimateRate);
            return new RtkStatistic(e.getKey(), descriptions.getOrDefault(e.getKey(), ""), status, count, estimateRate);
        };

    private void updateBasePosition(List<StatisticData> statisticsDataItems, Position posBase) {
        if (posBase != null) {
            String lon = String.format("%.3f°", posBase.getLongitude().getDegrees());
            String lat = String.format("%.3f°", posBase.getLatitude().getDegrees());
            String alt = String.format("%.1fm", posBase.getAltitude());
            String result =
                String.format(
                    "%s %s\n%s %s\n%s %s",
                    languageHelper.getString("com.intel.missioncontrol.RtkStatistic.basePosition.lon"),
                    lon,
                    languageHelper.getString("com.intel.missioncontrol.RtkStatistic.basePosition.lat"),
                    lat,
                    languageHelper.getString("com.intel.missioncontrol.RtkStatistic.basePosition.alt"),
                    alt);
            StatisticData basePosition = statisticsDataItems.get(BASE_POSITION_ROW);
            basePosition.updateContent(result);
            basePosition.updateLocation(posBase);
        }
    }

    private void updateBaseline(List<StatisticData> statisticsDataItems, Position posRover, Position posBase) {
        if (posBase != null && posRover != null) {
            double baseline =
                CAirplaneCache.distanceMeters(
                    posBase.latitude.degrees,
                    posBase.longitude.degrees,
                    posBase.elevation,
                    posRover.latitude.degrees,
                    posRover.longitude.degrees,
                    posRover.elevation);
            statisticsDataItems.get(BASELINE_ROW).updateContent(StringHelper.lengthToIngName(baseline, -2, true));
        }
    }

    private void updateAntennaId(List<StatisticData> statisticsDataItems, String antennaId) {
        statisticsDataItems.get(BASE_ANTENNA_ROW).updateContent(antennaId);
    }

    private void updateLastStationId(List<StatisticData> statisticsDataItems, int lastStationId) {
        statisticsDataItems.get(BASE_ID_ROW).updateContent(String.valueOf(lastStationId));
    }

    private void updateSkipped(long bytesSkipped, List<StatisticData> statisticsDataItems) {
        statisticsDataItems.get(SKIPPED_ROW).updateContent(StringHelper.bytesToIngName(bytesSkipped, -4, true));
    }

    private void updateVolumeOut(long byteTransferredOut, List<StatisticData> statisticsDataItems) {
        statisticsDataItems
            .get(VOLUME_OUT_ROW)
            .updateContent(StringHelper.bytesToIngName(byteTransferredOut, -4, true));
    }

    private void updateVolumeIn(long byteTransferredIn, List<StatisticData> statisticsDataItems) {
        statisticsDataItems.get(VOLUME_IN_ROW).updateContent(StringHelper.bytesToIngName(byteTransferredIn, -4, true));
    }

    private void updateTime(long msecConnected, List<StatisticData> statisticsDataItems) {
        statisticsDataItems.get(TIME_ROW).updateContent(StringHelper.secToShortDHMS(msecConnected / 1000.));
    }

    private void updatePackageStatus(List<StatisticData> statisticsDataItems) {
        statisticsDataItems
            .get(PACKAGE_STATUS_ROW)
            .updateContent(languageHelper.getString("com.intel.missioncontrol.RtkStatistic.packageStatus.good"));
    }

    private void updateStatus(List<StatisticData> statisticsDataItems) {
        statisticsDataItems
            .get(STATUS_ROW)
            .updateContent(languageHelper.getString("com.intel.missioncontrol.RtkStatistic.status.connected"));
    }

    private List<RtkStatistic> getUpdatedRtkDetailedStatistics(IRtkClient client) {
        return client.getStatistics().entrySet().stream().map(mapToRtkStatistic).collect(Collectors.toList());
    }
}
