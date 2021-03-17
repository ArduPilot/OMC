/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.insight;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public final class Target {
   @SerializedName("type")
   @Expose
   @Nullable
   private String type;
   @SerializedName("id")
   @Expose
   @Nullable
   private String id;
   @SerializedName("subId")
   @Expose
   @Nullable
   private String subId;

   @Nullable
   public final String getType() {
      return this.type;
   }

   public final void setType(@Nullable String var1) {
      this.type = var1;
   }

   @Nullable
   public final String getId() {
      return this.id;
   }

   public final void setId(@Nullable String var1) {
      this.id = var1;
   }

   @Nullable
   public final String getSubId() {
      return this.subId;
   }

   public final void setSubId(@Nullable String var1) {
      this.subId = var1;
   }

   @NotNull
   public final Target withType(@NotNull String type) {
            this.type = type;
      return this;
   }
}
