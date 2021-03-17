/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.insight.api

import com.google.gson.GsonBuilder
import com.intel.insight.InsightContext
import com.intel.insight.datastructures.*
import unirest.Unirest

var template = "{\n" +
        "  \"analytics\": \"photogrammetry\",\n" +
        "  \"_id\": \"5c911d3df0fc3e529a3a2d88\",\n" +
        "  \"parameters\": {\n" +
        "    \"flight_id\": \"5c911d3df0fc3e529a3a2d89\",\n" +
        "    \"processSettings\": {\n" +
        "      \"mapType\": \"map\",\n" +
        "      \"tools_set\": \"pix4d\",\n" +
        "      \"mapping\": {\n" +
        "        \"mesh\": true,\n" +
        "        \"facade\": false,\n" +
        "        \"preset\": \"SPEED\",\n" +
        "        \"processingAreaSetting\": \"none\",\n" +
        "        \"crs\": {\n" +
        "          \"output\": {\n" +
        "            \"horizontal_srs_wkt\": \"PROJCS[\\\"WGS 84 / UTM zone 34N\\\",GEOGCS[\\\"WGS 84\\\",DATUM[\\\"WGS_1984\\\",SPHEROID[\\\"WGS 84\\\",6378137,298.257223563,AUTHORITY[\\\"EPSG\\\",\\\"7030\\\"]],AUTHORITY[\\\"EPSG\\\",\\\"6326\\\"]],PRIMEM[\\\"Greenwich\\\",0,AUTHORITY[\\\"EPSG\\\",\\\"8901\\\"]],UNIT[\\\"degree\\\",0.0174532925199433,AUTHORITY[\\\"EPSG\\\",\\\"9122\\\"]],AUTHORITY[\\\"EPSG\\\",\\\"4326\\\"]],PROJECTION[\\\"Transverse_Mercator\\\"],PARAMETER[\\\"latitude_of_origin\\\",0],PARAMETER[\\\"central_meridian\\\",21],PARAMETER[\\\"scale_factor\\\",0.9996],PARAMETER[\\\"false_easting\\\",500000],PARAMETER[\\\"false_northing\\\",0],UNIT[\\\"metre\\\",1,AUTHORITY[\\\"EPSG\\\",\\\"9001\\\"]],AXIS[\\\"Easting\\\",EAST],AXIS[\\\"Northing\\\",NORTH],AUTHORITY[\\\"EPSG\\\",\\\"32634\\\"]]\",\n" +
        "            \"vertical_srs_wkt\": \"VERT_CS[\\\"EGM96 geoid (meters)\\\",VERT_DATUM[\\\"EGM96 geoid\\\",2005,EXTENSION[\\\"PROJ4_GRIDS\\\",\\\"egm96_15.gtx\\\"],AUTHORITY[\\\"EPSG\\\",\\\"5171\\\"]],UNIT[\\\"metre\\\",1,AUTHORITY[\\\"EPSG\\\",\\\"9001\\\"]],AXIS[\\\"Up\\\",UP]]\"\n" +
        "          },\n" +
        "          \"gcp\": {},\n" +
        "          \"image\": {\n" +
        "            \"vertical_srs_wkt\": \"VERT_CS[\\\\\\\"EGM96 geoid (meters)\\\\\\\",VERT_DATUM[\\\\\\\"EGM96 geoid\\\\\\\",2005,EXTENSION[\\\\\\\"PROJ4_GRIDS\\\\\\\",\\\\\\\"egm96_15.gtx\\\\\\\"],AUTHORITY[\\\\\\\"EPSG\\\\\\\",\\\\\\\"5171\\\\\\\"]],UNIT[\\\\\\\"metre\\\\\\\",1,AUTHORITY[\\\\\\\"EPSG\\\\\\\",\\\\\\\"9001\\\\\\\"]],AXIS[\\\\\\\"Up\\\\\\\",UP]]\"\n" +
        "          }\n" +
        "        }\n" +
        "      },\n" +
        "      \"gcpsByOperator\": false\n" +
        "    }\n" +
        "  }\n" +
        "}"


