/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.wwext;

import com.intel.missioncontrol.StaticInjector;
import com.intel.missioncontrol.map.worldwind.IWWGlobes;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.globes.Globe;
import java.util.Vector;

/**
 * Circle which is NOT supporting filling! (at least on ground)
 *
 * @author marco
 */
public class CircleWithUserData extends PolylineWithUserData {

    double radius;
    Position center;

    public static final int NUMBER_OF_SEGMENTS = 32;

    private static final Globe globe = StaticInjector.getInstance(IWWGlobes.class).getDefaultGlobe();

    public CircleWithUserData(Object userData, Position center, double radius) {
        setUserData(userData);
        this.center = center;
        this.radius = radius;
        setClosed(true);
        setPathType(LINEAR);
        setPositions(makeCircle(center, radius));
    }

    public static Vector<Position> makeCircle(Position center, double radius) {
        double da = (2 * Math.PI) / (NUMBER_OF_SEGMENTS);
        double globeRadius = globe.getRadiusAt(center.getLatitude(), center.getLongitude());

        Vector<Position> locations = new Vector<Position>();

        for (int i = 0; i < NUMBER_OF_SEGMENTS; i++) {
            double angle = i * da;
            locations.add(
                new Position(LatLon.greatCircleEndPosition(center, angle, radius / globeRadius), center.elevation));
        }

        return locations;
    }

}
