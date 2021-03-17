/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.beans.property;

import javafx.beans.value.ObservableValue;
import javafx.beans.value.WritableValue;
import org.asyncfx.PublishSource;
import org.asyncfx.beans.binding.BidirectionalValueConverter;
import org.asyncfx.beans.binding.ValueConverter;

@PublishSource(module = "openjfx", licenses = "intel-gpl-classpath-exception")
public interface AsyncProperty<T> extends ReadOnlyAsyncProperty<T>, WritableValue<T> {

    /** Sets the value of this property to the default value specified in its metadata. */
    void reset();

    void bind(ObservableValue<? extends T> observable);

    <U> void bind(ObservableValue<? extends U> observable, ValueConverter<U, T> converter);

    void unbind();

    boolean isBound();

    boolean isBoundBidirectionally();

    void bindBidirectional(AsyncProperty<T> source);

    <U> void bindBidirectional(AsyncProperty<U> source, BidirectionalValueConverter<U, T> converter);

    void unbindBidirectional(AsyncProperty<T> source);

    /**
     * Overrides the metadata defined on this property.
     *
     * @see PropertyMetadata
     */
    void overrideMetadata(PropertyMetadata<T> metadata);

}
