/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.flightplan;

/**
 * Currently the altitude managed via the interface is relative to the takeoff
 */
public interface IFlightplanPositionReferenced extends IFlightplanLatLonReferenced {

    void setAltInMAboveFPRefPoint(double altInM);

    double getAltInMAboveFPRefPoint();

}
