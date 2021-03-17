/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.dialogs.preflightchecks;

import com.google.inject.Inject;
import com.intel.missioncontrol.ui.dialogs.DialogViewModel;
import com.intel.missioncontrol.ui.validation.IValidationService;
import com.intel.missioncontrol.ui.validation.ResolvableValidationMessage;
import com.intel.missioncontrol.ui.validation.ResolvableValidationStatus;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;

public class AutomaticChecksDialogViewModel extends DialogViewModel {

    private final ObservableList<AutoCheckItemViewModel> warnings = FXCollections.observableArrayList();
    private final IValidationService validationService;
    private final ListChangeListener<ResolvableValidationStatus> listChangeListener = change -> refreshList();

    @Inject
    public AutomaticChecksDialogViewModel(IValidationService validationService) {
        this.validationService = validationService;
    }

    @Override
    protected void initializeViewModel() {
        super.initializeViewModel();

        ObservableList<ResolvableValidationStatus> messages = getStatuses();
        messages.addListener(new WeakListChangeListener<>(listChangeListener));

        refreshList();
    }

    public ReadOnlyListProperty<AutoCheckItemViewModel> warningsProperty() {
        return new ReadOnlyListWrapper<>(warnings);
    }

    private ObservableList<ResolvableValidationStatus> getStatuses() {
        return validationService.flightAndPlanAllValidationStatusesProperty();
    }

    private void refreshList() {
        List<AutoCheckItemViewModel> list = new ArrayList<>();
        for (ResolvableValidationStatus status : getStatuses()) {
            //just adding info about the progress to the viewmodel in any case
            boolean inProgress = status.isRunningProperty().get();
            if (status.getResolveableMessages().isEmpty() && !status.getOkMessage().get().isEmpty()) {
                list.add(new AutoCheckItemViewModel(status.getOkMessage().get(), inProgress));
            }
            else {
                for (ResolvableValidationMessage message : status.getResolveableMessages()) {
                    list.add(new AutoCheckItemViewModel(message, inProgress));
                }
            }
        }

        this.warnings.setAll(list);
    }
}
