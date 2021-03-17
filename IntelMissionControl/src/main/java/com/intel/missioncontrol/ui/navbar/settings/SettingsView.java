/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.settings;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.intel.missioncontrol.helper.Expect;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.settings.OperationLevel;
import com.intel.missioncontrol.ui.navbar.NavBarMenuView;
import com.intel.missioncontrol.ui.navigation.SettingsPage;
import de.saxsys.mvvmfx.InjectViewModel;
import eu.mavinci.core.licence.ILicenceManager;
import java.util.Map;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.checkerframework.checker.nullness.qual.Nullable;

public class SettingsView extends NavBarMenuView<SettingsViewModel, SettingsPage> {

    private final GeneralSettings settings;
    private final ILicenceManager licenceManager;

    @InjectViewModel
    private SettingsViewModel viewModel;

    @FXML
    private Pane layoutRoot;

    @FXML
    private Parent transformRoot;

    @FXML
    private Pane contentPane;

    @FXML
    private ToggleGroup menuToggleGroup;

    @FXML
    private RadioButton radioButtonConnection;

    @FXML
    private Region generalSettingsView;

    @FXML
    private Region displaySettingsView;

    @FXML
    private Region filesAndFoldersSettingsView;

    @FXML
    private Region airspacesProvidersSettingsView;

    @FXML
    private Region internetConnectivitySettingsView;

    @FXML
    private Region connectionSettingsView;

    @FXML
    private Region insightSettingsView;

    @FXML
    private VBox menuPane;

    @Inject
    public SettingsView(ISettingsManager settingsManager, ILicenceManager licenceManager) {
        this.settings = settingsManager.getSection(GeneralSettings.class);
        this.licenceManager = licenceManager;
    }

    @Override
    protected void initializeView() {
        super.initializeView();

        Expect.notNull(
            generalSettingsView, "generalSettingsView",
            displaySettingsView, "displaySettingsView",
            filesAndFoldersSettingsView, "filesAndFoldersSettingsView",
            airspacesProvidersSettingsView, "airspacesProvidersSettingsView",
            connectionSettingsView, "connectionSettingsView",
            insightSettingsView, "insightSettingsView");

        final Map<SettingsPage, Region> map =
            ImmutableMap.<SettingsPage, Region>builder()
                .put(SettingsPage.GENERAL, generalSettingsView)
                .put(SettingsPage.DISPLAY, displaySettingsView)
                .put(SettingsPage.FILES_FOLDERS, filesAndFoldersSettingsView)
                .put(SettingsPage.AIRSPACES_PROVIDERS, airspacesProvidersSettingsView)
                .put(SettingsPage.INTERNET_CONNECTIVITY, internetConnectivitySettingsView)
                .put(SettingsPage.CONNECTION, connectionSettingsView)
                .put(SettingsPage.INSIGHT, insightSettingsView)
                .build();

        selectRadioButton(SettingsPage.GENERAL);
        currentTabChanged(map, SettingsPage.GENERAL);

        licenceManager
            .isGrayHawkEditionProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    refreshConnection();
                });
        settings.operationLevelProperty().addListener(
                (observable, oldValue, newValue) -> {
                    refreshConnection();
                });
        radioButtonConnection
            .visibleProperty()
            .bind(
                settings.operationLevelProperty()
                    .isEqualTo(OperationLevel.DEBUG)
                    .or(licenceManager.isGrayHawkEditionProperty()));
        radioButtonConnection
            .disableProperty()
            .bind(
                settings.operationLevelProperty()
                    .isEqualTo(OperationLevel.DEBUG)
                    .or(licenceManager.isGrayHawkEditionProperty())
                    .not());

        settings.operationLevelProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    if (viewModel.currentPageProperty().equals(SettingsPage.CONNECTION)) {
                        selectRadioButton(SettingsPage.GENERAL);
                        viewModel.setCurrentPage(SettingsPage.GENERAL);
                    }
                });

        // When the current page changes programmatically, we need to manually toggle the corresponding radio button.
        //
        viewModel
            .currentPageProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        selectRadioButton(newValue);
                        currentTabChanged(map, newValue);
                    }
                });

        generalSettingsView
            .prefWidthProperty()
            .addListener((observable, oldValue, newValue) -> getContentPane().setPrefWidth(newValue.doubleValue()));

        // When the user clicks on a radio button and selects it, we need to update the current page property.
        //
        getMenuToggleGroup()
            .selectedToggleProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    if (viewModel != null && newValue != null && newValue.getUserData() != null) {
                        viewModel.setCurrentPage(SettingsPage.valueOf((String)newValue.getUserData()));
                    }
                });
    }

    private void refreshConnection() {
        if (viewModel.currentPageProperty().equals(SettingsPage.CONNECTION)) {
            selectRadioButton(SettingsPage.GENERAL);
            viewModel.setCurrentPage(SettingsPage.GENERAL);
        }
        if(settings.operationLevelProperty()
                .isEqualTo(OperationLevel.DEBUG)
                .or(licenceManager.isGrayHawkEditionProperty()).get()) {
            if (viewModel.currentPageProperty().equals(SettingsPage.CONNECTION)) {
                connectionSettingsView.setVisible(true);
                connectionSettingsView.setManaged(true);
            }
        } else {
            connectionSettingsView.setVisible(false);
            connectionSettingsView.setManaged(false);
        }
    }

    @Override
    protected ToggleGroup getMenuToggleGroup() {
        return menuToggleGroup;
    }

    @Override
    protected @Nullable Pane getContentPane() {
        return contentPane;
    }

    @Override
    protected Parent getRootNode() {
        return layoutRoot;
    }

    @Override
    protected Parent getTransformRoot() {
        return transformRoot;
    }

    @Override
    protected SettingsViewModel getViewModel() {
        return viewModel;
    }

    @FXML
    public void onBackgroundClicked(MouseEvent event) {
        if (event.getTarget() == getTransformRoot() && event.isStillSincePress()) {
            viewModel.getCloseCommand().execute();
        }
    }

    @FXML
    public void onCloseClicked() {
        viewModel.getCloseCommand().execute();
    }

}
