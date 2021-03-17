/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.beans.property;

import com.intel.missioncontrol.common.Optional;
import com.intel.missioncontrol.concurrent.ListenableExecutors;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.UnitInfo;
import javafx.application.Platform;

public class UIQuantityPropertyMetadata<Q extends Quantity<Q>> extends QuantityPropertyMetadata<Q> {

    public static class Builder<V extends Quantity<V>> {
        private Optional<String> name = Optional.empty();
        private Optional<Boolean> customBean = Optional.empty();
        private Optional<Quantity<V>> initialValue = Optional.empty();
        private Optional<IQuantityStyleProvider> quantityStyleProvider = Optional.empty();
        private Optional<UnitInfo<V>> unitInfo = Optional.empty();

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

        public Builder<V> quantityStyleProvider(IQuantityStyleProvider quantityStyleProvider) {
            this.quantityStyleProvider = Optional.of(quantityStyleProvider);
            return this;
        }

        public Builder<V> unitInfo(UnitInfo<V> unitInfo) {
            this.unitInfo = Optional.of(unitInfo);
            return this;
        }

        public UIQuantityPropertyMetadata<V> create() {
            return new UIQuantityPropertyMetadata<>(name, customBean, initialValue, quantityStyleProvider, unitInfo);
        }
    }

    UIQuantityPropertyMetadata(
            Optional<String> name,
            Optional<Boolean> customBean,
            Optional<Quantity<Q>> initialValue,
            Optional<IQuantityStyleProvider> quantityStyleProvider,
            Optional<UnitInfo<Q>> unitInfo) {
        super(
            name,
            customBean,
            initialValue,
            Optional.of(ListenableExecutors.platformExecutor()),
            Platform::isFxApplicationThread,
            quantityStyleProvider,
            unitInfo);
    }

    @Override
    PropertyMetadata<Quantity<Q>> merge(PropertyMetadata<Quantity<Q>> metadata) {
        QuantityPropertyMetadata<Q> baseMetadata = (QuantityPropertyMetadata<Q>)super.merge(metadata);

        return new UIQuantityPropertyMetadata<>(
            baseMetadata.name,
            baseMetadata.customBean,
            baseMetadata.initialValue,
            baseMetadata.quantityStyleProvider,
            baseMetadata.unitInfo);
    }

}
