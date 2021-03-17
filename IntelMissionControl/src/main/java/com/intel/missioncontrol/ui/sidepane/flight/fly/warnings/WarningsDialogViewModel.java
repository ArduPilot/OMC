/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight.fly.warnings;

import com.google.inject.Inject;
import com.intel.missioncontrol.ui.ViewModelBase;
import com.intel.missioncontrol.ui.validation.IValidationService;
import com.intel.missioncontrol.ui.validation.ResolvableValidationMessage;
import de.saxsys.mvvmfx.utils.validation.Severity;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

public class WarningsDialogViewModel extends ViewModelBase {

    public ObservableList<ResolvableValidationMessage> getWarnings() {
        return warnings.get();
    }

    private ListProperty<ResolvableValidationMessage> warnings =
        new SimpleListProperty<>(FXCollections.observableArrayList());

    @Inject
    private IValidationService validationService;

    @Override
    protected void initializeViewModel() {
        super.initializeViewModel();

        setWarnings(validationService.flightValidationMessagesProperty());
        validationService
            .flightValidationMessagesProperty()
            .addListener((ListChangeListener<ResolvableValidationMessage>)change -> setWarnings(change.getList()));
    }

    private void setWarnings(ObservableList<? extends ResolvableValidationMessage> listWarnings) {
        warnings.setAll(listWarnings.filtered(message -> message.getSeverity() == Severity.WARNING));
    }

    ListProperty<ResolvableValidationMessage> warningsProperty() {
        return warnings;
    }
}
