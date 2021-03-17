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

import java.time.Duration;
import java.util.HashSet;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlySetProperty;
import javafx.collections.SetChangeListener;
import org.asyncfx.Awaiter;
import org.asyncfx.TestBase;
import org.asyncfx.collections.AsyncObservableSet;
import org.asyncfx.collections.FXAsyncCollections;
import org.asyncfx.collections.LockedSet;
import org.asyncfx.concurrent.Dispatcher;
import org.asyncfx.concurrent.SynchronizationContext;
import org.junit.jupiter.api.Test;

class AsyncSetPropertyTest extends TestBase {

    @Test
    void Adding_Multiple_Items_To_Unlocked_Set_Calls_SetChangeListener_For_Each_Item() {
        var count = new int[1];
        var prop =
            new SimpleAsyncSetProperty<>(
                null,
                new PropertyMetadata.Builder<AsyncObservableSet<Integer>>()
                    .initialValue(FXAsyncCollections.observableSet(new HashSet<>()))
                    .customBean(true)
                    .create());
        prop.addListener(((SetChangeListener<Integer>)c -> count[0]++));
        prop.add(1);
        prop.add(2);
        prop.add(3);

        assertEquals(3, count[0]);
    }

    @Test
    void Modifying_Locked_Set_Calls_SetChangeListener_Four_Times() {
        var count = new int[1];
        var prop =
            new SimpleAsyncSetProperty<>(
                null,
                new PropertyMetadata.Builder<AsyncObservableSet<Integer>>()
                    .initialValue(FXAsyncCollections.observableSet(new HashSet<>()))
                    .customBean(true)
                    .create());
        prop.addListener(((SetChangeListener<Integer>)c -> count[0]++));

        try (var locked = prop.lock()) {
            locked.add(1);
            locked.add(2);
            locked.clear();
        }

        assertEquals(4, count[0]);
    }

    @Test
    void Listener_Should_Be_Called_On_FxApplicationThread() {
        var awaiter = new Awaiter();

        assertTrue(!Platform.isFxApplicationThread());

        var simpleListProp =
            new SimpleAsyncSetProperty<>(
                null,
                new PropertyMetadata.Builder<AsyncObservableSet<Integer>>()
                    .initialValue(FXAsyncCollections.observableSet(new HashSet<>()))
                    .customBean(true)
                    .create());

        simpleListProp.addListener(
            (SetChangeListener<Integer>)
                c -> {
                    awaiter.assertTrue(Platform.isFxApplicationThread());
                    awaiter.signal();
                },
            Dispatcher.platform());

        try (var view = simpleListProp.lock()) {
            view.add(1);
            view.add(2);
        }

        awaiter.await(1);
    }

    @Test
    void UIAsyncSetProperty_Binding_Should_Be_Evaluated_On_FxApplicationThread() {
        var awaiter = new Awaiter();

        var sourceListProp =
            new SimpleAsyncSetProperty<>(
                null,
                new PropertyMetadata.Builder<AsyncObservableSet<Integer>>()
                    .initialValue(FXAsyncCollections.observableSet(new HashSet<>()))
                    .customBean(true)
                    .create());

        sourceListProp.addListener((SetChangeListener<Integer>)c -> assertTrue(!Platform.isFxApplicationThread()));

        var targetListProp =
            new UIAsyncSetProperty<>(
                null, new UIPropertyMetadata.Builder<AsyncObservableSet<Integer>>().customBean(true).create());

        targetListProp.addListener(
            (SetChangeListener<Integer>)
                c -> {
                    awaiter.assertTrue(Platform.isFxApplicationThread());
                    awaiter.signal();
                },
            Dispatcher.platform());

        targetListProp.bind(sourceListProp);

        try (var view = sourceListProp.lock()) {
            view.add(1);
            view.add(2);
            view.add(3);
        }

        awaiter.await(1);
    }

    private interface ITestData {
        int getData();
    }

    private class TestData implements ITestData {
        @Override
        public int getData() {
            return 123;
        }
    }

