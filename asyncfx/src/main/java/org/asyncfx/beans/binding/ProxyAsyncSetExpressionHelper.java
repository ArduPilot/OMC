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
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.SetChangeListener;
import org.asyncfx.PublishSource;
import org.asyncfx.beans.AsyncSubObservable;
import org.asyncfx.beans.SubInvalidationListener;
import org.asyncfx.beans.value.AsyncObservableSetValue;
import org.asyncfx.beans.value.AsyncSubObservableValue;
import org.asyncfx.beans.value.SubChangeListener;
import org.asyncfx.collections.AsyncObservableSet;
import org.jetbrains.annotations.Nullable;

@PublishSource(
    module = "openjfx",
    licenses = {"openjfx", "intel-gpl-classpath-exception"}
)
public abstract class ProxyAsyncSetExpressionHelper<E> extends ExpressionHelperBase {

    public static <E> ProxyAsyncSetExpressionHelper<E> addListener(
            ProxyAsyncSetExpressionHelper<E> helper,
            AsyncObservableSetValue<E> observable,
            AsyncObservableSetValue<E> peer,
            AsyncObservableSet<E> currentValue,
            InvalidationListener listener) {
        if ((observable == null) || (listener == null)) {
            throw new NullPointerException();
        }

        return (helper == null)
            ? new SingleInvalidation<>(observable, peer, listener)
            : helper.addListener(listener, currentValue);
    }

    public static <E> ProxyAsyncSetExpressionHelper<E> removeListener(
            ProxyAsyncSetExpressionHelper<E> helper,
            AsyncObservableSet<E> currentValue,
            InvalidationListener listener) {
        if (listener == null) {
            throw new NullPointerException();
        }

        return (helper == null) ? null : helper.removeListener(listener, currentValue);
    }

    public static <E> ProxyAsyncSetExpressionHelper<E> addListener(
            ProxyAsyncSetExpressionHelper<E> helper,
            AsyncObservableSetValue<E> observable,
            AsyncObservableSetValue<E> peer,
            AsyncObservableSet<E> currentValue,
            SubInvalidationListener listener) {
        if ((observable == null) || (listener == null)) {
            throw new NullPointerException();
        }

        return (helper == null)
            ? new SingleSubInvalidation<>(observable, peer, listener)
            : helper.addListener(listener, currentValue);
    }

    public static <E> ProxyAsyncSetExpressionHelper<E> removeListener(
            ProxyAsyncSetExpressionHelper<E> helper,
            AsyncObservableSet<E> currentValue,
            SubInvalidationListener listener) {
        if (listener == null) {
            throw new NullPointerException();
        }

        return (helper == null) ? null : helper.removeListener(listener, currentValue);
    }

    public static <E> ProxyAsyncSetExpressionHelper<E> addListener(
            ProxyAsyncSetExpressionHelper<E> helper,
            AsyncObservableSetValue<E> observable,
            AsyncObservableSetValue<E> peer,
            AsyncObservableSet<E> currentValue,
            ChangeListener<? super AsyncObservableSet<E>> listener) {
        if ((observable == null) || (listener == null)) {
            throw new NullPointerException();
        }

        return (helper == null)
            ? new SingleChange<>(observable, peer, currentValue, listener)
            : helper.addListener(listener, currentValue);
    }

    public static <E> ProxyAsyncSetExpressionHelper<E> removeListener(
            ProxyAsyncSetExpressionHelper<E> helper,
            AsyncObservableSet<E> currentValue,
            ChangeListener<? super AsyncObservableSet<E>> listener) {
        if (listener == null) {
            throw new NullPointerException();
        }

        return (helper == null) ? null : helper.removeListener(listener, currentValue);
    }

    public static <E> ProxyAsyncSetExpressionHelper<E> addListener(
            ProxyAsyncSetExpressionHelper<E> helper,
            AsyncObservableSetValue<E> observable,
            AsyncObservableSetValue<E> peer,
            AsyncObservableSet<E> currentValue,
            SubChangeListener listener) {
        if ((observable == null) || (listener == null)) {
            throw new NullPointerException();
        }

        return (helper == null)
            ? new SingleSubChange<>(observable, peer, currentValue, listener)
            : helper.addListener(listener, currentValue);
    }

    public static <E> ProxyAsyncSetExpressionHelper<E> removeListener(
            ProxyAsyncSetExpressionHelper<E> helper, AsyncObservableSet<E> currentValue, SubChangeListener listener) {
        if (listener == null) {
            throw new NullPointerException();
        }

        return (helper == null) ? null : helper.removeListener(listener, currentValue);
    }

