/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.hardware;

public class DescriptionNotFoundException extends RuntimeException {

    public DescriptionNotFoundException() {}

    public DescriptionNotFoundException(String message) {
        super(message);
    }

}
