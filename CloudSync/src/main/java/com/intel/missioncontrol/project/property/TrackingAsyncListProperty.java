/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.property;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
import org.asyncfx.beans.property.PropertyMetadata;
import org.asyncfx.beans.property.SimpleAsyncListProperty;
import org.asyncfx.collections.AsyncObservableList;
import org.asyncfx.collections.LockedList;

public class TrackingAsyncListProperty<T extends Identifiable> extends SimpleAsyncListProperty<T>
        implements TrackingAsyncProperty<AsyncObservableList<T>> {

    private static final Function<UUID, UUID> IDENTITY = x -> x;

    private final List<UUID> previousValue = new ArrayList<>();

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
            List<S> theirList, MergeStrategy strategy, Function<S, T> createItem, BiConsumer<T, S> mergeItem) {
        getMetadata().verifyAccess();

        LockedList<T> ourList = get().lock();

        if (theirList instanceof AsyncObservableList) {
            theirList = ((AsyncObservableList<S>)theirList).lock();
        }

        try {
            List<UUID> theirAddedIds =
                CollectionHelper.difference(
                    theirList, previousValue, (l, r) -> r.contains(l.getId()), Identifiable::getId);

            List<UUID> ourRemovedIds =
                CollectionHelper.difference(
                    previousValue, ourList, (l, r) -> CollectionHelper.containsId(r, l), IDENTITY);

            List<UUID> theirRemovedIds =
                CollectionHelper.difference(
                    previousValue, theirList, (l, r) -> CollectionHelper.containsId(r, l), IDENTITY);

            // Resolve conflicts where 'we' deleted an item, but 'they' modified it.
            for (UUID id : ourRemovedIds) {
                S theirItem = CollectionHelper.getById(theirList, id);
                if (theirItem instanceof Mergeable && ((Mergeable)theirItem).isDirty()) {
                    strategy.resolveItemRemovedConflict(this, ourList, theirItem, createItem);
                }
            }

            // Resolve conflicts where 'we' modified an item, but 'they' deleted it.
            for (UUID id : theirRemovedIds) {
                T ourItem = CollectionHelper.getById(ourList, id);
                if (ourItem instanceof Mergeable && ((Mergeable)ourItem).isDirty()) {
                    strategy.resolveItemRemovedConflict(this, ourList, ourItem, null);
                } else {
                    strategy.removeItem(ourList, ourItem);
                }
            }

            // Merge all of 'their' items into 'our' items.
            for (S theirItem : theirList) {
                T ourItem = CollectionHelper.getById(ourList, theirItem.getId());
                if (ourItem instanceof Mergeable) {
                    mergeItem.accept(ourItem, theirItem);
                }
            }

            // Add all of 'their' new items to 'our' list.
            for (UUID id : theirAddedIds) {
                S theirItem = CollectionHelper.getById(theirList, id);
                strategy.addItem(this, ourList, theirItem, createItem);
            }
        } finally {
            if (theirList instanceof LockedList) {
                ((LockedList<S>)theirList).close();
            }

            if (ourList != null) {
                ourList.close();
            }
        }
    }

    @Override
    public void update(AsyncObservableList<T> newValue) {
        setValue(newValue);
        previousValue.clear();

        try (LockedList<T> list = newValue.lock()) {
            for (T item : list) {
                previousValue.add(item.getId());
            }
        }
    }

    @Override
    public boolean isDirty() {
        try (LockedList<T> list = lock()) {
            for (T item : list) {
                if (item instanceof Mergeable && ((Mergeable)item).isDirty()) {
                    return true;
                }
            }
        }

        return false;
    }

}
