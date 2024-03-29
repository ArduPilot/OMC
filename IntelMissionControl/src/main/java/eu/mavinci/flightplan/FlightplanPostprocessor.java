/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.flightplan;

import static gov.nasa.worldwind.geom.Angle.normalizedDegrees;

import com.intel.missioncontrol.StaticInjector;
import com.intel.missioncontrol.hardware.IGenericCameraConfiguration;
import com.intel.missioncontrol.hardware.IHardwareConfiguration;
import com.intel.missioncontrol.hardware.IPayloadDescription;
import com.intel.missioncontrol.hardware.IPlatformDescription;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.map.elevation.IElevationModel;
import com.intel.missioncontrol.map.worldwind.IWWGlobes;
import com.intel.missioncontrol.measure.Unit;
import eu.mavinci.core.flightplan.FlightplanContainerFullException;
import eu.mavinci.core.flightplan.FlightplanContainerWrongAddingException;
import eu.mavinci.core.flightplan.FlightplanSpeedModes;
import eu.mavinci.core.flightplan.IFlightplanContainer;
import eu.mavinci.core.flightplan.Orientation;
import eu.mavinci.core.flightplan.PlanType;
import eu.mavinci.core.flightplan.SpeedMode;
import eu.mavinci.core.flightplan.visitors.CollectsTypeVisitor;
import eu.mavinci.core.helper.MinMaxPair;
import eu.mavinci.desktop.main.debug.Debug;
import eu.mavinci.flightplan.computation.FastPositionTransformationProvider;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import java.util.Vector;
import java.util.logging.Level;

public class FlightplanPostprocessor {
    private Flightplan flightplan;
    private FastPositionTransformationProvider fastPositionTransformationProvider;
    private Vector<Waypoint> wpList;
    private Vector<Vec4> wpListVec;
    private IHardwareConfiguration hardwareConfiguration;
    private PhotoSettings photoSettings;
    private ReferencePoint refPoint;

    public static final double MIN_FALCON_SPEED = 0.1; // meters per sec

    private static final IElevationModel elevationModel = StaticInjector.getInstance(IElevationModel.class);

    public static final String DJI_PHANTOM_WAYPOINT_BODY =
        StaticInjector.getInstance(ILanguageHelper.class).getString("eu.mavinci.flightplan.DJIPhantomWaypointBody");

    public FlightplanPostprocessor(Flightplan flightplan) {
        this.flightplan = flightplan;
        this.fastPositionTransformationProvider = flightplan.fastPositionTransformationProvider;

        CollectsTypeVisitor<Waypoint> visCollect = new CollectsTypeVisitor<>(Waypoint.class);
        visCollect.startVisit(flightplan);

        wpList = visCollect.matches;
        wpListVec = new Vector<>(wpList.size());
        wpList.forEach(
            wp -> wpListVec.add(fastPositionTransformationProvider.cheapToLocalRelHeights(wp.getPosition())));

        hardwareConfiguration = flightplan.getHardwareConfiguration();
        photoSettings = flightplan.getPhotoSettings();
        refPoint = flightplan.getRefPoint();
    }

