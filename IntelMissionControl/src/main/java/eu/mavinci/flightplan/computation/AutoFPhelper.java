/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.flightplan.computation;

import com.intel.missioncontrol.hardware.IGenericCameraConfiguration;
import com.intel.missioncontrol.hardware.IPlatformDescription;
import com.intel.missioncontrol.helper.Ensure;
import com.intel.missioncontrol.measure.Unit;
import eu.mavinci.core.flightplan.AFlightplanContainer;
import eu.mavinci.core.flightplan.AltAssertModes;
import eu.mavinci.core.flightplan.AltitudeAdjustModes;
import eu.mavinci.core.flightplan.CFlightplan;
import eu.mavinci.core.flightplan.CPhotoSettings;
import eu.mavinci.core.flightplan.CWaypoint;
import eu.mavinci.core.flightplan.FlightplanContainerFullException;
import eu.mavinci.core.flightplan.FlightplanContainerWrongAddingException;
import eu.mavinci.core.flightplan.FlightplanSpeedModes;
import eu.mavinci.core.flightplan.IFlightplanRelatedObject;
import eu.mavinci.core.flightplan.Orientation;
import eu.mavinci.core.flightplan.ReentryPointID;
import eu.mavinci.core.helper.MinMaxPair;
import eu.mavinci.core.helper.Pair;
import eu.mavinci.desktop.helper.MathHelper;
import eu.mavinci.desktop.main.debug.Debug;
import eu.mavinci.flightplan.Photo;
import eu.mavinci.flightplan.Waypoint;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Line;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;

public class AutoFPhelper {

