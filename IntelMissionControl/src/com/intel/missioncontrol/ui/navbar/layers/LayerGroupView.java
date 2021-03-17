/*
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.layers;

import com.intel.missioncontrol.ui.controls.ItemsView;
import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.JavaView;
import de.saxsys.mvvmfx.ViewTuple;
import javafx.beans.binding.Bindings;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

public class LayerGroupView extends TitledPane implements JavaView<LayerGroupViewModel> {

    @InjectViewModel
    private LayerGroupViewModel viewModel;

    public void initialize() {
        ToggleGroup toggleGroup = new ToggleGroup();
        ItemsView<LayerViewModel> subItemsView = new ItemsView<>(new VBox());
        subItemsView.addViewFactory(
            SimpleLayerViewModel.class,
            vm -> {
                ViewTuple<SimpleLayerView, SimpleLayerViewModel> viewTuple =
                    FluentViewLoader.javaView(SimpleLayerView.class).viewModel(vm).load();
                viewTuple.getCodeBehind().configure(viewModel.getToggleHint(), toggleGroup);
                return viewTuple.getView();
            });

        subItemsView.itemsProperty().bind(viewModel.subLayerItemsProperty());

        tooltipProperty()
            .bind(
                Bindings.createObjectBinding(
                    (() -> {
                        if (viewModel.tooltipProperty().getValue() != null) {
                            return new Tooltip(viewModel.tooltipProperty().getValue());
                        }
                        // no tooltip
                        return null;
                    }),
                    viewModel.tooltipProperty()));

        Button settingsButton = new Button();
        settingsButton.getStyleClass().add("icon-settings-flat");
        settingsButton.getStyleClass().add("flat-icon-button");

        settingsButton.visibleProperty().bind(viewModel.openSettingsCommand().executableProperty());
        settingsButton.managedProperty().bind(viewModel.openSettingsCommand().executableProperty());
        settingsButton.setOnAction(event -> viewModel.openSettingsCommand().execute());

        Button deleteButton = new Button();
        deleteButton.getStyleClass().add("icon-trash");
        deleteButton.getStyleClass().add("flat-icon-button");
        deleteButton.getStyleClass().add("destructive");
        deleteButton.visibleProperty().bind(viewModel.deleteCommand().executableProperty());
        deleteButton.managedProperty().bind(viewModel.deleteCommand().executableProperty());
        deleteButton.setOnAction(event -> viewModel.deleteCommand().execute());

        Button warning = new Button();
        warning.getStyleClass().add("icon-warning-regular");
        warning.getStyleClass().add("flat-icon-button");
        warning.visibleProperty().bind(viewModel.hasWarningProperty());
        warning.managedProperty().bind(viewModel.hasWarningProperty());
        warning.setOnAction(event -> viewModel.resolveWarningCommand().execute());

        warning.tooltipProperty()
            .bind(
                Bindings.createObjectBinding(
                    (() -> {
                        if (viewModel.warningProperty().getValue() != null) {
                            return new Tooltip(viewModel.warningProperty().getValue());
                        }
                        // no tooltip
                        return null;
                    }),
                    viewModel.warningProperty()));

        visibleProperty().bind(viewModel.visibleProperty());
        managedProperty().bind(viewModel.visibleProperty());

        Label caption = new Label();
        caption.setTextAlignment(TextAlignment.CENTER);
        caption.setMaxHeight(Double.POSITIVE_INFINITY);
        caption.setPrefWidth(100000);
        caption.setTextOverrun(OverrunStyle.ELLIPSIS);
        caption.textProperty().bind(viewModel.nameProperty());

        HBox.setHgrow(caption, Priority.ALWAYS);
        HBox.setHgrow(settingsButton, Priority.NEVER);
        HBox.setHgrow(deleteButton, Priority.NEVER);
        HBox.setHgrow(warning, Priority.NEVER);

        HBox title = new HBox(caption, settingsButton, deleteButton, warning);
        title.setMinWidth(0);
        title.setMaxWidth(Double.POSITIVE_INFINITY);

        setGraphic(title);
        setContent(subItemsView);
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        setExpanded(viewModel.isInitiallyExpanded());

        // minWidth needs to be set, otherwise the width will not be constrained.
        // Maybe related to this bug: https://bugs.openjdk.java.net/browse/JDK-8163075
        setMinWidth(0);
    }
}
