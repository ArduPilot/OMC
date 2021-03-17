/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.beans.binding;

import static javafx.collections.SetChangeListener.Change;

import com.sun.javafx.binding.ExpressionHelperBase;
import java.util.Arrays;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.collections.SetChangeListener;
import org.asyncfx.PublishSource;
import org.asyncfx.beans.SubInvalidationListener;
import org.asyncfx.beans.value.AsyncObservableSetValue;
import org.asyncfx.beans.value.SubChangeListener;
import org.asyncfx.collections.AsyncObservableSet;
import org.asyncfx.collections.LockedSet;

@PublishSource(
    module = "openjfx",
    licenses = {"openjfx", "intel-gpl-classpath-exception"}
)
public abstract class AsyncSetExpressionHelper<E> extends ExpressionHelperBase {

    public static <E> AsyncSetExpressionHelper<E> addListener(
            AsyncSetExpressionHelper<E> helper,
            AsyncObservableSetValue<E> observable,
            AsyncObservableSet<E> currentValue,
            InvalidationListener listener) {
        if ((observable == null) || (listener == null)) {
            throw new NullPointerException();
        }

        return (helper == null)
            ? new SingleInvalidation<>(observable, listener)
            : helper.addListener(listener, currentValue);
    }

    public static <E> AsyncSetExpressionHelper<E> removeListener(
            AsyncSetExpressionHelper<E> helper, AsyncObservableSet<E> currentValue, InvalidationListener listener) {
        if (listener == null) {
            throw new NullPointerException();
        }

        return (helper == null) ? null : helper.removeListener(listener, currentValue);
    }

    public static <E> AsyncSetExpressionHelper<E> addListener(
            AsyncSetExpressionHelper<E> helper,
            AsyncObservableSetValue<E> observable,
            AsyncObservableSet<E> currentValue,
            SubInvalidationListener listener) {
        if ((observable == null) || (listener == null)) {
            throw new NullPointerException();
        }

        return (helper == null)
            ? new SingleSubInvalidation<>(observable, listener)
            : helper.addListener(listener, currentValue);
    }

    public static <E> AsyncSetExpressionHelper<E> removeListener(
            AsyncSetExpressionHelper<E> helper, AsyncObservableSet<E> currentValue, SubInvalidationListener listener) {
        if (listener == null) {
            throw new NullPointerException();
        }

        return (helper == null) ? null : helper.removeListener(listener, currentValue);
    }

    public static <E> AsyncSetExpressionHelper<E> addListener(
            AsyncSetExpressionHelper<E> helper,
            AsyncObservableSetValue<E> observable,
            AsyncObservableSet<E> currentValue,
            ChangeListener<? super AsyncObservableSet<E>> listener) {
        if ((observable == null) || (listener == null)) {
            throw new NullPointerException();
        }

        return (helper == null)
            ? new SingleChange<>(observable, currentValue, listener)
            : helper.addListener(listener, currentValue);
    }

    public static <E> AsyncSetExpressionHelper<E> removeListener(
            AsyncSetExpressionHelper<E> helper,
            AsyncObservableSet<E> currentValue,
            ChangeListener<? super AsyncObservableSet<E>> listener) {
        if (listener == null) {
            throw new NullPointerException();
        }

        return (helper == null) ? null : helper.removeListener(listener, currentValue);
    }

    public static <E> AsyncSetExpressionHelper<E> addListener(
            AsyncSetExpressionHelper<E> helper,
            AsyncObservableSetValue<E> observable,
            AsyncObservableSet<E> currentValue,
            SubChangeListener listener) {
        if ((observable == null) || (listener == null)) {
            throw new NullPointerException();
        }

        return (helper == null)
            ? new SingleSubChange<>(observable, currentValue, listener)
            : helper.addListener(listener, currentValue);
    }

    public static <E> AsyncSetExpressionHelper<E> removeListener(
            AsyncSetExpressionHelper<E> helper, AsyncObservableSet<E> currentValue, SubChangeListener listener) {
        if (listener == null) {
            throw new NullPointerException();
        }

        return (helper == null) ? null : helper.removeListener(listener, currentValue);
    }

    public static <E> AsyncSetExpressionHelper<E> addListener(
            AsyncSetExpressionHelper<E> helper,
            AsyncObservableSetValue<E> observable,
            AsyncObservableSet<E> currentValue,
            SetChangeListener<? super E> listener) {
        if ((observable == null) || (listener == null)) {
            throw new NullPointerException();
        }

        return (helper == null)
            ? new SingleSetChange<>(observable, currentValue, listener)
            : helper.addListener(listener, currentValue);
    }

    public static <E> AsyncSetExpressionHelper<E> removeListener(
            AsyncSetExpressionHelper<E> helper,
            AsyncObservableSet<E> currentValue,
            SetChangeListener<? super E> listener) {
        if (listener == null) {
            throw new NullPointerException();
        }

        return (helper == null) ? null : helper.removeListener(listener, currentValue);
    }

    public static <T> boolean validatesValue(AsyncSetExpressionHelper<T> helper) {
        if (helper != null) {
            return helper.validatesValue();
        }

        return false;
    }

    public static <T> boolean containsBidirectionalBindingEndpoints(AsyncSetExpressionHelper<T> helper) {
        if (helper != null) {
            return helper.containsBidirectionalBindingEndpoints();
        }

        return false;
    }

    public static <E> void fireValueChangedEvent(
            AsyncSetExpressionHelper<E> helper, AsyncObservableSet<E> newValue, boolean subChange) {
        if (helper != null) {
            helper.fireValueChangedEvent(newValue, subChange);
        }
    }

    public static <E> void fireValueChangedEvent(AsyncSetExpressionHelper<E> helper, Change<? extends E> change) {
        if (helper != null) {
            helper.fireValueChangedEvent(change);
        }
    }

    protected final AsyncObservableSetValue<E> observable;

