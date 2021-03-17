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
import javafx.collections.SetChangeListener;
import org.asyncfx.PublishSource;
import org.asyncfx.beans.SubInvalidationListener;

@PublishSource(
    module = "openjfx",
    licenses = {"openjfx", "intel-gpl-classpath-exception"}
)
public abstract class AsyncSetListenerHelper<E> extends ExpressionHelperBase {

    public static <E> AsyncSetListenerHelper<E> addListener(
            AsyncSetListenerHelper<E> helper, Observable observable, InvalidationListener listener) {
        if (listener == null) {
            throw new NullPointerException();
        }

        return (helper == null) ? new SingleInvalidation<>(observable, listener) : helper.addListener(listener);
    }

    public static <E> AsyncSetListenerHelper<E> removeListener(
            AsyncSetListenerHelper<E> helper, InvalidationListener listener) {
        if (listener == null) {
            throw new NullPointerException();
        }

        return (helper == null) ? null : helper.removeListener(listener);
    }

    public static <E> AsyncSetListenerHelper<E> addListener(
            AsyncSetListenerHelper<E> helper, Observable observable, SubInvalidationListener listener) {
        if (listener == null) {
            throw new NullPointerException();
        }

        return (helper == null) ? new SingleSubInvalidation<>(observable, listener) : helper.addListener(listener);
    }

    public static <E> AsyncSetListenerHelper<E> removeListener(
            AsyncSetListenerHelper<E> helper, SubInvalidationListener listener) {
        if (listener == null) {
            throw new NullPointerException();
        }

        return (helper == null) ? null : helper.removeListener(listener);
    }

    public static <E> AsyncSetListenerHelper<E> addListener(
            AsyncSetListenerHelper<E> helper, Observable observable, SetChangeListener<? super E> listener) {
        if (listener == null) {
            throw new NullPointerException();
        }

        return (helper == null) ? new SingleChange<>(observable, listener) : helper.addListener(listener);
    }

    public static <E> AsyncSetListenerHelper<E> removeListener(
            AsyncSetListenerHelper<E> helper, SetChangeListener<? super E> listener) {
        if (listener == null) {
            throw new NullPointerException();
        }

        return (helper == null) ? null : helper.removeListener(listener);
    }

    public static <E> void fireValueChangedEvent(
            AsyncSetListenerHelper<E> helper, SetChangeListener.Change<? extends E> change) {
        if (helper != null) {
            helper.fireValueChangedEvent(change);
        }
    }

    public static <E> boolean hasListeners(AsyncSetListenerHelper<E> helper) {
        return helper != null;
    }

    protected abstract AsyncSetListenerHelper<E> addListener(InvalidationListener listener);

    protected abstract AsyncSetListenerHelper<E> removeListener(InvalidationListener listener);

    protected abstract AsyncSetListenerHelper<E> addListener(SubInvalidationListener listener);

    protected abstract AsyncSetListenerHelper<E> removeListener(SubInvalidationListener listener);

    protected abstract AsyncSetListenerHelper<E> addListener(SetChangeListener<? super E> listener);

    protected abstract AsyncSetListenerHelper<E> removeListener(SetChangeListener<? super E> listener);

    protected abstract void fireValueChangedEvent(SetChangeListener.Change<? extends E> change);

    private static class SingleInvalidation<E> extends AsyncSetListenerHelper<E> {

        private final Observable observable;
        private final InvalidationListener listener;

        private SingleInvalidation(Observable observable, InvalidationListener listener) {
            this.observable = observable;
            this.listener = listener;
        }

        @Override
        protected AsyncSetListenerHelper<E> addListener(InvalidationListener listener) {
            return new Generic<>(observable, this.listener, listener);
        }

        @Override
        protected AsyncSetListenerHelper<E> removeListener(InvalidationListener listener) {
            if (this.listener == null) {
                return this;
            }

            return this.listener.equals(listener) ? null : this;
        }

