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
import com.intel.missioncontrol.beans.value.AsyncWritableAsyncListValue;
import com.intel.missioncontrol.collections.AsyncObservableList;
import javafx.collections.ObservableList;

@PublishSource(
    module = "openjfx",
    licenses = {"openjfx", "intel-gpl-classpath-exception"}
)
public abstract class AsyncListProperty<E> extends ReadOnlyAsyncListProperty<E>
        implements AsyncProperty<AsyncObservableList<E>>, AsyncWritableAsyncListValue<E> {

    private boolean contentBound;

    @Override
    public void setValue(AsyncObservableList<E> value) {
        set(value);
    }

    @Override
    public ListenableFuture<Void> setAsync(AsyncObservableList<E> value) {
        return getMetadata().getExecutor().executeListen(() -> set(value));
    }

    @Override
    public ListenableFuture<Void> setValueAsync(AsyncObservableList<E> value) {
        return getMetadata().getExecutor().executeListen(() -> set(value));
    }

    @Override
    public void bindBidirectional(AsyncProperty<AsyncObservableList<E>> source) {
        AsyncBidirectionalBinding.bind(this, source);
    }

    @Override
    public <U> void bindBidirectional(
            AsyncProperty<U> source, BidirectionalValueConverter<U, AsyncObservableList<E>> converter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void unbindBidirectional(AsyncProperty<AsyncObservableList<E>> source) {
        AsyncBidirectionalBinding.unbind(this, source);
    }

    @SuppressWarnings("unchecked")
    public void bindContent(ObservableList<? extends E> list) {
        checkBound();
        if (list instanceof AsyncObservableList) {
            AsyncContentBinding.bindContent(this, (AsyncObservableList<? extends E>)list);
        } else {
            AsyncContentBinding.bindContent(this, list);
        }

        contentBound = true;
    }

    public <T> void bindContent(ObservableList<T> list, ValueConverter<T, E> converter) {
        checkBound();
        if (list instanceof AsyncObservableList) {
            AsyncContentBinding.bindContent(this, (AsyncObservableList<T>)list, converter);
        } else {
            AsyncContentBinding.bindContent(this, list, converter);
        }

        contentBound = true;
    }

    public <T> void bindContent(ObservableList<T> list, LifecycleValueConverter<T, E> converter) {
        checkBound();
        if (list instanceof AsyncObservableList) {
            AsyncContentBinding.bindContent(this, (AsyncObservableList<T>)list, converter);
        } else {
            AsyncContentBinding.bindContent(this, list, converter);
        }

        contentBound = true;
    }

    public void unbindContent(ObservableList<E> content) {
        AsyncContentBinding.unbindContent(this, content);
        contentBound = false;
    }

    private void checkBound() {
        if (contentBound) {
            throw new IllegalStateException("The property is already bound.");
        }
    }

}