    AsyncSetExpressionHelper(AsyncObservableSetValue<E> observable) {
        this.observable = observable;
    }

    protected abstract AsyncSetExpressionHelper<E> addListener(
            InvalidationListener listener, AsyncObservableSet<E> currentValue);

    protected abstract AsyncSetExpressionHelper<E> removeListener(
            InvalidationListener listener, AsyncObservableSet<E> currentValue);

    protected abstract AsyncSetExpressionHelper<E> addListener(
            SubInvalidationListener listener, AsyncObservableSet<E> currentValue);

    protected abstract AsyncSetExpressionHelper<E> removeListener(
            SubInvalidationListener listener, AsyncObservableSet<E> currentValue);

    protected abstract AsyncSetExpressionHelper<E> addListener(
            ChangeListener<? super AsyncObservableSet<E>> listener, AsyncObservableSet<E> currentValue);

    protected abstract AsyncSetExpressionHelper<E> removeListener(
            ChangeListener<? super AsyncObservableSet<E>> listener, AsyncObservableSet<E> currentValue);

    protected abstract AsyncSetExpressionHelper<E> addListener(
            SubChangeListener listener, AsyncObservableSet<E> currentValue);

    protected abstract AsyncSetExpressionHelper<E> removeListener(
            SubChangeListener listener, AsyncObservableSet<E> currentValue);

    protected abstract AsyncSetExpressionHelper<E> addListener(
            SetChangeListener<? super E> listener, AsyncObservableSet<E> currentValue);

    protected abstract AsyncSetExpressionHelper<E> removeListener(
            SetChangeListener<? super E> listener, AsyncObservableSet<E> currentValue);

    protected abstract boolean validatesValue();

    protected abstract boolean containsBidirectionalBindingEndpoints();

    protected abstract void fireValueChangedEvent(AsyncObservableSet<E> newValue, boolean subChange);

    protected abstract void fireValueChangedEvent(Change<? extends E> change);

    private static class SingleInvalidation<E> extends AsyncSetExpressionHelper<E> {
        private final InvalidationListener listener;

        private SingleInvalidation(AsyncObservableSetValue<E> observable, InvalidationListener listener) {
            super(observable);
            this.listener = listener;
        }

        @Override
        protected AsyncSetExpressionHelper<E> addListener(
                InvalidationListener listener, AsyncObservableSet<E> currentValue) {
            return new Generic<>(observable, currentValue, this.listener, listener);
        }

        @Override
        protected AsyncSetExpressionHelper<E> removeListener(
                InvalidationListener listener, AsyncObservableSet<E> currentValue) {
            if (this.listener == null) {
                return this;
            }

            return this.listener.equals(listener) ? null : this;
        }

        @Override
        protected AsyncSetExpressionHelper<E> addListener(
                SubInvalidationListener listener, AsyncObservableSet<E> currentValue) {
            AsyncSetExpressionHelper<E> helper = new Generic<>(observable, currentValue);
            helper.addListener(this.listener, currentValue);
            helper.addListener(listener, currentValue);
            return helper;
        }

        @Override
        protected AsyncSetExpressionHelper<E> removeListener(
                SubInvalidationListener listener, AsyncObservableSet<E> currentValue) {
            return this;
        }

        @Override
        protected AsyncSetExpressionHelper<E> addListener(
                ChangeListener<? super AsyncObservableSet<E>> listener, AsyncObservableSet<E> currentValue) {
            AsyncSetExpressionHelper<E> helper = new Generic<>(observable, currentValue);
            helper.addListener(this.listener, currentValue);
            helper.addListener(listener, currentValue);
            return helper;
        }

        @Override
        protected AsyncSetExpressionHelper<E> removeListener(
                ChangeListener<? super AsyncObservableSet<E>> listener, AsyncObservableSet<E> currentValue) {
            return this;
        }

        @Override
        protected AsyncSetExpressionHelper<E> addListener(
                SubChangeListener listener, AsyncObservableSet<E> currentValue) {
            AsyncSetExpressionHelper<E> helper = new Generic<>(observable, currentValue);
            helper.addListener(this.listener, currentValue);
            helper.addListener(listener, currentValue);
            return helper;
        }

        @Override
        protected AsyncSetExpressionHelper<E> removeListener(
                SubChangeListener listener, AsyncObservableSet<E> currentValue) {
            return this;
        }

        @Override
        protected AsyncSetExpressionHelper<E> addListener(
                SetChangeListener<? super E> listener, AsyncObservableSet<E> currentValue) {
            AsyncSetExpressionHelper<E> helper = new Generic<>(observable, currentValue);
            helper.addListener(this.listener, currentValue);
            helper.addListener(listener, currentValue);
            return helper;
        }

        @Override
        protected AsyncSetExpressionHelper<E> removeListener(
                SetChangeListener<? super E> listener, AsyncObservableSet<E> currentValue) {
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
        protected void fireValueChangedEvent(AsyncObservableSet<E> newValue, boolean subChange) {
            if (!subChange) {
                listener.invalidated(observable);
            }
        }

        @Override
        protected void fireValueChangedEvent(Change<? extends E> change) {
            listener.invalidated(observable);
        }
    }

    private static class SingleSubInvalidation<E> extends AsyncSetExpressionHelper<E> {
        private final SubInvalidationListener listener;

        private SingleSubInvalidation(AsyncObservableSetValue<E> observable, SubInvalidationListener listener) {
            super(observable);
            this.listener = listener;
        }

        @Override
        protected AsyncSetExpressionHelper<E> addListener(
                InvalidationListener listener, AsyncObservableSet<E> currentValue) {
            AsyncSetExpressionHelper<E> helper = new Generic<>(observable, currentValue);
            helper.addListener(this.listener, currentValue);
            helper.addListener(listener, currentValue);
            return helper;
        }

