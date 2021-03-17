/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.beans.binding;

import com.sun.javafx.binding.ExpressionHelperBase;
import java.util.Arrays;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import org.asyncfx.PublishSource;
import org.asyncfx.beans.SubInvalidationListener;
import org.asyncfx.beans.value.SubChangeListener;

@PublishSource(
    module = "openjfx",
    licenses = {"openjfx", "intel-gpl-classpath-exception"}
)
public abstract class AsyncExpressionHelper<T> extends ExpressionHelperBase {

    public static <T> AsyncExpressionHelper<T> addListener(
            AsyncExpressionHelper<T> helper,
            ObservableValue<T> observable,
            T currentValue, // unused, but indicates that the observable must be validated
            InvalidationListener listener) {
        if ((observable == null) || (listener == null)) {
            throw new NullPointerException();
        }

        return (helper == null) ? new SingleInvalidation<>(observable, listener) : helper.addListener(listener);
    }

    public static <T> AsyncExpressionHelper<T> removeListener(
            AsyncExpressionHelper<T> helper, InvalidationListener listener) {
        if (listener == null) {
            throw new NullPointerException();
        }

        return (helper == null) ? null : helper.removeListener(listener);
    }

    public static <T> AsyncExpressionHelper<T> addListener(
            AsyncExpressionHelper<T> helper,
            ObservableValue<T> observable,
            T currentValue, // unused, but indicates that the observable must be validated
            SubInvalidationListener listener) {
        if ((observable == null) || (listener == null)) {
            throw new NullPointerException();
        }

        return (helper == null) ? new SingleSubInvalidation<>(observable, listener) : helper.addListener(listener);
    }

    public static <T> AsyncExpressionHelper<T> removeListener(
            AsyncExpressionHelper<T> helper, SubInvalidationListener listener) {
        if (listener == null) {
            throw new NullPointerException();
        }

        return (helper == null) ? null : helper.removeListener(listener);
    }

    public static <T> AsyncExpressionHelper<T> addListener(
            AsyncExpressionHelper<T> helper,
            ObservableValue<T> observable,
            T currentValue,
            ChangeListener<? super T> listener) {
        if ((observable == null) || (listener == null)) {
            throw new NullPointerException();
        }

        return (helper == null)
            ? new SingleChange<>(observable, currentValue, listener)
            : helper.addListener(listener, currentValue);
    }

    public static <T> AsyncExpressionHelper<T> removeListener(
            AsyncExpressionHelper<T> helper, ChangeListener<? super T> listener) {
        if (listener == null) {
            throw new NullPointerException();
        }

        return (helper == null) ? null : helper.removeListener(listener);
    }

    public static <T> AsyncExpressionHelper<T> addListener(
            AsyncExpressionHelper<T> helper,
            ObservableValue<T> observable,
            T currentValue,
            SubChangeListener listener) {
        if ((observable == null) || (listener == null)) {
            throw new NullPointerException();
        }

        return (helper == null)
            ? new SingleSubChange<>(observable, currentValue, listener)
            : helper.addListener(listener, currentValue);
    }

    public static <T> AsyncExpressionHelper<T> removeListener(
            AsyncExpressionHelper<T> helper, SubChangeListener listener) {
        if (listener == null) {
            throw new NullPointerException();
        }

        return (helper == null) ? null : helper.removeListener(listener);
    }

    public static <T> boolean validatesValue(AsyncExpressionHelper<T> helper) {
        if (helper != null) {
            return helper.validatesValue();
        }

        return false;
    }

    public static <T> boolean containsBidirectionalBindingEndpoints(AsyncExpressionHelper<T> helper) {
        if (helper != null) {
            return helper.containsBidirectionalBindingEndpoints();
        }

        return false;
    }

    public static <T> void fireValueChangedEvent(AsyncExpressionHelper<T> helper, T currentValue, boolean subChange) {
        if (helper != null) {
            helper.fireValueChangedEvent(currentValue, subChange);
        }
    }

    protected final ObservableValue<T> observable;

    private AsyncExpressionHelper(ObservableValue<T> observable) {
        this.observable = observable;
    }

    protected abstract AsyncExpressionHelper<T> addListener(InvalidationListener listener);

    protected abstract AsyncExpressionHelper<T> removeListener(InvalidationListener listener);

    protected abstract AsyncExpressionHelper<T> addListener(SubInvalidationListener listener);

    protected abstract AsyncExpressionHelper<T> removeListener(SubInvalidationListener listener);

    protected abstract AsyncExpressionHelper<T> addListener(ChangeListener<? super T> listener, T currentValue);

