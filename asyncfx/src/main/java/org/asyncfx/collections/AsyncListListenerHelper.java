/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.collections;

import com.sun.javafx.binding.ExpressionHelperBase;
import java.util.Arrays;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import org.asyncfx.PublishSource;
import org.asyncfx.beans.SubInvalidationListener;

@PublishSource(
    module = "openjfx",
    licenses = {"openjfx", "intel-gpl-classpath-exception"}
)
abstract class AsyncListListenerHelper<E> extends ExpressionHelperBase {

    public static <E> AsyncListListenerHelper<E> addListener(
            AsyncListListenerHelper<E> helper, Observable observable, InvalidationListener listener) {
        if (listener == null) {
            throw new NullPointerException();
        }

        return (helper == null) ? new SingleInvalidation<>(observable, listener) : helper.addListener(listener);
    }

    public static <E> AsyncListListenerHelper<E> removeListener(
            AsyncListListenerHelper<E> helper, InvalidationListener listener) {
        if (listener == null) {
            throw new NullPointerException();
        }

        return (helper == null) ? null : helper.removeListener(listener);
    }

    public static <E> AsyncListListenerHelper<E> addListener(
            AsyncListListenerHelper<E> helper, Observable observable, SubInvalidationListener listener) {
        if (listener == null) {
            throw new NullPointerException();
        }

        return (helper == null) ? new SingleSubInvalidation<>(observable, listener) : helper.addListener(listener);
    }

    public static <E> AsyncListListenerHelper<E> removeListener(
            AsyncListListenerHelper<E> helper, SubInvalidationListener listener) {
        if (listener == null) {
            throw new NullPointerException();
        }

        return (helper == null) ? null : helper.removeListener(listener);
    }

    public static <E> AsyncListListenerHelper<E> addListener(
            AsyncListListenerHelper<E> helper, Observable observable, ListChangeListener<? super E> listener) {
        if (listener == null) {
            throw new NullPointerException();
        }

        return (helper == null) ? new SingleChange<>(observable, listener) : helper.addListener(listener);
    }

    public static <E> AsyncListListenerHelper<E> removeListener(
            AsyncListListenerHelper<E> helper, ListChangeListener<? super E> listener) {
        if (listener == null) {
            throw new NullPointerException();
        }

        return (helper == null) ? null : helper.removeListener(listener);
    }

    public static <E> void fireValueChangedEvent(
            AsyncListListenerHelper<E> helper, ListChangeListener.Change<? extends E> change, boolean finalChange) {
        if (helper != null) {
            if (change != null) {
                change.reset();
            }

            helper.fireValueChangedEvent(change, finalChange);
        }
    }

    static <E> boolean hasListeners(AsyncListListenerHelper<E> helper) {
        return helper != null;
    }

    protected abstract AsyncListListenerHelper<E> addListener(InvalidationListener listener);

    protected abstract AsyncListListenerHelper<E> removeListener(InvalidationListener listener);

    protected abstract AsyncListListenerHelper<E> addListener(SubInvalidationListener listener);

    protected abstract AsyncListListenerHelper<E> removeListener(SubInvalidationListener listener);

    protected abstract AsyncListListenerHelper<E> addListener(ListChangeListener<? super E> listener);

    protected abstract AsyncListListenerHelper<E> removeListener(ListChangeListener<? super E> listener);

    protected abstract void fireValueChangedEvent(ListChangeListener.Change<? extends E> change, boolean finalChange);

    private static class SingleInvalidation<E> extends AsyncListListenerHelper<E> {

        private final Observable observable;
        private final InvalidationListener listener;

        private SingleInvalidation(Observable observable, InvalidationListener listener) {
            this.observable = observable;
            this.listener = listener;
        }

        @Override
        protected AsyncListListenerHelper<E> addListener(InvalidationListener listener) {
            return new Generic<>(observable, this.listener, listener);
        }

        @Override
        protected AsyncListListenerHelper<E> removeListener(InvalidationListener listener) {
            if (this.listener == null) {
                return this;
            }

            return this.listener.equals(listener) ? null : this;
        }

        @Override
        protected AsyncListListenerHelper<E> addListener(SubInvalidationListener listener) {
            return new Generic<>(observable, this.listener, listener);
        }

