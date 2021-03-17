/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.calculator;

import com.intel.missioncontrol.hardware.IGenericCameraConfiguration;
import com.intel.missioncontrol.hardware.IGenericCameraDescription;
import com.intel.missioncontrol.hardware.IHardwareConfiguration;
import com.intel.missioncontrol.hardware.ILensDescription;
import com.intel.missioncontrol.hardware.IPlatformDescription;
import com.intel.missioncontrol.measure.Unit;
import eu.mavinci.core.flightplan.CFlightplan;
import eu.mavinci.desktop.gui.doublepanel.camerasettings.CameraHelper;

/*
This guy will store gsd and altitude and guarantee their correspondance(object distance) of the AOI
 */
public class AltitudeGsdCalculator {
    public static final double DEF_GSD = 0.001;
    public static final double DEF_ALT = 70;

    public enum CalculationPreference {
        CALC_FROM_FOCAL,
        CALC_FROM_HEIGHT,
        // CALC_FROM_AREA,
        CALC_FROM_GSD;
    }

    // TODO should be final
    private CFlightplan flightPlan;
    private IHardwareConfiguration hardwareConfiguration;
    // fight height or object distance
    private double alt = DEF_ALT;

    // The worst of X and Y is the total one
    private double gsd;

    // GSD x
    private double objectResX;

    // GSD y
    private double objectResY;
    private double coveredX;
    private double coveredY;
    private double cameraRepTimeWithWind = 2;
    private double cameraRepTimeAgainst = 2;
    private double overlap;
    private double fuzzyAngle;
    private double fuzzySpeed;
    private double windSpeedInflight;

    private CalculationPreference mode = CalculationPreference.CALC_FROM_HEIGHT;

    public AltitudeGsdCalculator() {}

    public AltitudeGsdCalculator(IHardwareConfiguration hardwareConfiguration) {
        this.hardwareConfiguration = hardwareConfiguration;
    }

    public double getCameraRepTimeWithWind() {
        return cameraRepTimeWithWind;
    }

    public void setCameraRepTimeWithWind(double cameraRepTimeWithWind) {
        this.cameraRepTimeWithWind = cameraRepTimeWithWind;
        recalculateOverlap();
    }

    public double getCameraRepTimeAgainst() {
        return cameraRepTimeAgainst;
    }

    // public void setCameraRepTimeAgainst(double cameraRepTimeAgainst) {
    // this.cameraRepTimeAgainst = cameraRepTimeAgainst;
    // recalculateOverlap();
    // }

    public double getWindSpeedInflightMpS() {
        return windSpeedInflight;
    }

    public void setWindSpeedInflightMpS(double windSpeedInflight) {
        this.windSpeedInflight = windSpeedInflight;
        recalculateOverlap();
    }

    public double getAlt() {
        return alt;
    }

    public void setAlt(double alt) {
        this.alt = alt;
        mode = CalculationPreference.CALC_FROM_HEIGHT;
        recalculate(mode);
    }

    public double getObjectResX() {
        return this.objectResX;
    }

    public void setObject_res(double objectRes) {
        setObjectResX(objectRes);
        // setObjectResY(objectRes);
    }

    public void setObjectResX(double objectResX) {
        /*if (objectResX > this.objectResX) {
            this.objectResX = objectResX;
            this.coveredX = this.objectResX * getCameraDescription().getCcdResX();
            recalculate(CalculationPreference.CALC_FROM_AREA);
        } else if (objectResX < this.objectResX) {*/
        this.objectResX = objectResX;
        this.objectResY = objectResX;
        recalculate(CalculationPreference.CALC_FROM_GSD);
        // }
    }

    /*public void setObjectResY(double objectResY) {
        if (objectResY > this.objectResY) {
            this.objectResY = objectResY;
            this.coveredY = this.objectResY * getCameraDescription().getCcdResY();
            recalculate(CalculationPreference.CALC_FROM_AREA);
        } else if (objectResY < this.objectResY) {
            this.objectResY = objectResY;
            recalculate(CalculationPreference.CALC_FROM_GSD);
        }
    }*/

    public double getOverlap() {
        return overlap;
    }

    // public void setOverlap(double overlap) {
    // this.overlap = overlap;
    // }

    public double getFuzzyAngle() {
        return fuzzyAngle;
    }

