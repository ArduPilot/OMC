package com.intel.dronekitsample.ui;

import android.os.Bundle;
import com.intel.dronekitsample.Utils;
import com.intel.dronekitsample.model.DroneAttributes;
import com.intel.dronekitsample.model.DroneModel;
import com.o3dr.android.client.Drone;
import com.o3dr.android.client.apis.ControlApi;
import com.o3dr.android.client.apis.VehicleApi;
import com.o3dr.android.client.interfaces.DroneListener;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeEventExtra;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.property.*;
import com.o3dr.services.android.lib.model.AbstractCommandListener;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.File;
import java.util.List;
import java.util.function.Function;

public class DroneStateViewController extends ViewController {
    @FXML
    Label statusGps;

    @FXML
    Label statusAlt;

    @FXML
    Label statusBat;

    @FXML
    Label paramLabel;

    @FXML
    ProgressIndicator paramLoadingIndicator;

    @FXML
    ChoiceBox<VehicleMode> modeChoiceBox;

    @FXML
    ToggleButton armButton;

    @FXML
    Button launchButton;

    @FXML
    Hyperlink missionFile;

    public ObservableList<VehicleMode> flightModes = FXCollections.observableArrayList();

    public SimpleBooleanProperty enabled = new SimpleBooleanProperty(false);

    //final ObservableObjectValue<File> missionFile = new SimpleObjectProperty<File>();

    final DroneStateModel model = new DroneStateModel();

    final SimpleBooleanProperty loadingParameters = new SimpleBooleanProperty(false);
    final SimpleDoubleProperty loadingPercent = new SimpleDoubleProperty();

    //final SimpleDoubleProperty gpsAltitude = new SimpleDoubleProperty(Double.NaN);

    DroneAttributes droneAttrs;

    @Override
    public void doInitialize() {
        modeChoiceBox.setItems(flightModes);
        ObjectProperty<VehicleMode> str = modeChoiceBox.valueProperty();

        enabled.addListener((observable, oldValue, newValue) -> {
            getRoot().setDisable(!newValue);

        });

        paramLabel.visibleProperty().bind(loadingParameters.not());
        paramLoadingIndicator.visibleProperty().bind(loadingParameters);
        paramLoadingIndicator.progressProperty().bind(loadingPercent);

        droneAttrs = new DroneAttributes();

        setupAttributesBinding();
        Platform.runLater(() -> getRoot().setDisable(true));
        System.out.println("dronestate view intialized");
    }

    <T> StringBinding createStringBindings(ReadOnlyObjectProperty<T> property, Function<T, String> formatFun) {
        return Bindings.when(droneAttrs.enabledProperty().and(property.isNotNull()))
                .then(Bindings.createStringBinding(
                        () -> formatFun.apply(property.get()),
                        property))
                .otherwise("-");
    }

    private void setupAttributesBinding() {

//        StringBinding enabled = Bindings.when(droneAttrs.getEnabledProperty().and(droneAttrs.getBatteryProperty().isNotNull()))
//                .then(Bindings.createStringBinding(
//                        () -> String.format("%3.2f v", droneAttrs.getBattery().getBatteryVoltage()),
//                        droneAttrs.getBatteryProperty()))
//                .otherwise("-");
//
//        statusBat.textProperty().bind(enabled);
//
//
//        statusGps.textProperty().bind(createStringBindings(
//                droneAttrs.getGpsProperty(),
//                (gps) -> gps != null ? gps.getFixStatus() +" / " + gps.getSatellitesCount() : "n/a"
//        ));
//
////        statusGps.textProperty().bind(Bindings.createStringBinding(
////                () -> droneAttrs.getGps().getFixStatus(), droneAttrs.getGpsProperty()
////        ));
//
//        statusAlt.textProperty().bind(createStringBindings(
//                droneAttrs.getAltitudeProperty(),
//                altitude -> String.format("%3.1f m", altitude altitude.getAltitude())
//        ));

    }

    public void armedToggled(ActionEvent actionEvent) {
        model.toggleArm();
    }

    public void launchClicked(ActionEvent actionEvent) {
        model.takeOffDrone();
    }

