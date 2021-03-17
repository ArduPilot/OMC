/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.flightplanning.sensor;

public class Camera extends Sensor {

    float minRepTime;
    float maxTimeVariation;
    private String Model;
    private int SensorWidthPx;
    private int sensorHeightPx;
    private float sensorWidthCm;
    private float sensorHeightCm;
    private float fnumber;
    private float focalLength;
    private String created;
    private float horizontalFov;
    private String Format;
    private float imageRate;
    private String filtertype;
    private float exposureTime;
    private transient boolean immutable = false;
    private String id = "";
    private String name = "";
    private float roll;
    private float pitch;
    private float yaw;
    private float maxRoll;
    private float maxPitch;
    private float maxYaw;
    private float minRoll;
    private float minPitch;
    private float minYaw;
    private boolean enforcePitchRange;
    private boolean isRollFixed;
    private boolean isPitchFixed;
    private boolean isYawFixed;
    private boolean isRollStabilized;
    private boolean isPitchStabilized;
    private boolean isYawStabilized;
    private boolean isExposureTimeFixed;
    private int ccdResX;
    private int ccdResY;
    private float ccdHeight;
    private float ccdWidth;
    private float ccdXTransl;
    private float ccdYTransl;
    private float exposureTimeUS;
    // micro four thirds mit 20mm Pancace objektiv - lumix gf1
    private float cameraDelay;
    private float pictureSizeInMB;
    private float storageCapacityInMB;
    // private FilterTypes filterType = FilterTypes.RGB;
    private float offsetToTail;
    private float offsetToRightWing;
    private float offsetToSky;
    private boolean isProjectableToGround;
    private String[] bandNames = new String[0];
    private boolean providingFeedback; // what kind of feedback can we get?

    public String getModel() {
        return Model;
    }

    public void setModel(String model) {
        Model = model;
    }

    public int getSensorWidthPx() {
        return SensorWidthPx;
    }

    public void setSensorWidthPx(int sensorWidthPx) {
        SensorWidthPx = sensorWidthPx;
    }

    public int getSensorHeightPx() {
        return sensorHeightPx;
    }

    public void setSensorHeightPx(int sensorHeightPx) {
        this.sensorHeightPx = sensorHeightPx;
    }

    public float getSensorWidthCm() {
        return sensorWidthCm;
    }

    public void setSensorWidthCm(float sensorWidthCm) {
        this.sensorWidthCm = sensorWidthCm;
    }

    public float getSensorHeightCm() {
        return sensorHeightCm;
    }

    public void setSensorHeightCm(float sensorHeightCm) {
        this.sensorHeightCm = sensorHeightCm;
    }

    public float getFnumber() {
        return fnumber;
    }

    public void setFnumber(float fnumber) {
        this.fnumber = fnumber;
    }

    public float getFocalLength() {
        return focalLength;
    }

