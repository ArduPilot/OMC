/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.beans.property;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.value.ObservableValue;

public final class PropertyPathStore {

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private final List list = new ArrayList<>();

    @SuppressWarnings("unchecked")
    void add(ReadOnlyProperty<?> property) {
        list.add(property);
    }

    @SuppressWarnings("unchecked")
    void add(ReadOnlyAsyncProperty<?> property) {
        list.add(property);
    }

    public <T> PropertyPath.PropertyPathSegment<Void, T> from(ObservableValue<T> observable) {
        return new PropertyPath.PropertyPathSegment<>(this, observable);
    }

    public void clear() {
        list.clear();
    }

}
