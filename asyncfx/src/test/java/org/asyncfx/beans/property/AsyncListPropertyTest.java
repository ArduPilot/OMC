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
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import org.asyncfx.Awaiter;
import org.asyncfx.TestBase;
import org.asyncfx.collections.AsyncListChangeListener;
import org.asyncfx.collections.AsyncObservableList;
import org.asyncfx.collections.FXAsyncCollections;
import org.asyncfx.collections.LockedList;
import org.asyncfx.concurrent.Dispatcher;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class AsyncListPropertyTest extends TestBase {

    @Nested
    class Listeners {
        @Test
        void Adding_Multiple_Items_To_Unlocked_List_Calls_ListChangeListener_For_Each_Item() {
            var count = new int[1];
            var prop =
                new SimpleAsyncListProperty<>(
                    null,
                    new PropertyMetadata.Builder<AsyncObservableList<Integer>>()
                        .initialValue(FXAsyncCollections.observableArrayList())
                        .customBean(true)
                        .create());
            prop.addListener(((ListChangeListener<Integer>)c -> count[0]++));
            prop.add(1);
            prop.add(2);
            prop.add(3);

            assertEquals(3, count[0]);
        }

        @Test
        void Modifying_Locked_List_Calls_ListChangeListener_Once() {
            var count = new int[1];
            var prop =
                new SimpleAsyncListProperty<>(
                    null,
                    new PropertyMetadata.Builder<AsyncObservableList<Integer>>()
                        .initialValue(FXAsyncCollections.observableArrayList())
                        .customBean(true)
                        .create());
            prop.addListener(((ListChangeListener<Integer>)c -> count[0]++));

            try (var locked = prop.lock()) {
                locked.add(1);
                locked.add(2);
                locked.add(3);
            }

            assertEquals(1, count[0]);
        }

        @Test
        void Modifying_Locked_List_Calls_ListChangeListener_Twice() {
            var count = new int[1];
            var prop =
                new SimpleAsyncListProperty<>(
                    null,
                    new PropertyMetadata.Builder<AsyncObservableList<Integer>>()
                        .initialValue(FXAsyncCollections.observableArrayList())
                        .customBean(true)
                        .create());
            prop.addListener(((ListChangeListener<Integer>)c -> count[0]++));

            try (var locked = prop.lock()) {
                locked.add(1);
                locked.add(2);
                locked.add(3);
                locked.clear();
            }

            assertEquals(2, count[0]);
        }

        @Test
        void Modifying_Locked_List_Calls_ListChangeListener_Four_Times() {
            var count = new int[1];
            var prop =
                new SimpleAsyncListProperty<>(
                    null,
                    new PropertyMetadata.Builder<AsyncObservableList<Integer>>()
                        .initialValue(FXAsyncCollections.observableArrayList())
                        .customBean(true)
                        .create());
            prop.addListener(((ListChangeListener<Integer>)c -> count[0]++));

            try (var locked = prop.lock()) {
                locked.add(1);
                locked.add(2);
                locked.clear();
                locked.add(3);
                locked.add(4);
                locked.remove(locked.size() - 1);
                locked.clear();
            }

            assertEquals(4, count[0]);
        }

        @Test
        void Listener_Should_Be_Called_On_FxApplicationThread() {
            var awaiter = new Awaiter();

            assertTrue(!Platform.isFxApplicationThread());

            var simpleListProp =
                new SimpleAsyncListProperty<>(
                    null,
                    new PropertyMetadata.Builder<AsyncObservableList<Integer>>()
                        .initialValue(FXAsyncCollections.observableArrayList())
                        .customBean(true)
                        .create());

            simpleListProp.addListener(
                (ListChangeListener<Integer>)
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
    }

    @Nested
    class UnidirectionalBindings {
        @Test
        void UIAsyncListProperty_Binding_Should_Be_Evaluated_On_FxApplicationThread() {
            var awaiter = new Awaiter();

            var sourceListProp =
                new SimpleAsyncListProperty<>(
                    null,
                    new PropertyMetadata.Builder<AsyncObservableList<Integer>>()
                        .initialValue(FXAsyncCollections.observableArrayList())
                        .customBean(true)
                        .create());

            sourceListProp.addListener((ListChangeListener<Integer>)c -> assertTrue(!Platform.isFxApplicationThread()));

            var targetListProp =
                new UIAsyncListProperty<>(
                    null, new UIPropertyMetadata.Builder<AsyncObservableList<Integer>>().customBean(true).create());

            targetListProp.addListener(
                (ListChangeListener<Integer>)
                    c -> {
                        awaiter.assertTrue(Platform.isFxApplicationThread());
                        awaiter.signal();
                    },
                Platform::runLater);

            targetListProp.bind(sourceListProp);

            try (var view = sourceListProp.lock()) {
                view.add(1);
                view.add(2);
                view.add(3);
            }

            awaiter.await(1);
        }

        @Test
        void Binding_To_EmptyProperty_Should_Work() {
            var sourceListProp =
                new SimpleAsyncListProperty<>(
                    null,
                    new PropertyMetadata.Builder<AsyncObservableList<Object>>()
                        .customBean(true)
                        .name("sourceList")
                        .initialValue(FXAsyncCollections.observableArrayList())
                        .create());

            var targetBooleanProp =
                new SimpleAsyncBooleanProperty(null, new PropertyMetadata.Builder<Boolean>().customBean(true).create());

            targetBooleanProp.bind(sourceListProp.emptyProperty());

            int[] count = new int[1];
            targetBooleanProp.addListener(
                (o, oldValue, newValue) -> {
                    count[0]++;
                });

            assertEquals(0, count[0]);
            sourceListProp.add(new Object());
            assertEquals(1, count[0]);
        }
    }

    @Nested
    class ContentBindings {
        @Test
        void ContentBinding_To_Empty_List_Should_Work() {
            var sourceListProp = new SimpleAsyncListProperty<>(this);
            sourceListProp.set(FXAsyncCollections.emptyObservableList());

            var targetListProp = new SimpleAsyncListProperty<>(this);
            targetListProp.bindContent(sourceListProp);
        }

        @Test
        void Adding_To_Empty_List_Should_Throw() {
            var sourceListProp = new SimpleAsyncListProperty<>(this);
            sourceListProp.set(FXAsyncCollections.observableArrayList());

            var targetListProp = new SimpleAsyncListProperty<>(this);
            targetListProp.bindContent(sourceListProp);

            sourceListProp.add(new Object());

            assertTrue(targetListProp.isEmpty());
        }

        private class ListContainer {
            final AsyncListProperty<String> sourceList =
                new SimpleAsyncListProperty<>(
                    this,
                    new PropertyMetadata.Builder<AsyncObservableList<String>>()
                        .initialValue(FXAsyncCollections.observableArrayList())
                        .create());
        }

        @Test
        void ContentBinding_Updates_TargetList_From_PropertyPath() {
            AsyncListProperty<String> targetList =
                new SimpleAsyncListProperty<>(
                    this,
                    new PropertyMetadata.Builder<AsyncObservableList<String>>()
                        .initialValue(FXAsyncCollections.observableArrayList())
                        .create());

            AsyncObjectProperty<ListContainer> listContainer =
                new SimpleAsyncObjectProperty<>(this, new PropertyMetadata.Builder<ListContainer>().create());

            var sourceListFromPath = PropertyPath.from(listContainer).selectReadOnlyAsyncList(c -> c.sourceList);

            targetList.bindContent(sourceListFromPath);

            var container = new ListContainer();
            container.sourceList.add("String 1");
            container.sourceList.add("String 2");

            assertEquals(0, targetList.size());

            listContainer.set(container);

            assertEquals(2, targetList.size());
        }

        @Test
        void ContentBinding_Works_With_AsyncObservableList_Changes() {
            AsyncObservableList<String> sourceList = FXAsyncCollections.observableArrayList();

            AsyncListProperty<String> targetList =
                new SimpleAsyncListProperty<>(
                    this,
                    new PropertyMetadata.Builder<AsyncObservableList<String>>()
                        .initialValue(FXAsyncCollections.observableArrayList())
                        .create());

            targetList.bindContent(sourceList);

            String[] elements = {"String 1", "String 2"};
            sourceList.addAll(elements);

            assertEquals(2, targetList.size());
        }

        @Test
        void Multiple_Concurrent_AddRemove_Operations_Do_Not_Corrupt_AsyncList() {
            final int CONCURRENT_THREADS = 20;
            final int ITERATIONS = 1000;

            var sourceListProp =
                new SimpleAsyncListProperty<>(
                    null,
                    new PropertyMetadata.Builder<AsyncObservableList<Integer>>()
                        .customBean(true)
                        .name("source")
                        .initialValue(FXAsyncCollections.observableArrayList())
                        .create());

            var targetListProp =
                new UIAsyncListProperty<>(
                    null,
                    new UIPropertyMetadata.Builder<AsyncObservableList<Integer>>()
                        .customBean(true)
                        .name("target")
                        .initialValue(FXAsyncCollections.observableArrayList())
                        .create());

            targetListProp.bindContent(sourceListProp);

            var awaiter = new Awaiter();

            targetListProp.addListener(
                (AsyncListChangeListener<Integer>)
                    c -> {
                        awaiter.assertTrue(Platform.isFxApplicationThread());
                        awaiter.signal();
                    });

            assertFalse(Platform.isFxApplicationThread());

            Runnable changeListFunc =
                () -> {
                    for (int i = 0; i < ITERATIONS; ++i) {
                        try (var list = sourceListProp.lock()) {
                            for (int j = 0; j < 50; ++j) {
                                list.add(j);
                            }
                        }

                        try (var list = sourceListProp.lock()) {
                            for (int j = 0; j < 50; ++j) {
                                list.remove(0);
                            }
                        }

                        awaiter.signal();
                    }
                };

            for (int i = 0; i < CONCURRENT_THREADS; ++i) {
                Dispatcher.background().runLater(changeListFunc);
            }

            awaiter.await(CONCURRENT_THREADS * ITERATIONS * 3, Duration.ofSeconds(10));

            assertEquals(0, sourceListProp.size());
            assertEquals(0, targetListProp.size());
        }

        @Test
        void Multiple_Concurrent_Replace_Operations_Do_Not_Corrupt_AsyncList() {
            final int CONCURRENT_THREADS = 20;
            final int ITERATIONS = 1000;

            var sourceListProp =
                new SimpleAsyncListProperty<>(
                    null,
                    new PropertyMetadata.Builder<AsyncObservableList<Integer>>()
                        .customBean(true)
                        .name("source")
                        .initialValue(FXAsyncCollections.observableArrayList())
                        .create());

            sourceListProp.addAll(IntStream.rangeClosed(1, CONCURRENT_THREADS).boxed().collect(Collectors.toList()));

            var targetListProp =
                new UIAsyncListProperty<>(
                    null,
                    new UIPropertyMetadata.Builder<AsyncObservableList<Integer>>()
                        .customBean(true)
                        .name("target")
                        .initialValue(FXAsyncCollections.observableArrayList())
                        .create());

            targetListProp.bindContent(sourceListProp);

            var awaiter = new Awaiter();

            targetListProp.addListener(
                (AsyncListChangeListener<Integer>)
                    c -> {
                        awaiter.assertTrue(Platform.isFxApplicationThread());
                        awaiter.signal();
                    });

            assertFalse(Platform.isFxApplicationThread());

            Consumer<Integer> changeListFunc =
                index -> {
                    for (int i = 0; i < ITERATIONS; ++i) {
                        try (var list = sourceListProp.lock()) {
                            int value = list.get(index);
                            list.set(index, value == 0 ? index : 0);
                        }
                    }
                };

            for (int i = 0; i < CONCURRENT_THREADS; ++i) {
                final int j = i;
                Dispatcher.background().runLater(() -> changeListFunc.accept(j));
            }

            awaiter.await(CONCURRENT_THREADS * ITERATIONS, Duration.ofSeconds(10));
        }

        @Test
        @Disabled("Test fails with Gradle test runner, but succeeds in IntelliJ test runner")
        void Replace_Operations_On_Bound_ObservableList_Do_Not_Corrupt_AsyncList() {
            var sourceListProp = new SimpleListProperty<Integer>(FXCollections.observableArrayList());
            sourceListProp.addAll(1, 2);

            var targetListProp =
                new UIAsyncListProperty<>(
                    null,
                    new UIPropertyMetadata.Builder<AsyncObservableList<Integer>>()
                        .customBean(true)
                        .name("target")
                        .initialValue(FXAsyncCollections.observableArrayList())
                        .create());

            targetListProp.bindContent(sourceListProp);

            var awaiter = new Awaiter(true);

            targetListProp.addListener(
                (AsyncListChangeListener<Integer>)
                    c -> {
                        awaiter.assertTrue(Platform.isFxApplicationThread());
                        try (LockedList<Integer> list = targetListProp.lock()) {
                            if (list.size() == 5
                                    && list.get(0) == 5
                                    && list.get(1) == 6
                                    && list.get(2) == 7
                                    && list.get(3) == 8
                                    && list.get(4) == 9) {
                                awaiter.signal();
                            }
                        }
                    });

            assertFalse(Platform.isFxApplicationThread());

            sourceListProp.setAll(5, 6, 7, 8, 9);

            awaiter.await(1);
        }

        private class TestObject {
            private UIAsyncListProperty<Object> objects =
                new UIAsyncListProperty<>(
                    this,
                    new UIPropertyMetadata.Builder<AsyncObservableList<Object>>()
                        .initialValue(FXAsyncCollections.observableArrayList())
                        .create());

            private AsyncBooleanProperty bool = new SimpleAsyncBooleanProperty(this);

            UIAsyncListProperty<Object> objectsProperty() {
                return objects;
            }

            AsyncBooleanProperty boolProperty() {
                return bool;
            }
        }

        @Test
        void UnidirectionalBinding_To_Unresolved_PropertyPath_Is_Updated_When_Path_Is_Resolved() {
            var prop = new SimpleAsyncObjectProperty<>(this, new PropertyMetadata.Builder<TestObject>().create());

            var listProp =
                new SimpleAsyncListProperty<>(
                    this,
                    new PropertyMetadata.Builder<AsyncObservableList<Object>>()
                        .initialValue(FXAsyncCollections.observableArrayList())
                        .create());

            listProp.add(0);
            listProp.add(1);
            listProp.add(2);

            // prop contains no object, which means that listByPropertyPath is unresolved
            var listByPropertyPath = PropertyPath.from(prop).selectAsyncList(TestObject::objectsProperty);
            listByPropertyPath.bind(listProp);
            assertEquals(0, listByPropertyPath.size());

            // listByPropertyPath is now resolved
            prop.set(new TestObject());
            assertEquals(3, listByPropertyPath.size());
        }
    }

    @Nested
    class BidirectionalBindings {
        @Test
        void BidirectionalBinding_Is_Evaluated_On_Another_Thread() {
            var awaiter = new Awaiter();

            var sourceListProp1 =
                new SimpleAsyncListProperty<>(
                    null,
                    new PropertyMetadata.Builder<AsyncObservableList<Integer>>()
                        .customBean(true)
                        .name("source")
                        .initialValue(FXAsyncCollections.observableArrayList())
                        .create());

            var targetListProp1 =
                new SimpleAsyncListProperty<>(
                    null,
                    new PropertyMetadata.Builder<AsyncObservableList<Integer>>()
                        .customBean(true)
                        .name("target")
                        .initialValue(null)
                        .create());

            targetListProp1.bindBidirectional(sourceListProp1);
            targetListProp1.addListener(
                (ListChangeListener<Integer>)
                    c -> {
                        awaiter.assertTrue(!Platform.isFxApplicationThread());
                        awaiter.signal();
                    });

            sourceListProp1.add(1);
            awaiter.await(1);

            sourceListProp1.add(2);
            awaiter.await(1);

            assertEquals(1, (int)sourceListProp1.get(0));
            assertEquals(2, (int)sourceListProp1.get(1));
            assertEquals(1, (int)targetListProp1.get(0));
            assertEquals(2, (int)targetListProp1.get(1));
        }

        @Test
        void BidirectionalBinding_Is_Evaluated_On_FxApplicationThread() {
            var awaiter = new Awaiter();

            var sourceListProp2 =
                new SimpleAsyncListProperty<>(
                    null,
                    new PropertyMetadata.Builder<AsyncObservableList<Integer>>()
                        .customBean(true)
                        .initialValue(FXAsyncCollections.observableArrayList())
                        .create());

            var targetListProp2 =
                new UIAsyncListProperty<>(
                    null,
                    new UIPropertyMetadata.Builder<AsyncObservableList<Integer>>()
                        .customBean(true)
                        .initialValue(FXAsyncCollections.observableArrayList())
                        .create());

            Dispatcher.platform()
                .runLaterAsync(() -> targetListProp2.bindBidirectional(sourceListProp2))
                .getUnchecked();
            targetListProp2.addListener(
                (ListChangeListener<Integer>)
                    c -> {
                        awaiter.assertTrue(Platform.isFxApplicationThread());
                        awaiter.signal();
                    });

            sourceListProp2.add(1);
            awaiter.await(1);

            sourceListProp2.add(2);
            awaiter.await(1);

            assertEquals(1, (int)sourceListProp2.get(0));
            assertEquals(2, (int)sourceListProp2.get(1));
            assertEquals(1, (int)targetListProp2.get(0));
            assertEquals(2, (int)targetListProp2.get(1));
        }

        @Test
        void Listeners_Can_Be_Added_And_Removed() {
            var sourceListProp1 =
                new SimpleAsyncListProperty<>(
                    null,
                    new PropertyMetadata.Builder<AsyncObservableList<Integer>>()
                        .customBean(true)
                        .name("source")
                        .initialValue(FXAsyncCollections.observableArrayList())
                        .create());

            var targetListProp1 =
                new SimpleAsyncListProperty<>(
                    null,
                    new PropertyMetadata.Builder<AsyncObservableList<Integer>>()
                        .customBean(true)
                        .name("target")
                        .initialValue(null)
                        .create());

            int[] sourceCount = new int[1];
            int[] targetCount = new int[1];

            ListChangeListener<Integer> sourceListChangeListener = change -> sourceCount[0]++;
            ListChangeListener<Integer> targetListChangeListener = change -> targetCount[0]++;

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
    }

    @Nested
    class Locking {
        @Test
        void Nested_Locked_List_Is_ReadOnly() {
            var listProp =
                new SimpleAsyncListProperty<>(
                    null,
                    new PropertyMetadata.Builder<AsyncObservableList<Integer>>()
                        .customBean(true)
                        .name("source")
                        .initialValue(FXAsyncCollections.observableArrayList())
                        .create());

            listProp.addListener(
                (ListChangeListener<Integer>)
                    c -> {
                        try (LockedList<Integer> readOnlyLockedList = listProp.lock()) {
                            try {
                                readOnlyLockedList.add(1);
                                fail();
                            } catch (Exception expected) {
                            }
                        }

                        try (LockedList<Integer> readOnlyLockedList = listProp.lock()) {
                            assertEquals(1, (int)readOnlyLockedList.get(0));
                        }
                    });

            listProp.add(1);
        }

        @Test
        void Correct_Element_Is_Removed_In_ReadOnly_UIAsyncList() {
            var sourceListProp =
                new SimpleAsyncListProperty<>(
                    null,
                    new PropertyMetadata.Builder<AsyncObservableList<Integer>>()
                        .customBean(true)
                        .name("source")
                        .initialValue(FXAsyncCollections.observableArrayList())
                        .create());

            sourceListProp.addAll(IntStream.rangeClosed(1, 3).boxed().collect(Collectors.toList()));

            var uiListProp =
                new UIAsyncListProperty<>(
                    null,
                    new UIPropertyMetadata.Builder<AsyncObservableList<Integer>>()
                        .customBean(true)
                        .name("target")
                        .initialValue(FXAsyncCollections.observableArrayList())
                        .create());

            uiListProp.bind(sourceListProp);

            ReadOnlyListProperty<Integer> targetReadOnlyProp = uiListProp.getReadOnlyProperty();

            var awaiter = new Awaiter();

            targetReadOnlyProp.addListener(
                (ListChangeListener<Integer>)
                    c -> {
                        awaiter.assertTrue(Platform.isFxApplicationThread());

                        awaiter.assertEquals(2, targetReadOnlyProp.size());

                        awaiter.assertEquals(1, (int)targetReadOnlyProp.get(0));
                        awaiter.assertEquals(3, (int)targetReadOnlyProp.get(1));

                        awaiter.signal();
                    });

            assertFalse(Platform.isFxApplicationThread());

            Dispatcher.background()
                .run(
                    () -> {
                        try (var list = sourceListProp.lock()) {
                            list.remove(1);
                        }
                    });

            awaiter.await(1);
        }

        @Test
        void Correct_Element_Is_Removed_In_Locked_AsyncList() {
            var sourceListProp =
                new SimpleAsyncListProperty<>(
                    null,
                    new PropertyMetadata.Builder<AsyncObservableList<Integer>>()
                        .customBean(true)
                        .name("source")
                        .initialValue(FXAsyncCollections.observableArrayList())
                        .create());

            sourceListProp.addAll(IntStream.rangeClosed(1, 3).boxed().collect(Collectors.toList()));

            var secondListProp =
                new SimpleAsyncListProperty<>(
                    null,
                    new PropertyMetadata.Builder<AsyncObservableList<Integer>>()
                        .customBean(true)
                        .name("source")
                        .initialValue(FXAsyncCollections.observableArrayList())
                        .create());

            secondListProp.addAll(sourceListProp);

            sourceListProp.addListener(
                (AsyncListChangeListener.Change<? extends Integer> change) -> {
                    try (LockedList<Integer> lockedList = secondListProp.lock()) {
                        while (change.next()) {
                            if (change.wasAdded()) {
                                lockedList.addAll(change.getAddedSubList());
                            }

                            if (change.wasRemoved()) {
                                lockedList.removeAll(change.getRemoved());
                            }
                        }
                    }
                });

            var uiListProp =
                new UIAsyncListProperty<>(
                    null,
                    new UIPropertyMetadata.Builder<AsyncObservableList<Integer>>()
                        .customBean(true)
                        .name("target")
                        .create());

            uiListProp.bind(secondListProp);

            ReadOnlyListProperty<Integer> targetReadOnlyProp = uiListProp.getReadOnlyProperty();

            var awaiter = new Awaiter();

            targetReadOnlyProp.addListener(
                (ListChangeListener<Integer>)
                    c -> {
                        awaiter.assertTrue(Platform.isFxApplicationThread());

                        awaiter.assertEquals(2, targetReadOnlyProp.size());

                        awaiter.assertEquals(1, (int)targetReadOnlyProp.get(0));
                        awaiter.assertEquals(3, (int)targetReadOnlyProp.get(1));

                        awaiter.signal();
                    });

            assertFalse(Platform.isFxApplicationThread());

            // remove second element from source:
            Dispatcher.background()
                .run(
                    () -> {
                        try (var list = sourceListProp.lock()) {
                            list.remove(1);
                        }
                    });

            awaiter.await(1);
        }

        @Test
        void Iterating_Over_ReadOnly_List_Works() {
            var awaiter = new Awaiter();
            final int iterations = 10;

            var uiListProp =
                new UIAsyncListProperty<>(
                    null,
                    new UIPropertyMetadata.Builder<AsyncObservableList<Integer>>()
                        .customBean(true)
                        .name("target")
                        .initialValue(FXAsyncCollections.observableArrayList())
                        .create());

            ReadOnlyListProperty<Integer> targetReadOnlyProp = uiListProp.getReadOnlyProperty();

            Platform.runLater(
                () -> {
                    uiListProp.addAll(IntStream.rangeClosed(1, iterations).boxed().collect(Collectors.toList()));
                    for (Integer ignored : targetReadOnlyProp) {
                        awaiter.signal();
                    }
                });
            awaiter.await(iterations);
        }
    }

    @Nested
    class SubChanges {
        private class A extends PropertyObject {
            final AsyncObjectProperty<B> b = new SimpleAsyncObjectProperty<>(this);
        }

        private class B extends PropertyObject {
            final AsyncListProperty<C> c =
                new SimpleAsyncListProperty<>(
                    this,
                    new PropertyMetadata.Builder<AsyncObservableList<C>>()
                        .initialValue(FXAsyncCollections.observableArrayList())
                        .create());
        }

        private class C extends PropertyObject {
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

            var prop =
                new SimpleAsyncObjectProperty<A>(null, new PropertyMetadata.Builder<A>().customBean(true).create());
            prop.set(a);

            int[] count = new int[1];
            prop.addListener((observable, subInvalidation) -> count[0]++);
            prop.addListener(observable -> {}); // test AsyncListExpressionHelper.Generic with more than one listener

            b.c.get(0).d.set(true);
            assertEquals(1, count[0]);

            b.c.get(1).d.set(true);
            assertEquals(1, count[0]);

            b.c.get(2).d.set(true);
            assertEquals(1, count[0]);
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

            var prop =
                new SimpleAsyncObjectProperty<A>(null, new PropertyMetadata.Builder<A>().customBean(true).create());
            prop.set(a);

            int[] count = new int[1];
            prop.addListener((observable, oldValue, newValue, subChange) -> count[0]++);

            b.c.get(0).d.set(true);
            assertEquals(1, count[0]);

            b.c.get(1).d.set(true);
            assertEquals(2, count[0]);

            b.c.get(2).d.set(true);
            assertEquals(3, count[0]);
        }

        @Test
        void test() {}

    }

}
