/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.property;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import org.asyncfx.collections.ArraySet;
import org.asyncfx.collections.AsyncCollection;
import org.asyncfx.collections.AsyncObservableList;
import org.asyncfx.collections.AsyncObservableSet;
import org.asyncfx.collections.FXAsyncCollections;
import org.asyncfx.collections.LockedCollection;
import org.asyncfx.collections.LockedList;
import org.asyncfx.collections.LockedSet;

public class CollectionHelper {

    /** Makes a copy of the specified set. */
    @SuppressWarnings("unchecked")
    public static <T> Set<T> copy(Set<T> source) {
        Set<T> result = new ArraySet<>(source != null ? source.size() : 0);

        if (source instanceof AsyncCollection) {
            try (LockedCollection<T> lockedSourceCollection = ((AsyncCollection<T>)source).lock()) {
                result.addAll(lockedSourceCollection);
            }
        } else if (source != null) {
            result.addAll(source);
        }

        return result;
    }

    /** Makes a copy of the specified list. */
    @SuppressWarnings("unchecked")
    public static <T> List<T> copy(List<T> source) {
        List<T> result = new ArrayList<>(source != null ? source.size() : 0);

        if (source instanceof AsyncCollection) {
            try (LockedCollection<T> lockedSourceCollection = ((AsyncCollection<T>)source).lock()) {
                result.addAll(lockedSourceCollection);
            }
        } else if (source != null) {
            result.addAll(source);
        }

        return result;
    }

    /** Makes a copy of the specified list by mapping all items of S to T. */
    @SuppressWarnings("unchecked")
    public static <T, S> List<T> copy(List<S> source, Function<S, T> createItem) {
        List<T> result = new ArrayList<>(source != null ? source.size() : 0);

        if (source instanceof AsyncCollection) {
            try (LockedCollection<S> lockedSourceCollection = ((AsyncCollection<S>)source).lock()) {
                for (S item : lockedSourceCollection) {
                    result.add(createItem.apply(item));
                }
            }
        } else if (source != null) {
            for (S item : source) {
                result.add(createItem.apply(item));
            }
        }

        return result;
    }

    /**
     * Makes a copy of the specified source list with the following properties:
     *
     * <ul>
     *   <li>1. If an item of 'source' is contained in 'sourceDuplicates' with index I, the copied list will contain an
     *       item of 'targetDuplicates' that corresponds to the index I.
     *   <li>2. If an item of 'source' is not contained in 'sourceDuplicates', the copied list will contain an item that
     *       was obtained by mapping the source item to T.
     * </ul>
     */
    @SuppressWarnings("unchecked")
    public static <T, S> List<T> copyDeduplicate(
            List<T> targetDuplicates, List<S> sourceDuplicates, List<S> source, Function<S, T> createItem) {
        try {
            if (targetDuplicates instanceof AsyncCollection) {
                targetDuplicates = (List<T>)((AsyncCollection<T>)targetDuplicates).lock();
            }

            if (sourceDuplicates instanceof AsyncCollection) {
                sourceDuplicates = (List<S>)((AsyncCollection<S>)sourceDuplicates).lock();
            }

            if (source instanceof AsyncCollection) {
                source = (List<S>)((AsyncCollection<S>)source).lock();
            }

            List<T> target = new ArrayList<>(source.size());

            for (int i = 0; i < source.size(); ++i) {
                S sourceItem = source.get(i);
                int duplicateIndex = indexOfReference(source, sourceItem);
                if (duplicateIndex >= 0) {
                    target.add(targetDuplicates.get(duplicateIndex));
                } else {
                    target.add(createItem.apply(sourceItem));
                }
            }

            return target;
        } finally {
            if (targetDuplicates instanceof LockedCollection) {
                ((LockedCollection)targetDuplicates).close();
            }

            if (sourceDuplicates instanceof LockedCollection) {
                ((LockedCollection)sourceDuplicates).close();
            }

            if (source instanceof LockedCollection) {
                ((LockedCollection)source).close();
            }
        }
    }

    public static <T extends Identifiable> void initAll(
            TrackingAsyncListProperty<T> target, TrackingAsyncListProperty<T> source, Function<T, T> createItem) {
        try (LockedList<T> targetList = target.lock();
            LockedList<T> sourceList = source.lock()) {
            targetList.clear();
            for (T item : sourceList) {
                targetList.add(createItem.apply(item));
            }
        }
    }

    public static <T> void initAll(
            TrackingAsyncObjectProperty<AsyncObservableList<T>> target,
            TrackingAsyncObjectProperty<AsyncObservableList<T>> source,
            Function<T, T> createItem) {
        AsyncObservableList<T> targetList = target.get();
        if (targetList == null) {
            target.set(targetList = FXAsyncCollections.observableArrayList());
        }

        AsyncObservableList<T> sourceList = source.get();
        if (sourceList == null) {
            sourceList = FXAsyncCollections.observableArrayList();
        }

        try (LockedList<T> lockedTarget = targetList.lock();
            LockedList<T> lockedSource = sourceList.lock()) {
            lockedTarget.clear();
            for (T item : lockedSource) {
                lockedTarget.add(createItem.apply(item));
            }
        }
    }