    /**
     * @param flightLines
     * @param alt
     * @param sizeInFlightEff
     * @param sizeInFlightEffMax
     * @param nextFreeID
     * @param cellNo number of cell in that row. -1 means we are not in an cell, just in a NON multi FP
     * @param onlyInOneDirection
     * @param cont
     * @throws FlightplanContainerFullException
     * @throws FlightplanContainerWrongAddingException
     */
    public static double fillContainer(
            Vector<FlightLine> flightLines,
            // Vector<FlightLine> flightLines,
            double alt,
            double sizeInFlightEff,
            double sizeInFlightEffMax,
            double sizeParallelFlightEff,
            int nextFreeID,
            int nextFreeLineNo,
            int cellNo,
            int rowNo,
            boolean onlyInOneDirection,
            double gsdToleranceDefault,
            AFlightplanContainer cont,
            AltitudeAdjustModes shiftAltitudes,
            IPlatformDescription platformDescription,
            IGenericCameraConfiguration cameraConfiguration,
            double aoiYaw,
            boolean cameraTiltToggleEnable,
            double cameraTiltToggleDegrees,
            double imageCaptureSpeed,
            double cameraPitchOffsetDegrees,
            boolean cameraRollToggleEnable,
            double cameraRollToggleDegrees,
            double cameraRollOffsetDegrees,
            double pitchOffsetLineBegin,
            boolean keepPointOnTargetContantOnRotations)
            throws FlightplanContainerFullException, FlightplanContainerWrongAddingException {

        // Debug.printStackTrace();

        double startElev = 0;
        double gsdTolerance = 0;
        CFlightplan fp = cont.getFlightplan();
        CPhotoSettings photoSettings = fp.getPhotoSettings();
        double maxSpeed = photoSettings.getMaxGroundSpeedMPSec();
        FlightplanSpeedModes speedMode = photoSettings.getMaxGroundSpeedAutomatic();
        boolean forceTriggerIndividualImages =
            photoSettings.isStoppingAtWaypoints() && speedMode == FlightplanSpeedModes.MANUAL_CONSTANT;
        // TODO.. in this case we dont have to wait sooo long until image is stored, we could wait shorter?
        double minTimeInterval = forceTriggerIndividualImages ? photoSettings.getMinTimeInterval() * 1000 : 0;

        if (!platformDescription.getAPtype().makesNoImagesAfterEnablingCamByDefault()) {
            cont.addToFlightplanContainer(
                new Photo(
                    true,
                    Photo.MAX_DISTANCE,
                    Photo.MAX_DISTANCE,
                    ReentryPointID.createID(false, false, true, false, -1, cellNo, nextFreeID++)));
        }

        startElev = fp.getRefPointAltWgs84WithElevation();
        final double minGroundDistInM =
            platformDescription.getMinGroundDistance().convertTo(Unit.METER).getValue().doubleValue();
        final double turnRadius = platformDescription.getTurnRadius().convertTo(Unit.METER).getValue().doubleValue();
        /*
                // fix landing alt for terrain mode
                if (shiftAltitudes.usesAbsoluteHeights()) {
                    LandingPoint landP = (LandingPoint)(fp.getLandingpoint());
                    double altLandingP = EarthElevationModel.getElevationAsGoodAsPossible(landP.getLatLon());
                    altLandingP -= startElev;
                    // System.out.println("landP: oldAlt:" + landP.getAltInternalWithinM());
                    // System.out.println("ground:"+altLandingP);
                    if (landP.getMode() == LandingModes.DESC_FULL3d) {
                        altLandingP =
                            (float)
                                Math.max(
                                    landP.getAltInMAboveFPRefPoint(),
                                    altLandingP + minGroundDistInM + EarthElevationModel.TINY_GROUND_ELEVATION);
                    } else {
                        altLandingP =
                            (float)
                                Math.max(
                                    altLandingP + alt,
                                    altLandingP + minGroundDistInM + EarthElevationModel.TINY_GROUND_ELEVATION);
                    }
                    // System.out.println("newAltLanding:"+altLandingP);
                    landP.setAltInMAboveFPRefPoint(altLandingP);
                }

                // System.out.println("startElev:" + startElev);
                // create route to first waypoint, and assert its altitude
                PreviousOfTypeVisitor vis = new PreviousOfTypeVisitor(cont, IFlightplanPositionReferenced.class);
                vis.setSkipIgnoredPaths(true);
                vis.startVisit(fp);

                if (vis.prevObj instanceof LandingPoint && fp.getOrigin().isDefined()) {
                    // disabling legacy handling of landing point as takeoff location
                    vis.prevObj = null;
                }

                if (vis.prevObj instanceof IFlightplanDeactivateable) {
                    IFlightplanDeactivateable prev = (IFlightplanDeactivateable)vis.prevObj;
                    if (!prev.isActive()) {
                        vis.prevObj = null;
                    }
                }

                // there should be no waypoint on top of the origin
                // if (vis.prevObj == null && fp.getOrigin().isDefined()) {
                //    vis.prevObj = fp.getOrigin();
                // }

                if (vis.prevObj != null && !flightLines.isEmpty()) {
                    IFlightplanPositionReferenced prev = (IFlightplanPositionReferenced)vis.prevObj;

                    // System.out.println("prev Obj: " + prev);
                    Angle prevHeading = null;

                    LatLon pPrev = LatLon.fromDegrees(prev.getLat(), prev.getLon());
                    LatLon addPos;
                    LatLon p2 = transformator.transformToGlobe(flightLines.get(0).getCorners().get(0));
                    if (turnRadius > 0) {
                        LatLon p3 = transformator.transformToGlobe(flightLines.get(0).getCorners().get(1));
                        Angle lineHeading =
                            LatLon.ellipsoidalForwardAzimuth(p2, p3, Earth.WGS84_EQUATORIAL_RADIUS, Earth.WGS84_POLAR_RADIUS);

                        prevHeading =
                            LatLon.ellipsoidalForwardAzimuth(
                                p2, pPrev, Earth.WGS84_EQUATORIAL_RADIUS, Earth.WGS84_POLAR_RADIUS);
                        prevHeading = prevHeading.subtract(lineHeading).normalizedLongitude();
                        if (prevHeading.degrees < 0) {
                            prevHeading = Angle.NEG90;
                        } else {
                            prevHeading = Angle.POS90;
                        }

                        prevHeading = prevHeading.add(lineHeading).normalizedLongitude();
                        // System.out.println("turnRad:"+cam.getTurnRadius());
                        addPos =
                            LatLon.greatCircleEndPosition(
                                p2,
                                prevHeading,
                                Angle.fromRadians(
                                    (shiftAltitudes.usesAbsoluteHeights() ? 2 : 1)
                                        * turnRadius
                                        / WWFactory.getGlobe().getRadiusAt(p2)));
                        // System.out.println("line heading: " + lineHeading + " prevHEad:" +prevHeading + " p2:"+p2 + "
                        // adPos"+addPos);
                        // System.out.println(p2);
                        // System.out.println(addPos);
                    } else {
                        addPos = p2;
                    }

                    MinMaxPair minMax;
                    if (shiftAltitudes.usesAbsoluteHeights()) {
                        minMax = EarthElevationModel.computeMinMaxElevationAssertElevP2(prev, addPos);
                        // System.out.println("from " + prev + " to " + addPos + " alt: " +minMax);
                        minMax.shift(-startElev);
                        minMax.shift(minGroundDistInM + EarthElevationModel.TINY_GROUND_ELEVATION); // now this contains
                        // elevation model
                        // raltive to start plus safty
                        // distance

                        if (minMax.max > prev.getAltInMAboveFPRefPoint()) {
                            // take care that the previous point (typically over the starting procedure) is high enough..
                            Waypoint wp =
                                new Waypoint(
                                    prev.getLon(),
                                    prev.getLat(),
                                    (float)minMax.max,
                                    AltAssertModes.jump,
                                    0,
                                    CWaypoint.DEFAULT_BODY,
                                    ReentryPointID.createID(false, false, true, false, -1, cellNo, nextFreeID++),
                                    null);
                            wp.setSpeedMpSec(maxSpeed);
                            cont.addToFlightplanContainer(wp);
                            // System.out.println("A:"+cont.getFromFlightplanContainer(cont.sizeOfFlightplanContainer()-1));
                        }
                    } else {
                        minMax = new MinMaxPair(alt);
                    }

                    if (addPos != p2) {
                        // System.out.println("adding new line header at oldAlt " + alt + " -> " + minMax.max);
                        // this point is perpendicular shifted point next to the first point of this PicArea, but maybe on another altitude
                        Waypoint wp =
                            new Waypoint(
                                addPos.getLongitude().degrees,
                                addPos.getLatitude().degrees,
                                (float)minMax.max,
                                AltAssertModes.jump,
                                0,
                                CWaypoint.DEFAULT_BODY,
                                ReentryPointID.createID(false, false, true, false, -1, cellNo, nextFreeID++),
                                null);
                        wp.setAssertYawOn(true);
                        wp.setSpeedMpSec(maxSpeed);
                        Ensure.notNull(prevHeading, "prevHeading");
                        wp.setAssertYaw(
                            prevHeading.degrees
                                + 90
                                + 10); // 10 degrees additionally to make sure he is allready behind the devision
                        // point and is not accidentally trying to make another turn in the wrong
                        // direction
                        cont.addToFlightplanContainer(wp);

                        // System.out.println("B:"+cont.getFromFlightplanContainer(cont.sizeOfFlightplanContainer()-1));
                    }
                }
        */
        LatLon lastEnd = null;

        for (int i = 0; i < flightLines.size(); i++) {
            float lineAlt = (float)alt;
            FlightLine fl = flightLines.get(i);

            // System.out.println("----flight Line:"+i +" id:" +fl.getLineID());

            // System.out.println("i="+i);
            // System.out.println("fl-Forward:" + i + " \t"+isForward + " "+fl.parallelCoordinate);

            // Position p1 = transformator.transformToGlobe(fl.corners.get(0));
            // Position p2 = transformator.transformToGlobe(fl.corners.get(1));

            Position pNext = null;
            if (i + 1 < flightLines.size()) {
                // NOT last of all flight lines
                FlightLine flNext = flightLines.get(i + 1);
                pNext = fl.getTrafo().transformToGlobe(flNext.getCorners().firstElement());
            }

            RefineResult refined =
                fl.refine(
                    shiftAltitudes,
                    platformDescription,
                    startElev,
                    lineAlt,
                    pNext,
                    lastEnd,
                    sizeInFlightEff,
                    sizeInFlightEffMax,
                    sizeParallelFlightEff,
                    maxSpeed
                        * cameraConfiguration
                            .getLens()
                            .getDescription()
                            .getMinRepTime()
                            .convertTo(Unit.SECOND)
                            .getValue()
                            .doubleValue(),
                    gsdToleranceDefault);
            gsdTolerance = Math.max(gsdTolerance, refined.realToleranceGSD);

            if (i == 0 && shiftAltitudes.usesAbsoluteHeights()) {
                Waypoint previous = cont.sizeOfFlightplanContainer() == 0 ? null : (Waypoint)cont.getLastElement();
                if (refined.positions.isEmpty()) {
                    return 0;
                }

                Position first = refined.positions.get(0);
                double firstAlt = first.elevation;
                // check if the previous element in the container is on a lower level, if yes, just shift it up,
                // if it was higher, add an additional lower node, to circle down after flying over the hill
                if (previous == null) {
                } else if (turnRadius <= 0) {
                    if (previous.getAltInMAboveFPRefPoint() > firstAlt) {
                        Waypoint wp =
                            new Waypoint(
                                first.getLongitude().degrees,
                                first.getLatitude().degrees,
                                previous.getAltInMAboveFPRefPoint(),
                                AltAssertModes.jump,
                                0,
                                CWaypoint.DEFAULT_BODY,
                                ReentryPointID.createID(
                                    false,
                                    false,
                                    fl.isForward(),
                                    fl.isRot90(),
                                    fl.getLineID() + nextFreeLineNo,
                                    fl.isRot90() ? cellNo : rowNo,
                                    nextFreeID++),
                                null);
                        wp.setSpeedMpSec(maxSpeed);
                        cont.addToFlightplanContainer(wp);
                    }
                } else if (previous.getAltInMAboveFPRefPoint() < firstAlt) {
                    previous.setAltInMAboveFPRefPoint(firstAlt);
                } else {
                    Waypoint wp =
                        new Waypoint(
                            previous.getLon(),
                            previous.getLat(),
                            firstAlt,
                            AltAssertModes.jump,
                            0,
                            CWaypoint.DEFAULT_BODY,
                            ReentryPointID.createID(
                                false,
                                false,
                                fl.isForward(),
                                fl.isRot90(),
                                fl.getLineID() + nextFreeLineNo,
                                fl.isRot90() ? cellNo : rowNo,
                                nextFreeID++),
                            null);
                    wp.setSpeedMpSec(maxSpeed);
                    cont.addToFlightplanContainer(wp);
                }
            }

            if (i == 0 && platformDescription.planIndividualImagePositions()) {
                int idPhoto =
                    ReentryPointID.createID(
                        false,
                        true,
                        true,
                        fl.isRot90(),
                        fl.getLineID() + nextFreeLineNo,
                        fl.isRot90() ? cellNo : rowNo,
                        -1);
                Photo photo = new Photo(true, sizeInFlightEff * 100, sizeInFlightEffMax * 100, idPhoto, null);
                photo.setTriggerOnlyOnWaypoints(platformDescription.planIndividualImagePositions());
                cont.addToFlightplanContainer(photo);
            }

            // System.out.println("refined size:" + refined.positions.size());
            int max = refined.positions.size() - 1;
            for (int k = 0; k <= max; k++) {
                Position pos = refined.positions.get(k);

                AltAssertModes altAssertMode;
                if (platformDescription.planIndividualImagePositions()) {
                    altAssertMode = AltAssertModes.linear;
                } else {
                    if (k == 0) {
                        altAssertMode = AltAssertModes.jump;
                    } else {
                        if (shiftAltitudes == AltitudeAdjustModes.FOLLOW_TERRAIN) {
                            altAssertMode = AltAssertModes.linear;
                        } else {
                            altAssertMode = AltAssertModes.unasserted;
                        }
                    }
                }

                boolean isThisSegmentForward = k == 0 || !onlyInOneDirection;

                boolean isLast = max == k;
                Waypoint wp =
                    new Waypoint(
                        pos.getLongitude().degrees,
                        pos.getLatitude().degrees,
                        pos.elevation,
                        altAssertMode,
                        0,
                        CWaypoint.DEFAULT_BODY,
                        ReentryPointID.createID(
                            false,
                            k != 0,
                            fl.isForward(),
                            fl.isRot90(),
                            fl.getLineID() + nextFreeLineNo,
                            fl.isRot90() ? cellNo : rowNo,
                            isLast ? ReentryPointID.maxNoRefinements - 1 : k),
                        null);
                wp.setTriggerImageHereCopterMode(true);
                wp.setStopHereTimeCopter(minTimeInterval);
                wp.setSpeedMpSec(imageCaptureSpeed);
                if (k == 0) {
                    wp.setBeginFlightline(true);
                }

                if (platformDescription.planIndividualImagePositions()) {
                    if (refined.orientations.isEmpty()) {
                        wp.setCamYaw(aoiYaw);
                    } else {
                        Orientation tmp = refined.orientations.get(k);
                        wp.setOrientation(tmp);
                    }
                    // System.out.println("rollPitchYaw:" + tmp[0] + " " + tmp[1] + " " + tmp[2]);
                }

                cont.addToFlightplanContainer(wp);

                if (k == 0 && !platformDescription.planIndividualImagePositions()) {
                    int idPhoto =
                        ReentryPointID.createID(
                            false,
                            k != 0,
                            isThisSegmentForward,
                            fl.isRot90(),
                            fl.getLineID() + nextFreeLineNo,
                            fl.isRot90() ? cellNo : rowNo,
                            -1);
                    Photo photo = null;
                    if (shiftAltitudes.usesAbsoluteHeights()) {
                        photo =
                            new Photo(
                                true,
                                sizeInFlightEff * 100 * refined.groundDistance.min / alt,
                                sizeInFlightEffMax * 100 * refined.groundDistance.min / alt,
                                idPhoto,
                                null);
                    } else if (i == 0) {
                        photo = new Photo(true, sizeInFlightEff * 100, sizeInFlightEffMax * 100, idPhoto, null);
                    }

                    if (photo != null) {
                        photo.setTriggerOnlyOnWaypoints(platformDescription.planIndividualImagePositions());
                        cont.addToFlightplanContainer(photo);
                    }
                }
            }

            lastEnd = refined.positions.get(max);
        }

        // line direction
        cont.addToFlightplanContainer(
            new Photo(
                false,
                sizeInFlightEff * 100,
                sizeInFlightEffMax * 100,
                ReentryPointID.createID(false, false, true, false, -1, cellNo, nextFreeID++),
                null));

        if (cameraPitchOffsetDegrees != 0 || cameraRollOffsetDegrees != 0) {
            for (IFlightplanRelatedObject fpObj : cont) {
                if (fpObj instanceof Waypoint) {
                    Waypoint wpTog = (Waypoint)fpObj;
                    Orientation oldOrientation = wpTog.getOrientation();
                    Orientation newOrientation = oldOrientation.clone();
                    newOrientation.setPitch(oldOrientation.getPitch() + cameraPitchOffsetDegrees);

                    if (keepPointOnTargetContantOnRotations) {
                        boolean useRoll = distanceToPole(oldOrientation.getPitch()) < 45;
                        if (useRoll) {
                            newOrientation.setRoll(oldOrientation.getRoll() + cameraRollOffsetDegrees);
                        } else {
                            newOrientation.setYaw(oldOrientation.getYaw() + cameraRollOffsetDegrees);
                        }

                        double targetDistance = wpTog.getTargetDistance();
                        if (targetDistance < 0) {
                            targetDistance = alt;
                        }

                        // making shure pos on target doesnt move
                        Vec4 direction = new Vec4(0, 0, -targetDistance);
                        LocalTransformationProvider localTransformation =
                            new LocalTransformationProvider(wpTog.getPosition(), Angle.ZERO, 0, 0, true);

                        double rollOld = oldOrientation.isRollDefined() ? oldOrientation.getRoll() : 0;
                        double pitchOld = oldOrientation.isPitchDefined() ? oldOrientation.getPitch() : 0;
                        double yawOld = oldOrientation.isYawDefined() ? oldOrientation.getYaw() : 0;
                        Matrix mOld = MathHelper.getRollPitchYawTransformationMAVinicAngles(rollOld, pitchOld, yawOld);
                        Vec4 vecOnTarget = direction.transformBy4(mOld.getInverse());

                        double rollNew = newOrientation.isRollDefined() ? newOrientation.getRoll() : 0;
                        double pitchNew = newOrientation.isPitchDefined() ? newOrientation.getPitch() : 0;
                        double yawNew = newOrientation.isYawDefined() ? newOrientation.getYaw() : 0;
                        Matrix mNew = MathHelper.getRollPitchYawTransformationMAVinicAngles(rollNew, pitchNew, yawNew);
                        Vec4 vecNewPos = vecOnTarget.add3(direction.multiply3(-1).transformBy4(mNew.getInverse()));
                        Position newPos = localTransformation.transformToGlobe(vecNewPos);
                        wpTog.setAltWithinM((float)newPos.elevation);
                        wpTog.setLatLon(newPos.latitude.degrees, newPos.longitude.degrees);
                    }

                    wpTog.setOrientation(newOrientation);
                }
            }
        }

        if (cameraTiltToggleEnable && cameraRollToggleEnable) {
            int forward = 0;
            for (IFlightplanRelatedObject fpObj : cont) {
                if (fpObj instanceof CWaypoint) {
                    CWaypoint wpTog = (CWaypoint)fpObj;
                    Orientation o = wpTog.getOrientation();
                    double pitch = o.getPitch();
                    boolean useRoll = distanceToPole(pitch) < 45;
                    double roll = useRoll ? o.getRoll() : o.getYaw();
                    pitch += forward < 2 ? cameraTiltToggleDegrees : -cameraTiltToggleDegrees;
                    roll += (forward == 0 || forward == 3) ? cameraRollToggleDegrees : -cameraRollToggleDegrees;
                    wpTog.setCamPitch(pitch);
                    if (useRoll) {
                        wpTog.setCamRoll(roll);
                    } else {
                        wpTog.setCamYaw(roll);
                    }

                    forward++;
                    forward %= 4;
                }
            }
        } else if (cameraTiltToggleEnable) {
            boolean forward = false;
            for (IFlightplanRelatedObject fpObj : cont) {
                if (fpObj instanceof CWaypoint) {
                    CWaypoint wpTog = (CWaypoint)fpObj;
                    Orientation o = wpTog.getOrientation();
                    double pitch = o.getPitch();
                    pitch += forward ? cameraTiltToggleDegrees : -cameraTiltToggleDegrees;
                    wpTog.setCamPitch(pitch);
                    forward = !forward;
                }
            }
        } else if (cameraRollToggleEnable) {
            boolean forward = false;
            for (IFlightplanRelatedObject fpObj : cont) {
                if (fpObj instanceof CWaypoint) {
                    CWaypoint wpTog = (CWaypoint)fpObj;
                    Orientation o = wpTog.getOrientation();
                    boolean useRoll = distanceToPole(o.getPitch()) < 45;
                    double roll = useRoll ? o.getRoll() : o.getYaw();
                    roll += forward ? cameraRollToggleDegrees : -cameraRollToggleDegrees;

                    if (useRoll) {
                        wpTog.setCamRoll(roll);
                    } else {
                        wpTog.setCamYaw(roll);
                    }

                    forward = !forward;
                }
            }
        }

        if (pitchOffsetLineBegin != 0) {
            boolean lineBegin = false;
            for (IFlightplanRelatedObject fpObj : cont) {
                if (fpObj instanceof CWaypoint) {
                    CWaypoint wpTog = (CWaypoint)fpObj;
                    if (wpTog.isBeginFlightline()) {
                        lineBegin = true;
                    }

                    if (wpTog.isTriggerImageHereCopterMode() && lineBegin) {
                        lineBegin = false;
                        Orientation o = wpTog.getOrientation();
                        wpTog.setCamPitch(o.getPitch() + pitchOffsetLineBegin);
                    }
                }
            }
        }

        return gsdTolerance;
    }