    @Test
    void AsyncSetProperty_Adding_To_Empty_Set_Should_Throw() {
        var sourceListProp = new SimpleAsyncSetProperty<>(this);
        sourceListProp.set(FXAsyncCollections.observableSet(new HashSet<>()));

        var targetListProp = new SimpleAsyncSetProperty<>(this);
        targetListProp.bindContent(sourceListProp);

        var data = new TestData();
        sourceListProp.add(data);

        assertFalse(targetListProp.contains(data));
    }

    @Test
    void AsyncSetProperty_ContentBinding_To_Empty_Set_Should_Work() {
        var sourceListProp = new SimpleAsyncSetProperty<>(this);
        sourceListProp.set(FXAsyncCollections.emptyObservableSet());

        var targetListProp = new SimpleAsyncSetProperty<>(this);
        targetListProp.bindContent(sourceListProp);
    }

    @Test
    void BidirectionalBinding_Is_Evaluated_On_Another_Thread() {
        var awaiter = new Awaiter();
        var syncCtx = SynchronizationContext.getCurrent();

        var sourceListProp1 =
            new SimpleAsyncSetProperty<>(
                null,
                new PropertyMetadata.Builder<AsyncObservableSet<Integer>>()
                    .customBean(true)
                    .name("source")
                    .synchronizationContext(syncCtx)
                    .initialValue(FXAsyncCollections.observableSet(new HashSet<>()))
                    .create());

        var targetListProp1 =
            new SimpleAsyncSetProperty<>(
                null,
                new PropertyMetadata.Builder<AsyncObservableSet<Integer>>()
                    .customBean(true)
                    .name("target")
                    .synchronizationContext(syncCtx)
                    .initialValue(null)
                    .create());

        targetListProp1.bindBidirectional(sourceListProp1);
        targetListProp1.addListener(
            (SetChangeListener<Integer>)
                c -> {
                    awaiter.assertTrue(!Platform.isFxApplicationThread());
                    awaiter.signal();
                });

        sourceListProp1.add(1);
        awaiter.await(1);

        sourceListProp1.add(2);
        awaiter.await(1);

        assertTrue(sourceListProp1.contains(1));
        assertTrue(sourceListProp1.contains(2));
        assertTrue(targetListProp1.contains(1));
        assertTrue(targetListProp1.contains(2));
    }

    @Test
    void BidirectionalBinding_Is_Evaluated_On_FxApplicationThread() {
        var awaiter = new Awaiter();
        var syncCtx = SynchronizationContext.getCurrent();

        var sourceListProp2 =
            new SimpleAsyncSetProperty<>(
                null,
                new PropertyMetadata.Builder<AsyncObservableSet<Integer>>()
                    .customBean(true)
                    .synchronizationContext(syncCtx)
                    .initialValue(FXAsyncCollections.observableSet(new HashSet<>()))
                    .create());

        var targetListProp2 =
            new UIAsyncSetProperty<>(
                null,
                new UIPropertyMetadata.Builder<AsyncObservableSet<Integer>>()
                    .customBean(true)
                    .initialValue(FXAsyncCollections.observableSet(new HashSet<>()))
                    .create());

        targetListProp2.addListener(
            (SetChangeListener<Integer>)
                c -> {
                    awaiter.assertTrue(Platform.isFxApplicationThread());
                    awaiter.signal();
                });

        Dispatcher.platform()
            .run(
                () -> {
                    targetListProp2.bindBidirectional(sourceListProp2);
                    awaiter.signal();
                });

        awaiter.await(1);

        sourceListProp2.add(1);
        awaiter.await(1);

        sourceListProp2.add(2);
        awaiter.await(1);

        assertTrue(sourceListProp2.contains(1));
        assertTrue(sourceListProp2.contains(2));
        assertTrue(targetListProp2.contains(1));
        assertTrue(targetListProp2.contains(2));
    }