        @Override
        protected AsyncListListenerHelper<E> removeListener(SubInvalidationListener listener) {
            return this;
        }

        @Override
        protected AsyncListListenerHelper<E> addListener(ListChangeListener<? super E> listener) {
            return new Generic<>(observable, this.listener, listener);
        }

        @Override
        protected AsyncListListenerHelper<E> removeListener(ListChangeListener<? super E> listener) {
            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        protected void fireValueChangedEvent(ListChangeListener.Change<? extends E> change, boolean finalChange) {
            if (change == null) {
                return;
            }

            try {
                if (change instanceof UnsafeListAccess) {
                    listener.invalidated(((UnsafeListAccess<? extends E>)change).getListUnsafe());
                } else {
                    throw new IllegalArgumentException("Unsupported change type.");
                }
            } catch (Exception e) {
                Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
            }
        }
    }

    private static class SingleSubInvalidation<E> extends AsyncListListenerHelper<E> {

        private final Observable observable;
        private final SubInvalidationListener listener;

        private SingleSubInvalidation(Observable observable, SubInvalidationListener listener) {
            this.observable = observable;
            this.listener = listener;
        }

        @Override
        protected AsyncListListenerHelper<E> addListener(InvalidationListener listener) {
            return new Generic<>(observable, this.listener, listener);
        }

        @Override
        protected AsyncListListenerHelper<E> removeListener(InvalidationListener listener) {
            return this;
        }

        @Override
        protected AsyncListListenerHelper<E> addListener(SubInvalidationListener listener) {
            return new Generic<>(observable, this.listener, listener);
        }

        @Override
        protected AsyncListListenerHelper<E> removeListener(SubInvalidationListener listener) {
            if (this.listener == null) {
                return this;
            }

            return this.listener.equals(listener) ? null : this;
        }

        @Override
        protected AsyncListListenerHelper<E> addListener(ListChangeListener<? super E> listener) {
            return new Generic<>(observable, this.listener, listener);
        }

        @Override
        protected AsyncListListenerHelper<E> removeListener(ListChangeListener<? super E> listener) {
            return this;
        }

        @Override
        protected void fireValueChangedEvent(ListChangeListener.Change<? extends E> change, boolean finalChange) {
            try {
                listener.invalidated(observable, finalChange);
            } catch (Exception e) {
                Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
            }
        }
    }

    private static class SingleChange<E> extends AsyncListListenerHelper<E> {

        private final Observable observable;
        private final ListChangeListener<? super E> listener;

        private SingleChange(Observable observableValue, ListChangeListener<? super E> listener) {
            this.observable = observableValue;
            this.listener = listener;
        }

        @Override
        protected AsyncListListenerHelper<E> addListener(InvalidationListener listener) {
            return new Generic<>(observable, listener, this.listener);
        }

        @Override
        protected AsyncListListenerHelper<E> removeListener(InvalidationListener listener) {
            return this;
        }

        @Override
        protected AsyncListListenerHelper<E> addListener(SubInvalidationListener listener) {
            return new Generic<>(observable, listener, this.listener);
        }

        @Override
        protected AsyncListListenerHelper<E> removeListener(SubInvalidationListener listener) {
            return this;
        }

        @Override
        protected AsyncListListenerHelper<E> addListener(ListChangeListener<? super E> listener) {
            return new Generic<>(observable, this.listener, listener);
        }

        @Override
        protected AsyncListListenerHelper<E> removeListener(ListChangeListener<? super E> listener) {
            if (this.listener == null) {
                return this;
            }

            return this.listener.equals(listener) ? null : this;
        }

        @Override
        protected void fireValueChangedEvent(ListChangeListener.Change<? extends E> change, boolean finalChange) {
            if (change == null) {
                return;
            }

            try {
                listener.onChanged(change);
            } catch (Exception e) {
                Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
            }
        }
    }

    private static class Generic<E> extends AsyncListListenerHelper<E> {

        private final Observable observable;
        private InvalidationListener[] invalidationListeners;
        private SubInvalidationListener[] subInvalidationListeners;
        private ListChangeListener<? super E>[] changeListeners;
        private int invalidationSize;
        private int subInvalidationSize;
        private int changeSize;
        private boolean locked;

