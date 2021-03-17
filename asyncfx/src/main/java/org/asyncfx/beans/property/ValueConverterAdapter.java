/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.beans.property;

import org.asyncfx.PublishSource;
import org.asyncfx.beans.binding.LifecycleValueConverter;
import org.asyncfx.beans.binding.ValueConverter;

@PublishSource(module = "openjfx", licenses = "intel-gpl-classpath-exception")
class ValueConverterAdapter<S, T> implements ValueConverter<S, T>, LifecycleValueConverter<S, T> {

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

    @Override
    @SuppressWarnings("ConstantConditions")
    public T convert(S value) {
        return lifecycleConverter != null ? lifecycleConverter.convert(value) : converter.convert(value);
    }

    @Override
    public void update(S sourceValue, T targetValue) {
        if (lifecycleConverter != null) {
            lifecycleConverter.update(sourceValue, targetValue);
        }
    }

    @Override
    public void remove(T value) {
        if (lifecycleConverter != null) {
            lifecycleConverter.remove(value);
        }
    }

}
