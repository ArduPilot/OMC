/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.controls;

import com.intel.missioncontrol.common.Expect;
import de.saxsys.mvvmfx.ViewModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javafx.beans.DefaultProperty;
import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;

@DefaultProperty("itemsPane")
@SuppressWarnings({"unused", "FieldCanBeLocal"})
public class ItemsView<T extends ViewModel> extends StackPane {

    private static class Factory<X> {
        final Callback<? extends X, Node> createView;
        final Consumer<Node> destroyView;

        Factory(Callback<? extends X, Node> createView, Consumer<Node> destroyView) {
            this.createView = createView;
            this.destroyView = destroyView;
        }
    }

    private final ObjectProperty<Pane> itemsPane = new SimpleObjectProperty<>(this, "itemsPane");
    private final ObjectProperty<ObservableList<T>> items = new SimpleObjectProperty<>(this, "items");
    private final ListChangeListener<T> itemsListChangedMethod = this::itemsListChanged;
    private final Map<Class, Factory<T>> viewFactories = new HashMap<>();
    private final InvalidationListener invalidationListener;
    private final WeakInvalidationListener weakInvalidationListener;

    private boolean isUpdating = false;

    public ItemsView() {
        this.items.addListener(this::itemsChanged);
        this.itemsPane.addListener(this::itemsPaneChanged);

        this.invalidationListener =
            observable -> {
                Pane itemsPane = getItemsPane();
                if (!isUpdating && itemsPane != null) {
                    final String className = itemsPane.getClass().getSimpleName();
                    final String myName = ItemsView.class.getSimpleName();
                    throw new IllegalStateException(
                        "The " + className + " instance is managed by an " + myName + " and cannot be modified.");
                }
            };

        this.weakInvalidationListener = new WeakInvalidationListener(invalidationListener);
    }

    public ItemsView(Pane itemsPane) {
        this();
        this.itemsPane.set(itemsPane);
    }

    public Pane getItemsPane() {
        return itemsPane.get();
    }

    public void setItemsPane(Pane pane) {
        itemsPane.set(pane);
    }

    public ObjectProperty<ObservableList<T>> itemsProperty() {
        return items;
    }

    public ObservableList<T> getItems() {
        return items.get();
    }

    public void setItems(ObservableList<T> items) {
        this.items.set(items);
    }

    /**
     * Adds a view factory that produces the UI for the specified view model. If {@link ItemsView#itemsProperty()}
     * contains view models that are subclasses of known view factories, the view factory with the smallest inheritance
     * distance will be selected.
     */
    public <U extends T> void addViewFactory(Class<U> viewModelClass, Callback<U, Node> createView) {
        Expect.notNull(viewModelClass, "viewModelClass", createView, "createView");
        viewFactories.put(viewModelClass, new Factory<>(createView, null));
    }

    public <U extends T> void addViewFactory(
            Class<U> viewModelClass, Callback<U, Node> createView, Consumer<Node> destroyView) {
        Expect.notNull(
            viewModelClass, "viewModelClass",
            createView, "createView",
            destroyView, "destroyView");
        viewFactories.put(viewModelClass, new Factory<>(createView, destroyView));
    }

    private void itemsPaneChanged(ObservableValue<? extends Pane> observable, Pane oldValue, Pane newValue) {
        if (oldValue != null) {
            oldValue.getChildren().removeListener(weakInvalidationListener);
            getChildren().remove(oldValue);
        }

        if (newValue != null) {
            if (!newValue.getChildren().isEmpty()) {
                final String className = newValue.getClass().getSimpleName();
                final String myName = ItemsView.class.getSimpleName();
                throw new IllegalArgumentException(
                    "A " + className + " instance that is managed by an " + myName + " cannot have children.");
            }

            newValue.getChildren().addListener(weakInvalidationListener);
            getChildren().add(newValue);

            ObservableList<T> items = this.items.get();
            if (items != null && !items.isEmpty()) {
                isUpdating = true;

                try {
                    newValue.getChildren()
                        .addAll(items.stream().map(this::createItemView).collect(Collectors.toList()));
                } finally {
                    isUpdating = false;
                }
            }
        }
    }

