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
            ProxyAsyncSetExpressionHelper<E> helper = new Generic<>(observable, peer, currentValue);
            helper.addListener(this.listener, currentValue);
            helper.addListener(listener, currentValue);
            return helper;
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
            ProxyAsyncSetExpressionHelper<E> helper = new Generic<>(observable, peer, currentValue);
            helper.addListener(this.listener, currentValue);
            helper.addListener(listener, currentValue);
            return helper;
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
            ProxyAsyncSetExpressionHelper<E> helper = new Generic<>(observable, peer, currentValue);
            helper.addListener(this.listener, currentValue);
            helper.addListener(listener, currentValue);
            return helper;
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
            ProxyAsyncSetExpressionHelper<E> helper = new Generic<>(observable, peer, currentValue);
            helper.addListener(this.listener, currentValue);
            helper.addListener(listener, currentValue);
            return helper;
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
            ProxyAsyncSetExpressionHelper<E> helper = new Generic<>(observable, peer, currentValue);
            helper.addListener(this.listener, currentValue);
            helper.addListener(listener, currentValue);
            return helper;
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
            ProxyAsyncSetExpressionHelper<E> helper = new Generic<>(observable, peer, currentValue);
            helper.addListener(this.listener, currentValue);
            helper.addListener(listener, currentValue);
            return helper;
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
            ProxyAsyncSetExpressionHelper<E> helper = new Generic<>(observable, peer, currentValue);
            helper.addListener(this.listener, currentValue);
            helper.addListener(listener, currentValue);
            return helper;
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
            ProxyAsyncSetExpressionHelper<E> helper = new Generic<>(observable, peer, currentValue);
            helper.addListener(this.listener, currentValue);
            helper.addListener(listener, currentValue);
            return helper;
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
            ProxyAsyncSetExpressionHelper<E> helper = new Generic<>(observable, peer, currentValue);
            helper.addListener(this.listener, currentValue);
            helper.addListener(listener, currentValue);
            return helper;
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
            ProxyAsyncSetExpressionHelper<E> helper = new Generic<>(observable, peer, currentValue);
            helper.addListener(this.listener, currentValue);
            helper.addListener(listener, currentValue);
            return helper;
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
            ProxyAsyncSetExpressionHelper<E> helper = new Generic<>(observable, peer, currentValue);
            helper.addListener(this.listener, currentValue);
            helper.addListener(listener, currentValue);
            return helper;
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
            ProxyAsyncSetExpressionHelper<E> helper = new Generic<>(observable, peer, currentValue);
            helper.addListener(this.listener, currentValue);
            helper.addListener(listener, currentValue);
            return helper;
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
            ProxyAsyncSetExpressionHelper<E> helper = new Generic<>(observable, peer, currentValue);
            helper.addListener(this.listener, currentValue);
            helper.addListener(listener, currentValue);
            return helper;
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
            ProxyAsyncSetExpressionHelper<E> helper = new Generic<>(observable, peer, currentValue);
            helper.addListener(this.listener, currentValue);
            helper.addListener(listener, currentValue);
            return helper;
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
            ProxyAsyncSetExpressionHelper<E> helper = new Generic<>(observable, peer, currentValue);
            helper.addListener(this.listener, currentValue);
            helper.addListener(listener, currentValue);
            return helper;
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
            ProxyAsyncSetExpressionHelper<E> helper = new Generic<>(observable, peer, currentValue);
            helper.addListener(this.listener, currentValue);
            helper.addListener(listener, currentValue);
            return helper;
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
            ProxyAsyncSetExpressionHelper<E> helper = new Generic<>(observable, peer, currentValue);
            helper.addListener(this.listener, currentValue);
            helper.addListener(listener, currentValue);
            return helper;
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
            ProxyAsyncSetExpressionHelper<E> helper = new Generic<>(observable, peer, currentValue);
            helper.addListener(this.listener, currentValue);
            helper.addListener(listener, currentValue);
            return helper;
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
            ProxyAsyncSetExpressionHelper<E> helper = new Generic<>(observable, peer, currentValue);
            helper.addListener(this.listener, currentValue);
            helper.addListener(listener, currentValue);
            return helper;
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
            ProxyAsyncSetExpressionHelper<E> helper = new Generic<>(observable, peer, currentValue);
            helper.addListener(this.listener, currentValue);
            helper.addListener(listener, currentValue);
            return helper;
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

        @Override
        protected ProxyAsyncSetExpressionHelper<E> addListener(
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

            ensurePeerInvalidationListener();
            return this;
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
        protected ProxyAsyncSetExpressionHelper<E> addListener(
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

            ensurePeerSubInvalidationListener();
            return this;
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
        protected ProxyAsyncSetExpressionHelper<E> addListener(
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

            ensurePeerChangeListener();
            return this;
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
        protected ProxyAsyncSetExpressionHelper<E> addListener(
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

            ensurePeerSubChangeListener();
            return this;
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
        protected ProxyAsyncSetExpressionHelper<E> addListener(
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

            ensurePeerSetChangeListener();
            return this;
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
            final SetChangeListener<? super E>[] curListChangeList = setChangeListeners;
            final int curListChangeSize = setChangeSize;
            try {
                locked = true;
                for (int i = 0; i < curInvalidationSize; i++) {
                    curInvalidationList[i].invalidated(observable);
                }

                if ((currentValue != oldValue) || (change != null)) {
                    for (int i = 0; i < curChangeSize; i++) {
                        curChangeList[i].changed(observable, oldValue, currentValue);
                    }

                    if (curListChangeSize > 0) {
                        if (change != null) {
                            for (int i = 0; i < curListChangeSize; i++) {
                                curListChangeList[i].onChanged(change);
                            }
                        } else {
                            change = new SimpleChange<>(observable);
                            if (currentValue == null) {
                                for (final E element : oldValue) {
                                    change.setRemoved(element);
                                    for (int i = 0; i < curListChangeSize; i++) {
                                        curListChangeList[i].onChanged(change);
                                    }
                                }
                            } else if (oldValue == null) {
                                for (final E element : currentValue) {
                                    change.setAdded(element);
                                    for (int i = 0; i < curListChangeSize; i++) {
                                        curListChangeList[i].onChanged(change);
                                    }
                                }
                            } else {
                                for (final E element : oldValue) {
                                    if (!currentValue.contains(element)) {
                                        change.setRemoved(element);
                                        for (int i = 0; i < curListChangeSize; i++) {
                                            curListChangeList[i].onChanged(change);
                                        }
                                    }
                                }

                                for (final E element : currentValue) {
                                    if (!oldValue.contains(element)) {
                                        change.setAdded(element);
                                        for (int i = 0; i < curListChangeSize; i++) {
                                            curListChangeList[i].onChanged(change);
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
