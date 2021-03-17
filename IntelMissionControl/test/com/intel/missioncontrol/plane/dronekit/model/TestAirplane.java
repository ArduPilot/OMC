/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.plane.dronekit.model;

import com.intel.missioncontrol.beans.property.AsyncObjectProperty;
import com.intel.missioncontrol.hardware.IHardwareConfiguration;
import eu.mavinci.core.flightplan.CPicArea;
import eu.mavinci.core.flightplan.PlanType;
import eu.mavinci.core.plane.CAirplane;
import eu.mavinci.core.plane.listeners.AirplaneListenerDelegator;
import eu.mavinci.desktop.gui.doublepanel.manualcontrol.JoystickReader;
import eu.mavinci.desktop.gui.doublepanel.ntripclient.RtkClient;
import eu.mavinci.flightplan.FlightplanManager;
import eu.mavinci.plane.AirplaneCache;
import eu.mavinci.plane.FTPManager;
import eu.mavinci.plane.IAirplane;
import eu.mavinci.plane.WindEstimate;
import eu.mavinci.plane.logfile.ALogWriter;
import java.io.File;
import java.nio.file.Path;

public class TestAirplane extends CAirplane implements IAirplane {
    AirplaneCache cache;
    public TestAirplane() {
        super();
        fpManager = new FlightplanManager(this);
        rootHandler = new AirplaneListenerDelegator();
        cache = new AirplaneCache(this);
    }

    @Override
    public FlightplanManager getFPmanager() {
        return (FlightplanManager)super.getFPmanager();
    }


    @Override
    public AirplaneCache getAirplaneCache() {
        return cache;
    }

    @Override
    public IHardwareConfiguration getHardwareConfiguration() {
        return super.getNativeHardwareConfiguration();
    }

    @Override
    public CPicArea getPicAreaTemplate(PlanType type) {
        return null;
    }

    @Override
    public ALogWriter getFlgLogWriter() {
        return null;
    }

    @Override
    public ALogWriter getTCPLogWriter() {
        return null;
    }

    @Override
    public FTPManager getFTPManager() {
        return null;
    }

    @Override
    public void fireLoggingChangedTCP(boolean is_logging_tcp) {

    }

    @Override
    public void fireLoggingChangedFLG(boolean is_logging_flg) {

    }

    @Override
    public WindEstimate getWindEstimate() {
        return null;
    }

    @Override
    public RtkClient getRtkClient() {
        return null;
    }

    @Override
    public JoystickReader getJoystickReader() {
        return null;
    }

    @Override
    public void disconnectSilently() {

    }

    @Override
    public void setHardwareConfiguration(IHardwareConfiguration hardwareConfiguration) {

    }

    @Override
    public File getMatchingsFolder() {
        System.err.println("----------- getMatchingsFolder");
        return null;
    }

    @Override
    public File getFlightplanAutosaveFolder() {
        System.err.println("----------- getFlightplanAutosaveFolder");

        return null;
    }

    @Override
    public File getFTPFolder() {
        System.err.println("----------- getFTPFolder");

        return null;
    }

    @Override
    public File getBaseFolder() {
        System.err.println("----------- getBaseFolder");

        return null;
    }

    @Override
    public File getLogFolder() {
        System.err.println("----------- getLogFolder");

        return null;
    }

    @Override
    public AsyncObjectProperty<Path> baseFolderProperty() {
        System.err.println("----------- baseFolderProperty");

        return null;
    }
}
