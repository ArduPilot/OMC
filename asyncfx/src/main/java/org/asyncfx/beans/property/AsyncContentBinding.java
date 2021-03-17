/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.beans.property;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import javafx.beans.WeakListener;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import org.asyncfx.PublishSource;
import org.asyncfx.beans.binding.LifecycleValueConverter;
import org.asyncfx.beans.binding.ValueConverter;
import org.asyncfx.collections.AsyncListChangeListener;
import org.asyncfx.collections.AsyncObservableList;
import org.asyncfx.collections.AsyncObservableSet;
import org.asyncfx.collections.LockedList;
import org.asyncfx.collections.LockedSet;
import org.asyncfx.collections.UnsafeListAccess;

@PublishSource(
    module = "openjfx",
    licenses = {"openjfx", "intel-gpl-classpath-exception"}
)
class AsyncContentBinding {

    private static <T> AsyncListChangeListener.AsyncChange<T> makeUpdateChange(
            AsyncListProperty<T> targetList, int from, int to, List<T> targetUpdatedList) {
        return new AsyncListChangeListener.AsyncChange<>(targetList) {
            private int cursor = -1;

            @Override
            public boolean next() {
                cursor++;
                return cursor == 0;
            }

            @Override
            public void reset() {
                cursor = -1;
            }

            @Override
            public int getFrom() {
                return from;
            }

            @Override
            public int getTo() {
                return to;
            }

            @Override
            public List<T> getRemoved() {
                return Collections.emptyList();
            }

            @Override
            protected int[] getPermutation() {
                return new int[0];
            }

            @Override
            public List<T> getUpdatedSubList() {
                return targetUpdatedList;
            }

            @Override
            public boolean wasUpdated() {
                return true;
            }
        };
    }

    static <T> void bindContent(AsyncListProperty<T> target, ObservableList<? extends T> source) {
        checkParameters(target, source);
        final ListContentBinding<T, T> contentBinding = new ListContentBinding<>(target, (ValueConverter<T, T>)null);
        LockedList<T> targetListCopy = null;

        try {
            final LockedList<T> targetList = target.lock();
            final List<? extends T> sourceContentCopy = new ArrayList<>(source);
            targetListCopy = targetList;
            // TODO #potentialdeadlock why not lock inside the executor?
            target.getSequentialExecutor()
                .execute(
                    () -> {
                        //noinspection TryFinallyCanBeTryWithResources
                        try {
                            targetList.changeOwner(Thread.currentThread());
                            targetList.clear();
                            if (!sourceContentCopy.isEmpty()) {
                                targetList.addAll(sourceContentCopy);
                            }
                        } finally {
                            targetList.close();
                        }
                    });

            source.removeListener(contentBinding);
            source.addListener(contentBinding);
        } catch (Exception e) {
            if (targetListCopy != null) {
                targetListCopy.close();
            }

            throw e;
        }
    }

    static <T> void bindContent(AsyncListProperty<T> target, AsyncObservableList<? extends T> source) {
        checkParameters(target, source);
        final AsyncListContentBinding<T, T> contentBinding =
            new AsyncListContentBinding<>(target, (ValueConverter<T, T>)null);
        LockedList<T> targetListCopy = null;

        try (LockedList<? extends T> sourceList = source.lock()) {
            // TODO #potentialdeadlock
            final LockedList<T> targetList = target.lock();
            final List<? extends T> sourceContentCopy = new ArrayList<>(sourceList);
            targetListCopy = targetList;
            // TODO #potentialdeadlock
            target.getSequentialExecutor()
                .execute(
                    () -> {
                        try {
                            Thread newOwner = Thread.currentThread();
                            targetList.changeOwner(newOwner);
                            targetList.clear();
                            if (!sourceContentCopy.isEmpty()) {
                                targetList.addAll(sourceContentCopy);
                            }
                        } finally {
                            targetList.close();
                        }
                    });

            source.removeListener(contentBinding);
            source.addListener(contentBinding);
        } catch (Exception e) {
            if (targetListCopy != null) {
                try {
                    targetListCopy.close();
                } catch (Exception ignored) {
                }
            }

            throw e;
        }
    }

