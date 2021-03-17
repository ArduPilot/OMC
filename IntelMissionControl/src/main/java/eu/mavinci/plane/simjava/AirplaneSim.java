/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.plane.simjava;

import com.intel.missioncontrol.StaticInjector;
import com.intel.missioncontrol.common.IPathProvider;
import com.intel.missioncontrol.hardware.IHardwareConfiguration;
import com.intel.missioncontrol.hardware.IPlatformDescription;
import com.intel.missioncontrol.map.elevation.IEgmModel;
import com.intel.missioncontrol.map.elevation.IElevationModel;
import com.intel.missioncontrol.map.worldwind.IWWGlobes;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.utils.IVersionProvider;
import eu.mavinci.core.flightplan.AltAssertModes;
import eu.mavinci.core.flightplan.CFlightplan;
import eu.mavinci.core.flightplan.CPicAreaCorners;
import eu.mavinci.core.flightplan.CWaypointLoop;
import eu.mavinci.core.flightplan.GPSFixType;
import eu.mavinci.core.flightplan.IFlightplanContainer;
import eu.mavinci.core.flightplan.IFlightplanLatLonReferenced;
import eu.mavinci.core.flightplan.IFlightplanPositionReferenced;
import eu.mavinci.core.flightplan.IFlightplanRelatedObject;
import eu.mavinci.core.flightplan.IReentryPoint;
import eu.mavinci.core.flightplan.PhotoLogLineType;
import eu.mavinci.core.flightplan.visitors.AFlightplanVisitor;
import eu.mavinci.core.helper.MinMaxPair;
import eu.mavinci.core.licence.ILicenceManager;
import eu.mavinci.core.plane.AirplaneConnectorState;
import eu.mavinci.core.plane.AirplaneFlightmode;
import eu.mavinci.core.plane.AirplaneFlightphase;
import eu.mavinci.core.plane.IAirplaneConnector;
import eu.mavinci.core.plane.PlaneConstants;
import eu.mavinci.core.plane.listeners.IAirplaneListenerDelegator;
import eu.mavinci.core.plane.sendableobjects.AndroidState;
import eu.mavinci.core.plane.sendableobjects.Config_variables;
import eu.mavinci.core.plane.sendableobjects.DebugData;
import eu.mavinci.core.plane.sendableobjects.HealthData;
import eu.mavinci.core.plane.sendableobjects.MVector;
import eu.mavinci.core.plane.sendableobjects.PhotoData;
import eu.mavinci.core.plane.sendableobjects.PlaneInfo;
import eu.mavinci.core.plane.sendableobjects.PositionOrientationData;
import eu.mavinci.core.plane.sendableobjects.SimulationSettings;
import eu.mavinci.core.plane.sendableobjects.SingleHealthDescription;
import eu.mavinci.core.plane.tcp.AAirplaneConnector;
import eu.mavinci.desktop.helper.MFile;
import eu.mavinci.desktop.helper.MathHelper;
import eu.mavinci.desktop.main.debug.Debug;
import eu.mavinci.flightplan.Flightplan;
import eu.mavinci.flightplan.LandingPoint;
import eu.mavinci.flightplan.Photo;
import eu.mavinci.flightplan.Point;
import eu.mavinci.flightplan.ReferencePoint;
import eu.mavinci.flightplan.Waypoint;
import eu.mavinci.flightplan.WaypointLoop;
import eu.mavinci.plane.IAirplane;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Line;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Earth;
import gov.nasa.worldwind.globes.Globe;
import java.io.File;
import java.util.Stack;
import java.util.Vector;
import java.util.logging.Level;
import org.asyncfx.concurrent.Dispatcher;

public class AirplaneSim extends AAirplaneConnector implements IAirplaneConnector, Runnable {

    public static final int AIRBORNE_HEIGHT = 20;
    IAirplane plane;
    public static int nextSimNo = 0;

    int simNo;

    PlaneInfo planeInfo = new PlaneInfo();
    Config_variables config;
    AirplaneFlightphase flightPhase = AirplaneFlightphase.ground;
    String name;
    SimulationSettings simSettings = new SimulationSettings();

    public double MAIN_LOOP_STEP = 0.10; // sec
    public double POSITION_ORIENTATION_SAMPLE = .3; // sec
    public double DEBUG_SAMPLE = 0.5; // sec
    public double HEALTH_SAMPLE = 1; // sec
    public double AIR_SPEED; // m/s

    private long sleepUebertrag = 0;

    public static final double MAX_SIM_SPEED = 16;
    public static final double MIN_SIM_SPEED = 1. / 16;

    public static final double CROSSTRACK_METERS_TO_YAWOFFSET_DEG = 1;

    public double WAYPOINT_REACHED_M;
    public double LAST_IMAGE_BEFORE_WAYPOINT_M;

    Thread workerThread;

    boolean running = true;
    boolean runAsync;

    public boolean sendHealth = true;
    public boolean sendPositionOrientation = true;
    public boolean sendPositionGPSAlt = true;
    public boolean sendDebug = true;
    public boolean sendPhoto = true;

    double simSpeed = 4;
    double battery = 99;
    LatLon startPos = LatLon.fromDegrees(50, 10);

    Flightplan fp;
    boolean fpIsEmpty;

    public IFlightplanRelatedObject fpCurObj;
    int reentyPointID = 0;

    public Position pos;
    public Vec4 vec;
    public Vec4 vecStart;

    Angle yaw = Angle.ZERO;
    double roll = 0;
    double pitch = 0;
    double pitchRate = 0;
    double rollRate = 0;
    double yawRate = 0;
    double pitchCam = 0;
    double rollCam = 0;

    public double simTime;

    boolean engineOn;
    boolean photoOn;
    double photoDistance;
    double photoDistanceMax;

    public double flightDistance;

    int noLastImage;

    double nextPosOrSample;
    double nextDebugSample;
    double nextHEALTHsample;

    boolean shouldTakeImage;
    AltAssertModes assureAlt;

    Position lastTargetPos;
    Position lastPhotoPos;

    Vec4 lastTargetVec;
    Vec4 lastPhotoVec;

    double lastPhotoTime;
    int lastPhotoReentryTriggerID;
    int photo_roll_sign;
    boolean hasDoneLastBeforeCorner;
    Position curTargetPos;
    Vec4 curTargetVec;

    Vec4 lastTargetVec2d;
    Vec4 curTargetVec2d;

    double crossTrackErr;
    public double simStartTime;
    boolean isInCopterMode;
    boolean isPlannedImagesTriggersMode;

    public synchronized void reset() {
        // System.out.println("call reset");

        reentyPointID = 0;
        pos = new Position(startPos, 0);
        yaw = Angle.ZERO;
        roll = 0;
        pitch = 0;
        pitchCam = 0;
        rollCam = 0;
        pitchRate = 0;
        rollRate = 0;
        yawRate = 0;
        flightDistance = 0;

        engineOn = false;
        photoOn = false;
        photoDistance = 0;
        photoDistanceMax = 0;
        flightPhase = AirplaneFlightphase.ground;

        noLastImage = -1;

        nextPosOrSample = -1;
        nextDebugSample = -1;
        nextHEALTHsample = -1;

        shouldTakeImage = false;
        assureAlt = AltAssertModes.unasserted;

        lastPhotoPos = Position.ZERO;
        lastPhotoVec = Vec4.INFINITY;
        lastPhotoTime = -1;
        lastPhotoReentryTriggerID = -1;
        photo_roll_sign = 0;
        hasDoneLastBeforeCorner = false;

        sleepUebertrag = 0;

        curTargetPos = lastTargetPos = pos;
        curTargetVec = lastTargetVec = vec;
        altMaxInM = altMinInM = pos.elevation;
        lastTargetVec2d = curTargetVec2d = vec == null ? null : new Vec4(vec.x, vec.y);
        simStartTime = simTime = System.currentTimeMillis() / 1000.;
    }

    public boolean isFPReady() {
        return fpCurObj instanceof LandingPoint;
    }

    private class ContainerStackElem {
        IFlightplanContainer cont;
        double enteringTime;
        int loopCounter;
        int curIndex;

        @Override
        public String toString() {
            return "cont="
                + cont
                + "  enteringTime="
                + enteringTime
                + " loopCounter="
                + loopCounter
                + " curIndex="
                + curIndex;
        }
    }

    Stack<ContainerStackElem> fpConts = new Stack<ContainerStackElem>();

    public static final double MAX_SIMULATION_TIME = 60 * 60 * 2; // in sec. no
    // flight
    // should
    // takes
    // longer

    Matrix transform4Inv;
    Matrix transform4;
    private static final Globe globe = StaticInjector.getInstance(IWWGlobes.class).getDefaultGlobe();
    private static final IElevationModel elevationModel = StaticInjector.getInstance(IElevationModel.class);
    private static final IEgmModel egmModel = StaticInjector.getInstance(IEgmModel.class);
    double diveStep;
    double climbStep;
    double maxSimTimeSec;

    double WP_REACHED_RADIUS; // m
    double WP_REACHED_ALT; // m
    double YAW_RATE_MAX; // grad/sec
    double PITCH_RATE_MAX; // grad/sec
    double ROLL_RATE_MAX; // grad/sec

