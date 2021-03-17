/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.measure.property;

import com.intel.missioncontrol.measure.UnitInfo;
import com.intel.missioncontrol.measure.VariantQuantity;
import org.asyncfx.Optional;
import org.asyncfx.beans.property.AsyncProperty;
import org.asyncfx.beans.property.ConsistencyGroup;
import org.asyncfx.beans.property.PropertyMetadata;
import org.asyncfx.beans.property.UIDispatcher;

public class UIVariantQuantityPropertyMetadata extends VariantQuantityPropertyMetadata {

    public static class Builder {
        private Optional<String> name = Optional.empty();
        private Optional<Boolean> customBean = Optional.empty();
        private Optional<VariantQuantity> initialValue = Optional.empty();
        private Optional<ConsistencyGroup> consistencyGroup = Optional.empty();
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
         * Adds this property to a consistency group. If a property is part of a consistency group, it can only be
         * modified within a critical section. Modifications of multiple properties within a consistency group will
         * appear as atomic operations to observers.
         */
        public Builder consistencyGroup(ConsistencyGroup value) {
            this.consistencyGroup = Optional.of(value);
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
                name, customBean, initialValue, consistencyGroup, quantityStyleProvider, unitInfo);
        }
    }

    UIVariantQuantityPropertyMetadata(
            Optional<String> name,
            Optional<Boolean> customBean,
            Optional<VariantQuantity> initialValue,
            Optional<ConsistencyGroup> consistencyGroup,
            Optional<IQuantityStyleProvider> quantityStyleProvider,
            Optional<UnitInfo<?>[]> unitInfo) {
        super(
            name,
            customBean,
            initialValue,
            consistencyGroup,
            Optional.of(UIDispatcher::run),
            UIDispatcher::isDispatcherThread,
            quantityStyleProvider,
            unitInfo);
    }

    @Override
    protected PropertyMetadata<VariantQuantity> merge(PropertyMetadata<VariantQuantity> metadata) {
        VariantQuantityPropertyMetadata baseMetadata = (VariantQuantityPropertyMetadata)super.merge(metadata);

        return new UIVariantQuantityPropertyMetadata(
            PropertyMetadata.Accessor.getName(baseMetadata),
            PropertyMetadata.Accessor.getCustomBean(baseMetadata),
            PropertyMetadata.Accessor.getInitialValue(baseMetadata),
            PropertyMetadata.Accessor.getConsistencyGroup(baseMetadata),
            baseMetadata.quantityStyleProvider,
            baseMetadata.unitInfo);
    }

}
