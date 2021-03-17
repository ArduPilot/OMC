/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.connector;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import eu.mavinci.desktop.gui.doublepanel.ntripclient.NtripConnectionState;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class NtripConnectorTest {
    private NtripConnector connector;
    private IRtkStatisticListenerSpy listenerSpy;
    private Instant connectedInstant;
    private Instant disconnectInstant;

    @Before
    public void setUp() throws Exception {
        listenerSpy = new IRtkStatisticListenerSpy();

        connectedInstant = Instant.ofEpochSecond(100);
        disconnectInstant = Instant.ofEpochSecond(200);
        ClockStub clockStub = new ClockStub(connectedInstant, disconnectInstant);
        connector = new NtripConnector(clockStub);
        connector.addListener(listenerSpy);
    }

    @Test
    @Ignore
    public void sendConnectingStatus_afterConnecting() throws Exception {
        connector.connecting();
        assertThat(listenerSpy.conState, is(NtripConnectionState.connecting));
    }

    @Test
    @Ignore
    public void sendUnconnectedStatus_afterDisconnect() throws Exception {
        connector.connecting();
        connector.disconnect();
        assertThat(listenerSpy.conState, is(NtripConnectionState.unconnected));
    }

    @Test
    @Ignore
    public void connectTimeIsSet_afterConnecting() throws Exception {
        assertThat(connector.getConnectTime(), is(-1L));

        connector.connecting();

        assertThat(connector.getConnectTime(), is(connectedInstant.toEpochMilli()));
    }

    @Test
    @Ignore
    public void connectedFlagIsSet_afterConnecting() throws Exception {
        assertThat(connector.isConnected(), is(false));

        connector.connecting();

        assertThat(connector.isConnected(), is(true));
    }

    @Test
    @Ignore
    public void disconnectTimeIsSet_whenConnectorDisconnect() throws Exception {
        connector.connecting();
        assertThat(connector.getDisconnectTime(), is(-1L));

        connector.disconnect();
        assertThat(connector.getDisconnectTime(), is(disconnectInstant.toEpochMilli()));
    }

    private static class ClockStub extends Clock {
        final Instant connectTime;
        private final Instant disconnectTime;
        int invocationCounter;

        ClockStub(Instant connectTime, Instant disconnectTime) {
            this.connectTime = connectTime;
            this.disconnectTime = disconnectTime;
        }

        @Override
        public ZoneId getZone() {
            return null;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return null;
        }

        @Override
        public Instant instant() {
            invocationCounter += 1;
            if ((invocationCounter - 1) % 2 == 0) {
                return connectTime;
            } else {
                return disconnectTime;
            }
        }
    }
}