        @Override
        protected AsyncSetExpressionHelper<E> removeListener(
                InvalidationListener listener, AsyncObservableSet<E> currentValue) {
            return this;
        }

        @Override
        protected AsyncSetExpressionHelper<E> addListener(
                SubInvalidationListener listener, AsyncObservableSet<E> currentValue) {
            return new Generic<>(observable, currentValue, this.listener, listener);
        }

        @Override
        protected AsyncSetExpressionHelper<E> removeListener(
                SubInvalidationListener listener, AsyncObservableSet<E> currentValue) {
            if (this.listener == null) {
                return this;
            }

            return this.listener.equals(listener) ? null : this;
        }

        @Override
        protected AsyncSetExpressionHelper<E> addListener(
                ChangeListener<? super AsyncObservableSet<E>> listener, AsyncObservableSet<E> currentValue) {
            AsyncSetExpressionHelper<E> helper = new Generic<>(observable, currentValue);
            helper.addListener(this.listener, currentValue);
            helper.addListener(listener, currentValue);
            return helper;
        }

        @Override
        protected AsyncSetExpressionHelper<E> removeListener(
                ChangeListener<? super AsyncObservableSet<E>> listener, AsyncObservableSet<E> currentValue) {
            return this;
        }

        @Override
        protected AsyncSetExpressionHelper<E> addListener(
                SubChangeListener listener, AsyncObservableSet<E> currentValue) {
            AsyncSetExpressionHelper<E> helper = new Generic<>(observable, currentValue);
            helper.addListener(this.listener, currentValue);
            helper.addListener(listener, currentValue);
            return helper;
        }

        @Override
        protected AsyncSetExpressionHelper<E> removeListener(
                SubChangeListener listener, AsyncObservableSet<E> currentValue) {
            return this;
        }

        @Override
        protected AsyncSetExpressionHelper<E> addListener(
                SetChangeListener<? super E> listener, AsyncObservableSet<E> currentValue) {
            AsyncSetExpressionHelper<E> helper = new Generic<>(observable, currentValue);
            helper.addListener(this.listener, currentValue);
            helper.addListener(listener, currentValue);
            return helper;
        }

        @Override
        protected AsyncSetExpressionHelper<E> removeListener(
                SetChangeListener<? super E> listener, AsyncObservableSet<E> currentValue) {
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
        protected void fireValueChangedEvent(AsyncObservableSet<E> newValue, boolean subChange) {
            listener.invalidated(observable, subChange);
        }

        @Override
        protected void fireValueChangedEvent(Change<? extends E> change) {
            listener.invalidated(observable, false);
        }
    }

    private static class SingleChange<E> extends AsyncSetExpressionHelper<E> {
        private final ChangeListener<? super AsyncObservableSet<E>> listener;
        private AsyncObservableSet<E> currentValue;

        private SingleChange(
                AsyncObservableSetValue<E> observable,
                AsyncObservableSet<E> currentValue,
                ChangeListener<? super AsyncObservableSet<E>> listener) {
            super(observable);
            this.listener = listener;
            this.currentValue = currentValue;
        }

        @Override
        protected AsyncSetExpressionHelper<E> addListener(
                InvalidationListener listener, AsyncObservableSet<E> currentValue) {
            AsyncSetExpressionHelper<E> helper = new Generic<>(observable, currentValue);
            helper.addListener(this.listener, currentValue);
            helper.addListener(listener, currentValue);
            return helper;
        }

        @Override
        protected AsyncSetExpressionHelper<E> removeListener(
                InvalidationListener listener, AsyncObservableSet<E> currentValue) {
            return this;
        }

        @Override
        protected AsyncSetExpressionHelper<E> addListener(
                SubInvalidationListener listener, AsyncObservableSet<E> currentValue) {
            AsyncSetExpressionHelper<E> helper = new Generic<>(observable, currentValue);
            helper.addListener(this.listener, currentValue);
            helper.addListener(listener, currentValue);
            return helper;
        }

        @Override
        protected AsyncSetExpressionHelper<E> removeListener(
                SubInvalidationListener listener, AsyncObservableSet<E> currentValue) {
            return this;
        }

        @Override
        protected AsyncSetExpressionHelper<E> addListener(
                ChangeListener<? super AsyncObservableSet<E>> listener, AsyncObservableSet<E> currentValue) {
            return new Generic<>(observable, currentValue, this.listener, listener);
        }

        @Override
        protected AsyncSetExpressionHelper<E> removeListener(
                ChangeListener<? super AsyncObservableSet<E>> listener, AsyncObservableSet<E> currentValue) {
            if (this.listener == null) {
                return this;
            }

            return this.listener.equals(listener) ? null : this;
        }

        @Override
        protected AsyncSetExpressionHelper<E> addListener(
                SubChangeListener listener, AsyncObservableSet<E> currentValue) {
            AsyncSetExpressionHelper<E> helper = new Generic<>(observable, currentValue);
            helper.addListener(this.listener, currentValue);
            helper.addListener(listener, currentValue);
            return helper;
        }

        @Override
        protected AsyncSetExpressionHelper<E> removeListener(
                SubChangeListener listener, AsyncObservableSet<E> currentValue) {
            return this;
        }

        @Override
        protected AsyncSetExpressionHelper<E> addListener(
                SetChangeListener<? super E> listener, AsyncObservableSet<E> currentValue) {
            AsyncSetExpressionHelper<E> helper = new Generic<>(observable, currentValue);
            helper.addListener(this.listener, currentValue);
            helper.addListener(listener, currentValue);
            return helper;
        }

        @Override
        protected AsyncSetExpressionHelper<E> removeListener(
                SetChangeListener<? super E> listener, AsyncObservableSet<E> currentValue) {
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
        protected void fireValueChangedEvent(AsyncObservableSet<E> newValue, boolean subChange) {
            if (subChange) {
                return;
            }

            final AsyncObservableSet<E> oldValue = currentValue;
            currentValue = newValue;
            if (currentValue != oldValue) {
                listener.changed(observable, oldValue, currentValue);
            }
        }

        @Override
        protected void fireValueChangedEvent(Change<? extends E> change) {
            listener.changed(observable, currentValue, currentValue);
        }
    }