class PhotogrammetryService {
    var c: InsightContext = InsightContext();

    constructor(username: String, password: String) {
        var targetHost = "https://newdev.ixstack.net"
        c.authWithInsight(username, password)
        c.oauthResponse.accessToken
        this.targetHost = targetHost
    }

    constructor(username: String, password: String, targetHost: String = "https://newdev.ixstack.net") {
        c.authWithInsight(username, password)
        c.oauthResponse.accessToken
        this.targetHost = targetHost
    }

    constructor(context: InsightContext, targetHost: String = "https://newdev.ixstack.net") {
        c = context
        this.targetHost = targetHost
    }

    constructor(context: InsightContext) {
        c = context
        this.targetHost = "https://newdev.ixstack.net"
    }

    fun createPhotogrammetryRequest(missionId: String, flightId: String): Photogrammetry {
        return createPhotogrammetryRequest(missionId, flightId, createPix4DProcessSettings())
    }

    fun createPhotogrammetryRequest(missionId: String, flightId: String, processSettings: ProcessSettings = createPix4DProcessSettings()): Photogrammetry {

//        var template = "{\n" +
//                "  \"analytics\": \"photogrammetry\",\n" +
//                "  \"_id\": \"5c911d3df0fc3e529a3a2d88\",\n" +
//                "  \"parameters\": {\n" +
//                "    \"flight_id\": \"5c911d3df0fc3e529a3a2d89\",\n" +
//                "    \"processSettings\": {\n" +
//                "      \"mapType\": \"map\",\n" +
//                "      \"tools_set\": \"pix4d\",\n" +
//                "      \"mapping\": {\n" +
//                "        \"mesh\": true,\n" +
//                "        \"facade\": false,\n" +
//                "        \"preset\": \"SPEED\",\n" +
//                "        \"processingAreaSetting\": \"none\",\n" +
//                "        \"crs\": {\n" +
//                "          \"output\": {\n" +
//                "            \"horizontal_srs_wkt\": \"PROJCS[\\\"WGS 84 / UTM zone 34N\\\",GEOGCS[\\\"WGS 84\\\",DATUM[\\\"WGS_1984\\\",SPHEROID[\\\"WGS 84\\\",6378137,298.257223563,AUTHORITY[\\\"EPSG\\\",\\\"7030\\\"]],AUTHORITY[\\\"EPSG\\\",\\\"6326\\\"]],PRIMEM[\\\"Greenwich\\\",0,AUTHORITY[\\\"EPSG\\\",\\\"8901\\\"]],UNIT[\\\"degree\\\",0.0174532925199433,AUTHORITY[\\\"EPSG\\\",\\\"9122\\\"]],AUTHORITY[\\\"EPSG\\\",\\\"4326\\\"]],PROJECTION[\\\"Transverse_Mercator\\\"],PARAMETER[\\\"latitude_of_origin\\\",0],PARAMETER[\\\"central_meridian\\\",21],PARAMETER[\\\"scale_factor\\\",0.9996],PARAMETER[\\\"false_easting\\\",500000],PARAMETER[\\\"false_northing\\\",0],UNIT[\\\"metre\\\",1,AUTHORITY[\\\"EPSG\\\",\\\"9001\\\"]],AXIS[\\\"Easting\\\",EAST],AXIS[\\\"Northing\\\",NORTH],AUTHORITY[\\\"EPSG\\\",\\\"32634\\\"]]\",\n" +
//                "            \"vertical_srs_wkt\": \"VERT_CS[\\\"EGM96 geoid (meters)\\\",VERT_DATUM[\\\"EGM96 geoid\\\",2005,EXTENSION[\\\"PROJ4_GRIDS\\\",\\\"egm96_15.gtx\\\"],AUTHORITY[\\\"EPSG\\\",\\\"5171\\\"]],UNIT[\\\"metre\\\",1,AUTHORITY[\\\"EPSG\\\",\\\"9001\\\"]],AXIS[\\\"Up\\\",UP]]\"\n" +
//                "          },\n" +
//                "          \"gcp\": {},\n" +
//                "          \"image\": {\n" +
//                "            \"vertical_srs_wkt\": \"VERT_CS[\\\\\\\"EGM96 geoid (meters)\\\\\\\",VERT_DATUM[\\\\\\\"EGM96 geoid\\\\\\\",2005,EXTENSION[\\\\\\\"PROJ4_GRIDS\\\\\\\",\\\\\\\"egm96_15.gtx\\\\\\\"],AUTHORITY[\\\\\\\"EPSG\\\\\\\",\\\\\\\"5171\\\\\\\"]],UNIT[\\\\\\\"metre\\\\\\\",1,AUTHORITY[\\\\\\\"EPSG\\\\\\\",\\\\\\\"9001\\\\\\\"]],AXIS[\\\\\\\"Up\\\\\\\",UP]]\"\n" +
//                "          }\n" +
//                "        }\n" +
//                "      },\n" +
//                "      \"gcpsByOperator\": false\n" +
//                "    }\n" +
//                "  }\n" +
//                "}"

        var p = Photogrammetry()
        p.analytics = "photogrammetry"
        p.id = missionId
        var par = Parameters()
        par.flightId = flightId
        //var ps = ProcessSettings()

        par.processSettings = processSettings
        p.parameters = par

        return p
    }

