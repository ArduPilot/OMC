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

    static <T> void bindContent(AsyncListProperty<T> target, ObservableList<? extends T> source) {
        checkParameters(target, source);
        final ListContentBinding<T, T> contentBinding = new ListContentBinding<>(target, (ValueConverter<T, T>)null);
        LockedList<T> targetListCopy = null;

        try {
            final LockedList<T> targetList = target.lock();
            final List<? extends T> sourceContentCopy = new ArrayList<>(source);
            targetListCopy = targetList;
            target.getExecutor()
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
            final LockedList<T> targetList = target.lock();
            final List<? extends T> sourceContentCopy = new ArrayList<>(sourceList);
            targetListCopy = targetList;
            target.getExecutor()
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
            target.getExecutor()
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
            AsyncListProperty<T> target, ObservableList<? extends S> source, LifecycleValueConverter<S, T> converter) {
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
            target.getExecutor()
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
                convertedSourceList.add(converter.convert(item));
            }

            final LockedList<T> targetList = target.lock();
            targetListCopy = targetList;
            target.getExecutor()
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
                convertedSourceList.add(converter.convert(item));
            }

            final LockedList<T> targetList = target.lock();
            targetListCopy = targetList;
            target.getExecutor()
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

    @SuppressWarnings("unchecked")
    static <T> void bindContent(AsyncSetProperty<T> target, ObservableSet<? extends T> source) {
        checkParameters(target, source);
        final AsyncSetContentBinding<T, T> contentBinding = new AsyncSetContentBinding<>(target, (ValueConverter)null);
        LockedSet<T> targetSetCopy = null;

        try {
            final LockedSet<T> targetSet = target.lock();
            final List<? extends T> sourceContentCopy = new ArrayList<>(source);
            targetSetCopy = targetSet;
            target.getExecutor()
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
            final LockedSet<T> targetSet = target.lock();
            final List<? extends T> sourceContentCopy = new ArrayList<>(sourceSet);
            targetSetCopy = targetSet;
            target.getExecutor()
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
            target.getExecutor()
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
            AsyncSetProperty<T> target, ObservableSet<? extends S> source, LifecycleValueConverter<S, T> converter) {
        checkParameters(target, source);
        final AsyncSetContentBinding<T, S> contentBinding = new AsyncSetContentBinding<T, S>(target, converter);
        LockedSet<T> targetSetCopy = null;

        try {
            final List<T> sourceSet = new ArrayList<>(source.size());
            for (S item : source) {
                sourceSet.add(converter.convert(item));
            }

            final LockedSet<T> targetSet = target.lock();
            targetSetCopy = targetSet;
            target.getExecutor()
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
                convertedSourceList.add(converter.convert(item));
            }

            final LockedSet<T> targetSet = target.lock();
            targetSetCopy = targetSet;
            target.getExecutor()
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

    static <T, S> void bindContent(
            AsyncSetProperty<T> target,
            AsyncObservableSet<? extends S> source,
            LifecycleValueConverter<S, T> converter) {
        checkParameters(target, source);
        final AsyncSetContentBinding<T, S> contentBinding = new AsyncSetContentBinding<T, S>(target, converter);
        LockedSet<? extends S> sourceSetCopy = null;
        LockedSet<T> targetSetCopy = null;

        try {
            final LockedSet<? extends S> sourceSet = source.lock();
            sourceSetCopy = sourceSet;

            final List<T> convertedSourceList = new ArrayList<>(sourceSet.size());
            for (S item : sourceSet) {
                convertedSourceList.add(converter.convert(item));
            }

            final LockedSet<T> targetSet = target.lock();
            targetSetCopy = targetSet;
            target.getExecutor()
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

    @SuppressWarnings("unchecked")
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

    static class ListContentBinding<T, S> implements ListChangeListener<S>, WeakListener {
        private final WeakReference<AsyncListProperty<T>> listRef;
        private final ValueConverterAdapter<S, T> converter;

        ListContentBinding(AsyncListProperty<T> list, ValueConverter<S, T> converter) {
            this.listRef = new WeakReference<>(list);
            this.converter = converter != null ? new ValueConverterAdapter<>(converter) : null;
        }

        ListContentBinding(AsyncListProperty<T> list, LifecycleValueConverter<S, T> converter) {
            this.listRef = new WeakReference<>(list);
            this.converter = converter != null ? new ValueConverterAdapter<>(converter) : null;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void onChanged(Change<? extends S> change) {
            final AsyncListProperty<T> targetList = listRef.get();
            if (targetList == null) {
                change.getList().removeListener(this);
            } else {
                final Executor executor = targetList.getExecutor();

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
                                try (LockedList<T> targetLockedList = targetList.lock()) {
                                    if (converter != null) {
                                        targetUpdatedList = new ArrayList<>(targetLockedList.subList(from, to));
                                        for (int i = 0; i < targetUpdatedList.size(); ++i) {
                                            converter.update(updatedSublist.get(i), targetUpdatedList.get(i));
                                        }
                                    } else {
                                        targetUpdatedList = (List<T>)updatedSublist;
                                    }
                                }

                                AsyncListChangeListener.AsyncChange<T> updateChange =
                                    new AsyncListChangeListener.AsyncChange<>(targetList) {
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

                                ((AsyncListPropertyBase<T>)targetList).fireValueChangedEvent(updateChange);
                            });
                    } else {
                        if (change.wasReplaced()) {
                            final List<? extends S> addedList = new ArrayList<>(change.getAddedSubList());
                            executor.execute(
                                () -> {
                                    try (LockedList<T> lockedTargetList = targetList.lock()) {
                                        int index = from;
                                        int currentSize = lockedTargetList.size();
                                        if (converter != null) {
                                            for (S added : addedList) {
                                                if (index < currentSize) {
                                                    lockedTargetList.set(index++, converter.convert(added));
                                                } else {
                                                    lockedTargetList.add(converter.convert(added));
                                                }
                                            }
                                        } else {
                                            for (S added : addedList) {
                                                if (index < currentSize) {
                                                    lockedTargetList.set(index++, (T)added);
                                                } else {
                                                    lockedTargetList.add((T)added);
                                                }
                                            }
                                        }
                                    }
                                });
                        } else {
                            if (change.wasRemoved()) {
                                final int toRemoved = from + change.getRemovedSize();
                                executor.execute(
                                    () -> {
                                        try (LockedList<T> lockedTargetList = targetList.lock()) {
                                            List<T> removedList = lockedTargetList.subList(from, toRemoved);
                                            if (converter != null) {
                                                for (T item : removedList) {
                                                    converter.remove(item);
                                                }
                                            }

                                            removedList.clear();
                                        }
                                    });
                            }

                            if (change.wasAdded()) {
                                final List<? extends S> addedList = new ArrayList<>(change.getAddedSubList());
                                executor.execute(
                                    () -> {
                                        try (LockedList<T> lockedTargetList = targetList.lock()) {
                                            int index = from;
                                            if (converter != null) {
                                                for (S added : addedList) {
                                                    lockedTargetList.add(index++, converter.convert(added));
                                                }
                                            } else {
                                                for (S added : addedList) {
                                                    lockedTargetList.add(index++, (T)added);
                                                }
                                            }
                                        }
                                    });
                            }
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
        @SuppressWarnings("unchecked")
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            final List<T> list1 = listRef.get();
            if (list1 == null) {
                return false;
            }

            if (obj instanceof ListContentBinding) {
                final ListContentBinding<T, ?> other = (ListContentBinding<T, ?>)obj;
                final List<?> list2 = other.listRef.get();
                return list1 == list2;
            }

            return false;
        }
    }

    static class AsyncListContentBinding<T, S> implements AsyncListChangeListener<S>, WeakListener {
        private final WeakReference<AsyncListProperty<T>> listRef;
        private final ValueConverterAdapter<S, T> converter;

        AsyncListContentBinding(AsyncListProperty<T> list, ValueConverter<S, T> converter) {
            this.listRef = new WeakReference<>(list);
            this.converter = converter != null ? new ValueConverterAdapter<>(converter) : null;
        }

        AsyncListContentBinding(AsyncListProperty<T> list, LifecycleValueConverter<S, T> converter) {
            this.listRef = new WeakReference<>(list);
            this.converter = converter != null ? new ValueConverterAdapter<>(converter) : null;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void onChanged(AsyncChange<? extends S> change) {
            final AsyncListProperty<T> targetList = listRef.get();
            if (targetList == null) {
                change.getList().removeListener(this);
            } else {
                final Executor executor = targetList.getExecutor();

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
                                try (LockedList<T> targetLockedList = targetList.lock()) {
                                    if (converter != null) {
                                        targetUpdatedList = new ArrayList<>(targetLockedList.subList(from, to));
                                        for (int i = 0; i < targetUpdatedList.size(); ++i) {
                                            converter.update(updatedSublist.get(i), targetUpdatedList.get(i));
                                        }
                                    } else {
                                        targetUpdatedList = (List<T>)updatedSublist;
                                    }
                                }

                                AsyncListChangeListener.AsyncChange<T> updateChange =
                                    new AsyncListChangeListener.AsyncChange<>(targetList) {
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

                                ((AsyncListPropertyBase<T>)targetList).fireValueChangedEvent(updateChange);
                            });
                    } else {
                        if (change.wasReplaced()) {
                            AsyncObservableList<? extends S> list;
                            if (change instanceof UnsafeListAccess) {
                                list = ((UnsafeListAccess<? extends S>)change).getListUnsafe();
                            } else {
                                throw new IllegalArgumentException("Unsupported change type.");
                            }

                            final List<S> addedList;
                            try (LockedList<? extends S> lockedList = list.lock()) {
                                addedList = new ArrayList<>(lockedList.subList(from, to));
                            }

                            executor.execute(
                                () -> {
                                    try (LockedList<T> lockedTargetList = targetList.lock()) {
                                        int index = from;
                                        int currentSize = lockedTargetList.size();
                                        if (converter != null) {
                                            for (S added : addedList) {
                                                if (index < currentSize) {
                                                    lockedTargetList.set(index++, converter.convert(added));
                                                } else {
                                                    lockedTargetList.add(converter.convert(added));
                                                }
                                            }
                                        } else {
                                            for (S added : addedList) {
                                                if (index < currentSize) {
                                                    lockedTargetList.set(index++, (T)added);
                                                } else {
                                                    lockedTargetList.add((T)added);
                                                }
                                            }
                                        }
                                    }
                                });
                        } else {
                            if (change.wasRemoved()) {
                                final int toRemoved = from + change.getRemovedSize();
                                executor.execute(
                                    () -> {
                                        try (LockedList<T> lockedTargetList = targetList.lock()) {
                                            List<T> removedList = lockedTargetList.subList(from, toRemoved);
                                            if (converter != null) {
                                                for (T item : removedList) {
                                                    converter.remove(item);
                                                }
                                            }

                                            removedList.clear();
                                        }
                                    });
                            }

                            if (change.wasAdded()) {
                                AsyncObservableList<? extends S> list;
                                if (change instanceof UnsafeListAccess) {
                                    list = ((UnsafeListAccess<? extends S>)change).getListUnsafe();
                                } else {
                                    throw new IllegalArgumentException("Unsupported change type.");
                                }

                                final List<S> addedList;
                                try (LockedList<? extends S> lockedList = list.lock()) {
                                    addedList = new ArrayList<>(lockedList.subList(from, to));
                                }

                                executor.execute(
                                    () -> {
                                        try (LockedList<T> lockedTargetList = targetList.lock()) {
                                            int index = from;
                                            if (converter != null) {
                                                for (S added : addedList) {
                                                    lockedTargetList.add(index++, converter.convert(added));
                                                }
                                            } else {
                                                for (S added : addedList) {
                                                    lockedTargetList.add(index++, (T)added);
                                                }
                                            }
                                        }
                                    });
                            }
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
        @SuppressWarnings("unchecked")
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            final List<T> list1 = listRef.get();
            if (list1 == null) {
                return false;
            }

            if (obj instanceof AsyncListContentBinding) {
                final AsyncListContentBinding<T, ?> other = (AsyncListContentBinding<T, ?>)obj;
                final List<?> list2 = other.listRef.get();
                return list1 == list2;
            }

            return false;
        }
    }

    static class AsyncSetContentBinding<T, S> implements SetChangeListener<S>, WeakListener {
        private final WeakReference<AsyncSetProperty<T>> setRef;
        private final ValueConverterAdapter<S, T> converter;

        AsyncSetContentBinding(AsyncSetProperty<T> set, ValueConverter<S, T> converter) {
            this.setRef = new WeakReference<>(set);
            this.converter = converter != null ? new ValueConverterAdapter<>(converter) : null;
        }

        AsyncSetContentBinding(AsyncSetProperty<T> set, LifecycleValueConverter<S, T> converter) {
            this.setRef = new WeakReference<>(set);
            this.converter = converter != null ? new ValueConverterAdapter<>(converter) : null;
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
                    set.getExecutor().execute(() -> set.remove(removedElement));
                } else {
                    final T addedElement =
                        converter != null ? converter.convert(change.getElementAdded()) : (T)change.getElementAdded();
                    set.getExecutor().execute(() -> set.add(addedElement));
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
        @SuppressWarnings("unchecked")
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            final Set<T> set1 = setRef.get();
            if (set1 == null) {
                return false;
            }

            if (obj instanceof AsyncSetContentBinding) {
                final AsyncSetContentBinding<T, ?> other = (AsyncSetContentBinding<T, ?>)obj;
                final Set<?> set2 = other.setRef.get();
                return set1 == set2;
            }

            return false;
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

}
