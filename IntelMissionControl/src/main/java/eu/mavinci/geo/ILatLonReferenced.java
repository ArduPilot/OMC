/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.geo;

import gov.nasa.worldwind.geom.LatLon;

/**
 * Object with at least lat and lon
 *
 * @author colman
 */
public interface ILatLonReferenced extends IGeoReferenced {

    public LatLon getLatLon();

}
