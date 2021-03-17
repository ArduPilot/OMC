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


public final class Image {
   @SerializedName("vertical_srs_wkt")
   @Expose
   @Nullable
   private String verticalSrsWkt;
   @SerializedName("horizontal_srs_wkt")
   @Expose
   @Nullable
   private String horizontalSrsWkt;

   @Nullable
   public final String getVerticalSrsWkt() {
      return this.verticalSrsWkt;
   }

   public final void setVerticalSrsWkt(@Nullable String var1) {
      this.verticalSrsWkt = var1;
   }

   @Nullable
   public final String getHorizontalSrsWkt() {
      return this.horizontalSrsWkt;
   }

   public final void setHorizontalSrsWkt(@Nullable String var1) {
      this.horizontalSrsWkt = var1;
   }

   @NotNull
   public final Image withVerticalSrsWkt(@NotNull String verticalSrsWkt) {
            this.verticalSrsWkt = verticalSrsWkt;
      return this;
   }

   @NotNull
   public final Image withHorizontalSrsWkt(@NotNull String horizontalSrsWkt) {
            this.horizontalSrsWkt = horizontalSrsWkt;
      return this;
   }
}
