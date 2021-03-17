/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.beans.binding;

import java.lang.ref.WeakReference;
import javafx.beans.WeakListener;
import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import org.asyncfx.PublishSource;

@PublishSource(
    module = "openjfx",
    licenses = {"intel-gpl-classpath-exception"}
)
abstract class ConvertingBidirectionalBinding implements ChangeListener, WeakListener {

    private static void checkParameters(Object property1, Object property2) {
        if ((property1 == null) || (property2 == null)) {
            throw new NullPointerException("Both properties must be specified.");
        }

        if (property1 == property2) {
            throw new IllegalArgumentException("Cannot bind property to itself");
        }
    }

    @SuppressWarnings("unchecked")
    static <T, U> void bind(Property<T> property1, Property<U> property2, BidirectionalValueConverter<U, T> converter) {
        checkParameters(property1, property2);
        final ConvertingBidirectionalBinding binding =
            new TypedGenericBidirectionalBinding<>(property1, property2, converter);
        property1.setValue(converter.convert(property2.getValue()));
        property1.addListener(binding);
        property2.addListener(binding);
    }

    @SuppressWarnings("unchecked")
    static <T> void unbind(Property<T> property1, Property<T> property2) {
        checkParameters(property1, property2);
        final ConvertingBidirectionalBinding binding = new UntypedGenericBidirectionalBinding(property1, property2);
        property1.removeListener(binding);
        property2.removeListener(binding);
    }

    @SuppressWarnings("unchecked")
    static void unbind(Object property1, Object property2) {
        checkParameters(property1, property2);
        final ConvertingBidirectionalBinding binding = new UntypedGenericBidirectionalBinding(property1, property2);
        if (property1 instanceof ObservableValue) {
            ((ObservableValue)property1).removeListener(binding);
        }

        if (property2 instanceof ObservableValue) {
            ((ObservableValue)property2).removeListener(binding);
        }
    }

    private final int cachedHashCode;

    private ConvertingBidirectionalBinding(Object property1, Object property2) {
        cachedHashCode = property1.hashCode() * property2.hashCode();
    }

    protected abstract Object getProperty1();

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

        if (obj instanceof ConvertingBidirectionalBinding) {
            final ConvertingBidirectionalBinding otherBinding = (ConvertingBidirectionalBinding)obj;
            final Object propertyB1 = otherBinding.getProperty1();
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

    private static class TypedGenericBidirectionalBinding<T, U> extends ConvertingBidirectionalBinding {
        private final WeakReference<Property<T>> propertyRef1;
        private final WeakReference<Property<U>> propertyRef2;
        private final BidirectionalValueConverter<U, T> converter;
        private boolean updating = false;

        private TypedGenericBidirectionalBinding(
                Property<T> property1, Property<U> property2, BidirectionalValueConverter<U, T> converter) {
            super(property1, property2);
            propertyRef1 = new WeakReference<>(property1);
            propertyRef2 = new WeakReference<>(property2);
            this.converter = converter;
        }

        @Override
        protected Property<T> getProperty1() {
            return propertyRef1.get();
        }

        @Override
        protected Property<U> getProperty2() {
            return propertyRef2.get();
        }

        @Override
        @SuppressWarnings("unchecked")
        public void changed(ObservableValue sourceProperty, Object oldValue, Object newValue) {
            if (!updating) {
                final Property<T> property1 = propertyRef1.get();
                final Property<U> property2 = propertyRef2.get();
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
                        if (property1 == sourceProperty) {
                            property2.setValue(converter.convertBack((T)newValue));
                        } else {
                            property1.setValue(converter.convert((U)newValue));
                        }
                    } catch (RuntimeException e) {
                        try {
                            if (property1 == sourceProperty) {
                                property1.setValue(converter.convert((U)oldValue));
                            } else {
                                property2.setValue(converter.convertBack((T)oldValue));
                            }
                        } catch (Exception e2) {
                            e2.addSuppressed(e);
                            unbind(property1, property2);
                            throw new RuntimeException(
                                "Bidirectional binding failed together with an attempt"
                                    + " to restore the source property to the previous value."
                                    + " Removing the bidirectional binding from properties "
                                    + property1
                                    + " and "
                                    + property2,
                                e2);
                        }

                        throw new RuntimeException("Bidirectional binding failed, setting to the previous value", e);
                    } finally {
                        updating = false;
                    }
                }
            }
        }
    }

    private static class UntypedGenericBidirectionalBinding extends ConvertingBidirectionalBinding {
        private final Object property1;
        private final Object property2;

        UntypedGenericBidirectionalBinding(Object property1, Object property2) {
            super(property1, property2);
            this.property1 = property1;
            this.property2 = property2;
        }

        @Override
        protected Object getProperty1() {
            return property1;
        }

        @Override
        protected Object getProperty2() {
            return property2;
        }

        @Override
        public void changed(ObservableValue sourceProperty, Object oldValue, Object newValue) {
            throw new RuntimeException("Should not reach here");
        }
    }

}
