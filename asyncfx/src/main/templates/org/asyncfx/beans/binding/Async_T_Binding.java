/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.beans.binding;

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

    @Override
    public void addListener(InvalidationListener listener) {
        long stamp = 0;

        try {
            stamp = accessController.writeLock(false);
            helper = AsyncExpressionHelper.addListener(helper, this, getCore(), listener);
        } finally {
            accessController.unlockWrite(stamp);
        }
    }

    @Override
    public void addListener(InvalidationListener listener, Executor executor) {
        long stamp = 0;

        try {
            stamp = accessController.writeLock(false);
            helper =
                AsyncExpressionHelper.addListener(
                    helper, this, getCore(), AsyncInvalidationListenerWrapper.wrap(listener, executor));
        } finally {
            accessController.unlockWrite(stamp);
        }
    }

    @Override
    public void removeListener(InvalidationListener listener) {
        long stamp = 0;

        try {
            stamp = accessController.writeLock(false);
            helper = AsyncExpressionHelper.removeListener(helper, listener);
        } finally {
            accessController.unlockWrite(stamp);
        }
    }

    @Override
    public void addListener(ChangeListener<? super $numberType> listener) {
        long stamp = 0;

        try {
            stamp = accessController.writeLock(false);
            helper = AsyncExpressionHelper.addListener(helper, this, getCore(), listener);
        } finally {
            accessController.unlockWrite(stamp);
        }
    }

    @Override
    public void addListener(ChangeListener<? super $numberType> listener, Executor executor) {
        long stamp = 0;

        try {
            stamp = accessController.writeLock(false);
            helper =
                AsyncExpressionHelper.addListener(
                    helper, this, getCore(), AsyncChangeListenerWrapper.wrap(listener, executor));
        } finally {
            accessController.unlockWrite(stamp);
        }
    }

    @Override
    public void removeListener(ChangeListener<? super $numberType> listener) {
        long stamp = 0;

        try {
            stamp = accessController.writeLock(false);
            helper = AsyncExpressionHelper.removeListener(helper, listener);
        } finally {
            accessController.unlockWrite(stamp);
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
            if ((stamp = accessController.tryOptimisticRead(true)) != 0) {
                boolean valid = this.valid;
                $primType value = this.value;
                if (accessController.validate(true, stamp)) {
                    if (valid) {
                        return value;
                    }

                    read = false;
                }
            }

            if (read) {
                stamp = accessController.readLock(true);
                if (valid) {
                    return value;
                }
            }

            long newStamp = accessController.tryConvertToWriteLock(true, stamp);
            if (newStamp == 0) {
                accessController.unlockRead(stamp);
                stamp = accessController.writeLock(true);
            } else {
                stamp = newStamp;
            }

            return getCore();
        } finally {
            accessController.unlock(stamp);
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
        long stamp = 0;
        boolean invalidate;
        AsyncExpressionHelper helper;

    #if($isNumberType)
        $primType currentValue = 0;
    #elseif($boxedType == "Boolean")
        $primType currentValue = false;
    #else
        $primType currentValue = null;
    #end

        try {
            if ((stamp = accessController.tryOptimisticRead(true)) != 0) {
                boolean valid = this.valid;
                if (accessController.validate(true, stamp) && !valid) {
                    return;
                }
            }

            stamp = accessController.writeLock(true);
            invalidate = valid;
            helper = this.helper;

            if (invalidate) {
                valid = false;

                if (AsyncExpressionHelper.validatesValue(helper)) {
                    currentValue = getCore();
                }
            }
        } finally {
            accessController.unlockWrite(stamp);
        }

        if (invalidate) {
            onInvalidating();
            AsyncExpressionHelper.fireValueChangedEvent(helper, currentValue, false);
        }
    }

    @Override
    public final boolean isValid() {
        long stamp = 0;

        try {
            if ((stamp = accessController.tryOptimisticRead(false)) != 0) {
                boolean valid = this.valid;
                if (accessController.validate(false, stamp)) {
                    return valid;
                }
            }

            stamp = accessController.readLock(false);
            return this.valid;
        } finally {
            accessController.unlockRead(stamp);
        }
    }

    protected abstract $primType computeValue();

    @Override
    public String toString() {
        return valid ? "Async${boxedType}Binding [value: " + get() + "]" : "Async${boxedType}Binding [invalid]";
    }

}
