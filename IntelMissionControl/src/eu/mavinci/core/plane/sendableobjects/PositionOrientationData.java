/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.plane.sendableobjects;

import eu.mavinci.core.plane.AirplaneFlightmode;
import eu.mavinci.core.plane.AirplaneFlightphase;
import eu.mavinci.core.plane.PlaneConstants;

public class PositionOrientationData extends MObject {

    private static final long serialVersionUID = 1817721073857270090L;

    /** UNIX-Time stamp seconds with UTC-Time */
    public int time_sec = 0;

    /** Time stamp microseconds. */
    public int time_usec = 0;

    /** Altitude above starting point in cm. */
    public float altitude = 0;

    /** Latitude of last GPS fix. */
    public double lat = 0.0F;

    /** Longitude of last GPS fix. */
    public double lon = 0.0F;

    /** Roll angle in degree */
    public double roll = 0.0;

    /** Pitch angle in degree */
    public double pitch = 0.0;

    /** Yaw angle in degree */
    public double yaw = 0.0;

    /** Roll angle of Camera in degree */
    public double cameraRoll = PlaneConstants.UNDEFINED_ANGLE;

    /** Pitch angle of Camera in degree */
    public double cameraPitch = PlaneConstants.UNDEFINED_ANGLE;

    /** Yaw angle of Camera in degree */
    public double cameraYaw = PlaneConstants.UNDEFINED_ANGLE;

    /**
     * flightmode: 2=AssistedFlying, 1=manual control, 0: automatic flight.
     *
     * @see AirplaneFlightmode
     */
    public int flightmode = 0;

    /**
     * flightphase: 0: ground, 1: takeoff, 2: airborne, ...
     *
     * @see AirplaneFlightphase
     */
    public int flightphase = 0;

    /** last reached reentry point */
    public int reentrypoint = 0;

    /** main battery level in V */
    public float batteryVoltage = 0;

    /** main battery level in % */
    public float batteryPercent = 0;

    /** if true, this positions are extrapolated from last fix */
    public boolean gpsLossFallback = false;

    private PositionData posData = null;

    /** Flight time elapsed in sec */
    public long elapsed_time = 0;

    public PositionData getPositionData(DebugData d) {
        if (posData == null) {
            posData = new PositionData();
            posData.time_sec = time_sec;
            posData.time_usec = time_usec;
            posData.altitude = (int)altitude;
            posData.gpsAltitude = (int)altitude;
            posData.flightmode = flightmode;
            posData.flightphase = flightphase;
            posData.lat = lat;
            posData.lon = lon;
            posData.reentrypoint = reentrypoint;
            posData.synthezided = true;
            posData.gpsLossFallback = gpsLossFallback;
            if (d != null) {
                posData.cross_track_error = d.cross_track_error;
                posData.gpsAltitude = Math.round(d.gpsAltitude);
                posData.groundspeed = d.groundspeed;
            }
        }
        return posData;
    }

    private OrientationData orData = null;

    public OrientationData getOrientationData(DebugData d) {
        if (orData == null) {
            orData = new OrientationData();
            orData.altitude = (int)altitude;
            orData.roll = roll;
            orData.pitch = pitch;
            orData.yaw = yaw;
            orData.cameraRoll = cameraRoll;
            orData.cameraPitch = cameraPitch;
            orData.cameraYaw = cameraYaw;
            orData.synthezided = true;
            if (d != null) {
                orData.accl = d.accl;
                orData.ausage = d.ausage;
                orData.dtmax = d.dtmax;
                orData.dtmin = d.dtmin;
                orData.mainlooprate = d.mainlooprate;
                orData.manualServos = d.manualServos;
                orData.setpointPitch = d.setpointPitch;
                orData.setpointRoll = d.setpointRoll;
                orData.setpointYaw = d.setpointYaw;
            }
        }

        return orData;
    }

    /**
     * in seconds
     *
     * @return
     */
    public double getTimestamp() {
        return time_sec + time_usec / 1.e6;
    }
}
