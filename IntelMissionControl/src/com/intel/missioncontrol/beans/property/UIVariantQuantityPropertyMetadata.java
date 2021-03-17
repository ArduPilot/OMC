/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.beans.property;

import com.intel.missioncontrol.common.Optional;
import com.intel.missioncontrol.concurrent.ListenableExecutors;
import com.intel.missioncontrol.measure.UnitInfo;
import com.intel.missioncontrol.measure.VariantQuantity;
import javafx.application.Platform;

public class UIVariantQuantityPropertyMetadata extends VariantQuantityPropertyMetadata {

    public static class Builder {
        private Optional<String> name = Optional.empty();
        private Optional<Boolean> customBean = Optional.empty();
        private Optional<VariantQuantity> initialValue = Optional.empty();
        private Optional<IQuantityStyleProvider> quantityStyleProvider = Optional.empty();
        private Optional<UnitInfo<?>[]> unitInfo = Optional.empty();

        /**
         * The name of the property. If this value is not specified, the name of the property field will be
         * automatically detected at runtime. Generally, you should not manually specify the name.
         */
        public Builder name(String name) {
            this.name = Optional.of(name);
            return this;
        }

        /**
         * The initial value of the property. This is also the value which is assumed by the property after calling
         * {@link AsyncProperty#reset()}.
         */
        public Builder initialValue(VariantQuantity value) {
            this.initialValue = Optional.of(value);
            return this;
        }

        /**
         * For regular async properties, the bean value must be a reference to the object instance that contains the
         * property field. Setting this option allows the bean value to be any object reference. If this option is set,
         * automatic name detection will be disabled.
         */
        public Builder customBean(boolean value) {
            this.customBean = Optional.of(value);
            return this;
        }

        public Builder quantityStyleProvider(IQuantityStyleProvider quantityStyleProvider) {
            this.quantityStyleProvider = Optional.of(quantityStyleProvider);
            return this;
        }

        public Builder unitInfo(UnitInfo<?>... unitInfo) {
            this.unitInfo = Optional.of(unitInfo);
            return this;
        }

        public UIVariantQuantityPropertyMetadata create() {
            return new UIVariantQuantityPropertyMetadata(
                name, customBean, initialValue, quantityStyleProvider, unitInfo);
        }
    }

    UIVariantQuantityPropertyMetadata(
            Optional<String> name,
            Optional<Boolean> customBean,
            Optional<VariantQuantity> initialValue,
            Optional<IQuantityStyleProvider> quantityStyleProvider,
            Optional<UnitInfo<?>[]> unitInfo) {
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
    PropertyMetadata<VariantQuantity> merge(PropertyMetadata<VariantQuantity> metadata) {
        VariantQuantityPropertyMetadata baseMetadata = (VariantQuantityPropertyMetadata)super.merge(metadata);

        return new UIVariantQuantityPropertyMetadata(
            baseMetadata.name,
            baseMetadata.customBean,
            baseMetadata.initialValue,
            baseMetadata.quantityStyleProvider,
            baseMetadata.unitInfo);
    }

}
