/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.persistence.insight.primitives;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public final class Photo {
   @Nullable
   private String fileName;
   @SerializedName("__v")
   @Expose
   @Nullable
   private Integer v;
   @SerializedName("seq")
   @Expose
   @Nullable
   private String seq;
   @SerializedName("altitude")
   @Expose
   @Nullable
   private Double altitude;
   @SerializedName("UTC")
   @Expose
   @Nullable
   private String utc;
   @SerializedName("RTC")
   @Expose
   @Nullable
   private Integer rtc;
   @SerializedName("camera")
   @Expose
   @Nullable
   private String camera;
   @SerializedName("width")
   @Expose
   @Nullable
   private Integer width;
   @SerializedName("height")
   @Expose
   @Nullable
   private Integer height;
   @SerializedName("vertical_srs_wkt")
   @Expose
   @Nullable
   private String verticalSrsWkt;
   @SerializedName("horizontal_srs_wkt")
   @Expose
   @Nullable
   private String horizontalSrsWkt;
   @SerializedName("upload_id")
   @Expose
   @Nullable
   private String uploadId;
   @SerializedName("flight")
   @Expose
   @Nullable
   private String flight;
   @SerializedName("_id")
   @Expose
   @Nullable
   private String id;
   @SerializedName("metadata")
   @Expose
   @NotNull
   private List metadata = new ArrayList();
   @SerializedName("storage_locations")
   @Expose
   @NotNull
   private List storageLocations = new ArrayList();
   @SerializedName("calibration")
   @Expose
   @Nullable
   private Calibration calibration;
   @SerializedName("modified")
   @Expose
   @Nullable
   private String modified;
   @SerializedName("created")
   @Expose
   @Nullable
   private String created;
   @SerializedName("status")
   @Expose
   @Nullable
   private String status;
   @SerializedName("types")
   @Expose
   @NotNull
   private List types = new ArrayList();
   @SerializedName("tilt_deg")
   @Expose
   @Nullable
   private Integer tiltDeg;
   @SerializedName("pan_deg")
   @Expose
   @Nullable
   private Integer panDeg;
   @SerializedName("sharpened")
   @Expose
   @Nullable
   private Boolean sharpened;
   @SerializedName("gain")
   @Expose
   @Nullable
   private Integer gain;
   @SerializedName("shutter")
   @Expose
   @Nullable
   private Double shutter;
   @SerializedName("geometry")
   @Expose
   @Nullable
   private PhotoGeometry geometry;
   @SerializedName("tags")
   @Expose
   @NotNull
   private List tags = new ArrayList();
   @SerializedName("ground_footprint")
   @Expose
   @Nullable
   private GroundFootprint groundFootprint;
   @SerializedName("phi")
   @Expose
   @Nullable
   private Double phi;
   @SerializedName("psi")
   @Expose
   @Nullable
   private Double psi;
   @SerializedName("theta")
   @Expose
   @Nullable
   private Double theta;

   @Nullable
   public final String getFileName() {
      return this.fileName;
   }

   public final void setFileName(@Nullable String var1) {
      this.fileName = var1;
   }

   @Nullable
   public final Integer getV() {
      return this.v;
   }

   public final void setV(@Nullable Integer var1) {
      this.v = var1;
   }

   @Nullable
   public final String getSeq() {
      return this.seq;
   }

   public final void setSeq(@Nullable String var1) {
      this.seq = var1;
   }

   @Nullable
   public final Double getAltitude() {
      return this.altitude;
   }

   public final void setAltitude(@Nullable Double var1) {
      this.altitude = var1;
   }

   @Nullable
   public final String getUtc() {
      return this.utc;
   }

   public final void setUtc(@Nullable String var1) {
      this.utc = var1;
   }

   @Nullable
   public final Integer getRtc() {
      return this.rtc;
   }

   public final void setRtc(@Nullable Integer var1) {
      this.rtc = var1;
   }

   @Nullable
   public final String getCamera() {
      return this.camera;
   }

   public final void setCamera(@Nullable String var1) {
      this.camera = var1;
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
   public final String getVerticalSrsWkt() {
      return this.verticalSrsWkt;
   }

   public final void setVerticalSrsWkt(@Nullable String var1) {
      this.verticalSrsWkt = var1;
   }

   @Nullable
   public final String getHorizontalSrsWkt() {
      return this.horizontalSrsWkt;
   }

   public final void setHorizontalSrsWkt(@Nullable String var1) {
      this.horizontalSrsWkt = var1;
   }

   @Nullable
   public final String getUploadId() {
      return this.uploadId;
   }

   public final void setUploadId(@Nullable String var1) {
      this.uploadId = var1;
   }

   @Nullable
   public final String getFlight() {
      return this.flight;
   }

   public final void setFlight(@Nullable String var1) {
      this.flight = var1;
   }

   @Nullable
   public final String getId() {
      return this.id;
   }

   public final void setId(@Nullable String var1) {
      this.id = var1;
   }

   @NotNull
   public final List getMetadata() {
      return this.metadata;
   }

   public final void setMetadata(@NotNull List var1) {
            this.metadata = var1;
   }

   @NotNull
   public final List getStorageLocations() {
      return this.storageLocations;
   }

   public final void setStorageLocations(@NotNull List var1) {
            this.storageLocations = var1;
   }

   @Nullable
   public final Calibration getCalibration() {
      return this.calibration;
   }

   public final void setCalibration(@Nullable Calibration var1) {
      this.calibration = var1;
   }

   @Nullable
   public final String getModified() {
      return this.modified;
   }

   public final void setModified(@Nullable String var1) {
      this.modified = var1;
   }

   @Nullable
   public final String getCreated() {
      return this.created;
   }

   public final void setCreated(@Nullable String var1) {
      this.created = var1;
   }

   @Nullable
   public final String getStatus() {
      return this.status;
   }

   public final void setStatus(@Nullable String var1) {
      this.status = var1;
   }

   @NotNull
   public final List getTypes() {
      return this.types;
   }

   public final void setTypes(@NotNull List var1) {
            this.types = var1;
   }

   @Nullable
   public final Integer getTiltDeg() {
      return this.tiltDeg;
   }

   public final void setTiltDeg(@Nullable Integer var1) {
      this.tiltDeg = var1;
   }

   @Nullable
   public final Integer getPanDeg() {
      return this.panDeg;
   }

   public final void setPanDeg(@Nullable Integer var1) {
      this.panDeg = var1;
   }

   @Nullable
   public final Boolean getSharpened() {
      return this.sharpened;
   }

   public final void setSharpened(@Nullable Boolean var1) {
      this.sharpened = var1;
   }

   @Nullable
   public final Integer getGain() {
      return this.gain;
   }

   public final void setGain(@Nullable Integer var1) {
      this.gain = var1;
   }

   @Nullable
   public final Double getShutter() {
      return this.shutter;
   }

   public final void setShutter(@Nullable Double var1) {
      this.shutter = var1;
   }

   @Nullable
   public final PhotoGeometry getGeometry() {
      return this.geometry;
   }

   public final void setGeometry(@Nullable PhotoGeometry var1) {
      this.geometry = var1;
   }

   @NotNull
   public final List getTags() {
      return this.tags;
   }

   public final void setTags(@NotNull List var1) {
            this.tags = var1;
   }

   @Nullable
   public final GroundFootprint getGroundFootprint() {
      return this.groundFootprint;
   }

   public final void setGroundFootprint(@Nullable GroundFootprint var1) {
      this.groundFootprint = var1;
   }

   @Nullable
   public final Double getPhi() {
      return this.phi;
   }

   public final void setPhi(@Nullable Double var1) {
      this.phi = var1;
   }

   @Nullable
   public final Double getPsi() {
      return this.psi;
   }

   public final void setPsi(@Nullable Double var1) {
      this.psi = var1;
   }

   @Nullable
   public final Double getTheta() {
      return this.theta;
   }

   public final void setTheta(@Nullable Double var1) {
      this.theta = var1;
   }

   @NotNull
   public final Photo withV(@Nullable Integer v) {
      this.v = v;
      return this;
   }

   @NotNull
   public final Photo withSeq(@NotNull String seq) {
            this.seq = seq;
      return this;
   }

   @NotNull
   public final Photo withAltitude(@Nullable Double altitude) {
      this.altitude = altitude;
      return this;
   }

   @NotNull
   public final Photo withUTC(@NotNull String uTC) {
            this.utc = uTC;
      return this;
   }

   @NotNull
   public final Photo withRTC(@Nullable Integer rTC) {
      this.rtc = rTC;
      return this;
   }

   @NotNull
   public final Photo withCamera(@NotNull String camera) {
            this.camera = camera;
      return this;
   }

   @NotNull
   public final Photo withWidth(@Nullable Integer width) {
      this.width = width;
      return this;
   }

   @NotNull
   public final Photo withHeight(@Nullable Integer height) {
      this.height = height;
      return this;
   }

   @NotNull
   public final Photo withVerticalSrsWkt(@NotNull String verticalSrsWkt) {
            this.verticalSrsWkt = verticalSrsWkt;
      return this;
   }

   @NotNull
   public final Photo withHorizontalSrsWkt(@NotNull String horizontalSrsWkt) {
            this.horizontalSrsWkt = horizontalSrsWkt;
      return this;
   }

   @NotNull
   public final Photo withUploadId(@NotNull String uploadId) {
            this.uploadId = uploadId;
      return this;
   }

   @NotNull
   public final Photo withFlight(@NotNull String flight) {
            this.flight = flight;
      return this;
   }

   @NotNull
   public final Photo withId(@NotNull String id) {
            this.id = id;
      return this;
   }

   @NotNull
   public final Photo withMetadata(@NotNull List metadata) {
            this.metadata = metadata;
      return this;
   }

   @NotNull
   public final Photo withStorageLocations(@NotNull List storageLocations) {
            this.storageLocations = storageLocations;
      return this;
   }

   @NotNull
   public final Photo withCalibration(@NotNull Calibration calibration) {
            this.calibration = calibration;
      return this;
   }

   @NotNull
   public final Photo withModified(@NotNull String modified) {
            this.modified = modified;
      return this;
   }

   @NotNull
   public final Photo withCreated(@NotNull String created) {
            this.created = created;
      return this;
   }

   @NotNull
   public final Photo withStatus(@NotNull String status) {
            this.status = status;
      return this;
   }

   @NotNull
   public final Photo withTypes(@NotNull List types) {
            this.types = types;
      return this;
   }

   @NotNull
   public final Photo withTiltDeg(@Nullable Integer tiltDeg) {
      this.tiltDeg = tiltDeg;
      return this;
   }

   @NotNull
   public final Photo withPanDeg(@Nullable Integer panDeg) {
      this.panDeg = panDeg;
      return this;
   }

   @NotNull
   public final Photo withSharpened(@Nullable Boolean sharpened) {
      this.sharpened = sharpened;
      return this;
   }

   @NotNull
   public final Photo withGain(@Nullable Integer gain) {
      this.gain = gain;
      return this;
   }

   @NotNull
   public final Photo withShutter(@Nullable Double shutter) {
      this.shutter = shutter;
      return this;
   }

   @NotNull
   public final Photo withGeometry(@NotNull PhotoGeometry geometry) {
            this.geometry = geometry;
      return this;
   }

   @NotNull
   public final Photo withTags(@NotNull List tags) {
            this.tags = tags;
      return this;
   }
}
