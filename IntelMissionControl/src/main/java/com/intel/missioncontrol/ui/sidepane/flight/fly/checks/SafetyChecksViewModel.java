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
import com.intel.missioncontrol.map.worldwind.IWWGlobes;
import com.intel.missioncontrol.mission.FlightPlan;
import com.intel.missioncontrol.mission.Mission;
import com.intel.missioncontrol.ui.ViewModelBase;
import com.intel.missioncontrol.ui.common.CheckListUtils;
import com.intel.missioncontrol.ui.dialogs.IDialogService;
import com.intel.missioncontrol.ui.sidepane.flight.FlightScope;
import com.intel.missioncontrol.ui.sidepane.flight.fly.checklist.Checklist;
import com.intel.missioncontrol.ui.sidepane.flight.fly.checklist.ChecklistItem;
import com.intel.missioncontrol.ui.sidepane.flight.fly.checklist.ChecklistItemViewModel;
import com.intel.missioncontrol.ui.sidepane.flight.fly.checklist.ChecklistScope;
import com.intel.missioncontrol.ui.sidepane.flight.fly.checklist.ChecklistViewModel;
import com.intel.missioncontrol.ui.sidepane.flight.fly.checks.automatic.AutoCheckItemViewModel;
import com.intel.missioncontrol.ui.sidepane.flight.fly.checks.automatic.AutomaticChecksDialogViewModel;
import com.intel.missioncontrol.ui.sidepane.flight.fly.checks.preflight.PreflightChecklistDialogViewModel;
import de.saxsys.mvvmfx.InjectScope;
import de.saxsys.mvvmfx.ScopeProvider;
import de.saxsys.mvvmfx.utils.commands.Command;
import de.saxsys.mvvmfx.utils.commands.DelegateCommand;
import eu.mavinci.core.plane.AirplaneType;
import eu.mavinci.desktop.main.debug.Debug;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import java.util.HashMap;
import java.util.logging.Level;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
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
import org.asyncfx.beans.property.ReadOnlyAsyncStringProperty;
import org.asyncfx.beans.property.SimpleAsyncObjectProperty;
import org.asyncfx.beans.property.UIAsyncObjectProperty;
import org.asyncfx.beans.property.UIAsyncStringProperty;
import org.asyncfx.concurrent.Dispatcher;

@ScopeProvider(scopes = ChecklistScope.class)
public class SafetyChecksViewModel extends ViewModelBase {
    public static final int MAX_DISTANCE_TO_PLANNED_TAKEOFF = 10;

    @InjectScope
    private FlightScope flightScope;

    private ObjectProperty<AlertType> autoCheckAlertType = new SimpleObjectProperty<>();
    private StringProperty autoCheckStatusString = new SimpleStringProperty();
    private ObjectProperty<AlertType> manualCheckImageType = new SimpleObjectProperty<>();
    private StringProperty manualCheckStatus = new SimpleStringProperty();
    private IntegerProperty checkedCount = new SimpleIntegerProperty(0);
    private IntegerProperty totalCount = new SimpleIntegerProperty(0);

    private UIAsyncObjectProperty<AlertType> takeoffPositionAlertType = new UIAsyncObjectProperty<>(this);
    private UIAsyncStringProperty takeoffPositionStatus = new UIAsyncStringProperty(this);

    private HashMap<AirplaneType, ListProperty<ChecklistViewModel>> planeManualChecklist = new HashMap<>();
    private AirplaneType currentAirplaneType;

    private final ILanguageHelper languageHelper;
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

    private final Globe globe;

