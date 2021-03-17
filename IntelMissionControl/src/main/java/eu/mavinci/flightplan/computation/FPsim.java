/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.flightplan.computation;

import com.intel.missioncontrol.INotificationObject;
import com.intel.missioncontrol.map.elevation.ElevationModelRequestException;
import com.intel.missioncontrol.map.elevation.IEgmModel;
import com.intel.missioncontrol.map.elevation.IElevationModel;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.networking.INetworkInformation;
import com.intel.missioncontrol.settings.AirspacesProvidersSettings;
import com.intel.missioncontrol.settings.ExpertSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import de.saxsys.mvvmfx.MvvmFX;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import de.saxsys.mvvmfx.utils.notifications.NotificationObserver;
import de.saxsys.mvvmfx.utils.notifications.WeakNotificationObserver;
import eu.mavinci.airspace.AirspaceComperatorFloor;
import eu.mavinci.airspace.EAirspaceManager;
import eu.mavinci.airspace.IAirspace;
import eu.mavinci.airspace.IAirspaceListener;
import eu.mavinci.core.flightplan.CFlightplan;
import eu.mavinci.core.flightplan.CPhoto;
import eu.mavinci.core.flightplan.IFlightplanChangeListener;
import eu.mavinci.core.flightplan.IFlightplanLatLonReferenced;
import eu.mavinci.core.flightplan.IFlightplanPositionReferenced;
import eu.mavinci.core.flightplan.IFlightplanRelatedObject;
import eu.mavinci.core.flightplan.visitors.ExtractTypeVisitor;
import eu.mavinci.core.helper.MinMaxPair;
import eu.mavinci.core.listeners.IListener;
import eu.mavinci.core.plane.AirplaneConnectorState;
import eu.mavinci.core.plane.AirplaneFlightphase;
import eu.mavinci.core.plane.listeners.IAirplaneListenerDelegator;
import eu.mavinci.core.plane.sendableobjects.AndroidState;
import eu.mavinci.core.plane.sendableobjects.Backend;
import eu.mavinci.core.plane.sendableobjects.CommandResultData;
import eu.mavinci.core.plane.sendableobjects.Config_variables;
import eu.mavinci.core.plane.sendableobjects.DebugData;
import eu.mavinci.core.plane.sendableobjects.HealthData;
import eu.mavinci.core.plane.sendableobjects.LinkInfo;
import eu.mavinci.core.plane.sendableobjects.MVector;
import eu.mavinci.core.plane.sendableobjects.OrientationData;
import eu.mavinci.core.plane.sendableobjects.PhotoData;
import eu.mavinci.core.plane.sendableobjects.PlaneInfo;
import eu.mavinci.core.plane.sendableobjects.Port;
import eu.mavinci.core.plane.sendableobjects.PositionData;
import eu.mavinci.core.plane.sendableobjects.PositionOrientationData;
import eu.mavinci.core.plane.sendableobjects.SimulationSettings;
import eu.mavinci.desktop.helper.IRecomputerListenerManager;
import eu.mavinci.desktop.helper.Recomputer;
import eu.mavinci.desktop.main.debug.Debug;
import eu.mavinci.flightplan.Flightplan;
import eu.mavinci.flightplan.PicArea;
import eu.mavinci.flightplan.Point;
import eu.mavinci.plane.simjava.AirplaneSim;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globes.Earth;
import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.terrain.CompoundElevationModel;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import org.asyncfx.collections.ArraySet;

