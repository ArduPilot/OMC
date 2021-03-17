/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.plane.sendableobjects;

public class AndroidState extends MObject {
    /** */
    private static final long serialVersionUID = 2579594126747320366L;
    /** GPS Position of the Backup Pilote */
    public double lon = 0;

    public double lat = 0;
    public double alt = 0;

    /** Roll angle in degree */
    public double roll = 0.0;

    /** Pitch angle in degree */
    public double pitch = 0.0;

    /** Yaw angle in degree */
    public double yaw = 0.0;

    /** hat the Backend GPS receiver a fix */
    public boolean hasFix = false;

    /** system time (should be UTC) of handy */
    public int time_sec = 0;

    /** serial number of this android device */
    public String serialNumber = "123:456:789";

    /** Display name of this device */
    public String name = "TestName";
}
