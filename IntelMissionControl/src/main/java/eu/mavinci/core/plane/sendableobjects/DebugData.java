/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.plane.sendableobjects;

import eu.mavinci.core.plane.PlaneConstants;

public class DebugData extends MObject {

    private static final long serialVersionUID = 3937046609216032866L;

    /** Time stamp seconds. */
    public int time_sec = 0;

    /** Time stamp microseconds. */
    public int time_usec = 0;

    /**
     * in seconds
     *
     * @return
     */
    public double getTimestamp() {
        return time_sec + time_usec / 1.e6;
    }

    /**
     * Accelerometer trust This can be used to check if the gyros estimation is corrected by the accelerometer readings.
     * A low value is a sign for high vibrational noise on board the plane. Expect wrong IMU estimations if this value
     * is below a certain limit for longer periods of time.
     */
    public double ausage = 0.0;

    /** Main loop rate */
    public double mainlooprate = 0.0;

    /** Fastest main loop run */
    public double dtmin = 0.0;

    /** slowest main loop run */
    public double dtmax = 0.0;

    /** 3 x ACCL in plane system in nose, wings(left), ground-directions */
    public MVector<Double> accl;

    /** setpoint Roll angle in degree */
    public double setpointRoll = 0.0;

    /** setpoint Pitch angle in degree */
    public double setpointPitch = 0.0;

    /** setpoint Yaw angle in degree */
    public double setpointYaw = 0.0;

    /** GPS altitude in cm */
    public float gpsAltitude;

    /** groundspeed by GPS in cm/sec */
    public int groundspeed = 0;

    /** GPS heading in degree */
    public float heading = 0;

    /** Cross track error in cm */
    public int cross_track_error = 0;

    /** manual set servo values */
    public MVector<Integer> manualServos;

    /** 4bit debugging data0 */
    public int debug0;

    /** 32 bit debugging data1 */
    public int debug1;

    /** 32 bit debugging data2 */
    public int debug2;

    /** Roll angular velocity in degree/sec */
    public float gyroroll = (float)0.0;

    /** Pitch angular velocity in degree/sec */
    public float gyropitch = (float)0.0;

    /** Yaw angular velocity in degree/sec */
    public float gyroyaw = (float)0.0;

    /** time since become airborne in sec */
    public int airborneTime;

    /** total distance over ground in meter since battery on */
    public int groundDistance;

    /** time since first GPS-fix in sec. */
    public int upTime;

    /** GPS Ellispoid offset in cm <=-100000 => Undefined */
    public float gps_ellipsoid = -100000;

    public DebugData() {
        accl = new MVector<Double>(3, Double.class);
        for (int i = 0; i != 3; i++) {
            // init vectors!
            accl.add(0.);
        }

        manualServos = new MVector<Integer>(PlaneConstants.MANUAL_SERVO_COUNT, Integer.class);
        for (int i = 0; i != PlaneConstants.MANUAL_SERVO_COUNT; i++) {
            // init vectors!
            manualServos.add(0);
        }
    }

}