    private val targetHost: String

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


    fun createPix4DProcessSettings(): ProcessSettings {
//    val template = "{\n" +
//            "  \"mapType\": \"map\",\n" +
//            "  \"tools_set\": \"pix4d\",\n" +
//            "  \"analytics\": [],\n" +
//            "  \"mapping\": {\n" +
//            "    \"preset\": \"SPEED\",\n" +
//            "    \"mesh\": true,\n" +
//            "    \"processingAreaSetting\": \"none\",\n" +
//            "    \"crs\": {\n" +
//            "      \"output\": {\n" +
//            "        \"vertical_srs_wkt\": \"VERT_CS[\\\"EGM96 geoid (meters)\\\",VERT_DATUM[\\\"EGM96 geoid\\\",2005,EXTENSION[\\\"PROJ4_GRIDS\\\",\\\"egm96_15.gtx\\\"],AUTHORITY[\\\"EPSG\\\",\\\"5171\\\"]],UNIT[\\\"metre\\\",1,AUTHORITY[\\\"EPSG\\\",\\\"9001\\\"]],AXIS[\\\"Up\\\",UP]]\",\n" +
//            "        \"horizontal_srs_wkt\": \"PROJCS[\\\"WGS 84 / UTM zone 34N\\\",GEOGCS[\\\"WGS 84\\\",DATUM[\\\"WGS_1984\\\",SPHEROID[\\\"WGS 84\\\",6378137,298.257223563,AUTHORITY[\\\"EPSG\\\",\\\"7030\\\"]],AUTHORITY[\\\"EPSG\\\",\\\"6326\\\"]],PRIMEM[\\\"Greenwich\\\",0,AUTHORITY[\\\"EPSG\\\",\\\"8901\\\"]],UNIT[\\\"degree\\\",0.0174532925199433,AUTHORITY[\\\"EPSG\\\",\\\"9122\\\"]],AUTHORITY[\\\"EPSG\\\",\\\"4326\\\"]],PROJECTION[\\\"Transverse_Mercator\\\"],PARAMETER[\\\"latitude_of_origin\\\",0],PARAMETER[\\\"central_meridian\\\",21],PARAMETER[\\\"scale_factor\\\",0.9996],PARAMETER[\\\"false_easting\\\",500000],PARAMETER[\\\"false_northing\\\",0],UNIT[\\\"metre\\\",1,AUTHORITY[\\\"EPSG\\\",\\\"9001\\\"]],AXIS[\\\"Easting\\\",EAST],AXIS[\\\"Northing\\\",NORTH],AUTHORITY[\\\"EPSG\\\",\\\"32634\\\"]]\"\n" +
//            "      },\n" +
//            "      \"gcp\": {},\n" +
//            "      \"image\": {\n" +
//            "        \"vertical_srs_wkt\": \"VERT_CS[\\\"EGM96 geoid (meters)\\\",VERT_DATUM[\\\"EGM96 geoid\\\",2005,EXTENSION[\\\"PROJ4_GRIDS\\\",\\\"egm96_15.gtx\\\"],AUTHORITY[\\\"EPSG\\\",\\\"5171\\\"]],UNIT[\\\"metre\\\",1,AUTHORITY[\\\"EPSG\\\",\\\"9001\\\"]],AXIS[\\\"Up\\\",UP]]\",\n" +
//            "        \"horizontal_srs_wkt\": \"GEOGCS[\\\"WGS 84\\\",DATUM[\\\"WGS_1984\\\",SPHEROID[\\\"WGS 84\\\",6378137,298.257223563,AUTHORITY[\\\"EPSG\\\",\\\"7030\\\"]],TOWGS84[0,0,0,0,0,0,0],AUTHORITY[\\\"EPSG\\\",\\\"6326\\\"]],PRIMEM[\\\"Greenwich\\\",0,AUTHORITY[\\\"EPSG\\\",\\\"8901\\\"]],UNIT[\\\"degree\\\",0.0174532925199433,AUTHORITY[\\\"EPSG\\\",\\\"9122\\\"]],AUTHORITY[\\\"EPSG\\\",\\\"4326\\\"]]\"\n" +
//            "      }\n" +
//            "    }\n" +
//            "  }\n" +
//            "}"

        var processSettings = ProcessSettings()
        processSettings.mapType = "map"
        processSettings.toolsSet = "pix4d"
        //processSettings.analytics =
        var mapping = Mapping()
        mapping.preset = "SPEED"
        mapping.mesh = true
        mapping.processingAreaSetting = "none"
        var crs = Crs()
        var imageVerticalSrsWkt = "VERT_CS[\\\"EGM96 geoid (meters)\\\",VERT_DATUM[\\\"EGM96 geoid\\\",2005,EXTENSION[\\\"PROJ4_GRIDS\\\",\\\"egm96_15.gtx\\\"],AUTHORITY[\\\"EPSG\\\",\\\"5171\\\"]],UNIT[\\\"metre\\\",1,AUTHORITY[\\\"EPSG\\\",\\\"9001\\\"]],AXIS[\\\"Up\\\",UP]]"
        var outputHorizontalWKT = "PROJCS[\"WGS 84 / UTM zone 34N\",GEOGCS[\"WGS 84\",DATUM[\"WGS_1984\",SPHEROID[\"WGS 84\",6378137,298.257223563,AUTHORITY[\"EPSG\",\"7030\"]],AUTHORITY[\"EPSG\",\"6326\"]],PRIMEM[\"Greenwich\",0,AUTHORITY[\"EPSG\",\"8901\"]],UNIT[\"degree\",0.0174532925199433,AUTHORITY[\"EPSG\",\"9122\"]],AUTHORITY[\"EPSG\",\"4326\"]],PROJECTION[\"Transverse_Mercator\"],PARAMETER[\"latitude_of_origin\",0],PARAMETER[\"central_meridian\",21],PARAMETER[\"scale_factor\",0.9996],PARAMETER[\"false_easting\",500000],PARAMETER[\"false_northing\",0],UNIT[\"metre\",1,AUTHORITY[\"EPSG\",\"9001\"]],AXIS[\"Easting\",EAST],AXIS[\"Northing\",NORTH],AUTHORITY[\"EPSG\",\"32634\"]]"
        var imageHorizontalWKT = ""
        var outputVerticalSrsWkt = "VERT_CS[\"EGM96 geoid (meters)\",VERT_DATUM[\"EGM96 geoid\",2005,EXTENSION[\"PROJ4_GRIDS\",\"egm96_15.gtx\"],AUTHORITY[\"EPSG\",\"5171\"]],UNIT[\"metre\",1,AUTHORITY[\"EPSG\",\"9001\"]],AXIS[\"Up\",UP]]"

        var output = Output()
        output.withHorizontalSrsWkt(outputHorizontalWKT).withVerticalSrsWkt(outputVerticalSrsWkt)
        crs.output = output
        crs.image = Image().withVerticalSrsWkt(imageVerticalSrsWkt)
        mapping.crs = crs
        processSettings.mapping = mapping

        return processSettings

    }

