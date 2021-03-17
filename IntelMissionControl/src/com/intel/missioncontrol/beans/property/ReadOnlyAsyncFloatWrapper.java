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
public class ReadOnlyAsyncFloatWrapper extends SimpleAsyncFloatProperty {

    private final ReadOnlyAsyncPropertyImpl readOnlyProperty = new ReadOnlyAsyncPropertyImpl();

    public ReadOnlyAsyncFloatWrapper(Object bean) {
        super(bean);
    }

    public ReadOnlyAsyncFloatWrapper(Object bean, PropertyMetadata<Number> metadata) {
        super(bean, metadata);
    }

    public ReadOnlyAsyncFloatProperty getReadOnlyProperty() {
        return readOnlyProperty;
    }

    private class ReadOnlyAsyncPropertyImpl extends ReadOnlyAsyncFloatProperty {
        @Override
        public float get() {
            return ReadOnlyAsyncFloatWrapper.this.get();
        }

        @Override
        public Object getBean() {
            return ReadOnlyAsyncFloatWrapper.this.getBean();
        }

        @Override
        public String getName() {
            return ReadOnlyAsyncFloatWrapper.this.getName();
        }

        @Override
        public void addListener(InvalidationListener listener, Executor executor) {
            ReadOnlyAsyncFloatWrapper.this.addListener(listener, executor);
        }

        @Override
        public void addListener(ChangeListener<? super Number> listener, Executor executor) {
            ReadOnlyAsyncFloatWrapper.this.addListener(listener, executor);
        }

        @Override
        public void addListener(ChangeListener<? super Number> listener) {
            ReadOnlyAsyncFloatWrapper.this.addListener(listener);
        }

        @Override
        public void addListener(InvalidationListener listener) {
            ReadOnlyAsyncFloatWrapper.this.addListener(listener);
        }

        @Override
        public void removeListener(ChangeListener<? super Number> listener) {
            ReadOnlyAsyncFloatWrapper.this.removeListener(listener);
        }

        @Override
        public void removeListener(InvalidationListener listener) {
            ReadOnlyAsyncFloatWrapper.this.removeListener(listener);
        }
    }

}
