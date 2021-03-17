/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.hardware;

public enum APTypes {
    rev2_4,
    rev2_6,
    rev3_4,
    rev4_0,
    rev5_0;

    public static APTypes LATEST = values()[values().length - 1];

    public boolean compatibleWithThisAPrelease(String apRelease) {
        return getCompatibleAPrelease(apRelease) == this;
    }

    public static APTypes getCompatibleAPrelease(String apRelease) {
        if (apRelease == null) {
            return null;
        }

        if (apRelease.startsWith("2.")) {
            return rev2_4;
        } else if (apRelease.startsWith("3.0") || apRelease.startsWith("3.2")) {
            return rev2_6;
        } else if (apRelease.startsWith("3.4")) {
            return rev3_4;
        } else if (apRelease.startsWith("4.0")) {
            return rev4_0;
        } else {
            return rev5_0;
        }
        // return null;
    }

    public boolean canManageSafetyEvents() {
        return rev3_4.compareTo(this) <= 0;
    }

    public boolean canLinearClimbOnLine() {
        return rev5_0.compareTo(this) <= 0;
    }

    public boolean makesImagesBeforeAndAfterCorners() {
        return rev2_4 != this;
    }

    public boolean makesNoImagesAfterEnablingCamByDefault() {
        return rev2_4 != this;
    }

    public boolean supportsFlightphaseJumpToLanding() {
        return rev5_0.compareTo(this) <= 0;
    }

    public boolean supportsIgnoreWP() {
        return rev5_0.compareTo(this) <= 0;
    }

    public String getCompatibleReleaseVersionString() {
        switch (this) {
        case rev2_4:
            return "2.4";
        case rev2_6:
            return "3.0";
        case rev3_4:
            return "3.4";
        case rev4_0:
            return "4.0";
        case rev5_0:
            return "5.0";
        }

        return null;
    }
}
