/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.beans.property;

import static org.junit.jupiter.api.Assertions.assertTrue;

import javafx.application.Platform;
import org.asyncfx.Awaiter;
import org.asyncfx.TestBase;
import org.asyncfx.concurrent.SynchronizationContext;
import org.junit.jupiter.api.Test;

class AsyncStringPropertyTest extends TestBase {

    @Test
    void ThreeWayBinding_Is_Evaluated_On_Correct_Threads() {
        var awaiter = new Awaiter();
        var syncCtx = SynchronizationContext.getCurrent();

        AsyncStringProperty prop1 =
            new SimpleAsyncStringProperty(
                null,
                new PropertyMetadata.Builder<String>()
                    .name("prop1")
                    .customBean(true)
                    .initialValue("prop1-init")
                    .create());

        AsyncStringProperty prop2 =
            new UIAsyncStringProperty(
                null,
                new UIPropertyMetadata.Builder<String>()
                    .name("prop2")
                    .customBean(true)
                    .initialValue("prop2-init")
                    .create());

        AsyncStringProperty prop3 =
            new SimpleAsyncStringProperty(
                null,
                new PropertyMetadata.Builder<String>()
                    .name("prop3")
                    .customBean(true)
                    .synchronizationContext(syncCtx)
                    .initialValue("prop3-init")
                    .create());

        prop2.bindBidirectional(prop1);
        prop2.bindBidirectional(prop3);

        prop1.addListener(
            ((observable, oldValue, newValue) -> {
                awaiter.assertTrue(!Platform.isFxApplicationThread());
                awaiter.signal();
            }));

        prop2.addListener(
            ((observable, oldValue, newValue) -> {
                awaiter.assertTrue(Platform.isFxApplicationThread());
                awaiter.signal();
            }));

        prop3.addListener(
            ((observable, oldValue, newValue) -> {
                awaiter.assertTrue(!Platform.isFxApplicationThread());
                awaiter.signal();
            }));

        prop1.set("test");

        awaiter.await(3);

        assertTrue(prop1.get().equals("test") && prop2.get().equals("test") && prop3.get().equals("test"));
    }

}
