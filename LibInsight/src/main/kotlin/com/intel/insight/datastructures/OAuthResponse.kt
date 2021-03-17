/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.insight.datastructures


import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


class OAuthResponse {

    @SerializedName("access_token")
    @Expose
    var accessToken: String? = null

    @SerializedName("expires_in")
    @Expose
    var expiresIn: Int? = null

    @SerializedName("refresh_token")
    @Expose
    var refreshToken: String? = null

    @SerializedName("token_type")
    @Expose
    var tokenType: String? = null

}