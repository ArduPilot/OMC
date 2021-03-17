/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.flightplanning.core.rasterizer;

import com.intel.flightplanning.core.FlightLine;
import com.intel.flightplanning.core.FlightPlan;
import com.intel.flightplanning.core.Goal;
import com.intel.flightplanning.core.Waypoint;
import com.intel.flightplanning.core.terrain.IElevationModel;
import com.intel.flightplanning.core.vehicle.KinematicProvider;
import com.intel.flightplanning.core.vehicle.UAV;
import com.intel.flightplanning.geometry2d.Geometry2D;
import com.intel.flightplanning.sensor.Camera;
import com.intel.flightplanning.sensor.PhotoProperties;
import com.jme3.math.Vector3f;
import java.util.ArrayList;
import java.util.List;

public class RasterizerTower {

    public FlightPlan rasterize(
            Geometry2D aoi,
            Camera camera,
            UAV uav,
            IElevationModel elev,
            Goal goal,
            PhotoProperties photoProps,
            boolean circleLeftTrueRightFalse_,
            float alt,
            float corridorWidthInMeter,
            float cropHeightMin,
            KinematicProvider kinematicProvider,
            float maxYawRollChange,
            VerticalScanPatternTypes scanPattern,
            boolean addCeiling,
            float maxPitchChange,
            float cameraPitchOffsetDegrees,
            int corridorMinLines,
            float cropHeightMax) {
        var fp = new FlightPlan();
        var flightLines = new ArrayList<FlightLine>();

        boolean circleLeftTrueRightFalse = circleLeftTrueRightFalse_;

        float altMin = Math.max(uav.getMinGroundDistance(), goal.getMinGroundDistance());

        int lines = 0;
//            (int)
//                Math.max(
//                    Math.ceil(2 * Math.PI / (photoProps.getSizeParallelFlightEff() / goal.getCorridorWidthInMeter())),
//                    goal.getCorridorMinLines());

        lines = Math.max(lines, (int)Math.ceil(360 / maxYawRollChange));
        float step = (float)(2 * Math.PI / lines);
        int lineNo = 0;
        ArrayList<Vector3f> pointsBase = new ArrayList<>();
        ArrayList<Vector3f> directionsBase = new ArrayList<>();

        float radius = alt + corridorWidthInMeter + camera.getOffsetToTail();
        float minCropToMinGroundDist = uav.getMinGroundDistance() - cropHeightMin;

        ArrayList<ArrayList<Waypoint>> waypoints = new ArrayList<>();
        generateWaypointsTower(
            waypoints, minCropToMinGroundDist, altMin, step, radius, lines, circleLeftTrueRightFalse);

        makeFlightLines(waypoints, scanPattern, true, alt);

        float sizeParallelFlightEff = photoProps.getSizeParallelFlightEff();

        if (addCeiling) {
//            int tiltSteps = (int)Math.ceil((90 + cameraPitchOffsetDegrees) / maxPitchChange);
//            float tiltStepsDeg = (90 + cameraPitchOffsetDegrees) / tiltSteps;
//
//            for (int i = 1; i < tiltSteps; i++) {
//                Vector3f last = flightLines.get(flightLines.size() - 1).getLastWaypoint().getPosition();
//                float yawRadStart = (float)-Math.atan2(-last.y, -last.x);
//
//                float tiltDeg = i * tiltStepsDeg;
//                float tiltRad = (float)Math.toRadians(tiltDeg);
//
//                lines =
//                    (int)
//                        Math.max(
//                            Math.ceil(2 * Math.PI / (sizeParallelFlightEff / corridorWidthInMeter)), corridorMinLines);
//                lines = Math.max(lines, (int)Math.ceil(360 / maxYawRollChange));
//                step = (float)(2 * Math.PI / lines);
//
//                radius = (float)(corridorWidthInMeter + alt * Math.cos(tiltRad));
//                float curAlt = (float)(cropHeightMax + alt * Math.sin(tiltRad));
//                for (int k = 0; k < lines; k++) {
//                    float yawRad = yawRadStart + step * k * (circleLeftTrueRightFalse ? -1 : 1);
//                    Vector3f v =
//                        new Vector3f((float)(radius * -Math.cos(yawRad)), (float)(radius * Math.sin(yawRad)), curAlt);
////                    Vector3f o = new Vector3f(0, 90 - tiltDeg, Math.toDegrees(yawRad) + getYaw());
//                    Vector3f o = new Vector3f(0, 90 - tiltDeg, (float) Math.toDegrees(yawRad));
//
//                    // System.out.println("V:"+v + " o:"+o);
//                    pointsBase.add(v);
//                    directionsBase.add(o);
//                }
//
//                FlightLine fl = new FlightLine(pointsBase, directionsBase, alt, lineNo++);
//
//                // Todo: commented out, wtf does this do?
//                //                if (planType.useStartCaptureVertically()) {
//                //                    fl.resetDerrivates();
//                //                }
//
//                flightLines.add(fl);
//                pointsBase.clear();
//                directionsBase.clear();
//            }
//
//            // rasterize top polygone
//            float curAlt = cropHeightMax + alt;
//
//            Vector3f last = flightLines.lastElement().getCorners().lastElement();
//            float yawRadStart = Math.atan2(-last.y, last.x) + Math.PI;
//
//            if (corridorWidthInMeter * 2 < sizeInFlightEff) {
//                // single point on top:
//                Vector3f v = new Vector3f(0, 0, curAlt);
//                Vector3f o = new Vector3f(0, 0, Math.toDegrees(yawRadStart) + getYaw());
//                // System.out.println("V:"+v + " o:"+o);
//                pointsBase.addElement(v);
//                directionsBase.add(o);
//            } else {
//                yawRadStart -= Math.PI / 2.;
//
//                // this is line 0
//                Vector3f v =
//                    new Vector3f(
//                        corridorWidthInMeter * Math.sin(yawRadStart),
//                        corridorWidthInMeter * Math.cos(yawRadStart),
//                        curAlt);
//                Vector3f o = new Vector3f(0, 0, Math.toDegrees(yawRadStart) + getYaw() + 90);
//                // System.out.println("V:"+v + " o:"+o);
//                pointsBase.addElement(v);
//                directionsBase.add(o);
//
//                numberLines = (int)Math.round(2 * (corridorWidthInMeter - sizeInFlightEff) / sizeParallelFlightEff);
//                // System.out.println("numLines:"+numberLines);
//                float stepY = 2 * (corridorWidthInMeter - sizeInFlightEff) / numberLines;
//
//                float currentY = -corridorWidthInMeter + sizeInFlightEff;
//                if (numberLines <= 0) {
//                    currentY = 0;
//                    numberLines = 0;
//                }
//
//                // System.out.println();
//                Matrix m = Matrix.fromRotationZ(Angle.fromRadians(-yawRadStart));
//
//                for (lineNo = 0; lineNo <= numberLines; lineNo++) {
//
//                    // System.out.println("curY: " +currentY + " stepY"+stepY);
//
//                    float currentX = Math.sqrt(corridorWidthInMeter * corridorWidthInMeter - currentY * currentY);
//                    // System.out.println("width:"+currentX);
//                    int numImgs = (int)Math.ceil(2 * currentX / sizeInFlightEff);
//                    if (numImgs < 1) {
//                        numImgs = 1;
//                    }
//
//                    float stepX = 2 * currentX / numImgs;
//                    int sign = lineNo % 2 == 0 ? 1 : -1;
//                    // System.out.println("numImgs:"+numImgs );
//                    for (int imgNo = 0; imgNo <= numImgs; imgNo++) {
//
//                        // System.out.println("curX: " +currentX + " stepX"+stepX);
//                        v = new Vector3f(-currentX * sign, -currentY, curAlt);
//                        v = v.transformBy3(m);
//                        o = new Vector3f(0, 0, Math.toDegrees(yawRadStart) + getYaw() - 90 + 90 * sign);
//                        // System.out.println("V:" + v + " o:" + o);
//                        pointsBase.addElement(v);
//                        directionsBase.add(o);
//                        currentX -= stepX;
//                        // break;
//                    }
//
//                    currentY += stepY;
//                }
//
//                // this is the last line
//                v =
//                    new Vector3f(
//                        -corridorWidthInMeter * Math.sin(yawRadStart),
//                        -corridorWidthInMeter * Math.cos(yawRadStart),
//                        curAlt);
//                o = new Vector3f(0, 0, Math.toDegrees(yawRadStart) + getYaw() + 90);
//                // System.out.println("V:"+v + " o:"+o);
//                pointsBase.addElement(v);
//                directionsBase.add(o);
//            }
//
//            FlightLine fl = new FlightLine(pointsBase, directionsBase, alt, lineNo++, this);
//            flightLines.add(fl);
        }

        return fp;
    }

