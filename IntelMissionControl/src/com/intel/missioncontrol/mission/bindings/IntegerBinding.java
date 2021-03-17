/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.mission.bindings;

import eu.mavinci.core.flightplan.IMuteable;
import java.util.function.BiConsumer;
import java.util.function.Function;
import javafx.beans.property.IntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class IntegerBinding<T extends IMuteable> implements IBinding {

    static class NullBinding<T extends IMuteable> extends IntegerBinding<T> {
        NullBinding() {
            super(null, null);
        }

        @Override
        public void to(Function<T, Integer> getter) {}

        @Override
        public void to(Function<T, Integer> getter, BiConsumer<T, Integer> setter) {}

        @Override
        public void to(Function<T, Integer> getter, BiConsumer<T, Integer> setter, boolean muteSetter) {}

        @Override
        public void updateValueFromSource() {}
    }

    private final IntegerProperty property;
    private final T boundObject;
    private Function<T, Integer> getter;
    private BiConsumer<T, Integer> setter;
    private boolean muteSetter;
    private boolean updating;

    private final ChangeListener<Number> changeListener =
        new ChangeListener<>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if (updating) {
                    return;
                }

                try {
                    updating = true;
                    if (muteSetter) {
                        try (IMuteable.MuteScope scope = boundObject.openMuteScope()) {
                            setter.accept(boundObject, newValue.intValue());
                        }
                    } else {
                        setter.accept(boundObject, newValue.intValue());
                    }
                } finally {
                    updating = false;
                }
            }
        };

    IntegerBinding(T boundObject, IntegerProperty property) {
        this.boundObject = boundObject;
        this.property = property;
    }

    @Override
    public void close() {
        property.removeListener(changeListener);
    }

    public void to(Function<T, Integer> getter) {
        this.getter = getter;
        property.setValue(getter.apply(boundObject));
    }

    public void to(Function<T, Integer> getter, BiConsumer<T, Integer> setter) {
        to(getter, setter, false);
    }

    public void to(Function<T, Integer> getter, BiConsumer<T, Integer> setter, boolean muteSetter) {
        to(getter);
        this.setter = setter;
        this.muteSetter = muteSetter;
        property.addListener(changeListener);
    }

    @Override
    public void updateValueFromSource() {
        if (getter != null && !updating) {
            updating = true;
            property.setValue(getter.apply(boundObject));
            updating = false;
        }
    }

}
