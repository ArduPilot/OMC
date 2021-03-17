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
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Quaternion;
import java.time.Duration;
import org.asyncfx.beans.property.ReadOnlyAsyncBooleanProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncDoubleProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncIntegerProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncListProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncObjectProperty;
import org.asyncfx.concurrent.Future;

public interface IDrone {
    ReadOnlyAsyncObjectProperty<IHardwareConfiguration> hardwareConfigurationProperty();

    /** main flight battery */
    ReadOnlyAsyncObjectProperty<? extends IBattery> batteryProperty();

    ReadOnlyAsyncObjectProperty<? extends IHealth> healthProperty();

    /**
     * data storage status (e.g. space on sd card). A null value indicates no information available for this drone type.
     */
    ReadOnlyAsyncObjectProperty<? extends IStorage> storageProperty();

    /**
     * remote control status (e.g. connectivity and rc battery state). A null value indicates no information available
     * for this drone type.
     */
    ReadOnlyAsyncObjectProperty<? extends IRemoteControl> remoteControlProperty();

    /**
     * The progress of a flight plan upload, between 0.0 and 1.0, if an upload is ongoing. NaN otherwise. Flight plan
     * upload occurs during execution of takeOffAsync or startFlightPlanAsync.
     */
    ReadOnlyAsyncDoubleProperty flightPlanUploadProgressProperty();

    /**
     * The flight plan currently stored on the drone after uploading one. Null if no flight plan has been uploaded.
     * Flight plan upload occurs during execution of takeOffAsync or startFlightPlanAsync.
     */
    ReadOnlyAsyncObjectProperty<FlightPlan> activeFlightPlanProperty();

    /**
     * The index of the next waypoint of activeFlightPlanProperty, which will be targeted by the drone.
      * Only valid if activeFlightPlanProperty is not null. */
    ReadOnlyAsyncIntegerProperty activeFlightPlanWaypointIndexProperty();

    /** current position (WGS84 lat/lon with altitude in meters above takeoff position) */
    ReadOnlyAsyncObjectProperty<Position> positionProperty();

    ReadOnlyAsyncBooleanProperty positionTelemetryOldProperty();

    /** current attitude (orientation) in the aeronautical frame (right-handed, Z-down, X-front, Y-right) */
    ReadOnlyAsyncObjectProperty<Quaternion> attitudeProperty();

    ReadOnlyAsyncBooleanProperty attitudeTelemetryOldProperty();

    ReadOnlyAsyncObjectProperty<FlightSegment> flightSegmentProperty();

    ReadOnlyAsyncBooleanProperty flightSegmentTelemetryOldProperty();

    ReadOnlyAsyncObjectProperty<Duration> flightTimeProperty();

    ReadOnlyAsyncBooleanProperty flightTimeTelemetryOldProperty();

    ReadOnlyAsyncObjectProperty<? extends IGnssInfo> gnssInfoProperty();

    ReadOnlyAsyncObjectProperty<AutopilotState> autopilotStateProperty();

    ReadOnlyAsyncBooleanProperty autopilotStateTelemetryOldProperty();

    /** List of connected cameras */
    ReadOnlyAsyncListProperty<? extends ICamera> camerasProperty();

    /** Obstacle avoidance including distance sensors */
    ReadOnlyAsyncObjectProperty<? extends IObstacleAvoidance> obstacleAvoidanceProperty();

    // Events
    void addListener(IDroneConnectionExceptionListener droneConnectionExceptionListener);

    void removeListener(IDroneConnectionExceptionListener droneConnectionExceptionListener);

    void addListener(IDroneMessageListener droneMessageListener);

    void removeListener(IDroneMessageListener droneMessageListener);

    // Commands
    Future<Void> takeOffAsync(FlightPlanWithWayPointIndex flightPlanWithWayPointIndex);

    Future<Void> abortTakeOffAsync();

    Future<Void> landAsync();

    Future<Void> abortLandingAsync();

    Future<Void> startFlightPlanAsync(FlightPlanWithWayPointIndex flightPlanWithWayPointIndex);

    Future<Void> pauseFlightPlanAsync();

    Future<Void> returnHomeAsync();
}
