/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.beans.property;

import com.intel.missioncontrol.PublishSource;
import java.util.concurrent.Executor;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;

@PublishSource(module = "openjfx", licenses = "intel-gpl-classpath-exception")
public class ReadOnlyAsyncObjectWrapper<T> extends SimpleAsyncObjectProperty<T> {

    private final ReadOnlyAsyncPropertyImpl readOnlyProperty = new ReadOnlyAsyncPropertyImpl();

    public ReadOnlyAsyncObjectWrapper(Object bean) {
        super(bean);
    }

    public ReadOnlyAsyncObjectWrapper(Object bean, PropertyMetadata<T> metadata) {
        super(bean, metadata);
    }

    public ReadOnlyAsyncObjectProperty<T> getReadOnlyProperty() {
        return readOnlyProperty;
    }

    private class ReadOnlyAsyncPropertyImpl extends ReadOnlyAsyncObjectProperty<T> {
        @Override
        public T get() {
            return ReadOnlyAsyncObjectWrapper.this.get();
        }

        @Override
        public Object getBean() {
            return ReadOnlyAsyncObjectWrapper.this.getBean();
        }

        @Override
        public String getName() {
            return ReadOnlyAsyncObjectWrapper.this.getName();
        }

        @Override
        public void addListener(InvalidationListener listener, Executor executor) {
            ReadOnlyAsyncObjectWrapper.this.addListener(listener, executor);
        }

        @Override
        public void addListener(ChangeListener<? super T> listener, Executor executor) {
            ReadOnlyAsyncObjectWrapper.this.addListener(listener, executor);
        }

        @Override
        public void addListener(ChangeListener<? super T> listener) {
            ReadOnlyAsyncObjectWrapper.this.addListener(listener);
        }

        @Override
        public void addListener(InvalidationListener listener) {
            ReadOnlyAsyncObjectWrapper.this.addListener(listener);
        }

        @Override
        public void removeListener(ChangeListener<? super T> listener) {
            ReadOnlyAsyncObjectWrapper.this.removeListener(listener);
        }

        @Override
        public void removeListener(InvalidationListener listener) {
            ReadOnlyAsyncObjectWrapper.this.removeListener(listener);
        }
    }

}
