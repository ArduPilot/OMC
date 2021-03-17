/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.property;

import java.util.Objects;
import org.asyncfx.beans.property.PropertyMetadata;
import org.asyncfx.beans.property.SimpleAsyncStringProperty;

public class TrackingAsyncStringProperty extends SimpleAsyncStringProperty implements TrackingAsyncProperty<String> {

    private String cleanValue;

    public TrackingAsyncStringProperty(Object bean) {
        super(bean);
    }

    public TrackingAsyncStringProperty(Object bean, PropertyMetadata<String> metadata) {
        super(bean, metadata);
    }

    @Override
    public void merge(String newValue, MergeStrategy strategy) {
        verifyAccess();

        String currentValue = get();

        if (Objects.equals(currentValue, newValue)) {
            strategy.updateValue(this, currentValue);
        } else {
            boolean oursChanged = !Objects.equals(currentValue, cleanValue);
            boolean theirsChanged = !Objects.equals(newValue, cleanValue);

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
    public void init(TrackingAsyncProperty<String> newValue) {
        cleanValue = newValue.getCleanValue();
        set(newValue.getValue());
        get();
    }

    @Override
    public void init(String newValue) {
        cleanValue = newValue;
        setValue(newValue);
        get();
    }

    @Override
    public void init(String newValue, String previousValue) {
        cleanValue = previousValue;
        set(newValue);
        get();
    }

    @Override
    public void clean() {
        cleanValue = get();
    }

    @Override
    public String getCleanValue() {
        return cleanValue;
    }

    @Override
    public boolean isDirty() {
        return !Objects.equals(get(), cleanValue);
    }

}
