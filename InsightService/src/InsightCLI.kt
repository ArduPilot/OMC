package com.intel.insight


import com.drew.imaging.ImageMetadataReader
import com.drew.metadata.exif.*
import com.drew.metadata.jpeg.JpegDirectory
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import com.google.gson.GsonBuilder
import com.intel.insight.api.AnnotationService
import com.intel.insight.api.PhotogrammetryService
import com.intel.insight.datastructures.*
import com.intel.insight.datastructures.Annotation
import com.intel.insight.datastructures.Target
import gov.nasa.worldwind.geom.LatLon
import gov.nasa.worldwind.geom.Position
import java.io.File
import java.io.FileFilter
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.*


/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

class Insight : CliktCommand() {
    val userName: String by option(help = "userName").prompt("Your username")
    val password: String by option(help = "password").prompt("Your password")
    val folderName: String by option(help = "folderName").prompt("Images folderName")
    val surveyName: String by option(help = "surveyName").prompt("Name of new Project")
    val useIntelProxy: Boolean by option("--intel-proxy", "-i", help = "Use Intel Proxies").flag("--no-intel-proxy", "-I", default = false)
    val pix4DProcessingI: Boolean by option("--pix4DProcessing", "-p", help = "Start pix4DProcessing").flag("--no-pix4DProcessing", "-P", default = false)
    val continueIfPreviousUp: Boolean by option("--continue", "-c", help = "Continue previous upload if possible").flag("--no-continue", "-C", default = false)
    val bentleyProcessingI: Boolean by option("--bentleyProcessing", "-b", help = "Start bentleyProcessing").flag("--no-bentleyProcessing", "-B", default = false)
    val autoDiscover: Boolean by option("--autoDiscover", "-a", help = "Autodiscover proxy").flag("--no-autoDiscover", "-A", default = true)

