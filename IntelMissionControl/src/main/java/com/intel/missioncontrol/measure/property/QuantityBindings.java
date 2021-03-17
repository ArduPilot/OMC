/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.measure.property;

import com.intel.missioncontrol.common.Expect;
import com.intel.missioncontrol.measure.Dimension;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.QuantityFormat;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.measure.VariantQuantity;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.function.Predicate;
import javafx.beans.WeakListener;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import org.asyncfx.PublishSource;
import org.checkerframework.checker.nullness.qual.Nullable;

@PublishSource(
    module = "openjfx",
    licenses = {"openjfx", "intel-gpl-classpath-exception"}
)
public final class QuantityBindings {

    public static <Q extends Quantity<Q>> StringBinding createStringBinding(
            QuantityProperty<Q> property, QuantityFormat quantityFormat) {
        return Bindings.createStringBinding(
            () -> {
                var value = property.getValue();
                if (value == null) {
                    return null;
                }

                return quantityFormat.format(value, property.getUnitInfo());
            },
            property);
    }

    public static <Q extends Quantity<Q>> StringBinding createStringBinding(
            AsyncQuantityProperty<Q> property, QuantityFormat quantityFormat) {
        return Bindings.createStringBinding(
            () -> {
                var value = property.getValue();
                if (value == null) {
                    return null;
                }

                return quantityFormat.format(value, property.getUnitInfo());
            },
            property);
    }

    @SuppressWarnings("unchecked")
    public static void bindBidirectional(
            VariantQuantityProperty quantityProperty,
            Property<Number> numberProperty,
            Map<Dimension, Unit<? extends Quantity<?>>> units) {
        Expect.notNull(quantityProperty, "quantityProperty");
        Expect.notNull(numberProperty, "numberProperty");
        Expect.notNull(units, "units");

        final ConvertingNumberBidirectionalVariantQuantityBinding binding =
            new ConvertingNumberBidirectionalVariantQuantityBinding(quantityProperty, numberProperty, units);

        Unit unit;
        VariantQuantity quantity = quantityProperty.get();
        if (quantity != null) {
            unit = quantity.getUnit();
        } else {
            Dimension firstDimension = quantityProperty.getUnitInfo().iterator().next().getDimension();
            unit = Unit.getUnits(firstDimension)[0];
        }

        quantityProperty.set(Quantity.of(numberProperty.getValue().doubleValue(), unit).toVariant());
        quantityProperty.addListener(binding);
        numberProperty.addListener(binding);
    }

    public static <Q extends Quantity<Q>> void bindBidirectional(
            QuantityProperty<Q> quantityProperty, Property<Number> numberProperty, Unit<Q> unit) {
        Expect.notNull(quantityProperty, "quantityProperty");
        Expect.notNull(numberProperty, "numberProperty");
        Expect.notNull(unit, "unit");

        final ConvertingNumberBidirectionalQuantityBinding<Q> binding =
            new ConvertingNumberBidirectionalQuantityBinding<>(quantityProperty, numberProperty, unit, null);

        quantityProperty.setValue(Quantity.of(numberProperty.getValue().doubleValue(), unit));
        quantityProperty.addListener(binding);
        numberProperty.addListener(binding);
    }

    public static <Q extends Quantity<Q>> void unbindBidirectional(
            QuantityProperty<Q> quantityProperty, Property<Number> numberProperty) {
        Expect.notNull(quantityProperty, "quantityProperty");
        Expect.notNull(numberProperty, "numberProperty");

        final ConvertingNumberBidirectionalQuantityBinding<Q> binding =
            new ConvertingNumberBidirectionalQuantityBinding<>(quantityProperty, numberProperty, null, null);

        quantityProperty.removeListener(binding);
        numberProperty.removeListener(binding);
    }

