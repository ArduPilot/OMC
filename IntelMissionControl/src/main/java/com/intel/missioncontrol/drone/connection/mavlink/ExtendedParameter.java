/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone.connection.mavlink;

import static io.dronefleet.mavlink.common.MavParamExtType.MAV_PARAM_EXT_TYPE_REAL32;

import io.dronefleet.mavlink.common.MavParamExtType;
import io.dronefleet.mavlink.common.ParamExtValue;

public class ExtendedParameter implements IMavlinkParameter {
    private ParamExtValue paramExtValue;

    public ExtendedParameter(ParamExtValue paramExtValue) {
        this.paramExtValue = paramExtValue;
    }

    public static ExtendedParameter createInt8(String id, int value) {
        return createRaw(id, longToParamString(value), MavParamExtType.MAV_PARAM_EXT_TYPE_INT8);
    }

    public static ExtendedParameter createInt16(String id, int value) {
        return createRaw(id, longToParamString(value), MavParamExtType.MAV_PARAM_EXT_TYPE_INT16);
    }

    public static ExtendedParameter createInt32(String id, int value) {
        return createRaw(id, longToParamString(value), MavParamExtType.MAV_PARAM_EXT_TYPE_INT32);
    }

    public static ExtendedParameter createUInt8(String id, int value) {
        return createRaw(id, longToParamString(value), MavParamExtType.MAV_PARAM_EXT_TYPE_UINT8);
    }

    public static ExtendedParameter createUInt16(String id, int value) {
        return createRaw(id, longToParamString(value), MavParamExtType.MAV_PARAM_EXT_TYPE_UINT16);
    }

    public static ExtendedParameter createUInt32(String id, long value) {
        return createRaw(id, longToParamString(value), MavParamExtType.MAV_PARAM_EXT_TYPE_UINT32);
    }

    public static ExtendedParameter createFloat(String id, float value) {
        return createRaw(id, doubleToParamString(value), MAV_PARAM_EXT_TYPE_REAL32);
    }

    public static ExtendedParameter createDouble(String id, double value) {
        return createRaw(id, doubleToParamString(value), MavParamExtType.MAV_PARAM_EXT_TYPE_REAL64);
    }

    public static ExtendedParameter createString(String id, String value) {
        return createRaw(id, value, MavParamExtType.MAV_PARAM_EXT_TYPE_CUSTOM);
    }

    static ExtendedParameter createRaw(String id, String rawStringValue, MavParamExtType mavParamExtType) {
        if (id.length() > 16) {
            throw new IllegalArgumentException("Mavlink parameter ID is longer than 16 characters: " + id);
        }

        if (rawStringValue.length() > 128) {
            throw new IllegalArgumentException(
                "Extended Parameter value length is longer than 128 characters: " + rawStringValue);
        }

        return new ExtendedParameter(
            ParamExtValue.builder().paramId(id).paramType(mavParamExtType).paramValue(rawStringValue).build());
    }

    @Override
    public String getId() {
        return paramExtValue.paramId();
    }

    public int getIndex() {
        return paramExtValue.paramIndex();
    }

    int getParamCount() {
        return paramExtValue.paramCount();
    }

    MavParamExtType getType() {
        return paramExtValue.paramType().entry();
    }

    @Override
    public String toString() {
        return getId() + ": " + paramExtValue.paramValue();
    }

    String getRawStringValue() {
        return paramExtValue.paramValue();
    }

    @Override
    public String getStringValue() {
        String s = paramExtValue.paramValue();
        if (paramExtValue.paramType().entry() == MavParamExtType.MAV_PARAM_EXT_TYPE_CUSTOM) {
            return s;
        }

        throw new InvalidParamTypeException(this);
    }

    @Override
    public int getIntValue() {
        switch (paramExtValue.paramType().entry()) {
        case MAV_PARAM_EXT_TYPE_INT8:
        case MAV_PARAM_EXT_TYPE_UINT8:
        case MAV_PARAM_EXT_TYPE_INT16:
        case MAV_PARAM_EXT_TYPE_UINT16:
        case MAV_PARAM_EXT_TYPE_INT32:
            return Integer.parseInt(paramExtValue.paramValue());
        default:
            throw new InvalidParamTypeException(this);
        }
    }

    @Override
    public long getLongValue() {
        switch (paramExtValue.paramType().entry()) {
        case MAV_PARAM_EXT_TYPE_INT8:
        case MAV_PARAM_EXT_TYPE_UINT8:
        case MAV_PARAM_EXT_TYPE_INT16:
        case MAV_PARAM_EXT_TYPE_UINT16:
        case MAV_PARAM_EXT_TYPE_INT32:
            return Long.parseLong(paramExtValue.paramValue());
        default:
            throw new InvalidParamTypeException(this);
        }
    }

    @Override
    public float getFloatValue() {
        if (paramExtValue.paramType().entry() == MAV_PARAM_EXT_TYPE_REAL32) {
            return Float.parseFloat(paramExtValue.paramValue());
        }

        throw new InvalidParamTypeException(this);
    }

    @Override
    public double getDoubleValue() {
        switch (paramExtValue.paramType().entry()) {
        case MAV_PARAM_EXT_TYPE_REAL32:
            return getFloatValue();
        case MAV_PARAM_EXT_TYPE_REAL64:
            return Double.parseDouble(paramExtValue.paramValue());
        }

        throw new InvalidParamTypeException(this);
    }

    private static String longToParamString(long value) {
        return Long.toString(value);
    }

    private static String doubleToParamString(double value) {
        return Double.toString(value);
    }

}