    @Inject
    public SafetyChecksViewModel(
            ILanguageHelper languageHelper,
            IDialogService dialogService,
            IFlightValidationService flightValidationService,
            IApplicationContext applicationContext,
            IWWGlobes globes) {
        this.globe = globes.getActiveGlobe();
        this.languageHelper = languageHelper;
        this.flightValidationService = flightValidationService;

        AsyncObjectProperty<Mission> mission = new SimpleAsyncObjectProperty<>(this);

        mission.bind(applicationContext.currentMissionProperty());
        selectedFlightPlan.bind(flightValidationService.flightPlanProperty());
        hardwareConfiguration =
            PropertyPath.from(drone).selectReadOnlyAsyncObject(IDrone::hardwareConfigurationProperty);

        showAutomaticChecksDialog =
            new DelegateCommand(
                () -> dialogService.requestDialogAsync(this, AutomaticChecksDialogViewModel.class, true));

        showFlightCheckListChecksDialog =
            new DelegateCommand(
                () -> dialogService.requestDialogAsync(this, PreflightChecklistDialogViewModel.class, true));

        takeoffPositionAlertType.set(AlertType.WARNING);
        takeoffPositionStatus.set(
            languageHelper.getString(SafetyChecksViewModel.class, "takeoffPositionImageStatus.notConnected"));
        takeoffPositionAlertType.bind(
            Bindings.createObjectBinding(
                () -> {
                    FlightSegment segment = flightSegment.get();
                    var selectedFp = selectedFlightPlan.get();

                    if (segment == null) {
                        segment = FlightSegment.UNKNOWN;
                    }

                    if (segment != FlightSegment.ON_GROUND && segment != FlightSegment.UNKNOWN) {
                        return AlertType.COMPLETED;
                    }

                    Position pos = position.get();
                    if (pos == null
                            || segment == FlightSegment.UNKNOWN
                            || selectedFp == null
                            || selectedFp.takeoffPositionProperty().get() == null) {
                        return AlertType.LOADING;
                    }

                    double distanceMeters = getDistance(pos, selectedFp.takeoffPositionProperty().get());
                    if (distanceMeters > MAX_DISTANCE_TO_PLANNED_TAKEOFF) {
                        return AlertType.WARNING;
                    }

                    return AlertType.COMPLETED;
                },
                selectedFlightPlan,
                activeFlightPlan,
                position,
                flightSegment));

        takeoffPositionStatus.bind(
            takeoffPositionAlertType,
            value -> {
                if (takeoffPositionAlertType.get() == null
                        || takeoffPositionAlertType.get().equals(AlertType.LOADING)) {
                    return languageHelper.getString(
                        SafetyChecksViewModel.class, "takeoffPositionImageStatus.notConnected");
                } else if (takeoffPositionAlertType.get().equals(AlertType.COMPLETED)) {
                    return languageHelper.getString(SafetyChecksViewModel.class, "takeoffPositionImageStatus.ok");
                } else {
                    return languageHelper.getString(SafetyChecksViewModel.class, "takeoffPositionImageStatus.default");
                }
            });
    }

    private double getDistance(Position pos, Position fpTakeoffPos) {
        Vec4 vPos = globe.computePointFromPosition(pos);
        Vec4 vToff = globe.computePointFromPosition(fpTakeoffPos);

        return vPos.distanceTo3(vToff);
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

        flightSegment.bind(flightScope.flightSegmentProperty());

        activeFlightPlan.bind(PropertyPath.from(drone).selectReadOnlyAsyncObject(IDrone::activeFlightPlanProperty));
        position.bind(PropertyPath.from(drone).selectReadOnlyAsyncObject(IDrone::positionProperty));

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
                }

                refreshChecklistMessage();
            });

        propertyPathStore
            .from(drone)
            .selectReadOnlyAsyncObject(IDrone::flightSegmentProperty)
            .addListener(
                (o, oldValue, newValue) -> {
                    if (oldValue == FlightSegment.UNKNOWN) {
                        return;
                    }

                    ListProperty<ChecklistViewModel> checklistViewModels =
                        checklistScope.currentChecklistProperty().get();
                    if (checklistViewModels == null) {
                        return;
                    }

                    for (ChecklistViewModel checklistViewModel : checklistViewModels) {
                        for (ChecklistItemViewModel itemViewModel : checklistViewModel.itemsProperty()) {
                            // disable all after takeoff
                            if (oldValue == FlightSegment.ON_GROUND && newValue != FlightSegment.ON_GROUND) {
                                itemViewModel.disableProperty().set(true);
                            }

                            // uncheck and enable all after landing
                            if (oldValue != FlightSegment.ON_GROUND && newValue == FlightSegment.ON_GROUND) {
                                itemViewModel.checkedProperty().set(false);
                                itemViewModel.disableProperty().set(false);
                            }
                        }
                    }
                },
                Dispatcher.platform()::run);
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

    ReadOnlyAsyncStringProperty takeoffPositionStatusProperty() {
        return takeoffPositionStatus;
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

}
