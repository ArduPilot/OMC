/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.flightplanning.geometry2d;

import com.jme3.math.Vector2f;
import java.util.ArrayList;
import java.util.List;

/**
 * To specify a constraint specific to Polygons, it is useful to introduce the concept of a linear ring:
 *
 * <p>o A linear ring is a closed LineString with four or more positions.
 *
 * <p>o The first and last positions are equivalent, and they MUST contain identical values; their representation SHOULD
 * also be identical.
 *
 * <p>o A linear ring is the boundary of a surface or the boundary of a hole in a surface.
 *
 * <p>o A linear ring MUST follow the right-hand rule with respect to the area it bounds, i.e., exterior rings are
 * counterclockwise, and holes are clockwise.
 *
 * <p>Note: the [GJ2008] specification did not discuss linear ring winding order. For backwards compatibility, parsers
 * SHOULD NOT reject Polygons that do not follow the right-hand rule.
 *
 * <p>Though a linear ring is not explicitly represented as a GeoJSON geometry type, it leads to a canonical formulation
 * of the Polygon geometry type definition as follows:
 *
 * <p>o For type "Polygon", the "coordinates" member MUST be an array of linear ring coordinate arrays.
 *
 * <p>o For Polygons with more than one of these rings, the first MUST be the exterior ring, and any others MUST be
 * interior rings. The exterior ring bounds the surface, and the interior rings (if present) bound holes within the
 * surface.
 */
public class Polygon extends Geometry2D {

    List<Vector2f> cornerPoints;

    public Polygon() {
        this.cornerPoints = new ArrayList<>();
    }

    public Polygon(List<Vector2f> cornerPoints) {
        this.cornerPoints = cornerPoints;
    }

    public List<Vector2f> getCornerPoints() {
        return cornerPoints;
    }

    public void setCornerPoints(List<Vector2f> cornerPoints) {
        this.cornerPoints = cornerPoints;
    }
}
