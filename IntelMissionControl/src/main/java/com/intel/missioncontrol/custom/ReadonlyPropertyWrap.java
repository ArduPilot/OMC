/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.custom;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;

public class ReadonlyPropertyWrap<T> extends ReadOnlyObjectWrapper<T> {
    public ReadonlyPropertyWrap(ObservableValue<T> wrappedValue) {
        super(wrappedValue.getValue());
        wrappedValue.addListener(
            ((observable, oldValue, newValue) -> {
                this.set(newValue);
            }));
    }
}
