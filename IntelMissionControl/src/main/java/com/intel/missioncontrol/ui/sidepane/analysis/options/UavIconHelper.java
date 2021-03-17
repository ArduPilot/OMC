/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.analysis.options;

import com.intel.missioncontrol.drone.AlertLevel;
import com.intel.missioncontrol.drone.legacy.PlaneHealth.PlaneHealthChannelInfo;
import eu.mavinci.core.plane.AirplaneFlightmode;
import eu.mavinci.core.plane.AirplaneFlightphase;

public abstract class UavIconHelper {

    private static final String BATTERY_4 = "com/intel/missioncontrol/icons/icon_battery_4-4";
    private static final String BATTERY_3 = "com/intel/missioncontrol/icons/icon_battery_3-4";
    private static final String BATTERY_2 = "com/intel/missioncontrol/icons/icon_battery_2-4";
    private static final String BATTERY_1 = "com/intel/missioncontrol/icons/icon_battery_1-4";
    private static final String BATTERY_0 = "com/intel/missioncontrol/icons/icon_battery_0-4";

    private static final String BATTERY_LOW = "com/intel/missioncontrol/icons/icon_fw_lowbatt";
    private static final String BATTERY_LOW_WARN = "com/intel/missioncontrol/icons/icon_fw_lowbatt_warn";

    private static final String PHASE_PATH_PREFIX = "com/intel/missioncontrol/icons/icon_fp_";
    private static final String MODE_PATH_PREFIX = "com/intel/missioncontrol/icons/icon_fm_";

    private static final String ICON_ALERT = "com/intel/missioncontrol/icons/icon_warning";
    private static final String ICON_WARNING = "com/intel/missioncontrol/icons/icon_warning-notice";
    
    private static final String SVG = ".svg";

    private UavIconHelper() {
        // static class
    }

    public static String getFlightPhaseIconPng(AirplaneFlightphase flightPhase, AirplaneFlightmode flightMode) {
        if (flightMode != null && flightMode != AirplaneFlightmode.AutomaticFlight) {
            return MODE_PATH_PREFIX
                + "manual"
                + SVG; // TODO, on the long term we should distinguish here between modes with different icons
        }

        if (flightPhase == null) {
            return PHASE_PATH_PREFIX + AirplaneFlightphase.areaRestricted + SVG;
        }

        return PHASE_PATH_PREFIX + flightPhase.toString() + SVG;
    }

    public static String getBatteryIconPng(
            Number voltage, Number percent, PlaneHealthChannelInfo batteryInfo, boolean needLeadingSlash) {
        return getBatteryIcon(voltage, percent, batteryInfo, needLeadingSlash, SVG);
    }

    public static String getBatteryIconSvg(
            Number voltage, Number percent, PlaneHealthChannelInfo batteryInfo, boolean needLeadingSlash) {
        return getBatteryIcon(voltage, percent, batteryInfo, needLeadingSlash, SVG);
    }

    public static String getBatteryIcon(
            Number voltage,
            Number percent,
            PlaneHealthChannelInfo batteryInfo,
            boolean needLeadingSlash,
            String format) {
        if ((voltage == null) || (percent == null)) {
            return getIconPath(BATTERY_0, needLeadingSlash, format);
        }

        float percentValue = percent.floatValue();
        float voltageValue = voltage.floatValue();

        if ((voltageValue <= 0f) || (percentValue <= 0f)) {
            return getIconPath(BATTERY_0, needLeadingSlash, format);
        }

        if (batteryInfo == null) {
            return getIconPath(BATTERY_0, needLeadingSlash, format);
        }

        if (batteryInfo.isRed(voltageValue)) {
            return getIconPath(BATTERY_LOW, needLeadingSlash, format);
        }

        if (batteryInfo.isYellow(voltageValue)) {
            return getIconPath(BATTERY_LOW_WARN, needLeadingSlash, format);
        }

        if (percentValue > 75.0) {
            return getIconPath(BATTERY_4, needLeadingSlash, format);
        }

        if ((percentValue > 50.0) && (percentValue <= 75.0)) {
            return getIconPath(BATTERY_3, needLeadingSlash, format);
        }

        if ((percentValue > 25.0) && (percentValue <= 50.0)) {
            return getIconPath(BATTERY_2, needLeadingSlash, format);
        }

        if ((percentValue > 0.0) && (percentValue <= 25.0)) {
            return getIconPath(BATTERY_1, needLeadingSlash, format);
        }

        return getIconPath(BATTERY_0, needLeadingSlash, format);
    }

    public static String getAlertIconPng(AlertLevel alert, boolean needLeadingSlash) {
        return getAlertIcon(alert, needLeadingSlash, SVG);
    }

    public static String getAlertIconSvg(AlertLevel alert, boolean needLeadingSlash) {
        return getAlertIcon(alert, needLeadingSlash, SVG);
    }

    public static String getAlertIcon(AlertLevel alert, boolean needLeadingSlash, String format) {
        if ((alert == null) || (alert == AlertLevel.GREEN)) {
            // must be null
            return null;
        }

        if (alert == AlertLevel.RED) {
            return getIconPath(ICON_ALERT, needLeadingSlash, format);
        }

        if (alert == AlertLevel.YELLOW) {
            return getIconPath(ICON_WARNING, needLeadingSlash, format);
        }

        // must be null
        return null;
    }

    private static String getIconPath(String prefix, boolean needLeadingSlash, String format) {
        if (needLeadingSlash) {
            prefix = "/" + prefix;
        }

        return prefix + format;
    }

}
