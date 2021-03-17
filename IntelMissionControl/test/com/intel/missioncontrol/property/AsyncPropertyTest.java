/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.property;

import com.google.common.util.concurrent.Futures;
import com.intel.missioncontrol.concurrent.AwaitableTestBase;
import com.intel.missioncontrol.beans.property.AsyncIntegerProperty;
import com.intel.missioncontrol.beans.property.AsyncObjectProperty;
import com.intel.missioncontrol.beans.property.AsyncStringProperty;
import com.intel.missioncontrol.beans.property.PropertyMetadata;
import com.intel.missioncontrol.beans.property.SimpleAsyncIntegerProperty;
import com.intel.missioncontrol.beans.property.SimpleAsyncObjectProperty;
import com.intel.missioncontrol.beans.property.SimpleAsyncStringProperty;
import com.intel.missioncontrol.beans.property.UIAsyncIntegerProperty;
import com.intel.missioncontrol.beans.property.UIAsyncObjectProperty;
import com.intel.missioncontrol.beans.property.UIAsyncStringProperty;
import com.intel.missioncontrol.beans.property.UIPropertyMetadata;
import com.intel.missioncontrol.concurrent.Dispatcher;
import com.intel.missioncontrol.concurrent.SynchronizationContext;
import com.intel.missioncontrol.diagnostics.Debugger;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.testfx.api.FxToolkit;

@Ignore("Some of these tests never finish, which wrack havoc on the CI infrastructure")
public class AsyncPropertyTest extends AwaitableTestBase {

    @BeforeClass
    public static void globalSetup() throws Exception {
        Debugger.setIsRunningTests(true);
        FxToolkit.registerPrimaryStage();
    }

    @Test
    public void IntegerProperty_ObserveOnUI() {
        await(
            1,
            () -> {
                AsyncIntegerProperty property =
                    new SimpleAsyncIntegerProperty(
                        null, new PropertyMetadata.Builder<Number>().customBean(true).create());
                property.addListener(
                    listener -> {
                        Assert.assertTrue(Platform.isFxApplicationThread());
                        signal();
                    },
                    Platform::runLater);

                Dispatcher.post(
                    () -> {
                        Assert.assertTrue(!Platform.isFxApplicationThread());
                        property.set(10);
                    });
            });
    }

    enum Foo {
        BAR,
        BAZ,
        BAF
    }

    @Test
    public void ObjectProperty_ObserveOnUI() {
        await(
            1,
            () -> {
                AsyncObjectProperty<Foo> property =
                    new SimpleAsyncObjectProperty<>(
                        null, new PropertyMetadata.Builder<Foo>().customBean(true).create());
                property.addListener(
                    listener -> {
                        Assert.assertTrue(Platform.isFxApplicationThread());
                        signal();
                    },
                    Platform::runLater);

                Dispatcher.post(
                    () -> {
                        Assert.assertTrue(!Platform.isFxApplicationThread());
                        property.set(Foo.BAR);
                    });
            });
    }

    @Test
    public void IntegerProperty_BindToUI() {
        IntegerProperty sourceProperty = new SimpleIntegerProperty(5);
        AsyncIntegerProperty targetProperty =
            new UIAsyncIntegerProperty(null, new UIPropertyMetadata.Builder<Number>().customBean(true).create());

        await(
            2,
            () -> {
                targetProperty.addListener(
                    (observable, oldValue, newValue) -> {
                        Assert.assertTrue(Platform.isFxApplicationThread());
                        signal();
                    });

                targetProperty.bind(sourceProperty);
                Dispatcher.post(
                    () -> {
                        Assert.assertTrue(!Platform.isFxApplicationThread());
                        sourceProperty.set(10);
                    });
            });
    }

    @Test
    public void ObjectProperty_BindToUI() {
        ObjectProperty<Foo> sourceProperty = new SimpleObjectProperty<>(Foo.BAR);
        AsyncObjectProperty<Foo> targetProperty =
            new UIAsyncObjectProperty<>(null, new UIPropertyMetadata.Builder<Foo>().customBean(true).create());

        await(
            2,
            () -> {
                targetProperty.addListener(
                    (observable, oldValue, newValue) -> {
                        Assert.assertTrue(Platform.isFxApplicationThread());
                        signal();
                    });

                targetProperty.bind(sourceProperty);
                Dispatcher.post(
                    () -> {
                        Assert.assertTrue(!Platform.isFxApplicationThread());
                        sourceProperty.set(Foo.BAZ);
                    });
            });
    }

