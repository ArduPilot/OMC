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


public final class Feature {
   @SerializedName("geometry")
   @Expose
   @Nullable
   private Geometry geometry;
   @SerializedName("type")
   @Expose
   @Nullable
   private String type;
   @SerializedName("properties")
   @Expose
   @Nullable
   private Properties properties;

   @Nullable
   public final Geometry getGeometry() {
      return this.geometry;
   }

   public final void setGeometry(@Nullable Geometry var1) {
      this.geometry = var1;
   }

   @Nullable
   public final String getType() {
      return this.type;
   }

   public final void setType(@Nullable String var1) {
      this.type = var1;
   }

   @Nullable
   public final Properties getProperties() {
      return this.properties;
   }

   public final void setProperties(@Nullable Properties var1) {
      this.properties = var1;
   }

   @NotNull
   public final Feature withGeometry(@NotNull Geometry geometry) {
            this.geometry = geometry;
      return this;
   }

   @NotNull
   public final Feature withType(@NotNull String type) {
            this.type = type;
      return this;
   }

   @NotNull
   public final Feature withProperties(@NotNull Properties properties) {
            this.properties = properties;
      return this;
   }
}