    private static class SingleSubChange<E> extends AsyncSetExpressionHelper<E> {
        private final SubChangeListener listener;
        private AsyncObservableSet<E> currentValue;

        private SingleSubChange(
                AsyncObservableSetValue<E> observable, AsyncObservableSet<E> currentValue, SubChangeListener listener) {
            super(observable);
            this.listener = listener;
            this.currentValue = currentValue;
        }

        @Override
        protected AsyncSetExpressionHelper<E> addListener(
                InvalidationListener listener, AsyncObservableSet<E> currentValue) {
            AsyncSetExpressionHelper<E> helper = new Generic<>(observable, currentValue);
            helper.addListener(this.listener, currentValue);
            helper.addListener(listener, currentValue);
            return helper;
        }

        @Override
        protected AsyncSetExpressionHelper<E> removeListener(
                InvalidationListener listener, AsyncObservableSet<E> currentValue) {
            return this;
        }

        @Override
        protected AsyncSetExpressionHelper<E> addListener(
                SubInvalidationListener listener, AsyncObservableSet<E> currentValue) {
            AsyncSetExpressionHelper<E> helper = new Generic<>(observable, currentValue);
            helper.addListener(this.listener, currentValue);
            helper.addListener(listener, currentValue);
            return helper;
        }

        @Override
        protected AsyncSetExpressionHelper<E> removeListener(
                SubInvalidationListener listener, AsyncObservableSet<E> currentValue) {
            return this;
        }

        @Override
        protected AsyncSetExpressionHelper<E> addListener(
                ChangeListener<? super AsyncObservableSet<E>> listener, AsyncObservableSet<E> currentValue) {
            AsyncSetExpressionHelper<E> helper = new Generic<>(observable, currentValue);
            helper.addListener(this.listener, currentValue);
            helper.addListener(listener, currentValue);
            return helper;
        }

        @Override
        protected AsyncSetExpressionHelper<E> removeListener(
                ChangeListener<? super AsyncObservableSet<E>> listener, AsyncObservableSet<E> currentValue) {
            return this;
        }

        @Override
        protected AsyncSetExpressionHelper<E> addListener(
                SubChangeListener listener, AsyncObservableSet<E> currentValue) {
            return new Generic<>(observable, currentValue, this.listener, listener);
        }

        @Override
        protected AsyncSetExpressionHelper<E> removeListener(
                SubChangeListener listener, AsyncObservableSet<E> currentValue) {
            if (this.listener == null) {
                return this;
            }

            return this.listener.equals(listener) ? null : this;
        }

        @Override
        protected AsyncSetExpressionHelper<E> addListener(
                SetChangeListener<? super E> listener, AsyncObservableSet<E> currentValue) {
            AsyncSetExpressionHelper<E> helper = new Generic<>(observable, currentValue);
            helper.addListener(this.listener, currentValue);
            helper.addListener(listener, currentValue);
            return helper;
        }

        @Override
        protected AsyncSetExpressionHelper<E> removeListener(
                SetChangeListener<? super E> listener, AsyncObservableSet<E> currentValue) {
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
        protected void fireValueChangedEvent(AsyncObservableSet<E> newValue, boolean subChange) {
            final AsyncObservableSet<E> oldValue = currentValue;
            currentValue = newValue;
            if (subChange || (currentValue != oldValue)) {
                listener.changed(observable, oldValue, currentValue, subChange);
            }
        }

        @Override
        protected void fireValueChangedEvent(Change<? extends E> change) {
            listener.changed(observable, currentValue, currentValue, false);
        }
    }

    private static class SingleSetChange<E> extends AsyncSetExpressionHelper<E> {
        private final SetChangeListener<? super E> listener;
        private AsyncObservableSet<E> currentValue;

        private SingleSetChange(
                AsyncObservableSetValue<E> observable,
                AsyncObservableSet<E> currentValue,
                SetChangeListener<? super E> listener) {
            super(observable);
            this.listener = listener;
            this.currentValue = currentValue;
        }

        @Override
        protected AsyncSetExpressionHelper<E> addListener(
                InvalidationListener listener, AsyncObservableSet<E> currentValue) {
            AsyncSetExpressionHelper<E> helper = new Generic<>(observable, currentValue);
            helper.addListener(this.listener, currentValue);
            helper.addListener(listener, currentValue);
            return helper;
        }

        @Override
        protected AsyncSetExpressionHelper<E> removeListener(
                InvalidationListener listener, AsyncObservableSet<E> currentValue) {
            return this;
        }

        @Override
        protected AsyncSetExpressionHelper<E> addListener(
                SubInvalidationListener listener, AsyncObservableSet<E> currentValue) {
            AsyncSetExpressionHelper<E> helper = new Generic<>(observable, currentValue);
            helper.addListener(this.listener, currentValue);
            helper.addListener(listener, currentValue);
            return helper;
        }

        @Override
        protected AsyncSetExpressionHelper<E> removeListener(
                SubInvalidationListener listener, AsyncObservableSet<E> currentValue) {
            return this;
        }

        @Override
        protected AsyncSetExpressionHelper<E> addListener(
                ChangeListener<? super AsyncObservableSet<E>> listener, AsyncObservableSet<E> currentValue) {
            AsyncSetExpressionHelper<E> helper = new Generic<>(observable, currentValue);
            helper.addListener(this.listener, currentValue);
            helper.addListener(listener, currentValue);
            return helper;
        }

        @Override
        protected AsyncSetExpressionHelper<E> removeListener(
                ChangeListener<? super AsyncObservableSet<E>> listener, AsyncObservableSet<E> currentValue) {
            return this;
        }

