/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.intel.missioncontrol.concurrent.Dispatcher;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.helper.ScaleHelper;
import com.intel.missioncontrol.ui.EnumConverter;
import com.intel.missioncontrol.ui.sidepane.flight.PlaneHealth.PlaneHealthChannel;
import com.intel.missioncontrol.ui.sidepane.flight.PlaneHealth.PlaneHealthChannelInfo;
import com.intel.missioncontrol.ui.sidepane.flight.PlaneHealth.PlaneHealthChannelStatus;
import com.intel.missioncontrol.utils.Sounds;
import com.intel.missioncontrol.utils.SoundsUtils;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import eu.mavinci.core.flightplan.GPSFixType;
import eu.mavinci.core.plane.AirplaneConnectorState;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.Property;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class UavStatusView extends GridPane implements FxmlView<UavStatusViewModel>, Initializable {


    private static final String KEY_FAILURE = "com.intel.missioncontrol.ui.flight.UavStatusView.failure";

    private static final String ICON_LINK_GOOD = "/com/intel/missioncontrol/icons/icon_fw_linkgood.svg";
    private static final String ICON_LINK_LOSS = "/com/intel/missioncontrol/icons/icon_fw_linkloss.svg";

    @Inject
    private ILanguageHelper languageHelper;

    @FXML
    private VBox connectionWidget;

    @FXML
    private IconAndLabel connectionView;

    @FXML
    private IconAndLabelExtended batteryView;

    @FXML
    private IconAndLabelExtended connectorBatteryView;

    @FXML
    private GpsGlonass gpsGlonassView;

    @FXML
    private IconAndLabelExtended gpsQualityView;

    @FXML
    private EmergencyAction gpsLostView;

    @FXML
    private IconAndLabelExtended motor1View;

    @FXML
    private IconAndLabelExtended groundSpeedView;

    @FXML
    private IconAndLabelExtended altitudeView;

    @FXML
    private Wind windView;

    @FXML
    private FlowPane notImportantAlertsPane;

    @FXML
    private GridPane headerPane;

    @FXML
    private ImageView imageHeaderAlert;

    @InjectViewModel
    private UavStatusViewModel viewModel;

    private final Map<String, IconAndLabelExtended> notImportantAlerts = Maps.newHashMap();
    private final AlertCounter alertCounter = new AlertCounter();

    private ParallelTransition gpsLostTransitionClose;
    private ParallelTransition gpsLostTransitionOpen;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        viewModel.init();

        initAlertListeners();

        // initial alert
        updateMotor1Alert(viewModel.motor1RpmProperty().getValue());
        updateAltitudeAlert(altitudeView.getViewModel().alertPropery().getValue());

        Bindings.bindBidirectional(
            connectionView.getViewModel().textProperty(),
            viewModel.connectionProperty(),
            new EnumConverter<>(languageHelper, AirplaneConnectorState.class));

        viewModel
            .connectionProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    connectionView.setImageUrl(getConnectionImageUrl(newValue));
                    connectionView.setAlert(getConnectionAlert(newValue));
                });

        batteryView.getViewModel().labelLeftProperty().bind(viewModel.batteryVoltageProperty());
        batteryView.getViewModel().labelRightProperty().bind(viewModel.batteryPercentageProperty());

        viewModel
            .batteryPercentageValueProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    Number voltage = viewModel.batteryVoltageValueProperty().getValue();
                    updateBatteryAlert(batteryView, voltage, newValue, PlaneHealthChannel.BATTERY_MAIN);
                });

        connectorBatteryView.getViewModel().labelLeftProperty().bind(viewModel.connectorBatteryVoltageProperty());
        connectorBatteryView.getViewModel().labelRightProperty().bind(viewModel.connectorBatteryPercentageProperty());

        viewModel
            .connectorBatteryPercentageValueProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    Number voltage = viewModel.connectorBatteryVoltageValueProperty().getValue();
                    updateBatteryAlert(connectorBatteryView, voltage, newValue, PlaneHealthChannel.BATTERY_CONNECTOR);
                });

        gpsGlonassView.getViewModel().gpsProperty().bind(viewModel.gpsProperty());
        gpsGlonassView.getViewModel().glonassProperty().bind(viewModel.glonassProperty());

        viewModel
            .gpsProperty()
            .addListener(
                (observable, oldValue, newValue) ->
                    gpsGlonassView.setAlert(viewModel.getHealthAlert(newValue, PlaneHealthChannel.GPS)));

        Bindings.bindBidirectional(
            gpsQualityView.getViewModel().labelLeftProperty(),
            viewModel.gpsQualityProperty(),
            new EnumConverter<>(languageHelper, GPSFixType.class));

        viewModel
            .gpsQualityAlertProperty()
            .addListener((observable, oldValue, newValue) -> gpsQualityView.setAlert(newValue));

        gpsLostView.titleDetailsPart2Property().bind(viewModel.gpsLostMessageProperty());
        //        gpsLostView.alertProperty().bind(viewModel.gpsLostAlertLevelProperty());

        motor1View.getViewModel().labelLeftProperty().bind(viewModel.motor1RpmProperty().asString());
        viewModel.motor1RpmProperty().addListener((observable, oldValue, newValue) -> updateMotor1Alert(newValue));

        groundSpeedView.getViewModel().labelLeftProperty().bind(viewModel.getGroundSpeedProperty());

        altitudeView.getViewModel().labelLeftProperty().bind(viewModel.altitudeProperty());
        altitudeView.getViewModel().labelRightProperty().bind(viewModel.maxAltitudeProperty());
        altitudeView.getViewModel().separatorProperty().bind(viewModel.altitudeRelationProperty());

        viewModel
            .altitudeAlertProperty()
            .addListener((observable, oldValue, newValue) -> updateAltitudeAlert(newValue));

        windView.getViewModel().directionProperty().bind(viewModel.windDirectionProperty());
        windView.getViewModel().speedProperty().bind(viewModel.windSpeedProperty());

        viewModel
            .notImportantAlertsProperty()
            .addListener((observable, oldValue, newValue) -> updateNotImportantAlerts(newValue));

        viewModel
            .isGpsLostProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue) {
                        showGpsLostAlert();
                    } else if (gpsLostView.isVisible()) {
                        closeGpsLostAlert();
                    }
                });

        initGpsSectionTransitions();

        imageHeaderAlert.visibleProperty().bind(connectionWidget.visibleProperty().not());
    }

    private void initAlertListeners() {
        initAlertListener(connectionView);
        initAlertListener(batteryView);
        initAlertListener(connectorBatteryView);
        initAlertListener(gpsGlonassView);
        initAlertListener(gpsQualityView);
        initAlertListener(motor1View);
        initAlertListener(altitudeView);
        initAlertListener(gpsLostView);

        connectorBatteryView
            .visibleProperty()
            .bind(
                new BooleanBinding() {
                    @Override
                    protected boolean computeValue() {
                        if (viewModel != null && viewModel.getUav() != null) {
                            return !viewModel.getUav().isCopter();
                        } else {
                            return false;
                        }
                    }
                });
        motor1View
            .visibleProperty()
            .bind(
                new BooleanBinding() {
                    @Override
                    protected boolean computeValue() {
                        if (viewModel != null && viewModel.getUav() != null) {
                            return !viewModel.getUav().isCopter();
                        }

                        return false;
                    }
                });
    }

    private void initAlertListener(AlertAwareComponent view) {
        Property<AlertLevel> alertPropery = view.getViewModel().alertPropery();
        alertPropery.addListener((observable, oldValue, newValue) -> updateAlertCount(oldValue, newValue));

        // initial alert
        updateAlertCount(null, alertPropery.getValue());
    }

    private void initGpsSectionTransitions() {
        FadeTransition fadeInTransitionGlonassView = new FadeTransition(Duration.millis(600), gpsGlonassView);
        fadeInTransitionGlonassView.setFromValue(0.0);
        fadeInTransitionGlonassView.setToValue(1.0);
        FadeTransition fadeInTransitionQualityView = new FadeTransition(Duration.millis(600), gpsQualityView);
        fadeInTransitionQualityView.setFromValue(0.0);
        fadeInTransitionQualityView.setToValue(1.0);

        FadeTransition fadeOutTransitionGlonassView = new FadeTransition(Duration.millis(200), gpsGlonassView);
        fadeOutTransitionGlonassView.setFromValue(1.0);
        fadeOutTransitionGlonassView.setToValue(0.0);
        FadeTransition fadeOutTransitionQualityView = new FadeTransition(Duration.millis(200), gpsQualityView);
        fadeOutTransitionQualityView.setFromValue(1.0);
        fadeOutTransitionQualityView.setToValue(0.0);

        TranslateTransition openGpsLostView = new TranslateTransition(Duration.millis(400), gpsLostView);
        openGpsLostView.setToX(0);

        TranslateTransition closeGpsLostView = new TranslateTransition(Duration.millis(400), gpsLostView);
        closeGpsLostView.setOnFinished(
            event -> {
                gpsLostView.setVisible(false);
            });

        gpsLostTransitionClose = new ParallelTransition();
        gpsLostTransitionClose
            .getChildren()
            .addAll(fadeInTransitionGlonassView, fadeInTransitionQualityView, closeGpsLostView);

        gpsLostTransitionOpen = new ParallelTransition();
        gpsLostTransitionOpen
            .getChildren()
            .addAll(fadeOutTransitionGlonassView, fadeOutTransitionQualityView, openGpsLostView);

        gpsLostView
            .widthProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    // Set TranslateX property only when the view is not currently visible. Applying the
                    // value to the currently showing view
                    // can lead to its incorrect layout on the parent
                    if (!gpsLostView.isVisible()) {
                        gpsLostView.setTranslateX(-newValue.doubleValue());
                    }

                    closeGpsLostView.setToX(-newValue.doubleValue());
                });
    }

    private void showGpsLostAlert() {
        Dispatcher.postToUI(
            () -> {
                gpsLostView.setVisible(true);
                gpsLostView.setAlert(AlertLevel.RED);
                gpsLostTransitionOpen.play();
                SoundsUtils.playSound(Sounds.EMERGENCY_ALERT);
                viewModel.showEmergencyActionPopup();
            });
    }

    private void closeGpsLostAlert() {
        Dispatcher.postToUI(
            () -> {
                gpsLostTransitionClose.play();
                gpsLostView.setAlert(AlertLevel.GREEN);
                viewModel.closeEmergencyActionPopup();
            });
    }

    private void updateBatteryAlert(
            IconAndLabel batteryComponent, Number voltage, Number percent, PlaneHealthChannel channel) {
        PlaneHealthChannelInfo batteryInfo = viewModel.getHealthChannelInfo(channel);
        String batteryIcon = UavIconHelper.getBatteryIconSvg(voltage, percent, batteryInfo, true);
        batteryComponent.setImageUrl(batteryIcon);
        batteryComponent.setAlert(viewModel.getHealthAlert(voltage, channel));
    }

    private void updateMotor1Alert(Number newValue) {
        AlertLevel healthAlert = viewModel.getHealthAlert(newValue, PlaneHealthChannel.MOTOR1);
        motor1View.setAlert(healthAlert);
        motor1View.setImageUrl(UavIconHelper.getAlertIconSvg(healthAlert, true));
    }

    private void updateAltitudeAlert(AlertLevel newValue) {
        altitudeView.setAlert(newValue);
        altitudeView.setImageUrl(UavIconHelper.getAlertIconSvg(newValue, true));
    }

    private void updateAlertCount(AlertLevel oldAlert, AlertLevel newAlert) {
        alertCounter.decrementAlertCount(oldAlert);
        alertCounter.incrementAlertCount(newAlert);
        updateAlertIcon();
        updateAlertHeaderStyle();
    }

    private void updateAlertHeaderStyle() {
        AlertLevel alert = alertCounter.getAlert();

        if (alert == AlertLevel.RED) {
            updateAlertHeaderStyle(AlertAwareView.STYLE_RED, AlertAwareView.STYLE_YELLOW);
            return;
        }

        if (alert == AlertLevel.YELLOW) {
            updateAlertHeaderStyle(AlertAwareView.STYLE_YELLOW, AlertAwareView.STYLE_RED);
            return;
        }

        updateAlertHeaderStyle(null, AlertAwareView.STYLE_YELLOW, AlertAwareView.STYLE_RED);
    }

    private void updateAlertHeaderStyle(String newStyleClass, String... oldStyleClasses) {
        ObservableList<String> headerStyleClass = headerPane.getStyleClass();
        headerStyleClass.removeAll(oldStyleClasses);

        if (newStyleClass != null) {
            headerStyleClass.add(newStyleClass);
        }
    }

    private void updateAlertIcon() {
        String newAlertIconUrl = getAlertIconUrl();

        if (!isAlertIconUrlChanged(newAlertIconUrl)) {
            return;
        }

        Image image = ((newAlertIconUrl == null) ? (null) : (new Image(newAlertIconUrl)));
        imageHeaderAlert.imageProperty().setValue(image);
        imageHeaderAlert.setFitHeight(ScaleHelper.emsToPixels(1.1));
        imageHeaderAlert.setFitWidth(ScaleHelper.emsToPixels(1.1));
    }

    private boolean isAlertIconUrlChanged(String newAlertIconUrl) {
        Image currentAlertImage = imageHeaderAlert.imageProperty().get();
        String currentIconUrl = null;

        if (currentAlertImage != null) {
            currentIconUrl = currentAlertImage.getUrl();
        }

        if ((newAlertIconUrl == null) && (currentIconUrl == null)) {
            return false;
        }

        if ((newAlertIconUrl != null) && (currentIconUrl == null)) {
            return true;
        }

        return ((currentIconUrl != null) && (!currentIconUrl.equals(newAlertIconUrl)));
    }

    private String getAlertIconUrl() {
        AlertLevel alert = alertCounter.getAlert();
        return UavIconHelper.getAlertIconSvg(alert, true);
    }

    private AlertLevel getConnectionAlert(AirplaneConnectorState state) {
        if ((state == null) || (state == AirplaneConnectorState.unconnected)) {
            return AlertLevel.RED;
        }

        if (state == AirplaneConnectorState.fullyConnected) {
            return AlertLevel.GREEN;
        }

        return AlertLevel.YELLOW;
    }

    private String getConnectionImageUrl(AirplaneConnectorState state) {
        if ((state == null) || (state == AirplaneConnectorState.unconnected)) {
            return ICON_LINK_LOSS;
        }

        return ICON_LINK_GOOD;
    }

    private void updateNotImportantAlerts(ObservableMap<String, PlaneHealthChannelStatus> newAlerts) {
        ObservableList<Node> nodes = notImportantAlertsPane.getChildren();

        if ((newAlerts == null) || (newAlerts.isEmpty())) {
            notImportantAlerts.values().forEach(this::discardAlert);
            notImportantAlerts.clear();
            nodes.clear();
            return;
        }

        Map<String, PlaneHealthChannelStatus> alertsToAdd = Maps.newLinkedHashMap(newAlerts);
        Iterator<Node> nodeIterator = nodes.iterator();

        while (nodeIterator.hasNext()) {
            Node node = nodeIterator.next();

            if (updateAlertNode(node, alertsToAdd)) {
                continue;
            }

            nodeIterator.remove();
        }

        if (alertsToAdd.isEmpty()) {
            return;
        }

        alertsToAdd.values().stream().forEach(this::addAlertNode);
    }

    private boolean updateAlertNode(Node node, Map<String, PlaneHealthChannelStatus> alertsToAdd) {
        String id = node.getId();

        if (Strings.isNullOrEmpty(id)) {
            return true;
        }

        PlaneHealthChannelStatus channelStatus = alertsToAdd.get(id);
        IconAndLabelExtended alertComponent = notImportantAlerts.get(id);

        if (channelStatus == null) {
            if (alertComponent != null) {
                notImportantAlerts.remove(id);
                discardAlert(alertComponent);
            }

            return false;
        }

        if (alertComponent == null) {
            return false;
        }

        fillAlertComponent(channelStatus, alertComponent);

        alertsToAdd.remove(id);

        return true;
    }

    private void discardAlert(IconAndLabelExtended alertComponent) {
        alertComponent.setAlert(AlertLevel.GREEN);
    }

    private void fillAlertComponent(PlaneHealthChannelStatus channelStatus, IconAndLabelExtended alertComponent) {
        if (channelStatus.isFailEvent()) {
            alertComponent.setText(languageHelper.getString(KEY_FAILURE));
            alertComponent.setLeftLabel(viewModel.getLastFailEventDescription());
        } else {
            alertComponent.setText(channelStatus.getName());
            alertComponent.setLeftLabel(getHealthChannelStatusAsString(channelStatus));
        }

        AlertLevel alert = channelStatus.getAlert();
        alertComponent.setAlert(alert);
        alertComponent.setImageUrl(UavIconHelper.getAlertIconSvg(alert, true));
    }

    private String getHealthChannelStatusAsString(PlaneHealthChannelStatus channelStatus) {
        if (channelStatus.isFlightMode()) {
            String nameKey = IMC_FLIGHTMODE.values()[channelStatus.getAbsolute().intValue()].getDisplayNameKey();
            return languageHelper.getString(nameKey);
        }

        if (channelStatus.isFailEvent()) {
            return viewModel.getLastFailEventDescription();
        }

        return channelStatus.getAbsoluteAsString();
    }

    private void addAlertNode(PlaneHealthChannelStatus channelStatus) {
        IconAndLabelExtended alertComponent = new IconAndLabelExtended();
        alertComponent.setImageUrl(null);
        alertComponent.setSeparator("");
        alertComponent.setRightLabel("");

        fillAlertComponent(channelStatus, alertComponent);
        initAlertListener(alertComponent);

        String channelName = channelStatus.getName();
        notImportantAlerts.put(channelName, alertComponent);

        Parent alertView = alertComponent.getView();
        alertView.setId(channelName);
        notImportantAlertsPane.getChildren().add(alertView);
    }

    @FXML
    private void openRtkConnection(MouseEvent event) {
        viewModel.openRtkConnection();
    }
}
