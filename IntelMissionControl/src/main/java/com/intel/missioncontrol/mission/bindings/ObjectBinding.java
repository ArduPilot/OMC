/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.mission.bindings;

import eu.mavinci.core.flightplan.IMuteable;
import java.util.function.BiConsumer;
import java.util.function.Function;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class ObjectBinding<S, T extends IMuteable> implements IBinding {

    static class NullBinding<S, T extends IMuteable> extends ObjectBinding<S, T> {
        NullBinding() {
            super(null, null);
        }

        @Override
        public void to(Function<T, S> getter) {}

        @Override
        public void to(Function<T, S> getter, BiConsumer<T, S> setter) {}

        @Override
        public void to(Function<T, S> getter, BiConsumer<T, S> setter, boolean muteSetter) {}

        @Override
        public void updateValueFromSource() {}
    }

    private final ObjectProperty<S> property;
    private final T boundObject;
    private Function<T, S> getter;
    private BiConsumer<T, S> setter;
    private boolean muteSetter;
    private boolean updating;

    private final ChangeListener<S> changeListener =
        new ChangeListener<>() {
            @Override
            public void changed(ObservableValue<? extends S> observable, S oldValue, S newValue) {
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

    ObjectBinding(T boundObject, ObjectProperty<S> property) {
        this.boundObject = boundObject;
        this.property = property;
    }

    @Override
    public void close() {
        property.removeListener(changeListener);
    }

    public void to(Function<T, S> getter) {
        this.getter = getter;
        property.setValue(getter.apply(boundObject));
    }

    public void to(Function<T, S> getter, BiConsumer<T, S> setter) {
        to(getter, setter, false);
    }

    public void to(Function<T, S> getter, BiConsumer<T, S> setter, boolean muteSetter) {
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
