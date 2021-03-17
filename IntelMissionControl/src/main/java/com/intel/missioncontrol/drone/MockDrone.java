/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone;

import com.intel.missioncontrol.drone.connection.IDroneConnectionExceptionListener;
import com.intel.missioncontrol.drone.connection.IDroneMessageListener;
import com.intel.missioncontrol.hardware.IHardwareConfiguration;
import com.intel.missioncontrol.mission.FlightPlan;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Quaternion;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import org.asyncfx.beans.property.AsyncBooleanProperty;
import org.asyncfx.beans.property.AsyncDoubleProperty;
import org.asyncfx.beans.property.AsyncIntegerProperty;
import org.asyncfx.beans.property.AsyncListProperty;
import org.asyncfx.beans.property.AsyncObjectProperty;
import org.asyncfx.beans.property.PropertyMetadata;
import org.asyncfx.beans.property.ReadOnlyAsyncBooleanProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncDoubleProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncIntegerProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncListProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncObjectProperty;
import org.asyncfx.beans.property.SimpleAsyncBooleanProperty;
import org.asyncfx.beans.property.SimpleAsyncDoubleProperty;
import org.asyncfx.beans.property.SimpleAsyncIntegerProperty;
import org.asyncfx.beans.property.SimpleAsyncListProperty;
import org.asyncfx.beans.property.SimpleAsyncObjectProperty;
import org.asyncfx.collections.AsyncObservableList;
import org.asyncfx.collections.FXAsyncCollections;
import org.asyncfx.concurrent.Future;
import org.asyncfx.concurrent.Futures;

public class MockDrone implements IDrone {

    private final AsyncBooleanProperty attitudeTelemetryOld = new SimpleAsyncBooleanProperty(this);
    private final AsyncBooleanProperty positionTelemetryOld = new SimpleAsyncBooleanProperty(this);
    private final AsyncBooleanProperty flightSegmentTelemetryOld = new SimpleAsyncBooleanProperty(this);
    private final AsyncBooleanProperty flightTimeTelemetryOld = new SimpleAsyncBooleanProperty(this);
    private final AsyncBooleanProperty autopilotStateTelemetryOld = new SimpleAsyncBooleanProperty(this);
    private final AsyncObjectProperty<IHardwareConfiguration> hardwareConfiguration =
            new SimpleAsyncObjectProperty<>(this);
    private final AsyncObjectProperty<IBattery> battery = new SimpleAsyncObjectProperty<>(this);
    private final AsyncObjectProperty<Health> health = new SimpleAsyncObjectProperty<>(this);
    private final AsyncObjectProperty<Storage> storage = new SimpleAsyncObjectProperty<>(this);
    private final AsyncObjectProperty<RemoteControl> remoteControl = new SimpleAsyncObjectProperty<>(this);
    private final AsyncObjectProperty<GnssInfo> gnssInfo = new SimpleAsyncObjectProperty<>(this);
    private final AsyncObjectProperty<Position> position = new SimpleAsyncObjectProperty<>(this);
    private final AsyncObjectProperty<Quaternion> attitude = new SimpleAsyncObjectProperty<>(this);
    private final AsyncObjectProperty<FlightSegment> flightSegment =
            new SimpleAsyncObjectProperty<>(
                    this, new PropertyMetadata.Builder<FlightSegment>().initialValue(FlightSegment.UNKNOWN).create());
    private final AsyncObjectProperty<AutopilotState> autopilotState =
            new SimpleAsyncObjectProperty<>(
                    this, new PropertyMetadata.Builder<AutopilotState>().initialValue(AutopilotState.UNKNOWN).create());
    private final AsyncObjectProperty<Duration> flightTime =
            new SimpleAsyncObjectProperty<>(
                    this, new PropertyMetadata.Builder<Duration>().initialValue(Duration.ZERO).create());
    private final AsyncDoubleProperty flightPlanUploadProgress =
            new SimpleAsyncDoubleProperty(this, new PropertyMetadata.Builder<Number>().initialValue(Double.NaN).create());
    private final AsyncObjectProperty<FlightPlan> activeFlightPlan = new SimpleAsyncObjectProperty<>(this);
    private final AsyncIntegerProperty activeFlightPlanWaypointIndex = new SimpleAsyncIntegerProperty(this);
    private final AsyncListProperty<MavlinkCamera> cameras = new SimpleAsyncListProperty<>(this);
    private final AsyncObjectProperty<IObstacleAvoidance> obstacleAvoidance = new SimpleAsyncObjectProperty<>(this);
    private final DistanceSensor distanceSensor;
    private final AsyncListProperty<DistanceSensor> distanceSensors =
            new SimpleAsyncListProperty<>(
                    this,
                    new PropertyMetadata.Builder<AsyncObservableList<DistanceSensor>>()
                            .initialValue(FXAsyncCollections.observableArrayList())
                            .create());
    private final SimpleAsyncObjectProperty<IObstacleAvoidance.Mode> mode =
            new SimpleAsyncObjectProperty<>(
                    this,
                    new PropertyMetadata.Builder<IObstacleAvoidance.Mode>()
                            .initialValue(IObstacleAvoidance.Mode.ENABLED)
                            .create());

