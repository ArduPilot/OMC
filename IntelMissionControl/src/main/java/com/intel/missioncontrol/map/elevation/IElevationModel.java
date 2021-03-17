/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.elevation;

import eu.mavinci.core.flightplan.IFlightplanPositionReferenced;
import eu.mavinci.core.helper.MinMaxPair;
import eu.mavinci.flightplan.ITransformationProvider;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.terrain.CompoundElevationModel;
import java.util.List;

public interface IElevationModel {

    public static final double MAX_SAMPLES_PER_DIRECTION = 30;
    public static final double MIN_LEVEL_OVER_GROUND = 0.1;

    // the minimal resolution this code will ever request from the underlying elevation model. prevents very detailed
    // tiles to be loaded!
    public static final double MIN_RESOLUTION_REQUEST_METER = 1.;

    public static final double ELEVATION_SAMPLE_DISTANCE = 30D; // m

    public static final double TINY_GROUND_ELEVATION = 5;

    Position getPositionOverGround(Position p);

    Position getPositionOverGround(Position p, double minLevelOverGround);

    Position getPositionOverGround(LatLon p);

    Position getPositionOnGround(LatLon p);

    Position getPositionOverGroundRelativeToGround(Position p);

    Position getPositionOverGroundRelativeToGround(Position p, double minLevelOverGround);

    Position renormPosition(Position position);

    double getElevation(Angle latitude, Angle longitude) throws ElevationModelRequestException;

    double getElevation(Angle latitude, Angle longitude, boolean doWarning) throws ElevationModelRequestException;

    double getElevationAsGoodAsPossible(double latitude, double longitude);

    double getElevationAsGoodAsPossible(Angle latitude, Angle longitude);

    MinMaxPair getMaxElevation(Sector sector);

    double getElevation(LatLon latLon) throws ElevationModelRequestException;

    double getElevation(LatLon latLon, boolean doWarning) throws ElevationModelRequestException;

    double getElevation(LatLon latLon, boolean doWarning, double resolution) throws ElevationModelRequestException;

    double getElevation(
            LatLon latLon, boolean doWarning, double resolution, CompoundElevationModel.ElevationModelRerence bestModel)
            throws ElevationModelRequestException;

    double getElevationAsGoodAsPossible(LatLon latLon);

    double getElevationAsGoodAsPossible(LatLon latLon, double resolution);

    MinMaxPair computeMinMaxElevation(Vec4 start, Vec4 end, ITransformationProvider trafo);

    MinMaxPair computeMinMaxElevation(LatLon p1, LatLon p2);

    MinMaxPair computeMinMaxElevation2(LatLon p1, LatLon p2);

    MinMaxPair computeMinMaxElevation(IFlightplanPositionReferenced p);

    MinMaxPair computeMinMaxElevation(LatLon p, double radius, MinMaxPair minMax);

    MinMaxPair computeMinMaxElevation(IFlightplanPositionReferenced p1, LatLon p2);

    MinMaxPair computeMinMaxElevationAssertElevP2(IFlightplanPositionReferenced p1, LatLon p2);

    MinMaxPair computeMinMaxElevation(IFlightplanPositionReferenced p1, IFlightplanPositionReferenced p2);

    MinMaxTrackDistanceAndAbsolute computeMinMaxTrackDistanceAndAbsolute(Position p1, Position p2);

    List<Position> sampleGroundAtPos(LatLon pos, double resolution);

    ElevationList computeElevationList(LatLon p1, LatLon p2, double sampleDistance);

    ElevationList computeElevationList(IFlightplanPositionReferenced p);

    ElevationList computeElevationList(LatLon p, double radius, ElevationList minMax);

    ElevationList computeElevationList(IFlightplanPositionReferenced p1, IFlightplanPositionReferenced p2);

    ElevationList getElevationList(Sector sector);

    /**
     * Indicates the radius in meters of the globe's ellipsoid at a location.
     *
     * @param location the location at which to determine radius.
     * @return The radius in meters of the globe's ellipsoid at the specified location.
     */
    double getRadiusAt(LatLon location);

    double getElevations(
            Sector sector,
            List<? extends LatLon> latlons,
            double targetResolution,
            double[] buffer,
            CompoundElevationModel.ElevationModelRerence bestModel);
}
