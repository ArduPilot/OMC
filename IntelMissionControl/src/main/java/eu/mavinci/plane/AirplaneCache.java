/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.plane;

import static java.util.Comparator.reverseOrder;
import static java.util.stream.Collectors.toList;

import com.intel.missioncontrol.helper.Ensure;
import com.intel.missioncontrol.map.elevation.ElevationModelRequestException;
import com.intel.missioncontrol.map.elevation.IEgmModel;
import com.intel.missioncontrol.map.elevation.IElevationModel;
import com.intel.missioncontrol.map.worldwind.IWWGlobes;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.ui.navbar.layers.IMapClearingCenter;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import eu.mavinci.airspace.AirspaceComperatorFloor;
import eu.mavinci.airspace.EAirspaceManager;
import eu.mavinci.airspace.IAirspace;
import eu.mavinci.airspace.IAirspaceListener;
import eu.mavinci.airspace.LowestAirspace;
import eu.mavinci.core.flightplan.AFlightplanContainer;
import eu.mavinci.core.flightplan.CFlightplan;
import eu.mavinci.core.flightplan.CWaypoint;
import eu.mavinci.core.flightplan.IFlightplanRelatedObject;
import eu.mavinci.core.flightplan.IFlightplanStatement;
import eu.mavinci.core.flightplan.IReentryPoint;
import eu.mavinci.core.flightplan.ReentryPointID;
import eu.mavinci.core.flightplan.visitors.AFlightplanVisitor;
import eu.mavinci.core.flightplan.visitors.FirstWaypointVisitor;
import eu.mavinci.core.listeners.IListener;
import eu.mavinci.core.plane.AirplaneCacheEmptyException;
import eu.mavinci.core.plane.AirplaneFlightphase;
import eu.mavinci.core.plane.CAirplaneCache;
import eu.mavinci.core.plane.IAirplaneConnector;
import eu.mavinci.core.plane.listeners.IAirplaneListenerLogReplay;
import eu.mavinci.core.plane.sendableobjects.DebugData;
import eu.mavinci.core.plane.sendableobjects.PhotoData;
import eu.mavinci.core.plane.sendableobjects.PositionData;
import eu.mavinci.core.plane.sendableobjects.PositionOrientationData;
import eu.mavinci.desktop.helper.FileHelper;
import eu.mavinci.desktop.main.debug.Debug;
import eu.mavinci.flightplan.Flightplan;
import eu.mavinci.flightplan.LandingPoint;
import eu.mavinci.flightplan.PicArea;
import eu.mavinci.flightplan.Waypoint;
import eu.mavinci.flightplan.computation.LocalTransformationProvider;
import eu.mavinci.plane.logfile.ALogReader;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Line;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Earth;
import gov.nasa.worldwind.globes.Globe;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.OptionalDouble;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javafx.util.Pair;

public class AirplaneCache extends CAirplaneCache implements IAirspaceListener {

    /////////////////////////////////////////////////////
    // objects for line tracking in case on manned edition
    private static final double LIKELIHOOD_THRESHOLD = 0.6;

    protected static final double GPS_ERROR = 10;
    // protected static final int NAV_AIM_BUFFER = 8;

    protected static final double DIST_PRIORITY = 1;
    protected static final double HEAD_PRIORITY = 1;
    protected static final double DEFAULT_PRIORITY = 1;

    protected double max_dist;

    private Position prevPos;
    private double planeYaw;

    LocalTransformationProvider trafo;

    double headingSollDeg;
    double crossTrackErr2d;
    double crossTrackErrAlt;

    // list to store lines history
    private final ArrayList<Integer> linesBuffer = new ArrayList<>();

    // list to store last 20 positions
    private final ArrayList<Position> positionsBuffer = new ArrayList<>();

    private static final Globe globe =
        DependencyInjector.getInstance().getInstanceOf(IWWGlobes.class).getDefaultGlobe();
    private static final IElevationModel elevationModel =
        DependencyInjector.getInstance().getInstanceOf(IElevationModel.class);
    private static final IEgmModel egmModel = DependencyInjector.getInstance().getInstanceOf(IEgmModel.class);

