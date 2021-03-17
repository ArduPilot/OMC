/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.insight.api

import com.google.gson.GsonBuilder
import com.intel.insight.InsightContext
import com.intel.insight.datastructures.Annotations
import com.intel.insight.datastructures.FlightRequests
import org.junit.Ignore
import org.junit.Test
import unirest.Unirest

class SearchServiceTest {

    @Test
    @Ignore
    fun getAnnotationTest() {
        var photoResponse = getAnnotations()

        println(photoResponse)
    }

    private fun getAnnotations(id: String = "5c8a263cbcf3e41d197714cb"): Annotations? {
        var c = InsightContext()
        c.authWithInsight("imc_cloud@intel.com", "hdjetta18!")
        c.oauthResponse.accessToken

        var headers = c.prepareHeaders(c.oauthResponse.accessToken.orEmpty())
        var gson = GsonBuilder().create()

        var targetHost = "https://newdev.ixstack.net/"
        var reqs = Unirest.post("$targetHost/uisrv/annotations/search")
                .headers(headers)
                .body("{\"project_id\":\"$id\"}\n")
                .asString()


        var photoResponse = gson.fromJson(reqs.body.toString(), Annotations::class.java)
        return photoResponse
    }


    @Test
    @Ignore
    fun getAllOrderedMissionsTest() {
        var useIntelProxy = true
        if (useIntelProxy) {
            println("[InsightCLI]:\t Use Intel proxies")
            System.setProperty("http.proxyHost", "http://proxy-mu.intel.com")
            System.setProperty("http.proxyPort", "911")
            System.setProperty("https.proxyHost", "http://proxy-mu.intel.com")
            System.setProperty("https.proxyPort", "912")
            System.setProperty("socksProxyHost", "proxy-us.intel.com");
            System.setProperty("socksProxyPort", "1080");
        }
        var reqs = this.getRequests()
        var annotationIds = ArrayList<String>()
        var projectIds = ArrayList<String>()
        for (req in reqs.flightRequests) {
            annotationIds.add(req.annotationId!!)
            projectIds.add(req.projectId!!)
            println("project id: ${req.projectId}, annotation id: ${req.annotationId}, notes: ${req.notes}")
        }
        var annotationsList = ArrayList<Annotations>()
        for (id in projectIds) {
            annotationsList.add(getAnnotations(id)!!)
        }

        for (annotations in annotationsList) {
            for (annotation in annotations.annotations) {
                if (annotation.id in annotationIds) {
                    println(annotation.id)
                } else {
                    println("whoa" + annotation.id)
                }
            }
        }
    }

    fun getRequests(): FlightRequests {
        var c = InsightContext()
        c.authWithInsight("imc_cloud@intel.com", "hdjetta18!")
        c.oauthResponse.accessToken

        var headers = c.prepareHeaders(c.oauthResponse.accessToken.orEmpty())

        var targetHost = "https://newdev.ixstack.net/"
        var reqs = Unirest.get("$targetHost/uisrv/flight-request/")
                .headers(headers)
                .asString()

        var gson = GsonBuilder().create()


        var photoResponse = gson.fromJson(reqs.body.toString(), FlightRequests::class.java)

        println(photoResponse)
        return photoResponse
    }


}