    private void itemsChanged(
            ObservableValue<? extends ObservableList<T>> observable,
            ObservableList<T> oldItems,
            ObservableList<T> newItems) {
        Pane itemsPane = getItemsPane();

        if (oldItems != null) {
            oldItems.removeListener(itemsListChangedMethod);

            if (itemsPane != null) {
                isUpdating = true;
                itemsPane.getChildren().clear();
                isUpdating = false;
            }
        }

        if (newItems != null) {
            newItems.addListener(itemsListChangedMethod);

            if (!newItems.isEmpty() && itemsPane != null) {
                isUpdating = true;

                try {
                    itemsPane
                        .getChildren()
                        .addAll(newItems.stream().map(this::createItemView).collect(Collectors.toList()));
                } finally {
                    isUpdating = false;
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void itemsListChanged(ListChangeListener.Change<? extends T> change) {
        Pane itemsPane = getItemsPane();
        if (itemsPane == null) {
            return;
        }

        ObservableList<Node> children = itemsPane.getChildren();
        isUpdating = true;

        try {
            while (change.next()) {
                if (change.wasPermutated()) {
                    int from = change.getFrom();
                    int to = change.getTo();
                    final int[] indexMap = new int[to - from];
                    for (int i = from; i < to; ++i) {
                        indexMap[i - from] = change.getPermutation(i);
                    }

                    List<Node> copy = new ArrayList<>(children.subList(from, to));
                    for (int i = 0; i < to - from; ++i) {
                        int newIndex = indexMap[i];
                        children.set(newIndex, copy.get(i));
                    }
                } else {
                    if (change.wasRemoved()) {
                        Class<?> itemClass = change.getRemoved().get(0).getClass();
                        Consumer<Node> destroyView = (Consumer<Node>)getFactory(itemClass).destroyView;
                        if (destroyView != null) {
                            List<? extends T> removedItems = change.getRemoved();
                            List<Node> subList =
                                children.subList(change.getFrom(), change.getFrom() + change.getRemovedSize());
                            List<Node> removedNodes = new ArrayList<>(subList);
                            subList.clear();
                            for (int i = 0; i < removedNodes.size(); ++i) {
                                destroyItemView(removedItems.get(i), removedNodes.get(i));
                            }
                        } else {
                            children.subList(change.getFrom(), change.getFrom() + change.getRemovedSize()).clear();
                        }
                    }

                    if (change.wasAdded()) {
                        children.addAll(
                            change.getFrom(),
                            change.getAddedSubList().stream().map(this::createItemView).collect(Collectors.toList()));
                    }
                }
            }
        } finally {
            isUpdating = false;
        }
    }

    @SuppressWarnings("unchecked")
    private Node createItemView(T item) {
        return (Node)getFactory(item.getClass()).createView.call(item);
    }

    @SuppressWarnings("unchecked")
    private void destroyItemView(T item, Node node) {
        getFactory(item.getClass()).destroyView.accept(node);
    }

    private Factory getFactory(Class<?> itemClass) {
        Factory<T> factory = viewFactories.get(itemClass);
        if (factory != null) {
            return factory;
        }

        int currentDistance = Integer.MAX_VALUE;
        Class<?> currentClass = null;
        for (Class<?> cls : viewFactories.keySet()) {
            int dist = getInheritanceDistance(itemClass, cls);
            if (dist >= 0 && dist < currentDistance) {
                currentDistance = dist;
                currentClass = cls;
            }
        }

        if (currentClass != null) {
            factory = viewFactories.get(currentClass);
            if (factory != null) {
                return factory;
            }
        }

        final String itemTypeName = itemClass.getName();
        throw new IllegalStateException("No view factory available for type " + itemTypeName);
    }

    private int getInheritanceDistance(Class<?> from, Class<?> to) {
        if (from == to) {
            return 0;
        }

        int distance = 0;
        while (to.isAssignableFrom(from)) {
            from = from.getSuperclass();
            distance++;
        }

        return distance > 0 ? distance : -1;
    }

}
