/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.hardware;

public class HardwareConfigurationException extends RuntimeException {

    public HardwareConfigurationException() {}

    public HardwareConfigurationException(String message) {
        super(message);
    }

}
