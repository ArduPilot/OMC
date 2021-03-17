/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.insight.api

import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import com.intel.insight.InsightContext
import com.intel.insight.datastructures.*
import unirest.Unirest

class SearchService {
    val c: InsightContext = InsightContext();

    constructor(username: String, password: String) {
        c.authWithInsight(username, password)
        c.oauthResponse.accessToken
        this.targetHost = "https://newdev.ixstack.net"
    }

    constructor(username: String, password: String, targetHost: String = "https://newdev.ixstack.net") {
        c.authWithInsight(username, password)
        c.oauthResponse.accessToken
        this.targetHost = targetHost
    }

    fun getAll() {

    }

    fun getAllOrderedMissions(): ArrayList<Pair<Annotations, FlightRequest>> {
        var reqs = this.getRequests()
        var annotationIds = ArrayList<String>()
        var projectIds = ArrayList<String>()
        for (req in reqs.flightRequests) {
            annotationIds.add(req.annotationId!!)
            projectIds.add(req.projectId!!)
        }
        var annotationsList = ArrayList<Pair<Annotations, FlightRequest>>()
        var i = 0;
        for (id in projectIds) {
            try {
                annotationsList.add(Pair(getAnnotations(id)!!, reqs.flightRequests[i]))
            } catch (e: JsonSyntaxException) {
                println("[Insight Service] unsupported annotation type, please add me")
            }
            i = i + 1
        }

        var orderedAnnotationsList = ArrayList<Pair<Annotations, FlightRequest>>()

//        for (annotations in annotationsList) {
//            for (annotation in annotations.first.annotations) {
//                if (annotation.id in annotationIds) {
//                } else {
//                    println("whoa" + annotation.id)
//                }
//            }
//        }
        return annotationsList
    }


    private val targetHost: String

    fun getProjects(): Projects {
        var headers = c.prepareHeaders(c.oauthResponse.accessToken.orEmpty())
        var gson = GsonBuilder().create()
        val searchRequest = Search()
        var searchResponse = Unirest.post("$targetHost/uisrv/projects/search")
                .headers(headers)
                .body(gson.toJson(searchRequest))
                .asString()


        var projects = gson.fromJson(searchResponse.body.toString(), Projects::class.java)
        return projects
    }

    fun getRequests(): FlightRequests {
        var headers = c.prepareHeaders(c.oauthResponse.accessToken.orEmpty())

        var reqs = Unirest.get("$targetHost/uisrv/flight-request/")
                .headers(headers)
                .asString()

        var gson = GsonBuilder().create()


        var photoResponse = gson.fromJson(reqs.body.toString(), FlightRequests::class.java)

        println(photoResponse)
        return photoResponse
    }

    private fun getAnnotations(id: String = "5c8a263cbcf3e41d197714cb"): Annotations? {
        var headers = c.prepareHeaders(c.oauthResponse.accessToken.orEmpty())
        var gson = GsonBuilder().create()


        var reqs = Unirest.post("$targetHost/uisrv/annotations/search")
                .headers(headers)
                .body("{\"project_id\":\"$id\"}\n")
                .asString()

        var photoResponse: Annotations
        try {
            photoResponse = gson.fromJson(reqs.body.toString(), Annotations::class.java)
        } catch (e: JsonSyntaxException) {
            throw(e)
        }
        return photoResponse
    }

}