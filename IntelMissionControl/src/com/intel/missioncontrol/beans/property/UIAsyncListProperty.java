/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.beans.property;

import com.intel.missioncontrol.PublishSource;
import com.intel.missioncontrol.collections.AsyncListChangeListener;
import com.intel.missioncontrol.collections.AsyncObservableList;
import com.intel.missioncontrol.collections.FXAsyncCollections;
import com.intel.missioncontrol.collections.LockedList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.WeakListChangeListener;

@PublishSource(
    module = "openjfx",
    licenses = {"openjfx", "intel-gpl-classpath-exception"}
)
public class UIAsyncListProperty<E> extends SimpleAsyncListProperty<E> {

    private ReadOnlyListProperty<E> readOnlyProperty;

    public UIAsyncListProperty(Object bean) {
        this(bean, new UIPropertyMetadata.Builder<AsyncObservableList<E>>().create());
    }

    public UIAsyncListProperty(Object bean, UIPropertyMetadata<AsyncObservableList<E>> metadata) {
        super(bean, metadata);
    }

    @Override
    public void overrideMetadata(PropertyMetadata<AsyncObservableList<E>> metadata) {
        if (!(metadata instanceof UIPropertyMetadata)) {
            throw new IllegalArgumentException(
                "Metadata can only be overridden with an instance of "
                    + UIPropertyMetadata.class.getSimpleName()
                    + " or its derived classes.");
        }

        super.overrideMetadata(metadata);
    }

    public ReadOnlyListProperty<E> getReadOnlyProperty() {
        StampedLock valueLock = getValueLock();
        long stamp = 0;
        try {
            if ((stamp = valueLock.tryOptimisticRead()) != 0) {
                ReadOnlyListProperty<E> readOnlyProperty = this.readOnlyProperty;
                if (valueLock.validate(stamp) && readOnlyProperty != null) {
                    return readOnlyProperty;
                }
            }

            Object bean = getBean();
            String name = getName();
            ReadOnlyListPropertyImpl<E> property = new ReadOnlyListPropertyImpl<>(bean, name, this);

            stamp = valueLock.writeLock();
            if (readOnlyProperty == null) {
                readOnlyProperty = property;
            }

            return readOnlyProperty;
        } finally {
            if (StampedLock.isWriteLockStamp(stamp)) {
                valueLock.unlockWrite(stamp);
            }
        }
    }

    @Override
    public ListIterator<E> listIterator() {
        return new ListIterator<>() {
            ListIterator<E> it = UIAsyncListProperty.super.listIterator();

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public E next() {
                return it.next();
            }

            @Override
            public boolean hasPrevious() {
                return it.hasPrevious();
            }

            @Override
            public E previous() {
                return it.previous();
            }

            @Override
            public int nextIndex() {
                return it.nextIndex();
            }

            @Override
            public int previousIndex() {
                return it.previousIndex();
            }

            @Override
            public void remove() {
                verifyAccess();
                it.remove();
            }

            @Override
            public void set(E e) {
                verifyAccess();
                it.set(e);
            }

            @Override
            public void add(E e) {
                verifyAccess();
                it.add(e);
            }
        };
    }

    @Override
    public ListIterator<E> listIterator(int i) {
        return new ListIterator<>() {
            ListIterator<E> it = UIAsyncListProperty.super.listIterator(i);

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public E next() {
                return it.next();
            }

            @Override
            public boolean hasPrevious() {
                return it.hasPrevious();
            }

            @Override
            public E previous() {
                return it.previous();
            }

            @Override
            public int nextIndex() {
                return it.nextIndex();
            }

            @Override
            public int previousIndex() {
                return it.previousIndex();
            }

            @Override
            public void remove() {
                verifyAccess();
                it.remove();
            }

            @Override
            public void set(E e) {
                verifyAccess();
                it.set(e);
            }

            @Override
            public void add(E e) {
                verifyAccess();
                it.add(e);
            }
        };
    }

    @Override
    public E set(int i, E element) {
        verifyAccess();
        return super.set(i, element);
    }

    @Override
    public boolean setAll(E... elements) {
        verifyAccess();
        return super.setAll(elements);
    }

    @Override
    public boolean setAll(Collection<? extends E> elements) {
        verifyAccess();
        return super.setAll(elements);
    }

    @Override
    public void add(int i, E element) {
        verifyAccess();
        super.add(i, element);
    }

    @Override
    public boolean add(E element) {
        verifyAccess();
        return super.add(element);
    }

    @Override
    public boolean addAll(int i, Collection<? extends E> elements) {
        verifyAccess();
        return super.addAll(i, elements);
    }

    @Override
    public boolean addAll(Collection<? extends E> elements) {
        verifyAccess();
        return super.addAll(elements);
    }

    @Override
    public boolean addAll(E... elements) {
        verifyAccess();
        return super.addAll(elements);
    }

    @Override
    public E remove(int i) {
        verifyAccess();
        return super.remove(i);
    }

    @Override
    public boolean remove(Object obj) {
        verifyAccess();
        return super.remove(obj);
    }

    @Override
    public void remove(int from, int to) {
        verifyAccess();
        super.remove(from, to);
    }

