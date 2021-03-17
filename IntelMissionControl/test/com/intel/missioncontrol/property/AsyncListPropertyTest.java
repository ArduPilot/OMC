/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.property;

import com.google.common.util.concurrent.Futures;
import com.intel.missioncontrol.beans.property.PropertyMetadata;
import com.intel.missioncontrol.beans.property.SimpleAsyncListProperty;
import com.intel.missioncontrol.beans.property.UIAsyncListProperty;
import com.intel.missioncontrol.beans.property.UIPropertyMetadata;
import com.intel.missioncontrol.collections.AsyncListChangeListener;
import com.intel.missioncontrol.collections.AsyncObservableList;
import com.intel.missioncontrol.collections.FXAsyncCollections;
import com.intel.missioncontrol.collections.LockedList;
import com.intel.missioncontrol.concurrent.AwaitableTestBase;
import com.intel.missioncontrol.concurrent.Dispatcher;
import com.intel.missioncontrol.concurrent.SynchronizationContext;
import com.intel.missioncontrol.diagnostics.Debugger;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.application.Platform;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testfx.api.FxToolkit;

public class AsyncListPropertyTest extends AwaitableTestBase {

    @BeforeClass
    public static void globalSetup() throws Exception {
        FxToolkit.registerPrimaryStage();
        Debugger.setIsRunningTests(true);
    }

    @Test
    public void Adding_Multiple_Items_To_Unlocked_List_Calls_ListChangeListener_For_Each_Item() {
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

        Assert.assertEquals(3, count[0]);
    }

    @Test
    public void Modifying_Locked_List_Calls_ListChangeListener_Once() {
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

        Assert.assertEquals(1, count[0]);
    }

    @Test
    public void Modifying_Locked_List_Calls_ListChangeListener_Twice() {
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

        Assert.assertEquals(2, count[0]);
    }

    @Test
    public void Modifying_Locked_List_Calls_ListChangeListener_Four_Times() {
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

        Assert.assertEquals(4, count[0]);
    }

