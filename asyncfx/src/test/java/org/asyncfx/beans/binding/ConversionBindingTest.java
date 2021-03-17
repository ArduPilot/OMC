/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.beans.binding;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import org.asyncfx.Awaiter;
import org.asyncfx.TestBase;
import org.asyncfx.beans.property.PropertyMetadata;
import org.asyncfx.beans.property.SimpleAsyncObjectProperty;
import org.asyncfx.beans.property.SimpleAsyncStringProperty;
import org.asyncfx.beans.property.UIAsyncIntegerProperty;
import org.asyncfx.beans.property.UIAsyncObjectProperty;
import org.asyncfx.beans.property.UIPropertyMetadata;
import org.asyncfx.concurrent.SynchronizationContext;
import org.junit.jupiter.api.Test;

class ConversionBindingTest extends TestBase {

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
    void ContentBinding_List_StringToInt() {
        var sourceListProp = new SimpleListProperty<Integer>(FXCollections.observableArrayList());
        var targetListProp = new SimpleListProperty<String>(FXCollections.observableArrayList());

        ConversionBindings.bindContent(targetListProp, sourceListProp, Object::toString);

        sourceListProp.add(1);
        assertTrue(targetListProp.get(0).equalsIgnoreCase("1"));

        sourceListProp.add(2);
        assertTrue(targetListProp.get(1).equalsIgnoreCase("2"));

        sourceListProp.remove(0);
        assertTrue(targetListProp.get(0).equalsIgnoreCase("2"));

        sourceListProp.set(0, 3);
        assertTrue(targetListProp.get(0).equalsIgnoreCase("3"));

        ConversionBindings.unbindContent(targetListProp, sourceListProp);

        sourceListProp.set(0, 4);
        assertTrue(targetListProp.get(0).equalsIgnoreCase("3"));

        sourceListProp.add(5);
        assertEquals(1, targetListProp.size());
    }

    @Test
    void ContentBinding_CompositeObject() {
        var sourceListProp =
            new SimpleListProperty<FooInt>(FXCollections.observableArrayList(item -> new Observable[] {item.field}));
        var targetListProp = new SimpleListProperty<FooStr>(FXCollections.observableArrayList());

        ConversionBindings.bindContent(
            targetListProp, sourceListProp, value -> new FooStr(Integer.toString(value.field.get())));

        sourceListProp.add(new FooInt(1));
        assertTrue(targetListProp.get(0).field.get().equalsIgnoreCase("1"));

        sourceListProp.add(new FooInt(2));
        assertTrue(targetListProp.get(1).field.get().equalsIgnoreCase("2"));

        sourceListProp.remove(0);
        assertTrue(targetListProp.get(0).field.get().equalsIgnoreCase("2"));

        sourceListProp.get(0).field.set(3);
        assertTrue(targetListProp.get(0).field.get().equalsIgnoreCase("3"));

        ConversionBindings.unbindContent(targetListProp, sourceListProp);

        sourceListProp.add(new FooInt(5));
        assertEquals(1, targetListProp.size());
    }

    @Test
    void ContentBinding_Set_StringToInt() {
        var sourceListProp = new SimpleSetProperty<Integer>(FXCollections.observableSet());
        var targetListProp = new SimpleSetProperty<String>(FXCollections.observableSet());

        ConversionBindings.bindContent(targetListProp, sourceListProp, Object::toString);

        sourceListProp.add(1);
        assertTrue(targetListProp.contains("1"));

        sourceListProp.add(2);
        assertTrue(targetListProp.contains("2"));

        sourceListProp.remove(2);
        assertTrue(!targetListProp.contains("2"));

        sourceListProp.add(3);
        assertTrue(targetListProp.contains("3"));

        ConversionBindings.unbindContent(targetListProp, sourceListProp);

        sourceListProp.add(4);
        assertTrue(!targetListProp.contains("4"));
    }

    @Test
    void ConversionBinding_AsyncStringToInt() {
        var syncCtx = SynchronizationContext.getCurrent();
        var sourceProp = new UIAsyncIntegerProperty(this);
        var targetProp =
            new SimpleAsyncStringProperty(
                this, new PropertyMetadata.Builder<String>().synchronizationContext(syncCtx).create());

        var awaiter = new Awaiter();

        AtomicReference<String> expected = new AtomicReference<>();
        expected.set("0");

        targetProp.addListener(
            invalidation -> {
                awaiter.assertEquals(expected.get(), targetProp.get());
                awaiter.signal();
            });

        targetProp.bind(sourceProp, Object::toString);
        awaiter.await(1);

        expected.set("1");
        Platform.runLater(() -> sourceProp.set(1));
        awaiter.await(1);

        expected.set("2");
        Platform.runLater(() -> sourceProp.set(2));
        awaiter.await(1);
    }

    @Test
    void ConversionBinding_AsyncCompositeObjects() {
        var syncCtx = SynchronizationContext.getCurrent();

        var sourceProp =
            new UIAsyncObjectProperty<>(
                this, new UIPropertyMetadata.Builder<FooInt>().initialValue(new FooInt(0)).create());

        var targetProp =
            new SimpleAsyncObjectProperty<>(
                this,
                new PropertyMetadata.Builder<FooStr>()
                    .initialValue(new FooStr(""))
                    .synchronizationContext(syncCtx)
                    .create());

        var awaiter = new Awaiter();

        AtomicReference<String> expected = new AtomicReference<>();
        expected.set("0");

        targetProp.addListener(
            invalidation -> {
                awaiter.assertEquals(expected.get(), targetProp.get().field.get());
                awaiter.signal();
            });

        targetProp.bind(sourceProp, value -> new FooStr(Integer.toString(value.field.get())));
        awaiter.await(1);

        expected.set("1");
        Platform.runLater(() -> sourceProp.set(new FooInt(1)));
        awaiter.await(1);

        expected.set("2");
        Platform.runLater(() -> sourceProp.set(new FooInt(2)));
        awaiter.await(1);
    }

}
