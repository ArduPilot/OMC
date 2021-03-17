/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight;

import com.google.inject.Inject;
import org.asyncfx.beans.property.AsyncIntegerProperty;
import org.asyncfx.beans.property.AsyncObjectProperty;
import org.asyncfx.beans.property.SimpleAsyncIntegerProperty;
import org.asyncfx.beans.property.SimpleAsyncObjectProperty;
import com.intel.missioncontrol.drone.FlightSegment;
import com.intel.missioncontrol.drone.IDrone;
import com.intel.missioncontrol.drone.validation.IFlightValidationService;
import com.intel.missioncontrol.mission.FlightPlan;
import de.saxsys.mvvmfx.Scope;

/** Common ViewModel properties for ui.sidepane.flight */
public class FlightScope implements Scope {
    private final AsyncObjectProperty<IDrone> currentDrone = new SimpleAsyncObjectProperty<>(this);
    private final AsyncObjectProperty<FlightSegment> flightSegment = new SimpleAsyncObjectProperty<>(this);
    private final AsyncObjectProperty<FlightPlan> selectedFlightPlan = new SimpleAsyncObjectProperty<>(this);
    private final AsyncIntegerProperty nextWayPointIndex = new SimpleAsyncIntegerProperty(this);

    @Inject
    public FlightScope(IFlightValidationService flightValidationService) {
        flightValidationService.droneProperty().bind(currentDrone);
        flightValidationService.flightPlanProperty().bind(selectedFlightPlan);
    }

    /**
     * The currently selected / displayed / controlled IDrone instance which is assumed to be in a connected state, or
     * null otherwise.
     */
    public AsyncObjectProperty<IDrone> currentDroneProperty() {
        return currentDrone;
    }

    /** The current flight segment. */
    public AsyncObjectProperty<FlightSegment> flightSegmentProperty() {
        return flightSegment;
    }

    /** The mission selected for execution. */
    public AsyncObjectProperty<FlightPlan> selectedFlightPlanProperty() {
        return selectedFlightPlan;
    }

    /** The next waypoint index of the selected mission. */
    public AsyncIntegerProperty nextWayPointIndexProperty() {
        return nextWayPointIndex;
    }
}
