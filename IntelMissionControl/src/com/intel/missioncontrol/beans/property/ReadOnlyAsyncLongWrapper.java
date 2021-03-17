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
public class ReadOnlyAsyncLongWrapper extends SimpleAsyncLongProperty {

    private final ReadOnlyAsyncPropertyImpl readOnlyProperty = new ReadOnlyAsyncPropertyImpl();

    public ReadOnlyAsyncLongWrapper(Object bean) {
        super(bean);
    }

    public ReadOnlyAsyncLongWrapper(Object bean, PropertyMetadata<Number> metadata) {
        super(bean, metadata);
    }

    public ReadOnlyAsyncLongProperty getReadOnlyProperty() {
        return readOnlyProperty;
    }

    private class ReadOnlyAsyncPropertyImpl extends ReadOnlyAsyncLongProperty {
        @Override
        public long get() {
            return ReadOnlyAsyncLongWrapper.this.get();
        }

        @Override
        public Object getBean() {
            return ReadOnlyAsyncLongWrapper.this.getBean();
        }

        @Override
        public String getName() {
            return ReadOnlyAsyncLongWrapper.this.getName();
        }

        @Override
        public void addListener(InvalidationListener listener, Executor executor) {
            ReadOnlyAsyncLongWrapper.this.addListener(listener, executor);
        }

        @Override
        public void addListener(ChangeListener<? super Number> listener, Executor executor) {
            ReadOnlyAsyncLongWrapper.this.addListener(listener, executor);
        }

        @Override
        public void addListener(ChangeListener<? super Number> listener) {
            ReadOnlyAsyncLongWrapper.this.addListener(listener);
        }

        @Override
        public void addListener(InvalidationListener listener) {
            ReadOnlyAsyncLongWrapper.this.addListener(listener);
        }

        @Override
        public void removeListener(ChangeListener<? super Number> listener) {
            ReadOnlyAsyncLongWrapper.this.removeListener(listener);
        }

        @Override
        public void removeListener(InvalidationListener listener) {
            ReadOnlyAsyncLongWrapper.this.removeListener(listener);
        }
    }

}
