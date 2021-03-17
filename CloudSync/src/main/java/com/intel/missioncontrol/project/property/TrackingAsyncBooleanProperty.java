/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.property;

import org.asyncfx.beans.property.PropertyMetadata;
import org.asyncfx.beans.property.SimpleAsyncBooleanProperty;

public class TrackingAsyncBooleanProperty extends SimpleAsyncBooleanProperty implements TrackingAsyncProperty<Boolean> {

    private boolean previousValue;

    public TrackingAsyncBooleanProperty(Object bean) {
        super(bean);
    }

    public TrackingAsyncBooleanProperty(Object bean, PropertyMetadata<Boolean> metadata) {
        super(bean, metadata);
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void merge(Boolean newValue, MergeStrategy strategy) {
        getMetadata().verifyAccess();

        boolean currentValue = get();

        if (currentValue == newValue) {
            strategy.updateValue(this, currentValue);
        } else {
            boolean oursChanged = currentValue != previousValue;
            boolean theirsChanged = newValue != previousValue;

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
    public void update(Boolean newValue) {
        boolean currentValue = get();

        if (currentValue != newValue) {
            setValue(newValue);
        }

        previousValue = newValue;
    }

    @Override
    public boolean isDirty() {
        return get() != previousValue;
    }

}
