/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight;

import com.google.inject.Inject;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.concurrent.Dispatcher;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.mission.Mission;
import com.intel.missioncontrol.mission.Uav;
import com.intel.missioncontrol.ui.ViewModelBase;
import com.intel.missioncontrol.ui.navigation.ConnectionPage;
import com.intel.missioncontrol.ui.navigation.INavigationService;
import com.intel.missioncontrol.ui.navigation.WorkflowStep;
import com.intel.missioncontrol.ui.sidepane.flight.PlaneHealth.PlaneHealthChannel;
import com.intel.missioncontrol.ui.sidepane.flight.PlaneHealth.PlaneHealthChannelInfo;
import com.intel.missioncontrol.ui.sidepane.flight.PlaneHealth.PlaneHealthChannelStatus;
import com.intel.missioncontrol.ui.sidepane.planning.emergency.EventHelper;
import eu.mavinci.core.flightplan.CEvent;
import eu.mavinci.core.flightplan.GPSFixType;
import eu.mavinci.core.plane.AirplaneConnectorState;
import eu.mavinci.core.plane.AirplaneFlightmode;
import gov.nasa.worldwind.geom.Position;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.MapProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;

public class UavStatusViewModel extends ViewModelBase implements ChangeListener<Mission> {

    private static final String NO_DATA = "--";

    @Inject
    private ILanguageHelper languageHelper;

    @Inject
    private IApplicationContext applicationContext;

    @Inject
    private INavigationService navigationService;

    private Uav uav;
    private EventHelper eventHelper;

    private final Property<String> batteryVoltageProperty = new SimpleStringProperty(NO_DATA);
    private final Property<Number> batteryVoltageValueProperty = new SimpleFloatProperty(0f);
    private final Property<String> batteryPercentageProperty = new SimpleStringProperty(NO_DATA);
    private final Property<Number> batteryPercentageValueProperty = new SimpleFloatProperty(0f);

    private final Property<String> connectorBatteryVoltageProperty = new SimpleStringProperty(NO_DATA);
    private final Property<Number> connectorBatteryVoltageValueProperty = new SimpleFloatProperty(0f);
    private final Property<String> connectorBatteryPercentageProperty = new SimpleStringProperty(NO_DATA);
    private final Property<Number> connectorBatteryPercentageValueProperty = new SimpleFloatProperty(0f);

    private final Property<Number> gpsProperty = new SimpleIntegerProperty(0);
    private final Property<Number> glonassProperty = new SimpleIntegerProperty(0);
    private final Property<GPSFixType> gpsQualityProperty = new SimpleObjectProperty<>(GPSFixType.noFix);
    private final Property<AlertLevel> gpsQualityAlertProperty = new SimpleObjectProperty<>();

    private final BooleanProperty isGpsLost = new SimpleBooleanProperty();
    private final Property<String> gpsLostMessage = new SimpleStringProperty();

    private final IntegerProperty motor1RpmProperty = new SimpleIntegerProperty(0);
    private final Property<String> groundSpeedProperty = new SimpleStringProperty(NO_DATA);

    private final Property<AirplaneFlightmode> flightModeProperty =
        new SimpleObjectProperty<>(AirplaneFlightmode.AutomaticFlight);

    private final Property<String> altitudeProperty = new SimpleStringProperty(NO_DATA);
    private final Property<String> maxAltitudeProperty = new SimpleStringProperty(NO_DATA);
    private final Property<String> altitudeRelationProperty = new SimpleStringProperty("-");
    private final Property<AlertLevel> altitudeAlertProperty = new SimpleObjectProperty<>();

    private final DoubleProperty windDirectionProperty = new SimpleDoubleProperty(0.0);
    private final Property<String> windSpeedProperty = new SimpleStringProperty(NO_DATA);
    private final Property<Position> positionProperty = new SimpleObjectProperty<>();

    private final Property<AirplaneConnectorState> connectionProperty =
        new SimpleObjectProperty<>(AirplaneConnectorState.unconnected);

    private final MapProperty<String, PlaneHealthChannelStatus> notImportantAlertsProperty =
        new SimpleMapProperty<>(FXCollections.emptyObservableMap());

    public void init() {
        applicationContext.currentMissionProperty().addListener(this);

        isGpsLost.addListener(
            (observable, oldValue, newValue) -> {
                if (newValue) {
                    updateGpsLostMessage();
                }
            });

        positionProperty.addListener((observable, oldValue, newValue) -> updateGpsLostMessage());

        this.eventHelper = new EventHelper(languageHelper);
    }

    @Override
    public synchronized void changed(
            ObservableValue<? extends Mission> observable, Mission oldValue, Mission newValue) {
        if (newValue == null) {
            // do not disconnect on close mission
            return;
        }

        Uav newUav = newValue.uavProperty().get();

        if ((newUav == null) || (this.uav == newUav)) {
            return;
        }

        disconnectUav();
        connectUav(newUav);
    }

