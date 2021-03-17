/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.hardware;

public interface IMavlinkConnectionProperties extends IConnectionProperties {

    String getMavlinkAutopilot();

    String getMavlinkType();

}
