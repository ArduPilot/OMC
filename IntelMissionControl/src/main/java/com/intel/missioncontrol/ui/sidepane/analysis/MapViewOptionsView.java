/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.analysis;

import com.google.inject.Inject;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.measure.Dimension.Length;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.ui.EnumConverter;
import com.intel.missioncontrol.ui.ViewBase;
import com.intel.missioncontrol.ui.ViewHelper;
import com.intel.missioncontrol.ui.controls.AutoCommitSpinner;
import de.saxsys.mvvmfx.InjectViewModel;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.ProjectionType;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;

public class MapViewOptionsView extends ViewBase<MapViewOptionsViewModel> {

    @FXML
    private Parent rootNode;

    @FXML
    private CheckBox chkShowPreviews;

    @FXML
    private CheckBox chkShowImageLocations;

    @FXML
    private CheckBox chkShowAois;

    @FXML
    private CheckBox chkShowRtkBaseLocation;

    @FXML
    private CheckBox chkShowCoverage;

    // @FXML
    // private ImageView showOnMapImage;

    @FXML
    private CheckBox chkDatasetTrack;

    /*temporary removed
    @FXML
    private CheckBox chkShowAnnotations;*/

    @FXML
    private ToggleGroup imageProjectionGroup;

    @FXML
    private RadioButton rbtn2dSurveys;

    @FXML
    private RadioButton rbtn3dInspections;

    @FXML
    private VBox projectionDistanceBox;

    @FXML
    private AutoCommitSpinner<Quantity<Length>> elevationOffsetSpinner;

    @FXML
    private AutoCommitSpinner<Quantity<Length>> projectionDistanceSpinner;

    @FXML
    private ComboBox<ImageChannel> channelCombo;

    @FXML
    private Label elevationOffsetLabel;

    @InjectViewModel
    private MapViewOptionsViewModel viewModel;

    @Inject
    private ISettingsManager settingsManager;

    @Inject
    private ILanguageHelper languageHelper;

    @Override
    public void initializeView() {
        super.initializeView();

        chkShowPreviews.selectedProperty().bindBidirectional(viewModel.showPreviewsProperty());
        chkShowImageLocations.selectedProperty().bindBidirectional(viewModel.showImageLocationsProperty());
        chkShowAois.selectedProperty().bindBidirectional(viewModel.showAoisProperty());
        chkDatasetTrack.selectedProperty().bindBidirectional(viewModel.showDatasetTrackProperty());
        chkShowRtkBaseLocation.selectedProperty().bindBidirectional(viewModel.showRtkBaseLocationProperty());
        chkShowRtkBaseLocation.visibleProperty().bind(viewModel.rtkAvailableProperty());
        chkShowRtkBaseLocation.managedProperty().bind(chkShowRtkBaseLocation.visibleProperty());

        chkShowCoverage.selectedProperty().bindBidirectional(viewModel.showCoverageProperty());
        //        temporary removed
        //        chkShowAnnotations.selectedProperty().bindBidirectional(viewModel.showAnnotationsProperty());

        // showOnMapImage.setFitHeight(ScaleHelper.emsToPixels(1));

        initElevationOffsetSpinner();
        initProjectionDistanceSpinner();
        initImageProjectionToggleGroup();

        elevationOffsetSpinner.visibleProperty().bind(viewModel.showElevationOffsetProperty());
        elevationOffsetLabel.visibleProperty().bind(viewModel.showElevationOffsetProperty());
        elevationOffsetSpinner.managedProperty().bind(viewModel.showElevationOffsetProperty());
        elevationOffsetLabel.managedProperty().bind(viewModel.showElevationOffsetProperty());

        initChannelCombo();
    }

    @Override
    public Parent getRootNode() {
        return rootNode;
    }

    @Override
    public MapViewOptionsViewModel getViewModel() {
        return viewModel;
    }

    private void initElevationOffsetSpinner() {
        ViewHelper.initAutoCommitSpinner(
            elevationOffsetSpinner,
            viewModel.elevationOffsetQuantityProperty(),
            Unit.METER,
            settingsManager.getSection(GeneralSettings.class),
            MapViewOptionsViewModel.ELEVATION_OFFSET_DIGITS,
            MapViewOptionsViewModel.ELEVATION_OFFSET_MIN,
            MapViewOptionsViewModel.ELEVATION_OFFSET_MAX,
            MapViewOptionsViewModel.ELEVATION_OFFSET_STEP,
            false);
    }

    private void initProjectionDistanceSpinner() {
        ViewHelper.initAutoCommitSpinner(
            projectionDistanceSpinner,
            viewModel.projectionDistanceQuantityProperty(),
            Unit.METER,
            settingsManager.getSection(GeneralSettings.class),
            MapViewOptionsViewModel.PROJECTION_DISTANCE_DIGITS,
            MapViewOptionsViewModel.PROJECTION_DISTANCE_MIN,
            MapViewOptionsViewModel.PROJECTION_DISTANCE_MAX,
            MapViewOptionsViewModel.PROJECTION_DISTANCE_STEP,
            false);
    }

    private void initImageProjectionToggleGroup() {
        viewModel
            .imageProjectionProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    projectionDistanceBox.setVisible(newValue == ProjectionType.INSPECTIONS_3D);
                });
        projectionDistanceBox.managedProperty().bind(projectionDistanceBox.visibleProperty());
        projectionDistanceSpinner.managedProperty().bind(projectionDistanceBox.visibleProperty());
        projectionDistanceSpinner.visibleProperty().bind(projectionDistanceBox.visibleProperty());

        rbtn2dSurveys.setUserData(ProjectionType.SURVEYS_2D);
        rbtn3dInspections.setUserData(ProjectionType.INSPECTIONS_3D);

        imageProjectionGroup.selectToggle(
            imageProjectionGroup
                .getToggles()
                .filtered(toggle -> toggle.getUserData().equals(viewModel.imageProjectionProperty().getValue()))
                .get(0));

        viewModel
            .imageProjectionProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    imageProjectionGroup.selectToggle(
                        imageProjectionGroup
                            .getToggles()
                            .filtered(toggle -> toggle.getUserData().equals(newValue))
                            .get(0));
                });
        imageProjectionGroup
            .selectedToggleProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    viewModel.imageProjectionProperty().setValue((ProjectionType)newValue.getUserData());
                });
    }

    private void initChannelCombo() {
        channelCombo.itemsProperty().bind(viewModel.availableChannelsProperty());
        channelCombo.setConverter(new EnumConverter<>(languageHelper, ImageChannel.class));
        channelCombo.valueProperty().bindBidirectional(viewModel.imageChannelProperty());
    }

}
