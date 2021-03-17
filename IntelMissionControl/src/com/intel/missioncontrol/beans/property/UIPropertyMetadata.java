/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.beans.property;

import com.intel.missioncontrol.common.Optional;
import com.intel.missioncontrol.concurrent.ListenableExecutors;
import javafx.application.Platform;

public class UIPropertyMetadata<T> extends PropertyMetadata<T> {

    public static class Builder<V> {
        private Optional<String> name = Optional.empty();
        private Optional<Boolean> customBean = Optional.empty();
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
         * For regular async properties, the bean value must be a reference to the object instance that contains the
         * property field. Setting this option allows the bean value to be any object reference. If this option is set,
         * automatic name detection will be disabled.
         */
        public Builder<V> customBean(boolean value) {
            this.customBean = Optional.of(value);
            return this;
        }

        public UIPropertyMetadata<V> create() {
            return new UIPropertyMetadata<>(name, customBean, initialValue);
        }
    }

    UIPropertyMetadata(Optional<String> name, Optional<Boolean> customBean, Optional<T> initialValue) {
        super(
            name,
            customBean,
            initialValue,
            Optional.of(ListenableExecutors.platformExecutor()),
            Platform::isFxApplicationThread);
    }

    @Override
    PropertyMetadata<T> merge(PropertyMetadata<T> metadata) {
        return new UIPropertyMetadata<>(
            metadata.name.isPresent() ? metadata.name : name,
            metadata.customBean.isPresent() ? metadata.customBean : customBean,
            metadata.initialValue.isPresent() ? metadata.initialValue : initialValue);
    }

}
