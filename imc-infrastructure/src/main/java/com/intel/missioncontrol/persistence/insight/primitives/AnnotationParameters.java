/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.persistence.insight.primitives;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.Nullable;


public final class AnnotationParameters {
   @SerializedName("name")
   @Expose
   @Nullable
   private String name = "";
   @SerializedName("gsd")
   @Expose
   @Nullable
   private Double gsd = 0.0D;
   @SerializedName("distance")
   @Expose
   @Nullable
   private Double distance = 0.0D;
   @SerializedName("yaw")
   @Expose
   @Nullable
   private Double yaw = 0.0D;
   @SerializedName("type")
   @Expose
   @Nullable
   private AreaOfInterestType type;
   @SerializedName("forward_overlap")
   @Expose
   @Nullable
   private Double forwardOverlap;
   @SerializedName("forward_overlap_min")
   @Expose
   @Nullable
   private Double forwardOverlapMin;
   @SerializedName("lateral_overlap")
   @Expose
   @Nullable
   private Double lateralOverlap;
   @SerializedName("lateral_overlap_min")
   @Expose
   @Nullable
   private Double lateralOverlapMin;
   @SerializedName("min_ground_distance")
   @Expose
   @Nullable
   private Double minGroundDistance;
   @SerializedName("scan_direction")
   @Expose
   @Nullable
   private String scanDirection;
   @SerializedName("start_capture")
   @Expose
   @Nullable
   private String startCapture;
   @SerializedName("start_capture_vertically")
   @Expose
   @Nullable
   private String startCaptureVertically;
   @SerializedName("single_direction")
   @Expose
   @Nullable
   private Boolean singleDirection;
   @SerializedName("camera_roll")
   @Expose
   @Nullable
   private Double cameraRoll;
   @SerializedName("camera_roll_offset")
   @Expose
   @Nullable
   private Double cameraRollOffset;
   @SerializedName("camera_tilt")
   @Expose
   @Nullable
   private Double cameraTilt;
   @SerializedName("rotation_direction")
   @Expose
   @Nullable
   private Double rotationDirection;
   @SerializedName("max_pitch_change")
   @Expose
   @Nullable
   private Double maxPitchChange;
   @SerializedName("max_yaw_roll_change")
   @Expose
   @Nullable
   private Double maxYawRollChange;
   @SerializedName("camera_pitch_offset_line_begin")
   @Expose
   @Nullable
   private Double cameraPitchOffsetLineBegin;
   @SerializedName("camera_pitch_offset")
   @Expose
   @Nullable
   private Double cameraPitchOffset;

   @Nullable
   public final String getName() {
      return this.name;
   }

   public final void setName(@Nullable String var1) {
      this.name = var1;
   }

   @Nullable
   public final Double getGsd() {
      return this.gsd;
   }

   public final void setGsd(@Nullable Double var1) {
      this.gsd = var1;
   }

   @Nullable
   public final Double getDistance() {
      return this.distance;
   }

   public final void setDistance(@Nullable Double var1) {
      this.distance = var1;
   }

   @Nullable
   public final Double getYaw() {
      return this.yaw;
   }

   public final void setYaw(@Nullable Double var1) {
      this.yaw = var1;
   }

   @Nullable
   public final AreaOfInterestType getType() {
      return this.type;
   }

   public final void setType(@Nullable AreaOfInterestType var1) {
      this.type = var1;
   }

   @Nullable
   public final Double getForwardOverlap() {
      return this.forwardOverlap;
   }

   public final void setForwardOverlap(@Nullable Double var1) {
      this.forwardOverlap = var1;
   }

   @Nullable
   public final Double getForwardOverlapMin() {
      return this.forwardOverlapMin;
   }

   public final void setForwardOverlapMin(@Nullable Double var1) {
      this.forwardOverlapMin = var1;
   }

   @Nullable
   public final Double getLateralOverlap() {
      return this.lateralOverlap;
   }

