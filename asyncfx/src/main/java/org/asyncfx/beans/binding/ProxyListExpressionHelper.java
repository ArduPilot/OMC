/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.beans.binding;

import static javafx.collections.ListChangeListener.Change;

import com.sun.javafx.binding.ExpressionHelperBase;
import com.sun.javafx.collections.NonIterableChange;
import com.sun.javafx.collections.SourceAdapterChange;
import java.util.Arrays;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableListValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import org.asyncfx.PublishSource;
import org.jetbrains.annotations.Nullable;

@PublishSource(
    module = "openjfx",
    licenses = {"openjfx", "intel-gpl-classpath-exception"}
)
public abstract class ProxyListExpressionHelper<E> extends ExpressionHelperBase {

    public static <E> ProxyListExpressionHelper<E> addListener(
            ProxyListExpressionHelper<E> helper,
            ObservableListValue<E> observable,
            ObservableListValue<E> peer,
            InvalidationListener listener) {
        if ((observable == null) || (listener == null)) {
            throw new NullPointerException();
        }

        observable.getValue();
        return (helper == null) ? new SingleInvalidation<>(observable, peer, listener) : helper.addListener(listener);
    }

    public static <E> ProxyListExpressionHelper<E> removeListener(
            ProxyListExpressionHelper<E> helper, InvalidationListener listener) {
        if (listener == null) {
            throw new NullPointerException();
        }

        return (helper == null) ? null : helper.removeListener(listener);
    }

    public static <E> ProxyListExpressionHelper<E> addListener(
            ProxyListExpressionHelper<E> helper,
            ObservableListValue<E> observable,
            ObservableListValue<E> peer,
            ChangeListener<? super ObservableList<E>> listener) {
        if ((observable == null) || (listener == null)) {
            throw new NullPointerException();
        }

        return (helper == null) ? new SingleChange<>(observable, peer, listener) : helper.addListener(listener);
    }

    public static <E> ProxyListExpressionHelper<E> removeListener(
            ProxyListExpressionHelper<E> helper, ChangeListener<? super ObservableList<E>> listener) {
        if (listener == null) {
            throw new NullPointerException();
        }

        return (helper == null) ? null : helper.removeListener(listener);
    }

    public static <E> ProxyListExpressionHelper<E> addListener(
            ProxyListExpressionHelper<E> helper,
            ObservableListValue<E> observable,
            ObservableListValue<E> peer,
            ListChangeListener<? super E> listener) {
        if ((observable == null) || (listener == null)) {
            throw new NullPointerException();
        }

        return (helper == null) ? new SingleListChange<>(observable, peer, listener) : helper.addListener(listener);
    }

    public static <E> ProxyListExpressionHelper<E> removeListener(
            ProxyListExpressionHelper<E> helper, ListChangeListener<? super E> listener) {
        if (listener == null) {
            throw new NullPointerException();
        }

        return (helper == null) ? null : helper.removeListener(listener);
    }

    public static <E> void fireValueChangedEvent(ProxyListExpressionHelper<E> helper) {
        if (helper != null) {
            helper.fireValueChangedEvent();
        }
    }

    public static <E> void fireValueChangedEvent(ProxyListExpressionHelper<E> helper, Change<? extends E> change) {
        if (helper != null) {
            helper.fireValueChangedEvent(change);
        }
    }

    public static <T> void setPeer(ProxyListExpressionHelper<T> helper, @Nullable ObservableListValue<T> peer) {
        if (helper != null) {
            helper.setPeer(peer);
        }
    }

    protected final ObservableListValue<E> observable;
    protected ObservableListValue<E> peer;

    ProxyListExpressionHelper(ObservableListValue<E> observable, ObservableListValue<E> peer) {
        this.observable = observable;
        this.peer = peer;
    }

    protected abstract ProxyListExpressionHelper<E> addListener(InvalidationListener listener);

    protected abstract ProxyListExpressionHelper<E> removeListener(InvalidationListener listener);

    protected abstract ProxyListExpressionHelper<E> addListener(ChangeListener<? super ObservableList<E>> listener);

    protected abstract ProxyListExpressionHelper<E> removeListener(ChangeListener<? super ObservableList<E>> listener);

    protected abstract ProxyListExpressionHelper<E> addListener(ListChangeListener<? super E> listener);

    protected abstract ProxyListExpressionHelper<E> removeListener(ListChangeListener<? super E> listener);

    protected abstract void fireValueChangedEvent();

    protected abstract void fireValueChangedEvent(Change<? extends E> change);

    protected abstract void setPeer(@Nullable ObservableListValue<E> peer);

