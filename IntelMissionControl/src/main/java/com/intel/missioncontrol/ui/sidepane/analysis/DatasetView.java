/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.analysis;

import com.google.inject.Inject;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.mission.Matching;
import com.intel.missioncontrol.mission.MatchingStatus;
import com.intel.missioncontrol.mission.Mission;
import com.intel.missioncontrol.ui.controls.MenuButton;
import com.intel.missioncontrol.ui.dialogs.IDialogContextProvider;
import com.intel.missioncontrol.ui.dialogs.IDialogService;
import com.intel.missioncontrol.ui.sidepane.FancyTabView;
import de.saxsys.mvvmfx.Context;
import de.saxsys.mvvmfx.InjectContext;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.ViewModel;
import java.util.function.Predicate;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import org.asyncfx.beans.property.PropertyPath;

public class DatasetView extends FancyTabView<DatasetViewModel> {

    @InjectViewModel
    private DatasetViewModel viewModel;

    @InjectContext
    private Context context;

    @FXML
    private Label projectNameLabel;

    @FXML
    private MenuButton datasetMenuButton;

    @FXML
    private Button showOnMapButton;

    @FXML
    private SplitMenuButton exportMenuButton;

    @FXML
    private MenuItem exportToLastDestinationMenuItem;

    @FXML
    private MenuItem agiSoftPhotoScanExport;

    @FXML
    private MenuItem agiSoftMetashapeExport;

    @FXML
    private MenuItem contextCaptureExport;

    @FXML
    private MenuItem pix4DesktopExport;

    @FXML
    private Button saveDatasetBtn;

    @FXML
    private Button saveToCloudBtn;

    private KeyCombination exportBtnKeyCombination =
        new KeyCodeCombination(KeyCode.E, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN);

    private final IApplicationContext applicationContext;

    private final ILanguageHelper languageHelper;

    private final IDialogService dialogService;

    public static final String TITLE = "com.intel.missioncontrol.ui.analysis.AnalysisView.exportFileExists";
    public static final String MESSAGE = "com.intel.missioncontrol.ui.analysis.AnalysisView.exportFileOverride";

    private final IDialogContextProvider dialogContextProvider;

    @Inject
    public DatasetView(
            IApplicationContext applicationContext,
            IDialogService dialogService,
            ILanguageHelper languageHelper,
            IDialogContextProvider dialogContextProvider) {
        this.applicationContext = applicationContext;
        this.dialogService = dialogService;
        this.languageHelper = languageHelper;
        this.dialogContextProvider = dialogContextProvider;
    }

    @Override
    protected ViewModel getViewModel() {
        return viewModel;
    }

