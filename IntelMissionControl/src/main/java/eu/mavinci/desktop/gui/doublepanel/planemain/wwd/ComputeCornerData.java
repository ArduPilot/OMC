/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.planemain.wwd;

import com.intel.missioncontrol.StaticInjector;
import com.intel.missioncontrol.hardware.IGenericCameraConfiguration;
import com.intel.missioncontrol.hardware.IHardwareConfiguration;
import com.intel.missioncontrol.map.elevation.ElevationModelRequestException;
import com.intel.missioncontrol.map.elevation.IElevationModel;
import com.intel.missioncontrol.map.worldwind.IWWGlobes;
import eu.mavinci.core.flightplan.CPhotoLogLine;
import eu.mavinci.core.flightplan.ReentryPointID;
import eu.mavinci.desktop.gui.doublepanel.calculator.AltitudeGsdCalculator;
import eu.mavinci.desktop.gui.doublepanel.camerasettings.CameraHelper;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Line;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Plane;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.util.RayCastingSupport;
import java.util.ArrayList;

public class ComputeCornerData {

    private boolean isMatchable;
    private Double startingElev;
    private boolean elevationDataReady;
    private boolean elevationDataSomeAvailable;
    private Double altOverCenter;
    private Position centerRayPosition;
    private Position shiftedPosOnLevel;
    private ArrayList<LatLon> groundProjectedCorners;
    private ArrayList<Position> idealCorners;
    private Position shiftedPosOnLevelPlus2;
    private Sector sector;
    private int lineNumber;
    private Vec4 cameraDirectionNormal;
    private double gsdToDistanceMultiplier;

    private static final IElevationModel elevationModel = StaticInjector.getInstance(IElevationModel.class);
    private static final Globe globe = StaticInjector.getInstance(IWWGlobes.class).getDefaultGlobe();

    public static interface IAerialPinholeImageContext {
        Double getStartingElevationOverWgs84();

        double getProjectionDistance();

        IHardwareConfiguration getHardwareConfiguration();

        LatLon getStartingPosition();

        Vec4 getRtkOffset();

        double getStartingElevationOverWgs84WithOffset();

        double getElevationOffset();
    }

    public static ComputeCornerData computeCorners(IAerialPinholeImageContext context, CPhotoLogLine line) {
        return computeCorners(context, line, 0);
    }

