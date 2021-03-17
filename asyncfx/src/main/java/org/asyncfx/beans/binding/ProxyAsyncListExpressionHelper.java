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
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import org.asyncfx.PublishSource;
import org.asyncfx.beans.AsyncSubObservable;
import org.asyncfx.beans.SubInvalidationListener;
import org.asyncfx.beans.value.AsyncObservableListValue;
import org.asyncfx.beans.value.AsyncSubObservableValue;
import org.asyncfx.beans.value.SubChangeListener;
import org.asyncfx.collections.AsyncListChangeListener.AsyncChange;
import org.asyncfx.collections.AsyncNonIterableChange;
import org.asyncfx.collections.AsyncObservableList;
import org.asyncfx.collections.AsyncSourceAdapterChange;
import org.asyncfx.collections.FXAsyncCollections;
import org.jetbrains.annotations.Nullable;

@PublishSource(
    module = "openjfx",
    licenses = {"openjfx", "intel-gpl-classpath-exception"}
)
public abstract class ProxyAsyncListExpressionHelper<E> extends ExpressionHelperBase {

    public static <E> ProxyAsyncListExpressionHelper<E> addListener(
            ProxyAsyncListExpressionHelper<E> helper,
            AsyncObservableListValue<E> observable,
            AsyncObservableListValue<E> peer,
            AsyncObservableList<E> currentValue,
            InvalidationListener listener) {
        if ((observable == null) || (listener == null)) {
            throw new NullPointerException();
        }

        return (helper == null)
            ? new SingleInvalidation<>(observable, peer, listener)
            : helper.addListener(listener, currentValue);
    }

    public static <E> ProxyAsyncListExpressionHelper<E> removeListener(
            ProxyAsyncListExpressionHelper<E> helper,
            AsyncObservableList<E> currentValue,
            InvalidationListener listener) {
        if (listener == null) {
            throw new NullPointerException();
        }

        return (helper == null) ? null : helper.removeListener(listener, currentValue);
    }

    public static <E> ProxyAsyncListExpressionHelper<E> addListener(
            ProxyAsyncListExpressionHelper<E> helper,
            AsyncObservableListValue<E> observable,
            AsyncObservableListValue<E> peer,
            AsyncObservableList<E> currentValue,
            SubInvalidationListener listener) {
        if ((observable == null) || (listener == null)) {
            throw new NullPointerException();
        }

        return (helper == null)
            ? new SingleSubInvalidation<>(observable, peer, listener)
            : helper.addListener(listener, currentValue);
    }

    public static <E> ProxyAsyncListExpressionHelper<E> removeListener(
            ProxyAsyncListExpressionHelper<E> helper,
            AsyncObservableList<E> currentValue,
            SubInvalidationListener listener) {
        if (listener == null) {
            throw new NullPointerException();
        }

        return (helper == null) ? null : helper.removeListener(listener, currentValue);
    }

    public static <E> ProxyAsyncListExpressionHelper<E> addListener(
            ProxyAsyncListExpressionHelper<E> helper,
            AsyncObservableListValue<E> observable,
            AsyncObservableListValue<E> peer,
            AsyncObservableList<E> currentValue,
            ChangeListener<? super AsyncObservableList<E>> listener) {
        if ((observable == null) || (listener == null)) {
            throw new NullPointerException();
        }

        return (helper == null)
            ? new SingleChange<>(observable, peer, currentValue, listener)
            : helper.addListener(listener, currentValue);
    }

    public static <E> ProxyAsyncListExpressionHelper<E> removeListener(
            ProxyAsyncListExpressionHelper<E> helper,
            AsyncObservableList<E> currentValue,
            ChangeListener<? super AsyncObservableList<E>> listener) {
        if (listener == null) {
            throw new NullPointerException();
        }

        return (helper == null) ? null : helper.removeListener(listener, currentValue);
    }

    public static <E> ProxyAsyncListExpressionHelper<E> addListener(
            ProxyAsyncListExpressionHelper<E> helper,
            AsyncObservableListValue<E> observable,
            AsyncObservableListValue<E> peer,
            AsyncObservableList<E> currentValue,
            SubChangeListener listener) {
        if ((observable == null) || (listener == null)) {
            throw new NullPointerException();
        }

        return (helper == null)
            ? new SingleSubChange<>(observable, peer, currentValue, listener)
            : helper.addListener(listener, currentValue);
    }

    public static <E> ProxyAsyncListExpressionHelper<E> removeListener(
            ProxyAsyncListExpressionHelper<E> helper, AsyncObservableList<E> currentValue, SubChangeListener listener) {
        if (listener == null) {
            throw new NullPointerException();
        }

        return (helper == null) ? null : helper.removeListener(listener, currentValue);
    }

    public static <E> ProxyAsyncListExpressionHelper<E> addListener(
            ProxyAsyncListExpressionHelper<E> helper,
            AsyncObservableListValue<E> observable,
            AsyncObservableListValue<E> peer,
            AsyncObservableList<E> currentValue,
            ListChangeListener<? super E> listener) {
        if ((observable == null) || (listener == null)) {
            throw new NullPointerException();
        }

        return (helper == null)
            ? new SingleListChange<>(observable, peer, currentValue, listener)
            : helper.addListener(listener, currentValue);
    }

    public static <E> ProxyAsyncListExpressionHelper<E> removeListener(
            ProxyAsyncListExpressionHelper<E> helper,
            AsyncObservableList<E> currentValue,
            ListChangeListener<? super E> listener) {
        if (listener == null) {
            throw new NullPointerException();
        }

        return (helper == null) ? null : helper.removeListener(listener, currentValue);
    }