    public static <E> ProxyAsyncSetExpressionHelper<E> addListener(
            ProxyAsyncSetExpressionHelper<E> helper,
            AsyncObservableSetValue<E> observable,
            AsyncObservableSetValue<E> peer,
            AsyncObservableSet<E> currentValue,
            SetChangeListener<? super E> listener) {
        if ((observable == null) || (listener == null)) {
            throw new NullPointerException();
        }

        return (helper == null)
            ? new SingleSetChange<>(observable, peer, currentValue, listener)
            : helper.addListener(listener, currentValue);
    }

    public static <E> ProxyAsyncSetExpressionHelper<E> removeListener(
            ProxyAsyncSetExpressionHelper<E> helper,
            AsyncObservableSet<E> currentValue,
            SetChangeListener<? super E> listener) {
        if (listener == null) {
            throw new NullPointerException();
        }

        return (helper == null) ? null : helper.removeListener(listener, currentValue);
    }

    public static <T> boolean validatesValue(ProxyAsyncSetExpressionHelper<T> helper) {
        if (helper != null) {
            return helper.validatesValue();
        }

        return false;
    }

    public static <T> boolean containsBidirectionalBindingEndpoints(ProxyAsyncSetExpressionHelper<T> helper) {
        if (helper != null) {
            return helper.containsBidirectionalBindingEndpoints();
        }

        return false;
    }

    public static <E> void fireValueChangedEvent(
            ProxyAsyncSetExpressionHelper<E> helper, AsyncObservableSet<E> newValue, boolean subChange) {
        if (helper != null) {
            helper.fireValueChangedEvent(newValue, subChange);
        }
    }

    public static <E> void fireValueChangedEvent(ProxyAsyncSetExpressionHelper<E> helper, Change<? extends E> change) {
        if (helper != null) {
            helper.fireValueChangedEvent(change);
        }
    }

    public static <T> void setPeer(ProxyAsyncSetExpressionHelper<T> helper, @Nullable AsyncObservableSetValue<T> peer) {
        if (helper != null) {
            helper.setPeer(peer);
        }
    }

    protected final AsyncObservableSetValue<E> observable;
    protected AsyncObservableSetValue<E> peer;

    ProxyAsyncSetExpressionHelper(AsyncObservableSetValue<E> observable, AsyncObservableSetValue<E> peer) {
        this.observable = observable;
        this.peer = peer;
    }

    protected abstract ProxyAsyncSetExpressionHelper<E> addListener(
            InvalidationListener listener, AsyncObservableSet<E> currentValue);

    protected abstract ProxyAsyncSetExpressionHelper<E> removeListener(
            InvalidationListener listener, AsyncObservableSet<E> currentValue);

    protected abstract ProxyAsyncSetExpressionHelper<E> addListener(
            SubInvalidationListener listener, AsyncObservableSet<E> currentValue);

    protected abstract ProxyAsyncSetExpressionHelper<E> removeListener(
            SubInvalidationListener listener, AsyncObservableSet<E> currentValue);

    protected abstract ProxyAsyncSetExpressionHelper<E> addListener(
            ChangeListener<? super AsyncObservableSet<E>> listener, AsyncObservableSet<E> currentValue);

    protected abstract ProxyAsyncSetExpressionHelper<E> removeListener(
            ChangeListener<? super AsyncObservableSet<E>> listener, AsyncObservableSet<E> currentValue);

    protected abstract ProxyAsyncSetExpressionHelper<E> addListener(
            SubChangeListener listener, AsyncObservableSet<E> currentValue);

    protected abstract ProxyAsyncSetExpressionHelper<E> removeListener(
            SubChangeListener listener, AsyncObservableSet<E> currentValue);

    protected abstract ProxyAsyncSetExpressionHelper<E> addListener(
            SetChangeListener<? super E> listener, AsyncObservableSet<E> currentValue);

    protected abstract ProxyAsyncSetExpressionHelper<E> removeListener(
            SetChangeListener<? super E> listener, AsyncObservableSet<E> currentValue);

    protected abstract boolean validatesValue();

    protected abstract boolean containsBidirectionalBindingEndpoints();

    protected abstract void fireValueChangedEvent(AsyncObservableSet<E> newValue, boolean subChange);

    protected abstract void fireValueChangedEvent(Change<? extends E> change);

    protected abstract void setPeer(@Nullable AsyncObservableSetValue<E> peer);

    private static class SingleInvalidation<E> extends ProxyAsyncSetExpressionHelper<E> {
        private final InvalidationListener peerListener =
            new InvalidationListener() {
                @Override
                public void invalidated(Observable observable) {
                    listener.invalidated(SingleInvalidation.this.observable);
                }
            };

        private final InvalidationListener listener;

        private SingleInvalidation(
                AsyncObservableSetValue<E> observable, AsyncObservableSetValue<E> peer, InvalidationListener listener) {
            super(observable, peer);
            this.listener = listener;
            addPeerListener();
        }

        @Override
        protected ProxyAsyncSetExpressionHelper<E> addListener(
                InvalidationListener listener, AsyncObservableSet<E> currentValue) {
            removePeerListener();
            return new Generic<>(observable, peer, currentValue, this.listener, listener);
        }

