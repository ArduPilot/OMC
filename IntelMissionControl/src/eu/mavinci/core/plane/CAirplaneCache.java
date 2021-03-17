/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.plane;

import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.settings.OperationLevel;
import com.intel.missioncontrol.ui.notifications.Toast;
import com.intel.missioncontrol.ui.notifications.ToastType;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import eu.mavinci.core.flightplan.AltAssertModes;
import eu.mavinci.core.flightplan.CEvent;
import eu.mavinci.core.flightplan.CEventList;
import eu.mavinci.core.flightplan.CFlightplan;
import eu.mavinci.core.flightplan.CPreApproach;
import eu.mavinci.core.flightplan.CWaypoint;
import eu.mavinci.core.flightplan.FlightplanFactory;
import eu.mavinci.core.flightplan.IFlightplanContainer;
import eu.mavinci.core.flightplan.IFlightplanPositionReferenced;
import eu.mavinci.core.flightplan.IFlightplanRelatedObject;
import eu.mavinci.core.flightplan.IReentryPoint;
import eu.mavinci.core.flightplan.LandingModes;
import eu.mavinci.core.flightplan.ReentryPointID;
import eu.mavinci.core.flightplan.visitors.AFlightplanVisitor;
import eu.mavinci.core.flightplan.visitors.ExtractByIdVisitor;
import eu.mavinci.core.helper.AMeanEstimater;
import eu.mavinci.core.helper.IGeoFenceDetector;
import eu.mavinci.core.helper.IterativeEstimater;
import eu.mavinci.core.listeners.IListener;
import eu.mavinci.core.plane.listeners.IAirplaneFlightPlanSendingListener;
import eu.mavinci.core.plane.listeners.IAirplaneListenerAllExternal;
import eu.mavinci.core.plane.listeners.IAirplaneListenerAndroidState;
import eu.mavinci.core.plane.listeners.IAirplaneListenerBackend;
import eu.mavinci.core.plane.listeners.IAirplaneListenerConfig;
import eu.mavinci.core.plane.listeners.IAirplaneListenerConnectionEstablished;
import eu.mavinci.core.plane.listeners.IAirplaneListenerConnectionState;
import eu.mavinci.core.plane.listeners.IAirplaneListenerExpertSimulatedFails;
import eu.mavinci.core.plane.listeners.IAirplaneListenerFixedOrientation;
import eu.mavinci.core.plane.listeners.IAirplaneListenerFlightPlanASM;
import eu.mavinci.core.plane.listeners.IAirplaneListenerFlightPlanXML;
import eu.mavinci.core.plane.listeners.IAirplaneListenerFlightphase;
import eu.mavinci.core.plane.listeners.IAirplaneListenerHealth;
import eu.mavinci.core.plane.listeners.IAirplaneListenerIsSimulation;
import eu.mavinci.core.plane.listeners.IAirplaneListenerLinkInfo;
import eu.mavinci.core.plane.listeners.IAirplaneListenerName;
import eu.mavinci.core.plane.listeners.IAirplaneListenerOrientation;
import eu.mavinci.core.plane.listeners.IAirplaneListenerPhoto;
import eu.mavinci.core.plane.listeners.IAirplaneListenerPhotoLogName;
import eu.mavinci.core.plane.listeners.IAirplaneListenerPlaneInfo;
import eu.mavinci.core.plane.listeners.IAirplaneListenerPosition;
import eu.mavinci.core.plane.listeners.IAirplaneListenerSimulationSettings;
import eu.mavinci.core.plane.listeners.IAirplaneListenerStartPos;
import eu.mavinci.core.plane.listeners.IAirplaneParamsUpdateListener;
import eu.mavinci.core.plane.management.BackendState;
import eu.mavinci.core.plane.management.CAirport;
import eu.mavinci.core.plane.sendableobjects.AndroidState;
import eu.mavinci.core.plane.sendableobjects.Backend;
import eu.mavinci.core.plane.sendableobjects.BackendInfo;
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
import eu.mavinci.core.plane.sendableobjects.SingleHealthDescription;
import eu.mavinci.desktop.main.debug.Debug;
import eu.mavinci.flightplan.LandingPoint;
import java.util.HashMap;
import java.util.Vector;
import java.util.logging.Level;

public class CAirplaneCache implements IAirplaneListenerAllExternal, IAirplaneListenerConnectionState {

    protected String lastPhotoLogName = null;
    protected Integer lastPhotoBytes = null;

    protected double becomeAirborneTime = -2.;
    protected double becomeLandingTime = -2.;

    protected int lastGroundMode = 2; // 1 = ground, 0 = airborne, 2 = unknown...

    protected Float fixedOrientationRoll = null;
    protected Float fixedOrientationPitch = null;
    protected Float fixedOrientationYaw = null;

    protected Double curTime = null;
    protected Double curLat = null;
    protected Double curLon = null;
    protected Float curAltAboveStart = null;

    protected Double travelDistance = 0.;

    protected ICAirplane plane;

    protected PlaneInfo planeInfo = null;
    protected APTypes apType = null;
    protected BackendInfo backendInfo = null;

    protected Config_variables conf = null;
    protected AirplaneFlightphase flightPhase = null;
    protected Boolean isSimulation = null;
    protected String name = null;
    protected OrientationData orientation = null;
    protected PositionOrientationData positionOrientation = null;
    protected DebugData debugData = null;

    protected PositionData position = null;
    protected String flightplanASM = null;
    protected Boolean isFPreentryASM = null;
    protected Boolean isSucceedASM = null;
    protected String flightplanXML = null;
    protected Boolean isFPreentryXML = null;
    protected Boolean isSucceedXML = null;
    protected Double startLon = null;
    protected Double startLat = null;
    protected Double startLonBaro = null;
    protected Double startLatBaro = null;
    protected Float simSpeed = null;
    protected SimulationSettings simSettings = null;
    protected PhotoData lastPhoto = null;
    protected AirplaneFlightmode flightMode = null;

    protected Double startTime = null;

    protected HealthData healthD = null;

    protected boolean isWriteable = false;
    protected boolean isReadable = false;

    protected BackendState backendStateObj = null;
    protected Backend backend = null;
    protected MVector<Port> backendPorts = null;

    protected ConnectionLostReasons conLostReason = null;

    protected boolean isStartingpositionTemorarily = false;

    protected Integer simFail_failBitMask;
    protected Integer simFail_debug1;
    protected Integer simFail_debug2;
    protected Integer simFail_debug3;

    protected CEvent lastFailEvent;

    public CEvent getLastFailEvent() throws AirplaneCacheEmptyException {
        if (lastFailEvent == null) {
            throw new AirplaneCacheEmptyException();
        }

        return lastFailEvent;
    }

    public String getScreenName() throws AirplaneCacheEmptyException {
        try {
            return getName();
        } catch (AirplaneCacheEmptyException e) {
        }

        return "port:" + getPlanePort();
    }

    public String getFileName() throws AirplaneCacheEmptyException {
        return getScreenName();
    }

    /**
     * @return true if the currently cached startingposition (if there is one) wasn't send by the plane but was replaced
     *     temporarily by the first received position
     */
    public boolean isStartingpositionTemorarily() {
        return isStartingpositionTemorarily;
    }

    public BackendState getBackendState() throws AirplaneCacheEmptyException {
        backendStateObj = null;
        // try to identificate the belonging backend to this connection

        if (plane.isWriteable()) { // but only if this isn't a log replay...
            backendStateObj = CAirport.getInstance().findBackendState(getBackendInfo().serialNumber);
        }

        if (backendStateObj == null) {
            throw new AirplaneCacheEmptyException();
        }

        return backendStateObj;
    }

