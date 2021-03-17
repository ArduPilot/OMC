/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.hardware;

import com.intel.missioncontrol.drone.connection.DroneConnectionType;
import com.intel.missioncontrol.drone.validation.FlightValidatorType;
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

public interface IMutablePlatformDescription extends IPlatformDescription {

    void setId(String value);

    void setName(String value);

    void setConnectionType(DroneConnectionType value);

    void setAngularSpeed(Quantity<AngularSpeed> value);

    void setAngularSpeedNoise(Quantity<AngularSpeed> value);

    void setGpsDelay(Quantity<Time> value);

    void setGpsType(GPStype value);

    void setPreferredTurn(AirplanePreferredTurn value);

    /** returns value in m */
    void setOvershoot(Quantity<Length> value);

    /** returns value in m */
    void setTurnRadius(Quantity<Length> value);

    void setMaxLineOfSight(Quantity<Length> value);

    void setMaxFlightTime(Quantity<Time> value);

    /** returns value in km/h */
    void setPlaneSpeed(Quantity<Speed> value);

    void setMaxPlaneSpeed(Quantity<Speed> value);

    // double getPlaneSpeedMperSec();

    // double getPlaneSpeedMaxMperSec();

    void setApType(APTypes value);

    void setPlanIndividualImagePositions(boolean value);

    void setMinWaypointSeparation(Quantity<Length> value);

    void setMaxWaypointSeparation(Quantity<Length> value);

    void setMaxNumberOfWaypoints(int value);

    void setInsertPhantomWaypoints(boolean value);

    void setEmergencyActionsSettable(boolean value);

    void setMinGroundDistance(Quantity<Length> value);

    void setIsInCopterMode(boolean value);

    void setMaxClimbAngle(Quantity<Angle> value);

    void setMaxDiveAngle(Quantity<Angle> value);

    void setIsInMannedEditionMode(boolean value);

    void setIsInFixedWingEditionMode(boolean value);

    void setAirplaneType(AirplaneType value);

    void setImageFile(String imageFile);

    void setConnectionProperties(ConnectionProperties properties);

    void setMavlinkFlightPlanOptions(MavlinkFlightPlanOptions mavlinkFlightPlanOptions);

    void setFlightValidatorTypes(List<FlightValidatorType> flightValidatorTypes);
}
