/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.elevation;

import static eu.mavinci.desktop.gui.wwext.LatLongInterpolationUtils.makeFastInterpolatorIfSafe;

import com.intel.missioncontrol.measure.Unit;
import eu.mavinci.core.flightplan.AltAssertModes;
import eu.mavinci.core.flightplan.CFlightplan;
import eu.mavinci.core.flightplan.IFlightplanPositionReferenced;
import eu.mavinci.core.helper.MinMaxPair;
import eu.mavinci.desktop.gui.wwext.LatLongInterpolationUtils;
import eu.mavinci.desktop.helper.MathHelper;
import eu.mavinci.flightplan.ITransformationProvider;
import eu.mavinci.flightplan.LandingPoint;
import eu.mavinci.flightplan.StartProcedure;
import eu.mavinci.flightplan.Waypoint;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Earth;
import gov.nasa.worldwind.terrain.CompoundElevationModel;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

public abstract class AbstractElevationModel implements IElevationModel {

    /**
     * Changing the altitude of a position such, that is at least @MIN_LEVEL_OVER_GROUND meters over the ground
     *
     * @param p
     * @return
     */
    public Position getPositionOverGround(Position p) {
        return getPositionOverGround(p, MIN_LEVEL_OVER_GROUND);
    }

    public Position getPositionOverGround(Position p, double minLevelOverGround) {
        double elev = getElevationAsGoodAsPossible(p) + minLevelOverGround;
        if (elev > p.elevation) {
            return new Position(p, elev);
        } else {
            return p;
        }
    }

    public Position getPositionOverGround(LatLon p) {
        double elev = getElevationAsGoodAsPossible(p) + MIN_LEVEL_OVER_GROUND;
        return new Position(p, elev);
    }

    public Position getPositionOnGround(LatLon p) {
        return new Position(p, getElevationAsGoodAsPossible(p));
    }

    public Position getPositionOverGroundRelativeToGround(Position p) {
        double elev = p.elevation - getElevationAsGoodAsPossible(p);
        if (elev < MIN_LEVEL_OVER_GROUND) {
            elev = MIN_LEVEL_OVER_GROUND;
        }

        return new Position(p, elev);
    }

    public Position getPositionOverGroundRelativeToGround(Position p, double minLevelOverGround) {
        double elev = p.elevation - getElevationAsGoodAsPossible(p);
        if (elev < minLevelOverGround) {
            elev = minLevelOverGround;
        }

        return new Position(p, elev);
    }

    /**
     * Transforming a Position object from sea level elevations to ground level elevations
     *
     * @param position
     * @return
     */
    public Position renormPosition(Position position) {
        double elev = position.elevation - getElevationAsGoodAsPossible(position);
        return new Position(position, elev);
    }

    public double getElevation(Angle latitude, Angle longitude) throws ElevationModelRequestException {
        return getElevation(latitude, longitude, true);
    }

    public double getElevation(Angle latitude, Angle longitude, boolean doWarning)
            throws ElevationModelRequestException {
        return getElevation(new LatLon(latitude, longitude), doWarning);
    }

    public double getElevationAsGoodAsPossible(double latitude, double longitude) {
        return getElevationAsGoodAsPossible(LatLon.fromDegrees(latitude, longitude));
    }

    public double getElevationAsGoodAsPossible(Angle latitude, Angle longitude) {
        try {
            return getElevation(latitude, longitude, false);
        } catch (ElevationModelRequestException e) {
            return e.achievedAltitude;
        }
    }

