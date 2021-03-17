/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.plane;

public enum AirplaneType {
    SIRIUS_BASIC,
    SIRIUS_PRO,
    // WHITE_DWARF,
    FALCON8,
    FALCON8PLUS,
    GRAYHAWK,
    MANNED_PLANE_SELF_TRIGGER,
    MANNED_PLANE_MANUAL_TRIGGER;

    public boolean isManned() {
        if (this == MANNED_PLANE_MANUAL_TRIGGER || this == MANNED_PLANE_SELF_TRIGGER) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isFalcon() {
        if (this == FALCON8 || this == FALCON8PLUS) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isFixedWing() {
        if (!this.isFalcon() && !this.isGrayHawk() && !this.isManned()) {
            return true;
        } else {
            return false;
        }
    }

    public static AirplaneType getAirplaneType(String airplane) {
        if ("sirius".equalsIgnoreCase(airplane)) {
            return SIRIUS_PRO;
        } else if ("falcon8p".equalsIgnoreCase(airplane)) {
            return FALCON8PLUS;
        } else {
            for (AirplaneType t : values()) {
                if (t.name().equalsIgnoreCase(airplane)) {
                    return t;
                }
            }

            return null;
        }
    }

    public static AirplaneType getDefault(boolean setCopterMode) {
        if (setCopterMode) {
            return AirplaneType.GRAYHAWK; // CHECK F8+ license
        } else {
            return AirplaneType.SIRIUS_PRO;
        }
    }

    public static AirplaneType getDefault() {
        return AirplaneType.GRAYHAWK;
    }

    public boolean isSirius() {
        return this == SIRIUS_PRO || this == SIRIUS_BASIC;
    }

    public boolean planIndividualImagePositions() {
        switch (this) {
        case FALCON8:
        case FALCON8PLUS:
        case GRAYHAWK:
        case MANNED_PLANE_MANUAL_TRIGGER:
            return true;
        case SIRIUS_BASIC:
            // case WHITE_DWARF:
        case MANNED_PLANE_SELF_TRIGGER:
        default:
            return false;
        }
    }

    public boolean isBetterThan(AirplaneType other) {
        if (this == FALCON8PLUS && other == FALCON8) {
            return true;
        }

        if (this == SIRIUS_PRO && other == SIRIUS_BASIC) {
            return true;
        }

        return false;
    }

    public boolean isGrayHawk() {
        if (this == GRAYHAWK) {
            return true;
        } else {
            return false;
        }
    }
}
