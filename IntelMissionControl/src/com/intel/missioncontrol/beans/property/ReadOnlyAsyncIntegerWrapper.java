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
public class ReadOnlyAsyncIntegerWrapper extends SimpleAsyncIntegerProperty {

    private final ReadOnlyAsyncPropertyImpl readOnlyProperty = new ReadOnlyAsyncPropertyImpl();

    public ReadOnlyAsyncIntegerWrapper(Object bean) {
        super(bean);
    }

    public ReadOnlyAsyncIntegerWrapper(Object bean, PropertyMetadata<Number> metadata) {
        super(bean, metadata);
    }

    public ReadOnlyAsyncIntegerProperty getReadOnlyProperty() {
        return readOnlyProperty;
    }

    private class ReadOnlyAsyncPropertyImpl extends ReadOnlyAsyncIntegerProperty {
        @Override
        public int get() {
            return ReadOnlyAsyncIntegerWrapper.this.get();
        }

        @Override
        public Object getBean() {
            return ReadOnlyAsyncIntegerWrapper.this.getBean();
        }

        @Override
        public String getName() {
            return ReadOnlyAsyncIntegerWrapper.this.getName();
        }

        @Override
        public void addListener(InvalidationListener listener, Executor executor) {
            ReadOnlyAsyncIntegerWrapper.this.addListener(listener, executor);
        }

        @Override
        public void addListener(ChangeListener<? super Number> listener, Executor executor) {
            ReadOnlyAsyncIntegerWrapper.this.addListener(listener, executor);
        }

        @Override
        public void addListener(ChangeListener<? super Number> listener) {
            ReadOnlyAsyncIntegerWrapper.this.addListener(listener);
        }

        @Override
        public void addListener(InvalidationListener listener) {
            ReadOnlyAsyncIntegerWrapper.this.addListener(listener);
        }

        @Override
        public void removeListener(ChangeListener<? super Number> listener) {
            ReadOnlyAsyncIntegerWrapper.this.removeListener(listener);
        }

        @Override
        public void removeListener(InvalidationListener listener) {
            ReadOnlyAsyncIntegerWrapper.this.removeListener(listener);
        }
    }

}
