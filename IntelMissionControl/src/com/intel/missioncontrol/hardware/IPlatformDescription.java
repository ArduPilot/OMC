/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.hardware;

import com.intel.missioncontrol.INotificationObject;
import com.intel.missioncontrol.measure.Dimension.Angle;
import com.intel.missioncontrol.measure.Dimension.AngularSpeed;
import com.intel.missioncontrol.measure.Dimension.Length;
import com.intel.missioncontrol.measure.Dimension.Speed;
import com.intel.missioncontrol.measure.Dimension.Time;
import com.intel.missioncontrol.measure.Quantity;
import eu.mavinci.core.flightplan.camera.AirplanePreferredTurn;
import eu.mavinci.core.flightplan.camera.GPStype;
import eu.mavinci.core.plane.APTypes;
import eu.mavinci.core.plane.AirplaneType;
import java.util.List;

/**
 * IPlatformDescription contains hardware specifications of the vehicle. Provides a list of slots for the payloads
 * connection
 */
public interface IPlatformDescription extends INotificationObject {

    String ID_PROPERTY = "id";
    String NAME_PROPERTY = "name";
    String ANGULAR_SPEED_PROPERTY = "angularSpeed";
    String ANGULAR_SPEED_NOISE_PROPERTY = "angularSpeedNoise";
    String GPS_DELAY_PROPERTY = "gpsDelay";
    String GPS_TYPE_PROPERTY = "gpsType";
    String PREFERRED_TURN_PROPERTY = "preferredTurn";
    String OVERSHOOT_PROPERTY = "overshoot";
    String TURN_RADIUS_PROPERTY = "turnRadius";
    String MAX_LINE_OF_SIGHT_PROPERTY = "maxLineOfSight";
    String MAX_FLIGHT_TIME_PROPERTY = "maxFlightTime";
    String PLANE_SPEED_PROPERTY = "planeSpeed";
    String MAX_PLANE_SPEED_PROPERTY = "maxPlaneSpeed";
    String AP_TYPE_PROPERTY = "apType";
    String PLAN_INDIVIDUAL_IMAGE_POSITIONS_PROPERTY = "planIndividualImagePositions";
    String MIN_WAYPOINT_SEPARATION_PROPERTY = "minWaypointSeparation";
    String MAX_WAYPOINT_SEPARATION_PROPERTY = "maxWaypointSeparation";
    String MAX_NUMBER_OF_WAYPOINTS_PROPERTY = "maxNumberOfWaypoints";
    String INSERT_PHANTOM_WAYPOINTS_PROPERTY = "insertPhantomWaypoints";
    String MIN_GROUND_DISTANCE_PROPERTY = "minGroundDistance";
    String IS_IN_COPTER_MODE_PROPERTY = "isInCopterMode";
    String IS_IN_MANNED_EDITION_MODE_PROPERTY = "isInMannedEditionMode";
    String IS_IN_FIXED_WING_EDITION_MODE_PROPERTY = "isInFixedWingEditionMode";
    String MAX_CLIMB_ANGLE_PROPERTY = "maxClimbAngle";
    String MAX_DIVE_ANGLE_PROPERTY = "maxDiveAngle";
    String AIRPLANE_TYPE_PROPERTY = "airplaneType";
    String IMAGE_FILE_PROPERTY = "imageFile";
    String PAYLOAD_MOUNT_DESCRIPTIONS_PROPERTY = "payloadMountDescriptions";
    String CONNECTION_PROPERTIES_PROPERTY = "connectionProperties";
    String COMPATIBLE_CAMERA_IDS_PROPERTY = "compatibleCameraIds";

    String getId();

    String getName();

    Quantity<AngularSpeed> getAngularSpeed();

    Quantity<AngularSpeed> getAngularSpeedNoise();

    Quantity<Time> getGpsDelay();

    GPStype getGpsType();

    AirplanePreferredTurn getPreferredTurn();

    Quantity<Length> getOvershoot();

    Quantity<Length> getTurnRadius();

    Quantity<Length> getMaxLineOfSight();

    Quantity<Time> getMaxFlightTime();

    Quantity<Speed> getPlaneSpeed();

    Quantity<Speed> getMaxPlaneSpeed();

    APTypes getAPtype();

    boolean planIndividualImagePositions();

    Quantity<Length> getMinWaypointSeparation();

    Quantity<Length> getMaxWaypointSeparation();

    int getMaxNumberOfWaypoints();

    boolean getInsertPhantomWaypoints();

    Quantity<Length> getMinGroundDistance();

    boolean isInCopterMode();

    Quantity<Angle> getMaxClimbAngle();

    Quantity<Angle> getMaxDiveAngle();

    boolean isInMannedEditionMode();

    boolean isInFixedWingEditionMode();

    AirplaneType getAirplaneType();

    String getImageFile();

    List<IPayloadMountDescription> getPayloadMountDescriptions();

    IConnectionProperties getConnectionProperties();

    List<String> getCompatibleCameraIds();

    default IMutablePlatformDescription asMutable() {
        return (IMutablePlatformDescription)this;
    }

}
