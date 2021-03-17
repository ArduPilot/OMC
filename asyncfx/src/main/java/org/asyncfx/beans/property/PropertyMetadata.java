/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.beans.property;

import org.asyncfx.concurrent.Dispatcher;

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

    public static class Builder<V> {
        private String name;
        private boolean hasName;
        private boolean customBean;
        private boolean hasCustomBean;
        private Dispatcher dispatcher;
        private boolean hasDispatcher;
        private V initialValue;
        private boolean hasInitialValue;
        private ConsistencyGroup consistencyGroup;
        private boolean hasConsistencyGroup;

        /**
         * The name of the property. If this value is not specified, the name of the property field will be
         * automatically detected at runtime. Generally, you should not manually specify the name.
         */
        public Builder<V> name(String name) {
            this.name = name;
            this.hasName = true;
            return this;
        }

        /**
         * The initial value of the property. This is also the value which is assumed by the property after calling
         * {@link AsyncProperty#reset()}.
         */
        public Builder<V> initialValue(V value) {
            this.initialValue = value;
            this.hasInitialValue = true;
            return this;
        }

        /**
         * Specifies the synchronization context that is used when the property is bound to another property, or when
         * any of the -Async methods are called.
         */
        public Builder<V> dispatcher(Dispatcher dispatcher) {
            this.dispatcher = dispatcher;
            this.hasDispatcher = true;
            return this;
        }

        /**
         * For regular async properties, the bean value must be a reference to the object instance that contains the
         * property field. Setting this option allows the bean value to be any object reference. If this option is set,
         * automatic name detection will be disabled.
         */
        public Builder<V> customBean(boolean value) {
            this.customBean = value;
            this.hasCustomBean = true;
            return this;
        }

        /**
         * Adds this property to a consistency group. If a property is part of a consistency group, it can only be
         * modified within a critical section. Modifications of multiple properties within a consistency group will
         * appear as atomic operations to observers.
         */
        public Builder<V> consistencyGroup(ConsistencyGroup value) {
            this.consistencyGroup = value;
            this.hasConsistencyGroup = true;
            return this;
        }

        public PropertyMetadata<V> create() {
            return new PropertyMetadata<>(
                name,
                hasName,
                customBean,
                hasCustomBean,
                initialValue,
                hasInitialValue,
                consistencyGroup,
                hasConsistencyGroup,
                dispatcher,
                hasDispatcher);
        }
    }

    public static class Accessor {
        public static String getName(PropertyMetadata<?> metadata) {
            return metadata.name;
        }

        public static boolean hasName(PropertyMetadata<?> metadata) {
            return metadata.hasName;
        }

        public static boolean getCustomBean(PropertyMetadata<?> metadata) {
            return metadata.customBean;
        }

        public static boolean hasCustomBean(PropertyMetadata<?> metadata) {
            return metadata.hasCustomBean;
        }

        public static <T> T getInitialValue(PropertyMetadata<T> metadata) {
            return metadata.initialValue;
        }

        public static boolean hasInitialValue(PropertyMetadata<?> metadata) {
            return metadata.hasInitialValue;
        }

        public static ConsistencyGroup getConsistencyGroup(PropertyMetadata<?> metadata) {
            return metadata.consistencyGroup;
        }

        public static boolean hasConsistencyGroup(PropertyMetadata<?> metadata) {
            return metadata.hasConsistencyGroup;
        }

        public static Dispatcher getDispatcher(PropertyMetadata<?> metadata) {
            return metadata.dispatcher;
        }

        public static boolean hasDispatcher(PropertyMetadata<?> metadata) {
            return metadata.hasDispatcher;
        }
    }

    private final String name;
    private final boolean hasName;

    private final boolean customBean;
    private final boolean hasCustomBean;

    private final Dispatcher dispatcher;
    private final boolean hasDispatcher;

    private final T initialValue;
    private final boolean hasInitialValue;

    private final ConsistencyGroup consistencyGroup;
    private final boolean hasConsistencyGroup;

    protected PropertyMetadata(
            String name,
            boolean hasName,
            Boolean customBean,
            boolean hasCustomBean,
            T initialValue,
            boolean hasInitialValue,
            ConsistencyGroup consistencyGroup,
            boolean hasConsistencyGroup,
            Dispatcher dispatcher,
            boolean hasDispatcher) {
        this.name = name;
        this.hasName = hasName;
        this.customBean = customBean;
        this.hasCustomBean = hasCustomBean;
        this.initialValue = initialValue;
        this.hasInitialValue = hasInitialValue;
        this.consistencyGroup = consistencyGroup;
        this.hasConsistencyGroup = hasConsistencyGroup;
        this.dispatcher = dispatcher;
        this.hasDispatcher = hasDispatcher;
    }

    public String getName() {
        return name;
    }

    public boolean isCustomBean() {
        return customBean;
    }

    public Dispatcher getDispatcher() {
        return dispatcher;
    }

    public T getInitialValue() {
        return initialValue;
    }

    public ConsistencyGroup getConsistencyGroup() {
        return consistencyGroup;
    }

    protected PropertyMetadata<T> merge(PropertyMetadata<T> metadata) {
        return new PropertyMetadata<>(
            metadata.hasName ? metadata.name : name,
            metadata.hasName || hasName,
            metadata.hasCustomBean ? metadata.customBean : customBean,
            metadata.hasCustomBean || hasCustomBean,
            metadata.hasInitialValue ? metadata.initialValue : initialValue,
            metadata.hasInitialValue || hasInitialValue,
            metadata.hasConsistencyGroup ? metadata.consistencyGroup : consistencyGroup,
            metadata.hasConsistencyGroup || hasConsistencyGroup,
            metadata.hasDispatcher ? metadata.dispatcher : dispatcher,
            metadata.hasDispatcher || hasDispatcher);
    }

}
