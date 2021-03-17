/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.mission.bindings;

import eu.mavinci.core.flightplan.IMuteable;
import java.util.function.BiConsumer;
import java.util.function.Function;
import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class DoubleBinding<T extends IMuteable> implements IBinding {

    static class NullBinding<T extends IMuteable> extends DoubleBinding<T> {
        NullBinding() {
            super(null, null);
        }

        @Override
        public void to(Function<T, Double> getter) {}

        @Override
        public void to(Function<T, Double> getter, BiConsumer<T, Double> setter) {}

        @Override
        public void to(Function<T, Double> getter, BiConsumer<T, Double> setter, boolean muteSetter) {}

        @Override
        public void updateValueFromSource() {}
    }

    private final DoubleProperty property;
    private final T boundObject;
    private Function<T, Double> getter;
    private BiConsumer<T, Double> setter;
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
                            setter.accept(boundObject, newValue.doubleValue());
                        }
                    } else {
                        setter.accept(boundObject, newValue.doubleValue());
                    }
                } finally {
                    updating = false;
                }
            }
        };

    DoubleBinding(T boundObject, DoubleProperty property) {
        this.boundObject = boundObject;
        this.property = property;
    }

    @Override
    public void close() {
        property.removeListener(changeListener);
    }

    public void to(Function<T, Double> getter) {
        this.getter = getter;
        property.setValue(getter.apply(boundObject));
    }

    public void to(Function<T, Double> getter, BiConsumer<T, Double> setter) {
        to(getter, setter, false);
    }

    public void to(Function<T, Double> getter, BiConsumer<T, Double> setter, boolean muteSetter) {
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