    private void setDiveAndClimb(double airspeed) {
        double maxDiveAngleRad =
            nativeHardwareConfiguration
                .getPlatformDescription()
                .getMaxDiveAngle()
                .convertTo(Unit.RADIAN)
                .getValue()
                .doubleValue();
        double maxClimbAngleRad =
            nativeHardwareConfiguration
                .getPlatformDescription()
                .getMaxClimbAngle()
                .convertTo(Unit.RADIAN)
                .getValue()
                .doubleValue();
        diveStep = isInCopterMode ? airspeed * MAIN_LOOP_STEP : Math.sin(maxDiveAngleRad) * airspeed * MAIN_LOOP_STEP;
        climbStep = isInCopterMode ? airspeed * MAIN_LOOP_STEP : Math.sin(maxClimbAngleRad) * airspeed * MAIN_LOOP_STEP;
    }

    public void setNativeHardwareConfiguration(IHardwareConfiguration nativeHardwareConfiguration) {
        this.nativeHardwareConfiguration = nativeHardwareConfiguration;
    }

    public static final int MAX_SIM_TIME_OVER_BATTERY_TIME =
        20; // also change com.intel.missioncontrol.ui.planning.summary.WarningsView.warning

    private void initConstants() {
        // by intention NOT synchronized, but I have to assure manually that its only called once at a time
        // with synchrinizatiin this causes deadlocks
        IPlatformDescription platformDesc = nativeHardwareConfiguration.getPlatformDescription();
        isInCopterMode = platformDesc.isInCopterMode();
        maxSimTimeSec =
            simTime
                + platformDesc.getMaxFlightTime().convertTo(Unit.SECOND).getValue().doubleValue()
                    * MAX_SIM_TIME_OVER_BATTERY_TIME;
        // System.out.println("lastPhotoPos:"+ lastPhotoPos+ " pos:"+pos);

        double maxDiveAngle = platformDesc.getMaxDiveAngle().convertTo(Unit.RADIAN).getValue().doubleValue();
        double maxClimbAngle = platformDesc.getMaxClimbAngle().convertTo(Unit.RADIAN).getValue().doubleValue();
        // System.out.println("platformDesc:"+platformDesc.getId());

        AIR_SPEED = platformDesc.getMaxPlaneSpeed().convertTo(Unit.METER_PER_SECOND).getValue().doubleValue();

        WP_REACHED_RADIUS = isInCopterMode ? 1 : 35; // m
        WP_REACHED_ALT = isInCopterMode ? 1 : 10; // m
        YAW_RATE_MAX = isInCopterMode ? 45 : 30; // grad/sec
        PITCH_RATE_MAX = isInCopterMode ? 60 : 30; // grad/sec
        ROLL_RATE_MAX = isInCopterMode ? 60 : 30; // grad/sec
        // System.out.println("AIR_SPEED:"+AIR_SPEED);
        MAIN_LOOP_STEP = Math.min(0.10, Math.min(WP_REACHED_RADIUS, WP_REACHED_ALT) / AIR_SPEED * 0.5);
        // System.out.println("new MainLoop_step:"+MAIN_LOOP_STEP);

        diveStep = isInCopterMode ? AIR_SPEED * MAIN_LOOP_STEP : Math.sin(maxDiveAngle) * AIR_SPEED * MAIN_LOOP_STEP;
        climbStep = isInCopterMode ? AIR_SPEED * MAIN_LOOP_STEP : Math.sin(maxClimbAngle) * AIR_SPEED * MAIN_LOOP_STEP;

        setDiveAndClimb(AIR_SPEED);

        WAYPOINT_REACHED_M = 20 + 1.5 * AIR_SPEED; // 20m + 1.5s look ahead
        LAST_IMAGE_BEFORE_WAYPOINT_M = WAYPOINT_REACHED_M + 15;

        transform4Inv = globe.computeModelCoordinateOriginTransform(new Position(startPos, startElevOverR));
        transform4 = transform4Inv.getInverse();

        vec = globe.computePointFromPosition(pos).transformBy4(transform4);
        altMaxInM = altMinInM = pos.elevation;
        isPlannedImagesTriggersMode = platformDesc.planIndividualImagePositions();
    }

    int noCirclesCurrentTaget;
    int noCirclesDone;
    public double altMinInM;
    public double altMaxInM;