    public BackendState getBackendStateOffline() throws AirplaneCacheEmptyException {
        backendStateObj = null;
        // try to identificate the belonging backend to this connection

        backendStateObj = CAirport.getInstance().findBackendState(getBackendInfo().serialNumber);

        if (backendStateObj == null) {
            throw new AirplaneCacheEmptyException();
        }

        return backendStateObj;
    }

    public Backend getBackend() throws AirplaneCacheEmptyException {
        if (backend == null) {
            throw new AirplaneCacheEmptyException();
        }

        return backend;
    }

    public MVector<Port> getBackendPorts() throws AirplaneCacheEmptyException {
        if (backendPorts == null) {
            throw new AirplaneCacheEmptyException();
        }

        return backendPorts;
    }

    public CAirplaneCache(final ICAirplane plane) {
        this.plane = plane;
        reset();
    }

    public boolean isWriteable() {
        return plane.isWriteable();
    }

    public boolean isReadable() {
        return plane.isReadable();
    }

    public AirplaneConnectorState getConnectionState() {
        return plane.getConnectionState();
    }

    public Double getCurTime() throws AirplaneCacheEmptyException {
        if (curTime == null) {
            throw new AirplaneCacheEmptyException();
        }

        return curTime;
    }

    public Config_variables getConf() throws AirplaneCacheEmptyException {
        if (conf == null) {
            throw new AirplaneCacheEmptyException();
        }

        return conf;
    }

    public AirplaneFlightmode getFlightMode() throws AirplaneCacheEmptyException {
        if (flightMode == null) {
            throw new AirplaneCacheEmptyException();
        }

        return flightMode;
    }

    public AirplaneFlightphase getFlightPhase() throws AirplaneCacheEmptyException {
        if (flightPhase == null) {
            throw new AirplaneCacheEmptyException();
        }

        return flightPhase;
    }

    public Boolean isSimulation() throws AirplaneCacheEmptyException {
        if (isSimulation == null) {
            throw new AirplaneCacheEmptyException();
        }

        return isSimulation;
    }

    public String getName() throws AirplaneCacheEmptyException {
        if (name == null) {
            throw new AirplaneCacheEmptyException();
        }

        return name;
    }

    public OrientationData getOrientation() throws AirplaneCacheEmptyException {
        if (orientation == null) {
            throw new AirplaneCacheEmptyException();
        }

        return orientation;
    }

    public PositionData getPosition() throws AirplaneCacheEmptyException {
        if (position == null) {
            throw new AirplaneCacheEmptyException();
        }

        return position;
    }

    public String getFlightplanASM() throws AirplaneCacheEmptyException {
        if (flightplanASM == null) {
            throw new AirplaneCacheEmptyException();
        }

        return flightplanASM;
    }

    public String getFlightplanXML() throws AirplaneCacheEmptyException {
        if (flightplanXML == null) {
            throw new AirplaneCacheEmptyException();
        }

        return flightplanXML;
    }

    public Boolean isFlighplanReentryASM() throws AirplaneCacheEmptyException {
        if (isFPreentryASM == null) {
            throw new AirplaneCacheEmptyException();
        }

        return isFPreentryASM;
    }

    public Boolean isFlighplanReentryXML() throws AirplaneCacheEmptyException {
        if (isFPreentryXML == null) {
            throw new AirplaneCacheEmptyException();
        }

        return isFPreentryXML;
    }

    public Boolean isFlightplanSucceedASM() throws AirplaneCacheEmptyException {
        if (isSucceedASM == null) {
            throw new AirplaneCacheEmptyException();
        }

        return isSucceedASM;
    }

    public Boolean isFlightplanSucceedXML() throws AirplaneCacheEmptyException {
        if (isSucceedXML == null) {
            throw new AirplaneCacheEmptyException();
        }

        return isSucceedXML;
    }

    public Double getStartLon() throws AirplaneCacheEmptyException {
        if (startLon == null) {
            throw new AirplaneCacheEmptyException();
        }

        return startLon;
    }

    public Double getStartLat() throws AirplaneCacheEmptyException {
        if (startLat == null) {
            throw new AirplaneCacheEmptyException();
        }

        return startLat;
    }

    public Double getStartLonBaro() throws AirplaneCacheEmptyException {
        if (startLonBaro == null) {
            throw new AirplaneCacheEmptyException();
        }

        return startLonBaro;
    }

    public Double getStartLatBaro() throws AirplaneCacheEmptyException {
        if (startLatBaro == null) {
            throw new AirplaneCacheEmptyException();
        }

        return startLatBaro;
    }

    public ICAirplane getPlane() {
        return plane;
    }

    public Float getSimulationSpeed() throws AirplaneCacheEmptyException {
        if (simSpeed == null) {
            throw new AirplaneCacheEmptyException();
        }

        return simSpeed;
    }

    public SimulationSettings getSimulationSettings() throws AirplaneCacheEmptyException {
        if (simSettings == null) {
            throw new AirplaneCacheEmptyException();
        }

        return simSettings;
    }

    public PhotoData getPhoto() throws AirplaneCacheEmptyException {
        if (lastPhoto == null) {
            throw new AirplaneCacheEmptyException();
        }

        return lastPhoto;
    }

    public Double getStartTime() throws AirplaneCacheEmptyException {
        if (startTime == null) {
            throw new AirplaneCacheEmptyException();
        }

        return startTime;
    }

    public synchronized HealthData getHealthData() throws AirplaneCacheEmptyException {
        if (healthD == null) {
            throw new AirplaneCacheEmptyException();
        }

        return healthD;
    }

    public Float getFixedOrientationRoll() throws AirplaneCacheEmptyException {
        if (fixedOrientationRoll == null) {
            throw new AirplaneCacheEmptyException();
        }

        return fixedOrientationRoll;
    }

    public Float getFixedOrientationPitch() throws AirplaneCacheEmptyException {
        if (fixedOrientationPitch == null) {
            throw new AirplaneCacheEmptyException();
        }

        return fixedOrientationPitch;
    }

    public Float getFixedOrientationYaw() throws AirplaneCacheEmptyException {
        if (fixedOrientationYaw == null) {
            throw new AirplaneCacheEmptyException();
        }

        return fixedOrientationYaw;
    }

    public PositionOrientationData getPositionOrientation() throws AirplaneCacheEmptyException {
        if (positionOrientation == null) {
            throw new AirplaneCacheEmptyException();
        }

        return positionOrientation;
    }

    public DebugData getDebugData() throws AirplaneCacheEmptyException {
        if (debugData == null) {
            throw new AirplaneCacheEmptyException();
        }

        return debugData;
    }

