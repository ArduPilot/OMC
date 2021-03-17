/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.insight.datastructures

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Gsd {

    @SerializedName("value")
    @Expose
    var value: Int? = null
    @SerializedName("selected")
    @Expose
    var selected: Boolean? = null

}
