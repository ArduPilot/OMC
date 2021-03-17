/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.beans.property;

import java.util.ArrayList;
import java.util.List;

/** @see Critical */
public final class ConsistencyGroup {

    private List<ReadOnlyAsyncProperty> initList = new ArrayList<>(5);
    private volatile ReadOnlyAsyncProperty[] properties;

    void add(ReadOnlyAsyncProperty property) {
        synchronized (this) {
            if (initList == null) {
                throw new IllegalStateException(
                    ConsistencyGroup.class.getSimpleName() + " cannot be modified after initialization.");
            }

            initList.add(property);
        }
    }

    final ReadOnlyAsyncProperty[] getProperties() {
        ReadOnlyAsyncProperty[] propertiesCopy = properties;
        if (propertiesCopy == null) {
            synchronized (this) {
                propertiesCopy = properties;
                if (propertiesCopy == null) {
                    properties = propertiesCopy = initList.toArray(new ReadOnlyAsyncProperty[0]);
                    initList = null;
                }
            }
        }

        return propertiesCopy;
    }

}
