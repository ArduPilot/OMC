/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.helper;

import com.intel.missioncontrol.Localizable;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinDef;
import java.util.ArrayList;
import java.util.List;

public class DriveInformation {

    public static List<DriveInformation> getDrives() {
        List<DriveInformation> results = new ArrayList<>();

        char[] buffer = new char[1024];
        int ret = Kernel32.INSTANCE.GetLogicalDriveStrings(new WinDef.DWORD(buffer.length), buffer).intValue();
        check(ret > 0, "GetLogicalDriveStrings");

        for (String drive : splitDriveString(buffer)) {
            int res = Kernel32.INSTANCE.GetDriveType(drive);
            DriveType driveType;
            switch (res) {
            case Kernel32.DRIVE_FIXED:
                driveType = DriveType.FIXED;
                break;
            case Kernel32.DRIVE_REMOVABLE:
                driveType = DriveType.REMOVABLE;
                break;
            default:
                driveType = DriveType.UNKNOWN;
                break;
            }

            results.add(new DriveInformation(driveType, drive));
        }

        return results;
    }

    private static void check(boolean condition, String functionName) {
        if (!condition) {
            throw new RuntimeException(functionName + " failed with error " + Kernel32.INSTANCE.GetLastError() + ".");
        }
    }

    private static List<String> splitDriveString(char[] drives) {
        List<String> result = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < drives.length; ++i) {
            if (drives[i] != 0) {
                builder.append(drives[i]);
            } else if (builder.length() > 0) {
                result.add(builder.toString());
                builder = new StringBuilder();
            }
        }

        return result;
    }

    public enum DriveType implements Localizable {
        UNKNOWN,
        FIXED,
        REMOVABLE
    }

    private final DriveType type;
    private final String name;

    private DriveInformation(DriveType type, String name) {
        this.type = type;
        this.name = name;
    }

    public DriveType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

}
