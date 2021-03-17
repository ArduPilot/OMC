/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.flightplanning.core;

import com.intel.flightplanning.core.annotations.NeedsRework;
import com.intel.flightplanning.core.terrain.IElevationHelper;
import com.intel.flightplanning.core.terrain.IElevationModel;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

/**
 * A collection of references to way points that form a line. Usually a @see FlightPlan consists of multiple
 * FlightLines.
 *
 * <p>The primary use case of flight lines is the corridor type of a flight plan or polygon flight plans when using a
 * fixed wing UAV.
 *
 * <p>The list of waypoints are references to the way points on the flight line. The starting and ending point, as well
 * as direction, are always copies and do not refer to the first or last waypoint directly.
 *
 * <p>The fields of this class can never be null
 *
 * <p>The fields are still are mutable, since Vector3f and the List of waypoints is mutable.
 *
 * <p>When the user of the class changes a waypoint that the list of waypoints is referencing, this might change the
 * starting point, direction or ending point. It is the user's responsibility to update the startingPoint, direction or
 * endingPoint accordingly.
 */
public class FlightLine {
    private final List<Waypoint> waypoints;
    private final Vector3f startingPoint;
    private final Vector3f direction;
    private final Vector3f endingPoint;
    private int id = 0;
    /**
     * Distance of flight line to target (e.g. altitude above ground). This may be -1f if the information is not
     * provided.
     */
    private float distToTarget = -1f;

    /** Empty constructor that just initializes private fields. Can be used when adding waypoints in a loop. */
    FlightLine() {
        waypoints = new ArrayList<>();
        startingPoint = new Vector3f();
        direction = new Vector3f();
        endingPoint = new Vector3f();
    }

    /**
     * From existing list of waypoints. Direction is last way point - first way point. Waypoints may not be empty or
     * null.
     *
     * @param waypoints
     */
    public FlightLine(List<Waypoint> waypoints) {
        if (waypoints == null || waypoints.size() == 0) {
            throw new InvalidParameterException("List of way points should not be empty");
        }

        this.id = 0;
        this.waypoints = waypoints;
        this.startingPoint = new Vector3f(waypoints.get(0).getCameraPosition());
        this.endingPoint = new Vector3f(waypoints.get(waypoints.size() - 1).getCameraPosition());
        this.direction = this.startingPoint.subtract(this.endingPoint);
    }

    public FlightLine(List<Waypoint> waypoints, Vector3f direction) {
        if (waypoints == null || waypoints.size() == 0) {
            throw new InvalidParameterException("List of way points should not be empty");
        }

        this.id = 0;
        this.waypoints = waypoints;
        this.startingPoint = new Vector3f(waypoints.get(0).getCameraPosition());
        this.endingPoint = new Vector3f(waypoints.get(waypoints.size() - 1).getCameraPosition());
        this.direction = new Vector3f(direction);
    }

    public FlightLine(List<Waypoint> waypoints, Vector3f startingPoint, Vector3f endingPoint, Vector3f direction) {
        if (waypoints == null || startingPoint == null || endingPoint == null || direction == null) {
            throw new InvalidParameterException("Constructur args must be non-null");
        }

        this.waypoints = waypoints;
        this.startingPoint = new Vector3f(startingPoint);
        this.endingPoint = new Vector3f(endingPoint);
        this.direction = new Vector3f(direction);
    }

    /**
     * Define a flight line only by starting point, direction end end point. This will lead to an empty waypoints list.
     *
     * @param startingPoint
     * @param direction
     * @param end
     */
    public FlightLine(Vector3f startingPoint, Vector3f direction, Vector3f end) {
        this.waypoints = new ArrayList<>();
        this.startingPoint = new Vector3f(startingPoint);
        this.direction = new Vector3f(direction);
        this.endingPoint = new Vector3f(end);
    }

    /**
     * Given the corners of a flight line, stretch out the first and last corner by @see enlargeStartM and @see
     * enlargeEndM
     *
     * @param corners
     * @param enlargeStartM
     * @param enlargeEndM
     * @return
     */
    public static void enlarge(List<Vector3f> corners, float enlargeStartM, float enlargeEndM) {
        if (corners == null || corners.size() <= 2) {
            throw new InvalidParameterException("Need to have three or more corners");
        }

        if (enlargeEndM <= 0) {
            throw new InvalidParameterException("enlargeEndM must be larger than zero");
        }

        if (enlargeStartM <= 0) {
            throw new InvalidParameterException("enlargeStartM must be larger than zero");
        }

        int idxEnd = corners.size() - 1;
        Vector3f s0 = corners.get(0);
        Vector3f s1 = corners.get(1);

        Vector3f dStart = s0.subtract(s1).normalize();
        corners.set(0, s0.add(dStart.mult(enlargeStartM)));

        Vector3f e0 = corners.get(idxEnd);
        Vector3f e1 = corners.get(idxEnd - 1);

        Vector3f dEnd = e0.subtract(e1).normalize();
        corners.set(idxEnd, e0.add(dEnd.mult(enlargeEndM)));
    }

