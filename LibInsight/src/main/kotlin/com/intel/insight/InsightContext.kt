/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.insight

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.intel.insight.datastructures.*
import org.apache.http.conn.ssl.SSLConnectionSocketFactory
import org.apache.http.impl.client.HttpClientBuilder
import unirest.HttpResponse
import unirest.Unirest
import java.io.File
import java.io.InputStream
import java.math.BigInteger
import java.security.MessageDigest
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import javax.net.ssl.*


class InsightContext {

    private var gson = GsonBuilder().create()

    private lateinit var survey: CreatedProject

    public fun prepareHeaders(token: String = "None"): HashMap<String, String> {
        var headers = HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        if (!token.equals("None")) {
            headers.put("Authorization", "Bearer $token")
        }
        headers.put("Pragma", "no-cache");
        headers.put("Referer", "$targetHost/login");
        headers.put("Origin", "$targetHost");
        headers.put("host", "dev.ixstack.net");
        headers.put("Cache-Control", "no-cache");
        headers.put("Accept", "application/json, text/plain, */*");
        headers.put("Accept-Encoding", "gzip, deflate, br")
        headers.put("Expires", "Sat, 01 Jan 2000 00:00:00 GMT")
        headers.put("Accept-Language", "en-US,en;q=0.9")
        headers.put("Connection", "keep-alive")
        headers.put(
                "User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Safari/537.36"
        );
        return headers
    }


    private fun CreateHttpClientBuilder(): HttpClientBuilder? {
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return emptyArray()
            }


            override fun checkClientTrusted(certs: Array<X509Certificate>, authType: String) {}

