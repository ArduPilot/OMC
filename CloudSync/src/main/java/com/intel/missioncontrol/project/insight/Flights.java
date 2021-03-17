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


public final class Flights {
   @SerializedName("flights")
   @Expose
   @NotNull
   private List<Flight> flights = new ArrayList();

   @NotNull
   public final List<Flight> getFlights() {
      return this.flights;
   }

   public final void setFlights(@NotNull List var1) {
            this.flights = var1;
   }
}
