/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.persistence.insight.primitives;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public final class Crs {
   @SerializedName("output")
   @Expose
   @Nullable
   private Output output;
   @SerializedName("gcp")
   @Expose
   @Nullable
   private Gcp gcp;
   @SerializedName("image")
   @Expose
   @Nullable
   private Image image;

   @Nullable
   public final Output getOutput() {
      return this.output;
   }

   public final void setOutput(@Nullable Output var1) {
      this.output = var1;
   }

   @Nullable
   public final Gcp getGcp() {
      return this.gcp;
   }

   public final void setGcp(@Nullable Gcp var1) {
      this.gcp = var1;
   }

   @Nullable
   public final Image getImage() {
      return this.image;
   }

   public final void setImage(@Nullable Image var1) {
      this.image = var1;
   }

   @NotNull
   public final Crs withOutput(@NotNull Output output) {
            this.output = output;
      return this;
   }

   @NotNull
   public final Crs withGcp(@NotNull Gcp gcp) {
            this.gcp = gcp;
      return this;
   }

   @NotNull
   public final Crs withImage(@NotNull Image image) {
            this.image = image;
      return this;
   }
}
