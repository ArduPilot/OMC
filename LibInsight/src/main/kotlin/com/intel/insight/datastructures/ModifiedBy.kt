/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.insight.datastructures

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class ModifiedBy {

    @SerializedName("id")
    @Expose
    var id: String? = null
    @SerializedName("displayName")
    @Expose
    var displayName: String? = null

    fun withId(id: String): ModifiedBy {
        this.id = id
        return this
    }

    fun withDisplayName(displayName: String): ModifiedBy {
        this.displayName = displayName
        return this
    }

}
