/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.insight;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public final class Feature3D {
   @SerializedName("geometry")
   @Expose
   @Nullable
   private Geometry3D geometry;
   @SerializedName("type")
   @Expose
   @Nullable
   private String type;
   @SerializedName("properties")
   @Expose
   @Nullable
   private Properties properties;

   @Nullable
   public final Geometry3D getGeometry() {
      return this.geometry;
   }

   public final void setGeometry(@Nullable Geometry3D var1) {
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
   public final Feature3D withGeometry(@NotNull Geometry3D geometry) {
            this.geometry = geometry;
      return this;
   }

   @NotNull
   public final Feature3D withType(@NotNull String type) {
            this.type = type;
      return this;
   }

   @NotNull
   public final Feature3D withProperties(@NotNull Properties properties) {
            this.properties = properties;
      return this;
   }
}
