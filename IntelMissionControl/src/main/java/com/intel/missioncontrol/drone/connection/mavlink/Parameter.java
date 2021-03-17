/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone.connection.mavlink;

import io.dronefleet.mavlink.common.MavParamType;
import io.dronefleet.mavlink.common.ParamValue;

public class Parameter implements IMavlinkParameter {
    private ParamValue paramValue;

    public Parameter(ParamValue paramValue) {
        this.paramValue = paramValue;
    }

    public static Parameter createInt8(String id, int value) {
        return createRaw(id, Float.intBitsToFloat(value), MavParamType.MAV_PARAM_TYPE_INT8);
    }

    public static Parameter createInt16(String id, int value) {
        return createRaw(id, Float.intBitsToFloat(value), MavParamType.MAV_PARAM_TYPE_INT16);
    }

    public static Parameter createInt32(String id, int value) {
        return createRaw(id, Float.intBitsToFloat(value), MavParamType.MAV_PARAM_TYPE_INT32);
    }

    public static Parameter createUInt8(String id, int value) {
        return createRaw(id, Float.intBitsToFloat(value), MavParamType.MAV_PARAM_TYPE_UINT8);
    }

    public static Parameter createUInt16(String id, int value) {
        return createRaw(id, Float.intBitsToFloat(value), MavParamType.MAV_PARAM_TYPE_UINT16);
    }

    public static Parameter createUInt32(String id, int value) {
        return createRaw(id, Float.intBitsToFloat(value), MavParamType.MAV_PARAM_TYPE_UINT32);
    }

    public static Parameter createFloat(String id, float value) {
        return createRaw(id, value, MavParamType.MAV_PARAM_TYPE_REAL32);
    }

    public static Parameter create(String id, double value, MavParamType mavParamType) {
        switch (mavParamType) {
        case MAV_PARAM_TYPE_UINT8:
            return createUInt8(id, (int)Math.round(value));
        case MAV_PARAM_TYPE_INT8:
            return createInt8(id, (int)Math.round(value));
        case MAV_PARAM_TYPE_UINT16:
            return createUInt16(id, (int)Math.round(value));
        case MAV_PARAM_TYPE_INT16:
            return createInt16(id, (int)Math.round(value));
        case MAV_PARAM_TYPE_UINT32:
            return createUInt32(id, (int)Math.round(value));
        case MAV_PARAM_TYPE_INT32:
            return createInt32(id, (int)Math.round(value));
        case MAV_PARAM_TYPE_REAL32:
            return createFloat(id, (float)value);
        default:
            throw new IllegalArgumentException("MavParamType " + mavParamType + " is unsupported");
        }
    }

    static Parameter createRaw(String id, float rawFloatValue, MavParamType mavParamType) {
        if (id.length() > 16) {
            throw new IllegalArgumentException("Mavlink parameter ID is longer than 16 characters: " + id);
        }

        return new Parameter(
            ParamValue.builder().paramId(id).paramType(mavParamType).paramValue(rawFloatValue).build());
    }

    public String getId() {
        return paramValue.paramId();
    }

    public int getIndex() {
        return paramValue.paramIndex();
    }

    int getParamCount() {
        return paramValue.paramCount();
    }

    MavParamType getType() {
        return paramValue.paramType().entry();
    }

    @Override
    public String toString() {
        String str = getId() + ": ";
        switch (paramValue.paramType().entry()) {
        case MAV_PARAM_TYPE_INT8:
        case MAV_PARAM_TYPE_INT16:
        case MAV_PARAM_TYPE_INT32:
        case MAV_PARAM_TYPE_UINT8:
        case MAV_PARAM_TYPE_UINT16:
        case MAV_PARAM_TYPE_UINT32:
            return str + getLongValue();
        default:
            return str + getFloatValue();
        }
    }

    public float getFloatValue() {
        float f = paramValue.paramValue();
        if (paramValue.paramType().entry() == MavParamType.MAV_PARAM_TYPE_REAL32) {
            return f;
        }

        throw new InvalidParamTypeException(this);
    }

    @Override
    public double getDoubleValue() {
        return getFloatValue();
    }

    @Override
    public String getStringValue() {
        throw new InvalidParamTypeException(this);
    }

    float getRawFloatValue() {
        return paramValue.paramValue();
    }

    public int getIntValue() {
        float f = paramValue.paramValue();
        int intBits = Float.floatToRawIntBits(f);

        switch (paramValue.paramType().entry()) {
        case MAV_PARAM_TYPE_INT8:
            return (int)(byte)(intBits & 0xFF);
        case MAV_PARAM_TYPE_INT16:
            return (int)(short)(intBits & 0xFFFF);
        case MAV_PARAM_TYPE_INT32:
            return intBits;
        case MAV_PARAM_TYPE_UINT8:
            return intBits & 0xFF;
        case MAV_PARAM_TYPE_UINT16:
            return intBits & 0xFFFF;
        default:
            throw new InvalidParamTypeException(this);
        }
    }

    public long getLongValue() {
        float f = paramValue.paramValue();
        int intBits = Float.floatToRawIntBits(f);

        switch (paramValue.paramType().entry()) {
        case MAV_PARAM_TYPE_INT8:
            return (int)(byte)(intBits & 0xFF);
        case MAV_PARAM_TYPE_INT16:
            return (int)(short)(intBits & 0xFFFF);
        case MAV_PARAM_TYPE_INT32:
            return intBits;
        case MAV_PARAM_TYPE_UINT8:
            return intBits & 0xFF;
        case MAV_PARAM_TYPE_UINT16:
            return intBits & 0xFFFF;
        case MAV_PARAM_TYPE_UINT32:
            return (long)intBits;
        default:
            throw new InvalidParamTypeException(this);
        }
    }
}
