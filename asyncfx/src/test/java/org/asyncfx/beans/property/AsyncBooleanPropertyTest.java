/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.beans.property;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ReadOnlyProperty;
import org.asyncfx.Awaiter;
import org.asyncfx.TestBase;
import org.asyncfx.concurrent.Dispatcher;
import org.junit.jupiter.api.Test;

class AsyncBooleanPropertyTest extends TestBase {

    @Test
    void UIBooleanProperty_createBinding_is_on_UI_thread() {
        var awaiter = new Awaiter();

        AsyncBooleanProperty sourceProp1 =
            new SimpleAsyncBooleanProperty(null, new PropertyMetadata.Builder<Boolean>().customBean(true).create());

        sourceProp1.setValue(false);

        UIAsyncBooleanProperty targetProp1 =
            new UIAsyncBooleanProperty(null, new UIPropertyMetadata.Builder<Boolean>().customBean(true).create());

        targetProp1.bind(sourceProp1);

        ReadOnlyProperty<Boolean> readOnlyProp1 = targetProp1;

        AsyncBooleanProperty sourceProp2 =
            new SimpleAsyncBooleanProperty(null, new PropertyMetadata.Builder<Boolean>().customBean(true).create());

        sourceProp2.setValue(false);

        UIAsyncBooleanProperty targetProp2 =
            new UIAsyncBooleanProperty(null, new UIPropertyMetadata.Builder<Boolean>().customBean(true).create());

        targetProp2.bind(sourceProp2);

        ReadOnlyProperty<Boolean> readOnlyProp2 = targetProp2;

        StringBinding stringBinding =
            Bindings.createStringBinding(
                () -> readOnlyProp1.getValue().toString() + ", " + readOnlyProp2.getValue().toString(),
                readOnlyProp1,
                readOnlyProp2);

        stringBinding.addListener(
            listener -> {
                awaiter.assertTrue(Platform.isFxApplicationThread());
                if (stringBinding.get().equals("true, true")) {
                    awaiter.signal();
                }
            });

        Dispatcher dispatcher = Dispatcher.background();

        dispatcher.run(
            () -> {
                awaiter.assertTrue(!Platform.isFxApplicationThread());
                sourceProp1.set(true);
            });

        dispatcher.run(
            () -> {
                awaiter.assertTrue(!Platform.isFxApplicationThread());
                sourceProp2.set(true);
            });

        awaiter.await(1);
    }

    static class Base {
        final ConsistencyGroup CG = new ConsistencyGroup();

        final AsyncBooleanProperty a =
            new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().consistencyGroup(CG).create());

