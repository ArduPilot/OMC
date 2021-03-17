/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.beans.property;

import com.google.common.util.concurrent.ListenableFuture;
import com.intel.missioncontrol.PublishSource;
import com.intel.missioncontrol.beans.binding.BidirectionalValueConverter;
import com.intel.missioncontrol.beans.binding.LifecycleValueConverter;
import com.intel.missioncontrol.beans.binding.ValueConverter;
import com.intel.missioncontrol.beans.value.AsyncWritableAsyncSetValue;
import com.intel.missioncontrol.collections.AsyncObservableSet;
import javafx.collections.ObservableSet;

@PublishSource(module = "openjfx", licenses = "intel-gpl-classpath-exception")
public abstract class AsyncSetProperty<E> extends ReadOnlyAsyncSetProperty<E>
        implements AsyncProperty<AsyncObservableSet<E>>, AsyncWritableAsyncSetValue<E> {

    final Object mutex = new Object();
    private boolean contentBound;

    @Override
    public void setValue(AsyncObservableSet<E> value) {
        set(value);
    }

    @Override
    public ListenableFuture<Void> setAsync(AsyncObservableSet<E> value) {
        return getMetadata().getExecutor().executeListen(() -> set(value));
    }

    @Override
    public ListenableFuture<Void> setValueAsync(AsyncObservableSet<E> value) {
        return getMetadata().getExecutor().executeListen(() -> set(value));
    }

    @Override
    public void bindBidirectional(AsyncProperty<AsyncObservableSet<E>> source) {
        AsyncBidirectionalBinding.bind(this, source);
    }

    @Override
    public <U> void bindBidirectional(
            AsyncProperty<U> source, BidirectionalValueConverter<U, AsyncObservableSet<E>> converter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void unbindBidirectional(AsyncProperty<AsyncObservableSet<E>> source) {
        AsyncBidirectionalBinding.unbind(this, source);
    }

    public void bindContent(ObservableSet<? extends E> list) {
        checkBound();
        AsyncContentBinding.bindContent(this, list);
        contentBound = true;
    }

    public void bindContent(AsyncObservableSet<? extends E> list) {
        checkBound();
        AsyncContentBinding.bindContent(this, list);
        contentBound = true;
    }

    public <T> void bindContent(ObservableSet<T> list, ValueConverter<T, E> converter) {
        checkBound();
        AsyncContentBinding.bindContent(this, list, converter);
        contentBound = true;
    }

    public <T> void bindContent(ObservableSet<T> list, LifecycleValueConverter<T, E> converter) {
        checkBound();
        AsyncContentBinding.bindContent(this, list, converter);
        contentBound = true;
    }

    public <T> void bindContent(AsyncObservableSet<T> list, ValueConverter<T, E> converter) {
        checkBound();
        AsyncContentBinding.bindContent(this, list, converter);
        contentBound = true;
    }

    public <T> void bindContent(AsyncObservableSet<T> list, LifecycleValueConverter<T, E> converter) {
        checkBound();
        AsyncContentBinding.bindContent(this, list, converter);
        contentBound = true;
    }

    public void unbindContent(ObservableSet<E> content) {
        AsyncContentBinding.unbindContent(this, content);
        contentBound = false;
    }

    private void checkBound() {
        if (contentBound) {
            throw new IllegalStateException("The property is already bound.");
        }
    }

}
