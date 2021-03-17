/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight;

import static com.intel.missioncontrol.ui.sidepane.flight.PreflightChecks.IdlWorkflowState.EXECUTING_FLIGHT_PLAN;
import static com.intel.missioncontrol.ui.sidepane.flight.PreflightChecks.IdlWorkflowState.HOVER_ON_SPOT;

import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.helper.ScaleHelper;
import com.intel.missioncontrol.ui.common.components.TitledForm;
import com.intel.missioncontrol.ui.controls.ActivityButton;
import com.intel.missioncontrol.ui.dialogs.IDialogContextProvider;
import com.intel.missioncontrol.ui.sidepane.FancyTabView;
import com.intel.missioncontrol.ui.sidepane.flight.PreflightChecks.PreflightChecksView;
import com.intel.missioncontrol.ui.sidepane.flight.PreflightChecks.PreflightChecksViewModel;
import com.intel.missioncontrol.ui.sidepane.flight.flightplan.FlightplanOptionView;
import com.intel.missioncontrol.ui.sidepane.flight.flightplan.FlightplanOptionViewModel;
import de.saxsys.mvvmfx.Context;
import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.InjectContext;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.ViewModel;
import de.saxsys.mvvmfx.ViewTuple;
import java.util.ArrayList;
import java.util.List;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javax.inject.Inject;

public class FlightConnectedView extends FancyTabView<FlightConnectedViewModel> {

    private static final String ICON_GPS_1 = "/com/intel/missioncontrol/icons/icon_gps_1.svg";
    private static final String ICON_GPS_2 = "/com/intel/missioncontrol/icons/icon_gps_2.svg";
    private static final String ICON_GPS_3 = "/com/intel/missioncontrol/icons/icon_gps_3.svg";
    private static final String ICON_GPS_4 = "/com/intel/missioncontrol/icons/icon_gps_4.svg";
    private static final String ICON_GPS_5 = "/com/intel/missioncontrol/icons/icon_gps_5.svg";
    private static final String ICON_BATTERY_1 = "/com/intel/missioncontrol/icons/icon_battery_1-4.svg";
    private static final String ICON_BATTERY_2 = "/com/intel/missioncontrol/icons/icon_battery_2-4.svg";
    private static final String ICON_BATTERY_3 = "/com/intel/missioncontrol/icons/icon_battery_3-4.svg";
    private static final String ICON_BATTERY_4 = "/com/intel/missioncontrol/icons/icon_battery_4-4.svg";
    private static final String ICON_BATTERY_EMPTY = "/com/intel/missioncontrol/icons/icon_battery_0-4.svg";
    private static final String ICON_FP_AIRBORNE = "/com/intel/missioncontrol/icons/icon_fp_airborne.svg";
    private static final String ICON_FP_ONGROUND = "/com/intel/missioncontrol/icons/icon_fp_onground.svg";
    private static final String ICON_FP_LANDING = "/com/intel/missioncontrol/icons/icon_fp_landing.svg";
    private static final String ICON_FP_TAKEOFF = "/com/intel/missioncontrol/icons/icon_fp_takeoff.svg";
    private static final double REFRESH_ICON_SIZE = ScaleHelper.emsToPixels(1.3);

    private final List<TitledForm> forms = new ArrayList<>();

    @InjectViewModel
    FlightConnectedViewModel viewModel;

    @FXML
    private Button showOnMapButton;

    @FXML
    private Label projectNameLabel;

    @FXML
    private Label uavName;

    @FXML
    private HBox systemStatusbox;

    @FXML
    private Label systemStatus;

    @FXML
    private Button batteryButton;

    @FXML
    private Button rtkButton;

    @FXML
    private Button altitudeAGLButton;

    @FXML
    private Button onGroundButton;

    @FXML
    private Button remainingButton;

    @FXML
    private Button autoPilotButton;

    @FXML
    private VBox formsContainer;

    //    @FXML
    //    private Button takeoffButton;
    //
    //    @FXML
    //    private ProgressBar takeoffProgressbar;
    //
    //    @FXML
    //    private CheckBox runFlightplanAuto;
    //
    //    @FXML
    //    private HBox firstFooter;
    //
    //    @FXML
    //    private HBox secondFooter;
    //
    //    @FXML
    //    private Button runPlanOrLandButton;
    //
    //    @FXML
    //    private Button returnHomeButton;
    //
    //    @FXML
    //    private Button nextPlanButton;
    @FXML
    private Button flyByClickButton;

