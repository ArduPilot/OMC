/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.planning;

import com.google.inject.Inject;
import com.intel.missioncontrol.flightplantemplate.FlightPlanTemplate;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.ui.dialogs.DialogView;
import com.intel.missioncontrol.ui.dialogs.IDialogService;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FlightPlanTemplateManagementView extends DialogView<FlightPlanTemplateManagementViewModel> {

    public static final String TEMPLATE_MANAGMENT_CAPTION_KEY =
        "com.intel.missioncontrol.ui.planning.FlightPlanTemplateManagerView.caption";

    public static float VIEW_DEFAULT_HEIGHT = 35;
    public static float VIEW_DEFAULT_WIDTH = 65;

    @Inject
    private ILanguageHelper languageHelper;

    @Inject
    private IDialogService dialogService;

    @FXML
    private Pane layoutRoot;

    @FXML
    public TableColumn<FlightPlanTemplateManagementItem, String> templateName;

    @FXML
    public TableColumn<FlightPlanTemplateManagementItem, String> uavType;

    @FXML
    public TableColumn<FlightPlanTemplateManagementItem, String> cameraName;

    @FXML
    public TableColumn<FlightPlanTemplateManagementItem, String> lensName;

    @FXML
    public TableColumn<FlightPlanTemplateManagementItem, String> aois;

    @FXML
    public TableColumn<FlightPlanTemplateManagementItem, Button> action;

    @FXML
    public TableView<FlightPlanTemplateManagementItem> templatesTable;

    @FXML
    public Button editButton;

    @FXML
    public Button duplicateButton;

    @FXML
    public MenuItem exportTemplateMenuItem;

    @FXML
    public Button importTemplateButton;

    @FXML
    public Button selectTemplateButton;

    @InjectViewModel
    private FlightPlanTemplateManagementViewModel viewModel;

    private BooleanProperty selectionIndicator = new SimpleBooleanProperty(false);

    @Override
    protected void initializeView() {
        super.initializeView();
        editButton.disableProperty().bind(selectionIndicator.not());
        duplicateButton.disableProperty().bind(selectionIndicator.not());
        exportTemplateMenuItem.disableProperty().bind(selectionIndicator.not());
        selectTemplateButton.disableProperty().bind(selectionIndicator.not());

        templateName.setCellValueFactory(fpt -> fpt.getValue().fpNameProperty());

        uavType.setCellValueFactory(fpt -> fpt.getValue().uavProperty());

        cameraName.setCellValueFactory(fpt -> fpt.getValue().cameraProperty());

        lensName.setCellValueFactory(fpt -> fpt.getValue().lensProperty());

        aois.setCellValueFactory(fpt -> fpt.getValue().aoisProperty());

        action.setCellFactory(
            column ->
                new TableCell<FlightPlanTemplateManagementItem, Button>() {
                    @Override
                    protected void updateItem(Button item, boolean empty) {
                        super.updateItem(item, empty);
                        FlightPlanTemplateManagementItem rowItem = null;
                        if (item == null || empty) {
                            TableRow tmp = getTableRow();
                            if (tmp != null) {
                                rowItem = (FlightPlanTemplateManagementItem)tmp.getItem();
                            }
                        }
                        getStyleClass().add("transparent-icon-button");
                        getStyleClass().remove("icon-revert");
                        getStyleClass().remove("icon-trash");
                        if (rowItem != null) {
                            setText("");
                            if (rowItem.getBuildIn()) {
                                getStyleClass().add("icon-revert");
                                setOnMouseClicked(event -> doRevertTemplateAction(getTableRow().getIndex()));
                            } else {
                                getStyleClass().add("icon-trash");
                                setOnMouseClicked(event -> doDeleteTemplateAction(getTableRow().getIndex()));
                            }
                        }
                    }
                });


        TableView.TableViewSelectionModel<FlightPlanTemplateManagementItem> selectionModel =
            templatesTable.getSelectionModel();
        selectionModel
            .selectedItemProperty()
            .addListener(
                (obs, oldSelection, newSelection) -> selectionIndicator.set(selectionModel.getSelectedIndex() >= 0));
        templatesTable.setItems(viewModel.getTemplates());
    }

    @Override
    public ReadOnlyStringProperty titleProperty() {
        return new ReadOnlyStringWrapper(languageHelper.getString(TEMPLATE_MANAGMENT_CAPTION_KEY));
    }

    @Override
    protected Parent getRootNode() {
        return layoutRoot;
    }

    @Override
    protected FlightPlanTemplateManagementViewModel getViewModel() {
        return viewModel;
    }

    private void doDeleteTemplateAction(int index) {
        Stage window = (Stage)layoutRoot.getScene().getWindow();
        window.setAlwaysOnTop(false);
        boolean importAnswer =
            dialogService.requestConfirmation(
                languageHelper.getString(
                    "com.intel.missioncontrol.ui.planning.FlightPlanTemplateManagerView.dialog.delete.title"),
                languageHelper.getString(
                    "com.intel.missioncontrol.ui.planning.FlightPlanTemplateManagerView.dialog.delete.question",
                    templatesTable.getItems().get(index).getFpName()));

        if (importAnswer && viewModel.deleteTemplate(index)) {
            templatesTable.getSelectionModel().clearSelection();
        }

        window.setAlwaysOnTop(true);
    }

    private void doRevertTemplateAction(int index) {
        viewModel.revertTemplate(index);
    }

    public void doEditTemplateAction(ActionEvent actionEvent) {
        FlightPlanTemplateManagementItem selectedItem = getSelectedTemplate();
        viewModel.editTemplate(selectedItem);
        doCloseAction(actionEvent);
    }

    public void doDuplicateTemplateAction(ActionEvent actionEvent) {
        FlightPlanTemplateManagementItem selectedItem = getSelectedTemplate();
        viewModel.duplicateTemplate(selectedItem);
    }

    public void doExportTemplateAction(ActionEvent actionEvent) {
        FlightPlanTemplateManagementItem selectedItem = getSelectedTemplate();
        doExportTemplates(Arrays.asList(selectedItem));
    }

    public void doExportAllTemplatesAction(ActionEvent actionEvent) {
        doExportTemplates(templatesTable.getItems());
    }

    private void doExportTemplates(Collection<FlightPlanTemplateManagementItem> items) {
        final File destination = openFlightPlanTemplateSaver(new File(System.getProperty("user.home")));
        if (destination != null) {
            viewModel.exportTemplate(
                destination,
                items.stream().map(FlightPlanTemplateManagementItem::getFpTemplate).collect(Collectors.toList()));
        }
    }

    private List<File> openFlightPlanTemplateChooser(File defaultFolder) {
        FileChooser fpFileChooser = new FileChooser();
        fpFileChooser
            .getExtensionFilters()
            .add(
                new FileChooser.ExtensionFilter(
                    languageHelper.getString(
                        "com.intel.missioncontrol.ui.SidePaneView.dialog.openFlightPlan.extension"),
                    "*.fml"));
        fpFileChooser.setTitle(
            languageHelper.getString(
                "com.intel.missioncontrol.ui.planning.FlightPlanTemplateManagerView.dialog.import.caption"));
        fpFileChooser.setInitialDirectory(defaultFolder);
        return fpFileChooser.showOpenMultipleDialog(layoutRoot.getScene().getWindow());
    }

    private File openFlightPlanTemplateSaver(File defaultFolder) {
        DirectoryChooser fpFileChooser = new DirectoryChooser();
        fpFileChooser.setTitle(
            languageHelper.getString(
                "com.intel.missioncontrol.ui.planning.FlightPlanTemplateManagerView.dialog.export.caption"));
        fpFileChooser.setInitialDirectory(defaultFolder);
        return fpFileChooser.showDialog(layoutRoot.getScene().getWindow());
    }

    public void doImportTemplateAction(ActionEvent actionEvent) {
        List<File> selectedTemplatesFiles = openFlightPlanTemplateChooser(new File(System.getProperty("user.home")));
        if (selectedTemplatesFiles == null || selectedTemplatesFiles.isEmpty()) {
            return;
        }

        if (viewModel.alreadyHasSuchFiles(selectedTemplatesFiles)) {
            dialogService.showWarningMessage(
                languageHelper.getString(
                    "com.intel.missioncontrol.ui.planning.FlightPlanTemplateManagerView.dialog.import.warning.title"),
                languageHelper.getString(
                    "com.intel.missioncontrol.ui.planning.FlightPlanTemplateManagerView.dialog.import.warning"));
            return;
        }

        selectedTemplatesFiles.forEach(viewModel::importTemplate);
    }

    public void doSelectTemplateAction(ActionEvent actionEvent) {
        FlightPlanTemplateManagementItem selectedItem = getSelectedTemplate();
        if (viewModel.useTemplate(selectedItem)) {
            doCloseAction(actionEvent);
        }
    }

    public FlightPlanTemplateManagementItem getSelectedTemplate() {
        int index = templatesTable.getSelectionModel().getSelectedIndex();
        return viewModel.getTemplates().get(index);
    }

    public void doCloseAction(ActionEvent actionEvent) {
        @SuppressWarnings("unchecked")
        Control sourceControl = (Control)actionEvent.getSource();
        Stage stage = (Stage)sourceControl.getScene().getWindow();
        stage.close();
    }

    public void setCurrentTemplate(FlightPlanTemplate currentTemplate) {
        templatesTable
            .getSelectionModel()
            .select(
                IntStream.range(0, templatesTable.getItems().size())
                    .filter(i -> templatesTable.getItems().get(i).getFpTemplate().equals(currentTemplate))
                    .findFirst()
                    .orElse(0));
    }
}
