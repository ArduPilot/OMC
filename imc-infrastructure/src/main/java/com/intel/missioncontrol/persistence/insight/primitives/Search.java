/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.persistence.insight.primitives;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.NotNull;


public final class Search {
   @SerializedName("search")
   @Expose
   @NotNull
   private String search = "";

   @NotNull
   public final String getSearch() {
      return this.search;
   }

   public final void setSearch(@NotNull String var1) {
            this.search = var1;
   }

   @NotNull
   public final Search withSearch(@NotNull String search) {
            this.search = search;
      return this;
   }
}