    @FXML
    private Button manualControlsButton;

    @FXML
    private Button emergencyButton;

    @FXML
    private Label panelMessage;

    @FXML
    private Pane coverbody;

    @FXML
    private Pane coverfooter;

    @InjectContext
    private Context context;

    @FXML
    private ActivityButton takeoffButton;

    @FXML
    private ActivityButton runPlanButton;

    @FXML
    private ActivityButton landButton;

    @FXML
    private ActivityButton returnHomeButton;

    @FXML
    private ActivityButton pauseButton;

    @FXML
    private ActivityButton resumeButton;

    private ILanguageHelper languageHelper;
    private IDialogContextProvider dialogContextProvider;

    private final IApplicationContext applicationContext;
    private Timeline takeoffTimeline = new Timeline();

    private Image gps1Icon;
    private Image gps2Icon;
    private Image gps3Icon;
    private Image gps4Icon;
    private Image gps5Icon;
    private Image batteryEmptyIcon;
    private Image battery1Icon;
    private Image battery2Icon;
    private Image battery3Icon;
    private Image battery4Icon;
    private Image airborneIcon;
    private Image ongroundIcon;
    private Image takeoffIcon;
    private Image landingIcon;

    @Inject
    public FlightConnectedView(ILanguageHelper languageHelper, IDialogContextProvider dialogContextProvider, IApplicationContext applicationContext) {
        this.languageHelper = languageHelper;
        this.dialogContextProvider = dialogContextProvider;
        this.applicationContext = applicationContext;
    }

    @Override
    protected ViewModel getViewModel() {
        return viewModel;
    }

