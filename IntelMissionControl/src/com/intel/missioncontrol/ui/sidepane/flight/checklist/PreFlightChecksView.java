/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight.checklist;

import com.intel.missioncontrol.helper.ScaleHelper;
import com.intel.missioncontrol.ui.ViewBase;
import com.intel.missioncontrol.ui.common.CheckListUtils;
import com.intel.missioncontrol.ui.validation.ResolvableValidationMessage;
import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.ViewTuple;
import eu.mavinci.core.plane.AirplaneType;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;

public class PreFlightChecksView extends ViewBase<PreFlightChecksViewModel> {

    private final String AUTOCHECKS_ALL_SUCCEEDED = "com.intel.missioncontrol.ui.flight.autochecks.allSucceeded";
    private final String FLIGHT_CHECKLIST_TEMPLATE = "Flight checklist: %s of %s checked";
    private final String AUTOMATIC_CHECKS_TEMPLATE = "Automatic checks: %s failed";

    private static final String CHECKLIST_ADVANCED_PARAMETERS_VIEW_TITLE =
            "com.intel.missioncontrol.ui.flight.checklist.popupTitle";

    private static final double CHECKLIST_PARAMETERS_STAGE_HEIGHT = ScaleHelper.emsToPixels(40);
    private static final double CHECKLIST_PARAMETERS_STAGE_WIDTH = ScaleHelper.emsToPixels(34);

    private static final String ICON_COMPLETE = "/com/intel/missioncontrol/icons/icon_complete(fill=theme-green).svg";
    private static final String ICON_ALERT = "/com/intel/missioncontrol/icons/icon_warning(fill=theme-warning-color).svg";

    private final IntegerProperty checkedCount = new SimpleIntegerProperty(0);
    private final IntegerProperty totalCount = new SimpleIntegerProperty(0);

    @FXML
    private Pane rootNode;

    @FXML
    public Label preFlightValidationStatus;

    @FXML
    public Label checklistStatus;

    @FXML
    public Button showChecklistButton;

    @FXML
    private VBox formsContainer;

    @FXML
    public ImageView imageStatus;

    @FXML
    public ImageView checklistImageStatus;

    HashMap<AirplaneType, ListProperty<ChecklistViewModel>> planeVboxes = new HashMap<>();

    AirplaneType currentAirplaneType;

    private Scene scene;

    private Button btnDone;

    private ChecklistScope checklistScope;

    @InjectViewModel
    private PreFlightChecksViewModel viewModel;

    private Stage checklistStage;
    private Image completeIcon;
    private Image alertIcon;

    @Override
    public void initializeView() {
        super.initializeView();

        checklistScope = new ChecklistScope();
        checkedCount.bind(checklistScope.checkedCountProperty());
        totalCount.bind(checklistScope.totalCountProperty());
        checkedCount.addListener((observable, oldValue, newvalue) -> refreshChecklistMessage());
        totalCount.addListener((observable, oldValue, newvalue) -> refreshChecklistMessage());

        //initPopupWindow();

        showChecklistButton.setDisable(true);
        preFlightValidationStatus.setText(viewModel.getTextByKey(AUTOCHECKS_ALL_SUCCEEDED));

        checklistImageStatus.imageProperty().setValue(null);
        imageStatus.imageProperty().setValue(getCompleteIcon());

        viewModel
                .validationMessagesProperty()
                .addListener(
                        (ListChangeListener<ResolvableValidationMessage>)
                                l -> {
                                    int size = l.getList().size();
                                    if (size > 0) {
                                        preFlightValidationStatus.setText(
                                                String.format(AUTOMATIC_CHECKS_TEMPLATE, l.getList().size()));
                                    } else {
                                        preFlightValidationStatus.setText(viewModel.getTextByKey(AUTOCHECKS_ALL_SUCCEEDED));
                                    }

                                    updateAlertIcon(l.getList().size());
                                });

        initPlaneChecklists();
        checklistImageStatus.setFitHeight(ScaleHelper.emsToPixels(1.1));
        checklistImageStatus.setFitWidth(ScaleHelper.emsToPixels(1.1));
        imageStatus.setFitHeight(ScaleHelper.emsToPixels(1.1));
        imageStatus.setFitWidth(ScaleHelper.emsToPixels(1.1));

        viewModel
                .selectedUavProperty()
                .addListener(
                        (observable, oldValue, newValue) -> {
                            if (newValue != null && newValue.model != null) {
                                currentAirplaneType = newValue.model;
                                checklistScope.currentChecklistProperty().setValue(null);
                                if (planeVboxes.containsKey(newValue.model)) {
                                    checklistScope.currentChecklistProperty().setValue(planeVboxes.get(newValue.model));
                                }
                            } else {
                                currentAirplaneType = null;
                                checklistScope.currentChecklistProperty().setValue(null);
                            }
                            refreshChecklistMessage();
                        });

    }