    private static double distanceToPole(double pitch) {
        if (Math.abs(pitch) > 90) {
            return Math.abs(180 - pitch);
        } else {
            return Math.abs(pitch);
        }
    }

    public static MinMaxPair getFlightlineIntersectionsX(
            Vector<Vector<Vec4>> polygons, double currentY, double sweepWidth) {
        MinMaxPair minMax = new MinMaxPair();
        final double currentYUp = currentY + sweepWidth;
        final double currentYLow = currentY - sweepWidth;
        for (Vector<Vec4> corVec : polygons) {
            final int max = corVec.size();
            for (int k = 0; k != max; k++) {
                Vec4 v1 = corVec.get(k);
                Vec4 v2 = corVec.get((k + 1) % max);
                if (v1.y < v2.y) {
                    Vec4 t = v1;
                    v1 = v2;
                    v2 = t;
                }
                // System.out.printf("k=%d currentYUp=%f currentYLow=%f\n v1.y=%f
                // y2.y=%f",k,currentYUp,currentYLow,v1.y,v2.y);
                if (sweepWidth > 0) {
                    if (v1.y >= currentYUp && v2.y <= currentYUp) {
                        double x = v1.x + (v2.x - v1.x) / (v2.y - v1.y) * (currentYUp - v1.y);
                        minMax.update(x);
                    }

                    if (v1.y >= currentYLow && v2.y <= currentYLow) {
                        double x = v1.x + (v2.x - v1.x) / (v2.y - v1.y) * (currentYLow - v1.y);
                        minMax.update(x);
                    }
                }

                if (v1.y >= currentY && v2.y <= currentY) {
                    double x = v1.x + (v2.x - v1.x) / (v2.y - v1.y) * (currentY - v1.y);
                    minMax.update(x);
                }
                //
            }
        }
        // System.out.println(currentY + " => " + minMax);
        return minMax;
    }

