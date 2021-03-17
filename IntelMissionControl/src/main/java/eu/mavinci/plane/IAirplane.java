/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.plane;

import org.asyncfx.beans.property.AsyncObjectProperty;
import eu.mavinci.core.flightplan.CPicArea;
import eu.mavinci.core.flightplan.PlanType;
import com.intel.missioncontrol.hardware.IHardwareConfiguration;
import eu.mavinci.core.plane.ICAirplane;
import eu.mavinci.flightplan.FlightplanManager;
import eu.mavinci.plane.logfile.ALogWriter;
import java.io.File;
import java.nio.file.Path;

public interface IAirplane extends ICAirplane {

    @Override
    public AirplaneCache getAirplaneCache();

    @Override
    public FlightplanManager getFPmanager();

    /** @return configuration of the platform from the current flight plane */
    public IHardwareConfiguration getHardwareConfiguration();

    /** @return description of the current flight plane */
    public CPicArea getPicAreaTemplate(PlanType type);

    // public void setSession(AirplaneSession session);

    /**
     * get the log writer for the airplane log
     *
     * @return
     */
    public ALogWriter getFlgLogWriter();

    /**
     * get the logwriter for TCP messages logging
     *
     * @return
     */
    public ALogWriter getTCPLogWriter();

    /**
     * are we currently logging to a file or not
     *
     * @param is_logging_tcp
     */
    public void fireLoggingChangedTCP(boolean is_logging_tcp);

    /**
     * are we currently logging to a file or not
     *
     * @param is_logging_flg
     */
    public void fireLoggingChangedFLG(boolean is_logging_flg);

    public WindEstimate getWindEstimate();

    public void disconnectSilently();

    void setHardwareConfiguration(IHardwareConfiguration hardwareConfiguration);

    void cancelLaunch();

    void cancelLanding();

    File getMatchingsFolder();

    File getFlightplanAutosaveFolder();

    File getFTPFolder();

    File getBaseFolder();

    File getLogFolder();

    AsyncObjectProperty<Path> baseFolderProperty();
}
