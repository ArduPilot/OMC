/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.plane.sendableobjects;

import eu.mavinci.core.flightplan.GPSFixType;
import eu.mavinci.core.plane.PlaneConstants;
import eu.mavinci.core.plane.tcp.TCPConnection;
import eu.mavinci.core.flightplan.GPSFixType;

import java.util.Vector;

public class PlaneInfo extends PlaneInfoLight {

    private static final long serialVersionUID = -2993190968359925538L;

    /** all members of PlaneInfoLight are also avaliable here */

    /** Number of servo channels */
    public int servoChannelCount = 4;

    /** host for FTP to UAV */
    public String ftpHost = "";

    /** port for FTP to UAV */
    public int ftpPort = -1;

    /** Total air km (in cm) */
    public int totalairkm = -1;

    /** total air time (in seconds) */
    public int totalairtime = -1;

    /** number of flights */
    public int numberofflights = -1;

    /** Description of Health Channels */
    public MVector<SingleHealthDescription> healthDescriptions =
        new MVector<SingleHealthDescription>(SingleHealthDescription.class);

    public Vector<Integer> indexesToShow(HealthData d) {
        Vector<Integer> ind = new Vector<Integer>();
        boolean isRTK = false;
        int addFailEventsId = -1;
        for (int i = 0; i < healthDescriptions.size() && i < d.absolute.size(); i++) {
            SingleHealthDescription hd = healthDescriptions.get(i);
            float abs = d.absolute.get(i);

            if (hd.name.equals(PlaneConstants.DEF_GLONASS)) {
                continue;
            }

            if (hd.name.equals(PlaneConstants.DEF_GPS_QUALITY)) {
                if (abs == GPSFixType.rtkFixedBL.ordinal() || abs == GPSFixType.rtkFloatingBL.ordinal()) {
                    isRTK = true;
                }

                continue;
            }

            if (hd.name.equals("RTCMAge") && !isRTK) {
                continue;
            }

            if (hd.name.equals("Failevents")) {
                if (Math.round(abs) >= 512) {
                    addFailEventsId = i;
                }

                continue;
            }

            if (hd.isImportant) {
                ind.add(i);
                continue;
            } else if ((!hd.isGreen(abs)) && hd.doWarnings) {
                ind.add(i);
                continue;
            }
        }

        if (addFailEventsId >= 0) {
            ind.add(addFailEventsId);
        }

        return ind;
    }

    public Vector<Integer> indexesWarnRed(HealthData d) {
        Vector<Integer> ind = new Vector<Integer>();
        for (int i = 0; i < healthDescriptions.size() && i < d.absolute.size(); i++) {
            SingleHealthDescription hd = healthDescriptions.get(i);
            if (!hd.doWarnings) {
                continue;
            }

            float abs = d.absolute.get(i);
            if (hd.isRed(abs)) {
                ind.add(i);
            }
        }

        return ind;
    }

    public Vector<Integer> indexesWarnYellow(HealthData d) {
        Vector<Integer> ind = new Vector<Integer>();
        for (int i = 0; i < healthDescriptions.size() && i < d.absolute.size(); i++) {
            SingleHealthDescription hd = healthDescriptions.get(i);
            if (!hd.doWarnings) {
                continue;
            }

            float abs = d.absolute.get(i);
            if (hd.isYellow(abs)) {
                ind.add(i);
            }
        }

        return ind;
    }

    public String getFTPurl() {
        return "ftp://"
            + TCPConnection.DEFAULT_FTP_USER
            + ":"
            + TCPConnection.DEFAULT_FTP_PW
            + "@"
            + ftpHost
            + ":"
            + ftpPort
            + "/";
    }

    public boolean isUpdateable() {
        return !(serialNumber.toUpperCase().contains(PlaneConstants.MAC_SIM_SERVER)
            || hardwareType.toUpperCase().contains("SIMULATION"));
    }

}