    public static Pair<MinMaxPair, MinMaxPair> getFlightlineIntersectionsXNegativePositive(
            Vector<Vector<Vec4>> lines, double currentY, double sweepWidth) {
        MinMaxPair minMaxPositive = new MinMaxPair();
        MinMaxPair minMaxNegative = new MinMaxPair();
        final double currentYUp = currentY + sweepWidth;
        final double currentYLow = currentY - sweepWidth;
        for (Vector<Vec4> corVec : lines) {
            final int max = corVec.size();
            for (int k = 0; k < max - 1; k++) {
                Vec4 v1 = corVec.get(k);
                Vec4 v2 = corVec.get(k + 1);
                if (v1.y < v2.y) {
                    Vec4 t = v1;
                    v1 = v2;
                    v2 = t;
                }
                // System.out.printf("k=%d currentYUp=%f currentYLow=%f\n v1.y=%f
                // y2.y=%f",k,currentYUp,currentYLow,v1.y,v2.y);
                if (sweepWidth > 0) {
                    if (v1.y >= currentYUp && v2.y <= currentYUp) {
                        double x = v1.x + (v2.x - v1.x) / (v2.y - v1.y) * (currentYUp - v1.y);
                        if (x < 0) {
                            minMaxNegative.update(x);
                        } else {
                            minMaxPositive.update(x);
                        }
                    }

                    if (v1.y >= currentYLow && v2.y <= currentYLow) {
                        double x = v1.x + (v2.x - v1.x) / (v2.y - v1.y) * (currentYLow - v1.y);
                        if (x < 0) {
                            minMaxNegative.update(x);
                        } else {
                            minMaxPositive.update(x);
                        }
                    }
                }

                if (v1.y >= currentY && v2.y <= currentY) {
                    double x = v1.x + (v2.x - v1.x) / (v2.y - v1.y) * (currentY - v1.y);
                    if (x < 0) {
                        minMaxNegative.update(x);
                    } else {
                        minMaxPositive.update(x);
                    }
                }
                //
            }
        }
        // System.out.println(currentY + " => " + minMax);
        return new Pair<>(minMaxNegative, minMaxPositive);
    }

