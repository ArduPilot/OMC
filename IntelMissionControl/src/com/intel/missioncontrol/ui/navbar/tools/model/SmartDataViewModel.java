/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.tools.model;

import com.intel.missioncontrol.mission.Mission;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/** Created by eivanchenko on 8/7/2017. */
public abstract class SmartDataViewModel<T> extends AbstractUavDataViewModel<T> {

    public static final Integer DEFAULT_ARRAYED_FIELD_VALUES_COUNT = 3;

    private Class<T> dataClass;
    private ObjectProperty<T> property;

    public SmartDataViewModel(Mission mission, Class<T> dataClass) {
        this(mission, dataClass, new HashMap<>());
    }

    public SmartDataViewModel(Mission mission, Class<T> dataClass, Map<String, Integer> arrayedFields) {
        super(mission);
        this.dataClass = dataClass;
        this.property = new SimpleObjectProperty<>();
        init(arrayedFields);
    }

    private void init(Map<String, Integer> arrayedFields) {
        final ObservableList<UavDataParameter<T>> data = getData();
        Arrays.asList(dataClass.getFields())
            .stream()
            .flatMap(f -> makeDataParamFromField(f, arrayedFields))
            .forEach(data::add);
        property.addListener((observable, oldValue, newValue) -> update(newValue));
    }

    private Stream<SmartUavDataParameter<T>> makeDataParamFromField(Field field, Map<String, Integer> arrayedFields) {
        String fieldName = field.getName();
        Class<?> fClass = field.getType();
        ArrayList<SmartUavDataParameter<T>> params = new ArrayList<>();
        if (fClass.isArray() || Collection.class.isAssignableFrom(fClass)) {
            Integer count = arrayedFields.get(fieldName);
            if (count == null) {
                count = DEFAULT_ARRAYED_FIELD_VALUES_COUNT;
            }

            for (int i = 0; i < count; i++) {
                SmartUavDataParameter<T> parameter = new SmartUavDataParameter<>(field, i);
                params.add(parameter);
            }
        } else {
            params.add(new SmartUavDataParameter<>(field));
        }

        return params.stream();
    }

    @Override
    protected void releaseUavReferences() {
        property.unbind();
    }

    @Override
    protected void establishUavReferences() {
        property.bind(propertyToBind());
    }

    protected abstract ObjectProperty<T> propertyToBind();
}
