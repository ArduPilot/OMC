/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.beans.property;

import static org.asyncfx.beans.AccessControllerImpl.LockName.VALUE;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.WeakListChangeListener;
import org.asyncfx.AsyncFX;
import org.asyncfx.PublishSource;
import org.asyncfx.beans.AccessControllerImpl;
import org.asyncfx.collections.AsyncListChangeListener;
import org.asyncfx.collections.AsyncObservableList;
import org.asyncfx.collections.LockedList;
import org.asyncfx.collections.UnsafeListAccess;
import org.jetbrains.annotations.NotNull;

@PublishSource(
    module = "openjfx",
    licenses = {"openjfx", "intel-gpl-classpath-exception"}
)
public class UIAsyncListProperty<E> extends SimpleAsyncListProperty<E> {

    private volatile ReadOnlyListProperty<E> readOnlyProperty;

    public UIAsyncListProperty(Object bean) {
        this(bean, new UIPropertyMetadata.Builder<AsyncObservableList<E>>().create());
    }

    public UIAsyncListProperty(ObservableObject bean) {
        this(bean, new UIPropertyMetadata.Builder<AsyncObservableList<E>>().create());
    }

    public UIAsyncListProperty(Object bean, UIPropertyMetadata<AsyncObservableList<E>> metadata) {
        super(bean, metadata);
    }

    public UIAsyncListProperty(ObservableObject bean, UIPropertyMetadata<AsyncObservableList<E>> metadata) {
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
        ReadOnlyListProperty<E> readOnlyProperty = this.readOnlyProperty;
        if (readOnlyProperty == null) {
            AccessControllerImpl accessController = (AccessControllerImpl)getAccessController();
            long stamp = 0;
            try {
                try (LockedList<E> lockedList = lock()) {
                    readOnlyProperty = this.readOnlyProperty;
                    if (readOnlyProperty == null) {
                        this.readOnlyProperty =
                            readOnlyProperty = new ReadOnlyListPropertyImpl<>(getBean(), getName(), this, lockedList);
                    }

                    return readOnlyProperty;
                }

            } finally {
                accessController.unlockWrite(VALUE, stamp);
            }
        }

        return readOnlyProperty;
    }