        private Generic(Observable observable, InvalidationListener listener0, InvalidationListener listener1) {
            this.observable = observable;
            this.invalidationListeners = new InvalidationListener[] {listener0, listener1};
            this.invalidationSize = 2;
        }

        private Generic(
                Observable observable,
                InvalidationListener invalidationListener,
                ListChangeListener<? super E> changeListener) {
            this.observable = observable;
            this.invalidationListeners = new InvalidationListener[] {invalidationListener};
            this.invalidationSize = 1;
            this.changeListeners = new ListChangeListener[] {changeListener};
            this.changeSize = 1;
        }

        private Generic(
                Observable observable,
                InvalidationListener invalidationListener,
                SubInvalidationListener subInvalidationListener) {
            this.observable = observable;
            this.invalidationListeners = new InvalidationListener[] {invalidationListener};
            this.invalidationSize = 1;
            this.subInvalidationListeners = new SubInvalidationListener[] {subInvalidationListener};
            this.subInvalidationSize = 1;
        }

        private Generic(Observable observable, SubInvalidationListener listener0, SubInvalidationListener listener1) {
            this.observable = observable;
            this.subInvalidationListeners = new SubInvalidationListener[] {listener0, listener1};
            this.subInvalidationSize = 2;
        }

        private Generic(Observable observable, SubInvalidationListener listener0, InvalidationListener listener1) {
            this.observable = observable;
            this.subInvalidationListeners = new SubInvalidationListener[] {listener0};
            this.subInvalidationSize = 1;
            this.invalidationListeners = new InvalidationListener[] {listener1};
            this.invalidationSize = 1;
        }

        private Generic(
                Observable observable,
                SubInvalidationListener invalidationListener,
                ListChangeListener<? super E> changeListener) {
            this.observable = observable;
            this.subInvalidationListeners = new SubInvalidationListener[] {invalidationListener};
            this.subInvalidationSize = 1;
            this.changeListeners = new ListChangeListener[] {changeListener};
            this.changeSize = 1;
        }

        private Generic(
                Observable observable,
                ListChangeListener<? super E> listener0,
                ListChangeListener<? super E> listener1) {
            this.observable = observable;
            this.changeListeners = new ListChangeListener[] {listener0, listener1};
            this.changeSize = 2;
        }