    @Test
    void Listeners_Can_Be_Added_And_Removed() {
        var sourceListProp1 =
            new SimpleAsyncSetProperty<>(
                null,
                new PropertyMetadata.Builder<AsyncObservableSet<Integer>>()
                    .customBean(true)
                    .name("source")
                    .initialValue(FXAsyncCollections.observableSet(new HashSet<>()))
                    .create());

        var targetListProp1 =
            new SimpleAsyncSetProperty<>(
                null,
                new PropertyMetadata.Builder<AsyncObservableSet<Integer>>()
                    .customBean(true)
                    .name("target")
                    .initialValue(null)
                    .create());

        int[] sourceCount = new int[1];
        int[] targetCount = new int[1];

        SetChangeListener<Integer> sourceListChangeListener = change -> sourceCount[0]++;
        SetChangeListener<Integer> targetListChangeListener = change -> targetCount[0]++;

        targetListProp1.bindBidirectional(sourceListProp1);
        targetListProp1.addListener(targetListChangeListener);

        sourceListProp1.add(1);
        assertEquals(0, sourceCount[0]);
        assertEquals(1, targetCount[0]);

        targetListProp1.add(2);
        assertEquals(0, sourceCount[0]);
        assertEquals(2, targetCount[0]);

        targetListProp1.removeListener(targetListChangeListener);
        sourceListProp1.addListener(sourceListChangeListener);

        targetListProp1.add(3);
        assertEquals(1, sourceCount[0]);
        assertEquals(2, targetCount[0]);

        sourceListProp1.add(4);
        assertEquals(2, sourceCount[0]);
        assertEquals(2, targetCount[0]);
    }

    @Test
    void Multiple_Concurrent_AddRemove_Operations_Do_Not_Corrupt_AsyncSet() {
        final int CONCURRENT_THREADS = 10;
        final int ITERATIONS = 500;
        final int NUMBERS = 50;

        var sourceListProp =
            new SimpleAsyncSetProperty<>(
                null,
                new PropertyMetadata.Builder<AsyncObservableSet<Integer>>()
                    .customBean(true)
                    .name("source")
                    .initialValue(FXAsyncCollections.observableSet(new HashSet<>()))
                    .create());

        var targetListProp =
            new UIAsyncSetProperty<>(
                null,
                new UIPropertyMetadata.Builder<AsyncObservableSet<Integer>>()
                    .customBean(true)
                    .name("target")
                    .initialValue(FXAsyncCollections.observableSet(new HashSet<>()))
                    .create());

        targetListProp.bindContent(sourceListProp);

        var awaiter = new Awaiter();

        targetListProp.addListener(
            (SetChangeListener<Integer>)
                c -> {
                    awaiter.assertTrue(Platform.isFxApplicationThread());
                    awaiter.signal();
                });

        assertFalse(Platform.isFxApplicationThread());

        Consumer<Integer> changeListFunc =
            threadNumber -> {
                final int offset = threadNumber * NUMBERS;

                for (int i = 0; i < ITERATIONS; ++i) {
                    try (var list = sourceListProp.lock()) {
                        for (int j = 0; j < NUMBERS; ++j) {
                            list.add(j + offset);
                        }
                    }

                    try (var list = sourceListProp.lock()) {
                        for (int j = 0; j < NUMBERS; ++j) {
                            list.remove(j + offset);
                        }
                    }

                    awaiter.signal();
                }
            };

        for (int i = 0; i < CONCURRENT_THREADS; ++i) {
            final int I = i;
            Dispatcher.background().run(() -> changeListFunc.accept(I));
        }

        awaiter.await(CONCURRENT_THREADS * ITERATIONS * (NUMBERS * 2 + 1), Duration.ofSeconds(20));

        assertEquals(0, sourceListProp.size());
        assertEquals(0, targetListProp.size());
    }

