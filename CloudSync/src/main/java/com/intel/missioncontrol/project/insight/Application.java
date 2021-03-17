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


public final class Application {
   @SerializedName("name")
   @Expose
   @Nullable
   private String name;
   @SerializedName("options")
   @Expose
   @NotNull
   private List options = new ArrayList();

   @Nullable
   public final String getName() {
      return this.name;
   }

   public final void setName(@Nullable String var1) {
      this.name = var1;
   }

   @NotNull
   public final List getOptions() {
      return this.options;
   }

   public final void setOptions(@NotNull List var1) {
            this.options = var1;
   }
}