    public void setWaypointSpeedSettings() {
        FlightplanSpeedModes speedMode = photoSettings.getMaxGroundSpeedAutomatic();

        // iterate over entire point set, adjustin speed and pause modes
        double maxSpeed = photoSettings.getMaxGroundSpeedMPSec();
        double minTimeInterval = photoSettings.getMinTimeInterval();
        boolean stopAtPoints = photoSettings.isStoppingAtWaypoints();

        // adding origin´s elevation here did not change anything TODO why ?
        Position pos = Position.fromDegrees(refPoint.getLat(), refPoint.getLon(), refPoint.getAltInMAboveFPRefPoint());
        Vec4 previousVec = fastPositionTransformationProvider.cheapToLocalRelHeights(pos);
        Orientation previousOrientation = new Orientation();
        double lastSpeed = photoSettings.getMaxGroundSpeedMPSec();

        double maxRoationSpeedDegPerSec =
            hardwareConfiguration
                .getPlatformDescription()
                .getAngularSpeed()
                .convertTo(Unit.DEGREE_PER_SECOND)
                .getValue()
                .doubleValue();
        double cameraNotRotateDelaySec =
            hardwareConfiguration
                .getPrimaryPayload(IGenericCameraConfiguration.class)
                .getDescription()
                .getCameraDelay()
                .convertTo(Unit.SECOND)
                .getValue()
                .doubleValue();

        for (int i = 0; i < wpList.size(); i++) {
            Waypoint nextWp = wpList.elementAt(i);
            Vec4 nextVec = wpListVec.get(i);

            // System.out.println(i + " -> dVec:" + dVec);
            if (speedMode == FlightplanSpeedModes.AUTOMATIC_DYNAMIC) {
                if (nextWp.getStopHereTimeCopter() == 0 && nextWp.isTriggerImageHereCopterMode()) {
                    // make sure that the remaining time to the next trigger is sufficient to rotate the drone
                    // in the time while roation is NOT forbidden
                    Orientation nextOrientation = nextWp.getOrientation();
                    double dOrientationDeg = nextOrientation.substract(previousOrientation).getMaxAbsDegrees();
                    double minTimeToNextImageDueRoation =
                        dOrientationDeg / maxRoationSpeedDegPerSec + cameraNotRotateDelaySec;

                    // make sure the "time between triggers" is sufficient
                    Vec4 dVec = nextVec.subtract3(previousVec);
                    double dist = dVec.getLength3();

                    // encode both in maximal flight speed
                    double speed =
                        Math.min(
                            dist / Math.max(minTimeInterval, minTimeToNextImageDueRoation), nextWp.getSpeedMpSec());
                    if (speed > maxSpeed) {
                        speed = maxSpeed;
                    }

                    if (speed < MIN_FALCON_SPEED) {
                        // go to stopping mode
                        speed = lastSpeed;
                        // TODO.. in this case we dont have to wait sooo long until image is stored, we could
                        // wait
                        // shorter?
                        nextWp.setStopHereTimeCopter(minTimeInterval * 1000);
                        // System.out.println("STOPPING");
                    }
                    if (!nextWp.getSpeedMode().equals(SpeedMode.fast)) {
                        nextWp.setSpeedMpSec(speed);
                    }
                    lastSpeed = speed;
                    previousOrientation = nextOrientation;
                }
            } else if (speedMode == FlightplanSpeedModes.MANUAL_CONSTANT) {
                if (!nextWp.getSpeedMode().equals(SpeedMode.fast)) {
                    nextWp.setSpeedMpSec(maxSpeed);
                }
            }

            previousVec = nextVec;
        }

        // fix speed of first waypoint, limit it by speed of second point
        if (speedMode == FlightplanSpeedModes.AUTOMATIC_DYNAMIC && wpList.size() > 1) {
            Waypoint firstWp = wpList.elementAt(0);
            Waypoint secondWp = wpList.elementAt(1);
            if (!firstWp.getSpeedMode().equals(SpeedMode.fast)) {
                firstWp.setSpeedMpSec(Math.min(firstWp.getSpeedMpSec(), secondWp.getSpeedMpSec()));
                firstWp.setStopHereTimeCopter(
                    Math.max(firstWp.getStopHereTimeCopter(), secondWp.getStopHereTimeCopter()));
            }
        }
    }

