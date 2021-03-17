/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.property;

import com.google.common.util.concurrent.Futures;
import com.intel.missioncontrol.concurrent.AwaitableTestBase;
import com.intel.missioncontrol.beans.binding.ConversionBindings;
import com.intel.missioncontrol.beans.property.PropertyMetadata;
import com.intel.missioncontrol.beans.property.SimpleAsyncObjectProperty;
import com.intel.missioncontrol.beans.property.SimpleAsyncStringProperty;
import com.intel.missioncontrol.beans.property.UIAsyncIntegerProperty;
import com.intel.missioncontrol.beans.property.UIAsyncObjectProperty;
import com.intel.missioncontrol.beans.property.UIPropertyMetadata;
import com.intel.missioncontrol.concurrent.Dispatcher;
import com.intel.missioncontrol.concurrent.SynchronizationContext;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleSetProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testfx.api.FxToolkit;

public class ConversionBindingTest extends AwaitableTestBase {

    @BeforeClass
    public static void globalSetup() throws Exception {
        FxToolkit.registerPrimaryStage();
    }

    private static class FooInt {
        IntegerProperty field = new SimpleIntegerProperty();

        FooInt(int field) {
            this.field.set(field);
        }

        @Override
        public String toString() {
            return "FooInt = " + field.get();
        }
    }

    private static class FooStr {
        StringProperty field = new SimpleStringProperty();

        FooStr(String field) {
            this.field.set(field);
        }

        @Override
        public String toString() {
            return "FooStr = " + field.get();
        }
    }

    @Test
    public void ContentBinding_List_StringToInt() {
        var sourceListProp = new SimpleListProperty<Integer>(FXCollections.observableArrayList());
        var targetListProp = new SimpleListProperty<String>(FXCollections.observableArrayList());

        ConversionBindings.bindContent(targetListProp, sourceListProp, Object::toString);

        sourceListProp.add(1);
        Assert.assertTrue(targetListProp.get(0).equalsIgnoreCase("1"));

        sourceListProp.add(2);
        Assert.assertTrue(targetListProp.get(1).equalsIgnoreCase("2"));

        sourceListProp.remove(0);
        Assert.assertTrue(targetListProp.get(0).equalsIgnoreCase("2"));

        sourceListProp.set(0, 3);
        Assert.assertTrue(targetListProp.get(0).equalsIgnoreCase("3"));

        ConversionBindings.unbindContent(targetListProp, sourceListProp);

        sourceListProp.set(0, 4);
        Assert.assertTrue(targetListProp.get(0).equalsIgnoreCase("3"));

        sourceListProp.add(5);
        Assert.assertEquals(1, targetListProp.size());
    }

    @Test
    public void ContentBinding_CompositeObject() {
        var sourceListProp =
            new SimpleListProperty<FooInt>(FXCollections.observableArrayList(item -> new Observable[] {item.field}));
        var targetListProp = new SimpleListProperty<FooStr>(FXCollections.observableArrayList());

        ConversionBindings.bindContent(
            targetListProp, sourceListProp, value -> new FooStr(Integer.toString(value.field.get())));

        sourceListProp.add(new FooInt(1));
        Assert.assertTrue(targetListProp.get(0).field.get().equalsIgnoreCase("1"));

        sourceListProp.add(new FooInt(2));
        Assert.assertTrue(targetListProp.get(1).field.get().equalsIgnoreCase("2"));

        sourceListProp.remove(0);
        Assert.assertTrue(targetListProp.get(0).field.get().equalsIgnoreCase("2"));

        sourceListProp.get(0).field.set(3);
        Assert.assertTrue(targetListProp.get(0).field.get().equalsIgnoreCase("3"));

        ConversionBindings.unbindContent(targetListProp, sourceListProp);

        sourceListProp.add(new FooInt(5));
        Assert.assertEquals(1, targetListProp.size());
    }

    @Test
    public void ContentBinding_Set_StringToInt() {
        var sourceListProp = new SimpleSetProperty<Integer>(FXCollections.observableSet());
        var targetListProp = new SimpleSetProperty<String>(FXCollections.observableSet());

        ConversionBindings.bindContent(targetListProp, sourceListProp, Object::toString);

        sourceListProp.add(1);
        Assert.assertTrue(targetListProp.contains("1"));

        sourceListProp.add(2);
        Assert.assertTrue(targetListProp.contains("2"));

        sourceListProp.remove(2);
        Assert.assertTrue(!targetListProp.contains("2"));

        sourceListProp.add(3);
        Assert.assertTrue(targetListProp.contains("3"));

        ConversionBindings.unbindContent(targetListProp, sourceListProp);

        sourceListProp.add(4);
        Assert.assertTrue(!targetListProp.contains("4"));
    }

    @Test
    public void ConversionBinding_AsyncStringToInt() {
        var syncCtx = Futures.getUnchecked(Dispatcher.run(SynchronizationContext::getCurrent));
        var sourceProp = new UIAsyncIntegerProperty(this);
        var targetProp =
            new SimpleAsyncStringProperty(
                this, new PropertyMetadata.Builder<String>().synchronizationContext(syncCtx).create());

        targetProp.bind(sourceProp, Object::toString);

        AtomicReference<String> expected = new AtomicReference<>();

        targetProp.addListener(
            invalidation -> {
                Assert.assertTrue(targetProp.get().equalsIgnoreCase(expected.get()));
                signal();
            });

        await(
            1,
            () -> {
                expected.set("1");
                Platform.runLater(() -> sourceProp.set(1));
            });

        await(
            1,
            () -> {
                expected.set("2");
                Platform.runLater(() -> sourceProp.set(2));
            });
    }

    @Test
    public void ConversionBinding_AsyncCompositeObjects() {
        var syncCtx = Futures.getUnchecked(Dispatcher.run(SynchronizationContext::getCurrent));
        var sourceProp =
            new UIAsyncObjectProperty<>(
                this,
                new UIPropertyMetadata.Builder<FooInt>()
                    .initialValue(new FooInt(0))
                    .create());
        var targetProp =
            new SimpleAsyncObjectProperty<>(
                this,
                new PropertyMetadata.Builder<FooStr>()
                    .initialValue(new FooStr(""))
                    .synchronizationContext(syncCtx)
                    .create());

        targetProp.bind(sourceProp, value -> new FooStr(Integer.toString(value.field.get())));

        AtomicReference<FooStr> expected = new AtomicReference<>();

        targetProp.addListener(
            invalidation -> {
                Assert.assertTrue(targetProp.get().field.get().equalsIgnoreCase(expected.get().field.get()));
                signal();
            });

        await(
            1,
            () -> {
                expected.set(new FooStr("1"));
                Platform.runLater(() -> sourceProp.set(new FooInt(1)));
            });

        await(
            1,
            () -> {
                expected.set(new FooStr("2"));
                Platform.runLater(() -> sourceProp.set(new FooInt(2)));
            });
    }

}