    public void setFocalLength(float focalLength) {
        this.focalLength = focalLength;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public float getHorizontalFov() {
        return horizontalFov;
    }

    public void setHorizontalFov(float horizontalFov) {
        this.horizontalFov = horizontalFov;
    }

    public String getFormat() {
        return Format;
    }

    public void setFormat(String format) {
        Format = format;
    }

    public float getImageRate() {
        return imageRate;
    }

    public void setImageRate(float imageRate) {
        this.imageRate = imageRate;
    }

    public String getFiltertype() {
        return filtertype;
    }

    public void setFiltertype(String filtertype) {
        this.filtertype = filtertype;
    }

    public float getExposureTime() {
        return exposureTime;
    }

    public void setExposureTime(float exposureTime) {
        this.exposureTime = exposureTime;
    }

    public boolean isImmutable() {
        return immutable;
    }

    public void setImmutable(boolean immutable) {
        this.immutable = immutable;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getRoll() {
        return roll;
    }

    public void setRoll(float roll) {
        this.roll = roll;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getMaxRoll() {
        return maxRoll;
    }

    public void setMaxRoll(float maxRoll) {
        this.maxRoll = maxRoll;
    }

    public float getMaxPitch() {
        return maxPitch;
    }

    public void setMaxPitch(float maxPitch) {
        this.maxPitch = maxPitch;
    }

    public float getMaxYaw() {
        return maxYaw;
    }

    public void setMaxYaw(float maxYaw) {
        this.maxYaw = maxYaw;
    }

    public float getMinRoll() {
        return minRoll;
    }

    public void setMinRoll(float minRoll) {
        this.minRoll = minRoll;
    }

    public float getMinPitch() {
        return minPitch;
    }

    public void setMinPitch(float minPitch) {
        this.minPitch = minPitch;
    }

    public float getMinYaw() {
        return minYaw;
    }

    public void setMinYaw(float minYaw) {
        this.minYaw = minYaw;
    }

    public boolean isEnforcePitchRange() {
        return enforcePitchRange;
    }

    public void setEnforcePitchRange(boolean enforcePitchRange) {
        this.enforcePitchRange = enforcePitchRange;
    }

    public boolean isRollFixed() {
        return isRollFixed;
    }

    public void setRollFixed(boolean rollFixed) {
        isRollFixed = rollFixed;
    }

    public boolean isPitchFixed() {
        return isPitchFixed;
    }

    public void setPitchFixed(boolean pitchFixed) {
        isPitchFixed = pitchFixed;
    }

    public boolean isYawFixed() {
        return isYawFixed;
    }

    public void setYawFixed(boolean yawFixed) {
        isYawFixed = yawFixed;
    }

    public boolean isRollStabilized() {
        return isRollStabilized;
    }

    public void setRollStabilized(boolean rollStabilized) {
        isRollStabilized = rollStabilized;
    }

    public boolean isPitchStabilized() {
        return isPitchStabilized;
    }

    public void setPitchStabilized(boolean pitchStabilized) {
        isPitchStabilized = pitchStabilized;
    }

    public boolean isYawStabilized() {
        return isYawStabilized;
    }

    public void setYawStabilized(boolean yawStabilized) {
        isYawStabilized = yawStabilized;
    }

    public boolean isExposureTimeFixed() {
        return isExposureTimeFixed;
    }

    public void setExposureTimeFixed(boolean exposureTimeFixed) {
        isExposureTimeFixed = exposureTimeFixed;
    }

    public int getCcdResX() {
        return ccdResX;
    }

    public void setCcdResX(int ccdResX) {
        this.ccdResX = ccdResX;
    }

    public int getCcdResY() {
        return ccdResY;
    }

    public void setCcdResY(int ccdResY) {
        this.ccdResY = ccdResY;
    }

    public float getCcdHeight() {
        return ccdHeight;
    }

    public void setCcdHeight(float ccdHeight) {
        this.ccdHeight = ccdHeight;
    }

    public float getCcdWidth() {
        return ccdWidth;
    }

    public void setCcdWidth(float ccdWidth) {
        this.ccdWidth = ccdWidth;
    }

    public float getCcdXTransl() {
        return ccdXTransl;
    }

    public void setCcdXTransl(float ccdXTransl) {
        this.ccdXTransl = ccdXTransl;
    }

    public float getCcdYTransl() {
        return ccdYTransl;
    }

    public void setCcdYTransl(float ccdYTransl) {
        this.ccdYTransl = ccdYTransl;
    }

    public float getExposureTimeUS() {
        return exposureTimeUS;
    }

    public void setExposureTimeUS(float exposureTimeUS) {
        this.exposureTimeUS = exposureTimeUS;
    }

    public float getCameraDelay() {
        return cameraDelay;
    }

    public void setCameraDelay(float cameraDelay) {
        this.cameraDelay = cameraDelay;
    }

    public float getPictureSizeInMB() {
        return pictureSizeInMB;
    }

    public void setPictureSizeInMB(float pictureSizeInMB) {
        this.pictureSizeInMB = pictureSizeInMB;
    }

    public float getStorageCapacityInMB() {
        return storageCapacityInMB;
    }

    public void setStorageCapacityInMB(float storageCapacityInMB) {
        this.storageCapacityInMB = storageCapacityInMB;
    }

    public float getOffsetToTail() {
        return offsetToTail;
    }

    public void setOffsetToTail(float offsetToTail) {
        this.offsetToTail = offsetToTail;
    }

    public float getOffsetToRightWing() {
        return offsetToRightWing;
    }

    public void setOffsetToRightWing(float offsetToRightWing) {
        this.offsetToRightWing = offsetToRightWing;
    }

    public float getOffsetToSky() {
        return offsetToSky;
    }

    public void setOffsetToSky(float offsetToSky) {
        this.offsetToSky = offsetToSky;
    }

    public boolean isProjectableToGround() {
        return isProjectableToGround;
    }

    public void setProjectableToGround(boolean projectableToGround) {
        isProjectableToGround = projectableToGround;
    }

    public String[] getBandNames() {
        return bandNames;
    }

    public void setBandNames(String[] bandNames) {
        this.bandNames = bandNames;
    }

    public boolean isProvidingFeedback() {
        return providingFeedback;
    }

    public void setProvidingFeedback(boolean providingFeedback) {
        this.providingFeedback = providingFeedback;
    }

}
