/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.insight.datastructures

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class ModificationUser {

    @SerializedName("_id")
    @Expose
    var id: String? = null
    @SerializedName("displayName")
    @Expose
    var displayName: String? = null

}