    private abstract static class BidirectionalQuantityBinding<Q extends Quantity<Q>>
            implements ChangeListener<Object>, WeakListener {
        private final int cachedHashCode;

        private BidirectionalQuantityBinding(QuantityProperty<Q> property1, Object property2) {
            cachedHashCode = property1.hashCode() * property2.hashCode();
        }

        protected abstract QuantityProperty<Q> getProperty1();

        protected abstract Object getProperty2();

        @Override
        public int hashCode() {
            return cachedHashCode;
        }

        @Override
        public boolean wasGarbageCollected() {
            return (getProperty1() == null) || (getProperty2() == null);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            final Object propertyA1 = getProperty1();
            final Object propertyA2 = getProperty2();
            if ((propertyA1 == null) || (propertyA2 == null)) {
                return false;
            }

            if (obj instanceof BidirectionalQuantityBinding<?>) {
                final BidirectionalQuantityBinding<?> otherBinding = (BidirectionalQuantityBinding<?>)obj;
                final QuantityProperty<?> propertyB1 = otherBinding.getProperty1();
                final Object propertyB2 = otherBinding.getProperty2();

                if ((propertyB1 == null) || (propertyB2 == null)) {
                    return false;
                }

                if (propertyA1 == propertyB1 && propertyA2 == propertyB2) {
                    return true;
                }

                if (propertyA1 == propertyB2 && propertyA2 == propertyB1) {
                    return true;
                }
            }

            return false;
        }
    }

    private abstract static class BidirectionalVariantQuantityBinding implements ChangeListener<Object>, WeakListener {
        private final int cachedHashCode;

        private BidirectionalVariantQuantityBinding(VariantQuantityProperty property1, Object property2) {
            cachedHashCode = property1.hashCode() * property2.hashCode();
        }

        protected abstract VariantQuantityProperty getProperty1();

        protected abstract Object getProperty2();

        @Override
        public int hashCode() {
            return cachedHashCode;
        }

        @Override
        public boolean wasGarbageCollected() {
            return (getProperty1() == null) || (getProperty2() == null);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            final Object propertyA1 = getProperty1();
            final Object propertyA2 = getProperty2();
            if ((propertyA1 == null) || (propertyA2 == null)) {
                return false;
            }

            if (obj instanceof BidirectionalQuantityBinding<?>) {
                final BidirectionalQuantityBinding<?> otherBinding = (BidirectionalQuantityBinding<?>)obj;
                final QuantityProperty<?> propertyB1 = otherBinding.getProperty1();
                final Object propertyB2 = otherBinding.getProperty2();

                if ((propertyB1 == null) || (propertyB2 == null)) {
                    return false;
                }

                if (propertyA1 == propertyB1 && propertyA2 == propertyB2) {
                    return true;
                }

                if (propertyA1 == propertyB2 && propertyA2 == propertyB1) {
                    return true;
                }
            }

            return false;
        }
    }

    private static class ConvertingNumberBidirectionalQuantityBinding<Q extends Quantity<Q>>
            extends BidirectionalQuantityBinding<Q> {
        private final WeakReference<QuantityProperty<Q>> propertyRef1;
        private final WeakReference<Property<Number>> propertyRef2;
        private final Unit<Q> unit;

        @Nullable
        private final Predicate<Quantity<Q>> updateCondition;

        private boolean updating = false;

        private ConvertingNumberBidirectionalQuantityBinding(
                QuantityProperty<Q> property1,
                Property<Number> property2,
                Unit<Q> unit,
                @Nullable Predicate<Quantity<Q>> updateCondition) {
            super(property1, property2);
            this.propertyRef1 = new WeakReference<>(property1);
            this.propertyRef2 = new WeakReference<>(property2);
            this.unit = unit;
            this.updateCondition = updateCondition;
        }

        @Override
        protected QuantityProperty<Q> getProperty1() {
            return propertyRef1.get();
        }

        @Override
        protected Property<Number> getProperty2() {
            return propertyRef2.get();
        }

        @Override
        public void changed(ObservableValue<?> observable, Object oldValue, Object newValue) {
            if (!updating) {
                final QuantityProperty<Q> property1 = propertyRef1.get();
                final Property<Number> property2 = propertyRef2.get();
                if ((property1 == null) || (property2 == null)) {
                    if (property1 != null) {
                        property1.removeListener(this);
                    }

                    if (property2 != null) {
                        property2.removeListener(this);
                    }
                } else {
                    try {
                        updating = true;
                        if (property1 == observable) {
                            try {
                                Quantity<Q> value = property1.getValue();
                                if (value != null) {
                                    if (updateCondition == null || updateCondition.test(value)) {
                                        property2.setValue(value.convertTo(unit).getValue().doubleValue());
                                    }
                                } else {
                                    property2.setValue(null);
                                }
                            } catch (Exception e) {
                                property2.setValue(null);
                            }
                        } else {
                            try {
                                Number value = property2.getValue();
                                if (value != null) {
                                    property1.setValue(Quantity.of(value.doubleValue(), unit));
                                } else {
                                    property1.setValue(null);
                                }
                            } catch (Exception e) {
                                property1.setValue(null);
                            }
                        }
                    } finally {
                        updating = false;
                    }
                }
            }
        }
    }