    public Double getTargetAltitude() throws AirplaneCacheEmptyException {
        // target altitude is 0 during descending or on ground
        try {
            if (getFlightPhase().isGroundTarget()) {
                return 0.;
            }
        } catch (AirplaneCacheEmptyException e) {
        }

        if (targetAltitude == null) {
            throw new AirplaneCacheEmptyException();
        }

        if (Double.isNaN(targetAltitude)) {
            // linear interpolation
            if (currentReentrypoint instanceof IFlightplanPositionReferenced && lastPositionReferencedWP != null) {
                IFlightplanPositionReferenced currentWP = (IFlightplanPositionReferenced)currentReentrypoint;
                IFlightplanPositionReferenced lastWP = lastPositionReferencedWP;

                double length =
                    CAirplaneCache.distanceMeters(
                        currentWP.getLat(), currentWP.getLon(), lastWP.getLat(), lastWP.getLon());
                double lengthRemaining =
                    CAirplaneCache.distanceMeters(curLat, curLon, currentWP.getLat(), currentWP.getLon());
                double percentage = lengthRemaining / length;

                // System.out.println("percentage : " + percentage);

                if (percentage > 1) {
                    percentage = 1;
                } else if (percentage < 0) {
                    percentage = 0;
                }

                if (currentWP instanceof LandingPoint) {
                    LandingPoint lp = (LandingPoint)currentWP;
                    if (lp.getMode() == LandingModes.DESC_FULL3d) {
                        return lp.getAltBreakoutWithinM()
                            - (lp.getAltBreakoutWithinM() - lastWP.getAltInMAboveFPRefPoint()) * percentage;
                    }
                }

                return currentWP.getAltInMAboveFPRefPoint()
                    - (currentWP.getAltInMAboveFPRefPoint() - lastWP.getAltInMAboveFPRefPoint()) * percentage;
            }

            throw new AirplaneCacheEmptyException();
        }

        return targetAltitude;
    }

    public void recv_config(Config_variables c) {
        conf = c;
    }

    public synchronized void recv_flightPhase(Integer fp) {
        AirplaneFlightphase newPhase = AirplaneFlightphase.values()[fp];
        int newGroundMode = newPhase.isFlightphaseOnGround();

        // geofencing stuff
        if (newPhase == AirplaneFlightphase.areaRestricted) {
            DependencyInjector.getInstance().getInstanceOf(IGeoFenceDetector.class).setGeoFencingRestrictionOn();
        }

        // initialize stuff, if it is not already..
        if (newGroundMode != 2 && lastGroundMode == 2) {
            lastGroundMode = newGroundMode;
            // if (flightPhase == null) flightPhase = newPhase; //MM this line seems totally depricated and wrong!
        }

        if ((1 == lastGroundMode && 1 != newGroundMode)
                || (newGroundMode == 0 && becomeAirborneTime == -2)) { // from ground -> unknown /
            // transient
            becomeAirborneTime = -1.;
            travelDistance = 0.;
            // System.out.println("resetting times:" + "lastPhase:" + flightPhase + " newPhase" + newPhase);
        }

        if ((1 != lastGroundMode && 1 == newGroundMode)
                || (newGroundMode == 1 && becomeLandingTime == -2)) { // from ground -> unknown /
            // transient
            becomeLandingTime = -1;
            // System.out.println("resetting becomeLandingTime times:" + "lastPhase:" + flightPhase + " newPhase" +
            // newPhase);
        }

        if (newGroundMode != 2) {
            lastGroundMode = newGroundMode;
        }

        if (flightPhase == newPhase) {
            return;
        }

        flightPhase = newPhase;
        getPlane().getRootHandler().recv_flightPhase(fp);

        if (flightPhase == AirplaneFlightphase.gpsloss && healthChannelGPS >= 0 && healthD != null) {
            if (!healthD.absolute.get(healthChannelGPS).equals(0f)) {
                // force health data patching
                getPlane().getRootHandler().recv_health(healthD);
            }
        }
    }

    public void recv_isSimulation(Boolean simulation) {
        isSimulation = simulation;
    }

    public void recv_nameChange(String name) {
        this.name = name;
    }

    public void recv_orientation(OrientationData o) {
        orientation = o;
    }

    /**
     * Returns the time since the plane become airborne
     *
     * @return
     */
    public double getAirborneTime() {
        // System.out.println("curTime:"+curTime+ " becomeAirborneTime:"+becomeAirborneTime);
        if (curTime == null) {
            return 0;
        }

        if (becomeAirborneTime < 0) {
            return 0;
        }

        if (becomeLandingTime > becomeAirborneTime) {
            // System.out.println("use landing time:"+becomeLandingTime + " becomeAirborneTime:"+becomeAirborneTime);
            return becomeLandingTime - becomeAirborneTime;
        }

        return curTime - becomeAirborneTime;
    }

    public double getBecomeLandingTime() {
        return becomeLandingTime;
    }

    public double getBecomeAirborneTime() {
        return becomeAirborneTime;
    }

    Double targetAltitude = null;
    IFlightplanPositionReferenced lastPositionReferencedWP = null;
    IReentryPoint currentReentrypoint = null;
    // IReentryPoint lastReentrypoint = null;
    Integer currentReentrypointID = null;

    public IReentryPoint getCurrentReentrypoint() throws AirplaneCacheEmptyException {
        if (currentReentrypoint == null) {
            throw new AirplaneCacheEmptyException();
        }

        return currentReentrypoint;
    }

    public Integer getCurrentReentrypointID() throws AirplaneCacheEmptyException {
        if (currentReentrypointID == null) {
            throw new AirplaneCacheEmptyException();
        }

        return currentReentrypointID;
    }

    /**
     * Returns the Distance in meters since the flightmode changed from a ground flightmode to a airborne flightmode
     *
     * @return
     */
    public double getAirborneDistance() {
        return travelDistance;
    }

    AMeanEstimater startAltEstimater = new IterativeEstimater(100);

    /**
     * Returns an estimation of the altitude of the starting point in meters over seelevel.
     *
     * @return
     */
    public double getStartAltitudeEstimation() throws AirplaneCacheEmptyException {
        if (startAltEstimater.getCurrentCount() == 0) {
            throw new AirplaneCacheEmptyException();
        }

        return startAltEstimater.getMean();
    }

    /**
     * airdistance support using Haversine formula var R = 6371; // km var dLat = (lat2-lat1).toRad(); var dLon =
     * (lon2-lon1).toRad(); var a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.cos(lat1.toRad()) *
     * Math.cos(lat2.toRad()) * Math.sin(dLon/2) * Math.sin(dLon/2); var c = 2 * Math.atan2(Math.sqrt(a),
     * Math.sqrt(1-a)); var d = R * c;
     *
     * @param lat1
     * @param lon1
     * @param lat2
     * @param lon2
     * @return
     */
    public static double distanceMeters(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371000; // earth radius in m
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a =
            Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                    * Math.cos(Math.toRadians(lat2))
                    * Math.sin(dLon / 2)
                    * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double d = R * c;
        // System.out.println(d);
        return d;
    }

    /**
     * Computes 3d distance of two positions
     *
     * @param lat1
     * @param lon1
     * @param alt1
     * @param lat2
     * @param lon2
     * @param alt2
     * @return
     */
    public static double distanceMeters(double lat1, double lon1, double alt1, double lat2, double lon2, double alt2) {
        double R = 6371000; // earth radius in m
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a =
            Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                    * Math.cos(Math.toRadians(lat2))
                    * Math.sin(dLon / 2)
                    * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double d = R * c;
        // System.out.println(d);
        double dAlt = alt1 - alt2;
        return Math.sqrt(d * d + dAlt * dAlt);
    }

