/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight.fly.checks;

import com.google.inject.Inject;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.drone.FlightSegment;
import com.intel.missioncontrol.drone.IDrone;
import com.intel.missioncontrol.drone.validation.IFlightValidationService;
import com.intel.missioncontrol.hardware.IHardwareConfiguration;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.mission.FlightPlan;
import com.intel.missioncontrol.mission.Mission;
import com.intel.missioncontrol.ui.ViewModelBase;
import com.intel.missioncontrol.ui.common.CheckListUtils;
import com.intel.missioncontrol.ui.dialogs.IDialogService;
import com.intel.missioncontrol.ui.sidepane.flight.FlightScope;
import com.intel.missioncontrol.ui.sidepane.flight.fly.checklist.Checklist;
import com.intel.missioncontrol.ui.sidepane.flight.fly.checklist.ChecklistItem;
import com.intel.missioncontrol.ui.sidepane.flight.fly.checklist.ChecklistScope;
import com.intel.missioncontrol.ui.sidepane.flight.fly.checklist.ChecklistViewModel;
import com.intel.missioncontrol.ui.sidepane.flight.fly.checks.automatic.AutoCheckItemViewModel;
import com.intel.missioncontrol.ui.sidepane.flight.fly.checks.automatic.AutomaticChecksDialogViewModel;
import com.intel.missioncontrol.ui.sidepane.flight.fly.checks.preflight.PreflightChecklistDialogViewModel;
import com.intel.missioncontrol.ui.validation.IValidationService;
import com.intel.missioncontrol.ui.validation.ResolvableValidationMessage;
import de.saxsys.mvvmfx.InjectScope;
import de.saxsys.mvvmfx.ScopeProvider;
import de.saxsys.mvvmfx.utils.commands.Command;
import de.saxsys.mvvmfx.utils.commands.DelegateCommand;
import eu.mavinci.core.plane.AirplaneType;
import eu.mavinci.desktop.main.debug.Debug;
import gov.nasa.worldwind.geom.Position;
import java.util.HashMap;
import java.util.logging.Level;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import org.asyncfx.beans.property.AsyncObjectProperty;
import org.asyncfx.beans.property.PropertyPath;
import org.asyncfx.beans.property.PropertyPathStore;
import org.asyncfx.beans.property.ReadOnlyAsyncObjectProperty;
import org.asyncfx.beans.property.SimpleAsyncObjectProperty;
import org.asyncfx.beans.property.UIAsyncObjectProperty;
import org.asyncfx.concurrent.Dispatcher;

@ScopeProvider(scopes = ChecklistScope.class)
public class SafetyChecksViewModel extends ViewModelBase {
    @InjectScope
    private FlightScope flightScope;

    private ObjectProperty<AlertType> autoCheckAlertType = new SimpleObjectProperty<>();
    private StringProperty autoCheckStatusString = new SimpleStringProperty();
    private ObjectProperty<AlertType> manualCheckImageType = new SimpleObjectProperty<>();
    private StringProperty manualCheckStatus = new SimpleStringProperty();
    private IntegerProperty checkedCount = new SimpleIntegerProperty(0);
    private IntegerProperty totalCount = new SimpleIntegerProperty(0);

    private ObjectProperty<AlertType> takeoffPositionImageTypeProperty = new SimpleObjectProperty<>();
    private StringProperty takeoffPositionImageStatusString = new SimpleStringProperty();

    private HashMap<AirplaneType, ListProperty<ChecklistViewModel>> planeManualChecklist = new HashMap<>();
    private AirplaneType currentAirplaneType;

    private final ILanguageHelper languageHelper;
    private final IValidationService validationService;
    private final IFlightValidationService flightValidationService;

    private final AsyncObjectProperty<IDrone> drone = new SimpleAsyncObjectProperty<>(this);
    private final ReadOnlyAsyncObjectProperty<IHardwareConfiguration> hardwareConfiguration;
    private final UIAsyncObjectProperty<FlightPlan> activeFlightPlan = new UIAsyncObjectProperty<>(this);
    private final UIAsyncObjectProperty<FlightPlan> selectedFlightPlan = new UIAsyncObjectProperty<>(this);
    private final UIAsyncObjectProperty<Position> position = new UIAsyncObjectProperty<>(this);

    private final UIAsyncObjectProperty<FlightSegment> flightSegment = new UIAsyncObjectProperty<>(this);

    @SuppressWarnings("FieldCanBeLocal")
    private final PropertyPathStore propertyPathStore = new PropertyPathStore();

