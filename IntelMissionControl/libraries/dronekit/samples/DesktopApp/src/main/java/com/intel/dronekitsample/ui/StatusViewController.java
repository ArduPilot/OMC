package com.intel.dronekitsample.ui;

import com.intel.dronekitsample.model.DroneAttributes;
import com.intel.dronekitsample.model.DroneModel;
import com.intel.dronekitsample.ui.StatusView.StatusLabel;
import com.o3dr.services.android.lib.drone.property.Altitude;
import com.o3dr.services.android.lib.drone.property.Battery;
import com.o3dr.services.android.lib.drone.property.Gps;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.Node;

import java.util.HashMap;
import java.util.function.Function;

public class StatusViewController {

    private final StatusView view;
    private final DroneModel model;

    // used to enable
    private final BooleanProperty disabled = new SimpleBooleanProperty(true);

    public StatusViewController(StatusView statusView, DroneModel model) {
        this.view = statusView;
        this.model = model;
        init();
    }

    private <T> StatusLabel makeStatusItem(String label, ObservableValue<T> property, Function<T, String> stringFunction) {
        StringBinding stringBinding = Bindings.createStringBinding(() -> {
            T val = property.getValue();
            if (disabled.get() || val == null) {
                return "-";
            } else {
                return stringFunction.apply(val);
            }
        }, property);

        StatusLabel l = new StatusLabel(label);
        l.textProperty().bind(stringBinding);
        return l;
    }


    private void init() {

        DroneAttributes attrs = model.getAttributes();
        ObservableList<Node> chl = view.pane.getChildren();
        chl.removeAll();

        disabled.bind(attrs.connectedProperty().not());
        view.pane.disableProperty().bind(disabled);
        chl.add(
                makeStatusItem("alt.", attrs.altitudeProperty(),
                        (a) -> String.format("%3.1f (%3.1f)", a.getAltitude(), a.getTargetAltitude()))
        );

        chl.add(
                makeStatusItem("gps", attrs.gpsProperty(),
                        (g) -> String.format("%s (%d)", g.getFixStatus(), g.getSatellitesCount()))
        );

        chl.add(
                makeStatusItem("bat.", attrs.batteryProperty(),
                        (b) -> String.format("% 2.1fV (%2.1f)", b.getBatteryVoltage(), b.getBatteryRemain()))
        );
        chl.add(
                makeStatusItem("ground spd.", attrs.speedProperty(),
                        (s) -> String.format("% 2.1f m/s", s.getGroundSpeed()))
        );

        chl.add(
                makeStatusItem("vert. spd.", attrs.speedProperty(),
                        (s) -> String.format("% 2.1f m/s", s.getVerticalSpeed()))
        );

    }

}
