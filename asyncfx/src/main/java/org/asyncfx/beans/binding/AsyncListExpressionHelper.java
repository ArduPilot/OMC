/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.beans.binding;

import static javafx.collections.ListChangeListener.Change;

import com.sun.javafx.binding.ExpressionHelperBase;
import java.util.Arrays;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import org.asyncfx.PublishSource;
import org.asyncfx.beans.SubInvalidationListener;
import org.asyncfx.beans.value.AsyncObservableListValue;
import org.asyncfx.beans.value.SubChangeListener;
import org.asyncfx.collections.AsyncListChangeListener.AsyncChange;
import org.asyncfx.collections.AsyncNonIterableChange;
import org.asyncfx.collections.AsyncObservableList;
import org.asyncfx.collections.AsyncSourceAdapterChange;
import org.asyncfx.collections.FXAsyncCollections;

@PublishSource(
    module = "openjfx",
    licenses = {"openjfx", "intel-gpl-classpath-exception"}
)
public abstract class AsyncListExpressionHelper<E> extends ExpressionHelperBase {

    public static <E> AsyncListExpressionHelper<E> addListener(
            AsyncListExpressionHelper<E> helper,
            AsyncObservableListValue<E> observable,
            AsyncObservableList<E> currentValue,
            InvalidationListener listener) {
        if ((observable == null) || (listener == null)) {
            throw new NullPointerException();
        }

        return (helper == null)
            ? new SingleInvalidation<>(observable, listener)
            : helper.addListener(listener, currentValue);
    }

    public static <E> AsyncListExpressionHelper<E> removeListener(
            AsyncListExpressionHelper<E> helper, AsyncObservableList<E> currentValue, InvalidationListener listener) {
        if (listener == null) {
            throw new NullPointerException();
        }

        return (helper == null) ? null : helper.removeListener(listener, currentValue);
    }

    public static <E> AsyncListExpressionHelper<E> addListener(
            AsyncListExpressionHelper<E> helper,
            AsyncObservableListValue<E> observable,
            AsyncObservableList<E> currentValue,
            SubInvalidationListener listener) {
        if ((observable == null) || (listener == null)) {
            throw new NullPointerException();
        }

        return (helper == null)
            ? new SingleSubInvalidation<>(observable, listener)
            : helper.addListener(listener, currentValue);
    }

    public static <E> AsyncListExpressionHelper<E> removeListener(
            AsyncListExpressionHelper<E> helper,
            AsyncObservableList<E> currentValue,
            SubInvalidationListener listener) {
        if (listener == null) {
            throw new NullPointerException();
        }

        return (helper == null) ? null : helper.removeListener(listener, currentValue);
    }

    public static <E> AsyncListExpressionHelper<E> addListener(
            AsyncListExpressionHelper<E> helper,
            AsyncObservableListValue<E> observable,
            AsyncObservableList<E> currentValue,
            ChangeListener<? super AsyncObservableList<E>> listener) {
        if ((observable == null) || (listener == null)) {
            throw new NullPointerException();
        }

        return (helper == null)
            ? new SingleChange<>(observable, currentValue, listener)
            : helper.addListener(listener, currentValue);
    }

    public static <E> AsyncListExpressionHelper<E> removeListener(
            AsyncListExpressionHelper<E> helper,
            AsyncObservableList<E> currentValue,
            ChangeListener<? super AsyncObservableList<E>> listener) {
        if (listener == null) {
            throw new NullPointerException();
        }

        return (helper == null) ? null : helper.removeListener(listener, currentValue);
    }

    public static <E> AsyncListExpressionHelper<E> addListener(
            AsyncListExpressionHelper<E> helper,
            AsyncObservableListValue<E> observable,
            AsyncObservableList<E> currentValue,
            SubChangeListener listener) {
        if ((observable == null) || (listener == null)) {
            throw new NullPointerException();
        }

        return (helper == null)
            ? new SingleSubChange<>(observable, currentValue, listener)
            : helper.addListener(listener, currentValue);
    }

    public static <E> AsyncListExpressionHelper<E> removeListener(
            AsyncListExpressionHelper<E> helper, AsyncObservableList<E> currentValue, SubChangeListener listener) {
        if (listener == null) {
            throw new NullPointerException();
        }

        return (helper == null) ? null : helper.removeListener(listener, currentValue);
    }

    public static <E> AsyncListExpressionHelper<E> addListener(
            AsyncListExpressionHelper<E> helper,
            AsyncObservableListValue<E> observable,
            AsyncObservableList<E> currentValue,
            ListChangeListener<? super E> listener) {
        if ((observable == null) || (listener == null)) {
            throw new NullPointerException();
        }

        return (helper == null)
            ? new SingleListChange<>(observable, currentValue, listener)
            : helper.addListener(listener, currentValue);
    }

    public static <E> AsyncListExpressionHelper<E> removeListener(
            AsyncListExpressionHelper<E> helper,
            AsyncObservableList<E> currentValue,
            ListChangeListener<? super E> listener) {
        if (listener == null) {
            throw new NullPointerException();
        }

        return (helper == null) ? null : helper.removeListener(listener, currentValue);
    }

    public static <T> boolean validatesValue(AsyncListExpressionHelper<T> helper) {
        if (helper != null) {
            return helper.validatesValue();
        }

        return false;
    }