    @Test
    void Nested_Locked_List_Is_ReadOnly() {
        var listProp =
            new SimpleAsyncSetProperty<>(
                null,
                new PropertyMetadata.Builder<AsyncObservableSet<Integer>>()
                    .customBean(true)
                    .name("source")
                    .initialValue(FXAsyncCollections.observableSet(new HashSet<>()))
                    .create());

        listProp.addListener(
            (SetChangeListener<Integer>)
                c -> {
                    try (LockedSet<Integer> readOnlyLockedList = listProp.lock()) {
                        try {
                            readOnlyLockedList.add(1);
                            fail();
                        } catch (Exception expected) {
                        }
                    }

                    try (LockedSet<Integer> readOnlyLockedList = listProp.lock()) {
                        assertEquals(1, (int)readOnlyLockedList.iterator().next());
                    }
                });

        listProp.add(1);
    }

    @Test
    void Correct_Element_Is_Removed_In_ReadOnly_UIAsyncSet() {
        var sourceListProp =
            new SimpleAsyncSetProperty<>(
                null,
                new PropertyMetadata.Builder<AsyncObservableSet<Integer>>()
                    .customBean(true)
                    .name("source")
                    .initialValue(FXAsyncCollections.observableSet(new HashSet<>()))
                    .create());

        sourceListProp.addAll(IntStream.rangeClosed(1, 3).boxed().collect(Collectors.toList()));

        var uiListProp =
            new UIAsyncSetProperty<>(
                null,
                new UIPropertyMetadata.Builder<AsyncObservableSet<Integer>>()
                    .customBean(true)
                    .name("target")
                    .initialValue(FXAsyncCollections.observableSet(new HashSet<>()))
                    .create());

        uiListProp.bind(sourceListProp);

        ReadOnlySetProperty<Integer> targetReadOnlyProp = uiListProp.getReadOnlyProperty();

        var awaiter = new Awaiter();

        targetReadOnlyProp.addListener(
            (SetChangeListener<Integer>)
                c -> {
                    awaiter.assertTrue(Platform.isFxApplicationThread());

                    awaiter.assertEquals(2, targetReadOnlyProp.size());

                    var it = targetReadOnlyProp.iterator();
                    awaiter.assertEquals(1, (int)it.next());
                    awaiter.assertEquals(3, (int)it.next());

                    awaiter.signal();
                });

        assertFalse(Platform.isFxApplicationThread());

        Dispatcher.background()
            .run(
                () -> {
                    try (var list = sourceListProp.lock()) {
                        list.remove(2);
                    }
                });

        awaiter.await(1);
    }

    @Test
    void Correct_Element_Is_Removed_In_Locked_AsyncList() {
        var sourceListProp =
            new SimpleAsyncSetProperty<>(
                null,
                new PropertyMetadata.Builder<AsyncObservableSet<Integer>>()
                    .customBean(true)
                    .name("source")
                    .initialValue(FXAsyncCollections.observableSet(new HashSet<>()))
                    .create());

        sourceListProp.addAll(IntStream.rangeClosed(1, 3).boxed().collect(Collectors.toList()));

        var secondListProp =
            new SimpleAsyncSetProperty<>(
                null,
                new PropertyMetadata.Builder<AsyncObservableSet<Integer>>()
                    .customBean(true)
                    .name("source")
                    .initialValue(FXAsyncCollections.observableSet(new HashSet<>()))
                    .create());

        secondListProp.addAll(sourceListProp);

        sourceListProp.addListener(
            (SetChangeListener.Change<? extends Integer> change) -> {
                try (LockedSet<Integer> lockedList = secondListProp.lock()) {
                    if (change.wasAdded()) {
                        lockedList.add(change.getElementAdded());
                    }

                    if (change.wasRemoved()) {
                        lockedList.remove(change.getElementRemoved());
                    }
                }
            });

        var uiListProp =
            new UIAsyncSetProperty<>(
                null,
                new UIPropertyMetadata.Builder<AsyncObservableSet<Integer>>().customBean(true).name("target").create());

        uiListProp.bind(secondListProp);

        ReadOnlySetProperty<Integer> targetReadOnlyProp = uiListProp.getReadOnlyProperty();

        var awaiter = new Awaiter();

        targetReadOnlyProp.addListener(
            (SetChangeListener<Integer>)
                c -> {
                    awaiter.assertTrue(Platform.isFxApplicationThread());

                    awaiter.assertEquals(2, targetReadOnlyProp.size());

                    var it = targetReadOnlyProp.iterator();
                    awaiter.assertEquals(1, (int)it.next());
                    awaiter.assertEquals(3, (int)it.next());

                    awaiter.signal();
                });

        assertFalse(Platform.isFxApplicationThread());

        // remove second element from source:
        Dispatcher.background()
            .run(
                () -> {
                    try (var list = sourceListProp.lock()) {
                        list.remove(2);
                    }
                });

        awaiter.await(1);
    }