    private void connectUav(Uav newUav) {
        this.uav = newUav;
        bindUav();
    }

    private void disconnectUav() {
        if (uav != null) {
            unbindUav();
        }

        uav = null;
    }

    private void bindUav() {
        batteryVoltageProperty.bind(uav.batteryVoltageProperty());
        batteryVoltageValueProperty.bind(uav.batteryVoltageValueProperty());
        batteryPercentageProperty.bind(uav.batteryPercentageProperty());
        batteryPercentageValueProperty.bind(uav.batteryPercentageValueProperty());

        connectorBatteryVoltageProperty.bind(uav.connectorBatteryVoltageProperty());
        connectorBatteryVoltageValueProperty.bind(uav.connectorBatteryVoltageValueProperty());
        connectorBatteryPercentageProperty.bind(uav.connectorBatteryPercentageProperty());
        connectorBatteryPercentageValueProperty.bind(uav.connectorBatteryPercentageValueProperty());

        gpsProperty.bind(uav.gpsProperty());
        glonassProperty.bind(uav.glonassProperty());
        gpsQualityProperty.bind(uav.gpsQualityProperty());
        gpsQualityAlertProperty.bind(uav.gpsQualityAlertProperty());

        isGpsLost.bind(uav.isGpsLostProperty());

        motor1RpmProperty.bind(uav.motor1RpmProperty());
        groundSpeedProperty.bind(uav.groundSpeedProperty());
        flightModeProperty.bind(uav.flightModeProperty());

        altitudeProperty.bind(uav.altitudeProperty());
        maxAltitudeProperty.bind(uav.maxAltitudeProperty());
        altitudeRelationProperty.bind(uav.altitudeRelationProperty());
        altitudeAlertProperty.bind(uav.altitudeAlertProperty());

        windDirectionProperty.bind(uav.windDirectionProperty());
        windSpeedProperty.bind(uav.windSpeedProperty());

        connectionProperty.bind(uav.connectionProperty());

        notImportantAlertsProperty.bind(uav.notImportantAlertsProperty());

        positionProperty.bind(uav.positionProperty());

        uav.connectionProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue == AirplaneConnectorState.unconnected) {
                        resetProperties();
                    }
                });
    }

    public void showEmergencyActionPopup() {
        // TODO: WWJFX - removed, since there's no Swing anymore
        /*popup = new PopupWidgetPanel(GpsLostEmergencyInstructionsView.class);
        popup.setSize(0, 0);
        popup.setMaximumSize(new Dimension(0, 0));
        popup.setDraggable(false);
        popup.setMinimizable(false);
        popup.setMaximizable(false);
        popup.setCloseable(true);
        popup.setScopes(mainScope);*/

        // TODO: WWJFX - What was the purpose of this code?
        /*Platform.runLater(
        () ->
            mainScope
                .getAppWindow()
                .getWidgetsLayer()
                .addPalettePanel(popup, MapPaletteLayout.anchorCenterCenter(0, 0)));*/

        navigationService.navigateTo(WorkflowStep.FLIGHT);
    }

    public void closeEmergencyActionPopup() {
        // TODO: WWJFX - removed, since there's no Swing anymore
        // Optional.ofNullable(popup).ifPresent(popupWidgetPanel -> popupWidgetPanel.close());
    }

    private void unbindUav() {
        batteryVoltageProperty.unbind();
        batteryVoltageValueProperty.unbind();
        batteryPercentageProperty.unbind();
        batteryPercentageValueProperty.unbind();

        connectorBatteryVoltageProperty.unbind();
        connectorBatteryVoltageValueProperty.unbind();
        connectorBatteryPercentageProperty.unbind();
        connectorBatteryPercentageValueProperty.unbind();

        gpsProperty.unbind();
        glonassProperty.unbind();
        gpsQualityProperty.unbind();
        gpsQualityAlertProperty.unbind();

        isGpsLost.unbind();
        isGpsLost.setValue(false);

        motor1RpmProperty.unbind();
        groundSpeedProperty.unbind();

        altitudeProperty.unbind();
        maxAltitudeProperty.unbind();
        altitudeRelationProperty.unbind();
        altitudeAlertProperty.unbind();

        windDirectionProperty.unbind();
        windSpeedProperty.unbind();

        connectionProperty.unbind();

        notImportantAlertsProperty.unbind();

        positionProperty.unbind();
    }

    private void resetProperties() {
        uav.batteryVoltageProperty().setValue(NO_DATA);
        uav.batteryVoltageValueProperty().setValue(0f);
        uav.batteryPercentageProperty().setValue(NO_DATA);
        uav.batteryPercentageValueProperty().setValue(0f);

        uav.connectorBatteryVoltageProperty().setValue(NO_DATA);
        uav.connectorBatteryVoltageValueProperty().setValue(0f);
        uav.connectorBatteryPercentageProperty().setValue(NO_DATA);
        uav.connectorBatteryPercentageValueProperty().setValue(0f);

        uav.gpsProperty().setValue(0);
        uav.glonassProperty().setValue(0);
        uav.gpsQualityProperty().setValue(GPSFixType.noFix);
        uav.gpsQualityAlertProperty().setValue(null);

        uav.motor1RpmProperty().setValue(0);
        uav.groundSpeedProperty().setValue(NO_DATA);

        uav.altitudeProperty().setValue(NO_DATA);
        uav.maxAltitudeProperty().setValue(NO_DATA);
        uav.altitudeRelationProperty().setValue("-");
        uav.altitudeAlertProperty().setValue(null);

        uav.windDirectionProperty().setValue(0.0);
        uav.windSpeedProperty().setValue(NO_DATA);

        uav.notImportantAlertsProperty().clear();
    }

    public Property<String> batteryVoltageProperty() {
        return batteryVoltageProperty;
    }

    public Property<String> batteryPercentageProperty() {
        return batteryPercentageProperty;
    }

    public Property<String> connectorBatteryVoltageProperty() {
        return connectorBatteryVoltageProperty;
    }

    public Property<String> connectorBatteryPercentageProperty() {
        return connectorBatteryPercentageProperty;
    }

    public Property<Number> gpsProperty() {
        return gpsProperty;
    }

    public Property<Number> glonassProperty() {
        return glonassProperty;
    }

    public Property<GPSFixType> gpsQualityProperty() {
        return gpsQualityProperty;
    }

    public Property<AlertLevel> gpsQualityAlertProperty() {
        return gpsQualityAlertProperty;
    }

    public IntegerProperty motor1RpmProperty() {
        return motor1RpmProperty;
    }

    public Property<String> getGroundSpeedProperty() {
        return groundSpeedProperty;
    }

    public BooleanProperty isGpsLostProperty() {
        return isGpsLost;
    }

    public Property<String> gpsLostMessageProperty() {
        return gpsLostMessage;
    }

    public Property<String> altitudeProperty() {
        return altitudeProperty;
    }

    public Property<String> maxAltitudeProperty() {
        return maxAltitudeProperty;
    }

    public Property<String> altitudeRelationProperty() {
        return altitudeRelationProperty;
    }

    public Property<AlertLevel> altitudeAlertProperty() {
        return altitudeAlertProperty;
    }

    public DoubleProperty windDirectionProperty() {
        return windDirectionProperty;
    }

    public Property<String> windSpeedProperty() {
        return windSpeedProperty;
    }

    public Property<AirplaneConnectorState> connectionProperty() {
        return connectionProperty;
    }

    public Property<Number> batteryPercentageValueProperty() {
        return batteryPercentageValueProperty;
    }

    public Property<Number> connectorBatteryPercentageValueProperty() {
        return connectorBatteryPercentageValueProperty;
    }

    public Property<Number> batteryVoltageValueProperty() {
        return batteryVoltageValueProperty;
    }

    public Property<Number> connectorBatteryVoltageValueProperty() {
        return connectorBatteryVoltageValueProperty;
    }

    public MapProperty<String, PlaneHealthChannelStatus> notImportantAlertsProperty() {
        return notImportantAlertsProperty;
    }

    public PlaneHealth getHealth() {
        Uav uav = getUav();
        if (uav == null) {
            return null;
        }

        return uav.getHealth();
    }

    public String getLastFailEventDescription() {
        CEvent event = getLastFailEvent();

        if (event == null) {
            return "";
        }

        return eventHelper.getEventDescription(event);
    }

    public CEvent getLastFailEvent() {
        return getUav() == null ? null : getUav().getLastFailEvent();
    }

    private void updateGpsLostMessage() {
        CEvent event = getLastFailEvent();
        String message =
            event != null
                ? languageHelper.getString(EmergencyActionViewModel.TITLE_DETAILS_PART_2_KEY, event.getDelay())
                : languageHelper.getString(EmergencyActionViewModel.TITLE_DETAILS_PART_2_UNKNOWN_KEY);
        Dispatcher.postToUI(() -> gpsLostMessage.setValue(message));
    }

    public PlaneHealthChannelInfo getHealthChannelInfo(PlaneHealthChannel channel) {
        Uav uav = getUav();
        if (uav == null) {
            return null;
        }

        return uav.getHealthChannelInfo(channel);
    }

    public AlertLevel getHealthAlert(Number healthValue, PlaneHealthChannel channel) {
        Uav uav = getUav();
        if (uav == null) {
            return AlertLevel.RED;
        }

        return uav.getHealthAlert(healthValue, channel);
    }

    public synchronized Uav getUav() {
        return uav;
    }

    public void openRtkConnection() {
        navigationService.navigateTo(ConnectionPage.RTK_BASE_STATION);
    }

    public Property<AirplaneFlightmode> flightModeProperty() {
        return flightModeProperty;
    }
}
