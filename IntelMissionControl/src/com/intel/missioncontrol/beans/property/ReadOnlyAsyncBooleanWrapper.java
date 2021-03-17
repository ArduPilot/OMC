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
public class ReadOnlyAsyncBooleanWrapper extends SimpleAsyncBooleanProperty {

    private final ReadOnlyAsyncPropertyImpl readOnlyProperty = new ReadOnlyAsyncPropertyImpl();

    public ReadOnlyAsyncBooleanWrapper(Object bean) {
        super(bean);
    }

    public ReadOnlyAsyncBooleanWrapper(Object bean, PropertyMetadata<Boolean> metadata) {
        super(bean, metadata);
    }

    public ReadOnlyAsyncBooleanProperty getReadOnlyProperty() {
        return readOnlyProperty;
    }

    private class ReadOnlyAsyncPropertyImpl extends ReadOnlyAsyncBooleanProperty {
        @Override
        public boolean get() {
            return ReadOnlyAsyncBooleanWrapper.this.get();
        }

        @Override
        public Object getBean() {
            return ReadOnlyAsyncBooleanWrapper.this.getBean();
        }

        @Override
        public String getName() {
            return ReadOnlyAsyncBooleanWrapper.this.getName();
        }

        @Override
        public void addListener(InvalidationListener listener, Executor executor) {
            ReadOnlyAsyncBooleanWrapper.this.addListener(listener, executor);
        }

        @Override
        public void addListener(ChangeListener<? super Boolean> listener, Executor executor) {
            ReadOnlyAsyncBooleanWrapper.this.addListener(listener, executor);
        }

        @Override
        public void addListener(ChangeListener<? super Boolean> listener) {
            ReadOnlyAsyncBooleanWrapper.this.addListener(listener);
        }

        @Override
        public void addListener(InvalidationListener listener) {
            ReadOnlyAsyncBooleanWrapper.this.addListener(listener);
        }

        @Override
        public void removeListener(ChangeListener<? super Boolean> listener) {
            ReadOnlyAsyncBooleanWrapper.this.removeListener(listener);
        }

        @Override
        public void removeListener(InvalidationListener listener) {
            ReadOnlyAsyncBooleanWrapper.this.removeListener(listener);
        }
    }

}
