/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.analysis;

import com.google.inject.Inject;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.mission.FlightPlan;
import com.intel.missioncontrol.ui.controls.MenuButton;
import com.intel.missioncontrol.ui.dialogs.DialogView;
import com.intel.missioncontrol.ui.dialogs.IDialogContextProvider;
import com.intel.missioncontrol.ui.validation.LabelValidationRichVisualizer;
import de.saxsys.mvvmfx.Context;
import de.saxsys.mvvmfx.InjectContext;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.ViewModel;
import de.saxsys.mvvmfx.utils.validation.visualization.ValidationVisualizer;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.controlsfx.control.CheckComboBox;
import org.controlsfx.control.ToggleSwitch;

public class DataImportNewView extends DialogView<DataImportNewViewModel> {

    private static final String DIALOG_VIEW_TITLE =
            "com.intel.missioncontrol.ui.sidepane.analysis.DataImportView.title";

    @InjectViewModel
    private DataImportNewViewModel viewModel;

    @InjectContext
    private Context context;

    @FXML
    private Pane layoutRoot;

    @FXML
    public ComboBox dataTypeCombobox;

    @FXML
    private Control imagePane;

    @FXML
    private VBox flightLogsPane;
    // @FXML
    // private  VBox flightPlanPane;

    // @FXML
    // private Label projectNameLabel;

    // @FXML
    // private MenuButton datasetMenuButton;

    @FXML
    private TableView<FlightLogEntry> tableView;

    @FXML
    private TableColumn<FlightLogEntry, FlightLogEntry> checkedColumn;

    @FXML
    private TableColumn<FlightLogEntry, FlightLogEntry> nameColumn;

    @FXML
    private TableColumn<FlightLogEntry, FlightLogEntry> dateColumn;

    @FXML
    private TableColumn<FlightLogEntry, FlightLogEntry> durationColumn;

    @FXML
    private TableColumn<FlightLogEntry, FlightLogEntry> imageCountColumn;

    @FXML
    private CheckBox selectionCheckBox;

    @FXML
    private MenuButton addFlightLogsButton;

    @FXML
    private Control flightPlansPane;

    @FXML
    private TitledPane optionsPane;

    @FXML
    private Label imagesCountLabel;

    @FXML
    private Button transferWirelesslyButton;

    @FXML
    private Button captureImagesButton;

    @FXML
    private Button importButton;

    @FXML
    private CheckBox chkImportAnotherSource;

    @FXML
    private Button cancelButton;

    @FXML
    private Button browseImagesButton;

    @FXML
    private Label mismatchedImagesLabel;

    @FXML
    private Label notEnoughSpaceLabel;

    @FXML
    private TextField imageFolderTextField;

    @FXML
    private CheckComboBox<FlightPlan> flightPlanNamesComboBox;

    @FXML
    private ToggleSwitch copyFilesToProjectFolderSwitch;

    @FXML
    private ToggleSwitch eraseFilesAfterCopyingSwitch;

    @FXML
    private ToggleSwitch eraseLogFilesAfterCopyingSwitch;

    @FXML
    private ToggleSwitch onlyIncludeImagesInPlan;
    //   @FXML
    // private Button showHelpButton;

    private final ILanguageHelper languageHelper;
    private final IDialogContextProvider dialogContextProvider;
    private final List<ValidationVisualizer> validationVisualizers = new ArrayList<>();
    // private final InvalidationListener tableInvalidatedListener = observable -> updateTableHeight();

    @Inject
    public DataImportNewView(ILanguageHelper languageHelper, IDialogContextProvider dialogContextProvider) {
        this.languageHelper = languageHelper;
        this.dialogContextProvider = dialogContextProvider;
    }

    @Override
    protected Parent getRootNode() {
        return layoutRoot;
    }

    @Override
    protected ViewModel getViewModel() {
        return viewModel;
    }

