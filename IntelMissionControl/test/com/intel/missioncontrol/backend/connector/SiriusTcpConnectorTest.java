/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.backend.connector;

import com.intel.missioncontrol.backend.IntegrationTests;
import eu.mavinci.plane.Airplane;
import eu.mavinci.core.plane.AirplaneConnectorState;
import eu.mavinci.plane.tcp.AirplaneTCPconnector;
import eu.mavinci.core.plane.tcp.CAirplaneTCPconnector;
import eu.mavinci.core.plane.tcp.TCPConnection;
import eu.mavinci.test.rules.GuiceInitializer;
import eu.mavinci.test.rules.MavinciInitializer;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.concurrent.CountDownLatch;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@Category(IntegrationTests.class)
public class SiriusTcpConnectorTest {
    @ClassRule
    public static final MavinciInitializer MAVINCI_INITIALIZER = new MavinciInitializer();
    @ClassRule
    public static final GuiceInitializer GUICE_INITIALIZER = new GuiceInitializer();

    @Test
    @Ignore("because of not stable network")
    public void connectToSiriusSimulation_overTcpToSimulationServer() throws Exception {
        BlockingConnectionHandler handler = new BlockingConnectionHandler();
        Airplane plane = new Airplane(null, null);
        AirplaneTCPconnector connector = new AirplaneTCPconnector(plane, handler, CAirplaneTCPconnector.DEFAULT_PORT_WAITER);

        TCPConnection connection = new TCPConnection("sim.mavinci.de", 9000, "UDP://localhost:9070");

        connector.socketConnect(connection);

        handler.waitForConnectedTcp();
        assertThat(connector.getConnectionState(), is(AirplaneConnectorState.connectedTCP));

        handler.waitForPortListReceived();
        assertThat(connector.getConnectionState(), is(AirplaneConnectorState.portlistReceived));

        handler.waitForConnectingDevice();
        assertThat(connector.getConnectionState(), is(AirplaneConnectorState.connectingDevice));

        handler.waitForFullyConnected();
        assertThat(connector.getConnectionState(), is(AirplaneConnectorState.fullyConnected));
    }

    private static class BlockingConnectionHandler implements CAirplaneTCPconnector.ConnectionHandler {
        final CountDownLatch onTcpConnectingEvent = new CountDownLatch(1);
        final CountDownLatch onTcpConnectedEvent = new CountDownLatch(1);
        final CountDownLatch onPortListReceivedEvent = new CountDownLatch(1);
        final CountDownLatch onConnectingDeviceEvent = new CountDownLatch(1);
        final CountDownLatch onFullyConnectedEvent = new CountDownLatch(1);

        void waitForConnectedTcp() throws InterruptedException {
            onTcpConnectedEvent.await();
        }

        @Override
        public void onConnectedTcp() {
            onTcpConnectedEvent.countDown();
        }

        void waitForPortListReceived() throws InterruptedException {
            onPortListReceivedEvent.await();
        }

        @Override
        public void onPortListReceived() {
            onPortListReceivedEvent.countDown();
        }

        void waitForConnectingDevice() throws InterruptedException {
            onConnectingDeviceEvent.await();
        }

        @Override
        public void onConnectingDevice() {
            onConnectingDeviceEvent.countDown();
        }

        void waitForFullyConnected() throws InterruptedException {
            onFullyConnectedEvent.await();
        }

        @Override
        public void onFullyConnected() {
            onFullyConnectedEvent.countDown();
        }
    }
}