    // map to store range queue of lines that are closer than (GPS error +
    // corridor/2) with their likelihood
    private class NavAimRangedMap extends TreeMap<NavAim, Double> {
        public NavAim getFirstKey() {
            TreeMap<NavAim, Double> map =
                entrySet()
                    .stream()
                    .sorted(Entry.comparingByValue())
                    .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, TreeMap::new));
            return map.firstEntry().getKey();
        }

        public double getFirstValue() {
            TreeMap<NavAim, Double> map =
                entrySet()
                    .stream()
                    .sorted(Entry.comparingByValue())
                    .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, TreeMap::new));
            return map.firstEntry().getValue();
        }
    }

    private final NavAimRangedMap rangedLines = new NavAimRangedMap();

    private class NavAimList extends LinkedList<NavAim> {
        private static final double MOVE_OUT_THRESHOLD = 0.2;
        private final double LENGTH_TOTAL;
        private double length;
        private LinkedList<Double> dists = new LinkedList<>();

        public NavAimList(double radius) {
            LENGTH_TOTAL = Math.PI * radius;
        }

        public boolean add(NavAim e, double dist) {
            while (length >= LENGTH_TOTAL && size() > 0) {
                remove(0);
                double d = dists.remove(0);
                length -= d;
            }

            length += dist;
            dists.add(dist);
            return super.add(e);
        }

        @Override
        public void clear() {
            super.clear();
            length = 0;
        }

        public boolean isFull() {
            return length >= LENGTH_TOTAL && size() > 0;
        }

        public NavAim getMostCommon() {
            List<Integer> sorted =
                stream().collect(Collectors.groupingBy(NavAim::getCurLineId, Collectors.counting()))
                    .entrySet()
                    .stream()
                    .sorted(
                        Map.Entry.<Integer, Long>comparingByValue(reverseOrder())
                            .thenComparing(Map.Entry.comparingByKey()))
                    .map(Map.Entry::getKey)
                    .collect(toList());

            Map<Integer, List<NavAim>> groupByLineId = stream().collect(Collectors.groupingBy(NavAim::getCurLineId));

            List<NavAim> mostCommon = groupByLineId.get(sorted.get(0));
            Ensure.notNull(mostCommon, "mostCommon");
            List<Integer> sorted2 =
                mostCommon
                    .stream()
                    .collect(Collectors.groupingBy(NavAim::getCurReentryPointId, Collectors.counting()))
                    .entrySet()
                    .stream()
                    .sorted(
                        Map.Entry.<Integer, Long>comparingByValue(reverseOrder())
                            .thenComparing(Map.Entry.comparingByKey()))
                    .map(Map.Entry::getKey)
                    .collect(toList());

            Map<Integer, List<NavAim>> groupByPointId =
                stream().collect(Collectors.groupingBy(NavAim::getCurReentryPointId));

            Ensure.notNull(groupByPointId, "groupByPointId");
            List<NavAim> singlePointId = groupByPointId.get(sorted2.get(0));
            Ensure.notNull(singlePointId, "singlePointId");
            return singlePointId.get(0);
        }

        public NavAim getTendent() {
            return null;
        }

        public boolean moveOutFrom(NavAim aim) {
            List<NavAim> result =
                stream().filter(a -> a.curReentryPointId == aim.curReentryPointId).collect(Collectors.toList());

            NavAim aim0 = result.get(0);
            NavAim aimN = result.get(result.size() - 1);

            if (aim0.value - aimN.value > MOVE_OUT_THRESHOLD) {
                return true;
            }

            return false;
        }
    };

    private NavAimList lastMostLikelyLines = new NavAimList(0);

    public static class NavAim implements Comparable<NavAim> {
        private volatile int curLineId = -Integer.MAX_VALUE;
        private volatile int curReentryPointId = -Integer.MAX_VALUE;
        private boolean forwardDirection = true;

        private boolean notInitialized = false;
        private double value;

        public NavAim() {}

        public NavAim(int curPointId, boolean forward, double value) {
            this.curReentryPointId = curPointId;
            curLineId = ReentryPointID.getLineNumberPure(curReentryPointId);
            this.forwardDirection = forward;
            this.value = value;
        }

        @Override
        protected NavAim clone() {
            return new NavAim(curReentryPointId, forwardDirection, false);
        }

        public NavAim(int curPointId, boolean forward, boolean init) {
            this.curReentryPointId = curPointId;
            curLineId = ReentryPointID.getLineNumberPure(curReentryPointId);
            this.forwardDirection = forward;
            this.notInitialized = init;
        }

        public int getCurLineId() {
            return curLineId;
        }

        public int getCurReentryPointId() {
            return curReentryPointId;
        }

        public boolean isForwardDirection() {
            return forwardDirection;
        }

        public void setUndefined() {
            if (!notInitialized) {
                curReentryPointId = Integer.MAX_VALUE;
                curLineId = Integer.MAX_VALUE;
                forwardDirection = true;
            }
        }

        public boolean undefined() {
            if (curLineId == Integer.MAX_VALUE) {
                return true;
            }

            return false;
        }

        public boolean notInitialized() {
            if (notInitialized) {
                return true;
            }

            return false;
        }

        public int getNextWP(IAirplane plane) {
            Flightplan fp = plane.getFPmanager().getOnAirFlightplan();
            IReentryPoint current = fp.getStatementById(curReentryPointId).rp;

            int wpId = 0;
            CWaypoint wp = fp.getNextWaypoint(current);
            if (wp != null) {
                wpId = wp.getId();
            }

            return wpId;
        }

        @Override
        public int compareTo(NavAim o) {
            return this.equals(o) ? 0 : -1;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof NavAim) {
                if (((NavAim)obj).curReentryPointId == this.curReentryPointId
                        && ((NavAim)obj).curLineId == this.curLineId
                        && ((NavAim)obj).forwardDirection == this.forwardDirection) {
                    return true;
                }
            }

            return false;
        }
        /*
         * public void setNotInitialized() { curReentryPointId = -Integer.MAX_VALUE; curLineId = -Integer.MAX_VALUE; forwardDirection =
         * true; }
         */
    }

    private NavAim currentAim = new NavAim();

    ////////////////////////////////////////////////////

    public AirplaneCache(IAirplane plane) {
        super(plane);
    }

    private Position curPos;
    private LatLon prevLatLon;
    private double prevAlt;
    private OptionalDouble headingEstimate = OptionalDouble.empty();
    private OptionalDouble climbEstimate = OptionalDouble.empty();
    private OptionalDouble curElev = OptionalDouble.empty();
    private boolean curElevExact;
    private LatLon startPos;
    private LatLon startPosBaro;
    private OptionalDouble startElev = OptionalDouble.empty();
    private OptionalDouble startElevEGMoffset = OptionalDouble.empty();
    private boolean startElevExact;
    // private COrigin origin;

    private LatLon curLatLon;
    private OptionalDouble curAlt = OptionalDouble.empty();

    public String getScreenName() {
        try {
            return super.getScreenName();
        } catch (AirplaneCacheEmptyException e) {
        }

        return "session:" + ((Airplane)getPlane()).getBaseFolder().getName();
    }

    public String getFileName() throws AirplaneCacheEmptyException {
        try {
            return FileHelper.makeValidFilenameFromString(getName());
        } catch (AirplaneCacheEmptyException e) {
            return FileHelper.makeValidFilenameFromString(getPlanePort());
        }
    }

    /**
     * Current altitude over Sea Level in Meter
     *
     * @return
     * @throws AirplaneCacheEmptyException
     */
    public double getCurAlt() throws AirplaneCacheEmptyException {
        if (!curAlt.isPresent()) {
            throw new AirplaneCacheEmptyException();
        }

        return curAlt.getAsDouble();
    }

    public LatLon getCurLatLon() throws AirplaneCacheEmptyException {
        if (curLatLon == null) {
            throw new AirplaneCacheEmptyException();
        }

        return curLatLon;
    }

    public Position getCurPos() throws AirplaneCacheEmptyException {
        if (curPos == null) {
            throw new AirplaneCacheEmptyException();
        }

        return curPos;
    }

    List<IAirspace> airspaceList = null;
    Sector bb = null;

    public List<IAirspace> getAirspaces() throws AirplaneCacheEmptyException {
        if (airspaceList == null) {
            PositionData p = getPosition();
            LatLon latLon = LatLon.fromDegrees(p.lat, p.lon);
            bb = Sector.boundingSector(latLon, latLon);
            airspaceList = EAirspaceManager.instance().getAirspacesInCache(bb);

            AirspaceComperatorFloor airspaceComperatorFloor =
                new AirspaceComperatorFloor(
                    latLon, getCurGroundElev() - getStartElevEGMoffset(), airspaceList, false, false);
            airspaceList.clear();
            for (Pair<IAirspace, Double> tmp : airspaceComperatorFloor.airspaceAlts) {
                airspaceList.add(tmp.getKey());
            }
        }

        return airspaceList;
    }

    LowestAirspace lowest = null;

    public LowestAirspace getMaxMAVAltitude() throws AirplaneCacheEmptyException {
        if (lowest == null) {
            // System.out.println("list="+getAirspaces());
            lowest =
                EAirspaceManager.getMaxMAVAltitude(getAirspaces(), bb, getCurGroundElev() - getStartElevEGMoffset());
            // System.out.println("lowest="+lowest);
        }

        return lowest;
    }

    public void resetAirspaces() {
        bb = null;
        airspaceList = null;
        lowest = null;
    }

    // new BoundingBox(lat-diffLat, lat+diffLat, lon-diffLon, lon+diffLon);
    // System.out.println("bb:"+bb);

    /**
     * Altitude abouve sea level in Meters of the Ground under the Plane
     *
     * @return
     * @throws AirplaneCacheEmptyException
     */
    public double getCurGroundElev() throws AirplaneCacheEmptyException {
        if (!curElev.isPresent()) {
            throw new AirplaneCacheEmptyException();
        }

        return curElev.getAsDouble();
    }

    public boolean isCurGroundElevExact() {
        if (curElev == null) {
            return false;
        }

        return curElevExact;
    }

    /**
     * Altitude from the plain over ground in meter
     *
     * @return
     * @throws AirplaneCacheEmptyException
     */
    public double getCurPlaneElevOverGround() throws AirplaneCacheEmptyException {
        return getCurAlt() - getCurGroundElev();
    }

    public LatLon getStartPos() throws AirplaneCacheEmptyException {
        if (startPos == null) {
            throw new AirplaneCacheEmptyException();
        }

        return startPos;
    }

    public LatLon getStartPosBaro() throws AirplaneCacheEmptyException {
        if (startPosBaro == null) {
            throw new AirplaneCacheEmptyException();
        }

        return startPosBaro;
    }

    /**
     * heading from GPS is avaliable, or estimation if system is not transmitting heading in degrees
     *
     * @return
     * @throws AirplaneCacheEmptyException
     */
    public double getHeadingEstimate() throws AirplaneCacheEmptyException {
        if (debugData != null) {
            return (double)debugData.heading;
        }

        if (!headingEstimate.isPresent()) {
            throw new AirplaneCacheEmptyException();
        }

        return headingEstimate.getAsDouble();
    }

    /**
     * climb in meter per m XY distance estimation if system is not transmitting in degrees
     *
     * @return
     * @throws AirplaneCacheEmptyException
     */
    public double getClimbEstimate() throws AirplaneCacheEmptyException {
        if (!climbEstimate.isPresent()) {
            throw new AirplaneCacheEmptyException();
        }

        return climbEstimate.getAsDouble();
    }

    /**
     * elevation above sea level of the planes starting position
     *
     * @return
     * @throws AirplaneCacheEmptyException
     */
    public double getStartElevOverWGS84() throws AirplaneCacheEmptyException {
        if (!startElev.isPresent()) {
            throw new AirplaneCacheEmptyException();
        }

        return startElev.getAsDouble();
    }

    public double getStartElevEGMoffset() throws AirplaneCacheEmptyException {
        if (!startElevEGMoffset.isPresent()) {
            throw new AirplaneCacheEmptyException();
        }

        return startElevEGMoffset.getAsDouble();
    }

    public boolean isStartElevExact() {
        if (!startElev.isPresent()) {
            return false;
        }

        return startElevExact;
    }

    public static final double MIN_DISTANCE_TO_ESTIMATE_HEADING = 5;

    Double externalLat = null;
    Double externalLon = null;

    private Vec4 prevVec;

    @Override
    public synchronized void recv_positionOrientation(PositionOrientationData po) {

        // trying to fake forward compute next position, so in case of GPS loss
        // we can extrapolate roughly the positions
        // System.out.println("healthChannelRPM:"+healthChannelRPM);
        if (externalLat != null
                && externalLon != null
                && externalLat.equals(po.lat)
                && externalLon.equals(po.lon)
                && healthChannelRPM >= 0
                && positionOrientation != null
                && flightPhase != AirplaneFlightphase.takeoff) {
            // System.out.println("fallback gps predict");
            // check if engine is on
            if ((healthD != null && healthD.absolute.get(healthChannelRPM) >= 100)
                    || (flightPhase != null && flightPhase.isFlightphaseOnGround() != 1)) {
                // System.out.println("engine on, so DO IT");
                IAirplane plane = ((IAirplane)this.plane);
                final LocalTransformationProvider trafo =
                    new LocalTransformationProvider(
                        Position.fromDegrees(positionOrientation.lat, positionOrientation.lon, 0),
                        Angle.ZERO,
                        0,
                        0,
                        false);
                Vec4 v = new Vec4(Angle.fromDegrees(po.yaw).sin(), Angle.fromDegrees(po.yaw).cos());
                v = v.multiply3(plane.getWindEstimate().vA);

                // TODO
                // FIXME..
                // maybe
                // here
                // belongs
                // a
                // minus
                // sign?
                // are
                // units
                // correct?
                // orientation
                // correct?
                v = v.add3(plane.getWindEstimate().vWE, plane.getWindEstimate().vWN, 0);
                v = v.multiply3(po.getTimestamp() - positionOrientation.getTimestamp());

                LatLon newLatLon = trafo.transformToGlobe(v);
                Ensure.notNull(newLatLon, "newLatLon");
                po.lat = newLatLon.latitude.degrees;
                po.lon = newLatLon.longitude.degrees;
                po.gpsLossFallback = true;
            }
        } else {
            externalLat = po.lat;
            externalLon = po.lon;
        }

        super.recv_positionOrientation(po);
    }

    @Override
    public void recv_position(PositionData p) {
        LatLon newLatLon = LatLon.fromDegrees(p.lat, p.lon);
        if (plane.isSimulation()) {
            try {
                p.gpsAltitude =
                    (int)Math.round(getStartElevOverWGS84() * 100 + p.altitude - getStartElevEGMoffset() * 100);
            } catch (AirplaneCacheEmptyException e) {
                p.gpsAltitude = -1000 * 100;
            }
        }
        // trying to fake forward compute next position, so in case of GPS loss
        // we can extrapolate roughly the positions
        // System.out.println("healthChannelRPM:"+healthChannelRPM);
        if (!p.synthezided) {
            if (externalLat != null
                    && externalLon != null
                    && externalLat.equals(p.lat)
                    && externalLon.equals(p.lon)
                    && healthChannelRPM >= 0
                    && orientation != null
                    && position != null
                    && flightPhase != AirplaneFlightphase.takeoff) {
                // System.out.println("fallback gps predict");
                // check if engine is on
                if ((healthD != null && healthD.absolute.get(healthChannelRPM) >= 100)
                        || (flightPhase != null && flightPhase.isFlightphaseOnGround() != 1)) {
                    // System.out.println("engine on, so DO IT");
                    IAirplane plane = ((IAirplane)this.plane);
                    final LocalTransformationProvider trafo =
                        new LocalTransformationProvider(
                            Position.fromDegrees(position.lat, position.lon, 0), Angle.ZERO, 0, 0, false);
                    Vec4 v =
                        new Vec4(Angle.fromDegrees(orientation.yaw).sin(), Angle.fromDegrees(orientation.yaw).cos());
                    v = v.multiply3(plane.getWindEstimate().vA);

                    // TODO
                    // FIXME..
                    // maybe
                    // here
                    // belongs
                    // a
                    // minus
                    // sign?
                    // are
                    // units
                    // correct?
                    // orientation
                    // correct?
                    v = v.add3(plane.getWindEstimate().vWE, plane.getWindEstimate().vWN, 0);
                    v = v.multiply3(p.getTimestamp() - position.getTimestamp());

                    newLatLon = trafo.transformToGlobe(v);
                    Ensure.notNull(newLatLon, "newLatLon");
                    p.lat = newLatLon.latitude.degrees;
                    p.lon = newLatLon.longitude.degrees;
                    p.gpsLossFallback = true;
                }
            } else {
                externalLat = p.lat;
                externalLon = p.lon;
            }
        }

        super.recv_position(p);

        if ((!startElev.isPresent() || !startElevExact) && startPosBaro != null) {
            // retry to determine altetude
            recv_startPos(startLonBaro, startLatBaro, 1);
        }

        curPos = null;

        curLatLon = newLatLon;
        if (prevLatLon == null) {
            prevLatLon = curLatLon;
        }

        double distPrev2d =
            LatLon.ellipsoidalDistance(prevLatLon, curLatLon, globe.getEquatorialRadius(), globe.getPolarRadius());
        if (distPrev2d >= MIN_DISTANCE_TO_ESTIMATE_HEADING) {
            headingEstimate = OptionalDouble.of(LatLon.greatCircleAzimuth(prevLatLon, curLatLon).degrees);
            prevLatLon = curLatLon;
            climbEstimate = OptionalDouble.of((p.altitude - prevAlt) / 100. / distPrev2d);
            prevAlt = p.altitude;
            // System.out.println("hadingEstimate:" + headingEstimate + "
            // curLatLon:"+curLatLon + " prevLatLon:"+prevLatLon);
        }

        try {
            curElev = OptionalDouble.of(elevationModel.getElevation(curLatLon));
            curElevExact = true;
        } catch (ElevationModelRequestException e) {
            curElev = OptionalDouble.of(e.achievedAltitude);
            curElevExact = false;
        }

        if (!startElev.isPresent()) {
            curAlt = OptionalDouble.empty();
        } else {
            curAlt = OptionalDouble.of((double)p.altitude / 100.0 + startElev.getAsDouble());
        }

        IAirplaneConnector connector = plane.getAirplaneConnector();

        if (curAlt.isPresent()) {
            curPos = new Position(curLatLon, curAlt.getAsDouble());
        }

        //        resetAirspaces();

        /*if (isManned && !plane.isSimulation() && isNmea) {
            computeCurrentLineAndWP(p);
        }*/
    }

    private void computeCurrentLineAndWP(PositionData p) {
        CFlightplan fp = plane.getFPmanager().getOnAirFlightplan();
        if (trafo == null) {
            try_recv_setFlightPlanXML();
        }

        if (fp != null && trafo != null) {
            Ensure.notNull(curPos, "curPos");
            if (prevPos == null) {
                prevPos = curPos;
            }

            Position posOverStart = new Position(curPos, curPos.elevation - startElev.getAsDouble());
            Vec4 vec = trafo.transformToLocalInclAlt(posOverStart);
            if (prevVec == null) {
                prevVec = vec;
            }

            AFlightplanVisitor fpVis =
                new AFlightplanVisitor() {
                    Waypoint prev = null;
                    Waypoint next = null;
                    private Double current_line;

                    @Override
                    public boolean visit(IFlightplanRelatedObject fpObj) {
                        Vec4 v1;
                        Vec4 v2;

                        if (fpObj instanceof Waypoint) {
                            next = (Waypoint)fpObj;
                            if (prev != null) {
                                v1 = trafo.transformToLocalInclAlt(prev.getPosition());
                                v2 = trafo.transformToLocalInclAlt(next.getPosition());

                                double corridor = getCorridorWidth(fp);
                                double dist = Line.distanceToSegment(v1, v2, vec);
                                double diff = anglesDiffUnsigned(prev, next);
                                int nextWPId = diff < 90 ? next.getId() : prev.getId();
                                boolean isDefault = isDefault(nextWPId, diff < 90);

                                if (dist < GPS_ERROR + corridor || isDefault) {
                                    double value =
                                        computeLikelihood(
                                            diff,
                                            dist,
                                            corridor,
                                            DIST_PRIORITY,
                                            HEAD_PRIORITY,
                                            DEFAULT_PRIORITY,
                                            isDefault);

                                    rangedLines.put(new NavAim(nextWPId, diff < 90, value), value);
                                }
                            }

                            prev = next;
                        }

                        return false;
                    }

                    private double anglesDiffUnsigned(Waypoint prev, Waypoint next) {
                        Angle lineYaw =
                            LatLon.ellipsoidalForwardAzimuth(
                                prev.getPosition(),
                                next.getPosition(),
                                Earth.WGS84_EQUATORIAL_RADIUS,
                                Earth.WGS84_POLAR_RADIUS);
                        double lineHeading = lineYaw.degrees < 0 ? 360 + lineYaw.degrees : lineYaw.degrees;

                        if (!prevPos.equals(curPos)) {
                            planeYaw =
                                LatLon.ellipsoidalForwardAzimuth(
                                        prevPos, curPos, Earth.WGS84_EQUATORIAL_RADIUS, Earth.WGS84_POLAR_RADIUS)
                                    .degrees;
                            prevPos = curPos;
                        }

                        planeYaw = planeYaw < 0 ? 360 + planeYaw : planeYaw;

                        double diff = Math.abs(lineHeading - planeYaw);
                        double norm = Math.abs(Math.abs(diff + 180) % 360 - 180);
                        return norm;
                    }

                    private double computeLikelihood(
                            double normAngDiff,
                            double dist,
                            double corridor,
                            double priorityDist,
                            double priorityAngle,
                            double priorityDefault,
                            boolean isDefault) {
                        double angleLikelihood = 0;
                        double distLikelihood = 0;
                        if (normAngDiff < 90) {
                            angleLikelihood = (90 - normAngDiff) / 90.0;
                        } else {
                            angleLikelihood = normAngDiff / 180.0;
                        }

                        distLikelihood = dist > corridor ? 0 : (Math.abs((corridor - dist) / (2 * corridor)) + 0.5);

                        return (priorityAngle * angleLikelihood
                                + priorityDist * distLikelihood
                                + priorityDefault * (isDefault ? 1 : 0))
                            / (priorityAngle + priorityDist + priorityDefault);
                    }
                };

            fpVis.startVisit(fp);

            if (!rangedLines.isEmpty()) {
                double bestAimVal = rangedLines.getFirstValue();
                if (bestAimVal > LIKELIHOOD_THRESHOLD) {
                    NavAim aim = rangedLines.getFirstKey();

                    double dist = vec.distanceTo3(prevVec);
                    lastMostLikelyLines.add(aim, dist);
                } else {
                    fallBackToDefault();
                }
            } else {
                fallBackToDefault();
            }

            if (lastMostLikelyLines.isFull()) {
                NavAim aim = lastMostLikelyLines.getMostCommon();

                if (!aim.equals(currentAim) && ((previousAim == null || !aim.equals(previousAim)))) {
                    currentAim = aim;
                    lastMostLikelyLines.clear();
                }
            }

            rangedLines.clear();

            p.reentrypoint = currentAim.curReentryPointId;
            p.forwardDirection = currentAim.forwardDirection;

            prevVec = vec;
            System.out.println("CUR POINT ID: " + p.reentrypoint + " NAV DIRECTION: " + p.forwardDirection);
        }
    }

    private void fallBackToDefault() {
        if (!currentAim.notInitialized() && !(isDefault(currentAim.curReentryPointId, currentAim.forwardDirection))) {
            lastMostLikelyLines.clear();
            if (defaultAim != null) {
                currentAim = defaultAim;
            } else {
                currentAim.setUndefined();
            }
        }
    }

    private boolean isDefault(int nextWPId, boolean b) {
        if (defaultAim != null && defaultAim.curReentryPointId == nextWPId && defaultAim.forwardDirection == b) {
            return true;
        }

        return false;
    }

    public double getCorridorWidth(CFlightplan fp) {
        double height = 0;
        double size = 0;
        Iterator<IFlightplanStatement> it = ((AFlightplanContainer)fp).iterator();
        while (it.hasNext()) {
            IFlightplanStatement segment = it.next();
            if (segment instanceof PicArea) {
                size = ((PicArea)segment).getSizeParallelFlightEff();
                height = ((PicArea)segment).getAlt();
                if (size > height) {
                    size = height;
                }

                size /= 2;
                break;
            }
        }

        if (size == 0) {
            size =
                ((IAirplane)plane)
                        .getHardwareConfiguration()
                        .getPlatformDescription()
                        .getTurnRadius()
                        .convertTo(Unit.METER)
                        .getValue()
                        .doubleValue()
                    / 2.0;
        }

        return size;
    }

    boolean wasLastRecvStartPosMajorChange = false;
    private NavAim previousAim = null;
    private NavAim defaultAim = null;

    public boolean wasLastRecvStartPosMajorChange() {
        return wasLastRecvStartPosMajorChange;
    }

    public static final double AutoRecenterStartDistanceMeter = 10000;
    public static final Angle AutoRecenterStartDistance =
        Angle.fromRadians(AutoRecenterStartDistanceMeter / Earth.WGS84_EQUATORIAL_RADIUS);

    @Override
    public void recv_startPos(Double lon, Double lat, Integer pressureZero) {
        wasLastRecvStartPosMajorChange = false;
        LatLon newStartPos = LatLon.fromDegrees(lat, lon);

        super.recv_startPos(lon, lat, pressureZero);
        if (lon == 0 && lat == 0) {
            Debug.getLog().log(Level.INFO, "Received Starting Position (0, 0) and discard it.");
            return;
        }

        if (pressureZero == 1 || startPosBaro == null) {
            startPosBaro = newStartPos;

            // System.out.println("new startpo: " + startPos.toString());
            OptionalDouble oldElev =
                startElev.isPresent() ? OptionalDouble.of(startElev.getAsDouble()) : OptionalDouble.empty();

            try {
                startElev = OptionalDouble.of(elevationModel.getElevation(startPosBaro));
                startElevExact = true;
            } catch (ElevationModelRequestException e) {
                startElev = OptionalDouble.of(e.achievedAltitude);
                startElevExact = false;
            }

            startElevEGMoffset = OptionalDouble.of(egmModel.getEGM96Offset(startPosBaro));

            if (!startElev.equals(oldElev)) {
                // System.out.println("new start elevation" + startElev);
                plane.getFPmanager().flightplanValuesChanged(null);
            }
        }

        if (pressureZero == 0 || startPos == null) {
            if (startPos == null
                    || LatLon.greatCircleDistance(newStartPos, startPos).compareTo(AutoRecenterStartDistance) >= 0) {
                wasLastRecvStartPosMajorChange = true;
            }

            startPos = newStartPos;
        }
    }

    @Override
    public void recv_flightPhase(Integer fp) {
        super.recv_flightPhase(fp);
        if (flightPhase.isGroundTarget()) {
            DependencyInjector.getInstance().getInstanceOf(IMapClearingCenter.class).clearUavImageCache();
        }
    }

    @Override
    public synchronized void reset() {
        curPos = null;
        prevLatLon = null;
        prevAlt = 0;
        headingEstimate = OptionalDouble.empty();
        climbEstimate = OptionalDouble.empty();
        curElev = OptionalDouble.empty();
        curElevExact = false;
        startPos = null;
        startPosBaro = null;
        startElev = OptionalDouble.empty();
        startElevEGMoffset = OptionalDouble.empty();
        startElevExact = false;
        curLatLon = null;
        wasLastRecvStartPosMajorChange = false;
        externalLat = null;
        externalLon = null;
        curAlt = OptionalDouble.empty();
        super.reset();
    }

    @Override
    public void airspacesChanged() {
        resetAirspaces();
    }

    @Override
    public void invokeWithCacheValues(IListener l) {
        super.invokeWithCacheValues(l);

        if (plane.getAirplaneConnector() != null && plane.getAirplaneConnector() instanceof ALogReader) {
            ALogReader reader = (ALogReader)plane.getAirplaneConnector();
            if (l instanceof IAirplaneListenerLogReplay) {
                IAirplaneListenerLogReplay tmp = (IAirplaneListenerLogReplay)l;
                tmp.replayPaused(reader.isPaused());
                tmp.replayStopped(reader.isStopped());
                tmp.replaySkipPhase(reader.isSkippingPhase());
                tmp.elapsedSimTime(reader.getElapsedTime(), reader.getElapsedTime());
            }
        }
    }

    @Override
    public void recv_photo(PhotoData photo) {
        // System.out.println("photo before: " + photo);
        if (photo.gps_ellipsoid <= -199 * 100) {
            photo.gps_ellipsoid = (float)egmModel.getEGM96Offset(LatLon.fromDegrees(photo.lat, photo.lon)) * 100;
            // System.out.println("fixing ellipsoid:" + photo);
        }

        if (plane.isSimulation()) {
            try {
                photo.gps_alt = (float)(getStartElevOverWGS84() * 100 + photo.alt - getStartElevEGMoffset() * 100);
            } catch (AirplaneCacheEmptyException e) {
                photo.gps_alt = -1000 * 100;
            }
        }

        // System.out.println("receive Foto: " + photo);
        super.recv_photo(photo);
    }

    @Override
    public void recv_debug(DebugData d) {
        Position p = curPos;
        // System.out.println("debug before:" + d);
        if (d.gps_ellipsoid <= -199 * 100 && p != null) {
            d.gps_ellipsoid = (float)egmModel.getEGM96Offset(p) * 100;
            // System.out.println("fixing ellipsoid:" + d);
        }

        if (plane.isSimulation()) {
            try {
                d.gpsAltitude =
                    (float)(getStartElevOverWGS84() * 100 + getPosition().altitude - getStartElevEGMoffset() * 100);
            } catch (AirplaneCacheEmptyException e) {
                d.gpsAltitude = -1000 * 100;
            }
        }

        super.recv_debug(d);
    }

    public void try_recv_setFlightPlanXML() {
        CFlightplan fp = plane.getFPmanager().getOnAirFlightplan();
        if (fp.isEmpty()) {
            trafo = null;
            return;
        }

        double r =
            ((IAirplane)plane)
                .getHardwareConfiguration()
                .getPlatformDescription()
                .getTurnRadius()
                .convertTo(Unit.METER)
                .getValue()
                .doubleValue();
        max_dist = r / 5.0; // TODO

        lastMostLikelyLines = new NavAimList(r / 10);

        trafo =
            new LocalTransformationProvider(((LandingPoint)fp.getLandingpoint()).getPosition(), Angle.ZERO, 0, 0, true);

        positionsBuffer.clear();
        lastMostLikelyLines.clear();
        linesBuffer.clear();

        FirstWaypointVisitor wpVis = new FirstWaypointVisitor();
        wpVis.setSkipIgnoredPaths(true);

        wpVis.startVisit(fp);
        if (wpVis.firstWaypoint != null) {
            int id = ((Waypoint)wpVis.firstWaypoint).getId();
            currentAim = new NavAim(id, true, true);
            defaultAim = currentAim;
        }
    }

    public void setNavAim(NavAim currentAim) {
        if (!currentAim.equals(this.currentAim)) {
            previousAim = this.currentAim.clone();
            this.currentAim = currentAim.clone();
            defaultAim = currentAim.clone();
            lastMostLikelyLines.clear();
        }
    }
    /*
     * public COrigin getOrigin() { return origin; } public void setValues(COrigin origin2) { this.origin = origin2; }
     */
}
