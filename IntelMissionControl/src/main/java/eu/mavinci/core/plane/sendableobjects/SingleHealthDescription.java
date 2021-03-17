/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.plane.sendableobjects;

import eu.mavinci.core.plane.PlaneConstants;

public class SingleHealthDescription extends MObject {

    private static final long serialVersionUID = 8998804877888191782L;

    /** Name of this Value the first value with name @see DEF_BATTERY is shown by andorid as battery */
    public String name = "";

    /** The unit of this health value if possible, use one of the following values: V, °C, mAh, Ah, mA, A, rpm, mbar */
    public String unit = "";

    /** Should warn if green value range is leaved */
    public boolean doWarnings = false;

    /** Show this always in Main Screen */
    public boolean isImportant = false;

    /**
     * ranges for yellow, green, red (implizit) absolute values. min and max values are beloging to the color of the
     * intervall
     */
    public float minYellow = 0f;

    public float maxYellow = 3f;
    public float minGreen = 1f;
    public float maxGreen = 2f;

    public boolean isRed(float abs) {
        return abs < minYellow || abs > maxYellow;
    }

    public boolean isYellow(float abs) {
        return (abs >= minYellow && abs < minGreen) || (abs <= maxYellow && abs > maxGreen);
    }

    public boolean isGreen(float abs) {
        if (unit.equals("")) {
            return true;
        }

        return abs >= minGreen && abs <= maxGreen;
    }

    public boolean isFlag() {
        return PlaneConstants.UNIT_FOR_FLAGS.equals(unit);
    }

    public boolean isMainBatt() {
        return PlaneConstants.DEF_BATTERY.equals(name);
    }

    public boolean isGPS_Sat() {
        return PlaneConstants.DEF_GPS.equals(name);
    }

    public boolean isConnectorBatt() {
        return PlaneConstants.DEF_BATTERY_CONNECTOR.equals(name);
    }
}
