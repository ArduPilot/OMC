/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.persistence.insight;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.Nullable;

public final class OAuthResponse {
    @SerializedName("access_token")
    @Expose
    private String accessToken;

    @SerializedName("expires_in")
    @Expose
    private Integer expiresIn;

    @SerializedName("refresh_token")
    @Expose
    private String refreshToken;

    @SerializedName("token_type")
    @Expose
    private String tokenType;

    @Nullable
    public final String getAccessToken() {
        return this.accessToken;
    }

    public final void setAccessToken(@Nullable String var1) {
        this.accessToken = var1;
    }

    @Nullable
    public final Integer getExpiresIn() {
        return this.expiresIn;
    }

    public final void setExpiresIn(@Nullable Integer var1) {
        this.expiresIn = var1;
    }

    @Nullable
    public final String getRefreshToken() {
        return this.refreshToken;
    }

    public final void setRefreshToken(@Nullable String var1) {
        this.refreshToken = var1;
    }

    @Nullable
    public final String getTokenType() {
        return this.tokenType;
    }

    public final void setTokenType(@Nullable String var1) {
        this.tokenType = var1;
    }
}
