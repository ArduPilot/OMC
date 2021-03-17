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


public final class Properties {
   @SerializedName("name")
   @Expose
   @Nullable
   private String name;
   @SerializedName("comment")
   @Expose
   @Nullable
   private String comment;
   @SerializedName("created")
   @Expose
   @Nullable
   private String created;
   @SerializedName("image")
   @Expose
   @Nullable
   private String image;
   @SerializedName("color")
   @Expose
   @NotNull
   private List color = new ArrayList();

   @Nullable
   public final String getName() {
      return this.name;
   }

   public final void setName(@Nullable String var1) {
      this.name = var1;
   }

   @Nullable
   public final String getComment() {
      return this.comment;
   }

   public final void setComment(@Nullable String var1) {
      this.comment = var1;
   }

   @Nullable
   public final String getCreated() {
      return this.created;
   }

   public final void setCreated(@Nullable String var1) {
      this.created = var1;
   }

   @Nullable
   public final String getImage() {
      return this.image;
   }

   public final void setImage(@Nullable String var1) {
      this.image = var1;
   }

   @NotNull
   public final List getColor() {
      return this.color;
   }

   public final void setColor(@NotNull List var1) {
            this.color = var1;
   }

   @NotNull
   public final Properties withName(@NotNull String name) {
            this.name = name;
      return this;
   }

   @NotNull
   public final Properties withComment(@NotNull String comment) {
            this.comment = comment;
      return this;
   }

   @NotNull
   public final Properties withCreated(@NotNull String created) {
            this.created = created;
      return this;
   }

   @NotNull
   public final Properties withImage(@NotNull String image) {
            this.image = image;
      return this;
   }
}
