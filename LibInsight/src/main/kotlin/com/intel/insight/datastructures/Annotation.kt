/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.insight.datastructures

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Annotation {

    @SerializedName("_id")
    @Expose
    var id: String? = null
    @SerializedName("target")
    @Expose
    var target: Target? = null
    @SerializedName("feature")
    @Expose
    var feature: Feature? = null
    @SerializedName("project_id")
    @Expose
    var projectId: String? = null
    @SerializedName("created_by")
    @Expose
    var createdBy: CreatedBy? = null
    @SerializedName("created_date")
    @Expose
    var createdDate: String? = null
    @SerializedName("modified_date")
    @Expose
    var modifiedDate: String? = null
    @SerializedName("__v")
    @Expose
    var v: Int? = null
    @SerializedName("modified_by")
    @Expose
    var modifiedBy: ModifiedBy? = null

    fun withId(id: String): Annotation {
        this.id = id
        return this
    }

    fun withTarget(target: Target): Annotation {
        this.target = target
        return this
    }

    fun withFeature(feature: Feature): Annotation {
        this.feature = feature
        return this
    }

    fun withProjectId(projectId: String): Annotation {
        this.projectId = projectId
        return this
    }

    fun withCreatedBy(createdBy: CreatedBy): Annotation {
        this.createdBy = createdBy
        return this
    }

    fun withCreatedDate(createdDate: String): Annotation {
        this.createdDate = createdDate
        return this
    }

    fun withModifiedDate(modifiedDate: String): Annotation {
        this.modifiedDate = modifiedDate
        return this
    }

    fun withV(v: Int?): Annotation {
        this.v = v
        return this
    }

    fun withModifiedBy(modifiedBy: ModifiedBy): Annotation {
        this.modifiedBy = modifiedBy
        return this
    }

    override fun toString(): String {
        return "annotation: id:" + id
    }
}