    @Test
    public void IntegerProperty_BindBidirectionalToUI() {
        AsyncIntegerProperty sourceProperty =
            new SimpleAsyncIntegerProperty(
                null, new PropertyMetadata.Builder<Number>().name("source").customBean(true).initialValue(5).create());
        AsyncIntegerProperty targetProperty =
            new UIAsyncIntegerProperty(
                null, new UIPropertyMetadata.Builder<Number>().name("target").customBean(true).create());

        ChangeListener<? super Number> changeListener =
            (observable, oldValue, newValue) -> {
                Assert.assertTrue(Platform.isFxApplicationThread());
                signal();
            };

        await(
            2,
            () -> {
                // This listener will always be called on the UI thread
                targetProperty.addListener(changeListener);

                // If the source property changes, the change will always be propagated to the target property on the UI
                // thread
                targetProperty.bindBidirectional(sourceProperty);

                // We're on a non-UI thread here:
                Assert.assertTrue(!Platform.isFxApplicationThread());
                sourceProperty.set(10);
            });

        targetProperty.removeListener(changeListener);

        await(
            1,
            () -> {
                // The source property listener may be called on any thread
                sourceProperty.addListener(changeListener);

                // We're on a non-UI thread here, so this fails:
                try {
                    targetProperty.set(20);
                    Assert.fail();
                } catch (Exception e) {
                }

                // this succeeds:
                Dispatcher.postToUI(() -> targetProperty.set(20));
            });

        Assert.assertEquals(20, sourceProperty.get());
    }

    @Test
    public void ObjectProperty_BindBidirectionalToUI() {
        AsyncObjectProperty<Foo> sourceProperty =
            new SimpleAsyncObjectProperty<>(
                null, new PropertyMetadata.Builder<Foo>().customBean(true).initialValue(Foo.BAR).create());
        AsyncObjectProperty<Foo> targetProperty =
            new UIAsyncObjectProperty<>(null, new UIPropertyMetadata.Builder<Foo>().customBean(true).create());

        ChangeListener<? super Foo> changeListener =
            (observable, oldValue, newValue) -> {
                Assert.assertTrue(Platform.isFxApplicationThread());
                signal();
            };

        await(
            2,
            () -> {

                // This listener will always be called on the UI thread
                targetProperty.addListener(changeListener);

                // If the source property changes, the change will always be propagated to the target property on the UI
                // thread
                targetProperty.bindBidirectional(sourceProperty);

                // We're on a non-UI thread here:
                Assert.assertTrue(!Platform.isFxApplicationThread());
                sourceProperty.set(Foo.BAZ);
            });

        targetProperty.removeListener(changeListener);

        await(
            1,
            () -> {
                // The source property listener may be called on any thread
                sourceProperty.addListener(changeListener);

                // We're on a non-UI thread here, so this fails:
                try {
                    targetProperty.set(Foo.BAF);
                    Assert.fail();
                } catch (Exception e) {
                }

                Dispatcher.postToUI(() -> targetProperty.set(Foo.BAF));
            });
    }

    @Test
    public void BidirectionalBinding_ThreeWayBinding() {
        var syncCtx = Futures.getUnchecked(Dispatcher.run(SynchronizationContext::getCurrent));

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

        await(
            3,
            () -> {
                prop2.bindBidirectional(prop1);
                prop2.bindBidirectional(prop3);

                prop1.addListener(
                    ((observable, oldValue, newValue) -> {
                        Assert.assertTrue(!Platform.isFxApplicationThread());
                        signal();
                    }));

                prop2.addListener(
                    ((observable, oldValue, newValue) -> {
                        Assert.assertTrue(Platform.isFxApplicationThread());
                        signal();
                    }));

                prop3.addListener(
                    ((observable, oldValue, newValue) -> {
                        Assert.assertTrue(!Platform.isFxApplicationThread());
                        signal();
                    }));

                prop1.set("test");
            });

        Assert.assertTrue(prop1.get().equals("test") && prop2.get().equals("test") && prop3.get().equals("test"));
    }

