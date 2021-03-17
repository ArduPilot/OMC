/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.beans.binding;

import org.asyncfx.beans.property.AsyncBooleanProperty;
import org.asyncfx.beans.property.ConsistencyGroup;
import org.asyncfx.beans.property.Critical;
import org.asyncfx.beans.property.PropertyMetadata;
import org.asyncfx.beans.property.SimpleAsyncBooleanProperty;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CriticalBindingsTest {

    @Test
    void Critical_BooleanBinding_Is_Never_Observed_In_Inconsistent_State() {
        ConsistencyGroup cg = new ConsistencyGroup();
        AsyncBooleanProperty p0 =
            new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().consistencyGroup(cg).create());
        AsyncBooleanProperty p1 =
            new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().consistencyGroup(cg).create());

        AsyncBooleanProperty r = new SimpleAsyncBooleanProperty(this);
        r.addListener((observable, oldValue, newValue) -> Assertions.assertTrue(newValue));

        r.bind(
            CriticalBindings.createBooleanBinding(
                (b0, b1) -> {
                    // Since this is a critical binding, we can never observe p0 and p1 as having different values.
                    Assertions.assertEquals(b0, b1);
                    return b0 && b1;
                },
                p0,
                p1));

        Critical.lock(
            p0,
            p1,
            () -> {
                p0.set(true);
                p1.set(true);
            });

        Assertions.assertTrue(r.get());
    }

}