    /**
     * Replaces all items in the specified {@link TrackingAsyncListProperty} by mapping all items of S to T, as if by
     * calling {@link TrackingAsyncListProperty#init(AsyncObservableList)}.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Identifiable, S> void initAll(
            TrackingAsyncListProperty<T> target, List<S> source, Function<S, T> createItem) {
        try (LockedList<T> lockedList = target.lock()) {
            lockedList.clear();

            if (source instanceof AsyncCollection) {
                try (LockedCollection<S> lockedSourceCollection = ((AsyncCollection<S>)source).lock()) {
                    for (S item : lockedSourceCollection) {
                        lockedList.add(createItem.apply(item));
                    }
                }
            } else if (source != null) {
                for (S item : source) {
                    lockedList.add(createItem.apply(item));
                }
            }
        }

        target.clean();
    }

    /**
     * Replaces all items in the {@link AsyncObservableList} contained in the specified {@link
     * TrackingAsyncObjectProperty} by adding all items of the source list.
     */
    @SuppressWarnings("unchecked")
    public static <T> void initAll(
            TrackingAsyncObjectProperty<AsyncObservableList<T>> target, List<? extends T> source) {
        try (LockedList<T> lockedList = target.get().lock()) {
            lockedList.clear();

            if (source instanceof AsyncCollection) {
                try (LockedCollection<T> lockedSourceCollection = ((AsyncCollection<T>)source).lock()) {
                    lockedList.addAll(lockedSourceCollection);
                }
            } else if (source != null) {
                lockedList.addAll(source);
            }
        }

        target.clean();
    }

    @SuppressWarnings("unchecked")
    public static <T, S> void initAll(
            TrackingAsyncObjectProperty<AsyncObservableList<T>> target, List<S> source, Function<S, T> createItem) {
        try (LockedList<T> lockedList = target.get().lock()) {
            lockedList.clear();

            if (source instanceof AsyncCollection) {
                try (LockedCollection<S> lockedSourceCollection = ((AsyncCollection<S>)source).lock()) {
                    for (S item : lockedSourceCollection) {
                        lockedList.add(createItem.apply(item));
                    }
                }
            } else if (source != null) {
                for (S item : source) {
                    lockedList.add(createItem.apply(item));
                }
            }
        }

        target.clean();
    }

    @SuppressWarnings("unchecked")
    public static <T, S> void initAll(
            TrackingAsyncObjectProperty<AsyncObservableSet<T>> target, Set<S> source, Function<S, T> createItem) {
        try (LockedSet<T> lockedSet = target.get().lock()) {
            lockedSet.clear();

            if (source instanceof AsyncCollection) {
                try (LockedCollection<S> lockedSourceCollection = ((AsyncCollection<S>)source).lock()) {
                    for (S item : lockedSourceCollection) {
                        lockedSet.add(createItem.apply(item));
                    }
                }
            } else if (source != null) {
                for (S item : source) {
                    lockedSet.add(createItem.apply(item));
                }
            }
        }

        target.clean();
    }

    @SuppressWarnings("unchecked")
    public static <T, S> void merge(
            TrackingAsyncObjectProperty<AsyncObservableList<T>> target,
            List<S> source,
            MergeStrategy strategy,
            Function<S, T> createItem) {
        List<T> targetList;

        if (source instanceof AsyncCollection) {
            try (LockedCollection<S> lockedCollection = ((AsyncCollection<S>)source).lock()) {
                targetList = new ArrayList<>(lockedCollection.size());
                for (S value : lockedCollection) {
                    targetList.add(value != null ? createItem.apply(value) : null);
                }
            }
        } else if (source != null) {
            targetList = new ArrayList<>(source.size());
            for (S value : source) {
                targetList.add(value != null ? createItem.apply(value) : null);
            }
        } else {
            targetList = new ArrayList<>(0);
        }

        target.merge(FXAsyncCollections.observableList(targetList), strategy);
    }

    @SuppressWarnings("unchecked")
    public static <T, S> void merge(
            TrackingAsyncObjectProperty<AsyncObservableSet<T>> target,
            Set<S> source,
            MergeStrategy strategy,
            Function<S, T> createItem) {
        Set<T> targetList;

        if (source instanceof AsyncCollection) {
            try (LockedCollection<S> lockedCollection = ((AsyncCollection<S>)source).lock()) {
                targetList = new ArraySet<>(lockedCollection.size());
                for (S value : lockedCollection) {
                    targetList.add(value != null ? createItem.apply(value) : null);
                }
            }
        } else if (source != null) {
            targetList = new ArraySet<>(source.size());
            for (S value : source) {
                targetList.add(value != null ? createItem.apply(value) : null);
            }
        } else {
            targetList = new ArraySet<>(0);
        }

        target.merge(FXAsyncCollections.observableSet(targetList), strategy);
    }

    static <S extends Identifiable, T extends Identifiable> List<Identifiable> difference(
            Collection<S> left, Collection<T> right) {
        List<Identifiable> result = new ArrayList<>();

        for (S leftItem : left) {
            UUID leftItemId = leftItem.getId();
            boolean found = false;

            for (T rightItem : right) {
                if (leftItemId.equals(rightItem.getId())) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                result.add(leftItem);
            }
        }

        return result;
    }

    static <T extends Identifiable> T getById(Collection<T> list, UUID id) {
        for (T item : list) {
            if (item.getId().equals(id)) {
                return item;
            }
        }

        return null;
    }

    static <T> int indexOfReference(List<T> list, T item) {
        for (int i = 0; i < list.size(); ++i) {
            if (list.get(i) == item) {
                return i;
            }
        }

        return -1;
    }

}