    override fun run() {
        // Todo: for demo let's deactivate this
        var continueIfPreviousUpload = continueIfPreviousUp
//        var continueIfPreviousUpload = false
        //var targetHost = "http://192.168.5.97:81/"
        var targetHost = "https://dev.ixstack.net/"
        var bentleyProcessing = bentleyProcessingI
        var pix4DProcessing = pix4DProcessingI
        if(bentleyProcessing && pix4DProcessing) {
            bentleyProcessing = false
            println("[InsightCLI]:\tCannot start bentley and pix4d processing, defaulting to pix4d processing")
        }
//        pix4DProcessing = false
//        bentleyProcessing = true
        if(autoDiscover) {
            AutoProxyService.autoDiscoverAndSet()
        }

        if (useIntelProxy) {
        //if (true) {
            println("[InsightCLI]:\t Use Intel proxies")
            System.setProperty("http.proxyHost", "http://proxy-mu.intel.com")
            System.setProperty("http.proxyPort", "911")
            System.setProperty("https.proxyHost", "http://proxy-mu.intel.com")
            System.setProperty("https.proxyPort", "912")
            System.setProperty("socksProxyHost", "proxy-us.intel.com");
            System.setProperty("socksProxyPort", "1080");
        }
        //var folder = "C:\\Users\\jtroseme\\Downloads\\InsightTestUploadFolder"
        var folder = folderName
        var camera = createCamera(folder) // Todo: create me right!
        var boundingBox = createBoundingBox(folder) // Todo: calculate me
        var numberOfPhotos = readFromFolder(folderName)?.size
        //-1 // Todo: fixme
        val context = InsightContext()
        context.targetHost = targetHost

        var createdProject: CreatedProject? = null
        var survey: Survey? = null
        var gson = GsonBuilder().create()
        var canContinueFromPastUpload = false

        if (continueIfPreviousUpload) {
            try {
                var fileContent = File("$folder/upload_createdProject").readText()
                createdProject = gson.fromJson(fileContent, CreatedProject::class.java)
                fileContent = File("$folder/upload_createdSurvey").readText()
                survey = gson.fromJson(fileContent, Survey::class.java)
                println("[InsightCLI]:\tContinuing from previous uploaded project")
                canContinueFromPastUpload = true
                context.authWithInsight(userName, password)
            } catch (e: Exception) {
                println("[InsightCLI]:\t$e")
            }
        }
        if (!canContinueFromPastUpload) {
            survey = createSurvey(surveyName, boundingBox, camera, numberOfPhotos!!)
            if (pix4DProcessing) {
                survey.processSettings = PhotogrammetryService(context, targetHost).createPix4DProcessSettings()
            } else if (bentleyProcessing) {
                survey.processSettings = PhotogrammetryService(context, targetHost).createBentleyProcessSettings()
            }

            if (pix4DProcessing || bentleyProcessing) {
                try {
                    var photogrammetryParamsFile = File("$folder/photogrammetry_config.json")
                    if (photogrammetryParamsFile.exists()) {
                        var photogrammetryParams = photogrammetryParamsFile.readText()
                        survey.processSettings?.mapping?.photogrammetry_params = photogrammetryParams
                        survey.processSettings?.mapping?.preset = "CUSTOM"
                        println("[InsightCLI]:\t Trying to use photogrammetry settings from photogrammetry_config.json")
                    }
                } catch (e: Exception) {
                    println("[InsightCLI]:\t$e")
                }
            }

            File("$folder/upload_createdSurvey").writeText(gson.toJson(survey))

            createdProject = intelInsightCreateProject(userName, password, survey, context)
            File("$folder/upload_createdProject").writeText(gson.toJson(createdProject))
        }


        var photos = createPhotos(createdProject!!, folder)
        photos = intelInsightUpload(createdProject!!, survey!!, photos, context, continueIfPreviousUpload)
        if (pix4DProcessing) {
            println("[InsightCLI]:\t Trying to start photogrammetry using Pix4D")
            var ps = PhotogrammetryService(context, targetHost)
            var p = ps.createPhotogrammetryRequest(createdProject.mission?.id!!, createdProject.flight?.id!!)
            ps.requestPhotogrammetry(p)
        } else if (bentleyProcessing) {
            println("[InsightCLI]:\t Trying to start photogrammetry using Bentley Context Capture")
            var ps = PhotogrammetryService(context, targetHost)
            var p = ps.createPhotogrammetryRequest(createdProject.mission?.id!!, createdProject.flight?.id!!, ps.createBentleyProcessSettings())
            ps.requestPhotogrammetry(p)
        }


        //Todo: do this only if this had not been run previously when continuing
        var annotationFiles = readAnnotationFromFolder(folder)
        if (annotationFiles != null && annotationFiles.size >= 1) {
            for (file in annotationFiles) {
                var text = file.readText()
                var feature = gson.fromJson(text, Feature::class.java)
                if (!feature.properties?.image.isNullOrEmpty()) {
                    //todo: complete me

                    for (p in photos) {
                        if (p.fileName?.contains(feature.properties?.image.toString(), true)!!) {
                            var target = Target()
                            target.type = "photo"
                            target.id = createdProject?.flight?.id
                            target.subId = p.id
                            var annotation = Annotation().withProjectId(createdProject?.project?.id!!).withTarget(target).withFeature(feature)
                            var annotationService = AnnotationService(userName, password, targetHost)
                            annotationService.uploadAnnotation(annotation)
                        }
                    }

                } else {
                    //todo: complete me
                    var target = Target()
                    target.type = "2d"
                    var annotation = Annotation().withProjectId(createdProject?.project?.id!!).withTarget(target).withFeature(feature)
                    var annotationService = AnnotationService(userName, password, targetHost)
                    annotationService.uploadAnnotation(annotation)
                }
            }
        }
        println("[InsightCLI]\tDone")
        // need to exit hard because CliktCommand is buggy
        System.exit(0)
    }
}


