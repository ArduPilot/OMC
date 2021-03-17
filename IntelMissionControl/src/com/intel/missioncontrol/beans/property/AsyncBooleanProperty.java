/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.beans.property;

import com.google.common.util.concurrent.ListenableFuture;
import com.intel.missioncontrol.PublishSource;
import com.intel.missioncontrol.beans.binding.BidirectionalValueConverter;
import com.intel.missioncontrol.beans.value.AsyncWritableBooleanValue;
import com.sun.javafx.binding.Logging;

@PublishSource(
    module = "openjfx",
    licenses = {"openjfx", "intel-gpl-classpath-exception"}
)
public abstract class AsyncBooleanProperty extends ReadOnlyAsyncBooleanProperty
        implements AsyncProperty<Boolean>, AsyncWritableBooleanValue {

    @Override
    public void setValue(Boolean v) {
        if (v == null) {
            Logging.getLogger()
                .fine(
                    "Attempt to set boolean property to null, using default value instead.",
                    new NullPointerException());
            set(false);
        } else {
            set(v);
        }
    }

    @Override
    public ListenableFuture<Void> setAsync(boolean value) {
        return getMetadata().getExecutor().executeListen(() -> set(value));
    }

    @Override
    public ListenableFuture<Void> setValueAsync(Boolean value) {
        return getMetadata().getExecutor().executeListen(() -> setValue(value));
    }

    @Override
    public void bindBidirectional(AsyncProperty<Boolean> source) {
        AsyncBidirectionalBinding.bind(this, source);
    }

    @Override
    public <U> void bindBidirectional(AsyncProperty<U> source, BidirectionalValueConverter<U, Boolean> converter) {
        AsyncBidirectionalBinding.bind(this, source, converter);
    }

    @Override
    public void unbindBidirectional(AsyncProperty<Boolean> source) {
        AsyncBidirectionalBinding.unbind(this, source);
    }

}
