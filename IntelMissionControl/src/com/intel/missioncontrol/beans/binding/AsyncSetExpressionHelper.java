/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.beans.binding;

import static javafx.collections.SetChangeListener.Change;

import com.intel.missioncontrol.PublishSource;
import com.intel.missioncontrol.beans.value.AsyncObservableSetValue;
import com.intel.missioncontrol.collections.AsyncObservableSet;
import com.sun.javafx.binding.ExpressionHelperBase;
import java.util.Arrays;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.collections.SetChangeListener;

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
            ? new SingleInvalidation<E>(observable, listener)
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
            ChangeListener<? super AsyncObservableSet<E>> listener) {
        if ((observable == null) || (listener == null)) {
            throw new NullPointerException();
        }

        return (helper == null)
            ? new SingleChange<E>(observable, listener, currentValue)
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
            SetChangeListener<? super E> listener) {
        if ((observable == null) || (listener == null)) {
            throw new NullPointerException();
        }

        return (helper == null)
            ? new SingleSetChange<E>(observable, listener, currentValue)
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

    public static <E> void fireValueChangedEvent(AsyncSetExpressionHelper<E> helper, AsyncObservableSet<E> newValue) {
        if (helper != null) {
            helper.fireValueChangedEvent(newValue);
        }
    }

    public static <E> void fireValueChangedEvent(AsyncSetExpressionHelper<E> helper, Change<? extends E> change) {
        if (helper != null) {
            helper.fireValueChangedEvent(change);
        }
    }

    protected final AsyncObservableSetValue<E> observable;

    protected AsyncSetExpressionHelper(AsyncObservableSetValue<E> observable) {
        this.observable = observable;
    }

    protected abstract AsyncSetExpressionHelper<E> addListener(
            InvalidationListener listener, AsyncObservableSet<E> currentValue);

    protected abstract AsyncSetExpressionHelper<E> removeListener(
            InvalidationListener listener, AsyncObservableSet<E> currentValue);

    protected abstract AsyncSetExpressionHelper<E> addListener(
            ChangeListener<? super AsyncObservableSet<E>> listener, AsyncObservableSet<E> currentValue);

    protected abstract AsyncSetExpressionHelper<E> removeListener(
            ChangeListener<? super AsyncObservableSet<E>> listener, AsyncObservableSet<E> currentValue);

    protected abstract AsyncSetExpressionHelper<E> addListener(
            SetChangeListener<? super E> listener, AsyncObservableSet<E> currentValue);

    protected abstract AsyncSetExpressionHelper<E> removeListener(
            SetChangeListener<? super E> listener, AsyncObservableSet<E> currentValue);

    protected abstract boolean validatesValue();

    protected abstract boolean containsBidirectionalBindingEndpoints();

    protected abstract void fireValueChangedEvent(AsyncObservableSet<E> newValue);

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
            return new Generic<E>(observable, this.listener, listener);
        }

        @Override
        protected AsyncSetExpressionHelper<E> removeListener(
                InvalidationListener listener, AsyncObservableSet<E> currentValue) {
            return (listener.equals(this.listener)) ? null : this;
        }

        @Override
        protected AsyncSetExpressionHelper<E> addListener(
                ChangeListener<? super AsyncObservableSet<E>> listener, AsyncObservableSet<E> currentValue) {
            return new Generic<E>(observable, this.listener, listener, currentValue);
        }

        @Override
        protected AsyncSetExpressionHelper<E> removeListener(
                ChangeListener<? super AsyncObservableSet<E>> listener, AsyncObservableSet<E> currentValue) {
            return this;
        }

        @Override
        protected AsyncSetExpressionHelper<E> addListener(
                SetChangeListener<? super E> listener, AsyncObservableSet<E> currentValue) {
            return new Generic<E>(observable, this.listener, listener, currentValue);
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
        protected void fireValueChangedEvent(AsyncObservableSet<E> newValue) {
            listener.invalidated(observable);
        }

        @Override
        protected void fireValueChangedEvent(Change<? extends E> change) {
            listener.invalidated(observable);
        }
    }

    private static class SingleChange<E> extends AsyncSetExpressionHelper<E> {
        private final ChangeListener<? super AsyncObservableSet<E>> listener;
        private AsyncObservableSet<E> currentValue;

        private SingleChange(
                AsyncObservableSetValue<E> observable,
                ChangeListener<? super AsyncObservableSet<E>> listener,
                AsyncObservableSet<E> currentValue) {
            super(observable);
            this.listener = listener;
            this.currentValue = currentValue;
        }

        @Override
        protected AsyncSetExpressionHelper<E> addListener(
                InvalidationListener listener, AsyncObservableSet<E> currentValue) {
            return new Generic<E>(observable, listener, this.listener, currentValue);
        }

        @Override
        protected AsyncSetExpressionHelper<E> removeListener(
                InvalidationListener listener, AsyncObservableSet<E> currentValue) {
            return this;
        }

        @Override
        protected AsyncSetExpressionHelper<E> addListener(
                ChangeListener<? super AsyncObservableSet<E>> listener, AsyncObservableSet<E> currentValue) {
            return new Generic<E>(observable, this.listener, listener, currentValue);
        }

        @Override
        protected AsyncSetExpressionHelper<E> removeListener(
                ChangeListener<? super AsyncObservableSet<E>> listener, AsyncObservableSet<E> currentValue) {
            return (listener.equals(this.listener)) ? null : this;
        }

        @Override
        protected AsyncSetExpressionHelper<E> addListener(
                SetChangeListener<? super E> listener, AsyncObservableSet<E> currentValue) {
            return new Generic<E>(observable, this.listener, listener, currentValue);
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
        protected void fireValueChangedEvent(AsyncObservableSet<E> newValue) {
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

    private static class SingleSetChange<E> extends AsyncSetExpressionHelper<E> {
        private final SetChangeListener<? super E> listener;
        private AsyncObservableSet<E> currentValue;

        private SingleSetChange(
                AsyncObservableSetValue<E> observable,
                SetChangeListener<? super E> listener,
                AsyncObservableSet<E> currentValue) {
            super(observable);
            this.listener = listener;
            this.currentValue = currentValue;
        }

        @Override
        protected AsyncSetExpressionHelper<E> addListener(
                InvalidationListener listener, AsyncObservableSet<E> currentValue) {
            return new Generic<E>(observable, listener, this.listener, currentValue);
        }

        @Override
        protected AsyncSetExpressionHelper<E> removeListener(
                InvalidationListener listener, AsyncObservableSet<E> currentValue) {
            return this;
        }

        @Override
        protected AsyncSetExpressionHelper<E> addListener(
                ChangeListener<? super AsyncObservableSet<E>> listener, AsyncObservableSet<E> currentValue) {
            return new Generic<E>(observable, listener, this.listener, currentValue);
        }

        @Override
        protected AsyncSetExpressionHelper<E> removeListener(
                ChangeListener<? super AsyncObservableSet<E>> listener, AsyncObservableSet<E> currentValue) {
            return this;
        }

        @Override
        protected AsyncSetExpressionHelper<E> addListener(
                SetChangeListener<? super E> listener, AsyncObservableSet<E> currentValue) {
            return new Generic<E>(observable, this.listener, listener, currentValue);
        }

        @Override
        protected AsyncSetExpressionHelper<E> removeListener(
                SetChangeListener<? super E> listener, AsyncObservableSet<E> currentValue) {
            return (listener.equals(this.listener)) ? null : this;
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
        protected void fireValueChangedEvent(AsyncObservableSet<E> newValue) {
            final AsyncObservableSet<E> oldValue = currentValue;
            currentValue = newValue;
            if (currentValue != oldValue) {
                final SimpleChange<E> change = new SimpleChange<E>(observable);
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
            listener.onChanged(new SimpleChange<E>(observable, change));
        }
    }

    private static class Generic<E> extends AsyncSetExpressionHelper<E> {
        private InvalidationListener[] invalidationListeners;
        private ChangeListener<? super AsyncObservableSet<E>>[] changeListeners;
        private SetChangeListener<? super E>[] setChangeListeners;
        private int invalidationSize;
        private int changeSize;
        private int setChangeSize;
        private boolean locked;
        private AsyncObservableSet<E> currentValue;

        private Generic(
                AsyncObservableSetValue<E> observable, InvalidationListener listener0, InvalidationListener listener1) {
            super(observable);
            this.invalidationListeners = new InvalidationListener[] {listener0, listener1};
            this.invalidationSize = 2;
        }

        private Generic(
                AsyncObservableSetValue<E> observable,
                ChangeListener<? super AsyncObservableSet<E>> listener0,
                ChangeListener<? super AsyncObservableSet<E>> listener1,
                AsyncObservableSet<E> currentValue) {
            super(observable);
            this.changeListeners = new ChangeListener[] {listener0, listener1};
            this.changeSize = 2;
            this.currentValue = currentValue;
        }

        private Generic(
                AsyncObservableSetValue<E> observable,
                SetChangeListener<? super E> listener0,
                SetChangeListener<? super E> listener1,
                AsyncObservableSet<E> currentValue) {
            super(observable);
            this.setChangeListeners = new SetChangeListener[] {listener0, listener1};
            this.setChangeSize = 2;
            this.currentValue = currentValue;
        }

        private Generic(
                AsyncObservableSetValue<E> observable,
                InvalidationListener invalidationListener,
                ChangeListener<? super AsyncObservableSet<E>> changeListener,
                AsyncObservableSet<E> currentValue) {
            super(observable);
            this.invalidationListeners = new InvalidationListener[] {invalidationListener};
            this.invalidationSize = 1;
            this.changeListeners = new ChangeListener[] {changeListener};
            this.changeSize = 1;
            this.currentValue = currentValue;
        }

        private Generic(
                AsyncObservableSetValue<E> observable,
                InvalidationListener invalidationListener,
                SetChangeListener<? super E> SetChangeListener,
                AsyncObservableSet<E> currentValue) {
            super(observable);
            this.invalidationListeners = new InvalidationListener[] {invalidationListener};
            this.invalidationSize = 1;
            this.setChangeListeners = new SetChangeListener[] {SetChangeListener};
            this.setChangeSize = 1;
            this.currentValue = currentValue;
        }

        private Generic(
                AsyncObservableSetValue<E> observable,
                ChangeListener<? super AsyncObservableSet<E>> changeListener,
                SetChangeListener<? super E> SetChangeListener,
                AsyncObservableSet<E> currentValue) {
            super(observable);
            this.changeListeners = new ChangeListener[] {changeListener};
            this.changeSize = 1;
            this.setChangeListeners = new SetChangeListener[] {SetChangeListener};
            this.setChangeSize = 1;
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
                    if (listener.equals(invalidationListeners[index])) {
                        if (invalidationSize == 1) {
                            if ((changeSize == 1) && (setChangeSize == 0)) {
                                return new SingleChange<>(observable, changeListeners[0], currentValue);
                            } else if ((changeSize == 0) && (setChangeSize == 1)) {
                                return new SingleSetChange<>(observable, setChangeListeners[0], currentValue);
                            }

                            invalidationListeners = null;
                            invalidationSize = 0;
                        } else if ((invalidationSize == 2) && (changeSize == 0) && (setChangeSize == 0)) {
                            return new SingleInvalidation<E>(observable, invalidationListeners[1 - index]);
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
        protected AsyncSetExpressionHelper<E> removeListener(
                ChangeListener<? super AsyncObservableSet<E>> listener, AsyncObservableSet<E> currentValue) {
            if (changeListeners != null) {
                for (int index = 0; index < changeSize; index++) {
                    if (listener.equals(changeListeners[index])) {
                        if (changeSize == 1) {
                            if ((invalidationSize == 1) && (setChangeSize == 0)) {
                                return new SingleInvalidation<>(observable, invalidationListeners[0]);
                            } else if ((invalidationSize == 0) && (setChangeSize == 1)) {
                                return new SingleSetChange<>(observable, setChangeListeners[0], currentValue);
                            }

                            changeListeners = null;
                            changeSize = 0;
                        } else if ((changeSize == 2) && (invalidationSize == 0) && (setChangeSize == 0)) {
                            return new SingleChange<E>(observable, changeListeners[1 - index], currentValue);
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
        protected AsyncSetExpressionHelper<E> removeListener(
                SetChangeListener<? super E> listener, AsyncObservableSet<E> currentValue) {
            if (setChangeListeners != null) {
                for (int index = 0; index < setChangeSize; index++) {
                    if (listener.equals(setChangeListeners[index])) {
                        if (setChangeSize == 1) {
                            if ((invalidationSize == 1) && (changeSize == 0)) {
                                return new SingleInvalidation<E>(observable, invalidationListeners[0]);
                            } else if ((invalidationSize == 0) && (changeSize == 1)) {
                                return new SingleChange<>(observable, changeListeners[0], currentValue);
                            }

                            setChangeListeners = null;
                            setChangeSize = 0;
                        } else if ((setChangeSize == 2) && (invalidationSize == 0) && (changeSize == 0)) {
                            return new SingleSetChange<>(observable, setChangeListeners[1 - index], currentValue);
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
        protected void fireValueChangedEvent(AsyncObservableSet<E> newValue) {
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
            final SimpleChange<E> mappedChange = (setChangeSize == 0) ? null : new SimpleChange<E>(observable, change);
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
                            change = new SimpleChange<E>(observable);
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
    }

    public static class SimpleChange<E> extends Change<E> {
        private E old;
        private E added;
        private boolean addOp;

        public SimpleChange(AsyncObservableSet<E> set) {
            super(set);
        }

        public SimpleChange(AsyncObservableSet<E> set, Change<? extends E> source) {
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
