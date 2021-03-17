/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.mission.bindings;

import eu.mavinci.core.flightplan.IMuteable;
import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Function;
import javafx.beans.property.ListProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;

public class ListBinding<S, T extends IMuteable> implements IBinding {

    static class NullBinding<S, T extends IMuteable> extends ListBinding<S, T> {
        NullBinding() {
            super(null, null);
        }

        @Override
        public void to(Function<T, Collection<S>> getter) {}

        @Override
        public void to(Function<T, Collection<S>> getter, BiConsumer<T, Collection<S>> setter) {}

        @Override
        public void to(Function<T, Collection<S>> getter, BiConsumer<T, Collection<S>> setter, boolean muteSetter) {}

        @Override
        public void updateValueFromSource() {}
    }

    private final ListProperty<S> property;
    private final T boundObject;
    private Function<T, Collection<S>> getter;
    private BiConsumer<T, Collection<S>> setter;
    private boolean muteSetter;
    private boolean updating;

    private final ChangeListener<ObservableList<S>> changeListener =
        new ChangeListener<>() {

            @Override
            public void changed(
                    ObservableValue<? extends ObservableList<S>> observable,
                    ObservableList<S> oldValue,
                    ObservableList<S> newValue) {
                if (updating) {
                    return;
                }

                try {
                    updating = true;
                    if (muteSetter) {
                        try (IMuteable.MuteScope scope = boundObject.openMuteScope()) {
                            setter.accept(boundObject, newValue);
                        }
                    } else {
                        setter.accept(boundObject, newValue);
                    }
                } finally {
                    updating = false;
                }
            }
        };

    ListBinding(T boundObject, ListProperty<S> property) {
        this.boundObject = boundObject;
        this.property = property;
    }

    @Override
    public void close() {
        property.removeListener(changeListener);
    }

    public void to(Function<T, Collection<S>> getter) {
        this.getter = getter;
        property.setAll(getter.apply(boundObject));
    }

    public void to(Function<T, Collection<S>> getter, BiConsumer<T, Collection<S>> setter) {
        to(getter, setter, false);
    }

    public void to(Function<T, Collection<S>> getter, BiConsumer<T, Collection<S>> setter, boolean muteSetter) {
        to(getter);
        this.setter = setter;
        this.muteSetter = muteSetter;
        property.addListener(changeListener);
    }

    @Override
    public void updateValueFromSource() {
        if (getter != null && !updating) {
            updating = true;
            property.setAll(getter.apply(boundObject));
            updating = false;
        }
    }

}