    public void recv_position(PositionData p) {
        if (!p.fromSession && !p.synthezided) {
            estimateSpeed(p.lat, p.lon, p.altitude / 100., p.getTimestamp());
        }

        if (p.groundspeed == 0 && lastSpeedEstimation >= 0) {
            p.groundspeed = (int)Math.round(lastSpeedEstimation * 100);
        }

        if (startLonBaro == null && startLatBaro == null && p.lat != 0 && p.lon != 0) {
            Debug.getLog().log(Level.CONFIG, "No valid starting position received until now, using current position");
            recv_startPos(p.lon, p.lat, 1); // if no valid startpos is received until now, use the current position!
            isStartingpositionTemorarily = true;
        }

        curTime = p.getTimestamp();
        if (startTime == null) {
            startTime = curTime;
        }

        position = p;

        // airborne Time support
        if (becomeAirborneTime == -1.) {
            // System.out.println("setAirborneTime");
            becomeAirborneTime = curTime;
        }

        if (becomeLandingTime == -1.) {
            // System.out.println("setLandingTime");
            becomeLandingTime = curTime;
        }

        // airdistance support using Haversine formula
        if (curLat != null
                && curLon != null
                && curAltAboveStart != null
                && becomeAirborneTime > -1
                && becomeLandingTime <= becomeAirborneTime) {
            // System.out.println("add ");
            travelDistance += distanceMeters(p.lat, p.lon, p.altitude / 100f, curLat, curLon, curAltAboveStart);
            // System.out.println("new travDist"+ travelDistance);
        }

        curLat = p.lat;
        curLon = p.lon;
        curAltAboveStart = p.altitude / 100f;

        // Position pOverStart = new Position(curLatLon, p.altitude/100.);
        // Vec4 vn = EarthElevationModel.model.getGlobe().computePointFromPosition(pOverStart);
        // if (lastPosVec != null && lastGroundMode==0) travelDistance += lastPosVec.distanceTo3(vn);
        // lastPosVec = vn;

        // last reentrypoint support
        if (currentReentrypointID == null || !ReentryPointID.equalIDexceptCell(currentReentrypointID, p.reentrypoint)) {
            // System.out.println("THINGS TO CHECK: ReentryID: cur, new: " + currentReentrypointID, p.reentrypoint);
            currentReentrypointID = p.reentrypoint;
            // System.out.println("reentrypoint="+p.reentrypoint);
            ExtractByIdVisitor vis = plane.getFPmanager().getOnAirFlightplan().getStatementById(p.reentrypoint);

            currentReentrypoint = vis.rp;
            lastPositionReferencedWP = vis.posRefBefore;
            // System.out.println("obj="+currentReentrypoint);
            // targetAltitude support

            boolean updated = false;
            if (currentReentrypoint instanceof IFlightplanPositionReferenced
                    && !(currentReentrypoint instanceof LandingPoint)
                    && !(currentReentrypoint instanceof CPreApproach)) {
                IFlightplanPositionReferenced wp = (IFlightplanPositionReferenced)currentReentrypoint;
                targetAltitude = wp.getAltInMAboveFPRefPoint();
                // System.out.println("targAlt="+targetAltitude);
                updated = true;
            }

            if (currentReentrypoint instanceof CWaypoint && lastPositionReferencedWP != null) {
                CWaypoint wp = (CWaypoint)currentReentrypoint;
                if (wp.getAssertAltitudeMode() == AltAssertModes.linear) {
                    // System.out.println("new linear WP:" + wp );
                    targetAltitude = Double.NaN;
                    updated = true;
                }
            }

            if (!updated && lastPositionReferencedWP != null) {
                targetAltitude = lastPositionReferencedWP.getAltInMAboveFPRefPoint();
                if (currentReentrypoint instanceof LandingPoint) {
                    LandingPoint lp = (LandingPoint)currentReentrypoint;
                    if (lp.getMode() == LandingModes.DESC_FULL3d) {
                        targetAltitude = Double.NaN;
                        updated = true;
                    }
                }
            }

            // update reached map
            reachdVisitor.startVisit(plane.getFPmanager().getOnAirFlightplan());
            reachdVisitor.startVisit(plane.getFPmanager().onAirRelatedLocalFP);
        }

        startAltEstimater.pushValue((p.gpsAltitude - p.altitude) / 100d);
        plane.getFPmanager().getOnAirFlightplan().resetDistanceVisitorCache();

        recv_flightmode(p.flightmode);
        recv_flightPhase(p.flightphase);
    }

    AFlightplanVisitor reachdVisitor =
        new AFlightplanVisitor() {

            boolean isNotReachedJet;

            public void preVisit() {
                isNotReachedJet = false;
            }

            public boolean visit(IFlightplanRelatedObject fpObj) {
                if (fpObj instanceof IReentryPoint) {
                    IReentryPoint rp = (IReentryPoint)fpObj;

                    if (ReentryPointID.equalIDexceptCell(currentReentrypointID, rp.getId())) {
                        isNotReachedJet = true;
                    }
                }

                if (!(fpObj instanceof IFlightplanContainer)) {
                    reachedMap.put(fpObj, isNotReachedJet);
                }

                return false;
            }

            public boolean visitExit(IFlightplanRelatedObject fpObj) {
                if (fpObj instanceof IFlightplanContainer) {
                    reachedMap.put(fpObj, isNotReachedJet);
                }

                return false;
            };
        };

    protected HashMap<IFlightplanRelatedObject, Boolean> reachedMap = new HashMap<IFlightplanRelatedObject, Boolean>();

    public Boolean isFPelementNotReachedJet(IFlightplanRelatedObject rp) {
        return reachedMap.get(rp);
    }

    public void recv_setFlightPlanASM(String plan, Boolean reentry, Boolean succeed) {
        flightplanASM = plan;
        this.isFPreentryASM = reentry;
        this.isSucceedASM = succeed;

        targetAltitude = null;
        currentReentrypoint = null;
        currentReentrypointID = null;
        reachedMap.clear();
        try {
            recv_position(getPosition());
        } catch (AirplaneCacheEmptyException e) {
        }
    }

    public void recv_setFlightPlanXML(String plan, Boolean reentry, Boolean succeed) {
        flightplanXML = plan;
        this.isFPreentryXML = reentry;
        this.isSucceedXML = succeed;

        targetAltitude = null;
        lastPositionReferencedWP = null;
        currentReentrypoint = null;
        currentReentrypointID = null;
        reachedMap.clear();
        try {
            recv_position(getPosition());
        } catch (AirplaneCacheEmptyException e) {
        }
    }

    public void recv_simulationSpeed(Float speed) {
        simSpeed = speed;
    }

    public void err_backendConnectionLost(ConnectionLostReasons reason) {
        isWriteable = false;
        isReadable = false;
        planePort = null; // otherwise it will cause at reconnections the new TCPConnector to think the connection is
        // established, even he
        // hasend send all his stuff.
        conLostReason = reason;
    }

    AMeanEstimater meanEstBattVolt = new IterativeEstimater(30);
    AMeanEstimater meanEstBattPercent = new IterativeEstimater(30);

    HealthData lastHealthLowpassBat = null;

