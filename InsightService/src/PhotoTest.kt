import com.drew.imaging.ImageMetadataReader
import com.drew.metadata.exif.ExifSubIFDDirectory
import com.drew.metadata.exif.GpsDirectory
import com.drew.metadata.jpeg.JpegDirectory
import com.intel.insight.datastructures.Photo
import org.junit.jupiter.api.Test
import java.io.File
import java.io.FileFilter
import java.text.SimpleDateFormat
import java.util.*

/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */


class PhotoTest {

    @Test
    fun testParsingDateFromFile() {
        var folderName = "C:\\Users\\jtroseme\\Downloads\\realsense"
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
                println(photo.utc)
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
}