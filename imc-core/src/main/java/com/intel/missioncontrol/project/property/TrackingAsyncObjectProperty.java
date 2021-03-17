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

    private T cleanValue;

    public TrackingAsyncObjectProperty(Object bean) {
        super(bean);
    }

    public TrackingAsyncObjectProperty(Object bean, PropertyMetadata<T> metadata) {
        super(bean, metadata);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void merge(T newValue, MergeStrategy strategy) {
        verifyAccess();

        T currentValue = get();

        if (currentValue instanceof Mergeable
                && currentValue instanceof Identifiable
                && newValue instanceof Identifiable
                && ((Identifiable)newValue).getId().equals(((Identifiable)currentValue).getId())) {
            ((Mergeable<T>)currentValue).merge(newValue, strategy);
        } else if (Objects.equals(currentValue, newValue)) {
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

    @SuppressWarnings("unchecked")
    public <S> void merge(S newValue, MergeStrategy strategy, Function<S, T> createItem) {
        verifyAccess();

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
    public void init(TrackingAsyncProperty<T> newValue) {
        cleanValue = newValue.getCleanValue();
        set(newValue.getValue());
        get();
    }

    @Override
    public void init(T newValue) {
        cleanValue = newValue;
        set(newValue);
        get();
    }

    @Override
    public void init(T newValue, T cleanValue) {
        this.cleanValue = cleanValue;
        set(newValue);
        get();
    }

    public void init(TrackingAsyncObjectProperty<T> newValue, Function<T, T> createItem) {
        cleanValue = newValue.cleanValue != null ? createItem.apply(newValue.cleanValue) : null;
        set(cleanValue);
        get();
    }

    public <S> void init(S newValue, Function<S, T> createItem) {
        cleanValue = newValue != null ? createItem.apply(newValue) : null;
        set(cleanValue);
        get();
    }

    @Override
    public void clean() {
        cleanValue = get();
    }

    @Override
    public T getCleanValue() {
        return cleanValue;
    }

    @Override
    public boolean isDirty() {
        T currentValue = get();

        if (currentValue != cleanValue) {
            return true;
        }

        if (currentValue instanceof Mergeable) {
            return ((Mergeable)currentValue).isDirty();
        }

        return false;
    }

}