    //

    //                    // FlightLine.mirrowAll(flightLines);
    //                    // Collections.reverse(flightLines);
    //
    //                    break;
    //                }

    private void generateWaypointsTower(
            ArrayList<ArrayList<Waypoint>> waypoints,
            float minCropToMinGroundDist,
            float altMin,
            float step,
            float radius,
            int lines,
            boolean circleLeftTrueRightFalse) {
        for (int k = 0; k < lines; k++) {
//            float yawRad = Math.PI / 2 + k * step * (circleLeftTrueRightFalse ? -1 : 1);
//            ArrayList<Waypoint> column = new ArrayList<>();
//            Vector3f o = new Vector3f(0, 90, Math.toDegrees(yawRad + Math.PI / 2) + getYaw());
//            Vector3f v = new Vector3f(radius * Math.sin(yawRad), radius * Math.cos(yawRad), 0);
//
//            v = new Vector3f(v.x, v.y, altMin);
//            column.add(new Waypoint(v, o, null));
//
//            // TODO extract method (repeating a part from generateWaypoints method)
//            if (minCropToMinGroundDist > 0) {
//                // number of additional steps needed to cover the bottom part
//                int steps = (int)Math.ceil(minCropToMinGroundDist / sizeInFlightEff);
//                float stepSize = sizeInFlightEff / (steps + 1);
//                for (float j = 0; j < steps; j++) {
//                    float height = minGroundDistance + stepSize * (j + 1);
//                    // angle of the waypoint to target a facade point
//                    float pitch = Math.toDegrees(Math.atan2(alt, height - (cropHeightMin + sizeInFlightEff * j)));
//                    float pitchRad = pitch * Math.PI / 180.0;
//                    // shift that need to be applied to a waypoint towards the object to preserve the resolution
//                    float shift = alt * (1.0 / Math.sin(pitchRad) - 1);
//                    if (j == 0) {
//                        this.distMinComputed = alt - shift;
//                    }
//
//                    float direction = (o.getYaw() - getYaw()) * Math.PI / 180.0;
//                    // TODO: maybe check if x and y have correct cos and sin :)
//                    float vx = v.x + Math.cos(direction) * shift;
//                    float vy = v.y - Math.sin(direction) * shift;
//                    Vector3f v1 = new Vector3f(vx, vy, height);
//
//                    Vector3f o_add = new Vector3f(0, pitch, o.getYaw());
//                    column.add(new Waypoint(v1, o_add, null));
//                }
//            }
//
//            for (float alt = altMin + sizeInFlightEff; alt <= cropHeightMax; alt += sizeInFlightEff) {
//                v = new Vector3f(v.x, v.y, alt);
//                column.add(new Waypoint(v, o, null));
//            }
//
//            waypoints.add(column);
        }
    }