    @Test
    public void Listener_Should_Be_Called_On_FxApplicationThread() {
        await(
            1,
            () -> {
                Assert.assertTrue(!Platform.isFxApplicationThread());

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
                            Assert.assertTrue(Platform.isFxApplicationThread());
                            signal();
                        },
                    Dispatcher::dispatchToUI);

                try (var view = simpleListProp.lock()) {
                    view.add(1);
                    view.add(2);
                }
            });
    }

    @Test
    public void UIAsyncListProperty_Binding_Should_Be_Evaluated_On_FxApplicationThread() {
        await(
            1,
            () -> {
                var sourceListProp =
                    new SimpleAsyncListProperty<>(
                        null,
                        new PropertyMetadata.Builder<AsyncObservableList<Integer>>()
                            .initialValue(FXAsyncCollections.observableArrayList())
                            .customBean(true)
                            .create());

                sourceListProp.addListener(
                    (ListChangeListener<Integer>)c -> Assert.assertTrue(!Platform.isFxApplicationThread()));

                var targetListProp =
                    new UIAsyncListProperty<>(
                        null, new UIPropertyMetadata.Builder<AsyncObservableList<Integer>>().customBean(true).create());

                targetListProp.addListener(
                    (ListChangeListener<Integer>)
                        c -> {
                            Assert.assertTrue(Platform.isFxApplicationThread());
                            signal();
                        },
                    Dispatcher::dispatchToUI);

                targetListProp.bind(sourceListProp);

                try (var view = sourceListProp.lock()) {
                    view.add(1);
                    view.add(2);
                    view.add(3);
                }
            });
    }

    @Test
    public void BidirectionalBinding_Is_Evaluated_On_Another_Thread() {
        var syncCtx = Futures.getUnchecked(Dispatcher.run(SynchronizationContext::getCurrent));

        var sourceListProp1 =
            new SimpleAsyncListProperty<>(
                null,
                new PropertyMetadata.Builder<AsyncObservableList<Integer>>()
                    .customBean(true)
                    .name("source")
                    .synchronizationContext(syncCtx)
                    .initialValue(FXAsyncCollections.observableArrayList())
                    .create());

        var targetListProp1 =
            new SimpleAsyncListProperty<>(
                null,
                new PropertyMetadata.Builder<AsyncObservableList<Integer>>()
                    .customBean(true)
                    .name("target")
                    .synchronizationContext(syncCtx)
                    .initialValue(null)
                    .create());

        targetListProp1.bindBidirectional(sourceListProp1);
        targetListProp1.addListener((ListChangeListener<Integer>)c -> signal());

        await(1, () -> sourceListProp1.add(1));
        await(1, () -> targetListProp1.add(2));

        Assert.assertEquals(1, (int)sourceListProp1.get(0));
        Assert.assertEquals(2, (int)sourceListProp1.get(1));
        Assert.assertEquals(1, (int)targetListProp1.get(0));
        Assert.assertEquals(2, (int)targetListProp1.get(1));
    }

    @Test
    public void BidirectionalBinding_Is_Evaluated_On_FxApplicationThread() {
        var syncCtx = Futures.getUnchecked(Dispatcher.run(SynchronizationContext::getCurrent));

        var sourceListProp2 =
            new SimpleAsyncListProperty<>(
                null,
                new PropertyMetadata.Builder<AsyncObservableList<Integer>>()
                    .customBean(true)
                    .synchronizationContext(syncCtx)
                    .initialValue(FXAsyncCollections.observableArrayList())
                    .create());

        var targetListProp2 =
            new UIAsyncListProperty<>(
                null,
                new UIPropertyMetadata.Builder<AsyncObservableList<Integer>>()
                    .customBean(true)
                    .initialValue(FXAsyncCollections.observableArrayList())
                    .create());

        Dispatcher.runOnUI(() -> targetListProp2.bindBidirectional(sourceListProp2));
        targetListProp2.addListener((ListChangeListener<Integer>)c -> signal());

        await(1, () -> sourceListProp2.add(1));
        await(1, () -> sourceListProp2.add(2));
        Assert.assertEquals(1, (int)sourceListProp2.get(0));
        Assert.assertEquals(2, (int)sourceListProp2.get(1));
        Assert.assertEquals(1, (int)targetListProp2.get(0));
        Assert.assertEquals(2, (int)targetListProp2.get(1));
    }

    @Test
    public void Listeners_Can_Be_Added_And_Removed() {
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
        Assert.assertEquals(0, sourceCount[0]);
        Assert.assertEquals(1, targetCount[0]);

        targetListProp1.add(2);
        Assert.assertEquals(0, sourceCount[0]);
        Assert.assertEquals(2, targetCount[0]);

        targetListProp1.removeListener(targetListChangeListener);
        sourceListProp1.addListener(sourceListChangeListener);

        targetListProp1.add(3);
        Assert.assertEquals(1, sourceCount[0]);
        Assert.assertEquals(2, targetCount[0]);

        sourceListProp1.add(4);
        Assert.assertEquals(2, sourceCount[0]);
        Assert.assertEquals(2, targetCount[0]);
    }

    @Test(timeout = 5000)
    public void Multiple_Concurrent_AddRemove_Operations_Do_Not_Corrupt_AsyncList() {
        final int CONCURRENT_THREADS = 10;
        final int ITERATIONS = 500;

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

        var latch = new CountDownLatch(CONCURRENT_THREADS * ITERATIONS * 3);

        targetListProp.addListener(
            (AsyncListChangeListener<Integer>)
                c -> {
                    Assert.assertTrue(Platform.isFxApplicationThread());
                    latch.countDown();
                });

        Assert.assertFalse(Platform.isFxApplicationThread());

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

                    latch.countDown();
                }
            };

        for (int i = 0; i < CONCURRENT_THREADS; ++i) {
            Dispatcher.post(changeListFunc);
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        Assert.assertEquals(0, sourceListProp.size());
        Assert.assertEquals(0, targetListProp.size());
    }

    @Test(timeout = 5000)
    public void Multiple_Concurrent_Replace_Operations_Do_Not_Corrupt_AsyncList() {
        final int CONCURRENT_THREADS = 10;
        final int ITERATIONS = 500;

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

        var latch = new CountDownLatch(CONCURRENT_THREADS * ITERATIONS);

        targetListProp.addListener(
            (AsyncListChangeListener<Integer>)
                c -> {
                    Assert.assertTrue(Platform.isFxApplicationThread());
                    latch.countDown();
                });

        Assert.assertFalse(Platform.isFxApplicationThread());

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
            Dispatcher.post(() -> changeListFunc.accept(j));
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test(timeout = 5000)
    public void Replace_Operations_On_Bound_ObservableList_Do_Not_Corrupt_AsyncList() {
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

        var latch = new CountDownLatch(1);

        targetListProp.addListener(
            (AsyncListChangeListener<Integer>)
                c -> {
                    Assert.assertTrue(Platform.isFxApplicationThread());
                    if (targetListProp.size() == 5) {
                        latch.countDown();
                    }
                });

        Assert.assertFalse(Platform.isFxApplicationThread());

        sourceListProp.setAll(5, 6, 7, 8, 9);

        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        Assert.assertEquals(5, targetListProp.size());
    }

    @Test
    public void Nested_Locked_List_Is_ReadOnly() {
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
                            Assert.fail();
                        } catch (Exception e) {
                        }
                    }

                    try (LockedList<Integer> readOnlyLockedList = listProp.lock()) {
                        Assert.assertEquals(1, (int)readOnlyLockedList.get(0));
                    }
                });

        listProp.add(1);
    }

}
