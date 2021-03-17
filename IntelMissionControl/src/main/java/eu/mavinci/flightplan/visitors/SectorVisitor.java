/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.flightplan.visitors;

import eu.mavinci.core.flightplan.CFlightplan;
import eu.mavinci.core.flightplan.IFlightplanRelatedObject;
import eu.mavinci.core.flightplan.visitors.AFlightplanVisitor;
import eu.mavinci.flightplan.Flightplan;
import eu.mavinci.flightplan.LandingPoint;
import eu.mavinci.flightplan.ReferencePoint;
import eu.mavinci.geo.ILatLonReferenced;
import eu.mavinci.geo.IPositionReferenced;
import eu.mavinci.geo.ISectorReferenced;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import java.util.ArrayList;
import java.util.OptionalDouble;

public class SectorVisitor extends AFlightplanVisitor implements ISectorReferenced {

    private IFlightplanRelatedObject root;
    private ArrayList<LatLon> l = new ArrayList<LatLon>();
    private ArrayList<Sector> ls = new ArrayList<Sector>();
    private Sector sector = null;
    private OptionalDouble max = OptionalDouble.of(Double.NEGATIVE_INFINITY);
    private OptionalDouble min = OptionalDouble.of(Double.POSITIVE_INFINITY);

    double offset;

    public SectorVisitor(IFlightplanRelatedObject root) {
        this.root = root;
        startVisit(root);
    }

    @Override
    public OptionalDouble getMaxElev() {
        return max;
    }

    @Override
    public OptionalDouble getMinElev() {
        return min;
    }

    @Override
    public Sector getSector() {
        return sector;
    }

    @Override
    public void preVisit() {
        l.clear();
        ls.clear();
        max = OptionalDouble.of(Double.NEGATIVE_INFINITY);
        min = OptionalDouble.of(Double.POSITIVE_INFINITY);
        sector = null;
    }

    @Override
    public void postVisit() {
        if (max.isPresent() && max.getAsDouble() == Double.NEGATIVE_INFINITY) {
            max = OptionalDouble.empty();
        }

        if (max.isPresent()) {
            max = OptionalDouble.of(max.getAsDouble() + offset);
        }

        if (min.isPresent() && min.getAsDouble() == Double.POSITIVE_INFINITY) {
            min = OptionalDouble.empty();
        }

        if (min.isPresent()) {
            min = OptionalDouble.of(min.getAsDouble() + offset);
        }

        if (l.size() != 0) {
            sector = Sector.boundingSector(l);
            if (sector != Sector.EMPTY_SECTOR) {
                ls.add(sector);
            }
        }

        if (ls.size() != 0) {
            sector = Sector.union(ls);
        }

        if (sector == Sector.EMPTY_SECTOR) {
            sector = null;
        }
    }

    @Override
    public boolean visit(IFlightplanRelatedObject fpObj) {
        if (fpObj == root) {
            if (root instanceof Flightplan) {
                Flightplan fp = (Flightplan)root;
            }

            return false;
        }

        if (fpObj instanceof ReferencePoint) {
            ReferencePoint origin = (ReferencePoint)fpObj;
            if (!origin.isDefined()) {
                return false;
            }
        }

        if (fpObj instanceof LandingPoint) {
            LandingPoint lp = (LandingPoint)fpObj;
            if (!lp.isActive()) {
                return false;
            }
        }

        if (fpObj instanceof ILatLonReferenced) {
            ILatLonReferenced latLonRef = (ILatLonReferenced)fpObj;
            LatLon latLon = latLonRef.getLatLon();
            if (!latLon.equals(LatLon.ZERO)) {
                l.add(latLon);
            }
        } else if (fpObj instanceof ISectorReferenced) {
            ISectorReferenced secRef = (ISectorReferenced)fpObj;
            Sector s = secRef.getSector();
            if (s != null && !s.equals(Sector.EMPTY_SECTOR)) {
                ls.add(s);
            }
        }

        if (fpObj instanceof IPositionReferenced) {
            double elevation = ((IPositionReferenced)fpObj).getPosition().getElevation();
            max =
                max.isPresent()
                    ? OptionalDouble.of(Math.max(max.getAsDouble(), elevation))
                    : OptionalDouble.of(elevation);
            min =
                min.isPresent()
                    ? OptionalDouble.of(Math.min(min.getAsDouble(), elevation))
                    : OptionalDouble.of(elevation);
        } else if (fpObj instanceof ISectorReferenced) {
            OptionalDouble maxElevOpt = ((ISectorReferenced)fpObj).getMaxElev();
            OptionalDouble minElevOpt = ((ISectorReferenced)fpObj).getMinElev();
            double maxElev = maxElevOpt.isPresent() ? maxElevOpt.getAsDouble() : Double.NEGATIVE_INFINITY;
            double minElev = minElevOpt.isPresent() ? minElevOpt.getAsDouble() : Double.POSITIVE_INFINITY;
            if (fpObj instanceof IFlightplanRelatedObject) {
                maxElev -= offset;
                minElev -= offset;
            }

            if (max.isPresent()) {
                max = OptionalDouble.of(Math.max(max.getAsDouble(), maxElev));
            }

            if (min.isPresent()) {
                min = OptionalDouble.of(Math.min(min.getAsDouble(), minElev));
            }
        }

        return false;
    }

    @Override
    public void startVisit(IFlightplanRelatedObject fpObj) {
        CFlightplan fp = root.getFlightplan();
        if (fp != null) {
            offset = fp.getRefPointAltWgs84WithElevation();
        }

        super.startVisit(fpObj);
    }
}