    @Override
    public @NotNull ListIterator<E> listIterator() {
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
    public @NotNull ListIterator<E> listIterator(int i) {
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
    @SafeVarargs
    public final boolean setAll(E... elements) {
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
    @SafeVarargs
    public final boolean addAll(E... elements) {
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
    @SafeVarargs
    public final boolean removeAll(E... elements) {
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
    @SafeVarargs
    public final boolean retainAll(E... elements) {
        verifyAccess();
        return super.retainAll(elements);
    }

    @Override
    public void replaceAll(UnaryOperator<E> operator) {
        verifyAccess();
        super.replaceAll(operator);
    }

    private void verifyAccess() {
        if ((AsyncFX.isVerifyPropertyAccess() || AsyncFX.isRunningTests()) && !UIDispatcher.isDispatcherThread()) {
            throw new IllegalStateException(
                "Illegal cross-thread access: list can only be modified on the JavaFX application thread.");
        }
    }

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private static class ReadOnlyListPropertyImpl<E> extends SimpleListProperty<E> {

        private static class FromWithElements<T> {
            final int from;
            final List<T> elements;

            FromWithElements(int from, List<T> elements) {
                this.from = from;
                this.elements = elements;
            }
        }

        private class Permuter extends ArrayDeque<int[]> implements Runnable {
            Permuter() {
                super(1);
            }

            @Override
            public synchronized void run() {
                while (!isEmpty()) {
                    permuteImpl(pop());
                }
            }

            synchronized void permuteElements(int[] indexMap) {
                if (UIDispatcher.isDispatcherThread()) {
                    permuteImpl(indexMap);
                } else {
                    addLast(indexMap);
                    UIDispatcher.run(this);
                }
            }

            private void permuteImpl(int[] indexMap) {
                int from = indexMap[indexMap.length - 2];
                int to = indexMap[indexMap.length - 1];

                List<E> copy = new ArrayList<>(ReadOnlyListPropertyImpl.this.get().subList(from, to));
                for (int i = 0; i < to - from; ++i) {
                    int newIndex = indexMap[i];
                    ReadOnlyListPropertyImpl.this.set(newIndex, copy.get(i));
                }
            }
        }

        private class Updater extends ArrayDeque<int[]> implements Runnable {
            Updater() {
                super(1);
            }

            @Override
            public synchronized void run() {
                while (!isEmpty()) {
                    int[] fromTo = pop();
                    fireChange(fromTo[0], fromTo[1]);
                }
            }

            synchronized void updateElements(int from, int to) {
                if (UIDispatcher.isDispatcherThread()) {
                    fireChange(from, to);
                } else {
                    addLast(new int[] {from, to});
                    UIDispatcher.runLater(this);
                }
            }

            private void fireChange(int from, int to) {
                ListChangeListener.Change<E> updateChange =
                    new ListChangeListener.Change<>(ReadOnlyListPropertyImpl.this.get()) {
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
                        public boolean wasUpdated() {
                            return true;
                        }
                    };

                fireValueChangedEvent(updateChange);
            }
        }

        private class Adder extends ArrayDeque<FromWithElements<? extends E>> implements Runnable {
            Adder() {
                super(1);
            }

            @Override
            public synchronized void run() {
                while (!isEmpty()) {
                    FromWithElements<? extends E> entry = pop();
                    ReadOnlyListPropertyImpl.this.addAll(entry.from, entry.elements);
                }
            }

            synchronized void addElements(int from, List<? extends E> list) {
                if (UIDispatcher.isDispatcherThread()) {
                    ReadOnlyListPropertyImpl.this.addAll(from, list);
                } else {
                    addLast(new FromWithElements<>(from, new ArrayList<>(list)));
                    UIDispatcher.runLater(this);
                }
            }
        }

        private class Remover extends ArrayDeque<int[]> implements Runnable {
            Remover() {
                super(1);
            }

            @Override
            public synchronized void run() {
                while (!isEmpty()) {
                    int[] fromTo = pop();
                    ReadOnlyListPropertyImpl.this.remove(fromTo[0], fromTo[1]);
                }
            }

            synchronized void removeElements(int from, int to) {
                if (UIDispatcher.isDispatcherThread()) {
                    ReadOnlyListPropertyImpl.this.remove(from, to);
                } else {
                    addLast(new int[] {from, to});
                    UIDispatcher.runLater(this);
                }
            }
        }

        private class Replacer extends ArrayDeque<FromWithElements<? extends E>> implements Runnable {
            Replacer() {
                super(1);
            }

            @Override
            public synchronized void run() {
                while (!isEmpty()) {
                    FromWithElements<? extends E> entry = pop();
                    replaceImpl(entry.from, entry.elements);
                }
            }

            synchronized void replaceElements(int from, List<? extends E> addedList) {
                if (UIDispatcher.isDispatcherThread()) {
                    replaceImpl(from, addedList);
                } else {
                    addLast(new FromWithElements<>(from, new ArrayList<>(addedList)));
                    UIDispatcher.runLater(this);
                }
            }

            private void replaceImpl(int from, List<? extends E> addedList) {
                int index = from;
                int currentSize = ReadOnlyListPropertyImpl.this.size();
                for (E added : addedList) {
                    if (index < currentSize) {
                        ReadOnlyListPropertyImpl.this.set(index++, added);
                    } else {
                        ReadOnlyListPropertyImpl.this.add(added);
                    }
                }
            }
        }

        private final Permuter permuter = new Permuter();
        private final Updater updater = new Updater();
        private final Adder adder = new Adder();
        private final Remover remover = new Remover();
        private final Replacer replacer = new Replacer();

        @SuppressWarnings({"unchecked", "FieldCanBeLocal"})
        private final AsyncListChangeListener<E> listener =
            change -> {
                while (change.next()) {
                    final int from = change.getFrom();
                    final int to = change.getTo();

                    if (change.wasPermutated()) {
                        final int[] indexMap = new int[to - from + 2];
                        for (int i = from; i < to; ++i) {
                            indexMap[i - from] = change.getPermutation(i);
                        }

                        indexMap[to] = from;
                        indexMap[to + 1] = to;
                        permuter.permuteElements(indexMap);
                    } else if (change.wasUpdated()) {
                        updater.updateElements(from, to);
                    } else {
                        if (change.wasReplaced()) {
                            AsyncObservableList<? extends E> list;
                            if (change instanceof UnsafeListAccess) {
                                list = ((UnsafeListAccess<? extends E>)change).getListUnsafe();
                            } else {
                                throw new IllegalArgumentException("Unsupported change type.");
                            }

                            try (LockedList<? extends E> lockedList = list.lock()) {
                                replacer.replaceElements(from, lockedList.subList(from, to));
                            }
                        } else {
                            if (change.wasRemoved()) {
                                final int toRemoved = from + change.getRemovedSize();
                                remover.removeElements(from, toRemoved);
                            }

                            if (change.wasAdded()) {
                                AsyncObservableList<? extends E> list;
                                if (change instanceof UnsafeListAccess) {
                                    list = ((UnsafeListAccess<? extends E>)change).getListUnsafe();
                                } else {
                                    throw new IllegalArgumentException("Unsupported change type.");
                                }

                                try (LockedList<? extends E> lockedList = list.lock()) {
                                    adder.addElements(from, lockedList.subList(from, to));
                                }
                            }
                        }
                    }
                }
            };

        ReadOnlyListPropertyImpl(Object bean, String name, AsyncListProperty<E> list, LockedList<E> source) {
            super(bean, name, FXCollections.observableArrayList());
            setAll(source);
            list.addListener(new WeakListChangeListener<>(listener));
        }

    }

}
