/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.beans.binding;

import static javafx.collections.ListChangeListener.Change;

import com.intel.missioncontrol.PublishSource;
import com.intel.missioncontrol.beans.value.AsyncObservableListValue;
import com.intel.missioncontrol.collections.AsyncListChangeListener.AsyncChange;
import com.intel.missioncontrol.collections.AsyncObservableList;
import com.intel.missioncontrol.collections.FXAsyncCollections;
import com.sun.javafx.binding.ExpressionHelperBase;
import com.sun.javafx.collections.NonIterableChange;
import java.util.Arrays;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;

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

    public static <E> void fireValueChangedEvent(AsyncListExpressionHelper<E> helper, AsyncObservableList<E> newValue) {
        if (helper != null) {
            helper.fireValueChangedEvent(newValue);
        }
    }

    public static <E> void fireValueChangedEvent(AsyncListExpressionHelper<E> helper, AsyncChange<? extends E> change) {
        if (helper != null) {
            helper.fireValueChangedEvent(change);
        }
    }

    protected final AsyncObservableListValue<E> observable;

    protected AsyncListExpressionHelper(AsyncObservableListValue<E> observable) {
        this.observable = observable;
    }

    protected abstract AsyncListExpressionHelper<E> addListener(
            InvalidationListener listener, AsyncObservableList<E> currentValue);

    protected abstract AsyncListExpressionHelper<E> removeListener(
            InvalidationListener listener, AsyncObservableList<E> currentValue);

    protected abstract AsyncListExpressionHelper<E> addListener(
            ChangeListener<? super AsyncObservableList<E>> listener, AsyncObservableList<E> currentValue);

    protected abstract AsyncListExpressionHelper<E> removeListener(
            ChangeListener<? super AsyncObservableList<E>> listener, AsyncObservableList<E> currentValue);

    protected abstract AsyncListExpressionHelper<E> addListener(
            ListChangeListener<? super E> listener, AsyncObservableList<E> currentValue);

    protected abstract AsyncListExpressionHelper<E> removeListener(
            ListChangeListener<? super E> listener, AsyncObservableList<E> currentValue);

    protected abstract boolean validatesValue();

    protected abstract boolean containsBidirectionalBindingEndpoints();

    protected abstract void fireValueChangedEvent(AsyncObservableList<E> newValue);

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
            return new Generic<>(observable, this.listener, listener);
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
                ChangeListener<? super AsyncObservableList<E>> listener, AsyncObservableList<E> currentValue) {
            return new Generic<>(observable, currentValue, this.listener, listener);
        }

        @Override
        protected AsyncListExpressionHelper<E> removeListener(
                ChangeListener<? super AsyncObservableList<E>> listener, AsyncObservableList<E> currentValue) {
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
        protected void fireValueChangedEvent(AsyncObservableList<E> newValue) {
            listener.invalidated(observable);
        }

        @Override
        protected void fireValueChangedEvent(AsyncChange<? extends E> change) {
            listener.invalidated(observable);
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
            return new Generic<>(observable, currentValue, listener, this.listener);
        }

        @Override
        protected AsyncListExpressionHelper<E> removeListener(
                InvalidationListener listener, AsyncObservableList<E> currentValue) {
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
                ListChangeListener<? super E> listener, AsyncObservableList<E> currentValue) {
            return new Generic<>(observable, currentValue, this.listener, listener);
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
        protected void fireValueChangedEvent(AsyncObservableList<E> newValue) {
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
            return new Generic<>(observable, currentValue, listener, this.listener);
        }

        @Override
        protected AsyncListExpressionHelper<E> removeListener(
                InvalidationListener listener, AsyncObservableList<E> currentValue) {
            return this;
        }

        @Override
        protected AsyncListExpressionHelper<E> addListener(
                ChangeListener<? super AsyncObservableList<E>> listener, AsyncObservableList<E> currentValue) {
            return new Generic<>(observable, currentValue, listener, this.listener);
        }

        @Override
        protected AsyncListExpressionHelper<E> removeListener(
                ChangeListener<? super AsyncObservableList<E>> listener, AsyncObservableList<E> currentValue) {
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
        protected void fireValueChangedEvent(AsyncObservableList<E> newValue) {
            final AsyncObservableList<E> oldValue = currentValue;
            currentValue = newValue;
            if (currentValue != oldValue) {
                final int size = (currentValue == null) ? 0 : currentValue.size();
                final AsyncObservableList<E> value =
                    (oldValue == null)
                        ? FXAsyncCollections.emptyObservableList()
                        : FXAsyncCollections.unmodifiableObservableList(oldValue);
                final Change<E> change = new NonIterableChange.GenericAddRemoveChange<>(0, size, value, observable);
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
        private ChangeListener<? super AsyncObservableList<E>>[] changeListeners;
        private ListChangeListener<? super E>[] listChangeListeners;
        private int invalidationSize;
        private int changeSize;
        private int listChangeSize;
        private boolean locked;
        private AsyncObservableList<E> currentValue;

        private Generic(
                AsyncObservableListValue<E> observable,
                InvalidationListener listener0,
                InvalidationListener listener1) {
            super(observable);
            this.invalidationListeners = new InvalidationListener[] {listener0, listener1};
            this.invalidationSize = 2;
        }

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
                ListChangeListener<? super E> listener0,
                ListChangeListener<? super E> listener1) {
            super(observable);
            this.listChangeListeners = new ListChangeListener[] {listener0, listener1};
            this.listChangeSize = 2;
            this.currentValue = currentValue;
        }

        private Generic(
                AsyncObservableListValue<E> observable,
                AsyncObservableList<E> currentValue,
                InvalidationListener invalidationListener,
                ChangeListener<? super AsyncObservableList<E>> changeListener) {
            super(observable);
            this.invalidationListeners = new InvalidationListener[] {invalidationListener};
            this.invalidationSize = 1;
            this.changeListeners = new ChangeListener[] {changeListener};
            this.changeSize = 1;
            this.currentValue = currentValue;
        }

        private Generic(
                AsyncObservableListValue<E> observable,
                AsyncObservableList<E> currentValue,
                InvalidationListener invalidationListener,
                ListChangeListener<? super E> listChangeListener) {
            super(observable);
            this.invalidationListeners = new InvalidationListener[] {invalidationListener};
            this.invalidationSize = 1;
            this.listChangeListeners = new ListChangeListener[] {listChangeListener};
            this.listChangeSize = 1;
            this.currentValue = currentValue;
        }

        private Generic(
                AsyncObservableListValue<E> observable,
                AsyncObservableList<E> currentValue,
                ChangeListener<? super AsyncObservableList<E>> changeListener,
                ListChangeListener<? super E> listChangeListener) {
            super(observable);
            this.changeListeners = new ChangeListener[] {changeListener};
            this.changeSize = 1;
            this.listChangeListeners = new ListChangeListener[] {listChangeListener};
            this.listChangeSize = 1;
            this.currentValue = currentValue;
        }

        @Override
        protected AsyncListExpressionHelper<E> addListener(
                InvalidationListener listener, AsyncObservableList<E> currentValue) {
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
        protected AsyncListExpressionHelper<E> removeListener(
                InvalidationListener listener, AsyncObservableList<E> currentValue) {
            if (invalidationListeners != null) {
                for (int index = 0; index < invalidationSize; index++) {
                    if (invalidationListeners[index].equals(listener)) {
                        if (invalidationSize == 1) {
                            if ((changeSize == 1) && (listChangeSize == 0)) {
                                return new SingleChange<>(observable, currentValue, changeListeners[0]);
                            } else if ((changeSize == 0) && (listChangeSize == 1)) {
                                return new SingleListChange<>(observable, currentValue, listChangeListeners[0]);
                            }

                            invalidationListeners = null;
                            invalidationSize = 0;
                        } else if ((invalidationSize == 2) && (changeSize == 0) && (listChangeSize == 0)) {
                            return new SingleInvalidation<>(observable, invalidationListeners[1 - index]);
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
        protected AsyncListExpressionHelper<E> addListener(
                ChangeListener<? super AsyncObservableList<E>> listener, AsyncObservableList<E> currentValue) {
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
        protected AsyncListExpressionHelper<E> removeListener(
                ChangeListener<? super AsyncObservableList<E>> listener, AsyncObservableList<E> currentValue) {
            if (changeListeners != null) {
                for (int index = 0; index < changeSize; index++) {
                    if (changeListeners[index].equals(listener)) {
                        if (changeSize == 1) {
                            if ((invalidationSize == 1) && (listChangeSize == 0)) {
                                return new SingleInvalidation<>(observable, invalidationListeners[0]);
                            } else if ((invalidationSize == 0) && (listChangeSize == 1)) {
                                return new SingleListChange<>(observable, currentValue, listChangeListeners[0]);
                            }

                            changeListeners = null;
                            changeSize = 0;
                        } else if ((changeSize == 2) && (invalidationSize == 0) && (listChangeSize == 0)) {
                            return new SingleChange<>(observable, currentValue, changeListeners[1 - index]);
                        } else {
                            final int numMoved = changeSize - index - 1;
                            final ChangeListener<? super AsyncObservableList<E>>[] oldListeners = changeListeners;
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
        protected AsyncListExpressionHelper<E> addListener(
                ListChangeListener<? super E> listener, AsyncObservableList<E> currentValue) {
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
                this.currentValue = currentValue;
            }

            return this;
        }

        @Override
        protected AsyncListExpressionHelper<E> removeListener(
                ListChangeListener<? super E> listener, AsyncObservableList<E> currentValue) {
            if (listChangeListeners != null) {
                for (int index = 0; index < listChangeSize; index++) {
                    if (listChangeListeners[index].equals(listener)) {
                        if (listChangeSize == 1) {
                            if ((invalidationSize == 1) && (changeSize == 0)) {
                                return new SingleInvalidation<>(observable, invalidationListeners[0]);
                            } else if ((invalidationSize == 0) && (changeSize == 1)) {
                                return new SingleChange<>(observable, currentValue, changeListeners[0]);
                            }

                            listChangeListeners = null;
                            listChangeSize = 0;
                        } else if ((listChangeSize == 2) && (invalidationSize == 0) && (changeSize == 0)) {
                            return new SingleListChange<>(observable, currentValue, listChangeListeners[1 - index]);
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
        protected void fireValueChangedEvent(AsyncObservableList<E> newValue) {
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
                        change = new NonIterableChange.GenericAddRemoveChange<>(0, safeSize, safeOldValue, observable);
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
            final InvalidationListener[] curInvalidationList = invalidationListeners;
            final int curInvalidationSize = invalidationSize;
            final ChangeListener<? super AsyncObservableList<E>>[] curChangeList = changeListeners;
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

    }
}
