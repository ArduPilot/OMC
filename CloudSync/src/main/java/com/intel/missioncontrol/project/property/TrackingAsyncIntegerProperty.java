/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.property;

import org.asyncfx.beans.property.PropertyMetadata;
import org.asyncfx.beans.property.SimpleAsyncIntegerProperty;

public class TrackingAsyncIntegerProperty extends SimpleAsyncIntegerProperty implements TrackingAsyncProperty<Number> {

    private int previousValue;

    public TrackingAsyncIntegerProperty(Object bean) {
        super(bean);
    }

    public TrackingAsyncIntegerProperty(Object bean, PropertyMetadata<Number> metadata) {
        super(bean, metadata);
    }

    @Override
    public void merge(Number newValue, MergeStrategy strategy) {
        getMetadata().verifyAccess();

        int currentValue = get();

        if (currentValue == newValue.intValue()) {
            strategy.updateValue(this, currentValue);
        } else {
            boolean oursChanged = currentValue != previousValue;
            boolean theirsChanged = newValue.intValue() != previousValue;

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
        int currentValue = get();
        int newIntValue = newValue != null ? newValue.intValue() : 0;

        if (currentValue != newIntValue) {
            setValue(newValue);
        }

        previousValue = newIntValue;
    }

    @Override
    public boolean isDirty() {
        return get() != previousValue;
    }

}
