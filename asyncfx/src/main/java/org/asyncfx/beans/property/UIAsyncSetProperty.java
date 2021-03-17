/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.beans.property;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashSet;
import java.util.function.Predicate;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlySetProperty;
import javafx.beans.property.SimpleSetProperty;
import javafx.collections.FXCollections;
import javafx.collections.SetChangeListener;
import javafx.collections.WeakSetChangeListener;
import org.asyncfx.PublishSource;
import org.asyncfx.beans.AccessControllerImpl;
import org.asyncfx.collections.AsyncObservableSet;
import org.asyncfx.collections.LockedSet;

@PublishSource(
    module = "openjfx",
    licenses = {"openjfx", "intel-gpl-classpath-exception"}
)
public class UIAsyncSetProperty<E> extends SimpleAsyncSetProperty<E> {

    private ReadOnlySetProperty<E> readOnlyProperty;

    public UIAsyncSetProperty(Object bean) {
        this(bean, new UIPropertyMetadata.Builder<AsyncObservableSet<E>>().create());
    }

    public UIAsyncSetProperty(PropertyObject bean) {
        this(bean, new UIPropertyMetadata.Builder<AsyncObservableSet<E>>().create());
    }

    public UIAsyncSetProperty(Object bean, UIPropertyMetadata<AsyncObservableSet<E>> metadata) {
        super(bean, metadata);
    }

    public UIAsyncSetProperty(PropertyObject bean, UIPropertyMetadata<AsyncObservableSet<E>> metadata) {
        super(bean, metadata);
    }

    @Override
    public void overrideMetadata(PropertyMetadata<AsyncObservableSet<E>> metadata) {
        if (!(metadata instanceof UIPropertyMetadata)) {
            throw new IllegalArgumentException(
                "Metadata can only be overridden with an instance of " + UIPropertyMetadata.class.getSimpleName());
        }

        super.overrideMetadata(metadata);
    }

    @Override
    public void set(AsyncObservableSet<E> newValue) {
        verifyAccess();
        super.set(newValue);
    }

    @Override
    public boolean add(E element) {
        verifyAccess();
        return super.add(element);
    }

    @Override
    public boolean addAll(Collection<? extends E> elements) {
        verifyAccess();
        return super.addAll(elements);
    }

    @Override
    public boolean remove(Object obj) {
        verifyAccess();
        return super.remove(obj);
    }

    @Override
    public boolean removeAll(Collection<?> objects) {
        verifyAccess();
        return super.removeAll(objects);
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

    public ReadOnlySetProperty<E> getReadOnlyProperty() {
        ReadOnlySetProperty<E> readOnlyProperty = this.readOnlyProperty;
        if (readOnlyProperty == null) {
            AccessControllerImpl accessController = (AccessControllerImpl)getAccessController();
            long stamp = 0;
            try {
                try (LockedSet<E> lockedList = lock()) {
                    readOnlyProperty = this.readOnlyProperty;
                    if (readOnlyProperty == null) {
                        this.readOnlyProperty =
                            readOnlyProperty = new ReadOnlySetPropertyImpl<>(getBean(), getName(), this, lockedList);
                    }

                    return readOnlyProperty;
                }

            } finally {
                accessController.unlockWrite(stamp);
            }
        }

        return readOnlyProperty;
    }

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private static class ReadOnlySetPropertyImpl<E> extends SimpleSetProperty<E> {

        private class Adder extends ArrayDeque<E> implements Runnable {
            Adder() {
                super(1);
            }

            @Override
            public synchronized void run() {
                while (!isEmpty()) {
                    ReadOnlySetPropertyImpl.this.add(pop());
                }
            }

            synchronized void addElement(E e) {
                if (Platform.isFxApplicationThread()) {
                    ReadOnlySetPropertyImpl.this.add(e);
                } else {
                    addLast(e);
                    Platform.runLater(this);
                }
            }
        }

        private class Remover extends ArrayDeque<E> implements Runnable {
            Remover() {
                super(1);
            }

            @Override
            public synchronized void run() {
                while (!isEmpty()) {
                    ReadOnlySetPropertyImpl.this.remove(pop());
                }
            }

            synchronized void removeElement(E e) {
                if (Platform.isFxApplicationThread()) {
                    ReadOnlySetPropertyImpl.this.remove(e);
                } else {
                    addLast(e);
                    Platform.runLater(this);
                }
            }
        }

        private final Adder adder = new Adder();
        private final Remover remover = new Remover();

        @SuppressWarnings("FieldCanBeLocal")
        private final SetChangeListener<E> listener =
            change -> {
                if (change.wasRemoved()) {
                    remover.removeElement(change.getElementRemoved());
                }

                if (change.wasAdded()) {
                    adder.addElement(change.getElementAdded());
                }
            };

        ReadOnlySetPropertyImpl(Object bean, String name, AsyncSetProperty<E> list, LockedSet<E> source) {
            super(bean, name, FXCollections.observableSet(new HashSet<>()));
            addAll(source);
            list.addListener(new WeakSetChangeListener<>(listener));
        }

    }

}
