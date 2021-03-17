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

    private String previousValue;

    public TrackingAsyncStringProperty(Object bean) {
        super(bean);
    }

    public TrackingAsyncStringProperty(Object bean, PropertyMetadata<String> metadata) {
        super(bean, metadata);
    }

    @Override
    public void merge(String newValue, MergeStrategy strategy) {
        getMetadata().verifyAccess();

        String currentValue = get();

        if (Objects.equals(currentValue, newValue)) {
            strategy.updateValue(this, currentValue);
        } else {
            boolean oursChanged = !Objects.equals(currentValue, previousValue);
            boolean theirsChanged = !Objects.equals(newValue, previousValue);

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
    public void update(String newValue) {
        String currentValue = get();

        if (!Objects.equals(currentValue, newValue)) {
            setValue(newValue);
        }

        previousValue = newValue;
    }

    @Override
    public boolean isDirty() {
        return !Objects.equals(get(), previousValue);
    }

}