    @Override
    protected void initializeView() {
        super.initializeView();

        dialogContextProvider.setContext(viewModel, context);

        showOnMapButton.setOnAction(event -> viewModel.getShowOnMapCommand().execute());
        projectNameLabel.textProperty().bind(viewModel.missionNameProperty());

        uavName.textProperty().bind(viewModel.currentUavNameProperty());
        systemStatus.textProperty().bind(viewModel.systemStatusProperty());
        batteryButton.textProperty().bind(viewModel.batteryStatusProperty());
        rtkButton.textProperty().bind(viewModel.rtkGpsStatusProperty());
        onGroundButton.textProperty().bind(viewModel.onGroundStatusProperty());
        altitudeAGLButton.textProperty().bind(viewModel.altitudeAglStatusProperty());
        remainingButton.textProperty().bind(viewModel.remainingStatusProperty());
        autoPilotButton.textProperty().bind(viewModel.autoPilotStatusProperty());

        coverbody.visibleProperty().bind(viewModel.getShouldCoverPage());
        coverfooter.visibleProperty().bind(viewModel.getShouldCoverPage());
        initFlightFlowButtons();

        viewModel
                .gpsLevelProperty()
                .addListener(
                        (observable, oldValue, newValue) -> {
                            switch (newValue.intValue() / 2) {
                                case 2:
                                    rtkButton.setGraphic(new ImageView(getGps2Icon()));
                                    break;
                                case 3:
                                    rtkButton.setGraphic(new ImageView(getGps3Icon()));
                                    break;
                                case 4:
                                    rtkButton.setGraphic(new ImageView(getGps4Icon()));
                                    break;
                                case 5:
                                    rtkButton.setGraphic(new ImageView(getGps5Icon()));
                                    break;
                                case 1:
                                default:
                                    rtkButton.setGraphic(new ImageView(getGps1Icon()));
                                    break;
                            }
                        });

        viewModel
                .alertBatteryLevelProperty()
                .addListener(
                        (observable, oldValue, newValue) -> {
                            systemStatusbox.getStyleClass().clear();
                            batteryButton.getStyleClass().clear();
                            switch (newValue) {
                                case YELLOW:
                                    systemStatusbox.getStyleClass().add("header-telemetry-status-yellow");
                                    batteryButton.getStyleClass().add("flat-telemetry-button-yellow");
                                    break;
                                case RED:
                                    systemStatusbox.getStyleClass().add("header-telemetry-status-red");
                                    batteryButton.getStyleClass().add("flat-telemetry-button-red");
                                    break;
                                default:
                                    systemStatusbox.getStyleClass().add("header-telemetry-status");
                                    batteryButton.getStyleClass().add("flat-telemetry-button");
                                    break;
                            }
                        });

        viewModel
                .batteryPercentageProperty()
                .addListener(
                        (observable, oldValue, newValue) -> {

                            if (newValue.intValue() >= 100) {
                                batteryButton.setGraphic(new ImageView(getBattery4Icon()));
                            } else if (newValue.intValue() >= 75) {
                                batteryButton.setGraphic(new ImageView(getBattery3Icon()));
                            } else if (newValue.intValue() >= 50) {
                                batteryButton.setGraphic(new ImageView(getBattery2Icon()));
                            } else if (newValue.intValue() >= 25) {
                                batteryButton.setGraphic(new ImageView(getBattery1Icon()));
                            } else {
                                batteryButton.setGraphic(new ImageView(getBatteryEmptyIcon()));
                            }
                        });

        viewModel
                .flightPhaseProperty()
                .addListener(
                        (observable, oldValue, newValue) -> {
                            switch (newValue) {
                                case ground:
                                    onGroundButton.setGraphic(new ImageView(getOngroundIcon()));
                                    break;
                                case takeoff:
                                    onGroundButton.setGraphic(new ImageView(getTakeoffIcon()));
                                    break;
                                case airborne:
                                    onGroundButton.setGraphic(new ImageView(getAirborneIcon()));
                                    break;
                                case landing:
                                    onGroundButton.setGraphic(new ImageView(getLandingIcon()));
                                    break;
                                case returnhome: // TODO: update reuturnhome icon
                                default:
                                    onGroundButton.setGraphic(new ImageView(getOngroundIcon()));
                                    break;
                            }
                        });

        resetforms();
        loadSections();

        //
        // runFlightplanAuto.selectedProperty().bindBidirectional(viewModel.isRunFlightplanAutomaticallyProperty());
        // takeoff
        // initTakeOffButtonUI();
        //        runPlanOrLandButton.textProperty().bind(
        //                Bindings.createStringBinding(
        //                        () -> {
        //                            switch(viewModel.currentIdlWorkflowStateProperty().get()) {
        //                                case FLIGHT_PLAN_COMPLETED:
        //                                    return
        // languageHelper.getString("com.intel.missioncontrol.ui.sidepane.flight.preflightChecks.land");
        //                                case LANDING:
        //                                    return
        // languageHelper.getString("com.intel.missioncontrol.ui.sidepane.flight.preflightChecks.abortLanding");
        //                                case EXECUTING_FLIGHT_PLAN:
        //                                    return
        // languageHelper.getString("com.intel.missioncontrol.ui.sidepane.flight.preflightChecks.pause");
        //                                case EXECUTING_FLIGHT_PLAN_PAUSED:
        //                                    return
        // languageHelper.getString("com.intel.missioncontrol.ui.sidepane.flight.preflightChecks.resume");
        //                                case HOVER_ON_SPOT:
        //                                {
        ////                                    if (viewModel.isFlightPlanExecutionStartedProperty().get()) {
        ////                                        return
        // languageHelper.getString("com.intel.missioncontrol.ui.sidepane.flight.preflightChecks.resume");
        ////                                    }
        ////                                    else {
        ////                                        return
        // languageHelper.getString("com.intel.missioncontrol.ui.sidepane.flight.preflightChecks.runPlan");
        ////                                    }
        //                                    return
        // languageHelper.getString("com.intel.missioncontrol.ui.sidepane.flight.preflightChecks.land");
        //                                }
        //                                default:
        //                                    return
        // languageHelper.getString("com.intel.missioncontrol.ui.sidepane.flight.preflightChecks.runPlan");
        //                            }
        //
        //                        },
        //                        viewModel.currentIdlWorkflowStateProperty()));
        //
        //        runPlanOrLandButton.setOnAction(event -> {
        //            String currentButtonText = runPlanOrLandButton.getText();
        //            if
        // (currentButtonText.equals(languageHelper.getString("com.intel.missioncontrol.ui.sidepane.flight.preflightChecks.land"))) {
        //                viewModel.getLandCommand().execute();
        //            } else if
        // (currentButtonText.equals(languageHelper.getString("com.intel.missioncontrol.ui.sidepane.flight.preflightChecks.abortLanding"))) {
        //                viewModel.getAbortLandingCommand().execute();
        //            } else if
        // (currentButtonText.equals(languageHelper.getString("com.intel.missioncontrol.ui.sidepane.flight.preflightChecks.pause"))) {
        //                viewModel.getPauseFlightCommand().execute();
        //            } else if
        // (currentButtonText.equals(languageHelper.getString("com.intel.missioncontrol.ui.sidepane.flight.preflightChecks.resume"))) {
        //                viewModel.getResumeFlightCommand().execute();
        //            } else if
        // (currentButtonText.equals(languageHelper.getString("com.intel.missioncontrol.ui.sidepane.flight.preflightChecks.runPlan"))) {
        //                viewModel.getRunFlightplanCommand().execute();
        //            }
        //        });
        //
        //
        //        nextPlanButton.visibleProperty().bind(viewModel.isFlightPlanExecutionCompletedProperty());
        //        nextPlanButton.setOnAction(event -> {
        //            viewModel.getRunNextFlightPlanCommand().execute();
        //        });
        //
        //
        //        returnHomeButton.setOnAction(
        //            event -> {
        //                viewModel.isTakeoffProperty().setValue(false);
        //                viewModel.getReturnToHomeCommand().execute();
        //            });

        flyByClickButton.setOnAction(
                event -> {
                    viewModel.getFlyByClickCommand().execute();
                });

        manualControlsButton.setOnAction(
                event -> {
                    viewModel.getManualControlsCommand().execute();
                });

        emergencyButton.setOnAction(
                event -> {
                    viewModel.getEmergencyCommand().execute();

                    // TODO: move this to viewModel: after emergencyCommand call back from backend, set workflow to hover on
                    // spot
                    viewModel.currentIdlWorkflowStateProperty().set(HOVER_ON_SPOT);

                    viewModel.getEmergencyDialog().execute();
                });

        panelMessage.textProperty().bind(viewModel.uavIPandPortProperty());
    }