    public MinMaxPair getMaxElevation(Sector sector) {
        double sampleSpacing = ELEVATION_SAMPLE_DISTANCE / Earth.WGS84_EQUATORIAL_RADIUS; // from meters to radians
        MinMaxPair minMaxPair = new MinMaxPair();
        MinMaxPair stretchRes = new MinMaxPair();
        double sampleSpacingLat = Math.max(sector.getDeltaLatRadians() / MAX_SAMPLES_PER_DIRECTION, sampleSpacing);
        stretchRes.update(sampleSpacingLat / sampleSpacing);

        LinkedList<LatLon> in = new LinkedList<LatLon>();
        for (double lat = sector.getMinLatitude().radians;
            lat <= sector.getMaxLatitude().radians;
            lat += sampleSpacingLat) {
            double sampleSpacingLon = sampleSpacing / Math.cos(Math.toRadians(lat));
            if (!MathHelper.isValid(sampleSpacingLon)) {
                sampleSpacingLon = Math.PI * 2;
            }

            sampleSpacingLon = Math.max(sector.getDeltaLonRadians() / MAX_SAMPLES_PER_DIRECTION, sampleSpacingLon);
            stretchRes.update(sampleSpacingLon / sampleSpacing);

            for (double lon = sector.getMinLongitude().radians;
                lon <= sector.getMaxLongitude().radians;
                lon += sampleSpacingLon) {
                in.add(LatLon.fromRadians(lat, lon));
            }
        }

        for (LatLon latLon : sector.getCorners()) {
            in.add(latLon);
        }

        in.add(sector.getCentroid());

        double[] out = new double[in.size()];
        getElevations(sector, in, sampleSpacing * stretchRes.max, out, null);
        for (double e : out) {
            minMaxPair.update(e);
        }

        return minMaxPair;
    }

    public double getElevation(LatLon latLon) throws ElevationModelRequestException {
        return getElevation(latLon, true);
    }

    public double getElevation(LatLon latLon, boolean doWarning) throws ElevationModelRequestException {
        return getElevation(latLon, doWarning, MIN_RESOLUTION_REQUEST_METER, null);
    }

    public double getElevation(LatLon latLon, boolean doWarning, double resolution)
            throws ElevationModelRequestException {
        return getElevation(latLon, doWarning, resolution, null);
    }

    public double getElevation(
            LatLon latLon, boolean doWarning, double resolution, CompoundElevationModel.ElevationModelRerence bestModel)
            throws ElevationModelRequestException {
        // looks somehow complicated, but with the easy method we have no chance to force the model to high accuricy
        if (Double.isNaN(latLon.latitude.degrees) || Double.isNaN(latLon.longitude.degrees)) {
            throw new RuntimeException("LatLon contains inf values :" + latLon);
        }

        Sector sec = new Sector(latLon.latitude, latLon.latitude, latLon.longitude, latLon.longitude);
        double bestRes = Math.max(getBestResolution(sec), resolution / Earth.WGS84_EQUATORIAL_RADIUS);

        LinkedList<LatLon> in = new LinkedList<LatLon>();
        in.add(latLon);
        double[] out = new double[1];
        double ret = getElevations(sec, in, bestRes, out, bestModel);
        double tmp = out[0];

        if (doWarning) {
            if (ret > bestRes) {
                throw new ElevationModelRequestException(ret / bestRes, tmp, ret * getRadiusAt(latLon), latLon);
            }

            if (tmp < -1e3) {
                throw new ElevationModelRequestException(latLon);
            }

            if (tmp > 1e5) {
                throw new ElevationModelRequestException(latLon);
            }
        }

        return tmp;
    }

    public double getElevationAsGoodAsPossible(LatLon latLon) {
        try {
            return getElevation(latLon, false);
        } catch (ElevationModelRequestException e) {
            return e.achievedAltitude;
        }
    }

    public double getElevationAsGoodAsPossible(LatLon latLon, double resolution) {
        try {
            return getElevation(latLon, false, resolution);
        } catch (ElevationModelRequestException e) {
            return e.achievedAltitude;
        }
    }

    public MinMaxPair computeMinMaxElevation(Vec4 start, Vec4 end, ITransformationProvider trafo) {
        return computeMinMaxElevation(trafo.transformToGlobe(start), trafo.transformToGlobe(end));
    }

