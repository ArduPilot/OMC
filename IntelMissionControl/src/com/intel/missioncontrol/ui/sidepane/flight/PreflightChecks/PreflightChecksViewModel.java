/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight.PreflightChecks;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.inject.Inject;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.ui.ViewModelBase;
import com.intel.missioncontrol.ui.commands.DelegateCommand;
import com.intel.missioncontrol.ui.commands.ICommand;
import com.intel.missioncontrol.ui.common.CheckListUtils;
import com.intel.missioncontrol.ui.dialogs.IDialogService;
import com.intel.missioncontrol.ui.dialogs.preflightchecks.AutomaticChecksDialogViewModel;
import com.intel.missioncontrol.ui.dialogs.preflightchecks.ManualChecklistDialogViewModel;
import com.intel.missioncontrol.ui.dialogs.preflightchecks.SetupEmergencyProceduresViewModel;
import com.intel.missioncontrol.ui.navbar.connection.UavConnectionScope;
import com.intel.missioncontrol.ui.navbar.connection.UnmannedAerialVehicle;
import com.intel.missioncontrol.ui.sidepane.flight.FlightIdlWorkflowScope;
import com.intel.missioncontrol.ui.sidepane.flight.checklist.Checklist;
import com.intel.missioncontrol.ui.sidepane.flight.checklist.ChecklistItem;
import com.intel.missioncontrol.ui.sidepane.flight.checklist.ChecklistScope;
import com.intel.missioncontrol.ui.sidepane.flight.checklist.ChecklistViewModel;
import com.intel.missioncontrol.ui.validation.IValidationService;
import com.intel.missioncontrol.ui.validation.ResolvableValidationMessage;
import de.saxsys.mvvmfx.InjectScope;
import de.saxsys.mvvmfx.ScopeProvider;
import eu.mavinci.core.plane.AirplaneType;
import java.util.HashMap;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;

@ScopeProvider(scopes = ChecklistScope.class)
public class PreflightChecksViewModel extends ViewModelBase {

    private ObjectProperty<AlertImageType> autoCheckImageType = new SimpleObjectProperty<>();
    private StringProperty autoCheckStatus = new SimpleStringProperty();
    private ObjectProperty<AlertImageType> manualCheckImageType = new SimpleObjectProperty<>();
    private StringProperty manualCheckStatus = new SimpleStringProperty();
    private StringProperty motorsOnMessage = new SimpleStringProperty();
    private BooleanProperty areMotorsOn = new SimpleBooleanProperty(false);
    private StringProperty motorsButtonString = new SimpleStringProperty();
    private IntegerProperty checkedCount = new SimpleIntegerProperty(0);
    private IntegerProperty totalCount = new SimpleIntegerProperty(0);

    private HashMap<AirplaneType, ListProperty<ChecklistViewModel>> planeManualChecklist = new HashMap<>();
    private AirplaneType currentAirplaneType;

    private ILanguageHelper languageHelper;
    private IDialogService dialogService;
    private IValidationService validationService;
    private IApplicationContext applicationContext;

    @InjectScope
    private UavConnectionScope uavConnectionScope;

    @InjectScope
    private FlightIdlWorkflowScope flightIdlWorkflowScope;

    @InjectScope
    private ChecklistScope checklistScope;

    @Inject
    public PreflightChecksViewModel(
            ILanguageHelper languageHelper,
            IDialogService dialogService,
            IValidationService validationService,
            IApplicationContext applicationContext) {
        this.languageHelper = languageHelper;
        this.dialogService = dialogService;
        this.validationService = validationService;
        this.applicationContext = applicationContext;
    }

    public void initializeViewModel() {
        super.initializeViewModel();

        // auto checks
        autoCheckImageType.set(AlertImageType.COMPLETED);
        autoCheckStatus.set(
            languageHelper.getString(
                "com.intel.missioncontrol.ui.sidepane.flight.preflightChecks.autoChecksAllPassed"));

        // manual checks
        manualCheckImageType.set(AlertImageType.COMPLETED);
        manualCheckStatus.set(
            String.format(
                languageHelper.getString("com.intel.missioncontrol.ui.sidepane.flight.preflightChecks.flightchecklist"),
                0,
                0));
        checkedCount.bind(checklistScope.checkedCountProperty());
        totalCount.bind(checklistScope.totalCountProperty());
        checkedCount.addListener((observable, oldValue, newvalue) -> refreshChecklistMessage());
        totalCount.addListener((observable, oldValue, newvalue) -> refreshChecklistMessage());
        initPlaneChecklists();
        selectedUavProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null && newValue.model != null) {
                        currentAirplaneType = newValue.model;
                        checklistScope.currentChecklistProperty().setValue(null);
                        if (planeManualChecklist.containsKey(newValue.model)) {
                            checklistScope
                                .currentChecklistProperty()
                                .setValue(planeManualChecklist.get(newValue.model));
                        }
                    } else {
                        currentAirplaneType = null;
                        checklistScope.currentChecklistProperty().setValue(null);
                    }

