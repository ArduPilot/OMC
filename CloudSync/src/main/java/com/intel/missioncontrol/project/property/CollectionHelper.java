/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.property;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.asyncfx.collections.AsyncCollection;
import org.asyncfx.collections.AsyncObservableList;
import org.asyncfx.collections.LockedCollection;
import org.asyncfx.collections.LockedList;

public class CollectionHelper {

    /** Makes a copy of the specified list by mapping all items of S to T. */
    @SuppressWarnings("unchecked")
    public static <T, S> List<T> copy(List<S> source, Function<S, T> createItem) {
        List<T> result = new ArrayList<>(source.size());

        if (source instanceof AsyncCollection) {
            try (LockedCollection<S> lockedSourceCollection = ((AsyncCollection<S>)source).lock()) {
                for (S item : lockedSourceCollection) {
                    result.add(createItem.apply(item));
                }
            }
        } else {
            for (S item : source) {
                result.add(createItem.apply(item));
            }
        }

        return result;
    }

    /**
     * Replaces all items in the specified {@link TrackingAsyncListProperty} by mapping all items of S to T, as if by
     * calling {@link TrackingAsyncListProperty#update(AsyncObservableList)}.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Identifiable, S> void updateAll(
            TrackingAsyncListProperty<T> target, List<S> source, Function<S, T> createItem) {
        try (LockedList<T> lockedList = target.lock()) {
            lockedList.clear();

            if (source instanceof AsyncCollection) {
                try (LockedCollection<S> lockedSourceCollection = ((AsyncCollection<S>)source).lock()) {
                    for (S item : lockedSourceCollection) {
                        target.add(createItem.apply(item));
                    }
                }
            } else {
                for (S item : source) {
                    target.add(createItem.apply(item));
                }
            }
        }

        target.update(target.get());
    }

    /**
     * Replaces all items in the {@link AsyncObservableList} contained in the specified {@link
     * TrackingAsyncObjectProperty} by adding all items of the source list.
     */
    @SuppressWarnings("unchecked")
    public static <T> void updateAll(TrackingAsyncObjectProperty<AsyncObservableList<T>> target, List<T> source) {
        try (LockedList<T> lockedList = target.get().lock()) {
            lockedList.clear();

            if (source instanceof AsyncCollection) {
                try (LockedCollection<T> lockedSourceCollection = ((AsyncCollection<T>)source).lock()) {
                    lockedList.addAll(lockedSourceCollection);
                }
            } else {
                lockedList.addAll(source);
            }
        }
    }

    static <T extends Identifiable> boolean containsId(Collection<T> list, UUID id) {
        for (T item : list) {
            if (item.getId().equals(id)) {
                return true;
            }
        }

        return false;
    }

    static <T extends Identifiable> T getById(Collection<T> list, UUID id) {
        for (T item : list) {
            if (item.getId().equals(id)) {
                return item;
            }
        }

        return null;
    }

    static <L, R, V> List<V> difference(
            Collection<L> left,
            Collection<R> right,
            BiFunction<L, Collection<R>, Boolean> selector,
            Function<L, V> extractor) {
        List<V> result = new ArrayList<>();

        for (L value : left) {
            if (!selector.apply(value, right)) {
                result.add(extractor.apply(value));
            }
        }

        return result;
    }

}
