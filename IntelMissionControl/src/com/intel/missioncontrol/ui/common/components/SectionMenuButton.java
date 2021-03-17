/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.common.components;

import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.stage.Window;

/**
 * MenuButton implementation which is able to splits its context menu into two sections: top and bottom. This allows to
 * update them separately. Use case is when you have dynamic section and static section which should be created only
 * once.
 */
@Deprecated
public class SectionMenuButton<T extends MenuItem, B extends MenuItem> extends MenuButton {

    private final SimpleListProperty<T> topItems = new SimpleListProperty<>(FXCollections.<T>observableArrayList());
    private final SimpleListProperty<B> bottomItems = new SimpleListProperty<>(FXCollections.<B>observableArrayList());

    public SectionMenuButton() {
        topItems.addListener((ListChangeListener<MenuItem>)c -> redrawMenu());
        bottomItems.addListener((ListChangeListener<MenuItem>)c -> redrawMenu());
    }

    private void redrawMenu() {
        getItems().clear();
        getItems().addAll(topItems);
        getItems().addAll(bottomItems);
    }

    @Override
    public void show() {
        redrawMenu();
        getItems().stream().findFirst().map(MenuItem::getParentPopup).ifPresent(Window::sizeToScene);
        super.show();
    }

    public final ObservableList<T> getTopItems() {
        return topItems;
    }

    public final ObservableList<B> getBottomItems() {
        return bottomItems;
    }

}
