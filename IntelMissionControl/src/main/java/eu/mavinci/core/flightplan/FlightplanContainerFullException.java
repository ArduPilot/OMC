/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.flightplan;

/**
 * This is thrown if someone trys to add elements to flightplancontainer, whitch exceed there capacity!
 *
 * @author caller
 */
public class FlightplanContainerFullException extends Exception {
    public FlightplanContainerFullException(String string) {
        super(string);
    }

    public FlightplanContainerFullException() {
        super();
    }

    private static final long serialVersionUID = 6135351691368165146L;

}
