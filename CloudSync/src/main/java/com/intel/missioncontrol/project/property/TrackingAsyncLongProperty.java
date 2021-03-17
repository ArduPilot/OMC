/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.property;

import org.asyncfx.beans.property.PropertyMetadata;
import org.asyncfx.beans.property.SimpleAsyncLongProperty;

public class TrackingAsyncLongProperty extends SimpleAsyncLongProperty implements TrackingAsyncProperty<Number> {

    private long previousValue;

    public TrackingAsyncLongProperty(Object bean) {
        super(bean);
    }

    public TrackingAsyncLongProperty(Object bean, PropertyMetadata<Number> metadata) {
        super(bean, metadata);
    }

    @Override
    public void merge(Number newValue, MergeStrategy strategy) {
        getMetadata().verifyAccess();

        long currentValue = get();

        if (currentValue == newValue.longValue()) {
            strategy.updateValue(this, currentValue);
        } else {
            boolean oursChanged = currentValue != previousValue;
            boolean theirsChanged = newValue.longValue() != previousValue;

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
        long currentValue = get();
        long newLongValue = newValue != null ? newValue.longValue() : 0;

        if (currentValue != newLongValue) {
            setValue(newValue);
        }

        previousValue = newLongValue;
    }

    @Override
    public boolean isDirty() {
        return get() != previousValue;
    }

}
