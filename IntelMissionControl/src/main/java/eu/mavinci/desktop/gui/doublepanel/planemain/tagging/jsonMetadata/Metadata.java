/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.planemain.tagging.jsonMetadata;

import com.google.gson.annotations.SerializedName;
import eu.mavinci.core.obfuscation.IKeepAll;

public class Metadata implements IKeepAll {

    @SerializedName("vendor_name") private final String vendorName;
    @SerializedName("model_name") private final String modelName;
    @SerializedName("metering_mode") private final String meteringMode;
    @SerializedName("lighting_mode") private final String lightingMode;
    @SerializedName("capture_time") private final String captureTime;

    @SerializedName("image_width") private final int imageWidth;
    @SerializedName("image_height") private final int imageHeight;

    @SerializedName("exposure_time") private final double exposureTime;
    @SerializedName("focal_length") private final double focalLength;
    private final double gain;
    private final double aperture;

    private final boolean color;
    private final int[] shape;

    private MetadataStatus status;

    public Metadata(final String vendorName,
                    final String modelName,
                    final String meteringMode,
                    final String lightingMode,
                    final String captureTime,
                    final int imageWidth,
                    final int imageHeight,
                    final double exposureTime,
                    final double focalLength,
                    final double gain,
                    final double aperture,
                    final boolean color,
                    final int[] shape) {
        this.vendorName = vendorName;
        this.modelName = modelName;
        this.meteringMode = meteringMode;
        this.lightingMode = lightingMode;
        this.captureTime = captureTime;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.exposureTime = exposureTime;
        this.focalLength = focalLength;
        this.gain = gain;
        this.aperture = aperture;
        this.color = color;
        this.shape = shape;
    }

    public String getVendorName() {
        return this.vendorName;
    }

    public String getModelName() {
        return this.modelName;
    }

    public String getMeteringMode() {
        return this.meteringMode;
    }

    public String getLightingMode() {
        return this.lightingMode;
    }

    public String getCaptureTime() {
        return this.captureTime;
    }

    public int getImageWidth() {
        return this.imageWidth;
    }

    public int getImageHeight() {
        return this.imageHeight;
    }

    public double getExposureTime() {
        return this.exposureTime;
    }

    public double getFocalLength() {
        return this.focalLength;
    }

    public double getGain() {
        return this.gain;
    }

    public double getAperture() {
        return this.aperture;
    }

    public boolean isColor() {
        return this.color;
    }

    public int[] getShape() {
        return this.shape;
    }

    public MetadataStatus getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append("Metadata [")
                .append("Vendor name=").append(this.getVendorName())
                .append(", Model name=").append(this.getModelName())
                .append(", imageWidth=").append(this.getImageWidth())
                .append(", imageHeight=").append(this.getImageHeight())
                .append(", meteringMode=").append(this.getMeteringMode())
                .append(", lightingMode=").append(this.getLightingMode())
                .append(", exposureTime=").append(this.getExposureTime())
                .append(", focalLength=").append(this.getFocalLength())
                .append(", gain=").append(this.getGain())
                .append(", aperture=").append(this.getAperture())
                .append(", captureTime=").append(this.getCaptureTime())
                .append(", status=").append(this.getStatus())
                .append(", color=").append(this.isColor())
                .append(", shape=").append(this.getShape())
                .append("]").toString();
    }
}
