package com.intel.insight.datastructures

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class FlightRequest {

    @SerializedName("_id")
    @Expose
    var id: String? = null
    @SerializedName("annotation_id")
    @Expose
    var annotationId: String? = null
    @SerializedName("capture_data")
    @Expose
    var captureData: String? = null
    @SerializedName("notes")
    @Expose
    var notes: String? = null
    @SerializedName("ratio")
    @Expose
    var ratio: String? = null //this is the GSD in meter
    @SerializedName("type")
    @Expose
    var type: String? = null  // can be INSPECTION  or  SURVEY
    @SerializedName("resolution")
    @Expose
    var resolution: String? = null // enum with quality type
    @SerializedName("project_id")
    @Expose
    var projectId: String? = null
    @SerializedName("company")
    @Expose
    var company: String? = null
    @SerializedName("user_id")
    @Expose
    var userId: String? = null
    @SerializedName("created")
    @Expose
    var created: String? = null

    fun withId(id: String): FlightRequest {
        this.id = id
        return this
    }

    fun withAnnotationId(annotationId: String): FlightRequest {
        this.annotationId = annotationId
        return this
    }

    fun withCaptureData(captureData: String): FlightRequest {
        this.captureData = captureData
        return this
    }

    fun withNotes(notes: String): FlightRequest {
        this.notes = notes
        return this
    }

    fun withRatio(ratio: String): FlightRequest {
        this.ratio = ratio
        return this
    }

    fun withType(type: String): FlightRequest {
        this.type = type
        return this
    }

    fun withResolution(resolution: String): FlightRequest {
        this.resolution = resolution
        return this
    }

    fun withProjectId(projectId: String): FlightRequest {
        this.projectId = projectId
        return this
    }

    fun withCompany(company: String): FlightRequest {
        this.company = company
        return this
    }

    fun withUserId(userId: String): FlightRequest {
        this.userId = userId
        return this
    }

    fun withCreated(created: String): FlightRequest {
        this.created = created
        return this
    }

    override fun toString(): String {
        return "flightReqiest: id:" + id
    }
}