    public void setBackToDiscoveryButton() {
        viewModel.getDisconnectCommand().execute();
        viewModel.getBackToDiscoveryCopterCommand().execute();
    }

    private void resetforms() {
        formsContainer.getChildren().clear();
        forms.clear();
    }

    private void loadSections() {
        formsContainer.getChildren().add(loadFlightplanOptionView());
        formsContainer.getChildren().add(loadPreflightChecksView());
    }

    private TitledPane loadFlightplanOptionView() {
        ViewTuple<FlightplanOptionView, FlightplanOptionViewModel> viewTuple =
                FluentViewLoader.fxmlView(FlightplanOptionView.class).context(context).load();
        return collapsed(
                new TitledPane(
                        languageHelper.getString("com.intel.missioncontrol.ui.sidepane.flight.FlightConnectedView.flightplan"),
                        viewTuple.getView()));
    }

    private TitledPane loadPreflightChecksView() {
        // old one: ViewTuple<PreFlightChecksView, PreFlightChecksViewModel> viewTuple =
        // FluentViewLoader.fxmlView(PreFlightChecksView.class).context(context).load();
        ViewTuple<PreflightChecksView, PreflightChecksViewModel> viewTuple =
                FluentViewLoader.fxmlView(PreflightChecksView.class).context(context).load();
        return collapsed(
                new TitledPane(
                        languageHelper.getString(
                                "com.intel.missioncontrol.ui.sidepane.flight.FlightConnectedView.preflightChecks"),
                        viewTuple.getView()));
    }

    private TitledPane collapsed(TitledPane titledPane) {
        titledPane.expandedProperty().set(true);
        return titledPane;
    }

    private Image getGps1Icon() {
        if (gps1Icon == null) {
            gps1Icon = new Image(ICON_GPS_1, REFRESH_ICON_SIZE, REFRESH_ICON_SIZE, true, false);
        }

        return gps1Icon;
    }

    private Image getGps2Icon() {
        if (gps2Icon == null) {
            gps2Icon = new Image(ICON_GPS_2, REFRESH_ICON_SIZE, REFRESH_ICON_SIZE, true, false);
        }

        return gps2Icon;
    }

    private Image getGps3Icon() {
        if (gps3Icon == null) {
            gps3Icon = new Image(ICON_GPS_3, REFRESH_ICON_SIZE, REFRESH_ICON_SIZE, true, false);
        }

        return gps3Icon;
    }

    private Image getGps4Icon() {
        if (gps4Icon == null) {
            gps4Icon = new Image(ICON_GPS_4, REFRESH_ICON_SIZE, REFRESH_ICON_SIZE, true, false);
        }

        return gps4Icon;
    }

