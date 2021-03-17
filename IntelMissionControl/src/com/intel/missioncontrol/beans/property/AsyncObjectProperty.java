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
import com.intel.missioncontrol.beans.value.AsyncWritableObjectValue;
import javafx.beans.value.ObservableValue;

@PublishSource(
    module = "openjfx",
    licenses = {"openjfx", "intel-gpl-classpath-exception"}
)
public abstract class AsyncObjectProperty<T> extends ReadOnlyAsyncObjectProperty<T>
        implements AsyncProperty<T>, AsyncWritableObjectValue<T> {

    abstract <U> void bind(ObservableValue<? extends U> observable, LifecycleValueConverter<U, ? extends T> converter);

    @Override
    public void setValue(T value) {
        set(value);
    }

    @Override
    public ListenableFuture<Void> setAsync(T value) {
        return getMetadata().getExecutor().executeListen(() -> set(value));
    }

    @Override
    public ListenableFuture<Void> setValueAsync(T value) {
        return getMetadata().getExecutor().executeListen(() -> set(value));
    }

    @Override
    public void bindBidirectional(AsyncProperty<T> source) {
        AsyncBidirectionalBinding.bind(this, source);
    }

    @Override
    public <U> void bindBidirectional(AsyncProperty<U> source, BidirectionalValueConverter<U, T> converter) {
        AsyncBidirectionalBinding.bind(this, source, converter);
    }

    @Override
    public void unbindBidirectional(AsyncProperty<T> source) {
        AsyncBidirectionalBinding.unbind(this, source);
    }

}
