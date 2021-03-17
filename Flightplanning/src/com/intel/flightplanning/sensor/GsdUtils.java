/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.flightplanning.sensor;

public class GsdUtils {

    public static float gsd(float alt, float f, float sensor_res) {
        return alt / f * sensor_res;
    }

    public static float gsd(float alt, float f, float sensor_size_px, float sensor_size_m) {
        return alt / f * sensor_size_m / sensor_size_px;
    }

    public static float altFromGSD(float gsd, float f, float sensor_res) {
        return gsd * f / sensor_res;
    }

    public static float gsdX(float alt, Camera cam ) {
        return alt / cam.getFocalLength() * cam.getSensorHeightCm() / 10.0f * cam.getSensorHeightPx();
    }

    public static float gsdY(float alt, Camera cam ) {
        return alt / cam.getFocalLength() * cam.getSensorWidthCm() / 10.0f * cam.getSensorWidthPx();
    }

    public static double calculateFuzzyAngle(double altitude, double angularSpeedNoise, double exposureTime) {
        double fuzzyAngle = altitude * angularSpeedNoise / 180 * Math.PI * exposureTime;

        return fuzzyAngle;
    }

    public static double getFuzzyTotal(double fuzzySpeed, double fuzzyAngle) {
        return Math.sqrt(fuzzySpeed * fuzzySpeed + fuzzyAngle * fuzzyAngle);
    }

    public static double calculateFuzzySpeed(double maxPlaneSpeed, double windSpeedInflight, double exposureTime) {
        double fuzzySpeed = maxPlaneSpeed + windSpeedInflight * exposureTime;
        return fuzzySpeed;
    }

    public double computeMaxGroundSpeedMpS(
            double fuzzyAngle, double objectResX, double objectResY, double exposureTime) {
        double res = objectResX * objectResY - fuzzyAngle * fuzzyAngle;
        if (res < 0) {
            return 0;
        }

        double maxSpeedFuzz = Math.sqrt(res);
        return maxSpeedFuzz / exposureTime;
    }
}
