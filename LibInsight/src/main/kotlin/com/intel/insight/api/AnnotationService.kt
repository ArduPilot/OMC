/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.insight.api

import com.google.gson.GsonBuilder
import com.intel.insight.InsightContext
import com.intel.insight.datastructures.Annotation
import com.intel.insight.datastructures.Photogrammetry
import unirest.Unirest

var c: InsightContext = InsightContext();

class AnnotationService {

    constructor(username: String, password: String, targetHost: String = "https://dev.ixstack.net") {
        c.targetHost = targetHost
        c.authWithInsight(username, password)
        c.oauthResponse.accessToken
        this.targetHost = targetHost
    }

    constructor(context: InsightContext, targetHost: String = "https://dev.ixstack.net") {
        c = context
        this.targetHost = targetHost
    }

    private val targetHost: String

    fun uploadAnnotation(a: Annotation) {

        var headers = c.prepareHeaders(c.oauthResponse.accessToken.orEmpty())
        var gson = GsonBuilder().create()


        var target = "$targetHost/uisrv/annotations"
        var reqs = Unirest.post(target)
                .headers(headers)
                .body(gson.toJson(a))
                .asString()

        if (reqs.status == 200) {
            println("[InsightCLI]:\tUploading Annotation succesfull")
        } else {
            System.err.println("[InsightCLI]:\t Uploading Annotation failed, Reason: ${reqs.status}, ${reqs.statusText}, ${reqs.body}")
        }


    }

    fun requestPhotogrammetry(p: Photogrammetry) {
        var headers = c.prepareHeaders(c.oauthResponse.accessToken.orEmpty())
        var gson = GsonBuilder().create()


        var target = "$targetHost/dxpm/missions/${p.id}/analytics/new"
        var reqs = Unirest.post(target)
                .headers(headers)
                .body(gson.toJson(p))
                .asString()

        if (reqs.status == 200) {
            println("[InsightCLI]:\tStarting Photogrammetry succesfull")
        } else {
            System.err.println("[InsightCLI]:\t Starting Photogrammetry failed, Reason: ${reqs.status}, ${reqs.statusText}, ${reqs.body}")
        }

        //var photoResponse = gson.fromJson(reqs.body.toString(), Annotations::class.java)
        //return photoResponse
    }
}