   public final void setLateralOverlap(@Nullable Double var1) {
      this.lateralOverlap = var1;
   }

   @Nullable
   public final Double getLateralOverlapMin() {
      return this.lateralOverlapMin;
   }

   public final void setLateralOverlapMin(@Nullable Double var1) {
      this.lateralOverlapMin = var1;
   }

   @Nullable
   public final Double getMinGroundDistance() {
      return this.minGroundDistance;
   }

   public final void setMinGroundDistance(@Nullable Double var1) {
      this.minGroundDistance = var1;
   }

   @Nullable
   public final String getScanDirection() {
      return this.scanDirection;
   }

   public final void setScanDirection(@Nullable String var1) {
      this.scanDirection = var1;
   }

   @Nullable
   public final String getStartCapture() {
      return this.startCapture;
   }

   public final void setStartCapture(@Nullable String var1) {
      this.startCapture = var1;
   }

   @Nullable
   public final String getStartCaptureVertically() {
      return this.startCaptureVertically;
   }

   public final void setStartCaptureVertically(@Nullable String var1) {
      this.startCaptureVertically = var1;
   }

   @Nullable
   public final Boolean getSingleDirection() {
      return this.singleDirection;
   }

   public final void setSingleDirection(@Nullable Boolean var1) {
      this.singleDirection = var1;
   }

   @Nullable
   public final Double getCameraRoll() {
      return this.cameraRoll;
   }

   public final void setCameraRoll(@Nullable Double var1) {
      this.cameraRoll = var1;
   }

   @Nullable
   public final Double getCameraRollOffset() {
      return this.cameraRollOffset;
   }

   public final void setCameraRollOffset(@Nullable Double var1) {
      this.cameraRollOffset = var1;
   }

   @Nullable
   public final Double getCameraTilt() {
      return this.cameraTilt;
   }

   public final void setCameraTilt(@Nullable Double var1) {
      this.cameraTilt = var1;
   }

   @Nullable
   public final Double getRotationDirection() {
      return this.rotationDirection;
   }

   public final void setRotationDirection(@Nullable Double var1) {
      this.rotationDirection = var1;
   }

   @Nullable
   public final Double getMaxPitchChange() {
      return this.maxPitchChange;
   }

   public final void setMaxPitchChange(@Nullable Double var1) {
      this.maxPitchChange = var1;
   }

   @Nullable
   public final Double getMaxYawRollChange() {
      return this.maxYawRollChange;
   }

   public final void setMaxYawRollChange(@Nullable Double var1) {
      this.maxYawRollChange = var1;
   }

   @Nullable
   public final Double getCameraPitchOffsetLineBegin() {
      return this.cameraPitchOffsetLineBegin;
   }

   public final void setCameraPitchOffsetLineBegin(@Nullable Double var1) {
      this.cameraPitchOffsetLineBegin = var1;
   }

   @Nullable
   public final Double getCameraPitchOffset() {
      return this.cameraPitchOffset;
   }

   public final void setCameraPitchOffset(@Nullable Double var1) {
      this.cameraPitchOffset = var1;
   }

   public AnnotationParameters() {
      this.type = AreaOfInterestType.POLYGON;
      this.forwardOverlap = 0.0D;
      this.forwardOverlapMin = 0.0D;
      this.lateralOverlap = 0.0D;
      this.lateralOverlapMin = 0.0D;
      this.minGroundDistance = 0.0D;
      this.scanDirection = "";
      this.startCapture = "";
      this.startCaptureVertically = "";
      this.singleDirection = false;
      this.cameraRoll = 0.0D;
      this.cameraRollOffset = 0.0D;
      this.cameraTilt = 0.0D;
      this.rotationDirection = 0.0D;
      this.maxPitchChange = 0.0D;
      this.maxYawRollChange = 0.0D;
      this.cameraPitchOffsetLineBegin = 0.0D;
      this.cameraPitchOffset = 0.0D;
   }
}
