/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.update;

import com.google.inject.Inject;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.ui.dialogs.DialogView;
import de.saxsys.mvvmfx.InjectViewModel;
import eu.mavinci.core.update.EnumUpdateTargets;
import eu.mavinci.desktop.main.core.Application;
import eu.mavinci.desktop.main.core.IAppListener;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.util.stream.Stream;

/** Created by bulatnikov on 7/16/17. */
public class UpdateView extends DialogView<UpdateViewModel> {

    @InjectViewModel
    private UpdateViewModel viewModel;

    @FXML
    private Pane layoutRoot;

    @FXML
    private VBox updatesBox;

    @FXML
    private Button runUpdateButton;

    @FXML
    private Button laterButton;

    @FXML
    private ButtonBar updateButtonBar;

    private Boolean isFirstUpdateItem = true;
    private final ILanguageHelper languageHelper;

    @Inject
    public UpdateView(ILanguageHelper languageHelper) {
        this.languageHelper = languageHelper;
    }

    @Override
    protected void initializeView() {
        super.initializeView();
        initUpdatesList();
    }

    @Override
    public ReadOnlyStringProperty titleProperty() {
        return new ReadOnlyStringWrapper(
            languageHelper.getString("com.intel.missioncontrol.ui.update.UpdateView.title"));
    }

    @Override
    protected Parent getRootNode() {
        return layoutRoot;
    }

    @Override
    protected UpdateViewModel getViewModel() {
        return viewModel;
    }

    private IAppListener closeAppListener = null;

    private void initUpdatesList() {
        updatesBox.getChildren().clear();
        updateButtonBar.getButtons().clear();
        updateButtonBar.getButtons().addAll(runUpdateButton, laterButton);
        viewModel
            .getAvailableUpdatesMap()
            .forEach(
                (key, availableUpdate) -> {
                    String target = String.valueOf(key);
                    String currentVersion;
                    if (key == EnumUpdateTargets.GUI) {
                        currentVersion = viewModel.getCurrentFullVersion();
                    } else {
                        currentVersion = viewModel.getCurrentLicenceVersion();
                    }

                    UpdateItemView updateItemView =
                        new UpdateItemView(target, availableUpdate.getVersion(), currentVersion, isFirstUpdateItem);
                    isFirstUpdateItem = false;
                    updateItemView
                        .getCheckbox()
                        .selectedProperty()
                        .addListener(
                            (observable, oldValue, newValue) -> {
                                if (newValue && runUpdateButton.isDisabled()) {
                                    runUpdateButton.setDisable(false);
                                } else if (!newValue && !runUpdateButton.isDisabled()) {
                                    boolean disable =
                                        getUpdateNodes().noneMatch(node -> node.getCheckbox().isSelected());
                                    runUpdateButton.setDisable(disable);
                                }
                            });

                    if (updateItemView.getCheckbox().isSelected()) {
                        runUpdateButton.setDisable(false);
                    }

                    updateItemView
                        .getSkipButton()
                        .setOnMouseClicked(
                            (event -> {
                                if (updateItemView.getPrefWidth() < 0) {
                                    getUpdateNodes().forEach(node -> node.setPrefWidth(updatesBox.getWidth()));
                                }

                                updatesBox.getChildren().remove(updateItemView);
                                viewModel.onSkipVersion(updateItemView.getId());
                                if (updatesBox.getChildren().isEmpty()) {
                                    runUpdateButton.setDisable(true);
                                }
                            }));
                    updatesBox.getChildren().add(updateItemView);
                });
        getRootNode().autosize();
    }

    @FXML
    public void runDownload() {
        if (closeAppListener != null) {
            Application.removeApplicationListener(closeAppListener);
        }

        viewModel.getCloseCommand().execute();
        doDownload();
    }

    private void doDownload() {
        getUpdateNodes()
            .filter(itemView -> itemView.getCheckbox().isSelected())
            .forEach(
                itemView -> {
                    EnumUpdateTargets target = EnumUpdateTargets.valueOf(itemView.getId());
                    viewModel.runUpdate(target);
                });
    }

    private Stream<UpdateItemView> getUpdateNodes() {
        return updatesBox.getChildren().stream().map(UpdateItemView.class::cast);
    }

    @FXML
    private void closeDialog() {
        viewModel.getCloseCommand().execute();
    }
}
