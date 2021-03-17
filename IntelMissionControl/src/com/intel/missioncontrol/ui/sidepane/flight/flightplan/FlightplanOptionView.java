/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight.flightplan;

import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.helper.ScaleHelper;
import com.intel.missioncontrol.mission.FlightPlan;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.ui.Animations;
import com.intel.missioncontrol.ui.ViewBase;
import com.intel.missioncontrol.utils.IntegerValidator;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.util.StringConverter;
import javax.inject.Inject;

public class FlightplanOptionView extends ViewBase<FlightplanOptionViewModel> {

    private static final double REFRESH_ICON_SIZE = ScaleHelper.emsToPixels(1.3);

    @InjectViewModel
    private FlightplanOptionViewModel viewModel;

    @FXML
    private Pane rootNode;

    @FXML
    private ImageView alertIcon;

    @FXML
    private ComboBox<FlightPlan> flightplanComboBox;

    @FXML
    private Button editFlightplan;

    @FXML
    private Button sendButton;

    @FXML
    private Label estimatetime;

    @FXML
    private Button refreshButton;

    @FXML
    private Spinner<Integer> waypointIndexSpinner;

    private final ILanguageHelper languageHelper;
    private final ISettingsManager settingsManager;

    private Property<Integer> waypointsProperty;
    //private Property<Integer> nextwpProperty;
    private SpinnerValueFactory<Integer> waypointValueFactory;

    private ProgressBar backProgress;
    private ProgressBar frontProgress;

    @FXML
    private StackPane progressbarPane;

    private ImageView infiniteProgressImageview = null;

    @Inject
    public FlightplanOptionView(ILanguageHelper languageHelper, ISettingsManager settingsManager) {
        this.languageHelper = languageHelper;
        this.settingsManager = settingsManager;
    }

    public FlightplanOptionViewModel getViewModel() {
        return viewModel;
    }

    @Override
    public void initializeView() {
        super.initializeView();

        flightplanComboBox.setConverter(
            new StringConverter<>() {
                @Override
                public String toString(FlightPlan object) {
                    if (object == null)
                        return languageHelper.getString(
                            "com.intel.missioncontrol.ui.sidepane.flight.flightplan.FlightplanOptionView.pleaseSelect");
                    else return object.getName();
                }

                @Override
                public FlightPlan fromString(String string) {
                    return null;
                }
            });

        Bindings.bindContent(flightplanComboBox.getItems(), viewModel.flightPlansProperty());

        alertIcon.visibleProperty().bind(viewModel.currentFlightplanSelectionProperty().isNull());
        flightplanComboBox.valueProperty().bindBidirectional(viewModel.currentFlightplanSelectionProperty());
        editFlightplan.setOnAction(event -> viewModel.getEditFlightplanCommand().execute());
        editFlightplan.disableProperty().bind(viewModel.getEditFlightplanCommand().notExecutableProperty());

        IntegerValidator wpSpinnerValueFactory = new IntegerValidator(0, 0, Integer.MAX_VALUE, 0, Integer.MAX_VALUE);

        waypointIndexSpinner.setValueFactory(wpSpinnerValueFactory.getValueFactory());
        waypointIndexSpinner.setEditable(false);
        //waypointIndexSpinner.getEditor().setTextFormatter(wpSpinnerValueFactory.getTextFormatter());

        waypointsProperty = viewModel.getCurrentWaypoint().asObject();
        //nextwpProperty = viewModel.nextWaypointProperty().asObject();
        waypointIndexSpinner.getValueFactory().valueProperty().bindBidirectional(waypointsProperty);

//        waypointIndexSpinner
//            .getValueFactory()
//            .valueProperty()
//            .addListener(
//                (observable, oldValue, newValue) -> {
//                    if (!viewModel.IsFPExecutionPausedProperty().get()) {
//                        refreshButton.setVisible(true);
//                        viewModel.setFPExecutionToPause(true);
//                    }
//                });

        sendButton.setOnAction(event -> {
            viewModel.getSendCommand().execute();
        });
        sendButton.disableProperty().bind(viewModel.sendInProgressProperty());
        sendButton.graphicProperty().bind(Bindings.createObjectBinding(() ->
                        viewModel.sendInProgressProperty().get()? getInfiniteProgressImage():null,
                viewModel.sendInProgressProperty()));

        frontProgress = new ProgressBar();
        frontProgress.getStylesheets().clear();
        frontProgress
            .getStylesheets()
            .add(
                FlightplanOptionView.class
                    .getResource("/com/intel/missioncontrol/ui/sidepane/flight/flightplan/FlightplanOptionView.css")
                    .toExternalForm());
        frontProgress.getStyleClass().add("progress_front");
        frontProgress.setProgress(0);

        backProgress = new ProgressBar();
        backProgress.getStyleClass().clear();
        backProgress
            .getStylesheets()
            .add(
                FlightplanOptionView.class
                    .getResource("/com/intel/missioncontrol/ui/sidepane/flight/flightplan/FlightplanOptionView.css")
                    .toExternalForm());
        backProgress.getStyleClass().add("progress_back");
        backProgress.setProgress(0);

        progressbarPane.getChildren().addAll(frontProgress);//backProgress, frontProgress);
        frontProgress.progressProperty().bind(viewModel.currentwpProgressProperty());
        //backProgress.progressProperty().bind(viewModel.nextwpProgressProperty());

//        viewModel
//            .IsFPExecutionPausedProperty()
//            .addListener(
//                (observable, oldValue, newValue) -> {
//                    if (newValue) {
//                        viewModel.nextWaypointProperty().set(viewModel.getCurrentWaypoint().get());
//                        waypointIndexSpinner.getValueFactory().valueProperty().unbindBidirectional(waypointsProperty);
//                        waypointIndexSpinner.getValueFactory().valueProperty().bindBidirectional(nextwpProperty);
//                    }
//                });
    }

    @Override
    public Pane getRootNode() {
        return rootNode;
    }

    public void onRefreshButtonClicked(ActionEvent actionEvent) {
        refreshButton.setVisible(false);
        //waypointIndexSpinner.getValueFactory().valueProperty().unbindBidirectional(nextwpProperty);
        waypointIndexSpinner.getValueFactory().valueProperty().bindBidirectional(waypointsProperty);
        //viewModel.setFPExecutionToPause(false);
    }

    private ImageView getInfiniteProgressImage() {
        if (infiniteProgressImageview == null) {
            infiniteProgressImageview = new ImageView(new Image("/com/intel/missioncontrol/icons/icon_progress.svg", REFRESH_ICON_SIZE, REFRESH_ICON_SIZE, true, false));
            Animations.spinForever(infiniteProgressImageview);
        }

        return infiniteProgressImageview;
    }

}
