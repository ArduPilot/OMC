/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.property;

import org.asyncfx.beans.property.PropertyMetadata;
import org.asyncfx.beans.property.SimpleAsyncDoubleProperty;

public class TrackingAsyncDoubleProperty extends SimpleAsyncDoubleProperty implements TrackingAsyncProperty<Number> {

    private double cleanValue;

    public TrackingAsyncDoubleProperty(Object bean) {
        super(bean);
    }

    public TrackingAsyncDoubleProperty(Object bean, PropertyMetadata<Number> metadata) {
        super(bean, metadata);
    }

    @Override
    public void merge(Number newValue, MergeStrategy strategy) {
        verifyAccess();

        double currentValue = get();

        if (currentValue == newValue.doubleValue()) {
            strategy.updateValue(this, currentValue);
        } else {
            boolean oursChanged = currentValue != cleanValue;
            boolean theirsChanged = newValue.doubleValue() != cleanValue;

            if (oursChanged && !theirsChanged) {
                strategy.updateValue(this, currentValue);
            } else if (!oursChanged && theirsChanged) {
                strategy.updateValue(this, newValue);
            } else if (oursChanged) {
                strategy.resolveValueConflict(this, currentValue, newValue);
            }
        }
    }

    @Override
    public void init(TrackingAsyncProperty<Number> newValue) {
        Number cleanValue = newValue.getCleanValue();
        this.cleanValue = cleanValue != null ? cleanValue.doubleValue() : 0;
        setValue(newValue.getValue());
        get();
    }

    @Override
    public void init(Number newValue) {
        cleanValue = newValue != null ? newValue.doubleValue() : 0;
        setValue(newValue);
        get();
    }

    @Override
    public void init(Number newValue, Number previousValue) {
        cleanValue = previousValue != null ? previousValue.doubleValue() : 0;
        setValue(newValue);
        get();
    }

    @Override
    public void clean() {
        cleanValue = get();
    }

    @Override
    public Double getCleanValue() {
        return cleanValue;
    }

    @Override
    public boolean isDirty() {
        return get() != cleanValue;
    }

}