    public static <T> boolean validatesValue(ProxyAsyncListExpressionHelper<T> helper) {
        if (helper != null) {
            return helper.validatesValue();
        }

        return false;
    }

    public static <T> boolean containsBidirectionalBindingEndpoints(ProxyAsyncListExpressionHelper<T> helper) {
        if (helper != null) {
            return helper.containsBidirectionalBindingEndpoints();
        }

        return false;
    }

    public static <E> void fireValueChangedEvent(
            ProxyAsyncListExpressionHelper<E> helper, AsyncObservableList<E> newValue, boolean subChange) {
        if (helper != null) {
            helper.fireValueChangedEvent(newValue, subChange);
        }
    }

    public static <E> void fireValueChangedEvent(
            ProxyAsyncListExpressionHelper<E> helper, AsyncChange<? extends E> change) {
        if (helper != null) {
            helper.fireValueChangedEvent(change);
        }
    }

    public static <T> void setPeer(
            ProxyAsyncListExpressionHelper<T> helper, @Nullable AsyncObservableListValue<T> peer) {
        if (helper != null) {
            helper.setPeer(peer);
        }
    }

    protected final AsyncObservableListValue<E> observable;
    protected AsyncObservableListValue<E> peer;

    ProxyAsyncListExpressionHelper(AsyncObservableListValue<E> observable, AsyncObservableListValue<E> peer) {
        this.observable = observable;
        this.peer = peer;
    }

    protected abstract ProxyAsyncListExpressionHelper<E> addListener(
            InvalidationListener listener, AsyncObservableList<E> currentValue);

    protected abstract ProxyAsyncListExpressionHelper<E> removeListener(
            InvalidationListener listener, AsyncObservableList<E> currentValue);

    protected abstract ProxyAsyncListExpressionHelper<E> addListener(
            SubInvalidationListener listener, AsyncObservableList<E> currentValue);

    protected abstract ProxyAsyncListExpressionHelper<E> removeListener(
            SubInvalidationListener listener, AsyncObservableList<E> currentValue);

    protected abstract ProxyAsyncListExpressionHelper<E> addListener(
            ChangeListener<? super AsyncObservableList<E>> listener, AsyncObservableList<E> currentValue);

    protected abstract ProxyAsyncListExpressionHelper<E> removeListener(
            ChangeListener<? super AsyncObservableList<E>> listener, AsyncObservableList<E> currentValue);

    protected abstract ProxyAsyncListExpressionHelper<E> addListener(
            SubChangeListener listener, AsyncObservableList<E> currentValue);

    protected abstract ProxyAsyncListExpressionHelper<E> removeListener(
            SubChangeListener listener, AsyncObservableList<E> currentValue);

    protected abstract ProxyAsyncListExpressionHelper<E> addListener(
            ListChangeListener<? super E> listener, AsyncObservableList<E> currentValue);

    protected abstract ProxyAsyncListExpressionHelper<E> removeListener(
            ListChangeListener<? super E> listener, AsyncObservableList<E> currentValue);

    protected abstract boolean validatesValue();

    protected abstract boolean containsBidirectionalBindingEndpoints();

    protected abstract void fireValueChangedEvent(AsyncObservableList<E> newValue, boolean subChange);

    protected abstract void fireValueChangedEvent(AsyncChange<? extends E> change);

    protected abstract void setPeer(@Nullable AsyncObservableListValue<E> peer);

    private static class SingleInvalidation<E> extends ProxyAsyncListExpressionHelper<E> {
        private final InvalidationListener peerListener =
            new InvalidationListener() {
                @Override
                public void invalidated(Observable observable) {
                    listener.invalidated(SingleInvalidation.this.observable);
                }
            };

        private final InvalidationListener listener;

        private SingleInvalidation(
                AsyncObservableListValue<E> observable,
                AsyncObservableListValue<E> peer,
                InvalidationListener listener) {
            super(observable, peer);
            this.listener = listener;
            addPeerListener();
        }

        @Override
        protected ProxyAsyncListExpressionHelper<E> addListener(
                InvalidationListener listener, AsyncObservableList<E> currentValue) {
            removePeerListener();
            return new Generic<>(observable, peer, currentValue, this.listener, listener);
        }

        @Override
        protected ProxyAsyncListExpressionHelper<E> removeListener(
                InvalidationListener listener, AsyncObservableList<E> currentValue) {
            if (this.listener != null && this.listener.equals(listener)) {
                removePeerListener();
                return null;
            }

            return this;
        }

        @Override
        protected ProxyAsyncListExpressionHelper<E> addListener(
                SubInvalidationListener listener, AsyncObservableList<E> currentValue) {
            removePeerListener();
            return new Generic<>(observable, peer, currentValue)
                .addListener(this.listener, currentValue)
                .addListener(listener, currentValue);
        }

        @Override
        protected ProxyAsyncListExpressionHelper<E> removeListener(
                SubInvalidationListener listener, AsyncObservableList<E> currentValue) {
            return this;
        }

        @Override
        protected ProxyAsyncListExpressionHelper<E> addListener(
                ChangeListener<? super AsyncObservableList<E>> listener, AsyncObservableList<E> currentValue) {
            removePeerListener();
            return new Generic<>(observable, peer, currentValue)
                .addListener(this.listener, currentValue)
                .addListener(listener, currentValue);
        }

