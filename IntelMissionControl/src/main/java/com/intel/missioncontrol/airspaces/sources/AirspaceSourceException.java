/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.airspaces.sources;

public class AirspaceSourceException extends RuntimeException {
    public AirspaceSourceException(String message, Throwable cause) {
        super(message, cause);
    }
}
