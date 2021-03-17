package com.intel.dronekitsample.model;

import android.os.Bundle;
import android.os.Parcelable;
import com.o3dr.android.client.Drone;
import com.o3dr.android.client.interfaces.DroneListener;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.mission.Mission;
import com.o3dr.services.android.lib.drone.property.*;
import com.sun.deploy.security.ValidationState;
import javafx.application.Platform;
import javafx.beans.property.*;

import java.util.List;

public class DroneAttributes {
    private Drone drone;

    private final BooleanProperty enabled = new SimpleBooleanProperty(false);

    private final Prop<Altitude> altitudeProperty = new Prop<>();
    private final Prop<Attitude> attitudeProperty = new Prop<>();
    private final Prop<Battery> batteryProperty = new Prop<>();
    private final Prop<Gps> gpsProperty = new Prop<>();
    private final Prop<State> stateProperty = new Prop<>();
    private final Prop<Parameters> parametersProperty = new Prop<>();
    private final Prop<Speed> speedProperty = new Prop<>();
    private final Prop<Mission> missionProperty = new Prop<>();
    private final Prop<GuidedState> guidedStateProperty = new Prop<>();

    private final SimpleBooleanProperty connectedProperty = new SimpleBooleanProperty(false);

    final Type UNKNOWN = new Type();

    private final SimpleObjectProperty<Type> typeProperty = new SimpleObjectProperty<Type>(UNKNOWN);

    final static class Prop<T> extends ReadOnlyObjectWrapper<T> {
        @Override
        public void set(T newValue) {
            T value = get();
            // fire change even though same underlying object, will allow
            // computed properties to update
            if (value == newValue) {
                fireValueChangedEvent();
            } else {
                super.set(newValue);
            }
        }
    }

    public DroneAttributes() {
    }

    public void init(Drone drone) {
        this.drone = drone;

        post(() -> {
            altitudeProperty.set(getAltitude());
            attitudeProperty.set(getAttitude());
            stateProperty.set(getState());
            batteryProperty.set(getBattery());
            gpsProperty.set(getGps());
            parametersProperty.set(getParameters());
            speedProperty.set(getSpeed());

            missionProperty.set(getMission());
            guidedStateProperty.set(getGuidedState());

            enabled.set(true);
            connectedProperty.set(drone.isConnected());
        });

        drone.registerDroneListener(listener);
    }


    private void checkConnect() {
        post(() -> connectedProperty.set(drone.isConnected()));
    }


    protected void post(Runnable run) {
        Platform.runLater(run);
    }

    private final DroneListener listener = new DroneListener() {
        @Override
        public void onDroneEvent(String event, Bundle extras) {
            switch (event) {
                case AttributeEvent.MISSION_RECEIVED:
                case AttributeEvent.MISSION_SENT:
                case AttributeEvent.MISSION_UPDATED:
                case AttributeEvent.MISSION_ITEM_REACHED:
                case AttributeEvent.MISSION_ITEM_UPDATED:
                    Mission mission = drone.getAttribute(AttributeType.SPEED);
                    post(() -> missionProperty.set(mission));
                    break;

                case AttributeEvent.GUIDED_POINT_UPDATED:
                case AttributeEvent.STATE_VEHICLE_MODE:
                    post(() -> guidedStateProperty.set(getGuidedState()));
                    break;

                case AttributeEvent.STATE_CONNECTED:
                case AttributeEvent.STATE_DISCONNECTED:
                    checkConnect();
                    break;

                case AttributeEvent.SPEED_UPDATED:
                    Speed speed = drone.getAttribute(AttributeType.SPEED);
                    post(() -> speedProperty.set(speed));
                    break;

                case AttributeEvent.TYPE_UPDATED:
                    Type newDroneType = drone.getAttribute(AttributeType.TYPE);
                    post(() -> typeProperty.set(newDroneType));
                    break;

                case AttributeEvent.ATTITUDE_UPDATED:
                    post(() -> attitudeProperty.set(getAttitude()));
                    break;

                case AttributeEvent.ALTITUDE_UPDATED:
                    post(() -> altitudeProperty.set(getAltitude()));
                    break;

                case AttributeEvent.BATTERY_UPDATED:
                    post(() -> batteryProperty.set(getBattery()));
                    break;

                case AttributeEvent.GPS_COUNT:
                case AttributeEvent.GPS_FIX:
                case AttributeEvent.GPS_POSITION:
                    post(() -> gpsProperty.set(getGps()));
                    break;

                case AttributeEvent.STATE_UPDATED:
                    checkConnect();
                    post(() -> stateProperty.set(getState()));
                    break;

                case AttributeEvent.PARAMETERS_REFRESH_COMPLETED:
                    post(() -> parametersProperty.set(getParameters()));
                    break;
            }
        }


        @Override
        public void onDroneServiceInterrupted(String errorMsg) {
            checkConnect();
        }
    };

    void test() {
        Parameters params = drone.getAttribute(AttributeType.PARAMETERS);
        List<Parameter> parameters = params.getParameters();

    }

    public Drone getDrone() {
        return drone;
    }

    public ReadOnlyBooleanProperty connectedProperty() {
        return connectedProperty;
    }

    public ReadOnlyObjectProperty<Type> typeProperty() {
        return typeProperty;
    }

    public ReadOnlyBooleanProperty enabledProperty() {
        return enabled;
    }

    public boolean getEnabled() {
        return enabled.get();
    }

    public Altitude getAltitude() {
        return drone.getAttribute(AttributeType.ALTITUDE);
    }

    public Attitude getAttitude() {
        return drone.getAttribute(AttributeType.ATTITUDE);
    }

    public Battery getBattery() {
        return drone.getAttribute(AttributeType.BATTERY);
    }

    public Gps getGps() {
        return drone.getAttribute(AttributeType.GPS);
    }

    public Parameters getParameters() {
        return drone.getAttribute(AttributeType.PARAMETERS);
    }

    public State getState() {
        return drone.getAttribute(AttributeType.STATE);
    }

    public ReadOnlyObjectProperty<Altitude> altitudeProperty() {
        return altitudeProperty.getReadOnlyProperty();
    }

    public ReadOnlyObjectProperty<Battery> batteryProperty() {
        return batteryProperty.getReadOnlyProperty();
    }

    public ReadOnlyObjectProperty<Gps> gpsProperty() {
        return gpsProperty.getReadOnlyProperty();
    }

    public ReadOnlyObjectProperty<State> stateProperty() {
        return stateProperty.getReadOnlyProperty();
    }

    public Speed getSpeed() {
        return drone.getAttribute(AttributeType.SPEED);
    }

    public ReadOnlyProperty<Speed> speedProperty() {
        return speedProperty;
    }

    public Mission getMission() {
        return drone.getAttribute(AttributeType.MISSION);
    }

    public Prop<Mission> missionProperty() {
        return missionProperty;
    }

    public GuidedState getGuidedState() {
        return drone.getAttribute(AttributeType.GUIDED_STATE);
    }

    public Prop<GuidedState> guidedStateProperty() {
        return guidedStateProperty;
    }
}