        @Override
        protected ProxyAsyncListExpressionHelper<E> removeListener(
                ChangeListener<? super AsyncObservableList<E>> listener, AsyncObservableList<E> currentValue) {
            return this;
        }

        @Override
        protected ProxyAsyncListExpressionHelper<E> addListener(
                SubChangeListener listener, AsyncObservableList<E> currentValue) {
            removePeerListener();
            return new Generic<>(observable, peer, currentValue)
                .addListener(this.listener, currentValue)
                .addListener(listener, currentValue);
        }

        @Override
        protected ProxyAsyncListExpressionHelper<E> removeListener(
                SubChangeListener listener, AsyncObservableList<E> currentValue) {
            return this;
        }

        @Override
        protected ProxyAsyncListExpressionHelper<E> addListener(
                ListChangeListener<? super E> listener, AsyncObservableList<E> currentValue) {
            removePeerListener();
            return new Generic<>(observable, peer, currentValue)
                .addListener(this.listener, currentValue)
                .addListener(listener, currentValue);
        }

        @Override
        protected ProxyAsyncListExpressionHelper<E> removeListener(
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

        @Override
        protected void setPeer(@Nullable AsyncObservableListValue<E> peer) {
            removePeerListener();
            this.peer = peer;
            addPeerListener();
            fireValueChangedEvent(observable.getValue(), false);
        }

        private void addPeerListener() {
            if (peer != null) {
                peer.addListener(peerListener);
            }
        }

        private void removePeerListener() {
            if (peer != null) {
                peer.removeListener(peerListener);
            }
        }
    }

    private static class SingleSubInvalidation<E> extends ProxyAsyncListExpressionHelper<E> {
        private final SubInvalidationListener peerListener =
            new SubInvalidationListener() {
                @Override
                public void invalidated(Observable observable, boolean subInvalidation) {
                    listener.invalidated(SingleSubInvalidation.this.observable, subInvalidation);
                }
            };

        private final SubInvalidationListener listener;

        private SingleSubInvalidation(
                AsyncObservableListValue<E> observable,
                AsyncObservableListValue<E> peer,
                SubInvalidationListener listener) {
            super(observable, peer);
            this.listener = listener;
            addPeerListener();
        }

        @Override
        protected ProxyAsyncListExpressionHelper<E> addListener(
                InvalidationListener listener, AsyncObservableList<E> currentValue) {
            removePeerListener();
            return new Generic<>(observable, peer, currentValue)
                .addListener(this.listener, currentValue)
                .addListener(listener, currentValue);
        }

        @Override
        protected ProxyAsyncListExpressionHelper<E> removeListener(
                InvalidationListener listener, AsyncObservableList<E> currentValue) {
            return this;
        }

        @Override
        protected ProxyAsyncListExpressionHelper<E> addListener(
                SubInvalidationListener listener, AsyncObservableList<E> currentValue) {
            removePeerListener();
            return new Generic<>(observable, peer, currentValue, this.listener, listener);
        }

        @Override
        protected ProxyAsyncListExpressionHelper<E> removeListener(
                SubInvalidationListener listener, AsyncObservableList<E> currentValue) {
            if (this.listener != null && this.listener.equals(listener)) {
                removePeerListener();
                return null;
            }

            return this;
        }

        @Override
        protected ProxyAsyncListExpressionHelper<E> addListener(
                ChangeListener<? super AsyncObservableList<E>> listener, AsyncObservableList<E> currentValue) {
            removePeerListener();
            return new Generic<>(observable, peer, currentValue)
                .addListener(this.listener, currentValue)
                .addListener(listener, currentValue);
        }

        @Override
        protected ProxyAsyncListExpressionHelper<E> removeListener(
                ChangeListener<? super AsyncObservableList<E>> listener, AsyncObservableList<E> currentValue) {
            return this;
        }

        @Override
        protected ProxyAsyncListExpressionHelper<E> addListener(
                SubChangeListener listener, AsyncObservableList<E> currentValue) {
            removePeerListener();
            return new Generic<>(observable, peer, currentValue)
                .addListener(this.listener, currentValue)
                .addListener(listener, currentValue);
        }

        @Override
        protected ProxyAsyncListExpressionHelper<E> removeListener(
                SubChangeListener listener, AsyncObservableList<E> currentValue) {
            return this;
        }

        @Override
        protected ProxyAsyncListExpressionHelper<E> addListener(
                ListChangeListener<? super E> listener, AsyncObservableList<E> currentValue) {
            removePeerListener();
            return new Generic<>(observable, peer, currentValue)
                .addListener(this.listener, currentValue)
                .addListener(listener, currentValue);
        }

        @Override
        protected ProxyAsyncListExpressionHelper<E> removeListener(
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

        @Override
        protected void setPeer(@Nullable AsyncObservableListValue<E> peer) {
            removePeerListener();
            this.peer = peer;
            addPeerListener();
            fireValueChangedEvent(observable.getValue(), false);
        }

        private void addPeerListener() {
            if (peer instanceof AsyncSubObservable) {
                ((AsyncSubObservable)peer).addListener(peerListener);
            }
        }

        private void removePeerListener() {
            if (peer instanceof AsyncSubObservable) {
                ((AsyncSubObservable)peer).removeListener(peerListener);
            }
        }
    }

    private static class SingleChange<E> extends ProxyAsyncListExpressionHelper<E> {
        private final ChangeListener<? super AsyncObservableList<E>> peerListener =
            new ChangeListener<>() {
                @Override
                public void changed(
                        ObservableValue<? extends AsyncObservableList<E>> observable,
                        AsyncObservableList<E> oldValue,
                        AsyncObservableList<E> newValue) {
                    listener.changed(SingleChange.this.observable, oldValue, newValue);
                }
            };

