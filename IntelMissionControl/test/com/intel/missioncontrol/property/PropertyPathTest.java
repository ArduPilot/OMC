/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.property;

import com.intel.missioncontrol.concurrent.AwaitableTestBase;
import com.intel.missioncontrol.beans.property.AsyncBooleanProperty;
import com.intel.missioncontrol.beans.property.AsyncObjectProperty;
import com.intel.missioncontrol.beans.property.PropertyMetadata;
import com.intel.missioncontrol.beans.property.PropertyPath;
import com.intel.missioncontrol.beans.property.SimpleAsyncBooleanProperty;
import com.intel.missioncontrol.beans.property.SimpleAsyncObjectProperty;
import com.intel.missioncontrol.beans.property.UIAsyncBooleanProperty;
import com.intel.missioncontrol.beans.property.UIAsyncObjectProperty;
import com.intel.missioncontrol.beans.property.UIPropertyMetadata;
import com.intel.missioncontrol.diagnostics.Debugger;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testfx.api.FxToolkit;

public class PropertyPathTest extends AwaitableTestBase {

    @BeforeClass
    public static void globalSetup() throws Exception {
        FxToolkit.registerPrimaryStage();
        Debugger.setIsRunningTests(true);
    }

    public class Bar {
        private final BooleanProperty value = new SimpleBooleanProperty();

        BooleanProperty valueProperty() {
            return value;
        }

    }

    public class Foo {
        private final ObjectProperty<Bar> bar = new SimpleObjectProperty<>(new Bar());

        ObjectProperty<Bar> barProperty() {
            return bar;
        }
    }

    public class BarAsync {
        private final AsyncBooleanProperty value = new SimpleAsyncBooleanProperty(this);

        AsyncBooleanProperty valueProperty() {
            return value;
        }
    }

    public class FooAsync {
        private final AsyncObjectProperty<BarAsync> bar =
            new SimpleAsyncObjectProperty<>(
                this, new PropertyMetadata.Builder<BarAsync>().initialValue(new BarAsync()).create());

        AsyncObjectProperty<BarAsync> barProperty() {
            return bar;
        }
    }

    public class UIBarAsync {
        private final AsyncBooleanProperty value = new UIAsyncBooleanProperty(this);

        AsyncBooleanProperty valueProperty() {
            return value;
        }
    }

    public class UIFooAsync {
        private final AsyncObjectProperty<UIBarAsync> bar =
            new UIAsyncObjectProperty<>(
                this, new UIPropertyMetadata.Builder<UIBarAsync>().initialValue(new UIBarAsync()).create());

        AsyncObjectProperty<UIBarAsync> barProperty() {
            return bar;
        }
    }

    @Test
    public void BooleanPathTest() {
        var counter = new int[1];
        var prop = new SimpleObjectProperty<Foo>(new Foo());
        var path = PropertyPath.from(prop).select(Foo::barProperty).selectBoolean(Bar::valueProperty);
        path.addListener(((observable, oldValue, newValue) -> counter[0]++));
        Assert.assertEquals(0, counter[0]);
        prop.get().barProperty().get().valueProperty().set(true);
        Assert.assertEquals(1, counter[0]);
    }

    @Test
    public void AsyncBooleanPath_Simple_Test() {
        var counter = new int[1];
        var prop =
            new SimpleAsyncObjectProperty<>(
                null, new PropertyMetadata.Builder<FooAsync>().customBean(true).initialValue(new FooAsync()).create());
        var path = PropertyPath.from(prop).select(FooAsync::barProperty).selectAsyncBoolean(BarAsync::valueProperty);
        path.addListener(((observable, oldValue, newValue) -> counter[0]++));
        Assert.assertEquals(0, counter[0]);
        prop.get().barProperty().get().valueProperty().set(true);
        Assert.assertEquals(1, counter[0]);
    }

    @Test
    public void AsyncBooleanPath_UIProperty_Test() {
        await(
            1,
            () -> {
                var prop =
                    new SimpleAsyncObjectProperty<>(
                        null,
                        new PropertyMetadata.Builder<UIFooAsync>()
                            .customBean(true)
                            .initialValue(new UIFooAsync())
                            .create());

                var path =
                    PropertyPath.from(prop)
                        .select(UIFooAsync::barProperty)
                        .selectAsyncBoolean(UIBarAsync::valueProperty);

                path.addListener(
                    ((observable, oldValue, newValue) -> {
                        Assert.assertTrue(Platform.isFxApplicationThread());
                        signal();
                    }));

                prop.get().barProperty().get().valueProperty().setAsync(true);
            });
    }

    @Test
    public void BindPath_Test() {
        await(
            1,
            () -> {
                var prop =
                    new SimpleAsyncObjectProperty<>(
                        null,
                        new PropertyMetadata.Builder<UIFooAsync>()
                            .customBean(true)
                            .initialValue(new UIFooAsync())
                            .create());

                var targetProp =
                    new UIAsyncBooleanProperty(
                        null, new UIPropertyMetadata.Builder<Boolean>().customBean(true).create());

                targetProp.bind(
                    PropertyPath.from(prop)
                        .select(UIFooAsync::barProperty)
                        .selectReadOnlyAsyncBoolean(UIBarAsync::valueProperty));

                System.gc();

                targetProp.addListener(
                    ((observable, oldValue, newValue) -> {
                        Assert.assertTrue(Platform.isFxApplicationThread());
                        signal();
                    }));

                prop.get().barProperty().get().valueProperty().setAsync(true);
            });
    }

}
