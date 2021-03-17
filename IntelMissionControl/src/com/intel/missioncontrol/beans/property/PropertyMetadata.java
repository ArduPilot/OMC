/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.beans.property;

import com.intel.missioncontrol.common.Optional;
import com.intel.missioncontrol.concurrent.ListenableExecutor;
import com.intel.missioncontrol.concurrent.ListenableExecutors;
import com.intel.missioncontrol.concurrent.SynchronizationContext;

public class PropertyMetadata<T> {

    public interface HasAccessDelegate {
        boolean hasAccess();
    }

    public static class Builder<V> {
        private Optional<String> name = Optional.empty();
        private Optional<Boolean> customBean = Optional.empty();
        private Optional<SynchronizationContext> synchronizationContext = Optional.empty();
        private Optional<V> initialValue = Optional.empty();

        /**
         * The name of the property. If this value is not specified, the name of the property field will be
         * automatically detected at runtime. Generally, you should not manually specify the name.
         */
        public Builder<V> name(String name) {
            this.name = Optional.of(name);
            return this;
        }

        /**
         * The initial value of the property. This is also the value which is assumed by the property after calling
         * {@link AsyncProperty#reset()}.
         */
        public Builder<V> initialValue(V value) {
            this.initialValue = Optional.of(value);
            return this;
        }

        /**
         * Specifies the synchronization context that is used when the property is bound to another property, or when
         * any of the -Async methods are called.
         */
        public Builder<V> synchronizationContext(SynchronizationContext synchronizationContext) {
            this.synchronizationContext = Optional.of(synchronizationContext);
            return this;
        }

        /**
         * For regular async properties, the bean value must be a reference to the object instance that contains the
         * property field. Setting this option allows the bean value to be any object reference. If this option is set,
         * automatic name detection will be disabled.
         */
        public Builder<V> customBean(boolean value) {
            this.customBean = Optional.of(value);
            return this;
        }

        public PropertyMetadata<V> create() {
            return new PropertyMetadata<V>(
                name,
                customBean,
                initialValue,
                synchronizationContext.isPresent() ? Optional.of(synchronizationContext.get()) : Optional.empty(),
                synchronizationContext.isPresent() ? synchronizationContext.get()::hasAccess : null);
        }
    }

    final Optional<String> name;
    final Optional<Boolean> customBean;
    final Optional<ListenableExecutor> executor;
    final Optional<T> initialValue;
    final HasAccessDelegate hasAccess;

    PropertyMetadata(
            Optional<String> name,
            Optional<Boolean> customBean,
            Optional<T> initialValue,
            Optional<ListenableExecutor> executor,
            HasAccessDelegate hasAccess) {
        this.name = name;
        this.customBean = customBean;
        this.initialValue = initialValue;
        this.executor = executor;
        this.hasAccess = hasAccess;
    }

    public String getName() {
        return name.orElse(null);
    }

    public boolean isCustomBean() {
        return customBean.orElse(false);
    }

    public ListenableExecutor getExecutor() {
        return executor.orElse(ListenableExecutors.directExecutor());
    }

    public T getInitialValue() {
        return initialValue.orElse(null);
    }

    public final void verifyAccess() {
        if (hasAccess != null && PropertyHelper.isVerifyPropertyAccessEnabled()) {
            if (!hasAccess.hasAccess()) {
                throw new IllegalStateException(
                    "Illegal cross-thread access: expected = "
                        + getExecutor().toString()
                        + "; currentThread = "
                        + Thread.currentThread().getName()
                        + ".");
            }
        }
    }

    PropertyMetadata<T> merge(PropertyMetadata<T> metadata) {
        return new PropertyMetadata<>(
            metadata.name.isPresent() ? metadata.name : name,
            metadata.customBean.isPresent() ? metadata.customBean : customBean,
            metadata.initialValue.isPresent() ? metadata.initialValue : initialValue,
            metadata.executor.isPresent() ? metadata.executor : executor,
            metadata.executor.isPresent() ? metadata.hasAccess : hasAccess);
    }

}