        private final ChangeListener<? super AsyncObservableList<E>> listener;
        private AsyncObservableList<E> currentValue;

        private SingleChange(
                AsyncObservableListValue<E> observable,
                AsyncObservableListValue<E> peer,
                AsyncObservableList<E> currentValue,
                ChangeListener<? super AsyncObservableList<E>> listener) {
            super(observable, peer);
            this.listener = listener;
            this.currentValue = currentValue;
            addPeerListener();
        }

        @Override
        protected ProxyAsyncListExpressionHelper<E> addListener(
                InvalidationListener listener, AsyncObservableList<E> currentValue) {
            removePeerListener();
            return new Generic<>(observable, peer, currentValue)
                .addListener(this.listener, currentValue)
                .addListener(listener, currentValue);
        }

        @Override
        protected ProxyAsyncListExpressionHelper<E> removeListener(
                InvalidationListener listener, AsyncObservableList<E> currentValue) {
            return this;
        }

        @Override
        protected ProxyAsyncListExpressionHelper<E> addListener(
                SubInvalidationListener listener, AsyncObservableList<E> currentValue) {
            removePeerListener();
            return new Generic<>(observable, peer, currentValue)
                .addListener(this.listener, currentValue)
                .addListener(listener, currentValue);
        }

        @Override
        protected ProxyAsyncListExpressionHelper<E> removeListener(
                SubInvalidationListener listener, AsyncObservableList<E> currentValue) {
            return this;
        }

        @Override
        protected ProxyAsyncListExpressionHelper<E> addListener(
                ChangeListener<? super AsyncObservableList<E>> listener, AsyncObservableList<E> currentValue) {
            removePeerListener();
            return new Generic<>(observable, peer, currentValue, this.listener, listener);
        }

        @Override
        protected ProxyAsyncListExpressionHelper<E> removeListener(
                ChangeListener<? super AsyncObservableList<E>> listener, AsyncObservableList<E> currentValue) {
            if (this.listener != null && this.listener.equals(listener)) {
                removePeerListener();
                return null;
            }

            return this;
        }

        @Override
        protected ProxyAsyncListExpressionHelper<E> addListener(
                SubChangeListener listener, AsyncObservableList<E> currentValue) {
            removePeerListener();
            return new Generic<>(observable, peer, currentValue)
                .addListener(this.listener, currentValue)
                .addListener(listener, currentValue);
        }

        @Override
        protected ProxyAsyncListExpressionHelper<E> removeListener(
                SubChangeListener listener, AsyncObservableList<E> currentValue) {
            return this;
        }

        @Override
        protected ProxyAsyncListExpressionHelper<E> addListener(
                ListChangeListener<? super E> listener, AsyncObservableList<E> currentValue) {
            removePeerListener();
            return new Generic<>(observable, peer, currentValue)
                .addListener(this.listener, currentValue)
                .addListener(listener, currentValue);
        }

        @Override
        protected ProxyAsyncListExpressionHelper<E> removeListener(
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

        @Override
        protected void setPeer(@Nullable AsyncObservableListValue<E> peer) {
            removePeerListener();
            this.peer = peer;
            addPeerListener();
            fireValueChangedEvent(observable.getValue(), false);
        }

        private void addPeerListener() {
            if (peer != null) {
                peer.addListener(peerListener);
            }
        }

        private void removePeerListener() {
            if (peer != null) {
                peer.removeListener(peerListener);
            }
        }
    }

    private static class SingleSubChange<E> extends ProxyAsyncListExpressionHelper<E> {
        private final SubChangeListener peerListener =
            new SubChangeListener() {
                @Override
                public void changed(
                        ObservableValue<?> observable, Object oldValue, Object newValue, boolean subChange) {
                    listener.changed(SingleSubChange.this.observable, oldValue, newValue, subChange);
                }
            };

        private final SubChangeListener listener;
        private AsyncObservableList<E> currentValue;

        private SingleSubChange(
                AsyncObservableListValue<E> observable,
                AsyncObservableListValue<E> peer,
                AsyncObservableList<E> currentValue,
                SubChangeListener listener) {
            super(observable, peer);
            this.listener = listener;
            this.currentValue = currentValue;
            addPeerListener();
        }

        @Override
        protected ProxyAsyncListExpressionHelper<E> addListener(
                InvalidationListener listener, AsyncObservableList<E> currentValue) {
            removePeerListener();
            return new Generic<>(observable, peer, currentValue)
                .addListener(this.listener, currentValue)
                .addListener(listener, currentValue);
        }

        @Override
        protected ProxyAsyncListExpressionHelper<E> removeListener(
                InvalidationListener listener, AsyncObservableList<E> currentValue) {
            return this;
        }

        @Override
        protected ProxyAsyncListExpressionHelper<E> addListener(
                SubInvalidationListener listener, AsyncObservableList<E> currentValue) {
            removePeerListener();
            return new Generic<>(observable, peer, currentValue)
                .addListener(this.listener, currentValue)
                .addListener(listener, currentValue);
        }

        @Override
        protected ProxyAsyncListExpressionHelper<E> removeListener(
                SubInvalidationListener listener, AsyncObservableList<E> currentValue) {
            return this;
        }

