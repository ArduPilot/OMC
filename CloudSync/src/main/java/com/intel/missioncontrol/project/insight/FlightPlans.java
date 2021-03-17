/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.insight;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public final class FlightPlans {
    @SerializedName("flight_plans")
    @Expose
    @NotNull
    private List<FlightPlan> flightPlans = new ArrayList();

    @NotNull
    public final List<FlightPlan> getFlightPlans() {
        return this.flightPlans;
    }

    public final void setFlightPlans(@NotNull List var1) {
                this.flightPlans = var1;
    }
}