            override fun checkServerTrusted(certs: Array<X509Certificate>, authType: String) {}

        })

        val sc = SSLContext.getInstance("SSL")
        sc.init(null, trustAllCerts, SecureRandom())
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.socketFactory)

        // Create all-trusting host name verifier
        val allHostsValid = object : HostnameVerifier {
            override fun verify(hostname: String, session: SSLSession): Boolean {
                return true
            }
        }

        var clientBuilder = HttpClientBuilder.create();

        var sslFactory = SSLConnectionSocketFactory(
                sc,
                arrayOf<String>("TLSv1.2"), null,
                SSLConnectionSocketFactory.getDefaultHostnameVerifier()
        );
        clientBuilder.setSSLSocketFactory(sslFactory);
        //clientBuilder.setProxy(HttpHost("http://10.217.247.236", 911))
        return clientBuilder
    }

    fun authenticateWithInsight() {

        val builder = GsonBuilder()
        var gson = builder.create()

        var clientBuilder = CreateHttpClientBuilder()


        var httpClient = clientBuilder?.build();

        System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");


        var headers = prepareHeaders()


        Unirest.config().reset()
        Unirest.config()
                .httpClient(httpClient)

        val oauthRequest = OAuthRequest()

        var jsonResponse = Unirest.post("$targetHost/dxauth/oauth2/token")
                .headers(headers)
                .body(gson.toJson(oauthRequest))
                .asString()


        println(jsonResponse.status)
        println(jsonResponse.body)
        println(jsonResponse.statusText)


        var resp = gson.fromJson(jsonResponse.body.toString(), OAuthResponse::class.java)

        oauthResponse = resp


    }

    var targetHost: String = "https://dev.ixstack.net"

    fun authWithInsight(userName: String, password: String) {

        val builder = GsonBuilder()
        var gson = builder.create()

        var clientBuilder = CreateHttpClientBuilder()


        var httpClient = clientBuilder?.build();


        System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");


        var headers = prepareHeaders()


        Unirest.config().reset()
        Unirest.config()
                .httpClient(httpClient)

        val oauthRequest = OAuthRequest()
        oauthRequest.password = password
        oauthRequest.username = userName
        var jsonResponse: HttpResponse<String>? = null


        // try {


        val executor = Executors.newSingleThreadExecutor()
        val future = executor.submit<HttpResponse<String>>({
            Unirest.post("$targetHost/dxauth/oauth2/token")
                    .headers(headers)
                    .body(gson.toJson(oauthRequest))
                    .asString()
        })

        try {
            jsonResponse = future.get(5, TimeUnit.SECONDS)
        } catch (e: TimeoutException) {
            System.err.println("[InsightCLI]:\t Login unsuccesfull, Reason: TimeOut")
            throw e
        }

        if (jsonResponse?.status == 200) {
            println("[InsightCLI]:\tLogin Succesfull")
        } else {
            System.err.println("[InsightCLI]:\t Login unsuccesfull, Reason: ${jsonResponse?.status}, ${jsonResponse?.statusText}")
        }

        var resp = gson.fromJson(jsonResponse?.body.toString(), OAuthResponse::class.java)

        oauthResponse = resp


    }

    fun createNewProject(survey: Survey): CreatedProject {
        val builder = GsonBuilder()
        var gson = builder.create()

        var clientBuilder = CreateHttpClientBuilder()


        var httpClient = clientBuilder?.build();

        System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");


        var headers = prepareHeaders(this.oauthResponse.accessToken.orEmpty())


        Unirest.config().reset()
        Unirest.config()
                .httpClient(httpClient)


        var createProjectResponse = Unirest.post("$targetHost/uisrv/projects/survey")
                .headers(headers)
                .body(gson.toJson(survey))
                .asString()
        if (createProjectResponse.status == 200) {
            println("[InsightCLI]:\tCreated project Succesfull")
        } else {
            System.err.println("[InsightCLI]:\t Project creation failed, Reason: ${createProjectResponse.status}, ${createProjectResponse.statusText}, ${createProjectResponse.body}")
        }

        var createdProject = gson.fromJson(createProjectResponse.body, CreatedProject::class.java)

        this.survey = createdProject
        return (this.survey)
    }

    fun preparePhotoUpload(gson: Gson, photoUpload: PhotoUpload, progressCallback: IUploadProgress?): List<Photo> {
        return preparePhotoUpload(gson,photoUpload,progressCallback,false)
    }

    fun preparePhotoUpload(gson: Gson, photoUpload: PhotoUpload, progressCallback: IUploadProgress?, continueIfPreviousUpload: Boolean = false): List<Photo> {

        var startIndex = 0
        var photoResponse: PhotoPrep = PhotoPrep()
        var headers = prepareHeaders(this.oauthResponse.accessToken.orEmpty())
        var directory: String = "."
        var continuePossibleFromPreviousUpload = false

        if (continueIfPreviousUpload) {
            try {


                directory = File(photoUpload?.photos!![0].seq).getParentFile().absolutePath
                var fileContent = File("$directory/upload_project_template").readText()
                photoResponse = gson.fromJson(fileContent, PhotoPrep::class.java)
                println("[InsightCLI]:\tCreated Photo-Upload-Request succesfully")
                var progressLog = File("$directory/upload_project_progress").readLines()
                startIndex = progressLog[0].split(",")[1].toInt()
                println("[InsightCLI]:\tContinuing from previous upload at image $startIndex")
                continuePossibleFromPreviousUpload = true
            } catch (e: Exception) {
                println("[InsightCLI]:\tContinuing from previous upload not possible, $e")
            }

        }

        if (!continuePossibleFromPreviousUpload) {

            var createProjectResponse = Unirest.post("$targetHost/dxpm/photos")
                    .headers(headers)
                    .body(gson.toJson(photoUpload))
                    .asString()

            if (createProjectResponse.status == 200) {
                println("[InsightCLI]:\tCreated Photo-Upload-Request succesfully")
            } else {
                System.err.println("[InsightCLI]:\t Photo-Upload preparation failed, Reason: ${createProjectResponse.status}, ${createProjectResponse.statusText}, ${createProjectResponse.body}")
            }

            var crResp = createProjectResponse.body.toString()

            photoResponse = gson.fromJson(crResp, PhotoPrep::class.java)
            directory = File(photoResponse?.photos!![0].seq).getParentFile().absolutePath
            File("$directory/upload_project_template").writeText(crResp)
        }


//        println("succ")

        val N = photoResponse?.photos?.size
        var i = 0
        var progressLog = File("$directory/upload_project_progress")
        progressLog.writeText("")

        for (photo in photoResponse?.photos!!) {
            if (i < startIndex) {
                i = i + 1
                continue;
            }

            i = i + 1

            var id = photo.id
            var seq = photo.seq
            val myFile = File(photo.seq)
            println("[InsightCLI]:\t Currently uploading id:$id,\t seq:$seq")

            progressCallback?.progressMessage("uploading:" + myFile.name, i.toDouble() / N?.toDouble()!!)
            var ins: InputStream = myFile.inputStream()

            var content = ins.readBytes()

            //headers = prepareHeaders(this.oauthResponse.accessToken.orEmpty())
            //headers.put("Content-MD5",md5(content))
            headers = HashMap<String, String>();
            headers.put("Content-Type", "application/octet-stream");
            headers.put("Authorization", "Bearer " + this.oauthResponse.accessToken.orEmpty());
            headers.put("Pragma", "no-cache");
            headers.put("Referer", "$targetHost/app/browse/projects");
            headers.put("Origin", "$targetHost");
            headers.put("host", "dev.ixstack.net");
            headers.put("Cache-Control", "no-cache");
            headers.put("Accept", "*/*");
            headers.put("Accept-Encoding", "gzip, deflate, br")
            headers.put("Expires", "Sat, 01 Jan 2000 00:00:00 GMT")
            headers.put("Accept-Language", "en-US,en;q=0.9")
            headers.put("Connection", "keep-alive")
            headers.put("Content-MD5", md5(content))
            var size = content.size
//            println(size)
//            println(md5(content))
            //headers.put("Content-Length","$size")
//            headers.put(
//                    "User-Agent",
//                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Safari/537.36"
//            )


            var target = "$targetHost/dxds/" +
                    "photos/jpg/$id"
            var createProjectResponse = Unirest.put(target)
                    .headers(headers)
                    .body(content).asString()
            if (createProjectResponse.status == 200) {
                println("[InsightCLI]:\tCreated Photo Upload  succesfull\t Progress ${i}/${N}")
                progressLog.writeText("")
                progressLog.appendText("$id,${i},${N}")
            } else {
                System.err.println("[InsightCLI]:\t Photo Upload ${i}/${N} failed, Reason: ${createProjectResponse.status}, ${createProjectResponse.statusText}, ${createProjectResponse.body}")
            }

        }

        var fid = photoUpload.flight?.orEmpty()
        var target = "$targetHost/dxpm/flights/$fid/uploads/status"
        var body = "{\"_id\":\"$fid\",\"status\":\"complete\"}"
        headers = prepareHeaders(this.oauthResponse.accessToken.orEmpty())

        var createProjectResponse = Unirest.post("$target")
                .headers(headers)
                .body(body)
                .asString()

        if (createProjectResponse.status == 200) {
            println("[InsightCLI]:\tUploading Project succesfull")
        } else {
            System.err.println("[InsightCLI]:\t Uploading Project failed, Reason: ${createProjectResponse.status}, ${createProjectResponse.statusText}, ${createProjectResponse.body}")
        }
//        println(gson.toJson(photoUpload))

        return photoResponse?.photos
    }


    lateinit var oauthResponse: OAuthResponse
}


