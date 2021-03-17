/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.insight.datastructures

import java.util.ArrayList
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class PhotoPrep {

    @SerializedName("photos")
    @Expose
    var photos: List<Photo> = ArrayList()

    fun withPhotos(photos: List<Photo>): PhotoPrep {
        this.photos = photos
        return this
    }

}
