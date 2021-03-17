/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight.checklist;

import com.google.inject.Inject;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.ui.ViewModelBase;
import com.intel.missioncontrol.ui.navbar.connection.UavConnectionScope;
import com.intel.missioncontrol.ui.navbar.connection.UnmannedAerialVehicle;
import com.intel.missioncontrol.ui.validation.IValidationService;
import com.intel.missioncontrol.ui.validation.ResolvableValidationMessage;
import de.saxsys.mvvmfx.InjectScope;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyListProperty;

public class PreFlightChecksViewModel extends ViewModelBase {

    @Inject
    private IValidationService validationService;

    @InjectScope
    private UavConnectionScope uavConnectionScope;

    @Inject
    private ILanguageHelper languageHelper;

    public ReadOnlyListProperty<ResolvableValidationMessage> validationMessagesProperty() {
        return validationService.flightValidationMessagesProperty();
    }

    public ObjectProperty<UnmannedAerialVehicle> selectedUavProperty() {
        return uavConnectionScope.selectedUavProperty();
    }

    public String getTextByKey(String key) {
        return languageHelper.getString(key);
    }

}
