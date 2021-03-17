/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.hardware;

import com.google.common.base.Objects;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.intel.missioncontrol.NotificationObject;
import com.intel.missioncontrol.helper.DoubleHelper;
import com.intel.missioncontrol.helper.Expect;
import com.intel.missioncontrol.measure.Dimension.Angle;
import com.intel.missioncontrol.measure.Dimension.Length;
import com.intel.missioncontrol.measure.Dimension.Time;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.Unit;
import eu.mavinci.core.flightplan.camera.FilterTypes;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class GenericCameraDescription extends NotificationObject implements IMutableGenericCameraDescription {

    static final GenericCameraDescription NONE;

    static {
        NONE = new GenericCameraDescription();
        NONE.setId("NONE");
        NONE.setName("NONE");
    }

    public static class Deserializer implements JsonDeserializer<GenericCameraDescription> {
        @Override
        public GenericCameraDescription deserialize(JsonElement arg0, Type arg1, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject json = arg0.getAsJsonObject();
            JsonHelper helper = new JsonHelper(json);
            GenericCameraDescription description = new GenericCameraDescription();
            description.setId(helper.getString("id"));
            description.setName(helper.getString("name"));
            description.setRoll(Quantity.of(helper.getDouble("roll"), Unit.DEGREE));
            description.setPitch(Quantity.of(helper.getDouble("pitch"), Unit.DEGREE));
            description.setYaw(Quantity.of(helper.getDouble("yaw"), Unit.DEGREE));
            description.setMinRoll(Quantity.of(helper.getDouble("minRoll"), Unit.DEGREE));
            description.setMinPitch(Quantity.of(helper.getDouble("minPitch"), Unit.DEGREE));
            description.setMinYaw(Quantity.of(helper.getDouble("minYaw"), Unit.DEGREE));
            description.setMaxRoll(Quantity.of(helper.getDouble("maxRoll"), Unit.DEGREE));
            description.setMaxPitch(Quantity.of(helper.getDouble("maxPitch"), Unit.DEGREE));
            description.setMaxYaw(Quantity.of(helper.getDouble("maxYaw"), Unit.DEGREE));
            description.setEnforcePitchRange(helper.getBoolean("enforcePitchRange"));
            description.setIsRollFixed(helper.getBoolean("isRollFixed"));
            description.setIsPitchFixed(helper.getBoolean("isPitchFixed"));
            description.setIsYawFixed(helper.getBoolean("isYawFixed"));
            description.setIsRollStabilized(helper.getBoolean("isRollStabilized"));
            description.setIsPitchStabilized(helper.getBoolean("isPitchStabilized"));
            description.setIsYawStabilized(helper.getBoolean("isYawStabilized"));
            description.setCcdResX(helper.getInteger("ccdResX"));
            description.setCcdResY(helper.getInteger("ccdResY"));
            description.setCcdHeight(Quantity.of(helper.getDouble("ccdHeight"), Unit.MILLIMETER));
            description.setCcdWidth(Quantity.of(helper.getDouble("ccdWidth"), Unit.MILLIMETER));
            description.setCcdXTransl(Quantity.of(helper.getDouble("ccdXTransl"), Unit.MILLIMETER));
            description.setCcdYTransl(Quantity.of(helper.getDouble("ccdYTransl"), Unit.MILLIMETER));
            description.setOneOverExposureTime(Quantity.of(helper.getDouble("exposureTimeUS"), Unit.MILLISECOND));
            description.setCameraDelay(Quantity.of(helper.getDouble("cameraDelayMS"), Unit.MILLISECOND));

            JsonArray array = json.getAsJsonArray("exifModels");
            if (array != null) {
                description.setExifModels(Arrays.asList(context.deserialize(array, String[].class)));
            } else {
                JsonHelper.warnMissingKey("exifModels");
            }

            description.setSdCapacityInGB(helper.getDouble("sdCapacityInGB"));
            description.setEnsoCalibFile(helper.getString("ensoCalibFile"));
            description.setIcarosCalibFile(helper.getString("icarosCalibFile"));
            description.setMenciCalibFile(helper.getString("menciCalibFile"));
            description.setPix4dCalibFile(helper.getString("pix4dCalibFile"));
            description.setAgisoftCalibFile(helper.getString("agisoftCalibFile"));
            description.setFilterType(FilterTypes.valueOf(helper.getString("filterType")));
            description.setOffsetToTail(Quantity.of(helper.getDouble("gpsOffsetToTailInM"), Unit.METER));
            description.setOffsetToRightWing(Quantity.of(helper.getDouble("gpsOffsetToRightWingInM"), Unit.METER));
            description.setOffsetToSky(Quantity.of(helper.getDouble("gpsOffsetToSkyInM"), Unit.METER));
            description.setIsProjectableToGround(helper.getBoolean("isProjectableToGround"));

            array = json.getAsJsonArray("bandNames");
            if (array != null) {
                description.setBandNamesSplit(context.deserialize(array, String[].class));
            } else {
                JsonHelper.warnMissingKey("bandNames");
            }

            description.setIsProvidingFeedback(helper.getBoolean("providingFeedback"));
            description.setIsExposureTimeFixed(helper.getBoolean("exposureTimeFixed"));

            array = json.getAsJsonArray("compatibleLensesIds");
            if (array != null) {
                description.setCompatibleLensIds(Arrays.asList(context.deserialize(array, String[].class)));
            } else {
                JsonHelper.warnMissingKey("compatibleLensesIds");
            }

            return description;
        }
    }

    private transient boolean immutable = false;
    private String id = "";
    private String name = "";
    private Quantity<Angle> roll = Quantity.of(0, Unit.RADIAN);
    private Quantity<Angle> pitch = Quantity.of(0, Unit.RADIAN);
    private Quantity<Angle> yaw = Quantity.of(0, Unit.RADIAN);
    private Quantity<Angle> maxRoll = Quantity.of(0, Unit.RADIAN);
    private Quantity<Angle> maxPitch = Quantity.of(0, Unit.RADIAN);
    private Quantity<Angle> maxYaw = Quantity.of(0, Unit.RADIAN);
    private Quantity<Angle> minRoll = Quantity.of(0, Unit.RADIAN);
    private Quantity<Angle> minPitch = Quantity.of(0, Unit.RADIAN);
    private Quantity<Angle> minYaw = Quantity.of(0, Unit.RADIAN);
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
    private Quantity<Length> ccdHeight = Quantity.of(0, Unit.MILLIMETER); // Sensohöhe in mm
    private Quantity<Length> ccdWidth = Quantity.of(0, Unit.MILLIMETER); // Sensobreite in mm
    private Quantity<Length> ccdXTransl = Quantity.of(0, Unit.MILLIMETER);
    private Quantity<Length> ccdYTransl = Quantity.of(0, Unit.MILLIMETER);
    private Quantity<Time> exposureTimeUS = Quantity.of(0, Unit.SECOND); // in 1 over Sec
    // micro four thirds mit 20mm Pancace objektiv - lumix gf1
    private Quantity<Time> cameraDelay = Quantity.of(0, Unit.SECOND); // in ms
    private List<String> exifModels = new ArrayList<>();
    private double pictureSizeInMB;
    private double sdCapacityInGB;
    private String ensoCalibFile = "";
    private String icarosCalibFile = "";
    private String menciCalibFile = "";
    private String pix4dCalibFile = "";
    private String agisoftCalibFile = "";
    private FilterTypes filterType = FilterTypes.RGB;
    private Quantity<Length> offsetToTail = Quantity.of(0, Unit.METER);
    private Quantity<Length> offsetToRightWing = Quantity.of(0, Unit.METER);
    private Quantity<Length> offsetToSky = Quantity.of(0, Unit.METER);
    private boolean isProjectableToGround;
    private String[] bandNames = new String[0];
    private boolean providingFeedback;
    private List<String> compatibleLensIds = new ArrayList<>();

    public GenericCameraDescription() {}

    @Override
    public void setIsProjectableToGround(boolean value) {
        Expect.notNull(value, "value");
        verifyMutable();
        if (this.isProjectableToGround != value) {
            boolean oldValue = this.isProjectableToGround;
            this.isProjectableToGround = value;
            notifyPropertyChanged("isProjectableToGround", oldValue, value);
        }
    }

    @Override
    public void setIsProvidingFeedback(boolean value) {
        Expect.notNull(value, "value");
        verifyMutable();
        if (this.providingFeedback != value) {
            boolean oldValue = this.providingFeedback;
            this.providingFeedback = value;
            notifyPropertyChanged(IS_PROVIDING_FEEDBACK_PROPERTY, oldValue, value);
        }
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String value) {
        Expect.notNull(value, "value");
        verifyMutable();
        if (!this.id.equals(value)) {
            String oldValue = this.id;
            this.id = value;
            notifyPropertyChanged(ID_PROPERTY, oldValue, value);
        }
    }

    @Override
    public Quantity<Length> getCcdHeight() {
        return ccdHeight;
    }

    @Override
    public void setCcdHeight(Quantity<Length> value) {
        Expect.notNull(value, "value");
        verifyMutable();
        if (!this.ccdHeight.equals(value)) {
            Quantity<Length> oldValue = this.ccdHeight;
            this.ccdHeight = value;
            notifyPropertyChanged(CCD_HEIGHT_PROPERTY, oldValue, value);
        }
    }

    @Override
    public int getCcdResX() {
        return ccdResX;
    }

    @Override
    public void setCcdResX(int value) {
        Expect.notNull(value, "value");
        verifyMutable();
        if (this.ccdResX != value) {
            int oldValue = this.ccdResX;
            this.ccdResX = value;
            notifyPropertyChanged(CCD_RESX_PROPERTY, oldValue, value);
        }
    }

    @Override
    public int getCcdResY() {
        return ccdResY;
    }

    @Override
    public void setCcdResY(int value) {
        Expect.notNull(value, "value");
        verifyMutable();
        if (this.ccdResY != value) {
            int oldValue = this.ccdResY;
            this.ccdResY = value;
            notifyPropertyChanged(CCD_RESY_PROPERTY, oldValue, value);
        }
    }

    @Override
    public Quantity<Length> getCcdWidth() {
        return ccdWidth;
    }

    @Override
    public void setCcdWidth(Quantity<Length> value) {
        Expect.notNull(value, "value");
        verifyMutable();
        if (!this.ccdWidth.equals(value)) {
            Quantity<Length> oldValue = this.ccdWidth;
            this.ccdWidth = value;
            notifyPropertyChanged(CCD_WIDTH_PROPERTY, oldValue, value);
        }
    }

    @Override
    public Quantity<Length> getCcdXTransl() {
        return ccdXTransl;
    }

    @Override
    public void setCcdXTransl(Quantity<Length> value) {
        Expect.notNull(value, "value");
        verifyMutable();
        if (!this.ccdXTransl.equals(value)) {
            Quantity<Length> oldValue = this.ccdXTransl;
            this.ccdXTransl = value;
            notifyPropertyChanged(CCD_X_TRANSL_PROPERTY, oldValue, value);
        }
    }

    @Override
    public Quantity<Length> getCcdYTransl() {
        return ccdYTransl;
    }

    @Override
    public void setCcdYTransl(Quantity<Length> value) {
        Expect.notNull(value, "value");
        verifyMutable();
        if (!this.ccdYTransl.equals(value)) {
            Quantity<Length> oldValue = this.ccdYTransl;
            this.ccdYTransl = value;
            notifyPropertyChanged(CCD_Y_TRANSL_PROPERTY, oldValue, value);
        }
    }

    @Override
    public Quantity<Time> getCameraDelay() {
        return cameraDelay;
    }

    @Override
    public void setCameraDelay(Quantity<Time> value) {
        Expect.notNull(value, "value");
        verifyMutable();
        if (!this.cameraDelay.equals(value)) {
            Quantity<Time> oldValue = this.cameraDelay;
            this.cameraDelay = value;
            notifyPropertyChanged(CAMERA_DELAY_PROPERTY, oldValue, value);
        }
    }

    @Override
    public String getEnsoCalibFile() {
        return ensoCalibFile;
    }

    @Override
    public void setEnsoCalibFile(String value) {
        Expect.notNull(value, "value");
        verifyMutable();
        if (!this.ensoCalibFile.equals(value)) {
            String oldValue = this.ensoCalibFile;
            this.ensoCalibFile = value;
            notifyPropertyChanged(ENSO_CALIB_FILE_PROPERTY, oldValue, value);
        }
    }

    @Override
    public String getIcarosCalibFile() {
        return icarosCalibFile;
    }

    @Override
    public void setIcarosCalibFile(String value) {
        Expect.notNull(value, "value");
        verifyMutable();
        if (!this.icarosCalibFile.equals(value)) {
            String oldValue = this.icarosCalibFile;
            this.icarosCalibFile = value;
            notifyPropertyChanged(ICAROS_CALIB_FILE_PROPERTY, oldValue, value);
        }
    }

    @Override
    public String getMenciCalibFile() {
        return menciCalibFile;
    }

    @Override
    public void setMenciCalibFile(String value) {
        Expect.notNull(value, "value");
        verifyMutable();
        if (!this.menciCalibFile.equals(value)) {
            String oldValue = this.menciCalibFile;
            this.menciCalibFile = value;
            notifyPropertyChanged(MENCI_CALIB_FILE_PROPERTY, oldValue, value);
        }
    }

    @Override
    public String getPix4dCalibFile() {
        return pix4dCalibFile;
    }

    @Override
    public void setPix4dCalibFile(String value) {
        Expect.notNull(value, "value");
        verifyMutable();
        if (!this.pix4dCalibFile.equals(value)) {
            String oldValue = this.pix4dCalibFile;
            this.pix4dCalibFile = value;
            notifyPropertyChanged(PIX4D_CALIB_FILE_PROPERTY, oldValue, value);
        }
    }

    @Override
    public String getAgisoftCalibFile() {
        return agisoftCalibFile;
    }

    @Override
    public void setAgisoftCalibFile(String value) {
        Expect.notNull(value, "value");
        verifyMutable();
        if (!this.agisoftCalibFile.equals(value)) {
            String oldValue = this.agisoftCalibFile;
            this.agisoftCalibFile = value;
            notifyPropertyChanged(AGISOFT_CALIB_FILE_PROPERTY, oldValue, value);
        }
    }

    @Override
    public FilterTypes getFilterType() {
        return filterType;
    }

    @Override
    public void setFilterType(FilterTypes value) {
        Expect.notNull(value, "value");
        verifyMutable();
        if (this.filterType != value) {
            FilterTypes oldValue = this.filterType;
            this.filterType = value;
            notifyPropertyChanged(FILTER_TYPE_PROPERTY, oldValue, value);
        }
    }

    @Override
    public double getSdCapacityInGB() {
        return sdCapacityInGB;
    }

    @Override
    public void setSdCapacityInGB(double value) {
        Expect.notNull(value, "value");
        verifyMutable();
        if (!DoubleHelper.areClose(this.sdCapacityInGB, value)) {
            double oldValue = this.sdCapacityInGB;
            this.sdCapacityInGB = value;
            notifyPropertyChanged(SD_CAPACITY_PROPERTY, oldValue, value);
        }
    }

    @Override
    public List<String> getExifModels() {
        return exifModels;
    }

    @Override
    public void setExifModels(List<String> value) {
        Expect.notNull(value, "value");
        verifyMutable();
        if (!this.exifModels.equals(value)) {
            List<String> oldValue = this.exifModels;
            this.exifModels = value;
            notifyPropertyChanged(EXIF_MODEL_PROPERTY, oldValue, value);
        }
    }

    @Override
    public boolean isProjectableToGround() {
        return isProjectableToGround;
    }

    @Override
    public Quantity<Time> getOneOverExposureTime() {
        return exposureTimeUS;
    }

    @Override
    public void setOneOverExposureTime(Quantity<Time> value) {
        Expect.notNull(value, "value");
        verifyMutable();
        if (!this.exposureTimeUS.equals(value)) {
            Quantity<Time> oldValue = this.exposureTimeUS;
            this.exposureTimeUS = value;
            notifyPropertyChanged(ONE_OVER_EXPOSURE_TIME_PROPERTY, oldValue, value);
        }
    }

    @Override
    public double getPictureSizeInMB() {
        return pictureSizeInMB;
    }

    @Override
    public void setPictureSizeInMB(double value) {
        Expect.notNull(value, "value");
        verifyMutable();
        if (!DoubleHelper.areClose(this.pictureSizeInMB, value)) {
            double oldValue = this.pictureSizeInMB;
            this.pictureSizeInMB = value;
            notifyPropertyChanged(PICTURE_SIZE_PROPERTY, oldValue, value);
        }
    }

    @Override
    public String[] getBandNamesSplit() {
        return bandNames;
    }

    @Override
    public void setBandNamesSplit(String[] value) {
        Expect.notNull(value, "value");
        verifyMutable();
        if (!Arrays.equals(this.bandNames, value)) {
            String[] oldValue = this.bandNames;
            this.bandNames = value;
            notifyPropertyChanged(BAND_NAMES_SPLIT_PROPERTY, oldValue, value);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String value) {
        Expect.notNull(value, "value");
        verifyMutable();
        if (!this.name.equals(value)) {
            String oldValue = this.name;
            this.name = value;
            notifyPropertyChanged(NAME_PROPERTY, oldValue, value);
        }
    }

    @Override
    public boolean isProvidingFeedback() {
        return providingFeedback;
    }

    @Override
    public List<String> getCompatibleLensIds() {
        return compatibleLensIds;
    }

    @Override
    public void setCompatibleLensIds(List<String> value) {
        Expect.notNull(value, "value");
        verifyMutable();
        if (!this.compatibleLensIds.equals(value)) {
            List<String> oldValue = this.compatibleLensIds;
            this.compatibleLensIds = value;
            notifyPropertyChanged(COMPATIBLE_LENS_IDS_PROPERTY, oldValue, value);
        }
    }

    @Override
    public Quantity<Angle> getRoll() {
        return roll;
    }

    @Override
    public void setRoll(Quantity<Angle> value) {
        Expect.notNull(value, "value");
        verifyMutable();
        if (!this.roll.equals(value)) {
            Quantity<Angle> oldValue = this.roll;
            this.roll = value;
            notifyPropertyChanged(ROLL_PROPERTY, oldValue, value);
        }
    }

    @Override
    public Quantity<Angle> getPitch() {
        return pitch;
    }

    @Override
    public void setPitch(Quantity<Angle> value) {
        Expect.notNull(value, "value");
        verifyMutable();
        if (!this.pitch.equals(value)) {
            Quantity<Angle> oldValue = this.pitch;
            this.pitch = value;
            notifyPropertyChanged(PITCH_PROPERTY, oldValue, value);
        }
    }

    @Override
    public Quantity<Angle> getYaw() {
        return yaw;
    }

    @Override
    public void setYaw(Quantity<Angle> value) {
        Expect.notNull(value, "value");
        verifyMutable();
        if (!this.yaw.equals(value)) {
            Quantity<Angle> oldValue = this.yaw;
            this.yaw = value;
            notifyPropertyChanged(YAW_PROPERTY, oldValue, value);
        }
    }

    @Override
    public boolean getEnforcePitchRange() {
        return enforcePitchRange;
    }

    @Override
    public void setEnforcePitchRange(boolean value) {
        Expect.notNull(value, "value");
        verifyMutable();
        if (!this.yaw.equals(value)) {
            boolean oldValue = this.enforcePitchRange;
            this.enforcePitchRange = value;
            notifyPropertyChanged(ENFORCE_PITCH_RANGE_PROPERTY, oldValue, value);
        }
    }

    @Override
    public boolean isRollFixed() {
        return isRollFixed;
    }

    @Override
    public boolean isPitchFixed() {
        return isPitchFixed;
    }

    @Override
    public boolean isYawFixed() {
        return isYawFixed;
    }

    @Override
    public boolean isRollStabilized() {
        return isRollStabilized;
    }

    @Override
    public boolean isPitchStabilized() {
        return isPitchStabilized;
    }

    @Override
    public boolean isYawStabilized() {
        return isYawStabilized;
    }

    @Override
    public Quantity<Angle> getMaxRoll() {
        return maxRoll;
    }

    @Override
    public void setMaxRoll(Quantity<Angle> value) {
        Expect.notNull(value, "value");
        verifyMutable();
        if (!this.maxRoll.equals(value)) {
            Quantity<Angle> oldValue = this.maxRoll;
            this.maxRoll = value;
            notifyPropertyChanged(MAX_ROLL_PROPERTY, oldValue, value);
        }
    }

    @Override
    public Quantity<Angle> getMinRoll() {
        return minRoll;
    }

    @Override
    public void setMinRoll(Quantity<Angle> value) {
        Expect.notNull(value, "value");
        verifyMutable();
        if (!this.minRoll.equals(value)) {
            Quantity<Angle> oldValue = this.minRoll;
            this.minRoll = value;
            notifyPropertyChanged(MIN_ROLL_PROPERTY, oldValue, value);
        }
    }

    @Override
    public Quantity<Angle> getMaxPitch() {
        return maxPitch;
    }

    @Override
    public void setMaxPitch(Quantity<Angle> value) {
        Expect.notNull(value, "value");
        verifyMutable();
        if (!this.maxPitch.equals(value)) {
            Quantity<Angle> oldValue = this.maxPitch;
            this.maxPitch = value;
            notifyPropertyChanged(MAX_PITCH_PROPERTY, oldValue, value);
        }
    }

    @Override
    public Quantity<Angle> getMinPitch() {
        return minPitch;
    }

    @Override
    public void setMinPitch(Quantity<Angle> value) {
        Expect.notNull(value, "value");
        verifyMutable();
        if (!this.minPitch.equals(value)) {
            Quantity<Angle> oldValue = this.minPitch;
            this.minPitch = value;
            notifyPropertyChanged(MIN_PITCH_PROPERTY, oldValue, value);
        }
    }

    @Override
    public Quantity<Angle> getMaxYaw() {
        return maxYaw;
    }

    @Override
    public void setMaxYaw(Quantity<Angle> value) {
        Expect.notNull(value, "value");
        verifyMutable();
        if (!this.maxYaw.equals(value)) {
            Quantity<Angle> oldValue = this.maxYaw;
            this.maxYaw = value;
            notifyPropertyChanged(MAX_YAW_PROPERTY, oldValue, value);
        }
    }

    @Override
    public Quantity<Angle> getMinYaw() {
        return minYaw;
    }

    @Override
    public void setMinYaw(Quantity<Angle> value) {
        Expect.notNull(value, "value");
        verifyMutable();
        if (!this.minYaw.equals(value)) {
            Quantity<Angle> oldValue = this.minYaw;
            this.minYaw = value;
            notifyPropertyChanged(MIN_YAW_PROPERTY, oldValue, value);
        }
    }

    @Override
    public void setOffsetToTail(Quantity<Length> value) {
        Expect.notNull(value, "value");
        verifyMutable();
        if (!this.offsetToTail.equals(value)) {
            Quantity<Length> oldValue = this.offsetToTail;
            this.offsetToTail = value;
            notifyPropertyChanged(OFFSET_TO_TAIL_PROPERTY, oldValue, value);
        }
    }

    @Override
    public void setOffsetToRightWing(Quantity<Length> value) {
        Expect.notNull(value, "value");
        verifyMutable();
        if (!this.offsetToRightWing.equals(value)) {
            Quantity<Length> oldValue = this.offsetToRightWing;
            this.offsetToRightWing = value;
            notifyPropertyChanged(OFFSET_TO_RIGHT_WING_PROPERTY, oldValue, value);
        }
    }

    @Override
    public void setOffsetToSky(Quantity<Length> value) {
        Expect.notNull(value, "value");
        verifyMutable();
        if (!this.offsetToSky.equals(value)) {
            Quantity<Length> oldValue = this.offsetToSky;
            this.offsetToSky = value;
            notifyPropertyChanged(OFFSET_TO_SKY_PROPERTY, oldValue, value);
        }
    }

    @Override
    public Quantity<Length> getOffsetToTail() {
        return offsetToTail;
    }

    @Override
    public Quantity<Length> getOffsetToRightWing() {
        return offsetToRightWing;
    }

    @Override
    public Quantity<Length> getOffsetToSky() {
        return offsetToSky;
    }

    @Override
    public void setIsRollFixed(boolean value) {
        Expect.notNull(value, "value");
        verifyMutable();
        if (this.isRollFixed != value) {
            boolean oldValue = this.isRollFixed;
            this.isRollFixed = value;
            notifyPropertyChanged(IS_ROLL_FIXED_PROPERTY, oldValue, value);
        }
    }

    @Override
    public void setIsPitchFixed(boolean value) {
        Expect.notNull(value, "value");
        verifyMutable();
        if (this.isPitchFixed != value) {
            boolean oldValue = this.isPitchFixed;
            this.isPitchFixed = value;
            notifyPropertyChanged(IS_PITCH_FIXED_PROPERTY, oldValue, value);
        }
    }

    @Override
    public void setIsYawFixed(boolean value) {
        Expect.notNull(value, "value");
        verifyMutable();
        if (this.isYawFixed != value) {
            boolean oldValue = this.isYawFixed;
            this.isYawFixed = value;
            notifyPropertyChanged(IS_YAW_FIXED_PROPERTY, oldValue, value);
        }
    }

    @Override
    public void setIsRollStabilized(boolean value) {
        Expect.notNull(value, "value");
        verifyMutable();
        if (this.isRollStabilized != value) {
            boolean oldValue = this.isRollStabilized;
            this.isRollStabilized = value;
            notifyPropertyChanged(IS_ROLL_STABILIZED_PROPERTY, oldValue, value);
        }
    }

    @Override
    public void setIsPitchStabilized(boolean value) {
        Expect.notNull(value, "value");
        verifyMutable();
        if (this.isPitchStabilized != value) {
            boolean oldValue = this.isPitchStabilized;
            this.isPitchStabilized = value;
            notifyPropertyChanged(IS_PITCH_STABILIZED_PROPERTY, oldValue, value);
        }
    }

    @Override
    public void setIsYawStabilized(boolean value) {
        Expect.notNull(value, "value");
        verifyMutable();
        if (this.isYawStabilized != value) {
            boolean oldValue = this.isYawStabilized;
            this.isYawStabilized = value;
            notifyPropertyChanged(IS_YAW_STABILIZED_PROPERTY, oldValue, value);
        }
    }

    @Override
    public void setIsExposureTimeFixed(boolean value) {
        Expect.notNull(value, "value");
        verifyMutable();
        if (this.isExposureTimeFixed != value) {
            boolean oldValue = this.isExposureTimeFixed;
            this.isExposureTimeFixed = value;
            notifyPropertyChanged(IS_EXPOSURE_TIME_FIXED, oldValue, value);
        }
    }

    public boolean isExposureTimeFixed() {
        return isExposureTimeFixed;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (!(obj instanceof GenericCameraDescription)) {
            return false;
        }

        GenericCameraDescription other = (GenericCameraDescription)obj;
        return id.equals(other.id)
            && name.equals(other.name)
            && roll.equals(other.roll)
            && pitch.equals(other.pitch)
            && yaw.equals(other.yaw)
            && maxRoll.equals(other.maxRoll)
            && maxPitch.equals(other.maxPitch)
            && maxYaw.equals(other.maxYaw)
            && minRoll.equals(other.minRoll)
            && minPitch.equals(other.minPitch)
            && minYaw.equals(other.minYaw)
            && enforcePitchRange == other.enforcePitchRange
            && isRollFixed == other.isRollFixed
            && isPitchFixed == other.isPitchFixed
            && isYawFixed == other.isYawFixed
            && isRollStabilized == other.isRollStabilized
            && isPitchStabilized == other.isPitchStabilized
            && isYawStabilized == other.isYawStabilized
            && ccdResX == other.ccdResX
            && ccdResY == other.ccdResY
            && ccdWidth.equals(other.ccdWidth)
            && ccdHeight.equals(other.ccdHeight)
            && ccdXTransl.equals(other.ccdXTransl)
            && ccdYTransl.equals(other.ccdYTransl)
            && exposureTimeUS.equals(other.exposureTimeUS)
            && cameraDelay.equals(other.cameraDelay)
            && exifModels.equals(other.exifModels)
            && DoubleHelper.areClose(pictureSizeInMB, other.pictureSizeInMB)
            && DoubleHelper.areClose(sdCapacityInGB, other.sdCapacityInGB)
            && ensoCalibFile.equals(other.ensoCalibFile)
            && icarosCalibFile.equals(other.icarosCalibFile)
            && menciCalibFile.equals(other.menciCalibFile)
            && pix4dCalibFile.equals(other.pix4dCalibFile)
            && agisoftCalibFile.equals(other.agisoftCalibFile)
            && filterType == other.filterType
            && offsetToTail.equals(other.offsetToTail)
            && offsetToRightWing.equals(other.offsetToRightWing)
            && offsetToSky.equals(other.offsetToSky)
            && isProjectableToGround == other.isProjectableToGround
            && Arrays.equals(bandNames, other.bandNames)
            && providingFeedback == other.providingFeedback
            && compatibleLensIds.equals(other.compatibleLensIds)
            && isExposureTimeFixed == other.isExposureTimeFixed;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(
            id,
            name,
            roll,
            pitch,
            yaw,
            maxRoll,
            maxPitch,
            maxYaw,
            minRoll,
            minPitch,
            minYaw,
            enforcePitchRange,
            isRollFixed,
            isPitchFixed,
            isYawFixed,
            isRollStabilized,
            isPitchStabilized,
            isYawStabilized,
            ccdResX,
            ccdResY,
            ccdWidth,
            ccdHeight,
            ccdXTransl,
            ccdYTransl,
            exposureTimeUS,
            exifModels,
            pictureSizeInMB,
            sdCapacityInGB,
            ensoCalibFile,
            icarosCalibFile,
            menciCalibFile,
            pix4dCalibFile,
            agisoftCalibFile,
            filterType,
            offsetToTail,
            offsetToRightWing,
            offsetToSky,
            isProjectableToGround,
            bandNames,
            providingFeedback,
            compatibleLensIds,
            isExposureTimeFixed);
    }

    @Override
    public String toString() {
        return id;
    }

    private void verifyMutable() {
        if (immutable) {
            throw new IllegalStateException("The configuration is immutable and cannot be changed.");
        }
    }

}
