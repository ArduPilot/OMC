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


public final class Annotations {
   @SerializedName("annotations")
   @Expose
   @NotNull
   private List<Annotation> annotations = new ArrayList();

   @NotNull
   public final List<Annotation> getAnnotations() {
      return this.annotations;
   }

   public final void setAnnotations(@NotNull List<Annotation> var1) {
            this.annotations = var1;
   }

   @NotNull
   public final Annotations withAnnotations(@NotNull List annotations) {
            this.annotations = annotations;
      return this;
   }
}
