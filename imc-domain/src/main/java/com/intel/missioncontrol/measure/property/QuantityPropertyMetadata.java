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

public class QuantityPropertyMetadata<Q extends Quantity<Q>> extends PropertyMetadata<Quantity<Q>> {

    public static class Builder<V extends Quantity<V>> {
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

        public QuantityPropertyMetadata<V> create() {
            return new QuantityPropertyMetadata<V>(
                name,
                hasName,
                customBean,
                hasCustomBean,
                initialValue,
                hasInitialValue,
                consistencyGroup,
                hasConsistencyGroup,
                dispatcher,
                hasDispatcher,
                quantityStyleProvider,
                hasQuantityStyleProvider,
                unitInfo,
                hasUnitInfo);
        }
    }

    final IQuantityStyleProvider quantityStyleProvider;
    final boolean hasQuantityStyleProvider;
    final UnitInfo<Q> unitInfo;
    final boolean hasUnitInfo;

    QuantityPropertyMetadata(
            String name,
            boolean hasName,
            Boolean customBean,
            boolean hasCustomBean,
            Quantity<Q> initialValue,
            boolean hasInitialValue,
            ConsistencyGroup consistencyGroup,
            boolean hasConsistencyGroup,
            Dispatcher dispatcher,
            boolean hasDispatcher,
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
            dispatcher,
            hasDispatcher);
        this.quantityStyleProvider = quantityStyleProvider;
        this.hasQuantityStyleProvider = hasQuantityStyleProvider;
        this.unitInfo = unitInfo;
        this.hasUnitInfo = hasUnitInfo;
    }

    public IQuantityStyleProvider getQuantityStyleProvider() {
        return quantityStyleProvider;
    }

    public UnitInfo<Q> getUnitInfo() {
        return unitInfo;
    }

    @Override
    protected PropertyMetadata<Quantity<Q>> merge(PropertyMetadata<Quantity<Q>> metadata) {
        PropertyMetadata<Quantity<Q>> baseMetadata = super.merge(metadata);

        if (metadata instanceof QuantityPropertyMetadata) {
            QuantityPropertyMetadata<Q> quantityMetadata = (QuantityPropertyMetadata<Q>)metadata;

            return new QuantityPropertyMetadata<>(
                Accessor.hasName(baseMetadata) ? Accessor.getName(baseMetadata) : Accessor.getName(this),
                Accessor.hasName(baseMetadata) || Accessor.hasName(this),
                Accessor.hasCustomBean(baseMetadata)
                    ? Accessor.getCustomBean(baseMetadata)
                    : Accessor.getCustomBean(this),
                Accessor.hasCustomBean(baseMetadata) || Accessor.hasCustomBean(this),
                Accessor.hasInitialValue(baseMetadata)
                    ? Accessor.getInitialValue(baseMetadata)
                    : Accessor.getInitialValue(this),
                Accessor.hasInitialValue(baseMetadata) || Accessor.hasInitialValue(this),
                Accessor.hasConsistencyGroup(baseMetadata)
                    ? Accessor.getConsistencyGroup(baseMetadata)
                    : Accessor.getConsistencyGroup(this),
                Accessor.hasConsistencyGroup(baseMetadata) || Accessor.hasConsistencyGroup(this),
                Accessor.hasDispatcher(baseMetadata)
                    ? Accessor.getDispatcher(baseMetadata)
                    : Accessor.getDispatcher(this),
                Accessor.hasDispatcher(baseMetadata) || Accessor.hasDispatcher(this),
                quantityMetadata.hasQuantityStyleProvider
                    ? quantityMetadata.quantityStyleProvider
                    : quantityStyleProvider,
                quantityMetadata.hasQuantityStyleProvider || hasQuantityStyleProvider,
                quantityMetadata.hasUnitInfo ? quantityMetadata.unitInfo : unitInfo,
                quantityMetadata.hasUnitInfo || hasUnitInfo);
        }

        return new QuantityPropertyMetadata<>(
            Accessor.hasName(baseMetadata) ? Accessor.getName(baseMetadata) : Accessor.getName(this),
            Accessor.hasName(baseMetadata) || Accessor.hasName(this),
            Accessor.hasCustomBean(baseMetadata) ? Accessor.getCustomBean(baseMetadata) : Accessor.getCustomBean(this),
            Accessor.hasCustomBean(baseMetadata) || Accessor.hasCustomBean(this),
            Accessor.hasInitialValue(baseMetadata)
                ? Accessor.getInitialValue(baseMetadata)
                : Accessor.getInitialValue(this),
            Accessor.hasInitialValue(baseMetadata) || Accessor.hasInitialValue(this),
            Accessor.hasConsistencyGroup(baseMetadata)
                ? Accessor.getConsistencyGroup(baseMetadata)
                : Accessor.getConsistencyGroup(this),
            Accessor.hasConsistencyGroup(baseMetadata) || Accessor.hasConsistencyGroup(this),
            Accessor.hasDispatcher(baseMetadata) ? Accessor.getDispatcher(baseMetadata) : Accessor.getDispatcher(this),
            Accessor.hasDispatcher(baseMetadata) || Accessor.hasDispatcher(this),
            quantityStyleProvider,
            hasQuantityStyleProvider,
            unitInfo,
            hasUnitInfo);
    }

}