    protected abstract AsyncExpressionHelper<T> removeListener(ChangeListener<? super T> listener);

    protected abstract AsyncExpressionHelper<T> addListener(SubChangeListener listener, T currentValue);

    protected abstract AsyncExpressionHelper<T> removeListener(SubChangeListener listener);

    protected abstract boolean validatesValue();

    protected abstract boolean containsBidirectionalBindingEndpoints();

    protected abstract void fireValueChangedEvent(T newValue, boolean subChange);

    static class SingleInvalidation<T> extends AsyncExpressionHelper<T> {

        private final InvalidationListener listener;

        private SingleInvalidation(ObservableValue<T> expression, InvalidationListener listener) {
            super(expression);
            this.listener = listener;
        }

        @Override
        protected AsyncExpressionHelper<T> addListener(InvalidationListener listener) {
            return new Generic<>(observable, null, this.listener, listener);
        }

        @Override
        protected AsyncExpressionHelper<T> removeListener(InvalidationListener listener) {
            if (this.listener == null) {
                return this;
            }

            return this.listener.equals(listener) ? null : this;
        }

        @Override
        protected AsyncExpressionHelper<T> addListener(SubInvalidationListener listener) {
            return new Generic<>(observable, null).addListener(this.listener).addListener(listener);
        }

        @Override
        protected AsyncExpressionHelper<T> removeListener(SubInvalidationListener listener) {
            return this;
        }

        @Override
        protected AsyncExpressionHelper<T> addListener(ChangeListener<? super T> listener, T currentValue) {
            return new Generic<>(observable, currentValue)
                .addListener(this.listener)
                .addListener(listener, currentValue);
        }

        @Override
        protected AsyncExpressionHelper<T> removeListener(ChangeListener<? super T> listener) {
            return this;
        }

        @Override
        protected AsyncExpressionHelper<T> addListener(SubChangeListener listener, T currentValue) {
            return new Generic<>(observable, currentValue)
                .addListener(this.listener)
                .addListener(listener, currentValue);
        }

        @Override
        protected AsyncExpressionHelper<T> removeListener(SubChangeListener listener) {
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
        protected void fireValueChangedEvent(T newValue, boolean subChange) {
            if (subChange) {
                return;
            }

            try {
                listener.invalidated(observable);
            } catch (Exception e) {
                Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
            }
        }
    }

    static class SingleSubInvalidation<T> extends AsyncExpressionHelper<T> {

        private final SubInvalidationListener listener;

        private SingleSubInvalidation(ObservableValue<T> expression, SubInvalidationListener listener) {
            super(expression);
            this.listener = listener;
        }

        @Override
        protected AsyncExpressionHelper<T> addListener(InvalidationListener listener) {
            return new Generic<>(observable, null).addListener(this.listener).addListener(listener);
        }

        @Override
        protected AsyncExpressionHelper<T> removeListener(InvalidationListener listener) {
            return this;
        }

        @Override
        protected AsyncExpressionHelper<T> addListener(SubInvalidationListener listener) {
            return new Generic<>(observable, null, this.listener, listener);
        }

        @Override
        protected AsyncExpressionHelper<T> removeListener(SubInvalidationListener listener) {
            if (this.listener == null) {
                return this;
            }

            return this.listener.equals(listener) ? null : this;
        }

        @Override
        protected AsyncExpressionHelper<T> addListener(ChangeListener<? super T> listener, T currentValue) {
            return new Generic<>(observable, currentValue)
                .addListener(this.listener)
                .addListener(listener, currentValue);
        }

        @Override
        protected AsyncExpressionHelper<T> removeListener(ChangeListener<? super T> listener) {
            return this;
        }

        @Override
        protected AsyncExpressionHelper<T> addListener(SubChangeListener listener, T currentValue) {
            return new Generic<>(observable, currentValue)
                .addListener(this.listener)
                .addListener(listener, currentValue);
        }

        @Override
        protected AsyncExpressionHelper<T> removeListener(SubChangeListener listener) {
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
        protected void fireValueChangedEvent(T newValue, boolean subChange) {
            try {
                listener.invalidated(observable, subChange);
            } catch (Exception e) {
                Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
            }
        }
    }

    static class SingleChange<T> extends AsyncExpressionHelper<T> {

        private final ChangeListener<? super T> listener;
        private T currentValue;

        private SingleChange(ObservableValue<T> observable, T currentValue, ChangeListener<? super T> listener) {
            super(observable);
            this.listener = listener;
            this.currentValue = currentValue;
        }