    public synchronized void recv_health(HealthData d) {
        if (healthExpectedSize != d.absolute.size()) {
            healthChannelMainBattery = -1;
            healthChannelConnectorBattery = -1;
            healthChannelGPS = -1;
        }

        if (flightPhase != null && flightPhase == AirplaneFlightphase.gpsloss && healthChannelGPS >= 0) {
            d.absolute.set(healthChannelGPS, 0f);
            d.percent.set(healthChannelGPS, 0f);
        }
        // lowpass main UAV battery stuff
        if (healthChannelMainBattery >= 0 && lastHealthLowpassBat != d) {
            lastHealthLowpassBat = d;
            //System.out.println("Battery raw:" +d.absolute.get(healthChannelMainBattery) + "  " + d.percent.get(healthChannelMainBattery));
            meanEstBattVolt.pushValue(d.absolute.get(healthChannelMainBattery));
            meanEstBattPercent.pushValue(d.percent.get(healthChannelMainBattery));

            // System.out.println("\nV: " +d.absolute.get(healthChannelMainBattery)+ " -> "+meanEstBattVolt.getMean());
            // System.out.println("%: " +d.percent.get(healthChannelMainBattery)+ " -> "+meanEstBattPercent.getMean());
            //TODO: Is this necessary. I commented this because it is not giving instantaneous values and is updating the battery values with avergae values.
            //d.absolute.set(healthChannelMainBattery, (float)meanEstBattVolt.getMean());
            //d.percent.set(healthChannelMainBattery, (float)meanEstBattPercent.getMean());
            //System.out.println("Battery avg:" +d.absolute.get(healthChannelMainBattery) + "  " + d.percent.get(healthChannelMainBattery));
        }
        // System.out.println("healthChannelFailevents:"+healthChannelFailevents);
        if (healthChannelFailevents >= 0) {
            // #define ACTION_NONE -1
            // #define ACTION_IGNORE 0
            // #define ACTION_CIRCLEDOWN 1
            // #define ACTION_POSITIONHOLD 2
            //
            // #define ACTION_RETURNTOSTART 3
            // #define ACTION_JUMPLANDING 4
            //
            // #define FAIL_NONE 0
            // #define FAIL_GPS 1
            // #define FAIL_RC 2
            // #define FAIL_DATA 4
            // #define FAIL_RCDATA 8
            //
            //
            // f = (event_action_reason<<1) | ((event_action_running+2)<<8) | (event_action_recover?1:0);
            float combinedF = d.absolute.get(healthChannelFailevents);
            int combined = Math.round(combinedF);
            int event_action_reason = (combined >> 1) & 0x00007F;
            int event_action_running = (combined >> 8) - 2;
            boolean event_action_recover = (combined & 1) == 1;
            String name = null;
            AirplaneEventActions action = null;
            switch (event_action_reason) {
            case 1:
                name = CEventList.NAME_GPSLOSS;
                break;
            case 2:
                name = CEventList.NAME_RCLOSS;
                break;
            case 4:
                name = CEventList.NAME_DATALOSS;
                break;
            case 8:
                name = CEventList.NAME_RCDATALOSS;
                break;
            }

            switch (event_action_running) {
            case -1:
            case 0:
                action = AirplaneEventActions.ignore;
                break;
            case 1:
                action = AirplaneEventActions.circleDown;
                break;
            case 2:
                action = AirplaneEventActions.positionHold;
                break;
            case 3:
                action = AirplaneEventActions.returnToStart;
                break;
            case 4:
                action = AirplaneEventActions.jumpLanging;
                break;
            }
            // System.out.println(" ==> combined: "+ combined + " -> name:"+name + " action:"+action + "
            // recover:"+event_action_recover);
            if (name != null) {
                lastFailEvent = FlightplanFactory.getFactory().newCEvent(null, name);
                lastFailEvent.setAction(action);
                lastFailEvent.setRecover(event_action_recover);
            } else {
                lastFailEvent = null;
            }
        }

        healthD = d;
    }

    public void recv_msg(Integer lvl, String data) {}

    public boolean hasWarnedCompatibility = false;

    public synchronized void recv_backend(Backend host, MVector<Port> ports) {
        if (!host.isCompatible()) {
            // System.out.println("host="+host);
            // System.out.println("host.info"+host.info);
            if (plane.isWriteable()) { // don't be soo strict if this is only a log replay!
                if (DependencyInjector.getInstance()
                            .getInstanceOf(ISettingsManager.class)
                            .getSection(GeneralSettings.class)
                            .getOperationLevel()
                        != OperationLevel.DEBUG) {
                    plane.getRootHandler().err_backendConnectionLost(ConnectionLostReasons.WRONG_AP_RELEASE);
                    throw new RuntimeException(
                        "Backend has other Release Version than Open Mission Control. Connection closed!");
                }
            } else {
                if (!hasWarnedCompatibility) {
                    DependencyInjector.getInstance()
                        .getInstanceOf(IApplicationContext.class)
                        .addToast(
                            Toast.of(ToastType.ALERT)
                                .setText(
                                    "The release of the autopilot where the session is (or was) connected to or the act. replayed logfile comes from ("
                                        + host.info.getHumanReadableSWversion()
                                        + ") is not equal to your version of Open Mission Control. This may lead to compatibility problems.")
                                .create());
                    hasWarnedCompatibility = true;
                }
            }
        }

        // if (backendInfo == null) (new Exception("writing backend info to cache
        // "+host.info.releaseVersion)).printStackTrace();
        backend = host;
        if (healthD != null && healthChannelConnectorBattery != -1) {
            float oldVal = healthD.absolute.get(healthChannelConnectorBattery);
            healthD.absolute.set(healthChannelConnectorBattery, (float)backend.batteryVoltage);
            if (Math.abs(oldVal - backend.batteryVoltage) > 0.05) {
                healthD.percent.set(healthChannelConnectorBattery, PlaneConstants.minLevelForValidPercent - 1);
                lastHealthLowpassBat = null;
                getPlane().getRootHandler().recv_health(healthD);
            }
        }

        backendInfo = host.info;
        backendPorts = ports;

        // System.out.println("CAirplaneCache:newInfo"+host.info );
    }

    public void recv_fixedOrientation(Float roll, Float pitch, Float yaw) {
        fixedOrientationPitch = pitch;
        fixedOrientationRoll = roll;
        fixedOrientationYaw = yaw;
    }

    public void recv_startPos(Double lon, Double lat, Integer pressureZero) {
        if (lon == 0 && lat == 0) {
            Debug.getLog().log(Level.SEVERE, "Received Starting Position (0, 0) and discard it.");
            return;
        }

        if (pressureZero == 1) {
            startLatBaro = lat;
            startLonBaro = lon;
        } else {
            startLat = lat;
            startLon = lon;
        }

        if (isStartingpositionTemorarily) {
            Debug.getLog().log(Level.CONFIG, "Valid starting position received! Replacing temporarily one.");
            isStartingpositionTemorarily = false;
        }

        this.lastSpeedEstimationTime = -1;
    }

    public ConnectionLostReasons getLastConnectionLostReason() {
        return conLostReason;
    }

    public void rawDataFromBackend(String line) {}

    public void rawDataToBackend(String line) {}

    public void replayPaused(boolean paused) {}

    public void replayStopped(boolean stopped) {}

    public void recv_photo(PhotoData photo) {
        lastPhoto = photo;
    }

    public void replayFinished() {}

    public void recv_powerOn() {
        // reset everything!
        reset();

        plane.requestAll();
    }