        @Override
        protected AsyncSetListenerHelper<E> addListener(SubInvalidationListener listener) {
            return new Generic<>(observable, this.listener, listener);
        }

        @Override
        protected AsyncSetListenerHelper<E> removeListener(SubInvalidationListener listener) {
            return this;
        }

        @Override
        protected AsyncSetListenerHelper<E> addListener(SetChangeListener<? super E> listener) {
            return new Generic<>(observable, this.listener, listener);
        }

        @Override
        protected AsyncSetListenerHelper<E> removeListener(SetChangeListener<? super E> listener) {
            return this;
        }

        @Override
        protected void fireValueChangedEvent(SetChangeListener.Change<? extends E> change) {
            if (change == null) {
                return;
            }

            try {
                listener.invalidated(change.getSet());
            } catch (Exception e) {
                Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
            }
        }
    }

    private static class SingleSubInvalidation<E> extends AsyncSetListenerHelper<E> {

        private final Observable observable;
        private final SubInvalidationListener listener;

        private SingleSubInvalidation(Observable observable, SubInvalidationListener listener) {
            this.observable = observable;
            this.listener = listener;
        }

        @Override
        protected AsyncSetListenerHelper<E> addListener(InvalidationListener listener) {
            return new Generic<>(observable, this.listener, listener);
        }

        @Override
        protected AsyncSetListenerHelper<E> removeListener(InvalidationListener listener) {
            return this;
        }

        @Override
        protected AsyncSetListenerHelper<E> addListener(SubInvalidationListener listener) {
            return new Generic<>(observable, this.listener, listener);
        }

        @Override
        protected AsyncSetListenerHelper<E> removeListener(SubInvalidationListener listener) {
            if (this.listener == null) {
                return this;
            }

            return this.listener.equals(listener) ? null : this;
        }

        @Override
        protected AsyncSetListenerHelper<E> addListener(SetChangeListener<? super E> listener) {
            return new Generic<>(observable, this.listener, listener);
        }

        @Override
        protected AsyncSetListenerHelper<E> removeListener(SetChangeListener<? super E> listener) {
            return this;
        }

        @Override
        protected void fireValueChangedEvent(SetChangeListener.Change<? extends E> change) {
            try {
                listener.invalidated(observable, change == null);
            } catch (Exception e) {
                Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
            }
        }
    }

    private static class SingleChange<E> extends AsyncSetListenerHelper<E> {

        private final Observable observable;
        private final SetChangeListener<? super E> listener;

        private SingleChange(Observable observable, SetChangeListener<? super E> listener) {
            this.observable = observable;
            this.listener = listener;
        }

        @Override
        protected AsyncSetListenerHelper<E> addListener(InvalidationListener listener) {
            return new Generic<>(observable, listener, this.listener);
        }

        @Override
        protected AsyncSetListenerHelper<E> removeListener(InvalidationListener listener) {
            return this;
        }

        @Override
        protected AsyncSetListenerHelper<E> addListener(SubInvalidationListener listener) {
            return new Generic<>(observable, listener, this.listener);
        }

        @Override
        protected AsyncSetListenerHelper<E> removeListener(SubInvalidationListener listener) {
            return this;
        }

        @Override
        protected AsyncSetListenerHelper<E> addListener(SetChangeListener<? super E> listener) {
            return new Generic<>(observable, this.listener, listener);
        }

        @Override
        protected AsyncSetListenerHelper<E> removeListener(SetChangeListener<? super E> listener) {
            if (this.listener == null) {
                return this;
            }

            return this.listener.equals(listener) ? null : this;
        }