    public void addPhantomWaypointsLitchi() {
        // adding phantom waypoints to get correct orientation of 1st litchi image of csv
        CollectsTypeVisitor<Waypoint> visCollect = new CollectsTypeVisitor<>(Waypoint.class);
        visCollect.startVisit(flightplan);
        wpList = visCollect.matches;
        if (wpList.size() > 0) {
            IPlatformDescription platformDesc =
                photoSettings.getFlightplan().getHardwareConfiguration().getPlatformDescription();
            int LITCHI_WAYPOINTS =
                photoSettings
                    .getFlightplan()
                    .getHardwareConfiguration()
                    .getPlatformDescription()
                    .getMaxNumberOfWaypoints();
            boolean LITCHI_TRIGGER_IMAGE = false;
            int LITCHI_STOP_HERE_TIME = 0;

            double minDistance =
                platformDesc.getPhantomWaypointsLitchiDistance().convertTo(Unit.METER).getValue().doubleValue();
            double minGroundDistance =
                platformDesc.getMinGroundDistance().convertTo(Unit.METER).getValue().doubleValue();
            final Globe globe = StaticInjector.getInstance(IWWGlobes.class).getDefaultGlobe();
            double minWpSeparation =
                platformDesc.getMinWaypointSeparation().convertTo(Unit.METER).getValue().doubleValue();
            double maxWpSeparation =
                platformDesc.getMaxWaypointSeparation().convertTo(Unit.METER).getValue().doubleValue();
            Double refPointAltWgs84WithElevation = 0.;

            IFlightplanContainer fp = wpList.get(0).getParent().getParent();

            if (fp instanceof Flightplan) {
                refPointAltWgs84WithElevation = ((Flightplan)fp).getRefPointAltWgs84WithElevation();
            }

            int max = wpList.size();
            try {
                if (platformDesc.isInsertPhantomWaypointsLitchi()) {
                    for (int i = 0; i < wpList.size(); i += (LITCHI_WAYPOINTS - 1)) {
                        Waypoint c_Wpt = wpList.get(i);
                        double changedDistance = minDistance;

                        // TODO additional possible to use e.g. savety waypoints
                        // if (!c_Wpt.isTriggerImageHereCopterMode()) wpList.get(i).setTriggerImageHereCopterMode(true);
                        // //TODO far away enough from 2nd waypoint?

                        double minAltitudeAboveR = 0;
                        if (c_Wpt.getParent() instanceof PicArea) {
                            MinMaxPair a =
                                ((PicArea)c_Wpt.getParent()).getRestrictionIntervalInFpHeights(c_Wpt.getPosition(), 0);
                            if (a != null) {
                                minAltitudeAboveR = ((PicArea)c_Wpt.getParent()).getMinGroundDistance();
                            }
                        }

                        changedDistance =
                            getCorrectedChangedDistance(
                                minGroundDistance,
                                minAltitudeAboveR,
                                refPointAltWgs84WithElevation,
                                wpList.get(i),
                                changedDistance);

                        Position first =
                            new Position(
                                wpList.get(i).getPosition().latitude,
                                wpList.get(i).getPosition().longitude,
                                wpList.get(i).getPosition().elevation - changedDistance);

                        first =
                            getCorrectedPosition(
                                minGroundDistance,
                                minAltitudeAboveR,
                                refPointAltWgs84WithElevation,
                                globe,
                                minWpSeparation,
                                maxWpSeparation,
                                i,
                                changedDistance,
                                first);

                        insertFirstPhantomPoint(
                            c_Wpt,
                            c_Wpt.getOrientation(),
                            c_Wpt.getSpeedMpSec(),
                            first,
                            LITCHI_TRIGGER_IMAGE,
                            LITCHI_STOP_HERE_TIME);

                        max++;
                    }
                }
            } catch (Exception e) {
                Debug.getLog().log(Level.WARNING, "cant add phantom waypoints", e);
            }
        }
    }

    private double getCorrectedChangedDistance(
            double minGroundDistance,
            double minAltitudeAboveR,
            Double refPointAltWgs84WithElevation,
            Waypoint wp,
            double changedDistance) {
        // TODO add Min/Max Distance from object (at the moment only checked in general)

        Position position = wp.getPosition();
        double positionElevation = refPointAltWgs84WithElevation + position.elevation;
        double ground = positionElevation - elevationModel.getElevationAsGoodAsPossible(position);

        if (ground - changedDistance >= minGroundDistance) {
            if (position.elevation - changedDistance >= minAltitudeAboveR || minAltitudeAboveR <= 0) {
                return changedDistance;
            } else {
                if (position.elevation + changedDistance >= minAltitudeAboveR) {
                    if (ground + changedDistance >= minGroundDistance) {
                        return inverseChangedDistance(minAltitudeAboveR, changedDistance, ground);
                    } else {
                        // TODO issue, elevations not ok
                        // System.out.println( "CHANGE 6  " + ground + "/" + position.elevation + " - " + (ground -
                        // minGroundDistance) + "(" + minAltitudeAboveR + ")");
                        return inverseChangedDistance(minAltitudeAboveR, changedDistance, ground);
                    }
                } else {
                    if (ground - (position.elevation - minAltitudeAboveR) >= minGroundDistance) {
                        return position.elevation - minAltitudeAboveR;
                    } else {
                        // TODO issue, elevations not ok
                        // System.out.println( "CHANGE 5  " + ground + "/" + position.elevation + " - " + (ground -
                        // minGroundDistance) + "(" + minAltitudeAboveR + ")");
                        return position.elevation - minAltitudeAboveR;
                    }
                }
            }
        } else {
            if (ground + changedDistance >= minGroundDistance) {
                if (position.elevation + changedDistance >= minAltitudeAboveR || minAltitudeAboveR <= 0) {
                    return inverseChangedDistance(minAltitudeAboveR, changedDistance, ground);
                } else {
                    // TODO issue, elevations not ok
                    // System.out.println("CHANGE 4  " + ground + "/" + position.elevation + " - " + (ground -
                    // minGroundDistance) + "(" + minAltitudeAboveR + ")");
                    return inverseChangedDistance(minAltitudeAboveR, changedDistance, ground);
                }
            } else {
                if (position.elevation - (ground - minGroundDistance) >= minAltitudeAboveR || minAltitudeAboveR <= 0) {
                    return ground - minGroundDistance;
                } else {
                    // TODO issue, elevations not ok
                    // System.out.println("CHANGE 3  " + ground + "/" + position.elevation + " - " + (ground -
                    // minGroundDistance) + "(" + minAltitudeAboveR + ")");
                    return ground - minGroundDistance;
                }
            }
        }
    }