fun createBoundingBox(folderName: String): ArrayList<LatLon> {
    var bb = ArrayList<LatLon>()
    var min_lat = 1000.0
    var max_lat = -1000.0
    var min_lon = 1000.0
    var max_lon = -1000.0

    val files = readFromFolder(folderName)
    if (files != null) {
        for (file in files) {
            val jpegFile = file
            val metadata = ImageMetadataReader.readMetadata(jpegFile)

            val gpsDirectories = metadata.getDirectoriesOfType(GpsDirectory::class.java)
            for (gpsDirectory in gpsDirectories) {
                // Try to read out the location, making sure it's non-zero
                val geoLocation = gpsDirectory.geoLocation
                if (geoLocation != null && !geoLocation.isZero) {


                    if (geoLocation.latitude > max_lat) {
                        max_lat = geoLocation.latitude
                    }
                    if (geoLocation.longitude > max_lon) {
                        max_lon = geoLocation.longitude
                    }
                    if (geoLocation.latitude < min_lat) {
                        min_lat = geoLocation.latitude
                    }
                    if (geoLocation.longitude < min_lon) {
                        min_lon = geoLocation.longitude
                    }

                    break
                }
            }
        }
    }


    if (min_lat == max_lat) {
        min_lat -= min_lat * 0.0001
        max_lat += max_lat * 0.0001
        println("[InsightCLI]:	Bounding Box minimum lat equals maximum lat, which is not supported by insight. Replacing $min_lat,$max_lat")
    }
    if (min_lon == max_lon) {
        min_lon -= min_lon * 0.0001
        max_lon += max_lon * 0.0001
        println("[InsightCLI]:	Bounding Box minimum lon equals maximum lon, which is not supported by insight. Replacing $min_lon,$max_lon")
    }

    var a = LatLon.fromDegrees(min_lat, min_lon)
    var b = LatLon.fromDegrees(max_lat, min_lon)
    var c = LatLon.fromDegrees(max_lat, max_lon)
    var d = LatLon.fromDegrees(min_lat, min_lon)

    if (min_lat == 1000.0 ||
            max_lat == -1000.0 ||
            min_lon == 1000.0 ||
            max_lon == -1000.0) {
        System.err.println("[InsightCLI]:\tBounding Box of images erroneous. min_lat: {$min_lat}, max_lat: {$max_lat}, min_lon: ${min_lon}, max_lon: ${max_lon}")
    }

    bb.add(a)
    bb.add(b)
    bb.add(c)
    bb.add(d)
    return (bb)
}

fun createCamera(folderName: String): Camera {
    val jpegFiles = readFromFolder(folderName)
    if (jpegFiles == null) {
        System.err.println("[InsightCLI]:\tFolder contains no valid files")
        return Camera()
    }
    val minNumberImages = 5
    if (jpegFiles.size < minNumberImages) {
        System.err.println("[InsightCLI]:\tFolder contains less than ${minNumberImages} images")
    }
    var jpegFile = jpegFiles.get(0)
    val metadata = ImageMetadataReader.readMetadata(jpegFile)
    val camera = Camera()
    val id0Directories = metadata.getFirstDirectoryOfType(ExifIFD0Directory::class.java)
    val desc = ExifIFD0Descriptor(id0Directories);
    //id0Directories.getString(ExifIFD0Directory.TAG_MAKE)
    var model = id0Directories.getString(ExifIFD0Directory.TAG_MODEL)

    val IFDDirectories = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory::class.java)
    var focalLength = IFDDirectories?.getString(ExifSubIFDDirectory.TAG_FOCAL_LENGTH)

    val jpegDirectory = metadata.getFirstDirectoryOfType(JpegDirectory::class.java)
    var width = jpegDirectory.getString(JpegDirectory.TAG_IMAGE_WIDTH)
    var height = jpegDirectory.getString(JpegDirectory.TAG_IMAGE_HEIGHT)

    var aspectRatio = width.toDouble() / height.toDouble()

    if (focalLength == null || aspectRatio == null || width == null || height == null || model == null) {
        System.err.println("[InsightCLI]: Error in loading Camera metadata from ${jpegFile?.absolutePath}." +
                "focalLength: ${focalLength}, aspectRatio: ${aspectRatio}, width: ${width}, height: ${height}, model: ${model}")
    }

    try {
        camera.focalLength = focalLength?.toInt()
    } catch (e: NumberFormatException) {
        var split = focalLength?.split("/")
        var num = split?.get(0)?.toDouble()!!
        var den = split?.get(1)?.toDouble()!!

        camera.focalLength = Math.round(num / den).toInt()
        println("[InsightCLI]:\\tFocal length given in unknowm format, assuming rational: ${focalLength}, now: ${camera.focalLength}\"")
    }
    camera.aspectRatio = aspectRatio
    camera.width = width.toInt()
    camera.height = height.toInt()
    camera.model = model
    return camera
}

