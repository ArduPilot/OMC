/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.helper;

import java.util.Arrays;

public final class SystemInformation {

    public static boolean isMac() {
        return isMac;
    }

    public static boolean isLinux() {
        return !isMac && !isWindows;
    }

    public static boolean isWindows() {
        return isWindows;
    }

    public static boolean isWindowsVista() {
        return isWindowsVistaOrLater && !isWindows7OrLater;
    }

    public static boolean isWindows7() {
        return isWindows7OrLater && !isWindows8OrLater;
    }

    public static boolean isWindowsVistaOrLater() {
        return isWindowsVistaOrLater;
    }

    public static boolean isWindows7OrLater() {
        return isWindows7OrLater;
    }

    public static boolean isWindows8OrLater() {
        return isWindows8OrLater;
    }

    public static boolean is64Bit() {
        return is64Bit;
    }

    public static int getMaxPath() {
        if (isWindows()) {
            return 260;
        }

        return Integer.MAX_VALUE;
    }

    private static final String OS_NAME = System.getProperty("os.name").toLowerCase();
    private static final String OS_VERSION = System.getProperty("os.version");
    private static final boolean isMac = OS_NAME.contains("mac");
    private static final boolean isWindows = OS_NAME.contains("win");
    private static final boolean isWindowsVistaOrLater = isWindows && (compareVersion(OS_VERSION, "6.0") >= 0);
    private static final boolean isWindows7OrLater = isWindows && (compareVersion(OS_VERSION, "6.1") >= 0);
    private static final boolean isWindows8OrLater = isWindows && (compareVersion(OS_VERSION, "6.2") >= 0);
    private static final boolean is64Bit = System.getProperty("sun.arch.data.model", "").contains("64");

    private static final String[] reservedWindowsNames = {
        "CON", "PRN", "AUX", "NUL", "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9", "LPT1",
        "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9"
    };

    private static final String windowsFilesFoldersIllegalChars = "<>:\"/\\|?*";

    public static String windowsFilesFoldersIllegalChars() {
        return windowsFilesFoldersIllegalChars;
    }

    public static String[] reservedWindowsNames() {
        return Arrays.copyOf(reservedWindowsNames, reservedWindowsNames.length);
    }

    private static int compareVersion(String str1, String str2) {
        String[] vals1 = str1.split("\\.");
        String[] vals2 = str2.split("\\.");
        int i = 0;
        while (i < vals1.length && i < vals2.length && vals1[i].equals(vals2[i])) {
            i++;
        }

        if (i < vals1.length && i < vals2.length) {
            int diff = Integer.valueOf(vals1[i]).compareTo(Integer.valueOf(vals2[i]));
            return Integer.signum(diff);
        }

        return Integer.signum(vals1.length - vals2.length);
    }

}
