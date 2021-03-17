/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.common.components;

import java.util.ArrayList;
import java.util.List;
import javafx.collections.ObservableList;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ToggleGroup;

@Deprecated
public class ItemsSelector<T> extends SectionMenuButton<RadioMenuItem, MenuItem> {

    private ItemFactory<T> factory;
    private ToggleGroup toggleGroup = new ToggleGroup();

    public void setItemFactory(ItemFactory<T> factory) {
        this.factory = factory;
        factory.selectedItemProperty()
            .addListener(
                ((observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        textProperty().unbind();
                        textProperty().bind(factory.getTextExtractor().call(newValue));
                        RadioMenuItem selectedItem =
                            getTopItems()
                                .stream()
                                .filter(
                                    mi ->
                                        mi.getUserData()
                                            .toString()
                                            .equals(factory.getUserDataExtractor().call(newValue)))
                                .findFirst()
                                .orElse(null);
                        if (selectedItem != null) {
                            selectedItem.setSelected(true);
                        }
                    } else {
                        if (!factory.availableItemsProperty().contains(oldValue)) {
                            getTopItems()
                                .removeIf(
                                    mi ->
                                        mi.getUserData()
                                            .toString()
                                            .equals(factory.getUserDataExtractor().call(oldValue)));
                            textProperty().unbind();
                            textProperty().set("");
                        }
                    }
                }));
        factory.availableItemsProperty()
            .addListener(
                ((observable, oldValue, newValue) -> {
                    getTopItems().clear();
                    getTopItems().addAll(buildFlightPlanMenuItems(newValue));
                }));
    }

    private List<RadioMenuItem> buildFlightPlanMenuItems(ObservableList<T> items) {
        List<RadioMenuItem> menuItems = new ArrayList<>();
        for (T item : items) {
            RadioMenuItem radioMenuItem = factory.buildMenuItem(item);
            radioMenuItem.setToggleGroup(toggleGroup);
            menuItems.add(radioMenuItem);
        }

        return menuItems;
    }
}
