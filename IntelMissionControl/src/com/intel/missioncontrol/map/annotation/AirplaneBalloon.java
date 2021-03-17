/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.annotation;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.intel.missioncontrol.hardware.IPlatformDescription;
import com.intel.missioncontrol.helper.Expect;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.helper.ScaleHelper;
import com.intel.missioncontrol.mission.Uav;
import com.intel.missioncontrol.ui.sidepane.flight.AlertCounter;
import com.intel.missioncontrol.ui.sidepane.flight.AlertLevel;
import com.intel.missioncontrol.ui.sidepane.flight.IMC_FLIGHTMODE;
import com.intel.missioncontrol.ui.sidepane.flight.PlaneHealth.PlaneHealthChannel;
import com.intel.missioncontrol.ui.sidepane.flight.PlaneHealth.PlaneHealthChannelInfo;
import com.intel.missioncontrol.ui.sidepane.flight.PlaneHealth.PlaneHealthChannelStatus;
import com.intel.missioncontrol.ui.sidepane.flight.UavIconHelper;
import com.intel.missioncontrol.ui.sidepane.planning.emergency.EventHelper;
import eu.mavinci.core.helper.StringHelper;
import eu.mavinci.core.plane.AirplaneCacheEmptyException;
import eu.mavinci.core.plane.AirplaneFlightmode;
import eu.mavinci.core.plane.AirplaneFlightphase;
import eu.mavinci.desktop.gui.doublepanel.planemain.wwd.GlobeBaloonAnnotation;
import eu.mavinci.plane.IAirplane;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.Annotation;
import gov.nasa.worldwind.render.AnnotationAttributes;
import gov.nasa.worldwind.render.AnnotationFlowLayout;
import gov.nasa.worldwind.render.ScreenAnnotation;
import gov.nasa.worldwindx.examples.util.ProgressAnnotation;
import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.util.List;
import java.util.Map;
import javafx.beans.property.Property;
import javafx.collections.ObservableMap;

public class AirplaneBalloon extends GlobeBaloonAnnotation {

    private static final double LINE_OF_SIGHT_SAFE_MARGIN = 20.0;
    private static final int DEFAULT_WIDTH = (int)ScaleHelper.emsToPixels(13);

    private static final String KEY_LINE_OF_SIGHT =
        "com.intel.missioncontrol.map.annotation.AirplaneBalloon.lineOfSightAnnotation";
    private static final String KEY_TIME_LEFT = "com.intel.missioncontrol.map.annotation.AirplaneBalloon.timeLeft";

    private static final AnnotationFlowLayout LAYOUT_HORIZONTAL =
        new AnnotationFlowLayout(AVKey.HORIZONTAL, AVKey.LEFT, 1, 5);

    private static final double ELEVATION = 50;
    private static final LatLon START_POSITION = LatLon.fromDegrees(49.246930, 8.641546);

    private final Uav uav;
    private final ILanguageHelper languageHelper;
    private final AlertCounter alertCounter = new AlertCounter();
    private final EventHelper eventHelper;

    private Annotation planeNameAnnotation;
    private ImageAndLabelAnnotation batteryAnnotation;
    private ImageAndLabelAnnotation phaseAnnotation;
    private ImageAndLabelAnnotation timeAnnotation;
    private ProgressAnnotation progressAnnotation;
    private ImageAndLabelAnnotation lineOfSightAnnotation;
    private Annotation alertsAnnotation;

    public AirplaneBalloon(Uav uav, ILanguageHelper languageHelper) {
        super("", new Position(START_POSITION, ELEVATION));

        Expect.notNull(uav, "uav");
        Expect.notNull(languageHelper, "languageHelper");

        this.uav = uav;
        this.languageHelper = languageHelper;
        this.eventHelper = new EventHelper(languageHelper);

        initAnnotation();
        connectUav();

        firePropertyChange(AVKey.LAYER, null, this);
    }

    private void initAnnotation() {
        setAltitudeMode(WorldWind.ABSOLUTE);
        setAlwaysOnTop(true);
        setLayout(new AnnotationFlowLayout(AVKey.VERTICAL, AVKey.LEFT, 10, 5));

        getAttributes().setDefaults(createDefaultAttributes());

        initPosition();
        buildAnnotation();
        initAlertListeners();
    }