    private void makeFlightLines(
            ArrayList<ArrayList<Waypoint>> columnsOrig,
            VerticalScanPatternTypes verticalScanPattern,
            boolean isFacade,
            float alt) {
//        List<ArrayList<Waypoint>> columns = sortColumns(columnsOrig);
//
//        if (columns == null || columns.size() == 0) {
//            return;
//        }
//
//        int lineNo = 0;
//        Vector<Vector3f> pointsBase = new Vector<>();
//        Vector<Orientation> directionsBase = new Vector<>();
//
//        if (verticalScanPattern.equals(VerticalScanPatternTypes.upDown)) {
//            int i = 0;
//            for (ArrayList<Waypoint> column : columns) {
//                for (Waypoint t : column) {
//                    pointsBase.add(t.positionOnDrone);
//                    directionsBase.add(t.orientation);
//                }
//
//                if (i % 2 == 0) {
//                    Collections.reverse(pointsBase);
//                    Collections.reverse(directionsBase);
//                }
//
//                i++;
//                FlightLine fl = new FlightLine(pointsBase, directionsBase, alt, lineNo++, this);
//                flightLines.add(fl);
//                pointsBase.clear();
//                directionsBase.clear();
//            }
//        } else if (verticalScanPattern.equals(VerticalScanPatternTypes.leftRight)) {
//            for (int i = 0; i < columns.get(0).size(); i++) {
//                for (ArrayList<Waypoint> c : columns) {
//                    pointsBase.add(c.get(i).positionOnDrone);
//                    directionsBase.add(c.get(i).orientation);
//                }
//
//                if (i % 2 == 1 && isFacade) {
//                    Collections.reverse(pointsBase);
//                    Collections.reverse(directionsBase);
//                }
//
//                FlightLine fl = new FlightLine(pointsBase, directionsBase, alt, lineNo++, this);
//                flightLines.add(fl);
//                pointsBase.clear();
//                directionsBase.clear();
//            }
//        }
    }

