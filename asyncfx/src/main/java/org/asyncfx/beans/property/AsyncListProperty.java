/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.beans.property;

import javafx.beans.value.WritableObjectValue;
import javafx.collections.ObservableList;
import org.asyncfx.PublishSource;
import org.asyncfx.beans.binding.BidirectionalValueConverter;
import org.asyncfx.beans.binding.LifecycleValueConverter;
import org.asyncfx.beans.binding.ValueConverter;
import org.asyncfx.collections.AsyncObservableList;

@PublishSource(
    module = "openjfx",
    licenses = {"openjfx", "intel-gpl-classpath-exception"}
)
public abstract class AsyncListProperty<E> extends ReadOnlyAsyncListProperty<E>
        implements AsyncProperty<AsyncObservableList<E>>, WritableObjectValue<AsyncObservableList<E>> {

    private boolean contentBound;

    @Override
    public void setValue(AsyncObservableList<E> value) {
        set(value);
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
