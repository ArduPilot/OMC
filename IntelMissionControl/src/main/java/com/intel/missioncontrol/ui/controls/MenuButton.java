/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.controls;

import com.intel.missioncontrol.ui.menu.MenuHelper;
import com.intel.missioncontrol.ui.menu.MenuModel;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.scene.control.MenuItem;

/** A menu button that supports a {@link MenuModel} hierarchy to define its menu structure. */
public class MenuButton extends javafx.scene.control.MenuButton {

    private final ObjectProperty<MenuModel> model = new SimpleObjectProperty<>();
    private final InvalidationListener visibleChangedListener = observable -> recreateMenu(model.get());
    private final ListChangeListener<MenuModel> listChangedListener = change -> recreateMenu(model.get());

    public MenuButton() {
        model.addListener(this::modelChanged);
    }

    public ObjectProperty<MenuModel> modelProperty() {
        return model;
    }

    public MenuModel getModel() {
        return model.get();
    }

    public void setModel(MenuModel model) {
        this.model.set(model);
    }

    private void modelChanged(ObservableValue<? extends MenuModel> observable, MenuModel oldValue, MenuModel newValue) {
        if (oldValue != null) {
            removeStructureChangedListeners(oldValue);
        }

        if (newValue != null) {
            addStructureChangedListeners(newValue);
        }

        recreateMenu(newValue);
    }

    private void addStructureChangedListeners(MenuModel menuModel) {
        menuModel.childrenProperty().addListener(listChangedListener);
        menuModel.visibleProperty().addListener(visibleChangedListener);

        for (MenuModel child : menuModel.getChildren()) {
            addStructureChangedListeners(child);
        }
    }

    private void removeStructureChangedListeners(MenuModel menuModel) {
        menuModel.childrenProperty().addListener(listChangedListener);
        menuModel.visibleProperty().removeListener(visibleChangedListener);

        for (MenuModel child : menuModel.getChildren()) {
            removeStructureChangedListeners(child);
        }
    }

    private void recreateMenu(MenuModel menuModel) {
        if (menuModel == null) {
            getItems().clear();
            mnemonicParsingProperty().unbind();
            textProperty().unbind();
        } else {
            mnemonicParsingProperty().bind(menuModel.mnemonicParsingProperty());
            textProperty().bind(menuModel.textProperty());
            List<MenuItem> menu = new ArrayList<>();
            MenuHelper.createMenuItems(menu, null, menuModel, status -> {});
            getItems().setAll(menu);
        }
    }

}