    public synchronized void reset() {
        // (new Exception("resetting cache")).printStackTrace();
        hasWarnedCompatibility = false;
        isStartingpositionTemorarily = false;

        lastPhotoLogName = null;
        lastPhotoBytes = null;

        lastHealthLowpassBat = null;
        meanEstBattPercent.reset();
        meanEstBattVolt.reset();

        // System.out.println("resetting becomeTimes");
        becomeAirborneTime = -2.;
        becomeLandingTime = -2.;

        lastGroundMode = 2;

        fixedOrientationRoll = null;
        fixedOrientationPitch = null;
        fixedOrientationYaw = null;

        currentReentrypointID = null;
        currentReentrypoint = null;
        targetAltitude = null;
        lastPositionReferencedWP = null;
        curTime = null;

        linkInfo = null;
        fpStatus = null;
        planeInfo = null;
        apType = null;
        backendInfo = null;
        conf = null;
        flightPhase = null;
        flightMode = null;
        isSimulation = null;
        name = null;
        orientation = null;
        position = null;
        debugData = null;
        positionOrientation = null;
        flightplanASM = null;
        isFPreentryASM = null;
        isSucceedASM = null;
        flightplanXML = null;
        isFPreentryXML = null;
        isSucceedXML = null;
        startLon = null;
        startLat = null;
        startLonBaro = null;
        startLatBaro = null;
        curAltAboveStart = null;

        simSpeed = null;
        simSettings = null;
        lastPhoto = null;
        startTime = null;

        curLat = null;
        curLon = null;

        travelDistance = 0.;
        healthChannelMainBattery = -1;
        healthChannelRPM = -1;
        healthChannelFailevents = -1;
        healthChannelConnectorBattery = -1;
        healthChannelGPS = -1;
        healthExpectedSize = -1;

        healthD = null;

        isWriteable = false;
        isReadable = false;
        backendStateObj = null;
        backend = null;
        backendPorts = null;

        androidState = null;

        simFail_failBitMask = null;
        simFail_debug1 = null;
        simFail_debug2 = null;
        simFail_debug3 = null;

        lastSpeedEstimationLat = 0;
        lastSpeedEstimationLon = 0;
        lastSpeedEstimationAlt = 0;
        lastSpeedEstimationTime = -1;
        lastSpeedEstimation = -1;

        startAltEstimater.reset();
        reachedMap.clear();
    }

    public APTypes getApType() throws AirplaneCacheEmptyException {
        if (this.apType == null) {
            throw new AirplaneCacheEmptyException();
        }

        return apType;
    }

    public PlaneInfo getPlaneInfo() throws AirplaneCacheEmptyException {
        if (this.planeInfo == null) {
            throw new AirplaneCacheEmptyException();
        }

        return planeInfo;
    }

    protected int healthChannelMainBattery = -1;
    protected int healthChannelConnectorBattery = -1;
    protected int healthChannelFailevents = -1;
    protected int healthChannelGPS = -1;
    protected int healthChannelRPM = -1;
    protected int healthExpectedSize = -1;

    public synchronized void recv_planeInfo(PlaneInfo info) {
        this.planeInfo = info;
        apType = APTypes.getCompatibleAPrelease(info.releaseVersion);
        Vector<SingleHealthDescription> descs = info.healthDescriptions;
        healthChannelMainBattery = -1;
        healthChannelConnectorBattery = -1;
        healthChannelGPS = -1;
        healthChannelFailevents = -1;
        healthChannelRPM = -1;
        for (int i = 0; i != descs.size(); i++) {
            // patching bat name DIRTY HACK
            if ("Battery".equals(descs.get(i).name)) {
                descs.get(i).name = PlaneConstants.DEF_BATTERY;
            } else if (healthChannelFailevents == -1 && "Failevents".equals(descs.get(i).name)) {
                healthChannelFailevents = i;
            } else if (PlaneConstants.DEF_MOTOR1.equals(descs.get(i).name)) {
                healthChannelRPM = i;
                // ignoring motor RPM validation
                descs.get(i).maxGreen = 100000;
                descs.get(i).maxYellow = 100000;
            }

            if (healthChannelMainBattery == -1 && descs.get(i).isMainBatt()) {
                healthChannelMainBattery = i;
            }

            if (healthChannelGPS == -1 && descs.get(i).isGPS_Sat()) {
                healthChannelGPS = i;
            }

            if (healthChannelConnectorBattery == -1 && descs.get(i).isConnectorBatt()) {
                healthChannelConnectorBattery = i;
            }
        }

        healthExpectedSize = descs.size();
        if (healthD != null && healthD.absolute.size() != healthExpectedSize) {
            // reset version in cache, if not compatible anymore
            healthD = null;
        }
    }

    public BackendInfo getBackendInfo() throws AirplaneCacheEmptyException {
        if (this.backendInfo == null) {
            throw new AirplaneCacheEmptyException();
        }

        return backendInfo;
    }

    public synchronized float getMainBatteryPercentage() throws AirplaneCacheEmptyException {
        try {
            return healthD.percent.get(healthChannelMainBattery);
        } catch (Throwable t) {
        }

        throw new AirplaneCacheEmptyException();
    }

    /**
     * returning the color of the main battery ("2"=red, "1"=yellow, "0"=green)
     *
     * @return
     * @throws AirplaneCacheEmptyException
     */
    public synchronized int getMainBatteryColor() throws AirplaneCacheEmptyException {
        try {
            float abs = healthD.absolute.get(healthChannelMainBattery);
            SingleHealthDescription hd = planeInfo.healthDescriptions.get(healthChannelMainBattery);
            if (hd.isGreen(abs)) {
                return 0;
            }

            if (hd.isRed(abs)) {
                return 2;
            }

            return 1;
        } catch (Throwable t) {
        }

        throw new AirplaneCacheEmptyException();
    }

    public int getHealthChannelMainBattery() {
        return healthChannelMainBattery;
    }

    /**
     * number betwee 0 and 1
     *
     * @return
     * @throws AirplaneCacheEmptyException
     */
    public double getFpProgress() throws AirplaneCacheEmptyException {
        CFlightplan fp = plane.getFPmanager().getOnAirFlightplan();
        if (fp == null) {
            throw new AirplaneCacheEmptyException();
        }

        if (curLat == null || curLon == null || curAltAboveStart == null) {
            throw new AirplaneCacheEmptyException();
        }
        // System.out.println("fp="+fp);
        // System.out.println("rp="+getCurrentReentrypointID());
        double p =
            fp.getProgressCachedInM(
                ReentryPointID.getIDPureWithoutCell(getCurrentReentrypointID()), curLat, curLon, curAltAboveStart);
        // System.out.println("progress="+p);
        if (p < 0) {
            throw new AirplaneCacheEmptyException();
        }

        return p / fp.getLengthInMeterCached();
    }

