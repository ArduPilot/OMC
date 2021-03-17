/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.beans.binding;

import com.sun.javafx.binding.ExpressionHelperBase;
import java.util.Arrays;
import java.util.Objects;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import org.asyncfx.PublishSource;
import org.jetbrains.annotations.Nullable;

@PublishSource(
    module = "openjfx",
    licenses = {"openjfx", "intel-gpl-classpath-exception"}
)
public abstract class ProxyExpressionHelper<T> extends ExpressionHelperBase {

    public static <T> ProxyExpressionHelper<T> addListener(
            ProxyExpressionHelper<T> helper,
            ObservableValue<T> observable,
            @Nullable ObservableValue<T> peer,
            InvalidationListener listener) {
        if ((observable == null) || (listener == null)) {
            throw new NullPointerException();
        }

        observable.getValue();
        return (helper == null) ? new SingleInvalidation<>(observable, peer, listener) : helper.addListener(listener);
    }

    public static <T> ProxyExpressionHelper<T> removeListener(
            ProxyExpressionHelper<T> helper, InvalidationListener listener) {
        if (listener == null) {
            throw new NullPointerException();
        }

        return (helper == null) ? null : helper.removeListener(listener);
    }

    public static <T> ProxyExpressionHelper<T> addListener(
            ProxyExpressionHelper<T> helper,
            ObservableValue<T> observable,
            @Nullable ObservableValue<T> peer,
            ChangeListener<? super T> listener) {
        if ((observable == null) || (listener == null)) {
            throw new NullPointerException();
        }

        return (helper == null) ? new SingleChange<>(observable, peer, listener) : helper.addListener(listener);
    }

    public static <T> ProxyExpressionHelper<T> removeListener(
            ProxyExpressionHelper<T> helper, ChangeListener<? super T> listener) {
        if (listener == null) {
            throw new NullPointerException();
        }

        return (helper == null) ? null : helper.removeListener(listener);
    }

    public static <T> void fireValueChangedEvent(ProxyExpressionHelper<T> helper) {
        if (helper != null) {
            helper.fireValueChangedEvent();
        }
    }

    public static <T> void setPeer(ProxyExpressionHelper<T> helper, @Nullable ObservableValue<T> peer) {
        if (helper != null) {
            helper.setPeer(peer);
        }
    }

    protected final ObservableValue<T> observable;
    protected ObservableValue<T> peer;

    private ProxyExpressionHelper(ObservableValue<T> observable, @Nullable ObservableValue<T> peer) {
        this.observable = observable;
        this.peer = peer;
    }

    protected abstract ProxyExpressionHelper<T> addListener(InvalidationListener listener);

    protected abstract ProxyExpressionHelper<T> removeListener(InvalidationListener listener);

    protected abstract ProxyExpressionHelper<T> addListener(ChangeListener<? super T> listener);

    protected abstract ProxyExpressionHelper<T> removeListener(ChangeListener<? super T> listener);

    protected abstract void fireValueChangedEvent();

    protected abstract void setPeer(@Nullable ObservableValue<T> peer);

    private static class SingleInvalidation<T> extends ProxyExpressionHelper<T> {

        private final InvalidationListener peerListener =
            new InvalidationListener() {
                @Override
                public void invalidated(Observable observable) {
                    listener.invalidated(SingleInvalidation.this.observable);
                }
            };

        private final InvalidationListener listener;

        private SingleInvalidation(
                ObservableValue<T> expression, @Nullable ObservableValue<T> peer, InvalidationListener listener) {
            super(expression, peer);
            this.listener = listener;
            addPeerListener();
        }

        @Override
        protected ProxyExpressionHelper<T> addListener(InvalidationListener listener) {
            removePeerListener();
            return new Generic<>(observable, peer, this.listener, listener);
        }

        @Override
        protected ProxyExpressionHelper<T> removeListener(InvalidationListener listener) {
            if (listener.equals(this.listener)) {
                removePeerListener();
                return null;
            }

            return this;
        }

        @Override
        protected ProxyExpressionHelper<T> addListener(ChangeListener<? super T> listener) {
            removePeerListener();
            return new Generic<>(observable, peer, this.listener, listener);
        }

