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


public final class Geometries {
   @SerializedName("type")
   @Expose
   @Nullable
   private String type;
   @SerializedName("geometries")
   @Expose
   @NotNull
   private List geometries = new ArrayList();

   @Nullable
   public final String getType() {
      return this.type;
   }

   public final void setType(@Nullable String var1) {
      this.type = var1;
   }

   @NotNull
   public final List getGeometries() {
      return this.geometries;
   }

   public final void setGeometries(@NotNull List var1) {
            this.geometries = var1;
   }
}
