/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.beans.property;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import org.asyncfx.Awaiter;
import org.asyncfx.TestBase;
import org.asyncfx.beans.binding.LifecycleValueConverter;
import org.asyncfx.concurrent.Dispatcher;
import org.junit.jupiter.api.Test;

class AsyncObjectPropertyTest extends TestBase {

    enum Foo {
        BAR,
        BAZ,
        BAF
    }

    @Test
    void InvalidationListener_Is_Called_On_FxApplicationThread() {
        var awaiter = new Awaiter();

        AsyncObjectProperty<Foo> property =
            new SimpleAsyncObjectProperty<>(null, new PropertyMetadata.Builder<Foo>().customBean(true).create());
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
                property.set(Foo.BAR);
            });

        awaiter.await(1);
    }

    @Test
    void Binding_Is_Evaluated_On_FxApplicationThread() {
        var awaiter = new Awaiter();
        ObjectProperty<Foo> sourceProperty = new SimpleObjectProperty<>(Foo.BAR);
        AsyncObjectProperty<Foo> targetProperty =
            new UIAsyncObjectProperty<>(null, new UIPropertyMetadata.Builder<Foo>().customBean(true).create());

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
                sourceProperty.set(Foo.BAZ);
            });

        awaiter.await(2);
    }

    @Test
    void BidirectionalBinding_Is_Evaluated_On_FxApplicationThread() {
        var awaiter = new Awaiter();

        AsyncObjectProperty<Foo> sourceProperty =
            new SimpleAsyncObjectProperty<>(
                null, new PropertyMetadata.Builder<Foo>().customBean(true).initialValue(Foo.BAR).create());
        AsyncObjectProperty<Foo> targetProperty =
            new UIAsyncObjectProperty<>(null, new UIPropertyMetadata.Builder<Foo>().customBean(true).create());

        ChangeListener<? super Foo> changeListener =
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
        sourceProperty.set(Foo.BAZ);

        awaiter.await(2);

        targetProperty.removeListener(changeListener);

        // The source property listener may be called on any thread
        sourceProperty.addListener(changeListener);

        // We're on a non-UI thread here, so this fails:
        try {
            targetProperty.set(Foo.BAF);
            fail();
        } catch (Exception e) {
        }

        Platform.runLater(() -> targetProperty.set(Foo.BAF));

        awaiter.await(1);
    }

    static class A extends PropertyObject {
        final AsyncObjectProperty<B> b = new SimpleAsyncObjectProperty<>(this);
    }

    static class B extends PropertyObject {
        final AsyncObjectProperty<C> c0 = new SimpleAsyncObjectProperty<>(this);
        final AsyncObjectProperty<C> c1 = new SimpleAsyncObjectProperty<>(this);
        final AsyncObjectProperty<C> c2 = new SimpleAsyncObjectProperty<>(this);
    }

    static class C extends PropertyObject {
        final AsyncBooleanProperty d = new SimpleAsyncBooleanProperty(this);
    }

    @Test
    void Invalidation_Event_Bubbles_Up() {
        A a = new A();
        B b = new B();
        C c = new C();
        a.b.set(b);
        b.c0.set(c);

        boolean[] isInvalidation = new boolean[1];
        boolean[] isSubInvalidation = new boolean[1];

        // Regular invalidation listener
        a.b.addListener(observable -> isInvalidation[0] = true);

        // Sub-invalidation listener
        a.b.addListener((observable, subInvalidation) -> isSubInvalidation[0] = subInvalidation);

        c.d.set(true);

        assertFalse(isInvalidation[0]);
        assertTrue(isSubInvalidation[0]);
    }

    @Test
    void Change_Event_Bubbles_Up() {
        A a = new A();
        B b = new B();
        C c = new C();
        a.b.set(b);
        b.c0.set(c);

        boolean[] isChange = new boolean[1];
        boolean[] isSubChange = new boolean[1];

        // Regular change listener
        a.b.addListener((observable, oldValue, newValue) -> isChange[0] = true);

        // Sub-change listener
        a.b.addListener((observable, oldValue, newValue, subChange) -> isSubChange[0] = subChange);

        c.d.set(true);

        assertFalse(isChange[0]);
        assertTrue(isSubChange[0]);
    }

    @Test
    void SubInvalidation_Event_Is_Not_Raised_Again() {
        A a = new A();
        B b = new B();
        C c = new C();
        a.b.set(b);
        b.c0.set(c);

        int[] isInvalidated = new int[1];
        a.b.addListener((observable, subInvalidation) -> isInvalidated[0]++);

        c.d.set(true);
        assertEquals(1, isInvalidated[0]);

        c.d.set(false);
        assertEquals(1, isInvalidated[0]);
    }

    @Test
    void SubInvalidation_Event_Is_Not_Raised_Again_With_Bindings() {
        A a = new A();
        B b = new B();
        C c = new C();

        var prop0 = new SimpleAsyncObjectProperty<>(null, new PropertyMetadata.Builder<A>().customBean(true).create());
        var prop1 = new SimpleAsyncObjectProperty<>(null, new PropertyMetadata.Builder<A>().customBean(true).create());

        prop0.bind(prop1);
        prop1.set(a);
        a.b.set(b);
        b.c0.set(c);

        int[] isInvalidated = new int[1];
        prop0.addListener((observable, subInvalidation) -> isInvalidated[0]++);

        c.d.set(true);
        assertEquals(1, isInvalidated[0]);

        c.d.set(false);
        assertEquals(1, isInvalidated[0]);
    }

    @Test
    void SubChange_Event_Is_Raised_For_Every_Change() {
        A a = new A();
        B b = new B();
        C c = new C();
        a.b.set(b);
        b.c0.set(c);

        int[] isChange = new int[1];
        a.b.addListener((observable, oldValue, newValue, subChange) -> isChange[0]++);

        c.d.set(true);
        assertEquals(1, isChange[0]);

        c.d.set(false);
        assertEquals(2, isChange[0]);

        c.d.set(true);
        assertEquals(3, isChange[0]);
    }

    @Test
    void SubChange_Event_Is_Raised_For_Every_Change_With_Bindings() {
        A a = new A();
        B b = new B();
        C c = new C();
        a.b.set(b);
        b.c0.set(c);

        var prop0 = new SimpleAsyncObjectProperty<>(null, new PropertyMetadata.Builder<A>().customBean(true).create());
        var prop1 = new SimpleAsyncObjectProperty<>(null, new PropertyMetadata.Builder<A>().customBean(true).create());

        prop1.set(a);
        prop0.bind(prop1);

        int[] isChange = new int[1];
        prop0.addListener((observable, oldValue, newValue, subChange) -> isChange[0]++);

        c.d.set(true);
        assertEquals(1, isChange[0]);

        c.d.set(false);
        assertEquals(2, isChange[0]);

        c.d.set(true);
        assertEquals(3, isChange[0]);
    }

    @Test
    void Subtree_Invalidation_Works_In_Complex_Graph() {
        /*
                C0  C1   C2
                 \  |   /
                  \ | /
                    B
                  / | \
                /   |  \
              A0   A1   A1
             /      |    \
          prop0   prop1  prop2
        */

        A a0 = new A();
        A a1 = new A();
        A a2 = new A();
        B b = new B();
        C c0 = new C();
        C c1 = new C();
        C c2 = new C();
        a0.b.set(b);
        a1.b.set(b);
        a2.b.set(b);
        b.c0.set(c0);
        b.c1.set(c1);
        b.c2.set(c2);

        var prop0 = new SimpleAsyncObjectProperty<>(null, new PropertyMetadata.Builder<A>().customBean(true).create());
        var prop1 = new SimpleAsyncObjectProperty<>(null, new PropertyMetadata.Builder<A>().customBean(true).create());
        var prop2 = new SimpleAsyncObjectProperty<>(null, new PropertyMetadata.Builder<A>().customBean(true).create());

        // set() validates the entire subtrees of a0 and a1
        prop0.set(a0);
        prop1.set(a1);
        prop2.set(a2);

        int[] count0 = new int[1];
        int[] count1 = new int[1];
        int[] count2 = new int[1];

        // prop0 validates the subtree each time it receives an invalidation event by calling get()
        prop0.addListener(
            (listener, subInvalidation) -> {
                assertTrue(subInvalidation);
                count0[0]++;
                prop0.get();
            });

        // prop1 does not validate the subtree
        prop1.addListener(
            (listener, subInvalidation) -> {
                assertTrue(subInvalidation);
                count1[0]++;
            });

        // prop2 validates the subtree by registering a change listener
        prop2.addListener(
            (observable, oldValue, newValue, subChange) -> {
                assertSame(oldValue, newValue);
                assertTrue(subChange);
                count2[0]++;
            });

        c0.d.set(true);
        assertEquals(1, count0[0]);
        assertEquals(1, count1[0]);
        assertEquals(1, count2[0]);

        c1.d.set(true);
        assertEquals(2, count0[0]);
        assertEquals(1, count1[0]);
        assertEquals(2, count2[0]);

        c2.d.set(true);
        assertEquals(3, count0[0]);
        assertEquals(1, count1[0]);
        assertEquals(3, count2[0]);

        a0.b.set(null);
        assertEquals(4, count0[0]);
        assertEquals(1, count1[0]);
        assertEquals(3, count2[0]);

        a0.b.set(b);
        assertEquals(5, count0[0]);
        assertEquals(1, count1[0]);
        assertEquals(3, count2[0]);
    }

    static class NumberHolder {
        NumberHolder(Number value) {
            this.value = value;
        }

        Number value;
        boolean updated;
        boolean removed;
    }

    @Test
    void LifecycleValueConverter_Updates_And_Removes_TargetValue() {
        var source =
            new SimpleAsyncIntegerProperty(null, new PropertyMetadata.Builder<Number>().customBean(true).create());
        var target =
            new SimpleAsyncObjectProperty<>(
                null, new PropertyMetadata.Builder<NumberHolder>().customBean(true).create());

        target.bind(
            source,
            new LifecycleValueConverter<>() {
                @Override
                public NumberHolder convert(Number value) {
                    return new NumberHolder(value);
                }

                @Override
                public void update(Number sourceValue, NumberHolder targetValue) {
                    targetValue.value = sourceValue;
                    targetValue.updated = true;
                }

                @Override
                public void remove(NumberHolder value) {
                    value.removed = true;
                }
            });

        NumberHolder t = target.get();
        assertEquals(0, t.value);
        assertFalse(t.updated);
        assertFalse(t.removed);

        source.set(1);
        assertSame(target.get(), t);
        assertTrue(t.updated);
        assertFalse(t.removed);

        target.unbind();
        assertSame(target.get(), t);
        assertTrue(t.updated);
        assertFalse(t.removed);

        target.set(new NumberHolder(1));
        assertNotSame(target.get(), t);
        assertTrue(t.removed);
    }

}
