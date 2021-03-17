/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.airspaces.cache.airspace;

import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Geometries;
import com.github.davidmoten.rtree.geometry.Rectangle;
import eu.mavinci.airspace.IAirspace;

import gov.nasa.worldwind.geom.Sector;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class AirspacesGeometryIndex {
    private RTree<IAirspace, Rectangle> index = RTree.create();

    public void add(IAirspace airspace) {
        index = index.add(airspace, toIndexGeometry(airspace.getBoundingBox()));
    }

    private Rectangle toIndexGeometry(Sector boundingBox) {
        double minLat = boundingBox.getMinLatitude().getDegrees();
        double minLon = boundingBox.getMinLongitude().getDegrees();
        double maxLat = boundingBox.getMaxLatitude().getDegrees();
        double maxLon = boundingBox.getMaxLongitude().getDegrees();
        return Geometries.rectangleGeographic(minLon, minLat, maxLon, maxLat);
    }

    @SafeVarargs
    public final List<IAirspace> search(Sector searchBoundingBox, Consumer<IAirspace>... actions) {
        return index.search(toIndexGeometry(searchBoundingBox))
            .map(Entry::value)
            .doOnNext(a -> invokeActions(a, actions))
            .toList()
            .toBlocking()
            .single();
    }

    private void invokeActions(IAirspace a, Consumer<IAirspace>[] consumers) {
        Stream.of(consumers).forEach(c -> c.accept(a));
    }
}
