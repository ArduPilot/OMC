/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.custom;

import javafx.beans.property.SimpleObjectProperty;

import java.util.function.Supplier;

public class LazyDefaultObjectProperty<T> extends SimpleObjectProperty<T> {

    private Supplier<T> defaultValueProvider;

    public LazyDefaultObjectProperty(Supplier<T> defaultValueProvider) {
        this.defaultValueProvider = defaultValueProvider;
    }

    @Override
    public T get() {
        T value = super.get();
        if (value == null) {
            set(defaultValueProvider.get());
            return super.get();
        }

        return value;
    }
}
