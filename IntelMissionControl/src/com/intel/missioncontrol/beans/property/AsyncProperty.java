/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.beans.property;

import com.google.common.util.concurrent.ListenableFuture;
import com.intel.missioncontrol.PublishSource;
import com.intel.missioncontrol.beans.binding.BidirectionalValueConverter;
import com.intel.missioncontrol.beans.binding.ValueConverter;
import com.intel.missioncontrol.beans.value.AsyncWritableValue;
import javafx.beans.value.ObservableValue;

@PublishSource(module = "openjfx", licenses = "intel-gpl-classpath-exception")
public interface AsyncProperty<T> extends ReadOnlyAsyncProperty<T>, AsyncWritableValue<T> {

    void reset();

    default ListenableFuture<Void> resetAsync() {
        return getMetadata().getExecutor().executeListen(this::reset);
    }

    void bind(ObservableValue<? extends T> observable);

    <U> void bind(ObservableValue<? extends U> observable, ValueConverter<U, ? extends T> converter);

    void unbind();

    boolean isBound();

    boolean isBoundBidirectionally();

    void bindBidirectional(AsyncProperty<T> source);

    <U> void bindBidirectional(AsyncProperty<U> source, BidirectionalValueConverter<U, T> converter);

    void unbindBidirectional(AsyncProperty<T> source);

    PropertyMetadata<T> getMetadata();

    void overrideMetadata(PropertyMetadata<T> metadata);

}