    public static <T> boolean containsBidirectionalBindingEndpoints(AsyncListExpressionHelper<T> helper) {
        if (helper != null) {
            return helper.containsBidirectionalBindingEndpoints();
        }

        return false;
    }

    public static <E> void fireValueChangedEvent(
            AsyncListExpressionHelper<E> helper, AsyncObservableList<E> newValue, boolean subChange) {
        if (helper != null) {
            helper.fireValueChangedEvent(newValue, subChange);
        }
    }

    public static <E> void fireValueChangedEvent(AsyncListExpressionHelper<E> helper, AsyncChange<? extends E> change) {
        if (helper != null) {
            helper.fireValueChangedEvent(change);
        }
    }

    protected final AsyncObservableListValue<E> observable;

    AsyncListExpressionHelper(AsyncObservableListValue<E> observable) {
        this.observable = observable;
    }

    protected abstract AsyncListExpressionHelper<E> addListener(
            InvalidationListener listener, AsyncObservableList<E> currentValue);

    protected abstract AsyncListExpressionHelper<E> removeListener(
            InvalidationListener listener, AsyncObservableList<E> currentValue);

    protected abstract AsyncListExpressionHelper<E> addListener(
            SubInvalidationListener listener, AsyncObservableList<E> currentValue);

    protected abstract AsyncListExpressionHelper<E> removeListener(
            SubInvalidationListener listener, AsyncObservableList<E> currentValue);

    protected abstract AsyncListExpressionHelper<E> addListener(
            ChangeListener<? super AsyncObservableList<E>> listener, AsyncObservableList<E> currentValue);

    protected abstract AsyncListExpressionHelper<E> removeListener(
            ChangeListener<? super AsyncObservableList<E>> listener, AsyncObservableList<E> currentValue);

    protected abstract AsyncListExpressionHelper<E> addListener(
            SubChangeListener listener, AsyncObservableList<E> currentValue);

    protected abstract AsyncListExpressionHelper<E> removeListener(
            SubChangeListener listener, AsyncObservableList<E> currentValue);

    protected abstract AsyncListExpressionHelper<E> addListener(
            ListChangeListener<? super E> listener, AsyncObservableList<E> currentValue);

    protected abstract AsyncListExpressionHelper<E> removeListener(
            ListChangeListener<? super E> listener, AsyncObservableList<E> currentValue);

    protected abstract boolean validatesValue();

    protected abstract boolean containsBidirectionalBindingEndpoints();

    protected abstract void fireValueChangedEvent(AsyncObservableList<E> newValue, boolean subChange);

    protected abstract void fireValueChangedEvent(AsyncChange<? extends E> change);

    private static class SingleInvalidation<E> extends AsyncListExpressionHelper<E> {
        private final InvalidationListener listener;

        private SingleInvalidation(AsyncObservableListValue<E> observable, InvalidationListener listener) {
            super(observable);
            this.listener = listener;
        }

        @Override
        protected AsyncListExpressionHelper<E> addListener(
                InvalidationListener listener, AsyncObservableList<E> currentValue) {
            return new Generic<>(observable, currentValue, this.listener, listener);
        }

        @Override
        protected AsyncListExpressionHelper<E> removeListener(
                InvalidationListener listener, AsyncObservableList<E> currentValue) {
            if (this.listener == null) {
                return this;
            }

            return this.listener.equals(listener) ? null : this;
        }

        @Override
        protected AsyncListExpressionHelper<E> addListener(
                SubInvalidationListener listener, AsyncObservableList<E> currentValue) {
            return new Generic<>(observable, currentValue)
                .addListener(this.listener, currentValue)
                .addListener(listener, currentValue);
        }

        @Override
        protected AsyncListExpressionHelper<E> removeListener(
                SubInvalidationListener listener, AsyncObservableList<E> currentValue) {
            return this;
        }

        @Override
        protected AsyncListExpressionHelper<E> addListener(
                ChangeListener<? super AsyncObservableList<E>> listener, AsyncObservableList<E> currentValue) {
            return new Generic<>(observable, currentValue)
                .addListener(this.listener, currentValue)
                .addListener(listener, currentValue);
        }

        @Override
        protected AsyncListExpressionHelper<E> removeListener(
                ChangeListener<? super AsyncObservableList<E>> listener, AsyncObservableList<E> currentValue) {
            return this;
        }

        @Override
        protected AsyncListExpressionHelper<E> addListener(
                SubChangeListener listener, AsyncObservableList<E> currentValue) {
            return new Generic<>(observable, currentValue)
                .addListener(this.listener, currentValue)
                .addListener(listener, currentValue);
        }

        @Override
        protected AsyncListExpressionHelper<E> removeListener(
                SubChangeListener listener, AsyncObservableList<E> currentValue) {
            return this;
        }

        @Override
        protected AsyncListExpressionHelper<E> addListener(
                ListChangeListener<? super E> listener, AsyncObservableList<E> currentValue) {
            return new Generic<>(observable, currentValue)
                .addListener(this.listener, currentValue)
                .addListener(listener, currentValue);
        }

        @Override
        protected AsyncListExpressionHelper<E> removeListener(
                ListChangeListener<? super E> listener, AsyncObservableList<E> currentValue) {
            return this;
        }

        @Override
        protected boolean validatesValue() {
            return false;
        }

        @Override
        protected boolean containsBidirectionalBindingEndpoints() {
            return false;
        }

        @Override
        protected void fireValueChangedEvent(AsyncObservableList<E> newValue, boolean subChange) {
            if (!subChange) {
                listener.invalidated(observable);
            }
        }

