/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.flightplanning.core.vehicle;

import com.intel.flightplanning.sensor.Sensor;
import com.intel.flightplanning.validators.IValidator;
import java.util.ArrayList;
import java.util.List;

public class UAV {
    // This contains only the description for the capabilities of the drone,
    // Live-connection is out of scope.

    private final List<String> compatibleCameraIds = new ArrayList<>();
    private final List<IValidator> flightValidatorTypes = new ArrayList<>();
    private final List<Sensor> sensort = new ArrayList<>();
    private float minGroundDistance;
    private transient boolean immutable = false;
    private String id = "";
    private String name = "";
    private double angularSpeed;
    private double angularSpeedNoise;
    private double gpsDelay;
    private PreferredTurn preferredTurn = PreferredTurn.NONE;
    private double overshootInM;
    private double turnRadiusInM;
    private double maxLineOfSight;
    private double maxFlightTimeInS;
    private double maxSpeedInMS;
    private double minGroundDistanceInM;
    private double maxClimbAngleInDeg;
    private double maxDiveAngleInDeg;
    private double minWaypointSeparationInM;
    private double maxWaypointSeparationInM;
    private int maxNumberOfWaypoints = 0;
    private boolean insertPhantomWaypoints = false;
    private AirplaneType airplaneType = AirplaneType.SIRIUS_BASIC;
    private boolean compatibleToLitchi;
    private double phantomWaypointsLitchiDistanceInM;
    private float offsetToRightWing;
    private float offsetToTail;
    private double width;

