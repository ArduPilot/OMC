/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.beans.property;

import com.intel.missioncontrol.PublishSource;
import com.intel.missioncontrol.beans.binding.LifecycleValueConverter;
import com.intel.missioncontrol.beans.binding.ValueConverter;

@PublishSource(module = "openjfx", licenses = "intel-gpl-classpath-exception")
class ValueConverterAdapter<S, T> {

    private final ValueConverter<S, T> converter;
    private final LifecycleValueConverter<S, T> lifecycleConverter;

    ValueConverterAdapter(ValueConverter<S, T> converter) {
        this.converter = converter;
        this.lifecycleConverter = null;
    }

    ValueConverterAdapter(LifecycleValueConverter<S, T> converter) {
        this.converter = null;
        this.lifecycleConverter = converter;
    }

    @SuppressWarnings("ConstantConditions")
    T convert(S value) {
        return lifecycleConverter != null ? lifecycleConverter.convert(value) : converter.convert(value);
    }

    void update(S sourceValue, T targetValue) {
        if (lifecycleConverter != null) {
            lifecycleConverter.update(sourceValue, targetValue);
        }
    }

    void remove(T value) {
        if (lifecycleConverter != null) {
            lifecycleConverter.remove(value);
        }
    }

}
