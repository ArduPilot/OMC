/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.api;

import com.intel.missioncontrol.flightplantemplate.FlightPlanTemplate;
import com.intel.missioncontrol.geometry.AreaOfInterest;
import com.intel.missioncontrol.mission.FlightPlan;
import com.intel.missioncontrol.mission.Mission;
import eu.mavinci.core.flightplan.PlanType;
import eu.mavinci.flightplan.Flightplan;

public interface IFlightPlanService {

    FlightPlan createFlightPlan(FlightPlanTemplate template, Mission mission);

    void saveFlightPlan(Mission mission, FlightPlan flightPlan);

    String cloneFlightPlanLocally(Flightplan flightPlan, String fpFullPath);

    String generateDefaultName(PlanType type);

    void renameFlightPlan(Mission mission, FlightPlan flightPlan, String flightPlanName);

    void remove(Mission mission, FlightPlan flightPlan);

    void updateTemplateAoi(FlightPlan currentFlightplan, AreaOfInterest areaOfInterest);

}
