/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.measure.property;

import com.intel.missioncontrol.measure.UnitInfo;
import com.intel.missioncontrol.measure.VariantQuantity;
import org.checkerframework.checker.nullness.qual.Nullable;

public class SimpleVariantQuantityProperty extends VariantQuantityProperty {

    private static final Object DEFAULT_BEAN = null;
    private static final String DEFAULT_NAME = "";

    private final Object bean;
    private final String name;

    public SimpleVariantQuantityProperty(IQuantityStyleProvider quantityStyleProvider, UnitInfo<?>... unitInfo) {
        this(DEFAULT_BEAN, DEFAULT_NAME, quantityStyleProvider, unitInfo, null);
    }

    public SimpleVariantQuantityProperty(
            IQuantityStyleProvider quantityStyleProvider, UnitInfo<?>[] unitInfo, VariantQuantity initialValue) {
        this(DEFAULT_BEAN, DEFAULT_NAME, quantityStyleProvider, unitInfo, initialValue);
    }

    public SimpleVariantQuantityProperty(
            @Nullable Object bean,
            @Nullable String name,
            IQuantityStyleProvider quantityStyleProvider,
            UnitInfo<?>... unitInfo) {
        this(bean, name, quantityStyleProvider, unitInfo, null);
    }

    public SimpleVariantQuantityProperty(
            @Nullable Object bean,
            @Nullable String name,
            IQuantityStyleProvider quantityStyleProvider,
            UnitInfo<?>[] unitInfo,
            @Nullable VariantQuantity initialValue) {
        super(quantityStyleProvider, unitInfo, initialValue);
        this.bean = bean;
        this.name = (name == null) ? DEFAULT_NAME : name;
    }

    @Override
    public Object getBean() {
        return bean;
    }

    @Override
    public String getName() {
        return name;
    }

}