    private double inverseChangedDistance(double minAltitudeAboveR, double changedDistance, double ground) {
        if (changedDistance <= 0) {
            // already tried to inverse distance
            // System.out.println("CHANGE 2  " + ground + " +- " + (changedDistance) + "(" + minAltitudeAboveR + ")");
            return changedDistance;
        } else {
            return -changedDistance;
        }
    }

    private Position getCorrectedPosition(
            double minGroundDistance,
            double minAltitudeAboveR,
            Double refPointAltWgs84WithElevation,
            Globe globe,
            double minWpSeparation,
            double maxWpSeparation,
            int i,
            double changedDistance,
            Position first) {
        if (i > 0) {
            Position previous = wpList.get(i - 1).getPosition();
            Vec4 vPrevious = globe.computePointFromPosition(wpList.get(i - 1).getPosition());
            Vec4 v = globe.computePointFromPosition(first);
            double distance = vPrevious.distanceTo3(v);
            boolean changed = false;
            Position position = wpList.get(i).getPosition();
            while (minWpSeparation > 0 && distance < minWpSeparation) {
                if (!changed && changedDistance > 0) {
                    changedDistance = -changedDistance; // first try opposite direction
                } else {
                    if (changedDistance >= 0) {
                        changedDistance += .1;
                    } else {
                        changedDistance -= .1;
                    }
                }

                changed = true;

                changedDistance =
                    getCorrectedChangedDistance(
                        minGroundDistance,
                        minAltitudeAboveR,
                        refPointAltWgs84WithElevation,
                        wpList.get(i),
                        changedDistance);

                first = new Position(position.latitude, position.longitude, position.elevation - changedDistance);

                v = globe.computePointFromPosition(first);
                distance = vPrevious.distanceTo3(v);
            }
            // if ((maxWpSeparation > 0 && distance > maxWpSeparation)) {} // ANYWAY BROKEN
        }

        return first;
    }

