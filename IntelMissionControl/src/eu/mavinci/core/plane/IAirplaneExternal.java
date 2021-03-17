/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.plane;

import eu.mavinci.core.plane.sendableobjects.AndroidState;
import eu.mavinci.core.plane.sendableobjects.Config_variables;
import eu.mavinci.core.plane.sendableobjects.SimulationSettings;
import eu.mavinci.core.plane.sendableobjects.AndroidState;
import eu.mavinci.core.plane.sendableobjects.Config_variables;
import eu.mavinci.core.plane.sendableobjects.SimulationSettings;

import java.util.Vector;

public interface IAirplaneExternal {

    /** Receive connectionInformation */
    public boolean isWriteable();

    /** Receive connectionInformation */
    public boolean isReadable();

    /** Close this connection and clean up */
    public void close();

    /**
     * Return the connected Port in the Backend
     *
     * @return anObject what describes the Port. The toString() Function should be implemented
     */
    public Object getPlanePort();

    /**
     * Connect to airplane.
     *
     * @see requestPorts()
     * @param Port One of the available ports.
     */
    public void connect(String port);

    /** mavinci protocol version check */
    public void mavinci(Integer version);

    // /** Disconnect from airplane.
    // *
    // */
    // public void disconnect();

    /** Request backend information */
    public void requestBackend();

    /** Request if it is a simulation */
    public void requestIsSimulation();

    /** request the PlaneInfo Data structure with version information of the airplane */
    public void requestPlaneInfo();

    /** Request configuration from airplane. The configuration data is send to the config handler. */
    public void requestConfig();

    /**
     * Set configuration data.
     *
     * @param c The configuration data is sent to the airplane.
     */
    public void setConfig(Config_variables c);

    /** Tell the airplane to shutdown. This is neccessary before power off. */
    @Deprecated
    public void shutdown();

    /**
     * Tell the airplane to write its config to the internal flash memory. If not written to the flash memory, the
     * configuration will be lost on power down. Does only work if the plane is on the ground.
     */
    public void saveConfig();

    /** flight phase. If you want to read flight phase, connect to the position event. */
    public void setFlightPhase(AirplaneFlightphase p);

    /**
     * Request flight phase from airplane.
     *
     * <p>The flight phase data is send to the handler.
     */
    public void requestFlightPhase();

    /**
     * Set flight plan ASM Code. If compiling is successful, the binary code will be sent to the airplane for execution.
     *
     * @param plan ASM Code of the Flightplan.
     * @param entrypoint If set to -1, the airplane tries to reenter the program (after a change). Sending binary diffs
     *     might be implemented in the future for this mode. A positive value means that the airplane should start at
     *     that id. Zero means that the airplane starts the flightplan from beginning. Use with caution.
     * @return Returns true on successful compilation.
     */
    public void setFlightPlanASM(String plan, Integer entrypoint);

    /**
     * Set flight plan XML Code. If compiling is successful, the binary code will be sent to the airplane for execution.
     *
     * @param plan XLM Code of the Flightplan.
     * @param entrypoint If set to -1, the airplane tries to reenter the program (after a change). Sending binary diffs
     *     might be implemented in the future for this mode. A positive value means that the airplane should start at
     *     that id. Zero means that the airplane starts the flightplan from beginning. Use with caution.
     * @return Returns true on successful compilation.
     */
    public void setFlightPlanXML(String plan, Integer entrypoint);

    /** Request the current fligplan loadet into the airplane in XML */
    public void requestFlightPlanXML();

    /** Request the current fligplan loadet into the airplane in Asembler */
    public void requestFlightPlanASM();

    /** Request start position from airplane. /* This is the position where the barometer was set to zero. */
    public void requestStartpos();

    /**
     * Set the current speed of the simulation. speed > 1 => fast| speed <1 slow | speed ==-1 => as faast as possible!
     *
     * @param speed
     */
    public void setSimulationSpeed(Float speed);

    /** @param settings */
    public void setSimulationSettings(SimulationSettings settings);

    public void requestSimulationSettings();

    /** Request the current speed of the simulation */
    public void requestSimulationSpeed();

