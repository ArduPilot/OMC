/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.hardware;

import com.google.common.base.Objects;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.intel.missioncontrol.NotificationObject;
import com.intel.missioncontrol.helper.Expect;
import com.intel.missioncontrol.measure.Dimension.Angle;
import com.intel.missioncontrol.measure.Dimension.Length;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.Unit;
import java.lang.reflect.Type;

class PayloadMountDescription extends NotificationObject implements IMutablePayloadMountDescription {

    public static class Deserializer implements JsonDeserializer<PayloadMountDescription> {
        @Override
        public PayloadMountDescription deserialize(JsonElement arg0, Type arg1, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject json = arg0.getAsJsonObject();
            JsonHelper helper = new JsonHelper(json);
            PayloadMountDescription description = new PayloadMountDescription();
            description.setRoll(Quantity.of(helper.getDouble("roll"), Unit.DEGREE));
            description.setPitch(Quantity.of(helper.getDouble("pitch"), Unit.DEGREE));
            description.setYaw(Quantity.of(helper.getDouble("yaw"), Unit.DEGREE));
            description.setMaxRoll(Quantity.of(helper.getDouble("maxRoll"), Unit.DEGREE));
            description.setMaxPitch(Quantity.of(helper.getDouble("maxPitch"), Unit.DEGREE));
            description.setMaxYaw(Quantity.of(helper.getDouble("maxYaw"), Unit.DEGREE));
            description.setMinRoll(Quantity.of(helper.getDouble("minRoll"), Unit.DEGREE));
            description.setMinPitch(Quantity.of(helper.getDouble("minPitch"), Unit.DEGREE));
            description.setMinYaw(Quantity.of(helper.getDouble("minYaw"), Unit.DEGREE));
            description.setOffsetToTail(Quantity.of(helper.getDouble("gpsOffsetToTailInM"), Unit.METER));
            description.setOffsetToRightWing(Quantity.of(helper.getDouble("gpsOffsetToRightWingInM"), Unit.METER));
            description.setOffsetToSky(Quantity.of(helper.getDouble("gpsOffsetToSkyInM"), Unit.METER));
            description.setIsRollFixed(helper.getBoolean("isRollFixed"));
            description.setIsPitchFixed(helper.getBoolean("isPitchFixed"));
            description.setIsYawFixed(helper.getBoolean("isYawFixed"));
            description.setIsRollStabilized(helper.getBoolean("isRollStabilized"));
            description.setIsPitchStabilized(helper.getBoolean("isPitchStabilized"));
            description.setIsYawStabilized(helper.getBoolean("isYawStabilized"));
            return description;
        }
    }

    private Quantity<Angle> roll = Quantity.of(0, Unit.RADIAN);
    private Quantity<Angle> pitch = Quantity.of(0, Unit.RADIAN);
    private Quantity<Angle> yaw = Quantity.of(0, Unit.RADIAN);
    private Quantity<Angle> maxRoll = Quantity.of(0, Unit.RADIAN);
    private Quantity<Angle> maxPitch = Quantity.of(0, Unit.RADIAN);
    private Quantity<Angle> maxYaw = Quantity.of(0, Unit.RADIAN);
    private Quantity<Angle> minRoll = Quantity.of(0, Unit.RADIAN);
    private Quantity<Angle> minPitch = Quantity.of(0, Unit.RADIAN);
    private Quantity<Angle> minYaw = Quantity.of(0, Unit.RADIAN);
    private Quantity<Length> offsetToTail = Quantity.of(0, Unit.METER);
    private Quantity<Length> offsetToRightWing = Quantity.of(0, Unit.METER);
    private Quantity<Length> offsetToSky = Quantity.of(0, Unit.METER);
    private boolean isRollFixed;
    private boolean isPitchFixed;
    private boolean isYawFixed;
    private boolean isRollStabilized;
    private boolean isPitchStabilized;
    private boolean isYawStabilized;

    @Override
    public void setRoll(Quantity<Angle> value) {
        Expect.notNull(value, "value");
        if (!this.roll.equals(value)) {
            Quantity<Angle> oldValue = this.roll;
            this.roll = value;
            notifyPropertyChanged(ROLL_PROPERTY, oldValue, value);
        }
    }

    @Override
    public Quantity<Angle> getRoll() {
        return roll;
    }

    @Override
    public Quantity<Angle> getPitch() {
        return pitch;
    }

    @Override
    public void setPitch(Quantity<Angle> value) {
        Expect.notNull(value, "value");
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
        if (!this.yaw.equals(value)) {
            Quantity<Angle> oldValue = this.yaw;
            this.yaw = value;
            notifyPropertyChanged(YAW_PROPERTY, oldValue, value);
        }
    }

    @Override
    public boolean isRollFixed() {
        return isRollFixed;
    }

    @Override
    public void setIsRollFixed(boolean value) {
        if (this.isRollFixed != value) {
            boolean oldValue = this.isRollFixed;
            this.isRollFixed = value;
            notifyPropertyChanged(IS_ROLL_FIXED_PROPERTY, oldValue, value);
        }
    }

    @Override
    public boolean isPitchFixed() {
        return isPitchFixed;
    }