    private void initPosition() {
        try {
            LatLon currentPosition = uav.getCurrentPositionLatLon();

            if (currentPosition == null) {
                currentPosition = LatLon.ZERO;
            }

            double currentElevation = uav.getCurrentElevation();
            setPosition(new Position(currentPosition, currentElevation));
        } catch (AirplaneCacheEmptyException ex) {
            // AirplaneCache is empty
        }
    }

    private void connectUav() {
        uav.batteryPercentageProperty()
            .addListener((observable, oldValue, newValue) -> batteryAnnotation.setRightAnnotationText(newValue));

        uav.batteryPercentageValueProperty().addListener((observable, oldValue, newValue) -> updateBattery(newValue));

        uav.batteryVoltageProperty()
            .addListener((observable, oldValue, newValue) -> batteryAnnotation.setLeftAnnotationText(newValue));

        uav.flightPhaseProperty()
            .addListener(
                (observable, oldValue, newValue) -> updateFlightPhase(newValue, uav.flightModeProperty().get()));
        uav.flightModeProperty()
            .addListener(
                (observable, oldValue, newValue) -> updateFlightPhase(uav.flightPhaseProperty().get(), newValue));
        uav.positionProperty().addListener((observable, oldValue, newValue) -> updatePositionAndProgress(newValue));
        uav.lineOfSightProperty().addListener((observable, oldValue, newValue) -> updateLineOfSight(newValue));
        uav.alertsProperty().addListener((observable, oldValue, newValue) -> updateAlerts(newValue));

        uav.connectionStateProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    updatePlaneName();
                    IAirplane airplane = uav.getLegacyPlane();
                    if (airplane != null) {
                        airplane.requestFlightPhase();
                        airplane.requestPlaneInfo();
                    }
                });
    }

    private void updateBattery(Number percent) {
        Number voltage = uav.batteryVoltageValueProperty().getValue();
        PlaneHealthChannelInfo batteryInfo = uav.getHealthChannelInfo(PlaneHealthChannel.BATTERY_MAIN);
        String batteryIcon = UavIconHelper.getBatteryIconPng(voltage, percent, batteryInfo, false);
        batteryAnnotation.setImageSource(batteryIcon);
        AlertLevel batteryAlert = uav.getHealthAlert(voltage, PlaneHealthChannel.BATTERY_MAIN);
        batteryAnnotation.alertProperty().setValue(batteryAlert);
    }

    private void updateFlightPhase(AirplaneFlightphase flightPhase, AirplaneFlightmode flightMode) {
        String phase = "N/A";

        if (flightMode != null && flightMode != AirplaneFlightmode.AutomaticFlight) {
            phase = languageHelper.getString(flightMode.getDisplayNameKey());
        } else {
            if (flightPhase != null) {
                phase = languageHelper.getString(flightPhase.getDisplayNameKey());
            }
        }

        phaseAnnotation.setLeftAnnotationText(phase);

        String phaseIcon = UavIconHelper.getFlightPhaseIconPng(flightPhase, flightMode);
        phaseAnnotation.setImageSource(phaseIcon);

        updatePlaneName();
    }

    private void updatePositionAndProgress(Position position) {
        moveTo(position);

        double airborneTimeSeconds = uav.getAirborneTimeSeconds();
        String airborneTimeString = StringHelper.secToShortDHMS(airborneTimeSeconds);

        timeAnnotation.setLeftAnnotationText(airborneTimeString);
        timeAnnotation.setRightAnnotationText(languageHelper.getString(KEY_TIME_LEFT, uav.getTimeLeft()));

        double progress = uav.getFlightPlanProgress() * 100.0;

        if (progress > 100.0) {
            progress = 100.0;
        } else if (progress < 0.0) {
            progress = 0.0;
        }

        progressAnnotation.setValue(progress);
    }

    private void updateLineOfSight(Number lineOfSightValue) {
        if (lineOfSightValue == null) {
            return;
        }

        double lineOfSight = lineOfSightValue.doubleValue();
        String lineOfSightString = StringHelper.lengthToIngName(lineOfSight, -3, false);
        lineOfSightAnnotation.setRightAnnotationText(lineOfSightString);
        AlertLevel lineOfSightAlert = getLineOfSightAlert(lineOfSight);
        lineOfSightAnnotation.alertProperty().setValue(lineOfSightAlert);
        lineOfSightAnnotation.setImageSource(UavIconHelper.getAlertIconPng(lineOfSightAlert, false));
    }

    private AlertLevel getLineOfSightAlert(double lineOfSight) {
        double maxLineOfSight = uav.getMaxLineOfSight();

        if (lineOfSight < (maxLineOfSight - LINE_OF_SIGHT_SAFE_MARGIN)) {
            return AlertLevel.GREEN;
        }

        if (lineOfSight < maxLineOfSight) {
            return AlertLevel.YELLOW;
        }

        return AlertLevel.RED;
    }

    private void updatePlaneName() {
        IPlatformDescription platformDescription = uav.getPlatformDescription();

        if (platformDescription == null) {
            return;
        }

        String name = platformDescription.getName();

        if (!Strings.isNullOrEmpty(name)) {
            planeNameAnnotation.setText(name);
        }
    }

    private AnnotationAttributes createDefaultAttributes() {
        AnnotationAttributes defaultAttributes = new AnnotationAttributes();

        defaultAttributes.setCornerRadius(ScaleHelper.scalePixelsAsInt(10));
        defaultAttributes.setInsets(ScaleHelper.scaleInsets(new Insets(5, 10, 5, 10)));
        // TODO need design
        // defaultAttributes.setBackgroundColor(new Color(0f, 0f, 0f, 0.9f));
        // defaultAttributes.setTextColor(Color.WHITE);
        defaultAttributes.setAdjustWidthToText(AVKey.SIZE_FIXED);
        defaultAttributes.setTextAlign(AVKey.LEFT);
        defaultAttributes.setSize(new java.awt.Dimension(DEFAULT_WIDTH, 0));
        defaultAttributes.setVisible(true);
        defaultAttributes.setHighlighted(true);

        return defaultAttributes;
    }

    private void buildAnnotation() {
        removeAllChildren();

        createPlaneNameAnnotation();

        createBatteryAnnotation();
        createPhaseAnnotation();
        createTimeAnnotation();
        createLineOfSightAnnotation();
        createProgressAnnotation();
        createAlertAnnotation();
    }

    private void createPlaneNameAnnotation() {
        planeNameAnnotation = createScreenAnnotation();
        planeNameAnnotation.setText("UAV");

        Font planeNameFont = planeNameAnnotation.getAttributes().getFont();
        Font planeNameFontBold = new Font(planeNameFont.getName(), Font.BOLD, (int)ScaleHelper.emsToPixels(1));
        planeNameAnnotation.getAttributes().setFont(planeNameFontBold);

        addChild(planeNameAnnotation);
    }

    private void createBatteryAnnotation() {
        batteryAnnotation = new ImageAndLabelAnnotation();

        batteryAnnotation.setImageSource(UavIconHelper.getBatteryIconPng(0.0, 0.0, null, false));
        batteryAnnotation.setLeftAnnotationText("--");
        batteryAnnotation.setRightAnnotationText("--");

        addChild(batteryAnnotation);
    }

    private void createPhaseAnnotation() {
        phaseAnnotation = new ImageAndLabelAnnotation();

        phaseAnnotation.setImageSource(
            UavIconHelper.getFlightPhaseIconPng(AirplaneFlightphase.ground, AirplaneFlightmode.AutomaticFlight));
        phaseAnnotation.setLeftAnnotationText("N/A");
        phaseAnnotation.setLeftAnnotationBold(true);

        addChild(phaseAnnotation);
    }

    public void createTimeAnnotation() {
        timeAnnotation = new ImageAndLabelAnnotation();

        timeAnnotation.setLeftAnnotationText("0:00");
        timeAnnotation.setRightAnnotationText(languageHelper.getString(KEY_TIME_LEFT, "0:00"));
        timeAnnotation.setRightAnnotationBold(false);

        addChild(timeAnnotation);
    }

    private void createProgressAnnotation() {
        progressAnnotation = new ProgressAnnotation(0.0, 0.0, 100.0);
        progressAnnotation.getInteriorInsets().set(0, 0, 0, 0);
        progressAnnotation.setLayout(new AnnotationFlowLayout(AVKey.HORIZONTAL, AVKey.CENTER, 4, 4));

        AnnotationAttributes progressAttributes = new AnnotationAttributes();

        Color transparentBlack = new Color(0f, 0f, 0f, 0f);

        progressAttributes.setBackgroundColor(transparentBlack);
        progressAttributes.setBorderColor(transparentBlack);
        progressAttributes.setBorderWidth(0);
        progressAttributes.setCornerRadius(0);
        progressAttributes.setDrawOffset(new java.awt.Point(0, 0));

        progressAttributes.setSize(new java.awt.Dimension(DEFAULT_WIDTH, (int)ScaleHelper.emsToPixels(0.8)));
        progressAttributes.setBorderColor(Color.WHITE);
        progressAttributes.setBorderWidth(1);
        progressAttributes.setInsets(new Insets(3, 0, 0, 3));
        progressAttributes.setAdjustWidthToText(AVKey.SIZE_FIT_TEXT);

        progressAnnotation.getAttributes().setDefaults(progressAttributes);
        progressAnnotation.setPickEnabled(false);

        addChild(progressAnnotation);
    }

    public void createLineOfSightAnnotation() {
        lineOfSightAnnotation = new ImageAndLabelAnnotation();

        lineOfSightAnnotation.setLeftAnnotationText(languageHelper.getString(KEY_LINE_OF_SIGHT));
        lineOfSightAnnotation.setRightAnnotationText("--");

        addChild(lineOfSightAnnotation);
    }

    private void createAlertAnnotation() {
        alertsAnnotation = createScreenAnnotation();
        alertsAnnotation.setLayout(new AnnotationFlowLayout(AVKey.VERTICAL, AVKey.LEFT, 1, 5));

        addChild(alertsAnnotation);
    }

    private Annotation createScreenAnnotation() {
        Annotation annotation = new ScreenAnnotation("", new java.awt.Point());
        annotation.setPickEnabled(false);
        annotation.setLayout(LAYOUT_HORIZONTAL);

        AnnotationAttributes attributes = new AnnotationAttributes();
        // TODO need design
        // Color transparentBlack = new Color(0f, 0f, 0f, 0f);
        // attributes.setBackgroundColor(transparentBlack);
        // attributes.setTextColor(Color.WHITE);
        attributes.setAdjustWidthToText(AVKey.SIZE_FIT_TEXT);
        // attributes.setSize(new java.awt.Dimension(40, 0));
        // attributes.setBorderColor(transparentBlack);
        attributes.setBorderWidth(0);
        attributes.setCornerRadius(0);
        attributes.setDrawOffset(new java.awt.Point(0, 0));
        attributes.setInsets(new java.awt.Insets(0, 0, 0, 0));
        attributes.setEffect(AVKey.TEXT_EFFECT_NONE);

        annotation.getAttributes().setDefaults(attributes);

        return annotation;
    }

    private void updateAlerts(ObservableMap<String, PlaneHealthChannelStatus> newAlerts) {
        List<? extends Annotation> alertAnnotations = alertsAnnotation.getChildren();

        if ((newAlerts == null) || (newAlerts.isEmpty())) {
            alertAnnotations.forEach(this::discardAlertAnnotation);
            alertsAnnotation.removeAllChildren();
            return;
        }

        Map<String, PlaneHealthChannelStatus> alertsToAdd = Maps.newLinkedHashMap(newAlerts);
        List<Annotation> alertAnnotationsToRemove = Lists.newLinkedList();

        for (Annotation alertAnnotation : alertAnnotations) {
            if (updateAlertAnnotation(alertAnnotation, alertsToAdd)) {
                continue;
            }

            alertAnnotationsToRemove.add(alertAnnotation);
            discardAlertAnnotation(alertAnnotation);
        }

        if (!alertAnnotationsToRemove.isEmpty()) {
            alertAnnotationsToRemove.forEach(alertsAnnotation::removeChild);
        }

        if (alertsToAdd.isEmpty()) {
            return;
        }

        alertsToAdd.values().stream().forEach(this::addAlertAnnotation);
    }

    private void discardAlertAnnotation(Annotation alertAnnotation) {
        if (alertAnnotation instanceof ImageAndLabelAnnotation) {
            ((ImageAndLabelAnnotation)alertAnnotation).alertProperty().setValue(AlertLevel.GREEN);
        }
    }

    private boolean updateAlertAnnotation(Annotation annotation, Map<String, PlaneHealthChannelStatus> alertsToAdd) {
        if (!(annotation instanceof ImageAndLabelAnnotation)) {
            return true;
        }

        ImageAndLabelAnnotation alertAnnotation = (ImageAndLabelAnnotation)annotation;
        String id = alertAnnotation.getId();

        if (Strings.isNullOrEmpty(id)) {
            return true;
        }

        PlaneHealthChannelStatus channelStatus = alertsToAdd.get(id);

        if (channelStatus == null) {
            return false;
        }

        fillAlertAnnotation(channelStatus, alertAnnotation);

        alertsToAdd.remove(id);

        return true;
    }

    private void fillAlertAnnotation(PlaneHealthChannelStatus channelStatus, ImageAndLabelAnnotation alertAnnotation) {
        if (channelStatus.isFailEvent()) {
            String eventDescription = eventHelper.getEventDescription(uav.getLastFailEvent());
            alertAnnotation.setLeftAnnotationText(eventDescription);
            alertAnnotation.setLeftAnnotationBold(true);
            alertAnnotation.setRightAnnotationText("");
        } else {
            alertAnnotation.setLeftAnnotationText(channelStatus.getName() + ":");
            alertAnnotation.setRightAnnotationText(getHealthChannelStatusAsString(channelStatus));
        }

        AlertLevel alert = channelStatus.getAlert();
        alertAnnotation.alertProperty().setValue(alert);
        alertAnnotation.setImageSource(UavIconHelper.getAlertIconPng(alert, false));
    }

    private String getHealthChannelStatusAsString(PlaneHealthChannelStatus channelStatus) {
        if (channelStatus.isGpsQuality()) {
            return getGpsQualityAsString(channelStatus);
        }

        if (channelStatus.isFlightMode()) {
            String nameKey = IMC_FLIGHTMODE.values()[channelStatus.getAbsolute().intValue()].getDisplayNameKey();
            return languageHelper.getString(nameKey);
        }

        return channelStatus.getAbsoluteAsString();
    }

    private String getGpsQualityAsString(PlaneHealthChannelStatus channelStatus) {
        return languageHelper.toFriendlyName(channelStatus.getGpsQuality());
    }

    private void addAlertAnnotation(PlaneHealthChannelStatus channelStatus) {
        // battery is already on balloon
        if (channelStatus.isMainBattery()) {
            return;
        }

        ImageAndLabelAnnotation alertAnnotation = new ImageAndLabelAnnotation(channelStatus.getName());
        fillAlertAnnotation(channelStatus, alertAnnotation);
        initAlertListener(alertAnnotation);

        alertsAnnotation.addChild(alertAnnotation);
    }

    private void initAlertListeners() {
        initAlertListener(batteryAnnotation);
        initAlertListener(lineOfSightAnnotation);
    }

    private void initAlertListener(ImageAndLabelAnnotation annotation) {
        Property<AlertLevel> alertProperty = annotation.alertProperty();
        alertProperty.addListener((observable, oldValue, newValue) -> updateAlertCount(oldValue, newValue));

        // initial alert
        updateAlertCount(null, alertProperty.getValue());
    }

    private void updateAlertCount(AlertLevel oldAlert, AlertLevel newAlert) {
        alertCounter.decrementAlertCount(oldAlert);
        alertCounter.incrementAlertCount(newAlert);

        updateAlertStyle();
    }

    private void updateAlertStyle() {
        AlertLevel alert = alertCounter.getAlert();

        if (alert == AlertLevel.RED) {
            getAttributes().setBorderColor(Color.RED);
            getAttributes().setBorderWidth(2.0);

            return;
        }

        if (alert == AlertLevel.YELLOW) {
            getAttributes().setBorderColor(Color.ORANGE);
            getAttributes().setBorderWidth(2.0);

            return;
        }

        getAttributes().setBorderColor(null);
        getAttributes().setBorderWidth(0.0);
    }
}