fun createSurvey(surveyName: String, boundingBox: List<LatLon>, camera: Camera, numberOfPhotos: Int): Survey {


    val survey = Survey()
    survey.name = surveyName
    survey.addProjectToUsers = true


    val geom = Geometries()
    geom.type = "GeometryCollection"

    val builder = GsonBuilder()
    val gson = builder.create()

    val coordinates = gson.fromJson(
            "{\n"
                    + "        \"type\": \"Polygon\",\n"
                    + "        \"coordinates\": [\n"
                    + "          [\n"
                    + "            [\n"
                    + "              "
                    + boundingBox[0].longitude.degrees
                    + ",\n"
                    + "              "
                    + boundingBox[0].latitude.degrees
                    + "\n"
                    + "            ],\n"
                    + "            [\n"
                    + "              "
                    + boundingBox[1].longitude.degrees
                    + ",\n"
                    + "              "
                    + boundingBox[1].latitude.degrees
                    + "\n"
                    + "            ],\n"
                    + "            [\n"
                    + "              "
                    + boundingBox[2].longitude.degrees
                    + ",\n"
                    + "              "
                    + boundingBox[2].latitude.degrees
                    + "\n"
                    + "            ],\n"
                    + "            [\n"
                    + "              "
                    + boundingBox[3].longitude.degrees
                    + ",\n"
                    + "              "
                    + boundingBox[3].latitude.degrees
                    + "\n"
                    + "            ],\n"
                    + "            [\n"
                    + "              "
                    + boundingBox[0].longitude.degrees
                    + ",\n"
                    + "              "
                    + boundingBox[0].latitude.degrees
                    + "\n"
                    + "            ]\n"
                    + "          ]\n"
                    + "        ]\n"
                    + "      }",
            Coordinates::class.java!!)
    geom.geometries.add(coordinates)
    survey.geometry = geom


    survey.cameras.add(camera)

    survey.area = 1000.0 // TODO, maybe just the area covered by the sector?
    val processSettings = ProcessSettings()
    processSettings.mapType = ""
    val inspection = Inspection()
    inspection.video = false
    processSettings.inspection = inspection
    survey.surveyDate = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
    survey.numberOfPhotos = numberOfPhotos
    survey.industry = "Geospatial"
    survey.processSettings = processSettings

    return survey

}

fun createPhotos(createdProject: CreatedProject, folderName: String): List<Photo> {


    val ps = ArrayList<Position>() // Todo: fill me

//    for (match in matches)
//    {
//        val p = match.getShiftedPositionExport(hardwareConfig)
//        ps.add(p)
//    }
    // batch gdal transformation
//    var vs:List<SRStransformCacheEntry>? = null
//    try
//    {
//        vs = srs.fromWgs84(ps)
//    }
//    catch (e1:Exception) {
//        Debug.getLog().log(Level.WARNING, "problems to transform coordinates in Matching-CVS export to SRS $srs")
//    }


    val photoUpload = PhotoUpload()
    photoUpload.flight = createdProject.flight!!.id
    photoUpload.mission = createdProject.mission!!.id
    photoUpload.project = createdProject.project!!.id

    val photos = ArrayList<Photo>()

    val files = readFromFolder(folderName)
    if (files != null) {
        for (file in files) {
            val jpegFile = file
            val metadata = ImageMetadataReader.readMetadata(jpegFile)

            val jpegDirectory = metadata.getFirstDirectoryOfType(JpegDirectory::class.java)


            var width = jpegDirectory.getString(JpegDirectory.TAG_IMAGE_WIDTH)
            var height = jpegDirectory.getString(JpegDirectory.TAG_IMAGE_HEIGHT)


            val IFDDirectories = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory::class.java)
            var exposure = 20e-3
            try {
                var exposure = IFDDirectories.getRational(ExifSubIFDDirectory.TAG_EXPOSURE_TIME).toDouble()
            } catch (t: Throwable) {
                exposure = 20e-3
                System.out.println("[InsightCLI]:\tCould not find ExifSubIFDDirectory.TAG_EXPOSURE_TIME in ${file.absolutePath}. Using a default value of ${exposure}")
            }


            val gpsDirectory = metadata.getFirstDirectoryOfType(GpsDirectory::class.java)
            var lat = gpsDirectory.geoLocation.latitude
            var lon = gpsDirectory.geoLocation.longitude
            var alt = gpsDirectory.getRational(GpsDirectory.TAG_ALTITUDE).toDouble()
            val photo = Photo()
            photo.altitude = alt.toDouble()


            try {
                var date = IFDDirectories.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL)
                //photo.utc = "2018-05-29T10:07:25.000Z" // TODO from timestamp

                val tz = TimeZone.getTimeZone("UTC")
                val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                df.setTimeZone(tz)
                photo.utc = df.format(date)
            } catch (t: Throwable) {
                System.out.println("[InsightCLI]:\tCould not find ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL in ${file.absolutePath}. Using a default value of ${Date()}")
                var date = Date()
                val tz = TimeZone.getTimeZone("UTC")
                val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                df.setTimeZone(tz)
                photo.utc = df.format(date)
            }

            photo.shutter = exposure.toDouble()
            photo.sharpened = false
            photo.status = "uploading"
            photo.rtc = 4 // ????
            photo.camera = createdProject.cameras[0].id // TODO
            photo.fileName = jpegFile.name

            photo.width = width.toInt()
            photo.height = height.toInt()
            val photoGeom = PhotoGeometry()
            val coord = ArrayList<Double>()
            coord.add(lon)
            coord.add(lat)
            photoGeom.coordinates = coord
            photo.geometry = photoGeom
            //photo.setVerticalSrsWkt(srs.getWkt());
            // TODO: the vertical srs uses a different syntax
            photo.verticalSrsWkt = "VERT_CS[\\\"EGM96 geoid (meters)\\\",VERT_DATUM[\\\"EGM96 geoid\\\",2005,EXTENSION[\\\"PROJ4_GRIDS\\\",\\\"egm96_15.gtx\\\"],AUTHORITY[\\\"EPSG\\\",\\\"5171\\\"]],UNIT[\\\"metre\\\",1,AUTHORITY[\\\"EPSG\\\",\\\"9001\\\"]],AXIS[\\\"Up\\\",UP]]"
            photo.horizontalSrsWkt = "GEOGCS[\\\"WGS 84\\\",DATUM[\\\"WGS_1984\\\",SPHEROID[\\\"WGS 84\\\",6378137,298.257223563,AUTHORITY[\\\"EPSG\\\",\\\"7030\\\"]],TOWGS84[0,0,0,0,0,0,0],AUTHORITY[\\\"EPSG\\\",\\\"6326\\\"]],PRIMEM[\\\"Greenwich\\\",0,AUTHORITY[\\\"EPSG\\\",\\\"8901\\\"]],UNIT[\\\"degree\\\",0.0174532925199433,AUTHORITY[\\\"EPSG\\\",\\\"9122\\\"]],AUTHORITY[\\\"EPSG\\\",\\\"4326\\\"]]"
            // SONY_UMC-R10C_24_5c4ffb256bb2a051df3bdf35_2018-04-12T09:56:53.000Z
            photo.uploadId = (createdProject.cameras[0].id
                    + createdProject.mission!!.id
                    + "2018-05-29T10:07:25.000Z") // TODO from timestamp
            photo.flight = createdProject.flight!!.id // TODO

            photo.seq = file.path//match.getResourceFile()!!.getPath()
            photos.add(photo)

        }
    }