        @Override
        protected ProxyAsyncListExpressionHelper<E> addListener(
                ChangeListener<? super AsyncObservableList<E>> listener, AsyncObservableList<E> currentValue) {
            removePeerListener();
            return new Generic<>(observable, peer, currentValue)
                .addListener(this.listener, currentValue)
                .addListener(listener, currentValue);
        }

        @Override
        protected ProxyAsyncListExpressionHelper<E> removeListener(
                ChangeListener<? super AsyncObservableList<E>> listener, AsyncObservableList<E> currentValue) {
            return this;
        }

        @Override
        protected ProxyAsyncListExpressionHelper<E> addListener(
                SubChangeListener listener, AsyncObservableList<E> currentValue) {
            removePeerListener();
            return new Generic<>(observable, peer, currentValue, this.listener, listener);
        }

        @Override
        protected ProxyAsyncListExpressionHelper<E> removeListener(
                SubChangeListener listener, AsyncObservableList<E> currentValue) {
            if (this.listener != null && this.listener.equals(listener)) {
                removePeerListener();
                return null;
            }

            return this;
        }

        @Override
        protected ProxyAsyncListExpressionHelper<E> addListener(
                ListChangeListener<? super E> listener, AsyncObservableList<E> currentValue) {
            removePeerListener();
            return new Generic<>(observable, peer, currentValue)
                .addListener(this.listener, currentValue)
                .addListener(listener, currentValue);
        }

        @Override
        protected ProxyAsyncListExpressionHelper<E> removeListener(
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

        @Override
        protected void setPeer(@Nullable AsyncObservableListValue<E> peer) {
            removePeerListener();
            this.peer = peer;
            addPeerListener();
            fireValueChangedEvent(observable.getValue(), false);
        }

        private void addPeerListener() {
            if (peer instanceof AsyncSubObservableValue) {
                ((AsyncSubObservableValue)peer).addListener(peerListener);
            }
        }

        private void removePeerListener() {
            if (peer instanceof AsyncSubObservableValue) {
                ((AsyncSubObservableValue)peer).removeListener(peerListener);
            }
        }
    }

    private static class SingleListChange<E> extends ProxyAsyncListExpressionHelper<E> {
        private final ListChangeListener<? super E> peerListener =
            new ListChangeListener<>() {
                @Override
                public void onChanged(Change<? extends E> change) {
                    listener.onChanged(change);
                }
            };

        private final ListChangeListener<? super E> listener;
        private AsyncObservableList<E> currentValue;

        private SingleListChange(
                AsyncObservableListValue<E> observable,
                AsyncObservableListValue<E> peer,
                AsyncObservableList<E> currentValue,
                ListChangeListener<? super E> listener) {
            super(observable, peer);
            this.listener = listener;
            this.currentValue = currentValue;
            addPeerListener();
        }

        @Override
        protected ProxyAsyncListExpressionHelper<E> addListener(
                InvalidationListener listener, AsyncObservableList<E> currentValue) {
            removePeerListener();
            return new Generic<>(observable, peer, currentValue)
                .addListener(this.listener, currentValue)
                .addListener(listener, currentValue);
        }

        @Override
        protected ProxyAsyncListExpressionHelper<E> removeListener(
                InvalidationListener listener, AsyncObservableList<E> currentValue) {
            return this;
        }

        @Override
        protected ProxyAsyncListExpressionHelper<E> addListener(
                SubInvalidationListener listener, AsyncObservableList<E> currentValue) {
            removePeerListener();
            return new Generic<>(observable, peer, currentValue)
                .addListener(this.listener, currentValue)
                .addListener(listener, currentValue);
        }

        @Override
        protected ProxyAsyncListExpressionHelper<E> removeListener(
                SubInvalidationListener listener, AsyncObservableList<E> currentValue) {
            return this;
        }

        @Override
        protected ProxyAsyncListExpressionHelper<E> addListener(
                ChangeListener<? super AsyncObservableList<E>> listener, AsyncObservableList<E> currentValue) {
            removePeerListener();
            return new Generic<>(observable, peer, currentValue)
                .addListener(this.listener, currentValue)
                .addListener(listener, currentValue);
        }

        @Override
        protected ProxyAsyncListExpressionHelper<E> removeListener(
                ChangeListener<? super AsyncObservableList<E>> listener, AsyncObservableList<E> currentValue) {
            return this;
        }

        @Override
        protected ProxyAsyncListExpressionHelper<E> addListener(
                SubChangeListener listener, AsyncObservableList<E> currentValue) {
            removePeerListener();
            return new Generic<>(observable, peer, currentValue)
                .addListener(this.listener, currentValue)
                .addListener(listener, currentValue);
        }

        @Override
        protected ProxyAsyncListExpressionHelper<E> removeListener(
                SubChangeListener listener, AsyncObservableList<E> currentValue) {
            return this;
        }

        @Override
        protected ProxyAsyncListExpressionHelper<E> addListener(
                ListChangeListener<? super E> listener, AsyncObservableList<E> currentValue) {
            removePeerListener();
            return new Generic<>(observable, peer, currentValue, this.listener, listener);
        }

        @Override
        protected ProxyAsyncListExpressionHelper<E> removeListener(
                ListChangeListener<? super E> listener, AsyncObservableList<E> currentValue) {
            if (this.listener != null && this.listener.equals(listener)) {
                removePeerListener();
                return null;
            }

            return this;
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

        @Override
        protected void setPeer(@Nullable AsyncObservableListValue<E> peer) {
            removePeerListener();
            this.peer = peer;
            addPeerListener();
            fireValueChangedEvent(observable.getValue(), false);
        }

        private void addPeerListener() {
            if (peer != null) {
                peer.addListener(peerListener);
            }
        }

        private void removePeerListener() {
            if (peer != null) {
                peer.removeListener(peerListener);
            }
        }
    }

