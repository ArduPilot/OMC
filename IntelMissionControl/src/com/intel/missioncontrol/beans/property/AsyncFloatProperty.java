/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.beans.property;

import com.google.common.util.concurrent.ListenableFuture;
import com.intel.missioncontrol.PublishSource;
import com.intel.missioncontrol.beans.binding.BidirectionalValueConverter;
import com.intel.missioncontrol.beans.value.AsyncWritableFloatValue;
import com.sun.javafx.binding.Logging;

@PublishSource(
    module = "openjfx",
    licenses = {"openjfx", "intel-gpl-classpath-exception"}
)
public abstract class AsyncFloatProperty extends ReadOnlyAsyncFloatProperty
        implements AsyncProperty<Number>, AsyncWritableFloatValue {

    @Override
    public void setValue(Number v) {
        if (v == null) {
            Logging.getLogger()
                .fine(
                    "Attempt to set float property to null, using default value instead.", new NullPointerException());
            set(0.0f);
        } else {
            set(v.floatValue());
        }
    }

    @Override
    public ListenableFuture<Void> setAsync(float value) {
        return getMetadata().getExecutor().executeListen(() -> set(value));
    }

    @Override
    public ListenableFuture<Void> setValueAsync(Number value) {
        return getMetadata().getExecutor().executeListen(() -> setValue(value));
    }

    @Override
    public void bindBidirectional(AsyncProperty<Number> source) {
        AsyncBidirectionalBinding.bind(this, source);
    }

    @Override
    public <U> void bindBidirectional(AsyncProperty<U> source, BidirectionalValueConverter<U, Number> converter) {
        AsyncBidirectionalBinding.bind(this, source, converter);
    }

    @Override
    public void unbindBidirectional(AsyncProperty<Number> source) {
        AsyncBidirectionalBinding.unbind(this, source);
    }

}
