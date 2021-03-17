/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.notifications;

import com.google.inject.Inject;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.ui.ViewModelBase;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ToastStackViewModel extends ViewModelBase {

    private ListProperty<ToastViewModel> toasts = new SimpleListProperty<>(FXCollections.observableArrayList());

    private final IApplicationContext applicationContext;

    @Inject
    public ToastStackViewModel(IApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        applicationContext.toastsProperty().addListener(((observable, oldValue, newValue) -> toastsChanged(newValue)));
    }

    public ReadOnlyListProperty<ToastViewModel> toastsProperty() {
        return toasts;
    }

    private void toastsChanged(ObservableList<Toast> newToasts) {
        List<ToastViewModel> viewModels = new ArrayList<>();

        int i = 0;
        int j = 0;
        while (i < toasts.size() && j < newToasts.size()) {
            if (toasts.get(i).getToast() == newToasts.get(j)) {
                viewModels.add(toasts.get(i));
                ++j;
            }

            ++i;
        }

        while (j < newToasts.size()) {
            viewModels.add(new ToastViewModel(newToasts.get(j), this::toastClosed));
            ++j;
        }

        for (ToastViewModel vm : toasts) {
            if (!viewModels.contains(vm)) {
                vm.dismiss();
            }
        }

        toasts.setAll(viewModels);
    }

    private void toastClosed(ToastViewModel toastViewModel) {
        applicationContext.toastsProperty().remove(toastViewModel.getToast());
    }

}
