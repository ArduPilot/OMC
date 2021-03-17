/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.insight.datastructures

import java.util.ArrayList
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class PhotoUpload {

    @SerializedName("project")
    @Expose
    var project: String? = null
    @SerializedName("mission")
    @Expose
    var mission: String? = null
    @SerializedName("flight")
    @Expose
    var flight: String? = null
    @SerializedName("photos")
    @Expose
    var photos: List<Photo> = ArrayList()

    fun withProject(project: String): PhotoUpload {
        this.project = project
        return this
    }

    fun withMission(mission: String): PhotoUpload {
        this.mission = mission
        return this
    }

    fun withFlight(flight: String): PhotoUpload {
        this.flight = flight
        return this
    }

    fun withPhotos(photos: List<Photo>): PhotoUpload {
        this.photos = photos
        return this
    }

}
