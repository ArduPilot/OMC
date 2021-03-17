/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.plane;

public class PlaneConstants {
    // public static final int SERVO_COUNT = 5;
    public static final int DEFAULT_SERVO_COUNT = 2;
    public static final int DEBUG_COUNT = 30;
    public static final int MANUAL_SERVO_COUNT = 16;
    public static final int PLANE_NAME_MAX_SIZE = 20;
    public static final String PLANE_NAME_REGEX = "[a-zA-Z0-9_:-]*";
    public static final int HEALTH_ADC_CHANNEL_COUNT = 8;
    public static final int DEF_MISC_TOPCONMODE = 1;

    public static final int DEF_CONT_NAV_CIRCR = 6000;
    public static final int DEF_CONT_ALT_LANDINGALTITUDE = 1000;

    public static final String DEF_BATTERY = "UAV Battery";
    public static final String DEF_BATTERY_CONNECTOR = "ConnectorBattery";
    public static final String DEF_GPS = "GPS";
    public static final String DEF_GLONASS = "GLONASS";
    public static final String DEF_GPS_QUALITY = "GPSQuality";
    public static final String DEF_MOTOR1 = "Motor1";
    public static final String DEF_FAIL_EVENTS = "Failevents";
    public static final String WIND_SPEED = "Wind speed";
    public static final String WIND_DIRECTION = "Wind direction";
    public static final String GROUND_SPEED = "Ground speed";
    public static final String FLIGHT_MODE = "Flight mode";
    public static final String CONNECTION_QUALITY = "Connection quality";

    public static final String UNIT_FOR_FLAGS = "flag";
    public static final float minLevelForValidPercent =
        -10000; // if healt percent value is larger than this, it will be shown

    public static final int DEF_MISC_BBOXSIZE = 30000; // in cm

    public static final double UNDEFINED_THREASHOLD =
        -100000; // times 10 smaller than sended value to avoid rounding problems
    public static final double UNDEFINED_ANGLE = -100000;

    public static final String MAC_SIM_SERVER = "54:04:A6:F1:7B:2C";

    public static final double ALT_TAKEOFF_ENDS_M = 20;

    public static final int SERVO_THROTTLE = 0; // 10 -> off, 245 -> full
    public static final int SERVO_AILERON = 1; // 10 -> left, 127 -> center, 245 -> right
    public static final int SERVO_ELEVATOR = 2; // 10 -> dive, 127 -> center, 245 -> climb
    public static final int SERVO_RUDDER = 3; // 10 -> left, 127 -> center, 245 -> right
    public static final int SERVO_SWITCH_LANDING = 7; // 245 -> pressed, 10 -> released
    public static final int SERVO_SWITCH_AP_MANUAL = 14; // 255 -> autopilot, 0 -> manual
    public static final int SERVO_SWITCH_AUTO_ASSISTED = 15; // 255 -> assisted, 0 -> automatic

    public static final int SERVO_VAL_AUTOMATIC = 0;
    public static final int SERVO_VAL_ASSISTED = 255;
    public static final int SERVO_VAL_MANUAL = 0;
    public static final int SERVO_VAL_AUTOPILOT = 255;
    public static final int SERVO_VAL_LANDING_PRESSED = 245;
    public static final int SERVO_VAL_LANDING_RELEASED = 10;

    public static final int SERVO_VAL_CENTER = 127;
    public static final int SERVO_VAL_MIN = 10;
    public static final int SERVO_VAL_MAX = 245;
    public static final int SERVO_VAL_LEFT = 10;
    public static final int SERVO_VAL_RIGHT = 245;
    public static final int SERVO_VAL_DIVE = 10;
    public static final int SERVO_VAL_CLIMB = 245;

}
