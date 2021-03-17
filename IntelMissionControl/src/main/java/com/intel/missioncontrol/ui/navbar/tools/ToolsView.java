/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.tools;

import com.google.common.collect.ImmutableMap;
import com.intel.missioncontrol.helper.Expect;
import com.intel.missioncontrol.ui.navbar.NavBarMenuView;
import com.intel.missioncontrol.ui.navbar.tools.model.ToolsViewModel;
import de.saxsys.mvvmfx.InjectViewModel;
import java.util.Map;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import org.jetbrains.annotations.Nullable;

public class ToolsView extends NavBarMenuView<ToolsViewModel, ToolsPage> {

    @InjectViewModel
    private ToolsViewModel viewModel;

    @FXML
    private Pane layoutRoot;

    @FXML
    private Parent transformRoot;

    @FXML
    private Pane contentPane;

    @FXML
    private ToggleGroup menuToggleGroup;

    @FXML
    private Region orientationDataView;

    @FXML
    private Region debugDataView;

    @FXML
    private Region positionDataView;

    @FXML
    private Region positionOrientationDataView;

    @FXML
    private Region linkInfoView;

    @FXML
    private Region photoDataView;

    @FXML
    private Region backendInfoView;

    @FXML
    private Region androidStateView;

    @FXML
    private Region otherDetailsView;

    @FXML
    private Region windAndAirspeedEstimationView;

    @FXML
    private Region simulatedSystemFailuresView;

    @FXML
    private Region manualServoInputValuesView;

    @Override
    protected void initializeView() {
        super.initializeView();

        Expect.notNull(
            orientationDataView, "orientationDataView",
            debugDataView, "debugDataView",
            positionDataView, "positionDataView",
            positionOrientationDataView, "positionOrientationDataView",
            linkInfoView, "linkInfoView",
            photoDataView, "photoDataView",
            backendInfoView, "backendInfoView",
            androidStateView, "androidStateView",
            otherDetailsView, "otherDetailsView",
            windAndAirspeedEstimationView, "windAndAirspeedEstimationView",
            simulatedSystemFailuresView, "simulatedSystemFailuresView",
            manualServoInputValuesView, "manualServoInputValuesView");

        final Map<ToolsPage, Region> map =
            ImmutableMap.<ToolsPage, Region>builder()
                .put(ToolsPage.ORIENTATION, orientationDataView)
                .put(ToolsPage.DEBUG_DATA, debugDataView)
                .put(ToolsPage.POSITION_DATA, positionDataView)
                .put(ToolsPage.POSITION_OPERATION_DATA, positionOrientationDataView)
                .put(ToolsPage.LINK_INFO, linkInfoView)
                .put(ToolsPage.PHOTO_DATA, photoDataView)
                .put(ToolsPage.BACKEND_INFO, backendInfoView)
                .put(ToolsPage.ANDROID_STATE, androidStateView)
                .put(ToolsPage.OTHER_DETAILS, otherDetailsView)
                .put(ToolsPage.AIRSPEED_ESTIMATION, windAndAirspeedEstimationView)
                .put(ToolsPage.SIMULATOR_FAILURES, simulatedSystemFailuresView)
                .put(ToolsPage.SERVO_MAN_INPUT, manualServoInputValuesView)
                .build();

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

        // When the user clicks on a radio button and selects it, we need to update the current page property.
        //
        getMenuToggleGroup()
            .selectedToggleProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    if (viewModel != null && newValue != null && newValue.getUserData() != null) {
                        viewModel.currentPageProperty().set(ToolsPage.valueOf((String)newValue.getUserData()));
                    }
                });

        orientationDataView
            .prefWidthProperty()
            .addListener((observable, oldValue, newValue) -> getContentPane().setPrefWidth(newValue.doubleValue()));

        selectRadioButton(ToolsPage.ORIENTATION);
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
    protected ToolsViewModel getViewModel() {
        return viewModel;
    }

    @FXML
    public void onBackgroundClicked() {
        viewModel.getCloseCommand().execute();
    }

    @FXML
    public void onCloseClicked() {
        viewModel.getCloseCommand().execute();
    }

    @FXML
    public void onDetachPageClicked() {}

}
