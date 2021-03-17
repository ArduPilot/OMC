/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.flightplan;

/**
 * This is thrown if someone trys to add elements to flightplancontainer, whitch is not allowed to be added their.
 *
 * @author caller
 */
public class FlightplanContainerWrongAddingException extends Exception {

    private static final long serialVersionUID = -4660837142862177154L;

    public FlightplanContainerWrongAddingException(Class<?> type) {
        super(type.getCanonicalName());
    }

    public FlightplanContainerWrongAddingException(Class<?> type, String into) {
        super(type.getCanonicalName() + " -> " + into);
    }

}
