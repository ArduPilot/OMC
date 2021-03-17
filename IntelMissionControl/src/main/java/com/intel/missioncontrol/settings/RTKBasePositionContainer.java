/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.settings;

import eu.mavinci.desktop.helper.gdal.ISrsManager;
import eu.mavinci.desktop.main.debug.Debug;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.globes.Earth;

public class RTKBasePositionContainer implements Comparable<RTKBasePositionContainer> {

    public static final RTKBasePositionContainer EMPTY = new RTKBasePositionContainer(null);

    private static final double ZERO = 0.0;

    private final RtkBasePosition position;
    private double distance;

    public RTKBasePositionContainer(RtkBasePosition position) {
        this(position, 0.0);
    }

    public RTKBasePositionContainer(RtkBasePosition position, double distance) {
        this.position = position;
        this.distance = distance;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public RtkBasePosition getPosition() {
        return position;
    }

    public String getName() {
        if (position == null) {
            return "";
        }

        return position.nameProperty().get();
    }

    public boolean isEmpty() {
        return (position == null);
    }

    @Override
    public int compareTo(RTKBasePositionContainer o) {
        if (o == null) {
            return 1;
        }

        return Double.compare(distance, o.getDistance());
    }

    public static RTKBasePositionContainer forBasePositionAndAssumed(
            RtkBasePosition position, Position assumedPosition, boolean updatePosition, ISrsManager srsManager) {
        if ((position != null) && (updatePosition)) {
            position.setDirty();
        }

        double distance = getDistance(position, assumedPosition, srsManager);
        return new RTKBasePositionContainer(position, distance);
    }

    private static double getDistance(RtkBasePosition position, Position assumedPosition, ISrsManager srsManager) {
        if (assumedPosition == null) {
            return ZERO;
        }

        // KW fix: if position is null, the distance would be NaN -> copy same behavior
        if (position == null) {
            Debug.getLog().log(Debug.WARNING, "distance cannot be calculated - position=null");
            return ZERO;
        }

        if (position == null) {
            return ZERO;
        }

        double distance =
            LatLon.ellipsoidalDistance(
                position.getPosition(srsManager),
                assumedPosition,
                Earth.WGS84_EQUATORIAL_RADIUS,
                Earth.WGS84_POLAR_RADIUS);

        if (Double.isNaN(distance)) {
            return ZERO;
        }

        return distance;
    }

}
