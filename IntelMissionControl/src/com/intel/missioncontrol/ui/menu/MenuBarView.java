/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.menu;

import com.intel.missioncontrol.ui.RootView;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.ViewModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.beans.InvalidationListener;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.Pane;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

public class MenuBarView extends RootView<MenuBarViewModel> {

    @InjectViewModel
    private MenuBarViewModel viewModel;

    @FXML @MonotonicNonNull
    private Pane layoutRoot;

    @FXML @MonotonicNonNull
    private MenuBar menuBar;

    private final InvalidationListener visibleChangedListener = observable -> recreateMenu();
    private final ListChangeListener<MenuModel> listChangedListener = change -> recreateMenu();
    private ObservableSet<PseudoClass> pseudoClassStates; // strong ref

    @Override
    protected void initializeView() {
        super.initializeView();

        pseudoClassStates = layoutRoot.getPseudoClassStates();
        pseudoClassStates.addListener(
            (SetChangeListener<? super PseudoClass>)
                change -> {
                    if (change.wasAdded()) {
                        if (change.getElementAdded().getPseudoClassName().equals("inverted")) {
                            menuBar.pseudoClassStateChanged(change.getElementAdded(), true);
                        }
                    } else if (change.wasRemoved()) {
                        if (change.getElementRemoved().getPseudoClassName().equals("inverted")) {
                            menuBar.pseudoClassStateChanged(change.getElementRemoved(), false);
                        }
                    }
                });

        Optional<PseudoClass> invertedClass =
            pseudoClassStates.stream().filter(item -> item.getPseudoClassName().equals("inverted")).findFirst();
        if (invertedClass.isPresent()) {
            menuBar.pseudoClassStateChanged(invertedClass.get(), true);
        }

        viewModel
            .menuModelProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    if (oldValue != null) {
                        removeStructureChangedListeners(oldValue);
                    }

                    if (newValue != null) {
                        addStructureChangedListeners(newValue);
                    }
                });

        addStructureChangedListeners(viewModel.menuModelProperty().get());
        recreateMenu();
    }

    @Override
    protected Parent getRootNode() {
        return layoutRoot;
    }

    @Override
    protected ViewModel getViewModel() {
        return viewModel;
    }

    private void statusTextChanged(String statusText) {
        viewModel.statusTextProperty().set(statusText);
    }

    private void recreateMenu() {
        List<Menu> menus = new ArrayList<>();

        for (MenuModel subMenuModel : viewModel.menuModelProperty().get().getChildren()) {
            if (subMenuModel.isVisible()) {
                Menu menu = new Menu();
                menu.mnemonicParsingProperty().bind(subMenuModel.mnemonicParsingProperty());
                menu.textProperty().bind(subMenuModel.textProperty());
                menus.add(menu);
                MenuHelper.createMenuItems(menu.getItems(), null, subMenuModel, this::statusTextChanged);
            }
        }

        menuBar.getMenus().setAll(menus);
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

}
