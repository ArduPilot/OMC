/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.insight;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.Nullable;


public final class Delivery {
   @SerializedName("properties")
   @Expose
   @Nullable
   private Object properties;

   @Nullable
   public final Object getProperties() {
      return this.properties;
   }

   public final void setProperties(@Nullable Object var1) {
      this.properties = var1;
   }
}
