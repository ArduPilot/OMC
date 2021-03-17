/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight.fly.checklist;

import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ChecklistViewModel implements ViewModel {

    private final StringProperty caption;
    private final ListProperty<ChecklistItemViewModel> items =
        new SimpleListProperty<>(FXCollections.observableArrayList());
    private final IntegerProperty checkedItemCount = new SimpleIntegerProperty();
    private final IntegerProperty totalItemCount = new SimpleIntegerProperty();
    private final InvalidationListener invalidationListener;

    public ChecklistViewModel(ChecklistItem checklist) {
        caption = new ReadOnlyStringWrapper(checklist.getTitle());

        for (String item : checklist.getItems()) {
            items.add(new ChecklistItemViewModel(item));
        }

        totalItemCount.set(items.size());

        invalidationListener =
            observable -> {
                int checkedCount = (int)items.stream().filter(ChecklistItemViewModel::isChecked).count();
                checkedItemCount.set(checkedCount);
            };

        items.forEach(item -> item.checkedProperty().addListener(new WeakInvalidationListener(invalidationListener)));
    }

    public String getCaption() {
        return caption.get();
    }

    public StringProperty captionProperty() {
        return caption;
    }

    public ObservableList<ChecklistItemViewModel> getItems() {
        return items.get();
    }

    public ListProperty<ChecklistItemViewModel> itemsProperty() {
        return items;
    }

    public int getCheckedItemCount() {
        return checkedItemCount.get();
    }

    public ReadOnlyIntegerProperty checkedItemCountProperty() {
        return checkedItemCount;
    }

    public int getTotalItemCount() {
        return totalItemCount.get();
    }

    public ReadOnlyIntegerProperty totalItemCountProperty() {
        return totalItemCount;
    }
}