    @Test
    void Iterating_Over_ReadOnly_List_Works() {
        var awaiter = new Awaiter();

        var uiListProp =
            new UIAsyncSetProperty<>(
                null,
                new UIPropertyMetadata.Builder<AsyncObservableSet<Integer>>()
                    .customBean(true)
                    .name("target")
                    .initialValue(FXAsyncCollections.observableSet(new HashSet<>()))
                    .create());

        ReadOnlySetProperty<Integer> targetReadOnlyProp = uiListProp.getReadOnlyProperty();

        Dispatcher.platform()
            .run(
                () -> {
                    uiListProp.addAll(IntStream.rangeClosed(1, 3).boxed().collect(Collectors.toList()));
                    for (Integer i : targetReadOnlyProp) {
                        awaiter.signal();
                    }
                });

        awaiter.await(1);
    }

    static class A extends ObservableObject {
        final AsyncObjectProperty<B> b = new SimpleAsyncObjectProperty<>(this);
    }

    static class B extends ObservableObject {
        final AsyncSetProperty<C> c =
            new SimpleAsyncSetProperty<>(
                this,
                new PropertyMetadata.Builder<AsyncObservableSet<C>>()
                    .initialValue(FXAsyncCollections.observableSet(new HashSet<>()))
                    .create());
    }

    static class C extends ObservableObject {
        final AsyncBooleanProperty d = new SimpleAsyncBooleanProperty(this);
    }

    @Test
    void Changing_SubValues_In_List_Raises_SubInvalidation_Event_Once() {
        //   c0  c1  c2  ... cN
        //    \  |  /       /
        //     \ | /______/
        //       B
        //       |
        //       A

        final int N = 10;
        A a = new A();
        B b = new B();
        a.b.set(b);

        for (int i = 0; i < N; ++i) {
            b.c.add(new C());
        }

        var prop = new SimpleAsyncObjectProperty<>(null, new PropertyMetadata.Builder<A>().customBean(true).create());
        prop.set(a);

        int[] count = new int[1];
        prop.addListener((observable, subInvalidation) -> count[0]++);

        try (LockedSet<C> set = b.c.lock()) {
            var it = set.iterator();

            it.next().d.set(true);
            assertEquals(1, count[0]);

            it.next().d.set(true);
            assertEquals(1, count[0]);

            it.next().d.set(true);
            assertEquals(1, count[0]);
        }
    }

    @Test
    void Changing_SubValues_In_List_Raises_SubChange_Event_Multiple_Times() {
        //   c0  c1  c2  ... cN
        //    \  |  /       /
        //     \ | /______/
        //       B
        //       |
        //       A

        final int N = 10;
        A a = new A();
        B b = new B();
        a.b.set(b);

        for (int i = 0; i < N; ++i) {
            b.c.add(new C());
        }

        var prop = new SimpleAsyncObjectProperty<A>(null, new PropertyMetadata.Builder<A>().customBean(true).create());
        prop.set(a);

        int[] count = new int[1];
        prop.addListener((observable, oldValue, newValue, subChange) -> count[0]++);

        try (var set = b.c.lock()) {
            var it = set.iterator();

            it.next().d.set(true);
            assertEquals(1, count[0]);

            it.next().d.set(true);
            assertEquals(2, count[0]);

            it.next().d.set(true);
            assertEquals(3, count[0]);
        }
    }

}
