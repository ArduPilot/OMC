/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.property;

import java.util.Objects;
import java.util.function.Function;
import org.asyncfx.beans.property.PropertyMetadata;
import org.asyncfx.beans.property.SimpleAsyncObjectProperty;

public class TrackingAsyncObjectProperty<T> extends SimpleAsyncObjectProperty<T> implements TrackingAsyncProperty<T> {

    private T previousValue;

    public TrackingAsyncObjectProperty(Object bean) {
        super(bean);
    }

    public TrackingAsyncObjectProperty(Object bean, PropertyMetadata<T> metadata) {
        super(bean, metadata);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void merge(T newValue, MergeStrategy strategy) {
        getMetadata().verifyAccess();

        T currentValue = get();

        if (currentValue instanceof Mergeable
                && currentValue instanceof Identifiable
                && newValue instanceof Identifiable
                && ((Identifiable)newValue).getId().equals(((Identifiable)currentValue).getId())) {
            ((Mergeable<T>)currentValue).merge(newValue, strategy);
        } else if (Objects.equals(currentValue, newValue)) {
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

    @SuppressWarnings("unchecked")
    public <S> void merge(S newValue, MergeStrategy strategy, Function<S, T> createItem) {
        getMetadata().verifyAccess();

        T currentValue = get();

        if (currentValue instanceof Mergeable
                && currentValue instanceof Identifiable
                && newValue instanceof Identifiable
                && ((Identifiable)newValue).getId().equals(((Identifiable)currentValue).getId())) {
            ((Mergeable<S>)currentValue).merge(newValue, strategy);
        } else {
            merge(createItem.apply(newValue), strategy);
        }
    }

    @Override
    public void update(T newValue) {
        T currentValue = get();

        if (!Objects.equals(currentValue, newValue)) {
            setValue(newValue);
        }

        previousValue = newValue;
    }

    public <S> void update(S newValue, Function<S, T> createItem) {
        T currentValue = get();

        if (!Objects.equals(currentValue, newValue)) {
            setValue(createItem.apply(newValue));
        }

        previousValue = createItem.apply(newValue);
    }

    @Override
    public boolean isDirty() {
        return !Objects.equals(get(), previousValue);
    }

}
