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


public final class Camera {
   @SerializedName("model")
   @Expose
   @Nullable
   private String model;
   @SerializedName("width")
   @Expose
   @Nullable
   private Integer width;
   @SerializedName("height")
   @Expose
   @Nullable
   private Integer height;
   @SerializedName("fnumber")
   @Expose
   @Nullable
   private Integer fnumber;
   @SerializedName("focal_length")
   @Expose
   @Nullable
   private Integer focalLength;
   @SerializedName("aspect_ratio")
   @Expose
   @Nullable
   private Double aspectRatio;
   @SerializedName("calibration")
   @Expose
   @NotNull
   private List calibration = new ArrayList();
   @SerializedName("_id")
   @Expose
   @Nullable
   private String id;
   @SerializedName("created")
   @Expose
   @Nullable
   private String created;

   @Nullable
   public final String getModel() {
      return this.model;
   }

   public final void setModel(@Nullable String var1) {
      this.model = var1;
   }

   @Nullable
   public final Integer getWidth() {
      return this.width;
   }

   public final void setWidth(@Nullable Integer var1) {
      this.width = var1;
   }

   @Nullable
   public final Integer getHeight() {
      return this.height;
   }

   public final void setHeight(@Nullable Integer var1) {
      this.height = var1;
   }

   @Nullable
   public final Integer getFnumber() {
      return this.fnumber;
   }

   public final void setFnumber(@Nullable Integer var1) {
      this.fnumber = var1;
   }

   @Nullable
   public final Integer getFocalLength() {
      return this.focalLength;
   }

   public final void setFocalLength(@Nullable Integer var1) {
      this.focalLength = var1;
   }

   @Nullable
   public final Double getAspectRatio() {
      return this.aspectRatio;
   }

   public final void setAspectRatio(@Nullable Double var1) {
      this.aspectRatio = var1;
   }

   @NotNull
   public final List getCalibration() {
      return this.calibration;
   }

   public final void setCalibration(@NotNull List var1) {
            this.calibration = var1;
   }

   @Nullable
   public final String getId() {
      return this.id;
   }

   public final void setId(@Nullable String var1) {
      this.id = var1;
   }

   @Nullable
   public final String getCreated() {
      return this.created;
   }

   public final void setCreated(@Nullable String var1) {
      this.created = var1;
   }
}
