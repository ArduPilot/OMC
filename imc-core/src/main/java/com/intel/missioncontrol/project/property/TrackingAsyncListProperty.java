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
import org.asyncfx.beans.property.SimpleAsyncListProperty;
import org.asyncfx.collections.AsyncObservableList;
import org.asyncfx.collections.AsyncObservableSet;
import org.asyncfx.collections.FXAsyncCollections;
import org.asyncfx.collections.LockedCollection;
import org.asyncfx.collections.LockedList;

public class TrackingAsyncListProperty<T extends Identifiable> extends SimpleAsyncListProperty<T>
        implements TrackingAsyncProperty<AsyncObservableList<T>> {

    private final AsyncObservableList<T> cleanValue = FXAsyncCollections.observableArrayList();

    public TrackingAsyncListProperty(Object bean) {
        super(bean);
    }

    public TrackingAsyncListProperty(Object bean, PropertyMetadata<AsyncObservableList<T>> metadata) {
        super(bean, metadata);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void merge(AsyncObservableList<T> newValue, MergeStrategy strategy) {
        merge(newValue, strategy, x -> x, (t, s) -> ((Mergeable)t).merge(s, strategy));
    }

    @SuppressWarnings("TryFinallyCanBeTryWithResources")
    public <S extends Identifiable> void merge(
            Collection<S> theirCollection,
            MergeStrategy strategy,
            Function<S, T> createItem,
            BiConsumer<T, S> mergeItem) {
        verifyAccess();

        LockedList<T> cleanList = cleanValue.lock();
        LockedList<T> ourList = get().lock();

        if (theirCollection instanceof AsyncObservableList) {
            theirCollection = ((AsyncObservableList<S>)theirCollection).lock();
        } else if (theirCollection instanceof AsyncObservableSet) {
            theirCollection = ((AsyncObservableSet<S>)theirCollection).lock();
        }

        try {
            List<Identifiable> theirRemovedItems = CollectionHelper.difference(cleanList, theirCollection);
            List<Identifiable> ourRemovedItems = CollectionHelper.difference(cleanList, ourList);

            // Resolve conflicts where 'we' deleted an item, but 'they' modified it.
            for (Identifiable ourRemovedItem : ourRemovedItems) {
                S theirItem = CollectionHelper.getById(theirCollection, ourRemovedItem.getId());
                if (theirItem instanceof Mergeable && ((Mergeable)theirItem).isDirty()) {
                    strategy.resolveItemRemovedConflict(this, ourList, theirItem, createItem);
                }
            }

            // Resolve conflicts where 'we' modified an item, but 'they' deleted it.
            for (Identifiable theirRemovedItem : theirRemovedItems) {
                T ourItem = CollectionHelper.getById(ourList, theirRemovedItem.getId());
                if (ourItem instanceof Mergeable && ((Mergeable)ourItem).isDirty()) {
                    strategy.resolveItemRemovedConflict(this, ourList, ourItem, null);
                } else {
                    strategy.removeItem(ourList, ourItem);
                }
            }

            // Merge all of 'their' items into 'our' items.
            for (S theirItem : theirCollection) {
                T ourItem = CollectionHelper.getById(ourList, theirItem.getId());
                if (ourItem == null) {
                    strategy.addItem(this, ourList, theirItem, createItem);
                } else if (ourItem instanceof Mergeable) {
                    mergeItem.accept(ourItem, theirItem);
                }
            }
        } finally {
            if (theirCollection instanceof LockedCollection) {
                ((LockedCollection<S>)theirCollection).close();
            }

            if (ourList != null) {
                ourList.close();
            }

            if (cleanList != null) {
                cleanList.close();
            }
        }
    }

    @Override
    public void init(TrackingAsyncProperty<AsyncObservableList<T>> newValue) {
        AsyncObservableList<T> newList = newValue.getValue();
        set(newList);
        get();

        try (LockedList<T> cleanList = this.cleanValue.lock()) {
            cleanList.clear();

            if (newList != null) {
                try (LockedList<T> list = newList.lock()) {
                    cleanList.addAll(list);
                }
            }
        }
    }

    @Override
    public void init(AsyncObservableList<T> newValue) {
        init(newValue, newValue);
    }

    @Override
    public void init(AsyncObservableList<T> newValue, AsyncObservableList<T> cleanValue) {
        set(newValue);
        get();

        try (LockedList<T> cleanList = this.cleanValue.lock();
            LockedList<T> newList = cleanValue.lock()) {
            cleanList.clear();
            cleanList.addAll(newList);
        }
    }

    @Override
    public void clean() {
        try (LockedList<T> list = lock()) {
            cleanValue.setAll(list);
        }
    }

    @Override
    public AsyncObservableList<T> getCleanValue() {
        return cleanValue;
    }

    @Override
    public boolean isDirty() {
        try (LockedList<T> list = lock();
            LockedList<T> cleanList = cleanValue.lock()) {
            if (cleanList.size() != list.size()) {
                return true;
            }

            for (T item : list) {
                if (item instanceof Mergeable && ((Mergeable)item).isDirty()) {
                    return true;
                }
            }
        }

        return false;
    }

}