    public MinMaxPair computeMinMaxElevation(LatLon p1, LatLon p2) {
        MinMaxPair minMaxElevation = new MinMaxPair();
        if (p1.equals(p2)) {
            minMaxElevation.update(getElevationAsGoodAsPossible(p1));
            return minMaxElevation;
        }

        LatLongInterpolationUtils.LatLongPairInterpolator inter = makeFastInterpolatorIfSafe(p1, p2);
        inter.sampleAtDistance(
            ELEVATION_SAMPLE_DISTANCE,
            latLon -> {
                minMaxElevation.update(getElevationAsGoodAsPossible(latLon));
            });

        return minMaxElevation;
    }

    public MinMaxPair computeMinMaxElevation2(LatLon p1, LatLon p2) {
        MinMaxPair minMaxElevation = new MinMaxPair(getElevationAsGoodAsPossible(p1));
        if (p1.equals(p2)) {
            return minMaxElevation;
        }

        double size = LatLon.greatCircleDistance(p1, p2).radians;
        Angle azimuth = LatLon.greatCircleAzimuth(p1, p2);
        int steps = (int)Math.ceil(size / (ELEVATION_SAMPLE_DISTANCE / Earth.WGS84_EQUATORIAL_RADIUS));
        double step = size / steps;
        double x = 0;
        for (int i = 1; i < steps; i++) {
            x += step;
            LatLon p = LatLon.greatCircleEndPosition(p1, azimuth, Angle.fromRadians(x));
            minMaxElevation.update(getElevationAsGoodAsPossible(p));
        }

        minMaxElevation.update(getElevationAsGoodAsPossible(p2));
        return minMaxElevation;
    }

    public MinMaxPair computeMinMaxElevation(IFlightplanPositionReferenced p) {
        LatLon latLon = LatLon.fromDegrees(p.getLat(), p.getLon());
        MinMaxPair minMax = new MinMaxPair(getElevationAsGoodAsPossible(latLon));
        CFlightplan flightplan = p.getFlightplan();
        double turnRadius =
            flightplan
                .getHardwareConfiguration()
                .getPlatformDescription()
                .getTurnRadius()
                .convertTo(Unit.METER)
                .getValue()
                .doubleValue();
        if (p instanceof Waypoint) {
            Waypoint wp = (Waypoint)p;
            if (wp.getAssertAltitudeMode() != AltAssertModes.unasserted) {
                computeMinMaxElevation(latLon, turnRadius, minMax);
            }

            if (wp.isCirceling()) {
                computeMinMaxElevation(latLon, wp.getRadiusWithinM(), minMax);
            }
        } else if (p instanceof StartProcedure) {
            computeMinMaxElevation(latLon, turnRadius, minMax);
        } else if (p instanceof LandingPoint) {
            computeMinMaxElevation(latLon, turnRadius, minMax);
        }

        return minMax;
    }

    public MinMaxPair computeMinMaxElevation(LatLon p, double radius, MinMaxPair minMax) {
        if (minMax == null) {
            minMax = new MinMaxPair();
        }

        double umfang = Math.PI * 2 * radius;
        int steps = Math.max(4, (int)Math.ceil(umfang / ELEVATION_SAMPLE_DISTANCE));
        double step = umfang / steps;
        double x = 0;
        Angle len = Angle.fromRadians(radius / getRadiusAt(p));
        for (int i = 1; i < steps; i++) {
            x += step;
            Angle azimuth = Angle.fromDegrees(x);
            LatLon pEnd = LatLon.greatCircleEndPosition(p, azimuth, len);
            minMax.update(getElevationAsGoodAsPossible(pEnd));
        }

        return minMax;
    }

    public MinMaxPair computeMinMaxElevation(IFlightplanPositionReferenced p1, LatLon p2) {
        MinMaxPair minMax = computeMinMaxElevation(LatLon.fromDegrees(p1.getLat(), p1.getLon()), p2);
        minMax.enlarge(computeMinMaxElevation(p1));
        minMax.enlarge(getElevationAsGoodAsPossible(p2));
        return minMax;
    }