    public static MinMaxPair getFlightlineIntersectionsY(
            Vector<Vector<Vec4>> polygons, double currentX, double sweepWidth) {
        MinMaxPair minMax = new MinMaxPair();
        final double currentXUp = currentX + sweepWidth;
        final double currentXLow = currentX - sweepWidth;
        for (Vector<Vec4> corVec : polygons) {
            final int max = corVec.size();
            for (int k = 0; k != max; k++) {
                Vec4 v1 = corVec.get(k);
                Vec4 v2 = corVec.get((k + 1) % max);
                if (v1.x < v2.x) {
                    Vec4 t = v1;
                    v1 = v2;
                    v2 = t;
                }
                // System.out.printf("k=%d currentYUp=%f currentYLow=%f\n v1.y=%f
                // y2.y=%f",k,currentYUp,currentYLow,v1.y,v2.y);
                if (sweepWidth > 0) {
                    if (v1.x >= currentXUp && v2.x <= currentXUp) {
                        double y = v1.y + (v2.y - v1.y) / (v2.x - v1.x) * (currentXUp - v1.x);
                        minMax.update(y);
                    }

                    if (v1.x >= currentXLow && v2.x <= currentXLow) {
                        double y = v1.y + (v2.y - v1.y) / (v2.x - v1.x) * (currentXLow - v1.x);
                        minMax.update(y);
                    }
                }

                if (v1.x >= currentX && v2.x <= currentX) {
                    double y = v1.y + (v2.y - v1.y) / (v2.x - v1.x) * (currentX - v1.x);
                    minMax.update(y);
                }
                // System.out.println("minMaxX=" + minMax);
            }
        }

        return minMax;
    }

    public static Pair<MinMaxPair, MinMaxPair> getFlightlineIntersectionsYNegativePositive(
            Vector<Vector<Vec4>> lines, double currentX, double sweepWidth) {
        MinMaxPair minMaxNegative = new MinMaxPair();
        MinMaxPair minMaxPositive = new MinMaxPair();
        final double currentXUp = currentX + sweepWidth;
        final double currentXLow = currentX - sweepWidth;
        for (Vector<Vec4> corVec : lines) {
            final int max = corVec.size();
            for (int k = 0; k < max - 1; k++) {
                Vec4 v1 = corVec.get(k);
                Vec4 v2 = corVec.get(k + 1);
                if (v1.x < v2.x) {
                    Vec4 t = v1;
                    v1 = v2;
                    v2 = t;
                }
                // System.out.printf("k=%d currentYUp=%f currentYLow=%f\n v1.y=%f
                // y2.y=%f",k,currentYUp,currentYLow,v1.y,v2.y);
                if (sweepWidth > 0) {
                    if (v1.x >= currentXUp && v2.x <= currentXUp) {
                        double y = v1.y + (v2.y - v1.y) / (v2.x - v1.x) * (currentXUp - v1.x);
                        if (y < 0) {
                            minMaxNegative.update(y);
                        } else {
                            minMaxPositive.update(y);
                        }
                    }

                    if (v1.x >= currentXLow && v2.x <= currentXLow) {
                        double y = v1.y + (v2.y - v1.y) / (v2.x - v1.x) * (currentXLow - v1.x);
                        if (y < 0) {
                            minMaxNegative.update(y);
                        } else {
                            minMaxPositive.update(y);
                        }
                    }
                }

                if (v1.x >= currentX && v2.x <= currentX) {
                    double y = v1.y + (v2.y - v1.y) / (v2.x - v1.x) * (currentX - v1.x);
                    if (y < 0) {
                        minMaxNegative.update(y);
                    } else {
                        minMaxPositive.update(y);
                    }
                }
                // System.out.println("minMaxX=" + minMax);
            }
        }

        return new Pair<>(minMaxNegative, minMaxPositive);
    }

