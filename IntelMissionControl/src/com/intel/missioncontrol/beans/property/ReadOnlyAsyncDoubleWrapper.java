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
public class ReadOnlyAsyncDoubleWrapper extends SimpleAsyncDoubleProperty {

    private final ReadOnlyAsyncPropertyImpl readOnlyProperty = new ReadOnlyAsyncPropertyImpl();

    public ReadOnlyAsyncDoubleWrapper(Object bean) {
        super(bean);
    }

    public ReadOnlyAsyncDoubleWrapper(Object bean, PropertyMetadata<Number> metadata) {
        super(bean, metadata);
    }

    public ReadOnlyAsyncDoubleProperty getReadOnlyProperty() {
        return readOnlyProperty;
    }

    private class ReadOnlyAsyncPropertyImpl extends ReadOnlyAsyncDoubleProperty {
        @Override
        public double get() {
            return ReadOnlyAsyncDoubleWrapper.this.get();
        }

        @Override
        public Object getBean() {
            return ReadOnlyAsyncDoubleWrapper.this.getBean();
        }

        @Override
        public String getName() {
            return ReadOnlyAsyncDoubleWrapper.this.getName();
        }

        @Override
        public void addListener(InvalidationListener listener, Executor executor) {
            ReadOnlyAsyncDoubleWrapper.this.addListener(listener, executor);
        }

        @Override
        public void addListener(ChangeListener<? super Number> listener, Executor executor) {
            ReadOnlyAsyncDoubleWrapper.this.addListener(listener, executor);
        }

        @Override
        public void addListener(ChangeListener<? super Number> listener) {
            ReadOnlyAsyncDoubleWrapper.this.addListener(listener);
        }

        @Override
        public void addListener(InvalidationListener listener) {
            ReadOnlyAsyncDoubleWrapper.this.addListener(listener);
        }

        @Override
        public void removeListener(ChangeListener<? super Number> listener) {
            ReadOnlyAsyncDoubleWrapper.this.removeListener(listener);
        }

        @Override
        public void removeListener(InvalidationListener listener) {
            ReadOnlyAsyncDoubleWrapper.this.removeListener(listener);
        }
    }

}
