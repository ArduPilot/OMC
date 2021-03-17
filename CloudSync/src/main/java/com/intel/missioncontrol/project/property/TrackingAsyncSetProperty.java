/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.property;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
import org.asyncfx.beans.property.PropertyMetadata;
import org.asyncfx.beans.property.SimpleAsyncSetProperty;
import org.asyncfx.collections.AsyncObservableSet;
import org.asyncfx.collections.LockedSet;

public class TrackingAsyncSetProperty<T extends Identifiable> extends SimpleAsyncSetProperty<T>
        implements TrackingAsyncProperty<AsyncObservableSet<T>> {

    private static final Function<UUID, UUID> IDENTITY = x -> x;

    private final List<UUID> previousValue = new ArrayList<>();

    public TrackingAsyncSetProperty(Object bean) {
        super(bean);
    }

    public TrackingAsyncSetProperty(Object bean, PropertyMetadata<AsyncObservableSet<T>> metadata) {
        super(bean, metadata);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void merge(AsyncObservableSet<T> newValue, MergeStrategy strategy) {
        try (LockedSet<T> set = newValue.lock()) {
            merge(set, strategy, x -> x, (t, s) -> ((Mergeable)t).merge(s, strategy));
        }
    }

    public <S extends Identifiable> void merge(
            Set<S> theirList, MergeStrategy strategy, Function<S, T> createItem, BiConsumer<T, S> mergeItem) {
        getMetadata().verifyAccess();

        try (LockedSet<T> ourList = get().lock()) {
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

            // Merge all of 'their' items into 'our' set.
            for (S theirItem : theirList) {
                T ourItem = CollectionHelper.getById(ourList, theirItem.getId());
                if (ourItem instanceof Mergeable) {
                    mergeItem.accept(ourItem, theirItem);
                }
            }

            // Add all of 'their' new items to 'our' set.
            for (UUID id : theirAddedIds) {
                S theirItem = CollectionHelper.getById(theirList, id);
                strategy.addItem(this, ourList, theirItem, createItem);
            }
        }
    }

    @Override
    public void update(AsyncObservableSet<T> newValue) {
        setValue(newValue);
        previousValue.clear();

        try (LockedSet<T> set = newValue.lock()) {
            for (T item : set) {
                previousValue.add(item.getId());
            }
        }
    }

    @Override
    public boolean isDirty() {
        try (LockedSet<T> set = lock()) {
            for (T item : set) {
                if (item instanceof Mergeable && ((Mergeable)item).isDirty()) {
                    return true;
                }
            }
        }

        return false;
    }

}
