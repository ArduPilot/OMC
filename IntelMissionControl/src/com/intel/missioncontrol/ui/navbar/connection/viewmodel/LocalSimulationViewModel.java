/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.connection.viewmodel;

import com.google.inject.Inject;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.beans.property.VariantQuantityProperty;
import com.intel.missioncontrol.concurrent.Dispatcher;
import com.intel.missioncontrol.hardware.IHardwareConfigurationManager;
import com.intel.missioncontrol.hardware.IPlatformDescription;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.helper.MavinciObjectFactory;
import com.intel.missioncontrol.map.IMapController;
import com.intel.missioncontrol.map.IMapModel;
import com.intel.missioncontrol.map.ISelectionManager;
import com.intel.missioncontrol.map.InputMode;
import com.intel.missioncontrol.map.api.LocateMeApi;
import com.intel.missioncontrol.mission.FlightPlan;
import com.intel.missioncontrol.mission.Mission;
import com.intel.missioncontrol.mission.SrsPosition;
import com.intel.missioncontrol.mission.Uav;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.settings.SrsSettings;
import com.intel.missioncontrol.ui.MainScope;
import com.intel.missioncontrol.ui.ViewModelBase;
import com.intel.missioncontrol.ui.dialogs.IDialogService;
import com.intel.missioncontrol.ui.navbar.connection.ConnectionState;
import com.intel.missioncontrol.ui.navbar.connection.UavConnectionInfo;
import com.intel.missioncontrol.ui.navbar.connection.UavConnectionScope;
import com.intel.missioncontrol.ui.navbar.connection.UavInFlightInfo;
import com.intel.missioncontrol.ui.navbar.connection.UavInfo;
import com.intel.missioncontrol.ui.navbar.connection.UnmannedAerialVehicle;
import com.intel.missioncontrol.ui.navbar.connection.model.LocalSimulationItem;
import com.intel.missioncontrol.ui.navigation.ConnectionPage;
import com.intel.missioncontrol.ui.navigation.INavigationService;
import com.intel.missioncontrol.ui.navigation.NavBarDialog;
import com.intel.missioncontrol.ui.notifications.Toast;
import com.intel.missioncontrol.ui.notifications.ToastType;
import de.saxsys.mvvmfx.InjectScope;
import de.saxsys.mvvmfx.utils.commands.Action;
import de.saxsys.mvvmfx.utils.commands.Command;
import de.saxsys.mvvmfx.utils.commands.DelegateCommand;
import eu.mavinci.plane.IAirplane;
import eu.mavinci.plane.simjava.AirplaneSim;
import gov.nasa.worldwind.geom.Position;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalSimulationViewModel extends ViewModelBase {

    private static Logger LOGGER = LoggerFactory.getLogger(LocalSimulationViewModel.class);

    public static final String MISSION_ABSENT_TITLE =
        "com.intel.missioncontrol.ui.connection.viewmodel.LocalSimulationViewModel.missionAbsentError.title";
    public static final String MISSION_ABSENT_MESSAGE =
        "com.intel.missioncontrol.ui.connection.viewmodel.LocalSimulationViewModel.missionAbsentError.message";
    public static final String SIMULATION_INTERRUPTED_TITLE =
        "com.intel.missioncontrol.ui.connection.viewmodel.LocalSimulationViewModel.simulationInterrupted.title";
    public static final String SIMULATION_INTERRUPTED_MESSAGE =
        "com.intel.missioncontrol.ui.connection.viewmodel.LocalSimulationViewModel.simulationInterrupted.message";

    private static final int START_SIMULATION_HINT_SHOW_DELAY = 5;

    private final BooleanProperty isSimulationStarted = new SimpleBooleanProperty(false);

    private final ListProperty<LocalSimulationItem> simulators =
        new SimpleListProperty<>(FXCollections.observableArrayList());
    private final ObjectProperty<LocalSimulationItem> selectedSimulator = new SimpleObjectProperty<>();
    private final ObjectProperty<Float> simulationSpeed = new SimpleObjectProperty<>();
    private final SimpleBooleanProperty takeoffButtonPressed = new SimpleBooleanProperty();

    private Command actionButtonCommand;
    private Command setStartPositionButtonCommand;

    private Mission mission;
    private AirplaneSim airplaneSimulator;

    @InjectScope
    protected MainScope mainScope;

    @InjectScope
    protected UavConnectionScope uavConnectionScope;

    @Inject
    private MavinciObjectFactory mavinciObjectFactory;

    @Inject
    private IDialogService dialogService;

    @Inject
    private IMapModel mapModel;

    @Inject
    private IMapController mapController;

    @Inject
    private ISelectionManager selectionManager;

    @Inject
    private ILanguageHelper languageHelper;

    @Inject
    private LocateMeApi locateMeApi;

    @Inject
    private ISettingsManager settingsManager;

    @Inject
    private IHardwareConfigurationManager hardwareConfigurationManager;

    @Inject
    private IApplicationContext applicationContext;

    @Inject
    private INavigationService navigationService;

    private SrsSettings srsSettings;
    private SrsPosition startPostion;
    boolean pushStateDone;

    private ChangeListener<InputMode> mouseModesChangeListener =
        ((observable, oldValue, newValue) -> {
            if (newValue.equals(InputMode.SET_SIMULATION_TAKEOFF)) {
                takeoffButtonPressed.setValue(true);
            } else {
                if (pushStateDone) {
                    pushStateDone = false;
                    navigationService.popNavigationState();
                }

                takeoffButtonPressed.setValue(false);
            }
        });

    @Override
    protected void initializeViewModel() {
        super.initializeViewModel();

        initBindings();
        initValues();
    }

    private void initBindings() {
        actionButtonCommand =
            new DelegateCommand(
                () -> {
                    return new Action() {
                        @Override
                        protected void action() throws Exception {
                            processActionButtonClick();
                        }
                    };
                },
                selectedSimulator.isNotNull());

        setStartPositionButtonCommand =
            new DelegateCommand(
                () -> {
                    return new Action() {
                        @Override
                        protected void action() throws Exception {
                            startPositionOnMapMode();
                        }
                    };
                },
                new SimpleBooleanProperty(true));

        isSimulationStarted.addListener(
            (observable, oldValue, newValue) -> {
                boolean isStarted = newValue;
                if (isStarted) {
                    startSimulation();
                } else {
                    stopSimulation();
                }
            });

        simulationSpeed.addListener(
            (observable, oldValue, newValue) -> {
                changeSpeed(newValue);
            });

        selectedSimulator.addListener(
            (observable, oldValue, newValue) -> {
                if (newValue != null) {
                    initStartPosition();
                }
            });

        applicationContext
            .currentMissionProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    if (isSimulationStarted()) {
                        isSimulationStarted.setValue(false);
                        String title = languageHelper.getString(SIMULATION_INTERRUPTED_TITLE);
                        String message = languageHelper.getString(SIMULATION_INTERRUPTED_MESSAGE);
                        dialogService.showWarningMessage(title, message);
                    }

                    if (newValue != null) {
                        initStartPosition();
                    }
                });

        mapController
            .mouseModeProperty()
            .addListener(new WeakChangeListener<>(mouseModesChangeListener), Dispatcher::dispatchToUI);
    }

    private void changeSpeed(Float speed) {
        if (airplaneSimulator != null) {
            airplaneSimulator.setSimulationSpeed(speed);
        }
    }

    private void startPositionOnMapMode() {
        if (mapController.getMouseMode() != InputMode.SET_SIMULATION_TAKEOFF) {
            selectionManager.setSelection(applicationContext.getCurrentMission().uavProperty().get().getLegacyPlane());
            mapController.setMouseMode(InputMode.SET_SIMULATION_TAKEOFF);
            navigationService.pushNavigationState();
            navigationService.navigateTo(NavBarDialog.NONE);
            pushStateDone = true;
        } else {
            mapController.tryCancelMouseModes(InputMode.SET_SIMULATION_TAKEOFF);
            pushStateDone = false;
            navigationService.popNavigationState();
        }
    }

    private void startSimulation() {
        Uav uav = mission.uavProperty().get();
        IAirplane airplane = uav.getLegacyPlane();
        IPlatformDescription platformDescription = selectedSimulator.get().getPlatformDescription();
        airplane.setNativeHardwareConfiguration(
            hardwareConfigurationManager.getHardwareConfiguration(platformDescription.getId()));
        uav.setSimulatedPlatformDescription(platformDescription);
        // We don't need to start it manually. It is started on creation automatically.
        airplaneSimulator = mavinciObjectFactory.createAirplaneSimulator(platformDescription, airplane);
        airplane.setSimulationSpeed(getSimulationSpeed());
        airplane.setStartpos(getLongitude(), getLatitude());
        applicationContext.addToast(
            Toast.of(ToastType.INFO)
                .setText(
                    languageHelper.getString(
                        "com.intel.missioncontrol.ui.connection.view.LocalSimulationView.hint.startSimulation"))
                .setTimeout(Duration.seconds(START_SIMULATION_HINT_SHOW_DELAY))
                .create());

        updateSelectedUavProperty(platformDescription);
    }

    private void updateSelectedUavProperty(IPlatformDescription platformDescription) {
        // TODO create proper simulation object
        UavInFlightInfo uavInFlightInfo = new UavInFlightInfo(getLongitude(), getLatitude(), 0, 0, 0, 0);
        UnmannedAerialVehicle simualtedUav =
            new UnmannedAerialVehicle(
                platformDescription.getAirplaneType(),
                "simulation",
                uavInFlightInfo,
                new UavInfo("s0", 0, "n/a", "n/a", 0),
                new UavConnectionInfo("n/a", 0, "n/a"),
                null);
        uavConnectionScope.selectedUavProperty().set(simualtedUav);
    }

    private void stopSimulation() {
        if (airplaneSimulator != null) {
            airplaneSimulator.close();
            airplaneSimulator = null;
        }

        if (mission != null) {
            mission.getLegacyPlane().close();
            mission.getLegacyPlane().unsetAirplaneConnector();
        }

        mission = null;
        uavConnectionScope.selectedUavProperty().set(null);
        initValues();
    }

    private void initValues() {
        simulators.clear();
        IPlatformDescription[] availableDescriptions = hardwareConfigurationManager.getPlatforms();
        for (IPlatformDescription availableDescription : availableDescriptions) {
            simulators.add(new LocalSimulationItem(availableDescription));
        }

        simulationSpeed.setValue(1f);

        startPostion = new SrsPosition(settingsManager.getSection(GeneralSettings.class));
        srsSettings = settingsManager.getSection(SrsSettings.class);
        startPostion.setSrs(srsSettings.getApplicationSrs());

        srsSettings
            .applicationSrsProperty()
            .addListener((observable, oldValue, newValue) -> startPostion.setSrs(newValue));
    }

    private void processActionButtonClick() {
        boolean start = !isSimulationStarted.getValue();
        if (start) {
            mission = applicationContext.getCurrentMission();
            if (mission == null) {
                errorMissionIsAbsent();
                return;
            }
        }

        isSimulationStarted.setValue(start);
    }

    public void initStartPosition() {
        LocateMeApi.SectorCeiling currentPosition = locateMeApi.tryLocateCurrentPosition();
        if (currentPosition != null) {
            startPostion.setPositionFromDegrees(
                currentPosition.sector.getCentroid().getLatitude().getDegrees(),
                currentPosition.sector.getCentroid().getLongitude().getDegrees(),
                0.0);

            return;
        }

        Position firstWaypointPosition = getFirstWaypointPosition();

        if (firstWaypointPosition != null) {
            startPostion.setPosition(firstWaypointPosition);
        }
    }

    private Position getFirstWaypointPosition() {
        Mission currentMission = applicationContext.currentMissionProperty().getValue();

        if (currentMission == null) {
            return null;
        }

        FlightPlan selectedFlightPlan = currentMission.getCurrentFlightPlan();

        if (selectedFlightPlan == null) {
            return null;
        }

        return selectedFlightPlan.getFirstWaypointPosition(false);
    }

    public Double getLatitude() {
        return startPostion.latitudeValueProperty().get();
    }

    public Double getLongitude() {
        return startPostion.longitudeValueProperty().get();
    }

    public Float getSimulationSpeed() {
        return simulationSpeed.get();
    }

    public ObjectProperty<Float> simulationSpeedProperty() {
        return simulationSpeed;
    }

    private void errorMissionIsAbsent() {
        String errorMessage = languageHelper.getString(MISSION_ABSENT_MESSAGE);
        String errorTitle = languageHelper.getString(MISSION_ABSENT_TITLE);
        LOGGER.error(errorMessage);
        dialogService.showErrorMessage(errorTitle, errorMessage);
    }

    public ConnectionPage getConnectedPage() {
        return connectedPageProperty().get();
    }

    public ObjectProperty<ConnectionPage> connectedPageProperty() {
        return uavConnectionScope.connectedPageProperty();
    }

    public ConnectionState getConnectionState() {
        return uavConnectionScope.connectionStateProperty().get();
    }

    public ObjectProperty<ConnectionState> connectionStateProperty() {
        return uavConnectionScope.connectionStateProperty();
    }

    public ObservableList<LocalSimulationItem> getSimulators() {
        return simulators.get();
    }

    public ListProperty<LocalSimulationItem> simulatorsProperty() {
        return simulators;
    }

    public LocalSimulationItem getSelectedSimulator() {
        return selectedSimulator.get();
    }

    public ObjectProperty<LocalSimulationItem> selectedSimulatorProperty() {
        return selectedSimulator;
    }

    public boolean getIsSimulationStarted() {
        return isSimulationStarted.get();
    }

    public VariantQuantityProperty latitudeQuantityProperty() {
        return startPostion.latitudeQuantity();
    }

    public VariantQuantityProperty longitudeQuantityProperty() {
        return startPostion.longitudeQuantity();
    }

    public boolean isSimulationStarted() {
        return isSimulationStarted.get();
    }

    public BooleanProperty isSimulationStartedProperty() {
        return isSimulationStarted;
    }

    public SimpleBooleanProperty takeoffButtonPressedProperty() {
        return takeoffButtonPressed;
    }

    public Command getActionButtonCommand() {
        return actionButtonCommand;
    }

    public Command getSetStartPositionButtonCommand() {
        return setStartPositionButtonCommand;
    }

}
