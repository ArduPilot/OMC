/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.connection;

import com.google.inject.Inject;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.custom.ReadonlyPropertyWrap;
import com.intel.missioncontrol.hardware.IHardwareConfiguration;
import com.intel.missioncontrol.hardware.IPlatformDescription;
import com.intel.missioncontrol.mission.Mission;
import com.intel.missioncontrol.mission.Uav;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.settings.OperationLevel;
import com.intel.missioncontrol.ui.common.LateBinding;
import com.intel.missioncontrol.ui.dialogs.DialogViewModel;
import com.intel.missioncontrol.ui.navbar.connection.scope.RtkConnectionScope;
import com.intel.missioncontrol.ui.navigation.ConnectionPage;
import com.intel.missioncontrol.ui.navigation.INavigationService;
import com.intel.missioncontrol.ui.navigation.NavBarDialog;
import com.intel.missioncontrol.ui.validation.IValidationService;
import de.saxsys.mvvmfx.InjectScope;
import de.saxsys.mvvmfx.ScopeProvider;
import eu.mavinci.plane.IAirplane;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;

@ScopeProvider(scopes = {RtkConnectionScope.class})
public class ConnectionViewModel extends DialogViewModel {
    private static final int VIEW_VISIBLE_PROPERTY_INDEX = 0;
    private static final int BUTTON_SELECTED_PROPERTY_INDEX = 1;

    private final ObjectProperty<ConnectionPage> currentPage;
    private final Map<ConnectionPage, List<BooleanProperty>> componentsProperties;
    private final Map<ConnectionPage, Consumer<ConnectionPage>> onConnectingTo;

    // for unit tests
    @InjectScope
    UavConnectionScope uavConnectionScope;

    @InjectScope
    RtkConnectionScope rtkConnectionScope;

    private final IApplicationContext applicationContext;
    private final IValidationService validationService;
    private final INavigationService navigationService;
    private GeneralSettings settings;

    @Inject
    public ConnectionViewModel(
            IApplicationContext applicationContext,
            IValidationService validationService,
            INavigationService navigationService,
            ISettingsManager settingsManager) {
        this.applicationContext = applicationContext;
        this.validationService = validationService;
        this.navigationService = navigationService;
        componentsProperties = new EnumMap<>(ConnectionPage.class);
        initializeComponentsProperties();

        onConnectingTo = new EnumMap<>(ConnectionPage.class);
        initializeConnectionActions();

        currentPage = new SimpleObjectProperty<>();
        currentPage.addListener(getCurrentPageChangeListener());

        this.settings = settingsManager.getSection(GeneralSettings.class);
    }

    @Override
    protected void onClosing() {
        navigationService.navigateTo(NavBarDialog.NONE);
    }

    private void initializeComponentsProperties() {
        EnumSet.allOf(ConnectionPage.class)
            .forEach(
                page ->
                    componentsProperties.put(
                        page, Arrays.asList(new SimpleBooleanProperty(), new SimpleBooleanProperty())));
    }