                    refreshChecklistMessage();
                });

        // motors
        motorsOnMessage.setValue(
                languageHelper.getString("com.intel.missioncontrol.ui.sidepane.flight.preflightChecks.motorsNotRunning"));
        areMotorsOn.setValue(false);
        motorsButtonString.setValue(
            languageHelper.getString("com.intel.missioncontrol.ui.sidepane.flight.preflightChecks.startMotors"));

        flightIdlWorkflowScope.currentIdlWorkflowStateProperty().addListener((observable, oldValue, newValue) -> {
            switch (newValue) {
                case MOTORS_OFF:
                    motorsOnMessage.setValue(
                            languageHelper.getString("com.intel.missioncontrol.ui.sidepane.flight.preflightChecks.motorsNotRunning"));
                    break;
                    default:
                        motorsOnMessage.setValue(
                                languageHelper.getString("com.intel.missioncontrol.ui.sidepane.flight.preflightChecks.motorsRunning"));
                        break;

            }
        });

    }

    public ReadOnlyObjectProperty<AlertImageType> autoCheckImageTypeProperty() {
        return autoCheckImageType;
    }

    public ReadOnlyStringProperty autoCheckStatusProperty() {
        return autoCheckStatus;
    }

    public ReadOnlyObjectProperty<AlertImageType> manualCheckImageTypeProperty() {
        return manualCheckImageType;
    }

    public ReadOnlyStringProperty manualCheckStatusProperty() {
        return manualCheckStatus;
    }

    public BooleanProperty areMotorsOnProperty() {
        return areMotorsOn;
    }

    public ReadOnlyStringProperty motorsOnMessageProperty() {
        return motorsOnMessage;
    }

    public ReadOnlyStringProperty motorsButtonStringProperty() {
        return motorsButtonString;
    }

    private final ICommand turnOnMotorsCommand =
        new DelegateCommand(
            () -> {
                // TODO: to call backend method to turn on Motors
                flightIdlWorkflowScope.currentIdlWorkflowStateProperty().set(IdlWorkflowState.MOTORS_ON);
            });

    private final ICommand turnOffMotorsCommand =
        new DelegateCommand(
            () -> {
                // TODO: to call backend method to turn off Motors
                flightIdlWorkflowScope.currentIdlWorkflowStateProperty().set(IdlWorkflowState.MOTORS_OFF);
            });

    private final ICommand showEmergencyEditDialog =
        new DelegateCommand(
            () ->
                Futures.addCallback(
                    dialogService.requestDialog(this, SetupEmergencyProceduresViewModel.class, true),
                    new FutureCallback<>() {
                        @Override
                        public void onSuccess(SetupEmergencyProceduresViewModel automaticChecksDialogViewModel) {}

                        @Override
                        public void onFailure(Throwable throwable) {}

                    }));

    private final ICommand showAutomaticChecksDialog =
        new DelegateCommand(
            () ->
                Futures.addCallback(
                    dialogService.requestDialog(this, AutomaticChecksDialogViewModel.class, true),
                    new FutureCallback<>() {
                        @Override
                        public void onSuccess(AutomaticChecksDialogViewModel automaticChecksDialogViewModel) {}

                        @Override
                        public void onFailure(Throwable throwable) {}

                    }));
    private final ICommand showFlightCheckListChecksDialog =
        new DelegateCommand(
            () ->
                Futures.addCallback(
                    dialogService.requestDialog(this, ManualChecklistDialogViewModel.class, true),
                    new FutureCallback<>() {
                        @Override
                        public void onSuccess(ManualChecklistDialogViewModel manualChecksDialogViewModel) {}

                        @Override
                        public void onFailure(Throwable throwable) {}

                    }));

    public ICommand getTurnOnMotorsCommand() {
        return turnOnMotorsCommand;
    }

    public ICommand getTurnOffMotorsCommand() {
        return turnOffMotorsCommand;
    }

    public ICommand getEmergencyEditDialogCommand() {
        return showEmergencyEditDialog;
    }

    public ICommand getShowAutomaticChecksDialogCommand() {
        return showAutomaticChecksDialog;
    }

    public ICommand getShowFlightCheckListChecksDialogCommand() {
        return showFlightCheckListChecksDialog;
    }

    public ObjectProperty<UnmannedAerialVehicle> selectedUavProperty() {
        return uavConnectionScope.selectedUavProperty();
    }

    public ReadOnlyListProperty<ResolvableValidationMessage> validationMessagesProperty() {
        return validationService.flightValidationMessagesProperty();
    }

    private void refreshChecklistMessage() {
        if (currentAirplaneType != null && planeManualChecklist.containsKey(currentAirplaneType)) {
            manualCheckStatus.set(
                String.format(
                    languageHelper.getString(
                        "com.intel.missioncontrol.ui.sidepane.flight.preflightChecks.flightchecklist"),
                    checkedCount.get(),
                    totalCount.get()));
            // showChecklistButton.setDisable(false);

            AlertImageType imageType;
            if (checkedCount.get() != totalCount.get()) {
                imageType = AlertImageType.ALERT;
            } else {
                imageType = AlertImageType.COMPLETED;
            }

            manualCheckImageType.set(imageType);
        } else {
            manualCheckStatus.set(
                String.format(
                    languageHelper.getString(
                        "com.intel.missioncontrol.ui.sidepane.flight.preflightChecks.flightchecklist"),
                    0,
                    0));
            // showChecklistButton.setDisable(true);
            manualCheckImageType.set(AlertImageType.NONE);
        }
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

            planeManualChecklist.put(checklist.getAirplaneType(), checklists);
        }
    }

    private void fillTextByKeys(ChecklistItem item) {
        item.setTitle(languageHelper.getString(item.getTitle()));
        for (int i = 0; i < item.getItems().length; i++) {
            item.getItems()[i] = languageHelper.getString(item.getItems()[i]);
        }
    }
}