    public void doUpdateState(State state) {
        VehicleMode vehicleMode = state.getVehicleMode();
        modeChoiceBox.setValue(vehicleMode);

        armButton.setSelected(state.isArmed());
        armButton.textProperty().set(state.isArmed() ? "disarm" : "arm");

        launchButton.setDisable(state.isArmed());
        launchButton.setText(state.isFlying() ? "land" : "takeoff");
    }

    public void setModel(DroneModel drone) {
        this.model.init(drone);
        this.droneAttrs.init(drone.drone);
    }

    public void startMission(ActionEvent actionEvent) {
    }

    final class DroneStateModel implements DroneListener {
        private int droneType = Type.TYPE_UNKNOWN;

        public DroneModel droneModel;
        public Drone drone;

        public void init(DroneModel model) {
            this.droneModel = model;
            this.drone = model.drone;

            drone.registerDroneListener(this);
            enabled.set(drone.isConnected());
        }

        @Override
        public void onDroneEvent(String event, Bundle extras) {
            System.out.println("--- DroneStateModel event: " + event + " extras: " + extras);

            switch (event) {
                case AttributeEvent.STATE_CONNECTED:
                    enabled.set(true);
                    updateState();
                    break;

                case AttributeEvent.STATE_DISCONNECTED:
                    enabled.set(false);
                    updateState();
                    break;

                case AttributeEvent.STATE_UPDATED:
                    updateState();
                    break;

                case AttributeEvent.TYPE_UPDATED:
                    Type newDroneType = drone.getAttribute(AttributeType.TYPE);
                    if (newDroneType.getDroneType() != this.droneType) {
                        this.droneType = newDroneType.getDroneType();
                        updateVehicleModesForType(this.droneType);
                    }
                    updateState();
                    break;

                case AttributeEvent.ATTITUDE_UPDATED:
                    updateTelemetry();
                    break;

                case AttributeEvent.BATTERY_UPDATED:
                    updateTelemetry();
                    break;

                case AttributeEvent.GPS_COUNT:
                case AttributeEvent.GPS_FIX:
                    updateTelemetry();
                    break;

                case AttributeEvent.PARAMETERS_REFRESH_STARTED:
                    loadingParameters.set(true);
                    loadingPercent.set(0.0);
                    break;
                case AttributeEvent.PARAMETER_RECEIVED:
                    double index = 0;
                    double count = 1;
                    if (extras != null) {
                        index = extras.getInt(AttributeEventExtra.EXTRA_PARAMETER_INDEX);
                        count = extras.getInt(AttributeEventExtra.EXTRA_PARAMETERS_COUNT);
                    }
                    loadingPercent.set(index/count);
                    break;
                case AttributeEvent.PARAMETERS_REFRESH_COMPLETED:
                    loadingParameters.set(false);
                    System.out.println("++++++ Got all parameters");

                    Parameters params = drone.getAttribute(AttributeType.PARAMETERS);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            File file = new File(System.getProperty("java.io.tmpdir"), "mavlink-params.json");
                            System.err.println(">>>>>\n>>>> dumping params to: "+file);
                            Utils.dumpParametersToJson(file, params);
                        }
                    }).start();
                    break;
            }
        }

        private void updateTelemetry() {
            final State state = drone.getAttribute(AttributeType.STATE);

            drone.getAttribute(AttributeType.ALTITUDE);
        }

        private void updateState() {
            final State state = drone.getAttribute(AttributeType.STATE);
            Platform.runLater(() -> doUpdateState(state));
        }

        protected void updateVehicleModesForType(int droneType) {
            List<VehicleMode> vehicleModes = VehicleMode.getVehicleModePerDroneType(droneType);
            Platform.runLater(() -> {
                flightModes.setAll(vehicleModes);
            });
        }

        @Override
        public void onDroneServiceInterrupted(String errorMsg) {

        }

        public void toggleArm() {
            final Drone drone = this.droneModel.drone;
            State vehicleState = drone.getAttribute(AttributeType.STATE);
            boolean arm = !(vehicleState.isArmed());
            VehicleApi.getApi(drone).arm(arm);
        }

        public void takeOffDrone() {
            ControlApi.getApi(this.drone).takeoff(20.0, new AbstractCommandListener() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError(int executionError) {

                }

                @Override
                public void onTimeout() {

                }
            });
        }

    }

}
