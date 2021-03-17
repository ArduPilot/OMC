/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.persistence.insight.primitives;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public final class Mapping {
   @SerializedName("preset")
   @Expose
   @Nullable
   private String preset;
   @SerializedName("mesh")
   @Expose
   @Nullable
   private Boolean mesh;
   @SerializedName("processingAreaSetting")
   @Expose
   @Nullable
   private String processingAreaSetting;
   @SerializedName("crs")
   @Expose
   @Nullable
   private Crs crs;
   @SerializedName("photogrammetry_params")
   @Expose
   @JsonAdapter(RawJsonAdapter.class)
   @Nullable
   private String photogrammetry_params;

   @Nullable
   public final String getPreset() {
      return this.preset;
   }

   public final void setPreset(@Nullable String var1) {
      this.preset = var1;
   }

   @Nullable
   public final Boolean getMesh() {
      return this.mesh;
   }

   public final void setMesh(@Nullable Boolean var1) {
      this.mesh = var1;
   }

   @Nullable
   public final String getProcessingAreaSetting() {
      return this.processingAreaSetting;
   }

   public final void setProcessingAreaSetting(@Nullable String var1) {
      this.processingAreaSetting = var1;
   }

   @Nullable
   public final Crs getCrs() {
      return this.crs;
   }

   public final void setCrs(@Nullable Crs var1) {
      this.crs = var1;
   }

   @Nullable
   public final String getPhotogrammetry_params() {
      return this.photogrammetry_params;
   }

   public final void setPhotogrammetry_params(@Nullable String var1) {
      this.photogrammetry_params = var1;
   }

   @NotNull
   public final Mapping withPreset(@NotNull String preset) {
            this.preset = preset;
      return this;
   }

   @NotNull
   public final Mapping withMesh(@Nullable Boolean mesh) {
      this.mesh = mesh;
      return this;
   }

   @NotNull
   public final Mapping withProcessingAreaSetting(@NotNull String processingAreaSetting) {
            this.processingAreaSetting = processingAreaSetting;
      return this;
   }

   @NotNull
   public final Mapping withCrs(@NotNull Crs crs) {
            this.crs = crs;
      return this;
   }
}
