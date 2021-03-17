/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.flightplan;

public interface IFlightplanLatLonReferenced extends IFlightplanRelatedObject {

    double getLon();

    void setLon(double lon);

    double getLat();

    void setLat(double lat);

    void setLatLon(double lat, double lon);

    boolean isStickingToGround();

}
