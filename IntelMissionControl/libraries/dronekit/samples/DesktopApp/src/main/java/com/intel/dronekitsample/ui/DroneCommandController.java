package com.intel.dronekitsample.ui;

import android.os.Bundle;
import com.intel.dronekitsample.model.DroneAttributes;
import com.intel.dronekitsample.model.DroneModel;
import com.o3dr.android.client.interfaces.DroneListener;
import com.o3dr.services.android.lib.drone.property.State;
import com.o3dr.services.android.lib.drone.property.Type;
import com.o3dr.services.android.lib.drone.property.VehicleMode;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.StringBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.TextInputDialog;

import java.util.List;

public class DroneCommandController {
    private final DroneModel model;
    private MissionView missionView;
    private ControlView controlView;

    public ObservableList<VehicleMode> vehicleModes = FXCollections.observableArrayList();

    public DroneCommandController(DroneModel model) {
        this.model = model;
    }

    public void setViews(ControlView controlView, MissionView missionView) {
        this.controlView = controlView;
        this.missionView = missionView;
        setup();
    }

    private void setup() {
        model.drone.registerDroneListener(listener);

        final DroneAttributes attr = model.getAttributes();
        controlView.enabled.bind(attr.connectedProperty());

        // --- Mode selector
        controlView.modeChoiceBox.setItems(vehicleModes);
        attr.typeProperty().addListener((o, oldValue, newValue) -> {
            updateVehicleModesForType(newValue);
        });
        ObjectBinding<VehicleMode> vehicleModeBinding = Bindings.createObjectBinding(() -> attr.getState().getVehicleMode(), attr.stateProperty());
        controlView.modeChoiceBox.valueProperty().bind(vehicleModeBinding);
        controlView.modeChoiceBox.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Object value = controlView.modeChoiceBox.getValue();
                controlView.modeChoiceBox.setDisable(true);
                System.out.println("choice choosed: "+value +"event"+ event);
            }
        });

        StringBinding status = Bindings.createStringBinding(() -> {
            State state = attr.stateProperty().get();

            if (state == null || attr.connectedProperty().not().get()) {
                return "";
            } else if (state.isFlying()) {
                double a = attr.getAltitude().getAltitude();
                String s = Math.round(a) < 10.0 ? " < 10 m" : String.format("%3.0fm", a);
                return String.format("flying " + s);
            } else if (state.isArmed()) {
                return "armed";
            } else if (state.isConnected()) {
                return "connected";
            } else {
                return "";
            }
        }, attr.stateProperty(), attr.altitudeProperty());

        StringBinding type = Bindings.createStringBinding(() -> {
            Type type1 = attr.typeProperty().get();

            return  (type1 == null || type1.getFirmware() == null) ? "" : type1.getFirmware().getLabel() + " ";
        }, attr.typeProperty());

        controlView.stateLabel.textProperty().bind(Bindings.concat(type, status));
        controlView.stateLabel.disableProperty().bind(attr.connectedProperty().not());
        // --- arm/launch
        controlView.armButton.setOnAction((actionEvent) -> {
            State state = attr.getState();
            model.setArm(!state.isArmed());
        });

        controlView.launchButton.setOnAction((actionEvent) -> {
            State state = attr.getState();
            if (state.isArmed()) model.takeOff();
        });

        controlView.altButton.setOnAction((actionEvent) -> {
            TextInputDialog textInputDialog = controlView.createTextInputDialog();
            textInputDialog.setOnCloseRequest(event -> {
                String text = textInputDialog.getEditor().getText();
                if (text != null) {
                    double height = Double.parseDouble(text);
                    model.climbTo(height);
                }
            });
        });

        controlView.armed.bind(Bindings.createBooleanBinding(() -> attr.getState().isArmed(), attr.stateProperty()));
    }

    private void updateVehicleModesForType(Type type) {
        List<VehicleMode> modes = VehicleMode.getVehicleModePerDroneType(type.getDroneType());
        vehicleModes.setAll(modes);
    }

    private final DroneListener listener = new DroneListener() {
        @Override
        public void onDroneEvent(String event, Bundle extras) {

        }

        @Override
        public void onDroneServiceInterrupted(String errorMsg) {

        }
    };
}