        @Override
        protected void fireValueChangedEvent(AsyncChange<? extends E> change) {
            listener.invalidated(observable);
        }
    }

    private static class SingleSubInvalidation<E> extends AsyncListExpressionHelper<E> {
        private final SubInvalidationListener listener;

        private SingleSubInvalidation(AsyncObservableListValue<E> observable, SubInvalidationListener listener) {
            super(observable);
            this.listener = listener;
        }

        @Override
        protected AsyncListExpressionHelper<E> addListener(
                InvalidationListener listener, AsyncObservableList<E> currentValue) {
            return new Generic<>(observable, currentValue)
                .addListener(this.listener, currentValue)
                .addListener(listener, currentValue);
        }

        @Override
        protected AsyncListExpressionHelper<E> removeListener(
                InvalidationListener listener, AsyncObservableList<E> currentValue) {
            return this;
        }

        @Override
        protected AsyncListExpressionHelper<E> addListener(
                SubInvalidationListener listener, AsyncObservableList<E> currentValue) {
            return new Generic<>(observable, currentValue, this.listener, listener);
        }

        @Override
        protected AsyncListExpressionHelper<E> removeListener(
                SubInvalidationListener listener, AsyncObservableList<E> currentValue) {
            if (this.listener == null) {
                return this;
            }

            return this.listener.equals(listener) ? null : this;
        }

        @Override
        protected AsyncListExpressionHelper<E> addListener(
                ChangeListener<? super AsyncObservableList<E>> listener, AsyncObservableList<E> currentValue) {
            return new Generic<>(observable, currentValue)
                .addListener(this.listener, currentValue)
                .addListener(listener, currentValue);
        }

        @Override
        protected AsyncListExpressionHelper<E> removeListener(
                ChangeListener<? super AsyncObservableList<E>> listener, AsyncObservableList<E> currentValue) {
            return this;
        }

        @Override
        protected AsyncListExpressionHelper<E> addListener(
                SubChangeListener listener, AsyncObservableList<E> currentValue) {
            return new Generic<>(observable, currentValue)
                .addListener(this.listener, currentValue)
                .addListener(listener, currentValue);
        }

        @Override
        protected AsyncListExpressionHelper<E> removeListener(
                SubChangeListener listener, AsyncObservableList<E> currentValue) {
            return this;
        }

        @Override
        protected AsyncListExpressionHelper<E> addListener(
                ListChangeListener<? super E> listener, AsyncObservableList<E> currentValue) {
            return new Generic<>(observable, currentValue)
                .addListener(this.listener, currentValue)
                .addListener(listener, currentValue);
        }

        @Override
        protected AsyncListExpressionHelper<E> removeListener(
                ListChangeListener<? super E> listener, AsyncObservableList<E> currentValue) {
            return this;
        }

        @Override
        protected boolean validatesValue() {
            return false;
        }

        @Override
        protected boolean containsBidirectionalBindingEndpoints() {
            return false;
        }

        @Override
        protected void fireValueChangedEvent(AsyncObservableList<E> newValue, boolean subChange) {
            listener.invalidated(observable, subChange);
        }

        @Override
        protected void fireValueChangedEvent(AsyncChange<? extends E> change) {
            listener.invalidated(observable, false);
        }
    }

    private static class SingleChange<E> extends AsyncListExpressionHelper<E> {
        private final ChangeListener<? super AsyncObservableList<E>> listener;
        private AsyncObservableList<E> currentValue;

        private SingleChange(
                AsyncObservableListValue<E> observable,
                AsyncObservableList<E> currentValue,
                ChangeListener<? super AsyncObservableList<E>> listener) {
            super(observable);
            this.listener = listener;
            this.currentValue = currentValue;
        }

        @Override
        protected AsyncListExpressionHelper<E> addListener(
                InvalidationListener listener, AsyncObservableList<E> currentValue) {
            return new Generic<>(observable, currentValue)
                .addListener(this.listener, currentValue)
                .addListener(listener, currentValue);
        }

        @Override
        protected AsyncListExpressionHelper<E> removeListener(
                InvalidationListener listener, AsyncObservableList<E> currentValue) {
            return this;
        }

        @Override
        protected AsyncListExpressionHelper<E> addListener(
                SubInvalidationListener listener, AsyncObservableList<E> currentValue) {
            return new Generic<>(observable, currentValue)
                .addListener(this.listener, currentValue)
                .addListener(listener, currentValue);
        }

        @Override
        protected AsyncListExpressionHelper<E> removeListener(
                SubInvalidationListener listener, AsyncObservableList<E> currentValue) {
            return this;
        }

        @Override
        protected AsyncListExpressionHelper<E> addListener(
                ChangeListener<? super AsyncObservableList<E>> listener, AsyncObservableList<E> currentValue) {
            return new Generic<>(observable, currentValue, this.listener, listener);
        }

        @Override
        protected AsyncListExpressionHelper<E> removeListener(
                ChangeListener<? super AsyncObservableList<E>> listener, AsyncObservableList<E> currentValue) {
            if (this.listener == null) {
                return this;
            }

            return this.listener.equals(listener) ? null : this;
        }

        @Override
        protected AsyncListExpressionHelper<E> addListener(
                SubChangeListener listener, AsyncObservableList<E> currentValue) {
            return new Generic<>(observable, currentValue)
                .addListener(this.listener, currentValue)
                .addListener(listener, currentValue);
        }

