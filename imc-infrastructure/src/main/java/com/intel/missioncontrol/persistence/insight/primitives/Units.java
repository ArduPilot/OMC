/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.persistence.insight.primitives;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.Nullable;


public final class Units {
   @SerializedName("distances")
   @Expose
   @Nullable
   private String distances;
   @SerializedName("surfaces")
   @Expose
   @Nullable
   private String surfaces;
   @SerializedName("volumes")
   @Expose
   @Nullable
   private String volumes;
   @SerializedName("altitude")
   @Expose
   @Nullable
   private String altitude;
   @SerializedName("gsd")
   @Expose
   @Nullable
   private String gsd;

   @Nullable
   public final String getDistances() {
      return this.distances;
   }

   public final void setDistances(@Nullable String var1) {
      this.distances = var1;
   }

   @Nullable
   public final String getSurfaces() {
      return this.surfaces;
   }

   public final void setSurfaces(@Nullable String var1) {
      this.surfaces = var1;
   }

   @Nullable
   public final String getVolumes() {
      return this.volumes;
   }

   public final void setVolumes(@Nullable String var1) {
      this.volumes = var1;
   }

   @Nullable
   public final String getAltitude() {
      return this.altitude;
   }

   public final void setAltitude(@Nullable String var1) {
      this.altitude = var1;
   }

   @Nullable
   public final String getGsd() {
      return this.gsd;
   }

   public final void setGsd(@Nullable String var1) {
      this.gsd = var1;
   }
}
