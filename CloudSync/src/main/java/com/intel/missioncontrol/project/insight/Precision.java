/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.insight;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.Nullable;


public final class Precision {
   @SerializedName("gsd")
   @Expose
   @Nullable
   private Gsd gsd;
   @SerializedName("xy")
   @Expose
   @Nullable
   private Xy xy;
   @SerializedName("z")
   @Expose
   @Nullable
   private Z z;

   @Nullable
   public final Gsd getGsd() {
      return this.gsd;
   }

   public final void setGsd(@Nullable Gsd var1) {
      this.gsd = var1;
   }

   @Nullable
   public final Xy getXy() {
      return this.xy;
   }

   public final void setXy(@Nullable Xy var1) {
      this.xy = var1;
   }

   @Nullable
   public final Z getZ() {
      return this.z;
   }

   public final void setZ(@Nullable Z var1) {
      this.z = var1;
   }
}
