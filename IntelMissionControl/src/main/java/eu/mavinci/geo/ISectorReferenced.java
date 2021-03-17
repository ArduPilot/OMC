/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.geo;

import gov.nasa.worldwind.geom.Sector;

import java.util.OptionalDouble;

public interface ISectorReferenced extends IGeoReferenced {

    // public static final double noDataElev = -1e100;

    public Sector getSector();

    /**
     * Returns the maximum distance above sea level in meters. Iff the object also implements IFlightplanRelatedObject,
     * it is the altitude above the plane's starting position.
     */
    public OptionalDouble getMaxElev();

    /**
     * Returns the minimum distance above sea level in meters. Iff the object also implements IFlightplanRelatedObject,
     * it is the altitude above the plane's starting position.
     */
    public OptionalDouble getMinElev();
}