public class FPsim extends Recomputer
        implements IFlightplanChangeListener, IRecomputerListenerManager, INotificationObject.ChangeListener {

    private static final IElevationModel elevationModel =
        DependencyInjector.getInstance().getInstanceOf(IElevationModel.class);
    private static final IEgmModel egmModel = DependencyInjector.getInstance().getInstanceOf(IEgmModel.class);

    private static final double TAKEOFF_RADIUS = 10;

    static boolean computePreviewSim =
        DependencyInjector.getInstance().getInstanceOf(ExpertSettings.class).getComputePreviewSim();

    final AirplaneSim sim;
    final Flightplan fp;
    private final NotificationObserver golfChangedNotifcationObserver;

    Runnable runnable =
        new Runnable() {

            @Override
            public void run() {
                simFP();
            }

        };

    public static String KEY = "eu.mavinci.flightplan.FPsim";

    LocalSimListener localSimListener = new LocalSimListener();
    private final ChangeListener<Boolean> networkBecomesAvailableListener =
        new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (lastSimResult == null || lastSimResult.elevationDataAvaliable) {
                    return;
                }

                if (newValue) {
                    FPsim.this.tryStartRecomp();
                }
            }
        };
    private final ChangeListener<Boolean> airspaceUseChangeListener =
        new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                FPsim.this.tryStartRecomp();
            }
        };

    private final IAirspaceListener airspaceListener =
        new IAirspaceListener() {
            @Override
            public void airspacesChanged() {
                FPsim.this.tryStartRecomp();
            }
        };

    class LocalSimListener implements IAirplaneListenerDelegator {

        IFlightplanPositionReferenced lastPosRef;

        @Override
        public void recv_position(PositionData p) {}

        @Override
        public void recv_photo(PhotoData photo) {
            tmpSimResult.photos.add(photo);
        }

        @Override
        public void err_backendConnectionLost(ConnectionLostReasons reason) {}

        @Override
        public void recv_config(Config_variables c) {}

        @Override
        public void recv_flightPhase(Integer fp) {}

        @Override
        public void recv_setFlightPlanASM(String plan, Boolean reentry, Boolean succeed) {}

        @Override
        public void recv_setFlightPlanXML(String plan, Boolean reentry, Boolean succeed) {}

        @Override
        public void recv_health(HealthData d) {}

        @Override
        public void recv_isSimulation(Boolean simulation) {}

        @Override
        public void recv_msg(Integer lvl, String data) {}

        @Override
        public void recv_nameChange(String name) {}

        @Override
        public void recv_orientation(OrientationData o) {}

        @Override
        public void recv_backend(Backend host, MVector<Port> ports) {}

        @Override
        public void recv_simulationSpeed(Float speed) {}

        @Override
        public void recv_simulationSettings(SimulationSettings settings) {}

        @Override
        public void recv_startPos(Double lon, Double lat, Integer pressureZero) {}

        @Override
        public void recv_fixedOrientation(Float roll, Float pitch, Float yaw) {}

        @Override
        public void rawDataFromBackend(String line) {}

        @Override
        public void rawDataToBackend(String line) {}

        @Override
        public void recv_connectionEstablished(String port) {}

        @Override
        public void recv_powerOn() {}

        @Override
        public void recv_planeInfo(PlaneInfo info) {}

        @Override
        public void recv_androidState(AndroidState state) {}

        @Override
        public void recv_linkInfo(LinkInfo li) {}

        @Override
        public void ping(String senderID) {}

        @Override
        public void recv_dirListing(String parentPath, MVector<String> files, MVector<Integer> sizes) {}

        @Override
        public void recv_fileSendingProgress(String path, Integer progress) {}

        @Override
        public void recv_fileReceivingProgress(String path, Integer progress) {}

        @Override
        public void recv_fileSendingSucceeded(String path) {}

        @Override
        public void recv_fileReceivingSucceeded(String path) {}

        @Override
        public void recv_fileSendingCancelled(String path) {}

        @Override
        public void recv_fileReceivingCancelled(String path) {}

        @Override
        public void loggingStateChangedTCP(boolean tcp_log_active) {}

        @Override
        public void loggingStateChangedFLG(boolean plane_log_active) {}

        @Override
        public void guiClose() {}

        @Override
        public boolean guiCloseRequest() {
            return true;
        }

        @Override
        public void storeToSessionNow() {}

        @Override
        public void connectionStateChange(AirplaneConnectorState newState) {}

        @Override
        public void elapsedSimTime(double secs, double secsTotal) {}

        @Override
        public void replayStopped(boolean stopped) {}

        @Override
        public void replayPaused(boolean paused) {}

        @Override
        public void replaySkipPhase(boolean isSkipping) {}

        @Override
        public void replayFinished() {}

        @Override
        public void addListener(IListener l) {}

        @Override
        public void addListenerAtBegin(IListener l) {}

        @Override
        public void addListenerAtSecond(IListener l) {}

        @Override
        public void removeListener(IListener l) {}

        @Override
        public void recv_startPos(Double lon, Double lat) {}

        @Override
        public void recv_debug(DebugData d) {}

        @Override
        public void recv_cmd_result(CommandResultData d) {}

        Position posLastLocalHeights;

        @Override
        public void recv_positionOrientation(PositionOrientationData po) {
            tmpSimResult.progressMap.put(po.reentrypoint, sim.flightDistance);
            if (sim.fpCurObj instanceof IFlightplanPositionReferenced) {
                lastPosRef = (IFlightplanPositionReferenced)sim.fpCurObj;
            }

            if (lastPosRef != null) {
                tmpSimResult.posMap.put(po.reentrypoint, lastPosRef);
            }

            if (tmpSimResult.firstFPobj == null) {
                tmpSimResult.firstFPobj = sim.fpCurObj;
            }

            if (!tmpSimResult.startCircleDone
                    && (tmpSimResult.firstFPobj != sim.fpCurObj || !tooCloseToTheTakeoff(sim.pos))) {
                tmpSimResult.startCircleDone = true;
            }

            Position posLocalHeights = sim.pos;
            double startElevOverWGS84 = sim.getStartElevOverWGS84();

            Position pos = new Position(posLocalHeights, posLocalHeights.elevation + startElevOverWGS84);
            double altMin = sim.altMinInM + startElevOverWGS84;
            double altMax = sim.altMaxInM + startElevOverWGS84;

            double groundElevationWGS84;
            CompoundElevationModel.ElevationModelRerence elevationModelRerence =
                new CompoundElevationModel.ElevationModelRerence();
            try {
                groundElevationWGS84 =
                    elevationModel.getElevation(
                        pos, true, IElevationModel.MIN_RESOLUTION_REQUEST_METER, elevationModelRerence);
            } catch (ElevationModelRequestException e) {
                tmpSimResult.elevationDataAvaliable = false;
                if (e.isWorst) {
                    return;
                }

                groundElevationWGS84 = e.achievedAltitude;
            }

            SimDistance simDistances = null;

            if (tmpSimResult.startCircleDone) {
                // tmpSimResult.posListAfterStartCircle.add(pos);

                simDistances = new SimDistance();
                simDistances.fpRelObjectHeading = sim.fpCurObj;
                simDistances.elevationSource = new WeakReference<>(elevationModelRerence.elevationModel);
                if (posLastLocalHeights != null) {
                    // find PicArea collisions on the segment posLast to pos
                    simDistances.aoiCollisions.addAll(
                        fp.firstCollisionLineWithAOI(posLastLocalHeights, posLocalHeights, allPicAreas).picAreas);
                    /*System.out.println(
                    "simDistances.firstCollistion:"
                        + (simDistances.firstCollistion != null)
                        + " "
                        + posLast
                        + "  "
                        + pos
                        + "  "
                        + simDistances.firstCollistion);*/
                    if (tmpSimResult.firstFPobj != sim.fpCurObj) {
                        tmpSimResult.aoiCollisions.addAll(simDistances.aoiCollisions);
                    } else {
                        tmpSimResult.aoiCollisionsTakeoff.addAll(simDistances.aoiCollisions);
                    }
                }

                simDistances.position = pos;

                tmpSimResult.minMaxHeightOverTakeoff.update(altMax - startElevOverWGS84);
                tmpSimResult.minMaxHeightOverTakeoff.update(altMin - startElevOverWGS84);
                tmpSimResult.minMaxDistanceToTakeoff.update(sim.vec.distanceTo3(sim.vecStart));

                double startElevEGMoffset = sim.getStartElevEgmOffset();
                tmpSimResult.minMaxDistanceToMSL.update(altMin - startElevEGMoffset);
                tmpSimResult.minMaxDistanceToMSL.update(altMax - startElevEGMoffset);

                if (tmpSimResult.minMaxDistanceToGround.updateMinChanged(altMin - groundElevationWGS84)) {
                    tmpSimResult.worstPostGroundDistance = pos;
                }

                if (tmpSimResult.minMaxDistanceToGround.updateMinChanged(altMax - groundElevationWGS84)) {
                    tmpSimResult.worstPostGroundDistance = pos;
                }

                simDistances.groundDistanceMeter =
                    Math.min(altMin - groundElevationWGS84, altMax - groundElevationWGS84);
                if (simDistances.groundDistanceMeter < IElevationModel.MIN_LEVEL_OVER_GROUND) {
                    simDistances.positionOverGround =
                        new Position(
                            simDistances.position, groundElevationWGS84 + IElevationModel.MIN_LEVEL_OVER_GROUND);
                } else {
                    simDistances.positionOverGround = simDistances.position;
                }
            }
            // tmpSimResult.posList.add(pos);

            double egmOffset = egmModel.getEGM96Offset(pos);
            double elevationEGM = groundElevationWGS84 - egmOffset;
            double flyingAltEGMmax = altMax - egmOffset;

            AirspaceComperatorFloor airspaces =
                new AirspaceComperatorFloor(pos, elevationEGM, tmpSimResult.airspaceList, true, true);
            for (javafx.util.Pair<IAirspace, Double> airspace : airspaces.airspaceAlts) {

                // only check first forbidden one, since they are sorted by altitude
                double distanceToFloorMin = airspace.getValue() - flyingAltEGMmax;
                // System.out.println("egmOffset " + egmOffset);
                if (simDistances != null && simDistances.airspaceDistanceMeter > distanceToFloorMin) {
                    simDistances.airspaceDistanceMeter = distanceToFloorMin;
                    simDistances.lowestAirspace = airspace.getKey();
                }

                if (distanceToFloorMin < tmpSimResult.minDistanceToFloor) {
                    // System.out.println("new lowest: floor:"+ (airspace.floorMeters(latLon,elevationEGM)-flyingAltEGM)
                    // + " -> " +
                    // distanceToFloor + " @ " + airspace);
                    // System.out.println("floorMeters:"+airspace.floorMeters(latLon,elevationEGM));
                    // System.out.println("elevationEGM:"+elevationEGM);
                    // System.out.println("flyingAltEGM:"+flyingAltEGM);
                    // System.out.println("egmOffset:"+egmOffset);
                    // System.out.println();
                    tmpSimResult.lowestAirspace = airspace.getKey();
                    tmpSimResult.minDistanceToFloor = distanceToFloorMin;
                    tmpSimResult.worstPostAispraceDistance = new Position(pos, flyingAltEGMmax);
                }

                break;
            }

            posLastLocalHeights = posLocalHeights;

            if (simDistances != null) {
                tmpSimResult.simDistances.add(simDistances);
            }
        }

        @Override
        public void recv_simulatedFails(Integer failBitMask, Integer debug1, Integer debug2, Integer debug3) {}

        @Override
        public void recv_newPhotoLog(String name, Integer bytes) {}

        @Override
        public void recv_fpSendingStatusChange(FlightPlanStaus fpStatus) {
            // TODO Auto-generated method stub

        }

        @Override
        public void recv_paramsUpdateStatusChange(ParamsUpdateStatus fpStatus) {}

    }

    private boolean tooCloseToTheTakeoff(Position pos) {
        double dist2d =
            Position.ellipsoidalDistance(
                fp.getTakeoffPosition(), pos, Earth.WGS84_EQUATORIAL_RADIUS, Earth.WGS84_POLAR_RADIUS);
        if (Double.isNaN(dist2d)) {
            dist2d = 0;
        }

        double height = fp.getTakeoffPosition().elevation - pos.elevation;

        // this is a cylinder around takeoff point
        if (dist2d < TAKEOFF_RADIUS || Math.abs(height) < TAKEOFF_RADIUS) {
            return true;
        }

        return false;
    };

    SimResultData lastSimResult;
    SimResultData tmpSimResult;

    public FPsim(Flightplan fp) {
        init(runnable);
        // System.out.println("create sim Pair : " + simPair.plane + " "+simPair.fp );

        this.fp = fp;
        sim = new AirplaneSim(localSimListener, false, fp.getHardwareConfiguration());
        sim.sendHealth = false;
        sim.sendDebug = false;
        sim.sendPositionGPSAlt = false;

        fp.addFPChangeListener(this);
        fp.getHardwareConfiguration().addListener(new INotificationObject.WeakChangeListener(this));

        DependencyInjector.getInstance()
            .getInstanceOf(INetworkInformation.class)
            .networkAvailableProperty()
            .addListener(new WeakChangeListener<>(networkBecomesAvailableListener));
        golfChangedNotifcationObserver = (s, objects) -> tryStartRecomp();
        MvvmFX.getNotificationCenter()
            .subscribe(
                EAirspaceManager.GOLF_CHANGED_EVENT, new WeakNotificationObserver(golfChangedNotifcationObserver));
        DependencyInjector.getInstance()
            .getInstanceOf(ISettingsManager.class)
            .getSection(AirspacesProvidersSettings.class)
            .useAirspaceDataForPlanningProperty()
            .addListener(new WeakChangeListener<>(airspaceUseChangeListener));
        EAirspaceManager.instance().addAirspaceListListener(airspaceListener);
        tryStartRecomp();
    }

    public boolean willEverCompute() {
        return (computePreviewSim && !fp.isOnAirFlightplan());
    }

    @Override
    public boolean tryStartRecomp() {
        if (!willEverCompute()) {
            return false;
        }
        // Debug.printStackTrace(""+fp.hashCode());
        return super.tryStartRecomp();
    }

    @Override
    public void endRecomp(long runNo) {
        synchronized (this) {
            tmpSimResult.pic_count = tmpSimResult.photos.size();
            setSimulatedTimeValid(tmpSimResult);
            lastSimResult = tmpSimResult; // publish data with this atomic expression

            if (!tmpSimResult.elevationDataAvaliable && !WorldWind.getNetworkStatus().isNetworkUnavailable()) {
                Debug.getLog().config("recomputeCoverage FPsim " + this + " due to missing elevation model data");
                maybeStartAgainIfNotDoneYet(5000);
            }
        }
        // long ts =System.currentTimeMillis();
        // System.out.println("end remcomp FP:" + ts + "  " + fp.hashCode());
        super.endRecomp(runNo);
    }

    Vector<PicArea> allPicAreas;

    private void simFP() {
        long start = System.currentTimeMillis();
        try {
            tmpSimResult = new SimResultData();

            Sector s = fp.getSector();
            if (s == null) {
                return; // there is nothing defined in this flight plan what can be flewn
            }

            tmpSimResult.airspaceList = EAirspaceManager.instance().getAirspaces(s);
            ExtractTypeVisitor<PicArea> visPic = new ExtractTypeVisitor<>(PicArea.class);

            visPic.startVisit(fp);
            allPicAreas = visPic.filterResults;
            simSingleFP(fp);
            // System.out.println("photosReady:"+tmpSimResult.pic_count);
            // System.out.println("curthread:" + Thread.currentThread());
        } finally {
            updateTimestamp = System.currentTimeMillis();
            Debug.getLog()
                .info(
                    "flightplan simulation recalc Done. "
                        + FPsim.this
                        + ".  It took "
                        + (System.currentTimeMillis() - start) / 1000.
                        + " sec");
        }
    }

    private void simSingleFP(Flightplan fp) {
        // Debug.printStackTrace("sim single FP" , fp);
        tmpSimResult.startCircleDone = false;
        tmpSimResult.firstFPobj = null;
        sim.setNativeHardwareConfiguration(fp.getHardwareConfiguration());
        sim.reset();

        // this ensures to have thread save access to a snapshot of the flight plan, otherwise it might change in the
        // meantime while we are simulating it
        fp = fp.getCopy();

        /*
        if (fp.getHardwareConfiguration().getPlatformDescription().getAirplaneType().isSirius()) {
            sim.setStartpos(fp.getLandingpoint().getLatLon());
        } else {
            FirstWaypointVisitor vis = new FirstWaypointVisitor();
            vis.startVisit(fp);
            if (vis.firstWaypoint == null) {
                return;
            }
            // System.out.println("start;" +vis.firstWaypoint);
            sim.setStartpos(LatLon.fromDegrees(vis.firstWaypoint.getLat(), vis.firstWaypoint.getLon()));

        }*/
        sim.setStartpos(fp.getTakeoff().getLatLon(), fp.getTakeoff().getAltInMAboveFPRefPoint());
        sim.setStartElev(fp.getRefPointAltWgs84WithElevation());

        sim.setStartEgmOffset(fp.getRefPoint().getGeoidSeparation());

        sim.setFlightPlan(fp, 0);
        sim.setFlightPhase(AirplaneFlightphase.airborne);
        sim.run();
        sim.setFlightPhase(AirplaneFlightphase.ground);
        tmpSimResult.flightTime += sim.simTime - sim.simStartTime;
        tmpSimResult.distance += sim.flightDistance;
        // System.out.println("imgCnt " + tmpSimResult.photos.size());
    }

    @Override
    public String toString() {
        return "Sim of: " + fp;
    }

    @Override
    public void flightplanStructureChanged(IFlightplanRelatedObject fp) {
        // System.out.println("FPSim -fpChange4"+fp);
        tryStartRecomp();
    }

    @Override
    public void flightplanValuesChanged(IFlightplanRelatedObject fpObj) {
        // System.out.println("FPSim -fpChange3"+fpObj);
        if (fpObj instanceof IFlightplanLatLonReferenced && !(fpObj instanceof Point)) {
            // while dragging a PicArea Corner we dont need to recompute since this isnt effecting the flightlines
            // IFlightplanLatLonReferenced new_name = (IFlightplanLatLonReferenced) fpObj;
            tryStartRecomp();
        } else if (fpObj instanceof CPhoto) {
            tryStartRecomp();
        } else if (fpObj instanceof PicArea && !fpObj.getFlightplan().getRecalculateOnEveryChange()) {
            // on GSD change we have to update the coverage preview
            tryStartRecomp();
        }
    }

    @Override
    public void flightplanElementRemoved(CFlightplan fp, int i, IFlightplanRelatedObject statement) {
        // System.out.println("FPSim -fpChange2"+fp);
        tryStartRecomp();
    }

    @Override
    public void flightplanElementAdded(CFlightplan fp, IFlightplanRelatedObject statement) {
        // System.out.println("FPSim -fpChange1"+fp);
        tryStartRecomp();
    }

    @Override
    public void propertyChange(INotificationObject.ChangeEvent configurationChangeEvent) {
        // hardware configuration somehow different now
        tryStartRecomp();
    }

    public double getStartElevOverWGS84() {
        return sim.getStartElevOverWGS84();
    }

    public boolean isValidMinGroundDistanceOK() {
        // moved here: CheckMinGroundDistance
        SimResultData simResult = lastSimResult;
        return simResult == null
            || simResult.minMaxDistanceToGround.min
                >= fp.getHardwareConfiguration()
                    .getPlatformDescription()
                    .getMinGroundDistance()
                    .convertTo(Unit.METER)
                    .getValue()
                    .doubleValue();
    }

    public boolean isAirspaceRestrictionsOK() {
        SimResultData simResult = lastSimResult;
        // System.out.println("simResult.minDistanceToFloor:"+simResult.minDistanceToFloor);
        return simResult == null || simResult.lowestAirspace == null || simResult.minDistanceToFloor > 0;
    }

    public String getSimulatedTimeMessage() {
        String TOO_LONG_OVER_SIM_TIME_MAX = "tooLongOverSimTimeMax";
        String TOO_LONG = "tooLong";

        SimResultData simResult = lastSimResult;
        if (simResult == null) {
            return null;
        }

        double flightTime = simResult.flightTime;
        double batteryTime =
            fp.getHardwareConfiguration()
                .getPlatformDescription()
                .getMaxFlightTime()
                .convertTo(Unit.SECOND)
                .getValue()
                .doubleValue();
        if (flightTime >= 0.9999 * AirplaneSim.MAX_SIM_TIME_OVER_BATTERY_TIME * batteryTime) {
            return TOO_LONG_OVER_SIM_TIME_MAX;
        } else if (batteryTime <= flightTime) {
            return TOO_LONG;
        }

        return null;
    }

    private void setSimulatedTimeValid(SimResultData lastSimResult) {
        SimResultData simResult = lastSimResult;
        if (simResult == null) {
            return;
        }

        double flightTime = simResult.flightTime;
        double batteryTime =
            fp.getHardwareConfiguration()
                .getPlatformDescription()
                .getMaxFlightTime()
                .convertTo(Unit.SECOND)
                .getValue()
                .doubleValue();
        if (flightTime >= 0.9999 * AirplaneSim.MAX_SIM_TIME_OVER_BATTERY_TIME * batteryTime) {
            simResult.simulatedTimeValid = false;
            return;
        }

        simResult.simulatedTimeValid = true;
        return;
    }

    public static class SimDistance {
        public IAirspace lowestAirspace;
        public IFlightplanRelatedObject fpRelObjectHeading;
        public Position position;
        public Position positionOverGround;
        public WeakReference<ElevationModel> elevationSource;
        public double groundDistanceMeter;
        public double airspaceDistanceMeter = Double.POSITIVE_INFINITY;
        public Set<PicArea> aoiCollisions = new ArraySet<>();
    }

    public static class SimResultData {

        public boolean elevationDataAvaliable = true;
        public ArrayList<PhotoData> photos = new ArrayList<PhotoData>();
        public ArrayList<SimDistance> simDistances = new ArrayList<>();

        public int pic_count = 0;
        public double distance = 0;
        public double flightTime = 0;

        public Position worstPostGroundDistance = null;
        public IFlightplanPositionReferenced first = null;
        public IFlightplanPositionReferenced last = null;

        public TreeMap<Integer, Double> progressMap = new TreeMap<Integer, Double>();
        public TreeMap<Integer, IFlightplanPositionReferenced> posMap =
            new TreeMap<Integer, IFlightplanPositionReferenced>();

        public boolean startCircleDone = false;
        public IFlightplanRelatedObject firstFPobj = null;
        public MinMaxPair minMaxDistanceToGround = new MinMaxPair();
        public MinMaxPair minMaxDistanceToMSL = new MinMaxPair();
        public MinMaxPair minMaxHeightOverTakeoff = new MinMaxPair();
        public MinMaxPair minMaxDistanceToTakeoff = new MinMaxPair();

        public List<IAirspace> airspaceList;
        public IAirspace lowestAirspace = null;
        public double minDistanceToFloor = Double.POSITIVE_INFINITY;
        public Position worstPostAispraceDistance = null;
        public Set<PicArea> aoiCollisions = new ArraySet<>();
        public Set<PicArea> aoiCollisionsTakeoff = new ArraySet<>();
        public boolean simulatedTimeValid;
    }

    public SimResultData getSimResult() {
        return lastSimResult;
    }

    public SimResultData getSimResultBlocking() {
        SimResultData tmpRes = lastSimResult;
        if (isRunningRecompute()) {
            synchronized (waitMutex) {
                while (tmpRes != lastSimResult) {
                    try {
                        waitMutex.wait();
                    } catch (InterruptedException e) {
                    }
                }
            }
        }

        return lastSimResult;
    }

    public FPsim waitOnceReadyBlocking() {
        SimResultData tmpRes = lastSimResult;
        if (isRunningRecompute()) {
            synchronized (waitMutex) {
                while (tmpRes != lastSimResult) {
                    try {
                        waitMutex.wait();
                    } catch (InterruptedException e) {
                    }
                }
            }
        }

        return this;
    }

    long updateTimestamp;

    public long getUpdateTimestamp() {
        return updateTimestamp;
    }
}
