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


public final class CreatedBy {
   @SerializedName("id")
   @Expose
   @Nullable
   private String id;
   @SerializedName("displayName")
   @Expose
   @Nullable
   private String displayName;

   @Nullable
   public final String getId() {
      return this.id;
   }

   public final void setId(@Nullable String var1) {
      this.id = var1;
   }

   @Nullable
   public final String getDisplayName() {
      return this.displayName;
   }

   public final void setDisplayName(@Nullable String var1) {
      this.displayName = var1;
   }

   @NotNull
   public final CreatedBy withId(@NotNull String id) {
            this.id = id;
      return this;
   }

   @NotNull
   public final CreatedBy withDisplayName(@NotNull String displayName) {
            this.displayName = displayName;
      return this;
   }
}
