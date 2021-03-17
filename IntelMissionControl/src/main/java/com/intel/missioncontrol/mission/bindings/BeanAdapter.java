/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.mission.bindings;

import com.intel.missioncontrol.helper.Expect;
import eu.mavinci.core.flightplan.IMuteable;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;

public class BeanAdapter<T extends IMuteable> {

    private static class NullAdapter<T extends IMuteable> extends BeanAdapter<T> {
        @Override
        public BooleanBinding<T> bind(BooleanProperty property) {
            return new BooleanBinding.NullBinding<>();
        }

        @Override
        public DoubleBinding<T> bind(DoubleProperty property) {
            return new DoubleBinding.NullBinding<>();
        }

        @Override
        public IntegerBinding<T> bind(IntegerProperty property) {
            return new IntegerBinding.NullBinding<>();
        }

        @Override
        public LongBinding<T> bind(LongProperty property) {
            return new LongBinding.NullBinding<>();
        }

        @Override
        public <S> ObjectBinding<S, T> bind(ObjectProperty<S> property) {
            return new ObjectBinding.NullBinding<>();
        }

        @Override
        public StringBinding<T> bind(StringProperty property) {
            return new StringBinding.NullBinding<>();
        }

        @Override
        public <S> ListBinding<S, T> bind(ListProperty<S> property) {
            return new ListBinding.NullBinding<>();
        }
    }

    private final List<IBinding> bindings = new ArrayList<>();
    private final T boundObject;

    private BeanAdapter() {
        boundObject = null;
    }

    public BeanAdapter(T boundObject) {
        Expect.notNull(boundObject, "boundObject");
        this.boundObject = boundObject;
    }

    public void unbindAll() {
        for (IBinding binding : bindings) {
            binding.close();
        }

        bindings.clear();
    }

    public void updateValuesFromSource() {
        for (IBinding binding : bindings) {
            binding.updateValueFromSource();
        }
    }

    @SuppressWarnings("unchecked")
    public <U extends T> BeanAdapter<U> subtype(Class<U> cls) {
        if (boundObject.getClass().isAssignableFrom(cls)) {
            return (BeanAdapter<U>)this;
        } else {
            return new NullAdapter<>();
        }
    }

    public DoubleBinding<T> bind(DoubleProperty property) {
        DoubleBinding<T> binding = new DoubleBinding<>(boundObject, property);
        bindings.add(binding);
        return binding;
    }

    public IntegerBinding<T> bind(IntegerProperty property) {
        IntegerBinding<T> binding = new IntegerBinding<>(boundObject, property);
        bindings.add(binding);
        return binding;
    }

    public LongBinding<T> bind(LongProperty property) {
        LongBinding<T> binding = new LongBinding<>(boundObject, property);
        bindings.add(binding);
        return binding;
    }

    public StringBinding<T> bind(StringProperty property) {
        StringBinding<T> binding = new StringBinding<>(boundObject, property);
        bindings.add(binding);
        return binding;
    }

    public BooleanBinding<T> bind(BooleanProperty property) {
        BooleanBinding<T> binding = new BooleanBinding<>(boundObject, property);
        bindings.add(binding);
        return binding;
    }

    public <S> ObjectBinding<S, T> bind(ObjectProperty<S> property) {
        ObjectBinding<S, T> binding = new ObjectBinding<>(boundObject, property);
        bindings.add(binding);
        return binding;
    }

    public <S> ListBinding<S, T> bind(ListProperty<S> property) {
        ListBinding<S, T> binding = new ListBinding<>(boundObject, property);
        bindings.add(binding);
        return binding;
    }

}