        @Override
        protected Generic<E> addListener(InvalidationListener listener) {
            if (invalidationListeners == null) {
                invalidationListeners = new InvalidationListener[] {listener};
                invalidationSize = 1;
            } else {
                final int oldCapacity = invalidationListeners.length;
                if (locked) {
                    final int newCapacity = (invalidationSize < oldCapacity) ? oldCapacity : (oldCapacity * 3) / 2 + 1;
                    invalidationListeners = Arrays.copyOf(invalidationListeners, newCapacity);
                } else if (invalidationSize == oldCapacity) {
                    invalidationSize = ExpressionHelperBase.trim(invalidationSize, invalidationListeners);
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
        protected AsyncListListenerHelper<E> removeListener(InvalidationListener listener) {
            if (invalidationListeners != null) {
                for (int index = 0; index < invalidationSize; index++) {
                    if (invalidationListeners[index].equals(listener)) {
                        AsyncListListenerHelper<E> helper = getSingleListenerHelper(index, 1, 0, 0);
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
        protected Generic<E> addListener(SubInvalidationListener listener) {
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
                    subInvalidationSize = ExpressionHelperBase.trim(subInvalidationSize, subInvalidationListeners);
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
        protected AsyncListListenerHelper<E> removeListener(SubInvalidationListener listener) {
            if (subInvalidationListeners != null) {
                for (int index = 0; index < subInvalidationSize; index++) {
                    if (subInvalidationListeners[index].equals(listener)) {
                        AsyncListListenerHelper<E> helper = getSingleListenerHelper(index, 0, 1, 0);
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
                                System.arraycopy(oldListeners, 0, subInvalidationListeners, 0, index);
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
        protected AsyncListListenerHelper<E> addListener(ListChangeListener<? super E> listener) {
            if (changeListeners == null) {
                changeListeners = new ListChangeListener[] {listener};
                changeSize = 1;
            } else {
                final int oldCapacity = changeListeners.length;
                if (locked) {
                    final int newCapacity = (changeSize < oldCapacity) ? oldCapacity : (oldCapacity * 3) / 2 + 1;
                    changeListeners = Arrays.copyOf(changeListeners, newCapacity);
                } else if (changeSize == oldCapacity) {
                    changeSize = ExpressionHelperBase.trim(changeSize, changeListeners);
                    if (changeSize == oldCapacity) {
                        final int newCapacity = (oldCapacity * 3) / 2 + 1;
                        changeListeners = Arrays.copyOf(changeListeners, newCapacity);
                    }
                }

                changeListeners[changeSize++] = listener;
            }

            return this;
        }

        @Override
        protected AsyncListListenerHelper<E> removeListener(ListChangeListener<? super E> listener) {
            if (changeListeners != null) {
                for (int index = 0; index < changeSize; index++) {
                    if (changeListeners[index].equals(listener)) {
                        AsyncListListenerHelper<E> helper = getSingleListenerHelper(index, 0, 0, 1);
                        if (helper != null) {
                            return helper;
                        }

                        if (changeSize == 1) {
                            changeListeners = null;
                            changeSize = 0;
                        } else {
                            final int numMoved = changeSize - index - 1;
                            final ListChangeListener<? super E>[] oldListeners = changeListeners;
                            if (locked) {
                                changeListeners = new ListChangeListener[changeListeners.length];
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
        @SuppressWarnings("unchecked")
        protected void fireValueChangedEvent(ListChangeListener.Change<? extends E> change, boolean finalChange) {
            final InvalidationListener[] curInvalidationList = invalidationListeners;
            final int curInvalidationSize = invalidationSize;
            final SubInvalidationListener[] curSubInvalidationList = subInvalidationListeners;
            final int curSubInvalidationSize = subInvalidationSize;
            final ListChangeListener<? super E>[] curChangeList = changeListeners;
            final int curChangeSize = changeSize;

            try {
                locked = true;

                if (change != null) {
                    ObservableList<? extends E> list;
                    if (change instanceof UnsafeListAccess) {
                        list = ((UnsafeListAccess<? extends E>)change).getListUnsafe();
                    } else {
                        throw new IllegalArgumentException("Unsupported change type.");
                    }

                    for (int i = 0; i < curInvalidationSize; i++) {
                        try {
                            curInvalidationList[i].invalidated(list);
                        } catch (Exception e) {
                            Thread.currentThread()
                                .getUncaughtExceptionHandler()
                                .uncaughtException(Thread.currentThread(), e);
                        }
                    }

                    for (int i = 0; i < curChangeSize; i++) {
                        change.reset();
                        try {
                            curChangeList[i].onChanged(change);
                        } catch (Exception e) {
                            Thread.currentThread()
                                .getUncaughtExceptionHandler()
                                .uncaughtException(Thread.currentThread(), e);
                        }
                    }
                }

                if (change == null || finalChange) {
                    for (int i = 0; i < curSubInvalidationSize; i++) {
                        try {
                            curSubInvalidationList[i].invalidated(observable, change == null);
                        } catch (Exception e) {
                            Thread.currentThread()
                                .getUncaughtExceptionHandler()
                                .uncaughtException(Thread.currentThread(), e);
                        }
                    }
                }
            } finally {
                locked = false;
            }
        }

        private AsyncListListenerHelper<E> getSingleListenerHelper(
                int index, int removeInvalidation, int removeSubInvalidation, int removeChange) {
            if (invalidationSize - removeInvalidation == 1
                    && subInvalidationSize - removeSubInvalidation == 0
                    && changeSize - removeChange == 0) {
                return new SingleInvalidation<>(
                    observable, invalidationListeners[invalidationSize == 2 ? 1 - index : 0]);
            }

            if (invalidationSize - removeInvalidation == 0
                    && subInvalidationSize - removeSubInvalidation == 1
                    && changeSize - removeChange == 0) {
                return new SingleSubInvalidation<>(
                    observable, subInvalidationListeners[subInvalidationSize == 2 ? 1 - index : 0]);
            }

            if (invalidationSize - removeInvalidation == 0
                    && subInvalidationSize - removeSubInvalidation == 0
                    && changeSize - removeChange == 1) {
                return new SingleChange<>(observable, changeListeners[changeSize == 2 ? 1 - index : 0]);
            }

            return null;
        }
    }

}