    public MockDrone(IHardwareConfiguration hardwareConfiguration) {
        this.hardwareConfiguration.set(hardwareConfiguration);

        distanceSensor = new DistanceSensor();
        distanceSensor.alertLevelProperty().setValue(DistanceSensor.AlertLevel.LEVEL0_CRITICAL);
        distanceSensor.closestDistanceMetersProperty().setValue(1);
        distanceSensors.add(distanceSensor);

        obstacleAvoidance.set(getObstacleAvoidance());

        position.setValue(new Position(LatLon.fromDegrees(50.0379, 8.5622), 10));

        battery.setValue(getBatteryInfo());
    }

    private IBattery getBatteryInfo() {
        return new IBattery() {
            private final AsyncDoubleProperty remainingChargePercentage = new SimpleAsyncDoubleProperty(this);
            private final AsyncDoubleProperty voltage = new SimpleAsyncDoubleProperty(this);
            private final AsyncObjectProperty<BatteryAlertLevel> batteryAlertLevel =
                    new SimpleAsyncObjectProperty<>(this);

            @Override
            public ReadOnlyAsyncDoubleProperty remainingChargePercentageProperty() {
                remainingChargePercentage.set(10.0);
                return remainingChargePercentage;
            }

            @Override
            public double getRemainingChargePercentage() {
                remainingChargePercentage.set(10.0);
                return remainingChargePercentage.get();
            }

            @Override
            public ReadOnlyAsyncDoubleProperty voltageProperty() {
                voltage.set(10.0);
                return voltage;
            }

            @Override
            public double getVoltage() {
                return voltage.get();
            }

            @Override
            public ReadOnlyAsyncObjectProperty<BatteryAlertLevel> alertLevelProperty() {
                batteryAlertLevel.setValue(BatteryAlertLevel.RED);
                return batteryAlertLevel;
            }

            @Override
            public BatteryAlertLevel getAlertLevel() {
                batteryAlertLevel.setValue(BatteryAlertLevel.RED);
                return batteryAlertLevel.get();
            }

            @Override
            public ReadOnlyAsyncBooleanProperty telemetryOldProperty() {
                return null;
            }
        };
    }

    private IObstacleAvoidance getObstacleAvoidance() {
        return new IObstacleAvoidance() {
            private AtomicInteger counter = new AtomicInteger(0);

            @Override
            public ReadOnlyAsyncListProperty<? extends IDistanceSensor> distanceSensorsProperty() {
                return distanceSensors;
            }

            @Override
            public IDistanceSensor getAggregatedDistanceSensor() {
                return distanceSensor;
            }

            @Override
            public ReadOnlyAsyncObjectProperty<Mode> modeProperty() {
                return mode;
            }

            @Override
            public Future<Void> enableAsync(boolean enable) {
                // return Futures.failed(new Throwable("Mock Drone - Obstacle Avoidance - TESTING"));
                return successScenario(enable);
                // return failureScenario(enable);
            }

            private Future<Void> successScenario(boolean enable) {
                if (!enable) {
                    distanceSensor.alertLevelProperty().setValue(DistanceSensor.AlertLevel.UNKNOWN);
                    distanceSensor.closestDistanceMetersProperty().setValue(Double.NEGATIVE_INFINITY);
                    mode.setValue(Mode.DISABLED);
                    if (counter.incrementAndGet() == 3) counter.set(0);
                } else {
                    if (counter.incrementAndGet() == 3) {
                        distanceSensor.alertLevelProperty().setValue(DistanceSensor.AlertLevel.LEVEL0_CRITICAL);
                        distanceSensor.closestDistanceMetersProperty().setValue(0);
                        mode.setValue(Mode.ENABLED);
                        counter.set(0);
                    } else {
                        distanceSensor.alertLevelProperty().setValue(DistanceSensor.AlertLevel.LEVEL3);
                        distanceSensor.closestDistanceMetersProperty().setValue(5);
                        mode.setValue(Mode.ENABLED);
                    }
                }


                return Futures.successful();
            }
        };
    }

