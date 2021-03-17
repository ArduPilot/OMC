/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight.fly.flightplan;

import com.intel.missioncontrol.measure.QuantityFormat;
import com.intel.missioncontrol.mission.FlightPlan;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.ui.ViewBase;
import com.intel.missioncontrol.ui.controls.AdaptiveQuantityFormat;
import com.intel.missioncontrol.ui.controls.ItemsView;
import com.intel.missioncontrol.ui.navigation.INavigationService;
import com.intel.missioncontrol.ui.navigation.SidePanePage;
import de.saxsys.mvvmfx.Context;
import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.InjectContext;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.ViewModel;
import de.saxsys.mvvmfx.ViewTuple;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javax.inject.Inject;

public class FlightplanOptionView extends ViewBase<FlightplanOptionViewModel> {

    @FXML
    VBox selectedFlightPlan;

    @InjectContext
    private Context context;

    @InjectViewModel
    private FlightplanOptionViewModel viewModel;

    @FXML
    private Pane rootNode;

    @FXML
    private ItemsView<ViewModel> itemsView;

    @FXML
    private ScrollPane scrollPane;

    private final QuantityFormat quantityFormat;
    private INavigationService navigationService;

    @Inject
    public FlightplanOptionView(ISettingsManager settingsManager, INavigationService navigationService) {
        this.navigationService = navigationService;
        quantityFormat = new AdaptiveQuantityFormat(settingsManager.getSection(GeneralSettings.class));
        quantityFormat.setSignificantDigits(2);
    }

    @Override
    protected Parent getRootNode() {
        return rootNode;
    }

    @Override
    public FlightplanOptionViewModel getViewModel() {
        return viewModel;
    }

    @Override
    public void initializeView() {
        super.initializeView();

        ViewTuple<SelectedFlightplanItemView, SelectedFlightplanItemViewModel> viewTuple =
            FluentViewLoader.fxmlView(SelectedFlightplanItemView.class).context(context).load();

        selectedFlightPlan.getChildren().add(viewTuple.getView());

        itemsView.addViewFactory(
            FlightplanItemViewModel.class,
            vm -> FluentViewLoader.fxmlView(FlightplanItemView.class).viewModel(vm).load().getView());

        itemsView.setItems(viewModel.itemsProperty().getValue());

        viewModel
            .selectedFlightplanProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    double i = activeFlightplanPosition(newValue);
                    if (i != -1) scrollPane.setVvalue(i);
                });
    }

    public double activeFlightplanPosition(FlightPlan newValue) {
        int size = viewModel.itemsProperty().getValue().size();
        if (!(navigationService.getSidePanePage().equals(SidePanePage.EDIT_FLIGHTPLAN)
                    || navigationService.getSidePanePage().equals(SidePanePage.START_PLANNING))
                || size == 0) {
            return -1;
        }

        for (int i = 0; i < size; i++) {
            FlightplanItemViewModel a = (FlightplanItemViewModel)viewModel.itemsProperty().getValue().get(i);
            if (a.getFlightPlan().equals(newValue)) {
                return (i + .5) / size;
            }
        }

        return -1;
    }
}