    public MinMaxPair computeMinMaxElevationAssertElevP2(IFlightplanPositionReferenced p1, LatLon p2) {
        MinMaxPair minMax = computeMinMaxElevation(LatLon.fromDegrees(p1.getLat(), p1.getLon()), p2);
        minMax.enlarge(computeMinMaxElevation(p1));
        double turnRadius =
            p1.getFlightplan()
                .getHardwareConfiguration()
                .getPlatformDescription()
                .getTurnRadius()
                .convertTo(Unit.METER)
                .getValue()
                .doubleValue();
        computeMinMaxElevation(p2, turnRadius, minMax);
        return minMax;
    }

    public MinMaxTrackDistanceAndAbsolute computeMinMaxTrackDistanceAndAbsolute(Position p1, Position p2) {
        LatLongInterpolationUtils.LatLongPairInterpolator inter = makeFastInterpolatorIfSafe(p1, p2);

        MinMaxPair minMaxDistanceToGround = new MinMaxPair();
        MinMaxPair minMaxGroundHeight = new MinMaxPair();

        inter.sampleAtDistance(
            ELEVATION_SAMPLE_DISTANCE,
            latLon -> {
                Position p = (Position)latLon;
                double elev = getElevationAsGoodAsPossible(latLon);
                minMaxDistanceToGround.update(p.getElevation() - elev);
                minMaxGroundHeight.update(elev);
            });

        return new MinMaxTrackDistanceAndAbsolute(minMaxDistanceToGround, minMaxGroundHeight);
    }

    public MinMaxPair computeMinMaxElevation(IFlightplanPositionReferenced p1, IFlightplanPositionReferenced p2) {
        MinMaxPair minMax =
            computeMinMaxElevation(
                LatLon.fromDegrees(p1.getLat(), p1.getLon()), LatLon.fromDegrees(p2.getLat(), p2.getLon()));
        minMax.enlarge(computeMinMaxElevation(p1));
        minMax.enlarge(computeMinMaxElevation(p2));
        return minMax;
    }

    public ElevationList computeElevationList(LatLon p1, LatLon p2, double sampleDistance) {
        final ElevationList elev = new ElevationList();
        if (p1.equals(p2)) {
            elev.add(p1, getElevationAsGoodAsPossible(p1));
            return elev;
        }

        LatLongInterpolationUtils.LatLongPairInterpolator inter = makeFastInterpolatorIfSafe(p1, p2);
        inter.sampleAtDistance(
            sampleDistance,
            latLon -> {
                elev.add(latLon, getElevationAsGoodAsPossible(latLon));
            });

        return elev;
    }

    public ElevationList computeElevationList(IFlightplanPositionReferenced p) {
        LatLon latLon = LatLon.fromDegrees(p.getLat(), p.getLon());
        ElevationList minMax = new ElevationList();
        minMax.add(latLon, getElevationAsGoodAsPossible(latLon));
        CFlightplan flightplan = p.getFlightplan();
        double turnRadius =
            flightplan
                .getHardwareConfiguration()
                .getPlatformDescription()
                .getTurnRadius()
                .convertTo(Unit.METER)
                .getValue()
                .doubleValue();
        if (p instanceof Waypoint) {
            Waypoint wp = (Waypoint)p;
            if (wp.getAssertAltitudeMode() != AltAssertModes.unasserted) {
                computeElevationList(latLon, turnRadius, minMax);
            }

            if (wp.isCirceling()) {
                computeElevationList(latLon, wp.getRadiusWithinM(), minMax);
            }
        } else if (p instanceof StartProcedure) {
            computeElevationList(latLon, turnRadius, minMax);
        } else if (p instanceof LandingPoint) {
            computeElevationList(latLon, turnRadius, minMax);
        }

        return minMax;
    }

    public ElevationList computeElevationList(LatLon p, double radius, ElevationList minMax) {
        if (minMax == null) {
            minMax = new ElevationList();
        }

        double umfang = Math.PI * 2 * radius;
        int steps = Math.max(4, (int)Math.ceil(umfang / ELEVATION_SAMPLE_DISTANCE));
        double step = umfang / steps;
        double x = 0;
        Angle len = Angle.fromRadians(radius / getRadiusAt(p));
        for (int i = 1; i < steps; i++) {
            x += step;
            Angle azimuth = Angle.fromDegrees(x);
            LatLon pEnd = LatLon.greatCircleEndPosition(p, azimuth, len);
            minMax.add(pEnd, getElevationAsGoodAsPossible(pEnd));
        }

        return minMax;
    }