        @Override
        protected AsyncSetExpressionHelper<E> addListener(
                SubChangeListener listener, AsyncObservableSet<E> currentValue) {
            AsyncSetExpressionHelper<E> helper = new Generic<>(observable, currentValue);
            helper.addListener(this.listener, currentValue);
            helper.addListener(listener, currentValue);
            return helper;
        }

        @Override
        protected AsyncSetExpressionHelper<E> removeListener(
                SubChangeListener listener, AsyncObservableSet<E> currentValue) {
            return this;
        }

        @Override
        protected AsyncSetExpressionHelper<E> addListener(
                SetChangeListener<? super E> listener, AsyncObservableSet<E> currentValue) {
            return new Generic<>(observable, currentValue, this.listener, listener);
        }

        @Override
        protected AsyncSetExpressionHelper<E> removeListener(
                SetChangeListener<? super E> listener, AsyncObservableSet<E> currentValue) {
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
        protected void fireValueChangedEvent(AsyncObservableSet<E> newValue, boolean subChange) {
            if (subChange) {
                return;
            }

            final AsyncObservableSet<E> oldValue = currentValue;
            currentValue = newValue;
            if (currentValue != oldValue) {
                final SimpleChange<E> change = new SimpleChange<>(observable);
                if (currentValue == null) {
                    try (LockedSet<E> lockedSet = oldValue.lock()) {
                        for (final E element : lockedSet) {
                            listener.onChanged(change.setRemoved(element));
                        }
                    }
                } else if (oldValue == null) {
                    try (LockedSet<E> lockedSet = currentValue.lock()) {
                        for (final E element : lockedSet) {
                            listener.onChanged(change.setAdded(element));
                        }
                    }
                } else {
                    try (LockedSet<E> lockedSet = oldValue.lock()) {
                        for (final E element : lockedSet) {
                            if (!currentValue.contains(element)) {
                                listener.onChanged(change.setRemoved(element));
                            }
                        }
                    }

                    try (LockedSet<E> lockedSet = currentValue.lock()) {
                        for (final E element : lockedSet) {
                            if (!oldValue.contains(element)) {
                                listener.onChanged(change.setAdded(element));
                            }
                        }
                    }
                }
            }
        }

        @Override
        protected void fireValueChangedEvent(final Change<? extends E> change) {
            listener.onChanged(new SimpleChange<>(observable, change));
        }
    }

    private static class Generic<E> extends AsyncSetExpressionHelper<E> {

        private InvalidationListener[] invalidationListeners;
        private SubInvalidationListener[] subInvalidationListeners;
        private ChangeListener<? super AsyncObservableSet<E>>[] changeListeners;
        private SubChangeListener[] subChangeListeners;
        private SetChangeListener<? super E>[] setChangeListeners;
        private int invalidationSize;
        private int subInvalidationSize;
        private int changeSize;
        private int subChangeSize;
        private int setChangeSize;
        private boolean locked;
        private AsyncObservableSet<E> currentValue;

        private Generic(AsyncObservableSetValue<E> observable, AsyncObservableSet<E> currentValue) {
            super(observable);
            this.currentValue = currentValue;
        }

        private Generic(
                AsyncObservableSetValue<E> observable,
                AsyncObservableSet<E> currentValue,
                InvalidationListener listener0,
                InvalidationListener listener1) {
            super(observable);
            this.invalidationListeners = new InvalidationListener[] {listener0, listener1};
            this.invalidationSize = 2;
            this.currentValue = currentValue;
        }

        private Generic(
                AsyncObservableSetValue<E> observable,
                AsyncObservableSet<E> currentValue,
                SubInvalidationListener listener0,
                SubInvalidationListener listener1) {
            super(observable);
            this.subInvalidationListeners = new SubInvalidationListener[] {listener0, listener1};
            this.subInvalidationSize = 2;
            this.currentValue = currentValue;
        }

        @SuppressWarnings("unchecked")
        private Generic(
                AsyncObservableSetValue<E> observable,
                AsyncObservableSet<E> currentValue,
                ChangeListener<? super AsyncObservableSet<E>> listener0,
                ChangeListener<? super AsyncObservableSet<E>> listener1) {
            super(observable);
            this.changeListeners = new ChangeListener[] {listener0, listener1};
            this.changeSize = 2;
            this.currentValue = currentValue;
        }

        private Generic(
                AsyncObservableSetValue<E> observable,
                AsyncObservableSet<E> currentValue,
                SubChangeListener listener0,
                SubChangeListener listener1) {
            super(observable);
            this.subChangeListeners = new SubChangeListener[] {listener0, listener1};
            this.subChangeSize = 2;
            this.currentValue = currentValue;
        }

        @SuppressWarnings("unchecked")
        private Generic(
                AsyncObservableSetValue<E> observable,
                AsyncObservableSet<E> currentValue,
                SetChangeListener<? super E> listener0,
                SetChangeListener<? super E> listener1) {
            super(observable);
            this.setChangeListeners = new SetChangeListener[] {listener0, listener1};
            this.setChangeSize = 2;
            this.currentValue = currentValue;
        }

        @Override
        protected AsyncSetExpressionHelper<E> addListener(
                InvalidationListener listener, AsyncObservableSet<E> currentValue) {
            if (invalidationListeners == null) {
                invalidationListeners = new InvalidationListener[] {listener};
                invalidationSize = 1;
            } else {
                final int oldCapacity = invalidationListeners.length;
                if (locked) {
                    final int newCapacity = (invalidationSize < oldCapacity) ? oldCapacity : (oldCapacity * 3) / 2 + 1;
                    invalidationListeners = Arrays.copyOf(invalidationListeners, newCapacity);
                } else if (invalidationSize == oldCapacity) {
                    invalidationSize = trim(invalidationSize, invalidationListeners);
                    if (invalidationSize == oldCapacity) {
                        final int newCapacity = (oldCapacity * 3) / 2 + 1;
                        invalidationListeners = Arrays.copyOf(invalidationListeners, newCapacity);
                    }
                }

                invalidationListeners[invalidationSize++] = listener;
            }

            return this;
        }