    private Image getGps5Icon() {
        if (gps5Icon == null) {
            gps5Icon = new Image(ICON_GPS_5, REFRESH_ICON_SIZE, REFRESH_ICON_SIZE, true, false);
        }

        return gps5Icon;
    }

    private Image getBatteryEmptyIcon() {
        if (batteryEmptyIcon == null) {
            batteryEmptyIcon = new Image(ICON_BATTERY_EMPTY, REFRESH_ICON_SIZE, REFRESH_ICON_SIZE, true, false);
        }

        return batteryEmptyIcon;
    }

    private Image getBattery1Icon() {
        if (battery1Icon == null) {
            battery1Icon = new Image(ICON_BATTERY_1, REFRESH_ICON_SIZE, REFRESH_ICON_SIZE, true, false);
        }

        return battery1Icon;
    }

    private Image getBattery2Icon() {
        if (battery2Icon == null) {
            battery2Icon = new Image(ICON_BATTERY_2, REFRESH_ICON_SIZE, REFRESH_ICON_SIZE, true, false);
        }

        return battery2Icon;
    }

    private Image getBattery3Icon() {
        if (battery3Icon == null) {
            battery3Icon = new Image(ICON_BATTERY_3, REFRESH_ICON_SIZE, REFRESH_ICON_SIZE, true, false);
        }

        return battery3Icon;
    }

    private Image getBattery4Icon() {
        if (battery4Icon == null) {
            battery4Icon = new Image(ICON_BATTERY_4, REFRESH_ICON_SIZE, REFRESH_ICON_SIZE, true, false);
        }

        return battery4Icon;
    }

    private Image getAirborneIcon() {
        if (airborneIcon == null) {
            airborneIcon = new Image(ICON_FP_AIRBORNE, REFRESH_ICON_SIZE, REFRESH_ICON_SIZE, true, false);
        }

        return airborneIcon;
    }

    private Image getOngroundIcon() {
        if (ongroundIcon == null) {
            ongroundIcon = new Image(ICON_FP_ONGROUND, REFRESH_ICON_SIZE, REFRESH_ICON_SIZE, true, false);
        }

        return ongroundIcon;
    }

    private Image getTakeoffIcon() {
        if (takeoffIcon == null) {
            takeoffIcon = new Image(ICON_FP_TAKEOFF, REFRESH_ICON_SIZE, REFRESH_ICON_SIZE, true, false);
        }

        return takeoffIcon;
    }

    private Image getLandingIcon() {
        if (landingIcon == null) {
            landingIcon = new Image(ICON_FP_LANDING, REFRESH_ICON_SIZE, REFRESH_ICON_SIZE, true, false);
        }

        return landingIcon;
    }

    private void initTakeOffButtonUI() {
        // takeoffButton.disableProperty().bind(viewModel.areMotorsOnProperty().not());
        //        takeoffProgressbar.getStyleClass().clear();
        //        takeoffProgressbar.getStyleClass().setAll("holding-progress");
        //        takeoffProgressbar.setProgress(0);
        //        takeoffButton.setText(
        //            languageHelper.getString("com.intel.missioncontrol.ui.sidepane.flight.preflightChecks.takeoff"));
        //
        //        takeoffButton.addEventHandler(
        //            MouseEvent.ANY,
        //            new EventHandler<>() {
        //                long starttime = 0;
        //
        //                @Override
        //                public void handle(MouseEvent event) {
        //                    if (event.getEventType().equals(MouseEvent.MOUSE_PRESSED)) {
        //                        starttime = System.currentTimeMillis();
        //                        onTakeOffButtonPressed();
        //                        //the rotation should be stopped when backend signals back with motors are on,
        //                        //for temporarily, just stop it when user start take off
        //                        rotation.stop();
        //                    } else if (event.getEventType().equals(MouseEvent.MOUSE_RELEASED)) {
        //                        if (System.currentTimeMillis() - starttime > LONG_PRESSED_TIMER) {
        //                            takeoffTimeline.stop();
        //                            takeoffTimeline.getKeyFrames().clear();
        //                            takeoffButton.setText(
        //                                languageHelper.getString(
        //
        // "com.intel.missioncontrol.ui.sidepane.flight.preflightChecks.abortTakeoff"));
        //                            takeoffButton.getStyleClass().clear();
        //                            takeoffButton.getStyleClass().addAll("preflight-button-abort");
        //
        //                            // switch UI and execute takeoff
        //                            viewModel.isTakeoffProperty().setValue(true);
        //                            viewModel.getTakeoffCommand().execute();
        ////                            viewModel.currentIdlWorkflowStateProperty().set(
        ////                                    runFlightplanAuto.isSelected()?
        ////
        // IdlWorkflowState.TAKE_OFF_WITH_RUN_FP_AUTOMATICALLY:IdlWorkflowState.TAKE_OFF_WITHOUT_RUN_FP_AUTOMATICALLY);
        //                        } else {
        //                            double currenttime = takeoffTimeline.getCurrentTime().toMillis();
        //                            takeoffTimeline.stop();
        //                            takeoffTimeline.getKeyFrames().clear();
        //                            animateTakeoffProgressBack(currenttime, progressbar.getProgress());
        //                        }
        //                    }
        //                }
        //            });
    }

