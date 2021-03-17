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


public final class FlightRequests {
   @SerializedName("flightRequests")
   @Expose
   @NotNull
   private List flightRequests = new ArrayList();

   @NotNull
   public final List getFlightRequests() {
      return this.flightRequests;
   }

   public final void setFlightRequests(@NotNull List var1) {
            this.flightRequests = var1;
   }

   @NotNull
   public final FlightRequests withFlightRequests(@NotNull List flightRequests) {
            this.flightRequests = flightRequests;
      return this;
   }
}
