/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.planning.aoi;

import com.google.inject.Inject;
import com.intel.missioncontrol.geometry.AreaOfInterest;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.measure.Dimension.Length;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.measure.property.IQuantityStyleProvider;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.ui.ViewBase;
import com.intel.missioncontrol.ui.ViewHelper;
import com.intel.missioncontrol.ui.controls.AutoCommitSpinner;
import com.intel.missioncontrol.ui.controls.ToggleSwitch;
import de.saxsys.mvvmfx.Context;
import de.saxsys.mvvmfx.InjectContext;
import de.saxsys.mvvmfx.InjectViewModel;
import eu.mavinci.core.flightplan.CPicArea;
import eu.mavinci.core.flightplan.CPicArea.FacadeScanningSide;
import eu.mavinci.core.flightplan.PlanType;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Control;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;

public class AoiDimensionsTabView extends ViewBase<AoiDimensionsTabViewModel> {

    private static final double HEIGHT_MIN = CPicArea.MIN_CROP_HEIGHT_METER;
    private static final double HEIGHT_MAX = CPicArea.MAX_CROP_HEIGHT_METER;
    private static final double HEIGHT_STEP = 1.0;

    @FXML
    private Control rootPane;

    @FXML
    private VBox aoiSettingsPane;

    @FXML
    private VBox minCaptureHeight;

    @FXML
    private VBox maxCaptureHeight;

    @FXML
    private AutoCommitSpinner<Quantity<Length>> cropBelowSpinner;

    @FXML
    private AutoCommitSpinner<Quantity<Length>> cropAboveSpinner;

    @FXML
    private ToggleGroup facadeScanSideGroup;

    @FXML
    private VBox facadeScanSideBox;

    @FXML
    private VBox scanTopSurfaceBox;

    @FXML
    private ToggleSwitch scanTopSurfaceSwitch;

    @InjectViewModel
    private AoiDimensionsTabViewModel viewModel;

    @InjectContext
    private Context context;

    @Inject
    private ISettingsManager settingsManager;

    @Inject
    private IQuantityStyleProvider quantityStyleProvider;

    @Inject
    private ILanguageHelper languageHelper;

    private AoiEditComponent aoiEditComponent;

    @Override
    public void initializeView() {
        super.initializeView();

        AreaOfInterest areaOfInterest = viewModel.getAreaOfInterest();
        aoiEditComponent =
            new AoiEditComponent(areaOfInterest, context, false, false, quantityStyleProvider, languageHelper);

        initAoiSettings(aoiEditComponent);

        areaOfInterest
            .typeProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    initAoiSettings(aoiEditComponent);
                });

        ViewHelper.initToggleGroup(
            areaOfInterest.facadeScanSideProperty(), FacadeScanningSide.class, facadeScanSideGroup);

        ViewHelper.initAutoCommitSpinner(
            cropBelowSpinner,
            viewModel.cropHeightMinQuantityProperty(),
            Unit.METER,
            settingsManager.getSection(GeneralSettings.class),
            1,
            HEIGHT_MIN,
            HEIGHT_MAX,
            HEIGHT_STEP,
            false);

        ViewHelper.initAutoCommitSpinner(
            cropAboveSpinner,
            viewModel.cropHeightMaxQuantityProperty(),
            Unit.METER,
            settingsManager.getSection(GeneralSettings.class),
            1,
            HEIGHT_MIN,
            HEIGHT_MAX,
            HEIGHT_STEP,
            false);

        scanTopSurfaceSwitch.selectedProperty().bindBidirectional(areaOfInterest.addCeilingProperty());

        initControlsVisibility(areaOfInterest);
    }

    @Override
    protected Parent getRootNode() {
        return rootPane;
    }

    @Override
    protected AoiDimensionsTabViewModel getViewModel() {
        return viewModel;
    }

    private void initAoiSettings(AoiEditComponent aoiEditComponent) {
        aoiSettingsPane.getChildren().clear();
        aoiSettingsPane.setMinWidth(0);
        aoiSettingsPane.setMaxWidth(Double.MAX_VALUE);
        List editors = aoiEditComponent.getEditors();
        aoiSettingsPane.getChildren().addAll(editors);
    }

    private void updateFacadeScanSideBoxVisibility(PlanType aoiType) {
        facadeScanSideBox.setVisible(aoiType == PlanType.FACADE);
    }

    private void initControlsVisibility(AreaOfInterest areaOfInterest) {
        facadeScanSideBox.managedProperty().bind(facadeScanSideBox.visibleProperty());
        scanTopSurfaceBox.managedProperty().bind(scanTopSurfaceBox.visibleProperty());

        bindNodeVisibility(cropBelowSpinner);
        bindNodeVisibility(cropAboveSpinner);

        PlanType planType = areaOfInterest.getType();

        updateFacadeScanSideBoxVisibility(planType);
        updateScanTopSurfaceVisibility(planType);
        updateCropVisibility(planType);

        areaOfInterest
            .typeProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    // initAoiSettings(aoiEditComponent);
                    updateEditorsVisibility(newValue);
                    updateFacadeScanSideBoxVisibility(newValue);
                    updateScanTopSurfaceVisibility(newValue);
                    updateCropVisibility(newValue);
                });
    }

    private void updateEditorsVisibility(PlanType newValue) {
        aoiSettingsPane.setVisible(true);
    }

    private void updateScanTopSurfaceVisibility(PlanType type) {
        boolean supportsTopSurface = (type != null) && (type.needsCeiling());

        scanTopSurfaceBox.setVisible(supportsTopSurface);
    }

    private void updateCropVisibility(PlanType type) {
        boolean supportsCrop = (type != null) && (type.supportsCrop()) && (type != PlanType.WINDMILL);

        minCaptureHeight.setVisible(supportsCrop);
        maxCaptureHeight.setVisible(supportsCrop);
    }

    private void bindNodeVisibility(Node node) {
        node.managedProperty().bind(node.visibleProperty());
    }

}
