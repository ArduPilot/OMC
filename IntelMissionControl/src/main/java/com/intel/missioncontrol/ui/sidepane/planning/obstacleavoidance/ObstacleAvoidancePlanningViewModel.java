/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.planning.obstacleavoidance;

import com.google.inject.Inject;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.mission.FlightPlan;
import com.intel.missioncontrol.mission.Mission;
import com.intel.missioncontrol.ui.ViewModelBase;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.asyncfx.beans.property.PropertyPathStore;

public class ObstacleAvoidancePlanningViewModel extends ViewModelBase {

    private final IApplicationContext applicationContext;
    private final BooleanProperty enableObstacleAvoidance = new SimpleBooleanProperty();
    private final BooleanProperty enableAddSafetyWaypoints = new SimpleBooleanProperty();
    private final BooleanProperty isHardwareOACapable = new SimpleBooleanProperty();
    private final PropertyPathStore propertyPathStore = new PropertyPathStore();

    @Inject
    public ObstacleAvoidancePlanningViewModel(IApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    protected void initializeViewModel() {
        super.initializeViewModel();

        enableObstacleAvoidance.bindBidirectional(
            propertyPathStore
                .from(applicationContext.currentMissionProperty())
                .select(Mission::currentFlightPlanProperty)
                .selectBoolean(FlightPlan::obstacleAvoidanceEnabledProperty));
        enableAddSafetyWaypoints.bindBidirectional(
            propertyPathStore
                .from(applicationContext.currentMissionProperty())
                .select(Mission::currentFlightPlanProperty)
                .selectBoolean(FlightPlan::enableJumpOverWaypointsProperty));
        isHardwareOACapable.bindBidirectional(
            propertyPathStore
                .from(applicationContext.currentMissionProperty())
                .select(Mission::currentFlightPlanProperty)
                .selectBoolean(FlightPlan::isHardwareOACapable));
    }

    public BooleanProperty enableAddSafetyWaypointsProperty() {
        return enableAddSafetyWaypoints;
    }

    public BooleanProperty enableObstacleAvoidanceProperty() {
        return enableObstacleAvoidance;
    }

    /**
     * Is the selected Hardware has obstacle avoidance sensors or not.
     *
     * @return true- available, false - not available.
     */
    public BooleanProperty hardwareOACapableProperty() {
        return isHardwareOACapable;
    }
}
