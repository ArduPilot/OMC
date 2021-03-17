/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.common;

import com.intel.missioncontrol.custom.ReadonlyPropertyWrap;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import java.util.function.Function;

/**
 * Helper class for null-safe reaching of JavaFx bean's properties. Common use-case is when you need to get the value of
 * object which itself can be null at the moment of binding but eventually will obtain some value.
 *
 * <p>For example: <br>
 * {@code context.getSelectedCountry().get().getSelectedCity().get().getSelectedStreet() //NPE can occur if
 * getSelectedCountry() or city returns null }
 *
 * <p>With this class it is possible to write: {@code
 * LateBinding.of(context.getSelectedCountry()).get(Country::getSelectedCity).get(City::getSelectedStreet).property(); }
 *
 * <p>It will add listeners for each of the values in chain, so they whole chain will update last returned property
 *
 * <h3>Limitations</h3>
 *
 * <ul>
 *   <li>It only works with Observable properties
 * </ul>
 *
 * @param <V> type of value in Observable
 */
@Deprecated
public class LateBinding<V> {

    private ObservableValue<V> wrappedProp;
    private ChangeListener<V> wrappedPropListener;

    public static <I> LateBinding<I> of(ObservableValue<I> initial) {
        return new LateBinding<>(initial);
    }

    private LateBinding(ObservableValue<V> current) {
        this.wrappedProp = current;
    }

    /**
     * Specifies how to get next value from wrapped value.
     *
     * @param nextValueFunction function which gets value from currenly wrapped value
     * @param <R> type of next value
     * @return new instance of LateBinding with new wrapped value
     */
    public <R> LateBinding<R> get(Function<V, ObservableValue<R>> nextValueFunction) {
        SimpleObjectProperty<R> internalProp = new SimpleObjectProperty<>();
        wrappedPropListener =
            wrappedPropListener != null
                ? wrappedPropListener
                : (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        ObservableValue<R> nextObservable = nextValueFunction.apply(newValue);
                        nextObservable.addListener(
                            (o, old, newV) -> {
                                internalProp.set(newV);
                            });
                        if (nextObservable.getValue() != null) {
                            internalProp.set(nextObservable.getValue());
                        }
                    } else {
                        internalProp.set(null);
                    }
                };
        wrappedProp.removeListener(wrappedPropListener);
        wrappedProp.addListener(wrappedPropListener);
        if (wrappedProp.getValue() != null) {
            internalProp.set(nextValueFunction.apply(wrappedProp.getValue()).getValue());
        }

        return new LateBinding<>(internalProp);
    }

    /**
     * Returns wrapped property.
     *
     * @return wrapped property
     */
    public ReadonlyPropertyWrap<V> property() {
        return new ReadonlyPropertyWrap<>(wrappedProp);
    }

    /**
     * Returns wrap of wrapped property with default value
     *
     * @param defaultValue for property.
     * @return wrapped property with default value
     */
    public ReadonlyPropertyWrap<V> property(V defaultValue) {
        SimpleObjectProperty<V> propWithDefault = new SimpleObjectProperty<>(defaultValue);
        wrappedProp.addListener(
            (observable, oldValue, newValue) -> {
                if (newValue == null) {
                    propWithDefault.set(defaultValue);
                } else {
                    propWithDefault.set(newValue);
                }
            });
        return new ReadonlyPropertyWrap<>(propWithDefault);
    }
}