        @Override
        protected AsyncExpressionHelper<T> addListener(InvalidationListener listener) {
            return new Generic<>(observable, currentValue)
                .addListener(this.listener, currentValue)
                .addListener(listener);
        }

        @Override
        protected AsyncExpressionHelper<T> removeListener(InvalidationListener listener) {
            return this;
        }

        @Override
        protected AsyncExpressionHelper<T> addListener(SubInvalidationListener listener) {
            return new Generic<>(observable, currentValue)
                .addListener(this.listener, currentValue)
                .addListener(listener);
        }

        @Override
        protected AsyncExpressionHelper<T> removeListener(SubInvalidationListener listener) {
            return this;
        }

        @Override
        protected AsyncExpressionHelper<T> addListener(ChangeListener<? super T> listener, T currentValue) {
            return new Generic<>(observable, currentValue, this.listener, listener);
        }

        @Override
        protected AsyncExpressionHelper<T> removeListener(ChangeListener<? super T> listener) {
            if (this.listener == null) {
                return this;
            }

            return this.listener.equals(listener) ? null : this;
        }

        @Override
        protected AsyncExpressionHelper<T> addListener(SubChangeListener listener, T currentValue) {
            return new Generic<>(observable, currentValue)
                .addListener(this.listener, currentValue)
                .addListener(listener, currentValue);
        }

        @Override
        protected AsyncExpressionHelper<T> removeListener(SubChangeListener listener) {
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
        protected void fireValueChangedEvent(T newValue, boolean subChange) {
            if (subChange) {
                return;
            }

            final T oldValue = currentValue;
            currentValue = newValue;
            final boolean changed = (currentValue == null) ? (oldValue != null) : !currentValue.equals(oldValue);
            if (changed) {
                try {
                    listener.changed(observable, oldValue, currentValue);
                } catch (Exception e) {
                    Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
                }
            }
        }
    }

    static class SingleSubChange<T> extends AsyncExpressionHelper<T> {

        private final SubChangeListener listener;
        private T currentValue;

        private SingleSubChange(ObservableValue<T> observable, T currentValue, SubChangeListener listener) {
            super(observable);
            this.listener = listener;
            this.currentValue = currentValue;
        }

        @Override
        protected AsyncExpressionHelper<T> addListener(InvalidationListener listener) {
            return new Generic<>(observable, currentValue)
                .addListener(this.listener, currentValue)
                .addListener(listener);
        }

        @Override
        protected AsyncExpressionHelper<T> removeListener(InvalidationListener listener) {
            return this;
        }

        @Override
        protected AsyncExpressionHelper<T> addListener(SubInvalidationListener listener) {
            return new Generic<>(observable, currentValue)
                .addListener(this.listener, currentValue)
                .addListener(listener);
        }

        @Override
        protected AsyncExpressionHelper<T> removeListener(SubInvalidationListener listener) {
            return this;
        }

        @Override
        protected AsyncExpressionHelper<T> addListener(ChangeListener<? super T> listener, T currentValue) {
            return new Generic<>(observable, currentValue)
                .addListener(this.listener, currentValue)
                .addListener(listener, currentValue);
        }

        @Override
        protected AsyncExpressionHelper<T> removeListener(ChangeListener<? super T> listener) {
            return this;
        }

        @Override
        protected AsyncExpressionHelper<T> addListener(SubChangeListener listener, T currentValue) {
            return new Generic<>(observable, currentValue, this.listener, listener);
        }

        @Override
        protected AsyncExpressionHelper<T> removeListener(SubChangeListener listener) {
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
        protected void fireValueChangedEvent(T newValue, boolean subChange) {
            final T oldValue = currentValue;
            currentValue = newValue;
            try {
                final boolean changed =
                    subChange || ((currentValue == null) ? (oldValue != null) : !currentValue.equals(oldValue));

                if (changed) {
                    listener.changed(observable, oldValue, currentValue, subChange);
                }
            } catch (Exception e) {
                Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
            }
        }
    }

    static class Generic<T> extends AsyncExpressionHelper<T> {

        private InvalidationListener[] invalidationListeners;
        private SubInvalidationListener[] subInvalidationListeners;
        private ChangeListener<? super T>[] changeListeners;
        private SubChangeListener[] subChangeListeners;
        private int invalidationSize;
        private int subInvalidationSize;
        private int changeSize;
        private int subChangeSize;
        protected T currentValue;

        private Generic(ObservableValue<T> observable, T currentValue) {
            super(observable);
            this.currentValue = currentValue;
        }