        @Override
        protected ProxyAsyncSetExpressionHelper<E> removeListener(
                InvalidationListener listener, AsyncObservableSet<E> currentValue) {
            if (this.listener != null && this.listener.equals(listener)) {
                removePeerListener();
                return null;
            }

            return this;
        }

        @Override
        protected ProxyAsyncSetExpressionHelper<E> addListener(
                SubInvalidationListener listener, AsyncObservableSet<E> currentValue) {
            removePeerListener();
            return new Generic<>(observable, peer, currentValue)
                .addListener(this.listener, currentValue)
                .addListener(listener, currentValue);
        }

        @Override
        protected ProxyAsyncSetExpressionHelper<E> removeListener(
                SubInvalidationListener listener, AsyncObservableSet<E> currentValue) {
            return this;
        }

        @Override
        protected ProxyAsyncSetExpressionHelper<E> addListener(
                ChangeListener<? super AsyncObservableSet<E>> listener, AsyncObservableSet<E> currentValue) {
            removePeerListener();
            return new Generic<>(observable, peer, currentValue)
                .addListener(this.listener, currentValue)
                .addListener(listener, currentValue);
        }

        @Override
        protected ProxyAsyncSetExpressionHelper<E> removeListener(
                ChangeListener<? super AsyncObservableSet<E>> listener, AsyncObservableSet<E> currentValue) {
            return this;
        }

        @Override
        protected ProxyAsyncSetExpressionHelper<E> addListener(
                SubChangeListener listener, AsyncObservableSet<E> currentValue) {
            removePeerListener();
            return new Generic<>(observable, peer, currentValue)
                .addListener(this.listener, currentValue)
                .addListener(listener, currentValue);
        }

        @Override
        protected ProxyAsyncSetExpressionHelper<E> removeListener(
                SubChangeListener listener, AsyncObservableSet<E> currentValue) {
            return this;
        }

        @Override
        protected ProxyAsyncSetExpressionHelper<E> addListener(
                SetChangeListener<? super E> listener, AsyncObservableSet<E> currentValue) {
            removePeerListener();
            return new Generic<>(observable, peer, currentValue)
                .addListener(this.listener, currentValue)
                .addListener(listener, currentValue);
        }

