/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.plane.tcp;

import eu.mavinci.core.plane.listeners.IAirplaneListenerBackendConnectionLost;
import eu.mavinci.desktop.main.debug.Debug;
import eu.mavinci.core.plane.listeners.IAirplaneListenerBackendConnectionLost;

import java.io.BufferedReader;
import java.util.logging.Level;

public class UavConnectionPortListenerImpl implements UavConnectionPortListener {
    private final CAirplaneTCPconnector.RequestProcessor requestProcessor;
    private final CAirplaneTCPconnector.ConnectionLostHandler handler;
    private final BufferedReader input;

    public UavConnectionPortListenerImpl(
            CAirplaneTCPconnector.RequestProcessor requestProcessor,
            CAirplaneTCPconnector.ConnectionLostHandler handler,
            BufferedReader input) {
        this.requestProcessor = requestProcessor;
        this.handler = handler;
        this.input = input;
    }

    @Override
    public void listen() {
        String responseLine = "";

        try {
            long startTime = System.currentTimeMillis();
            while (!input.ready()) {
                if (System.currentTimeMillis() - startTime > CAirplaneTCPconnector.INPUT_READY_TIMEOUT) {
                    throw new UavConnectionException("timeout to get input ready");
                }
            }
        } catch (Throwable e) { // catch will be processed from next try
            handler.onConnectionLost(IAirplaneListenerBackendConnectionLost.ConnectionLostReasons.TIMEOUT, e);
            return;
        }

        try {
            // while (!server.isClosed()) {
            boolean skipNext = false;
            while (true) {
                responseLine = input.readLine();
                Debug.getLog().info("data from uav - " + responseLine);
                if (skipNext) {
                    skipNext = false;
                    continue;
                }

                if (responseLine == null) {
                    throw new UavConnectionException("Unable to read from socket");
                }
                // System.out.println(responseLine);
                try {
                    if (responseLine.length() == CAirplaneTCPconnector.INPUT_BUFFER_SIZE) {
                        skipNext = true;
                        throw new UavConnectionException("TCP input Buffer overflow");
                    }

                    requestProcessor.process(responseLine);
                } catch (Exception e) {
                    boolean isLinkProblem = false;
                    try {
                        isLinkProblem = responseLine.substring(1).contains("#");
                    } catch (Exception e2) {
                    }

                    Debug.getLog()
                        .log(isLinkProblem ? Debug.WARNING : Level.WARNING, "error handling:" + responseLine, e);
                    // e.printStackTrace();
                }
            }
        } catch (Throwable e) {
            handler.onConnectionLost(
                IAirplaneListenerBackendConnectionLost.ConnectionLostReasons.TCP_CONNECTION_LOST, e);
        }
    }

    private static class UavConnectionException extends RuntimeException {
        public UavConnectionException(String message) {
            super(message);
        }
    }
}