        @Override
        protected AsyncListExpressionHelper<E> removeListener(
                SubChangeListener listener, AsyncObservableList<E> currentValue) {
            return this;
        }

        @Override
        protected AsyncListExpressionHelper<E> addListener(
                ListChangeListener<? super E> listener, AsyncObservableList<E> currentValue) {
            return new Generic<>(observable, currentValue)
                .addListener(this.listener, currentValue)
                .addListener(listener, currentValue);
        }

        @Override
        protected AsyncListExpressionHelper<E> removeListener(
                ListChangeListener<? super E> listener, AsyncObservableList<E> currentValue) {
            return this;
        }

        @Override
        protected boolean validatesValue() {
            return true;
        }

        @Override
        protected boolean containsBidirectionalBindingEndpoints() {
            return listener instanceof BidirectionalBindingMarker;
        }

        @Override
        protected void fireValueChangedEvent(AsyncObservableList<E> newValue, boolean subChange) {
            if (subChange) {
                return;
            }

            final AsyncObservableList<E> oldValue = currentValue;
            currentValue = newValue;
            if (currentValue != oldValue) {
                listener.changed(observable, oldValue, currentValue);
            }
        }

        @Override
        protected void fireValueChangedEvent(AsyncChange<? extends E> change) {
            listener.changed(observable, currentValue, currentValue);
        }
    }

    private static class SingleSubChange<E> extends AsyncListExpressionHelper<E> {
        private final SubChangeListener listener;
        private AsyncObservableList<E> currentValue;

        private SingleSubChange(
                AsyncObservableListValue<E> observable,
                AsyncObservableList<E> currentValue,
                SubChangeListener listener) {
            super(observable);
            this.listener = listener;
            this.currentValue = currentValue;
        }

        @Override
        protected AsyncListExpressionHelper<E> addListener(
                InvalidationListener listener, AsyncObservableList<E> currentValue) {
            return new Generic<>(observable, currentValue)
                .addListener(this.listener, currentValue)
                .addListener(listener, currentValue);
        }

        @Override
        protected AsyncListExpressionHelper<E> removeListener(
                InvalidationListener listener, AsyncObservableList<E> currentValue) {
            return this;
        }

        @Override
        protected AsyncListExpressionHelper<E> addListener(
                SubInvalidationListener listener, AsyncObservableList<E> currentValue) {
            return new Generic<>(observable, currentValue)
                .addListener(this.listener, currentValue)
                .addListener(listener, currentValue);
        }

        @Override
        protected AsyncListExpressionHelper<E> removeListener(
                SubInvalidationListener listener, AsyncObservableList<E> currentValue) {
            return this;
        }

        @Override
        protected AsyncListExpressionHelper<E> addListener(
                ChangeListener<? super AsyncObservableList<E>> listener, AsyncObservableList<E> currentValue) {
            return new Generic<>(observable, currentValue)
                .addListener(this.listener, currentValue)
                .addListener(listener, currentValue);
        }

        @Override
        protected AsyncListExpressionHelper<E> removeListener(
                ChangeListener<? super AsyncObservableList<E>> listener, AsyncObservableList<E> currentValue) {
            return this;
        }

        @Override
        protected AsyncListExpressionHelper<E> addListener(
                SubChangeListener listener, AsyncObservableList<E> currentValue) {
            return new Generic<>(observable, currentValue, this.listener, listener);
        }

        @Override
        protected AsyncListExpressionHelper<E> removeListener(
                SubChangeListener listener, AsyncObservableList<E> currentValue) {
            if (this.listener == null) {
                return this;
            }

            return this.listener.equals(listener) ? null : this;
        }

        @Override
        protected AsyncListExpressionHelper<E> addListener(
                ListChangeListener<? super E> listener, AsyncObservableList<E> currentValue) {
            return new Generic<>(observable, currentValue)
                .addListener(this.listener, currentValue)
                .addListener(listener, currentValue);
        }

        @Override
        protected AsyncListExpressionHelper<E> removeListener(
                ListChangeListener<? super E> listener, AsyncObservableList<E> currentValue) {
            return this;
        }

        @Override
        protected boolean validatesValue() {
            return true;
        }

        @Override
        protected boolean containsBidirectionalBindingEndpoints() {
            return listener instanceof BidirectionalBindingMarker;
        }

        @Override
        protected void fireValueChangedEvent(AsyncObservableList<E> newValue, boolean subChange) {
            final AsyncObservableList<E> oldValue = currentValue;
            currentValue = newValue;
            if (subChange || (currentValue != oldValue)) {
                listener.changed(observable, oldValue, currentValue, subChange);
            }
        }

        @Override
        protected void fireValueChangedEvent(AsyncChange<? extends E> change) {
            listener.changed(observable, currentValue, currentValue, false);
        }
    }

    private static class SingleListChange<E> extends AsyncListExpressionHelper<E> {
        private final ListChangeListener<? super E> listener;
        private AsyncObservableList<E> currentValue;

        private SingleListChange(
                AsyncObservableListValue<E> observable,
                AsyncObservableList<E> currentValue,
                ListChangeListener<? super E> listener) {
            super(observable);
            this.listener = listener;
            this.currentValue = currentValue;
        }