        final AsyncBooleanProperty b =
            new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().consistencyGroup(CG).create());

        final AsyncBooleanProperty c =
            new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().consistencyGroup(CG).create());
    }

    static class Derived extends Base {
        final AsyncBooleanProperty d =
            new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().consistencyGroup(CG).create());
    }

    @Test
    void ChangeEvents_Are_Deferred_In_CriticalSection() {
        int[] count = new int[1];

        Base obj = new Base();
        obj.a.addListener((observable, oldValue, newValue) -> count[0]++);
        obj.b.addListener((observable, oldValue, newValue) -> count[0]++);

        Critical.lock(
            obj.a,
            () -> {
                assertEquals(0, count[0]);
                assertFalse(obj.a.get());
                obj.a.set(true);
                assertTrue(obj.a.get());
                assertEquals(0, count[0]);
                assertFalse(obj.b.get());
                obj.b.set(true);
                assertTrue(obj.b.get());
                assertEquals(0, count[0]);
            });

        assertEquals(2, count[0]);
    }

    @Test
    void ChangeEvents_Are_Deferred_In_Nested_CriticalSection() {
        int[] count = new int[1];

        Base obj = new Base();
        obj.a.addListener((observable, oldValue, newValue) -> count[0]++);
        obj.b.addListener((observable, oldValue, newValue) -> count[0]++);
        obj.c.addListener((observable, oldValue, newValue) -> count[0]++);

        Critical.lock(
            obj.b,
            () -> {
                Critical.lock(
                    obj.a,
                    () -> {
                        Critical.lock(
                            obj.a,
                            () -> {
                                obj.a.set(true);
                                obj.b.set(true);
                                obj.c.set(true);
                            });

                        // No events fired yet...
                        assertEquals(0, count[0]);
                    });

                // And not yet...
                assertEquals(0, count[0]);
            });

        // Only now have the events been fired.
        assertEquals(3, count[0]);
    }

    @Test
    void Extending_LockedGroup_In_Nested_CriticalSection_Fails() {
        Base obj = new Base();
        Base obj2 = new Base();

        Critical.lock(
            obj.b,
            () -> {
                try {
                    Critical.lock(obj2.a, () -> {});
                    fail();
                } catch (IllegalStateException expected) {
                }
            });
    }

    @Test
    void CriticalSection_Locks_Properties_Of_Different_Objects() {
        int[] count = new int[1];

        Base obj0 = new Base();
        Base obj1 = new Base();
        obj0.a.addListener((observable, oldValue, newValue) -> count[0]++);
        obj0.b.addListener((observable, oldValue, newValue) -> count[0]++);
        obj0.c.addListener((observable, oldValue, newValue) -> count[0]++);
        obj1.a.addListener((observable, oldValue, newValue) -> count[0]++);
        obj1.b.addListener((observable, oldValue, newValue) -> count[0]++);
        obj1.c.addListener((observable, oldValue, newValue) -> count[0]++);

        Critical.lock(
            obj0.a,
            obj1.b,
            () -> {
                assertEquals(0, count[0]);
                obj0.a.set(true);
                obj0.b.set(true);
                obj1.b.set(true);
                obj1.c.set(true);
                assertEquals(0, count[0]);
            });

        assertEquals(4, count[0]);
    }

    @Test
    void ChangeEvents_Are_Deferred_In_CriticalSection_With_Extended_LockGroup() {
        int[] count = new int[1];

        Derived obj = new Derived();
        obj.a.addListener((observable, oldValue, newValue) -> count[0]++);
        obj.b.addListener((observable, oldValue, newValue) -> count[0]++);
        obj.c.addListener((observable, oldValue, newValue) -> count[0]++);
        obj.d.addListener((observable, oldValue, newValue) -> count[0]++);

        Critical.lock(
            obj.a,
            () -> {
                assertEquals(0, count[0]);
                obj.c.set(true);
                obj.d.set(true);
                assertEquals(0, count[0]);
            });

        assertEquals(2, count[0]);
    }

    @Test
    void Property_Access_Fails_Outside_Of_CriticalSection() {
        Base obj = new Derived();

        try {
            obj.a.set(true);
            fail();
        } catch (IllegalStateException expected) {
        } catch (Exception ex) {
            fail();
        }

        Critical.lock(obj.a, obj.b, obj.c, () -> obj.a.set(true));
    }

    @Test
    void Consistency_Is_Preserved_When_Reading_From_Multiple_Threads() throws InterruptedException {
        final int threadCount = 50;
        final int iterations = 10000;

        boolean[] stop = new boolean[1];
        Thread[] threads = new Thread[threadCount];
        Base obj = new Base();

        // These threads will observe the value of a, b and c.
        for (int i = 0; i < threadCount; ++i) {
            threads[i] =
                new Thread(
                    () -> {
                        while (!stop[0]) {
                            Critical.lock(
                                obj.a,
                                () -> {
                                    assertEquals(obj.a.get(), obj.b.get());
                                    assertEquals(obj.b.get(), obj.c.get());
                                });
                        }
                    }, "Consistency_Is_Preserved_When_Reading_From_Multiple_Threads-"+i);
            threads[i].setDaemon(true);
            threads[i].start();
        }

        Thread.sleep(100);

        for (int i = 0; i < iterations; ++i) {
            // In this critical section, we're setting a, b, and c to the same value.
            // In the observing threads, we test whether at any point a, b or c don't have the same value.
            Critical.lock(
                obj.a,
                () -> {
                    boolean flag = obj.a.get();
                    obj.a.set(!flag);
                    obj.b.set(!flag);
                    obj.c.set(!flag);
                });
        }

        stop[0] = true;
        for (int i = 0; i < threadCount; ++i) {
            threads[i].join();
        }
    }

}
