/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.insight;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.Nullable;


public final class Gsd {
   @SerializedName("value")
   @Expose
   @Nullable
   private Integer value;
   @SerializedName("selected")
   @Expose
   @Nullable
   private Boolean selected;

   @Nullable
   public final Integer getValue() {
      return this.value;
   }

   public final void setValue(@Nullable Integer var1) {
      this.value = var1;
   }

   @Nullable
   public final Boolean getSelected() {
      return this.selected;
   }

   public final void setSelected(@Nullable Boolean var1) {
      this.selected = var1;
   }
}
