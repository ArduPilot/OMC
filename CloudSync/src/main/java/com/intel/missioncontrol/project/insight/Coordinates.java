/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.insight;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public final class Coordinates {
   @SerializedName("type")
   @Expose
   @Nullable
   private String type;
   @SerializedName("coordinates")
   @Expose
   @NotNull
   private List coordinates = new ArrayList();

   @Nullable
   public final String getType() {
      return this.type;
   }

   public final void setType(@Nullable String var1) {
      this.type = var1;
   }

   @NotNull
   public final List getCoordinates() {
      return this.coordinates;
   }

   public final void setCoordinates(@NotNull List var1) {
            this.coordinates = var1;
   }
}