    @Test
    public void BidirectionalBinding_AddRemoveBinding() {
        AsyncIntegerProperty prop1 =
            new SimpleAsyncIntegerProperty(null, new PropertyMetadata.Builder<Number>().customBean(true).create());
        AsyncIntegerProperty prop2 =
            new SimpleAsyncIntegerProperty(null, new PropertyMetadata.Builder<Number>().customBean(true).create());

        var count = new int[1];

        prop1.addListener(((observable, oldValue, newValue) -> count[0]++));
        Assert.assertEquals(0, count[0]);

        prop1.bindBidirectional(prop2);
        Assert.assertEquals(0, count[0]);

        prop2.set(1);
        Assert.assertEquals(1, count[0]);

        System.gc();

        prop2.set(2);
        Assert.assertEquals(2, count[0]);

        prop1.unbindBidirectional(prop2);
        prop2.set(3);
        prop2.set(4);
        prop2.set(5);
        Assert.assertEquals(2, count[0]);
    }

    @Test
    public void UIAsyncIntegerProperty_ListenOnUI() {
        AsyncIntegerProperty prop1 =
            new UIAsyncIntegerProperty(null, new UIPropertyMetadata.Builder<Number>().customBean(true).create());
        AsyncIntegerProperty prop2 =
            new SimpleAsyncIntegerProperty(
                null, new PropertyMetadata.Builder<Number>().customBean(true).initialValue(1).create());
        var count = new int[1];

        await(
            1,
            () -> {
                prop1.addListener(
                    ((observable, oldValue, newValue) -> {
                        Assert.assertTrue(Platform.isFxApplicationThread());
                        count[0]++;
                        signal();
                    }));

                prop1.bind(prop2);
            });

        Assert.assertEquals(1, count[0]);

        await(
            1,
            () -> {
                prop2.set(2);
            });

        Assert.assertEquals(2, count[0]);
    }

    @Test
    public void UIAsyncIntegerProperty_IllegalAccess() {
        AsyncIntegerProperty prop1 =
            new UIAsyncIntegerProperty(null, new UIPropertyMetadata.Builder<Number>().customBean(true).create());
        AsyncIntegerProperty prop2 =
            new SimpleAsyncIntegerProperty(null, new PropertyMetadata.Builder<Number>().customBean(true).create());

        // Fails: prop1 can't be set from a non-UI thread.
        try {
            prop1.set(1);
            Assert.fail();
        } catch (IllegalStateException e) {
        }

        // Works
        try {
            prop1.bind(prop2);
        } catch (IllegalStateException e) {
            Assert.fail();
        }

        // Works
        try {
            prop2.set(2);
        } catch (IllegalStateException e) {
            Assert.fail();
        }
    }

    @Test
    public void IntegerProperty_InvalidationListenerInvalidatesOnlyOnce() {
        int[] count = new int[1];
        var prop =
            new SimpleAsyncIntegerProperty(null, new PropertyMetadata.Builder<Number>().customBean(true).create());
        prop.addListener(listener -> count[0]++);
        prop.set(1);
        Assert.assertEquals(1, count[0]);
        prop.set(2);
        Assert.assertEquals(1, count[0]);
        prop.set(3);
        Assert.assertEquals(1, count[0]);
    }

    @Test
    public void IntegerProperty_ChangeListenerAlwaysInvalidates() {
        int[] count = new int[1];
        var prop =
            new SimpleAsyncIntegerProperty(null, new PropertyMetadata.Builder<Number>().customBean(true).create());
        prop.addListener((observable, oldValue, newValue) -> count[0]++);
        prop.set(1);
        Assert.assertEquals(1, count[0]);
        prop.set(2);
        Assert.assertEquals(2, count[0]);
        prop.set(3);
        Assert.assertEquals(3, count[0]);
    }

    @Test
    public void IntegerProperty_InavlidationListenerIsAlwaysCalledIfChangeListenerIsPresent() {
        int[] count = new int[1];
        var prop =
            new SimpleAsyncIntegerProperty(null, new PropertyMetadata.Builder<Number>().customBean(true).create());
        prop.addListener(listener -> count[0]++);
        prop.addListener((observable, oldValue, newValue) -> count[0]++);
        prop.set(1);
        Assert.assertEquals(2, count[0]);
        prop.set(2);
        Assert.assertEquals(4, count[0]);
        prop.set(3);
        Assert.assertEquals(6, count[0]);
    }

}
