/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.plane.sendableobjects;

public class Backend extends MObject {

    /** */
    private static final long serialVersionUID = -150851311055570093L;

    /** TCP Port of this Backend */
    public volatile int port = -1;

    /** FTP Port of this Backend */
    public volatile int ftpPort = -1;

    /** Version information of this Backend */
    public BackendInfo info = new BackendInfo();

    /** name of this Backend */
    public volatile String name = "";

    /** GPS Position of the Backend */
    public volatile double lon = 0;

    public volatile double lat = 0;
    public volatile double alt = 0; // in cm

    /** hat the Backend GPS receiver a fix */
    public volatile boolean hasFix = false;

    /** is a GPS receiver atatched to the Backend */
    public volatile boolean hasGPS = false;

    /** is a DPGS (RTCM) base Station atatched to the Backend */
    public volatile boolean hasRTCMInput = false;

    /** the Battary voltage of the Backend's power supply */
    public volatile double batteryVoltage = 0;

    /** system time of backend. this time is sometimes resetted to 0 after reboot / update of backend */
    public volatile int time_sec = 0;

    /** is true, if this package was received by broadcast */
    public volatile boolean wasReceivedByBroadcast = false;

    public boolean isCompatible() {
        return info.isCompatible();
    }

    public boolean isConnectedByUsbCockpit() {
        return true;
    }
}