    public static ComputeCornerData computeCorners(
            IAerialPinholeImageContext context, CPhotoLogLine line, double additionalDelaySec) {
        IHardwareConfiguration hardwareConfiguration = context.getHardwareConfiguration();

        if (!hardwareConfiguration.hasPrimaryPayload(IGenericCameraConfiguration.class)) {
            return null;
        }

        if (line == null) {
            return null;
        }

        ComputeCornerData computeCornerData = new ComputeCornerData();
        computeCornerData.groundProjectedCorners = new ArrayList<LatLon>(4);
        computeCornerData.idealCorners = new ArrayList<Position>(4);
        AltitudeGsdCalculator calc = new AltitudeGsdCalculator(hardwareConfiguration);
        calc.setObject_res(1);
        computeCornerData.gsdToDistanceMultiplier = calc.getAlt();

        // System.out.println("----" + Thread.currentThread().getName()); // print out current thread for debug
        Vec4[] cornerDirections = CameraHelper.getCornerDirections(hardwareConfiguration);
        computeCornerData.startingElev = context.getStartingElevationOverWgs84();
        Position shiftedPos =
            CameraHelper.shiftPosition(
                line, 0, context.getRtkOffset(), additionalDelaySec, true, hardwareConfiguration);

        try {
            elevationModel.getElevation(shiftedPos);
            computeCornerData.elevationDataReady = true;
            computeCornerData.elevationDataSomeAvailable = true;
        } catch (ElevationModelRequestException e1) {
            computeCornerData.elevationDataReady = false; // automatically triggers retry
            computeCornerData.elevationDataSomeAvailable = (e1.achivedResMeter < 1000);
        }

        if (computeCornerData.startingElev == null) {
            computeCornerData.elevationDataSomeAvailable = false;
        }

        Matrix cameraTransform =
            CameraHelper.getCorrectedStateTransform(line, 0, additionalDelaySec, hardwareConfiguration);
        Vec4 cameraDirection = CameraHelper.getCenterDirection(hardwareConfiguration);
        cameraDirection = cameraDirection.transformBy3(cameraTransform);
        computeCornerData.cameraDirectionNormal = cameraDirection.normalize3();

        // try {
        if (computeCornerData.elevationDataSomeAvailable) {
            // recomputeCoverage shifted pos, since starting elevation has changed
            shiftedPos =
                CameraHelper.shiftPosition(
                    line,
                    computeCornerData.startingElev,
                    context.getRtkOffset(),
                    additionalDelaySec,
                    true,
                    hardwareConfiguration);

            LatLon startingPos;
            if (line.lonTakeoff == 0 && line.latTakeoff == 0) {
                startingPos = context.getStartingPosition();
            } else {
                startingPos = LatLon.fromDegrees(line.latTakeoff, line.lonTakeoff);
            }

            if (startingPos != null && computeCornerData.startingElev != null) {
                double altShift =
                    elevationModel.getElevationAsGoodAsPossible(startingPos) - computeCornerData.startingElev;
                shiftedPos = new Position(shiftedPos, shiftedPos.elevation + altShift + context.getElevationOffset());
            } else {
                shiftedPos = new Position(shiftedPos, shiftedPos.elevation + context.getElevationOffset());
            }

            Matrix m = globe.computeModelCoordinateOriginTransform(shiftedPos);
            Vec4 origin = globe.computePointFromPosition(shiftedPos);

            cameraDirection = cameraDirection.transformBy3(m);

            while (true) {
                try {
                    computeCornerData.centerRayPosition =
                        RayCastingSupport.intersectRayWithTerrain(globe, origin, cameraDirection, 100000, 0.02);
                    if (computeCornerData.centerRayPosition == null) {
                        break;
                    }

                    if (!Angle.isValidLatitude(computeCornerData.centerRayPosition.latitude.degrees)
                            || !Angle.isValidLongitude(computeCornerData.centerRayPosition.longitude.degrees)) {
                        computeCornerData.centerRayPosition = null;
                        break;
                    }

                    computeCornerData.altOverCenter =
                        origin.distanceTo3(globe.computePointFromPosition(computeCornerData.centerRayPosition));
                } catch (ArrayIndexOutOfBoundsException e) {
                    // we are right now removing one elevation model from a geoTIFF from the global list
                    // just retry!
                    continue;
                }

                break;
            }

            for (int i = 0; i != 4; i++) {
                cornerDirections[i] = cornerDirections[i].transformBy3(cameraTransform).transformBy3(m);
                Position cornerPosition;

                if (computeCornerData.groundProjectedCorners != null && computeCornerData.centerRayPosition != null) {
                    // if centerRayPosition is null we are not able to find terrain intersection for center ray... this
                    // makes it impossible to find intersections for ALL 4 image corners ==> for inspection datasets not
                    // trying to make further globe spanning raytraces saves a lot of time
                    while (true) {
                        try {
                            cornerPosition =
                                RayCastingSupport.intersectRayWithTerrain(
                                    globe, origin, cornerDirections[i], 100000, 0.02);
                        } catch (ArrayIndexOutOfBoundsException e) {
                            // we are right now removing one elevation model from a geoTIFF from the global list
                            // just retry!
                            continue;
                        }

                        break;
                    }

                    if (cornerPosition != null
                            && Angle.isValidLatitude(cornerPosition.latitude.degrees)
                            && Angle.isValidLongitude(cornerPosition.longitude.degrees)) {
                        computeCornerData.groundProjectedCorners.add(cornerPosition);
                    }
                }

                if (computeCornerData.idealCorners != null) {
                    computeCornerData.altOverCenter = null; // make this configureable?
                    double projectionDistance =
                        computeCornerData.altOverCenter != null
                            ? computeCornerData.altOverCenter
                            : context.getProjectionDistance();

                    Vec4 point = origin.add3(cornerDirections[i].normalize3().multiply3(projectionDistance));
                    cornerPosition = globe.computePositionFromPoint(point);
                    computeCornerData.idealCorners.add(cornerPosition);
                }
            }
        } else {
            Matrix m = globe.computeModelCoordinateOriginTransform(shiftedPos);
            double altOverStart = line.getAltInM();
            Plane pl = new Plane(0, 0, 1, 0);

            Vec4 origin = new Vec4(0, 0, altOverStart, 0); // NOT EQUAL TO THE UPPER ONE!!
            for (int i = 0; i < 4; i++) {
                cornerDirections[i] = cornerDirections[i].transformBy3(cameraTransform);

                Line ray = new Line(origin, cornerDirections[i]);
                if (pl.intersectDistance(ray) < 0) {
                    break;
                }

                Vec4 intersect = pl.intersect(ray);
                if (intersect == null) {
                    break;
                }

                if (computeCornerData.groundProjectedCorners != null) {
                    Position p = globe.computePositionFromPoint(intersect.transformBy4(m));
                    if (p != null
                            && Angle.isValidLatitude(p.latitude.degrees)
                            && Angle.isValidLongitude(p.longitude.degrees)) {
                        computeCornerData.groundProjectedCorners.add(p);
                    }
                }

                if (computeCornerData.idealCorners != null) {
                    Vec4 point =
                        origin.add3(cornerDirections[i].normalize3().multiply3(context.getProjectionDistance()));
                    Position cornerPosition = globe.computePositionFromPoint(point);
                    computeCornerData.idealCorners.add(cornerPosition);
                }
            }

            Line ray = new Line(origin, cameraDirection);
            if (pl.intersectDistance(ray) < 0) {
                computeCornerData.centerRayPosition = null;
                computeCornerData.altOverCenter = null;
            } else {
                Vec4 intersect = pl.intersect(ray);
                if (intersect == null) {
                    computeCornerData.centerRayPosition = null;
                    computeCornerData.altOverCenter = null;
                } else {
                    computeCornerData.altOverCenter = origin.distanceTo3(intersect);
                    computeCornerData.centerRayPosition = globe.computePositionFromPoint(intersect.transformBy4(m));
                }
            }
        }

        // } catch (NullPointerException e) {
        //    return false;
        // }

        if (additionalDelaySec == 0) {
            computeCornerData.shiftedPosOnLevel = shiftedPos; // +2 to make it drawn in front of track
            // FIXME... this might cause rendering issues in future...
            computeCornerData.shiftedPosOnLevelPlus2 =
                new Position(shiftedPos, shiftedPos.elevation + 0.02); // +2 to make it drawn in front of track
        }

        if (computeCornerData.groundProjectedCorners != null) {
            computeCornerData.sector = Sector.boundingSector(computeCornerData.groundProjectedCorners);
            computeCornerData.isMatchable = true;
        }

        if (computeCornerData.groundProjectedCorners != null && computeCornerData.groundProjectedCorners.size() != 4) {
            computeCornerData.groundProjectedCorners = null;
        }

        if (computeCornerData.idealCorners != null && computeCornerData.idealCorners.size() != 4) {
            computeCornerData.idealCorners = null;
        }

        return computeCornerData;
    }

