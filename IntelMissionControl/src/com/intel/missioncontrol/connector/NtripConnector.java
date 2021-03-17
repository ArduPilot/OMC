/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.connector;

import eu.mavinci.desktop.gui.doublepanel.ntripclient.IRtkStatisticListener;
import eu.mavinci.desktop.gui.doublepanel.ntripclient.NtripConnectionState;
import eu.mavinci.desktop.gui.doublepanel.ntripclient.RtcmParser.StatisticEntry;
import eu.mavinci.desktop.gui.doublepanel.ntripclient.RtkClient;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NtripConnector extends RtkClient {

    private final Clock clock;
    volatile Map<Integer, StatisticEntry> statisticEntryMap;
    private List<IRtkStatisticListener> listeners = new ArrayList<>();

    public NtripConnector(Clock clock) {
        super(null);
        this.clock = clock;
    }

    @Override
    public String toString() {
        return "NTRIP CONNECTOR";
    }

    public void connecting() {
        if (isConnected()) {
            return;
        }

        isConnected = true;
        connectTime = clock.millis();

        for (IRtkStatisticListener listener : listeners) {
            listener.connectionStateChanged(NtripConnectionState.connecting, -1);
        }
    }

    @Override
    public void disconnect() {
        boolean wasConnected = isConnected;
        isConnected = false;
        for (IRtkStatisticListener listener : listeners) {
            listener.connectionStateChanged(NtripConnectionState.unconnected, -1);
        }

        if (wasConnected) {
            disconnectTime = clock.millis();
        }

        sentTimerTick();

        if (!wasConnected) {
            return;
        }

        try {
            connection.close();
        } catch (Exception e) {
        }

        connection = null;
    }

    @Override
    public Map<Integer, StatisticEntry> getStatistics() {
        return statisticEntryMap;
    }

    public Runnable timeTickerTask() {
        return runnableSetTimeTick;
    }

    public long getConnectTime() {
        return connectTime;
    }

    public long getDisconnectTime() {
        return disconnectTime;
    }

    @Override
    public void addListener(IRtkStatisticListener l) {
        listeners.add(l);
        super.addListener(l);
    }
}
