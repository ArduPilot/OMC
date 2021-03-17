/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.insight.datastructures

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Units {

    @SerializedName("distances")
    @Expose
    var distances: String? = null
    @SerializedName("surfaces")
    @Expose
    var surfaces: String? = null
    @SerializedName("volumes")
    @Expose
    var volumes: String? = null
    @SerializedName("altitude")
    @Expose
    var altitude: String? = null
    @SerializedName("gsd")
    @Expose
    var gsd: String? = null

}