    private static class SingleInvalidation<E> extends ProxyListExpressionHelper<E> {
        private final InvalidationListener peerListener =
            new InvalidationListener() {
                @Override
                public void invalidated(Observable observable) {
                    listener.invalidated(SingleInvalidation.this.observable);
                }
            };

        private final InvalidationListener listener;

        private SingleInvalidation(
                ObservableListValue<E> observable, ObservableListValue<E> peer, InvalidationListener listener) {
            super(observable, peer);
            this.listener = listener;
            addPeerListener();
        }

        @Override
        protected ProxyListExpressionHelper<E> addListener(InvalidationListener listener) {
            removePeerListener();
            return new Generic<>(observable, peer, this.listener, listener);
        }

        @Override
        protected ProxyListExpressionHelper<E> removeListener(InvalidationListener listener) {
            if (listener.equals(this.listener)) {
                removePeerListener();
                return null;
            }

            return this;
        }

        @Override
        protected ProxyListExpressionHelper<E> addListener(ChangeListener<? super ObservableList<E>> listener) {
            removePeerListener();
            return new Generic<>(observable, peer, this.listener, listener);
        }

        @Override
        protected ProxyListExpressionHelper<E> removeListener(ChangeListener<? super ObservableList<E>> listener) {
            return this;
        }

        @Override
        protected ProxyListExpressionHelper<E> addListener(ListChangeListener<? super E> listener) {
            removePeerListener();
            return new Generic<>(observable, peer, this.listener, listener);
        }

        @Override
        protected ProxyListExpressionHelper<E> removeListener(ListChangeListener<? super E> listener) {
            return this;
        }

        @Override
        protected void fireValueChangedEvent() {
            listener.invalidated(observable);
        }

        @Override
        protected void fireValueChangedEvent(Change<? extends E> change) {
            listener.invalidated(observable);
        }