    public ElevationList computeElevationList(IFlightplanPositionReferenced p1, IFlightplanPositionReferenced p2) {
        ElevationList minMax =
            computeElevationList(
                LatLon.fromDegrees(p1.getLat(), p1.getLon()),
                LatLon.fromDegrees(p2.getLat(), p2.getLon()),
                ELEVATION_SAMPLE_DISTANCE);
        minMax.add(computeElevationList(p1));
        minMax.add(computeElevationList(p2));
        return minMax;
    }

    public ElevationList getElevationList(Sector sector) {
        ElevationList list = new ElevationList();

        double sampleSpacing = ELEVATION_SAMPLE_DISTANCE / Earth.WGS84_EQUATORIAL_RADIUS; // from meters to radians
        MinMaxPair stretchRes = new MinMaxPair();
        double sampleSpacingLat = Math.max(sector.getDeltaLatRadians() / MAX_SAMPLES_PER_DIRECTION, sampleSpacing);
        stretchRes.update(sampleSpacingLat / sampleSpacing);

        LinkedList<LatLon> in = new LinkedList<LatLon>();
        for (double lat = sector.getMinLatitude().radians;
            lat <= sector.getMaxLatitude().radians;
            lat += sampleSpacingLat) {
            double sampleSpacingLon = sampleSpacing / Math.cos(Math.toRadians(lat));
            if (!MathHelper.isValid(sampleSpacingLon)) {
                sampleSpacingLon = Math.PI * 2;
            }

            sampleSpacingLon = Math.max(sector.getDeltaLonRadians() / MAX_SAMPLES_PER_DIRECTION, sampleSpacingLon);
            stretchRes.update(sampleSpacingLon / sampleSpacing);

            for (double lon = sector.getMinLongitude().radians;
                lon <= sector.getMaxLongitude().radians;
                lon += sampleSpacingLon) {
                in.add(LatLon.fromRadians(lat, lon));
            }
        }

        for (LatLon latLon : sector.getCorners()) {
            in.add(latLon);
        }

        in.add(sector.getCentroid());

        double[] out = new double[in.size()];
        for (int i = 0; i != out.length; i++) {
            list.add(in.get(i), out[i]);
        }

        return list;
    }

    /**
     *
     * @param pos
     * @param resolution
     * @return (dLat, dLon):(0,0)(-1,-1)(-1,0)(-1,1)(0,-1)(0,1)(1,-1)(1,0)(1,1)
     */
    public List<Position> sampleGroundAtPos(LatLon pos, double resolution) {
        double resolutionRadLat = resolution / Earth.WGS84_EQUATORIAL_RADIUS; // from meters to radians
        double resolutionRadLon = resolutionRadLat / pos.latitude.cos();

        ArrayList<LatLon> in = new ArrayList<LatLon>();
        in.add(pos);
        for (int iLat = -1; iLat != 2; iLat++) {
            double lat = pos.latitude.radians + iLat * resolutionRadLat;
            for (int iLon = -1; iLon != 2; iLon++) {
                if (iLon == 0 && iLat == 0) {
                    continue;
                }

                double lon = pos.longitude.radians + iLon * resolutionRadLon;
                in.add(LatLon.fromRadians(lat, lon));
            }
        }

        Sector sector =
            Sector.fromRadians(
                pos.latitude.radians - resolutionRadLat,
                pos.latitude.radians + resolutionRadLat,
                pos.longitude.radians - resolutionRadLon,
                pos.longitude.radians + resolutionRadLon);

        double[] out = new double[in.size()];
        getElevations(sector, in, resolutionRadLat, out, null);
        Vector<Position> vec = new Vector<Position>(out.length);
        for (int i = 0; i != out.length; i++) {
            vec.add(new Position(in.get(i), out[i]));
        }

        return vec;
    }

    protected abstract double getBestResolution(Sector sector);

}