        @Override
        protected ProxyExpressionHelper<T> removeListener(ChangeListener<? super T> listener) {
            return this;
        }

        @Override
        protected void fireValueChangedEvent() {
            try {
                listener.invalidated(observable);
            } catch (Exception e) {
                Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
            }
        }

        @Override
        protected void setPeer(@Nullable ObservableValue<T> peer) {
            removePeerListener();
            this.peer = peer;
            addPeerListener();
            fireValueChangedEvent();
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

    private static class SingleChange<T> extends ProxyExpressionHelper<T> {

        private final ChangeListener<? super T> peerListener =
            new ChangeListener<>() {
                @Override
                public void changed(ObservableValue<? extends T> observable, T oldValue, T newValue) {
                    listener.changed(SingleChange.this.observable, oldValue, newValue);
                }
            };

        private final ChangeListener<? super T> listener;
        private T currentValue;

        private SingleChange(
                ObservableValue<T> observable, ObservableValue<T> peer, ChangeListener<? super T> listener) {
            super(observable, peer);
            this.listener = listener;
            this.currentValue = observable.getValue();
            addPeerListener();
        }

        @Override
        protected ProxyExpressionHelper<T> addListener(InvalidationListener listener) {
            removePeerListener();
            return new Generic<>(observable, peer, listener, this.listener);
        }

        @Override
        protected ProxyExpressionHelper<T> removeListener(InvalidationListener listener) {
            return this;
        }

        @Override
        protected ProxyExpressionHelper<T> addListener(ChangeListener<? super T> listener) {
            removePeerListener();
            return new Generic<>(observable, peer, this.listener, listener);
        }

        @Override
        protected ProxyExpressionHelper<T> removeListener(ChangeListener<? super T> listener) {
            if (listener.equals(this.listener)) {
                removePeerListener();
                return null;
            }

            return this;
        }

        @Override
        protected void fireValueChangedEvent() {
            final T oldValue = currentValue;
            currentValue = observable.getValue();
            final boolean changed = !Objects.equals(currentValue, oldValue);
            if (changed) {
                try {
                    listener.changed(observable, oldValue, currentValue);
                } catch (Exception e) {
                    Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
                }
            }
        }

        @Override
        protected void setPeer(@Nullable ObservableValue<T> peer) {
            removePeerListener();
            this.peer = peer;
            addPeerListener();
            fireValueChangedEvent();
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

    private static class Generic<T> extends ProxyExpressionHelper<T> {

        private InvalidationListener peerInvalidationListener;
        private ChangeListener<? super T> peerChangeListener;
        private InvalidationListener[] invalidationListeners;
        private ChangeListener<? super T>[] changeListeners;
        private int invalidationSize;
        private int changeSize;
        private boolean locked;
        private T currentValue;

        private Generic(
                ObservableValue<T> observable,
                @Nullable ObservableValue<T> peer,
                InvalidationListener listener0,
                InvalidationListener listener1) {
            super(observable, peer);
            this.invalidationListeners = new InvalidationListener[] {listener0, listener1};
            this.invalidationSize = 2;
            ensurePeerInvalidationListener();
        }

        @SuppressWarnings("unchecked")
        private Generic(
                ObservableValue<T> observable,
                @Nullable ObservableValue<T> peer,
                ChangeListener<? super T> listener0,
                ChangeListener<? super T> listener1) {
            super(observable, peer);
            this.changeListeners = new ChangeListener[] {listener0, listener1};
            this.changeSize = 2;
            this.currentValue = observable.getValue();
            ensurePeerChangeListener();
        }

        @SuppressWarnings("unchecked")
        private Generic(
                ObservableValue<T> observable,
                @Nullable ObservableValue<T> peer,
                InvalidationListener invalidationListener,
                ChangeListener<? super T> changeListener) {
            super(observable, peer);
            this.invalidationListeners = new InvalidationListener[] {invalidationListener};
            this.invalidationSize = 1;
            this.changeListeners = new ChangeListener[] {changeListener};
            this.changeSize = 1;
            this.currentValue = observable.getValue();
            ensurePeerInvalidationListener();
            ensurePeerChangeListener();
        }

        @Override
        protected Generic<T> addListener(InvalidationListener listener) {
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
        protected ProxyExpressionHelper<T> removeListener(InvalidationListener listener) {
            if (invalidationListeners != null) {
                for (int index = 0; index < invalidationSize; index++) {
                    if (listener.equals(invalidationListeners[index])) {
                        if (invalidationSize == 1) {
                            if (changeSize == 1) {
                                removePeerListeners();
                                return new SingleChange<>(observable, peer, changeListeners[0]);
                            }

                            invalidationListeners = null;
                            invalidationSize = 0;
                        } else if ((invalidationSize == 2) && (changeSize == 0)) {
                            removePeerListeners();
                            return new SingleInvalidation<>(observable, peer, invalidationListeners[1 - index]);
                        } else {
                            final int numMoved = invalidationSize - index - 1;
                            final InvalidationListener[] oldListeners = invalidationListeners;
                            if (locked) {
                                invalidationListeners = new InvalidationListener[invalidationListeners.length];
                                System.arraycopy(oldListeners, 0, invalidationListeners, 0, index);
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
        @SuppressWarnings("unchecked")
        protected ProxyExpressionHelper<T> addListener(ChangeListener<? super T> listener) {
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
                currentValue = observable.getValue();
            }

            ensurePeerChangeListener();
            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        protected ProxyExpressionHelper<T> removeListener(ChangeListener<? super T> listener) {
            if (changeListeners != null) {
                for (int index = 0; index < changeSize; index++) {
                    if (listener.equals(changeListeners[index])) {
                        if (changeSize == 1) {
                            if (invalidationSize == 1) {
                                removePeerListeners();
                                return new SingleInvalidation<>(observable, peer, invalidationListeners[0]);
                            }

                            changeListeners = null;
                            changeSize = 0;
                        } else if ((changeSize == 2) && (invalidationSize == 0)) {
                            removePeerListeners();
                            return new SingleChange<>(observable, peer, changeListeners[1 - index]);
                        } else {
                            final int numMoved = changeSize - index - 1;
                            final ChangeListener<? super T>[] oldListeners = changeListeners;
                            if (locked) {
                                changeListeners = new ChangeListener[changeListeners.length];
                                System.arraycopy(oldListeners, 0, changeListeners, 0, index);
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
        protected void fireValueChangedEvent() {
            final InvalidationListener[] curInvalidationList = invalidationListeners;
            final int curInvalidationSize = invalidationSize;
            final ChangeListener<? super T>[] curChangeList = changeListeners;
            final int curChangeSize = changeSize;

            try {
                locked = true;
                for (int i = 0; i < curInvalidationSize; i++) {
                    try {
                        curInvalidationList[i].invalidated(observable);
                    } catch (Exception e) {
                        Thread.currentThread()
                            .getUncaughtExceptionHandler()
                            .uncaughtException(Thread.currentThread(), e);
                    }
                }

                if (curChangeSize > 0) {
                    final T oldValue = currentValue;
                    currentValue = observable.getValue();
                    final boolean changed = !Objects.equals(currentValue, oldValue);
                    if (changed) {
                        for (int i = 0; i < curChangeSize; i++) {
                            try {
                                curChangeList[i].changed(observable, oldValue, currentValue);
                            } catch (Exception e) {
                                Thread.currentThread()
                                    .getUncaughtExceptionHandler()
                                    .uncaughtException(Thread.currentThread(), e);
                            }
                        }
                    }
                }
            } finally {
                locked = false;
            }
        }

        @Override
        protected void setPeer(@Nullable ObservableValue<T> peer) {
            removePeerListeners();

            this.peer = peer;

            if (invalidationSize > 0) {
                ensurePeerInvalidationListener();
            }

            if (changeSize > 0) {
                ensurePeerChangeListener();
            }

            fireValueChangedEvent();
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

        private void removePeerListeners() {
            if (peer != null) {
                if (peerInvalidationListener != null) {
                    peer.removeListener(peerInvalidationListener);
                }

                if (peerChangeListener != null) {
                    peer.removeListener(peerChangeListener);
                }
            }

            peerInvalidationListener = null;
            peerChangeListener = null;
        }
    }

}