private fun createTestSurvey(gson: Gson): Survey {
    val survey = Survey()
    survey.name = "hallo marco"
    survey.addProjectToUsers = true
    val geom = Geometries()
    geom.type = "GeometryCollection"
    // the following is just very annoying to do in Kotlin/Java
    val coordinates = gson.fromJson(
            "{\n" +
                    "        \"type\": \"Polygon\",\n" +
                    "        \"coordinates\": [\n" +
                    "          [\n" +
                    "            [\n" +
                    "              23.864491681523386,\n" +
                    "              61.452707086048974\n" +
                    "            ],\n" +
                    "            [\n" +
                    "              23.865460152914082,\n" +
                    "              61.452707086048974\n" +
                    "            ],\n" +
                    "            [\n" +
                    "              23.865460152914082,\n" +
                    "              61.453012636318704\n" +
                    "            ],\n" +
                    "            [\n" +
                    "              23.864491681523386,\n" +
                    "              61.453012636318704\n" +
                    "            ],\n" +
                    "            [\n" +
                    "              23.864491681523386,\n" +
                    "              61.452707086048974\n" +
                    "            ]\n" +
                    "          ]\n" +
                    "        ]\n" +
                    "      }", Coordinates::class.java
    )
    geom.geometries.add(coordinates)
    geom.geometries
    // survey.
    val camera = Camera()
    camera.aspectRatio = 1.502202643171806
    camera.focalLength = 24
    camera.height = 3632
    camera.width = 5456
    camera.model = "SONY_UMC-R10C_24"
    survey.cameras
    survey.geometry = geom
    survey.area = 1752.3959482859652
    val processSettings = ProcessSettings()
    processSettings.mapType = ""
    val inspection = Inspection()
    inspection.video = false
    processSettings.inspection = inspection
    survey.surveyDate = "2018-04-12T00:00:00.000Z"
    survey.numberOfPhotos = 5
    survey.cameras
    survey.industry = "Geospatial"
    survey.processSettings = processSettings
    survey.cameras.add(camera)
    return survey
}

fun main(args: Array<String>) {
    var context = InsightContext()
    context.authenticateWithInsight()
    println(context.oauthResponse.accessToken)
    val builder = GsonBuilder()
    var gson = builder.create()
    context.createNewProject(createTestSurvey(gson))


}


fun String.md5(): String {
    val md = MessageDigest.getInstance("MD5")
    return BigInteger(1, md.digest(toByteArray())).toString(16).padStart(32, '0')
}

//fun ByteArray.md5(): String {
//    val md = MessageDigest.getInstance("MD5")
//    return BigInteger(1, md.digest(toByteArray())).toString(16).padStart(32, '0')
//}

fun md5(foo: ByteArray): String {
    val md = MessageDigest.getInstance("MD5")
    return BigInteger(1, md.digest(foo)).toString(16).padStart(32, '0')
}