    public void addPhantomWaypoints() {
        // adding phantom waypoints to smooth turns and get correct orientation of 1st litchi image
        double a_desired_turn = 3; // m/s^2 desired acceleration for parabolic turns (set at 3m/s2 at the moment, to be
        // confirmed)
        double security_offset_gen_traj = 0.5; // m (to avoid problem later during trajectory generation).
        double security_factor_misalignement_lines = 0.9; // to cope with possible misalignement between lines
        int max = wpList.size();
        try {
            if (photoSettings
                    .getFlightplan()
                    .getHardwareConfiguration()
                    .getPlatformDescription()
                    .getInsertPhantomWaypoints()) {
                // adding phantom waypoints to smooth turns
                if (max > 3) {
                    Waypoint previous_Wpt;
                    Waypoint current_Wpt = wpList.get(0);
                    Waypoint next_Wpt = wpList.get(1);
                    Waypoint next_next_Wpt = wpList.get(2);
                    Vec4 previous_Wpt_vec;
                    Vec4 current_Wpt_vec = wpListVec.get(0);
                    Vec4 next_Wpt_vec = wpListVec.get(1);
                    Vec4 next_next_Wpt_vec = wpListVec.get(2);
                    max -= 2;
                    for (int current_wpt_id = 1; current_wpt_id < max; current_wpt_id++) {
                        previous_Wpt = current_Wpt;
                        current_Wpt = next_Wpt;
                        next_Wpt = next_next_Wpt;
                        next_next_Wpt = wpList.get(current_wpt_id + 2);

                        previous_Wpt_vec = current_Wpt_vec;
                        current_Wpt_vec = next_Wpt_vec;
                        next_Wpt_vec = next_next_Wpt_vec;
                        next_next_Wpt_vec = wpListVec.get(current_wpt_id + 2);

                        // End of line condition & make sure that this and next are real waypoints!
                        if (!next_Wpt.isBeginFlightline()
                                || !current_Wpt.isTriggerImageHereCopterMode()
                                || !next_Wpt.isTriggerImageHereCopterMode()) {
                            continue;
                        }

                        // ignore if this is the start of an new AOI
                        if (next_Wpt.getParent() != current_Wpt.getParent()) {
                            continue;
                        }

                        // System.out.println("begin of line: " + current_wpt_id);

                        // If last wpt of line requires a stop, do not generate turns
                        if (current_Wpt.getStopHereTimeCopter() > 0 || next_Wpt.getStopHereTimeCopter() > 0) {
                            continue;
                        }

                        // System.out.println("non stopping: " + current_wpt_id);

                        // Generate phantom wpt for turn
                        // Unit turn vectors, distances and factor k
                        Vec4 vect_in = current_Wpt_vec.subtract3(previous_Wpt_vec).normalize3();
                        Vec4 vect_out = next_next_Wpt_vec.subtract3(next_Wpt_vec).normalize3();

                        // Turn vector with signed offset
                        Vec4 vect_turn = next_Wpt_vec.subtract3(current_Wpt_vec);
                        double d_offset = vect_turn.getLength3();
                        vect_turn = vect_turn.normalize3();

                        // Factor k, turn distances
                        double v_desired_in = current_Wpt.getSpeedMpSec();
                        if (v_desired_in < 0) {
                            v_desired_in = photoSettings.getMaxGroundSpeedMPSec();
                        }

                        double k_in = vect_turn.subtract3(vect_in).getLength3() / 2;

                        double d_turn_in = v_desired_in * v_desired_in * k_in / a_desired_turn;
                        double v_desired_out = current_Wpt.getSpeedMpSec();
                        if (v_desired_out < 0) {
                            v_desired_out = photoSettings.getMaxGroundSpeedMPSec();
                        }

                        double k_out = vect_out.subtract3(vect_turn).getLength3() / 2;
                        double d_turn_out = v_desired_out * v_desired_out * k_out / a_desired_turn;

                        // The distance between the lines is long enough
                        if (security_factor_misalignement_lines * d_offset > d_turn_in + d_turn_out) {
                            // Generate turn wpt 1, we add a 50 cm offset to avoid problem at the moment
                            // of generation
                            Vec4 turn_Wpt1_vec =
                                current_Wpt_vec.add3(
                                    vect_in.multiply3(Math.max(d_turn_in, d_turn_out) + security_offset_gen_traj));
                            Position p1 = fastPositionTransformationProvider.cheapToGlobalRelHeights(turn_Wpt1_vec);

                            Vec4 turn_Wpt2_vec =
                                next_Wpt_vec.subtract3(
                                    vect_out.multiply3(Math.max(d_turn_in, d_turn_out) + security_offset_gen_traj));
                            Position p2 = fastPositionTransformationProvider.cheapToGlobalRelHeights(turn_Wpt2_vec);

                            if (validate(current_Wpt.getParent(), p1) && validate(current_Wpt.getParent(), p2)) {
                                // do point number two before 1 in order to get them inserted in the right
                                // places
                                // Phantom wpt 2
                                insertPhantomPoint(current_Wpt, next_Wpt.getOrientation(), v_desired_out, p2);

                                // Phantom wpt 1
                                insertPhantomPoint(current_Wpt, current_Wpt.getOrientation(), v_desired_in, p1);
                            } else {

                                // Force stop at end of line and beginning of next one.. but without waiting for
                                // the image triggering & storing
                                current_Wpt.setStopHereTimeCopter(1);
                                next_Wpt.setStopHereTimeCopter(1);
                            }
                        } else {
                            // We have to reduce speed in order to handle turn
                            // New speed computation
                            // Both speeds are reduced equivalently
                            double alpha_v = v_desired_out / v_desired_in;
                            double v_desired_in_new =
                                Math.sqrt(
                                    security_factor_misalignement_lines
                                        * d_offset
                                        / (k_in + alpha_v * alpha_v * k_out));
                            double v_desired_out_new = v_desired_in_new * alpha_v;
                            // New turn distances
                            double d_turn_in_new = v_desired_in_new * v_desired_in_new * k_in / a_desired_turn;
                            double d_turn_out_new = v_desired_out_new * v_desired_out_new * k_out / a_desired_turn;
                            // Deceleration distance
                            double d_dec =
                                (v_desired_in * v_desired_in - v_desired_in_new * v_desired_in_new)
                                    / 2
                                    / a_desired_turn;

                            // Generate turn wpt 1, we add a 50 cm offset to avoid problem at the moment
                            // of generation
                            Vec4 turn_Wpt1_vec =
                                current_Wpt_vec.add3(
                                    vect_in.multiply3(
                                        Math.max(d_turn_in_new, d_turn_out_new) + d_dec + security_offset_gen_traj));
                            Position p1 = fastPositionTransformationProvider.cheapToGlobalRelHeights(turn_Wpt1_vec);

                            Vec4 turn_Wpt2_vec =
                                next_Wpt_vec.subtract3(
                                    vect_out.multiply3(
                                        Math.max(d_turn_in_new, d_turn_out_new) + d_dec + security_offset_gen_traj));
                            Position p2 = fastPositionTransformationProvider.cheapToGlobalRelHeights(turn_Wpt2_vec);

                            if (k_in > 0
                                    && k_out > 0
                                    && validate(current_Wpt.getParent(), p1)
                                    && validate(current_Wpt.getParent(), p2)) {
                                // do point number two before 1 in order to get them inserted in the right
                                // places
                                // Phantom wpt 2
                                insertPhantomPoint(current_Wpt, next_Wpt.getOrientation(), v_desired_out_new, p2);

                                // Phantom wpt 1
                                insertPhantomPoint(current_Wpt, current_Wpt.getOrientation(), v_desired_in_new, p1);
                            } else {

                                // Force stop at end of line and beginning of next one.. but without waiting for
                                // the
                                // image triggering & storing
                                current_Wpt.setStopHereTimeCopter(1);
                                next_Wpt.setStopHereTimeCopter(1);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Debug.getLog().log(Level.WARNING, "cant add phantom waypoints", e);
        }
    }

    private void insertPhantomPoint(Waypoint wpAfter, Orientation orientation, double speed, Position position)
            throws FlightplanContainerWrongAddingException, FlightplanContainerFullException {
        Waypoint turn_Wpt = wpAfter.getCopy();
        turn_Wpt.setBody("Phantom");
        turn_Wpt.setTriggerImageHereCopterMode(false);
        turn_Wpt.setStopHereTimeCopter(0);
        turn_Wpt.setLatLon(position.getLatitude().degrees, position.getLongitude().degrees);
        turn_Wpt.setAltInMAboveFPRefPoint(position.elevation);
        turn_Wpt.setSpeedMpSec(speed);
        // Heading towards next waypoint..
        // turn_Wpt.getOrientation() since this is allready cloned from the current_WP (its
        // precessor, we dont have to fix this here)
        // System.out.println("inserted phantom: " + speed);
        turn_Wpt.setOrientation(orientation);
        wpAfter.getParent().addAfterToFlightplanContainer(wpAfter, turn_Wpt);
    }

    private void insertFirstPhantomPoint(
            Waypoint addBeforeThisOne,
            Orientation orientation,
            double speed,
            Position position,
            boolean triggerImage,
            int stopHereTime)
            throws FlightplanContainerWrongAddingException, FlightplanContainerFullException {
        Waypoint turn_Wpt = addBeforeThisOne.getCopy();
        turn_Wpt.setBody(DJI_PHANTOM_WAYPOINT_BODY);
        turn_Wpt.setTriggerImageHereCopterMode(triggerImage);
        turn_Wpt.setStopHereTimeCopter(stopHereTime);
        turn_Wpt.setLatLon(position.getLatitude().degrees, position.getLongitude().degrees);
        turn_Wpt.setAltInMAboveFPRefPoint(position.elevation);
        turn_Wpt.setSpeedMpSec(speed);
        // Heading towards next waypoint..
        // turn_Wpt.getOrientation() since this is allready cloned from the current_WP (its
        // precessor, we dont have to fix this here)
        // System.out.println("inserted phantom: " + speed);
        turn_Wpt.setOrientation(orientation);
        addBeforeThisOne.getParent().addBeforeToFlightplanContainer(addBeforeThisOne, turn_Wpt);
    }

    /**
     * Remove any waypoints that are separated by less than Platformdescription->MinWaypointSeparation (workaround for
     * DJI limitation).
     */
    public void removeCloseWaypoints() {
        IPlatformDescription platformDesc = hardwareConfiguration.getPlatformDescription();
        double minWpSeparation = platformDesc.getMinWaypointSeparation().convertTo(Unit.METER).getValue().doubleValue();

        if (minWpSeparation == 0) {
            return;
        }

        Vec4 v = null;
        Vec4 vPrevious = null;
        int del = 0;
        for (var wp : wpList) {
            if (wp.getParent() instanceof PicArea) {
                if (((PicArea)wp.getParent()).getPlanType() == PlanType.INSPECTION_POINTS) continue;
            }

            v = fastPositionTransformationProvider.cheapToLocalRelHeights(wp.getPosition());
            if (vPrevious != null) {
                double distance = vPrevious.distanceTo3(v);

                if (distance < minWpSeparation) {
                    // waypoints are too close, remove.
                    wp.getParent().removeFromFlightplanContainer(wp);
                    del++;
                    continue;
                }
            }

            vPrevious = v;
        }

        if (del > 0) {
            Debug.getLog()
                .log(
                    Level.WARNING,
                    "Removed "
                        + del
                        + " waypoints because they are too near to each other (distance < "
                        + minWpSeparation
                        + ")");
        }
    }

    /**
     * Enforce a valid payload pitch at all waypoints by turning around yaw by 180° or clipping to minPitch/maxPitch
     * limits. (workaround for drones that have limited gimbal pitch range).
     */
    public void enforcePitchRange() {
        IPlatformDescription platformDesc = hardwareConfiguration.getPlatformDescription();

        IPayloadDescription payloadDescription = hardwareConfiguration.getPrimaryPayload().getDescription();
        double minPitch = payloadDescription.getMinPitch().convertTo(Unit.DEGREE).getValue().doubleValue();
        double maxPitch = payloadDescription.getMaxPitch().convertTo(Unit.DEGREE).getValue().doubleValue();

        for (var wp : wpList) {
            var orientation = wp.getOrientation();
            // 0 pitch: horizontal, -90: looking down.
            double pitch = normalizedDegrees(orientation.getPitch() - 90);
            double pitchTolerance = 2; // degrees tolerance.

            if (pitch + pitchTolerance < minPitch || pitch - pitchTolerance > maxPitch) {
                // try turning around yaw by 180 degrees and flip pitch around vertical axis so gimbal points forward.
                var newPitch = normalizedDegrees(90 - normalizedDegrees(pitch - 90));

                if (newPitch + pitchTolerance >= minPitch && newPitch - pitchTolerance <= maxPitch) {
                    wp.setCamPitch(newPitch + 90);
                    var newYaw = normalizedDegrees(orientation.getYaw() + 180.0);
                    var newRoll = -orientation.getRoll();
                    wp.setCamYaw(newYaw);
                    wp.setCamRoll(newRoll);
                    pitch = newPitch;
                }
            }

            // clamp to limits if necessary
            if (pitch < minPitch) {
                wp.setCamPitch(minPitch + 90);
            } else if (pitch > maxPitch) {
                wp.setCamPitch(maxPitch + 90);
            }
        }
    }

    private boolean validate(IFlightplanContainer container, Position p) {
        double ground = elevationModel.getElevationAsGoodAsPossible(p);
        double distToGround = p.elevation + flightplan.getRefPointAltWgs84WithElevation() - ground;
        // TODO IMPROVE THIS
        // System.out.println("validate: " + p +" -> "+ distToGround);
        if (hardwareConfiguration
                    .getPlatformDescription()
                    .getMinGroundDistance()
                    .convertTo(Unit.METER)
                    .getValue()
                    .doubleValue()
                > distToGround) {
            return false;
        }

        if (container instanceof PicArea) {
            PicArea picArea = (PicArea)container;
            if (picArea.getCropHeightMin() > distToGround) {
                return false;
            }
        }

        // System.out.println("OK");
        return true;
    }
}