    DistanceSensor getDistanceSensor() {
        return distanceSensor;
    }

    @Override
    public ReadOnlyAsyncObjectProperty<IHardwareConfiguration> hardwareConfigurationProperty() {
        return hardwareConfiguration;
    }

    @Override
    public ReadOnlyAsyncObjectProperty<? extends IBattery> batteryProperty() {
        return battery;
    }

    @Override
    public ReadOnlyAsyncObjectProperty<? extends IHealth> healthProperty() {
        return health;
    }

    @Override
    public ReadOnlyAsyncObjectProperty<? extends IStorage> storageProperty() {
        return storage;
    }

    @Override
    public ReadOnlyAsyncObjectProperty<? extends IRemoteControl> remoteControlProperty() {
        return remoteControl;
    }

    @Override
    public ReadOnlyAsyncDoubleProperty flightPlanUploadProgressProperty() {
        return flightPlanUploadProgress;
    }

    @Override
    public ReadOnlyAsyncObjectProperty<FlightPlan> activeFlightPlanProperty() {
        return activeFlightPlan;
    }

    @Override
    public ReadOnlyAsyncIntegerProperty activeFlightPlanWaypointIndexProperty() {
        return activeFlightPlanWaypointIndex;
    }

    @Override
    public ReadOnlyAsyncObjectProperty<Position> positionProperty() {
        return position;
    }

    @Override
    public ReadOnlyAsyncBooleanProperty positionTelemetryOldProperty() {
        return positionTelemetryOld;
    }

    @Override
    public ReadOnlyAsyncObjectProperty<Quaternion> attitudeProperty() {
        return attitude;
    }

    @Override
    public ReadOnlyAsyncBooleanProperty attitudeTelemetryOldProperty() {
        return attitudeTelemetryOld;
    }

    @Override
    public ReadOnlyAsyncObjectProperty<FlightSegment> flightSegmentProperty() {
        return flightSegment;
    }

    @Override
    public ReadOnlyAsyncBooleanProperty flightSegmentTelemetryOldProperty() {
        return flightSegmentTelemetryOld;
    }

    @Override
    public ReadOnlyAsyncObjectProperty<Duration> flightTimeProperty() {
        return flightTime;
    }

    @Override
    public ReadOnlyAsyncBooleanProperty flightTimeTelemetryOldProperty() {
        return flightTimeTelemetryOld;
    }

    @Override
    public ReadOnlyAsyncObjectProperty<? extends IGnssInfo> gnssInfoProperty() {
        return gnssInfo;
    }

    @Override
    public ReadOnlyAsyncObjectProperty<AutopilotState> autopilotStateProperty() {
        return autopilotState;
    }

    @Override
    public ReadOnlyAsyncBooleanProperty autopilotStateTelemetryOldProperty() {
        return autopilotStateTelemetryOld;
    }

    @Override
    public ReadOnlyAsyncListProperty<? extends ICamera> camerasProperty() {
        return cameras;
    }

    @Override
    public ReadOnlyAsyncObjectProperty<? extends IObstacleAvoidance> obstacleAvoidanceProperty() {
        return obstacleAvoidance;
    }

    @Override
    public void addListener(IDroneConnectionExceptionListener droneConnectionExceptionListener) {
    }

    @Override
    public void removeListener(IDroneConnectionExceptionListener droneConnectionExceptionListener) {
    }

    @Override
    public void addListener(IDroneMessageListener droneMessageListener) {
    }

    @Override
    public void removeListener(IDroneMessageListener droneMessageListener) {
    }

    @Override
    public Future<Void> takeOffAsync(FlightPlanWithWayPointIndex flightPlanWithWayPointIndex) {
        return Futures.successful();
    }

    @Override
    public Future<Void> abortTakeOffAsync() {
        return Futures.successful();
    }

    @Override
    public Future<Void> landAsync() {
        return Futures.successful();
    }

    @Override
    public Future<Void> abortLandingAsync() {
        return Futures.successful();
    }

    @Override
    public Future<Void> startFlightPlanAsync(FlightPlanWithWayPointIndex flightPlanWithWayPointIndex) {
        return Futures.successful();
    }

    @Override
    public Future<Void> pauseFlightPlanAsync() {
        return Futures.successful();
    }

    @Override
    public Future<Void> returnHomeAsync() {
        return Futures.successful();
    }

}