    /**
     * Given a list of polygons, a starting line defined by y=const. and a sweep width, return a list of start and end
     * points of intersections with said polygons.
     *
     * <p>Todo: currently it only returns one minmax pair. in the case of multi-polygons this should be a list of
     * multiple minmax pairs
     *
     * @param polygons A list of polygons, i.e. a list of list of corners that define polygon
     * @param y
     * @param sweepWidth
     * @return
     */
    @NeedsRework
    public static List<MinMaxPair> getFlightlineIntersectionWithPolygonsHorizontal(
            List<List<Vector2f>> polygons, double y, double sweepWidth) {
        if (polygons == null) {
            throw new InvalidParameterException("Polygons shall not be null.");
        }

        MinMaxPair minMax = new MinMaxPair();
        final double currentYUp = y + sweepWidth;
        final double currentYLow = y - sweepWidth;

        for (List<Vector2f> corners : polygons) {
            final int max = corners.size();
            for (int k = 0; k != max; k++) {
                Vector2f v1 = corners.get(k);
                Vector2f v2 = corners.get((k + 1) % max);
                if (v1.y < v2.y) {
                    Vector2f t = v1;
                    v1 = v2;
                    v2 = t;
                }

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

                if (v1.y >= y && v2.y <= y) {
                    double x = v1.x + (v2.x - v1.x) / (v2.y - v1.y) * (y - v1.y);
                    minMax.update(x);
                }
                //
            }
        }

        var minMaxPairs = new ArrayList<MinMaxPair>();
        minMaxPairs.add(minMax);
        return minMaxPairs;
    }

    /**
     * This is a simple adapter to an elevation model / terrain.
     *
     * <p>Todo: There should be an alternative, maybe functional interface or so for adapting flight lines or waypoints
     * to terrain?
     *
     * <p>Todo: in principle you should prefer a LineRasterizer, in @see RasterizerLine
     *
     * @param flightLine
     * @param elev
     * @param elevHelper
     * @param distToTarget
     * @return
     */
    @NeedsRework
    public static FlightLine adaptToTerrain(
            FlightLine flightLine, IElevationModel elev, IElevationHelper elevHelper, float distToTarget) {
        if (flightLine == null) {
            throw new InvalidParameterException("Can not adapt a null flight-line");
        }

        var newFlightLine = new FlightLine();
        var waypoints = flightLine.getWaypoints();

        for (var wp : waypoints) {
            var pos = wp.getCameraPosition();
            var camDirection = wp.getCameraDirection();

            // just "shift" the last point, instead of adding two very close to each other
            var intersection = elevHelper.intersectionWithTerrain(elev, pos, camDirection);

            var cameraPosition=(intersection.subtract(camDirection.mult(distToTarget)));
            var newWp = new Waypoint(cameraPosition,intersection,0f);
            newFlightLine.getWaypoints().add(newWp);
            // if distance between last to current waypoint is smaller, check if the
            // camera can
            //		trigger fast enough.

            // if distance between last to current waypoint is larger, check if we
            // need to insert additional
            //		waypoint in the middle

            // auto dist_to_last_wp = distance(new_wp.cam_pos, new_wp.cam_pos);
            //
            //            if (dist_to_last_wp < vehicle_descr.min_waypoint_separation_m) {
            //                // now what?
            //            }
            //
            //            if (dist_to_last_wp > vehicle_descr.max_waypoint_separation_m) {
            //                // now what?
            //            }

        }

        return newFlightLine;
    }

    public List<Waypoint> getWaypoints() {
        return waypoints;
    }

    public Vector3f getStartingPoint() {
        return startingPoint;
    }

    public Vector3f getDirection() {
        return direction;
    }

    public Vector3f getEndingPoint() {
        return endingPoint;
    }

    public Waypoint getLastWaypoint() {
        return this.getWaypoints().get(this.getWaypoints().size() - 1);
    }
}
