/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.diagnostics;

import java.lang.management.ManagementFactory;

public class Debugger {

    private static final boolean IS_ATTACHED = checkJdwpArgument();
    private static boolean runningTests;

    private static boolean checkJdwpArgument() {
        for (String arg : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
            if (arg.contains("jdwp=")) {
                return true;
            }
        }

        return false;
    }

    public static boolean isAttached() {
        return IS_ATTACHED;
    }

    public static boolean isRunningTests() {
        return runningTests;
    }

    public static void setIsRunningTests(boolean value) {
        runningTests = value;
    }

}
