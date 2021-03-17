/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.flightplanning.sensor;

public class PhotoProperties {

    float centrencyParallelFlight;
    double centrencyInFlight;
    double sizeParallelFlight;
    double sizeInFlight;
    double leftOvershoot;
    double efficiency;
    double pixelEnlargingCenter;
    private float sizeParallelFlightEff;
    private float sizeInFlightEff;
    private float overShootParallelFlight;

    public String toString() {
        return "centrencyParallelFlight:"
            + centrencyParallelFlight
            + "\t,centrencyInFlight:"
            + centrencyInFlight
            + "\t,sizeParallelFlight:"
            + sizeParallelFlight
            + "\t,sizeInFlight:"
            + sizeInFlight
            + "\t,leftOvershoot:"
            + leftOvershoot
            + "\t,efficiency:"
            + efficiency
            + "\t,pixelEnlargingCenter"
            + pixelEnlargingCenter;
    }

    public float getCentrencyParallelFlight() {
        return centrencyParallelFlight;
    }

    public void setCentrencyParallelFlight(float centrencyParallelFlight) {
        this.centrencyParallelFlight = centrencyParallelFlight;
    }

    public double getCentrencyInFlight() {
        return centrencyInFlight;
    }

    public void setCentrencyInFlight(double centrencyInFlight) {
        this.centrencyInFlight = centrencyInFlight;
    }

    public double getSizeParallelFlight() {
        return sizeParallelFlight;
    }

    public void setSizeParallelFlight(double sizeParallelFlight) {
        this.sizeParallelFlight = sizeParallelFlight;
    }

    public double getSizeInFlight() {
        return sizeInFlight;
    }

    public void setSizeInFlight(double sizeInFlight) {
        this.sizeInFlight = sizeInFlight;
    }

    public double getLeftOvershoot() {
        return leftOvershoot;
    }

    public void setLeftOvershoot(double leftOvershoot) {
        this.leftOvershoot = leftOvershoot;
    }

    public double getEfficiency() {
        return efficiency;
    }

    public void setEfficiency(double efficiency) {
        this.efficiency = efficiency;
    }

    public double getPixelEnlargingCenter() {
        return pixelEnlargingCenter;
    }

    public void setPixelEnlargingCenter(double pixelEnlargingCenter) {
        this.pixelEnlargingCenter = pixelEnlargingCenter;
    }

    PhotoProperties getSizeInFlight(double alt, Object placeHolder) {
        return this;
    }

    public float getSizeParallelFlightEff() {
        return sizeParallelFlightEff;
    }

    public void setSizeParallelFlightEff(float sizeParallelFlightEff) {
        this.sizeParallelFlightEff = sizeParallelFlightEff;
    }

    public float getSizeInFlightEff() {
        return sizeInFlightEff;
    }

    public void setSizeInFlightEff(float sizeInFlightEff) {
        this.sizeInFlightEff = sizeInFlightEff;
    }

    public float getOverShootParallelFlight() {
        return overShootParallelFlight;
    }

    public void setOverShootParallelFlight(float overShootParallelFlight) {
        this.overShootParallelFlight = overShootParallelFlight;
    }
}
