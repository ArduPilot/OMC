/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.insight.datastructures

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Search {

    @SerializedName("search")
    @Expose
    var search: String = ""

    fun withSearch(search: String): Search {
        this.search = search
        return this
    }

}