        @Override
        protected AsyncListExpressionHelper<E> addListener(
                InvalidationListener listener, AsyncObservableList<E> currentValue) {
            return new Generic<>(observable, currentValue)
                .addListener(this.listener, currentValue)
                .addListener(listener, currentValue);
        }

        @Override
        protected AsyncListExpressionHelper<E> removeListener(
                InvalidationListener listener, AsyncObservableList<E> currentValue) {
            return this;
        }

        @Override
        protected AsyncListExpressionHelper<E> addListener(
                SubInvalidationListener listener, AsyncObservableList<E> currentValue) {
            return new Generic<>(observable, currentValue)
                .addListener(this.listener, currentValue)
                .addListener(listener, currentValue);
        }

        @Override
        protected AsyncListExpressionHelper<E> removeListener(
                SubInvalidationListener listener, AsyncObservableList<E> currentValue) {
            return this;
        }

        @Override
        protected AsyncListExpressionHelper<E> addListener(
                ChangeListener<? super AsyncObservableList<E>> listener, AsyncObservableList<E> currentValue) {
            return new Generic<>(observable, currentValue)
                .addListener(this.listener, currentValue)
                .addListener(listener, currentValue);
        }

        @Override
        protected AsyncListExpressionHelper<E> removeListener(
                ChangeListener<? super AsyncObservableList<E>> listener, AsyncObservableList<E> currentValue) {
            return this;
        }

        @Override
        protected AsyncListExpressionHelper<E> addListener(
                SubChangeListener listener, AsyncObservableList<E> currentValue) {
            return new Generic<>(observable, currentValue)
                .addListener(this.listener, currentValue)
                .addListener(listener, currentValue);
        }

        @Override
        protected AsyncListExpressionHelper<E> removeListener(
                SubChangeListener listener, AsyncObservableList<E> currentValue) {
            return this;
        }

        @Override
        protected AsyncListExpressionHelper<E> addListener(
                ListChangeListener<? super E> listener, AsyncObservableList<E> currentValue) {
            return new Generic<>(observable, currentValue, this.listener, listener);
        }

        @Override
        protected AsyncListExpressionHelper<E> removeListener(
                ListChangeListener<? super E> listener, AsyncObservableList<E> currentValue) {
            if (this.listener == null) {
                return this;
            }

            return this.listener.equals(listener) ? null : this;
        }

        @Override
        protected boolean validatesValue() {
            return true;
        }

        @Override
        protected boolean containsBidirectionalBindingEndpoints() {
            return false;
        }

        @Override
        protected void fireValueChangedEvent(AsyncObservableList<E> newValue, boolean subChange) {
            if (subChange) {
                return;
            }

            final AsyncObservableList<E> oldValue = currentValue;
            currentValue = newValue;
            if (currentValue != oldValue) {
                final int size = (currentValue == null) ? 0 : currentValue.size();
                final AsyncObservableList<E> value =
                    (oldValue == null)
                        ? FXAsyncCollections.emptyObservableList()
                        : FXAsyncCollections.unmodifiableObservableList(oldValue);
                final Change<E> change =
                    new AsyncNonIterableChange.GenericAddRemoveChange<>(0, size, value, observable);
                listener.onChanged(change);
            }
        }

        @Override
        protected void fireValueChangedEvent(final AsyncChange<? extends E> change) {
            listener.onChanged(new AsyncSourceAdapterChange<>(observable, change));
        }
    }

    private static class Generic<E> extends AsyncListExpressionHelper<E> {

        private InvalidationListener[] invalidationListeners;
        private SubInvalidationListener[] subInvalidationListeners;
        private ChangeListener<? super AsyncObservableList<E>>[] changeListeners;
        private SubChangeListener[] subChangeListeners;
        private ListChangeListener<? super E>[] listChangeListeners;
        private int invalidationSize;
        private int subInvalidationSize;
        private int changeSize;
        private int subChangeSize;
        private int listChangeSize;
        private AsyncObservableList<E> currentValue;

        private Generic(AsyncObservableListValue<E> observable, AsyncObservableList<E> currentValue) {
            super(observable);
            this.currentValue = currentValue;
        }

        private Generic(
                AsyncObservableListValue<E> observable,
                AsyncObservableList<E> currentValue,
                InvalidationListener listener0,
                InvalidationListener listener1) {
            super(observable);
            this.invalidationListeners = new InvalidationListener[] {listener0, listener1};
            this.invalidationSize = 2;
            this.currentValue = currentValue;
        }

        private Generic(
                AsyncObservableListValue<E> observable,
                AsyncObservableList<E> currentValue,
                SubInvalidationListener listener0,
                SubInvalidationListener listener1) {
            super(observable);
            this.subInvalidationListeners = new SubInvalidationListener[] {listener0, listener1};
            this.subInvalidationSize = 2;
            this.currentValue = currentValue;
        }

        @SuppressWarnings("unchecked")
        private Generic(
                AsyncObservableListValue<E> observable,
                AsyncObservableList<E> currentValue,
                ChangeListener<? super AsyncObservableList<E>> listener0,
                ChangeListener<? super AsyncObservableList<E>> listener1) {
            super(observable);
            this.changeListeners = new ChangeListener[] {listener0, listener1};
            this.changeSize = 2;
            this.currentValue = currentValue;
        }

        private Generic(
                AsyncObservableListValue<E> observable,
                AsyncObservableList<E> currentValue,
                SubChangeListener listener0,
                SubChangeListener listener1) {
            super(observable);
            this.subChangeListeners = new SubChangeListener[] {listener0, listener1};
            this.subChangeSize = 2;
            this.currentValue = currentValue;
        }

