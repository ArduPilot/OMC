/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.flightplantemplate;

import eu.mavinci.flightplan.InvalidFlightPlanFileException;

/** Created by ekorotkova on 21.12.2017. */
@FunctionalInterface
public interface FlightPlanCreator<File, Flightplan> {

    Flightplan apply(File t) throws InvalidFlightPlanFileException;

}