    private void onTakeOffButtonPressed() {
        //        double progress = takeoffProgressbar.getProgress();
        //        System.out.println("onTakeOffButtonPressed progress: " + progress);
        //        if (takeoffButton
        //                .getText()
        //                .equals(
        //                    languageHelper.getString(
        //                        "com.intel.missioncontrol.ui.sidepane.flight.preflightChecks.abortTakeoff"))) {
        //            takeoffProgressbar.setProgress(0);
        //            takeoffTimeline.getKeyFrames().clear();
        //            takeoffButton.setText(
        //
        // languageHelper.getString("com.intel.missioncontrol.ui.sidepane.flight.preflightChecks.takeoff"));
        //            takeoffButton.getStyleClass().clear();
        //            takeoffButton.getStyleClass().add("longpress-button");
        //            viewModel.getAbortTakeoffCommand();
        //        } else if (progress == 0.0) {
        //            animateTakeoffProgress();
        //        }
    }

    private void animateTakeoffProgress() {
        //        KeyValue keyValue;
        //        KeyFrame keyFrame;
        //
        //        takeoffTimeline.getKeyFrames().clear();
        //        keyValue = new KeyValue(takeoffProgressbar.progressProperty(), 1);
        //        keyFrame = new KeyFrame(new Duration(LONG_PRESSED_TIMER), keyValue);
        //
        //        takeoffTimeline.getKeyFrames().add(keyFrame);
        //        takeoffTimeline.play();
    }

    private void animateTakeoffProgressBack(double currenttime, double progress) {
        //        KeyValue keyValue;
        //        KeyFrame keyFrame;
        //
        //        if (progress > 0 && progress <= 1) {
        //            keyValue = new KeyValue(takeoffProgressbar.progressProperty(), 0);
        //
        //            if (currenttime > 0) {
        //                keyFrame = new KeyFrame(new Duration(currenttime), keyValue);
        //
        //                takeoffTimeline.getKeyFrames().add(keyFrame);
        //                takeoffTimeline.play();
        //            }
        //        }
    }

    public void onTakeoffClicked(ActionEvent actionEvent) {
        viewModel.getTakeoffCommand().execute();
    }

    public void onRunFlightplanClicked(ActionEvent actionEvent) {
        viewModel.getRunFlightplanCommand().execute();
    }

    public void onReturnHomeClicked(ActionEvent actionEvent) {
        viewModel.getReturnToHomeCommand().execute();
    }

    public void onLandClicked(ActionEvent actionEvent) {
        viewModel.getLandCommand().execute();
    }

    public void onPauseClicked(ActionEvent actionEvent) {
        viewModel.getPauseFlightCommand().execute();
    }

    public void onResumeClicked(ActionEvent actionEvent) {
        viewModel.getResumeFlightCommand().execute();
    }

    private void initFlightFlowButtons() {
        takeoffButton.disableProperty().bind(viewModel.disableFlightFlowActions());

        runPlanButton.disableProperty().bind(viewModel.disableFlightFlowActions().or(viewModel.currentIdlWorkflowStateProperty().isEqualTo(EXECUTING_FLIGHT_PLAN)));

        landButton.disableProperty().bind(viewModel.disableFlightFlowActions());
        returnHomeButton.disableProperty().bind(viewModel.disableFlightFlowActions());
        pauseButton.disableProperty().bind(viewModel.disableFlightFlowActions());
        resumeButton.disableProperty().bind(viewModel.disableFlightFlowActions());
    }

    @FXML
    public void renameClicked() {
        viewModel.getRenameMissionCommand().execute();
    }
}
