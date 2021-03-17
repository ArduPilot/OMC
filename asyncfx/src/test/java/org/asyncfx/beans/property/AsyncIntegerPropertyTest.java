/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.beans.property;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.concurrent.locks.LockSupport;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import org.asyncfx.Awaiter;
import org.asyncfx.TestBase;
import org.asyncfx.concurrent.Dispatcher;
import org.junit.jupiter.api.Test;

class AsyncIntegerPropertyTest extends TestBase {
    public static String getThreadState(Thread thread) {
        StringBuilder builder = new StringBuilder("Thread \"");
        builder.append(thread.getName());
        builder.append("\" is ");
        builder.append(thread.getState());
        Object blocker = LockSupport.getBlocker(thread);
        if (blocker != null) builder.append(" for ").append(blocker);
        for (StackTraceElement e : thread.getStackTrace()) {
            builder.append("\n\tat ");
            builder.append(e);
        }

        return builder.toString();
    }

    @Test
    void testCircularListeners_should_run_and_terminate() {
        AsyncIntegerProperty a =
            new SimpleAsyncIntegerProperty(null, new PropertyMetadata.Builder<Number>().customBean(true).create());
        AsyncIntegerProperty b =
            new SimpleAsyncIntegerProperty(null, new PropertyMetadata.Builder<Number>().customBean(true).create());
        a.addListener((observable, from, to) -> b.set(to.intValue() * 2));
        a.set(123);

        assertEquals(246, b.get());

        b.addListener((observable, from, to) -> a.set(to.intValue() / 2));
        a.set(333);

        assertEquals(333, a.get());
        assertEquals(666, b.get());
    }

    @Test
    void testCircularListenerBindingIntToString_should_run_and_terminate() {
        AsyncIntegerProperty a =
            new SimpleAsyncIntegerProperty(null, new PropertyMetadata.Builder<Number>().customBean(true).create());
        AsyncStringProperty b =
            new SimpleAsyncStringProperty(null, new PropertyMetadata.Builder<String>().customBean(true).create());
        b.bind(a, num -> Integer.toString(num.intValue() * 2));
        a.set(123);

        assertEquals("246", b.get());

        b.addListener((observable, from, to) -> a.set(Integer.parseInt(to) / 2));
        a.set(333);

        assertEquals(333, a.get());
        assertEquals("666", b.get());
    }

    @Test
    void testCircularListenerBindingStringToInt_should_run_and_terminate() {
        AsyncStringProperty a =
            new SimpleAsyncStringProperty(null, new PropertyMetadata.Builder<String>().customBean(true).create());
        a.set("0");
        AsyncIntegerProperty b =
            new SimpleAsyncIntegerProperty(null, new PropertyMetadata.Builder<Number>().customBean(true).create());
        b.bind(a, num -> Integer.parseInt(num) * 2);
        a.set("123");

        assertEquals(246, b.get());

        b.addListener((observable, from, to) -> a.set(Integer.toString(to.intValue() / 2)));
        a.set("333");

        assertEquals("333", a.get());
        assertEquals(666, b.get());
    }

    @Test
    void InvalidationListener_Is_Called_On_FxApplicationThread() {
        var awaiter = new Awaiter();

        AsyncIntegerProperty property =
            new SimpleAsyncIntegerProperty(null, new PropertyMetadata.Builder<Number>().customBean(true).create());
        property.addListener(
            listener -> {
                awaiter.assertTrue(Platform.isFxApplicationThread());
                awaiter.signal();
            },
            Platform::runLater);

        Dispatcher dispatcher = Dispatcher.background();
        dispatcher.run(
            () -> {
                awaiter.assertTrue(!Platform.isFxApplicationThread());
                property.set(10);
            });

        awaiter.await(1);
    }

    @Test
    void Binding_Is_Evaluated_On_FxApplicationThread() {
        var awaiter = new Awaiter();

        IntegerProperty sourceProperty = new SimpleIntegerProperty(5);
        AsyncIntegerProperty targetProperty =
            new UIAsyncIntegerProperty(null, new UIPropertyMetadata.Builder<Number>().customBean(true).create());

        targetProperty.addListener(
            (observable, oldValue, newValue) -> {
                awaiter.assertTrue(Platform.isFxApplicationThread());
                awaiter.signal();
            });

        targetProperty.bind(sourceProperty);

        Dispatcher dispatcher = Dispatcher.background();
        dispatcher.run(
            () -> {
                awaiter.assertTrue(!Platform.isFxApplicationThread());
                sourceProperty.set(10);
            });

        awaiter.await(2);
    }

    @Test
    void BidirectionalBinding_Is_Evaluated_On_FxApplicationThread() {
        var awaiter = new Awaiter();

        AsyncIntegerProperty sourceProperty =
            new SimpleAsyncIntegerProperty(
                null, new PropertyMetadata.Builder<Number>().name("source").customBean(true).initialValue(5).create());
        AsyncIntegerProperty targetProperty =
            new UIAsyncIntegerProperty(
                null, new UIPropertyMetadata.Builder<Number>().name("target").customBean(true).create());

        ChangeListener<? super Number> changeListener =
            (observable, oldValue, newValue) -> {
                awaiter.assertTrue(Platform.isFxApplicationThread());
                awaiter.signal();
            };

        // This listener will always be called on the UI thread
        targetProperty.addListener(changeListener);

        // If the source property changes, the change will always be propagated to the target property on the UI
        // thread
        targetProperty.bindBidirectional(sourceProperty);

        // We're on a non-UI thread here:
        assertTrue(!Platform.isFxApplicationThread());
        sourceProperty.set(10);

        awaiter.await(2);

        targetProperty.removeListener(changeListener);

        // The source property listener may be called on any thread
        sourceProperty.addListener(changeListener);

        // We're on a non-UI thread here, so this fails:
        try {
            targetProperty.set(20);
            fail();
        } catch (Exception e) {
        }

        // this succeeds:
        Platform.runLater(() -> targetProperty.set(20));

        awaiter.await(1);

        assertEquals(20, sourceProperty.get());
    }

