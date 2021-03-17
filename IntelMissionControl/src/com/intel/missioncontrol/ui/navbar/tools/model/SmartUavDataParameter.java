/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.tools.model;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.List;

/** Created by eivanchenko on 8/7/2017. */
public class SmartUavDataParameter<T> extends UavDataParameter<T> {

    private String fieldName;
    private Field field;
    private boolean inArray;
    private boolean inList;
    private int index;

    public SmartUavDataParameter(Field field) {
        this(field, 0);
    }

    public SmartUavDataParameter(Field field, int index) {
        this.fieldName = field == null ? "UNKNOWN" : field.getName();
        this.field = field;
        this.index = index;
        inArray = false;
        if (field != null) {
            Class<?> rawType = field.getType();
            inArray = rawType.isArray();
            inList = List.class.isAssignableFrom(rawType);
            setDisplayName(fieldName);
            if (inArray || inList) {
                rawType = rawType.getComponentType();
                setDisplayName(String.format("%s[%d]", getFieldName(), getChannel() + 1));
            }

            if (Double.class == rawType || Double.TYPE == rawType) {
                setType(UavDataParameterType.DOUBLE);
            } else if (Float.class == rawType || Float.TYPE == rawType) {
                setType(UavDataParameterType.FLOAT);
            }
        }
    }

    @Override
    public boolean isInArray() {
        return inArray;
    }

    @Override
    public boolean isInList() {
        return inList;
    }

    public final int getChannel() {
        return index;
    }

    public final String getFieldName() {
        return fieldName;
    }

    @Override
    protected Object extractRawValue(T valueContainer) throws Exception {
        Object rawValue = field.get(valueContainer);
        if (isInArray()) {
            rawValue = Array.get(rawValue, getChannel());
        } else if (isInList()) {
            rawValue = ((List)rawValue).get(getChannel());
        }

        return rawValue;
    }
}
