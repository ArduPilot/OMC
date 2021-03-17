/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol;

public final class EnvironmentOptions {

    public static final boolean VERIFY_METHOD_ACCESS =
        Boolean.parseBoolean(System.getProperty("com.intel.missioncontrol.verifyMethodAccess", "false"));

    public static final boolean VERIFY_PROPERTY_ACCESS =
        Boolean.parseBoolean(System.getProperty("com.intel.missioncontrol.verifyPropertyAccess", "false"));

    public static final boolean ENABLE_DEADLOCK_DETECTION =
        Boolean.parseBoolean(System.getProperty("com.intel.missioncontrol.enableDeadlockDetection", "false"));

    public static final boolean ENABLE_FUTURE_OPTIMIZATION =
        Boolean.parseBoolean(System.getProperty("com.intel.missioncontrol.enableFutureOptimization", "true"));

    public static final int DEADLOCK_DETECTION_TIMEOUT =
        Integer.parseInt(
            System.getProperty("com.intel.missioncontrol.deadlockDetectionTimeout", Integer.toString(10000)));

    public static final String NEWS_URI =
        "http://imc-releasenotes-courteous-rabbit.apps1-fm-int.icloud.intel.com/index.html";

    public static final String NEWS_URI_DJI =
        "http://imc-releasenotes-courteous-rabbit.apps1-fm-int.icloud.intel.com/3rd-party.html";

    public static final int INIT_OBJECT_COUNT = 500;

    private EnvironmentOptions() {}

}