//    var i = -1
//    for (match in matches)
//    {
//        i++
//        val line = match.getPhotoLogLine()
//        val p = ps[i]
//        val v = vs!![i]
//        val imgFile = match.getResourceFile()
//        val photoFile = match.getCurPhotoFile()
//        val o = CameraHelper.getCorrectedOrientation(line, hardwareConfig)
//        val oRates = CameraHelper.getCorrectedRotationRates(line, hardwareConfig)
//        val m = CameraHelper.getCorrectedStateTransform(line, hardwareConfig).getTranspose()
//        val omegaPhiKappa = MathHelper.transformationToOmegaPhiKappa(m)
//        val rollPitchYaw = MathHelper.transformationToRollPitchYaw(m)
//
//        val exif = photoFile!!.getExif()
//        val timestampExif = exif.timestamp.toLong()
//
//        val photo = Photo()
//        photo.altitude = v.z
//        photo.utc = "2018-05-29T10:07:25.000Z" // TODO from timestamp
//        photo.shutter = exif.exposureSec
//        photo.sharpened = false
//        photo.status = "uploading"
//        photo.rtc = 4 // ????
//        photo.camera = createdProject.cameras[0].id // TODO
//        photo.width = cam.getDescription().getCcdResX()
//        photo.height = cam.getDescription().getCcdResY()
//        val photoGeom = PhotoGeometry()
//        val coord = ArrayList<Double>()
//        coord.add(v.x)
//        coord.add(v.y)
//        photoGeom.coordinates = coord
//        photo.geometry = photoGeom
//        //photo.setVerticalSrsWkt(srs.getWkt());
//        // TODO: the vertical srs uses a different syntax
//        photo.verticalSrsWkt = "VERT_CS[\\\"EGM96 geoid (meters)\\\",VERT_DATUM[\\\"EGM96 geoid\\\",2005,EXTENSION[\\\"PROJ4_GRIDS\\\",\\\"egm96_15.gtx\\\"],AUTHORITY[\\\"EPSG\\\",\\\"5171\\\"]],UNIT[\\\"metre\\\",1,AUTHORITY[\\\"EPSG\\\",\\\"9001\\\"]],AXIS[\\\"Up\\\",UP]]"
//        photo.horizontalSrsWkt = srs.getWkt()
//        // SONY_UMC-R10C_24_5c4ffb256bb2a051df3bdf35_2018-04-12T09:56:53.000Z
//        photo.uploadId = (createdProject.cameras[0].id
//                + createdProject.mission!!.id
//                + "2018-05-29T10:07:25.000Z") // TODO from timestamp
//        photo.flight = createdProject.flight!!.id // TODO
//
//        photo.seq = match.getResourceFile()!!.getPath()
//        photos.add(photo)
//    }


    return photos
}