    static <T, S> void bindContent(
            AsyncListProperty<T> target, ObservableList<? extends S> source, ValueConverter<S, T> converter) {
        checkParameters(target, source);
        final ListContentBinding<T, S> contentBinding = new ListContentBinding<>(target, converter);
        LockedList<T> targetListCopy = null;

        try {
            final List<T> sourceList = new ArrayList<>(source.size());
            for (S item : source) {
                sourceList.add(converter.convert(item));
            }

            final LockedList<T> targetList = target.lock();
            targetListCopy = targetList;
            // TODO #potentialdeadlock
            target.getSequentialExecutor()
                .execute(
                    () -> {
                        //noinspection TryFinallyCanBeTryWithResources
                        try {
                            targetList.changeOwner(Thread.currentThread());
                            targetList.clear();
                            if (!sourceList.isEmpty()) {
                                targetList.addAll(sourceList);
                            }
                        } finally {
                            targetList.close();
                        }
                    });

            source.removeListener(contentBinding);
            source.addListener(contentBinding);
        } catch (Exception e) {
            if (targetListCopy != null) {
                try {
                    targetListCopy.close();
                } catch (Exception ignored) {
                }
            }

            throw e;
        }
    }

    static <T, S> void bindContent(
            AsyncListProperty<T> target, AsyncObservableList<? extends S> source, ValueConverter<S, T> converter) {
        checkParameters(target, source);
        final AsyncListContentBinding<T, S> contentBinding = new AsyncListContentBinding<>(target, converter);
        LockedList<T> targetListCopy = null;

        try (LockedList<? extends S> sourceList = source.lock()) {
            final List<T> convertedSourceList = new ArrayList<>(sourceList.size());
            for (S item : sourceList) {
                // TODO #potentialdeadlock
                convertedSourceList.add(converter.convert(item));
            }

            // TODO #potentialdeadlock
            final LockedList<T> targetList = target.lock();
            targetListCopy = targetList;
            // TODO #potentialdeadlock
            target.getSequentialExecutor()
                .execute(
                    () -> {
                        //noinspection TryFinallyCanBeTryWithResources
                        try {
                            targetList.changeOwner(Thread.currentThread());
                            targetList.clear();
                            if (!convertedSourceList.isEmpty()) {
                                targetList.addAll(convertedSourceList);
                            }
                        } finally {
                            targetList.close();
                        }
                    });

            source.removeListener(contentBinding);
            source.addListener(contentBinding);
        } catch (Exception e) {
            if (targetListCopy != null) {
                try {
                    targetListCopy.close();
                } catch (Exception ignored) {
                }
            }

            throw e;
        }
    }

    static <T, S> void bindContent(
            AsyncListProperty<T> target,
            AsyncObservableList<? extends S> source,
            LifecycleValueConverter<S, T> converter) {
        checkParameters(target, source);
        final AsyncListContentBinding<T, S> contentBinding = new AsyncListContentBinding<>(target, converter);
        LockedList<T> targetListCopy = null;

        try (LockedList<? extends S> sourceList = source.lock()) {
            final List<T> convertedSourceList = new ArrayList<>(sourceList.size());
            for (S item : sourceList) {
                // TODO #potentialdeadlock
                convertedSourceList.add(converter.convert(item));
            }

            // TODO #potentialdeadlock
            final LockedList<T> targetList = target.lock();
            targetListCopy = targetList;
            // TODO #potentialdeadlock
            target.getSequentialExecutor()
                .execute(
                    () -> {
                        //noinspection TryFinallyCanBeTryWithResources
                        try {
                            targetList.changeOwner(Thread.currentThread());
                            targetList.clear();
                            if (!convertedSourceList.isEmpty()) {
                                targetList.addAll(convertedSourceList);
                            }
                        } finally {
                            targetList.close();
                        }
                    });

            source.removeListener(contentBinding);
            source.addListener(contentBinding);
        } catch (Exception e) {
            if (targetListCopy != null) {
                try {
                    targetListCopy.close();
                } catch (Exception ignored) {
                }
            }

            throw e;
        }
    }

