/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.connector;

import com.google.inject.Inject;
import com.intel.missioncontrol.concurrent.Dispatcher;
import com.intel.missioncontrol.concurrent.FluentFuture;
import com.intel.missioncontrol.ui.navbar.connection.model.IRtkStatisticFactory;
import com.intel.missioncontrol.ui.navbar.connection.model.RtkStatisticsData;
import eu.mavinci.desktop.gui.doublepanel.ntripclient.ConnectionObjects;
import eu.mavinci.desktop.gui.doublepanel.ntripclient.IRtkStatisticListener;
import eu.mavinci.desktop.gui.doublepanel.ntripclient.NtripConnectionSettings;
import eu.mavinci.desktop.gui.doublepanel.ntripclient.NtripConnectionState;
import eu.mavinci.desktop.gui.doublepanel.ntripclient.RtcmParser;
import eu.mavinci.desktop.gui.doublepanel.ntripclient.RtcmParser.StatisticEntryProvider;
import eu.mavinci.plane.nmea.NMEA;
import gov.nasa.worldwind.geom.Position;
import io.reactivex.Emitter;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.time.Clock;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectorService implements IConnectorService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectorService.class);

    private final ConnectorIdGenerator connectorIdGenerator;
    private final IRtkStatisticFactory rtkStatisticFactory;
    private final Clock clock = Clock.systemDefaultZone();
    private final IConnectionSupplier supplier;
    private final Map<UUID, NtripConnector> connectors = new HashMap<>();
    private final Map<UUID, ScheduledFuture<?>> timeTickers = new HashMap<>();
    private final Map<UUID, Future<?>> trafficReaders = new HashMap<>();
    private final Map<UUID, ConnectorStatus> connectorsStatus = new HashMap<>();
    private final Map<UUID, Observable<Integer>> packageSources = new HashMap<>();

    public static final int MAX_CONNECTING_ATTEMPTS = 3;
    private static final int WAIT_AFTER_CONNECTING_FAIL = 10;
    private static final int CONNECT_TIMEOUT = 15 * 1000;
    private static final int READ_TIMEOUT = 5 * 1000;

    @Inject
    public ConnectorService(
            ConnectorIdGenerator connectorIdGenerator,
            IRtkStatisticFactory rtkStatisticFactory,
            IConnectionSupplier supplier) {
        this.connectorIdGenerator = connectorIdGenerator;
        this.rtkStatisticFactory = rtkStatisticFactory;
        this.supplier = supplier;
    }

    @Override
    public UUID createNtripConnector(RtkStatisticsData statisticsData, NtripConnectionSettings connectionSetting) {
        UUID uuid = connectorIdGenerator.generateNextId();

        FluentFuture<ConnectionObjects> execute =
            new RetryTask<>(
                    MAX_CONNECTING_ATTEMPTS * MAX_CONNECTING_ATTEMPTS,
                    () -> {
                        LOGGER.debug("Attempt to connect");
                        connectorsStatus.put(uuid, ConnectorStatus.CONNECTING);
                        return supplier.getNtripConnection(connectionSetting);
                    })
                .execute();

        execute.onFailure(f -> connectorsStatus.put(uuid, ConnectorStatus.FAILED))
            .onSuccess(
                conn -> {
                    connectorsStatus.put(uuid, ConnectorStatus.CONNECTED);

                    NtripConnector connector = new NtripConnector(clock);
                    IRtkStatisticListener statisticListener =
                        rtkStatisticFactory.createStatisticListener(statisticsData, connector);
                    connector.addListener(statisticListener);
                    connectors.put(uuid, connector);
                    connector.connecting();

                    ScheduledFuture<?> timeTickerFuture =
                        Dispatcher.schedule(connector.timeTickerTask(), Duration.seconds(2), Duration.seconds(2));
                    timeTickers.put(uuid, timeTickerFuture);

                    final RtcmParser rtcmParser = new RtcmParser(new StatisticEntryProvider());

                    connector.statisticEntryMap = rtcmParser.getStatistics();
                    connector.ntripParser = rtcmParser;

                    packageSources.put(
                        uuid,
                        Observable.<Integer>create(
                                emitter -> {
                                    readPackagesFromNetwork(connector, rtcmParser, conn, emitter);
                                    emitter.onComplete();
                                })
                            .subscribeOn(Schedulers.single()));
                });

        return uuid;
    }

    private void readPackagesFromNetwork(
            NtripConnector connector,
            RtcmParser rtcmParser,
            ConnectionObjects connection,
            Emitter<Integer> lengthEmitter) {
        int connectionAttempts = 0;
        try {
            connectionAttempts = 0;

            long lastGgaSent = 0;

            Position lastPosWGS84 = connector.getLastPosWGS84();
            if (lastPosWGS84 != null) {
                lastGgaSent = System.currentTimeMillis();
                LOGGER.debug("NTRIP-GGA sent {} -> {}", lastPosWGS84, NMEA.createGPGGA(lastPosWGS84));
                String toSend = "Ntrip-GGA: " + NMEA.createGPGGA(lastPosWGS84) + "\r\n";
                connection.print(toSend);
                connection.flush();
            }

            byte[] buffRaw = new byte[RtcmParser.inBufferSize];
            int buffRawLevel = 0;
            rtcmParser.resetReconnect();

            boolean headerDone = connection.expectHeader();
            boolean chunkedEncoding = false;
            long lastRead = System.currentTimeMillis();
            while (connector.isConnected()) {
                if (buffRawLevel > buffRaw.length - 100) {
                    buffRawLevel = 0;
                }

                int len;
                try {
                    int min = Math.min(400, buffRaw.length - buffRawLevel);
                    len = connection.read(buffRaw, buffRawLevel, min);
                } catch (SocketTimeoutException e) {
                    LOGGER.debug("Connection setTimeout", e);
                    len = 0;
                }

                LOGGER.debug("Read {} bytes from network", len);
                lengthEmitter.onNext(len);

                if (connector.isConnected()) {
                    if (len <= 0) {
                        if (lastRead + 20 * 1000 < System.currentTimeMillis()) {
                            LOGGER.debug("Last read setTimeout");
                            throw new IOException(
                                "NTRIP source provided no RTCM data within "
                                    + (20 * 1000 / 1000)
                                    + " seconds, disconnecting!");
                        }
                    } else {
                        lastRead = System.currentTimeMillis();
                        buffRawLevel += len;
                    }

                    if (!headerDone) {
                        String bufStr = new String(buffRaw, 0, buffRawLevel);
                        LOGGER.debug("Received data from NTRIP station {}", bufStr);

                        int pos = bufStr.indexOf("\r\n\r\n");
                        if (pos < 0) {
                            pos = bufStr.indexOf("\n\n");
                        }

                        if (pos >= 0) {
                            headerDone = true;
                            LOGGER.info("NTRIP response header: {}", bufStr.substring(0, pos));
                            int k = 0;
                            for (int j = pos + 4; j < buffRawLevel; j++) {
                                buffRaw[k] = buffRaw[j];
                                k++;
                            }

                            buffRawLevel = k;

                            bufStr = bufStr.toLowerCase().replaceAll(" ", "");
                            pos = bufStr.indexOf("transfer-encoding:chunked");
                            if (pos >= 0) {
                                LOGGER.info("chunked encoding detected");
                                chunkedEncoding = true;
                            }
                        }
                    }

                    if (lastPosWGS84 != null && (System.currentTimeMillis() - lastGgaSent >= 5 * 1000)) {
                        lastGgaSent = System.currentTimeMillis();
                        LOGGER.debug("NTRIP-GGA sent {} -> {}", lastPosWGS84, NMEA.createGPGGA(lastPosWGS84));
                        String toSend = "Ntrip-GGA: " + NMEA.createGPGGA(lastPosWGS84) + "\r\n";
                        connection.print(toSend);
                        connection.flush();
                    }

                    if (chunkedEncoding) {
                        buffRawLevel = getBuffRawLevel(buffRaw, buffRawLevel, rtcmParser);
                    } else {
                        LOGGER.debug("Submit data to Rtcm parser");
                        rtcmParser.addToBuffer(buffRaw, 0, buffRawLevel);
                        buffRawLevel = 0;
                    }
                }
            }
        } catch (Exception e) {
            connectionAttempts++;
            if (connector.isConnected()) {
                LOGGER.debug("NTRIP connecting problems.... trying reconnect no." + connectionAttempts + "!!", e);
            } else {
                return;
            }

            for (int i = WAIT_AFTER_CONNECTING_FAIL; i > 0; i--) {
                for (IRtkStatisticListener listener : new ArrayList<IRtkStatisticListener>()) {
                    listener.connectionStateChanged(NtripConnectionState.waitingReconnect, i);
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            }
        }
    }

    private int getBuffRawLevel(byte[] buffRaw, int buffRawLevel, RtcmParser rtcmParser) {
        while (true) {
            if (buffRawLevel < 6) {
                break;
            }

            int i = 1;
            for (; i < 5; i++) {
                if (i + 1 >= buffRawLevel) {
                    break;
                }

                if (buffRaw[i] != '\r') {
                    continue;
                }

                if (buffRaw[i + 1] != '\n') {
                    continue;
                }

                break;
            }

            String hexLen = new String(buffRaw, 0, i);
            int lenChunk = Integer.parseInt(hexLen, 16);
            if (i + lenChunk + 4 > buffRawLevel) {
                break;
            }

            rtcmParser.addToBuffer(buffRaw, i + 2, lenChunk);
            int k = 0;
            for (int n = i + lenChunk + 4; n < buffRawLevel; n++) {
                buffRaw[k] = buffRaw[n];
                k++;
            }

            buffRawLevel = k;
        }

        return buffRawLevel;
    }

    @Override
    public void closeConnector(UUID connectorId) {
        NtripConnector connector = connectors.remove(connectorId);
        if (connector != null) {
            connector.disconnect();
            packageSources.remove(connectorId);
            ScheduledFuture<?> timeTicker = timeTickers.remove(connectorId);
            timeTicker.cancel(true);
        }
    }

    @Override
    public Observable<Integer> getPackageSourceFor(UUID connectorId) {
        return packageSources.getOrDefault(connectorId, Observable.empty());
    }

    public Map<UUID, ConnectorStatus> getConnectorsStatus() {
        return connectorsStatus;
    }

    private static class RetryTask<T> {

        private final int maxOfRetries;
        private final Callable<T> task;
        private int retriesCounter;
        private final CompletableFuture<T> future = new CompletableFuture<>();

        RetryTask(int maxOfRetries, Callable<T> task) {
            this.maxOfRetries = maxOfRetries;
            this.task = task;
        }

        public FluentFuture<T> execute() {
            return Dispatcher.post(task)
                .onSuccess(
                    value -> {
                        LOGGER.debug("Connected to {}", value);
                        future.complete(value);
                    })
                .onFailure(
                    exception -> {
                        LOGGER.trace("Can't connect because of", exception);
                        retriesCounter++;
                        LOGGER.debug("{} reconnection attempt", retriesCounter);
                        if (retriesCounter >= maxOfRetries) {
                            future.completeExceptionally(exception);
                        } else {
                            execute();
                        }
                    });
        }
    }
}
