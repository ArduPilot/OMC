/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.insight.datastructures

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Precision {

    @SerializedName("gsd")
    @Expose
    var gsd: Gsd? = null
    @SerializedName("xy")
    @Expose
    var xy: Xy? = null
    @SerializedName("z")
    @Expose
    var z: Z? = null

}