        @Override
        protected ProxyAsyncSetExpressionHelper<E> removeListener(
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

        @Override
        protected void setPeer(@Nullable AsyncObservableSetValue<E> peer) {
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

    private static class SingleSubInvalidation<E> extends ProxyAsyncSetExpressionHelper<E> {
        private final SubInvalidationListener peerListener =
            new SubInvalidationListener() {
                @Override
                public void invalidated(Observable observable, boolean subInvalidation) {
                    listener.invalidated(SingleSubInvalidation.this.observable, subInvalidation);
                }
            };

        private final SubInvalidationListener listener;

        private SingleSubInvalidation(
                AsyncObservableSetValue<E> observable,
                AsyncObservableSetValue<E> peer,
                SubInvalidationListener listener) {
            super(observable, peer);
            this.listener = listener;
            addPeerListener();
        }

        @Override
        protected ProxyAsyncSetExpressionHelper<E> addListener(
                InvalidationListener listener, AsyncObservableSet<E> currentValue) {
            removePeerListener();
            return new Generic<>(observable, peer, currentValue)
                .addListener(this.listener, currentValue)
                .addListener(listener, currentValue);
        }

        @Override
        protected ProxyAsyncSetExpressionHelper<E> removeListener(
                InvalidationListener listener, AsyncObservableSet<E> currentValue) {
            return this;
        }

        @Override
        protected ProxyAsyncSetExpressionHelper<E> addListener(
                SubInvalidationListener listener, AsyncObservableSet<E> currentValue) {
            removePeerListener();
            return new Generic<>(observable, peer, currentValue, this.listener, listener);
        }

        @Override
        protected ProxyAsyncSetExpressionHelper<E> removeListener(
                SubInvalidationListener listener, AsyncObservableSet<E> currentValue) {
            if (this.listener != null && this.listener.equals(listener)) {
                removePeerListener();
                return null;
            }

            return this;
        }

        @Override
        protected ProxyAsyncSetExpressionHelper<E> addListener(
                ChangeListener<? super AsyncObservableSet<E>> listener, AsyncObservableSet<E> currentValue) {
            removePeerListener();
            return new Generic<>(observable, peer, currentValue)
                .addListener(this.listener, currentValue)
                .addListener(listener, currentValue);
        }

        @Override
        protected ProxyAsyncSetExpressionHelper<E> removeListener(
                ChangeListener<? super AsyncObservableSet<E>> listener, AsyncObservableSet<E> currentValue) {
            return this;
        }

        @Override
        protected ProxyAsyncSetExpressionHelper<E> addListener(
                SubChangeListener listener, AsyncObservableSet<E> currentValue) {
            removePeerListener();
            return new Generic<>(observable, peer, currentValue)
                .addListener(this.listener, currentValue)
                .addListener(listener, currentValue);
        }

        @Override
        protected ProxyAsyncSetExpressionHelper<E> removeListener(
                SubChangeListener listener, AsyncObservableSet<E> currentValue) {
            return this;
        }

        @Override
        protected ProxyAsyncSetExpressionHelper<E> addListener(
                SetChangeListener<? super E> listener, AsyncObservableSet<E> currentValue) {
            removePeerListener();
            return new Generic<>(observable, peer, currentValue)
                .addListener(this.listener, currentValue)
                .addListener(listener, currentValue);
        }

        @Override
        protected ProxyAsyncSetExpressionHelper<E> removeListener(
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

        @Override
        protected void setPeer(@Nullable AsyncObservableSetValue<E> peer) {
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

    private static class SingleChange<E> extends ProxyAsyncSetExpressionHelper<E> {
        private final ChangeListener<? super AsyncObservableSet<E>> peerListener =
            new ChangeListener<>() {
                @Override
                public void changed(
                        ObservableValue<? extends AsyncObservableSet<E>> observable,
                        AsyncObservableSet<E> oldValue,
                        AsyncObservableSet<E> newValue) {
                    listener.changed(SingleChange.this.observable, oldValue, newValue);
                }
            };

        private final ChangeListener<? super AsyncObservableSet<E>> listener;
        private AsyncObservableSet<E> currentValue;

        private SingleChange(
                AsyncObservableSetValue<E> observable,
                AsyncObservableSetValue<E> peer,
                AsyncObservableSet<E> currentValue,
                ChangeListener<? super AsyncObservableSet<E>> listener) {
            super(observable, peer);
            this.listener = listener;
            this.currentValue = currentValue;
            addPeerListener();
        }

        @Override
        protected ProxyAsyncSetExpressionHelper<E> addListener(
                InvalidationListener listener, AsyncObservableSet<E> currentValue) {
            removePeerListener();
            return new Generic<>(observable, peer, currentValue)
                .addListener(this.listener, currentValue)
                .addListener(listener, currentValue);
        }

        @Override
        protected ProxyAsyncSetExpressionHelper<E> removeListener(
                InvalidationListener listener, AsyncObservableSet<E> currentValue) {
            return this;
        }

        @Override
        protected ProxyAsyncSetExpressionHelper<E> addListener(
                SubInvalidationListener listener, AsyncObservableSet<E> currentValue) {
            removePeerListener();
            return new Generic<>(observable, peer, currentValue)
                .addListener(this.listener, currentValue)
                .addListener(listener, currentValue);
        }

        @Override
        protected ProxyAsyncSetExpressionHelper<E> removeListener(
                SubInvalidationListener listener, AsyncObservableSet<E> currentValue) {
            return this;
        }

        @Override
        protected ProxyAsyncSetExpressionHelper<E> addListener(
                ChangeListener<? super AsyncObservableSet<E>> listener, AsyncObservableSet<E> currentValue) {
            removePeerListener();
            return new Generic<>(observable, peer, currentValue, this.listener, listener);
        }

        @Override
        protected ProxyAsyncSetExpressionHelper<E> removeListener(
                ChangeListener<? super AsyncObservableSet<E>> listener, AsyncObservableSet<E> currentValue) {
            if (this.listener != null && this.listener.equals(listener)) {
                removePeerListener();
                return null;
            }

            return this;
        }

        @Override
        protected ProxyAsyncSetExpressionHelper<E> addListener(
                SubChangeListener listener, AsyncObservableSet<E> currentValue) {
            removePeerListener();
            return new Generic<>(observable, peer, currentValue)
                .addListener(this.listener, currentValue)
                .addListener(listener, currentValue);
        }

        @Override
        protected ProxyAsyncSetExpressionHelper<E> removeListener(
                SubChangeListener listener, AsyncObservableSet<E> currentValue) {
            return this;
        }

        @Override
        protected ProxyAsyncSetExpressionHelper<E> addListener(
                SetChangeListener<? super E> listener, AsyncObservableSet<E> currentValue) {
            removePeerListener();
            return new Generic<>(observable, peer, currentValue)
                .addListener(this.listener, currentValue)
                .addListener(listener, currentValue);
        }

        @Override
        protected ProxyAsyncSetExpressionHelper<E> removeListener(
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

        @Override
        protected void setPeer(@Nullable AsyncObservableSetValue<E> peer) {
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

    private static class SingleSubChange<E> extends ProxyAsyncSetExpressionHelper<E> {
        private final SubChangeListener peerListener =
            new SubChangeListener() {
                @Override
                public void changed(
                        ObservableValue<?> observable, Object oldValue, Object newValue, boolean subChange) {
                    listener.changed(SingleSubChange.this.observable, oldValue, newValue, subChange);
                }
            };

        private final SubChangeListener listener;
        private AsyncObservableSet<E> currentValue;

        private SingleSubChange(
                AsyncObservableSetValue<E> observable,
                AsyncObservableSetValue<E> peer,
                AsyncObservableSet<E> currentValue,
                SubChangeListener listener) {
            super(observable, peer);
            this.listener = listener;
            this.currentValue = currentValue;
            addPeerListener();
        }

        @Override
        protected ProxyAsyncSetExpressionHelper<E> addListener(
                InvalidationListener listener, AsyncObservableSet<E> currentValue) {
            removePeerListener();
            return new Generic<>(observable, peer, currentValue)
                .addListener(this.listener, currentValue)
                .addListener(listener, currentValue);
        }

        @Override
        protected ProxyAsyncSetExpressionHelper<E> removeListener(
                InvalidationListener listener, AsyncObservableSet<E> currentValue) {
            return this;
        }

        @Override
        protected ProxyAsyncSetExpressionHelper<E> addListener(
                SubInvalidationListener listener, AsyncObservableSet<E> currentValue) {
            removePeerListener();
            return new Generic<>(observable, peer, currentValue)
                .addListener(this.listener, currentValue)
                .addListener(listener, currentValue);
        }

        @Override
        protected ProxyAsyncSetExpressionHelper<E> removeListener(
                SubInvalidationListener listener, AsyncObservableSet<E> currentValue) {
            return this;
        }

        @Override
        protected ProxyAsyncSetExpressionHelper<E> addListener(
                ChangeListener<? super AsyncObservableSet<E>> listener, AsyncObservableSet<E> currentValue) {
            removePeerListener();
            return new Generic<>(observable, peer, currentValue)
                .addListener(this.listener, currentValue)
                .addListener(listener, currentValue);
        }

        @Override
        protected ProxyAsyncSetExpressionHelper<E> removeListener(
                ChangeListener<? super AsyncObservableSet<E>> listener, AsyncObservableSet<E> currentValue) {
            return this;
        }

        @Override
        protected ProxyAsyncSetExpressionHelper<E> addListener(
                SubChangeListener listener, AsyncObservableSet<E> currentValue) {
            removePeerListener();
            return new Generic<>(observable, peer, currentValue, this.listener, listener);
        }

        @Override
        protected ProxyAsyncSetExpressionHelper<E> removeListener(
                SubChangeListener listener, AsyncObservableSet<E> currentValue) {
            if (this.listener != null && this.listener.equals(listener)) {
                removePeerListener();
                return null;
            }

            return this;
        }

        @Override
        protected ProxyAsyncSetExpressionHelper<E> addListener(
                SetChangeListener<? super E> listener, AsyncObservableSet<E> currentValue) {
            removePeerListener();
            return new Generic<>(observable, peer, currentValue)
                .addListener(this.listener, currentValue)
                .addListener(listener, currentValue);
        }

        @Override
        protected ProxyAsyncSetExpressionHelper<E> removeListener(
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

        @Override
        protected void setPeer(@Nullable AsyncObservableSetValue<E> peer) {
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

    private static class SingleSetChange<E> extends ProxyAsyncSetExpressionHelper<E> {
        private final SetChangeListener<? super E> peerListener =
            new SetChangeListener<>() {
                @Override
                public void onChanged(Change<? extends E> change) {
                    listener.onChanged(change);
                }
            };

        private final SetChangeListener<? super E> listener;
        private AsyncObservableSet<E> currentValue;

        private SingleSetChange(
                AsyncObservableSetValue<E> observable,
                AsyncObservableSetValue<E> peer,
                AsyncObservableSet<E> currentValue,
                SetChangeListener<? super E> listener) {
            super(observable, peer);
            this.listener = listener;
            this.currentValue = currentValue;
            addPeerListener();
        }

        @Override
        protected ProxyAsyncSetExpressionHelper<E> addListener(
                InvalidationListener listener, AsyncObservableSet<E> currentValue) {
            removePeerListener();
            return new Generic<>(observable, peer, currentValue)
                .addListener(this.listener, currentValue)
                .addListener(listener, currentValue);
        }

        @Override
        protected ProxyAsyncSetExpressionHelper<E> removeListener(
                InvalidationListener listener, AsyncObservableSet<E> currentValue) {
            return this;
        }

        @Override
        protected ProxyAsyncSetExpressionHelper<E> addListener(
                SubInvalidationListener listener, AsyncObservableSet<E> currentValue) {
            removePeerListener();
            return new Generic<>(observable, peer, currentValue)
                .addListener(this.listener, currentValue)
                .addListener(listener, currentValue);
        }

        @Override
        protected ProxyAsyncSetExpressionHelper<E> removeListener(
                SubInvalidationListener listener, AsyncObservableSet<E> currentValue) {
            return this;
        }

        @Override
        protected ProxyAsyncSetExpressionHelper<E> addListener(
                ChangeListener<? super AsyncObservableSet<E>> listener, AsyncObservableSet<E> currentValue) {
            removePeerListener();
            return new Generic<>(observable, peer, currentValue)
                .addListener(this.listener, currentValue)
                .addListener(listener, currentValue);
        }

        @Override
        protected ProxyAsyncSetExpressionHelper<E> removeListener(
                ChangeListener<? super AsyncObservableSet<E>> listener, AsyncObservableSet<E> currentValue) {
            return this;
        }

        @Override
        protected ProxyAsyncSetExpressionHelper<E> addListener(
                SubChangeListener listener, AsyncObservableSet<E> currentValue) {
            removePeerListener();
            return new Generic<>(observable, peer, currentValue)
                .addListener(this.listener, currentValue)
                .addListener(listener, currentValue);
        }

        @Override
        protected ProxyAsyncSetExpressionHelper<E> removeListener(
                SubChangeListener listener, AsyncObservableSet<E> currentValue) {
            return this;
        }

        @Override
        protected ProxyAsyncSetExpressionHelper<E> addListener(
                SetChangeListener<? super E> listener, AsyncObservableSet<E> currentValue) {
            removePeerListener();
            return new Generic<>(observable, peer, currentValue, this.listener, listener);
        }

        @Override
        protected ProxyAsyncSetExpressionHelper<E> removeListener(
                SetChangeListener<? super E> listener, AsyncObservableSet<E> currentValue) {
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
        protected void fireValueChangedEvent(AsyncObservableSet<E> newValue, boolean subChange) {
            if (subChange) {
                return;
            }

            final AsyncObservableSet<E> oldValue = currentValue;
            currentValue = newValue;
            if (currentValue != oldValue) {
                final SimpleChange<E> change = new SimpleChange<>(observable);
                if (currentValue == null) {
                    for (final E element : oldValue) {
                        listener.onChanged(change.setRemoved(element));
                    }
                } else if (oldValue == null) {
                    for (final E element : currentValue) {
                        listener.onChanged(change.setAdded(element));
                    }
                } else {
                    for (final E element : oldValue) {
                        if (!currentValue.contains(element)) {
                            listener.onChanged(change.setRemoved(element));
                        }
                    }

                    for (final E element : currentValue) {
                        if (!oldValue.contains(element)) {
                            listener.onChanged(change.setAdded(element));
                        }
                    }
                }
            }
        }

        @Override
        protected void fireValueChangedEvent(final Change<? extends E> change) {
            listener.onChanged(new SimpleChange<>(observable, change));
        }

        @Override
        protected void setPeer(@Nullable AsyncObservableSetValue<E> peer) {
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

    private static class Generic<E> extends ProxyAsyncSetExpressionHelper<E> {

        private InvalidationListener peerInvalidationListener;
        private SubInvalidationListener peerSubInvalidationListener;
        private ChangeListener<? super AsyncObservableSet<E>> peerChangeListener;
        private SubChangeListener peerSubChangeListener;
        private SetChangeListener<? super E> peerSetChangeListener;
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

        private Generic(
                AsyncObservableSetValue<E> observable,
                AsyncObservableSetValue<E> peer,
                AsyncObservableSet<E> currentValue) {
            super(observable, peer);
            this.currentValue = currentValue;
        }

        private Generic(
                AsyncObservableSetValue<E> observable,
                AsyncObservableSetValue<E> peer,
                AsyncObservableSet<E> currentValue,
                InvalidationListener listener0,
                InvalidationListener listener1) {
            super(observable, peer);
            this.invalidationListeners = new InvalidationListener[] {listener0, listener1};
            this.invalidationSize = 2;
            this.currentValue = currentValue;
            ensurePeerInvalidationListener();
        }

        private Generic(
                AsyncObservableSetValue<E> observable,
                AsyncObservableSetValue<E> peer,
                AsyncObservableSet<E> currentValue,
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
                AsyncObservableSetValue<E> observable,
                AsyncObservableSetValue<E> peer,
                AsyncObservableSet<E> currentValue,
                ChangeListener<? super AsyncObservableSet<E>> listener0,
                ChangeListener<? super AsyncObservableSet<E>> listener1) {
            super(observable, peer);
            this.changeListeners = new ChangeListener[] {listener0, listener1};
            this.changeSize = 2;
            this.currentValue = currentValue;
            ensurePeerChangeListener();
        }

        private Generic(
                AsyncObservableSetValue<E> observable,
                AsyncObservableSetValue<E> peer,
                AsyncObservableSet<E> currentValue,
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
                AsyncObservableSetValue<E> observable,
                AsyncObservableSetValue<E> peer,
                AsyncObservableSet<E> currentValue,
                SetChangeListener<? super E> listener0,
                SetChangeListener<? super E> listener1) {
            super(observable, peer);
            this.setChangeListeners = new SetChangeListener[] {listener0, listener1};
            this.setChangeSize = 2;
            this.currentValue = currentValue;
            ensurePeerSetChangeListener();
        }

        private Generic(
                AsyncObservableSetValue<E> observable,
                AsyncObservableSetValue<E> peer,
                AsyncObservableSet<E> currentValue,
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

            if (source.setChangeListeners != null) {
                setChangeListeners = source.setChangeListeners;
                setChangeSize = source.setChangeSize;
                ensurePeerSetChangeListener();
            }
        }

        @Override
        protected ProxyAsyncSetExpressionHelper<E> addListener(
                InvalidationListener listener, AsyncObservableSet<E> currentValue) {
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
        protected ProxyAsyncSetExpressionHelper<E> removeListener(
                InvalidationListener listener, AsyncObservableSet<E> currentValue) {
            if (invalidationListeners != null) {
                for (int index = 0; index < invalidationSize; index++) {
                    if (invalidationListeners[index].equals(listener)) {
                        ProxyAsyncSetExpressionHelper<E> helper = getSingleListenerHelper(index, 1, 0, 0, 0, 0);
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
        protected ProxyAsyncSetExpressionHelper<E> addListener(
                SubInvalidationListener listener, AsyncObservableSet<E> currentValue) {
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
        protected ProxyAsyncSetExpressionHelper<E> removeListener(
                SubInvalidationListener listener, AsyncObservableSet<E> currentValue) {
            if (subInvalidationListeners != null) {
                for (int index = 0; index < subInvalidationSize; index++) {
                    if (subInvalidationListeners[index].equals(listener)) {
                        ProxyAsyncSetExpressionHelper<E> helper = getSingleListenerHelper(index, 0, 1, 0, 0, 0);
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
        protected ProxyAsyncSetExpressionHelper<E> addListener(
                ChangeListener<? super AsyncObservableSet<E>> listener, AsyncObservableSet<E> currentValue) {
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
        protected ProxyAsyncSetExpressionHelper<E> removeListener(
                ChangeListener<? super AsyncObservableSet<E>> listener, AsyncObservableSet<E> currentValue) {
            if (changeListeners != null) {
                for (int index = 0; index < changeSize; index++) {
                    if (changeListeners[index].equals(listener)) {
                        ProxyAsyncSetExpressionHelper<E> helper = getSingleListenerHelper(index, 0, 0, 1, 0, 0);
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
        protected ProxyAsyncSetExpressionHelper<E> addListener(
                SubChangeListener listener, AsyncObservableSet<E> currentValue) {
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
        protected ProxyAsyncSetExpressionHelper<E> removeListener(
                SubChangeListener listener, AsyncObservableSet<E> currentValue) {
            if (subChangeListeners != null) {
                for (int index = 0; index < subChangeSize; index++) {
                    if (subChangeListeners[index].equals(listener)) {
                        ProxyAsyncSetExpressionHelper<E> helper = getSingleListenerHelper(index, 0, 0, 0, 1, 0);
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
        protected ProxyAsyncSetExpressionHelper<E> addListener(
                SetChangeListener<? super E> listener, AsyncObservableSet<E> currentValue) {
            Generic<E> helper = new Generic<>(observable, peer, currentValue, this);

            if (helper.setChangeListeners == null) {
                helper.setChangeListeners = new SetChangeListener[] {listener};
                helper.setChangeSize = 1;
            } else {
                helper.setChangeListeners = Arrays.copyOf(helper.setChangeListeners, helper.setChangeSize + 1);
                helper.setChangeSize = trim(helper.setChangeSize, helper.setChangeListeners);
                helper.setChangeListeners[helper.setChangeSize++] = listener;
            }

            if (helper.setChangeSize == 1) {
                helper.currentValue = currentValue;
            }

            ensurePeerSetChangeListener();
            return helper;
        }

        @Override
        @SuppressWarnings("unchecked")
        protected ProxyAsyncSetExpressionHelper<E> removeListener(
                SetChangeListener<? super E> listener, AsyncObservableSet<E> currentValue) {
            if (setChangeListeners != null) {
                for (int index = 0; index < setChangeSize; index++) {
                    if (setChangeListeners[index].equals(listener)) {
                        ProxyAsyncSetExpressionHelper<E> helper = getSingleListenerHelper(index, 0, 0, 0, 0, 1);
                        if (helper != null) {
                            return helper;
                        }

                        Generic<E> generic = new Generic<>(observable, peer, currentValue, this);

                        if (setChangeSize == 1) {
                            generic.setChangeListeners = null;
                            generic.setChangeSize = 0;
                        } else {
                            final int numMoved = setChangeSize - index - 1;
                            generic.setChangeSize = setChangeSize - 1;
                            generic.setChangeListeners = new SetChangeListener[setChangeSize - 1];
                            System.arraycopy(setChangeListeners, 0, generic.setChangeListeners, 0, index);

                            if (numMoved > 0) {
                                System.arraycopy(
                                    setChangeListeners, index + 1, generic.setChangeListeners, index, numMoved);
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
            for (int i = 0; i < invalidationSize; i++) {
                invalidationListeners[i].invalidated(observable);
            }

            if ((currentValue != oldValue) || (change != null)) {
                for (int i = 0; i < changeSize; i++) {
                    changeListeners[i].changed(observable, oldValue, currentValue);
                }

                if (setChangeSize > 0) {
                    if (change != null) {
                        for (int i = 0; i < setChangeSize; i++) {
                            setChangeListeners[i].onChanged(change);
                        }
                    } else {
                        change = new SimpleChange<>(observable);
                        if (currentValue == null) {
                            for (final E element : oldValue) {
                                change.setRemoved(element);
                                for (int i = 0; i < setChangeSize; i++) {
                                    setChangeListeners[i].onChanged(change);
                                }
                            }
                        } else if (oldValue == null) {
                            for (final E element : currentValue) {
                                change.setAdded(element);
                                for (int i = 0; i < setChangeSize; i++) {
                                    setChangeListeners[i].onChanged(change);
                                }
                            }
                        } else {
                            for (final E element : oldValue) {
                                if (!currentValue.contains(element)) {
                                    change.setRemoved(element);
                                    for (int i = 0; i < setChangeSize; i++) {
                                        setChangeListeners[i].onChanged(change);
                                    }
                                }
                            }

                            for (final E element : currentValue) {
                                if (!oldValue.contains(element)) {
                                    change.setAdded(element);
                                    for (int i = 0; i < setChangeSize; i++) {
                                        setChangeListeners[i].onChanged(change);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        private ProxyAsyncSetExpressionHelper<E> getSingleListenerHelper(
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
                removePeerListeners();
                return new SingleInvalidation<>(
                    observable, peer, invalidationListeners[invalidationSize == 2 ? 1 - index : 0]);
            }

            if (invalidationSize - removeInvalidation == 0
                    && subInvalidationSize - removeSubInvalidation == 1
                    && changeSize - removeChange == 0
                    && subChangeSize - removeSubChange == 0
                    && setChangeSize - removeSetChange == 0) {
                removePeerListeners();
                return new SingleSubInvalidation<>(
                    observable, peer, subInvalidationListeners[subInvalidationSize == 2 ? 1 - index : 0]);
            }

            if (invalidationSize - removeInvalidation == 0
                    && subInvalidationSize - removeSubInvalidation == 0
                    && changeSize - removeChange == 1
                    && subChangeSize - removeSubChange == 0
                    && setChangeSize - removeSetChange == 0) {
                removePeerListeners();
                return new SingleChange<>(
                    observable, peer, currentValue, changeListeners[changeSize == 2 ? 1 - index : 0]);
            }

            if (invalidationSize - removeInvalidation == 0
                    && subInvalidationSize - removeSubInvalidation == 0
                    && changeSize - removeChange == 0
                    && subChangeSize - removeSubChange == 1
                    && setChangeSize - removeSetChange == 0) {
                removePeerListeners();
                return new SingleSubChange<>(
                    observable, peer, currentValue, subChangeListeners[subChangeSize == 2 ? 1 - index : 0]);
            }

            if (invalidationSize - removeInvalidation == 0
                    && subInvalidationSize - removeSubInvalidation == 0
                    && changeSize - removeChange == 0
                    && subChangeSize - removeSubChange == 0
                    && setChangeSize - removeSetChange == 1) {
                removePeerListeners();
                return new SingleSetChange<>(
                    observable, peer, currentValue, setChangeListeners[setChangeSize == 2 ? 1 - index : 0]);
            }

            return null;
        }

        @Override
        protected void setPeer(@Nullable AsyncObservableSetValue<E> peer) {
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

            if (setChangeSize > 0) {
                ensurePeerSetChangeListener();
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

        private void ensurePeerSetChangeListener() {
            boolean created = false;

            if (peerSetChangeListener == null) {
                peerSetChangeListener =
                    change -> {
                        for (int i = 0; i < setChangeSize; ++i) {
                            setChangeListeners[i].onChanged(change);
                        }
                    };

                created = true;
            }

            if (peer != null && created) {
                peer.addListener(peerSetChangeListener);
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

                if (peerSetChangeListener != null) {
                    peer.removeListener(peerSetChangeListener);
                }
            }

            peerInvalidationListener = null;
            peerChangeListener = null;
            peerSubInvalidationListener = null;
            peerSubChangeListener = null;
            peerSetChangeListener = null;
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
