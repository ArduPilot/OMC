/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.geo;

import gov.nasa.worldwind.geom.Position;

/**
 * For Objects with a true position.
 *
 * @author colman
 */
public interface IPositionReferenced extends IGeoReferenced, ILatLonReferenced {
    public Position getPosition();
}