    @Override
    protected void initializeView() {
        super.initializeView();

        dialogContextProvider.setContext(viewModel, context);
        //    projectNameLabel.textProperty().bind(viewModel.missionNameProperty());
        //    datasetMenuButton.modelProperty().bind(viewModel.datasetMenuModelProperty());

        AddFlightLogsView.configureTable(
                languageHelper, tableView, checkedColumn, nameColumn, dateColumn, durationColumn, imageCountColumn);

        tableView.itemsProperty().bind(viewModel.flightLogsProperty());
        tableView.visibleProperty().bind(viewModel.flightLogSelectionAvailableProperty());
        tableView.managedProperty().bind(tableView.visibleProperty());

        addFlightLogsButton.setModel(viewModel.getAddFlightLogsMenuModel());
        addFlightLogsButton
                .showingProperty()
                .addListener(observable -> viewModel.getUpdateDriveListCommand().execute());

        chkImportAnotherSource.selectedProperty().bindBidirectional(viewModel.importAnotherSourceProperty());

        importButton.disableProperty().bind(viewModel.getImportCommand().notExecutableProperty());
        importButton.setOnAction(event -> viewModel.getImportCommand().execute());

        cancelButton.setOnAction(event -> viewModel.getCancelCommand().execute());

        transferWirelesslyButton
                .disableProperty()
                .bind(viewModel.getTransferWirelesslyCommand().notExecutableProperty());
        transferWirelesslyButton.setOnAction(event -> viewModel.getTransferWirelesslyCommand().execute());
        captureImagesButton.disableProperty().bind(viewModel.getCaptureImagesCommand().notExecutableProperty());
        captureImagesButton.setOnAction(event -> viewModel.getCaptureImagesCommand().execute());

        //viewModel.copyFilesProperty().setValue(true);
        copyFilesToProjectFolderSwitch.selectedProperty().bindBidirectional(viewModel.copyFilesProperty());
        eraseFilesAfterCopyingSwitch.disableProperty().bind(copyFilesToProjectFolderSwitch.selectedProperty().not());
        eraseFilesAfterCopyingSwitch.selectedProperty().bindBidirectional(viewModel.eraseImagesProperty());
        eraseLogFilesAfterCopyingSwitch.selectedProperty().bindBidirectional(viewModel.eraseLogsProperty());
        onlyIncludeImagesInPlan.selectedProperty().bindBidirectional(viewModel.onlyIncludeImagesInPlanProperty());

        validationVisualizers.add(
                new LabelValidationRichVisualizer(viewModel.cameraImagesValidationStatusProperty(), mismatchedImagesLabel));
        validationVisualizers.add(
                new LabelValidationRichVisualizer(viewModel.dataSetSizeValidationStatusProperty(), notEnoughSpaceLabel));

        imagesCountLabel.textProperty().bind(viewModel.imagesFolderValidTextProperty());
        imageFolderTextField.textProperty().bindBidirectional(viewModel.imageFolderProperty());
        mismatchedImagesLabel.managedProperty().bind(mismatchedImagesLabel.visibleProperty());

        browseImagesButton.disableProperty().bind(viewModel.getBrowseImagesCommand().notExecutableProperty());
        browseImagesButton.setOnAction(event -> viewModel.getBrowseImagesCommand().execute());

        //   showHelpButton.disableProperty().bind(viewModel.getShowHelpCommand().notExecutableProperty());
        //   showHelpButton.setOnAction(event -> viewModel.getShowHelpCommand().execute());

        flightPlanNamesComboBox.setConverter(
                new StringConverter<>() {
                    @Override
                    public String toString(FlightPlan object) {
                        return object.getName();
                    }

                    @Override
                    public FlightPlan fromString(String string) {
                        return null;
                    }
                });

        Bindings.bindContent(flightPlanNamesComboBox.getItems(), viewModel.flightPlansProperty());
        Bindings.bindContent(
                viewModel.selectedFlightPlansProperty(), flightPlanNamesComboBox.getCheckModel().getCheckedItems());

        selectionCheckBox.selectedProperty().bindBidirectional(viewModel.selectionCheckBoxProperty());
        /*
        //dirty hack to get strings upadted on whatever change even on the mission names
        //BUT: listener is called, but its not triggering any redraw of the items  Marco: 2018.04.06
        viewModel
            .flightPlansProperty()
            .addListener((InvalidationListener)  c -> flightPlanNames. setConverter(createStringConverter()));
        */

        dataTypeCombobox.itemsProperty().bind(viewModel.dataTypeProperty());
        dataTypeCombobox.valueProperty().bindBidirectional(viewModel.selecteDataTypeProperty());

        imagePane.visibleProperty().bind(viewModel.imageDataTypeProperty());
        flightLogsPane.visibleProperty().bind(viewModel.flightLogsDataTypeProperty());
        flightPlansPane
                .visibleProperty()
                .bind(viewModel.flightPlanDataTypeProperty()); // TODO IMC-3137 + viewModel.flightPlanSelectionAvailableProperty()

        imagePane.managedProperty().bind(viewModel.imageDataTypeProperty());
        flightLogsPane.managedProperty().bind(viewModel.flightLogsDataTypeProperty());
        flightPlansPane.managedProperty().bind(flightPlansPane.visibleProperty());
    }

    @FXML
    public void renameClicked() {
        viewModel.getRenameMissionCommand().execute();
    }

    @Override
    public ReadOnlyStringProperty titleProperty() {
        return new ReadOnlyStringWrapper(languageHelper.getString(DIALOG_VIEW_TITLE));
    }

}
