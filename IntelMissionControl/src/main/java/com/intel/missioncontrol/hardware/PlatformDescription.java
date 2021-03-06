/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.hardware;

import com.google.common.base.Objects;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.intel.missioncontrol.NotificationObject;
import com.intel.missioncontrol.drone.connection.DroneConnectionType;
import com.intel.missioncontrol.drone.validation.FlightValidatorType;
import com.intel.missioncontrol.helper.Expect;
import com.intel.missioncontrol.measure.Dimension.Angle;
import com.intel.missioncontrol.measure.Dimension.AngularSpeed;
import com.intel.missioncontrol.measure.Dimension.Length;
import com.intel.missioncontrol.measure.Dimension.Speed;
import com.intel.missioncontrol.measure.Dimension.Time;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.Unit;
import eu.mavinci.core.flightplan.camera.AirplanePreferredTurn;
import eu.mavinci.core.flightplan.camera.GPStype;
import eu.mavinci.core.plane.APTypes;
import eu.mavinci.core.plane.AirplaneType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class PlatformDescription extends NotificationObject implements IMutablePlatformDescription {

    static final PlatformDescription NONE;

    static {
        NONE = new PlatformDescription();
        NONE.setId("NONE");
        NONE.setName("NONE");
        NONE.payloadMountDescriptions.add(new PayloadMountDescription());
        NONE.immutable = true;
    }

    public static class Deserializer implements JsonDeserializer<PlatformDescription> {
        @Override
        public PlatformDescription deserialize(JsonElement arg0, Type arg1, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject json = arg0.getAsJsonObject();
            JsonHelper helper = new JsonHelper(json);
            PlatformDescription description = new PlatformDescription();
            description.setId(helper.getString("id"));
            description.setName(helper.getString("name"));
            description.setConnectionType(helper.getEnum(DroneConnectionType.class, "connectionType"));
            description.setAngularSpeed(Quantity.of(helper.getDouble("angularSpeed"), Unit.DEGREE_PER_SECOND));
            description.setAngularSpeedNoise(
                Quantity.of(helper.getDouble("angularSpeedNoise"), Unit.DEGREE_PER_SECOND));
            description.setGpsDelay(Quantity.of(helper.getDouble("gpsDelayMS"), Unit.MILLISECOND));
            description.setGpsType(GPStype.valueOf(helper.getString("gpsType")));
            description.setPreferredTurn(AirplanePreferredTurn.valueOf(helper.getString("preferredTurn")));
            description.setOvershoot(Quantity.of(helper.getDouble("overshoot"), Unit.METER));
            description.setTurnRadius(Quantity.of(helper.getDouble("turnRadius"), Unit.METER));
            description.setMaxLineOfSight(Quantity.of(helper.getDouble("maxLineOfSightInM"), Unit.METER));
            description.setMaxFlightTime(Quantity.of(helper.getDouble("maxFlighttimeInMinutes"), Unit.MINUTE));
            description.setPlaneSpeed(Quantity.of(helper.getDouble("planeSpeed"), Unit.KILOMETER_PER_HOUR));
            description.setMaxPlaneSpeed(Quantity.of(helper.getDouble("planeSpeedMax"), Unit.KILOMETER_PER_HOUR));
            description.setApType(APTypes.valueOf(helper.getString("apType")));
            description.setMinGroundDistance(Quantity.of(helper.getDouble("minGroundDistanceInM"), Unit.METER));
            description.setMaxClimbAngle(Quantity.of(helper.getDouble("maxClimbAngleDeg"), Unit.DEGREE));
            description.setMaxDiveAngle(Quantity.of(helper.getDouble("maxDiveAngleDeg"), Unit.DEGREE));
            description.setPlanIndividualImagePositions(helper.getBoolean("planIndividualImagePositions"));
            description.setMinWaypointSeparation(Quantity.of(helper.getDouble("minWaypointSeparation"), Unit.METER));
            description.setMaxWaypointSeparation(Quantity.of(helper.getDouble("maxWaypointSeparation"), Unit.METER));
            description.setMaxNumberOfWaypoints(helper.getInteger("maxNumberOfWaypoints"));
            description.setInsertPhantomWaypoints(helper.getBoolean("insertPhantomWaypoints"));
            description.setEmergencyActionsSettable(helper.getBoolean("emergencyActionsSettable"));
            description.setIsInCopterMode(helper.getBoolean("isInCopterMode"));
            description.setIsInFixedWingEditionMode(helper.getBoolean("isInFixedWingMode"));
            description.setIsInMannedEditionMode(helper.getBoolean("isInMannedEditionMode"));
            description.setAirplaneType(AirplaneType.valueOf(helper.getString("airplaneType")));
            description.setJpgMetadataLocationInCameraFrame(helper.getBoolean("isJpgMetadataLocationInCameraFrame"));
            description.setWaypointLocationInCameraFrame(helper.getBoolean("isWaypointLocationInCameraFrame"));

            String imageFile = helper.getNullableString("imageFile");
            if (imageFile != null) {
                description.setImageFile(imageFile);
            }

            JsonArray array = json.getAsJsonArray("compatibleCameraIds");
            if (array != null) {
                for (JsonElement element : array) {
                    description.getCompatibleCameraIds().add(element.getAsString());
                }
            } else {
                JsonHelper.warnMissingKey("compatibleCameraIds");
            }

            description.setIsCompatibleToLitchi(helper.getBoolean("compatibleToLitchi"));

            description.setIsObstacleAvoidanceCapable(helper.getBoolean("isObstacleAvoidanceCapable"));

            description.setPhantomWaypointsLitchiDistance(
                Quantity.of(helper.getDouble("phantomWaypointsLitchiDistance"), Unit.METER));

            array = json.getAsJsonArray("payloadMounts");
            if (array != null) {
                for (JsonElement element : array) {
                    description
                        .getPayloadMountDescriptions()
                        .add(context.deserialize(element, PayloadMountDescription.class));
                }
            } else {
                JsonHelper.warnMissingKey("payloadMounts");
            }

            switch (description.getConnectionType()) {
            case MAVLINK:
                JsonObject element = json.getAsJsonObject("connectionProperties");
                if (element != null) {
                    description.setConnectionProperties(
                        context.deserialize(element, MavlinkConnectionProperties.class));
                } else {
                    JsonHelper.warnMissingKey("connectionProperties");
                }
            case MANUAL:
            case GRPC:
            default:
                // ignore
            }

            JsonObject element = json.getAsJsonObject("mavlinkFlightPlanOptions");
            if (element != null) {
                description.setMavlinkFlightPlanOptions(context.deserialize(element, MavlinkFlightPlanOptions.class));
            } else {
                JsonHelper.warnMissingKey("mavlinkFlightPlanOptions");
            }

            List<FlightValidatorType> flightValidatorTypes = new ArrayList<>();
            array = json.getAsJsonArray("flightValidatorTypes");
            if (array != null) {
                for (JsonElement el : array) {
                    flightValidatorTypes.add(context.deserialize(el, FlightValidatorType.class));
                }
            } else {
                JsonHelper.warnMissingKey("flightValidatorTypes");

                // add defaults:
                flightValidatorTypes.add(FlightValidatorType.FLIGHTPLAN_WARNINGS);
                flightValidatorTypes.add(FlightValidatorType.HARDWARE_COMPATIBILITY);
            }

            description.setFlightValidatorTypes(flightValidatorTypes);

            return description;
        }
    }

    private transient boolean immutable = false;
    private String id = "";
    private String name = "";
    private DroneConnectionType connectionType = DroneConnectionType.MANUAL;
    private Quantity<AngularSpeed> angularSpeed = Quantity.of(0, Unit.RADIAN_PER_SECOND);
    private Quantity<AngularSpeed> angularSpeedNoise = Quantity.of(0, Unit.RADIAN_PER_SECOND);
    private Quantity<Time> gpsDelay = Quantity.of(0, Unit.SECOND);
    private GPStype gpsType = GPStype.GPS;
    private AirplanePreferredTurn preferredTurn = AirplanePreferredTurn.NONE;
    private Quantity<Length> overshoot = Quantity.of(0, Unit.METER);
    private Quantity<Length> turnRadius = Quantity.of(0, Unit.METER);
    private Quantity<Length> maxLineOfSight = Quantity.of(0, Unit.METER);
    private Quantity<Time> maxFlightTime = Quantity.of(0, Unit.SECOND);
    private Quantity<Speed> planeSpeed = Quantity.of(0, Unit.METER_PER_SECOND);
    private Quantity<Speed> maxPlaneSpeed = Quantity.of(0, Unit.METER_PER_SECOND);
    private APTypes apType = APTypes.LATEST;
    private Quantity<Length> minGroundDistance = Quantity.of(0, Unit.METER);
    private Quantity<Angle> maxClimbAngle = Quantity.of(0, Unit.RADIAN);
    private Quantity<Angle> maxDiveAngle = Quantity.of(0, Unit.RADIAN);
    private boolean planIndividualImagePositions;
    private Quantity<Length> minWaypointSeparation = Quantity.of(0, Unit.METER);
    private Quantity<Length> maxWaypointSeparation = Quantity.of(0, Unit.METER);
    private int maxNumberOfWaypoints = 0;

    private boolean insertPhantomWaypoints = false;
    private boolean emergencyActionsSettable = false;
    private boolean isInCopterMode;
    private boolean isInFixedWingEditionMode;
    private boolean isInMannedEditionMode;
    private AirplaneType airplaneType = AirplaneType.SIRIUS_BASIC;
    private String imageFile = "";
    private final List<String> compatibleCameraIds = new ArrayList<>();
    private final List<FlightValidatorType> flightValidatorTypes = new ArrayList<>();
    private boolean compatibleToLitchi;
    private Quantity<Length> phantomWaypointsLitchiDistance = Quantity.of(0, Unit.METER);
    private final List<IPayloadMountDescription> payloadMountDescriptions = new ArrayList<>();
    private ConnectionProperties connectionProperties;
    private MavlinkFlightPlanOptions mavlinkFlightPlanOptions;
    private boolean isObstacleAvoidanceCapable;
    private boolean isJpgMetadataLocationInCameraFrame = false;
    private boolean isWaypointLocationInCameraFrame = false;

    @Override
    public void setId(String value) {
        Expect.notNull(value, "value");
        verifyMutable();
        if (!this.id.equals(value)) {
            String oldValue = this.id;
            this.id = value;
            notifyPropertyChanged(ID_PROPERTY, oldValue, value);
        }
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String value) {
        Expect.notNull(value, "value");
        verifyMutable();
        if (!this.name.equals(value)) {
            String oldValue = this.name;
            this.name = value;
            notifyPropertyChanged(NAME_PROPERTY, oldValue, value);
        }
    }

    @Override
    public DroneConnectionType getConnectionType() {
        return connectionType;
    }

    @Override
    public void setConnectionType(DroneConnectionType value) {
        if (value == null) {
            value = DroneConnectionType.MANUAL;
        }

        verifyMutable();
        if (!this.connectionType.equals(value)) {
            DroneConnectionType oldValue = this.connectionType;
            this.connectionType = value;
            notifyPropertyChanged(CONNECTION_TYPE_PROPERTY, oldValue, value);
        }
    }

    @Override
    public Quantity<AngularSpeed> getAngularSpeed() {
        return angularSpeed;
    }

    @Override
    public void setAngularSpeed(Quantity<AngularSpeed> value) {
        Expect.notNull(value, "value");
        verifyMutable();
        if (!this.angularSpeed.equals(value)) {
            Quantity<AngularSpeed> oldValue = this.angularSpeed;
            this.angularSpeed = value;
            notifyPropertyChanged(ANGULAR_SPEED_PROPERTY, oldValue, value);
        }
    }

    @Override
    public Quantity<AngularSpeed> getAngularSpeedNoise() {
        return angularSpeedNoise;
    }

    @Override
    public void setAngularSpeedNoise(Quantity<AngularSpeed> value) {
        Expect.notNull(value, "value");
        verifyMutable();
        if (!this.angularSpeedNoise.equals(value)) {
            Quantity<AngularSpeed> oldValue = this.angularSpeedNoise;
            this.angularSpeedNoise = value;
            notifyPropertyChanged(ANGULAR_SPEED_NOISE_PROPERTY, oldValue, value);
        }
    }

    @Override
    public Quantity<Time> getGpsDelay() {
        return gpsDelay;
    }

    @Override
    public void setGpsDelay(Quantity<Time> value) {
        Expect.notNull(value, "value");
        verifyMutable();
        if (!this.gpsDelay.equals(value)) {
            Quantity<Time> oldValue = this.gpsDelay;
            this.gpsDelay = value;
            notifyPropertyChanged(GPS_DELAY_PROPERTY, oldValue, value);
        }
    }

    @Override
    public GPStype getGpsType() {
        return gpsType;
    }

    @Override
    public void setGpsType(GPStype value) {
        Expect.notNull(value, "value");
        verifyMutable();
        if (!this.gpsType.equals(value)) {
            GPStype oldValue = this.gpsType;
            this.gpsType = value;
            notifyPropertyChanged(GPS_TYPE_PROPERTY, oldValue, value);
        }
    }

    @Override
    public AirplanePreferredTurn getPreferredTurn() {
        return preferredTurn;
    }

    @Override
    public void setPreferredTurn(AirplanePreferredTurn value) {
        Expect.notNull(value, "value");
        verifyMutable();
        if (!this.preferredTurn.equals(value)) {
            AirplanePreferredTurn oldValue = this.preferredTurn;
            this.preferredTurn = value;
            notifyPropertyChanged(PREFERRED_TURN_PROPERTY, oldValue, value);
        }
    }

    @Override
    public Quantity<Length> getOvershoot() {
        return overshoot;
    }

    @Override
    public void setOvershoot(Quantity<Length> value) {
        Expect.notNull(value, "value");
        verifyMutable();
        if (!this.overshoot.equals(value)) {
            Quantity<Length> oldValue = this.overshoot;
            this.overshoot = value;
            notifyPropertyChanged(OVERSHOOT_PROPERTY, oldValue, value);
        }
    }

    @Override
    public Quantity<Length> getTurnRadius() {
        return turnRadius;
    }

    @Override
    public void setTurnRadius(Quantity<Length> value) {
        Expect.notNull(value, "value");
        verifyMutable();
        if (!this.turnRadius.equals(value)) {
            Quantity<Length> oldValue = this.turnRadius;
            this.turnRadius = value;
            notifyPropertyChanged(TURN_RADIUS_PROPERTY, oldValue, value);
        }
    }

    @Override
    public Quantity<Length> getMaxLineOfSight() {
        return maxLineOfSight;
    }

    @Override
    public void setMaxLineOfSight(Quantity<Length> value) {
        Expect.notNull(value, "value");
        verifyMutable();
        if (!this.maxLineOfSight.equals(value)) {
            Quantity<Length> oldValue = this.maxLineOfSight;
            this.maxLineOfSight = value;
            notifyPropertyChanged(MAX_LINE_OF_SIGHT_PROPERTY, oldValue, value);
        }
    }

    @Override
    public Quantity<Time> getMaxFlightTime() {
        return maxFlightTime;
    }

    @Override
    public void setMaxFlightTime(Quantity<Time> value) {
        Expect.notNull(value, "value");
        verifyMutable();
        if (!this.maxFlightTime.equals(value)) {
            Quantity<Time> oldValue = this.maxFlightTime;
            this.maxFlightTime = value;
            notifyPropertyChanged(MAX_FLIGHT_TIME_PROPERTY, oldValue, value);
        }
    }

    @Override
    public Quantity<Speed> getPlaneSpeed() {
        return planeSpeed;
    }

    @Override
    public void setPlaneSpeed(Quantity<Speed> value) {
        Expect.notNull(value, "value");
        verifyMutable();
        if (!this.planeSpeed.equals(value)) {
            Quantity<Speed> oldValue = this.planeSpeed;
            this.planeSpeed = value;
            notifyPropertyChanged(PLANE_SPEED_PROPERTY, oldValue, value);
        }
    }

    @Override
    public Quantity<Speed> getMaxPlaneSpeed() {
        return maxPlaneSpeed;
    }

    @Override
    public void setMaxPlaneSpeed(Quantity<Speed> value) {
        Expect.notNull(value, "value");
        verifyMutable();
        if (!this.maxPlaneSpeed.equals(value)) {
            Quantity<Speed> oldValue = this.maxPlaneSpeed;
            this.maxPlaneSpeed = value;
            notifyPropertyChanged(MAX_PLANE_SPEED_PROPERTY, oldValue, value);
        }
    }

    @Override
    public APTypes getAPtype() {
        return apType;
    }

    @Override
    public void setApType(APTypes value) {
        Expect.notNull(value, "value");
        verifyMutable();
        if (!this.apType.equals(value)) {
            APTypes oldValue = this.apType;
            this.apType = value;
            notifyPropertyChanged(AP_TYPE_PROPERTY, oldValue, value);
        }
    }

    @Override
    public boolean planIndividualImagePositions() {
        return planIndividualImagePositions;
    }

    @Override
    public void setPlanIndividualImagePositions(boolean value) {
        Expect.notNull(value, "value");
        verifyMutable();
        if (this.planIndividualImagePositions != value) {
            boolean oldValue = this.planIndividualImagePositions;
            this.planIndividualImagePositions = value;
            notifyPropertyChanged(PLAN_INDIVIDUAL_IMAGE_POSITIONS_PROPERTY, oldValue, value);
        }
    }

    @Override
    public Quantity<Length> getMinWaypointSeparation() {
        return minWaypointSeparation;
    }

    @Override
    public void setMinWaypointSeparation(Quantity<Length> value) {
        Expect.notNull(value, "value");
        verifyMutable();
        if (this.minWaypointSeparation != value) {
            Quantity<Length> oldValue = this.minWaypointSeparation;
            this.minWaypointSeparation = value;
            notifyPropertyChanged(MIN_WAYPOINT_SEPARATION_PROPERTY, oldValue, value);
        }
    }

    @Override
    public Quantity<Length> getMaxWaypointSeparation() {
        return maxWaypointSeparation;
    }

    @Override
    public void setMaxWaypointSeparation(Quantity<Length> value) {
        Expect.notNull(value, "value");
        verifyMutable();
        if (this.maxWaypointSeparation != value) {
            Quantity<Length> oldValue = this.maxWaypointSeparation;
            this.maxWaypointSeparation = value;
            notifyPropertyChanged(MAX_WAYPOINT_SEPARATION_PROPERTY, oldValue, value);
        }
    }

    @Override
    public int getMaxNumberOfWaypoints() {
        return maxNumberOfWaypoints;
    }

    @Override
    public void setMaxNumberOfWaypoints(int value) {
        Expect.notNull(value, "value");
        verifyMutable();
        if (this.maxNumberOfWaypoints != value) {
            int oldValue = this.maxNumberOfWaypoints;
            this.maxNumberOfWaypoints = value;
            notifyPropertyChanged(MAX_NUMBER_OF_WAYPOINTS_PROPERTY, oldValue, value);
        }
    }

    @Override
    public boolean getInsertPhantomWaypoints() {
        return insertPhantomWaypoints;
    }

    @Override
    public void setInsertPhantomWaypoints(boolean value) {
        Expect.notNull(value, "value");
        verifyMutable();
        if (this.insertPhantomWaypoints != value) {
            boolean oldValue = this.insertPhantomWaypoints;
            this.insertPhantomWaypoints = value;
            notifyPropertyChanged(INSERT_PHANTOM_WAYPOINTS_PROPERTY, oldValue, value);
        }
    }

    @Override
    public boolean areEmergencyActionsSettable() {
        return emergencyActionsSettable;
    }

    @Override
    public void setEmergencyActionsSettable(boolean value) {
        Expect.notNull(value, "value");
        verifyMutable();
        if (this.emergencyActionsSettable != value) {
            boolean oldValue = this.emergencyActionsSettable;
            this.emergencyActionsSettable = value;
            notifyPropertyChanged(EMERGENCY_ACTIONS_SETTABLE_PROPERTY, oldValue, value);
        }
    }

    @Override
    public Quantity<Length> getMinGroundDistance() {
        return minGroundDistance;
    }

    @Override
    public void setMinGroundDistance(Quantity<Length> value) {
        Expect.notNull(value, "value");
        verifyMutable();
        if (!this.minGroundDistance.equals(value)) {
            Quantity<Length> oldValue = this.minGroundDistance;
            this.minGroundDistance = value;
            notifyPropertyChanged(MIN_GROUND_DISTANCE_PROPERTY, oldValue, value);
        }
    }

    @Override
    public boolean isInCopterMode() {
        return isInCopterMode;
    }

    @Override
    public void setIsInCopterMode(boolean value) {
        Expect.notNull(value, "value");
        verifyMutable();
        if (this.isInCopterMode != value) {
            boolean oldValue = this.isInCopterMode;
            this.isInCopterMode = value;
            notifyPropertyChanged(IS_IN_COPTER_MODE_PROPERTY, oldValue, value);
        }
    }

    @Override
    public Quantity<Angle> getMaxClimbAngle() {
        return maxClimbAngle;
    }

    @Override
    public void setMaxClimbAngle(Quantity<Angle> value) {
        Expect.notNull(value, "value");
        verifyMutable();
        if (!this.maxClimbAngle.equals(value)) {
            Quantity<Angle> oldValue = this.maxClimbAngle;
            this.maxClimbAngle = value;
            notifyPropertyChanged(MAX_CLIMB_ANGLE_PROPERTY, oldValue, value);
        }
    }

    @Override
    public Quantity<Angle> getMaxDiveAngle() {
        return maxDiveAngle;
    }

    @Override
    public void setMaxDiveAngle(Quantity<Angle> value) {
        Expect.notNull(value, "value");
        verifyMutable();
        if (!this.maxDiveAngle.equals(value)) {
            Quantity<Angle> oldValue = this.maxDiveAngle;
            this.maxDiveAngle = value;
            notifyPropertyChanged(MAX_DIVE_ANGLE_PROPERTY, oldValue, value);
        }
    }

    @Override
    public boolean isInMannedEditionMode() {
        return isInMannedEditionMode;
    }

    @Override
    public void setIsInMannedEditionMode(boolean value) {
        Expect.notNull(value, "value");
        verifyMutable();
        if (this.isInMannedEditionMode != value) {
            boolean oldValue = this.isInMannedEditionMode;
            this.isInMannedEditionMode = value;
            notifyPropertyChanged(IS_IN_MANNED_EDITION_MODE_PROPERTY, oldValue, value);
        }
    }

    @Override
    public boolean isInFixedWingEditionMode() {
        return isInFixedWingEditionMode;
    }

    @Override
    public void setIsInFixedWingEditionMode(boolean value) {
        Expect.notNull(value, "value");
        verifyMutable();
        if (this.isInFixedWingEditionMode != value) {
            boolean oldValue = this.isInFixedWingEditionMode;
            this.isInFixedWingEditionMode = value;
            notifyPropertyChanged(IS_IN_FIXED_WING_EDITION_MODE_PROPERTY, oldValue, value);
        }
    }

    @Override
    public AirplaneType getAirplaneType() {
        return airplaneType;
    }

    @Override
    public void setAirplaneType(AirplaneType value) {
        Expect.notNull(value, "value");
        verifyMutable();
        if (this.airplaneType != value) {
            AirplaneType oldValue = this.airplaneType;
            this.airplaneType = value;
            notifyPropertyChanged(AIRPLANE_TYPE_PROPERTY, oldValue, value);
        }
    }

    @Override
    public String getImageFile() {
        return imageFile;
    }

    @Override
    public void setImageFile(String value) {
        Expect.notNull(value, "value");
        verifyMutable();
        if (!this.imageFile.equals(value)) {
            String oldValue = this.imageFile;
            this.imageFile = value;
            notifyPropertyChanged(IMAGE_FILE_PROPERTY, oldValue, value);
        }
    }

    @Override
    public List<IPayloadMountDescription> getPayloadMountDescriptions() {
        return payloadMountDescriptions;
    }

    @Override
    public List<String> getCompatibleCameraIds() {
        return compatibleCameraIds;
    }

    @Override
    public List<FlightValidatorType> getFlightValidatorTypes() {
        return Collections.unmodifiableList(flightValidatorTypes);
    }

    @Override
    public void setFlightValidatorTypes(List<FlightValidatorType> flightValidatorTypes) {
        this.flightValidatorTypes.clear();
        this.flightValidatorTypes.addAll(flightValidatorTypes);
    }

    @Override
    public void setJpgMetadataLocationInCameraFrame(boolean value) {
        Expect.notNull(value, "value");
        verifyMutable();
        if (this.isJpgMetadataLocationInCameraFrame != value) {
            boolean oldValue = this.isJpgMetadataLocationInCameraFrame;
            this.isJpgMetadataLocationInCameraFrame = value;
            notifyPropertyChanged(IS_JPG_METADATA_LOCATION_IN_CAMERA_FRAME, oldValue, value);
        }
    }

    @Override
    public void setWaypointLocationInCameraFrame(boolean value) {
        Expect.notNull(value, "value");
        verifyMutable();
        if (this.isWaypointLocationInCameraFrame != value) {
            boolean oldValue = this.isWaypointLocationInCameraFrame;
            this.isWaypointLocationInCameraFrame = value;
            notifyPropertyChanged(IS_WAYPOINT_LOCATION_IN_CAMERA_FRAME, oldValue, value);
        }
    }

    @Override
    public boolean isCompatibleToLitchi() {
        return compatibleToLitchi;
    }

    @Override
    public void setIsCompatibleToLitchi(boolean value) {
        Expect.notNull(value, "value");
        verifyMutable();
        if (this.compatibleToLitchi != value) {
            boolean oldValue = this.compatibleToLitchi;
            this.compatibleToLitchi = value;
            notifyPropertyChanged(IS_COMPATIBLE_TO_LITCHI_PROPERTY, oldValue, value);
        }
    }

    @Override
    public boolean isObstacleAvoidanceCapable() {
        return isObstacleAvoidanceCapable;
    }

    @Override
    public void setIsObstacleAvoidanceCapable(boolean value) {
        Expect.notNull(value, "value");
        verifyMutable();
        if (this.isObstacleAvoidanceCapable != value) {
            boolean oldValue = this.isObstacleAvoidanceCapable;
            this.isObstacleAvoidanceCapable = value;
            notifyPropertyChanged(IS_OBSTACLE_AVOIDANCE_CAPABLE, oldValue, value);
        }
    }

    @Override
    public boolean isInsertPhantomWaypointsLitchi() {
        return !phantomWaypointsLitchiDistance.equals(Quantity.of(0, Unit.METER));
    }

    @Override
    public Quantity<Length> getPhantomWaypointsLitchiDistance() {
        return phantomWaypointsLitchiDistance;
    }

    @Override
    public void setPhantomWaypointsLitchiDistance(Quantity<Length> value) {
        Expect.notNull(value, "value");
        verifyMutable();
        if (this.phantomWaypointsLitchiDistance != value) {
            Quantity<Length> oldValue = this.phantomWaypointsLitchiDistance;
            this.phantomWaypointsLitchiDistance = value;
            notifyPropertyChanged(PHANTOM_WAYPOINTS_LITCHI_DISTANCE_PROPERTY, oldValue, value);
        }
    }

    @Override
    public boolean isJpgMetadataLocationInCameraFrame() {
        return isJpgMetadataLocationInCameraFrame;
    }

    @Override
    public boolean isWaypointLocationInCameraFrame() {
        return isWaypointLocationInCameraFrame;
    }

    @Override
    public IConnectionProperties getConnectionProperties() {
        return connectionProperties;
    }

    @Override
    public IMavlinkFlightPlanOptions getMavlinkFlightPlanOptions() {
        return mavlinkFlightPlanOptions;
    }

    public void setConnectionProperties(ConnectionProperties value) {
        Expect.notNull(value, "value");
        verifyMutable();
        if (connectionProperties == null || !this.connectionProperties.equals(value)) {
            ConnectionProperties oldValue = this.connectionProperties;
            this.connectionProperties = value;
            notifyPropertyChanged(CONNECTION_PROPERTIES_PROPERTY, oldValue, value);
        }
    }

    public void setMavlinkFlightPlanOptions(MavlinkFlightPlanOptions value) {
        Expect.notNull(value, "value");
        verifyMutable();
        if (mavlinkFlightPlanOptions == null || !this.mavlinkFlightPlanOptions.equals(value)) {
            MavlinkFlightPlanOptions oldValue = this.mavlinkFlightPlanOptions;
            this.mavlinkFlightPlanOptions = value;
            notifyPropertyChanged(MAVLINK_FLIGHTPLAN_OPTIONS_PROPERTY, oldValue, value);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (!(obj instanceof PlatformDescription)) {
            return false;
        }

        PlatformDescription other = (PlatformDescription)obj;
        return id.equals(other.id)
            && name.equals(other.name)
            && connectionType.equals(other.connectionType)
            && angularSpeed.equals(other.angularSpeed)
            && angularSpeedNoise.equals(other.angularSpeedNoise)
            && gpsDelay.equals(other.gpsDelay)
            && gpsType == other.gpsType
            && preferredTurn == other.preferredTurn
            && overshoot.equals(other.overshoot)
            && turnRadius.equals(other.turnRadius)
            && maxLineOfSight.equals(other.maxLineOfSight)
            && maxFlightTime.equals(other.maxFlightTime)
            && planeSpeed.equals(other.planeSpeed)
            && maxPlaneSpeed.equals(other.maxPlaneSpeed)
            && apType == other.apType
            && minGroundDistance.equals(other.minGroundDistance)
            && maxClimbAngle.equals(other.maxClimbAngle)
            && maxDiveAngle.equals(other.maxDiveAngle)
            && planIndividualImagePositions == other.planIndividualImagePositions
            && minWaypointSeparation == other.minWaypointSeparation
            && maxWaypointSeparation == other.maxWaypointSeparation
            && maxNumberOfWaypoints == other.maxNumberOfWaypoints
            && insertPhantomWaypoints == other.insertPhantomWaypoints
            && isInCopterMode == other.isInCopterMode
            && isInFixedWingEditionMode == other.isInFixedWingEditionMode
            && isInMannedEditionMode == other.isInMannedEditionMode
            && airplaneType == other.airplaneType
            && imageFile.equals(other.imageFile)
            && compatibleCameraIds.equals(other.compatibleCameraIds)
            && flightValidatorTypes.equals(other.flightValidatorTypes)
            && compatibleToLitchi == other.compatibleToLitchi
            && phantomWaypointsLitchiDistance.equals(other.phantomWaypointsLitchiDistance)
            && payloadMountDescriptions.equals(other.payloadMountDescriptions)
            && connectionProperties.equals(other.connectionProperties)
            && mavlinkFlightPlanOptions.equals(other.mavlinkFlightPlanOptions)
            && isObstacleAvoidanceCapable == other.isObstacleAvoidanceCapable;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(
            id,
            name,
            connectionType,
            angularSpeed,
            angularSpeedNoise,
            gpsDelay,
            gpsType,
            preferredTurn,
            overshoot,
            turnRadius,
            maxLineOfSight,
            maxFlightTime,
            planeSpeed,
            maxPlaneSpeed,
            apType,
            minGroundDistance,
            maxClimbAngle,
            maxDiveAngle,
            planIndividualImagePositions,
            minWaypointSeparation,
            maxWaypointSeparation,
            maxNumberOfWaypoints,
            insertPhantomWaypoints,
            isInCopterMode,
            isInFixedWingEditionMode,
            isInMannedEditionMode,
            airplaneType,
            imageFile,
            compatibleCameraIds,
            flightValidatorTypes,
            compatibleToLitchi,
            phantomWaypointsLitchiDistance,
            payloadMountDescriptions,
            connectionProperties,
            mavlinkFlightPlanOptions,
            isObstacleAvoidanceCapable);
    }

    private void verifyMutable() {
        if (immutable) {
            throw new IllegalStateException("The configuration is immutable and cannot be changed.");
        }
    }

}
