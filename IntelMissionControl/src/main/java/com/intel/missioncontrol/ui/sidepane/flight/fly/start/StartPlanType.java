/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight.fly.start;

import com.intel.missioncontrol.Localizable;
import eu.mavinci.core.obfuscation.IKeepAll;

@Localizable
public enum StartPlanType implements IKeepAll {
    RESUME_PLAN,
    START_PLAN_FROM_BEGINNING,
    START_PLAN_FROM_WAYPOINT
}
