/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.beans.property;

import org.asyncfx.Optional;

public class UIPropertyMetadata<T> extends PropertyMetadata<T> {

    public static class Builder<V> {
        private Optional<String> name = Optional.empty();
        private Optional<Boolean> customBean = Optional.empty();
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
         * Adds this property to a consistency group. If a property is part of a consistency group, it can only be
         * modified within a critical section. Modifications of multiple properties within a consistency group will
         * appear as atomic operations to observers.
         */
        public Builder<V> consistencyGroup(ConsistencyGroup value) {
            this.consistencyGroup = Optional.of(value);
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

        public UIPropertyMetadata<V> create() {
            return new UIPropertyMetadata<>(name, customBean, initialValue, consistencyGroup);
        }
    }

    UIPropertyMetadata(
            Optional<String> name,
            Optional<Boolean> customBean,
            Optional<T> initialValue,
            Optional<ConsistencyGroup> consistencyGroup) {
        super(
            name,
            customBean,
            initialValue,
            consistencyGroup,
            Optional.of(UIDispatcher::run),
            UIDispatcher::isDispatcherThread);
    }

    @Override
    protected PropertyMetadata<T> merge(PropertyMetadata<T> metadata) {
        return new UIPropertyMetadata<>(
            orElse(Accessor.getName(metadata), Accessor.getName(this)),
            orElse(Accessor.getCustomBean(metadata), Accessor.getCustomBean(this)),
            orElse(Accessor.getInitialValue(metadata), Accessor.getInitialValue(this)),
            orElse(Accessor.getConsistencyGroup(metadata), Accessor.getConsistencyGroup(this)));
    }

    private static <T> Optional<T> orElse(Optional<T> first, Optional<T> second) {
        return first.isPresent() ? first : second;
    }

}
