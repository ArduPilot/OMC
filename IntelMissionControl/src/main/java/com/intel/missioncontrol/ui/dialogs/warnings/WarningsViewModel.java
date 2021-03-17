/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.dialogs.warnings;

import com.intel.missioncontrol.ui.dialogs.DialogViewModel;
import com.intel.missioncontrol.ui.navigation.INavigationService;
import com.intel.missioncontrol.ui.navigation.WorkflowStep;
import com.intel.missioncontrol.ui.validation.IValidationService;
import com.intel.missioncontrol.ui.validation.ValidationMessageCategory;
import com.intel.missioncontrol.ui.validation.ResolvableValidationMessage;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;

public class WarningsViewModel<TDialogResult> extends DialogViewModel<TDialogResult, Void> {

    private final ObservableList<WarningItemViewModel> warnings = FXCollections.observableArrayList();
    private final ObservableList<WarningItemViewModel> importantWarnings = FXCollections.observableArrayList();

    private final IValidationService validationService;
    private final INavigationService navigationService;
    private final ListChangeListener<ResolvableValidationMessage> listChangeListener = change -> refreshList();

    public WarningsViewModel(IValidationService validationService, INavigationService navigationService) {
        this.validationService = validationService;
        this.navigationService = navigationService;
    }

    @Override
    protected void initializeViewModel() {
        super.initializeViewModel();

        ObservableList<ResolvableValidationMessage> messages = getMessages(navigationService.getWorkflowStep());
        messages.addListener(new WeakListChangeListener<>(listChangeListener));
        refreshList();
    }

    public ReadOnlyListProperty<WarningItemViewModel> warningsProperty() {
        return new ReadOnlyListWrapper<>(warnings);
    }

    public ReadOnlyListProperty<WarningItemViewModel> importantWarningsProperty() {
        return new ReadOnlyListWrapper<>(importantWarnings);
    }

    protected INavigationService getNavigationService() {
        return navigationService;
    }

    private ObservableList<ResolvableValidationMessage> getMessages(WorkflowStep workflowStep) {
        if (workflowStep == WorkflowStep.PLANNING) {
            return validationService.planningValidationMessagesProperty();
        } else if (workflowStep == WorkflowStep.FLIGHT) {
            return validationService.flightValidationMessagesProperty();
        } else {
            return validationService.datasetValidationMessagesProperty();
        }
    }

    private void refreshList() {
        List<WarningItemViewModel> list = new ArrayList<>();
        List<WarningItemViewModel> importantWarnings = new ArrayList<>();

        for (ResolvableValidationMessage message : getMessages(navigationService.getWorkflowStep())) {
            list.add(new WarningItemViewModel(message));
            if (!message.getCategory().equals(ValidationMessageCategory.NOTICE)) {
                importantWarnings.add(new WarningItemViewModel(message));
            }
        }
        this.warnings.setAll(list);
        this.importantWarnings.setAll(importantWarnings);
    }

}
