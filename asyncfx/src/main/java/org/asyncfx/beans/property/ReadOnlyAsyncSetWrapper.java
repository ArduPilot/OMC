/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.beans.property;

import java.util.concurrent.Executor;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.collections.SetChangeListener;
import org.asyncfx.PublishSource;
import org.asyncfx.beans.AccessController;
import org.asyncfx.beans.SubInvalidationListener;
import org.asyncfx.beans.value.SubChangeListener;
import org.asyncfx.collections.AsyncObservableSet;
import org.asyncfx.collections.LockedSet;

@PublishSource(module = "openjfx", licenses = "intel-gpl-classpath-exception")
public class ReadOnlyAsyncSetWrapper<E> extends SimpleAsyncSetProperty<E> {

    private final ReadOnlyAsyncPropertyImpl readOnlyProperty = new ReadOnlyAsyncPropertyImpl();

    public ReadOnlyAsyncSetWrapper(Object bean) {
        super(bean);
    }

    public ReadOnlyAsyncSetWrapper(Object bean, PropertyMetadata<AsyncObservableSet<E>> metadata) {
        super(bean, metadata);
    }

    public ReadOnlyAsyncSetProperty<E> getReadOnlyProperty() {
        return readOnlyProperty;
    }

    private class ReadOnlyAsyncPropertyImpl extends ReadOnlyAsyncSetProperty<E> {
        @Override
        public long getUniqueId() {
            return ReadOnlyAsyncSetWrapper.this.getUniqueId();
        }

        @Override
        public AsyncObservableSet<E> getUncritical() {
            return ReadOnlyAsyncSetWrapper.this.getUncritical();
        }

        @Override
        public ReadOnlyAsyncIntegerProperty sizeProperty() {
            return ReadOnlyAsyncSetWrapper.this.sizeProperty();
        }

        @Override
        public LockedSet<E> lock() {
            return ReadOnlyAsyncSetWrapper.this.lock();
        }

        @Override
        public ReadOnlyAsyncBooleanProperty emptyProperty() {
            return ReadOnlyAsyncSetWrapper.this.emptyProperty();
        }

        @Override
        public AsyncObservableSet<E> get() {
            return ReadOnlyAsyncSetWrapper.this.get();
        }

        @Override
        public Object getBean() {
            return ReadOnlyAsyncSetWrapper.this.getBean();
        }

        @Override
        public String getName() {
            return ReadOnlyAsyncSetWrapper.this.getName();
        }

        @Override
        public PropertyMetadata<AsyncObservableSet<E>> getMetadata() {
            return ReadOnlyAsyncSetWrapper.this.getMetadata();
        }

        @Override
        public AccessController getAccessController() {
            return ReadOnlyAsyncSetWrapper.this.getAccessController();
        }

        @Override
        public void addListener(InvalidationListener listener) {
            ReadOnlyAsyncSetWrapper.this.addListener(listener);
        }

        @Override
        public void addListener(InvalidationListener listener, Executor executor) {
            ReadOnlyAsyncSetWrapper.this.addListener(listener, executor);
        }

        @Override
        public void addListener(SubInvalidationListener listener) {
            ReadOnlyAsyncSetWrapper.this.addListener(listener);
        }

        @Override
        public void addListener(SubInvalidationListener listener, Executor executor) {
            ReadOnlyAsyncSetWrapper.this.addListener(listener, executor);
        }

        @Override
        public void addListener(ChangeListener<? super AsyncObservableSet<E>> listener) {
            ReadOnlyAsyncSetWrapper.this.addListener(listener);
        }

        @Override
        public void addListener(ChangeListener<? super AsyncObservableSet<E>> listener, Executor executor) {
            ReadOnlyAsyncSetWrapper.this.addListener(listener, executor);
        }

        @Override
        public void addListener(SubChangeListener listener) {
            ReadOnlyAsyncSetWrapper.this.addListener(listener);
        }

        @Override
        public void addListener(SubChangeListener listener, Executor executor) {
            ReadOnlyAsyncSetWrapper.this.addListener(listener, executor);
        }

        @Override
        public void addListener(SetChangeListener<? super E> listener) {
            ReadOnlyAsyncSetWrapper.this.addListener(listener);
        }

        @Override
        public void addListener(SetChangeListener<? super E> listener, Executor executor) {
            ReadOnlyAsyncSetWrapper.this.addListener(listener, executor);
        }

        @Override
        public void removeListener(ChangeListener<? super AsyncObservableSet<E>> listener) {
            ReadOnlyAsyncSetWrapper.this.removeListener(listener);
        }

        @Override
        public void removeListener(SubChangeListener listener) {
            ReadOnlyAsyncSetWrapper.this.removeListener(listener);
        }

        @Override
        public void removeListener(InvalidationListener listener) {
            ReadOnlyAsyncSetWrapper.this.removeListener(listener);
        }

        @Override
        public void removeListener(SubInvalidationListener listener) {
            ReadOnlyAsyncSetWrapper.this.removeListener(listener);
        }

        @Override
        public void removeListener(SetChangeListener<? super E> listener) {
            ReadOnlyAsyncSetWrapper.this.removeListener(listener);
        }
    }

}