    /**
     * Set start position (of simulator)
     *
     * @param lon
     * @param lat
     */
    public void setStartpos(Double lon, Double lat);

    /**
     * Update firmware. EXPERT function. Send TAR.GZ file to Airplane via FTP and than send its path via this function
     * Works only if plane is on the ground. Send new binary firmware file and reload firmware. Use with caution.
     */
    public void expertUpdateFirmware(String path);

    /**
     * Update Backend. EXPERT function. Send new rpm file to update backend service (via FTP) and than call this functio
     * with the path to this file and restart service. Use with caution.
     */
    public void expertUpdateBackend(String path);

    /**
     * Recalibrate Gyro sensors. EXPERT function. Calibration takes several seconds, plane must not be moved during that
     * time. Note: if it is moving during calibration, it will crash due to wrong IMU estimations. Use with caution.
     * Calibration is cached on internal Flash.
     */
    public void expertRecalibrate();

    /**
     * Trim servos ON. EXPERT function. All servos are set to the middle position. Trim data is part of the config data.
     * Should only be needed once.
     */
    public void expertTrimOn();

    /**
     * Trim servos OFF. EXPERT function. All servos are set to the middle position. Trim data is part of the config
     * data. Should only be needed once.
     */
    public void expertTrimOff();

    /**
     * set a bitmask which enables single system fails. for masks @see IAirplaneListenerExpertSimulatedFails
     *
     * @param failBitMask if 0, all fails are disabled -> everything allright!
     * @param debug1 for future use
     * @param debug2 for future use
     * @param debug3 for future use
     */
    public void expertSendSimulatedFails(Integer failBitMask, Integer debug1, Integer debug2, Integer debug3);

    public void expertRequestSimulatedFails();

    /** Recalibrate Magneto sensors. EXPERT function. */
    public void expertRecalibrateCompassStart();

    public void expertRecalibrateCompassStop();

    /** Turn Realtime scheduling on. DEBUG function. */
    public void dbgRTon();

    /** Turn Realtime scheduling off. DEBUG function. */
    public void dbgRToff();

    /** Quits autopilot application. For debugging process only. DEBUG only! */
    public void dbgExitAutopilot();

    /** Reset internal debug variables. For debugging process only. DEBUG only! */
    public void dbgResetDebug();

    /** Ask for airplane name */
    public void requestAirplaneName();

    /**
     * Set Airplane Name
     *
     * @param newName
     */
    public void setAirplaneName(String newName);

    /**
     * Set a fixed airplane orientation
     *
     * @param roll
     * @param pitch
     * @param yaw
     */
    public void setFixedOrientation(Float roll, Float pitch, Float yaw);

    /** Request a fixed airplane orientation */
    public void requestFixedOrientation();

    /** universal debug commands */
    public void dbgCommand0();

    public void dbgCommand1();

    public void dbgCommand2();

    public void dbgCommand3();

    public void dbgCommand4();

    public void dbgCommand5();

    public void dbgCommand6();

    public void dbgCommand7();

    public void dbgCommand8();

    public void dbgCommand9();

    /** reset GNSS module */
    public void clearNVRAM();

    /** Set an new state for the Backup Pilot Support device and broadcast this to all listeners at the backend */
    public void updateAndroidState(AndroidState state);

    // ! File transfer functions
    public void requestDirListing(String path);

    public void getFile(String path);

    public void sendFile(String path);

    public void deleteFile(String path);

    public void makeDir(String path);

    public void writeToFlash(String path);

    // ! cancel current transfers
    public void cancelSending(String path);

    public void cancelReceiving(String path);

    /**
     * Triggering backend to send local licence file to Topcon GPS
     *
     * @param path
     */
    public void expertUpdateBackendTopconOAF(String path);

    /**
     * Triggering autopilot to send local licence file to Topcon GPS
     *
     * @param path
     */
    public void expertUpdateFirmwareTopconOAF(String path);

    /**
     * sending @PlaneConstants.MANUAL_SERVO_COUNT integer values between 0 and 255 to the UAV (most likely only used in
     * simulations)
     *
     * @param manualServos
     */
    public void setManualServos(Vector<Integer> manualServos);
}
