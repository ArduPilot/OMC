/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.menu;

import com.intel.missioncontrol.ui.commands.ICommand;
import java.net.URL;
import java.util.List;
import java.util.function.Consumer;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.scene.Node;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

public class MenuHelper {

    public static void createMenuItems(
            List<MenuItem> menus, ToggleGroup toggleGroup, MenuModel menuModel, Consumer<String> statusTextConsumer) {
        boolean lastGroup = false;
        boolean firstItem = true;

        for (MenuModel model : menuModel.getChildren()) {
            if (!model.isVisible()) {
                continue;
            }

            if (lastGroup && !model.isGroup()) {
                menus.add(new SeparatorMenuItem());
            }

            if (model.isGroup()) {
                if (model.getChildren().stream().noneMatch(MenuModel::isVisible)) {
                    continue;
                }

                if (!firstItem) {
                    menus.add(new SeparatorMenuItem());
                }

                createMenuItems(menus, null, model, statusTextConsumer);
                lastGroup = true;
            } else if (model.isCheckGroup()) {
                if (model.getChildren().stream().noneMatch(MenuModel::isVisible)) {
                    continue;
                }

                ToggleGroup newToggleGroup = new ToggleGroup();
                newToggleGroup.setUserData(model);
                newToggleGroup
                    .selectedToggleProperty()
                    .addListener(
                        (observable, oldValue, newValue) -> {
                            if (newValue != null) {
                                MenuModel checkGroupModel = (MenuModel)newToggleGroup.getUserData();
                                checkGroupModel.setCheckedItem(newValue.getUserData());
                            }
                        });

                model.checkedItemProperty()
                    .addListener(
                        (observable, oldValue, newValue) -> {
                            for (Toggle toggle : newToggleGroup.getToggles()) {
                                if (toggle.getUserData() == newValue) {
                                    toggle.setSelected(true);
                                } else {
                                    toggle.setSelected(false);
                                }
                            }
                        });

                createMenuItems(menus, newToggleGroup, model, statusTextConsumer);

                for (Toggle toggle : newToggleGroup.getToggles()) {
                    if (toggle.getUserData() == model.getCheckedItem()) {
                        toggle.setSelected(true);
                    }
                }
            } else if (model.isLeaf()) {
                final ICommand command = model.getCommand();

                MenuItem menuItem;
                if (model.isCheckable()) {
                    RadioMenuItem checkMenuItem = new RadioMenuItem();
                    checkMenuItem.setUserData(model.getUserData());

                    if (toggleGroup != null) {
                        checkMenuItem.setToggleGroup(toggleGroup);
                    } else {
                        checkMenuItem.selectedProperty().bindBidirectional(model.checkedProperty());
                    }

                    menuItem = checkMenuItem;
                } else {
                    menuItem = new MenuItem();
                    menuItem.disableProperty().bind(command.notExecutableProperty());
                }

                menuItem.parentPopupProperty()
                    .addListener(
                        (observable1, oldValue1, newValue1) -> {
                            if (newValue1 != null) {
                                newValue1
                                    .skinProperty()
                                    .addListener(
                                        (observable2, oldValue2, newValue2) -> {
                                            Node node = menuItem.getStyleableNode();
                                            if (node == null) {
                                                return;
                                            }

                                            node.addEventHandler(
                                                MouseEvent.MOUSE_ENTERED,
                                                handler -> {
                                                    String statusText = model.getStatusText();
                                                    if (statusText != null) {
                                                        statusTextConsumer.accept(statusText);
                                                    }
                                                });

                                            node.addEventHandler(
                                                MouseEvent.MOUSE_EXITED, handler -> statusTextConsumer.accept(null));
                                        });
                            }
                        });

                menuItem.mnemonicParsingProperty().bind(model.mnemonicParsingProperty());
                menuItem.textProperty().bind(model.textProperty());
                menuItem.acceleratorProperty().bind(model.acceleratorProperty());
                menuItem.setOnAction(
                    event -> {
                        if (command.isExecutable()) {
                            command.execute();
                        }
                    });

                menuItem.graphicProperty()
                    .bind(
                        Bindings.createObjectBinding(
                            () -> {
                                if (model.imageSourceProperty().get() != null) {
                                    ImageView imageView = new ImageView(model.imageSourceProperty().get().toExternalForm());
                                    imageView.setFitWidth(16);
                                    imageView.setFitHeight(16);
                                    return imageView;
                                }

                                return null;
                            },
                            model.imageSourceProperty()));

                menus.add(menuItem);
                lastGroup = false;
            } else {
                Menu menu = new Menu();
                menu.mnemonicParsingProperty().bind(menuModel.mnemonicParsingProperty());
                menu.textProperty().bind(model.textProperty());
                URL imageSource = model.imageSourceProperty().get();
                if (imageSource != null) {
                    ImageView imageView = new ImageView(imageSource.toExternalForm());
                    menu.setGraphic(imageView);
                }

                menus.add(menu);
                createMenuItems(menu.getItems(), null, model, statusTextConsumer);

                ReadOnlyBooleanProperty[] dependencies =
                    menu.getItems().stream().map(MenuItem::disableProperty).toArray(ReadOnlyBooleanProperty[]::new);

                menu.disableProperty()
                    .bind(
                        Bindings.createBooleanBinding(
                            () -> {
                                for (ReadOnlyBooleanProperty disableProperty : dependencies) {
                                    if (!disableProperty.get()) {
                                        return false;
                                    }
                                }

                                return true;
                            },
                            dependencies));

                lastGroup = false;
            }

            firstItem = false;
        }
    }

}
