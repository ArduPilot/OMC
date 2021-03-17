/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.planning.aoi;

import com.google.inject.Inject;
import com.intel.missioncontrol.geometry.AreaOfInterest;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.settings.OperationLevel;
import com.intel.missioncontrol.ui.EnumConverter;
import com.intel.missioncontrol.ui.ViewBase;
import com.intel.missioncontrol.ui.ViewHelper;
import com.intel.missioncontrol.ui.controls.Button;
import com.intel.missioncontrol.ui.sidepane.planning.widgets.GsdComponent;
import com.intel.missioncontrol.ui.sidepane.planning.widgets.GsdWidgetViewModel;
import de.saxsys.mvvmfx.InjectViewModel;
import eu.mavinci.core.flightplan.CPicArea;
import eu.mavinci.core.flightplan.PlanType;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class AoiGeneralTabView extends ViewBase<AoiGeneralTabViewModel> {

    @FXML
    private Control rootNode;

    @FXML
    private TextField txtAoiName;

    @FXML
    private GsdComponent gsdComponent;

    @FXML
    private VBox objectSurfaceBox;

    @FXML
    private VBox aoiOrderBox;

    @FXML
    private VBox spec3dFileBox;

    @FXML
    private Spinner<Integer> aoiOrderSpinner;

    @FXML
    private ComboBox<CPicArea.ModelSourceTypes> modelSourceType;

    @FXML
    private TextField modelPathField;

    @FXML
    private Button browseModel;

    @InjectViewModel
    private AoiGeneralTabViewModel viewModel;

    @Inject
    private ILanguageHelper languageHelper;

    @Inject
    private ISettingsManager settingsManager;

    private AoiAdvancedParametersView advancedParametersView;

    @Override
    public void initializeView() {
        super.initializeView();

        AreaOfInterest areaOfInterest = viewModel.getAreaOfInterest();

        GsdWidgetViewModel gsdViewModel = gsdComponent.getViewModel();
        gsdViewModel.altitudeProperty().bindBidirectional(areaOfInterest.altProperty());
        gsdViewModel.gsdProperty().bindBidirectional(areaOfInterest.gsdProperty());
        gsdViewModel.aoiTypeProperty().bind(areaOfInterest.typeProperty());

        gsdComponent
            .visibleProperty()
            .bind(
                areaOfInterest
                    .typeProperty()
                    .isNotEqualTo(PlanType.NO_FLY_ZONE_POLY)
                    .and(areaOfInterest.typeProperty().isNotEqualTo(PlanType.NO_FLY_ZONE_CIRC))
                    .and(areaOfInterest.typeProperty().isNotEqualTo(PlanType.GEOFENCE_POLY))
                    .and(areaOfInterest.typeProperty().isNotEqualTo(PlanType.GEOFENCE_CIRC))
                    .and(areaOfInterest.typeProperty().isNotEqualTo(PlanType.PANORAMA)));

        int aoiCount = viewModel.aoiCountProperty().getValue().intValue();

        aoiOrderBox
            .visibleProperty()
            .bind(
                areaOfInterest
                    .typeProperty()
                    .isNotEqualTo(PlanType.NO_FLY_ZONE_POLY)
                    .and(areaOfInterest.typeProperty().isNotEqualTo(PlanType.NO_FLY_ZONE_CIRC))
                    .and(areaOfInterest.typeProperty().isNotEqualTo(PlanType.GEOFENCE_POLY))
                    .and(areaOfInterest.typeProperty().isNotEqualTo(PlanType.GEOFENCE_CIRC))
                    .and(viewModel.aoiCountProperty().greaterThan(1))
                    .and(
                        settingsManager
                            .getSection(GeneralSettings.class)
                            .operationLevelProperty()
                            .isEqualTo(OperationLevel.DEBUG)));

        aoiOrderBox.managedProperty().bind(aoiOrderBox.visibleProperty());

        txtAoiName.textProperty().bindBidirectional(areaOfInterest.nameProperty());

        if (aoiCount >= 1) {
            aoiOrderSpinner.setEditable(true);
            ViewHelper.initSpinner(
                aoiOrderSpinner,
                new IntegerSpinnerValueFactory(1, aoiCount, 1),
                viewModel.aoiOrderProperty().asObject());
        } else {
            aoiOrderSpinner.setEditable(false);
        }

        objectSurfaceBox
            .visibleProperty()
            .bind(
                areaOfInterest
                    .typeProperty()
                    .isEqualTo(PlanType.COPTER3D)
                    .or(
                        areaOfInterest
                            .typeProperty()
                            .isEqualTo(
                                PlanType
                                    .INSPECTION_POINTS))); // TODO 20180822: for the INSPECTION_POINTS POC we temporarily reue
        // the modelFile property for specifying the import source...

        objectSurfaceBox.managedProperty().bind(objectSurfaceBox.visibleProperty());

        modelSourceType.getItems().addAll(CPicArea.ModelSourceTypes.values());
        modelSourceType.setConverter(new EnumConverter<>(languageHelper, CPicArea.ModelSourceTypes.class));
        modelSourceType.valueProperty().bindBidirectional(areaOfInterest.modelSourceProperty());

        modelPathField.textProperty().bindBidirectional(areaOfInterest.modelFilePathProperty());

        spec3dFileBox
            .visibleProperty()
            .bind(areaOfInterest.modelSourceProperty().isEqualTo(CPicArea.ModelSourceTypes.MODEL_FILE));
        spec3dFileBox.managedProperty().bind(spec3dFileBox.visibleProperty());

        browseModel.setOnAction((a) -> viewModel.getBrowseModelCommand().execute());
    }

    @Override
    protected Control getRootNode() {
        return rootNode;
    }

    @FXML
    public void openTransformationTab() {
        if (advancedParametersView == null) {
            return;
        }

        advancedParametersView.openTransformationTab();
    }

    public void setAdvancedParametersView(AoiAdvancedParametersView advancedParametersView) {
        this.advancedParametersView = advancedParametersView;
    }

    public AoiGeneralTabViewModel getViewModel() {
        return viewModel;
    }

}
