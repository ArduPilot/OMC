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
import com.intel.missioncontrol.measure.Dimension.Length;
import com.intel.missioncontrol.measure.Dimension.Time;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.Unit;
import eu.mavinci.core.flightplan.camera.LensTypes;
import java.lang.reflect.Type;

class LensDescription extends NotificationObject implements IMutableLensDescription {

    static final LensDescription NONE;

    static {
        NONE = new LensDescription();
        NONE.setId("NONE");
        NONE.setName("NONE");
    }

    public static class Deserializer implements JsonDeserializer<LensDescription> {
        @Override
        public LensDescription deserialize(JsonElement arg0, Type arg1, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject json = arg0.getAsJsonObject();
            JsonHelper helper = new JsonHelper(json);
            LensDescription description = new LensDescription();
            description.setId(helper.getString("id"));
            description.setName(helper.getString("name"));
            description.setFocalLength(Quantity.of(helper.getDouble("focalLength"), Unit.MILLIMETER));
            description.setMaxTimeVariation(Quantity.of(helper.getDouble("maxTimeVariation"), Unit.SECOND));
            description.setIsLensManual(helper.getBoolean("isManual"));
            description.setLensType(LensTypes.valueOf(helper.getString("lensType")));
            description.setMinRepTime(Quantity.of(helper.getDouble("minRepTime"), Unit.SECOND));
            return description;
        }
    }

    private String id = "";
    private String name = "";
    private Quantity<Length> focalLength = Quantity.of(0, Unit.MILLIMETER);
    private Quantity<Time> maxTimeVariation = Quantity.of(0, Unit.SECOND);
    private boolean isLensManual;
    private LensTypes lensType = LensTypes.STANDARD;
    private Quantity<Time> minRepTime = Quantity.of(0, Unit.SECOND);

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        Expect.notNull(id, "id");
        if (!this.id.equals(id)) {
            String oldValue = this.id;
            this.id = id;
            notifyPropertyChanged(ID_PROPERTY, oldValue, id);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        Expect.notNull(name, "name");
        if (!this.name.equals(name)) {
            String oldValue = this.name;
            this.name = name;
            notifyPropertyChanged(NAME_PROPERTY, oldValue, name);
        }
    }

    @Override
    public Quantity<Time> getMaxTimeVariation() {
        return maxTimeVariation;
    }

    @Override
    public void setMaxTimeVariation(Quantity<Time> maxTimeVariation) {
        Expect.notNull(maxTimeVariation, "maxTimeVariation");
        if (!this.maxTimeVariation.equals(maxTimeVariation)) {
            Quantity<Time> oldValue = this.maxTimeVariation;
            this.maxTimeVariation = maxTimeVariation;
            notifyPropertyChanged(MAX_TIME_VARIATION_PROPERTY, oldValue, maxTimeVariation);
        }
    }

    @Override
    public boolean isLensManual() {
        return isLensManual;
    }

    @Override
    public void setIsLensManual(boolean isLensManual) {
        if (this.isLensManual != isLensManual) {
            boolean oldValue = this.isLensManual;
            this.isLensManual = isLensManual;
            notifyPropertyChanged(IS_LENS_MANUAL_PROPERTY, oldValue, isLensManual);
        }
    }

    @Override
    public LensTypes getLensType() {
        return lensType;
    }

    @Override
    public void setLensType(LensTypes lensType) {
        Expect.notNull(lensType, "lensType");
        if (this.lensType != lensType) {
            LensTypes oldValue = this.lensType;
            this.lensType = lensType;
            notifyPropertyChanged(LENS_TYPE_PROPERTY, oldValue, lensType);
        }
    }

    @Override
    public Quantity<Length> getFocalLength() {
        return focalLength;
    }

    @Override
    public void setFocalLength(Quantity<Length> focalLength) {
        Expect.notNull(focalLength, "focalLength");
        if (!this.focalLength.equals(focalLength)) {
            Quantity<Length> oldValue = this.focalLength;
            this.focalLength = focalLength;
            notifyPropertyChanged(FOCAL_LENGTH_PROPERTY, oldValue, focalLength);
        }
    }

    @Override
    public Quantity<Time> getMinRepTime() {
        return minRepTime;
    }

    @Override
    public void setMinRepTime(Quantity<Time> minRepTime) {
        Expect.notNull(minRepTime, "minRepTime");
        if (!this.minRepTime.equals(minRepTime)) {
            Quantity<Time> oldValue = this.minRepTime;
            this.minRepTime = minRepTime;
            notifyPropertyChanged(MIN_REP_TIME_PROPERTY, oldValue, minRepTime);
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

        if (!(obj instanceof LensDescription)) {
            return false;
        }

        LensDescription other = (LensDescription)obj;
        return id.equals(other.id)
            && name.equals(other.name)
            && focalLength.equals(other.focalLength)
            && maxTimeVariation.equals(other.maxTimeVariation)
            && isLensManual == other.isLensManual
            && lensType == other.lensType
            && minRepTime.equals(other.minRepTime);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, name, focalLength, maxTimeVariation, isLensManual, lensType, minRepTime);
    }

    @Override
    public String toString() {
        return id;
    }
}
