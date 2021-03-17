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


public final class Missions {
   @SerializedName("count")
   @Expose
   private int count;
   @SerializedName("missions")
   @Expose
   @NotNull
   private List<Mission> missions = new ArrayList();

   public final int getCount() {
      return this.count;
   }

   public final void setCount(int var1) {
      this.count = var1;
   }

   @NotNull
   public final List<Mission> getMissions() {
      return this.missions;
   }

   public final void setMissions(@NotNull List var1) {
            this.missions = var1;
   }
}