        private Generic(
                ObservableValue<T> observable,
                T currentValue,
                InvalidationListener listener0,
                InvalidationListener listener1) {
            super(observable);
            this.invalidationListeners = new InvalidationListener[] {listener0, listener1};
            this.invalidationSize = 2;
            this.currentValue = currentValue;
        }

        private Generic(
                ObservableValue<T> observable,
                T currentValue,
                SubInvalidationListener listener0,
                SubInvalidationListener listener1) {
            super(observable);
            this.subInvalidationListeners = new SubInvalidationListener[] {listener0, listener1};
            this.subInvalidationSize = 2;
            this.currentValue = currentValue;
        }

        private Generic(
                ObservableValue<T> observable,
                T currentValue,
                ChangeListener<? super T> listener0,
                ChangeListener<? super T> listener1) {
            super(observable);
            this.changeListeners = new ChangeListener[] {listener0, listener1};
            this.changeSize = 2;
            this.currentValue = currentValue;
        }

        private Generic(
                ObservableValue<T> observable,
                T currentValue,
                SubChangeListener listener0,
                SubChangeListener listener1) {
            super(observable);
            this.subChangeListeners = new SubChangeListener[] {listener0, listener1};
            this.subChangeSize = 2;
            this.currentValue = currentValue;
        }

        private Generic(ObservableValue<T> observable, T currentValue, Generic<T> source) {
            super(observable);
            this.currentValue = currentValue;

            if (source.invalidationListeners != null) {
                invalidationListeners = source.invalidationListeners;
                invalidationSize = source.invalidationSize;
            }

            if (source.subInvalidationListeners != null) {
                subInvalidationListeners = source.subInvalidationListeners;
                subInvalidationSize = source.subInvalidationSize;
            }

            if (source.changeListeners != null) {
                changeListeners = source.changeListeners;
                changeSize = source.changeSize;
            }

            if (source.subChangeListeners != null) {
                subChangeListeners = source.subChangeListeners;
                subChangeSize = source.subChangeSize;
            }
        }

        @Override
        protected Generic<T> addListener(InvalidationListener listener) {
            Generic<T> helper = new Generic<>(observable, currentValue, this);

            if (helper.invalidationListeners == null) {
                helper.invalidationListeners = new InvalidationListener[] {listener};
                helper.invalidationSize = 1;
            } else {
                helper.invalidationListeners = Arrays.copyOf(helper.invalidationListeners, helper.invalidationSize + 1);
                helper.invalidationSize = trim(helper.invalidationSize, helper.invalidationListeners);
                helper.invalidationListeners[helper.invalidationSize++] = listener;
            }

            return helper;
        }

