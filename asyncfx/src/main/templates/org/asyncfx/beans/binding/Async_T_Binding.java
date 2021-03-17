/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.beans.binding;

import static org.asyncfx.beans.AccessControllerImpl.LockName.EVENT;
import static org.asyncfx.beans.AccessControllerImpl.LockName.VALUE;
import static org.asyncfx.beans.AccessControllerImpl.LockType.GROUP;
import static org.asyncfx.beans.AccessControllerImpl.LockType.INSTANCE;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.Executor;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.${boxedType}Expression;
import javafx.beans.value.ChangeListener;
import org.asyncfx.beans.AccessControllerImpl;
import org.asyncfx.beans.AsyncInvalidationListenerWrapper;
import org.asyncfx.beans.DeferredListener;
import org.asyncfx.beans.SubInvalidationListener;
import org.asyncfx.beans.value.AsyncChangeListenerWrapper;
import org.asyncfx.beans.value.SubChangeListener;
import org.asyncfx.collections.AsyncObservableList;
import org.asyncfx.collections.FXAsyncCollections;

#set($isNumberType = $boxedType == "Integer" || $boxedType == "Long" || $boxedType == "Float" || $boxedType == "Double")

public abstract class Async${boxedType}Binding$!{genericType} extends ${boxedType}Expression$!{genericType} implements AsyncBinding<$numberType> {

    private final AccessControllerImpl accessController = new AccessControllerImpl();
    private $primType value;
    private boolean valid = false;
    private AsyncBindingHelperObserver observer;
    private AsyncExpressionHelper<$numberType> helper = null;
    private Queue<DeferredListener<? super $numberType>> deferredListeners;

    @Override
    public void addListener(InvalidationListener listener) {
        long valueStamp = 0;
        long eventStamp = 0;

        try {
            valueStamp = accessController.writeLock(VALUE, INSTANCE);
            if ((eventStamp = accessController.tryWriteLock(EVENT, INSTANCE)) != 0) {
                resolveDeferredListeners();
                helper = AsyncExpressionHelper.addListener(helper, this, getCore(), listener);
            } else {
                addListenerDeferred(listener);
            }
        } finally {
            accessController.unlockWrite(valueStamp, eventStamp);
        }
    }

    @Override
    public void addListener(InvalidationListener listener, Executor executor) {
        long valueStamp = 0;
        long eventStamp = 0;

        try {
            valueStamp = accessController.writeLock(VALUE, INSTANCE);
            if ((eventStamp = accessController.tryWriteLock(EVENT, INSTANCE)) != 0) {
                resolveDeferredListeners();
                helper =
                    AsyncExpressionHelper.addListener(
                        helper, this, getCore(), AsyncInvalidationListenerWrapper.wrap(listener, executor));
            } else {
                addListenerDeferred(AsyncInvalidationListenerWrapper.wrap(listener, executor));
            }
        } finally {
            accessController.unlockWrite(valueStamp, eventStamp);
        }
    }

    @Override
    public void removeListener(InvalidationListener listener) {
        long valueStamp = 0;
        long eventStamp = 0;

        try {
            valueStamp = accessController.writeLock(VALUE, INSTANCE);
            if ((eventStamp = accessController.tryWriteLock(EVENT, INSTANCE)) != 0) {
                resolveDeferredListeners();
                helper = AsyncExpressionHelper.removeListener(helper, getCore(), listener);
            } else {
                removeListenerDeferred(listener);
            }
        } finally {
            accessController.unlockWrite(valueStamp, eventStamp);
        }
    }

    @Override
    public void addListener(ChangeListener<? super $numberType> listener) {
        long valueStamp = 0;
        long eventStamp = 0;

        try {
            valueStamp = accessController.writeLock(VALUE, INSTANCE);
            if ((eventStamp = accessController.tryWriteLock(EVENT, INSTANCE)) != 0) {
                resolveDeferredListeners();
                helper = AsyncExpressionHelper.addListener(helper, this, getCore(), listener);
            } else {
                addListenerDeferred(listener);
            }
        } finally {
            accessController.unlockWrite(valueStamp, eventStamp);
        }
    }

