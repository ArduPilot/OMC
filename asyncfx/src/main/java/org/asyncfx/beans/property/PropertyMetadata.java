/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.beans.property;

import com.google.common.util.concurrent.MoreExecutors;
import java.util.concurrent.Executor;
import org.asyncfx.AsyncFX;
import org.asyncfx.Optional;
import org.asyncfx.concurrent.SynchronizationContext;

/**
 * Defines metadata for {@link AsyncProperty}.
 *
 * <p>Initial metadata should be specified when the property is constructed. If no custom metadata is provided, the
 * property will be created with default metadata. After the property is constructed, metadata can be overridden by
 * calling {@link AsyncProperty#overrideMetadata(PropertyMetadata)}. If a field was specified in the overriding
 * metadata, it replaces the corresponding field of the base metadata; if it was not specified in the overriding
 * metadata, the corresponding field of the base metadata is retained.
 */
public class PropertyMetadata<T> {

    public interface HasAccessDelegate {
        boolean hasAccess();
    }

    public static class Builder<V> {
        private Optional<String> name = Optional.empty();
        private Optional<Boolean> customBean = Optional.empty();
        private Optional<SynchronizationContext> synchronizationContext = Optional.empty();
        private Optional<V> initialValue = Optional.empty();
        private Optional<ConsistencyGroup> consistencyGroup = Optional.empty();

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

        /**
         * Adds this property to a consistency group. If a property is part of a consistency group, it can only be
         * modified within a critical section. Modifications of multiple properties within a consistency group will
         * appear as atomic operations to observers.
         */
        public Builder<V> consistencyGroup(ConsistencyGroup value) {
            this.consistencyGroup = Optional.of(value);
            return this;
        }

        public PropertyMetadata<V> create() {
            return new PropertyMetadata<>(
                name,
                customBean,
                initialValue,
                consistencyGroup,
                synchronizationContext.isPresent() ? Optional.of(synchronizationContext.get()) : Optional.empty(),
                synchronizationContext.isPresent() ? synchronizationContext.get()::hasAccess : null);
        }
    }

    public static class Accessor {
        public static Optional<String> getName(PropertyMetadata<?> metadata) {
            return metadata.name;
        }

        public static Optional<Boolean> getCustomBean(PropertyMetadata<?> metadata) {
            return metadata.customBean;
        }

        public static <T> Optional<T> getInitialValue(PropertyMetadata<T> metadata) {
            return metadata.initialValue;
        }

        public static Optional<ConsistencyGroup> getConsistencyGroup(PropertyMetadata<?> metadata) {
            return metadata.consistencyGroup;
        }

        public static Optional<Executor> getExecutor(PropertyMetadata<?> metadata) {
            return metadata.executor;
        }

        public static HasAccessDelegate getHasAccess(PropertyMetadata<?> metadata) {
            return metadata.hasAccess;
        }
    }

    private final Optional<String> name;
    private final Optional<Boolean> customBean;
    private final Optional<T> initialValue;
    private final Optional<ConsistencyGroup> consistencyGroup;
    private final Optional<Executor> executor;
    private final HasAccessDelegate hasAccess;

    protected PropertyMetadata(
            Optional<String> name,
            Optional<Boolean> customBean,
            Optional<T> initialValue,
            Optional<ConsistencyGroup> consistencyGroup,
            Optional<Executor> executor,
            HasAccessDelegate hasAccess) {
        this.name = name;
        this.customBean = customBean;
        this.initialValue = initialValue;
        this.consistencyGroup = consistencyGroup;
        this.executor = executor;
        this.hasAccess = hasAccess;
    }

    public String getName() {
        return name.orElse(null);
    }

    public boolean isCustomBean() {
        return customBean.orElse(false);
    }

    public Executor getExecutor() {
        return executor.orElse(MoreExecutors.directExecutor());
    }

    public T getInitialValue() {
        return initialValue.orElse(null);
    }

    public final void verifyAccess() {
        if (hasAccess != null
                && (AsyncFX.isVerifyPropertyAccess() || AsyncFX.isRunningTests())
                && !hasAccess.hasAccess()) {
            throw new IllegalStateException(
                "Illegal cross-thread access: expected = "
                    + getExecutor().toString()
                    + "; currentThread = "
                    + Thread.currentThread().getName()
                    + ".");
        }

        verifyConsistency();
    }

    final void verifyConsistency() {
        if (consistencyGroup.isPresent()) {
            for (ReadOnlyAsyncProperty property : consistencyGroup.get().getProperties()) {
                if (!property.getAccessController().isLocked()) {
                    throw new IllegalStateException(
                        "Illegal access: property is part of a consistency group"
                            + " and can only be accessed within a critical section.");
                }
            }
        }
    }

    protected PropertyMetadata<T> merge(PropertyMetadata<T> metadata) {
        return new PropertyMetadata<>(
            metadata.name.isPresent() ? metadata.name : name,
            metadata.customBean.isPresent() ? metadata.customBean : customBean,
            metadata.initialValue.isPresent() ? metadata.initialValue : initialValue,
            metadata.consistencyGroup.isPresent() ? metadata.consistencyGroup : consistencyGroup,
            metadata.executor.isPresent() ? metadata.executor : executor,
            metadata.executor.isPresent() ? metadata.hasAccess : hasAccess);
    }

}
