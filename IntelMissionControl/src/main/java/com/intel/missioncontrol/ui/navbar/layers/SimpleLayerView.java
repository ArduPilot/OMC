package com.intel.missioncontrol.ui.navbar.layers;

import com.intel.missioncontrol.map.LayerGroup.ToggleHint;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.JavaView;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public class SimpleLayerView extends HBox implements JavaView<SimpleLayerViewModel> {

    @InjectViewModel
    private SimpleLayerViewModel viewModel;

    public void initialize() {
        getStyleClass().add("layer-row");
        setAlignment(Pos.CENTER_LEFT);
        managedProperty().bind(viewModel.visibleProperty());
        visibleProperty().bind(viewModel.visibleProperty());
    }

    void configure(ToggleHint toggleHint, ToggleGroup toggleGroup) {
        ButtonBase mainButton;
        if (toggleHint == ToggleHint.ANY) {
            CheckBox checkBox = new CheckBox();
            checkBox.selectedProperty().bindBidirectional(viewModel.selectedProperty());
            mainButton = checkBox;
        } else if (toggleHint == ToggleHint.ONE_OR_NONE) {
            RadioButton radioButton =
                new RadioButton() {
                    @Override
                    public void fire() {
                        if (!isDisabled()) {
                            setSelected(!isSelected());
                            fireEvent(new ActionEvent());
                        }
                    }
                };

            radioButton.setToggleGroup(toggleGroup);
            radioButton.selectedProperty().bindBidirectional(viewModel.selectedProperty());
            mainButton = radioButton;
        } else {
            RadioButton radioButton = new RadioButton();
            radioButton.setToggleGroup(toggleGroup);
            radioButton.selectedProperty().bindBidirectional(viewModel.selectedProperty());
            mainButton = radioButton;
        }

        HBox.setHgrow(mainButton, Priority.ALWAYS);
        mainButton.setMaxWidth(Double.POSITIVE_INFINITY);
        mainButton.setMaxHeight(Double.POSITIVE_INFINITY);
        mainButton.setMnemonicParsing(false);
        mainButton.textProperty().bind(viewModel.nameProperty());
        mainButton
            .tooltipProperty()
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

        Button showOnMapButton = new Button();
        showOnMapButton.getStyleClass().add("icon-show-on-map");
        showOnMapButton.getStyleClass().add("flat-icon-button");
        showOnMapButton
            .visibleProperty()
            .bind(this.hoverProperty().and(viewModel.getShowOnMapCommand().executableProperty()));
        showOnMapButton.managedProperty().bind(showOnMapButton.visibleProperty());
        showOnMapButton.setOnAction(event -> viewModel.getShowOnMapCommand().execute());
        HBox.setHgrow(showOnMapButton, Priority.NEVER);

        Button deleteButton = new Button();
        deleteButton.getStyleClass().add("icon-trash");
        deleteButton.getStyleClass().add("flat-icon-button");
        deleteButton.getStyleClass().add("destructive");
        deleteButton.visibleProperty().bind(this.hoverProperty().and(viewModel.getDeleteCommand().executableProperty()));
        deleteButton.managedProperty().bind(deleteButton.visibleProperty());
        deleteButton.setOnAction(event -> viewModel.getDeleteCommand().execute());
        HBox.setHgrow(deleteButton, Priority.NEVER);

        getChildren().addAll(mainButton, showOnMapButton, deleteButton);
    }

}
