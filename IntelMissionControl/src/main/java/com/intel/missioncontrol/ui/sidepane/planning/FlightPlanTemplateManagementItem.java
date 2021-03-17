/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.planning;

import com.intel.missioncontrol.flightplantemplate.FlightPlanTemplate;
import com.intel.missioncontrol.hardware.IGenericCameraConfiguration;
import com.intel.missioncontrol.hardware.IGenericCameraDescription;
import com.intel.missioncontrol.hardware.IHardwareConfiguration;
import com.intel.missioncontrol.hardware.ILensConfiguration;
import com.intel.missioncontrol.hardware.ILensDescription;
import com.intel.missioncontrol.hardware.IPlatformDescription;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.mission.FlightPlan;
import eu.mavinci.flightplan.Flightplan;
import java.util.Optional;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableObjectValue;

public class FlightPlanTemplateManagementItem {

    private static final String UNKNOWN_VALUE_KEY =
        "com.intel.missioncontrol.ui.planning.FlightPlanTemplateManagerView.unknown.value";

    private StringProperty fpName = new SimpleStringProperty();
    private StringProperty uav = new SimpleStringProperty();
    private StringProperty camera = new SimpleStringProperty();
    private StringProperty lens = new SimpleStringProperty();
    private StringProperty aois = new SimpleStringProperty();
    private BooleanProperty buildIn = null;

    private ILanguageHelper languageHelper;

    private FlightPlanTemplate fpTemplate;

    public FlightPlanTemplateManagementItem(FlightPlanTemplate fpTemplate, ILanguageHelper languageHelper) {
        this.fpTemplate = fpTemplate;
        this.languageHelper = languageHelper;
    }

    public String getAois() {
        return aoisProperty().get();
    }

    public ReadOnlyStringProperty aoisProperty() {
        if (aois.get() == null) {
            refreshAois();
        }

        return aois;
    }

    private void refreshAois() {
        aois.setValue(
            Optional.ofNullable(fpTemplate)
                .map(FlightPlanTemplate::getFlightPlan)
                .map(FlightPlan::areasOfInterestProperty)
                .map(ObservableObjectValue::get)
                .map(
                    aoiList -> {
                        final int count = aoiList.size();
                        switch (count) {
                        case 0:
                            return "";
                        case 1:
                            return aoiList.get(0).getLocalizedTypeName(languageHelper);
                        default:
                            return languageHelper.getString(
                                "com.intel.missioncontrol.ui.planning.FlightPlanTemplateManagerView.table.column.aois,value.pattern",
                                count);
                        }
                    })
                .orElse(languageHelper.getString(UNKNOWN_VALUE_KEY)));
    }

    public boolean getBuildIn() {
        return buildInProperty().get();
    }

    public ReadOnlyBooleanProperty buildInProperty() {
        if (buildIn == null) {
            buildIn = new SimpleBooleanProperty();
            refreshBuildIn();
        }

        return buildIn;
    }

    private void refreshBuildIn() {
        buildIn.set(Optional.ofNullable(fpTemplate).map(FlightPlanTemplate::isSystem).orElse(false));
    }

    public String getCamera() {
        return cameraProperty().get();
    }

    public ReadOnlyStringProperty cameraProperty() {
        if (camera.get() == null) {
            refreshCamera();
        }

        return camera;
    }

    private void refreshCamera() {
        camera.setValue(
            Optional.ofNullable(fpTemplate)
                .map(FlightPlanTemplate::getFlightPlan)
                .map(FlightPlan::getLegacyFlightplan)
                .map(Flightplan::getHardwareConfiguration)
                // .filter(IHardwareConfiguration::initialized)
                .map(
                    hardwareConfig ->
                        hardwareConfig.getPrimaryPayload(IGenericCameraConfiguration.class).getDescription())
                .map(IGenericCameraDescription::getName)
                .orElse(languageHelper.getString(UNKNOWN_VALUE_KEY)));
    }

    public String getFpName() {
        return fpNameProperty().get();
    }

    public ReadOnlyStringProperty fpNameProperty() {
        if (fpName.get() == null) {
            refreshFpName();
        }

        return fpName;
    }

    private void refreshFpName() {
        fpName.setValue(Optional.ofNullable(fpTemplate).map(FlightPlanTemplate::getName).orElse(""));
    }

    public String getLens() {
        return lensProperty().get();
    }

    public ReadOnlyStringProperty lensProperty() {
        if (lens.get() == null) {
            refreshLens();
        }

        return lens;
    }

    private void refreshLens() {
        lens.setValue(
            Optional.ofNullable(fpTemplate)
                .map(FlightPlanTemplate::getFlightPlan)
                .map(FlightPlan::getLegacyFlightplan)
                .map(Flightplan::getHardwareConfiguration)
                // .filter(IHardwareConfiguration::initialized)
                .map(hardwareConfig -> hardwareConfig.getPrimaryPayload(IGenericCameraConfiguration.class))
                .map(IGenericCameraConfiguration::getLens)
                .map(ILensConfiguration::getDescription)
                .map(ILensDescription::getName)
                .orElse(languageHelper.getString(UNKNOWN_VALUE_KEY)));
    }

    public String getUav() {
        return uavProperty().get();
    }

    public ReadOnlyStringProperty uavProperty() {
        if (uav.get() == null) {
            refreshUav();
        }

        return uav;
    }

    private void refreshUav() {
        uav.setValue(
            Optional.ofNullable(fpTemplate)
                .map(FlightPlanTemplate::getFlightPlan)
                .map(FlightPlan::getLegacyFlightplan)
                .map(Flightplan::getHardwareConfiguration)
                .map(IHardwareConfiguration::getPlatformDescription)
                .map(IPlatformDescription::getName)
                .orElse(languageHelper.getString(UNKNOWN_VALUE_KEY)));
    }

    public FlightPlanTemplate getFpTemplate() {
        return fpTemplate;
    }

    public void refresh() {
        refreshLens();
        refreshUav();
        refreshFpName();
        refreshCamera();
        refreshBuildIn();
        refreshAois();
    }
}