    @Override
    protected void initializeViewModel() {
        super.initializeViewModel();

        currentPage.set(ConnectionPage.UAV_CONNECT);
        navigationService
            .connectionPageProperty()
            .addListener((observable, old, page) -> currentPageProperty().set(page));
        uavConnectionScope
            .connectionStateProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue == ConnectionState.CONNECTED || newValue == ConnectionState.CONNECTED_WARNING) {
                        validationService.notifyAirplaneConnected();
                    } else {
                        validationService.notifyAirplaneDisconnected();
                    }
                });
    }

    private ChangeListener<ConnectionPage> getCurrentPageChangeListener() {
        return (observable, oldValue, current) -> onConnectingTo.get(current).accept(current);
    }

    private void initializeConnectionActions() {
        Predicate<ConnectionPage> allowToSelectPage = page -> disconnected() || connectingTo(page);
        onConnectingTo.put(
            ConnectionPage.LOCAL_SIMULATION, current -> selectPageWithCondition(current, allowToSelectPage));
        onConnectingTo.put(ConnectionPage.UAV_CONNECT, current -> selectPageWithCondition(current, allowToSelectPage));
        onConnectingTo.put(ConnectionPage.LOG_FILE, current -> selectPageWithCondition(current, allowToSelectPage));
        onConnectingTo.put(ConnectionPage.RTK_BASE_STATION, this::selectPage);
        onConnectingTo.put(ConnectionPage.JOYSTICK, this::selectPage);
        onConnectingTo.put(ConnectionPage.DATA_TRANSFER_FTP, this::selectPage);
    }

    private void selectPageWithCondition(ConnectionPage current, Predicate<ConnectionPage> allowToSelectPage) {
        if (allowToSelectPage.test(current)) {
            selectPage(current);
        }
    }

    private void selectPage(ConnectionPage current) {
        getViewVisibleProperty(current).set(true);
        getButtonSelectedProperty(current).set(true);

        componentsProperties
            .entrySet()
            .stream()
            .filter(entry -> entry.getKey() != current)
            .flatMap(e -> e.getValue().stream())
            .forEach(prop -> prop.set(false));
    }

    private boolean connectingTo(ConnectionPage connectionPage) {
        return connectedPageProperty().get() == connectionPage;
    }

    private boolean disconnected() {
        return connectingTo(null);
    }

    public ObjectProperty<ConnectionPage> connectedPageProperty() {
        return uavConnectionScope.connectedPageProperty();
    }

    public ObjectProperty<ConnectionPage> currentPageProperty() {
        return currentPage;
    }

    public BooleanProperty localSimulationViewVisibleProperty() {
        return getViewVisibleProperty(ConnectionPage.LOCAL_SIMULATION);
    }

    public BooleanProperty localSimulationButtonSelectedProperty() {
        return getButtonSelectedProperty(ConnectionPage.LOCAL_SIMULATION);
    }

    public BooleanProperty uavConnectViewVisibleProperty() {
        return getViewVisibleProperty(ConnectionPage.UAV_CONNECT);
    }

    public BooleanProperty uavConnectButtonSelectedProperty() {
        return getButtonSelectedProperty(ConnectionPage.UAV_CONNECT);
    }

    public BooleanProperty logFileViewVisibleProperty() {
        return getViewVisibleProperty(ConnectionPage.LOG_FILE);
    }

    public BooleanProperty logFileButtonSelectedProperty() {
        return getButtonSelectedProperty(ConnectionPage.LOG_FILE);
    }

    public BooleanProperty rtkBaseStationViewVisibleProperty() {
        return getViewVisibleProperty(ConnectionPage.RTK_BASE_STATION);
    }

    public BooleanProperty rtkBaseStationButtonSelectedProperty() {
        return getButtonSelectedProperty(ConnectionPage.RTK_BASE_STATION);
    }

    public BooleanProperty joystickViewVisibleProperty() {
        return getViewVisibleProperty(ConnectionPage.JOYSTICK);
    }

    public BooleanProperty joystickButtonSelectedProperty() {
        return getButtonSelectedProperty(ConnectionPage.JOYSTICK);
    }

    public BooleanProperty dataTransferFtpViewVisibleProperty() {
        return getViewVisibleProperty(ConnectionPage.DATA_TRANSFER_FTP);
    }

    public BooleanProperty dataTransferFtpSelectedProperty() {
        return getButtonSelectedProperty(ConnectionPage.DATA_TRANSFER_FTP);
    }

    public BooleanBinding dataTransferFtpViewDisableProperty() {
        ReadonlyPropertyWrap<Uav> uav =
            LateBinding.of(applicationContext.currentMissionProperty()).get(Mission::uavProperty).property();
        return Bindings.createBooleanBinding(
            () ->
                connectedPageProperty().get() == null
                    || Optional.ofNullable(uav.get())
                        .map(Uav::getLegacyPlane)
                        .map(IAirplane::getHardwareConfiguration)
                        .map(IHardwareConfiguration::getPlatformDescription)
                        .map(IPlatformDescription::getAirplaneType)
                        .filter(type -> !type.isSirius())
                        .isPresent(),
            connectedPageProperty());
    }

    private BooleanProperty getViewVisibleProperty(ConnectionPage page) {
        return getPageProperties(page).get(VIEW_VISIBLE_PROPERTY_INDEX);
    }

    private BooleanProperty getButtonSelectedProperty(ConnectionPage page) {
        return getPageProperties(page).get(BUTTON_SELECTED_PROPERTY_INDEX);
    }

    private List<BooleanProperty> getPageProperties(ConnectionPage page) {
        return componentsProperties.getOrDefault(
            page, Arrays.asList(new SimpleBooleanProperty(), new SimpleBooleanProperty()));
    }

    public ObjectProperty<RtkType> rtkBaseConnectionType() {
        return rtkConnectionScope.rtkSourceProperty();
    }

    public ObjectProperty<ConnectionState> rtkConnectionState() {
        return rtkConnectionScope.connectedStateProperty();
    }

    public BooleanBinding isMissionChosen() {
        return applicationContext.currentMissionProperty().isNotNull();
    }

    public BooleanBinding isAdvancedOperationLevelBinding() {
        return this.settings
            .operationLevelProperty()
            .isEqualTo(OperationLevel.TECHNICIAN)
            .or(this.settings.operationLevelProperty().isEqualTo(OperationLevel.DEBUG));
    }
}
