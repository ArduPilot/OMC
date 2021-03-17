/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight.fly.telemetry;

import com.intel.missioncontrol.drone.FlightSegment;
import com.intel.missioncontrol.drone.GnssState;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.ui.EnumConverter;
import com.intel.missioncontrol.ui.ViewBase;
import com.intel.missioncontrol.ui.dialogs.IDialogContextProvider;
import de.saxsys.mvvmfx.Context;
import de.saxsys.mvvmfx.InjectContext;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javax.inject.Inject;

public class TelemetryListView extends ViewBase<TelemetryListViewModel> {

    @FXML
    public VBox gnssVBox;

    @FXML
    public Label latitude;

    @FXML
    public Label longitude;

    @FXML
    public VBox gnss2VBox;

    private IDialogContextProvider dialogContextProvider;
    private final EnumConverter<GnssState> gnssStateStringConverter;

    @InjectViewModel
    private TelemetryListViewModel viewModel;

    @InjectContext
    private Context context;

    @FXML
    public Label gnssQuality;

    @FXML
    private Label gnssStatus;

    @FXML
    public Label gnssSatellites;

    @FXML
    private GridPane rootNode;


    @Inject
    public TelemetryListView(ILanguageHelper languageHelper, IDialogContextProvider dialogContextProvider) {
        this.dialogContextProvider = dialogContextProvider;
        this.gnssStateStringConverter = new EnumConverter<>(languageHelper, GnssState.class);
    }

    @Override
    protected Parent getRootNode() {
        return rootNode;
    }

    @Override
    protected ViewModel getViewModel() {
        return viewModel;
    }

    @Override
    protected void initializeView() {
        super.initializeView();
        dialogContextProvider.setContext(viewModel, context);


        // gnss
        gnssQuality.textProperty().bind(viewModel.gnssQualityTextProperty());
        gnssStatus
                .textProperty()
                .bind(
                        Bindings.createStringBinding(
                                () -> {
                                    var gnssState = viewModel.gnssStateProperty().getValue();
                                    if (gnssState == null) {
                                        gnssState = GnssState.UNKNOWN;
                                    }

                                    return gnssStateStringConverter.toString(gnssState);
                                },
                                viewModel.gnssStateProperty()));

        gnssSatellites
                .textProperty()
                .bind(
                        Bindings.createStringBinding(
                                () -> {
                                    int gnssNumberOfSatellites = viewModel.gnssNumberOfSatellitesProperty().getValue().intValue();
                                    if (gnssNumberOfSatellites < 0) {
                                        return "--";
                                    }

                                    return String.valueOf(gnssNumberOfSatellites);
                                },
                                viewModel.gnssNumberOfSatellitesProperty()));


        latitude.textProperty().bind(viewModel.latitudeTextProperty());
        longitude.textProperty().bind(viewModel.longitudeTextProperty());

    }


}
