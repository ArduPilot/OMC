/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.flightplanning.core;

import com.intel.flightplanning.core.vehicle.UAV;
import com.intel.flightplanning.sensor.Sensor;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A FlightPlan is an aggregation object that holds references to the list of waypoints, optionally flightLines and
 * holds a weakreference (maybe strong reference?) of the goal, uav or sensor that was used for calculating the flight
 * plan.
 *
 * <p>waypoints and flightLines can not be null, however flightLines can be empty if the concept is not meaningful
 */
public class FlightPlan {

    /** Should only be used internally. */
    private final List<FlightLine> flightLines;

    private final List<Waypoint> waypoints;

    /** a flight plan may be derived by a goal but can also be created standalone */
    private Optional<Goal> goal;
    /** a flight plan may be derived for a specific uav but can also be created standalone */
    private Optional<UAV> uav;
    /** a flight plan may be derived for a specific sensor but can also be created standalone */
    private Optional<Sensor> sensor;

    public FlightPlan() {
        this.flightLines = new ArrayList<>();
        this.waypoints = new ArrayList<>();
    }

    public FlightPlan(List<Waypoint> waypoints) {
        if (waypoints == null) {
            throw new InvalidParameterException("Waypoints should not be zero");
        }

        this.waypoints = waypoints;
        this.flightLines = new ArrayList<>();
    }

    public FlightPlan(List<FlightLine> flightLines, List<Waypoint> waypoints) {
        if (waypoints == null) {
            throw new InvalidParameterException("Waypoints should not be zero");
        }

        if (flightLines == null) {
            throw new InvalidParameterException("Flight lines should not be zero");
        }

        this.flightLines = flightLines;
        this.waypoints = waypoints;
    }

    public FlightPlan(
            List<FlightLine> flightLines,
            List<Waypoint> waypoints,
            Optional<Goal> goal,
            Optional<UAV> uav,
            Optional<Sensor> sensor) {
        if (waypoints == null) {
            throw new InvalidParameterException("Waypoints should not be zero");
        }

        if (flightLines == null) {
            throw new InvalidParameterException("Flight lines should not be zero");
        }

        this.flightLines = flightLines;
        this.waypoints = waypoints;
        this.goal = goal;
        this.uav = uav;
        this.sensor = sensor;
    }

    /**
     * Assumes that the flight lines contain the waypoints. Needs at least one flight line that contains a waypoint.
     *
     * @param flightLines
     */
    public static FlightPlan fromFlightLines(List<FlightLine> flightLines) {
        if (flightLines == null || flightLines.isEmpty()) {
            throw new InvalidParameterException("Flight lines cannot be empty or null");
        }

        var waypoints = new ArrayList<Waypoint>();
        flightLines.forEach(flightLine -> waypoints.addAll(flightLine.getWaypoints()));
        return new FlightPlan(waypoints);
    }

    @Override
    public String toString() {
        return "FlightPlan{" + "flightLines={" + flightLines + "}, waypoints={" + waypoints + "}}";
    }

    protected List<FlightLine> getFlightLines() {
        return flightLines;
    }

    public List<Waypoint> getWaypoints() {
        return waypoints;
    }

}
