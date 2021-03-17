/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.connection;

import eu.mavinci.core.plane.AirplaneType;
import eu.mavinci.core.plane.management.BackendState;
import eu.mavinci.core.plane.management.CAirport;
import eu.mavinci.core.plane.sendableobjects.Backend;
import eu.mavinci.core.plane.sendableobjects.BackendInfo;
import eu.mavinci.desktop.main.debug.Debug;
import eu.mavinci.desktop.rs232.MSerialPort;
import eu.mavinci.desktop.rs232.Rs232Params;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

public class DetectedUavSource implements UavSource {
    private static final Logger LOGGER = LoggerFactory.getLogger(DetectedUavSource.class);
    private static final byte[] IDL_DETECT_SEQUENCE = new byte[] {33, 35, 33, -128};

    @Override
    public List<UnmannedAerialVehicle> listUavs() {
        delayForAnimation();
        List<UnmannedAerialVehicle> uavs = new ArrayList<>();
        Vector<BackendState> backendList = CAirport.getInstance().getBackendList();
        for (BackendState backendState : backendList) {
            backendState
                .getPorts()
                .forEach(
                    port -> {
                        AirplaneType model = AirplaneType.SIRIUS_PRO;
                        Backend backend = backendState.getBackend();
                        String name = String.format("%s %s", backend.name, port.name);
                        UavInFlightInfo position = createUavInFlightInfo(backend);
                        UavInfo info = createUavInfo(backend.info);
                        UavConnectionInfo connectionInfo =
                            new UavConnectionInfo(backendState.getHost().getHostString(), backend.port, port.device);
                        uavs.add(
                            new UnmannedAerialVehicle(
                                model, name, position, info, connectionInfo, Rs232Params.conAscTec));
                    });
        }

        List<Rs232Params> rs232Params =
            Rs232Params.listDetectedPorts(
                Rs232Params.DEFAULT_PORTS_SOURCE,
                Rs232Params.DEFAULT_CONFIGURATION_PROPERTIES,
                params -> {
                    MSerialPort port = null;
                    try {
                        port = params.openPort();
                        boolean idlDetect = detectIDL(port);
                        System.out.println("IDL detected status: " + idlDetect);
                        params.setIsAscTec(idlDetect);
                        port.closePort();
                        return true;
                    } catch (IOException e) {
                        // TODO if port is not valid -- tell it to the user and ask to replug to the other port
                        return false;
                    } finally {
                        if (port != null && port.isOpened())
                            try {
                                port.closePort();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                    }
                });
        for (Rs232Params params : rs232Params) {
            if (params.isAscTec()) {
                AirplaneType model = AirplaneType.FALCON8PLUS;
                String name = "IDL connected to " + params.getPort();
                UavInFlightInfo position = new UavInFlightInfo(0, 0, 0, 0, 25, 88);
                UavInfo info = new UavInfo("SERIAL_NUMBER", 0, "HARDWARE TYPE", "SOFTWARE REVISION", 4);
                UavConnectionInfo connectionInfo = new UavConnectionInfo(params.getPort(), 1, "");
                uavs.add(new UnmannedAerialVehicle(model, name, position, info, connectionInfo, params));
            }
        }

        // no longer F8+ TCP connection
        // detectAndAddFalcon8plus(uavs);
        uavs.add(generateMavlinkStub());
        uavs.addAll(generateStubs());

        return uavs;
    }

    private UnmannedAerialVehicle generateMavlinkStub() {
        return new UnmannedAerialVehicle(
                AirplaneType.SIRIUS_BASIC,
                "MAVLink",
                new UavInFlightInfo(18.6417654, 394.987582, 100.234, 10, 219.85, 99),
                new UavInfo("serial-2", 6, "hwtype-2", "3.6.7", 3),
                new UavConnectionInfo("sim.mavinci.de", 9000, "UDP://localhost:9070"),
                Rs232Params.conAscTec);
    }

    private boolean detectIDL(MSerialPort port) {
        long startTime = System.currentTimeMillis();
        long timeout = 2000;
        try {
            byte[] bytes = null;
            while ((bytes = port.readBytes()) == null && System.currentTimeMillis() - startTime < timeout) ;
            if (bytes != null) {
                String bytesStr = new String(bytes, StandardCharsets.UTF_8);
                String IDL_DETECT_SEQUENCE_STR = new String(IDL_DETECT_SEQUENCE, StandardCharsets.UTF_8);
                int i = bytesStr.indexOf(IDL_DETECT_SEQUENCE_STR);

                return i != -1;
            } else {
                // TODO notify user
                Debug.getLog().severe("No bytes received on COM port " + port.toString() + " in 2 seconds");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    private void detectAndAddFalcon8plus(List<UnmannedAerialVehicle> uavs) {
        try {
            long finish = 0;

            AirplaneType model = AirplaneType.FALCON8PLUS;
            String name = "Falcon 8+ TCP cockpit connection";
            UavInFlightInfo position = new UavInFlightInfo(0, 0, 0, 0, 25, 88);
            UavInfo info = new UavInfo("SERIAL_NUMBER", 0, "HARDWARE TYPE", "SOFTWARE REVISION", 4);
            UavConnectionInfo connectionInfo = new UavConnectionInfo("", 1, "");
            uavs.add(new UnmannedAerialVehicle(model, name, position, info, connectionInfo, Rs232Params.conAscTec));

        } catch (Exception e) {
            System.out.println("Exception:" + e.getMessage());
        }
    }

    private List<UnmannedAerialVehicle> generateStubs() {
        return Collections.singletonList(
            new UnmannedAerialVehicle(
                AirplaneType.SIRIUS_BASIC,
                "SIRIUS_BASIC Simulation Server",
                new UavInFlightInfo(18.6417654, 394.987582, 100.234, 10, 219.85, 99),
                new UavInfo("serial-2", 6, "hwtype-2", "3.6.7", 3),
                new UavConnectionInfo("sim.mavinci.de", 9000, "UDP://localhost:9070"),
                Rs232Params.conAscTec));
    }

    private void delayForAnimation() {
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            LOGGER.error("Connection thread was interrupted", e);
            Thread.currentThread().interrupt();
        }
    }

    private UavInFlightInfo createUavInFlightInfo(Backend backend) {
        double longitude = backend.lon;
        double latitude = backend.lat;
        double altitude = backend.alt;
        // TODO find where to get number of satellites from old API
        int numberOfSatellites = 0;
        double batteryVoltage = backend.batteryVoltage;
        // TODO find how old API calculate battery percent
        int batteryPercent = 99; // even God does not give you 100%
        return new UavInFlightInfo(longitude, latitude, altitude, numberOfSatellites, batteryVoltage, batteryPercent);
    }

    private UavInfo createUavInfo(BackendInfo info) {
        return new UavInfo(
            info.serialNumber,
            info.revisionHardware,
            info.hardwareType,
            info.getHumanReadableSWversion(),
            info.protocolVersion);
    }
}