    private static class ConvertingNumberBidirectionalVariantQuantityBinding
            extends BidirectionalVariantQuantityBinding {
        private final WeakReference<VariantQuantityProperty> propertyRef1;
        private final WeakReference<Property<Number>> propertyRef2;
        private final Map<Dimension, Unit<? extends Quantity<?>>> units;
        private boolean updating = false;

        private ConvertingNumberBidirectionalVariantQuantityBinding(
                VariantQuantityProperty property1,
                Property<Number> property2,
                Map<Dimension, Unit<? extends Quantity<?>>> units) {
            super(property1, property2);
            this.propertyRef1 = new WeakReference<>(property1);
            this.propertyRef2 = new WeakReference<>(property2);
            this.units = units;
        }

        @Override
        protected VariantQuantityProperty getProperty1() {
            return propertyRef1.get();
        }

        @Override
        protected Object getProperty2() {
            return propertyRef2.get();
        }

        @Override
        public void changed(ObservableValue<?> observable, Object oldValue, Object newValue) {
            if (!updating) {
                final VariantQuantityProperty property1 = propertyRef1.get();
                final Property<Number> property2 = propertyRef2.get();
                if ((property1 == null) || (property2 == null)) {
                    if (property1 != null) {
                        property1.removeListener(this);
                    }

                    if (property2 != null) {
                        property2.removeListener(this);
                    }
                } else {
                    try {
                        updating = true;
                        if (property1 == observable) {
                            try {
                                VariantQuantity value = property1.getValue();
                                if (value != null) {
                                    property2.setValue(getConvertedValue(value, units));
                                } else {
                                    property2.setValue(null);
                                }
                            } catch (Exception e) {
                                property2.setValue(null);
                            }
                        } else {
                            try {
                                VariantQuantity quantity = property1.getValue();
                                Number value = property2.getValue();
                                if (value != null && quantity != null) {
                                    Quantity<?> newQuantity =
                                        Quantity.of(value.doubleValue(), units.get(quantity.getDimension()));
                                    property1.setValue(getConvertedQuantity(quantity, newQuantity));
                                } else {
                                    property1.setValue(null);
                                }
                            } catch (Exception e) {
                                property1.setValue(null);
                            }
                        }
                    } finally {
                        updating = false;
                    }
                }
            }
        }
    }

    private static Number getConvertedValue(
            VariantQuantity quantity, Map<Dimension, Unit<? extends Quantity<?>>> units) {
        Unit<?> unit = units.get(quantity.getDimension());
        return quantity.convertTo(unit).getValue();
    }

    private static VariantQuantity getConvertedQuantity(VariantQuantity targetQuantity, Quantity<?> sourceQuantity) {
        if (sourceQuantity.getDimension() != targetQuantity.getDimension()) {
            throw new IllegalArgumentException(
                "Quantity dimension mismatch: source="
                    + sourceQuantity.getDimension()
                    + ", target="
                    + targetQuantity.getDimension());
        }

        return sourceQuantity.toVariant().convertTo(targetQuantity.getUnit()).toVariant();
    }

}