        @Override
        protected void fireValueChangedEvent(SetChangeListener.Change<? extends E> change) {
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

    private static class Generic<E> extends AsyncSetListenerHelper<E> {

        private final Observable observable;
        private InvalidationListener[] invalidationListeners;
        private SubInvalidationListener[] subInvalidationListeners;
        private SetChangeListener<? super E>[] changeListeners;
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
                SetChangeListener<? super E> changeListener) {
            this.observable = observable;
            this.invalidationListeners = new InvalidationListener[] {invalidationListener};
            this.invalidationSize = 1;
            this.changeListeners = new SetChangeListener[] {changeListener};
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
                SetChangeListener<? super E> changeListener) {
            this.observable = observable;
            this.subInvalidationListeners = new SubInvalidationListener[] {invalidationListener};
            this.subInvalidationSize = 1;
            this.changeListeners = new SetChangeListener[] {changeListener};
            this.changeSize = 1;
        }

        private Generic(
                Observable observable, SetChangeListener<? super E> listener0, SetChangeListener<? super E> listener1) {
            this.observable = observable;
            this.changeListeners = new SetChangeListener[] {listener0, listener1};
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
        protected AsyncSetListenerHelper<E> removeListener(InvalidationListener listener) {
            if (invalidationListeners != null) {
                for (int index = 0; index < invalidationSize; index++) {
                    if (invalidationListeners[index].equals(listener)) {
                        AsyncSetListenerHelper<E> helper = getSingleListenerHelper(index, 1, 0, 0);
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
        protected AsyncSetListenerHelper<E> removeListener(SubInvalidationListener listener) {
            if (subInvalidationListeners != null) {
                for (int index = 0; index < subInvalidationSize; index++) {
                    if (subInvalidationListeners[index].equals(listener)) {
                        AsyncSetListenerHelper<E> helper = getSingleListenerHelper(index, 0, 1, 0);
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
        protected AsyncSetListenerHelper<E> addListener(SetChangeListener<? super E> listener) {
            if (changeListeners == null) {
                changeListeners = new SetChangeListener[] {listener};
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
        protected AsyncSetListenerHelper<E> removeListener(SetChangeListener<? super E> listener) {
            if (changeListeners != null) {
                for (int index = 0; index < changeSize; index++) {
                    if (changeListeners[index].equals(listener)) {
                        AsyncSetListenerHelper<E> helper = getSingleListenerHelper(index, 0, 0, 1);
                        if (helper != null) {
                            return helper;
                        }

                        if (changeSize == 1) {
                            changeListeners = null;
                            changeSize = 0;
                        } else {
                            final int numMoved = changeSize - index - 1;
                            final SetChangeListener<? super E>[] oldListeners = changeListeners;
                            if (locked) {
                                changeListeners = new SetChangeListener[changeListeners.length];
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
        protected void fireValueChangedEvent(SetChangeListener.Change<? extends E> change) {
            final InvalidationListener[] curInvalidationList = invalidationListeners;
            final int curInvalidationSize = invalidationSize;
            final SubInvalidationListener[] curSubInvalidationList = subInvalidationListeners;
            final int curSubInvalidationSize = subInvalidationSize;
            final SetChangeListener<? super E>[] curChangeList = changeListeners;
            final int curChangeSize = changeSize;

            try {
                locked = true;

                if (change != null) {
                    for (int i = 0; i < curInvalidationSize; i++) {
                        try {
                            curInvalidationList[i].invalidated(change.getSet());
                        } catch (Exception e) {
                            Thread.currentThread()
                                .getUncaughtExceptionHandler()
                                .uncaughtException(Thread.currentThread(), e);
                        }
                    }

                    for (int i = 0; i < curChangeSize; i++) {
                        try {
                            curChangeList[i].onChanged(change);
                        } catch (Exception e) {
                            Thread.currentThread()
                                .getUncaughtExceptionHandler()
                                .uncaughtException(Thread.currentThread(), e);
                        }
                    }
                }

                for (int i = 0; i < curSubInvalidationSize; i++) {
                    try {
                        curSubInvalidationList[i].invalidated(observable, change == null);
                    } catch (Exception e) {
                        Thread.currentThread()
                            .getUncaughtExceptionHandler()
                            .uncaughtException(Thread.currentThread(), e);
                    }
                }

            } finally {
                locked = false;
            }
        }

        private AsyncSetListenerHelper<E> getSingleListenerHelper(
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
