/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.settings;

import com.google.common.collect.ImmutableMap;
import com.intel.missioncontrol.helper.Expect;
import com.intel.missioncontrol.ui.navbar.NavBarMenuView;
import com.intel.missioncontrol.ui.navigation.SettingsPage;
import de.saxsys.mvvmfx.InjectViewModel;
import java.util.Map;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.checkerframework.checker.nullness.qual.EnsuresNonNull;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class SettingsView extends NavBarMenuView<SettingsViewModel, SettingsPage> {

    @InjectViewModel @MonotonicNonNull
    private SettingsViewModel viewModel;

    @FXML @MonotonicNonNull
    private Pane layoutRoot;

    @FXML @MonotonicNonNull
    private Parent transformRoot;

    @FXML @MonotonicNonNull
    private Pane contentPane;

    @FXML @MonotonicNonNull
    private ToggleGroup menuToggleGroup;

    @FXML @MonotonicNonNull
    private Region generalSettingsView;

    @FXML @MonotonicNonNull
    private Region displaySettingsView;

    @FXML @MonotonicNonNull
    private Region filesAndFoldersSettingsView;

    @FXML @MonotonicNonNull
    private Region airspacesProvidersSettingsView;

    @FXML @MonotonicNonNull
    private Region airTrafficMonitorSettingsView;

    @FXML @MonotonicNonNull
    private Region rtkBasePositionsSettingsView;

    @FXML @MonotonicNonNull
    private Region internetConnectivitySettingsView;

    @FXML
    private VBox menuPane;

    @Override
    @EnsuresNonNull({"viewModel"})
    protected void initializeView() {
        super.initializeView();

        Expect.notNull(
            generalSettingsView, "generalSettingsView",
            displaySettingsView, "displaySettingsView",
            filesAndFoldersSettingsView, "filesAndFoldersSettingsView",
            airspacesProvidersSettingsView, "airspacesProvidersSettingsView",
            airTrafficMonitorSettingsView, "airTrafficMonitorSettingsView",
            rtkBasePositionsSettingsView, "rtkBasePositionsSettingsView");

        final Map<SettingsPage, Region> map =
            ImmutableMap.<SettingsPage, Region>builder()
                .put(SettingsPage.GENERAL, generalSettingsView)
                .put(SettingsPage.DISPLAY, displaySettingsView)
                .put(SettingsPage.FILES_FOLDERS, filesAndFoldersSettingsView)
                .put(SettingsPage.AIRSPACES_PROVIDERS, airspacesProvidersSettingsView)
                .put(SettingsPage.ATC_MONITOR, airTrafficMonitorSettingsView)
                .put(SettingsPage.RTK_BASE_POSITION, rtkBasePositionsSettingsView)
                .put(SettingsPage.INTERNET_CONNECTIVITY, internetConnectivitySettingsView)
                .build();

        if (!viewModel.rtkVisibleProperty().get()) {
            String rtkPageName = SettingsPage.RTK_BASE_POSITION.toString();
            for (Toggle t : getMenuToggleGroup().getToggles()) {
                if (t.getUserData().equals(rtkPageName)) {
                    getMenuToggleGroup().getToggles().remove(t);
                    menuPane.getChildren().remove(t);
                    break;
                }
            }
        }

        selectRadioButton(SettingsPage.GENERAL);
        currentTabChanged(map, SettingsPage.GENERAL);

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