    @InjectScope
    private ChecklistScope checklistScope;

    @Inject
    public SafetyChecksViewModel(
            ILanguageHelper languageHelper,
            IDialogService dialogService,
            IValidationService validationService,
            IFlightValidationService flightValidationService,
            IApplicationContext applicationContext) {
        this.languageHelper = languageHelper;
        this.validationService = validationService;
        this.flightValidationService = flightValidationService;

        AsyncObjectProperty<Mission> mission = new SimpleAsyncObjectProperty<>(this);

        mission.bind(applicationContext.currentLegacyMissionProperty());
        selectedFlightPlan.bindBidirectional( // TODO decouple
            propertyPathStore.from(mission).selectObject(Mission::currentFlightPlanProperty));
        hardwareConfiguration = PropertyPath.from(drone).selectReadOnlyAsyncObject(IDrone::hardwareConfigurationProperty);

        showAutomaticChecksDialog =
            new DelegateCommand(
                () -> dialogService.requestDialogAsync(this, AutomaticChecksDialogViewModel.class, true));

        showFlightCheckListChecksDialog =
            new DelegateCommand(
                () -> dialogService.requestDialogAsync(this, PreflightChecklistDialogViewModel.class, true));

        takeoffPositionImageTypeProperty.set(AlertType.WARNING);
        takeoffPositionImageStatusString.set(
            languageHelper.getString(SafetyChecksViewModel.class, "takeoffPositionImageStatus.notConnected"));
    }

    public void initializeViewModel() {
        super.initializeViewModel();

        drone.bind(flightScope.currentDroneProperty());

        // automatic checks:
        AutoCheckItemViewModel combinedAutoCheckItemViewModel =
            AutoCheckItemViewModel.fromValidator(flightValidationService);
        autoCheckAlertType.bind(combinedAutoCheckItemViewModel.alertTypeProperty());
        autoCheckStatusString.bind(combinedAutoCheckItemViewModel.messageStringProperty());

        // manual checks
        manualCheckImageType.set(AlertType.WARNING);
        manualCheckStatus.set(
            String.format(languageHelper.getString(SafetyChecksViewModel.class, "flightChecklist"), 0, 0));
        checkedCount.bind(checklistScope.checkedCountProperty());
        totalCount.bind(checklistScope.totalCountProperty());
        checkedCount.addListener((observable, oldValue, newvalue) -> refreshChecklistMessage());
        totalCount.addListener((observable, oldValue, newvalue) -> refreshChecklistMessage());

        flightScope.selectedFlightPlanProperty().bind(selectedFlightPlan);
        flightSegment.bind(flightScope.flightSegmentProperty());

        activeFlightPlan.bind(PropertyPath.from(drone).selectReadOnlyAsyncObject(IDrone::activeFlightPlanProperty));
        position.bind(PropertyPath.from(drone).selectReadOnlyAsyncObject(IDrone::positionProperty));

        takeoffPositionImageTypeProperty.set(AlertType.WARNING);

        takeoffPositionImageStatusString.set(
            languageHelper.getString(SafetyChecksViewModel.class, "takeoffPositionImageStatus.notConnected"));

        initPlaneChecklists();
        hardwareConfiguration.addListener(
            (observable, oldValue, newValue) -> {
                if (newValue != null) {
                    currentAirplaneType = newValue.getPlatformDescription().getAirplaneType();
                    checklistScope.currentChecklistProperty().setValue(null);
                    if (planeManualChecklist.containsKey(currentAirplaneType)) {
                        checklistScope
                            .currentChecklistProperty()
                            .setValue(planeManualChecklist.get(currentAirplaneType));
                        int count = 0;
                        for (ChecklistViewModel checklist : checklistScope.currentChecklistProperty().get()) {
                            count += checklist.getItems().size();
                        }

                        checklistScope.totalCountProperty().setValue(count);
                    }
                } else {
                    currentAirplaneType = null;
                    checklistScope.currentChecklistProperty().setValue(null);
                    checklistScope.totalCountProperty().setValue(0);
                }

                refreshChecklistMessage();
            });

        getUpdateTakeoffPositionImageStatusString =
            new DelegateCommand(
                () -> {
                    if (flightSegment.get() == null) {
                        takeoffPositionImageStatusString.set(
                            languageHelper.getString(
                                SafetyChecksViewModel.class, "takeoffPositionImageStatus.notConnected"));
                    } else if (takeoffPositionImageTypeProperty.get().equals(AlertType.COMPLETED)) {
                        takeoffPositionImageStatusString.set(
                            languageHelper.getString(SafetyChecksViewModel.class, "takeoffPositionImageStatus.ok"));
                    } else {
                        takeoffPositionImageStatusString.set(
                            languageHelper.getString(
                                SafetyChecksViewModel.class, "takeoffPositionImageStatus.default"));
                    }
                });
    }

