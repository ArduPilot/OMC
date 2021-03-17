/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.plane.sendableobjects;

import eu.mavinci.core.plane.AirplaneFlightmode;
import eu.mavinci.core.plane.AirplaneFlightphase;

public class PositionData extends MObject {
    /** */
    private static final long serialVersionUID = -6617469230179426261L;

    /** UNIX-Time stamp seconds with UTC-Time */
    public int time_sec = 0;

    /** Time stamp microseconds. */
    public int time_usec = 0;

    /** Altitude above starting point in cm. */
    public int altitude = 0;

    /** Latitude of last GPS fix. */
    public double lat = 0.0F;

    /** Longitude of last GPS fix. */
    public double lon = 0.0F;

    /** Ground speed (GPS) in cm/sec. */
    public int groundspeed = 0;

    /** Cross track error in cm */
    public int cross_track_error = 0;

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

    /** Temperature of air pressure sensor in 1/10th degrees C. */
    @Deprecated
    public int temperature = 0;

    /** GPS Altitude above sea level in cm. */
    public int gpsAltitude = 0;

    /** last reached reentry point */
    public int reentrypoint = 0;

    /** if true, this positions are extrapolated from last fix */
    public boolean gpsLossFallback = false;

    /** if true, this package is not normally reveiced, but only avaliable for backward compatibility */
    public boolean synthezided = false;

    /**
     * forwardDirection in manned navigation edition means that plane is going along the current flight line in forward
     * direction and otherwise
     */
    public boolean forwardDirection = true;

    /**
     * in seconds
     *
     * @return
     */
    public double getTimestamp() {
        return time_sec + time_usec / 1.e6;
    }
}
