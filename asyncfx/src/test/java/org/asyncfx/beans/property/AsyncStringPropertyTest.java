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
import org.asyncfx.concurrent.Dispatcher;
import org.junit.jupiter.api.Test;

class AsyncStringPropertyTest extends TestBase {

    @Test
    void ThreeWayBinding_Is_Evaluated_On_Correct_Threads() {
        var awaiter = new Awaiter();

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
                    .initialValue("prop3-init")
                    .dispatcher(Dispatcher.background())
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

    static class A extends PropertyObject {
        final AsyncObjectProperty<String> string1 = new SimpleAsyncObjectProperty<>(this);
        final AsyncObjectProperty<String> string2 = new SimpleAsyncObjectProperty<>(this);
    }

    @Test
    void Properties_With_Shared_AccessController_Can_Be_Bound() {
        A a = new A();
        a.string1.bind(a.string2);
    }

}
