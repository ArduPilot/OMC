/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.beans.property;

import com.intel.missioncontrol.PublishSource;
import com.intel.missioncontrol.collections.AsyncObservableSet;
import com.intel.missioncontrol.collections.LockedSet;
import java.util.Collection;
import java.util.function.Predicate;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanPropertyBase;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerPropertyBase;
import javafx.beans.property.ReadOnlySetProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;

@PublishSource(
    module = "openjfx",
    licenses = {"openjfx", "intel-gpl-classpath-exception"}
)
public class UIAsyncSetProperty<E> extends SimpleAsyncSetProperty<E> {

    private ReadOnlySetProperty<E> readOnlyProperty;

    public UIAsyncSetProperty(Object bean) {
        this(bean, new UIPropertyMetadata.Builder<AsyncObservableSet<E>>().create());
    }

    public UIAsyncSetProperty(Object bean, UIPropertyMetadata<AsyncObservableSet<E>> metadata) {
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
        synchronized (mutex) {
            if (readOnlyProperty == null) {
                readOnlyProperty = new ReadOnlySetPropertyImpl();
            }

            return readOnlyProperty;
        }
    }

    private void verifyAccess() {
        if (PropertyHelper.isVerifyPropertyAccessEnabled() && !Platform.isFxApplicationThread()) {
            throw new IllegalStateException(
                "Illegal cross-thread access: list can only be modified on the JavaFX application thread.");
        }
    }

    private class ReadOnlySetPropertyImpl extends ReadOnlySetProperty<E> {
        private class SizeProperty extends ReadOnlyIntegerPropertyBase {
            @Override
            public Object getBean() {
                return ReadOnlySetPropertyImpl.this;
            }

            @Override
            public String getName() {
                return "size";
            }

            @Override
            public int get() {
                return set != null ? set.size() : 0;
            }

            @Override
            protected void fireValueChangedEvent() {
                super.fireValueChangedEvent();
            }
        }

        private class EmptyProperty extends ReadOnlyBooleanPropertyBase {
            @Override
            public Object getBean() {
                return ReadOnlySetPropertyImpl.this;
            }

            @Override
            public String getName() {
                return "empty";
            }

            @Override
            public boolean get() {
                return set == null || set.isEmpty();
            }

            @Override
            protected void fireValueChangedEvent() {
                super.fireValueChangedEvent();
            }
        }

        private ObservableSet<E> set;
        private final SizeProperty size = new SizeProperty();
        private final EmptyProperty empty = new EmptyProperty();

        ReadOnlySetPropertyImpl() {
            AsyncObservableSet<E> asyncList = UIAsyncSetProperty.this.get();
            LockedSet<E> lockedSet = null;
            if (asyncList != null) {
                lockedSet = asyncList.lock();
                set = FXCollections.observableSet(lockedSet);
            } else {
                set = null;
            }

            UIAsyncSetProperty.this.addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue == null) {
                        set = null;
                    } else {
                        try (LockedSet<E> view = newValue.lock()) {
                            set = FXCollections.observableSet(view);
                        }
                    }
                },
                UIAsyncSetProperty.this.getMetadata().getExecutor());

            UIAsyncSetProperty.this.addListener(
                (SetChangeListener<E>)
                    change -> {
                        if (change.wasRemoved()) {
                            set.remove(change.getElementRemoved());
                        }

                        if (change.wasAdded()) {
                            set.add(change.getElementAdded());
                        }
                    },
                UIAsyncSetProperty.this.getMetadata().getExecutor());

            UIAsyncSetProperty.this
                .sizeProperty()
                .addListener(
                    listener -> size.fireValueChangedEvent(), UIAsyncSetProperty.this.getMetadata().getExecutor());

            UIAsyncSetProperty.this
                .emptyProperty()
                .addListener(
                    listener -> empty.fireValueChangedEvent(), UIAsyncSetProperty.this.getMetadata().getExecutor());

            if (lockedSet != null) {
                lockedSet.close();
            }
        }

        @Override
        public ReadOnlyIntegerProperty sizeProperty() {
            return size;
        }

        @Override
        public ReadOnlyBooleanProperty emptyProperty() {
            return empty;
        }

        @Override
        public Object getBean() {
            return UIAsyncSetProperty.this.getBean();
        }

        @Override
        public String getName() {
            return UIAsyncSetProperty.this.getName();
        }

        @Override
        public ObservableSet<E> get() {
            return set;
        }

        @Override
        public void addListener(ChangeListener<? super ObservableSet<E>> listener) {
            UIAsyncSetProperty.this.addListener(listener);
        }

        @Override
        public void removeListener(ChangeListener<? super ObservableSet<E>> listener) {
            UIAsyncSetProperty.this.removeListener(listener);
        }

        @Override
        public void addListener(SetChangeListener<? super E> listener) {
            set.addListener(listener);
        }

        @Override
        public void removeListener(SetChangeListener<? super E> listener) {
            set.removeListener(listener);
        }

        @Override
        public void addListener(InvalidationListener listener) {
            set.addListener(listener);
        }

        @Override
        public void removeListener(InvalidationListener listener) {
            set.removeListener(listener);
        }
    }

}