    public static boolean isDroneInsideFaceadeOrBehind(
            Vector<Vec4> polygoneNoGo, Vector<Vec4> pathFront, boolean closedFront, Vec4 pointDrone, Vec4 pointTarget) {
        if (isInsidePolygone(polygoneNoGo, pointDrone)) {
            return true;
        }

        // the camera pointing line HAS to intersect the front.... lets search for that
        pointDrone = new Vec4(pointDrone.x, pointDrone.y);
        pointTarget = new Vec4(pointTarget.x, pointTarget.y);
        MathHelper.LineSegment cameraLine = new MathHelper.LineSegment(pointDrone, pointTarget);
        int max = pathFront.size() - 1;
        Vec4 v1 = closedFront ? pathFront.get(max) : pathFront.get(0);
        v1 = new Vec4(v1.x, v1.y);
        int intersectionCount = 0;
        Vec4 lastIntersection = null;
        Vec4 firstIntersection = null;
        for (int i = closedFront ? 0 : 1; i <= max; i++) {
            Vec4 v2 = pathFront.get(i);
            v2 = new Vec4(v2.x, v2.y);
            MathHelper.LineSegment segment = new MathHelper.LineSegment(v1, v2);
            MathHelper.LineSegment diffSegment = MathHelper.shortestLineBetween(cameraLine, segment, true);
            Vec4 diff = diffSegment.second.subtract3(diffSegment.first);
            double len = diff.getLength3();
            if (lastIntersection != null && diffSegment.second.distanceTo3(lastIntersection) < 0.00001) {
                // this intersection was already encountered.. we are just at the junction of two segments now
                // ignore it!
            } else if (firstIntersection != null
                    && firstIntersection != lastIntersection
                    && diffSegment.second.distanceTo3(firstIntersection) < 0.00001) {
                // this intersection was already encountered.. we are just at the junction of two segments now
                // ignore it!
            } else if (len < 0.00001) {
                intersectionCount++;
            }

            lastIntersection = diffSegment.second;
            if (firstIntersection == null) {
                firstIntersection = lastIntersection;
            }

            v1 = v2;
        }
        // no intersection with front found -> that means we are on the backside
        return intersectionCount != 1;
    }

    public static boolean isInsidePolygone(Vector<Vec4> polygone, Vec4 point) {
        int max = polygone.size() - 1;
        Vec4 v1 = polygone.get(max);
        int xIntersections = 0;

        for (int i = 0; i <= max; i++) {
            Vec4 v2 = polygone.get(i);

            double v1x = v1.x;
            double v2x = v2.x;
            double v1y = v1.y;
            double v2y = v2.y;
            if (v1y < v2y) {
                double tmpX = v1x;
                double tmpY = v1y;
                v1x = v2x;
                v1y = v2y;
                v2x = tmpX;
                v2y = tmpY;
            }

            // possible intersection
            if (v1y >= point.y && v2y < point.y) {
                double x = v1x + (v2x - v1x) / (v2y - v1y) * (point.y - v1y);
                if (x > point.x) {
                    xIntersections++;
                }
            }

            v1 = v2;
        }

        boolean inside = xIntersections % 2 == 1;
        return inside;
    }

    public static Vec4 closestOnPolygone(
            Vector<Vec4> polygoneNoGo, Vector<Vec4> pathFront, Vec4 point, Vec4 camTarget, boolean closed) {
        int max = pathFront.size() - 1;
        Vec4 v1 = closed ? pathFront.get(max) : pathFront.get(0);
        v1 = new Vec4(v1.x, v1.y);
        point = new Vec4(point.x, point.y);

        Vec4 pointDirection = null;
        double camDist = 0;
        if (camTarget != null) {
            pointDirection = camTarget.subtract3(point);
            camDist = pointDirection.getLength3();
            pointDirection = new Vec4(pointDirection.x, pointDirection.y);
        }

        // first search on circles around the camTarget, only if this fails, fall back on another approach below
        // search for all circle intersections
        if (camDist > 0) {
            LinkedList<Vec4> circleIntersections = new LinkedList<>();
            for (int i = closed ? 0 : 1; i <= max; i++) {
                Vec4 v2 = pathFront.get(i);
                v2 = new Vec4(v2.x, v2.y);
                if (v1.distanceToSquared3(v2) == 0) {
                    continue;
                }

                circleIntersections.addAll(Line.segmentIntersectionBall(v1, v2, camTarget, camDist));
                v1 = v2;
            }

            if (!circleIntersections.isEmpty()) {
                double bestCosDirectionDelta = 0;
                Vec4 bestPoint = point;
                Vec4 optimalDirection = point.subtract3(camTarget).normalize3();
                for (Vec4 circIntersection : circleIntersections) {
                    Vec4 nearestDirection = circIntersection.subtract3(camTarget).normalize3();

                    double cosDirectionDelta = nearestDirection.dot3(optimalDirection);
                    if (cosDirectionDelta > 0 && cosDirectionDelta > bestCosDirectionDelta) {
                        bestCosDirectionDelta = cosDirectionDelta;
                        bestPoint = circIntersection;
                    }
                }

                if (bestPoint != point) {
                    return bestPoint;
                }
            }
        }

        // search closest point somewhere on the path in front of the facade/building
        double bestDist = Double.POSITIVE_INFINITY;
        Vec4 bestPoint = point;
        v1 = closed ? pathFront.get(max) : pathFront.get(0);
        v1 = new Vec4(v1.x, v1.y);
        for (int i = closed ? 0 : 1; i <= max; i++) {
            Vec4 v2 = pathFront.get(i);
            v2 = new Vec4(v2.x, v2.y);
            if (v1.distanceToSquared3(v2) == 0) {
                continue;
            }

            Vec4 nearest = Line.nearestPointOnSegment(v1, v2, point);
            v1 = v2;
            if (camTarget != null) {
                Vec4 nearestDirection = camTarget.subtract3(nearest);
                if (nearestDirection.dot3(pointDirection) <= 0) {
                    continue;
                }
            }

            double dist = point.distanceTo2(nearest);
            if (dist < bestDist) {
                bestDist = dist;
                bestPoint = nearest;
            }
        }

        if (bestPoint == point) {
            Debug.getLog()
                .log(
                    Level.WARNING,
                    "could not find best point",
                    new RuntimeException("no matching point on surface found for point:" + point));
            return null;
        }

        if (camTarget != null && bestPoint.distanceTo3(camTarget) > 2 * camDist) {
            return null;
        }

        return bestPoint;
    }

    public static double getDiameter(Vector<Vec4> polygone, double alt) {
        double d = getDiameter(polygone);
        return Math.sqrt(d * d + alt * alt);
    }