    @Override
    protected void initializeView() {
        super.initializeView();
        dialogContextProvider.setContext(viewModel, context);

        projectNameLabel
            .textProperty()
            .bind(
                PropertyPath.from(applicationContext.currentMissionProperty())
                    .selectReadOnlyString(Mission::nameProperty));

        datasetMenuButton.modelProperty().bind(viewModel.datasetMenuModelProperty());
        showOnMapButton.setOnAction(event -> viewModel.getShowOnMapCommand().execute());
        showOnMapButton.disableProperty().bind(viewModel.getShowOnMapCommand().notExecutableProperty());

        exportMenuButton.textProperty().bind(viewModel.exportButtonTextProperty());
        exportMenuButton.setOnAction(e -> viewModel.getExporter(viewModel.lastExportTypeProperty().get()).export(e));

        exportMenuButton.disableProperty().bind(PropertyPath.from(viewModel.currentMatchingProperty())
                .selectReadOnlyObject(Matching::statusProperty)
                .isEqualTo(MatchingStatus.NEW));

        exportToLastDestinationMenuItem.textProperty().bind(viewModel.exportButtonTextProperty());
        exportToLastDestinationMenuItem.setOnAction(
            e -> viewModel.getExporter(viewModel.lastExportTypeProperty().get()).export(e));
        exportToLastDestinationMenuItem
            .disableProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue) {
                        exportToLastDestinationMenuItem.setAccelerator(null);
                    } else {
                        exportToLastDestinationMenuItem.setAccelerator(exportBtnKeyCombination);
                    }
                });

        saveDatasetBtn
                .disableProperty()
                .bind(
                        PropertyPath.from(viewModel.currentMatchingProperty())
                                .selectReadOnlyBoolean(Matching::matchingLayerChangedProperty)
                                .not().or(PropertyPath.from(viewModel.currentMatchingProperty())
                                .selectReadOnlyObject(Matching::statusProperty)
                                .isEqualTo(MatchingStatus.NEW)));

        //TODO: define when enabled!
        saveToCloudBtn
                .disableProperty()
                .bind(
                        PropertyPath.from(viewModel.currentMatchingProperty())
                                .selectReadOnlyBoolean(Matching::matchingLayerChangedProperty)
                                .not().or(PropertyPath.from(viewModel.currentMatchingProperty())
                                .selectReadOnlyObject(Matching::statusProperty)
                                .isEqualTo(MatchingStatus.NEW)));



        viewModel.getExporter(ExportTypes.CSV).alsoAvailableIf(userAllowsToOverrideFile(ExportTypes.CSV));
        viewModel
            .getExporter(ExportTypes.PIX4D_DESKTOP)
            .alsoAvailableIf(userAllowsToOverrideFile(ExportTypes.PIX4D_DESKTOP));

        viewModel
            .getExporter(ExportTypes.AGISOFT_PHOTOSCAN)
            .alsoAvailableIf(userAllowsToOverrideFile(ExportTypes.AGISOFT_PHOTOSCAN));

        viewModel
            .getExporter(ExportTypes.AGISOFT_METASHAPE)
            .alsoAvailableIf(userAllowsToOverrideFile(ExportTypes.AGISOFT_METASHAPE));

        viewModel
            .getExporter(ExportTypes.CONTEXT_CAPTURE)
            .alsoAvailableIf(userAllowsToOverrideFile(ExportTypes.CONTEXT_CAPTURE));

        setDisabilityForExternalExportApplication();
    }

    private Predicate<Matching> userAllowsToOverrideFile(ExportTypes type) {
        return matching ->
            dialogService.requestConfirmation(
                languageHelper.getString(TITLE),
                languageHelper.getString(MESSAGE, viewModel.getTargetPathCommand(type).apply(matching)));
    }

    private void setDisabilityForExternalExportApplication() {
        agiSoftPhotoScanExport
            .visibleProperty()
            .bind(viewModel.agiSoftPhotoScanEnabledProperty().and(viewModel.agiSoftIsMetashapeProperty().not()));
        agiSoftMetashapeExport
            .visibleProperty()
            .bind(viewModel.agiSoftPhotoScanEnabledProperty().and(viewModel.agiSoftIsMetashapeProperty()));

        contextCaptureExport.disableProperty().bind(viewModel.contextCaptureExportEnabledProperty().not());
        pix4DesktopExport.disableProperty().bind(viewModel.pix4DDesktopExportEnabledProperty().not());
    }

    public void saveDatasetAction(ActionEvent actionEvent) {
        viewModel.saveDataset();
    }

    public void saveToCloudAction(ActionEvent actionEvent) {
        viewModel.saveToCloud();
    }

    public void showDefineAppSettings(ActionEvent actionEvent) {
        viewModel.goToDefineAppPaths();
    }

    public void exportToPix4Desktop(ActionEvent event) {
        viewModel.getExporter(ExportTypes.PIX4D_DESKTOP).export(event);
    }

    public void intelInsightUploadProcessing(ActionEvent event) {
        viewModel.getExporter(ExportTypes.INTEL_INSIGHT_PROCESS).export(event);
    }

    public void intelInsightUploadNotProcessing(ActionEvent event) {
        viewModel.getExporter(ExportTypes.INTEL_INSIGHT_NOTPROCESS).export(event);
    }

    public void exportToContextCapture(ActionEvent event) {
        viewModel.getExporter(ExportTypes.CONTEXT_CAPTURE).export(event);
    }

    public void exportToAgiSoftPhotoScan(ActionEvent event) {
        if (viewModel.agiSoftIsMetashapeProperty().get()) {
            viewModel.getExporter(ExportTypes.AGISOFT_METASHAPE).export(event);
        } else {
            viewModel.getExporter(ExportTypes.AGISOFT_PHOTOSCAN).export(event);
        }
    }

    public void writeExifToImages(ActionEvent actionEvent) {
        viewModel.getExporter(ExportTypes.WRITE_EXIF).export(new ActionEvent());
    }

    public void exportToCsv(ActionEvent actionEvent) {
        viewModel.getExporter(ExportTypes.CSV).export(new ActionEvent());
    }

    @FXML
    public void renameClicked() {
        viewModel.getRenameMissionCommand().execute();
    }

    public void sparseDataset(ActionEvent actionEvent) {
        viewModel.sparseDataset();
    }
}