fun intelInsightCreateProject(userName: String, password: String, survey: Survey, context: InsightContext): CreatedProject {

    context.authWithInsight(userName, password)


    val createdProject = context.createNewProject(survey)
    return createdProject
}

fun intelInsightUpload(createdProject: CreatedProject, survey: Survey, photos: List<Photo>, context: InsightContext, continueIfPreviousUpload: Boolean = false): List<Photo> {


    val photoUpload = PhotoUpload()
    photoUpload.flight = (createdProject.flight?.id)
    photoUpload.mission = (createdProject.mission?.id)
    photoUpload.project = (createdProject.project?.id)

    photoUpload.photos = (photos)

    var gson = GsonBuilder().create()
    return context.preparePhotoUpload(gson, photoUpload, null, continueIfPreviousUpload)
}

fun Insight.foo() {

}

fun main(args: Array<String>) {

    //Insight().main(args)

    System.exit(0)

    var folder = "C:\\Users\\jtroseme\\Downloads\\InsightTestUploadFolder"
    createPhotos(CreatedProject(), folder)


    val files = readFromFolder(folder)
    if (files != null) {
        for (file in files) {
            val jpegFile = file
            val metadata = ImageMetadataReader.readMetadata(jpegFile)
            for (directory in metadata.directories) {
                for (tag in directory.tags) {
                    System.out.println(tag)
                }
            }

            var camera = createCamera(folder)


            val gpsDirectories = metadata.getDirectoriesOfType(GpsDirectory::class.java)
            for (gpsDirectory in gpsDirectories) {
                // Try to read out the location, making sure it's non-zero
                val geoLocation = gpsDirectory.geoLocation
                if (geoLocation != null && !geoLocation.isZero) {
                    // Add to our collection for use below
                    //println(geoLocation.latitude)
                    //photoLocations.add(PhotoLocation(geoLocation, file))
                    break
                }
            }


            val exifDirectoryBase = metadata.getDirectoriesOfType(ExifDirectoryBase::class.java)
            for (exifDirectory in exifDirectoryBase) {
                //exifDirectory.
                // Try to read out the location, making sure it's non-zero
//            val geoLocation = exifDirectory.
//            if (geoLocation != null && !geoLocation.isZero) {
//                // Add to our collection for use below
//                println(geoLocation.latitude)
//                //photoLocations.add(PhotoLocation(geoLocation, file))
//                break
//            }
            }


        }
    }
}

private fun readFromFolder(folder: String): Array<out File>? {
    val acceptedExtensions = arrayOf(".jpg", ".jpeg")
    if (!File(folder).isDirectory) {
        System.err.println("[InsightCLI]:\tFolder ist not a directory")
    }
    val files = File(folder).listFiles(object : FileFilter {
        override fun accept(file: File): Boolean {
            if (file.isDirectory)
                return false
            for (extension in acceptedExtensions) {
                if (file.name.toLowerCase().endsWith(extension))
                    return true
            }
            return false
        }
    })

    return files
}

private fun readAnnotationFromFolder(folder: String): Array<out File>? {
    val acceptedExtensions = arrayOf(".aoi", ".gson", ".geojson")
    if (!File(folder).isDirectory) {
        System.err.println("[InsightCLI]:\t readAnnotationFromFolder, Folder ist not a directory")
    }
    val files = File(folder).listFiles(object : FileFilter {
        override fun accept(file: File): Boolean {
            if (file.isDirectory)
                return false
            for (extension in acceptedExtensions) {
                if (file.name.toLowerCase().endsWith(extension))
                    return true
            }
            return false
        }
    })

    return files
}