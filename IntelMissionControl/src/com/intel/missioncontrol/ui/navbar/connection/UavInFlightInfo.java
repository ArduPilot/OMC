/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.connection;

public final class UavInFlightInfo {

    private double longitude;
    private double latitude;
    private double altitude;
    private int numberOfSatellites;
    private double batteryVoltage;
    private int batteryPercent;

    private float parametersUpdateProgress;

    public UavInFlightInfo(
            double longitude,
            double latitude,
            double altitude,
            int numberOfSatellites,
            double batteryVoltage,
            int batteryPercent) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.altitude = altitude;
        this.numberOfSatellites = numberOfSatellites;
        this.batteryVoltage = batteryVoltage;
        this.batteryPercent = batteryPercent;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public int getBatteryPercent() {
        return batteryPercent;
    }

    public void setBatteryPercent(int batteryPercent) {
        this.batteryPercent = batteryPercent;
    }

    public double getBatteryVoltage() {
        return batteryVoltage;
    }

    public void setBatteryVoltage(double batteryVoltage) {
        this.batteryVoltage = batteryVoltage;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getNumberOfSatellites() {
        return numberOfSatellites;
    }

    public void setNumberOfSatellites(int numberOfSatellites) {
        this.numberOfSatellites = numberOfSatellites;
    }

    public float getParametersUpdateProgress() {
        return parametersUpdateProgress;
    }

    public void setParametersUpdateProgress(float parametersUpdateProgress) {
        this.parametersUpdateProgress = parametersUpdateProgress;
    }
}
