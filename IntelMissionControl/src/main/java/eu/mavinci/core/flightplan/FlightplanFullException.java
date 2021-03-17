/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.flightplan;

/**
 * This is thrown if someone trys to add elements to flightplan, which has no free IDs left on its upper bound
 *
 * @author caller
 */
public class FlightplanFullException extends RuntimeException {

    private static final long serialVersionUID = -4430917271711949191L;

}
