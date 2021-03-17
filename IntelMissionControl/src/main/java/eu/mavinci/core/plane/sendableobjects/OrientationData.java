/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.plane.sendableobjects;

import eu.mavinci.core.plane.PlaneConstants;

public class OrientationData extends MObject {
    /** */
    private static final long serialVersionUID = -2554643986210368625L;

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

    // ! altitude in cm
    public double altitude;

    @Deprecated
    public int vario; // ! vario in cm

    /** if true, this package is not normally reveiced, but only avaliable for backward compatibility */
    public boolean synthezided = false;

    /** manual set servo values */
    public MVector<Integer> manualServos;

    public OrientationData() {
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