    @Override
    public void addListener(ChangeListener<? super $numberType> listener, Executor executor) {
        long valueStamp = 0;
        long eventStamp = 0;

        try {
            valueStamp = accessController.writeLock(VALUE, INSTANCE);
            if ((eventStamp = accessController.tryWriteLock(EVENT, INSTANCE)) != 0) {
                resolveDeferredListeners();
                helper =
                    AsyncExpressionHelper.addListener(
                        helper, this, getCore(), AsyncChangeListenerWrapper.wrap(listener, executor));
            } else {
                addListenerDeferred(AsyncChangeListenerWrapper.wrap(listener, executor));
            }
        } finally {
            accessController.unlockWrite(valueStamp, eventStamp);
        }
    }

    @Override
    public void removeListener(ChangeListener<? super $numberType> listener) {
        long valueStamp = 0;
        long eventStamp = 0;

        try {
            valueStamp = accessController.writeLock(VALUE, INSTANCE);
            if ((eventStamp = accessController.tryWriteLock(EVENT, INSTANCE)) != 0) {
                resolveDeferredListeners();
                helper = AsyncExpressionHelper.removeListener(helper, getCore(), listener);
            } else {
                removeListenerDeferred(listener);
            }
        } finally {
            accessController.unlockWrite(valueStamp, eventStamp);
        }
    }

    protected final void bind(Observable... dependencies) {
        if ((dependencies != null) && (dependencies.length > 0)) {
            if (observer == null) {
                observer = new AsyncBindingHelperObserver(this);
            }

            for (final Observable dep : dependencies) {
                dep.addListener(observer);
            }
        }
    }

    protected final void unbind(Observable... dependencies) {
        if (observer != null) {
            for (final Observable dep : dependencies) {
                dep.removeListener(observer);
            }

            observer = null;
        }
    }

    @Override
    public void dispose() {}

    @Override
    public AsyncObservableList<?> getDependencies() {
        return FXAsyncCollections.emptyObservableList();
    }

    @Override
    public final $primType get() {
        long stamp = 0;
        boolean read = true;

        try {
            if ((stamp = accessController.tryOptimisticRead(VALUE, GROUP)) != 0) {
                boolean valid = this.valid;
                $primType value = this.value;
                if (accessController.validate(VALUE, GROUP, stamp)) {
                    if (valid) {
                        return value;
                    }

                    read = false;
                }
            }

            if (read) {
                stamp = accessController.readLock(VALUE, GROUP);
                if (valid) {
                    return value;
                }
            }

            long newStamp = accessController.tryConvertToWriteLock(VALUE, GROUP, stamp);
            if (newStamp == 0) {
                accessController.unlockRead(VALUE, stamp);
                stamp = accessController.writeLock(VALUE, GROUP);
            } else {
                stamp = newStamp;
            }

            return getCore();
        } finally {
            accessController.unlock(VALUE, stamp);
        }
    }

    private $primType getCore() {
        if (!valid) {
            value = computeValue();
            valid = true;
        }

        return value;
    }

    protected void onInvalidating() {}

    @Override
    public final void invalidate() {
        long valueStamp = 0;
        long eventStamp = 0;
        boolean invalidate;

    #if($isNumberType)
        $primType currentValue = 0;
    #elseif($boxedType == "Boolean")
        $primType currentValue = false;
    #else
        $primType currentValue = null;
    #end

        try {
            if ((valueStamp = accessController.tryOptimisticRead(VALUE, GROUP)) != 0) {
                boolean valid = this.valid;
                if (accessController.validate(VALUE, GROUP, valueStamp) && !valid) {
                    return;
                }
            }

            valueStamp = accessController.writeLock(VALUE, GROUP);
            invalidate = valid;

            if (invalidate) {
                valid = false;
                eventStamp = accessController.writeLock(EVENT, INSTANCE);
                resolveDeferredListeners();

                if (AsyncExpressionHelper.validatesValue(helper)) {
                    try {
                        currentValue = getCore();
                    } catch (Exception e) {
                        accessController.unlockWrite(EVENT, eventStamp);
                        throw e;
                    }
                }
            }
        } finally {
            accessController.unlockWrite(VALUE, valueStamp);
        }

        if (invalidate) {
            try {
                onInvalidating();
                AsyncExpressionHelper.fireValueChangedEvent(helper, currentValue, false);
            } finally {
                accessController.unlockWrite(EVENT, eventStamp);
            }
        }
    }

