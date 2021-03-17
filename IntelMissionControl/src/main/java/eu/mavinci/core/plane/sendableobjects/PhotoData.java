/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.plane.sendableobjects;

import eu.mavinci.core.flightplan.GPSFixType;
import eu.mavinci.core.flightplan.GPSFixType;

public class PhotoData extends MObject {
    /** */
    private static final long serialVersionUID = 78366450243012948L;

    /** Number of this Picture */
    public int number = 0;

    /** Time stamp seconds. */
    public int time_sec = 0;

    /** Time stamp microseconds. */
    public int time_usec = 0;

    /** Roll angle in degree */
    public float camera_roll = (float)0.0;

    /** Pitch angle in degree */
    public float camera_pitch = (float)0.0;

    /** Yaw angle in degree */
    public float camera_yaw = (float)0.0;

    /** Roll angle in degree */
    public float plane_roll = (float)0.0;

    /** Pitch angle in degree */
    public float plane_pitch = (float)0.0;

    /** Yaw angle in degree */
    public float plane_yaw = (float)0.0;

    /** Altitude above starting point in cm. */
    public float alt = 0;

    /** Latitude of last GPS fix. */
    public double lat = 0.0F;

    /** Longitude of last GPS fix. */
    public double lon = 0.0F;

    /** Focal length in this picture in mm relative to kleinbildformat CCD. */
    @Deprecated
    public int focalLength = 0;

    /** Roll angular velocity in degree/sec */
    public float gyroroll = (float)0.0;

    /** Pitch angular velocity in degree/sec */
    public float gyropitch = (float)0.0;

    /** Yaw angular velocity in degree/sec */
    public float gyroyaw = (float)0.0;

    /** the current reentrypoint of the flightplan this can be used as a kind of linenumber */
    public int reentrypoint = -1;

    /** time in ms since last gps fix was received */
    public int time_since_last_fix = 0;

    /** groundspeed by GPS in cm/sec values below or equal -100000 are handled as undefined */
    public int groundspeed = -1000000;

    /** GPS heading in degree values below or equal -100000 are handled as undefined */
    public float heading = -1000000;

    /** this number is equivalent to X in $PHOTOX in the photolog files */
    public int type = 1;

    /** GPS Ellispoid offset in cm <=-100000 => Undefined */
    public float gps_ellipsoid = -100000;

    /** GPS altitude in cm <=-100000 => Undefined */
    public float gps_alt = -100000;

    /** @see GPSFixType */
    public int gps_mode = 1;

    /**
     * in seconds
     *
     * @return
     */
    public double getTimestamp() {
        return time_sec + time_usec / 1.e6;
    }

}
