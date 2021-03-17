/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.property;

import org.asyncfx.beans.property.PropertyMetadata;
import org.asyncfx.beans.property.SimpleAsyncFloatProperty;

public class TrackingAsyncFloatProperty extends SimpleAsyncFloatProperty implements TrackingAsyncProperty<Number> {

    private float previousValue;

    public TrackingAsyncFloatProperty(Object bean) {
        super(bean);
    }

    public TrackingAsyncFloatProperty(Object bean, PropertyMetadata<Number> metadata) {
        super(bean, metadata);
    }

    @Override
    public void merge(Number newValue, MergeStrategy strategy) {
        getMetadata().verifyAccess();

        float currentValue = get();

        if (currentValue == newValue.floatValue()) {
            strategy.updateValue(this, currentValue);
        } else {
            boolean oursChanged = currentValue != previousValue;
            boolean theirsChanged = newValue.floatValue() != previousValue;

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
        float currentValue = get();
        float newFloatValue = newValue != null ? newValue.floatValue() : 0;

        if (currentValue != newFloatValue) {
            setValue(newValue);
        }

        previousValue = newFloatValue;
    }

    @Override
    public boolean isDirty() {
        return get() != previousValue;
    }

}