        @SuppressWarnings("unchecked")
        private Generic(
                AsyncObservableListValue<E> observable,
                AsyncObservableList<E> currentValue,
                ListChangeListener<? super E> listener0,
                ListChangeListener<? super E> listener1) {
            super(observable);
            this.listChangeListeners = new ListChangeListener[] {listener0, listener1};
            this.listChangeSize = 2;
            this.currentValue = currentValue;
        }

        private Generic(
                AsyncObservableListValue<E> observable, AsyncObservableList<E> currentValue, Generic<E> source) {
            super(observable);
            this.currentValue = currentValue;

            if (source.invalidationListeners != null) {
                invalidationListeners = source.invalidationListeners;
                invalidationSize = source.invalidationSize;
            }

            if (source.subInvalidationListeners != null) {
                subInvalidationListeners = source.subInvalidationListeners;
                subInvalidationSize = source.subInvalidationSize;
            }

            if (source.changeListeners != null) {
                changeListeners = source.changeListeners;
                changeSize = source.changeSize;
            }

            if (source.subChangeListeners != null) {
                subChangeListeners = source.subChangeListeners;
                subChangeSize = source.subChangeSize;
            }

            if (source.listChangeListeners != null) {
                listChangeListeners = source.listChangeListeners;
                listChangeSize = source.listChangeSize;
            }
        }

        @Override
        protected AsyncListExpressionHelper<E> addListener(
                InvalidationListener listener, AsyncObservableList<E> currentValue) {
            Generic<E> helper = new Generic<>(observable, currentValue, this);

            if (helper.invalidationListeners == null) {
                helper.invalidationListeners = new InvalidationListener[] {listener};
                helper.invalidationSize = 1;
            } else {
                helper.invalidationListeners = Arrays.copyOf(helper.invalidationListeners, helper.invalidationSize + 1);
                helper.invalidationSize = trim(helper.invalidationSize, helper.invalidationListeners);
                helper.invalidationListeners[helper.invalidationSize++] = listener;
            }

            return helper;
        }

        @Override
        protected AsyncListExpressionHelper<E> removeListener(
                InvalidationListener listener, AsyncObservableList<E> currentValue) {
            if (invalidationListeners != null) {
                for (int index = 0; index < invalidationSize; index++) {
                    if (invalidationListeners[index].equals(listener)) {
                        AsyncListExpressionHelper<E> helper = getSingleListenerHelper(index, 1, 0, 0, 0, 0);
                        if (helper != null) {
                            return helper;
                        }

                        Generic<E> generic = new Generic<>(observable, currentValue, this);

                        if (invalidationSize == 1) {
                            generic.invalidationListeners = null;
                            generic.invalidationSize = 0;
                        } else {
                            final int numMoved = invalidationSize - index - 1;
                            generic.invalidationSize = invalidationSize - 1;
                            generic.invalidationListeners = new InvalidationListener[invalidationSize - 1];
                            System.arraycopy(invalidationListeners, 0, generic.invalidationListeners, 0, index);

                            if (numMoved > 0) {
                                System.arraycopy(
                                    invalidationListeners, index + 1, generic.invalidationListeners, index, numMoved);
                            }
                        }

                        return generic;
                    }
                }
            }

            return this;
        }

        @Override
        protected AsyncListExpressionHelper<E> addListener(
                SubInvalidationListener listener, AsyncObservableList<E> currentValue) {
            Generic<E> helper = new Generic<>(observable, currentValue, this);

            if (helper.subInvalidationListeners == null) {
                helper.subInvalidationListeners = new SubInvalidationListener[] {listener};
                helper.subInvalidationSize = 1;
            } else {
                helper.subInvalidationListeners =
                    Arrays.copyOf(helper.subInvalidationListeners, helper.subInvalidationSize + 1);
                helper.subInvalidationSize = trim(helper.subInvalidationSize, helper.subInvalidationListeners);
                helper.subInvalidationListeners[helper.subInvalidationSize++] = listener;
            }

            return helper;
        }