    /** call every listener with cached values, if the needed values are existing in the cache. */
    public void invokeWithCacheValues(IListener l) {
        if (l instanceof IAirplaneListenerConfig) {
            IAirplaneListenerConfig tmp = (IAirplaneListenerConfig)l;
            try {
                tmp.recv_config(getConf());
            } catch (AirplaneCacheEmptyException e) {
            }
        }

        if (l instanceof IAirplaneListenerFlightphase) {
            IAirplaneListenerFlightphase tmp = (IAirplaneListenerFlightphase)l;
            try {
                tmp.recv_flightPhase(getFlightPhase().ordinal());
            } catch (AirplaneCacheEmptyException e) {
            }
        }

        if (l instanceof IAirplaneListenerFlightPlanASM) {
            IAirplaneListenerFlightPlanASM tmp = (IAirplaneListenerFlightPlanASM)l;
            try {
                tmp.recv_setFlightPlanASM(getFlightplanASM(), isFlighplanReentryASM(), isFlightplanSucceedASM());
            } catch (AirplaneCacheEmptyException e) {
            }
        }

        if (l instanceof IAirplaneListenerFlightPlanXML) {
            IAirplaneListenerFlightPlanXML tmp = (IAirplaneListenerFlightPlanXML)l;
            try {
                tmp.recv_setFlightPlanXML(getFlightplanXML(), isFlighplanReentryXML(), isFlightplanSucceedXML());
            } catch (AirplaneCacheEmptyException e) {
            }
        }

        if (l instanceof IAirplaneListenerIsSimulation) {
            IAirplaneListenerIsSimulation tmp = (IAirplaneListenerIsSimulation)l;
            try {
                tmp.recv_isSimulation(isSimulation());
            } catch (AirplaneCacheEmptyException e) {
            }
        }

        if (l instanceof IAirplaneListenerName) {
            IAirplaneListenerName tmp = (IAirplaneListenerName)l;
            try {
                tmp.recv_nameChange(getName());
            } catch (AirplaneCacheEmptyException e) {
            }
        }

        if (l instanceof IAirplaneListenerPhotoLogName) {
            IAirplaneListenerPhotoLogName tmp = (IAirplaneListenerPhotoLogName)l;
            try {
                tmp.recv_newPhotoLog(getLastPhotoLogName(), getLastPhotoBytes());
            } catch (AirplaneCacheEmptyException e) {
            }
        }

        if (l instanceof IAirplaneListenerOrientation) {
            IAirplaneListenerOrientation tmp = (IAirplaneListenerOrientation)l;
            try {
                tmp.recv_orientation(getOrientation());
            } catch (AirplaneCacheEmptyException e) {
            }
        }

        if (l instanceof IAirplaneListenerPosition) {
            IAirplaneListenerPosition tmp = (IAirplaneListenerPosition)l;
            try {
                tmp.recv_position(getPosition());
            } catch (AirplaneCacheEmptyException e) {
            }
        }

        if (l instanceof IAirplaneListenerSimulationSettings) {
            IAirplaneListenerSimulationSettings tmp = (IAirplaneListenerSimulationSettings)l;
            try {
                tmp.recv_simulationSpeed(getSimulationSpeed());

            } catch (AirplaneCacheEmptyException e) {
            }

            try {
                tmp.recv_simulationSettings(getSimulationSettings());
            } catch (AirplaneCacheEmptyException e) {
            }
        }

        if (l instanceof IAirplaneListenerStartPos) {
            IAirplaneListenerStartPos tmp = (IAirplaneListenerStartPos)l;
            try {
                tmp.recv_startPos(getStartLon(), getStartLat(), 0);
            } catch (AirplaneCacheEmptyException e) {
            }

            try {
                tmp.recv_startPos(getStartLonBaro(), getStartLatBaro(), 1);
            } catch (AirplaneCacheEmptyException e) {
            }
        }

        if (l instanceof IAirplaneListenerHealth) {
            IAirplaneListenerHealth tmp = (IAirplaneListenerHealth)l;
            try {
                tmp.recv_health(getHealthData());
            } catch (AirplaneCacheEmptyException e) {
            }
        }

        if (l instanceof IAirplaneListenerFixedOrientation) {
            IAirplaneListenerFixedOrientation tmp = (IAirplaneListenerFixedOrientation)l;
            try {
                tmp.recv_fixedOrientation(
                    getFixedOrientationRoll(), getFixedOrientationPitch(), getFixedOrientationYaw());
            } catch (AirplaneCacheEmptyException e) {
            }
        }

        if (l instanceof IAirplaneListenerPhoto) {
            IAirplaneListenerPhoto tmp = (IAirplaneListenerPhoto)l;
            try {
                tmp.recv_photo(getPhoto());
            } catch (AirplaneCacheEmptyException e) {
            }
        }

        if (l instanceof IAirplaneListenerAndroidState) {
            IAirplaneListenerAndroidState tmp = (IAirplaneListenerAndroidState)l;
            try {
                tmp.recv_androidState(getAndroidState());
            } catch (AirplaneCacheEmptyException e) {
            }
        }

        if (l instanceof IAirplaneListenerPlaneInfo) {
            IAirplaneListenerPlaneInfo tmp = (IAirplaneListenerPlaneInfo)l;
            try {
                tmp.recv_planeInfo(getPlaneInfo());
            } catch (AirplaneCacheEmptyException e) {
            }
        }

        if (l instanceof IAirplaneListenerBackend) {
            IAirplaneListenerBackend tmp = (IAirplaneListenerBackend)l;
            try {
                tmp.recv_backend(getBackend(), getBackendPorts());
            } catch (AirplaneCacheEmptyException e) {
            }
        }

        if (l instanceof IAirplaneListenerLinkInfo) {
            IAirplaneListenerLinkInfo tmp = (IAirplaneListenerLinkInfo)l;
            try {
                tmp.recv_linkInfo(getLinkInfo());
            } catch (AirplaneCacheEmptyException e) {
            }
        }

        if (l instanceof IAirplaneFlightPlanSendingListener) {
            IAirplaneFlightPlanSendingListener tmp = (IAirplaneFlightPlanSendingListener)l;
            try {
                tmp.recv_fpSendingStatusChange(getFpStatus());
            } catch (AirplaneCacheEmptyException e) {
            }
        }

        if (l instanceof IAirplaneListenerConnectionEstablished) {
            IAirplaneListenerConnectionEstablished tmp = (IAirplaneListenerConnectionEstablished)l;
            try {
                tmp.recv_connectionEstablished(getPlanePort());
            } catch (AirplaneCacheEmptyException e) {
            }
        }

        if (l instanceof IAirplaneListenerConnectionState) {
            IAirplaneListenerConnectionState tmp = (IAirplaneListenerConnectionState)l;
            tmp.connectionStateChange(getConnectionState());
        }

        if (l instanceof IAirplaneListenerExpertSimulatedFails) {
            IAirplaneListenerExpertSimulatedFails tmp = (IAirplaneListenerExpertSimulatedFails)l;
            try {
                tmp.recv_simulatedFails(
                    getSimulatedFail_Bitmask(),
                    getSimulatedFail_Debug1(),
                    getSimulatedFail_Debug2(),
                    getSimulatedFail_Debug3());
            } catch (AirplaneCacheEmptyException e) {
            }
        }

        if (l instanceof IAirplaneParamsUpdateListener) {
            IAirplaneParamsUpdateListener tmp = (IAirplaneParamsUpdateListener)l;
            try {
                tmp.recv_paramsUpdateStatusChange(getParamsUpdateStatus());
            } catch (AirplaneCacheEmptyException e) {
            }
        }
    }

    protected ParamsUpdateStatus paramsUpdateStatus = null;

    public ParamsUpdateStatus getParamsUpdateStatus() throws AirplaneCacheEmptyException {
        if (paramsUpdateStatus == null) throw new AirplaneCacheEmptyException();
        return paramsUpdateStatus;
    }

    protected AndroidState androidState = null;

    public void recv_androidState(AndroidState state) {
        androidState = state;
    }

    public AndroidState getAndroidState() throws AirplaneCacheEmptyException {
        if (androidState == null) {
            throw new AirplaneCacheEmptyException();
        }

        return androidState;
    }

    protected LinkInfo linkInfo = null;
    protected FlightPlanStaus fpStatus = null;

    public void recv_linkInfo(LinkInfo li) {
        linkInfo = li;
    }

    public LinkInfo getLinkInfo() throws AirplaneCacheEmptyException {
        if (linkInfo == null) {
            throw new AirplaneCacheEmptyException();
        }

        return linkInfo;
    }

    public FlightPlanStaus getFpStatus() throws AirplaneCacheEmptyException {
        if (fpStatus == null) {
            throw new AirplaneCacheEmptyException();
        }

        return fpStatus;
    }

    public void recv_connectionEstablished(String port) {
        planePort = port;
    }