    @Override
    public String toString() {
        return "UAV{"
            + "compatibleCameraIds="
            + compatibleCameraIds
            + ", flightValidatorTypes="
            + flightValidatorTypes
            + ", sensort="
            + sensort
            + ", immutable="
            + immutable
            + ", id='"
            + id
            + '\''
            + ", name='"
            + name
            + '\''
            + ", angularSpeed="
            + angularSpeed
            + ", angularSpeedNoise="
            + angularSpeedNoise
            + ", gpsDelay="
            + gpsDelay
            + ", preferredTurn="
            + preferredTurn
            + ", overshootInM="
            + overshootInM
            + ", turnRadiusInM="
            + turnRadiusInM
            + ", maxLineOfSight="
            + maxLineOfSight
            + ", maxFlightTimeInS="
            + maxFlightTimeInS
            + ", maxSpeedInMS="
            + maxSpeedInMS
            + ", minGroundDistanceInM="
            + minGroundDistanceInM
            + ", maxClimbAngleInDeg="
            + maxClimbAngleInDeg
            + ", maxDiveAngleInDeg="
            + maxDiveAngleInDeg
            + ", minWaypointSeparationInM="
            + minWaypointSeparationInM
            + ", maxWaypointSeparationInM="
            + maxWaypointSeparationInM
            + ", maxNumberOfWaypoints="
            + maxNumberOfWaypoints
            + ", insertPhantomWaypoints="
            + insertPhantomWaypoints
            + ", airplaneType="
            + airplaneType
            + ", compatibleToLitchi="
            + compatibleToLitchi
            + ", phantomWaypointsLitchiDistanceInM="
            + phantomWaypointsLitchiDistanceInM
            + ", offsetToRightWing="
            + offsetToRightWing
            + ", offsetToTail="
            + offsetToTail
            + ", width="
            + width
            + '}';
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public boolean isImmutable() {
        return immutable;
    }

    public void setImmutable(boolean immutable) {
        this.immutable = immutable;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getAngularSpeed() {
        return angularSpeed;
    }

    public void setAngularSpeed(double angularSpeed) {
        this.angularSpeed = angularSpeed;
    }

    public double getAngularSpeedNoise() {
        return angularSpeedNoise;
    }

    public void setAngularSpeedNoise(double angularSpeedNoise) {
        this.angularSpeedNoise = angularSpeedNoise;
    }

    public double getGpsDelay() {
        return gpsDelay;
    }

    public void setGpsDelay(double gpsDelay) {
        this.gpsDelay = gpsDelay;
    }

    public PreferredTurn getPreferredTurn() {
        return preferredTurn;
    }

    public void setPreferredTurn(PreferredTurn preferredTurn) {
        this.preferredTurn = preferredTurn;
    }

    public double getOvershootInM() {
        return overshootInM;
    }

    public void setOvershootInM(double overshootInM) {
        this.overshootInM = overshootInM;
    }

    public double getTurnRadiusInM() {
        return turnRadiusInM;
    }

    public void setTurnRadiusInM(double turnRadiusInM) {
        this.turnRadiusInM = turnRadiusInM;
    }

    public double getMaxLineOfSight() {
        return maxLineOfSight;
    }

    public void setMaxLineOfSight(double maxLineOfSight) {
        this.maxLineOfSight = maxLineOfSight;
    }

    public double getMaxFlightTimeInS() {
        return maxFlightTimeInS;
    }

    public void setMaxFlightTimeInS(double maxFlightTimeInS) {
        this.maxFlightTimeInS = maxFlightTimeInS;
    }

    public double getMaxSpeedInMS() {
        return maxSpeedInMS;
    }

    public void setMaxSpeedInMS(double maxSpeedInMS) {
        this.maxSpeedInMS = maxSpeedInMS;
    }

    public double getMinGroundDistanceInM() {
        return minGroundDistanceInM;
    }

    public void setMinGroundDistanceInM(double minGroundDistanceInM) {
        this.minGroundDistanceInM = minGroundDistanceInM;
    }

    public double getMaxClimbAngleInDeg() {
        return maxClimbAngleInDeg;
    }

    public void setMaxClimbAngleInDeg(double maxClimbAngleInDeg) {
        this.maxClimbAngleInDeg = maxClimbAngleInDeg;
    }

    public double getMaxDiveAngleInDeg() {
        return maxDiveAngleInDeg;
    }

    public void setMaxDiveAngleInDeg(double maxDiveAngleInDeg) {
        this.maxDiveAngleInDeg = maxDiveAngleInDeg;
    }

    public double getMinWaypointSeparationInM() {
        return minWaypointSeparationInM;
    }

    public void setMinWaypointSeparationInM(double minWaypointSeparationInM) {
        this.minWaypointSeparationInM = minWaypointSeparationInM;
    }

    public double getMaxWaypointSeparationInM() {
        return maxWaypointSeparationInM;
    }

    public void setMaxWaypointSeparationInM(double maxWaypointSeparationInM) {
        this.maxWaypointSeparationInM = maxWaypointSeparationInM;
    }

    public int getMaxNumberOfWaypoints() {
        return maxNumberOfWaypoints;
    }

    public void setMaxNumberOfWaypoints(int maxNumberOfWaypoints) {
        this.maxNumberOfWaypoints = maxNumberOfWaypoints;
    }

    public boolean isInsertPhantomWaypoints() {
        return insertPhantomWaypoints;
    }

    public void setInsertPhantomWaypoints(boolean insertPhantomWaypoints) {
        this.insertPhantomWaypoints = insertPhantomWaypoints;
    }

    public AirplaneType getAirplaneType() {
        return airplaneType;
    }

    public void setAirplaneType(AirplaneType airplaneType) {
        this.airplaneType = airplaneType;
    }

    public List<String> getCompatibleCameraIds() {
        return compatibleCameraIds;
    }

    public List<IValidator> getFlightValidatorTypes() {
        return flightValidatorTypes;
    }

    public boolean isCompatibleToLitchi() {
        return compatibleToLitchi;
    }

    public void setCompatibleToLitchi(boolean compatibleToLitchi) {
        this.compatibleToLitchi = compatibleToLitchi;
    }

    public double getPhantomWaypointsLitchiDistanceInM() {
        return phantomWaypointsLitchiDistanceInM;
    }

    public void setPhantomWaypointsLitchiDistanceInM(double phantomWaypointsLitchiDistanceInM) {
        this.phantomWaypointsLitchiDistanceInM = phantomWaypointsLitchiDistanceInM;
    }

    public List<Sensor> getSensort() {
        return sensort;
    }

    public float getOffsetToRightWing() {
        return offsetToRightWing;
    }

    public void setOffsetToRightWing(float offsetToRightWing) {
        this.offsetToRightWing = offsetToRightWing;
    }

    public float getOffsetToTail() {
        return offsetToTail;
    }

    public void setOffsetToTail(float offsetToTail) {
        this.offsetToTail = offsetToTail;
    }

    public float getMinGroundDistance() {
        return minGroundDistance;
    }

    public void setMinGroundDistance(float minGroundDistance) {
        this.minGroundDistance = minGroundDistance;
    }
}
