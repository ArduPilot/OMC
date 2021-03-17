/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project;

import com.intel.missioncontrol.project.serialization.DeserializationContext;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import org.asyncfx.collections.AsyncListChangeListener;

public class Mission extends AbstractMission {

    // private final SurfacePointCoordinateSystem coordinateSystem;
    private LatLon referenceLatLon;

    public Mission() {
        getGoals().addListener(this::handleGoalsAddedOrRemoved);
    }

    public Mission(IMission other) {
        super(other);
        getGoals().addListener(this::handleGoalsAddedOrRemoved);
    }

    public Mission(DeserializationContext context) {
        super(context);
        double lat = context.readDouble("refLat");
        double lon = context.readDouble("refLon");
        referenceLatLon = new LatLon(Angle.fromDegreesLatitude(lat), Angle.fromDegreesLongitude(lon));
    }

    public LatLon getReferenceLatLon() {
        return referenceLatLon;
    }

    private void handleGoalsAddedOrRemoved(AsyncListChangeListener.Change<? extends Goal> change) {
        while (change.next()) {
            for (Goal goal : change.getAddedSubList()) {
                goal.setMission(this);
            }

            for (Goal goal : change.getRemoved()) {
                goal.setMission(null);
            }
        }
    }

}