        @Override
        protected AsyncExpressionHelper<T> removeListener(InvalidationListener listener) {
            if (invalidationListeners != null) {
                for (int index = 0; index < invalidationSize; index++) {
                    if (invalidationListeners[index].equals(listener)) {
                        AsyncExpressionHelper<T> helper = getSingleListenerHelper(index, 1, 0, 0, 0);
                        if (helper != null) {
                            return helper;
                        }

                        Generic<T> generic = new Generic<>(observable, currentValue, this);

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
        protected Generic<T> addListener(SubInvalidationListener listener) {
            Generic<T> helper = new Generic<>(observable, currentValue, this);

            if (helper.subInvalidationListeners == null) {
                helper.subInvalidationListeners = new SubInvalidationListener[] {listener};
                helper.subInvalidationSize = 1;
            } else {
                helper.subInvalidationListeners =
                    Arrays.copyOf(helper.subInvalidationListeners, helper.subInvalidationSize + 1);
                helper.subInvalidationSize = trim(helper.subInvalidationSize, helper.subInvalidationListeners);
                helper.subInvalidationListeners[helper.subInvalidationSize++] = listener;
            }

            return helper;
        }

        @Override
        protected AsyncExpressionHelper<T> removeListener(SubInvalidationListener listener) {
            if (subInvalidationListeners != null) {
                for (int index = 0; index < subInvalidationSize; index++) {
                    if (subInvalidationListeners[index].equals(listener)) {
                        AsyncExpressionHelper<T> helper = getSingleListenerHelper(index, 0, 1, 0, 0);
                        if (helper != null) {
                            return helper;
                        }

                        Generic<T> generic = new Generic<>(observable, currentValue, this);

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
        protected AsyncExpressionHelper<T> addListener(ChangeListener<? super T> listener, T currentValue) {
            Generic<T> helper = new Generic<>(observable, currentValue, this);

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

            return helper;
        }

        @Override
        protected AsyncExpressionHelper<T> removeListener(ChangeListener<? super T> listener) {
            if (changeListeners != null) {
                for (int index = 0; index < changeSize; index++) {
                    if (changeListeners[index].equals(listener)) {
                        AsyncExpressionHelper<T> helper = getSingleListenerHelper(index, 0, 0, 1, 0);
                        if (helper != null) {
                            return helper;
                        }

                        Generic<T> generic = new Generic<>(observable, currentValue, this);

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
        protected AsyncExpressionHelper<T> addListener(SubChangeListener listener, T currentValue) {
            Generic<T> helper = new Generic<>(observable, currentValue, this);

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

            return helper;
        }

        @Override
        protected AsyncExpressionHelper<T> removeListener(SubChangeListener listener) {
            if (subChangeListeners != null) {
                for (int index = 0; index < subChangeSize; index++) {
                    if (subChangeListeners[index].equals(listener)) {
                        AsyncExpressionHelper<T> helper = getSingleListenerHelper(index, 0, 0, 0, 1);
                        if (helper != null) {
                            return helper;
                        }

                        Generic<T> generic = new Generic<>(observable, currentValue, this);

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
        protected boolean validatesValue() {
            return changeSize > 0 || subChangeSize > 0;
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
        protected void fireValueChangedEvent(T newValue, boolean subChange) {
            if (!subChange) {
                for (int i = 0; i < invalidationSize; i++) {
                    try {
                        invalidationListeners[i].invalidated(observable);
                    } catch (Exception e) {
                        Thread.currentThread()
                            .getUncaughtExceptionHandler()
                            .uncaughtException(Thread.currentThread(), e);
                    }
                }
            }

            for (int i = 0; i < subInvalidationSize; i++) {
                try {
                    subInvalidationListeners[i].invalidated(observable, subChange);
                } catch (Exception e) {
                    Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
                }
            }

            if (changeSize > 0 || subChangeSize > 0) {
                final T oldValue = currentValue;
                currentValue = newValue;

                if (!subChange) {
                    final boolean changed =
                        (currentValue == null) ? (oldValue != null) : !currentValue.equals(oldValue);
                    if (!changed) {
                        return;
                    }

                    for (int i = 0; i < changeSize; i++) {
                        try {
                            changeListeners[i].changed(observable, oldValue, currentValue);
                        } catch (Exception e) {
                            Thread.currentThread()
                                .getUncaughtExceptionHandler()
                                .uncaughtException(Thread.currentThread(), e);
                        }
                    }
                }

                for (int i = 0; i < subChangeSize; i++) {
                    try {
                        subChangeListeners[i].changed(observable, oldValue, currentValue, subChange);
                    } catch (Exception e) {
                        Thread.currentThread()
                            .getUncaughtExceptionHandler()
                            .uncaughtException(Thread.currentThread(), e);
                    }
                }
            }
        }

        private AsyncExpressionHelper<T> getSingleListenerHelper(
                int index, int removeInvalidation, int removeSubInvalidation, int removeChange, int removeSubChange) {
            if (invalidationSize - removeInvalidation == 1
                    && subInvalidationSize - removeSubInvalidation == 0
                    && changeSize - removeChange == 0
                    && subChangeSize - removeSubChange == 0) {
                return new SingleInvalidation<>(
                    observable, invalidationListeners[invalidationSize == 2 ? 1 - index : 0]);
            }

            if (invalidationSize - removeInvalidation == 0
                    && subInvalidationSize - removeSubInvalidation == 1
                    && changeSize - removeChange == 0
                    && subChangeSize - removeSubChange == 0) {
                return new SingleSubInvalidation<>(
                    observable, subInvalidationListeners[subInvalidationSize == 2 ? 1 - index : 0]);
            }

            if (invalidationSize - removeInvalidation == 0
                    && subInvalidationSize - removeSubInvalidation == 0
                    && changeSize - removeChange == 1
                    && subChangeSize - removeSubChange == 0) {
                return new SingleChange<>(observable, currentValue, changeListeners[changeSize == 2 ? 1 - index : 0]);
            }

            if (invalidationSize - removeInvalidation == 0
                    && subInvalidationSize - removeSubInvalidation == 0
                    && changeSize - removeChange == 0
                    && subChangeSize - removeSubChange == 1) {
                return new SingleSubChange<>(
                    observable, currentValue, subChangeListeners[subChangeSize == 2 ? 1 - index : 0]);
            }

            return null;
        }
    }

}