    public static double getDiameter(Vector<Vec4> polygone) {
        int max = polygone.size();
        double cur = 0;
        for (int i = 0; i != max - 1; i++) {
            Vec4 v1 = polygone.get(i);
            for (int k = i + 1; k != max; k++) {
                Vec4 v2 = polygone.get(k);
                double dx = v1.x - v2.x;
                double dy = v1.y - v2.y;
                dx *= dx;
                dy *= dy;
                cur = Math.max(cur, Math.sqrt(dx + dy));
            }
        }

        return cur;
    }

    public static double getDiameterMultiPol(Vector<Vector<Vec4>> polygone, double alt) {
        double d = getDiameterMultiPol(polygone);
        return Math.sqrt(d * d + alt * alt);
    }

    public static double getDiameterMultiPol(Vector<Vector<Vec4>> polygone) {
        int maxD = polygone.size();
        double cur = 0;
        for (int d1 = 0; d1 != maxD; d1++) {
            final Vector<Vec4> pol1 = polygone.get(d1);

            for (int d2 = d1; d2 != maxD; d2++) {
                final Vector<Vec4> pol2 = polygone.get(d2);

                for (int i1 = 0; i1 != pol1.size(); i1++) {
                    final Vec4 v1 = pol1.get(i1);

                    for (int i2 = (pol1 == pol2 ? i1 + 1 : 0); i2 != pol2.size(); i2++) {
                        final Vec4 v2 = pol2.get(i2);

                        double dx = v1.x - v2.x;
                        double dy = v1.y - v2.y;
                        dx *= dx;
                        dy *= dy;
                        cur = Math.max(cur, Math.sqrt(dx + dy));
                    }
                }
            }
        }

        return cur;
    }

    /**
     * computes the area covered by a polygon given in cartesion coordinates in the unit given in the input
     *
     * @param polygon
     * @return
     */
    public static double computeArea(List<Vec4> polygon) {
        return Math.abs(computeAreaWithSign(polygon));
    }

    public static double computeAreaWithSign(List<Vec4> polygon) {
        // see http://de.wikipedia.org/wiki/Polygon#Fl.C3.A4che
        Ensure.notNull(polygon, "polygon");
        final int n = polygon.size();
        if (n < 3) {
            return 0;
        }

        double A = 0;
        for (int i = 0; i != n; ++i) {
            int m = (i + 1) % n;
            A += (polygon.get(i).x + polygon.get(m).x) * (polygon.get(m).y - polygon.get(i).y);
        }

        return A / 2;
    }

    public static double computeAreaMulti(Vector<Vector<Vec4>> polygons) {
        double area = 0;
        for (Vector<Vec4> polygon : polygons) {
            area += computeArea(polygon);
        }

        return area;
    }

    public static Vec4 computeCenterMulti(Vector<Vector<Vec4>> polygons) {
        // http://local.wasp.uwa.edu.au/~pbourke/geometry/polyarea/
        // http://www.seas.upenn.edu/~sys502/extra_materials/Polygon%20Area%20and%20Centroid.pdf
        double x = 0;
        double y = 0;
        double areaTotal = computeAreaMulti(polygons);
        if (areaTotal <= 0) {
            for (Vector<Vec4> polygon : polygons) {
                final int n = polygon.size();
                if (n == 0) {
                    continue;
                }

                if (n < 3) {
                    Vec4 v = polygon.get(0);
                    if (n == 2) {
                        v = v.add3(polygon.get(1)).multiply3(0.5);
                    }

                    return v;
                }
            }

            return Vec4.ZERO;
        }

        for (Vector<Vec4> polygon : polygons) {
            final int n = polygon.size();
            if (n < 3) {
                continue;
            }

            double area = computeAreaWithSign(polygon);
            if (area == 0) {
                continue;
            }

            double xl = 0;
            double yl = 0;
            for (int i = 0; i != n; ++i) {
                int m = (i + 1) % n;
                final Vec4 v1 = polygon.get(i);
                final Vec4 v2 = polygon.get(m);
                final double t = v1.x * v2.y - v2.x * v1.y;
                xl += (v1.x + v2.x) * t;
                yl += (v1.y + v2.y) * t;
            }

            double factor = Math.signum(area);
            x += xl * factor;
            y += yl * factor;
        }

        final double factor = 6 * areaTotal;
        x /= factor;
        y /= factor;
        return new Vec4(x, y);
    }

    private static class Intersection implements Comparable<Intersection> {
        public int
            directionIndex; // +/-1 for direction i have to traverse the corners of the polygone to go away from the
        // intersection
        public double position;
        public int index; // index of the last corner before the intersection
        public int idxNextIntersection; // index of the next intersection
        boolean processed = false;

        @Override
        public String toString() {
            return "intersection: pos="
                + position
                + " i="
                + index
                + " direction="
                + directionIndex
                + " nextIntersecIdx="
                + idxNextIntersection
                + "  processed="
                + processed;
        }

        @Override
        public int compareTo(Intersection o) {
            return Double.compare(position, o.position);
        }
    }

    /**
     * if trueXfalseY == true, Splitting a given polygone along a line parallel to the Y-Axis which crosses the X-Axis
     * at splitX All polygones whicht are on the left are added to the argument lowerPolygons, the others to
     * upperPolygons
     *
     * <p>if trueXfalseY == false, X and Y- Axis are switched in the explenation before
     *
     * <p>there containers are NOT cleared before the are filled with the results
     *
     * @param inPolygone
     * @param splitVal
     * @param trueXfalseY
     * @param lowerPolygons
     * @param upperPolygons
     */
    public static void splitPolygone(
            Vector<Vec4> inPolygone,
            double splitVal,
            boolean trueXfalseY,
            Vector<Vector<Vec4>> lowerPolygons,
            Vector<Vector<Vec4>> upperPolygons) {
        // search for first scrossing point
        // System.out.println("Splitting at="+splitVal + " trurXfalseY="+trueXfalseY+ "
        // polygon("+inPolygone.size()+")="+inPolygone);

        final int max = inPolygone.size();
        if (max < 2) {
            throw new IllegalArgumentException("inPolygon is not a polygone sine it has too less edges");
        }

        Vector<Intersection> lowerIntersections = new Vector<Intersection>(max);
        Vector<Intersection> upperIntersections = new Vector<Intersection>(max);

        for (int i = 0; i != max; i++) {
            int fp = i;
            int fpp = (i + 1) % max;
            int dir = 1;

            processLocalIntersect(
                inPolygone, splitVal, trueXfalseY, lowerIntersections, upperIntersections, fp, fpp, dir);

            fpp = i;
            fp = (i + 1) % max;
            dir = -1;

            processLocalIntersect(
                inPolygone, splitVal, trueXfalseY, lowerIntersections, upperIntersections, fp, fpp, dir);
        }

        reorderIntersections(lowerIntersections);
        reorderIntersections(upperIntersections);
        // System.out.println("lowerIntersections("+lowerIntersections.size()+"): " + lowerIntersections);
        // System.out.println("upperIntersections("+upperIntersections.size()+"): " + upperIntersections);

        if (lowerIntersections.size() == 0) {
            if (trueXfalseY ? (inPolygone.get(0).x < splitVal) : (inPolygone.get(0).y < splitVal)) {
                if (lowerPolygons != null) {
                    // System.out.println("totally lower");
                    lowerPolygons.add(inPolygone);
                }
            } else {
                if (upperPolygons != null) {
                    // System.out.println("totally upper");
                    upperPolygons.add(inPolygone);
                }
            }

            return;
        }

        if (lowerPolygons != null) {
            constructSubPolygon(
                inPolygone, splitVal + INTERSECTION_OVERLAP, trueXfalseY, lowerPolygons, lowerIntersections);
        }

        if (upperPolygons != null) {
            constructSubPolygon(
                inPolygone, splitVal - INTERSECTION_OVERLAP, trueXfalseY, upperPolygons, upperIntersections);
        }

        return;
    }

