/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.property;

import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import org.asyncfx.beans.property.PropertyMetadata;
import org.asyncfx.beans.property.SimpleAsyncSetProperty;
import org.asyncfx.collections.AsyncObservableList;
import org.asyncfx.collections.AsyncObservableSet;
import org.asyncfx.collections.FXAsyncCollections;
import org.asyncfx.collections.LockedCollection;
import org.asyncfx.collections.LockedSet;

public class TrackingAsyncSetProperty<T extends Identifiable> extends SimpleAsyncSetProperty<T>
        implements TrackingAsyncProperty<AsyncObservableSet<T>> {

    private final AsyncObservableSet<T> cleanValue = FXAsyncCollections.observableArraySet();

    public TrackingAsyncSetProperty(Object bean) {
        super(bean);
    }

    public TrackingAsyncSetProperty(Object bean, PropertyMetadata<AsyncObservableSet<T>> metadata) {
        super(bean, metadata);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void merge(AsyncObservableSet<T> newValue, MergeStrategy strategy) {
        merge(newValue, strategy, x -> x, (t, s) -> ((Mergeable)t).merge(s, strategy));
    }

    @SuppressWarnings("TryFinallyCanBeTryWithResources")
    public <S extends Identifiable> void merge(
            Collection<S> theirCollection,
            MergeStrategy strategy,
            Function<S, T> createItem,
            BiConsumer<T, S> mergeItem) {
        verifyAccess();

        LockedSet<T> cleanSet = cleanValue.lock();
        LockedSet<T> ourSet = get().lock();

        if (theirCollection instanceof AsyncObservableList) {
            theirCollection = ((AsyncObservableList<S>)theirCollection).lock();
        } else if (theirCollection instanceof AsyncObservableSet) {
            theirCollection = ((AsyncObservableSet<S>)theirCollection).lock();
        }

        try {
            List<Identifiable> theirRemovedItems = CollectionHelper.difference(cleanSet, theirCollection);
            List<Identifiable> ourRemovedItems = CollectionHelper.difference(cleanSet, ourSet);

            // Resolve conflicts where 'we' deleted an item, but 'they' modified it.
            for (Identifiable ourRemovedItem : ourRemovedItems) {
                S theirItem = CollectionHelper.getById(theirCollection, ourRemovedItem.getId());
                if (theirItem instanceof Mergeable && ((Mergeable)theirItem).isDirty()) {
                    strategy.resolveItemRemovedConflict(this, ourSet, theirItem, createItem);
                }
            }

            // Resolve conflicts where 'we' modified an item, but 'they' deleted it.
            for (Identifiable ourRemovedItem : theirRemovedItems) {
                T ourItem = CollectionHelper.getById(ourSet, ourRemovedItem.getId());
                if (ourItem instanceof Mergeable && ((Mergeable)ourItem).isDirty()) {
                    strategy.resolveItemRemovedConflict(this, ourSet, ourItem, null);
                } else {
                    strategy.removeItem(ourSet, ourItem);
                }
            }

            // Merge all of 'their' items into 'our' items.
            for (S theirItem : theirCollection) {
                T ourItem = CollectionHelper.getById(ourSet, theirItem.getId());
                if (ourItem == null) {
                    strategy.addItem(this, ourSet, theirItem, createItem);
                } else if (ourItem instanceof Mergeable) {
                    mergeItem.accept(ourItem, theirItem);
                }
            }
        } finally {
            if (theirCollection instanceof LockedCollection) {
                ((LockedCollection<S>)theirCollection).close();
            }

            if (ourSet != null) {
                ourSet.close();
            }

            if (cleanSet != null) {
                cleanSet.close();
            }
        }
    }

    @Override
    public void init(TrackingAsyncProperty<AsyncObservableSet<T>> newValue) {
        AsyncObservableSet<T> newSet = newValue.getValue();
        set(newSet);
        get();

        try (LockedSet<T> cleanSet = this.cleanValue.lock()) {
            cleanSet.clear();

            if (newSet != null) {
                try (LockedSet<T> set = newSet.lock()) {
                    cleanSet.addAll(set);
                }
            }
        }
    }

    @Override
    public void init(AsyncObservableSet<T> newValue) {
        init(newValue, newValue);
    }

    @Override
    public void init(AsyncObservableSet<T> newValue, AsyncObservableSet<T> cleanValue) {
        set(newValue);
        get();

        try (LockedSet<T> cleanSet = this.cleanValue.lock();
            LockedSet<T> newSet = cleanValue.lock()) {
            cleanSet.clear();
            cleanSet.addAll(newSet);
        }
    }

    @Override
    public void clean() {
        try (LockedSet<T> set = lock()) {
            cleanValue.clear();
            cleanValue.addAll(set);
        }
    }

    @Override
    public AsyncObservableSet<T> getCleanValue() {
        return cleanValue;
    }

    @Override
    public boolean isDirty() {
        try (LockedSet<T> set = lock();
            LockedSet<T> cleanSet = cleanValue.lock()) {
            if (cleanSet.size() != set.size()) {
                return true;
            }

            for (T item : set) {
                if (item instanceof Mergeable && ((Mergeable)item).isDirty()) {
                    return true;
                }
            }
        }

        return false;
    }

}
