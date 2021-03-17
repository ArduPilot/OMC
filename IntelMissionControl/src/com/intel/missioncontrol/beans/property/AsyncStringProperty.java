/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.beans.property;

import com.google.common.util.concurrent.ListenableFuture;
import com.intel.missioncontrol.PublishSource;
import com.intel.missioncontrol.beans.binding.BidirectionalValueConverter;
import com.intel.missioncontrol.beans.value.AsyncWritableStringValue;

@PublishSource(
    module = "openjfx",
    licenses = {"openjfx", "intel-gpl-classpath-exception"}
)
public abstract class AsyncStringProperty extends ReadOnlyAsyncStringProperty
        implements AsyncProperty<String>, AsyncWritableStringValue {

    @Override
    public void setValue(String value) {
        set(value);
    }

    @Override
    public ListenableFuture<Void> setAsync(String value) {
        return getMetadata().getExecutor().executeListen(() -> set(value));
    }

    @Override
    public ListenableFuture<Void> setValueAsync(String value) {
        return getMetadata().getExecutor().executeListen(() -> set(value));
    }

    @Override
    public void bindBidirectional(AsyncProperty<String> source) {
        AsyncBidirectionalBinding.bind(this, source);
    }

    @Override
    public <U> void bindBidirectional(AsyncProperty<U> source, BidirectionalValueConverter<U, String> converter) {
        AsyncBidirectionalBinding.bind(this, source, converter);
    }

    @Override
    public void unbindBidirectional(AsyncProperty<String> source) {
        AsyncBidirectionalBinding.unbind(this, source);
    }

}