    static <T> void bindContent(AsyncSetProperty<T> target, ObservableSet<? extends T> source) {
        checkParameters(target, source);
        final AsyncSetContentBinding<T, T> contentBinding =
            new AsyncSetContentBinding<>(target, (ValueConverter<T, T>)null);
        LockedSet<T> targetSetCopy = null;

        try {
            final LockedSet<T> targetSet = target.lock();
            final List<? extends T> sourceContentCopy = new ArrayList<>(source);
            targetSetCopy = targetSet;
            // TODO #potentialdeadlock
            target.getSequentialExecutor()
                .execute(
                    () -> {
                        //noinspection TryFinallyCanBeTryWithResources
                        try {
                            targetSet.changeOwner(Thread.currentThread());
                            targetSet.clear();
                            if (!sourceContentCopy.isEmpty()) {
                                targetSet.addAll(sourceContentCopy);
                            }
                        } finally {
                            targetSet.close();
                        }
                    });

            source.removeListener(contentBinding);
            source.addListener(contentBinding);
        } catch (Exception e) {
            if (targetSetCopy != null) {
                try {
                    targetSetCopy.close();
                } catch (Exception ignored) {
                }
            }

            throw e;
        }
    }

    static <T> void bindContent(AsyncSetProperty<T> target, AsyncObservableSet<? extends T> source) {
        checkParameters(target, source);
        final AsyncSetContentBinding<T, T> contentBinding =
            new AsyncSetContentBinding<>(target, (ValueConverter<T, T>)null);
        LockedSet<T> targetSetCopy = null;

        try (LockedSet<? extends T> sourceSet = source.lock()) {
            // TODO #potentialdeadlock
            final LockedSet<T> targetSet = target.lock();
            final List<? extends T> sourceContentCopy = new ArrayList<>(sourceSet);
            targetSetCopy = targetSet;
            // TODO #potentialdeadlock
            target.getSequentialExecutor()
                .execute(
                    () -> {
                        try {
                            Thread newOwner = Thread.currentThread();
                            targetSet.changeOwner(newOwner);
                            targetSet.clear();
                            if (!sourceContentCopy.isEmpty()) {
                                targetSet.addAll(sourceContentCopy);
                            }
                        } finally {
                            targetSet.close();
                        }
                    });

            source.removeListener(contentBinding);
            source.addListener(contentBinding);
        } catch (Exception e) {
            if (targetSetCopy != null) {
                try {
                    targetSetCopy.close();
                } catch (Exception ignored) {
                }
            }

            throw e;
        }
    }

    static <T, S> void bindContent(
            AsyncSetProperty<T> target, ObservableSet<? extends S> source, ValueConverter<S, T> converter) {
        checkParameters(target, source);
        final AsyncSetContentBinding<T, S> contentBinding = new AsyncSetContentBinding<>(target, converter);
        LockedSet<T> targetSetCopy = null;

        try {
            final List<T> sourceSet = new ArrayList<>(source.size());
            for (S item : source) {
                sourceSet.add(converter.convert(item));
            }

            final LockedSet<T> targetSet = target.lock();
            targetSetCopy = targetSet;
            // TODO #potentialdeadlock
            target.getSequentialExecutor()
                .execute(
                    () -> {
                        //noinspection TryFinallyCanBeTryWithResources
                        try {
                            targetSet.changeOwner(Thread.currentThread());
                            targetSet.clear();
                            if (!sourceSet.isEmpty()) {
                                targetSet.addAll(sourceSet);
                            }
                        } finally {
                            targetSet.close();
                        }
                    });

            source.removeListener(contentBinding);
            source.addListener(contentBinding);
        } catch (Exception e) {
            if (targetSetCopy != null) {
                try {
                    targetSetCopy.close();
                } catch (Exception ignored) {
                }
            }

            throw e;
        }
    }

