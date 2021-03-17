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


public final class PhotoPrep {
   @SerializedName("photos")
   @Expose
   @NotNull
   private List photos = new ArrayList();

   @NotNull
   public final List getPhotos() {
      return this.photos;
   }

   public final void setPhotos(@NotNull List var1) {
            this.photos = var1;
   }

   @NotNull
   public final PhotoPrep withPhotos(@NotNull List photos) {
            this.photos = photos;
      return this;
   }
}
