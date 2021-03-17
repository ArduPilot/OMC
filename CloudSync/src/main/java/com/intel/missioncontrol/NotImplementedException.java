/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol;

public class NotImplementedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public NotImplementedException() {}

    public NotImplementedException(String message) {
        super(message);
    }

}
