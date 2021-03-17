/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.beans.property;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import org.asyncfx.Awaiter;
import org.asyncfx.TestBase;
import org.asyncfx.collections.AsyncObservableList;
import org.asyncfx.collections.FXAsyncCollections;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class PropertyPathTest extends TestBase {

    class First {
        private final ObjectProperty<Second> second = new SimpleObjectProperty<>(new Second());

        ObjectProperty<Second> secondProperty() {
            return second;
        }
    }

    class Second {
        private final BooleanProperty value = new SimpleBooleanProperty(this, "value");
        private final ListProperty<Integer> list =
            new SimpleListProperty<>(this, "list", FXCollections.observableArrayList());

        BooleanProperty valueProperty() {
            return value;
        }

        ListProperty<Integer> listProperty() {
            return list;
        }
    }

    class FirstAsync {
        private final AsyncObjectProperty<SecondAsync> second =
            new SimpleAsyncObjectProperty<>(
                this, new PropertyMetadata.Builder<SecondAsync>().initialValue(new SecondAsync()).create());

        AsyncObjectProperty<SecondAsync> secondProperty() {
            return second;
        }
    }

    class SecondAsync {
        private final AsyncBooleanProperty value = new SimpleAsyncBooleanProperty(this);
        private final AsyncListProperty<Integer> list =
            new SimpleAsyncListProperty<>(
                this,
                new PropertyMetadata.Builder<AsyncObservableList<Integer>>()
                    .initialValue(FXAsyncCollections.observableArrayList())
                    .create());

        AsyncBooleanProperty valueProperty() {
            return value;
        }

        AsyncListProperty<Integer> listProperty() {
            return list;
        }
    }

    class FirstAsyncUI {
        private final AsyncObjectProperty<SecondAsyncUI> second =
            new UIAsyncObjectProperty<>(
                this, new UIPropertyMetadata.Builder<SecondAsyncUI>().initialValue(new SecondAsyncUI()).create());

        AsyncObjectProperty<SecondAsyncUI> secondProperty() {
            return second;
        }
    }

    class SecondAsyncUI {
        private final AsyncBooleanProperty value = new UIAsyncBooleanProperty(this);

        AsyncBooleanProperty valueProperty() {
            return value;
        }
    }

    @Nested
    class Listeners {
        @Test
        void Endpoint_ChangeListener_Is_Called_When_Value_Changes() {
            var counter = new int[1];
            var prop = new SimpleObjectProperty<First>(new First());
            var path = PropertyPath.from(prop).select(First::secondProperty).selectBoolean(Second::valueProperty);
            path.addListener(((observable, oldValue, newValue) -> counter[0]++));
            assertEquals(0, counter[0]);
            prop.get().secondProperty().get().valueProperty().set(true);
            assertEquals(1, counter[0]);
        }

        @Test
        void Async_Endpoint_ChangeListener_Is_Called_When_Value_Changes() {
            var counter = new int[1];
            var prop =
                new SimpleAsyncObjectProperty<>(
                    null,
                    new PropertyMetadata.Builder<FirstAsync>()
                        .customBean(true)
                        .initialValue(new FirstAsync())
                        .create());
            var path =
                PropertyPath.from(prop)
                    .select(FirstAsync::secondProperty)
                    .selectAsyncBoolean(SecondAsync::valueProperty);
            path.addListener(((observable, oldValue, newValue) -> counter[0]++));
            assertEquals(0, counter[0]);
            prop.get().secondProperty().get().valueProperty().set(true);
            assertEquals(1, counter[0]);
        }

        @Test
        void UIAsync_Endpoint_ChangeListener_Is_Called_When_Value_Changes() {
            var awaiter = new Awaiter();

            var prop =
                new SimpleAsyncObjectProperty<>(
                    null,
                    new PropertyMetadata.Builder<FirstAsyncUI>()
                        .customBean(true)
                        .initialValue(new FirstAsyncUI())
                        .create());

            var path =
                PropertyPath.from(prop)
                    .select(FirstAsyncUI::secondProperty)
                    .selectAsyncBoolean(SecondAsyncUI::valueProperty);

            path.addListener(
                ((observable, oldValue, newValue) -> {
                    awaiter.assertTrue(Platform.isFxApplicationThread());
                    awaiter.signal();
                }));

            PropertyHelper.setValueSafe(prop.get().secondProperty().get().valueProperty(), true);

            awaiter.await(1);
        }

    }

    @Nested
    class UnresolvedPath {
        @Test
        void Setting_Value_To_An_Unresolved_Endpoint_Has_No_Effect() {
            var sourceProp =
                new SimpleAsyncObjectProperty<>(null, new PropertyMetadata.Builder<First>().customBean(true).create());

            var endpoint =
                PropertyPath.from(sourceProp).select(First::secondProperty).selectBoolean(Second::valueProperty);

            endpoint.set(true); // has no effect because the path is unresolved

            assertFalse(endpoint.get());
        }

        @Test
        void Setting_Value_To_An_Unresolved_Async_Endpoint_Has_No_Effect() {
            var sourceProp =
                new SimpleAsyncObjectProperty<>(
                    null, new PropertyMetadata.Builder<FirstAsync>().customBean(true).create());

            var endpoint =
                PropertyPath.from(sourceProp)
                    .select(FirstAsync::secondProperty)
                    .selectAsyncBoolean(SecondAsync::valueProperty);

            endpoint.set(true); // has no effect because the path is unresolved

            assertFalse(endpoint.get());
        }

        @Test
        void Override_Metadata_Of_Unresolved_Async_Endpoint_Has_No_Effect() {
            var sourceProp =
                new SimpleAsyncObjectProperty<>(
                    null, new PropertyMetadata.Builder<FirstAsync>().customBean(true).create());

            var endpoint =
                PropertyPath.from(sourceProp)
                    .select(FirstAsync::secondProperty)
                    .selectAsyncBoolean(SecondAsync::valueProperty);

            assertNull(endpoint.getMetadata().getInitialValue());
            endpoint.overrideMetadata(new PropertyMetadata.Builder<Boolean>().initialValue(true).create());
            assertNull(endpoint.getMetadata().getInitialValue());
        }
    }

    @Nested
    class SourceBinding {
        @Test
        void LocalProperty_Is_Bound_To_Path_Endpoint() {
            var awaiter = new Awaiter();

            var sourceProp =
                new SimpleAsyncObjectProperty<>(
                    null,
                    new PropertyMetadata.Builder<FirstAsyncUI>()
                        .customBean(true)
                        .initialValue(new FirstAsyncUI())
                        .create());

            var targetProp =
                new UIAsyncBooleanProperty(null, new UIPropertyMetadata.Builder<Boolean>().customBean(true).create());

            targetProp.bind(
                PropertyPath.from(sourceProp)
                    .select(FirstAsyncUI::secondProperty)
                    .selectReadOnlyAsyncBoolean(SecondAsyncUI::valueProperty));

            System.gc();

            targetProp.addListener(
                ((observable, oldValue, newValue) -> {
                    awaiter.assertTrue(Platform.isFxApplicationThread());
                    awaiter.signal();
                }));

            PropertyHelper.setValueSafe(sourceProp.get().secondProperty().get().valueProperty(), true);

            awaiter.await(1);
        }

        @Test
        void LocalProperty_Is_Bidirectionally_Bound_To_Path_Endpoint() {
            var awaiter = new Awaiter();

            var sourceProp =
                new SimpleAsyncObjectProperty<>(
                    null,
                    new PropertyMetadata.Builder<FirstAsyncUI>()
                        .customBean(true)
                        .initialValue(new FirstAsyncUI())
                        .create());

            var targetProp =
                new UIAsyncBooleanProperty(null, new UIPropertyMetadata.Builder<Boolean>().customBean(true).create());

            var endpoint =
                PropertyPath.from(sourceProp)
                    .select(FirstAsyncUI::secondProperty)
                    .selectAsyncBoolean(SecondAsyncUI::valueProperty);

            targetProp.bindBidirectional(endpoint);

            System.gc();

            targetProp.addListener(
                ((observable, oldValue, newValue) -> {
                    awaiter.assertTrue(Platform.isFxApplicationThread());
                    awaiter.signal();
                }));

            PropertyHelper.setValueSafe(sourceProp.get().secondProperty().get().valueProperty(), true);

            awaiter.await(1);
        }

        @Test
        void LocalProperties_Are_Bound_To_List_SizeProperty_And_EmptyProperty() {
            var sourceProp = new SimpleObjectProperty<First>();

            var size = new SimpleIntegerProperty();
            var empty = new SimpleBooleanProperty();

            // --------------------------------------------------------------------
            // First scenario: select a mutable list
            size.bind(
                PropertyPath.from(sourceProp)
                    .select(First::secondProperty)
                    .selectList(Second::listProperty)
                    .sizeProperty());

            empty.bind(
                PropertyPath.from(sourceProp)
                    .select(First::secondProperty)
                    .selectList(Second::listProperty)
                    .emptyProperty());

            assertEquals(0, size.get());
            assertTrue(empty.get());

            var first = new First();
            first.second.get().list.add(0);
            first.second.get().list.add(1);
            sourceProp.set(first);

            assertEquals(2, size.get());
            assertFalse(empty.get());

            sourceProp.set(null);

            // --------------------------------------------------------------------
            // Second scenario: select a read-only list
            size.bind(
                PropertyPath.from(sourceProp)
                    .select(First::secondProperty)
                    .selectReadOnlyList(Second::listProperty)
                    .sizeProperty());

            empty.bind(
                PropertyPath.from(sourceProp)
                    .select(First::secondProperty)
                    .selectReadOnlyList(Second::listProperty)
                    .emptyProperty());

            assertEquals(0, size.get());
            assertTrue(empty.get());

            first = new First();
            first.second.get().list.add(0);
            first.second.get().list.add(1);
            sourceProp.set(first);

            assertEquals(2, size.get());
            assertFalse(empty.get());
        }

        @Test
        void LocalProperties_Are_Bound_To_AsyncList_SizeProperty_And_EmptyProperty() {
            var sourceProp =
                new SimpleAsyncObjectProperty<>(
                    null, new PropertyMetadata.Builder<FirstAsync>().customBean(true).create());

            var size =
                new SimpleAsyncIntegerProperty(null, new PropertyMetadata.Builder<Number>().customBean(true).create());
            var empty =
                new SimpleAsyncBooleanProperty(null, new PropertyMetadata.Builder<Boolean>().customBean(true).create());

            // --------------------------------------------------------------------
            // First scenario: select a mutable list
            size.bind(
                PropertyPath.from(sourceProp)
                    .select(FirstAsync::secondProperty)
                    .selectAsyncList(SecondAsync::listProperty)
                    .sizeProperty());

            empty.bind(
                PropertyPath.from(sourceProp)
                    .select(FirstAsync::secondProperty)
                    .selectAsyncList(SecondAsync::listProperty)
                    .emptyProperty());

            assertEquals(0, size.get());
            assertTrue(empty.get());

            var first = new FirstAsync();
            first.second.get().list.add(0);
            first.second.get().list.add(1);
            sourceProp.set(first);

            assertEquals(2, size.get());
            assertFalse(empty.get());

            sourceProp.set(null);

            // --------------------------------------------------------------------
            // Second scenario: select a read-only list
            size.bind(
                PropertyPath.from(sourceProp)
                    .select(FirstAsync::secondProperty)
                    .selectReadOnlyAsyncList(SecondAsync::listProperty)
                    .sizeProperty());

            empty.bind(
                PropertyPath.from(sourceProp)
                    .select(FirstAsync::secondProperty)
                    .selectReadOnlyAsyncList(SecondAsync::listProperty)
                    .emptyProperty());

            assertEquals(0, size.get());
            assertTrue(empty.get());

            first = new FirstAsync();
            first.second.get().list.add(0);
            first.second.get().list.add(1);
            sourceProp.set(first);

            assertEquals(2, size.get());
            assertFalse(empty.get());
        }
    }

    @Nested
    class TargetBinding {
        @Test
        void Endpoint_Can_Be_Unidirectional_Binding_Target() {
            var sourceProp = new SimpleBooleanProperty();

            var targetProp =
                new SimpleAsyncObjectProperty<>(null, new PropertyMetadata.Builder<First>().customBean(true).create());

            // property path is unresolved
            var targetEndpoint =
                PropertyPath.from(targetProp).select(First::secondProperty).selectBoolean(Second::valueProperty);
            assertFalse(targetEndpoint.get());

            // bind the target unidirectionally to source
            targetEndpoint.bind(sourceProp);
            assertFalse(targetEndpoint.get());

            // now property path will be resolved
            var firstObj1 = new First();
            firstObj1.second.get().value.set(true);
            targetProp.set(firstObj1);
            assertFalse(targetEndpoint.get());

            // set source=true, which updates target
            sourceProp.set(true);
            assertTrue(targetEndpoint.get());

            // target can't be set via proxy, because it's bound
            try {
                targetEndpoint.set(false);
                fail();
            } catch (RuntimeException expected) {
            }

            // target also can't be set via original property
            try {
                firstObj1.second.get().value.set(false);
                fail();
            } catch (RuntimeException expected) {
            }

            // change the object at the end of the property path to a different instance
            var firstObj2 = new First();
            firstObj2.second.get().value.set(false);
            targetProp.set(firstObj2); // --> sets the endpoint to TRUE because source=TRUE
            assertTrue(targetEndpoint.get());

            // the old object at the end of the property path is not bound any longer and can be set manually
            firstObj1.second.get().value.set(false);

            // the new object at the end of the property path is bound
            try {
                firstObj2.second.get().value.set(false);
                fail();
            } catch (RuntimeException expected) {
            }

            // remove the binding, then the target can be set again manually
            targetEndpoint.unbind();
            firstObj1.second.get().value.set(true);
            assertTrue(targetEndpoint.get());
        }

        @Test
        void Async_Endpoint_Can_Be_Unidirectional_Binding_Target() {
            var sourceProp =
                new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().customBean(true).create());

            var targetProp =
                new SimpleAsyncObjectProperty<>(
                    null, new PropertyMetadata.Builder<FirstAsync>().customBean(true).create());

            // property path is unresolved
            var targetEndpoint =
                PropertyPath.from(targetProp)
                    .select(FirstAsync::secondProperty)
                    .selectAsyncBoolean(SecondAsync::valueProperty);
            assertFalse(targetEndpoint.get());

            // bind the target unidirectionally to source
            targetEndpoint.bind(sourceProp);
            assertFalse(targetEndpoint.get());

            // now property path will be resolved
            var firstObj1 = new FirstAsync();
            firstObj1.second.get().value.set(true);
            targetProp.set(firstObj1);
            assertFalse(targetEndpoint.get());

            // set source=true, which updates target
            sourceProp.set(true);
            assertTrue(targetEndpoint.get());

            // target can't be set via proxy, because it's bound
            try {
                targetEndpoint.set(false);
                fail();
            } catch (RuntimeException expected) {
            }

            // target also can't be set via original property
            try {
                firstObj1.second.get().value.set(false);
                fail();
            } catch (RuntimeException expected) {
            }

            // change the object at the end of the property path to a different instance
            var firstObj2 = new FirstAsync();
            firstObj2.second.get().value.set(false);
            targetProp.set(firstObj2); // --> sets the endpoint to TRUE because source=TRUE
            assertTrue(targetEndpoint.get());

            // the old object at the end of the property path is not bound any longer and can be set manually
            firstObj1.second.get().value.set(false);

            // the new object at the end of the property path is bound
            try {
                firstObj2.second.get().value.set(false);
                fail();
            } catch (RuntimeException expected) {
            }

            // remove the binding, then the target can be set again manually
            targetEndpoint.unbind();
            firstObj1.second.get().value.set(true);
            assertTrue(targetEndpoint.get());
        }

        @Test
        void Endpoint_Can_Be_Bound_Bidirectionally() {
            var sourceProp = new SimpleBooleanProperty(null, "source");

            var targetProp =
                new SimpleAsyncObjectProperty<>(null, new PropertyMetadata.Builder<First>().customBean(true).create());

            // property path is unresolved
            var targetEndpoint =
                PropertyPath.from(targetProp).select(First::secondProperty).selectBoolean(Second::valueProperty);
            assertFalse(targetEndpoint.get());

            // --------------------------------------------------------------------
            // First scenario: bind unresolved target to source
            targetEndpoint.bindBidirectional(sourceProp);
            assertFalse(targetEndpoint.get());

            // now property path will be resolved
            var firstObj = new First();
            firstObj.second.get().value.set(true);
            targetProp.set(firstObj);
            assertTrue(targetEndpoint.get());

            // set source=false, which updates target
            sourceProp.set(false);
            assertFalse(targetEndpoint.get());

            // set target=true, which updates source
            targetEndpoint.set(true);
            assertTrue(sourceProp.get());

            // reset
            targetEndpoint.unbindBidirectional(sourceProp);
            targetProp.set(null);
            assertFalse(targetEndpoint.get());

            // --------------------------------------------------------------------
            // Second scenario: bind source to unresolved target
            sourceProp.bindBidirectional(targetEndpoint);
            assertFalse(sourceProp.get());

            // now property path will be resolved again
            firstObj = new First();
            firstObj.second.get().value.set(true);
            targetProp.set(firstObj);
            assertTrue(sourceProp.get());

            // set source=false, which updates target
            sourceProp.set(false);
            assertFalse(targetEndpoint.get());

            // set target=true, which updates source
            targetEndpoint.set(true);
            assertTrue(sourceProp.get());
        }

        @Test
        void Async_Endpoint_Can_Be_Bound_Bidirectionally() {
            var sourceProp =
                new SimpleAsyncBooleanProperty(null, new PropertyMetadata.Builder<Boolean>().customBean(true).create());

            var targetProp =
                new SimpleAsyncObjectProperty<>(
                    null, new PropertyMetadata.Builder<FirstAsync>().customBean(true).create());

            // property path is unresolved
            var targetEndpoint =
                PropertyPath.from(targetProp)
                    .select(FirstAsync::secondProperty)
                    .selectAsyncBoolean(SecondAsync::valueProperty);
            assertFalse(targetEndpoint.get());

            // --------------------------------------------------------------------
            // First scenario: bind unresolved target to source
            targetEndpoint.bindBidirectional(sourceProp);
            assertFalse(targetEndpoint.get());

            // now property path will be resolved
            var firstObj = new FirstAsync();
            firstObj.second.get().value.set(true);
            targetProp.set(firstObj);
            assertTrue(targetEndpoint.get());

            // set source=false, which updates target
            sourceProp.set(false);
            assertFalse(targetEndpoint.get());

            // set target=true, which updates source
            targetEndpoint.set(true);
            assertTrue(sourceProp.get());

            // reset
            targetEndpoint.unbindBidirectional(sourceProp);
            targetProp.set(null);
            assertFalse(targetEndpoint.get());

            // --------------------------------------------------------------------
            // Second scenario: bind source to unresolved target
            sourceProp.bindBidirectional(targetEndpoint);
            assertFalse(sourceProp.get());

            // now property path will be resolved again
            firstObj = new FirstAsync();
            firstObj.second.get().value.set(true);
            targetProp.set(firstObj);
            assertTrue(sourceProp.get());

            // set source=false, which updates target
            sourceProp.set(false);
            assertFalse(targetEndpoint.get());

            // set target=true, which updates source
            targetEndpoint.set(true);
            assertTrue(sourceProp.get());
        }

        @Test
        void List_Endpoint_Is_ContentBound() {
            var sourceProp = new SimpleListProperty<Integer>(FXCollections.observableArrayList());
            sourceProp.addAll(1, 2, 3);

            var targetProp =
                new SimpleAsyncObjectProperty<>(null, new PropertyMetadata.Builder<First>().customBean(true).create());

            var endpoint = PropertyPath.from(targetProp).select(First::secondProperty).selectList(Second::listProperty);
            endpoint.bindContent(sourceProp);

            targetProp.set(new First());

            assertEquals(3, endpoint.size());
        }

        @Test
        void AsyncList_Endpoint_Is_ContentBound() {
            var sourceProp =
                new SimpleAsyncListProperty<>(
                    this,
                    new PropertyMetadata.Builder<AsyncObservableList<Integer>>()
                        .customBean(true)
                        .initialValue(FXAsyncCollections.observableArrayList())
                        .create());

            sourceProp.addAll(1, 2, 3);

            var targetProp =
                new SimpleAsyncObjectProperty<>(
                    null, new PropertyMetadata.Builder<FirstAsync>().customBean(true).create());

            var endpoint =
                PropertyPath.from(targetProp)
                    .select(FirstAsync::secondProperty)
                    .selectAsyncList(SecondAsync::listProperty);
            endpoint.bindContent(sourceProp);

            targetProp.set(new FirstAsync());

            assertEquals(3, endpoint.size());
        }

    }

}
