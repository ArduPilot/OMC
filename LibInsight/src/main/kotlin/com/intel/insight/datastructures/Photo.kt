/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.insight.datastructures

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.util.*

class Photo {

    var fileName: String? = null


    @SerializedName("__v")
    @Expose
    var v: Int? = null

    @SerializedName("seq")
    @Expose
    var seq: String? = null

    @SerializedName("altitude")
    @Expose
    var altitude: Double? = null

    @SerializedName("UTC")
    @Expose
    var utc: String? = null

    @SerializedName("RTC")
    @Expose
    var rtc: Int? = null

    @SerializedName("camera")
    @Expose
    var camera: String? = null

    @SerializedName("width")
    @Expose
    var width: Int? = null

    @SerializedName("height")
    @Expose
    var height: Int? = null

    @SerializedName("vertical_srs_wkt")
    @Expose
    var verticalSrsWkt: String? = null

    @SerializedName("horizontal_srs_wkt")
    @Expose
    var horizontalSrsWkt: String? = null

    @SerializedName("upload_id")
    @Expose
    var uploadId: String? = null

    @SerializedName("flight")
    @Expose
    var flight: String? = null

    @SerializedName("_id")
    @Expose
    var id: String? = null

    @SerializedName("metadata")
    @Expose
    var metadata: List<Any> = ArrayList()

    @SerializedName("storage_locations")
    @Expose
    var storageLocations: List<Any> = ArrayList()

    @SerializedName("calibration")
    @Expose
    var calibration: Calibration? = null

    @SerializedName("modified")
    @Expose
    var modified: String? = null

    @SerializedName("created")
    @Expose
    var created: String? = null

    @SerializedName("status")
    @Expose
    var status: String? = null

    @SerializedName("types")
    @Expose
    var types: List<Any> = ArrayList()

    @SerializedName("tilt_deg")
    @Expose
    var tiltDeg: Int? = null

    @SerializedName("pan_deg")
    @Expose
    var panDeg: Int? = null

    @SerializedName("sharpened")
    @Expose
    var sharpened: Boolean? = null

    @SerializedName("gain")
    @Expose
    var gain: Int? = null

    @SerializedName("shutter")
    @Expose
    var shutter: Double? = null

    @SerializedName("geometry")
    @Expose
    var geometry: PhotoGeometry? = null

    @SerializedName("tags")
    @Expose
    var tags: List<Any> = ArrayList()

    @SerializedName("ground_footprint")
    @Expose
    var groundFootprint: GroundFootprint? = null

    @SerializedName("phi")
    @Expose
    var phi: Double? = null

    @SerializedName("psi")
    @Expose
    var psi: Double? = null

    @SerializedName("theta")
    @Expose
    var theta: Double? = null


    fun withV(v: Int?): Photo {
        this.v = v
        return this
    }

    fun withSeq(seq: String): Photo {
        this.seq = seq
        return this
    }

    fun withAltitude(altitude: Double?): Photo {
        this.altitude = altitude
        return this
    }

    fun withUTC(uTC: String): Photo {
        this.utc = uTC
        return this
    }

    fun withRTC(rTC: Int?): Photo {
        this.rtc = rTC
        return this
    }

    fun withCamera(camera: String): Photo {
        this.camera = camera
        return this
    }

    fun withWidth(width: Int?): Photo {
        this.width = width
        return this
    }

    fun withHeight(height: Int?): Photo {
        this.height = height
        return this
    }

    fun withVerticalSrsWkt(verticalSrsWkt: String): Photo {
        this.verticalSrsWkt = verticalSrsWkt
        return this
    }

    fun withHorizontalSrsWkt(horizontalSrsWkt: String): Photo {
        this.horizontalSrsWkt = horizontalSrsWkt
        return this
    }

    fun withUploadId(uploadId: String): Photo {
        this.uploadId = uploadId
        return this
    }

    fun withFlight(flight: String): Photo {
        this.flight = flight
        return this
    }

    fun withId(id: String): Photo {
        this.id = id
        return this
    }

    fun withMetadata(metadata: List<Any>): Photo {
        this.metadata = metadata
        return this
    }

    fun withStorageLocations(storageLocations: List<Any>): Photo {
        this.storageLocations = storageLocations
        return this
    }

    fun withCalibration(calibration: Calibration): Photo {
        this.calibration = calibration
        return this
    }

    fun withModified(modified: String): Photo {
        this.modified = modified
        return this
    }

    fun withCreated(created: String): Photo {
        this.created = created
        return this
    }

    fun withStatus(status: String): Photo {
        this.status = status
        return this
    }

    fun withTypes(types: List<Any>): Photo {
        this.types = types
        return this
    }

    fun withTiltDeg(tiltDeg: Int?): Photo {
        this.tiltDeg = tiltDeg
        return this
    }

    fun withPanDeg(panDeg: Int?): Photo {
        this.panDeg = panDeg
        return this
    }

    fun withSharpened(sharpened: Boolean?): Photo {
        this.sharpened = sharpened
        return this
    }

    fun withGain(gain: Int?): Photo {
        this.gain = gain
        return this
    }

    fun withShutter(shutter: Double?): Photo {
        this.shutter = shutter
        return this
    }

    fun withGeometry(geometry: PhotoGeometry): Photo {
        this.geometry = geometry
        return this
    }

    fun withTags(tags: List<Any>): Photo {
        this.tags = tags
        return this
    }

}
