/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.persistence.insight.primitives;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.Nullable;


public final class Status {
   @SerializedName("_id")
   @Expose
   @Nullable
   private Integer id;
   @SerializedName("name")
   @Expose
   @Nullable
   private String name;

   @Nullable
   public final Integer getId() {
      return this.id;
   }

   public final void setId(@Nullable Integer var1) {
      this.id = var1;
   }

   @Nullable
   public final String getName() {
      return this.name;
   }

   public final void setName(@Nullable String var1) {
      this.name = var1;
   }
}