    @Test
    void ThreeWayBinding_Is_Evaluated_On_Correct_Threads() {
        var awaiter = new Awaiter();

        AsyncIntegerProperty prop1 =
            new SimpleAsyncIntegerProperty(
                null, new PropertyMetadata.Builder<Number>().name("prop1").customBean(true).initialValue(1).create());

        AsyncIntegerProperty prop2 =
            new UIAsyncIntegerProperty(
                null, new UIPropertyMetadata.Builder<Number>().name("prop2").customBean(true).initialValue(2).create());

        AsyncIntegerProperty prop3 =
            new SimpleAsyncIntegerProperty(
                null,
                new PropertyMetadata.Builder<Number>()
                    .name("prop3")
                    .customBean(true)
                    .dispatcher(Dispatcher.background())
                    .initialValue(3)
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

        prop1.set(999);

        awaiter.await(3);

        assertTrue(prop1.get() == 999 && prop2.get() == 999 && prop3.get() == 999);
    }

    @Test
    void UnbindBidirectional_Removes_Listener() {
        AsyncIntegerProperty prop1 =
            new SimpleAsyncIntegerProperty(null, new PropertyMetadata.Builder<Number>().customBean(true).create());
        AsyncIntegerProperty prop2 =
            new SimpleAsyncIntegerProperty(null, new PropertyMetadata.Builder<Number>().customBean(true).create());

        var count = new int[1];

        prop1.addListener(((observable, oldValue, newValue) -> count[0]++));
        assertEquals(0, count[0]);

        prop1.bindBidirectional(prop2);
        assertEquals(0, count[0]);

        prop2.set(1);
        assertEquals(1, count[0]);

        System.gc();

        prop2.set(2);
        assertEquals(2, count[0]);

        prop1.unbindBidirectional(prop2);
        prop2.set(3);
        prop2.set(4);
        prop2.set(5);
        assertEquals(2, count[0]);
    }

    @Test
    void Add_Binding_Will_Call_Listener_On_FxApplicationThread() {
        var awaiter = new Awaiter();

        AsyncIntegerProperty prop1 =
            new UIAsyncIntegerProperty(null, new UIPropertyMetadata.Builder<Number>().customBean(true).create());
        AsyncIntegerProperty prop2 =
            new SimpleAsyncIntegerProperty(
                null, new PropertyMetadata.Builder<Number>().customBean(true).initialValue(1).create());
        var count = new int[1];

        prop1.addListener(
            ((observable, oldValue, newValue) -> {
                awaiter.assertTrue(Platform.isFxApplicationThread());
                count[0]++;
                awaiter.signal();
            }));

        prop1.bind(prop2);

        awaiter.await(1);

        assertEquals(1, count[0]);

        prop2.set(2);
        awaiter.await(1);

        assertEquals(2, count[0]);
    }

    @Test
    void Setting_Property_Value_On_Wrong_Thread_Will_Fail() {
        AsyncIntegerProperty prop1 =
            new UIAsyncIntegerProperty(null, new UIPropertyMetadata.Builder<Number>().customBean(true).create());
        AsyncIntegerProperty prop2 =
            new SimpleAsyncIntegerProperty(null, new PropertyMetadata.Builder<Number>().customBean(true).create());

        // Fails: prop1 can't be set from a non-UI thread.
        try {
            prop1.set(1);
            fail();
        } catch (IllegalStateException e) {
        }

        // Works
        try {
            prop1.bind(prop2);
        } catch (IllegalStateException e) {
            fail();
        }

        // Works
        try {
            prop2.set(2);
        } catch (IllegalStateException e) {
            fail();
        }
    }

    @Test
    void InvalidationListener_Will_Be_Called_Only_Once_If_Getter_Is_Not_Called() {
        int[] count = new int[1];
        var prop =
            new SimpleAsyncIntegerProperty(null, new PropertyMetadata.Builder<Number>().customBean(true).create());
        prop.addListener(listener -> count[0]++);
        prop.set(1);
        assertEquals(1, count[0]);
        prop.set(2);
        assertEquals(1, count[0]);
        prop.set(3);
        assertEquals(1, count[0]);
    }

    @Test
    void ChangeListener_Will_Be_Called_For_Every_Change() {
        int[] count = new int[1];
        var prop =
            new SimpleAsyncIntegerProperty(null, new PropertyMetadata.Builder<Number>().customBean(true).create());
        prop.addListener((observable, oldValue, newValue) -> count[0]++);
        prop.set(1);
        assertEquals(1, count[0]);
        prop.set(2);
        assertEquals(2, count[0]);
        prop.set(3);
        assertEquals(3, count[0]);
    }

    @Test
    void InvalidationListener_Is_Always_Called_If_ChangeListener_Is_Present() {
        int[] count = new int[1];
        var prop =
            new SimpleAsyncIntegerProperty(null, new PropertyMetadata.Builder<Number>().customBean(true).create());
        prop.addListener(listener -> count[0]++);
        prop.addListener((observable, oldValue, newValue) -> count[0]++);
        prop.set(1);
        assertEquals(2, count[0]);
        prop.set(2);
        assertEquals(4, count[0]);
        prop.set(3);
        assertEquals(6, count[0]);
    }

}