    @Override
    public boolean removeAll(Collection<?> objects) {
        verifyAccess();
        return super.removeAll(objects);
    }

    @Override
    public boolean removeAll(E... elements) {
        verifyAccess();
        return super.removeAll(elements);
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        verifyAccess();
        return super.removeIf(filter);
    }

    @Override
    public boolean retainAll(Collection<?> objects) {
        verifyAccess();
        return super.retainAll(objects);
    }

    @Override
    public boolean retainAll(E... elements) {
        verifyAccess();
        return super.retainAll(elements);
    }

    @Override
    public void replaceAll(UnaryOperator<E> operator) {
        verifyAccess();
        super.replaceAll(operator);
    }

    private void verifyAccess() {
        if (PropertyHelper.isVerifyPropertyAccessEnabled() && !Platform.isFxApplicationThread()) {
            throw new IllegalStateException(
                "Illegal cross-thread access: list can only be modified on the JavaFX application thread.");
        }
    }

    private static class ReadOnlyListPropertyImpl<E> extends SimpleListProperty<E> {

        private final AsyncListChangeListener<E> listener =
            change -> {
                while (change.next()) {
                    final int from = change.getFrom();
                    final int to = change.getTo();

                    if (change.wasPermutated()) {
                        final int[] indexMap = new int[to - from];
                        for (int i = from; i < to; ++i) {
                            indexMap[i - from] = change.getPermutation(i);
                        }

                        if (Platform.isFxApplicationThread()) {
                            List<E> copy = new ArrayList<>(get().subList(from, to));
                            for (int i = 0; i < to - from; ++i) {
                                int newIndex = indexMap[i];
                                set(newIndex, copy.get(i));
                            }
                        } else {
                            Platform.runLater(
                                () -> {
                                    List<E> copy = new ArrayList<>(get().subList(from, to));
                                    for (int i = 0; i < to - from; ++i) {
                                        int newIndex = indexMap[i];
                                        set(newIndex, copy.get(i));
                                    }
                                });
                        }
                    } else if (change.wasUpdated()) {
                        final List<E> updatedSublist = new ArrayList<>(change.getUpdatedSubList());
                        if (Platform.isFxApplicationThread()) {
                            AsyncListChangeListener.AsyncChange<E> updateChange =
                                new AsyncListChangeListener.AsyncChange<>(get()) {
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
                                    public List<E> getRemoved() {
                                        return Collections.emptyList();
                                    }

                                    @Override
                                    protected int[] getPermutation() {
                                        return new int[0];
                                    }

                                    @Override
                                    public List<E> getUpdatedSubList() {
                                        return updatedSublist;
                                    }

                                    @Override
                                    public boolean wasUpdated() {
                                        return true;
                                    }
                                };

                            fireValueChangedEvent(updateChange);
                        } else {
                            Platform.runLater(
                                () -> {
                                    AsyncListChangeListener.AsyncChange<E> updateChange =
                                        new AsyncListChangeListener.AsyncChange<>(get()) {
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
                                            public List<E> getRemoved() {
                                                return Collections.emptyList();
                                            }

                                            @Override
                                            protected int[] getPermutation() {
                                                return new int[0];
                                            }

                                            @Override
                                            public List<E> getUpdatedSubList() {
                                                return updatedSublist;
                                            }

                                            @Override
                                            public boolean wasUpdated() {
                                                return true;
                                            }
                                        };

                                    fireValueChangedEvent(updateChange);
                                });
                        }
                    } else {
                        if (change.wasReplaced()) {
                            final List<E> addedList = new ArrayList<>(change.getAddedSubList());
                            if (Platform.isFxApplicationThread()) {
                                int index = from;
                                int currentSize = size();
                                for (E added : addedList) {
                                    if (index < currentSize) {
                                        set(index++, added);
                                    } else {
                                        add(added);
                                    }
                                }
                            } else {
                                Platform.runLater(
                                    () -> {
                                        int index = from;
                                        int currentSize = size();
                                        for (E added : addedList) {
                                            if (index < currentSize) {
                                                set(index++, added);
                                            } else {
                                                add(added);
                                            }
                                        }
                                    });
                            }
                        } else {
                            if (change.wasRemoved()) {
                                final int toRemoved = from + change.getRemovedSize();
                                if (Platform.isFxApplicationThread()) {
                                    remove(from, toRemoved);
                                } else {
                                    Platform.runLater(() -> remove(from, toRemoved));
                                }
                            }

                            if (change.wasAdded()) {
                                final List<E> addedList = new ArrayList<>(change.getAddedSubList());
                                if (Platform.isFxApplicationThread()) {
                                    addAll(from, addedList);
                                } else {
                                    Platform.runLater(() -> addAll(from, addedList));
                                }
                            }
                        }
                    }
                }
            };

        ReadOnlyListPropertyImpl(Object bean, String name, UIAsyncListProperty<E> source) {
            super(bean, name, FXAsyncCollections.observableArrayList());

            try (LockedList<E> lockedList = source.lock()) {
                setAll(lockedList);
            }

            source.addListener(new WeakListChangeListener<>(listener));
        }

    }

}