    ReadOnlyObjectProperty<AlertType> autoCheckAlertTypeProperty() {
        return autoCheckAlertType;
    }

    ReadOnlyStringProperty autoCheckStatusStringProperty() {
        return autoCheckStatusString;
    }

    ReadOnlyObjectProperty<AlertType> manualCheckImageTypeProperty() {
        return manualCheckImageType;
    }

    ReadOnlyStringProperty manualCheckStatusProperty() {
        return manualCheckStatus;
    }

    ReadOnlyStringProperty takeoffPositionStatusProperty() {
        return takeoffPositionImageStatusString;
    }

    AsyncObjectProperty<FlightSegment> flightSegmentProperty() {
        return flightSegment;
    }

    private final Command showAutomaticChecksDialog;

    private final Command showFlightCheckListChecksDialog;

    Command getShowAutomaticChecksDialogCommand() {
        return showAutomaticChecksDialog;
    }

    Command getShowFlightCheckListChecksDialogCommand() {
        return showFlightCheckListChecksDialog;
    }

    public ReadOnlyListProperty<ResolvableValidationMessage> validationMessagesProperty() {
        return validationService.flightValidationMessagesProperty();
    }

    private void refreshChecklistMessage() {
        Dispatcher dispatcher = Dispatcher.platform();
        dispatcher.run(
            () -> {
                try {
                    if (currentAirplaneType != null && planeManualChecklist.containsKey(currentAirplaneType)) {
                        manualCheckStatus.set(
                            String.format(
                                languageHelper.getString(SafetyChecksViewModel.class, "flightChecklist"),
                                checkedCount.get(),
                                totalCount.get()));
                        // showChecklistButton.setDisable(false);

                        AlertType imageType;
                        if (checkedCount.get() != totalCount.get()) {
                            imageType = AlertType.WARNING;
                        } else {
                            imageType = AlertType.COMPLETED;
                        }

                        manualCheckImageType.set(imageType);
                    } else {
                        manualCheckStatus.set(
                            String.format(
                                languageHelper.getString(SafetyChecksViewModel.class, "flightChecklist"), 0, 0));
                        // showChecklistButton.setDisable(true);
                        manualCheckImageType.set(AlertType.NONE);
                    }
                } catch (Exception e) {
                    Debug.getLog().log(Level.WARNING, "ErrorCallingHandler for flightChecklist", e);
                }
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

            planeManualChecklist.put(checklist.getAirplaneType(), checklists);
        }
    }

    private void fillTextByKeys(ChecklistItem item) {
        item.setTitle(languageHelper.getString(item.getTitle()));
        for (int i = 0; i < item.getItems().length; i++) {
            item.getItems()[i] = languageHelper.getString(item.getItems()[i]);
        }
    }

    ReadOnlyProperty<FlightPlan> activeFlightplanProperty() {
        return activeFlightPlan;
    }

    // TODO
    public Command getUpdateTakeoffPositionImageStatusString;

    private final Command updateTakeoffPosition =
        new DelegateCommand(
            () -> {
                if (flightSegment.get() != null) {
                    FlightPlan fp = selectedFlightPlan.get();

                    // Use lat/lon from UAV and elevation offset from ground from flightplan
                    double takeoffElevation =
                        fp.takeoffElevationProperty().get().convertTo(Unit.METER).getValue().doubleValue();
                    Position pos =
                        new Position(position.get().getLatitude(), position.get().getLongitude(), takeoffElevation);

                    fp.takeoffAutoProperty().set(false);
                    fp.takeoffPositionProperty().setValue(pos);

                    takeoffPositionImageTypeProperty.set(AlertType.COMPLETED);
                }

                getUpdateTakeoffPositionImageStatusString.execute();
            },
            position.isNotNull().and(selectedFlightPlan.isNotNull()).and(activeFlightPlan.isNull()));

    Command getUpdateTakeoffPositionCommand() {
        return updateTakeoffPosition;
    }

    public ObjectProperty<AlertType> takeoffPositionImageTypeProperty() {
        return takeoffPositionImageTypeProperty;
    }

}
