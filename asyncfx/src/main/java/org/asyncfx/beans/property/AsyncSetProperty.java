/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.beans.property;

import javafx.beans.value.WritableObjectValue;
import javafx.collections.ObservableSet;
import org.asyncfx.PublishSource;
import org.asyncfx.beans.binding.BidirectionalValueConverter;
import org.asyncfx.beans.binding.LifecycleValueConverter;
import org.asyncfx.beans.binding.ValueConverter;
import org.asyncfx.collections.AsyncObservableSet;

@PublishSource(module = "openjfx", licenses = "intel-gpl-classpath-exception")
public abstract class AsyncSetProperty<E> extends ReadOnlyAsyncSetProperty<E>
        implements AsyncProperty<AsyncObservableSet<E>>, WritableObjectValue<AsyncObservableSet<E>> {

    private boolean contentBound;

    @Override
    public void setValue(AsyncObservableSet<E> value) {
        set(value);
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