        @Override
        protected AsyncSetExpressionHelper<E> removeListener(
                InvalidationListener listener, AsyncObservableSet<E> currentValue) {
            if (invalidationListeners != null) {
                for (int index = 0; index < invalidationSize; index++) {
                    if (invalidationListeners[index].equals(listener)) {
                        AsyncSetExpressionHelper<E> helper = getSingleListenerHelper(index, 1, 0, 0, 0, 0);
                        if (helper != null) {
                            return helper;
                        }

                        if (invalidationSize == 1) {
                            invalidationListeners = null;
                            invalidationSize = 0;
                        } else {
                            final int numMoved = invalidationSize - index - 1;
                            final InvalidationListener[] oldListeners = invalidationListeners;
                            if (locked) {
                                invalidationListeners = new InvalidationListener[invalidationListeners.length];
                                System.arraycopy(oldListeners, 0, invalidationListeners, 0, index + 1);
                            }

                            if (numMoved > 0) {
                                System.arraycopy(oldListeners, index + 1, invalidationListeners, index, numMoved);
                            }

                            invalidationSize--;
                            if (!locked) {
                                invalidationListeners[invalidationSize] = null; // Let gc do its work
                            }
                        }

                        break;
                    }
                }
            }

            return this;
        }

        @Override
        protected AsyncSetExpressionHelper<E> addListener(
                SubInvalidationListener listener, AsyncObservableSet<E> currentValue) {
            if (subInvalidationListeners == null) {
                subInvalidationListeners = new SubInvalidationListener[] {listener};
                subInvalidationSize = 1;
            } else {
                final int oldCapacity = subInvalidationListeners.length;
                if (locked) {
                    final int newCapacity =
                        (subInvalidationSize < oldCapacity) ? oldCapacity : (oldCapacity * 3) / 2 + 1;
                    subInvalidationListeners = Arrays.copyOf(subInvalidationListeners, newCapacity);
                } else if (subInvalidationSize == oldCapacity) {
                    subInvalidationSize = trim(subInvalidationSize, subInvalidationListeners);
                    if (subInvalidationSize == oldCapacity) {
                        final int newCapacity = (oldCapacity * 3) / 2 + 1;
                        subInvalidationListeners = Arrays.copyOf(subInvalidationListeners, newCapacity);
                    }
                }

                subInvalidationListeners[subInvalidationSize++] = listener;
            }

            return this;
        }

