/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.hardware;


/**
 * 0: no fix (sollte nie sein ;-) 1: autonomous gps 2: dgps (meist sbas) 4: rtk with fixed baseline (bestes) 5: rtk with
 * floating baseline (etwas weniger genau als das vorher)
 *
 * @author marco
 */
public enum GPSFixType {
    // see: http://gpsd.berlios.de/NMEA.txt
    // - 0 - fix not available,
    // - 1 - GPS fix,
    // - 2 - Differential GPS fix
    // (values above 2 are 2.3 features)
    // - 3 = PPS fix
    // - 4 = Real Time Kinematic
    // - 5 = Float RTK
    // - 6 = estimated (dead reckoning)
    // - 7 = Manual input mode
    // - 8 = Simulation mode
    // - 9 = No GPS connected (MAVLink)
    // - 10 = Static fix that is typically used for base stations (MAVLink)
    // - 11 = PPP fix (MAVLink. Precision Point Positioning. Military grade DGPS. see:
    // https://www.u-blox.com/en/press-release/u-blox-achieves-sub-meter-gps-accuracy-ppp-precise-point-positioning)
    noFix,
    gpsFix,
    dgps,
    PPS,
    rtkFixedBL,
    rtkFloatingBL,
    estimated,
    manualInput,
    simulation,
    unknown,
    staticFixed,
    PPP;

    public static boolean isValid(Float val) {
        if (val == null) {
            return false;
        }

        int id = (int)val.floatValue();
        return id >= 0 && id < values().length;
    }

    public String getName() {
        switch (this) {
        case noFix:
            return "NONE";
        case gpsFix:
            return "AUTO";
        case dgps:
            return "SBAS";
        case rtkFixedBL:
            return "rtkFIXED";
        case rtkFloatingBL:
            return "rtkFLOAT";
        default:
            return toString();
        }
    }

    public static GPSFixType parseMeta(String metaParsing, GPSFixType fallback) {
        try {
            return GPSFixType.values()[Integer.parseInt(metaParsing)];
        } catch (Exception e) {
        }

        try {
            return GPSFixType.valueOf(metaParsing);
        } catch (Exception e1) {
        }

        if ("autonomousGps".equals(metaParsing)) {
            return gpsFix;
        }

        if ("gpsFix".equals(metaParsing)) {
            return gpsFix;
        }

        if ("rtkFloatingBaseline".equals(metaParsing)) {
            return rtkFloatingBL;
        }

        if ("rtkFixedBaseline".equals(metaParsing)) {
            return rtkFixedBL;
        }

        return fallback;
    }

    public double getXYaccuracy() {
        switch (this) {
        case dgps:
            return 2;
        case rtkFixedBL:
            return 0.02;
        case rtkFloatingBL:
            return 0.30;
        default:
            return 10;
        }
    }

    public double getZaccuracy() {
        return getXYaccuracy() * 1.5;
    }
}
