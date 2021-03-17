/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.property;

import org.asyncfx.beans.property.PropertyMetadata;
import org.asyncfx.beans.property.SimpleAsyncLongProperty;

public class TrackingAsyncLongProperty extends SimpleAsyncLongProperty implements TrackingAsyncProperty<Number> {

    private long cleanValue;

    public TrackingAsyncLongProperty(Object bean) {
        super(bean);
    }

    public TrackingAsyncLongProperty(Object bean, PropertyMetadata<Number> metadata) {
        super(bean, metadata);
    }

    @Override
    public void merge(Number newValue, MergeStrategy strategy) {
        verifyAccess();

        long currentValue = get();

        if (currentValue == newValue.longValue()) {
            strategy.updateValue(this, currentValue);
        } else {
            boolean oursChanged = currentValue != cleanValue;
            boolean theirsChanged = newValue.longValue() != cleanValue;

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
        this.cleanValue = cleanValue != null ? cleanValue.longValue() : 0;
        setValue(newValue.getValue());
        get();
    }

    @Override
    public void init(Number newValue) {
        cleanValue = newValue != null ? newValue.longValue() : 0;
        setValue(newValue);
        get();
    }

    @Override
    public void init(Number newValue, Number previousValue) {
        cleanValue = previousValue != null ? previousValue.longValue() : 0;
        setValue(newValue);
        get();
    }

    @Override
    public void clean() {
        cleanValue = get();
    }

    @Override
    public Long getCleanValue() {
        return cleanValue;
    }

    @Override
    public boolean isDirty() {
        return get() != cleanValue;
    }

}
