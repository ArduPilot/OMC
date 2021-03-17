/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.geo;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import java.util.OptionalDouble;

public class GeoReferencedHelper {

    public static OptionalDouble getMaxElev(Iterable<?> list) {
        double maxElev = Double.NEGATIVE_INFINITY;

        for (Object subLayer : list) {
            if (subLayer instanceof IPositionReferenced) {
                Position position = ((IPositionReferenced)subLayer).getPosition();
                if (position != null) {
                    maxElev = Math.max(maxElev, position.getElevation());
                }
            }

            if (subLayer instanceof ISectorReferenced) {
                OptionalDouble maxSectorElev = ((ISectorReferenced)subLayer).getMaxElev();
                if (maxSectorElev.isPresent()) {
                    maxElev = Math.max(maxElev, maxSectorElev.getAsDouble());
                }
            }
        }

        if (maxElev == Double.NEGATIVE_INFINITY) {
            return OptionalDouble.empty();
        }

        return OptionalDouble.of(maxElev);
    }

    public static OptionalDouble getMinElev(Iterable<?> list) {
        double minElev = Double.POSITIVE_INFINITY;

        for (Object subLayer : list) {
            if (subLayer instanceof IPositionReferenced) {
                Position position = ((IPositionReferenced)subLayer).getPosition();
                if (position != null) {
                    minElev = Math.min(minElev, position.getElevation());
                }
            }

            if (subLayer instanceof ISectorReferenced) {
                OptionalDouble minSectorElev = ((ISectorReferenced)subLayer).getMinElev();
                if (minSectorElev.isPresent()) {
                    minElev = Math.min(minElev, minSectorElev.getAsDouble());
                }
            }
        }

        if (minElev == Double.POSITIVE_INFINITY) {
            return OptionalDouble.empty();
        }

        return OptionalDouble.of(minElev);
    }

    public static Sector getSector(Iterable<?> list) {
        Sector sector = null;
        for (Object subLayer : list) {
            Sector newSector = null;
            if (subLayer instanceof ILatLonReferenced) {
                ILatLonReferenced posRef = (ILatLonReferenced)subLayer;
                LatLon latLon = posRef.getLatLon();
                if (latLon != null && !latLon.equals(LatLon.ZERO)) {
                    newSector = new Sector(latLon.latitude, latLon.latitude, latLon.longitude, latLon.longitude);
                }
            }

            if (subLayer instanceof ISectorReferenced) {
                ISectorReferenced secRef = (ISectorReferenced)subLayer;
                Sector tmp = secRef.getSector();
                if (tmp != null && !tmp.equals(Sector.EMPTY_SECTOR)) {
                    newSector = tmp;
                }
            }

            if (newSector == null) {
                continue;
            }

            if (sector == null || sector.equals(Sector.EMPTY_SECTOR)) {
                sector = newSector;
            } else {
                sector = sector.union(newSector);
            }
        }

        return sector;
    }
}
