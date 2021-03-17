/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol;

public final class EnvironmentOptions {

    public static final boolean EAGER_PROPERTY_NAME_EVALUATION =
        (boolean)
            Boolean.parseBoolean(System.getProperty("com.intel.missioncontrol.eagerPropertyNameEvaluation", "false"));

    public static final boolean VERIFY_METHOD_ACCESS =
        (boolean)Boolean.parseBoolean(System.getProperty("com.intel.missioncontrol.verifyMethodAccess", "false"));

    public static final boolean VERIFY_PROPERTY_ACCESS =
        (boolean)Boolean.parseBoolean(System.getProperty("com.intel.missioncontrol.verifyPropertyAccess", "false"));

    public static final boolean ENABLE_DEADLOCK_DETECTION =
        (boolean)Boolean.parseBoolean(System.getProperty("com.intel.missioncontrol.enableDeadlockDetection", "false"));

    public static final int DEADLOCK_DETECTION_TIMEOUT =
        (int)
            Integer.parseInt(
                System.getProperty("com.intel.missioncontrol.deadlockDetectionTimeout", Integer.toString(10000)));

    public static final int EVENT_HANDLER_TIMEOUT =
        (int)
            Integer.parseInt(
                System.getProperty(
                    "com.intel.missioncontrol.eventHandlerTimeout", Integer.toString(Integer.MAX_VALUE)));

    public static final String NEWS_URI = "http://imc-releasenotes-courteous-rabbit.apps1-fm-int.icloud.intel.com/index.html";

    public static final String NEWS_URI_DJI = "http://imc-releasenotes-courteous-rabbit.apps1-fm-int.icloud.intel.com/3rd-party.html";

    public static final int INIT_OBJECT_COUNT = 500;

    private EnvironmentOptions() {}

}
