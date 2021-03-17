/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.airspace;

import gov.nasa.worldwind.geom.LatLon;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AirspaceComperatorFloor implements Comparator<Pair<IAirspace, Double>> {
    public final LatLon latLon;
    public final double groundLevelElevationEGM;

    public final List<Pair<IAirspace, Double>> airspaceAlts;

    public AirspaceComperatorFloor(
            LatLon latLon,
            double groundLevelElevationEGM,
            List<IAirspace> airspaceCandidates,
            boolean removeMAVallowed,
            boolean removeOutsiders) {
        this.groundLevelElevationEGM = groundLevelElevationEGM;
        this.latLon = latLon;

        airspaceAlts = new ArrayList();
        for (IAirspace airspace : airspaceCandidates) {
            if (removeMAVallowed && airspace.getType().isMAVAllowed()) {
                continue;
            }

            double alt = airspace.floorMeters(latLon, groundLevelElevationEGM);

            if (removeOutsiders && alt == Double.POSITIVE_INFINITY) {
                continue;
            }

            airspaceAlts.add(new Pair<>(airspace, alt));
        }

        Collections.sort(airspaceAlts, this);
    }

    public int compare(Pair<IAirspace, Double> arg0, Pair<IAirspace, Double> arg1) {
        return Double.compare(arg0.getValue(), arg1.getValue());
    }
}