        @Override
        protected AsyncSetExpressionHelper<E> removeListener(
                SubInvalidationListener listener, AsyncObservableSet<E> currentValue) {
            if (subInvalidationListeners != null) {
                for (int index = 0; index < subInvalidationSize; index++) {
                    if (subInvalidationListeners[index].equals(listener)) {
                        AsyncSetExpressionHelper<E> helper = getSingleListenerHelper(index, 0, 1, 0, 0, 0);
                        if (helper != null) {
                            return helper;
                        }

                        if (subInvalidationSize == 1) {
                            subInvalidationListeners = null;
                            subInvalidationSize = 0;
                        } else {
                            final int numMoved = subInvalidationSize - index - 1;
                            final SubInvalidationListener[] oldListeners = subInvalidationListeners;
                            if (locked) {
                                subInvalidationListeners = new SubInvalidationListener[subInvalidationListeners.length];
                                System.arraycopy(oldListeners, 0, subInvalidationListeners, 0, index + 1);
                            }

                            if (numMoved > 0) {
                                System.arraycopy(oldListeners, index + 1, subInvalidationListeners, index, numMoved);
                            }

                            subInvalidationSize--;
                            if (!locked) {
                                subInvalidationListeners[subInvalidationSize] = null; // Let gc do its work
                            }
                        }

                        break;
                    }
                }
            }

            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        protected AsyncSetExpressionHelper<E> addListener(
                ChangeListener<? super AsyncObservableSet<E>> listener, AsyncObservableSet<E> currentValue) {
            if (changeListeners == null) {
                changeListeners = new ChangeListener[] {listener};
                changeSize = 1;
            } else {
                final int oldCapacity = changeListeners.length;
                if (locked) {
                    final int newCapacity = (changeSize < oldCapacity) ? oldCapacity : (oldCapacity * 3) / 2 + 1;
                    changeListeners = Arrays.copyOf(changeListeners, newCapacity);
                } else if (changeSize == oldCapacity) {
                    changeSize = trim(changeSize, changeListeners);
                    if (changeSize == oldCapacity) {
                        final int newCapacity = (oldCapacity * 3) / 2 + 1;
                        changeListeners = Arrays.copyOf(changeListeners, newCapacity);
                    }
                }

                changeListeners[changeSize++] = listener;
            }

            if (changeSize == 1) {
                this.currentValue = currentValue;
            }

            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        protected AsyncSetExpressionHelper<E> removeListener(
                ChangeListener<? super AsyncObservableSet<E>> listener, AsyncObservableSet<E> currentValue) {
            if (changeListeners != null) {
                for (int index = 0; index < changeSize; index++) {
                    if (changeListeners[index].equals(listener)) {
                        AsyncSetExpressionHelper<E> helper = getSingleListenerHelper(index, 0, 0, 1, 0, 0);
                        if (helper != null) {
                            return helper;
                        }

                        if (changeSize == 1) {
                            changeListeners = null;
                            changeSize = 0;
                        } else {
                            final int numMoved = changeSize - index - 1;
                            final ChangeListener<? super AsyncObservableSet<E>>[] oldListeners = changeListeners;
                            if (locked) {
                                changeListeners = new ChangeListener[changeListeners.length];
                                System.arraycopy(oldListeners, 0, changeListeners, 0, index + 1);
                            }

                            if (numMoved > 0) {
                                System.arraycopy(oldListeners, index + 1, changeListeners, index, numMoved);
                            }

                            changeSize--;
                            if (!locked) {
                                changeListeners[changeSize] = null; // Let gc do its work
                            }
                        }

                        break;
                    }
                }
            }

            return this;
        }

        @Override
        protected AsyncSetExpressionHelper<E> addListener(
                SubChangeListener listener, AsyncObservableSet<E> currentValue) {
            if (subChangeListeners == null) {
                subChangeListeners = new SubChangeListener[] {listener};
                subChangeSize = 1;
            } else {
                final int oldCapacity = subChangeListeners.length;
                if (locked) {
                    final int newCapacity = (subChangeSize < oldCapacity) ? oldCapacity : (oldCapacity * 3) / 2 + 1;
                    subChangeListeners = Arrays.copyOf(subChangeListeners, newCapacity);
                } else if (subChangeSize == oldCapacity) {
                    subChangeSize = trim(subChangeSize, subChangeListeners);
                    if (subChangeSize == oldCapacity) {
                        final int newCapacity = (oldCapacity * 3) / 2 + 1;
                        subChangeListeners = Arrays.copyOf(subChangeListeners, newCapacity);
                    }
                }

                subChangeListeners[subChangeSize++] = listener;
            }

            if (subChangeSize == 1) {
                this.currentValue = currentValue;
            }

            return this;
        }

        @Override
        protected AsyncSetExpressionHelper<E> removeListener(
                SubChangeListener listener, AsyncObservableSet<E> currentValue) {
            if (subChangeListeners != null) {
                for (int index = 0; index < subChangeSize; index++) {
                    if (subChangeListeners[index].equals(listener)) {
                        AsyncSetExpressionHelper<E> helper = getSingleListenerHelper(index, 0, 0, 0, 1, 0);
                        if (helper != null) {
                            return helper;
                        }

                        if (subChangeSize == 1) {
                            subChangeListeners = null;
                            subChangeSize = 0;
                        } else {
                            final int numMoved = subChangeSize - index - 1;
                            final SubChangeListener[] oldListeners = subChangeListeners;
                            if (locked) {
                                subChangeListeners = new SubChangeListener[subChangeListeners.length];
                                System.arraycopy(oldListeners, 0, subChangeListeners, 0, index + 1);
                            }

                            if (numMoved > 0) {
                                System.arraycopy(oldListeners, index + 1, subChangeListeners, index, numMoved);
                            }

                            subChangeSize--;
                            if (!locked) {
                                subChangeListeners[subChangeSize] = null; // Let gc do its work
                            }
                        }

                        break;
                    }
                }
            }

            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        protected AsyncSetExpressionHelper<E> addListener(
                SetChangeListener<? super E> listener, AsyncObservableSet<E> currentValue) {
            if (setChangeListeners == null) {
                setChangeListeners = new SetChangeListener[] {listener};
                setChangeSize = 1;
            } else {
                final int oldCapacity = setChangeListeners.length;
                if (locked) {
                    final int newCapacity = (setChangeSize < oldCapacity) ? oldCapacity : (oldCapacity * 3) / 2 + 1;
                    setChangeListeners = Arrays.copyOf(setChangeListeners, newCapacity);
                } else if (setChangeSize == oldCapacity) {
                    setChangeSize = trim(setChangeSize, setChangeListeners);
                    if (setChangeSize == oldCapacity) {
                        final int newCapacity = (oldCapacity * 3) / 2 + 1;
                        setChangeListeners = Arrays.copyOf(setChangeListeners, newCapacity);
                    }
                }

                setChangeListeners[setChangeSize++] = listener;
            }

            if (setChangeSize == 1) {
                this.currentValue = currentValue;
            }

            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        protected AsyncSetExpressionHelper<E> removeListener(
                SetChangeListener<? super E> listener, AsyncObservableSet<E> currentValue) {
            if (setChangeListeners != null) {
                for (int index = 0; index < setChangeSize; index++) {
                    if (setChangeListeners[index].equals(listener)) {
                        AsyncSetExpressionHelper<E> helper = getSingleListenerHelper(index, 0, 0, 0, 0, 1);
                        if (helper != null) {
                            return helper;
                        }

                        if (setChangeSize == 1) {
                            setChangeListeners = null;
                            setChangeSize = 0;
                        } else {
                            final int numMoved = setChangeSize - index - 1;
                            final SetChangeListener<? super E>[] oldListeners = setChangeListeners;
                            if (locked) {
                                setChangeListeners = new SetChangeListener[setChangeListeners.length];
                                System.arraycopy(oldListeners, 0, setChangeListeners, 0, index + 1);
                            }

                            if (numMoved > 0) {
                                System.arraycopy(oldListeners, index + 1, setChangeListeners, index, numMoved);
                            }

                            setChangeSize--;
                            if (!locked) {
                                setChangeListeners[setChangeSize] = null; // Let gc do its work
                            }
                        }

                        break;
                    }
                }
            }

            return this;
        }

        @Override
        protected boolean validatesValue() {
            return changeSize > 0 || setChangeSize > 0;
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
        protected void fireValueChangedEvent(AsyncObservableSet<E> newValue, boolean subChange) {
            if ((changeSize == 0) && (setChangeSize == 0)) {
                notifyListeners(currentValue, null);
            } else {
                final AsyncObservableSet<E> oldValue = currentValue;
                currentValue = newValue;
                notifyListeners(oldValue, null);
            }
        }

        @Override
        protected void fireValueChangedEvent(final Change<? extends E> change) {
            final SimpleChange<E> mappedChange = (setChangeSize == 0) ? null : new SimpleChange<>(observable, change);
            notifyListeners(currentValue, mappedChange);
        }

        private void notifyListeners(AsyncObservableSet<E> oldValue, SimpleChange<E> change) {
            final InvalidationListener[] curInvalidationList = invalidationListeners;
            final int curInvalidationSize = invalidationSize;
            final ChangeListener<? super AsyncObservableSet<E>>[] curChangeList = changeListeners;
            final int curChangeSize = changeSize;
            final SetChangeListener<? super E>[] curSetChangeList = setChangeListeners;
            final int curSetChangeSize = setChangeSize;
            try {
                locked = true;
                for (int i = 0; i < curInvalidationSize; i++) {
                    curInvalidationList[i].invalidated(observable);
                }

                if ((currentValue != oldValue) || (change != null)) {
                    for (int i = 0; i < curChangeSize; i++) {
                        curChangeList[i].changed(observable, oldValue, currentValue);
                    }

                    if (curSetChangeSize > 0) {
                        if (change != null) {
                            for (int i = 0; i < curSetChangeSize; i++) {
                                curSetChangeList[i].onChanged(change);
                            }
                        } else {
                            change = new SimpleChange<>(observable);
                            if (currentValue == null) {
                                try (LockedSet<E> lockedSet = oldValue.lock()) {
                                    for (final E element : lockedSet) {
                                        change.setRemoved(element);
                                        for (int i = 0; i < curSetChangeSize; i++) {
                                            curSetChangeList[i].onChanged(change);
                                        }
                                    }
                                }
                            } else if (oldValue == null) {
                                try (LockedSet<E> lockedSet = currentValue.lock()) {
                                    for (final E element : lockedSet) {
                                        change.setAdded(element);
                                        for (int i = 0; i < curSetChangeSize; i++) {
                                            curSetChangeList[i].onChanged(change);
                                        }
                                    }
                                }
                            } else {
                                try (LockedSet<E> lockedSet = oldValue.lock()) {
                                    for (final E element : lockedSet) {
                                        if (!currentValue.contains(element)) {
                                            change.setRemoved(element);
                                            for (int i = 0; i < curSetChangeSize; i++) {
                                                curSetChangeList[i].onChanged(change);
                                            }
                                        }
                                    }
                                }

                                try (LockedSet<E> lockedSet = currentValue.lock()) {
                                    for (final E element : lockedSet) {
                                        if (!oldValue.contains(element)) {
                                            change.setAdded(element);
                                            for (int i = 0; i < curSetChangeSize; i++) {
                                                curSetChangeList[i].onChanged(change);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } finally {
                locked = false;
            }
        }

        private AsyncSetExpressionHelper<E> getSingleListenerHelper(
                int index,
                int removeInvalidation,
                int removeSubInvalidation,
                int removeChange,
                int removeSubChange,
                int removeSetChange) {
            if (invalidationSize - removeInvalidation == 1
                    && subInvalidationSize - removeSubInvalidation == 0
                    && changeSize - removeChange == 0
                    && subChangeSize - removeSubChange == 0
                    && setChangeSize - removeSetChange == 0) {
                return new SingleInvalidation<>(
                    observable, invalidationListeners[invalidationSize == 2 ? 1 - index : 0]);
            }

            if (invalidationSize - removeInvalidation == 0
                    && subInvalidationSize - removeSubInvalidation == 1
                    && changeSize - removeChange == 0
                    && subChangeSize - removeSubChange == 0
                    && setChangeSize - removeSetChange == 0) {
                return new SingleSubInvalidation<>(
                    observable, subInvalidationListeners[subInvalidationSize == 2 ? 1 - index : 0]);
            }

            if (invalidationSize - removeInvalidation == 0
                    && subInvalidationSize - removeSubInvalidation == 0
                    && changeSize - removeChange == 1
                    && subChangeSize - removeSubChange == 0
                    && setChangeSize - removeSetChange == 0) {
                return new SingleChange<>(observable, currentValue, changeListeners[changeSize == 2 ? 1 - index : 0]);
            }

            if (invalidationSize - removeInvalidation == 0
                    && subInvalidationSize - removeSubInvalidation == 0
                    && changeSize - removeChange == 0
                    && subChangeSize - removeSubChange == 1
                    && setChangeSize - removeSetChange == 0) {
                return new SingleSubChange<>(
                    observable, currentValue, subChangeListeners[subChangeSize == 2 ? 1 - index : 0]);
            }

            if (invalidationSize - removeInvalidation == 0
                    && subInvalidationSize - removeSubInvalidation == 0
                    && changeSize - removeChange == 0
                    && subChangeSize - removeSubChange == 0
                    && setChangeSize - removeSetChange == 1) {
                return new SingleSetChange<>(
                    observable, currentValue, setChangeListeners[setChangeSize == 2 ? 1 - index : 0]);
            }

            return null;
        }
    }

    public static class SimpleChange<E> extends Change<E> {
        private E old;
        private E added;
        private boolean addOp;

        SimpleChange(AsyncObservableSet<E> set) {
            super(set);
        }

        SimpleChange(AsyncObservableSet<E> set, Change<? extends E> source) {
            super(set);
            old = source.getElementRemoved();
            added = source.getElementAdded();
            addOp = source.wasAdded();
        }

        public SimpleChange<E> setRemoved(E old) {
            this.old = old;
            this.added = null;
            addOp = false;
            return this;
        }

        public SimpleChange<E> setAdded(E added) {
            this.old = null;
            this.added = added;
            addOp = true;
            return this;
        }

        @Override
        public boolean wasAdded() {
            return addOp;
        }

        @Override
        public boolean wasRemoved() {
            return !addOp;
        }

        @Override
        public E getElementAdded() {
            return added;
        }

        @Override
        public E getElementRemoved() {
            return old;
        }

        @Override
        public String toString() {
            return addOp ? "added " + added : "removed " + old;
        }
    }
}