    private static class Generic<E> extends ProxyAsyncListExpressionHelper<E> {

        private InvalidationListener peerInvalidationListener;
        private SubInvalidationListener peerSubInvalidationListener;
        private ChangeListener<? super AsyncObservableList<E>> peerChangeListener;
        private SubChangeListener peerSubChangeListener;
        private ListChangeListener<? super E> peerListChangeListener;
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
        private boolean locked;
        private AsyncObservableList<E> currentValue;

        private Generic(
                AsyncObservableListValue<E> observable,
                AsyncObservableListValue<E> peer,
                AsyncObservableList<E> currentValue) {
            super(observable, peer);
            this.currentValue = currentValue;
        }

        private Generic(
                AsyncObservableListValue<E> observable,
                AsyncObservableListValue<E> peer,
                AsyncObservableList<E> currentValue,
                InvalidationListener listener0,
                InvalidationListener listener1) {
            super(observable, peer);
            this.invalidationListeners = new InvalidationListener[] {listener0, listener1};
            this.invalidationSize = 2;
            this.currentValue = currentValue;
            ensurePeerInvalidationListener();
        }

        private Generic(
                AsyncObservableListValue<E> observable,
                AsyncObservableListValue<E> peer,
                AsyncObservableList<E> currentValue,
                SubInvalidationListener listener0,
                SubInvalidationListener listener1) {
            super(observable, peer);
            this.subInvalidationListeners = new SubInvalidationListener[] {listener0, listener1};
            this.subInvalidationSize = 2;
            this.currentValue = currentValue;
            ensurePeerSubInvalidationListener();
        }

        @SuppressWarnings("unchecked")
        private Generic(
                AsyncObservableListValue<E> observable,
                AsyncObservableListValue<E> peer,
                AsyncObservableList<E> currentValue,
                ChangeListener<? super AsyncObservableList<E>> listener0,
                ChangeListener<? super AsyncObservableList<E>> listener1) {
            super(observable, peer);
            this.changeListeners = new ChangeListener[] {listener0, listener1};
            this.changeSize = 2;
            this.currentValue = currentValue;
            ensurePeerChangeListener();
        }

        private Generic(
                AsyncObservableListValue<E> observable,
                AsyncObservableListValue<E> peer,
                AsyncObservableList<E> currentValue,
                SubChangeListener listener0,
                SubChangeListener listener1) {
            super(observable, peer);
            this.subChangeListeners = new SubChangeListener[] {listener0, listener1};
            this.subChangeSize = 2;
            this.currentValue = currentValue;
            ensurePeerSubChangeListener();
        }

        @SuppressWarnings("unchecked")
        private Generic(
                AsyncObservableListValue<E> observable,
                AsyncObservableListValue<E> peer,
                AsyncObservableList<E> currentValue,
                ListChangeListener<? super E> listener0,
                ListChangeListener<? super E> listener1) {
            super(observable, peer);
            this.listChangeListeners = new ListChangeListener[] {listener0, listener1};
            this.listChangeSize = 2;
            this.currentValue = currentValue;
            ensurePeerListChangeListener();
        }

        private Generic(
                AsyncObservableListValue<E> observable,
                AsyncObservableListValue<E> peer,
                AsyncObservableList<E> currentValue,
                Generic<E> source) {
            super(observable, peer);
            this.currentValue = currentValue;

            if (source.invalidationListeners != null) {
                invalidationListeners = source.invalidationListeners;
                invalidationSize = source.invalidationSize;
                ensurePeerInvalidationListener();
            }

            if (source.subInvalidationListeners != null) {
                subInvalidationListeners = source.subInvalidationListeners;
                subInvalidationSize = source.subInvalidationSize;
                ensurePeerSubInvalidationListener();
            }

            if (source.changeListeners != null) {
                changeListeners = source.changeListeners;
                changeSize = source.changeSize;
                ensurePeerChangeListener();
            }

            if (source.subChangeListeners != null) {
                subChangeListeners = source.subChangeListeners;
                subChangeSize = source.subChangeSize;
                ensurePeerSubChangeListener();
            }

            if (source.listChangeListeners != null) {
                listChangeListeners = source.listChangeListeners;
                listChangeSize = source.listChangeSize;
                ensurePeerListChangeListener();
            }
        }

        @Override
        protected ProxyAsyncListExpressionHelper<E> addListener(
                InvalidationListener listener, AsyncObservableList<E> currentValue) {
            Generic<E> helper = new Generic<>(observable, peer, currentValue, this);

            if (helper.invalidationListeners == null) {
                helper.invalidationListeners = new InvalidationListener[] {listener};
                helper.invalidationSize = 1;
            } else {
                helper.invalidationListeners = Arrays.copyOf(helper.invalidationListeners, helper.invalidationSize + 1);
                helper.invalidationSize = trim(helper.invalidationSize, helper.invalidationListeners);
                helper.invalidationListeners[helper.invalidationSize++] = listener;
            }

            ensurePeerInvalidationListener();
            return helper;
        }