    fun createBentleyProcessSettings(): ProcessSettings {
//    val template = "{\n" +
//            "  \"mapType\": \"map\",\n" +
//            "  \"tools_set\": \"pix4d\",\n" +
//            "  \"analytics\": [],\n" +
//            "  \"mapping\": {\n" +
//            "    \"preset\": \"SPEED\",\n" +
//            "    \"mesh\": true,\n" +
//            "    \"processingAreaSetting\": \"none\",\n" +
//            "    \"crs\": {\n" +
//            "      \"output\": {\n" +
//            "        \"vertical_srs_wkt\": \"VERT_CS[\\\"EGM96 geoid (meters)\\\",VERT_DATUM[\\\"EGM96 geoid\\\",2005,EXTENSION[\\\"PROJ4_GRIDS\\\",\\\"egm96_15.gtx\\\"],AUTHORITY[\\\"EPSG\\\",\\\"5171\\\"]],UNIT[\\\"metre\\\",1,AUTHORITY[\\\"EPSG\\\",\\\"9001\\\"]],AXIS[\\\"Up\\\",UP]]\",\n" +
//            "        \"horizontal_srs_wkt\": \"PROJCS[\\\"WGS 84 / UTM zone 34N\\\",GEOGCS[\\\"WGS 84\\\",DATUM[\\\"WGS_1984\\\",SPHEROID[\\\"WGS 84\\\",6378137,298.257223563,AUTHORITY[\\\"EPSG\\\",\\\"7030\\\"]],AUTHORITY[\\\"EPSG\\\",\\\"6326\\\"]],PRIMEM[\\\"Greenwich\\\",0,AUTHORITY[\\\"EPSG\\\",\\\"8901\\\"]],UNIT[\\\"degree\\\",0.0174532925199433,AUTHORITY[\\\"EPSG\\\",\\\"9122\\\"]],AUTHORITY[\\\"EPSG\\\",\\\"4326\\\"]],PROJECTION[\\\"Transverse_Mercator\\\"],PARAMETER[\\\"latitude_of_origin\\\",0],PARAMETER[\\\"central_meridian\\\",21],PARAMETER[\\\"scale_factor\\\",0.9996],PARAMETER[\\\"false_easting\\\",500000],PARAMETER[\\\"false_northing\\\",0],UNIT[\\\"metre\\\",1,AUTHORITY[\\\"EPSG\\\",\\\"9001\\\"]],AXIS[\\\"Easting\\\",EAST],AXIS[\\\"Northing\\\",NORTH],AUTHORITY[\\\"EPSG\\\",\\\"32634\\\"]]\"\n" +
//            "      },\n" +
//            "      \"gcp\": {},\n" +
//            "      \"image\": {\n" +
//            "        \"vertical_srs_wkt\": \"VERT_CS[\\\"EGM96 geoid (meters)\\\",VERT_DATUM[\\\"EGM96 geoid\\\",2005,EXTENSION[\\\"PROJ4_GRIDS\\\",\\\"egm96_15.gtx\\\"],AUTHORITY[\\\"EPSG\\\",\\\"5171\\\"]],UNIT[\\\"metre\\\",1,AUTHORITY[\\\"EPSG\\\",\\\"9001\\\"]],AXIS[\\\"Up\\\",UP]]\",\n" +
//            "        \"horizontal_srs_wkt\": \"GEOGCS[\\\"WGS 84\\\",DATUM[\\\"WGS_1984\\\",SPHEROID[\\\"WGS 84\\\",6378137,298.257223563,AUTHORITY[\\\"EPSG\\\",\\\"7030\\\"]],TOWGS84[0,0,0,0,0,0,0],AUTHORITY[\\\"EPSG\\\",\\\"6326\\\"]],PRIMEM[\\\"Greenwich\\\",0,AUTHORITY[\\\"EPSG\\\",\\\"8901\\\"]],UNIT[\\\"degree\\\",0.0174532925199433,AUTHORITY[\\\"EPSG\\\",\\\"9122\\\"]],AUTHORITY[\\\"EPSG\\\",\\\"4326\\\"]]\"\n" +
//            "      }\n" +
//            "    }\n" +
//            "  }\n" +
//            "}"

        var processSettings = ProcessSettings()
        processSettings.mapType = "map"
        processSettings.toolsSet = "bentley"
        //processSettings.analytics =
        var mapping = Mapping()
        mapping.preset = "QUALITY"
        mapping.mesh = true
        mapping.processingAreaSetting = "mission"
        var crs = Crs()

        var imageVerticalSrsWkt = "VERT_CS[\"EGM96 geoid (meters)\",VERT_DATUM[\"EGM96 geoid\",2005,EXTENSION[\"PROJ4_GRIDS\",\"egm96_15.gtx\"],AUTHORITY[\"EPSG\",\"5171\"]],UNIT[\"metre\",1,AUTHORITY[\"EPSG\",\"9001\"]],AXIS[\"Up\",UP]]"

        var imageHorizontalWKT = "GEOGCS[\"WGS 84\",DATUM[\"WGS_1984\",SPHEROID[\"WGS 84\",6378137,298.257223563,AUTHORITY[\"EPSG\",\"7030\"]],TOWGS84[0,0,0,0,0,0,0],AUTHORITY[\"EPSG\",\"6326\"]],PRIMEM[\"Greenwich\",0,AUTHORITY[\"EPSG\",\"8901\"]],UNIT[\"degree\",0.0174532925199433,AUTHORITY[\"EPSG\",\"9122\"]],AUTHORITY[\"EPSG\",\"4326\"]]"

        var outputVerticalSrsWkt = "VERT_CS[\"EGM96 geoid (meters)\",VERT_DATUM[\"EGM96 geoid\",2005,EXTENSION[\"PROJ4_GRIDS\",\"egm96_15.gtx\"],AUTHORITY[\"EPSG\",\"5171\"]],UNIT[\"metre\",1,AUTHORITY[\"EPSG\",\"9001\"]],AXIS[\"Up\",UP]]"
        var outputHorizontalWKT = "PROJCS[\"WGS 84 / UTM zone 12N\",GEOGCS[\"WGS 84\",DATUM[\"WGS_1984\",SPHEROID[\"WGS 84\",6378137,298.257223563,AUTHORITY[\"EPSG\",\"7030\"]],AUTHORITY[\"EPSG\",\"6326\"]],PRIMEM[\"Greenwich\",0,AUTHORITY[\"EPSG\",\"8901\"]],UNIT[\"degree\",0.0174532925199433,AUTHORITY[\"EPSG\",\"9122\"]],AUTHORITY[\"EPSG\",\"4326\"]],PROJECTION[\"Transverse_Mercator\"],PARAMETER[\"latitude_of_origin\",0],PARAMETER[\"central_meridian\",-111],PARAMETER[\"scale_factor\",0.9996],PARAMETER[\"false_easting\",500000],PARAMETER[\"false_northing\",0],UNIT[\"metre\",1,AUTHORITY[\"EPSG\",\"9001\"]],AXIS[\"Easting\",EAST],AXIS[\"Northing\",NORTH],AUTHORITY[\"EPSG\",\"32612\"]]"


        var output = Output()
        output.withHorizontalSrsWkt(outputHorizontalWKT).withVerticalSrsWkt(outputVerticalSrsWkt)
        crs.output = output
        crs.image = Image().withVerticalSrsWkt(imageVerticalSrsWkt).withHorizontalSrsWkt(imageHorizontalWKT)
        mapping.crs = crs
        processSettings.mapping = mapping

        return processSettings

    }


}