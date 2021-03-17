/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.persistence.insight.primitives;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.Nullable;


public final class AnnotationResponse {
   @SerializedName("annotation")
   @Expose
   @Nullable
   private final Annotation annotation;

   public AnnotationResponse(@Nullable Annotation annotation) {
      this.annotation = annotation;
   }

   @Nullable
   public final Annotation getAnnotation() {
      return this.annotation;
   }
}
