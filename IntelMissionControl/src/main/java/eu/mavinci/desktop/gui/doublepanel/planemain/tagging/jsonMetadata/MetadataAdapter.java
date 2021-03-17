/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.planemain.tagging.jsonMetadata;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class MetadataAdapter {

    private final Metadata metadata;

    public MetadataAdapter(final Metadata metadata) {
        this.metadata = metadata;
    }

    public String getAperture() {
        return Double.toString(this.metadata.getAperture());
    }

    public String getShutterSpeed() {
        return this.getExposureTime();
    }

    public String getImageWidth() {
        return Integer.toString(this.metadata.getImageWidth());
    }

    public String getImageHeight() {
        return Integer.toString(this.metadata.getImageHeight());
    }

    public String getMeteringMode() {
        String meteringMode = this.metadata.getMeteringMode().trim().toLowerCase();

        switch (meteringMode) {
        case "unknown":
            return "0";
        case "average":
            return "1";
        case "centerweightedaverage":
            return "2";
        case "spot":
            return "3";
        case "multispot":
            return "4";
        case "pattern":
            return "5";
        case "partial":
            return "6";
        default:
            return "255";
        }
    }

    public String getFocalLength() {
        return formatDouble(this.metadata.getFocalLength());
    }

    public String getExposureTime() {
        return Double.toString(this.metadata.getExposureTime() / 1_000_000);
    }

    public String getMake() {
        return this.metadata.getVendorName();
    }

    public String getCameraModelName() {
        return this.metadata.getModelName();
    }

    public String getImageSize() {
        return String.format("%s %s", this.metadata.getImageWidth(), this.metadata.getImageHeight());
    }

    public String getDateTimeOriginal() {
        try {
            SimpleDateFormat source = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            SimpleDateFormat destination = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");

            return destination.format(source.parse(this.metadata.getCaptureTime()));
        } catch (final ParseException e) {
            return "";
        }
    }

    public String getLatitude() {
        return Double.toString(this.metadata.getStatus().getGPS().getLatitude() / 10_000_000);
    }

    public String getLatitudeRef() {
        return (this.metadata.getStatus().getGPS().getLatitude() >= 0) ? "N" : "S";
    }

    public String getLongitude() {
        return Double.toString(this.metadata.getStatus().getGPS().getLongitude() / 10_000_000);
    }

    public String getLongitudeRef() {
        return (this.metadata.getStatus().getGPS().getLongitude() >= 0) ? "E" : "W";
    }

    public String getAltitude() {
        return Double.toString(this.metadata.getStatus().getGPS().getAltitude() / 1000);
    }

    public String getAltitudeRef() {
        return (this.metadata.getStatus().getGPS().getAltitude() >= 0) ? "0" : "1";
    }

    public String getRoll() {
        return this.metadata.getStatus().getAirframe().getRoll();
    }

    public String getPitch() {
        return this.metadata.getStatus().getAirframe().getPitch();
    }

    public String getYaw() {
        return this.metadata.getStatus().getAirframe().getYaw();
    }

    @Override
    public String toString() {
        return new StringBuilder()
            .append("MetadataAdapter [")
            .append("Aperture=")
            .append(this.getAperture())
            .append(", ShutterSpeed=")
            .append(this.getShutterSpeed())
            .append(", ImageWidth=")
            .append(this.getImageWidth())
            .append(", ImageHeight=")
            .append(this.getImageHeight())
            .append(", FocalLength=")
            .append(this.getFocalLength())
            .append(", ExposureTime=")
            .append(this.getExposureTime())
            .append(", Make=")
            .append(this.getMake())
            .append(", Model=")
            .append(this.getCameraModelName())
            .append(", ImageSize=")
            .append(this.getImageSize())
            .append(", DateTimeOriginal=")
            .append(this.getDateTimeOriginal())
            .append(", GPSLatitude=")
            .append(this.getLatitude())
            .append(", GPSLatitudeRef=")
            .append(this.getLatitudeRef())
            .append(", GPSLongitude=")
            .append(this.getLongitude())
            .append(", GPSLongitudeRef=")
            .append(this.getLongitudeRef())
            .append(", GPSAltitude=")
            .append(this.getAltitude())
            .append(", GPSAltitudeRef=")
            .append(this.getAltitudeRef())
            .append("]")
            .toString();
    }

    private static String formatDouble(double value) {
        return (value == (long)value) ? String.format("%d", (long)value) : String.format("%s", value);
    }
}
