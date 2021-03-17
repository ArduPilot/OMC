/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.insight;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.NotNull;


public final class OAuthRequest {
   @SerializedName("client_id")
   @Expose
   @NotNull
   private String clientId = "browserid";
   @SerializedName("grant_type")
   @Expose
   @NotNull
   private String grantType = "password";
   @SerializedName("username")
   @Expose
   @NotNull
   private String username = "jan-hendrik.troesemeier@intel.com";
   @SerializedName("password")
   @Expose
   @NotNull
   private String password = "hdjetta18!";
   @SerializedName("client_secret")
   @Expose
   @NotNull
   private String clientSecret = "29wmbX3W92";

   @NotNull
   public final String getClientId() {
      return this.clientId;
   }

   public final void setClientId(@NotNull String var1) {
            this.clientId = var1;
   }

   @NotNull
   public final String getGrantType() {
      return this.grantType;
   }

   public final void setGrantType(@NotNull String var1) {
            this.grantType = var1;
   }

   @NotNull
   public final String getUsername() {
      return this.username;
   }

   public final void setUsername(@NotNull String var1) {
            this.username = var1;
   }

   @NotNull
   public final String getPassword() {
      return this.password;
   }

   public final void setPassword(@NotNull String var1) {
            this.password = var1;
   }

   @NotNull
   public final String getClientSecret() {
      return this.clientSecret;
   }

   public final void setClientSecret(@NotNull String var1) {
            this.clientSecret = var1;
   }
}
