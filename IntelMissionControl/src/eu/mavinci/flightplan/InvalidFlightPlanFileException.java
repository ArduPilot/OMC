/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.flightplan;

public class InvalidFlightPlanFileException extends Exception {
    public InvalidFlightPlanFileException() {
        super();
    }

    public InvalidFlightPlanFileException(String message) {
        super(message);
    }

    public InvalidFlightPlanFileException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidFlightPlanFileException(Throwable cause) {
        super(cause);
    }
}
