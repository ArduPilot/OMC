/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.insight.datastructures

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


class OAuthRequest {

    @SerializedName("client_id")
    @Expose
    var clientId = "browserid"

    @SerializedName("grant_type")
    @Expose
    var grantType = "password"

    @SerializedName("username")
    @Expose
    var username = "imc_cloud@intel.com"

    @SerializedName("password")
    @Expose
    var password = "hdjetta18!"

    @SerializedName("client_secret")
    @Expose
    var clientSecret = "29wmbX3W92"

}