    @Override
    public void setIsPitchFixed(boolean value) {
        if (this.isPitchFixed != value) {
            boolean oldValue = this.isPitchFixed;
            this.isPitchFixed = value;
            notifyPropertyChanged(IS_PITCH_FIXED_PROPERTY, oldValue, value);
        }
    }

    @Override
    public boolean isYawFixed() {
        return isYawFixed;
    }

    @Override
    public void setIsYawFixed(boolean value) {
        if (this.isYawFixed != value) {
            boolean oldValue = this.isYawFixed;
            this.isYawFixed = value;
            notifyPropertyChanged(IS_YAW_FIXED_PROPERTY, oldValue, value);
        }
    }

    @Override
    public boolean isRollStabilized() {
        return isRollStabilized;
    }

    @Override
    public void setIsRollStabilized(boolean value) {
        if (this.isRollStabilized != value) {
            boolean oldValue = this.isRollStabilized;
            this.isRollStabilized = value;
            notifyPropertyChanged(IS_ROLL_STABILIZED_PROPERTY, oldValue, value);
        }
    }

    @Override
    public boolean isPitchStabilized() {
        return isPitchStabilized;
    }

    @Override
    public void setIsPitchStabilized(boolean value) {
        if (this.isPitchStabilized != value) {
            boolean oldValue = this.isPitchStabilized;
            this.isPitchStabilized = value;
            notifyPropertyChanged(IS_PITCH_STABILIZED_PROPERTY, oldValue, value);
        }
    }

    @Override
    public boolean isYawStabilized() {
        return isYawStabilized;
    }

    @Override
    public void setIsYawStabilized(boolean value) {
        if (this.isYawStabilized != value) {
            boolean oldValue = this.isYawStabilized;
            this.isYawStabilized = value;
            notifyPropertyChanged(IS_YAW_STABILIZED_PROPERTY, oldValue, value);
        }
    }

    @Override
    public Quantity<Angle> getMaxRoll() {
        return maxRoll;
    }

    @Override
    public void setMaxRoll(Quantity<Angle> value) {
        Expect.notNull(value, "value");
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
        if (!this.minYaw.equals(value)) {
            Quantity<Angle> oldValue = this.minYaw;
            this.minYaw = value;
            notifyPropertyChanged(MIN_YAW_PROPERTY, oldValue, value);
        }
    }

    @Override
    public Quantity<Length> getOffsetToTail() {
        return offsetToTail;
    }

    @Override
    public void setOffsetToTail(Quantity<Length> value) {
        Expect.notNull(value, "value");
        if (!this.offsetToTail.equals(value)) {
            Quantity<Length> oldValue = this.offsetToTail;
            this.offsetToTail = value;
            notifyPropertyChanged(OFFSET_TO_TAIL_PROPERTY, oldValue, value);
        }
    }

    @Override
    public Quantity<Length> getOffsetToRightWing() {
        return offsetToRightWing;
    }

    @Override
    public void setOffsetToRightWing(Quantity<Length> value) {
        Expect.notNull(value, "value");
        if (!this.offsetToRightWing.equals(value)) {
            Quantity<Length> oldValue = this.offsetToRightWing;
            this.offsetToRightWing = value;
            notifyPropertyChanged(OFFSET_TO_RIGHT_WING_PROPERTY, oldValue, value);
        }
    }

    @Override
    public Quantity<Length> getOffsetToSky() {
        return offsetToSky;
    }

    @Override
    public void setOffsetToSky(Quantity<Length> value) {
        Expect.notNull(value, "value");
        if (!this.offsetToSky.equals(value)) {
            Quantity<Length> oldValue = this.offsetToSky;
            this.offsetToSky = value;
            notifyPropertyChanged(OFFSET_TO_SKY_PROPERTY, oldValue, value);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (!(obj instanceof PayloadMountDescription)) {
            return false;
        }

        PayloadMountDescription other = (PayloadMountDescription)obj;
        return roll.equals(other.roll)
            && pitch.equals(other.pitch)
            && yaw.equals(other.yaw)
            && maxRoll.equals(other.maxRoll)
            && maxPitch.equals(other.maxPitch)
            && maxYaw.equals(other.maxYaw)
            && minRoll.equals(other.minRoll)
            && minPitch.equals(other.minPitch)
            && minYaw.equals(other.minYaw)
            && offsetToTail.equals(other.offsetToTail)
            && offsetToRightWing.equals(other.offsetToRightWing)
            && offsetToSky.equals(other.offsetToSky)
            && isRollFixed == other.isRollFixed
            && isPitchFixed == other.isPitchFixed
            && isYawFixed == other.isYawFixed
            && isRollStabilized == other.isRollStabilized
            && isPitchStabilized == other.isPitchStabilized
            && isYawStabilized == other.isYawStabilized;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(
            roll,
            pitch,
            yaw,
            maxRoll,
            maxPitch,
            maxYaw,
            minRoll,
            minPitch,
            minYaw,
            offsetToTail,
            offsetToRightWing,
            offsetToSky,
            isRollFixed,
            isPitchFixed,
            isYawFixed,
            isRollStabilized,
            isPitchStabilized,
            isYawStabilized);
    }

}