    static <T, S> void bindContent(
            AsyncSetProperty<T> target, AsyncObservableSet<? extends S> source, ValueConverter<S, T> converter) {
        checkParameters(target, source);
        final AsyncSetContentBinding<T, S> contentBinding = new AsyncSetContentBinding<>(target, converter);
        LockedSet<? extends S> sourceSetCopy = null;
        LockedSet<T> targetSetCopy = null;

        try {
            final LockedSet<? extends S> sourceSet = source.lock();
            sourceSetCopy = sourceSet;

            final List<T> convertedSourceList = new ArrayList<>(sourceSet.size());
            for (S item : sourceSet) {
                // TODO #potentialdeadlock
                convertedSourceList.add(converter.convert(item));
            }

            // TODO #potentialdeadlock
            final LockedSet<T> targetSet = target.lock();
            targetSetCopy = targetSet;
            // TODO #potentialdeadlock
            target.getSequentialExecutor()
                .execute(
                    () -> {
                        //noinspection TryFinallyCanBeTryWithResources
                        try {
                            targetSet.changeOwner(Thread.currentThread());
                            targetSet.clear();
                            if (!convertedSourceList.isEmpty()) {
                                targetSet.addAll(convertedSourceList);
                            }
                        } finally {
                            targetSet.close();
                        }
                    });

            source.removeListener(contentBinding);
            source.addListener(contentBinding);
        } catch (Exception e) {
            if (targetSetCopy != null) {
                try {
                    targetSetCopy.close();
                } catch (Exception ignored) {
                }
            }

            throw e;
        } finally {
            if (sourceSetCopy != null) {
                sourceSetCopy.close();
            }
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    static void unbindContent(Object obj1, Object obj2) {
        checkParameters(obj1, obj2);
        if (obj1 instanceof AsyncListProperty) {
            if (obj2 instanceof AsyncObservableList) {
                ((ObservableList)obj2)
                    .removeListener(new AsyncListContentBinding((AsyncListProperty)obj1, (ValueConverter)null));
            } else if (obj2 instanceof ObservableList) {
                ((ObservableList)obj2)
                    .removeListener(new ListContentBinding((AsyncListProperty)obj1, (ValueConverter)null));
            }
        } else if (obj1 instanceof AsyncSetProperty) {
            if (obj2 instanceof AsyncObservableSet) {
                ((ObservableSet)obj2)
                    .removeListener(new AsyncSetContentBinding((AsyncSetProperty)obj1, (ValueConverter)null));
            }
        }
    }

    private static void checkParameters(Object property1, Object property2) {
        if (property1 == null || property2 == null) {
            throw new NullPointerException("Both parameters must be specified.");
        }

        if (property1 == property2) {
            throw new IllegalArgumentException("Cannot bind object to itself");
        }
    }

    static class ListContentBinding<T, S> implements ListChangeListener<S>, WeakListener {
        private final WeakReference<AsyncListProperty<T>> listRef;
        private final ValueConverter<S, T> converter;

        ListContentBinding(AsyncListProperty<T> list, ValueConverter<S, T> converter) {
            this.listRef = new WeakReference<>(list);
            this.converter = converter;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void onChanged(Change<? extends S> change) {
            final AsyncListProperty<T> targetList = listRef.get();
            if (targetList == null) {
                change.getList().removeListener(this);
            } else {
                final Executor executor = targetList.getSequentialExecutor();

                while (change.next()) {
                    final int from = change.getFrom();
                    final int to = change.getTo();

                    if (change.wasPermutated()) {
                        final int[] indexMap = new int[to - from];
                        for (int i = from; i < to; ++i) {
                            indexMap[i - from] = change.getPermutation(i);
                        }

                        executor.execute(
                            () -> {
                                try (LockedList<T> lockedTargetList = targetList.lock()) {
                                    List<T> copy = new ArrayList<>(lockedTargetList.subList(from, to));
                                    for (int i = 0; i < to - from; ++i) {
                                        int newIndex = indexMap[i];
                                        lockedTargetList.set(newIndex, copy.get(i));
                                    }
                                }
                            });
                    } else if (change.wasUpdated()) {
                        final List<S> updatedSublist = new ArrayList<>(change.getList().subList(from, to));

                        executor.execute(
                            () -> {
                                final List<T> targetUpdatedList;

                                if (converter != null) {
                                    try (LockedList<T> targetLockedList = targetList.lock()) {
                                        targetUpdatedList = new ArrayList<>(targetLockedList.subList(from, to));
                                    }
                                } else {
                                    targetUpdatedList = (List<T>)updatedSublist;
                                }

                                if (converter instanceof LifecycleValueConverter) {
                                    LifecycleValueConverter<S, T> lifecycleConverter =
                                        (LifecycleValueConverter<S, T>)converter;

                                    for (int i = 0; i < updatedSublist.size(); ++i) {
                                        lifecycleConverter.update(updatedSublist.get(i), targetUpdatedList.get(i));
                                    }
                                } else if (converter != null) {
                                    List<T> convertedList = new ArrayList<>(updatedSublist.size());
                                    for (int i = 0; i < updatedSublist.size(); ++i) {
                                        convertedList.add(i, converter.convert(updatedSublist.get(i)));
                                    }

                                    try (LockedList<T> targetLockedList = targetList.lock()) {
                                        for (int i = 0; i < updatedSublist.size(); ++i) {
                                            targetLockedList.set(from + i, convertedList.get(i));
                                        }
                                    }
                                }

                                ((AsyncListPropertyBase<T>)targetList)
                                    .fireValueChangedEvent(makeUpdateChange(targetList, from, to, targetUpdatedList));
                            });
                    } else {
                        Runnable removedAction;
                        Runnable addedAction;

                        if (change.wasRemoved()) {
                            final int toRemoved = from + change.getRemovedSize();

                            removedAction =
                                () -> {
                                    if (converter instanceof LifecycleValueConverter) {
                                        List<T> removedList;

                                        try (LockedList<T> lockedTargetList = targetList.lock()) {
                                            removedList = new ArrayList<>(lockedTargetList.subList(from, toRemoved));
                                        }

                                        for (T item : removedList) {
                                            ((LifecycleValueConverter<S, T>)converter).remove(item);
                                        }
                                    }

                                    targetList.remove(from, toRemoved);
                                };
                        } else {
                            removedAction = null;
                        }

                        if (change.wasAdded()) {
                            final List<Object> addedList = new ArrayList<>(change.getAddedSubList());

                            addedAction =
                                () -> {
                                    if (converter != null) {
                                        for (int i = 0; i < addedList.size(); ++i) {
                                            addedList.set(i, converter.convert((S)addedList.get(i)));
                                        }
                                    }

                                    try (LockedList<T> lockedTargetList = targetList.lock()) {
                                        int index = from;
                                        for (Object added : addedList) {
                                            lockedTargetList.add(index++, (T)added);
                                        }
                                    }
                                };
                        } else {
                            addedAction = null;
                        }

                        if (removedAction != null && addedAction != null) {
                            executor.execute(
                                () -> {
                                    removedAction.run();
                                    addedAction.run();
                                });
                        } else if (removedAction != null) {
                            executor.execute(removedAction);
                        } else if (addedAction != null) {
                            executor.execute(addedAction);
                        }
                    }
                }
            }
        }

        @Override
        public boolean wasGarbageCollected() {
            return listRef.get() == null;
        }

        @Override
        public int hashCode() {
            final List<T> list = listRef.get();
            return (list == null) ? 0 : list.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            final List<T> list1 = listRef.get();
            if (list1 == null) {
                return false;
            }

            if (obj instanceof ListContentBinding) {
                final ListContentBinding<?, ?> other = (ListContentBinding<?, ?>)obj;
                final List<?> list2 = other.listRef.get();
                return list1 == list2;
            }

            return false;
        }
    }

    static class AsyncListContentBinding<T, S> implements AsyncListChangeListener<S>, WeakListener {
        private final WeakReference<AsyncListProperty<T>> listRef;
        private final ValueConverter<S, T> converter;

        AsyncListContentBinding(AsyncListProperty<T> list, ValueConverter<S, T> converter) {
            this.listRef = new WeakReference<>(list);
            this.converter = converter;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void onChanged(AsyncChange<? extends S> change) {
            final AsyncListProperty<T> targetList = listRef.get();
            if (targetList == null) {
                AsyncObservableList<? extends S> list;
                if (change instanceof UnsafeListAccess) {
                    list = ((UnsafeListAccess<? extends S>)change).getListUnsafe();
                } else {
                    throw new IllegalArgumentException("Unsupported change type.");
                }

                list.removeListener(this);
            } else {
                final Executor executor = targetList.getSequentialExecutor();

                while (change.next()) {
                    final int from = change.getFrom();
                    final int to = change.getTo();

                    if (change.wasPermutated()) {
                        final int[] indexMap = new int[to - from];
                        for (int i = from; i < to; ++i) {
                            indexMap[i - from] = change.getPermutation(i);
                        }

                        executor.execute(
                            () -> {
                                try (LockedList<T> lockedTargetList = targetList.lock()) {
                                    List<T> copy = new ArrayList<>(lockedTargetList.subList(from, to));
                                    for (int i = 0; i < to - from; ++i) {
                                        int newIndex = indexMap[i];
                                        lockedTargetList.set(newIndex, copy.get(i));
                                    }
                                }
                            });
                    } else if (change.wasUpdated()) {
                        final List<S> updatedSublist = new ArrayList<>(change.getUpdatedSubList());

                        executor.execute(
                            () -> {
                                final List<T> targetUpdatedList;

                                if (converter != null) {
                                    try (LockedList<T> targetLockedList = targetList.lock()) {
                                        targetUpdatedList = new ArrayList<>(targetLockedList.subList(from, to));
                                    }
                                } else {
                                    targetUpdatedList = (List<T>)updatedSublist;
                                }

                                if (converter instanceof LifecycleValueConverter) {
                                    LifecycleValueConverter<S, T> lifecycleConverter =
                                        (LifecycleValueConverter<S, T>)converter;

                                    for (int i = 0; i < updatedSublist.size(); ++i) {
                                        lifecycleConverter.update(updatedSublist.get(i), targetUpdatedList.get(i));
                                    }
                                } else if (converter != null) {
                                    List<T> convertedList = new ArrayList<>(updatedSublist.size());
                                    for (int i = 0; i < updatedSublist.size(); ++i) {
                                        convertedList.add(i, converter.convert(updatedSublist.get(i)));
                                    }

                                    try (LockedList<T> targetLockedList = targetList.lock()) {
                                        for (int i = 0; i < updatedSublist.size(); ++i) {
                                            targetLockedList.set(from + i, convertedList.get(i));
                                        }
                                    }
                                }

                                ((AsyncListPropertyBase<T>)targetList)
                                    .fireValueChangedEvent(makeUpdateChange(targetList, from, to, targetUpdatedList));
                            });
                    } else {
                        Runnable removedAction;
                        Runnable addedAction;

                        if (change.wasRemoved()) {
                            final int toRemoved = from + change.getRemovedSize();

                            removedAction =
                                () -> {
                                    if (converter instanceof LifecycleValueConverter) {
                                        List<T> removedList;

                                        try (LockedList<T> lockedTargetList = targetList.lock()) {
                                            removedList = new ArrayList<>(lockedTargetList.subList(from, toRemoved));
                                        }

                                        for (T item : removedList) {
                                            ((LifecycleValueConverter<S, T>)converter).remove(item);
                                        }
                                    }

                                    targetList.remove(from, toRemoved);
                                };
                        } else {
                            removedAction = null;
                        }

                        if (change.wasAdded()) {
                            AsyncObservableList<? extends S> list;
                            if (change instanceof UnsafeListAccess) {
                                list = ((UnsafeListAccess<? extends S>)change).getListUnsafe();
                            } else {
                                throw new IllegalArgumentException("Unsupported change type.");
                            }

                            final List<Object> addedList;
                            try (LockedList<? extends S> lockedList = list.lock()) {
                                addedList = new ArrayList<>(lockedList.subList(from, to));
                            }

                            addedAction =
                                () -> {
                                    if (converter != null) {
                                        for (int i = 0; i < addedList.size(); ++i) {
                                            addedList.set(i, converter.convert((S)addedList.get(i)));
                                        }
                                    }

                                    try (LockedList<T> lockedTargetList = targetList.lock()) {
                                        int index = from;
                                        for (Object added : addedList) {
                                            lockedTargetList.add(index++, (T)added);
                                        }
                                    }
                                };
                        } else {
                            addedAction = null;
                        }

                        if (removedAction != null && addedAction != null) {
                            executor.execute(
                                () -> {
                                    removedAction.run();
                                    addedAction.run();
                                });
                        } else if (removedAction != null) {
                            executor.execute(removedAction);
                        } else if (addedAction != null) {
                            executor.execute(addedAction);
                        }
                    }
                }
            }
        }

        @Override
        public boolean wasGarbageCollected() {
            return listRef.get() == null;
        }

        @Override
        public int hashCode() {
            final List<T> list = listRef.get();
            return (list == null) ? 0 : list.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            final List<T> list1 = listRef.get();
            if (list1 == null) {
                return false;
            }

            if (obj instanceof AsyncListContentBinding) {
                final AsyncListContentBinding<?, ?> other = (AsyncListContentBinding<?, ?>)obj;
                final List<?> list2 = other.listRef.get();
                return list1 == list2;
            }

            return false;
        }
    }

    static class AsyncSetContentBinding<T, S> implements SetChangeListener<S>, WeakListener {
        private final WeakReference<AsyncSetProperty<T>> setRef;
        private final ValueConverter<S, T> converter;

        AsyncSetContentBinding(AsyncSetProperty<T> set, ValueConverter<S, T> converter) {
            this.setRef = new WeakReference<>(set);
            this.converter = converter;

            if (converter instanceof LifecycleValueConverter) {
                throw new UnsupportedOperationException("LifecycleValueConverter is not supported.");
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public void onChanged(Change<? extends S> change) {
            final AsyncSetProperty<T> set = setRef.get();
            if (set == null) {
                change.getSet().removeListener(this);
            } else {
                if (change.wasRemoved()) {
                    final T removedElement =
                        converter != null
                            ? converter.convert(change.getElementRemoved())
                            : (T)change.getElementRemoved();

                    set.getSequentialExecutor().execute(() -> set.remove(removedElement));
                } else {
                    final T addedElement =
                        converter != null ? converter.convert(change.getElementAdded()) : (T)change.getElementAdded();

                    set.getSequentialExecutor().execute(() -> set.add(addedElement));
                }
            }
        }

        @Override
        public boolean wasGarbageCollected() {
            return setRef.get() == null;
        }

        @Override
        public int hashCode() {
            final Set<T> list = setRef.get();
            return (list == null) ? 0 : list.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            final Set<T> set1 = setRef.get();
            if (set1 == null) {
                return false;
            }

            if (obj instanceof AsyncSetContentBinding) {
                final AsyncSetContentBinding<?, ?> other = (AsyncSetContentBinding<?, ?>)obj;
                final Set<?> set2 = other.setRef.get();
                return set1 == set2;
            }

            return false;
        }
    }

}
