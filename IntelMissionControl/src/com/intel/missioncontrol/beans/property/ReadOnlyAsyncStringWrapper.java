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
public class ReadOnlyAsyncStringWrapper extends SimpleAsyncStringProperty {

    private final ReadOnlyAsyncPropertyImpl readOnlyProperty = new ReadOnlyAsyncPropertyImpl();

    public ReadOnlyAsyncStringWrapper(Object bean) {
        super(bean);
    }

    public ReadOnlyAsyncStringWrapper(Object bean, PropertyMetadata<String> metadata) {
        super(bean, metadata);
    }

    public ReadOnlyAsyncStringProperty getReadOnlyProperty() {
        return readOnlyProperty;
    }

    private class ReadOnlyAsyncPropertyImpl extends ReadOnlyAsyncStringProperty {
        @Override
        public String get() {
            return ReadOnlyAsyncStringWrapper.this.get();
        }

        @Override
        public Object getBean() {
            return ReadOnlyAsyncStringWrapper.this.getBean();
        }

        @Override
        public String getName() {
            return ReadOnlyAsyncStringWrapper.this.getName();
        }

        @Override
        public void addListener(InvalidationListener listener, Executor executor) {
            ReadOnlyAsyncStringWrapper.this.addListener(listener, executor);
        }

        @Override
        public void addListener(ChangeListener<? super String> listener, Executor executor) {
            ReadOnlyAsyncStringWrapper.this.addListener(listener, executor);
        }

        @Override
        public void addListener(ChangeListener<? super String> listener) {
            ReadOnlyAsyncStringWrapper.this.addListener(listener);
        }

        @Override
        public void addListener(InvalidationListener listener) {
            ReadOnlyAsyncStringWrapper.this.addListener(listener);
        }

        @Override
        public void removeListener(ChangeListener<? super String> listener) {
            ReadOnlyAsyncStringWrapper.this.removeListener(listener);
        }

        @Override
        public void removeListener(InvalidationListener listener) {
            ReadOnlyAsyncStringWrapper.this.removeListener(listener);
        }
    }

}