    public Vec4 getCameraDirectionNormal() {
        return cameraDirectionNormal;
    }

    public double getGsdToDistanceMultiplier() {
        return gsdToDistanceMultiplier;
    }

    public ArrayList<LatLon> getGroundProjectedCorners() {
        return groundProjectedCorners;
    }

    public ArrayList<Position> getIdealCorners() {
        return idealCorners;
    }

    public Double getStartingElev() {
        return startingElev;
    }

    public boolean isElevationDataReady() {
        return elevationDataReady;
    }

    public boolean isElevationDataSomeAvailable() {
        return elevationDataSomeAvailable;
    }

    public Position getCenterRayPosition() {
        return centerRayPosition;
    }

    public Position getShiftedPosOnLevel() {
        return shiftedPosOnLevel;
    }

    public Double getAltOverCenter() {
        return altOverCenter;
    }

    public Position getShiftedPosOnLevelPlus2() {
        return shiftedPosOnLevelPlus2;
    }

    public Sector getSector() {
        return sector;
    }

    public boolean isMatchable() {
        return isMatchable;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void updateLineNumber(int lineNo) {
        lineNumber = ReentryPointID.isAutoPlanned(lineNo) ? ReentryPointID.getLineNumberPure(lineNo) : lineNo;
    }

}
