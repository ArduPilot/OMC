/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone;

class PX4CustomMode {
    enum MainMode {
        PX4_CUSTOM_MAIN_MODE_UNDEFINED,
        PX4_CUSTOM_MAIN_MODE_MANUAL,
        PX4_CUSTOM_MAIN_MODE_ALTCTL,
        PX4_CUSTOM_MAIN_MODE_POSCTL,
        PX4_CUSTOM_MAIN_MODE_AUTO,
        PX4_CUSTOM_MAIN_MODE_ACRO,
        PX4_CUSTOM_MAIN_MODE_OFFBOARD,
        PX4_CUSTOM_MAIN_MODE_STABILIZED,
        PX4_CUSTOM_MAIN_MODE_RATTITUDE,
        PX4_CUSTOM_MAIN_MODE_SIMPLE;

        public int getValue() {
            return ordinal();
        }

        static MainMode fromValue(int value) {
            return (value >= 0 && value < values().length) ? values()[value] : PX4_CUSTOM_MAIN_MODE_UNDEFINED;
        }
    }

    enum SubModeAuto {
        PX4_CUSTOM_SUB_MODE_AUTO_UNDEFINED,
        PX4_CUSTOM_SUB_MODE_AUTO_READY,
        PX4_CUSTOM_SUB_MODE_AUTO_TAKEOFF,
        PX4_CUSTOM_SUB_MODE_AUTO_LOITER,
        PX4_CUSTOM_SUB_MODE_AUTO_MISSION,
        PX4_CUSTOM_SUB_MODE_AUTO_RTL,
        PX4_CUSTOM_SUB_MODE_AUTO_LAND,
        PX4_CUSTOM_SUB_MODE_AUTO_RTGS,
        PX4_CUSTOM_SUB_MODE_AUTO_FOLLOW_TARGET,
        PX4_CUSTOM_SUB_MODE_AUTO_PRECLAND;

        public int getValue() {
            return ordinal();
        }

        static SubModeAuto fromValue(int value) {
            return (value >= 0 && value < values().length) ? values()[value] : PX4_CUSTOM_SUB_MODE_AUTO_UNDEFINED;
        }
    }

    private final MainMode mainMode;
    private final SubModeAuto subModeAuto;

    static final PX4CustomMode UNDEFINED =
        new PX4CustomMode(MainMode.PX4_CUSTOM_MAIN_MODE_UNDEFINED, SubModeAuto.PX4_CUSTOM_SUB_MODE_AUTO_UNDEFINED);

    private PX4CustomMode(MainMode mainMode, SubModeAuto subModeAuto) {
        this.mainMode = mainMode;
        this.subModeAuto = subModeAuto;
    }

    MainMode getMainMode() {
        return mainMode;
    }

    SubModeAuto getSubModeAuto() {
        return subModeAuto;
    }

    static PX4CustomMode fromCustomMode(long customMode) {
        MainMode mainMode = MainMode.fromValue((int)((customMode & 0xFF0000) >> 16));
        SubModeAuto subModeAuto = SubModeAuto.fromValue((int)((customMode & 0xFF000000) >> 24));

        return new PX4CustomMode(mainMode, subModeAuto);
    }
}
