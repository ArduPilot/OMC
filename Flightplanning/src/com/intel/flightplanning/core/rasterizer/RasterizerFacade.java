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
import com.intel.flightplanning.math.CorridorHelper;
import com.intel.flightplanning.sensor.Camera;
import com.intel.flightplanning.sensor.PhotoProperties;
import com.jme3.math.Vector3f;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

public class RasterizerFacade {

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
            boolean circleLeftTrueRightFalse,
            StartCaptureVerticallyTypes startCaptureVertically,
            FacadeScanningSide facadeScanningSide,
            float minGroundDistance,
            float minObjectDistance,
            CorridorHelper corridorHelper) {
        var fp = new FlightPlan();
        var flightLines = new ArrayList<FlightLine>();

        if (startCaptureVertically.equals(StartCaptureVerticallyTypes.up)) {
            circleLeftTrueRightFalse = !circleLeftTrueRightFalse;
        }

        float sizeInFlightEff = photoProps.getSizeInFlightEff();
        float ADD_TOP_LINE_THRESHOLD = 0.3f * sizeInFlightEff;

        int lineNo = 0;
        // corridorHelper = new CorridorHelper(this.cornerList, planType == PlanType.BUILDING);

        List<Vector3f> pointsAdd = null;
        List<Vector3f> directionsAdd = null;
        Vector3f lastLowerLevel = null;

        List<Vector3f> pointsBase = new ArrayList<>();
        List<Vector3f> directionsBase = new ArrayList<>();

        List<Vector3f> pathSafetyDist;
        List<Vector3f> polyNoGo;
        double sign = 1;
        // System.out.println("facadeScanningSide:"+facadeScanningSide);

        List<Vector3f> corVecEnlarged = new ArrayList<>();
        List<Vector3f> cp =
            aoi.getCornerPoints().stream().map(v -> new Vector3f(v.x, v.y, 0f)).collect(Collectors.toList());

        corVecEnlarged.addAll(cp);

        // TODO: move this to RasterizerBuilding
        //        if (planType == PlanType.BUILDING) {
        //            if (AutoFPhelper.computeAreaWithSign(cornerList) > 0) {
        //                sign = -1;
        //            }
        //
        //            polyNoGo = corridorHelper.getShifted(sign * (minObjectDistance + 1.2));
        //            pathSafetyDist = polyNoGo;
        //        } else

        float overshootParallelFlight = photoProps.getOverShootParallelFlight();
        float centrencyParallelFlight = photoProps.getCentrencyParallelFlight();
        { // FACADE
            if (facadeScanningSide == FacadeScanningSide.left) {
                sign = -1;
            }

            FlightLine.enlarge(
                corVecEnlarged,
                overshootParallelFlight - centrencyParallelFlight,
                overshootParallelFlight + centrencyParallelFlight);

            pathSafetyDist = new ArrayList<>();
            pathSafetyDist.addAll(corridorHelper.getShifted((float)(sign * (minObjectDistance + 1.2))));
            FlightLine.enlarge(
                pathSafetyDist,
                overshootParallelFlight - centrencyParallelFlight + alt,
                overshootParallelFlight + centrencyParallelFlight + alt);

            polyNoGo = corridorHelper.getShifted((float)(-sign * 2 * CorridorHelper.MINIMAL_POSSIBLE_SHIFT));
            FlightLine.enlarge(polyNoGo, alt, alt);
            Collections.reverse(polyNoGo);
            polyNoGo.addAll(pathSafetyDist);
        }

        double altMin = Math.max(Math.max(cropHeightMin, minGroundDistance), uav.getMinGroundDistance());

        double minCropToMinGroundDist = minGroundDistance - cropHeightMin;

//        ArrayList<ArrayList<Waypoint>> waypoints =
//            generateWaypoints(
//                corVecEnlarged,
//                polyNoGo,
//                pathSafetyDist,
//                sign,
//                altMin,
//                minCropToMinGroundDist,
//                alt,
//                circleLeftTrueRightFalse);
//        RasterizerTower.makeFlightLines(waypoints, getVerticalScanPattern(), planType);

        if (addCeiling) {
//            pointsAdd = new ArrayList<>();
//            directionsAdd = new ArrayList<>();
//
////            lastLowerLevel = flightLines.lastElement().getCorners().lastElement();
//
//            pointsBase.clear();
//            directionsBase.clear();
//
//            // first the circles on the roof edge
//            boolean reverse = !(circleLeftTrueRightFalse == (sign > 0));
//            double signOrg = sign;
//            if (reverse) {
//                sign *= -1;
//            }
//
//            int tiltSteps = (int)Math.ceil((90 + cameraPitchOffsetDegrees) / maxPitchChange);
//            double tiltStepsDeg = (90 + cameraPitchOffsetDegrees) / tiltSteps;
//            if (planType == PlanType.FACADE) {
//                tiltSteps++;
//            }
//
//            for (int s = 1; s < tiltSteps; s++) {
//                double tiltDeg = s * tiltStepsDeg;
//                double tiltRad = Math.toRadians(tiltDeg);
//
//                double curAlt = cropHeightMax + alt * Math.sin(tiltRad);
//                List<Vector3f> path = corridorHelper.getShifted(signOrg * alt * Math.cos(tiltRad));
//                if (planType == PlanType.FACADE) {
//                    FlightLine.enlargePolygone(
//                        path,
//                        overshootParallelFlight - centrencyParallelFlight,
//                        overshootParallelFlight + centrencyParallelFlight);
//                }
//
//                if (reverse) {
//                    Collections.reverse(path);
//                }
//
//                Ensure.notNull(path, "path");
//                // roof line looking 45°
//                Vector3f last = planType == PlanType.FACADE ? path.get(1) : path.get(path.size() - 2);
//                Vector3f next = path.firstElement();
//                Vector3f diffLast = next.subtract(last).normalize();
//                if (planType == PlanType.FACADE) {
//                    diffLast = diffLast.mult(-1);
//                }
//
//                for (int i = 1; i < path.size(); i++) {
//                    last = next;
//                    next = path.get(i);
//                    Vector3f diff = next.subtract(last);
//                    double len = diff.getLength3();
//                    if (len == 0) continue;
//                    diff = diff.mult(1 / len);
//                    double yaw = Math.toDegrees(Math.atan2(diff.x, diff.y)) + getYaw() - 90 + 90 * sign;
//
//                    Vector3f v = last;
//                    v = new Vector3f(v.x, v.y, curAlt);
//                    Vector3f diffAvg = diffLast.add3(diff);
//                    double yawAvg = Math.toDegrees(Math.atan2(diffAvg.x, diffAvg.y)) + getYaw() - 90 + 90 * sign;
//                    Vector3f o = new Vector3f(0, 90 - tiltDeg, yawAvg);
//                    pointsBase.addElement(v);
//                    directionsBase.add(o);
//
//                    diffLast = diff;
//                    int noImg = (int)Math.ceil(len / sizeParallelFlightEff);
//                    double step = len / noImg;
//                    if (planType == PlanType.FACADE && i == path.size() - 1) {
//                        noImg++;
//                    }
//
//                    double dist = step;
//                    for (int n = 1; n < noImg; n++) {
//                        v = last.add3(diff.mult(dist));
//                        v = new Vector3f(v.x, v.y, curAlt);
//                        o = new Vector3f(0, 90 - tiltDeg, yaw);
//                        pointsBase.addElement(v);
//                        directionsBase.add(o);
//                        dist += step;
//                    }
//                }
//
//                // find element which is as closest to lastLowerLevel
//                if (planType == PlanType.FACADE) {
//                    if (lastLowerLevel.distanceTo3(pointsBase.firstElement())
//                            > lastLowerLevel.distanceTo3(pointsBase.lastElement())) {
//                        Collections.reverse(pointsBase);
//                        Collections.reverse(directionsBase);
//                    }
//
//                    FlightLine fl = new FlightLine(pointsBase, directionsBase, alt, lineNo++, this);
//                    flightLines.add(fl);
//                    lastLowerLevel = fl.getCorners().lastElement();
//                } else {
//                    double minDist = Double.POSITIVE_INFINITY;
//                    int bestI = -1;
//                    for (int i = 0; i != pointsBase.size(); i++) {
//                        double dist = lastLowerLevel.distanceTo3(pointsBase.get(i));
//                        if (dist < minDist) {
//                            minDist = dist;
//                            bestI = i;
//                        }
//                    }
//
//                    for (int i = 0; i != pointsBase.size(); i++) {
//                        int k = (-i + bestI) % pointsBase.size();
//                        if (k < 0) {
//                            k += pointsBase.size();
//                        }
//
//                        pointsAdd.add(pointsBase.get(k));
//                        directionsAdd.add(directionsBase.get(k));
//                    }
//
//                    FlightLine fl = new FlightLine(pointsAdd, directionsAdd, alt, lineNo++, this);
//                    flightLines.add(fl);
//                }
//
//                pointsBase.clear();
//                directionsBase.clear();
//                pointsAdd.clear();
//                directionsAdd.clear();
//            }
//
//            double curAlt = cropHeightMax + alt;
//
//            // rasterize roof
//            if (planType != PlanType.FACADE) {
//                numberLines = (int)Math.round(this.lengthY / sizeParallelFlightEff);
//                if (numberLines < 1) {
//                    numberLines = 1;
//                }
//                // System.out.println("numLines:" +numberLines+ " lengthY:"+lengthY);
//                double stepY = this.lengthY / numberLines;
//
//                double currentY = 0;
//
//                for (lineNo = 0; lineNo <= numberLines; lineNo++) {
//                    // System.out.println("============ line No:"+lineNo + " curY:"+currentY);
//                    MinMaxPair minMaxX = AutoFPhelper.getFlightlineIntersectionsX(polygonsLocal, currentY, stepY / 2);
//                    // System.out.println("minMaxX:"+minMaxX);
//                    if (minMaxX.isValid()) {
//                        int numImgs = (int)Math.ceil(minMaxX.size() / sizeInFlightEff);
//                        if (numImgs < 1) {
//                            numImgs = 1;
//                        }
//
//                        double stepX = minMaxX.size() / numImgs;
//                        sign = lineNo % 2 == 0 ? 1 : -1;
//                        // System.out.println("numImgs:"+numImgs );
//                        double currentX = sign < 0 ? minMaxX.min : minMaxX.max;
//                        for (int imgNo = 0; imgNo <= numImgs; imgNo++) {
//
//                            // System.out.println("curX: " +currentX + " stepX"+stepX);
//                            Vector3f v = new Vector3f(currentX, currentY, curAlt);
//                            // v= v.transformBy3(m);
//                            Vector3f o = new Vector3f(0, 0, getYaw() - 90 - 90 * sign);
//                            // System.out.println("V:"+v + " o:"+o);
//                            pointsBase.addElement(v);
//                            directionsBase.add(o);
//                            currentX -= stepX * sign;
//                            // break;
//                        }
//                    }
//
//                    currentY += stepY;
//                }
//
//                FlightLine fl = new FlightLine(pointsBase, directionsBase, alt, lineNo++, this);
//                flightLines.add(fl);
//                pointsBase.clear();
//                directionsBase.clear();
//            }
        }
        // FlightLine.mirrowAll(flightLines);
        // Collections.reverse(flightLines);

        return fp;
    }

    public enum StartCaptureVerticallyTypes {
        up,
        down
    };

    public enum FacadeScanningSide {
        left,
        right
    }

    private ArrayList<ArrayList<Waypoint>> generateWaypoints(
            Vector<Vector3f> path,
            Vector<Vector3f> polyNoGo,
            Vector<Vector3f> pathSafetyDist,
            double sign,
            double altMin,
            double minCropToMinGroundDist,
            double distanceToObj,
            boolean circleLeftTrueRightFalse) {
//        if (planType.isClosedPolygone()) {
//            if (circleLeftTrueRightFalse == (sign > 0)) {
//                Collections.reverse(path);
//                Collections.reverse(polyNoGo);
//                Collections.reverse(pathSafetyDist);
//                sign *= -1;
//            }
//        }
//
//        int maxPath = planType.isClosedPolygone() ? path.size() - 1 : path.size();
//        Vector3f last = path.get(path.size() - 2);
//        Vector3f next = path.firstElement();
//        Vector3f diffLast = planType == PlanType.FACADE ? null : next.subtract(last).normalize();
//
//        // compute baseline of path, maybe with too high angle changes, but with OK overlap
//        ArrayList<Waypoint> waypoints = new ArrayList();
//        for (int i = 1; i < maxPath; i++) {
//            // for each segment of the path
//            last = next;
//            next = path.get(i);
//            Vector3f diff = next.subtract(last);
//            double len = diff.getLength3();
//            if (len == 0) continue;
//
//            diff = diff.mult(1 / len);
//            double yawStraight = Math.toDegrees(Math.atan2(diff.x, diff.y)) - 90 + 90 * sign;
//
//            Vector3f vOnObject = last;
//            Vector3f diffAvg = diffLast == null ? diff : diffLast.add3(diff);
//            double yawAvg = Math.toDegrees(Math.atan2(diffAvg.x, diffAvg.y)) - 90 + 90 * sign;
//            double yawLastDiff = diffLast == null ? 0 : diffLast.angleBetween3(diff).degrees;
//
//            Vector3f nextnext = path.get(Math.min(i + 1, path.size() - 1));
//            Vector3f diffnext = nextnext.subtract(next);
//            double lennext = diffnext.getLength3();
//            if (lennext > 0) {
//                diffnext = diffnext.mult(1 / lennext);
//            }
//
//            double yawNextDiff = diff.angleBetween3(diffnext).degrees;
//
//            vOnObject = new Vector3f(vOnObject.x, vOnObject.y, 0);
//
//            diffLast = diff;
//            int noSubRows = (int)Math.ceil(len / sizeParallelFlightEff);
//            double step = len / noSubRows;
//
//            int max =
//                    noSubRows
//                            + ((!planType.isClosedPolygone() && i == path.size() - 1) || yawNextDiff > maxYawRollChange
//                            ? 1
//                            : 0);
//
//            double dist = 0;
//
//            for (int n = 0; n < max; n++) {
//                // adding subsegments, since typically a path segment is wide and will carry more then one image
//                vOnObject = last.add3(diff.mult(dist));
//                double yaw = n == 0 && yawLastDiff <= maxYawRollChange ? yawAvg : yawStraight;
//                Orientation o = new Orientation(0, 90, yaw);
//
//                double yawRad = Math.toRadians(yaw);
//                // todo: check coordinate system used :)
//                Vector3f camDist = new Vector3f(Math.cos(yawRad), -Math.sin(yawRad));
//                Vector3f vOnDrone = vOnObject.add3(camDist.mult(-distanceToObj));
//
//                if (AutoFPhelper.isDroneInsideFaceadeOrBehind(
//                        polyNoGo, pathSafetyDist, planType.isClosedPolygone(), vOnDrone, vOnObject)) {
//                    // search closest legal point, and dont care if distanceToObject might get violated
//                    vOnDrone =
//                            AutoFPhelper.closestOnPolygone(
//                                    polyNoGo, pathSafetyDist, vOnDrone, vOnObject, planType.isClosedPolygone());
//                    if (vOnDrone == null) {
//                        dist += step;
//                        continue;
//                    }
//
//                    Vector3f diff2 = vOnDrone.subtract(vOnObject);
//                    yaw = Math.toDegrees(Math.atan2(diff2.x, diff2.y)) + 90;
//                    o.setYaw(yaw);
//                }
//
//                vOnDrone = new Vector3f(vOnDrone.x, vOnDrone.y, altMin);
//                vOnObject = new Vector3f(vOnObject.x, vOnObject.y, altMin);
//                waypoints.add(new Waypoint(vOnDrone, o, vOnObject));
//
//                dist += step;
//            }
//        }
//
//        // add additional points for ensuring maximal angle change at corners
//        ArrayList<Waypoint> waypointsRefined = new ArrayList();
//        Waypoint lastWP =
//                planType.isClosedPolygone() && !waypoints.isEmpty() ? waypoints.get(waypoints.size() - 1) : null;
//        for (Waypoint tmp : waypoints) {
//            if (lastWP != null) {
//                double yawDiff = tmp.orientation.getYaw() - lastWP.orientation.getYaw();
//                while (yawDiff > 180) {
//                    yawDiff -= 360;
//                }
//
//                while (yawDiff < -180) {
//                    yawDiff += 360;
//                }
//
//                double yawDiffAbs = Math.abs(yawDiff);
//                if (yawDiffAbs > maxYawRollChange) {
//                    // refine line
//                    int steps = (int)Math.ceil(yawDiffAbs / maxYawRollChange);
//                    double yawStep = yawDiff / steps;
//                    Vector3f posStep = tmp.positionOnObject.subtract(lastWP.positionOnObject).divide3(steps);
//
//                    for (int i = 1; i < steps; i++) {
//                        // add intermediate points, make sure angles and object position is as desired, so drone flies
//                        // curves
//                        Orientation o = new Orientation(0, 90, lastWP.orientation.getYaw() + i * yawStep);
//                        Vector3f positionOnObject = lastWP.positionOnObject.add3(posStep.mult(i));
//                        double yawRad = Math.toRadians(o.getYaw());
//                        // todo: check coordinate system used :)
//                        Vector3f camDist = new Vector3f(Math.cos(yawRad), -Math.sin(yawRad));
//                        Vector3f vOnDrone = positionOnObject.add3(camDist.mult(-distanceToObj));
//
//                        if (AutoFPhelper.isDroneInsideFaceadeOrBehind(
//                                polyNoGo, pathSafetyDist, planType.isClosedPolygone(), vOnDrone, positionOnObject)) {
//                            // search closest legal point, and dont care if distanceToObject might get violated
//                            vOnDrone =
//                                    AutoFPhelper.closestOnPolygone(
//                                            polyNoGo, pathSafetyDist, vOnDrone, positionOnObject, planType.isClosedPolygone());
//                            if (vOnDrone == null) {
//                                continue;
//                            }
//
//                            Vector3f diff2 = vOnDrone.subtract(positionOnObject);
//                            double yaw = Math.toDegrees(Math.atan2(diff2.x, diff2.y)) + 90;
//                            o.setYaw(yaw);
//                        }
//
//                        vOnDrone = new Vector3f(vOnDrone.x, vOnDrone.y, altMin);
//                        positionOnObject = new Vector3f(positionOnObject.x, positionOnObject.y, altMin);
//                        Waypoint insert = new Waypoint(vOnDrone, o, positionOnObject);
//                        waypointsRefined.add(insert);
//                    }
//                }
//            }
//
//            waypointsRefined.add(tmp);
//            lastWP = tmp;
//        }
//
//        // multiply those points into z-direction
//        ArrayList<ArrayList<Waypoint>> waypointsAll = new ArrayList();
//        for (Waypoint tmp : waypointsRefined) {
//            ArrayList<Waypoint> column = new ArrayList<>();
//            /*
//            Add bottom waypoint
//             */
//            column.add(tmp);
//
//            Vector3f v = tmp.positionOnDrone;
//            double yaw =
//                    tmp.orientation.getYaw()
//                            + getYaw(); // so far all rotations have been in local coordinate frame which is rotated relatively
//            // to the world
//            tmp.orientation.setYaw(yaw);
//
//            /**
//             * Add if needed (in the situation when minCropHeight is lower than minGroundDistance) waypoints between the
//             * bottom one and the one on top of it to cover the bottom part of the building
//             */
//            if (minCropToMinGroundDist > 0) {
//                // number of additional steps needed to cover the bottom part
//                int steps = (int)Math.ceil(minCropToMinGroundDist / sizeInFlightEff);
//                double stepSize = sizeInFlightEff / (steps + 1);
//                for (double j = 0; j < steps; j++) {
//                    double height = minGroundDistance + stepSize * (j + 1);
//                    // angle of the waypoint to target a facade point
//                    double pitchRad = Math.atan2(alt, height - (cropHeightMin + sizeInFlightEff * j));
//                    double pitch = Math.toDegrees(pitchRad);
//                    // shift that need to be applied to a waypoint towards the object to preserve the resolution
//                    double shift = alt * (1 - Math.sin(pitchRad));
//                    if (j == 0) {
//                        this.distMinComputed = alt - shift;
//                    }
//
//                    double direction = (yaw - getYaw()) * Math.PI / 180.0;
//                    // todo: check coordinate system used
//                    double vx = v.x + Math.cos(direction) * shift;
//                    double vy = v.y - Math.sin(direction) * shift;
//                    Vector3f v1 = new Vector3f(vx, vy, height);
//
//                    Orientation oAvg_add = new Orientation(0, pitch, yaw);
//                    column.add(new Waypoint(v1, oAvg_add, null));
//                }
//            }
//
//            double altMax = cropHeightMax;
//            if (!isAddCeiling()) {
//                altMax += overshootTotalLinesEnd;
//            }
//
//            double dAlt = altMax - altMin - sizeInFlightEff;
//            int stepsToClimb = (int)Math.max(0, Math.ceil(dAlt / sizeInFlightEff));
//            double stepAlt = Math.max(0, dAlt / stepsToClimb);
//
//            for (int k = 0; k <= stepsToClimb; k++) {
//                double alt = altMin + sizeInFlightEff + k * stepAlt;
//                v = new Vector3f(v.x, v.y, alt);
//                column.add(new Waypoint(v, tmp.orientation, null));
//            }
//
//            waypointsAll.add(column);
//        }
//
//        return waypointsAll;
        return null;
    }
}
