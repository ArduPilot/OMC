/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.measure.property;

import com.intel.missioncontrol.measure.UnitInfo;
import com.intel.missioncontrol.measure.VariantQuantity;
import org.asyncfx.beans.property.AsyncProperty;
import org.asyncfx.beans.property.ConsistencyGroup;
import org.asyncfx.beans.property.PropertyMetadata;
import org.asyncfx.concurrent.Dispatcher;

public class UIVariantQuantityPropertyMetadata extends VariantQuantityPropertyMetadata {

    public static class Builder {
        private String name;
        private boolean hasName;
        private boolean customBean;
        private boolean hasCustomBean;
        private VariantQuantity initialValue;
        private boolean hasInitialValue;
        private ConsistencyGroup consistencyGroup;
        private boolean hasConsistencyGroup;
        private IQuantityStyleProvider quantityStyleProvider;
        private boolean hasQuantityStyleProvider;
        private UnitInfo<?>[] unitInfo;
        private boolean hasUnitInfo;

        /**
         * The name of the property. If this value is not specified, the name of the property field will be
         * automatically detected at runtime. Generally, you should not manually specify the name.
         */
        public Builder name(String name) {
            this.name = name;
            this.hasName = true;
            return this;
        }

        /**
         * The initial value of the property. This is also the value which is assumed by the property after calling
         * {@link AsyncProperty#reset()}.
         */
        public Builder initialValue(VariantQuantity value) {
            this.initialValue = value;
            this.hasInitialValue = true;
            return this;
        }

        /**
         * For regular async properties, the bean value must be a reference to the object instance that contains the
         * property field. Setting this option allows the bean value to be any object reference. If this option is set,
         * automatic name detection will be disabled.
         */
        public Builder customBean(boolean value) {
            this.customBean = value;
            this.hasCustomBean = true;
            return this;
        }

        /**
         * Adds this property to a consistency group. If a property is part of a consistency group, it can only be
         * modified within a critical section. Modifications of multiple properties within a consistency group will
         * appear as atomic operations to observers.
         */
        public Builder consistencyGroup(ConsistencyGroup value) {
            this.consistencyGroup = value;
            this.hasConsistencyGroup = true;
            return this;
        }

        public Builder quantityStyleProvider(IQuantityStyleProvider quantityStyleProvider) {
            this.quantityStyleProvider = quantityStyleProvider;
            this.hasQuantityStyleProvider = true;
            return this;
        }

        public Builder unitInfo(UnitInfo<?>[] unitInfo) {
            this.unitInfo = unitInfo;
            this.hasUnitInfo = true;
            return this;
        }

        public UIVariantQuantityPropertyMetadata create() {
            return new UIVariantQuantityPropertyMetadata(
                name,
                hasName,
                customBean,
                hasCustomBean,
                initialValue,
                hasInitialValue,
                consistencyGroup,
                hasConsistencyGroup,
                quantityStyleProvider,
                hasQuantityStyleProvider,
                unitInfo,
                hasUnitInfo);
        }
    }

    UIVariantQuantityPropertyMetadata(
            String name,
            boolean hasName,
            Boolean customBean,
            boolean hasCustomBean,
            VariantQuantity initialValue,
            boolean hasInitialValue,
            ConsistencyGroup consistencyGroup,
            boolean hasConsistencyGroup,
            IQuantityStyleProvider quantityStyleProvider,
            boolean hasQuantityStyleProvider,
            UnitInfo<?>[] unitInfo,
            boolean hasUnitInfo) {
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
            true,
            quantityStyleProvider,
            hasQuantityStyleProvider,
            unitInfo,
            hasUnitInfo);
    }

    @Override
    protected PropertyMetadata<VariantQuantity> merge(PropertyMetadata<VariantQuantity> metadata) {
        VariantQuantityPropertyMetadata baseMetadata = (VariantQuantityPropertyMetadata)super.merge(metadata);

        return new UIVariantQuantityPropertyMetadata(
            PropertyMetadata.Accessor.getName(baseMetadata),
            PropertyMetadata.Accessor.hasName(baseMetadata),
            PropertyMetadata.Accessor.getCustomBean(baseMetadata),
            PropertyMetadata.Accessor.hasCustomBean(baseMetadata),
            PropertyMetadata.Accessor.getInitialValue(baseMetadata),
            PropertyMetadata.Accessor.hasInitialValue(baseMetadata),
            PropertyMetadata.Accessor.getConsistencyGroup(baseMetadata),
            PropertyMetadata.Accessor.hasConsistencyGroup(baseMetadata),
            baseMetadata.quantityStyleProvider,
            baseMetadata.hasQuantityStyleProvider,
            baseMetadata.unitInfo,
            baseMetadata.hasUnitInfo);
    }

}
