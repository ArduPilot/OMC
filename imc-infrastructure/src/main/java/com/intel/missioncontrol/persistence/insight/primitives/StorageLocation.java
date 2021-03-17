/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.persistence.insight.primitives;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.Nullable;


public final class StorageLocation {
   @SerializedName("_id")
   @Expose
   @Nullable
   private String id;
   @SerializedName("location_name")
   @Expose
   @Nullable
   private String locationName;
   @SerializedName("region")
   @Expose
   @Nullable
   private String region;
   @SerializedName("driver")
   @Expose
   @Nullable
   private String driver;
   @SerializedName("access_date")
   @Expose
   @Nullable
   private String accessDate;
   @SerializedName("creation_date")
   @Expose
   @Nullable
   private String creationDate;

   @Nullable
   public final String getId() {
      return this.id;
   }

   public final void setId(@Nullable String var1) {
      this.id = var1;
   }

   @Nullable
   public final String getLocationName() {
      return this.locationName;
   }

   public final void setLocationName(@Nullable String var1) {
      this.locationName = var1;
   }

   @Nullable
   public final String getRegion() {
      return this.region;
   }

   public final void setRegion(@Nullable String var1) {
      this.region = var1;
   }

   @Nullable
   public final String getDriver() {
      return this.driver;
   }

   public final void setDriver(@Nullable String var1) {
      this.driver = var1;
   }

   @Nullable
   public final String getAccessDate() {
      return this.accessDate;
   }

   public final void setAccessDate(@Nullable String var1) {
      this.accessDate = var1;
   }

   @Nullable
   public final String getCreationDate() {
      return this.creationDate;
   }

   public final void setCreationDate(@Nullable String var1) {
      this.creationDate = var1;
   }
}
