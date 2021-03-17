/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.planning.widgets;

import com.google.inject.Inject;
import com.intel.missioncontrol.geometry.AreaOfInterest;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.helper.WindowHelper;
import com.intel.missioncontrol.ui.dialogs.IDialogContextProvider;
import com.intel.missioncontrol.ui.dialogs.IDialogService;
import de.saxsys.mvvmfx.Context;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectContext;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.utils.notifications.NotificationObserver;
import de.saxsys.mvvmfx.utils.notifications.WeakNotificationObserver;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

public class PowerPoleWidgetView extends GridPane implements Initializable, FxmlView<PowerPoleWidgetViewModel> {

    private final NotificationObserver getWindowObserver =
        (key, payload) -> {
            if (payload.length > 0 && payload[0] instanceof IDialogService.GetWindowRequest) {
                IDialogService.GetWindowRequest getWindowRequest = (IDialogService.GetWindowRequest)payload[0];
                getWindowRequest.setWindow(WindowHelper.getPrimaryStage());
            }
        };
    private final IDialogContextProvider dialogContextProvider;
    private final ILanguageHelper languageHelper;

    @FXML
    public Button openPowerPoleDialog;

    @FXML
    public Label nrPointsLbl;

    @InjectViewModel
    private PowerPoleWidgetViewModel viewModel;

    @InjectContext
    private Context context;

    private AreaOfInterest areaOfInterest;

    @Inject
    PowerPoleWidgetView(IDialogContextProvider dialogContextProvider, ILanguageHelper languageHelper) {
        this.dialogContextProvider = dialogContextProvider;
        this.languageHelper = languageHelper;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        openPowerPoleDialog.setOnAction((actionEvent) -> viewModel.getShowEditPowerPoleDialogCommand().execute());
        openPowerPoleDialog.setDisable(false);
        dialogContextProvider.setContext(viewModel, context);
        this.areaOfInterest = viewModel.areaOfInterest;

        nrPointsLbl
            .textProperty()
            .bind(
                Bindings.createStringBinding(
                    () -> {
                        return languageHelper.getString(
                            PowerPoleWidgetView.class,
                            "nrpoints",
                            viewModel.getNumberOfPoints(),
                            viewModel.getNumberOfActivePoints());
                    },
                    viewModel.numberOfPointsProperty(),
                    viewModel.numberOfActivePointsProperty()));
        viewModel.subscribe(IDialogService.GET_WINDOW_REQUEST, new WeakNotificationObserver(getWindowObserver));
    }
}