    String planePort = null;

    public String getPlanePort() throws AirplaneCacheEmptyException {
        if (planePort == null) {
            throw new AirplaneCacheEmptyException();
        }

        return planePort;
    }

    public void ping(String myID) {}

    public void recv_dirListing(String parentPath, MVector<String> files, MVector<Integer> sizes) {}

    public void recv_file(String path) {}

    public void recv_simulationSettings(SimulationSettings settings) {
        simSettings = settings;
    }

    public void recv_fileReceivingCancelled(String path) {}

    public void recv_fileReceivingProgress(String path, Integer progress) {}

    public void recv_fileReceivingSucceeded(String path) {}

    public void recv_fileSendingCancelled(String path) {}

    public void recv_fileSendingProgress(String path, Integer progress) {}

    public void recv_fileSendingSucceeded(String path) {}

    AirplaneConnectorState airplaneConnectorState = null;

    public void connectionStateChange(AirplaneConnectorState newState) {
        if (airplaneConnectorState == null || airplaneConnectorState.equals(AirplaneConnectorState.unconnected)) {
            if (!newState.equals(AirplaneConnectorState.unconnected)) {
                // reset backendInfo state, since the backend serial number my have changed in different to the cached
                // value!
                // with wrong chache values, maybe wrong backend broadcasts are passed into this airplane!
                // System.out.println("RESET BACKENDINFO CACHE!!");
                backendInfo = null;
            }
        }

        airplaneConnectorState = newState;
    }

    double lastSpeedEstimationLat;
    double lastSpeedEstimationLon;
    double lastSpeedEstimationAlt;
    double lastSpeedEstimationTime;
    double lastSpeedEstimation;

    public void recv_debug(DebugData d) {
        // System.out.println(System.currentTimeMillis()+ "recv_debug:"+d);
        debugData = d;
        travelDistance = d.groundDistance / 100.;
        if (d.groundspeed == 0 && lastSpeedEstimation >= 0) {
            d.groundspeed = (int)Math.round(lastSpeedEstimation * 100);
        }
    }

    protected void estimateSpeed(double lat, double lon, double alt, double timeStamp) {
        double lastSpeedEstimationLat = lat;
        double lastSpeedEstimationLon = lon;
        double lastSpeedEstimationAlt = alt;
        double lastSpeedEstimationTime = timeStamp;
        double dist =
            CAirplaneCache.distanceMeters(
                lastSpeedEstimationLat,
                lastSpeedEstimationLon,
                lastSpeedEstimationAlt,
                this.lastSpeedEstimationLat,
                this.lastSpeedEstimationLon,
                this.lastSpeedEstimationAlt);
        double dt = (lastSpeedEstimationTime - this.lastSpeedEstimationTime);
        // System.out.println("dist:" + dist + " dt:"+dt);
        if ((dt > 0 && dist > 0) || this.lastSpeedEstimationTime == -1) {
            if (this.lastSpeedEstimationTime != -1) {
                this.lastSpeedEstimation = dist / (lastSpeedEstimationTime - this.lastSpeedEstimationTime);
            } else {
                this.lastSpeedEstimation = 0;
            }
            // System.out.println("lastSpeedEstimation: "+(int)(lastSpeedEstimation*100+.5));
            this.lastSpeedEstimationLat = lastSpeedEstimationLat;
            this.lastSpeedEstimationLon = lastSpeedEstimationLon;
            this.lastSpeedEstimationAlt = lastSpeedEstimationAlt;
            this.lastSpeedEstimationTime = lastSpeedEstimationTime;
        }
    }

    public synchronized void recv_positionOrientation(PositionOrientationData po) {
        // System.out.println(System.currentTimeMillis()+ "recv_posOr:"+po);
        positionOrientation = po;
        if (!po.fromSession) {
            estimateSpeed(po.lat, po.lon, po.altitude / 100., po.getTimestamp());
        }

        if (po.cameraPitch == PlaneConstants.UNDEFINED_ANGLE) {
            po.cameraPitch = po.pitch;
        }

        if (po.cameraYaw == PlaneConstants.UNDEFINED_ANGLE) {
            po.cameraYaw = po.yaw;
        }

        if (po.cameraRoll == PlaneConstants.UNDEFINED_ANGLE) {
            po.cameraRoll = po.roll;
        }

        curTime = po.getTimestamp();
        if (startTime == null) {
            startTime = curTime;
        }

        if(po.time_sec > 0){
            positionOrientation.time_sec = po.time_sec;
        }

        if (healthD != null && healthChannelMainBattery >= 0) {
            healthD.absolute.set(healthChannelMainBattery, po.batteryVoltage);
            healthD.percent.set(healthChannelMainBattery, po.batteryPercent);
            lastHealthLowpassBat = null;
            getPlane().getRootHandler().recv_health(healthD);
        }

        getPlane().getRootHandler().recv_orientation(po.getOrientationData(debugData));
        if (po.flightphase != AirplaneFlightphase.waitingforgps.ordinal()) {
            getPlane().getRootHandler().recv_position(po.getPositionData(debugData));
        } else {
            recv_flightmode(po.flightmode);
            recv_flightPhase(po.flightphase);
        }


    }

    protected void recv_flightmode(int flightmode) {
        this.flightMode = AirplaneFlightmode.values()[flightmode];
    }

    public void recv_simulatedFails(Integer failBitMask, Integer debug1, Integer debug2, Integer debug3) {
        simFail_failBitMask = failBitMask;
        simFail_debug1 = debug1;
        simFail_debug2 = debug2;
        simFail_debug3 = debug3;
    }

    public int getSimulatedFail_Bitmask() throws AirplaneCacheEmptyException {
        if (simFail_failBitMask == null) {
            throw new AirplaneCacheEmptyException();
        }

        return simFail_failBitMask;
    }

    public int getSimulatedFail_Debug1() throws AirplaneCacheEmptyException {
        if (simFail_debug1 == null) {
            throw new AirplaneCacheEmptyException();
        }

        return simFail_debug1;
    }

    public int getSimulatedFail_Debug2() throws AirplaneCacheEmptyException {
        if (simFail_debug2 == null) {
            throw new AirplaneCacheEmptyException();
        }

        return simFail_debug2;
    }

    public int getSimulatedFail_Debug3() throws AirplaneCacheEmptyException {
        if (simFail_debug3 == null) {
            throw new AirplaneCacheEmptyException();
        }

        return simFail_debug3;
    }

    private String getLastPhotoLogName() throws AirplaneCacheEmptyException {
        if (lastPhotoLogName == null) {
            throw new AirplaneCacheEmptyException();
        }

        return lastPhotoLogName;
    }

    private int getLastPhotoBytes() throws AirplaneCacheEmptyException {
        if (lastPhotoBytes == null) {
            throw new AirplaneCacheEmptyException();
        }

        return lastPhotoBytes;
    }

    @Override
    public void recv_newPhotoLog(String name, Integer bytes) {
        lastPhotoLogName = name;
    }

    @Override
    public void recv_fpSendingStatusChange(FlightPlanStaus fpStatus) {
        this.fpStatus = fpStatus;
    }

    @Override
    public void recv_paramsUpdateStatusChange(ParamsUpdateStatus paramsUpdateStatus) {
        this.paramsUpdateStatus = paramsUpdateStatus;
    }

    // public static void main(String[] args) {
    // int combined = 1285;
    // int event_action_reason = (combined >> 1) & 0x00007F;
    // System.out.println(event_action_reason);
    // }
}
