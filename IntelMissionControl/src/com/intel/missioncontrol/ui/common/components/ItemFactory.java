/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.common.components;

import javafx.beans.property.ListProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.RadioMenuItem;
import javafx.util.Callback;

public class ItemFactory<T> {

    private final ReadOnlyListProperty<T> availableItems;
    private final ObservableValue<T> selectedItem;
    private Callback<T, Property<String>> textExtractor;
    private final Callback<T, Object> userDataExtractor;

    public ItemFactory(
            ReadOnlyListProperty<T> availableItems,
            ObservableValue<T> selectedItem,
            Callback<T, Property<String>> textExtractor,
            Callback<T, Object> userDataExtractor) {
        this.availableItems = availableItems;
        this.selectedItem = selectedItem;
        this.textExtractor = textExtractor;
        this.userDataExtractor = userDataExtractor;
    }

    public ObservableList<T> getAvailableItems() {
        return availableItems.get();
    }

    public ReadOnlyListProperty<T> availableItemsProperty() {
        return availableItems;
    }

    public T getSelectedItem() {
        return selectedItem.getValue();
    }

    public ObservableValue<T> selectedItemProperty() {
        return selectedItem;
    }

    public Callback<T, Property<String>> getTextExtractor() {
        return textExtractor;
    }

    public Callback<T, Object> getUserDataExtractor() {
        return userDataExtractor;
    }

    protected RadioMenuItem buildMenuItem(T item) {
        RadioMenuItem flightPlanItem = new RadioMenuItem();
        flightPlanItem.setUserData(userDataExtractor.call(item));
        flightPlanItem.setMnemonicParsing(false);
        flightPlanItem.textProperty().bind(textExtractor.call(item));
        flightPlanItem.setSelected(item.equals(selectedItem.getValue()));
        return flightPlanItem;
    }
}
