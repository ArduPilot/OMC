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


public final class Waypoint {
   @SerializedName("type")
   @Expose
   @Nullable
   private String type = "SURVEY";
   @SerializedName("drone_point")
   @Expose
   @NotNull
   private List dronePoint = new ArrayList();
   @SerializedName("target_point")
   @Expose
   @NotNull
   private List targetPoint = new ArrayList();
   @SerializedName("roll")
   @Expose
   @Nullable
   private Double roll;
   @SerializedName("pitch")
   @Expose
   @Nullable
   private Double pitch;
   @SerializedName("yaw")
   @Expose
   @Nullable
   private Double yaw;
   @SerializedName("speed")
   @Expose
   @Nullable
   private Double speed;
   @SerializedName("acc")
   @Expose
   @Nullable
   private Double acc;
   @SerializedName("warning")
   @Expose
   @Nullable
   private String warning;

   @Nullable
   public final String getType() {
      return this.type;
   }

   public final void setType(@Nullable String var1) {
      this.type = var1;
   }

   @NotNull
   public final List getDronePoint() {
      return this.dronePoint;
   }

   public final void setDronePoint(@NotNull List var1) {
            this.dronePoint = var1;
   }

   @NotNull
   public final List getTargetPoint() {
      return this.targetPoint;
   }

   public final void setTargetPoint(@NotNull List var1) {
            this.targetPoint = var1;
   }

   @Nullable
   public final Double getRoll() {
      return this.roll;
   }

   public final void setRoll(@Nullable Double var1) {
      this.roll = var1;
   }

   @Nullable
   public final Double getPitch() {
      return this.pitch;
   }

   public final void setPitch(@Nullable Double var1) {
      this.pitch = var1;
   }

   @Nullable
   public final Double getYaw() {
      return this.yaw;
   }

   public final void setYaw(@Nullable Double var1) {
      this.yaw = var1;
   }

   @Nullable
   public final Double getSpeed() {
      return this.speed;
   }

   public final void setSpeed(@Nullable Double var1) {
      this.speed = var1;
   }

   @Nullable
   public final Double getAcc() {
      return this.acc;
   }

   public final void setAcc(@Nullable Double var1) {
      this.acc = var1;
   }

   @Nullable
   public final String getWarning() {
      return this.warning;
   }

   public final void setWarning(@Nullable String var1) {
      this.warning = var1;
   }

   @NotNull
   public final Waypoint withType(@NotNull String type) {
            this.type = type;
      return this;
   }
}
