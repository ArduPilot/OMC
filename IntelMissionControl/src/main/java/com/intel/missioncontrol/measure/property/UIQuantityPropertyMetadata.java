/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.measure.property;

import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.UnitInfo;
import org.asyncfx.beans.property.AsyncProperty;
import org.asyncfx.beans.property.ConsistencyGroup;
import org.asyncfx.beans.property.PropertyMetadata;
import org.asyncfx.concurrent.Dispatcher;

public class UIQuantityPropertyMetadata<Q extends Quantity<Q>> extends QuantityPropertyMetadata<Q> {

    public static class Builder<V extends Quantity<V>> {
        private String name;
        private boolean hasName;
        private boolean customBean;
        private boolean hasCustomBean;
        private V initialValue;
        private boolean hasInitialValue;
        private ConsistencyGroup consistencyGroup;
        private boolean hasConsistencyGroup;
        private IQuantityStyleProvider quantityStyleProvider;
        private boolean hasQuantityStyleProvider;
        private UnitInfo<V> unitInfo;
        private boolean hasUnitInfo;

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

        public Builder<V> quantityStyleProvider(IQuantityStyleProvider quantityStyleProvider) {
            this.quantityStyleProvider = quantityStyleProvider;
            this.hasQuantityStyleProvider = true;
            return this;
        }

        public Builder<V> unitInfo(UnitInfo<V> unitInfo) {
            this.unitInfo = unitInfo;
            this.hasUnitInfo = true;
            return this;
        }

        public UIQuantityPropertyMetadata<V> create() {
            return new UIQuantityPropertyMetadata<V>(
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

    UIQuantityPropertyMetadata(
            String name,
            boolean hasName,
            Boolean customBean,
            boolean hasCustomBean,
            Quantity<Q> initialValue,
            boolean hasInitialValue,
            ConsistencyGroup consistencyGroup,
            boolean hasConsistencyGroup,
            IQuantityStyleProvider quantityStyleProvider,
            boolean hasQuantityStyleProvider,
            UnitInfo<Q> unitInfo,
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
    protected PropertyMetadata<Quantity<Q>> merge(PropertyMetadata<Quantity<Q>> metadata) {
        QuantityPropertyMetadata<Q> baseMetadata = (QuantityPropertyMetadata<Q>)super.merge(metadata);

        return new UIQuantityPropertyMetadata<>(
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
