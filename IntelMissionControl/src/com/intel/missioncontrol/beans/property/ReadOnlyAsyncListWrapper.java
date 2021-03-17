/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.beans.property;

import com.intel.missioncontrol.PublishSource;
import com.intel.missioncontrol.collections.AsyncObservableList;
import com.intel.missioncontrol.collections.LockedList;
import java.util.concurrent.Executor;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;

@PublishSource(module = "openjfx", licenses = "intel-gpl-classpath-exception")
public class ReadOnlyAsyncListWrapper<E> extends SimpleAsyncListProperty<E> {

    private final ReadOnlyAsyncPropertyImpl readOnlyProperty = new ReadOnlyAsyncPropertyImpl();

    public ReadOnlyAsyncListWrapper(Object bean) {
        super(bean);
    }

    public ReadOnlyAsyncListWrapper(Object bean, PropertyMetadata<AsyncObservableList<E>> metadata) {
        super(bean, metadata);
    }

    public ReadOnlyAsyncListProperty<E> getReadOnlyProperty() {
        return readOnlyProperty;
    }

    private class ReadOnlyAsyncPropertyImpl extends ReadOnlyAsyncListProperty<E> {
        @Override
        public ReadOnlyAsyncIntegerProperty sizeProperty() {
            return ReadOnlyAsyncListWrapper.this.sizeProperty();
        }

        @Override
        public LockedList<E> lock() {
            return ReadOnlyAsyncListWrapper.this.lock();
        }

        @Override
        public ReadOnlyAsyncBooleanProperty emptyProperty() {
            return ReadOnlyAsyncListWrapper.this.emptyProperty();
        }

        @Override
        public AsyncObservableList<E> get() {
            return ReadOnlyAsyncListWrapper.this.get();
        }

        @Override
        public Object getBean() {
            return ReadOnlyAsyncListWrapper.this.getBean();
        }

        @Override
        public String getName() {
            return ReadOnlyAsyncListWrapper.this.getName();
        }

        @Override
        public void addListener(InvalidationListener listener, Executor executor) {
            ReadOnlyAsyncListWrapper.this.addListener(listener, executor);
        }

        @Override
        public void addListener(ChangeListener<? super AsyncObservableList<E>> listener, Executor executor) {
            ReadOnlyAsyncListWrapper.this.addListener(listener, executor);
        }

        @Override
        public void addListener(ChangeListener<? super AsyncObservableList<E>> listener) {
            ReadOnlyAsyncListWrapper.this.addListener(listener);
        }

        @Override
        public void addListener(ListChangeListener<? super E> listener) {
            ReadOnlyAsyncListWrapper.this.addListener(listener);
        }

        @Override
        public void addListener(ListChangeListener<? super E> listener, Executor executor) {
            ReadOnlyAsyncListWrapper.this.addListener(listener, executor);
        }

        @Override
        public void addListener(InvalidationListener listener) {
            ReadOnlyAsyncListWrapper.this.addListener(listener);
        }

        @Override
        public void removeListener(ChangeListener<? super AsyncObservableList<E>> listener) {
            ReadOnlyAsyncListWrapper.this.removeListener(listener);
        }

        @Override
        public void removeListener(InvalidationListener listener) {
            ReadOnlyAsyncListWrapper.this.removeListener(listener);
        }

        @Override
        public void removeListener(ListChangeListener<? super E> listener) {
            ReadOnlyAsyncListWrapper.this.removeListener(listener);
        }
    }

}