    private List<ArrayList<Waypoint>> sortColumns(ArrayList<ArrayList<Waypoint>> columns) {
//        ScanDirectionsTypes type = getScanDirection();
//        List<ArrayList<Waypoint>> sorted = new ArrayList<>();
//        Flightplan flightplan = getFlightplan();
//        // erticalScanPattern.equals(VerticalScanPatternTypes.leftRight)
//
//        if (type.equals(ScanDirectionsTypes.fromStarting) || type.equals(ScanDirectionsTypes.towardLaning)) {
//            Position position = null;
//
//            if (type.equals(ScanDirectionsTypes.fromStarting)) {
//                // compute previous positionOnDrone before picArea
//                PreviousOfTypeVisitor vis2 = new PreviousOfTypeVisitor(this, IPositionReferenced.class, Point.class);
//                vis2.setSkipIgnoredPaths(true);
//                vis2.startVisit(flightplan);
//                IPositionReferenced wp = (IPositionReferenced)vis2.prevObj;
//                if (wp == null || isNullIsland(wp.getLatLon()) || wp == getFlightplan().getRefPoint()) {
//                    ReferencePoint origin = flightplan.getTakeoff();
//                    position = new Position(Angle.fromDegrees(origin.getLat()), Angle.fromDegrees(origin.getLon()), 0);
//                } else {
//                    position = wp.getPosition();
//                }
//            } else {
//                // compute next positionOnDrone after picArea
//                NextOfTypeVisitor vis = new NextOfTypeVisitor(this, IPositionReferenced.class, Point.class);
//                vis.setSkipIgnoredPaths(true);
//                vis.startVisit(flightplan);
//                IPositionReferenced wp = (IPositionReferenced)vis.nextObj;
//                if (wp == null || isNullIsland(wp.getPosition()) || wp == getFlightplan().getRefPoint()) {
//                    if (!flightplan.getLandingpoint().isEmpty()) {
//                        wp = flightplan.getLandingpoint();
//                    } else {
//                        wp = flightplan.getTakeoff();
//                    }
//
//                    position = new Position(wp.getLatLon(), 0);
//                } else {
//                    position = wp.getPosition();
//                }
//            }
//
//            Vec4 vec = globe.computePointFromPosition(position);
//
//            // get index of the nearest column to the point of interest
//            int idx =
//                IntStream.range(0, columns.size())
//                    .reduce(
//                        (a, b) ->
//                            computeDistance(columns.get(a).get(0).positionOnDrone, vec)
//                                    < computeDistance(columns.get(b).get(0).positionOnDrone, vec)
//                                ? a
//                                : b)
//                    .getAsInt();
//
//            if (planType.equals(PlanType.BUILDING)
//                    || planType.equals(PlanType.TOWER)
//                    || planType.equals(PlanType.WINDMILL)) {
//
//                // cyclic sift of columns
//                return IntStream.range(idx, columns.size() + idx)
//                    .mapToObj((i) -> columns.get((i % columns.size())))
//                    .collect(Collectors.toList());
//            } else if (planType.equals(PlanType.FACADE)) {
//                // non cyclic order, but first from the column to the right and come back and from the column to the
//                // left
//                /*return IntStream.range(idx, columns.size() + idx)
//                .mapToObj((i) -> columns.get((i < columns.size() ? i : (idx - i % columns.size() - 1))))
//                .collect(Collectors.toList());*/
//                if (idx <= columns.size() / 2 && type.equals(ScanDirectionsTypes.fromStarting)
//                        || idx > columns.size() / 2 && type.equals(ScanDirectionsTypes.towardLaning)) {
//                    // do nothing start from the left
//                    return columns;
//                } else {
//                    // start from the right
//                    Collections.reverse(columns);
//                    return columns;
//                }
//            }
//        } else if (scanDirection.equals(ScanDirectionsTypes.left) && facadeScanningSide == FacadeScanningSide.left
//                || scanDirection.equals(ScanDirectionsTypes.right) && facadeScanningSide == FacadeScanningSide.right) {
//            return columns;
//        } else if (scanDirection.equals(ScanDirectionsTypes.custom)) {
//            return columns;
//        } else {
//            Collections.reverse(columns);
//            return columns;
//        }
//
//        return sorted;
return null;
    }

}
