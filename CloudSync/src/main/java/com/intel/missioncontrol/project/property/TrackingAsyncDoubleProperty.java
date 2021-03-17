/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.property;

import org.asyncfx.beans.property.PropertyMetadata;
import org.asyncfx.beans.property.SimpleAsyncDoubleProperty;

public class TrackingAsyncDoubleProperty extends SimpleAsyncDoubleProperty implements TrackingAsyncProperty<Number> {

    private double previousValue;

    public TrackingAsyncDoubleProperty(Object bean) {
        super(bean);
    }

    public TrackingAsyncDoubleProperty(Object bean, PropertyMetadata<Number> metadata) {
        super(bean, metadata);
    }

    @Override
    public void merge(Number newValue, MergeStrategy strategy) {
        getMetadata().verifyAccess();

        double currentValue = get();

        if (currentValue == newValue.doubleValue()) {
            strategy.updateValue(this, currentValue);
        } else {
            boolean oursChanged = currentValue != previousValue;
            boolean theirsChanged = newValue.doubleValue() != previousValue;

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
    public void update(Number newValue) {
        double currentValue = get();
        double newDoubleValue = newValue != null ? newValue.doubleValue() : 0;

        if (currentValue != newDoubleValue) {
            setValue(newValue);
        }

        previousValue = newDoubleValue;
    }

    @Override
    public boolean isDirty() {
        return get() != previousValue;
    }

}
