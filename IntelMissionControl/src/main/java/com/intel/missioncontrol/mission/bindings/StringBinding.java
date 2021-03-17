/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.mission.bindings;

import eu.mavinci.core.flightplan.IMuteable;
import java.util.function.BiConsumer;
import java.util.function.Function;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class StringBinding<T extends IMuteable> implements IBinding {

    static class NullBinding<T extends IMuteable> extends StringBinding<T> {
        NullBinding() {
            super(null, null);
        }

        @Override
        public void to(Function<T, String> getter) {}

        @Override
        public void to(Function<T, String> getter, BiConsumer<T, String> setter) {}

        @Override
        public void to(Function<T, String> getter, BiConsumer<T, String> setter, boolean muteSetter) {}

        @Override
        public void updateValueFromSource() {}
    }

    private final StringProperty property;
    private final T boundObject;
    private Function<T, String> getter;
    private BiConsumer<T, String> setter;
    private boolean muteSetter;
    private boolean updating;

    private final ChangeListener<String> changeListener =
        new ChangeListener<>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
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

    StringBinding(T boundObject, StringProperty property) {
        this.boundObject = boundObject;
        this.property = property;
    }

    @Override
    public void close() {
        property.removeListener(changeListener);
    }

    public void to(Function<T, String> getter) {
        this.getter = getter;
        property.setValue(getter.apply(boundObject));
    }

    public void to(Function<T, String> getter, BiConsumer<T, String> setter) {
        to(getter, setter, false);
    }

    public void to(Function<T, String> getter, BiConsumer<T, String> setter, boolean muteSetter) {
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