    // public void setFuzzyAngle(double fuzzyAngle) {
    // this.fuzzyAngle = fuzzyAngle;
    // }

    public double getFuzzySpeedMpS() {
        return fuzzySpeed;
    }

    public double getFuzzyTotal() {
        return Math.sqrt(fuzzySpeed * fuzzySpeed + fuzzyAngle * fuzzyAngle);
    }

    // public void setFuzzySpeed(double fuzzySpeed) {
    // this.fuzzySpeed = fuzzySpeed;
    // }

    public void recalculateOverlap() {
        // take the lenght of the axis, what is closeer to the flightdirection
        double yaw = -getCameraDescription().getYaw().convertTo(Unit.RADIAN).getValue().doubleValue();
        boolean isXAxis = Math.sin(2 * (yaw + Math.PI / 4.)) > 0; // 0° = north = Y
        double covered = isXAxis ? coveredX : coveredY;
        double shortening = Math.abs(isXAxis ? Math.cos(yaw) : Math.sin(yaw));
        overlap =
            100
                * (1
                    - (shortening
                        * (getPlatformDescription()
                                .getMaxPlaneSpeed()
                                .convertTo(Unit.METER_PER_SECOND)
                                .getValue()
                                .doubleValue()
                            + windSpeedInflight)
                        / ((cameraRepTimeWithWind) * covered)));
        recalculateFuzzy();
    }

    public void recalculateFuzzy() {
        // fuzzyAngle = Math.sqrt(alt * alt + coveredX * coveredX / 4. + coveredY * coveredY / 4.) *
        // (getPlatformDescription().getAngularSpeed().convertTo(Unit.DEGREE_PER_SECOND).getValue().doubleValue()) / 180
        // * Math.PI /
        // getCameraDescription().getOneOverExposureTime().convertTo(Unit.MILLISECOND).getValue().doubleValue();
        fuzzyAngle =
            alt
                * (getPlatformDescription()
                    .getAngularSpeedNoise()
                    .convertTo(Unit.DEGREE_PER_SECOND)
                    .getValue()
                    .doubleValue())
                / 180
                * Math.PI
                / getCameraDescription().getOneOverExposureTime().convertTo(Unit.MILLISECOND).getValue().doubleValue();
        fuzzySpeed =
            (getPlatformDescription().getMaxPlaneSpeed().convertTo(Unit.METER_PER_SECOND).getValue().doubleValue()
                    + windSpeedInflight)
                / getCameraDescription().getOneOverExposureTime().convertTo(Unit.MILLISECOND).getValue().doubleValue();
    }

    public void recalculateFuzzy(double maxGroundSpeedMpS) {
        // fuzzyAngle = Math.sqrt(alt * alt + coveredX * coveredX / 4. + coveredY * coveredY / 4.) *
        // (getPlatformDescription().getAngularSpeed().convertTo(Unit.DEGREE_PER_SECOND).getValue().doubleValue()) / 180
        // * Math.PI
        //        /
        // getCameraDescription().getOneOverExposureTime().convertTo(Unit.MILLISECOND).getValue().doubleValue();
        fuzzyAngle =
            alt
                * (getPlatformDescription()
                    .getAngularSpeedNoise()
                    .convertTo(Unit.DEGREE_PER_SECOND)
                    .getValue()
                    .doubleValue())
                / 180
                * Math.PI
                / getCameraDescription().getOneOverExposureTime().convertTo(Unit.MILLISECOND).getValue().doubleValue();
        fuzzySpeed =
            (maxGroundSpeedMpS)
                / getCameraDescription().getOneOverExposureTime().convertTo(Unit.MILLISECOND).getValue().doubleValue();
    }

    public double computeMaxGroundSpeedMpS() {
        double res = objectResX * objectResY - fuzzyAngle * fuzzyAngle;
        if (res < 0) {
            return 0;
        }

        double maxSpeedFuzz = Math.sqrt(res);
        return maxSpeedFuzz
            * getCameraDescription().getOneOverExposureTime().convertTo(Unit.MILLISECOND).getValue().doubleValue();
    }

    public void recalculate() {
        recalculate(mode);
    }