    private void initPlaneChecklists() {
        Checklist[] checklistItems = CheckListUtils.readAllCheckLists();

        if (checklistItems == null) {
            return;
        }

        for (Checklist checklist : checklistItems) {
            ListProperty<ChecklistViewModel> checklists = new SimpleListProperty<>(FXCollections.observableArrayList());
            for (ChecklistItem item : checklist.getChecklistItem()) {
                fillTextByKeys(item);
                checklists.add(new ChecklistViewModel(item));
            }
            planeVboxes.put(checklist.getAirplaneType(), checklists);
        }
    }

    @Override
    public Pane getRootNode() {
        return rootNode;
    }

    @Override
    public PreFlightChecksViewModel getViewModel() {
        return viewModel;
    }

    private void fillTextByKeys(ChecklistItem item) {
        item.setTitle(viewModel.getTextByKey(item.getTitle()));
        for (int i = 0; i < item.getItems().length; i++) {
            item.getItems()[i] = viewModel.getTextByKey(item.getItems()[i]);
        }
    }

    private void updateAlertIcon(int size) {
        Image image = getAlertIconUrl(size);
        imageStatus.imageProperty().setValue(image);
    }

    private void refreshChecklistMessage() {
        if (currentAirplaneType != null && planeVboxes.containsKey(currentAirplaneType)) {
            checklistStatus.setText(String.format(FLIGHT_CHECKLIST_TEMPLATE, checkedCount.get(), totalCount.get()));
            showChecklistButton.setDisable(false);

            Image image;
            if (checkedCount.get() != totalCount.get()) {
                image = getAlertIcon();
            } else {
                image = getCompleteIcon();
            }

            checklistImageStatus.imageProperty().setValue(image);
        } else {
            checklistStatus.setText(String.format(FLIGHT_CHECKLIST_TEMPLATE, 0, 0));
            showChecklistButton.setDisable(true);
            checklistImageStatus.imageProperty().setValue(null);
        }
    }

    private Image getAlertIconUrl(int size) {
        if (size == 0) {
            return getCompleteIcon();
        } else {
            return getAlertIcon();
        }
    }

    private Image getCompleteIcon() {
        if (completeIcon == null) {
            completeIcon = new Image(ICON_COMPLETE);
        }

        return completeIcon;
    }

    private Image getAlertIcon() {
        if (alertIcon == null) {
            alertIcon = new Image(ICON_ALERT);
        }

        return alertIcon;
    }

    public void showPopupChecklist(ActionEvent actionEvent) {

        if (scene == null) {
            return;
        }

        if (checklistStage == null) {
            try {
                checklistStage = new Stage();
                String title = viewModel.getTextByKey(CHECKLIST_ADVANCED_PARAMETERS_VIEW_TITLE);
                checklistStage.setTitle(String.format("\n   %s", title));
                checklistStage.setScene(scene);
                checklistStage.setAlwaysOnTop(true);
                checklistStage.initOwner(formsContainer.getScene().getWindow());
                checklistStage.initModality(Modality.APPLICATION_MODAL);

                checklistStage.setResizable(false);
                checklistStage.initStyle(StageStyle.UTILITY);

                // window closed by [x]
                checklistStage.setOnCloseRequest(
                        closeEvent -> {
                            checklistStage.hide();
                        });

                // window closed by stage.close()
                checklistStage.setOnHidden(
                        closeEvent -> {
                            checklistStage.hide();
                        });
                checklistStage.show();
            } catch (Exception ex) {
                checklistStage = null;
                ex.printStackTrace();
            }
        } else {
            checklistStage.show();
        }
    }

}