    @Override
    public final boolean isValid() {
        long stamp = 0;

        try {
            if ((stamp = accessController.tryOptimisticRead(VALUE, INSTANCE)) != 0) {
                boolean valid = this.valid;
                if (accessController.validate(VALUE, INSTANCE, stamp)) {
                    return valid;
                }
            }

            stamp = accessController.readLock(VALUE, INSTANCE);
            return this.valid;
        } finally {
            accessController.unlockRead(VALUE, stamp);
        }
    }

    protected abstract $primType computeValue();

    private void addListenerDeferred(InvalidationListener listener) {
        if (deferredListeners == null) {
            deferredListeners = new ArrayDeque<>();
        }

        deferredListeners.add(new DeferredListener<>(listener, true));
    }

    private void addListenerDeferred(SubInvalidationListener listener) {
        if (deferredListeners == null) {
            deferredListeners = new ArrayDeque<>();
        }

        deferredListeners.add(new DeferredListener<>(listener, true));
    }

    private void addListenerDeferred(ChangeListener<? super $numberType> listener) {
        if (deferredListeners == null) {
            deferredListeners = new ArrayDeque<>();
        }

        deferredListeners.add(new DeferredListener<>(listener, true));
    }

    private void addListenerDeferred(SubChangeListener listener) {
        if (deferredListeners == null) {
            deferredListeners = new ArrayDeque<>();
        }

        deferredListeners.add(new DeferredListener<>(listener, true));
    }

    private void removeListenerDeferred(InvalidationListener listener) {
        if (deferredListeners == null) {
            deferredListeners = new ArrayDeque<>();
        }

        deferredListeners.add(new DeferredListener<>(listener, false));
    }

    private void removeListenerDeferred(SubInvalidationListener listener) {
        if (deferredListeners == null) {
            deferredListeners = new ArrayDeque<>();
        }

        deferredListeners.add(new DeferredListener<>(listener, false));
    }

    private void removeListenerDeferred(ChangeListener<? super $numberType> listener) {
        if (deferredListeners == null) {
            deferredListeners = new ArrayDeque<>();
        }

        deferredListeners.add(new DeferredListener<>(listener, false));
    }

    private void removeListenerDeferred(SubChangeListener listener) {
        if (deferredListeners == null) {
            deferredListeners = new ArrayDeque<>();
        }

        deferredListeners.add(new DeferredListener<>(listener, false));
    }

    private void resolveDeferredListeners() {
        if (deferredListeners == null) {
            return;
        }

        $primType value = getCore();

        while (!deferredListeners.isEmpty()) {
            DeferredListener<? super $numberType> deferredListener = deferredListeners.remove();
            if (deferredListener.getInvalidationListener() != null) {
                if (deferredListener.wasAdded()) {
                    helper = AsyncExpressionHelper.addListener(helper, this, value, deferredListener.getInvalidationListener());
                } else {
                    helper = AsyncExpressionHelper.removeListener(helper, value, deferredListener.getInvalidationListener());
                }
            } else if (deferredListener.getSubInvalidationListener() != null) {
                if (deferredListener.wasAdded()) {
                    helper = AsyncExpressionHelper.addListener(helper, this, value, deferredListener.getSubInvalidationListener());
                } else {
                    helper = AsyncExpressionHelper.removeListener(helper, value, deferredListener.getSubInvalidationListener());
                }
            } else if (deferredListener.getChangeListener() != null) {
                if (deferredListener.wasAdded()) {
                    helper = AsyncExpressionHelper.addListener(helper, this, value, deferredListener.getChangeListener());
                } else {
                    helper = AsyncExpressionHelper.removeListener(helper, value, deferredListener.getChangeListener());
                }
            } else if (deferredListener.getSubChangeListener() != null) {
                if (deferredListener.wasAdded()) {
                    helper = AsyncExpressionHelper.addListener(helper, this, value, deferredListener.getSubChangeListener());
                } else {
                    helper = AsyncExpressionHelper.removeListener(helper, value, deferredListener.getSubChangeListener());
                }
            }
        }

        deferredListeners = null;
    }

    @Override
    public String toString() {
        return valid ? "Async${boxedType}Binding [value: " + get() + "]" : "Async${boxedType}Binding [invalid]";
    }

}