    private void recalculate(CalculationPreference mode) {
        double focalLength = getLensDescription().getFocalLength().convertTo(Unit.MILLIMETER).getValue().doubleValue();
        double ccdWidth = getCameraDescription().getCcdWidth().convertTo(Unit.MILLIMETER).getValue().doubleValue();
        double ccdHeight = getCameraDescription().getCcdHeight().convertTo(Unit.MILLIMETER).getValue().doubleValue();
        int ccdResX = getCameraDescription().getCcdResX();
        int ccdResY = getCameraDescription().getCcdResY();

        if (mode == CalculationPreference.CALC_FROM_HEIGHT || mode == CalculationPreference.CALC_FROM_FOCAL) {
            coveredX = getCorrectedAlt(alt, true) / (focalLength / 1000.0) * ccdWidth / 1000.0;
            coveredY = getCorrectedAlt(alt, true) / (focalLength / 1000.0) * ccdHeight / 1000.0;

            objectResX = coveredX / ccdResX;
            objectResY = coveredY / ccdResY;
            /*} else if (mode == CalculationPreference.CALC_FROM_AREA) {
            double height1 = (coveredX * (focalLength / 1000.0)) / (ccdWidth / 1000.0);
            double height2 = (coveredY * (focalLength / 1000.0)) / (ccdHeight / 1000.0);
            alt = getCorrectedAlt(Math.max(height1, height2), false);
            recalculate(CalculationPreference.CALC_FROM_HEIGHT);*/
        } else if (mode == CalculationPreference.CALC_FROM_GSD) {
            coveredX = objectResX * ccdResX;
            coveredY = objectResY * ccdResY;
            double height1 = (coveredX * (focalLength / 1000.0)) / (ccdWidth / 1000.0);
            double height2 = (coveredY * (focalLength / 1000.0)) / (ccdHeight / 1000.0);
            alt = getCorrectedAlt(Math.min(height1, height2), false);
        }

        recalculateOverlap();
    }

    public double getPlaneSpeed() {
        return getPlatformDescription().getMaxPlaneSpeed().convertTo(Unit.METER_PER_SECOND).getValue().doubleValue();
    }

    private IGenericCameraDescription getCameraDescription() {
        IGenericCameraConfiguration cameraConfiguration =
            getHardwareConfiguration().getPrimaryPayload(IGenericCameraConfiguration.class);
        IGenericCameraDescription cameraDescription = cameraConfiguration.getDescription();
        return cameraDescription;
    }

    private IPlatformDescription getPlatformDescription() {
        IPlatformDescription platformDescription = getHardwareConfiguration().getPlatformDescription();
        return platformDescription;
    }

    private ILensDescription getLensDescription() {
        IGenericCameraConfiguration cameraConfiguration =
            getHardwareConfiguration().getPrimaryPayload(IGenericCameraConfiguration.class);
        ILensDescription lensDescription = cameraConfiguration.getLens().getDescription();
        return lensDescription;
    }

    public double getGsd() {
        return Math.max(objectResX, objectResY);
    }

    public void setGsd(double gsd) {
        mode = CalculationPreference.CALC_FROM_GSD;
        this.gsd = gsd;
        setObject_res(gsd);
    }

    public void setFlightPlan(CFlightplan flightPlan) {
        if (flightPlan != null) {
            this.flightPlan = flightPlan;
            recalculate(mode);
        }
    }

    private double getCorrectedAlt(double alt, boolean fromUI) {
        double correctedAlt = alt;
        double[] effectiveFootprint = CameraHelper.getSizeInFlight(alt, getHardwareConfiguration());

        double pixelEnlargingCenter = effectiveFootprint[6];

        if (fromUI) {
            correctedAlt /= pixelEnlargingCenter;
            // gps offset corrections
            correctedAlt += getCameraDescription().getOffsetToSky().convertTo(Unit.METER).getValue().doubleValue();
        } else { // fixing the alt computed based on the GSD
            correctedAlt *= pixelEnlargingCenter;
            correctedAlt -= getCameraDescription().getOffsetToSky().convertTo(Unit.METER).getValue().doubleValue();
        }

        return correctedAlt;
    }

    private IHardwareConfiguration getHardwareConfiguration() {
        if (this.hardwareConfiguration != null) {
            return hardwareConfiguration;
        } else if (flightPlan != null) {
            return flightPlan.getHardwareConfiguration();
        }

        return null;
    }

}
