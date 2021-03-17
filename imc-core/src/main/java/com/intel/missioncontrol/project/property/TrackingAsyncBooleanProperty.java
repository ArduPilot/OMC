/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.property;

import org.asyncfx.beans.property.PropertyMetadata;
import org.asyncfx.beans.property.SimpleAsyncBooleanProperty;

public class TrackingAsyncBooleanProperty extends SimpleAsyncBooleanProperty implements TrackingAsyncProperty<Boolean> {

    private boolean cleanValue;

    public TrackingAsyncBooleanProperty(Object bean) {
        super(bean);
    }

    public TrackingAsyncBooleanProperty(Object bean, PropertyMetadata<Boolean> metadata) {
        super(bean, metadata);
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void merge(Boolean newValue, MergeStrategy strategy) {
        verifyAccess();

        boolean currentValue = get();

        if (currentValue == newValue) {
            strategy.updateValue(this, currentValue);
        } else {
            boolean oursChanged = currentValue != cleanValue;
            boolean theirsChanged = newValue != cleanValue;

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
    public void init(TrackingAsyncProperty<Boolean> newValue) {
        cleanValue = newValue.getCleanValue();
        set(newValue.getValue());
        get();
    }

    @Override
    public void init(Boolean newValue) {
        cleanValue = newValue;
        set(newValue);
        get();
    }

    @Override
    public void init(Boolean newValue, Boolean previousValue) {
        cleanValue = previousValue;
        set(newValue);
        get();
    }

    @Override
    public void clean() {
        cleanValue = get();
    }

    @Override
    public Boolean getCleanValue() {
        return cleanValue;
    }

    @Override
    public boolean isDirty() {
        return get() != cleanValue;
    }

}
