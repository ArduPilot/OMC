/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.persistence.insight.primitives;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public final class RealBbox {
   @SerializedName("type")
   @Expose
   @Nullable
   private String type;
   @SerializedName("coordinates")
   @Expose
   @NotNull
   private List<List<List<Double>>> coordinates = new ArrayList<>();
   @SerializedName("bbox")
   @Expose
   @NotNull
   private List<Double> bbox = (List<Double>)(new ArrayList<Double>());

   @Nullable
   public final String getType() {
      return this.type;
   }

   public final void setType(@Nullable String var1) {
      this.type = var1;
   }

   @NotNull
   public final List<List<List<Double>>> getCoordinates() {
      return this.coordinates;
   }

   public final void setCoordinates(@NotNull List<List<List<Double>>> var1) {
            this.coordinates = var1;
   }

   @NotNull
   public final List<Double> getBbox() {
      return this.bbox;
   }

   public final void setBbox(@NotNull List<Double> var1) {
            this.bbox = var1;
   }
}
