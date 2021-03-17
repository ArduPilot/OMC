/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.insight.datastructures

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Properties {

    @SerializedName("name")
    @Expose
    var name: String? = null
    @SerializedName("comment")
    @Expose
    var comment: String? = null
    @SerializedName("created")
    @Expose
    var created: String? = null
    @SerializedName("image")
    @Expose
    var image: String? = null
    @SerializedName("color")
    @Expose
    var color: List<Int> = ArrayList<Int>()

    fun withName(name: String): Properties {
        this.name = name
        return this
    }

    fun withComment(comment: String): Properties {
        this.comment = comment
        return this
    }

    fun withCreated(created: String): Properties {
        this.created = created
        return this
    }

    fun withImage(image: String): Properties {
        this.image = image
        return this
    }

}
