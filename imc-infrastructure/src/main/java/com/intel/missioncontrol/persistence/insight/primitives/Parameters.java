/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.persistence.insight.primitives;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public final class Parameters {
   @SerializedName("flight_id")
   @Expose
   @Nullable
   private String flightId;
   @SerializedName("processSettings")
   @Expose
   @Nullable
   private ProcessSettings processSettings;

   @Nullable
   public final String getFlightId() {
      return this.flightId;
   }

   public final void setFlightId(@Nullable String var1) {
      this.flightId = var1;
   }

   @Nullable
   public final ProcessSettings getProcessSettings() {
      return this.processSettings;
   }

   public final void setProcessSettings(@Nullable ProcessSettings var1) {
      this.processSettings = var1;
   }

   @NotNull
   public final Parameters withFlightId(@NotNull String flightId) {
            this.flightId = flightId;
      return this;
   }

   @NotNull
   public final Parameters withProcessSettings(@NotNull ProcessSettings processSettings) {
            this.processSettings = processSettings;
      return this;
   }
}
