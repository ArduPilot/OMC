/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.insight.datastructures

import java.util.ArrayList
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class FlightRequests {

    @SerializedName("flightRequests")
    @Expose
    var flightRequests: List<FlightRequest> = ArrayList()

    fun withFlightRequests(flightRequests: List<FlightRequest>): FlightRequests {
        this.flightRequests = flightRequests
        return this
    }

}