        @Override
        protected ProxyAsyncListExpressionHelper<E> removeListener(
                InvalidationListener listener, AsyncObservableList<E> currentValue) {
            if (invalidationListeners != null) {
                for (int index = 0; index < invalidationSize; index++) {
                    if (invalidationListeners[index].equals(listener)) {
                        ProxyAsyncListExpressionHelper<E> helper = getSingleListenerHelper(index, 1, 0, 0, 0, 0);
                        if (helper != null) {
                            return helper;
                        }

                        Generic<E> generic = new Generic<>(observable, peer, currentValue, this);

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
        protected ProxyAsyncListExpressionHelper<E> addListener(
                SubInvalidationListener listener, AsyncObservableList<E> currentValue) {
            Generic<E> helper = new Generic<>(observable, peer, currentValue, this);

            if (helper.subInvalidationListeners == null) {
                helper.subInvalidationListeners = new SubInvalidationListener[] {listener};
                helper.subInvalidationSize = 1;
            } else {
                helper.subInvalidationListeners =
                    Arrays.copyOf(helper.subInvalidationListeners, helper.subInvalidationSize + 1);
                helper.subInvalidationSize = trim(helper.subInvalidationSize, helper.subInvalidationListeners);
                helper.subInvalidationListeners[helper.subInvalidationSize++] = listener;
            }

            ensurePeerSubInvalidationListener();
            return helper;
        }

        @Override
        protected ProxyAsyncListExpressionHelper<E> removeListener(
                SubInvalidationListener listener, AsyncObservableList<E> currentValue) {
            if (subInvalidationListeners != null) {
                for (int index = 0; index < subInvalidationSize; index++) {
                    if (subInvalidationListeners[index].equals(listener)) {
                        ProxyAsyncListExpressionHelper<E> helper = getSingleListenerHelper(index, 0, 1, 0, 0, 0);
                        if (helper != null) {
                            return helper;
                        }

                        Generic<E> generic = new Generic<>(observable, peer, currentValue, this);

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
        protected ProxyAsyncListExpressionHelper<E> addListener(
                ChangeListener<? super AsyncObservableList<E>> listener, AsyncObservableList<E> currentValue) {
            Generic<E> helper = new Generic<>(observable, peer, currentValue, this);

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

            ensurePeerChangeListener();
            return helper;
        }

        @Override
        @SuppressWarnings("unchecked")
        protected ProxyAsyncListExpressionHelper<E> removeListener(
                ChangeListener<? super AsyncObservableList<E>> listener, AsyncObservableList<E> currentValue) {
            if (changeListeners != null) {
                for (int index = 0; index < changeSize; index++) {
                    if (changeListeners[index].equals(listener)) {
                        ProxyAsyncListExpressionHelper<E> helper = getSingleListenerHelper(index, 0, 0, 1, 0, 0);
                        if (helper != null) {
                            return helper;
                        }

                        Generic<E> generic = new Generic<>(observable, peer, currentValue, this);

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
        protected ProxyAsyncListExpressionHelper<E> addListener(
                SubChangeListener listener, AsyncObservableList<E> currentValue) {
            Generic<E> helper = new Generic<>(observable, peer, currentValue, this);

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

            ensurePeerSubChangeListener();
            return helper;
        }

        @Override
        protected ProxyAsyncListExpressionHelper<E> removeListener(
                SubChangeListener listener, AsyncObservableList<E> currentValue) {
            if (subChangeListeners != null) {
                for (int index = 0; index < subChangeSize; index++) {
                    if (subChangeListeners[index].equals(listener)) {
                        ProxyAsyncListExpressionHelper<E> helper = getSingleListenerHelper(index, 0, 0, 0, 1, 0);
                        if (helper != null) {
                            return helper;
                        }

                        Generic<E> generic = new Generic<>(observable, peer, currentValue, this);

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
        protected ProxyAsyncListExpressionHelper<E> addListener(
                ListChangeListener<? super E> listener, AsyncObservableList<E> currentValue) {
            Generic<E> helper = new Generic<>(observable, peer, currentValue, this);

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

            ensurePeerListChangeListener();
            return helper;
        }

        @Override
        @SuppressWarnings("unchecked")
        protected ProxyAsyncListExpressionHelper<E> removeListener(
                ListChangeListener<? super E> listener, AsyncObservableList<E> currentValue) {
            if (listChangeListeners != null) {
                for (int index = 0; index < listChangeSize; index++) {
                    if (listChangeListeners[index].equals(listener)) {
                        ProxyAsyncListExpressionHelper<E> helper = getSingleListenerHelper(index, 0, 0, 0, 0, 1);
                        if (helper != null) {
                            return helper;
                        }

                        Generic<E> generic = new Generic<>(observable, peer, currentValue, this);

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
                notifyListeners(currentValue, null, false);
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

                    notifyListeners(oldValue, change, false);
                } else {
                    notifyListeners(currentValue, null, true);
                }
            }
        }

        @Override
        protected void fireValueChangedEvent(final AsyncChange<? extends E> change) {
            final Change<E> mappedChange =
                (listChangeSize == 0) ? null : new AsyncSourceAdapterChange<>(observable, change);
            notifyListeners(currentValue, mappedChange, false);
        }

        private void notifyListeners(AsyncObservableList<E> oldValue, Change<E> change, boolean noChange) {
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
        }

        private ProxyAsyncListExpressionHelper<E> getSingleListenerHelper(
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
                removePeerListeners();
                return new SingleInvalidation<>(
                    observable, peer, invalidationListeners[invalidationSize == 2 ? 1 - index : 0]);
            }

            if (invalidationSize - removeInvalidation == 0
                    && subInvalidationSize - removeSubInvalidation == 1
                    && changeSize - removeChange == 0
                    && subChangeSize - removeSubChange == 0
                    && listChangeSize - removeListChange == 0) {
                removePeerListeners();
                return new SingleSubInvalidation<>(
                    observable, peer, subInvalidationListeners[subInvalidationSize == 2 ? 1 - index : 0]);
            }

            if (invalidationSize - removeInvalidation == 0
                    && subInvalidationSize - removeSubInvalidation == 0
                    && changeSize - removeChange == 1
                    && subChangeSize - removeSubChange == 0
                    && listChangeSize - removeListChange == 0) {
                removePeerListeners();
                return new SingleChange<>(
                    observable, peer, currentValue, changeListeners[changeSize == 2 ? 1 - index : 0]);
            }

            if (invalidationSize - removeInvalidation == 0
                    && subInvalidationSize - removeSubInvalidation == 0
                    && changeSize - removeChange == 0
                    && subChangeSize - removeSubChange == 1
                    && listChangeSize - removeListChange == 0) {
                removePeerListeners();
                return new SingleSubChange<>(
                    observable, peer, currentValue, subChangeListeners[subChangeSize == 2 ? 1 - index : 0]);
            }

            if (invalidationSize - removeInvalidation == 0
                    && subInvalidationSize - removeSubInvalidation == 0
                    && changeSize - removeChange == 0
                    && subChangeSize - removeSubChange == 0
                    && listChangeSize - removeListChange == 1) {
                removePeerListeners();
                return new SingleListChange<>(
                    observable, peer, currentValue, listChangeListeners[listChangeSize == 2 ? 1 - index : 0]);
            }

            return null;
        }

        @Override
        protected void setPeer(@Nullable AsyncObservableListValue<E> peer) {
            removePeerListeners();

            this.peer = peer;

            if (invalidationSize > 0) {
                ensurePeerInvalidationListener();
            }

            if (subInvalidationSize > 0) {
                ensurePeerSubInvalidationListener();
            }

            if (changeSize > 0) {
                ensurePeerChangeListener();
            }

            if (subChangeSize > 0) {
                ensurePeerSubChangeListener();
            }

            if (listChangeSize > 0) {
                ensurePeerListChangeListener();
            }

            fireValueChangedEvent(observable.getValue(), false);
        }

        private void ensurePeerInvalidationListener() {
            boolean created = false;

            if (peerInvalidationListener == null) {
                peerInvalidationListener =
                    observable -> {
                        for (int i = 0; i < invalidationSize; ++i) {
                            invalidationListeners[i].invalidated(Generic.this.observable);
                        }
                    };

                created = true;
            }

            if (peer != null && created) {
                peer.addListener(peerInvalidationListener);
            }
        }

        private void ensurePeerSubInvalidationListener() {
            boolean created = false;

            if (peerSubInvalidationListener == null) {
                peerSubInvalidationListener =
                    (observable, subChange) -> {
                        for (int i = 0; i < subInvalidationSize; ++i) {
                            subInvalidationListeners[i].invalidated(Generic.this.observable, subChange);
                        }
                    };

                created = true;
            }

            if (peer instanceof AsyncSubObservable && created) {
                ((AsyncSubObservable)peer).addListener(peerSubInvalidationListener);
            }
        }

        private void ensurePeerChangeListener() {
            boolean created = false;

            if (peerChangeListener == null) {
                peerChangeListener =
                    (observable, oldValue, newValue) -> {
                        for (int i = 0; i < changeSize; ++i) {
                            changeListeners[i].changed(Generic.this.observable, oldValue, newValue);
                        }
                    };

                created = true;
            }

            if (peer != null && created) {
                peer.addListener(peerChangeListener);
            }
        }

        private void ensurePeerSubChangeListener() {
            boolean created = false;

            if (peerSubChangeListener == null) {
                peerSubChangeListener =
                    (observable, oldValue, newValue, subChange) -> {
                        for (int i = 0; i < subChangeSize; ++i) {
                            subChangeListeners[i].changed(Generic.this.observable, oldValue, newValue, subChange);
                        }
                    };

                created = true;
            }

            if (peer instanceof AsyncSubObservableValue && created) {
                ((AsyncSubObservableValue)peer).addListener(peerSubChangeListener);
            }
        }

        private void ensurePeerListChangeListener() {
            boolean created = false;

            if (peerListChangeListener == null) {
                peerListChangeListener =
                    change -> {
                        for (int i = 0; i < listChangeSize; ++i) {
                            listChangeListeners[i].onChanged(change);
                        }
                    };

                created = true;
            }

            if (peer != null && created) {
                peer.addListener(peerListChangeListener);
            }
        }

        private void removePeerListeners() {
            if (peer != null) {
                if (peerInvalidationListener != null) {
                    peer.removeListener(peerInvalidationListener);
                }

                if (peerChangeListener != null) {
                    peer.removeListener(peerChangeListener);
                }

                if (peerSubInvalidationListener != null && peer instanceof AsyncSubObservable) {
                    ((AsyncSubObservable)peer).removeListener(peerSubInvalidationListener);
                }

                if (peerSubChangeListener != null && peer instanceof AsyncSubObservableValue) {
                    ((AsyncSubObservableValue)peer).removeListener(peerSubChangeListener);
                }

                if (peerListChangeListener != null) {
                    peer.removeListener(peerListChangeListener);
                }
            }

            peerInvalidationListener = null;
            peerChangeListener = null;
            peerSubInvalidationListener = null;
            peerSubChangeListener = null;
            peerListChangeListener = null;
        }
    }
}
