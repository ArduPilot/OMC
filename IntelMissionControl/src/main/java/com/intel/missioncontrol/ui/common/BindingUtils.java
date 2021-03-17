/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.common;

import com.intel.missioncontrol.helper.Expect;
import java.util.function.Function;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import javafx.scene.Node;

/** Set of helper methods to bind bean properties and JavaFX components. */
public class BindingUtils {

    /**
     * Create two-ways binding between {@code newObject} and {@code property}. If property had been bound with {@code
     * oldObject} those binding is removed before.
     *
     * @param property the watched property
     * @param propertySupplier function that returns property from {@code newObject} and {@code oldObject} to bind with
     *     {@code property}
     * @param oldObject object that was bound with {@code property} before (may be null)
     * @param newObject now binding target
     */
    public static <T, V> void rebindBidirectional(
            Property<T> property, Function<V, Property<T>> propertySupplier, V oldObject, V newObject) {
        if (oldObject != null) {
            propertySupplier.apply(oldObject).unbindBidirectional(property);
        }

        if (newObject != null) {
            property.bindBidirectional(propertySupplier.apply(newObject));
        }
    }

    /**
     * Unbind the property and then bind to a property of a new object
     *
     * @param property Property to rebind
     * @param propertySupplier function that returns the property to bind to
     * @param newObject owner of the property to bind to
     */
    public static <T, V> void rebind(
            Property<T> property, Function<V, ReadOnlyProperty<T>> propertySupplier, V newObject) {
        Expect.notNull(property, "property");

        property.unbind();

        if (newObject != null) {
            property.bind(propertySupplier.apply(newObject));
        }
    }

    /**
     * Map visibility and managembility of JavaFX {@code node} to {@code expression}.
     *
     * @param node the visual component
     * @param expression the expression controlling component visibility
     */
    public static void bindVisibility(Node node, BooleanExpression expression) {
        node.visibleProperty().bind(expression);
        node.managedProperty().bind(expression);
    }

    public static void unbindVisibility(Node node) {
        node.visibleProperty().unbind();
        node.managedProperty().unbind();
    }

    private BindingUtils() {
        // empty
    }

}
