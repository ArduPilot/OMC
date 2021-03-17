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


public final class Photogrammetry {
   @SerializedName("analytics")
   @Expose
   @Nullable
   private String analytics;
   @SerializedName("_id")
   @Expose
   @Nullable
   private String id;
   @SerializedName("parameters")
   @Expose
   @Nullable
   private Parameters parameters;

   @Nullable
   public final String getAnalytics() {
      return this.analytics;
   }

   public final void setAnalytics(@Nullable String var1) {
      this.analytics = var1;
   }

   @Nullable
   public final String getId() {
      return this.id;
   }

   public final void setId(@Nullable String var1) {
      this.id = var1;
   }

   @Nullable
   public final Parameters getParameters() {
      return this.parameters;
   }

   public final void setParameters(@Nullable Parameters var1) {
      this.parameters = var1;
   }

   @NotNull
   public final Photogrammetry withAnalytics(@NotNull String analytics) {
            this.analytics = analytics;
      return this;
   }

   @NotNull
   public final Photogrammetry withId(@NotNull String id) {
            this.id = id;
      return this;
   }

   @NotNull
   public final Photogrammetry withParameters(@NotNull Parameters parameters) {
            this.parameters = parameters;
      return this;
   }
}
