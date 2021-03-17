/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.mission.bindings;

import eu.mavinci.core.flightplan.IMuteable;
import java.util.function.BiConsumer;
import java.util.function.Function;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class BooleanBinding<T extends IMuteable> implements IBinding {

    static class NullBinding<T extends IMuteable> extends BooleanBinding<T> {
        NullBinding() {
            super(null, null);
        }

        @Override
        public void to(Function<T, Boolean> getter) {}

        @Override
        public void to(Function<T, Boolean> getter, BiConsumer<T, Boolean> setter) {}

        @Override
        public void to(Function<T, Boolean> getter, BiConsumer<T, Boolean> setter, boolean muteSetter) {}

        @Override
        public void updateValueFromSource() {}
    }

    private final BooleanProperty property;
    private final T boundObject;
    private Function<T, Boolean> getter;
    private BiConsumer<T, Boolean> setter;
    private boolean muteSetter;
    private boolean updating;

    private final ChangeListener<Boolean> changeListener =
        new ChangeListener<>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
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

    BooleanBinding(T boundObject, BooleanProperty property) {
        this.boundObject = boundObject;
        this.property = property;
    }

    @Override
    public void close() {
        property.removeListener(changeListener);
    }

    public void to(Function<T, Boolean> getter) {
        this.getter = getter;
        property.setValue(getter.apply(boundObject));
    }

    public void to(Function<T, Boolean> getter, BiConsumer<T, Boolean> setter) {
        to(getter, setter, false);
    }

    public void to(Function<T, Boolean> getter, BiConsumer<T, Boolean> setter, boolean muteSetter) {
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
