/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.geo;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;

public interface ICountryDetector {

    boolean allowProceed(LatLon latLon);

    boolean allowProceed(LatLon latLon, boolean silent);

    boolean allowProceed(Sector sector);

    boolean allowProceed(Sector sector, boolean silent);

    Country getFirstCountry(LatLon latLon);

    Country getFirstCountry(Sector sector);

}