    private static void reorderIntersections(Vector<Intersection> intersections) {
        Collections.sort(intersections);
        int dir = +1;
        final int max = intersections.size();
        for (int i = 0; i != max; i++) {
            int next = i + dir;
            // if (next < 0) next+=max;
            // next %= max;
            intersections.get(i).idxNextIntersection = next;
            dir *= -1;
        }
    }

    private static void constructSubPolygon(
            Vector<Vec4> inPolygone,
            double splitVal,
            boolean trueXfalseY,
            Vector<Vector<Vec4>> outPolygon,
            Vector<Intersection> intersections) {
        final int max = inPolygone.size();
        // System.out.println();
        while (true) {
            Intersection curIntersection = null;
            for (Intersection tmp : intersections) {
                if (!tmp.processed) {
                    curIntersection = tmp;
                    break;
                }
            }

            if (curIntersection == null) {
                // System.out.println("ready SubPol list("+outPolygon.size()+")=" + outPolygon+"\n");
                return;
            }
            // System.out.println("start with this intersection:"+curIntersection);
            curIntersection.processed = true;

            Vector<Vec4> current = new Vector<Vec4>();
            outPolygon.add(current);
            Vec4 split;
            if (trueXfalseY) {
                split = new Vec4(splitVal, curIntersection.position);
            } else {
                split = new Vec4(curIntersection.position, splitVal);
            }

            current.add(split);
            int i = curIntersection.index;
            int incr = curIntersection.directionIndex;
            // System.out.println("starting SubOutPol at i="+i+"direct" + incr + " pos="+split);
            while (true) {
                if (i < 0) {
                    i += max;
                }

                i %= max;
                // System.out.println("adding point i="+i+ " pos="+inPolygone.get(i));
                current.add(inPolygone.get(i));
                // serch if their is another intersection with index i
                boolean found = false;
                // System.out.println("searching through remaining intersections="+intersections);
                for (Intersection tmp : intersections) {
                    if (tmp.index == i && tmp != curIntersection) {
                        curIntersection = tmp;
                        found = true;
                        break;
                    }
                }

                // if their is another intersection, add its position, and update position and direction in polygone
                if (found) {
                    // System.out.println("curIntersection="+curIntersection);
                    if (trueXfalseY) {
                        split = new Vec4(splitVal, curIntersection.position);
                    } else {
                        split = new Vec4(curIntersection.position, splitVal);
                    }

                    current.add(split);
                    curIntersection.processed = true;

                    // if not, continue it
                    curIntersection = intersections.get(curIntersection.idxNextIntersection);
                    if (curIntersection.processed) {
                        break; // one subpolygone is cloesd and ready
                    }
                    // System.out.println("curIntersection2="+curIntersection);
                    if (trueXfalseY) {
                        split = new Vec4(splitVal, curIntersection.position);
                    } else {
                        split = new Vec4(curIntersection.position, splitVal);
                    }

                    current.add(split);
                    curIntersection.processed = true;

                    i = curIntersection.index;
                    incr = curIntersection.directionIndex;
                    // System.out.println("continue SubOutPol at i="+i+"direct" + incr + " pos="+split);
                } else {
                    // if not, continue to tranvers the corners
                    i += incr;
                }
            }
        }
    }

    // usage of this number prevents rounding error gaps while merging polygones afterwards
    public static final double INTERSECTION_OVERLAP = 0.10; // in meter

    private static void processLocalIntersect(
            Vector<Vec4> inPolygone,
            double splitVal,
            boolean trueXfalseY,
            Vector<Intersection> lowerIntersections,
            Vector<Intersection> upperIntersections,
            int fp,
            int fpp,
            int dir) {
        // intersection from lower to upper beteween i and i+1
        if (trueXfalseY
                ? (inPolygone.get(fp).x < splitVal && inPolygone.get(fpp).x >= splitVal)
                : (inPolygone.get(fp).y < splitVal && inPolygone.get(fpp).y >= splitVal)) {
            // found

            Intersection upperInt = new Intersection();
            Intersection lowerInt = new Intersection();
            upperInt.position =
                trueXfalseY
                    ? inPolygone.get(fpp).y
                        + (splitVal - inPolygone.get(fpp).x)
                            / (inPolygone.get(fp).x - inPolygone.get(fpp).x)
                            * (inPolygone.get(fp).y - inPolygone.get(fpp).y)
                    : inPolygone.get(fpp).x
                        + (splitVal - inPolygone.get(fpp).y)
                            / (inPolygone.get(fp).y - inPolygone.get(fpp).y)
                            * (inPolygone.get(fp).x - inPolygone.get(fpp).x);
            lowerInt.position = upperInt.position;

            lowerInt.directionIndex = -1 * dir;
            lowerInt.index = fp;
            upperInt.index = fpp;
            upperInt.directionIndex = dir;
            lowerIntersections.add(lowerInt);
            upperIntersections.add(upperInt);
        }
    }

    public static void splitMultiPolygons(
            Vector<Vector<Vec4>> inPolygons,
            double splitVal,
            boolean trueXfalseY,
            Vector<Vector<Vec4>> lowerPolygons,
            Vector<Vector<Vec4>> upperPolygons) {
        for (Vector<Vec4> inPolygon : inPolygons) {
            splitPolygone(inPolygon, splitVal, trueXfalseY, lowerPolygons, upperPolygons);
        }
    }

}
