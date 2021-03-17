/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.dialogs.preflightchecks;

import com.google.inject.Inject;
import com.intel.missioncontrol.helper.LanguageHelper;
import com.intel.missioncontrol.measure.Dimension;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.beans.property.QuantityProperty;
import com.intel.missioncontrol.beans.property.SimpleQuantityProperty;
import com.intel.missioncontrol.measure.UnitInfo;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.ui.dialogs.DialogViewModel;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;

public class SetupEmergencyProceduresViewModel extends DialogViewModel {

    private ListProperty<String> emergencyProcedures = new SimpleListProperty<>(FXCollections.observableArrayList());
    private ListProperty<String> backupControllers = new SimpleListProperty<>(FXCollections.observableArrayList());
    private StringProperty emergencyProcedureSelected = new SimpleStringProperty();
    private StringProperty backupControllerSelected = new SimpleStringProperty();
    private QuantityProperty<Dimension.Length> safetyAltitudeQuantity;
    private final DoubleProperty safetyAltitudeMeterProperty = new SimpleDoubleProperty();

    private final LanguageHelper languageHelper;
    private GeneralSettings settings;

    @Inject
    public SetupEmergencyProceduresViewModel(LanguageHelper languageHelper, ISettingsManager settingsManager) {
        this.languageHelper = languageHelper;
        this.settings = settingsManager.getSection(GeneralSettings.class);
    }

    @Override
    protected void initializeViewModel() {
        super.initializeViewModel();

        safetyAltitudeQuantity =
            new SimpleQuantityProperty<>(
                settings, UnitInfo.LOCALIZED_LENGTH, Quantity.of(safetyAltitudeMeterProperty.getValue(), Unit.METER));

        emergencyProcedures.clear();
        emergencyProcedures.addAll(
            languageHelper.getString(
                "com.intel.missioncontrol.ui.dialogs.preflightChecks.SetupEmergencyProceduresView.emergencyAction.returnHomeatFixedSafetyAltitude"),
            languageHelper.getString(
                "com.intel.missioncontrol.ui.dialogs.preflightChecks.SetupEmergencyProceduresView.emergencyAction.returnHomeatMaxReachedAltitude"),
            languageHelper.getString(
                "com.intel.missioncontrol.ui.dialogs.preflightChecks.SetupEmergencyProceduresView.emergencyAction.holdposition"));

        emergencyProcedureSelected.set(
            languageHelper.getString(
                "com.intel.missioncontrol.ui.dialogs.preflightChecks.SetupEmergencyProceduresView.emergencyAction.returnHomeatFixedSafetyAltitude"));

        backupControllers.clear();
        backupControllers.addAll(
            languageHelper.getString(
                "com.intel.missioncontrol.ui.dialogs.preflightChecks.SetupEmergencyProceduresView.backupControllers.allAvailable"));
        backupControllerSelected.set(
            languageHelper.getString(
                "com.intel.missioncontrol.ui.dialogs.preflightChecks.SetupEmergencyProceduresView.backupControllers.allAvailable"));
    }

    public ReadOnlyListProperty<String> emergencyProceduresProperty() {
        return emergencyProcedures;
    }

    public ReadOnlyListProperty<String> backupControllersProperty() {
        return backupControllers;
    }

    public ReadOnlyStringProperty emergencyProcedureSelectedProperty() {
        return emergencyProcedureSelected;
    }

    public ReadOnlyStringProperty backupControllerSelectedProperty() {
        return backupControllerSelected;
    }

    public QuantityProperty<Dimension.Length> safetyAltitudeProperty() {
        return safetyAltitudeQuantity;
    }

}