    @Override
    public void run() {
        try {
            // System.out.println("sim AirplaneSim start" + this);

            initConstants();
            synchronized (this) {
                flightDistance = 0;
                simStartTime = simTime = System.currentTimeMillis() / 1000.;
                sleepUebertrag = 0;
                lastPhotoTime = -1;
                lastPhotoReentryTriggerID = -1;
                photo_roll_sign = 0;
                hasDoneLastBeforeCorner = false;
            }

            assureInitFP();
            synchronized (this) {
                curTargetPos = lastTargetPos = pos;
                curTargetVec = lastTargetVec = vec;
                altMaxInM = altMinInM = pos.elevation;
                lastTargetVec2d = curTargetVec2d = vec == null ? null : new Vec4(vec.x, vec.y);
            }

            // this values are critical, since sometimes point will never be reached
            // otherwise!

            // reset();

            // return
            // globe.computePointFromLocation(latLon).transformBy4(transform4);

            // return
            // globe.computePositionFromPoint(vec.transformBy4(transform4Inv));

            invokeMaybeAsyc(
                new Runnable() {
                    @Override
                    public void run() {
                        rootHandler.recv_powerOn();
                    }
                });

            // if (runAsync) System.out.println("sim this fp:" + fp.toXML());

            boolean reached2Donce = false;

            int curLandingPhase = 0;

            double groundDist = 0;

            Line currentLine2d = null;
            Vec4 directCurLineNorm2d = null;
            double currentLine2dLen = 0;
            double radiusAssert = PlaneConstants.DEF_CONT_NAV_CIRCR / 100.;
            double radiusReached = PlaneConstants.DEF_CONT_NAV_CIRCR / 100.;

            Double relDirectionCircleStart = null;
            boolean yawCircleStartInsideCountingWindow = false;

            Waypoint copterImagePointLast = null;
            Waypoint copterImagePointNext = null;

            double currentSpeed = AIR_SPEED;
            setDiveAndClimb(currentSpeed);

            final double turnRadius =
                fp.getHardwareConfiguration()
                    .getPlatformDescription()
                    .getTurnRadius()
                    .convertTo(Unit.METER)
                    .getValue()
                    .doubleValue();

            // boolean landingPointReached = false;

            boolean simDone = false;

            long simUntil = -1;
            long cycleCount = 0;
            while (!simDone && running) {
                cycleCount++;
                /*if (simDone) {
                    System.out.println("fpCurObj:" + fpCurObj);
                    if (simUntil == -1) {
                        simUntil = cycleCount;
                    }

                    if (cycleCount > simUntil) {
                        return;
                    }
                }*/
                // synchronized (this) {
                long timeStartThisLoop = System.currentTimeMillis();
                // if (simTime - simStartTime > MAX_SIMULATION_TIME) throw new
                // RuntimeException("simulation exceeds maximal runtime: " +
                // (simTime-simStartTime) +" > " +MAX_SIMULATION_TIME);
                // System.out.println("sim AirplaneSim start" + this +" "+runAsync +" "+ fpCurObj);
                if (!runAsync) {
                    if (simTime >= maxSimTimeSec) {
                        simDone = true;
                    }
                }

                simTime += MAIN_LOOP_STEP;
                int sec = (int)simTime;
                int usec = (int)(1000000 * (simTime - sec));
                battery = (1 - (simTime - simStartTime) / (maxSimTimeSec - simStartTime)) * 100;

                boolean reachedAlt =
                    (assureAlt == AltAssertModes.jump || isInCopterMode)
                        ? Math.abs(vec.z - curTargetVec.z) <= WP_REACHED_ALT
                        : true;
                // System.out.println("\n\nreachedAlt:"+reachedAlt + " reached2Donce:"+reached2Donce);
                Waypoint copterImagePoint = null;

                // double distToTarget3D= vec.distanceTo3(curTargetVec);
                if (!reached2Donce) {
                    double distToTarget2D = vec.distanceTo2(curTargetVec);
                    double reachedRadius = WP_REACHED_RADIUS;
                    if (isInCopterMode) {
                        // fine
                    } else if (!reachedAlt && assureAlt == AltAssertModes.jump) {
                        reachedRadius += radiusAssert;
                    } else if (!reachedAlt && noCirclesCurrentTaget != 0) {
                        reachedRadius += radiusAssert;
                    } else if (reachedAlt && noCirclesCurrentTaget != 0) {
                        reachedRadius += radiusReached;
                    } else if (radiusAssert < 0) {
                        // used in spot landing sim
                        reachedRadius += radiusAssert;
                    }

                    reached2Donce = distToTarget2D <= reachedRadius;
                    // if (runAsync) System.out.println("reachedRadius: " + reachedRadius +
                    // "\tdistToTarget2D:"+distToTarget2D + " =>
                    // reached2Donce:"+reached2Donce);
                }

                boolean reachedCircleCount =
                    isInCopterMode || (noCirclesCurrentTaget != -1 && noCirclesCurrentTaget <= noCirclesDone);

                // jump to landing point if someone sends descending!
                if (flightPhase == AirplaneFlightphase.descending && !(fpCurObj instanceof LandingPoint)) {
                    reached2Donce = true;
                    reachedAlt = true;
                    reachedCircleCount = true;
                    // FIXME ... maybe jump first to safety altitude??? TODO
                }

                if (reachedCircleCount) {
                    relDirectionCircleStart = null;
                }
                // System.out.println("reachedCircleCount:"+reachedCircleCount + " noCirclesCurrentTaget:"+
                // noCirclesCurrentTaget+"
                // noCirclesDone:"+noCirclesDone);

                // System.out.println("mainLoopTime:"+ (simTime - simStartTime)+ " "+ fpCurObj + " pos="+pos +" target="
                // +curTargetPos +
                // "engineOn:"+engineOn);

                if (Double.isNaN(pos.latitude.degrees)) {
                    Debug.getLog().log(Level.WARNING, "NaN in LocalSim detected", new Exception());
                    return;
                }

                // navigation, determine next waypoint
                if (reached2Donce && reachedAlt && reachedCircleCount) {
                    reached2Donce = false;
                    lastTargetPos = curTargetPos;
                    lastTargetVec = curTargetVec;
                    lastTargetVec2d = curTargetVec2d;
                    currentLine2dLen = 0;
                    // System.out.println("reset noCirclesCurrentTaget");
                    noCirclesCurrentTaget = 0;

                    // if (runAsync) System.out.println("WP reched, figure out next WP of " + fpCurObj + "
                    // curLandingPhase:"+curLandingPhase);

                    if (fpCurObj instanceof Waypoint) {
                        Waypoint wp = (Waypoint)fpCurObj;
                        simTime += wp.getStopHereTimeCopter() / 1000.;
                        // TODO, if this number is larget then zero, we have to encounter for slowing down and
                        // accelerating

                        curLandingPhase = 0; // make sure this is resetted after a restart
                    } else {
                        curLandingPhase = 0; // make sure this is resetted after a restart
                    }

                    // determine next WP
                    while (!(fpCurObj instanceof LandingPoint) && curLandingPhase == 0) {
                        noCirclesDone = 0; // reset circle stuff
                        // if (runAsync) System.out.println("check successor of fpCurObj " + fpCurObj);
                        // scroll to next FP object
                        while (fpConts.size() > 0) {
                            ContainerStackElem parent = fpConts.peek();
                            // if (runAsync) System.out.println("curCont: " + parent.cont + " " +
                            // parent.cont.sizeOfFlightplanContainer());
                            if (parent.cont.sizeOfFlightplanContainer() > parent.curIndex + 1) {
                                // next child in current container
                                parent.curIndex++;
                                fpCurObj = parent.cont.getFromFlightplanContainer(parent.curIndex);
                                if (fpCurObj instanceof IFlightplanContainer
                                        && !(fpCurObj instanceof CPicAreaCorners)) {
                                    if (fpCurObj instanceof CWaypointLoop) {
                                        CWaypointLoop loop = (CWaypointLoop)fpCurObj;
                                        if (loop.isIgnore()) {
                                            continue;
                                        }
                                    }

                                    parent = new ContainerStackElem();
                                    parent.enteringTime = simTime;
                                    parent.loopCounter = 0;
                                    parent.curIndex = -1;
                                    parent.cont = (IFlightplanContainer)fpCurObj;
                                    fpConts.push(parent);
                                }

                                break;
                            } else if (parent.cont instanceof WaypointLoop) {
                                parent.loopCounter++;
                                WaypointLoop loop = (WaypointLoop)parent.cont;
                                if (loop.getCount() > 0 && loop.getCount() >= parent.loopCounter) {
                                    // this container ready
                                    fpConts.pop();
                                } else if (loop.getTime() > 0 && loop.getTime() + parent.enteringTime >= simTime) {
                                    // this container ready
                                    fpConts.pop();
                                } else {
                                    // back to start of the loop
                                    parent.curIndex = -1;
                                }
                            } else {
                                // this container ready
                                fpConts.pop();
                            }
                        }

                        if (fpConts.isEmpty() || flightPhase == AirplaneFlightphase.descending) {
                            if (!runAsync) {
                                simDone = true;
                            }

                            fpCurObj = fp.getLandingpoint();
                            // System.out.println("set landingP as target");
                        }

                        // if (runAsync) System.out.println("nextObj:" + fpCurObj);

                        if (fpCurObj instanceof Point && !(fpCurObj instanceof LandingPoint)) {
                            continue;
                        }

                        if (fpCurObj instanceof CPicAreaCorners) {
                            continue;
                        }

                        if (fpCurObj instanceof ReferencePoint) {
                            continue;
                        }

                        // search for next breakpoint in FP
                        if (fpCurObj instanceof Photo) {
                            Photo photo = (Photo)fpCurObj;
                            photoOn = photo.isPowerOn();
                            photoDistance = photo.getDistanceInCm();
                            photoDistanceMax = photo.getDistanceMaxInCm();
                            continue;
                        }

                        if (fpCurObj instanceof IReentryPoint) {
                            reentyPointID = ((IReentryPoint)fpCurObj).getId();
                        }

                        if (fpCurObj instanceof LandingPoint) {
                            if (flightPhase == AirplaneFlightphase.airborne && !isInCopterMode) {
                                setFlightPhase(AirplaneFlightphase.descending);
                            }

                            radiusReached = radiusAssert = turnRadius;

                            LandingPoint landP = (LandingPoint)fpCurObj;
                            if (isInCopterMode) {
                                // System.out.println("landP:" + landP + "  " + landP.getMode() + " "
                                // +landP.getPosition());
                                switch (landP.getMode()) {
                                case CUSTOM_LOCATION: // =0 //copters will use this for custom auto landing location
                                    noCirclesCurrentTaget = -1; // circeling some while
                                    assureAlt = AltAssertModes.unasserted;
                                    curTargetPos = landP.getPosition();
                                    break;

                                case LAND_AT_TAKEOFF: // =1 //copters will use this for auto landing on Same as actual
                                    // takeoff location
                                    noCirclesCurrentTaget = -1; // circeling some while
                                    assureAlt = AltAssertModes.unasserted;
                                    curTargetPos = new Position(startPos, landP.getAltInMAboveFPRefPoint());
                                    break;

                                default:
                                    Debug.getLog()
                                        .log(Level.CONFIG, "unsuported landing mode detected:" + landP.getMode());
                                    // fallthrough
                                case LAST_WAYPOINT: // =3 //copters will stay airborne on last waypoint, fixedwing
                                    // will go to startprocedure location==same as landing but stay
                                    // on alt
                                    noCirclesCurrentTaget = -1; // circeling some while
                                    assureAlt = AltAssertModes.unasserted;
                                    curTargetPos = pos;
                                }
                            }

                            curTargetVec = globe.computePointFromPosition(curTargetPos).transformBy4(transform4);
                            break;
                        }

                        if (fpCurObj instanceof IFlightplanPositionReferenced) {
                            IFlightplanPositionReferenced posRef = (IFlightplanPositionReferenced)fpCurObj;
                            curTargetPos =
                                Position.fromDegrees(
                                    posRef.getLat(), posRef.getLon(), posRef.getAltInMAboveFPRefPoint());
                            curTargetVec = globe.computePointFromPosition(curTargetPos).transformBy4(transform4);
                            // System.out.println("new target
                            // Pos:"+curTargetPos);

                            currentSpeed = AIR_SPEED;
                            if (posRef instanceof Waypoint) {
                                Waypoint wp = (Waypoint)posRef;
                                if (wp.isIgnore()) {
                                    continue;
                                }

                                if (wp.getSpeedMpSec() > 0) {
                                    currentSpeed = wp.getSpeedMpSec();
                                }

                                assureAlt = wp.getAssertAltitudeMode();
                                radiusAssert = turnRadius;
                                if (wp.isCirceling()) {
                                    radiusReached = wp.getRadiusWithinM();
                                    noCirclesCurrentTaget = 1; // more than one is done by outer loops
                                }
                            } else {
                                radiusAssert = radiusReached = turnRadius;
                                assureAlt = AltAssertModes.unasserted;
                            }

                            setDiveAndClimb(currentSpeed);

                            // System.out.println("new alt mode:"+assureAlt);
                            break;
                        }

                        if (fpCurObj instanceof IFlightplanLatLonReferenced) {
                            IFlightplanLatLonReferenced posRef = (IFlightplanLatLonReferenced)fpCurObj;
                            curTargetPos =
                                Position.fromDegrees(posRef.getLat(), posRef.getLon(), curTargetPos.elevation);

                            curTargetVec = globe.computePointFromPosition(curTargetPos).transformBy4(transform4);
                            assureAlt = AltAssertModes.unasserted;
                            break;
                        }
                    }

                    if (lastTargetVec.x != curTargetVec.x || lastTargetVec.y != curTargetVec.y) {
                        curTargetVec2d = new Vec4(curTargetVec.x, curTargetVec.y);
                        currentLine2d = Line.fromSegment(lastTargetVec2d, curTargetVec2d);
                        directCurLineNorm2d = currentLine2d.getDirection();
                        currentLine2dLen = directCurLineNorm2d.getLength3();
                        directCurLineNorm2d = directCurLineNorm2d.multiply3(1 / currentLine2dLen);
                    }
                }

                if (isPlannedImagesTriggersMode && copterImagePointNext != fpCurObj) {
                    if (copterImagePointNext != null && copterImagePointNext.isTriggerImageHereCopterMode()) {
                        copterImagePoint = copterImagePointNext;
                    }

                    copterImagePointLast = copterImagePointNext;
                    if (fpCurObj instanceof Waypoint) {
                        copterImagePointNext = (Waypoint)fpCurObj;
                    } else {
                        copterImagePointNext = null;
                    }
                }

                boolean isFlyingLine = false;
                Double percentLineDone = null;
                Angle yawSoll;
                double pitchCamSoll = 0;
                double rollCamSoll = 0;

                if (isInCopterMode) {
                    // flying always on line with infinite climb rate
                    Vec4 direction = curTargetVec.subtract3(vec);
                    Vec4 line = curTargetVec.subtract3(lastTargetVec);
                    // Angle yawSoll = LatLon.greatCircleAzimuth(pos,curTargetPos);
                    // System.out.println("direction: "+ direction);
                    percentLineDone =
                        MathHelper.intoRange(
                            1 - Math.sqrt(direction.getLengthSquared3() / line.getLengthSquared3()), 0, 1);
                    if (copterImagePointLast != null && copterImagePointNext != null) {
                        double dYaw =
                            copterImagePointNext.getOrientation().getYaw()
                                - copterImagePointLast.getOrientation().getYaw();
                        while (dYaw > 180) {
                            dYaw -= 360;
                        }

                        while (dYaw < -180) {
                            dYaw += 360;
                        }

                        yawSoll =
                            Angle.fromDegrees(copterImagePointLast.getOrientation().getYaw() + percentLineDone * dYaw);
                        pitchCamSoll = copterImagePointNext.getOrientation().getPitch();
                        rollCamSoll = copterImagePointNext.getOrientation().getRoll();
                    } else if (copterImagePointNext != null) {
                        yawSoll = Angle.fromDegrees(copterImagePointNext.getOrientation().getYaw());
                        pitchCamSoll = copterImagePointNext.getOrientation().getPitch();
                        rollCamSoll = copterImagePointNext.getOrientation().getRoll();
                    } else {
                        yawSoll = yaw;
                        pitchCamSoll = pitchCam;
                        rollCamSoll = rollCam;
                    }

                    crossTrackErr = 0;
                    isFlyingLine = true;
                } else if (!isInCopterMode
                        && reached2Donce
                        && reachedAlt
                        && curTargetVec != null
                        && !reachedCircleCount) {
                    // System.out.println("counting circle");

                    Vec4 vec2d = new Vec4(vec.x, vec.y);
                    Vec4 crossErr = vec2d.subtract3(curTargetVec);
                    Vec4 crossErr2d = new Vec4(crossErr.x, crossErr.y);
                    crossTrackErr = crossErr2d.getLength3() - radiusReached;
                    double relDirection = Math.toDegrees(Math.atan2(crossErr2d.x, crossErr2d.y));
                    // System.out.println("counted circ: " + noCirclesDone);
                    // do counted circles
                    if (relDirectionCircleStart == null) {
                        relDirectionCircleStart = relDirection;
                        noCirclesDone = 0;
                        yawCircleStartInsideCountingWindow = true;
                    }

                    double relDirectionNorm = relDirection - relDirectionCircleStart;
                    while (relDirectionNorm > 180) {
                        relDirectionNorm -= 360;
                    }

                    while (relDirectionNorm < -180) {
                        relDirectionNorm += 360;
                    }

                    // count current circle
                    if (relDirectionNorm >= 0 && relDirectionNorm <= YAW_RATE_MAX * MAIN_LOOP_STEP * 2) {
                        if (!yawCircleStartInsideCountingWindow) {
                            noCirclesDone++;
                            yawCircleStartInsideCountingWindow = true;
                            // System.out.println("yaw reached: " + vec.z + " " + vec.z );
                        }
                    } else {
                        relDirectionNorm += 180;
                        while (relDirectionNorm > 180) {
                            relDirectionNorm -= 360;
                        }

                        while (relDirectionNorm < -180) {
                            relDirectionNorm += 360;
                        }

                        if (relDirectionNorm >= 0 && relDirectionNorm <= YAW_RATE_MAX * MAIN_LOOP_STEP * 2) {
                            yawCircleStartInsideCountingWindow = false;
                        }
                    }

                    percentLineDone = 1d;
                    yawSoll = Angle.fromDegrees(relDirection + 90 + crossTrackErr * CROSSTRACK_METERS_TO_YAWOFFSET_DEG);
                } else if (reached2Donce && !reachedAlt && curTargetVec != null) {
                    // System.out.println("circle nav");
                    percentLineDone = 1d;
                    // double yawRate = Math.toDegrees(AIR_SPEED/radius);
                    // System.out.println();
                    // System.out.println("yawRate:"+yawRate);
                    // double yawAdd = yawRate * MAIN_LOOP_STEP;
                    // System.out.println("yawAdd:"+yawAdd);
                    Vec4 vec2d = new Vec4(vec.x, vec.y);
                    Vec4 crossErr = vec2d.subtract3(curTargetVec);
                    Vec4 crossErr2d = new Vec4(crossErr.x, crossErr.y);
                    crossTrackErr = crossErr2d.getLength3() - radiusAssert;
                    // System.out.println("crossErr:" + crossTrackErr + " " + crossErr2d.getLength3());
                    // System.out.println("perfect curren yaw : "+Math.toDegrees(Math.atan2(crossErr2d.x,
                    // crossErr2d.y)));
                    // System.out.println("actual yaw : "+yaw);
                    yawSoll =
                        Angle.fromDegrees(
                            Math.toDegrees(Math.atan2(crossErr2d.x, crossErr2d.y))
                                + 90
                                + crossTrackErr * CROSSTRACK_METERS_TO_YAWOFFSET_DEG);
                    // System.out.println("yawSoll:"+yawSoll);

                    // yawAdd += crossTrackErr*CROSSTRACK_METERS_TO_YAWOFFSET_DEG/3;
                    // yawSoll = yaw.addDegrees(MathHelper.intoRange(yawAdd, -yawRate/2, 2*yawRate));
                    // System.out.println("compensation:"+MathHelper.intoRange(crossTrackErr*CROSSTRACK_METERS_TO_YAWOFFSET_DEG, -5, +10));

                } else {
                    // System.out.println("line nav");
                    // determine yaw
                    Vec4 direction = curTargetVec.subtract3(vec);
                    // Angle yawSoll = LatLon.greatCircleAzimuth(pos,curTargetPos);
                    // System.out.println("direction: "+ direction);

                    // System.out.println();

                    yawSoll = Angle.fromRadians(Math.atan2(direction.x, direction.y));

                    if (currentLine2dLen > 0 && !reached2Donce) {
                        isFlyingLine = true;
                        Vec4 vec2d = new Vec4(vec.x, vec.y);

                        Vec4 offP2d = currentLine2d.nearestPointTo(vec2d);
                        Vec4 crossErr = vec2d.subtract3(offP2d);
                        Vec4 crossErr2d = new Vec4(crossErr.x, crossErr.y); // otherwise it has a wrong lenght!

                        // System.out.println("scalarPRof="+(crossErr2d.dot3(directCurLineNorm2d)));
                        crossTrackErr =
                            crossErr2d.getLength3()
                                * Math.signum(
                                    (directCurLineNorm2d.x * crossErr.y) - (directCurLineNorm2d.y * crossErr.x));

                        Vec4 dirOffP = offP2d.subtract3(lastTargetVec2d);
                        Vec4 dirOffP2d = new Vec4(dirOffP.x, dirOffP.y);

                        percentLineDone =
                            MathHelper.intoRange(dirOffP2d.dot3(directCurLineNorm2d) / currentLine2dLen, 0, 1);
                        // System.out.format("percentLineDone=%f\tcrossTrackErr=%f\tyawSoll=%f\n",percentLineDone,crossTrackErr,yawSoll.degrees);
                        // percentLineDone = MathHelper.intoRange(percentLineDone ,0,1);

                        // System.out.format("crossErr=%s\tdirNorm=%s\n",crossErr.toString(),directCurLineNorm2d.toString());

                        // double criteria = WP_REACHED_RADIUS/(currentLine2dLen*2);
                        // if (percentLineDone>criteria && percentLineDone <1-criteria){
                        yawSoll =
                            yawSoll.addDegrees(
                                MathHelper.intoRange(crossTrackErr * CROSSTRACK_METERS_TO_YAWOFFSET_DEG, -45, +45));
                        // }

                    }
                }
                // System.out.println( "direction: "+ direction + "
                // yaw;"+yawSoll);
                // limiting yawRate
                double yawSollStep = yawSoll.degrees - yaw.degrees;
                while (yawSollStep > 180) {
                    yawSollStep -= 360;
                }

                while (yawSollStep <= -180) {
                    yawSollStep += 360;
                }

                double yawStep =
                    engineOn
                        ? Math.signum(yawSollStep) * Math.min(YAW_RATE_MAX * MAIN_LOOP_STEP, Math.abs(yawSollStep))
                        : 0;
                yawRate = yawStep / MAIN_LOOP_STEP;
                // System.out.format("yawSoll=%f\tyawSollStep=%f\tyawStep=%f\tyaw=%f\n",yawSoll.degrees,yawSollStep,yawStep,yaw.degrees);
                double yawNew = yaw.degrees + yawStep;
                while (yawNew >= 360) {
                    yawNew -= 360;
                }

                while (yawNew < 0) {
                    yawNew += 360;
                }

                yaw = Angle.fromDegrees(yawNew);

                // System.out.println("yawNew:"+ yawNew + " engineOn:"+engineOn);

                // simulate roll in such a way, that acceleration inside the UAV points downwards
                double newRoll =
                    -Math.toDegrees(Math.atan(Math.toRadians(yawRate) * currentSpeed / 9.81)); // * YAW_RATE_TO_ROLL;
                // System.out.println("newRoll:"+newRoll + " yawRate:"+yawRate );
                rollRate = (newRoll - roll) / MAIN_LOOP_STEP;
                roll = isInCopterMode ? 0 : newRoll;

                // moving position and altitude / compute pitch
                double newPitch = 0;
                // Position posOld = pos;
                Vec4 vecOld = vec;
                if (engineOn) { // && !(fpCurObj instanceof LandingPoint && isInCopterMode && reached2Donce)) {
                    double altPrevious = pos.elevation;

                    if (isInCopterMode) {
                        double dist = currentSpeed * MAIN_LOOP_STEP;
                        Vec4 dVec = curTargetVec.subtract3(vec);
                        if (dVec.getLengthSquared3() != 0) {
                            dVec = dVec.normalize3().multiply3(dist);
                        }

                        vec = vec.add3(dVec);
                        pos = globe.computePositionFromPoint(vec.transformBy4(transform4Inv));
                        // System.out.println("vec: " + vec + "   step:"+dist + " p:"+pos + "
                        // dToTarget:"+dVec.getLength3()+ " dVec:"+dVec);
                        altMinInM = pos.elevation;
                        altMaxInM = pos.elevation;
                        newPitch = 0;
                        double pitchCamD = pitchCamSoll - pitchCam;
                        pitchCam =
                            pitchCam
                                + Math.signum(pitchCamD)
                                    * Math.min(PITCH_RATE_MAX * MAIN_LOOP_STEP, Math.abs(pitchCamD));

                        double rollCamD = rollCamSoll - rollCam;
                        rollCam =
                            rollCam
                                + Math.signum(rollCamD) * Math.min(ROLL_RATE_MAX * MAIN_LOOP_STEP, Math.abs(rollCamD));

                        // set airborne either after reaching a specified height or after reaching a waypoint's height
                        // (if it's lower that "AIRBORNE_HEIGHT")
                        if (pos.elevation >= AIRBORNE_HEIGHT && flightPhase == AirplaneFlightphase.takeoff
                                || (fpCurObj instanceof Waypoint
                                    && ((Waypoint)fpCurObj).getAltInMAboveFPRefPoint() < AIRBORNE_HEIGHT
                                    && pos.elevation >= ((Waypoint)fpCurObj).getAltInMAboveFPRefPoint() - 1.0d)) {
                            groundDist = 0;
                            setFlightPhase(AirplaneFlightphase.airborne);
                        }
                    } else {

                        // shift pos in 2d
                        double dist = currentSpeed * MAIN_LOOP_STEP;
                        Vec4 dVec = new Vec4(yaw.sin(), yaw.cos()).multiply3(dist);
                        vec = vec.add3(dVec);
                        pos = globe.computePositionFromPoint(vec.transformBy4(transform4Inv));

                        // compute current target altitude
                        double altSoll;
                        if ((assureAlt == AltAssertModes.linear || isInCopterMode) && percentLineDone != null) {
                            altSoll =
                                lastTargetPos.elevation
                                    + percentLineDone * (curTargetPos.elevation - lastTargetPos.elevation);
                        } else {
                            altSoll = curTargetPos.elevation;
                        }

                        // compute new altitude, and also range in which this could be
                        double altNew =
                            MathHelper.intoRange(
                                altSoll,
                                altPrevious - diveStep,
                                (flightPhase == AirplaneFlightphase.takeoff && !isInCopterMode)
                                    ? altPrevious + climbStep * 1.5
                                    : altPrevious + climbStep);
                        double altMinNew =
                            MathHelper.intoRange(
                                altSoll,
                                altMinInM - diveStep,
                                (flightPhase == AirplaneFlightphase.takeoff && !isInCopterMode)
                                    ? altMinInM + climbStep * 1.5
                                    : altMinInM + climbStep);
                        double altMaxNew =
                            MathHelper.intoRange(
                                altSoll,
                                altMaxInM - diveStep,
                                (flightPhase == AirplaneFlightphase.takeoff && !isInCopterMode)
                                    ? altMaxInM + climbStep * 1.5
                                    : altMaxInM + climbStep);
                        MinMaxPair alt = new MinMaxPair();
                        alt.update(altSoll);
                        alt.update(altNew);
                        alt.update(altMinNew);
                        alt.update(altMaxNew);
                        altMinInM = alt.min;
                        altMaxInM = alt.max;

                        pos = new Position(pos, altNew);
                        vec = globe.computePointFromPosition(pos).transformBy4(transform4);
                        if (!isFlyingLine && sendPositionOrientation) {
                            crossTrackErr = vec.distanceTo2(curTargetVec);
                        }

                        newPitch = isInCopterMode ? 0 : Math.toDegrees(Math.atan2(altNew - altPrevious, dist));
                        // System.out.println("altNew:"+altNew +" altPrevious:"+altPrevious + " altSoll:"+altSoll +"
                        // dist:"+dist
                        // +"pitch:"+newPitch);

                    }
                }

                double dist = vec.distanceTo3(vecOld);
                groundDist += dist;
                // System.out.println("vec:"+vec+ " vecOld:"+vecOld + " dist:"+dist);
                flightDistance += dist;
                double groundspeed = dist / MAIN_LOOP_STEP; // m/sec
                pitchRate = (newPitch - pitch) / MAIN_LOOP_STEP;
                pitch = newPitch;
                // System.out.println("groundSpeed_cms:"+groundspeed);

                Vec4 headingVec = vec.subtract3(vecOld);
                double heading = Math.toDegrees(Math.atan2(headingVec.y, headingVec.x));

                if (simTime >= nextPosOrSample) {
                    nextPosOrSample += POSITION_ORIENTATION_SAMPLE;
                    // System.out.println("sendPosOr:"+sendPositionOrientation);
                    if (sendPositionOrientation) {
                        // if (runAsync) System.out.println("send position");
                        final PositionOrientationData p = new PositionOrientationData();
                        p.time_sec = sec;
                        p.time_usec = usec;
                        p.flightmode = AirplaneFlightmode.AutomaticFlight.ordinal();
                        p.flightphase = flightPhase.ordinal();
                        p.altitude = (int)((pos.elevation + (Math.random() - 0.5) * 0.05) * 100);
                        // p.altitude = (int) ((altMaxInM+(Math.random()-0.5)*0.05) * 100) ;
                        p.lat =
                            pos.latitude.degrees
                                + (Math.random() - 0.5) * (0.001 * 360 / (Earth.WGS84_POLAR_RADIUS * 2 * Math.PI));
                        p.lon =
                            pos.longitude.degrees
                                + (Math.random() - 0.5) * (0.001 * 360 / (Earth.WGS84_POLAR_RADIUS * 2 * Math.PI));
                        p.reentrypoint = reentyPointID;
                        p.batteryVoltage = (float)battery;
                        p.batteryPercent = (float)battery;
                        if (isInCopterMode) { // && copterImagePointLast != null && copterImagePointNext != null &&
                            // isFlyingLine) {
                            p.cameraPitch = pitchCam; // percentLineDone * dPitch;
                            p.cameraRoll = rollCam; // percentLineDone * dRoll;
                            p.cameraYaw = yaw.degrees; // percentLineDone * dYaw;
                            while (p.cameraRoll < -180) {
                                p.cameraRoll += 360;
                            }

                            while (p.cameraRoll >= 180) {
                                p.cameraRoll -= 360;
                            }

                            while (p.cameraYaw < -180) {
                                p.cameraYaw += 360;
                            }

                            while (p.cameraYaw >= 180) {
                                p.cameraYaw -= 360;
                            }

                            while (p.cameraPitch < -180) {
                                p.cameraPitch += 360;
                            }

                            while (p.cameraPitch >= 180) {
                                p.cameraPitch -= 360;
                            }

                            p.pitch = 0; // p.cameraPitch;//TODO, actally this isnt the physic of the plattform
                            p.roll = 0; // p.cameraRoll;//TODO, actally this isnt the physic of the plattform
                            p.yaw = p.cameraYaw;
                        } else {
                            p.pitch = (float)pitch;
                            p.roll = (float)roll;
                            p.yaw = (float)yaw.degrees;
                        }

                        invokeMaybeAsyc(
                            new Runnable() {
                                @Override
                                public void run() {
                                    rootHandler.recv_positionOrientation(p);
                                }
                            });
                    }

                    // take images if distance and angles is ok
                    if (sendPhoto
                            && ((photoOn
                                    && !isPlannedImagesTriggersMode
                                    && vec.distanceTo3(lastPhotoVec) >= photoDistance / 100
                                    && (simTime - lastPhotoTime) >= fp.getPhotoSettings().getMinTimeInterval())
                                || (photoOn
                                    && !isPlannedImagesTriggersMode
                                    && !hasDoneLastBeforeCorner
                                    && vec.distanceTo3(curTargetVec) <= LAST_IMAGE_BEFORE_WAYPOINT_M
                                    && (simTime - lastPhotoTime) >= fp.getPhotoSettings().getMinTimeInterval())
                                || (isPlannedImagesTriggersMode && copterImagePoint != null))) {

                        // wait on point until cam is ready... simulated by time jump;
                        if (isPlannedImagesTriggersMode) {
                            if (simTime < fp.getPhotoSettings().getMinTimeInterval() + lastPhotoTime) {
                                simTime = fp.getPhotoSettings().getMinTimeInterval() + lastPhotoTime;
                            }
                        }

                        if (!shouldTakeImage) {
                            if (lastPhotoReentryTriggerID != reentyPointID) {
                                hasDoneLastBeforeCorner = false;
                                if (roll < 0) {
                                    photo_roll_sign = -1;
                                } else {
                                    photo_roll_sign = +1;
                                }
                            }

                            lastPhotoReentryTriggerID = reentyPointID;
                            shouldTakeImage = true;
                        }

                        // but sometimes also if the last image is way to far
                        // away, even with bad angles
                        if ((photo_roll_sign * rollRate >= -5
                                    && Math.abs(pitch) <= fp.getPhotoSettings().getMaxNick()
                                    && Math.abs(roll) <= fp.getPhotoSettings().getMaxRoll())
                                || vec.distanceTo3(lastPhotoVec) >= photoDistanceMax / 100
                                || vec.distanceTo3(curTargetVec) <= LAST_IMAGE_BEFORE_WAYPOINT_M
                                || lastPhotoReentryTriggerID != reentyPointID
                                || copterImagePoint != null) {
                            // System.out.format("photo roll=%f\tpitch=%f\n",
                            // roll,pitch);
                            if (vec.distanceTo3(curTargetVec) <= LAST_IMAGE_BEFORE_WAYPOINT_M) {
                                hasDoneLastBeforeCorner = true;
                            }

                            shouldTakeImage = false;

                            photo_roll_sign = 0;
                            lastPhotoPos = pos;
                            lastPhotoVec = vec;
                            lastPhotoTime = simTime;
                            noLastImage++;
                            final PhotoData photo = new PhotoData();
                            photo.alt = (int)(pos.elevation * 100);
                            photo.time_sec = sec;
                            photo.time_usec = usec;
                            photo.lat = pos.latitude.degrees;
                            photo.lon = pos.longitude.degrees;
                            photo.reentrypoint = reentyPointID;
                            if (isInCopterMode) {
                                photo.camera_pitch = (float)pitchCam; // percentLineDone * dPitch;
                                photo.camera_roll = (float)rollCam; // percentLineDone * dRoll;
                                photo.camera_yaw = (float)yaw.degrees; // percentLineDone * dYaw;
                                while (photo.camera_roll < -180) {
                                    photo.camera_roll += 360;
                                }

                                while (photo.camera_roll >= 180) {
                                    photo.camera_roll -= 360;
                                }

                                while (photo.camera_yaw < -180) {
                                    photo.camera_yaw += 360;
                                }

                                while (photo.camera_yaw >= 180) {
                                    photo.camera_yaw -= 360;
                                }

                                while (photo.camera_pitch < -180) {
                                    photo.camera_pitch += 360;
                                }

                                while (photo.camera_pitch >= 180) {
                                    photo.camera_pitch -= 360;
                                }
                            } else {
                                photo.camera_pitch = (float)pitch;
                                photo.camera_roll = (float)roll;
                                photo.camera_yaw = (float)yaw.degrees;
                            }

                            photo.time_since_last_fix = 0;
                            photo.gyropitch = (float)pitchRate;
                            photo.gyroroll = (float)rollRate;
                            photo.gyroyaw = (float)yawRate;
                            photo.number = noLastImage;
                            photo.heading = (float)heading;
                            photo.groundspeed = (int)Math.round(groundspeed * 100);
                            photo.type = PhotoLogLineType.FLASH.ordinal() + 1;
                            photo.gps_alt =
                                (int)
                                    Math.round(
                                        100 * (getStartElevOverWGS84() + pos.elevation - getStartElevEgmOffset()));
                            photo.gps_ellipsoid = (float)(getStartElevEgmOffset() * 100);
                            photo.gps_mode = 1;
                            invokeMaybeAsyc(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        rootHandler.recv_photo(photo);
                                    }
                                });
                        }
                    }
                }

                if (sendDebug && simTime >= nextDebugSample) {
                    nextDebugSample += DEBUG_SAMPLE;

                    final DebugData d = new DebugData();
                    d.time_sec = sec;
                    d.time_usec = usec;
                    if (sendPositionGPSAlt) {
                        d.gpsAltitude =
                            (int)Math.round(100 * (getStartElevOverWGS84() + pos.elevation - getStartElevEgmOffset()));
                        d.gps_ellipsoid = (float)(getStartElevEgmOffset() * 100);
                    }

                    d.groundspeed = (int)Math.round(groundspeed * 100);
                    // System.out.println("AirSim:d.groundSpped:"+d.groundspeed + " " + groundspeed);
                    d.cross_track_error = (int)(100 * crossTrackErr);
                    d.heading = (float)heading;
                    d.groundDistance = (int)(groundDist * 100);

                    d.gyropitch = (float)pitchRate;
                    d.gyroroll = (float)rollRate;
                    d.gyroyaw = (float)yawRate;

                    invokeMaybeAsyc(
                        new Runnable() {
                            @Override
                            public void run() {
                                rootHandler.recv_debug(d);
                            }
                        });
                }

                if (sendHealth && simTime >= nextHEALTHsample) {
                    nextHEALTHsample += HEALTH_SAMPLE;

                    final HealthData h = new HealthData();
                    h.absolute.add((float)battery);
                    h.percent.add((float)battery);
                    h.absolute.add((float)10); // GPS sats
                    h.percent.add((float)10); // GPS sats
                    GPSFixType fixType =
                        fp.getHardwareConfiguration().getPlatformDescription().getGpsType().getBestFixType();
                    h.absolute.add((float)(fixType.ordinal())); // gps fix type
                    h.percent.add((float)(fixType.ordinal())); // gps fix type
                    invokeMaybeAsyc(
                        new Runnable() {
                            @Override
                            public void run() {
                                rootHandler.recv_health(h);
                            }
                        });
                }

                if (runAsync) {
                    double simSpeedTmp = Math.max(Math.min(simSpeed, MAX_SIM_SPEED), MIN_SIM_SPEED);
                    // if (simSpeed <= 0)
                    // simSpeedTmp = MAX_SIM_SPEED;
                    long sleep_time =
                        Math.round(1000. * MAIN_LOOP_STEP / simSpeedTmp)
                            - (System.currentTimeMillis() - timeStartThisLoop);
                    sleep_time += sleepUebertrag;
                    // System.out.println("sleeping for: " + sleep_time + " @
                    // speed="+simSpeedTmp + "
                    // consumedTime="+(System.currentTimeMillis() -
                    // timeStartThisLoop));
                    if (sleep_time > 0) {
                        sleepUebertrag = 0;
                        try {
                            Thread.sleep(sleep_time);
                        } catch (InterruptedException e) {
                        }
                    } else {
                        sleepUebertrag = Math.max(sleep_time, -1000); // maximal
                        // üebertrag
                        // 1 sec
                    }
                }
            }
            // }
        } catch (Exception e) {
            e.printStackTrace();
            Debug.getLog().log(Level.WARNING, "local Mission simulation failed", e);
        } finally {
            clearFP();
            // System.out.println("simulation terminated "+AirplaneSim.this);
        }
    }

    protected void invokeMaybeAsyc(Runnable r) {
        if (runAsync) {
            try {
                Dispatcher.platform().run(r);
            } catch (Exception e) {
                if (e.getCause() instanceof InterruptedException) {
                    // ignore
                } else {
                    Debug.getLog().log(Level.WARNING, "Could not send messages from sim to listeners", e);
                }
            }
        } else {
            r.run();
        }
    }

    private IHardwareConfiguration nativeHardwareConfiguration;

    public AirplaneSim(IAirplane plane) {
        this.plane = plane;
        this.nativeHardwareConfiguration = plane.getNativeHardwareConfiguration();
        plane.setAirplaneConnector(this);
        init(plane.getRootHandler(), true);
    }

    public AirplaneSim(
            IAirplaneListenerDelegator rootHandler,
            boolean runAsync,
            IHardwareConfiguration nativeHardwareConfiguration) {
        init(rootHandler, runAsync);
        this.nativeHardwareConfiguration = nativeHardwareConfiguration;
    }

    protected void init(IAirplaneListenerDelegator rootHandler, boolean runAsync) {
        this.runAsync = runAsync;
        simNo = nextSimNo++;
        setRootHandler(rootHandler);

        name = "LocalSimulation:" + simNo;

        planeInfo.hardwareType = "LocalSimulation";
        planeInfo.revisionSoftware = (int)StaticInjector.getInstance(IVersionProvider.class).getBuildCommitTimeAsLong();
        planeInfo.releaseVersion = StaticInjector.getInstance(IVersionProvider.class).getAppMajorVersion();
        planeInfo.revisionHardware = planeInfo.revisionSoftware;
        planeInfo.serialNumber =
            StaticInjector.getInstance(ILicenceManager.class).getActiveLicence().getLicenceId() + simNo;
        planeInfo.servoChannelCount = 4;
        planeInfo.totalairkm = 650;
        planeInfo.totalairtime = 12345;
        planeInfo.numberofflights = 123;

        SingleHealthDescription sh = new SingleHealthDescription();
        sh.doWarnings = true;
        sh.isImportant = true;
        sh.maxGreen = 100;
        sh.minGreen = 20;
        sh.maxYellow = 100;
        sh.minYellow = 10;
        sh.name = PlaneConstants.DEF_BATTERY;
        sh.unit = "%";
        planeInfo.healthDescriptions.add(sh);

        sh = new SingleHealthDescription();
        sh.doWarnings = true;
        sh.isImportant = true;
        sh.maxGreen = 100;
        sh.minGreen = 4;
        sh.maxYellow = 100;
        sh.minYellow = 3;
        sh.name = PlaneConstants.DEF_GPS;
        sh.unit = "sat";
        planeInfo.healthDescriptions.add(sh);

        sh = new SingleHealthDescription();
        sh.doWarnings = true;
        sh.isImportant = true;
        sh.maxGreen = 100;
        sh.minGreen = 2;
        sh.maxYellow = 100;
        sh.minYellow = 1;
        sh.name = PlaneConstants.DEF_GPS_QUALITY;
        sh.unit = "mode";
        planeInfo.healthDescriptions.add(sh);

        config = new Config_variables(planeInfo.servoChannelCount);

        fireConnectionState(AirplaneConnectorState.fullyConnected);

        reset();

        if (runAsync) {
            workerThread = new Thread(this, "Java Sim Thread");
            workerThread.start();
            // System.out.println("starting sim thread");
        }
    }

    public void notImplemented(final String fctName) {
        invokeMaybeAsyc(
            new Runnable() {
                @Override
                public void run() {
                    rootHandler.recv_msg(0, "Function not implemented:" + fctName);
                }
            });
    }

    @Override
    public boolean isWriteable() {
        return true;
    }

    @Override
    public boolean isReadable() {
        return true;
    }

    @Override
    public void close() {
        running = false;
        if (workerThread != null) {
            workerThread.interrupt();
        }

        if (plane != null) {
            plane.unsetAirplaneConnector();
        }
    }

    @Override
    public Object getPlanePort() {
        return "InternalSim:" + simNo;
    }

    @Override
    public void connect(String port) {
        notImplemented("connect");
    }

    @Override
    public void mavinci(Integer version) {
        notImplemented("mavinci");
    }

    @Override
    public void requestBackend() {
        notImplemented("saveConfig");
    }

    @Override
    public void requestIsSimulation() {
        invokeMaybeAsyc(
            new Runnable() {
                @Override
                public void run() {
                    rootHandler.recv_isSimulation(true);
                }
            });
    }

    @Override
    public void requestPlaneInfo() {
        invokeMaybeAsyc(
            new Runnable() {
                @Override
                public void run() {
                    rootHandler.recv_planeInfo(planeInfo);
                }
            });
    }

    @Override
    public void requestConfig() {
        invokeMaybeAsyc(
            new Runnable() {
                @Override
                public void run() {
                    rootHandler.recv_config(config);
                }
            });
    }

    @Override
    public void setConfig(Config_variables c) {
        config = c;
        requestConfig();
    }

    @Override
    public void shutdown() {
        notImplemented("shutdown");
    }

    @Override
    public void saveConfig() {
        notImplemented("saveConfig");
    }

    @Override
    public synchronized void setFlightPhase(AirplaneFlightphase p) {
        if (flightPhase == p) {
            return;
        }

        if (fp != null && p == AirplaneFlightphase.jumpToLanding && p.isFlightphaseOnGround() != 1) {
            setFlightPlan(fp, fp.getLandingpoint().getId());
            requestFlightPhase();
            return;
        }

        flightPhase = p;
        engineOn = p.isFlightphaseOnGround() != 1;
        if (!engineOn && pos.elevation != 0) {
            pos = new Position(pos, 0);
            vec = new Vec4(vec.x, vec.y);
            altMaxInM = altMinInM = pos.elevation;
        }

        requestFlightPhase();
    }

    @Override
    public void requestFlightPhase() {
        invokeMaybeAsyc(
            new Runnable() {
                @Override
                public void run() {
                    rootHandler.recv_flightPhase(flightPhase.ordinal());
                }
            });
    }

    @Override
    public void setFlightPlanASM(final String plan, final Integer entrypoint) {
        notImplemented("setFlightPlanASM");
    }

    boolean continued = false;

    public synchronized void clearFP() {
        fpConts.clear();
        this.fp = null;
        fpIsEmpty = true;
        fpCurObj = null;
    }

    public synchronized void assureInitFP() {
        if (fp != null) {
            return;
        }

        fpIsEmpty = true; // the new FP below doesn't count!
        fp = new Flightplan();
        fp.getLandingpoint().setLatLon(startPos);
        ContainerStackElem tmp = new ContainerStackElem();
        tmp.cont = fp;
        tmp.curIndex = -1;
        fpConts.push(tmp);
    }

    public synchronized void setFlightPlan(Flightplan fp, final Integer entrypoint) {
        fpConts.clear();
        this.fp = fp;
        fpCurObj = fp;
        fpIsEmpty = false;
        // System.out.println("setFlightplanXML entrypoint="+entrypoint + "
        // xml="+fp.toXML());
        assureAlt = AltAssertModes.jump;
        lastTargetPos = curTargetPos = pos; // triggers waypoint is reached, searching for
        // nextWP
        lastTargetVec = curTargetVec = vec;
        lastTargetVec2d = curTargetVec2d = vec == null ? null : new Vec4(vec.x, vec.y);

        final int targetReentrypoint;
        if (entrypoint == -1) {
            targetReentrypoint = reentyPointID;
        } else {
            targetReentrypoint = entrypoint;
        }

        continued = false;

        if (targetReentrypoint != -1 && targetReentrypoint != 0) {
            // System.out.println("jump here: " + targetReentrypoint);
            // build container stack and search current object
            AFlightplanVisitor vis =
                new AFlightplanVisitor() {

                    @Override
                    public boolean visit(IFlightplanRelatedObject fpObj) {
                        if (fpObj instanceof IReentryPoint) {
                            IReentryPoint rp = (IReentryPoint)fpObj;
                            if (rp.getId() == targetReentrypoint) {
                                // System.out.println("RP-found = " +rp);
                                continued = true;
                                if (rp instanceof IFlightplanContainer) {
                                    // this codes enables to jump to the ignored breakup path loop, by just picking the
                                    // first internal jumpable
                                    // object
                                    fpCurObj = fpObj;
                                    IFlightplanContainer cont = (IFlightplanContainer)fpObj;
                                    ContainerStackElem tmp = new ContainerStackElem();
                                    tmp.enteringTime = simTime;
                                    tmp.cont = cont;
                                    tmp.curIndex = -1;
                                    tmp.loopCounter = 0;
                                    // System.out.println("final Cont:"+fpObj);
                                    fpConts.push(tmp);
                                    return true;
                                }
                                // System.out.println("final RP:" + rp);

                                if (rp instanceof LandingPoint) {
                                    fpConts.clear();
                                    fpCurObj = rp;
                                    reentyPointID = rp.getId();
                                    LandingPoint posRef = (LandingPoint)fpCurObj;
                                    curTargetPos = posRef.getPosition();
                                    curTargetVec =
                                        globe.computePointFromPosition(curTargetPos).transformBy4(transform4);
                                }

                                return true;
                            }
                        }
                        // this makes shure, that the last object BEFORE the
                        // searched reentrypoint is stored.
                        // this makes the reentrypoint processed in the FML VM above
                        fpCurObj = fpObj;
                        if (fpObj instanceof IFlightplanContainer) {
                            IFlightplanContainer cont = (IFlightplanContainer)fpObj;
                            ContainerStackElem tmp = new ContainerStackElem();
                            tmp.enteringTime = simTime;
                            tmp.cont = cont;
                            tmp.curIndex = -1;
                            tmp.loopCounter = 0;
                            // System.out.println("push:"+fpObj);
                            fpConts.push(tmp);
                        }

                        return false;
                    }

                    @Override
                    public boolean visitExit(IFlightplanRelatedObject fpObj) {
                        if (fpObj instanceof IFlightplanContainer) {
                            fpConts.pop();
                            // System.out.println("pop");
                        }

                        return super.visitExit(fpObj);
                    }
                };
            vis.startVisit(fp);

            // System.out.println("temp stack:" + fpConts);

            // fix indices in container stack
            IFlightplanRelatedObject toSearch = fpCurObj;
            for (int i = fpConts.size() - 1; i >= 0; i--) {
                ContainerStackElem tmp = fpConts.get(i);
                for (int k = 0; k != tmp.cont.sizeOfFlightplanContainer(); k++) {
                    if (tmp.cont.getFromFlightplanContainer(k) == toSearch) {
                        tmp.curIndex = k;
                        break;
                    }
                }

                toSearch = tmp.cont;
            }

            // System.out.println("current stack:" + fpConts + "
            // fpCurObj="+fpCurObj);

        }

        if (!continued) {
            continued = false;
            fpConts.clear();
            ContainerStackElem tmp = new ContainerStackElem();
            tmp.enteringTime = simTime;
            tmp.cont = fp;
            tmp.curIndex = -1;
            tmp.loopCounter = 0;
            fpConts.push(tmp);
            fpCurObj = fp;
            // System.out.println("current stack:" + fpConts);
        }

        noCirclesCurrentTaget = 0;
        noCirclesDone = 0;
        // if (runAsync) System.out.println("======================================\nSTART:\nfpCurObj"+fpCurObj);

        // System.out.println("current stack:" + fpConts + "
        // fpCurObj="+fpCurObj);
    }

    @Override
    public void setFlightPlanXML(String plan, final Integer entrypoint) {
        assureInitFP();
        fp.fromXML(plan);
        setFlightPlan(fp, entrypoint);
        requestFlightPlanXML();
    }

    @Override
    public void requestFlightPlanXML() {
        invokeMaybeAsyc(
            new Runnable() {
                @Override
                public void run() {
                    CFlightplan fp = AirplaneSim.this.fp;
                    if (fp == null || fpIsEmpty) {
                        // System.out.println("send empoty FP");
                        // sending empty mission causes setting default platform settings to the
                        // fpManager.onAirFlightPlan
                        // -- so the view on the map also correspond to the default platform
                        fp = new Flightplan();
                        rootHandler.recv_setFlightPlanXML(fp.toXML(), false, true);
                        return;
                    }

                    rootHandler.recv_setFlightPlanXML(fp.toXML(), continued, true);
                }
            });
    }

    @Override
    public void requestFlightPlanASM() {
        notImplemented("requestFlightPlanASM");
    }

    @Override
    public void requestStartpos() {
        invokeMaybeAsyc(
            new Runnable() {
                @Override
                public void run() {
                    rootHandler.recv_startPos(startPos.longitude.degrees, startPos.latitude.degrees, 0);
                    rootHandler.recv_startPos(startPos.longitude.degrees, startPos.latitude.degrees, 1);
                }
            });
    }

    @Override
    public void setSimulationSpeed(Float speed) {
        if (simSpeed != speed) {
            simSpeed = speed;
            sleepUebertrag = 0;
        }

        requestSimulationSpeed();
    }

    @Override
    public void setSimulationSettings(SimulationSettings settings) {
        simSettings = settings;
        requestSimulationSettings();
    }

    @Override
    public void requestSimulationSettings() {
        invokeMaybeAsyc(
            new Runnable() {
                @Override
                public void run() {
                    rootHandler.recv_simulationSettings(simSettings);
                }
            });
    }

    @Override
    public void requestSimulationSpeed() {
        invokeMaybeAsyc(
            new Runnable() {
                @Override
                public void run() {
                    rootHandler.recv_simulationSpeed((float)simSpeed);
                }
            });
    }

    @Override
    public void setStartpos(Double lon, Double lat) {
        // here is assumed that takeoff at the same elevation as the point R
        setStartpos(LatLon.fromDegrees(lat, lon), 0);
    }

    public synchronized void setStartpos(LatLon latLon, double takeoffElevOverR) {
        // System.out.println("setStartPos:"+latLon);
        // Debug.printStackTrace(latLon);
        startPos = latLon;
        startElev = null;
        startElevOverR = takeoffElevOverR;
        startElevEGMOffset = null;
        pos = new Position(startPos, engineOn ? pos.elevation + startElevOverR : startElevOverR);
        altMaxInM = altMinInM = pos.elevation;
        if (transform4 != null) {
            vec = globe.computePointFromPosition(pos).transformBy4(transform4);
        }

        initConstants();
        curTargetPos = lastTargetPos = pos;
        curTargetVec = lastTargetVec = vec;
        lastTargetVec2d = curTargetVec2d = vec == null ? null : new Vec4(vec.x, vec.y);
        if (!engineOn) {
            yaw = Angle.ZERO;
        }

        vecStart = globe.computePointFromPosition(pos).transformBy4(transform4);

        requestStartpos();
    }

    @Override
    public void expertUpdateFirmware(String path) {
        notImplemented("expertUpdateFirmware");
    }

    @Override
    public void expertUpdateBackend(String path) {
        notImplemented("expertUpdateBackend");
    }

    @Override
    public void expertRecalibrate() {
        notImplemented("expertRecalibrate");
    }

    @Override
    public void expertTrimOn() {
        notImplemented("expertTrimOn");
    }

    @Override
    public void expertTrimOff() {
        notImplemented("expertTrimOff");
    }

    @Override
    public void expertRecalibrateCompassStart() {
        notImplemented("expertRecalibrateCompassStart");
    }

    @Override
    public void expertRecalibrateCompassStop() {
        notImplemented("expertRecalibrateCompassStop");
    }

    @Override
    public void dbgRTon() {
        notImplemented("dbgRTon");
    }

    @Override
    public void dbgRToff() {
        notImplemented("dbgRToff");
    }

    @Override
    public void dbgExitAutopilot() {
        notImplemented("dbgExitAutopilot");
    }

    @Override
    public void dbgResetDebug() {
        notImplemented("dbgResetDebug");
    }

    @Override
    public void requestAirplaneName() {
        invokeMaybeAsyc(
            new Runnable() {
                @Override
                public void run() {
                    rootHandler.recv_nameChange(name);
                }
            });
    }

    @Override
    public void setAirplaneName(String newName) {
        name = newName;
        requestAirplaneName();
    }

    @Override
    public void setFixedOrientation(Float roll, Float pitch, Float yaw) {
        notImplemented("setFixedOrientation");
    }

    @Override
    public void requestFixedOrientation() {
        notImplemented("requestFixedOrientation");
    }

    @Override
    public void dbgCommand0() {
        notImplemented("dbgCommand0");
    }

    @Override
    public void dbgCommand1() {
        notImplemented("dbgCommand1");
    }

    @Override
    public void dbgCommand2() {
        notImplemented("dbgCommand2");
    }

    @Override
    public void dbgCommand3() {
        notImplemented("dbgCommand3");
    }

    @Override
    public void dbgCommand4() {
        notImplemented("dbgCommand4");
    }

    @Override
    public void dbgCommand5() {
        notImplemented("dbgCommand5");
    }

    @Override
    public void dbgCommand6() {
        notImplemented("dbgCommand6");
    }

    @Override
    public void dbgCommand7() {
        notImplemented("dbgCommand7");
    }

    @Override
    public void dbgCommand8() {
        notImplemented("dbgCommand8");
    }

    @Override
    public void dbgCommand9() {
        notImplemented("dbgCommand9");
    }

    @Override
    public void updateAndroidState(final AndroidState state) {
        invokeMaybeAsyc(
            new Runnable() {
                @Override
                public void run() {
                    rootHandler.recv_androidState(state);
                }
            });
    }

    File ftpFolderRoot =
        new File(StaticInjector.getInstance(IPathProvider.class).getSettingsDirectory().toFile(), "simulationFileRoot");

    @Override
    public void requestDirListing(final String path) {
        invokeMaybeAsyc(
            new Runnable() {
                @Override
                public void run() {
                    File[] list = getFileForFTPpath(path).listFiles();
                    MVector<String> files = new MVector<String>(String.class);
                    MVector<Integer> sizes = new MVector<Integer>(Integer.class);
                    files.add("..");
                    sizes.add(-1);
                    if (list != null && list.length > 0) {
                        for (File f : list) {
                            files.add(f.getName());
                            sizes.add(f.isDirectory() ? -1 : (int)f.length());
                        }
                    }

                    rootHandler.recv_dirListing(path, files, sizes);
                }
            });
    }

    @Override
    public void getFile(final String path) {
        invokeMaybeAsyc(
            new Runnable() {
                @Override
                public void run() {
                    rootHandler.recv_fileReceivingSucceeded(path);
                }
            });
    }

    @Override
    public void sendFile(final String path) {
        invokeMaybeAsyc(
            new Runnable() {
                @Override
                public void run() {
                    rootHandler.recv_fileSendingSucceeded(path);
                }
            });
    }

    public File getFileForFTPpath(String path) {
        File f = new File(MFile.adaptToCurSystem(ftpFolderRoot + File.separator + path));
        f.getParentFile().mkdirs();
        return f;
    }

    @Override
    public void deleteFile(String path) {
        getFileForFTPpath(path).delete();
    }

    @Override
    public void makeDir(String path) {
        getFileForFTPpath(path).mkdirs();
    }

    @Override
    public void writeToFlash(String path) {
        notImplemented("writeToFlash");
    }

    @Override
    public void cancelSending(String path) {
        notImplemented("cancelSending");
    }

    @Override
    public void cancelReceiving(String path) {
        notImplemented("cancelReceiving");
    }

    @Override
    public boolean isSimulation() {
        return true;
    }

    @Override
    public void cancelLaunch() {
        // TODO @Marco
    }

    @Override
    public void cancelLanding() {
        // TODO @Marco
    }

    Double startElev = null;
    protected double startElevOverR;

    public double getStartElevOverWGS84() {
        // System.out.println("getStartElev:"+startElev);
        if (startElev == null) {
            startElev = elevationModel.getElevationAsGoodAsPossible(startPos);
        }

        return startElev;
    }

    Double startElevEGMOffset = null;

    public double getStartElevEgmOffset() {
        // System.out.println("getStartElev:"+startElev);
        if (startElevEGMOffset == null) {
            startElevEGMOffset = egmModel.getEGM96Offset(startPos);
        }

        return startElevEGMOffset;
    }

    public void setStartEgmOffset(double startEgmOffset) {
        this.startElevEGMOffset = startEgmOffset;
    }

    public void setStartElev(double startElev) {
        this.startElev = startElev;
    }

    public double getStartElevOverR() {
        return startElevOverR;
    }

    @Override
    public void expertUpdateBackendTopconOAF(String path) {
        notImplemented("expertUpdateBackendTopconOAF");
    }

    @Override
    public void expertUpdateFirmwareTopconOAF(String path) {
        notImplemented("expertUpdateFirmwareTopconOAF");
    }

    @Override
    public void expertSendSimulatedFails(Integer failBitMask, Integer debug1, Integer debug2, Integer debug3) {
        notImplemented("expertSendSimulatedFails");
    }

    @Override
    public void expertRequestSimulatedFails() {
        notImplemented("expertRequestSimulatedFails");
    }

    @Override
    public void clearNVRAM() {
        notImplemented("clearNVRAM");
    }

    @Override
    public void setManualServos(Vector<Integer> manualServos) {
        notImplemented("setManualServos");
    }

    public AirplaneFlightphase getFlightPhase() {
        return flightPhase;
    }
}
