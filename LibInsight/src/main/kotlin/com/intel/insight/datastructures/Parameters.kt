/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.insight.datastructures

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Parameters {

    @SerializedName("flight_id")
    @Expose
    var flightId: String? = null
    @SerializedName("processSettings")
    @Expose
    var processSettings: ProcessSettings? = null

    fun withFlightId(flightId: String): Parameters {
        this.flightId = flightId
        return this
    }

    fun withProcessSettings(processSettings: ProcessSettings): Parameters {
        this.processSettings = processSettings
        return this
    }

}
