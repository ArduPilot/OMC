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


public final class Data {
   @SerializedName("bbox")
   @Expose
   @NotNull
   private List bbox = new ArrayList();
   @SerializedName("geometry")
   @Expose
   @Nullable
   private Coordinates geometry;

   @NotNull
   public final List getBbox() {
      return this.bbox;
   }

   public final void setBbox(@NotNull List var1) {
            this.bbox = var1;
   }

   @Nullable
   public final Coordinates getGeometry() {
      return this.geometry;
   }

   public final void setGeometry(@Nullable Coordinates var1) {
      this.geometry = var1;
   }
}
