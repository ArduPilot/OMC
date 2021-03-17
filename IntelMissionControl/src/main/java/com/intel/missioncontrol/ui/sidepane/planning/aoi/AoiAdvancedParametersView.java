/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.planning.aoi;

import com.google.inject.Inject;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.ui.dialogs.DialogView;
import de.saxsys.mvvmfx.InjectViewModel;
import java.util.function.BiConsumer;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Pane;

public class AoiAdvancedParametersView extends DialogView<AoiAdvancedParametersViewModel> {

    @FXML
    private Pane layoutRoot;

    @FXML
    private TabPane parametersTabPane;

    @FXML
    private AoiGeneralTabView aoiGeneralTabViewController;

    @FXML
    private Tab generalTab;

    @FXML
    private Tab locationTab;

    @FXML
    private Tab dimensionsTab;

    @FXML
    private Tab flightlinesTab;

    @FXML
    private Tab transformationTab;

    @FXML
    private MenuItem restoreDefaultsItem;

    @FXML
    private Button closeButton;

    @InjectViewModel
    private AoiAdvancedParametersViewModel viewModel;

    @Inject
    private ILanguageHelper languageHelper;

    private final StringProperty title = new SimpleStringProperty();

    @Override
    public void initializeView() {
        super.initializeView();
        aoiGeneralTabViewController.setAdvancedParametersView(this);
        restoreDefaultsItem
            .textProperty()
            .set(
                String.format(
                    restoreDefaultsItem.getText(),
                    languageHelper.toFriendlyName(viewModel.getAreaOfInterest().getType())));

        closeButton.setOnAction(event -> viewModel.getCloseCommand().execute());
        closeButton.disableProperty().bind(viewModel.getCloseCommand().notExecutableProperty());
        title.bind(
            Bindings.createStringBinding(
                () ->
                    languageHelper.getString(
                        "com.intel.missioncontrol.ui.planning.aoi.AoiAdvancedParametersView.title",
                        viewModel.getAreaOfInterest().nameProperty().getValue()),
                viewModel.getAreaOfInterest().nameProperty()));

        viewModel
            .needsFlightLinesTabProperty()
            .addListener((observable, oldValue, newValue) -> fixFlightlinesTabVisibility());
        fixFlightlinesTabVisibility();

        viewModel
            .needsDimensionsTabProperty()
            .addListener((observable, oldValue, newValue) -> fixDimensionsTabVisibility());
        fixDimensionsTabVisibility();

        viewModel
            .needsTransformationTabProperty()
            .addListener((observable, oldValue, newValue) -> fixTrafoTabVisibility());
        fixTrafoTabVisibility();
    }

    private void fixTrafoTabVisibility() {
        if (viewModel.needsTransformationTabProperty().get()
                != parametersTabPane.getTabs().contains(transformationTab)) {
            if (viewModel.needsTransformationTabProperty().get()) {
                parametersTabPane.getTabs().add(1, transformationTab);
            } else {
                parametersTabPane.getTabs().remove(transformationTab);
            }
        }
    }
    private void fixDimensionsTabVisibility() {
        if (viewModel.needsDimensionsTabProperty().get()
                != parametersTabPane.getTabs().contains(dimensionsTab)) {
            if (viewModel.needsDimensionsTabProperty().get()) {
                parametersTabPane.getTabs().add(1, dimensionsTab);
            } else {
                parametersTabPane.getTabs().remove(dimensionsTab);
            }
        }
    }

    private void fixFlightlinesTabVisibility() {
        if (viewModel.needsFlightLinesTabProperty().get() != parametersTabPane.getTabs().contains(flightlinesTab)) {
            if (viewModel.needsFlightLinesTabProperty().get()) {
                parametersTabPane.getTabs().add(flightlinesTab);
            } else {
                parametersTabPane.getTabs().remove(flightlinesTab);
            }
        }
    }

    @Override
    public ReadOnlyStringProperty titleProperty() {
        return title;
    }

    @Override
    protected AoiAdvancedParametersViewModel getViewModel() {
        return viewModel;
    }

    @Override
    protected Parent getRootNode() {
        return layoutRoot;
    }

    public void openTransformationTab() {
        parametersTabPane.getSelectionModel().select(transformationTab);
    }

    public void setAoiOrderChangedConsumer(BiConsumer<Integer, Integer> aoiOrderChangedConsumer) {
        aoiGeneralTabViewController.getViewModel().setAoiOrderChangedConsumer(aoiOrderChangedConsumer);
    }

    @FXML
    public void saveAsDefaults(ActionEvent event) {
        viewModel.saveAsDefaults();
    }

    @FXML
    public void restoreDefaults(ActionEvent event) {
        viewModel.restoreDefaults();
    }

}
