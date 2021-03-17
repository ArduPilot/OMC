/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.beans.property;

import org.asyncfx.concurrent.Dispatcher;

public class UIPropertyMetadata<T> extends PropertyMetadata<T> {

    public static class Builder<V> {
        private String name;
        private boolean hasName;
        private boolean customBean;
        private boolean hasCustomBean;
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
         * Adds this property to a consistency group. If a property is part of a consistency group, it can only be
         * modified within a critical section. Modifications of multiple properties within a consistency group will
         * appear as atomic operations to observers.
         */
        public Builder<V> consistencyGroup(ConsistencyGroup value) {
            this.consistencyGroup = value;
            this.hasConsistencyGroup = true;
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

        public UIPropertyMetadata<V> create() {
            return new UIPropertyMetadata<>(
                name,
                hasName,
                customBean,
                hasCustomBean,
                initialValue,
                hasInitialValue,
                consistencyGroup,
                hasConsistencyGroup);
        }
    }

    UIPropertyMetadata(
            String name,
            boolean hasName,
            boolean customBean,
            boolean hasCustomBean,
            T initialValue,
            boolean hasInitialValue,
            ConsistencyGroup consistencyGroup,
            boolean hasConsistencyGroup) {
        super(
            name,
            hasName,
            customBean,
            hasCustomBean,
            initialValue,
            hasInitialValue,
            consistencyGroup,
            hasConsistencyGroup,
            Dispatcher.platform(),
            true);
    }

    @Override
    protected PropertyMetadata<T> merge(PropertyMetadata<T> metadata) {
        return new UIPropertyMetadata<>(
            Accessor.hasName(metadata) ? Accessor.getName(metadata) : Accessor.getName(this),
            Accessor.hasName(metadata) || Accessor.hasName(this),
            Accessor.hasCustomBean(metadata) ? Accessor.getCustomBean(metadata) : Accessor.getCustomBean(this),
            Accessor.hasCustomBean(metadata) || Accessor.hasCustomBean(this),
            Accessor.hasInitialValue(metadata) ? Accessor.getInitialValue(metadata) : Accessor.getInitialValue(this),
            Accessor.hasInitialValue(metadata) || Accessor.hasInitialValue(this),
            Accessor.hasConsistencyGroup(metadata)
                ? Accessor.getConsistencyGroup(metadata)
                : Accessor.getConsistencyGroup(this),
            Accessor.hasConsistencyGroup(metadata) || Accessor.hasConsistencyGroup(this));
    }
}
