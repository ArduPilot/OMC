/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.planemain.tagging.jsonMetadata;

import java.util.AbstractMap;
import java.util.Map;
import thebuzzmedia.exiftool.ExifTool;

public class MetadataTagMapper {

    private interface Functor {
        String getValue();
    }

    private final Map<ExifTool.Tag, Functor> adapterMap;

    public MetadataTagMapper(final MetadataAdapter adapter) {
        adapterMap = Map.ofEntries(
                new AbstractMap.SimpleEntry<ExifTool.Tag, Functor>(ExifTool.Tag.APERTURE, adapter::getAperture),
                new AbstractMap.SimpleEntry<ExifTool.Tag, Functor>(ExifTool.Tag.SHUTTER_SPEED, adapter::getShutterSpeed),
                new AbstractMap.SimpleEntry<ExifTool.Tag, Functor>(ExifTool.Tag.IMAGE_WIDTH, adapter::getImageWidth),
                new AbstractMap.SimpleEntry<ExifTool.Tag, Functor>(ExifTool.Tag.IMAGE_HEIGHT, adapter::getImageHeight),
                new AbstractMap.SimpleEntry<ExifTool.Tag, Functor>(ExifTool.Tag.METERING_MODE, adapter::getMeteringMode),
                new AbstractMap.SimpleEntry<ExifTool.Tag, Functor>(ExifTool.Tag.FOCAL_LENGTH, adapter::getFocalLength),
                new AbstractMap.SimpleEntry<ExifTool.Tag, Functor>(ExifTool.Tag.EXPOSURE_TIME, adapter::getExposureTime),
                new AbstractMap.SimpleEntry<ExifTool.Tag, Functor>(ExifTool.Tag.XMP_MAKE, adapter::getMake),
                new AbstractMap.SimpleEntry<ExifTool.Tag, Functor>(ExifTool.Tag.MAKE, adapter::getMake),
                new AbstractMap.SimpleEntry<ExifTool.Tag, Functor>(ExifTool.Tag.MODEL, adapter::getCameraModelName),
                new AbstractMap.SimpleEntry<ExifTool.Tag, Functor>(ExifTool.Tag.DATE_TIME_ORIGINAL, adapter::getDateTimeOriginal),

                new AbstractMap.SimpleEntry<ExifTool.Tag, Functor>(ExifTool.Tag.GPS_LATITUDE, adapter::getLatitude),
                new AbstractMap.SimpleEntry<ExifTool.Tag, Functor>(ExifTool.Tag.GPS_LATITUDE_REF, adapter::getLatitudeRef),
                new AbstractMap.SimpleEntry<ExifTool.Tag, Functor>(ExifTool.Tag.GPS_LONGITUDE, adapter::getLongitude),
                new AbstractMap.SimpleEntry<ExifTool.Tag, Functor>(ExifTool.Tag.GPS_LONGITUDE_REF, adapter::getLongitudeRef),
                new AbstractMap.SimpleEntry<ExifTool.Tag, Functor>(ExifTool.Tag.GPS_ALTITUDE, adapter::getAltitude),
                new AbstractMap.SimpleEntry<ExifTool.Tag, Functor>(ExifTool.Tag.GPS_ALTITUDE_REF, adapter::getAltitudeRef),

                new AbstractMap.SimpleEntry<ExifTool.Tag, Functor>(ExifTool.Tag.ROLL, adapter::getRoll),
                new AbstractMap.SimpleEntry<ExifTool.Tag, Functor>(ExifTool.Tag.YAW, adapter::getYaw),
                new AbstractMap.SimpleEntry<ExifTool.Tag, Functor>(ExifTool.Tag.PITCH, adapter::getPitch),

                new AbstractMap.SimpleEntry<ExifTool.Tag, Functor>(ExifTool.Tag.AIRFRAME_ROLL_DEGREE, adapter::getRoll),
                new AbstractMap.SimpleEntry<ExifTool.Tag, Functor>(ExifTool.Tag.AIRFRAME_YAW_DEGREE, adapter::getYaw),
                new AbstractMap.SimpleEntry<ExifTool.Tag, Functor>(ExifTool.Tag.AIRFRAME_PITCH_DEGREE, adapter::getPitch),

                new AbstractMap.SimpleEntry<ExifTool.Tag, Functor>(ExifTool.Tag.GIMBAL_ROLL_DEGREE, adapter::getGimbalRoll),
                new AbstractMap.SimpleEntry<ExifTool.Tag, Functor>(ExifTool.Tag.GIMBAL_PITCH_DEGREE, adapter::getGimbalPitch),
                new AbstractMap.SimpleEntry<ExifTool.Tag, Functor>(ExifTool.Tag.GIMBAL_YAW_DEGREE, adapter::getGimbalYaw),

                new AbstractMap.SimpleEntry<ExifTool.Tag, Functor>(ExifTool.Tag.BASE_STATION_ALTITUDE, adapter::getBaseStationAltitude),
                new AbstractMap.SimpleEntry<ExifTool.Tag, Functor>(ExifTool.Tag.BASE_STATION_LATITUDE, adapter::getBaseStationLatitude),
                new AbstractMap.SimpleEntry<ExifTool.Tag, Functor>(ExifTool.Tag.BASE_STATION_LONGITUDE, adapter::getBaseStationLongitude),
                new AbstractMap.SimpleEntry<ExifTool.Tag, Functor>(ExifTool.Tag.BASE_STATION_FIX_TYPE, adapter::getBaseStationFixType)
                );
    }

    public String getValueByTag(final ExifTool.Tag tag) throws NullPointerException {
        if ((tag == null) || (adapterMap.containsKey(tag) == false)) {
            throw new NullPointerException("Invalid metadata tag.");
        }

        return adapterMap.get(tag).getValue();
    }
}