        @Override
        protected AsyncListExpressionHelper<E> removeListener(
                SubInvalidationListener listener, AsyncObservableList<E> currentValue) {
            if (subInvalidationListeners != null) {
                for (int index = 0; index < subInvalidationSize; index++) {
                    if (subInvalidationListeners[index].equals(listener)) {
                        AsyncListExpressionHelper<E> helper = getSingleListenerHelper(index, 0, 1, 0, 0, 0);
                        if (helper != null) {
                            return helper;
                        }

                        Generic<E> generic = new Generic<>(observable, currentValue, this);

                        if (subInvalidationSize == 1) {
                            generic.subInvalidationListeners = null;
                            generic.subInvalidationSize = 0;
                        } else {
                            final int numMoved = subInvalidationSize - index - 1;
                            generic.subInvalidationSize = subInvalidationSize - 1;
                            generic.subInvalidationListeners = new SubInvalidationListener[subInvalidationSize - 1];
                            System.arraycopy(subInvalidationListeners, 0, generic.subInvalidationListeners, 0, index);

                            if (numMoved > 0) {
                                System.arraycopy(
                                    subInvalidationListeners,
                                    index + 1,
                                    generic.subInvalidationListeners,
                                    index,
                                    numMoved);
                            }
                        }

                        return generic;
                    }
                }
            }

            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        protected AsyncListExpressionHelper<E> addListener(
                ChangeListener<? super AsyncObservableList<E>> listener, AsyncObservableList<E> currentValue) {
            Generic<E> helper = new Generic<>(observable, currentValue, this);

            if (helper.changeListeners == null) {
                helper.changeListeners = new ChangeListener[] {listener};
                helper.changeSize = 1;
            } else {
                helper.changeListeners = Arrays.copyOf(helper.changeListeners, helper.changeSize + 1);
                helper.changeSize = trim(helper.changeSize, helper.changeListeners);
                helper.changeListeners[helper.changeSize++] = listener;
            }

            if (helper.changeSize == 1) {
                helper.currentValue = currentValue;
            }

            return helper;
        }

        @Override
        @SuppressWarnings("unchecked")
        protected AsyncListExpressionHelper<E> removeListener(
                ChangeListener<? super AsyncObservableList<E>> listener, AsyncObservableList<E> currentValue) {
            if (changeListeners != null) {
                for (int index = 0; index < changeSize; index++) {
                    if (changeListeners[index].equals(listener)) {
                        AsyncListExpressionHelper<E> helper = getSingleListenerHelper(index, 0, 0, 1, 0, 0);
                        if (helper != null) {
                            return helper;
                        }

                        Generic<E> generic = new Generic<>(observable, currentValue, this);

                        if (changeSize == 1) {
                            generic.changeListeners = null;
                            generic.changeSize = 0;
                        } else {
                            final int numMoved = changeSize - index - 1;
                            generic.changeSize = changeSize - 1;
                            generic.changeListeners = new ChangeListener[changeSize - 1];
                            System.arraycopy(changeListeners, 0, generic.changeListeners, 0, index);

                            if (numMoved > 0) {
                                System.arraycopy(changeListeners, index + 1, generic.changeListeners, index, numMoved);
                            }
                        }

                        return generic;
                    }
                }
            }

            return this;
        }

        @Override
        protected AsyncListExpressionHelper<E> addListener(
                SubChangeListener listener, AsyncObservableList<E> currentValue) {
            Generic<E> helper = new Generic<>(observable, currentValue, this);

            if (helper.subChangeListeners == null) {
                helper.subChangeListeners = new SubChangeListener[] {listener};
                helper.subChangeSize = 1;
            } else {
                helper.subChangeListeners = Arrays.copyOf(helper.subChangeListeners, helper.subChangeSize + 1);
                helper.subChangeSize = trim(helper.subChangeSize, helper.subChangeListeners);
                helper.subChangeListeners[helper.subChangeSize++] = listener;
            }

            if (helper.subChangeSize == 1) {
                helper.currentValue = currentValue;
            }

            return helper;
        }

        @Override
        protected AsyncListExpressionHelper<E> removeListener(
                SubChangeListener listener, AsyncObservableList<E> currentValue) {
            if (subChangeListeners != null) {
                for (int index = 0; index < subChangeSize; index++) {
                    if (subChangeListeners[index].equals(listener)) {
                        AsyncListExpressionHelper<E> helper = getSingleListenerHelper(index, 0, 0, 0, 1, 0);
                        if (helper != null) {
                            return helper;
                        }

                        Generic<E> generic = new Generic<>(observable, currentValue, this);

                        if (subChangeSize == 1) {
                            generic.subChangeListeners = null;
                            generic.subChangeSize = 0;
                        } else {
                            final int numMoved = subChangeSize - index - 1;
                            generic.subChangeSize = subChangeSize - 1;
                            generic.subChangeListeners = new SubChangeListener[subChangeSize - 1];
                            System.arraycopy(subChangeListeners, 0, generic.subChangeListeners, 0, index);

                            if (numMoved > 0) {
                                System.arraycopy(
                                    subChangeListeners, index + 1, generic.subChangeListeners, index, numMoved);
                            }
                        }

                        return generic;
                    }
                }
            }

            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        protected AsyncListExpressionHelper<E> addListener(
                ListChangeListener<? super E> listener, AsyncObservableList<E> currentValue) {
            Generic<E> helper = new Generic<>(observable, currentValue, this);

            if (helper.listChangeListeners == null) {
                helper.listChangeListeners = new ListChangeListener[] {listener};
                helper.listChangeSize = 1;
            } else {
                helper.listChangeListeners = Arrays.copyOf(helper.listChangeListeners, helper.listChangeSize + 1);
                helper.listChangeSize = trim(helper.listChangeSize, helper.listChangeListeners);
                helper.listChangeListeners[helper.listChangeSize++] = listener;
            }

            if (helper.listChangeSize == 1) {
                helper.currentValue = currentValue;
            }

            return helper;
        }

        @Override
        @SuppressWarnings("unchecked")
        protected AsyncListExpressionHelper<E> removeListener(
                ListChangeListener<? super E> listener, AsyncObservableList<E> currentValue) {
            if (listChangeListeners != null) {
                for (int index = 0; index < listChangeSize; index++) {
                    if (listChangeListeners[index].equals(listener)) {
                        AsyncListExpressionHelper<E> helper = getSingleListenerHelper(index, 0, 0, 0, 0, 1);
                        if (helper != null) {
                            return helper;
                        }

                        Generic<E> generic = new Generic<>(observable, currentValue, this);

                        if (listChangeSize == 1) {
                            generic.listChangeListeners = null;
                            generic.listChangeSize = 0;
                        } else {
                            final int numMoved = listChangeSize - index - 1;
                            generic.listChangeSize = listChangeSize - 1;
                            generic.listChangeListeners = new ListChangeListener[listChangeSize - 1];
                            System.arraycopy(listChangeListeners, 0, generic.listChangeListeners, 0, index);

                            if (numMoved > 0) {
                                System.arraycopy(
                                    listChangeListeners, index + 1, generic.listChangeListeners, index, numMoved);
                            }
                        }

                        return generic;
                    }
                }
            }

            return this;
        }

        @Override
        protected boolean validatesValue() {
            return changeSize > 0 || listChangeSize > 0;
        }

        @Override
        protected boolean containsBidirectionalBindingEndpoints() {
            if (changeSize == 0) {
                return false;
            }

            for (int i = 0; i < changeSize; i++) {
                if (changeListeners[i] instanceof BidirectionalBindingMarker) {
                    return true;
                }
            }

            return false;
        }

        @Override
        protected void fireValueChangedEvent(AsyncObservableList<E> newValue, boolean subChange) {
            if ((changeSize == 0) && (listChangeSize == 0)) {
                notifyListeners(currentValue, null, false, subChange);
            } else {
                final AsyncObservableList<E> oldValue = currentValue;
                currentValue = newValue;
                if (currentValue != oldValue) {
                    Change<E> change = null;
                    if (listChangeSize > 0) {
                        final int safeSize = (currentValue == null) ? 0 : currentValue.size();
                        final AsyncObservableList<E> safeOldValue =
                            (oldValue == null)
                                ? FXAsyncCollections.emptyObservableList()
                                : FXAsyncCollections.unmodifiableObservableList(oldValue);
                        change =
                            new AsyncNonIterableChange.GenericAddRemoveChange<>(0, safeSize, safeOldValue, observable);
                    }

                    notifyListeners(oldValue, change, false, subChange);
                } else {
                    notifyListeners(currentValue, null, true, subChange);
                }
            }
        }

        @Override
        protected void fireValueChangedEvent(final AsyncChange<? extends E> change) {
            final Change<E> mappedChange =
                (listChangeSize == 0) ? null : new AsyncSourceAdapterChange<>(observable, change);
            notifyListeners(currentValue, mappedChange, false, false);
        }

        private void notifyListeners(
                AsyncObservableList<E> oldValue, Change<E> change, boolean noChange, boolean subChange) {
            for (int i = 0; i < invalidationSize; i++) {
                invalidationListeners[i].invalidated(observable);
            }

            if (!noChange) {
                for (int i = 0; i < changeSize; i++) {
                    changeListeners[i].changed(observable, oldValue, currentValue);
                }

                if (change != null) {
                    for (int i = 0; i < listChangeSize; i++) {
                        change.reset();
                        listChangeListeners[i].onChanged(change);
                    }
                }
            }

            if (subChange) {
                for (int i = 0; i < subInvalidationSize; i++) {
                    subInvalidationListeners[i].invalidated(observable, subChange);
                }

                for (int i = 0; i < subChangeSize; i++) {
                    subChangeListeners[i].changed(observable, oldValue, currentValue, subChange);
                }
            }
        }

        private AsyncListExpressionHelper<E> getSingleListenerHelper(
                int index,
                int removeInvalidation,
                int removeSubInvalidation,
                int removeChange,
                int removeSubChange,
                int removeListChange) {
            if (invalidationSize - removeInvalidation == 1
                    && subInvalidationSize - removeSubInvalidation == 0
                    && changeSize - removeChange == 0
                    && subChangeSize - removeSubChange == 0
                    && listChangeSize - removeListChange == 0) {
                return new SingleInvalidation<>(
                    observable, invalidationListeners[invalidationSize == 2 ? 1 - index : 0]);
            }

            if (invalidationSize - removeInvalidation == 0
                    && subInvalidationSize - removeSubInvalidation == 1
                    && changeSize - removeChange == 0
                    && subChangeSize - removeSubChange == 0
                    && listChangeSize - removeListChange == 0) {
                return new SingleSubInvalidation<>(
                    observable, subInvalidationListeners[subInvalidationSize == 2 ? 1 - index : 0]);
            }

            if (invalidationSize - removeInvalidation == 0
                    && subInvalidationSize - removeSubInvalidation == 0
                    && changeSize - removeChange == 1
                    && subChangeSize - removeSubChange == 0
                    && listChangeSize - removeListChange == 0) {
                return new SingleChange<>(observable, currentValue, changeListeners[changeSize == 2 ? 1 - index : 0]);
            }

            if (invalidationSize - removeInvalidation == 0
                    && subInvalidationSize - removeSubInvalidation == 0
                    && changeSize - removeChange == 0
                    && subChangeSize - removeSubChange == 1
                    && listChangeSize - removeListChange == 0) {
                return new SingleSubChange<>(
                    observable, currentValue, subChangeListeners[subChangeSize == 2 ? 1 - index : 0]);
            }

            if (invalidationSize - removeInvalidation == 0
                    && subInvalidationSize - removeSubInvalidation == 0
                    && changeSize - removeChange == 0
                    && subChangeSize - removeSubChange == 0
                    && listChangeSize - removeListChange == 1) {
                return new SingleListChange<>(
                    observable, currentValue, listChangeListeners[listChangeSize == 2 ? 1 - index : 0]);
            }

            return null;
        }
    }
}