        @Override
        protected void setPeer(@Nullable ObservableListValue<E> peer) {
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

    private static class SingleChange<E> extends ProxyListExpressionHelper<E> {
        private final ChangeListener<? super ObservableList<E>> peerListener =
            new ChangeListener<>() {
                @Override
                public void changed(
                        ObservableValue<? extends ObservableList<E>> observable,
                        ObservableList<E> oldValue,
                        ObservableList<E> newValue) {
                    listener.changed(SingleChange.this.observable, oldValue, newValue);
                }
            };

        private final ChangeListener<? super ObservableList<E>> listener;
        private ObservableList<E> currentValue;

        private SingleChange(
                ObservableListValue<E> observable,
                ObservableListValue<E> peer,
                ChangeListener<? super ObservableList<E>> listener) {
            super(observable, peer);
            this.listener = listener;
            this.currentValue = observable.getValue();
            addPeerListener();
        }

        @Override
        protected ProxyListExpressionHelper<E> addListener(InvalidationListener listener) {
            removePeerListener();
            return new Generic<>(observable, peer, listener, this.listener);
        }

        @Override
        protected ProxyListExpressionHelper<E> removeListener(InvalidationListener listener) {
            return this;
        }

        @Override
        protected ProxyListExpressionHelper<E> addListener(ChangeListener<? super ObservableList<E>> listener) {
            removePeerListener();
            return new Generic<>(observable, peer, this.listener, listener);
        }

        @Override
        protected ProxyListExpressionHelper<E> removeListener(ChangeListener<? super ObservableList<E>> listener) {
            if (listener.equals(this.listener)) {
                removePeerListener();
                return null;
            }

            return this;
        }

        @Override
        protected ProxyListExpressionHelper<E> addListener(ListChangeListener<? super E> listener) {
            removePeerListener();
            return new Generic<>(observable, peer, this.listener, listener);
        }

        @Override
        protected ProxyListExpressionHelper<E> removeListener(ListChangeListener<? super E> listener) {
            return this;
        }

        @Override
        protected void fireValueChangedEvent() {
            final ObservableList<E> oldValue = currentValue;
            currentValue = observable.getValue();
            if (currentValue != oldValue) {
                listener.changed(observable, oldValue, currentValue);
            }
        }

        @Override
        protected void fireValueChangedEvent(Change<? extends E> change) {
            listener.changed(observable, currentValue, currentValue);
        }

        @Override
        protected void setPeer(@Nullable ObservableListValue<E> peer) {
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

    private static class SingleListChange<E> extends ProxyListExpressionHelper<E> {
        private final ListChangeListener<? super E> peerListener =
            new ListChangeListener<>() {
                @Override
                public void onChanged(Change<? extends E> change) {
                    listener.onChanged(change);
                }
            };

        private final ListChangeListener<? super E> listener;
        private ObservableList<E> currentValue;

        private SingleListChange(
                ObservableListValue<E> observable,
                ObservableListValue<E> peer,
                ListChangeListener<? super E> listener) {
            super(observable, peer);
            this.listener = listener;
            this.currentValue = observable.getValue();
            addPeerListener();
        }

        @Override
        protected ProxyListExpressionHelper<E> addListener(InvalidationListener listener) {
            removePeerListener();
            return new Generic<>(observable, peer, listener, this.listener);
        }

        @Override
        protected ProxyListExpressionHelper<E> removeListener(InvalidationListener listener) {
            return this;
        }

        @Override
        protected ProxyListExpressionHelper<E> addListener(ChangeListener<? super ObservableList<E>> listener) {
            removePeerListener();
            return new Generic<>(observable, peer, listener, this.listener);
        }

        @Override
        protected ProxyListExpressionHelper<E> removeListener(ChangeListener<? super ObservableList<E>> listener) {
            return this;
        }

        @Override
        protected ProxyListExpressionHelper<E> addListener(ListChangeListener<? super E> listener) {
            removePeerListener();
            return new Generic<>(observable, peer, this.listener, listener);
        }

        @Override
        protected ProxyListExpressionHelper<E> removeListener(ListChangeListener<? super E> listener) {
            if (listener.equals(this.listener)) {
                removePeerListener();
                return null;
            }

            return this;
        }

        @Override
        protected void fireValueChangedEvent() {
            final ObservableList<E> oldValue = currentValue;
            currentValue = observable.getValue();
            if (currentValue != oldValue) {
                final int safeSize = (currentValue == null) ? 0 : currentValue.size();
                final ObservableList<E> safeOldValue =
                    (oldValue == null)
                        ? FXCollections.emptyObservableList()
                        : FXCollections.unmodifiableObservableList(oldValue);
                final Change<E> change =
                    new NonIterableChange.GenericAddRemoveChange<>(0, safeSize, safeOldValue, observable);
                listener.onChanged(change);
            }
        }

        @Override
        protected void fireValueChangedEvent(final Change<? extends E> change) {
            listener.onChanged(new SourceAdapterChange<>(observable, change));
        }

        @Override
        protected void setPeer(@Nullable ObservableListValue<E> peer) {
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

    private static class Generic<E> extends ProxyListExpressionHelper<E> {
        private InvalidationListener peerInvalidationListener;
        private ChangeListener<? super ObservableList<E>> peerChangeListener;
        private ListChangeListener<E> peerListChangeListener;
        private InvalidationListener[] invalidationListeners;
        private ChangeListener<? super ObservableList<E>>[] changeListeners;
        private ListChangeListener<? super E>[] listChangeListeners;
        private int invalidationSize;
        private int changeSize;
        private int listChangeSize;
        private boolean locked;
        private ObservableList<E> currentValue;

        private Generic(
                ObservableListValue<E> observable,
                ObservableListValue<E> peer,
                InvalidationListener listener0,
                InvalidationListener listener1) {
            super(observable, peer);
            this.invalidationListeners = new InvalidationListener[] {listener0, listener1};
            this.invalidationSize = 2;
            ensurePeerInvalidationListener();
        }

        @SuppressWarnings("unchecked")
        private Generic(
                ObservableListValue<E> observable,
                ObservableListValue<E> peer,
                ChangeListener<? super ObservableList<E>> listener0,
                ChangeListener<? super ObservableList<E>> listener1) {
            super(observable, peer);
            this.changeListeners = new ChangeListener[] {listener0, listener1};
            this.changeSize = 2;
            this.currentValue = observable.getValue();
            ensurePeerChangeListener();
        }

        @SuppressWarnings("unchecked")
        private Generic(
                ObservableListValue<E> observable,
                ObservableListValue<E> peer,
                ListChangeListener<? super E> listener0,
                ListChangeListener<? super E> listener1) {
            super(observable, peer);
            this.listChangeListeners = new ListChangeListener[] {listener0, listener1};
            this.listChangeSize = 2;
            this.currentValue = observable.getValue();
            ensurePeerListChangeListener();
        }

        @SuppressWarnings("unchecked")
        private Generic(
                ObservableListValue<E> observable,
                ObservableListValue<E> peer,
                InvalidationListener invalidationListener,
                ChangeListener<? super ObservableList<E>> changeListener) {
            super(observable, peer);
            this.invalidationListeners = new InvalidationListener[] {invalidationListener};
            this.invalidationSize = 1;
            this.changeListeners = new ChangeListener[] {changeListener};
            this.changeSize = 1;
            this.currentValue = observable.getValue();
            ensurePeerInvalidationListener();
            ensurePeerChangeListener();
        }

        @SuppressWarnings("unchecked")
        private Generic(
                ObservableListValue<E> observable,
                ObservableListValue<E> peer,
                InvalidationListener invalidationListener,
                ListChangeListener<? super E> listChangeListener) {
            super(observable, peer);
            this.invalidationListeners = new InvalidationListener[] {invalidationListener};
            this.invalidationSize = 1;
            this.listChangeListeners = new ListChangeListener[] {listChangeListener};
            this.listChangeSize = 1;
            this.currentValue = observable.getValue();
            ensurePeerInvalidationListener();
            ensurePeerListChangeListener();
        }

        @SuppressWarnings("unchecked")
        private Generic(
                ObservableListValue<E> observable,
                ObservableListValue<E> peer,
                ChangeListener<? super ObservableList<E>> changeListener,
                ListChangeListener<? super E> listChangeListener) {
            super(observable, peer);
            this.changeListeners = new ChangeListener[] {changeListener};
            this.changeSize = 1;
            this.listChangeListeners = new ListChangeListener[] {listChangeListener};
            this.listChangeSize = 1;
            this.currentValue = observable.getValue();
            ensurePeerChangeListener();
            ensurePeerListChangeListener();
        }

        @Override
        protected ProxyListExpressionHelper<E> addListener(InvalidationListener listener) {
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
        protected ProxyListExpressionHelper<E> removeListener(InvalidationListener listener) {
            if (invalidationListeners != null) {
                for (int index = 0; index < invalidationSize; index++) {
                    if (listener.equals(invalidationListeners[index])) {
                        if (invalidationSize == 1) {
                            if ((changeSize == 1) && (listChangeSize == 0)) {
                                removePeerListeners();
                                return new SingleChange<>(observable, peer, changeListeners[0]);
                            } else if ((changeSize == 0) && (listChangeSize == 1)) {
                                removePeerListeners();
                                return new SingleListChange<>(observable, peer, listChangeListeners[0]);
                            }

                            invalidationListeners = null;
                            invalidationSize = 0;
                        } else if ((invalidationSize == 2) && (changeSize == 0) && (listChangeSize == 0)) {
                            removePeerListeners();
                            return new SingleInvalidation<>(observable, peer, invalidationListeners[1 - index]);
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
        @SuppressWarnings("unchecked")
        protected ProxyListExpressionHelper<E> addListener(ChangeListener<? super ObservableList<E>> listener) {
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
        protected ProxyListExpressionHelper<E> removeListener(ChangeListener<? super ObservableList<E>> listener) {
            if (changeListeners != null) {
                for (int index = 0; index < changeSize; index++) {
                    if (listener.equals(changeListeners[index])) {
                        if (changeSize == 1) {
                            if ((invalidationSize == 1) && (listChangeSize == 0)) {
                                removePeerListeners();
                                return new SingleInvalidation<>(observable, peer, invalidationListeners[0]);
                            } else if ((invalidationSize == 0) && (listChangeSize == 1)) {
                                removePeerListeners();
                                return new SingleListChange<>(observable, peer, listChangeListeners[0]);
                            }

                            changeListeners = null;
                            changeSize = 0;
                        } else if ((changeSize == 2) && (invalidationSize == 0) && (listChangeSize == 0)) {
                            removePeerListeners();
                            return new SingleChange<>(observable, peer, changeListeners[1 - index]);
                        } else {
                            final int numMoved = changeSize - index - 1;
                            final ChangeListener<? super ObservableList<E>>[] oldListeners = changeListeners;
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
        @SuppressWarnings("unchecked")
        protected ProxyListExpressionHelper<E> addListener(ListChangeListener<? super E> listener) {
            if (listChangeListeners == null) {
                listChangeListeners = new ListChangeListener[] {listener};
                listChangeSize = 1;
            } else {
                final int oldCapacity = listChangeListeners.length;
                if (locked) {
                    final int newCapacity = (listChangeSize < oldCapacity) ? oldCapacity : (oldCapacity * 3) / 2 + 1;
                    listChangeListeners = Arrays.copyOf(listChangeListeners, newCapacity);
                } else if (listChangeSize == oldCapacity) {
                    listChangeSize = trim(listChangeSize, listChangeListeners);
                    if (listChangeSize == oldCapacity) {
                        final int newCapacity = (oldCapacity * 3) / 2 + 1;
                        listChangeListeners = Arrays.copyOf(listChangeListeners, newCapacity);
                    }
                }

                listChangeListeners[listChangeSize++] = listener;
            }

            if (listChangeSize == 1) {
                currentValue = observable.getValue();
            }

            ensurePeerListChangeListener();
            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        protected ProxyListExpressionHelper<E> removeListener(ListChangeListener<? super E> listener) {
            if (listChangeListeners != null) {
                for (int index = 0; index < listChangeSize; index++) {
                    if (listener.equals(listChangeListeners[index])) {
                        if (listChangeSize == 1) {
                            if ((invalidationSize == 1) && (changeSize == 0)) {
                                removePeerListeners();
                                return new SingleInvalidation<>(observable, peer, invalidationListeners[0]);
                            } else if ((invalidationSize == 0) && (changeSize == 1)) {
                                removePeerListeners();
                                return new SingleChange<>(observable, peer, changeListeners[0]);
                            }

                            listChangeListeners = null;
                            listChangeSize = 0;
                        } else if ((listChangeSize == 2) && (invalidationSize == 0) && (changeSize == 0)) {
                            removePeerListeners();
                            return new SingleListChange<>(observable, peer, listChangeListeners[1 - index]);
                        } else {
                            final int numMoved = listChangeSize - index - 1;
                            final ListChangeListener<? super E>[] oldListeners = listChangeListeners;
                            if (locked) {
                                listChangeListeners = new ListChangeListener[listChangeListeners.length];
                                System.arraycopy(oldListeners, 0, listChangeListeners, 0, index + 1);
                            }

                            if (numMoved > 0) {
                                System.arraycopy(oldListeners, index + 1, listChangeListeners, index, numMoved);
                            }

                            listChangeSize--;
                            if (!locked) {
                                listChangeListeners[listChangeSize] = null; // Let gc do its work
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
            if ((changeSize == 0) && (listChangeSize == 0)) {
                notifyListeners(currentValue, null, false);
            } else {
                final ObservableList<E> oldValue = currentValue;
                currentValue = observable.getValue();
                if (currentValue != oldValue) {
                    Change<E> change = null;
                    if (listChangeSize > 0) {
                        final int safeSize = (currentValue == null) ? 0 : currentValue.size();
                        final ObservableList<E> safeOldValue =
                            (oldValue == null)
                                ? FXCollections.emptyObservableList()
                                : FXCollections.unmodifiableObservableList(oldValue);
                        change = new NonIterableChange.GenericAddRemoveChange<>(0, safeSize, safeOldValue, observable);
                    }

                    notifyListeners(oldValue, change, false);
                } else {
                    notifyListeners(currentValue, null, true);
                }
            }
        }

        @Override
        protected void fireValueChangedEvent(final Change<? extends E> change) {
            final Change<E> mappedChange = (listChangeSize == 0) ? null : new SourceAdapterChange<>(observable, change);
            notifyListeners(currentValue, mappedChange, false);
        }

        private void notifyListeners(ObservableList<E> oldValue, Change<E> change, boolean noChange) {
            final InvalidationListener[] curInvalidationList = invalidationListeners;
            final int curInvalidationSize = invalidationSize;
            final ChangeListener<? super ObservableList<E>>[] curChangeList = changeListeners;
            final int curChangeSize = changeSize;
            final ListChangeListener<? super E>[] curListChangeList = listChangeListeners;
            final int curListChangeSize = listChangeSize;
            try {
                locked = true;
                for (int i = 0; i < curInvalidationSize; i++) {
                    curInvalidationList[i].invalidated(observable);
                }

                if (!noChange) {
                    for (int i = 0; i < curChangeSize; i++) {
                        curChangeList[i].changed(observable, oldValue, currentValue);
                    }

                    if (change != null) {
                        for (int i = 0; i < curListChangeSize; i++) {
                            change.reset();
                            curListChangeList[i].onChanged(change);
                        }
                    }
                }
            } finally {
                locked = false;
            }
        }

        @Override
        protected void setPeer(@Nullable ObservableListValue<E> peer) {
            removePeerListeners();

            this.peer = peer;

            if (invalidationSize > 0) {
                ensurePeerInvalidationListener();
            }

            if (changeSize > 0) {
                ensurePeerChangeListener();
            }

            if (listChangeSize > 0) {
                ensurePeerListChangeListener();
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

                if (peerListChangeListener != null) {
                    peer.removeListener(peerListChangeListener);
                }
            }

            peerInvalidationListener = null;
            peerChangeListener = null;
            peerListChangeListener = null;
        }
    }

}
