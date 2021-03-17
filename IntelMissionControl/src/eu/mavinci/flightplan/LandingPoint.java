/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.flightplan;

import com.intel.missioncontrol.concurrent.Dispatcher;
import com.intel.missioncontrol.hardware.IPlatformDescription;
import com.intel.missioncontrol.helper.Ensure;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.map.worldwind.IWWGlobes;
import com.intel.missioncontrol.measure.Unit;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import eu.mavinci.core.flightplan.AltAssertModes;
import eu.mavinci.core.flightplan.CFlightplan;
import eu.mavinci.core.flightplan.CPreApproach;
import eu.mavinci.core.flightplan.CStartProcedure;
import eu.mavinci.core.flightplan.CWaypoint;
import eu.mavinci.core.flightplan.CWaypointLoop;
import eu.mavinci.core.flightplan.FlightplanContainerFullException;
import eu.mavinci.core.flightplan.FlightplanContainerWrongAddingException;
import eu.mavinci.core.flightplan.GPSFixType;
import eu.mavinci.core.flightplan.IFlightplanContainer;
import eu.mavinci.core.flightplan.IFlightplanDeactivateable;
import eu.mavinci.core.flightplan.IFlightplanPositionReferenced;
import eu.mavinci.core.flightplan.IFlightplanRelatedObject;
import eu.mavinci.core.flightplan.IFlightplanStatement;
import eu.mavinci.core.flightplan.IRecalculateable;
import eu.mavinci.core.flightplan.IReentryPoint;
import eu.mavinci.core.flightplan.LandingModes;
import eu.mavinci.core.flightplan.ReentryPoint;
import eu.mavinci.core.flightplan.ReentryPointID;
import eu.mavinci.core.flightplan.SpeedMode;
import eu.mavinci.core.flightplan.visitors.PreviousOfTypeVisitor;
import eu.mavinci.core.helper.MinMaxPair;
import eu.mavinci.core.plane.AirplaneCacheEmptyException;
import eu.mavinci.core.plane.ICAirplane;
import eu.mavinci.core.plane.PlaneConstants;
import eu.mavinci.core.plane.listeners.IAirplaneListenerPositionOrientation;
import eu.mavinci.core.plane.sendableobjects.PlaneInfo;
import eu.mavinci.core.plane.sendableobjects.PositionOrientationData;
import eu.mavinci.core.plane.sendableobjects.SingleHealthDescription;
import eu.mavinci.desktop.gui.widgets.IHintServiceBase;
import eu.mavinci.desktop.gui.widgets.IMProgressMonitor;
import eu.mavinci.desktop.helper.MathHelper;
import eu.mavinci.desktop.main.debug.Debug;
import eu.mavinci.flightplan.visitors.FirstPicAreaVisitor;
import eu.mavinci.geo.IPositionReferenced;
import eu.mavinci.plane.IAirplane;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.globes.Earth;
import gov.nasa.worldwind.globes.Globe;
import java.util.logging.Level;

public final class LandingPoint extends Point
        implements IPositionReferenced,
            IFlightplanPositionReferenced,
            IReentryPoint,
            IRecalculateable,
            IFlightplanDeactivateable {

    public static final String KEY = "eu.mavinci.flightplan.LandingPoint";
    public static final String KEY_LON = KEY + ".lon";
    public static final String KEY_LAT = KEY + ".lat";
    public static final String KEY_ALT = KEY + ".alt";
    public static final String KEY_ALT_BREAKOUT = KEY + ".altBreakout";
    public static final String KEY_ALT_TOUCHDOWN = KEY + ".altTouchdown";
    public static final String KEY_YAW = KEY + ".yaw";
    public static final String KEY_LANDING_ANGLE = KEY + ".landingAngleDeg";
    public static final String KEY_RADIUS = KEY + ".radius";
    public static final String KEY_MODE = KEY + ".mode";
    public static final String KEY_TO_STRING = KEY + ".toString";

    public static final double lengthLandLineInM = 500;
    public static final double endLandLineAltInM = -20;

    // in cm
    public static final int LandingCircleAltDef = 5500;
    public static final int LandingCircleAltitudeBreakoutDef = 10 * 100;

    public static final double LANDING_ANGLE_DEG_DEFAULT = 9;
    public static final double LANDING_ANGLE_DEG_MIN = 1;
    public static final double LANDING_ANGLE_DEG_MAX = 20;

    public static final double LANDING_ALT_MIN_OFFSET_TO_BREAKOUT_M = 35;
    public static final int LANDING_ALT_MIN_OFFSET_TO_BREAKOUT_CM = (int)(LANDING_ALT_MIN_OFFSET_TO_BREAKOUT_M * 100);

    public static final String breakup1 = Waypoint.SpecialPurposeBodyPrefix + "breakupWP1";
    public static final String breakup2 = Waypoint.SpecialPurposeBodyPrefix + "breakupWP2";
    public static final String breakup3 = Waypoint.SpecialPurposeBodyPrefix + "breakupWP3";
    public static final String breakupPath = Waypoint.SpecialPurposeBodyPrefix + "breakupLoop";
    public static final String downwind = Waypoint.SpecialPurposeBodyPrefix + "downwind";
    public static final String crosswind = Waypoint.SpecialPurposeBodyPrefix + "crosswind";

    public static final LandingModes DEFAULT_LANDING_MODES = LandingModes.DESC_STAYAIRBORNE;
    public static final LandingModes DefaultAllUserLevelMode = LandingModes.DESC_STAYAIRBORNE;

    public static final double ALT_SPOTLANDING_POINT_OF_NO_RETURN_METER = 20;

    private int id = ReentryPoint.INVALID_REENTRYPOINT;
    private LandingModes mode = DEFAULT_LANDING_MODES;
    private int altitude = LandingCircleAltDef;
    private int altitudeBreakout = LandingCircleAltitudeBreakoutDef;
    private double yaw = 0;
    private double landingAngleDeg = LANDING_ANGLE_DEG_DEFAULT;
    private double lastAutoLandingRefStartPosLat;
    private double lastAutoLandingRefStartPosLon;
    private double lastAutoLandingGroundLevelMeter = Double.MAX_VALUE;

    private static final ILanguageHelper languageHelper =
        DependencyInjector.getInstance().getInstanceOf(ILanguageHelper.class);

    private static final Globe globe =
        DependencyInjector.getInstance().getInstanceOf(IWWGlobes.class).getDefaultGlobe();

    @Deprecated
    public void setIdLegacy(int id) {
        this.id = id;
    }

    @Deprecated
    public void setModeLegacy(LandingModes mode) {
        this.mode = mode;
    }

    @Deprecated
    public void setAltitudeLegacy(int alt) {
        this.altitude = alt;
    }

    @Deprecated
    public void setYawLegacy(double yaw) {
        this.yaw = yaw;
    }

    @Deprecated
    public void setLandingAngleDegLegacy(double landingAngleDeg) {
        this.landingAngleDeg = landingAngleDeg;
    }

    @Deprecated
    public void setAltitudeBreakoutLegacy(int altBreakout) {
        this.altitudeBreakout = altBreakout;
    }

    @Deprecated
    public void setLastAutoLandingRefStartPosLatLegacy(double value) {
        this.lastAutoLandingRefStartPosLat = value;
    }

    @Deprecated
    public void setLastAutoLandingRefStartPosLonLegacy(double value) {
        this.lastAutoLandingRefStartPosLon = value;
    }

    @Deprecated
    public void setLastAutoLandingGroundLevelMeterLegacy(double value) {
        this.lastAutoLandingGroundLevelMeter = value;
    }

    public LandingPoint(Flightplan fp, double lat, double lon, LandingModes mode, double yaw, int id) {
        super(fp, lat, lon);
        this.mode = mode;
        this.yaw = yaw;
        this.id = id;
        reassignIDs();
    }

    public LandingPoint(double lat, double lon, LandingModes mode) {
        super(lat, lon);
        this.mode = mode;
        reassignIDs();
    }

    public LandingPoint(Flightplan fp, double lat, double lon) {
        super(fp, lat, lon);
        reassignIDs();
    }

    public LandingPoint(CFlightplan fp) {
        super(fp);
        reassignIDs();
    }

    public LandingPoint(double lat, double lon) {
        super(lat, lon);
        reassignIDs();
    }

    public LandingPoint(LandingPoint source) {
        super(source);
        this.altitude = source.altitude;
        this.altitudeBreakout = source.altitudeBreakout;
        this.id = source.id;
        this.mode = source.mode;
        this.yaw = source.yaw;
        this.landingAngleDeg = source.landingAngleDeg;
        this.lastAutoLandingGroundLevelMeter = source.lastAutoLandingGroundLevelMeter;
        this.lastAutoLandingRefStartPosLat = source.lastAutoLandingRefStartPosLat;
        this.lastAutoLandingRefStartPosLon = source.lastAutoLandingRefStartPosLon;
        reassignIDs();
    }

    public static double detLandingCircleRadius(ICAirplane plane) {
        double radius;
        try {
            radius = plane.getAirplaneCache().getConf().CONT_NAV_CIRCR / 100.;
        } catch (AirplaneCacheEmptyException | NullPointerException e) {
            radius = PlaneConstants.DEF_CONT_NAV_CIRCR / 100.;
        }

        return radius;
    }

    public static double detEndCircleAltInM(IAirplane plane) {
        double radius;
        try {
            radius = plane.getAirplaneCache().getConf().CONT_ALT_LANDINGALTITUDE / 100.;
        } catch (AirplaneCacheEmptyException e) {
            radius = PlaneConstants.DEF_CONT_ALT_LANDINGALTITUDE / 100.;
        }

        return radius;
    }

    @Override
    public void reassignIDs() {
        IFlightplanContainer parent = getParent();
        if (parent != null) {
            id = -1;
            CFlightplan pFlightP = parent.getFlightplan();
            Ensure.notNull(pFlightP, "pFlightP");
            setId(ReentryPointID.createNextSideLineID(pFlightP.getMaxUsedId()));
        }
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        if (this.id != id) {
            this.id = id;
            notifyStatementChanged(false);
        }
    }

    public Waypoint getBreakupWaypoint1() {
        if (mode != LandingModes.DESC_FULL3d) {
            return null;
        }

        WaypointLoop loop = getBreakupPath();
        if (loop == null) {
            return null;
        }

        int i = 0;
        if (i >= 0 && loop.getFromFlightplanContainer(i) instanceof Waypoint) {
            Waypoint wp = (Waypoint)loop.getFromFlightplanContainer(i);
            if (wp.getBody().equals(breakup1)) {
                return wp;
            }
        }

        return null;
    }

    public Waypoint getBreakupWaypoint2() {
        if (mode != LandingModes.DESC_FULL3d) {
            return null;
        }

        WaypointLoop loop = getBreakupPath();
        if (loop == null) {
            return null;
        }

        int i = 1;
        if (i >= 0 && loop.getFromFlightplanContainer(i) instanceof Waypoint) {
            Waypoint wp = (Waypoint)loop.getFromFlightplanContainer(i);
            if (wp.getBody().equals(breakup2)) {
                return wp;
            }
        }

        return null;
    }

    public Waypoint getBreakupWaypoint3() {
        if (mode != LandingModes.DESC_FULL3d) {
            return null;
        }

        WaypointLoop loop = getBreakupPath();
        if (loop == null) {
            return null;
        }

        int i = 2;
        if (i >= 0 && i < loop.sizeOfFlightplanContainer() && loop.getFromFlightplanContainer(i) instanceof Waypoint) {
            Waypoint wp = (Waypoint)loop.getFromFlightplanContainer(i);
            if (wp.getBody().equals(breakup3)) {
                return wp;
            }
        }

        return null;
    }

    public WaypointLoop getBreakupPath() {
        if (mode != LandingModes.DESC_FULL3d) {
            return null;
        }

        CFlightplan flightP = getFlightplan();
        Ensure.notNull(flightP, "flightP");
        int i = flightP.sizeOfFlightplanContainer() - 3;
        if (i >= 0 && flightP.getFromFlightplanContainer(i) instanceof WaypointLoop) {
            WaypointLoop loop = (WaypointLoop)flightP.getFromFlightplanContainer(i);
            if (loop.getBody().equals(breakupPath)) {
                return loop;
            }
        }

        // old position in data, make it compatible
        i = flightP.sizeOfFlightplanContainer() - 4;
        if (i >= 0 && flightP.getFromFlightplanContainer(i) instanceof WaypointLoop) {
            WaypointLoop loop = (WaypointLoop)flightP.getFromFlightplanContainer(i);
            if (loop.getBody().equals(breakupPath)) {
                return loop;
            }
        }

        return null;
    }

    public Waypoint getDownwindWaypoint() {
        if (mode != LandingModes.DESC_FULL3d) {
            return null;
        }

        CFlightplan flightP = getFlightplan();
        Ensure.notNull(flightP, "flightP");
        int i = flightP.sizeOfFlightplanContainer() - 4;
        if (i >= 0 && flightP.getFromFlightplanContainer(i) instanceof Waypoint) {
            Waypoint wp = (Waypoint)flightP.getFromFlightplanContainer(i);
            if (wp.getBody().equals(downwind)) {
                return wp;
            }
        }

        // old position in data, make it compatible
        i = flightP.sizeOfFlightplanContainer() - 3;
        if (i >= 0 && flightP.getFromFlightplanContainer(i) instanceof Waypoint) {
            Waypoint wp = (Waypoint)flightP.getFromFlightplanContainer(i);
            if (wp.getBody().equals(downwind)) {
                return wp;
            }
        }

        return null;
    }

    public Waypoint getCrosswindWaypoint() {
        if (mode != LandingModes.DESC_FULL3d) {
            return null;
        }

        CFlightplan flightP = getFlightplan();
        Ensure.notNull(flightP, "flightP");
        int i = flightP.sizeOfFlightplanContainer() - 2;
        if (i >= 0 && flightP.getFromFlightplanContainer(i) instanceof Waypoint) {
            Waypoint wp = (Waypoint)flightP.getFromFlightplanContainer(i);
            if (wp.getBody().equals(crosswind)) {
                return wp;
            }
        }

        return null;
    }

    public PreApproach getPreApproach() {
        if (mode != LandingModes.DESC_FULL3d) {
            return null;
        }

        CFlightplan flightP = getFlightplan();
        Ensure.notNull(flightP, "flightP");
        int i = flightP.sizeOfFlightplanContainer() - 1;
        if (i >= 0 && flightP.getFromFlightplanContainer(i) instanceof PreApproach) {
            return (PreApproach)flightP.getFromFlightplanContainer(i);
        }

        return null;
    }

    public int getIdLandingBegin() {
        CWaypointLoop loop = getBreakupPath();
        // System.out.println("LandingBegin:" + loop);
        if (loop == null) {
            return getId();
        } else {
            return loop.getId();
        }
    }

    public double getLastAutoLandingRefStartPosLat() {
        return lastAutoLandingRefStartPosLat;
    }

    public double getLastAutoLandingRefStartPosLon() {
        return lastAutoLandingRefStartPosLon;
    }

    public void setLastAutoLandingRefStartPosLat(double lastAutoLandingRefStartPosLat) {
        if (this.lastAutoLandingRefStartPosLat == lastAutoLandingRefStartPosLat) {
            return;
        }

        this.lastAutoLandingRefStartPosLat = lastAutoLandingRefStartPosLat;
        notifyStatementChanged(false);
    }

    public void setLastAutoLandingRefStartPosLon(double lastAutoLandingRefStartPosLon) {
        if (this.lastAutoLandingRefStartPosLon == lastAutoLandingRefStartPosLon) {
            return;
        }

        this.lastAutoLandingRefStartPosLon = lastAutoLandingRefStartPosLon;
        notifyStatementChanged(false);
    }

    public double getLastAutoLandingGroundLevelMeter() {
        return lastAutoLandingGroundLevelMeter;
    }

    public void setLastAutoLandingGroundLevelMeter(double lastAutoLandingGroundLevelMeter) {
        if (this.lastAutoLandingGroundLevelMeter == lastAutoLandingGroundLevelMeter) {
            return;
        }

        this.lastAutoLandingGroundLevelMeter = lastAutoLandingGroundLevelMeter;
        notifyStatementChanged(false);
    }

    public double getLandingAngleDeg() {
        return landingAngleDeg;
    }

    public void setLandingAngleDeg(double landingAngleDeg) {
        if (landingAngleDeg > LANDING_ANGLE_DEG_MAX) {
            landingAngleDeg = LANDING_ANGLE_DEG_MAX;
        }

        if (landingAngleDeg < LANDING_ANGLE_DEG_MIN) {
            landingAngleDeg = LANDING_ANGLE_DEG_MIN;
        }

        if (this.landingAngleDeg != landingAngleDeg) {
            this.landingAngleDeg = landingAngleDeg;
            notifyStatementChanged(false);
        }
    }

    // public static final int MINIMAL_LANDING_ALT_OVER_START_CM = 50*100;

    public int getAltWithinCM() {
        // return Math.max(MINIMAL_LANDING_ALT_OVER_START_CM,altitude);
        return altitude;
    }

    @Override
    public double getAltInMAboveFPRefPoint() {
        return getAltWithinCM() / 100.;
    }

    public int getAltBreakoutWithinCM() {
        return altitudeBreakout;
    }

    public double getAltBreakoutWithinM() {
        return getAltBreakoutWithinCM() / 100.;
    }

    public void setAltBreakoutWithinM(double altitudeBreakout) {
        setAltitudeBreakout((int)Math.round(altitudeBreakout * 100));
    }

    public void setAltitudeBreakout(int altitudeBreakout) {
        if (altitudeBreakout > CWaypoint.ALTITUDE_MAX_WITHIN_CM) {
            altitudeBreakout = CWaypoint.ALTITUDE_MAX_WITHIN_CM;
        }

        if (altitudeBreakout < CWaypoint.ALTITUDE_MIN_WITHIN_CM) {
            altitudeBreakout = CWaypoint.ALTITUDE_MIN_WITHIN_CM;
        }

        if (this.altitudeBreakout != altitudeBreakout) {
            this.altitudeBreakout = altitudeBreakout;
            notifyStatementChanged(false);
        }
    }

    private void maybeFixAltitude() {
        if (mode.isLandingAltitudeBreakoutRelevant()
                && mode.isLandingAltitudeRelevant()
                && altitudeBreakout + LANDING_ALT_MIN_OFFSET_TO_BREAKOUT_CM > altitude) {
            setAltitude(altitudeBreakout + LANDING_ALT_MIN_OFFSET_TO_BREAKOUT_CM);
        }
    }

    /**
     * landing YAW in degrees
     *
     * @return
     */
    public double getYaw() {
        return yaw;
    }

    public int getAltitude() {
        return altitude;
    }

    public double getAltInternalWithinM() {
        return altitude / 100.;
    }

    public double getAltInternalWithinCM() {
        return altitude;
    }

    @Override
    public void setAltInMAboveFPRefPoint(double altitude) {
        setAltitude((int)Math.round(altitude * 100));
    }

    @Override
    public boolean isStickingToGround() {
        return false;
    }

    public void setAltitude(int altitude) {
        if (altitude > CWaypoint.ALTITUDE_MAX_WITHIN_CM) {
            altitude = CWaypoint.ALTITUDE_MAX_WITHIN_CM;
        }

        if (altitude < CWaypoint.ALTITUDE_MIN_WITHIN_CM) {
            altitude = CWaypoint.ALTITUDE_MIN_WITHIN_CM;
        }

        if (this.altitude != altitude) {
            this.altitude = altitude;
            notifyStatementChanged(false);
        }
    }

    public static final int DEFAULT_FULL3D_LANDING_ALT_M = 50;
    public static final int DEFAULT_FULL3D_LANDING_ALT_CM = DEFAULT_FULL3D_LANDING_ALT_M * 100;

    public void setYaw(double yaw) {
        while (yaw >= 360) {
            yaw -= 360;
        }

        while (yaw < 0) {
            yaw += 360;
        }

        if (this.yaw != yaw) {
            this.yaw = yaw;
            notifyStatementChanged(false);
        }
    }

    public LandingModes getMode() {
        return mode;
    }

    public void setFromOther(LandingPoint lp) {
        setMode(lp.getMode());
        setAltitude(lp.getAltitude());
        setYaw(lp.getYaw());
    }

    public void setLatLon(LatLon latLon) {
        setLatLon(latLon.getLatitude().degrees, latLon.getLongitude().degrees);
    }

    @Override
    public String toString() {
        return languageHelper.getString(
            KEY_TO_STRING,
            lat,
            lon,
            getLandingModeI18N(mode),
            getAltInMAboveFPRefPoint() + "m",
            Math.round(getYaw()));
    }

    @Override
    public LatLon getLatLon() {
        return new LatLon(Angle.fromDegreesLatitude(lat), Angle.fromDegreesLongitude(lon));
    }

    public LatLon getEndOfCircling(IAirplane plane) {
        CFlightplan flightP = getFlightplan();
        Ensure.notNull(flightP, "flightP");
        double radiusInM =
            flightP.getHardwareConfiguration()
                .getPlatformDescription()
                .getTurnRadius()
                .convertTo(Unit.METER)
                .getValue()
                .doubleValue();
        return LatLon.rhumbEndPosition(
            getLatLon(), Angle.fromDegrees(yaw - 90), Angle.fromRadians(radiusInM / globe.getRadiusAt(getLatLon())));
    }

    /*public void setFromCurrentAirplane(IAirplane plane) throws AirplaneCacheEmptyException {
        setFromCurrentAirplane(plane, null,null,null);
    }*/

    public void setFromCurrentAirplane(
            IAirplane plane,
            IMProgressMonitor progressMonitor,
            IHintServiceBase hintService,
            ILanguageHelper languageHelper)
            throws AirplaneCacheEmptyException {
        PositionOrientationData po = plane.getAirplaneCache().getPositionOrientation();
        // shifting the position sidewards by circle radius
        CFlightplan flightP = getFlightplan();
        Ensure.notNull(flightP, "flightP");
        LatLon latLon = plane.getAirplaneCache().getCurLatLon();
        if (mode == LandingModes.DESC_CIRCLE) {
            double radiusInM =
                flightP.getHardwareConfiguration()
                    .getPlatformDescription()
                    .getTurnRadius()
                    .convertTo(Unit.METER)
                    .getValue()
                    .doubleValue();
            setYaw(po.yaw);
            setLatLon(
                LatLon.rhumbEndPosition(
                    latLon,
                    Angle.fromDegrees(plane.getAirplaneCache().getOrientation().yaw + 90),
                    Angle.fromRadians(radiusInM / globe.getRadiusAt(latLon))));
        } else if (mode == LandingModes.DESC_FULL3d) {
            boolean gpsOK = plane.isSimulation();
            if (!gpsOK) {
                int id_GPSquality = -1;
                PlaneInfo info = plane.getAirplaneCache().getPlaneInfo();
                for (int i = 0; i != info.healthDescriptions.size(); i++) {
                    SingleHealthDescription hd = info.healthDescriptions.get(i);
                    if (hd.name.equals(PlaneConstants.DEF_GPS_QUALITY)) {
                        id_GPSquality = i;
                        continue;
                    }
                }

                if (id_GPSquality != -1) {
                    GPSFixType fixType =
                        GPSFixType.values()[
                            (int)plane.getAirplaneCache().getHealthData().absolute.get(id_GPSquality).floatValue()];
                    gpsOK = fixType == GPSFixType.rtkFixedBL;
                }
            }

            if (!gpsOK) {
                /*if (GlobalSettings.isUserLevelAtLeast(GuiLevels.DEBUG)) {
                    int ret = JOptionPane.showConfirmDialog(UserNotificationHubSwing.getCurrentFocusWindow(),
                            UserNotificationHubSwing.wrapText(Language.getString(KEY + ".noRTK.msg")), Language.getString(KEY + ".noRTK.title"),
                            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, UserNotificationHubSwing.getWarningIcon());
                    if (ret != JOptionPane.YES_OPTION) {
                        return;
                    }
                } else {
                    Debug.getUserNotifier().handleWarningOrError(Language.getString(KEY + ".noRTKSTOP.msg"),
                            Language.getString(KEY + ".noRTKSTOP.title"));
                    return;
                }*/
                hintService.showAlert(
                    languageHelper.getString(
                        "com.intel.missioncontrol.ui.planning.landing.StartingLandingView.hintAskUavNoRTK"));
                return;
            }

            /*
            if (progressMonitor == null) {
                IMProgressMonitor swingProgressMonitor = new MProgressMonitor(UserNotificationHubSwing.getCurrentFocusWindow(), Language.getString(KEY + ".avgAltMon.msg"),
                                     Language.getString(KEY + ".avgAltMon.note.init"), 0, PressureAveraged.AVG_TIME);
                ThreadingHelper.getThreadingHelper().invokeConcurrentNow(new PressureAveraged(plane, swingProgressMonitor, hintService, languageHelper));
            } else {
                new PressureAveraged(plane, progressMonitor, hintService, languageHelper).run();
            }*/
            new PressureAveraged(plane, progressMonitor, hintService, languageHelper).run();
        } else {
            setYaw(po.yaw);
            setLatLon(latLon);
        }
    }

    @Override
    public boolean doSubRecalculationStage1() {
        return true;
    }

    @Override
    public boolean doSubRecalculationStage2() {
        updatePreApproach();
        CFlightplan cflightplan = getFlightplan();
        Ensure.notNull(cflightplan, "cflightplan");

        IFlightplanStatement tmp =
            cflightplan.sizeOfFlightplanContainer() > 0 ? cflightplan.getFromFlightplanContainer(0) : null;
        CStartProcedure start = null;
        if (tmp instanceof CStartProcedure) {
            start = (CStartProcedure)tmp;
            if (cflightplan.getHardwareConfiguration().getPlatformDescription().isInCopterMode()) {
                start.setHasOwnAltitude(mode == LandingModes.DESC_CIRCLE);
            } else {
                start.setHasOwnAltitude(mode == LandingModes.DESC_FULL3d);
            }
        }

        boolean changeFull3d = (mode == LandingModes.DESC_FULL3d) != (this.mode == LandingModes.DESC_FULL3d);
        if (changeFull3d) {
            if (mode == LandingModes.DESC_FULL3d) {
                if (getLastAutoLandingGroundLevelMeter() < Double.MAX_VALUE) {
                    altitudeBreakout = (int)Math.round(getLastAutoLandingGroundLevelMeter() * 100);
                }

                setAltitude(
                    getAltBreakoutWithinCM() + DEFAULT_FULL3D_LANDING_ALT_CM); // both cm here, so no conversion needed
                if (start != null) {
                    start.setHasOwnAltitude(true);
                    MinMaxPair alt = new MinMaxPair(getAltBreakoutWithinCM() + DEFAULT_FULL3D_LANDING_ALT_CM);
                    FirstPicAreaVisitor vis = new FirstPicAreaVisitor();
                    vis.startVisit(cflightplan);
                    if (vis.getPicArea() != null) {
                        alt.update(vis.getPicArea().getAlt() * 100);
                    }

                    int altI = (int)Math.round(alt.max);
                    start.setAltitude(
                        altI > LandingPoint.LandingCircleAltDef ? LandingPoint.LandingCircleAltDef : altI);
                }

                // add landing approach
                try {
                    Waypoint wpDown = new Waypoint(0, 0, cflightplan);
                    wpDown.setBody(downwind);
                    cflightplan.addToFlightplanContainer(wpDown);

                    WaypointLoop breakupPath = new WaypointLoop(1, 0, cflightplan);
                    breakupPath.setBody(LandingPoint.breakupPath);
                    cflightplan.addToFlightplanContainer(breakupPath);

                    Waypoint wpBreakup1 = new Waypoint(lat, lon, breakupPath);
                    wpBreakup1.setBody(breakup1);
                    breakupPath.addToFlightplanContainer(wpBreakup1);

                    Waypoint wpBreakup2 = new Waypoint(0, 0, breakupPath);
                    wpBreakup2.setBody(breakup2);
                    breakupPath.addToFlightplanContainer(wpBreakup2);

                    Waypoint wpBreakup3 = new Waypoint(0, 0, breakupPath);
                    wpBreakup3.setBody(breakup3);
                    breakupPath.addToFlightplanContainer(wpBreakup3);

                    Waypoint wpCross = new Waypoint(0, 0, cflightplan);
                    wpCross.setBody(crosswind);
                    cflightplan.addToFlightplanContainer(wpCross);

                    PreApproach pre = new PreApproach(lon, lat, altitude, cflightplan.getUnusedId());
                    cflightplan.addToFlightplanContainer(pre);
                    // updatePreApproach();//not nessesary,will be done by inform changeListener after mode change
                } catch (FlightplanContainerFullException | FlightplanContainerWrongAddingException e) {
                    Debug.getLog().log(Level.WARNING, "could not insert preApproach", e);
                }
            } else {
                // make sure breakout alt is not too low
                if (altitudeBreakout < LandingCircleAltitudeBreakoutDef) {
                    altitudeBreakout = LandingCircleAltitudeBreakoutDef;
                }

                // Remove landing approach
                CWaypointLoop breakupPath = getBreakupPath();
                if (breakupPath != null) {
                    cflightplan.removeFromFlightplanContainer(breakupPath);
                }

                CWaypoint wpDown = getDownwindWaypoint();
                if (wpDown != null) {
                    cflightplan.removeFromFlightplanContainer(wpDown);
                }

                CWaypoint wpCross = getCrosswindWaypoint();
                if (wpCross != null) {
                    cflightplan.removeFromFlightplanContainer(wpCross);
                }

                CPreApproach pre = getPreApproach();
                if (pre != null) {
                    cflightplan.removeFromFlightplanContainer(pre);
                }
            }
        }

        maybeFixAltitude();

        return true;
    }

    private class PressureAveraged implements Runnable, IAirplaneListenerPositionOrientation {

        static final int AVG_TIME = 10; // sec
        static final int MIN_PACK_COUNT = 2 * AVG_TIME;
        IMProgressMonitor mon;
        IAirplane plane;

        int count = 0;
        double sumAlt = 0;
        double sumLat = 0;
        double sumLon = 0;
        double yaw;
        IHintServiceBase hintService;
        ILanguageHelper languageHelper;

        public PressureAveraged(
                IAirplane plane,
                IMProgressMonitor monitor,
                IHintServiceBase hintService,
                ILanguageHelper languageHelper) {
            this.plane = plane;
            plane.addListener(this);
            mon = monitor;
            this.languageHelper = languageHelper;
            this.hintService = hintService;
        }

        @Override
        public void run() {
            for (int i = 0; i != AVG_TIME; i++) {
                if (mon.isCanceled()) {
                    return;
                }

                mon.setProgressNote(languageHelper.getString(KEY + ".avgAltMon.note", i, AVG_TIME), i);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
            }

            mon.close();
            if (count < MIN_PACK_COUNT) { // about one pressure measurement per second would be fine!
                hintService.showAlert(
                    languageHelper.getString(
                        "com.intel.missioncontrol.ui.planning.landing.StartingLandingView.hintAskUavNotEnoghData",
                        count,
                        MIN_PACK_COUNT));
                return;
            }

            plane.removeListener(this);
            synchronized (this) {
                final double alt = sumAlt /= count;
                final double lat = sumLat /= count;
                final double lon = sumLon /= count;
                Debug.getLog()
                    .config(
                        "averaging landing spot done with "
                            + count
                            + " samples in "
                            + AVG_TIME
                            + " sec: alt="
                            + alt
                            + "cm lat:"
                            + lat
                            + "° lon:"
                            + lon
                            + "°");
                Dispatcher.postToUI(
                    new Runnable() {

                        @Override
                        public void run() {
                            // make sure flight plan will not shifted much around vertically after setting this new
                            // alt's below!
                            try {
                                CFlightplan cFlightplan = getFlightplan();
                                Ensure.notNull(cFlightplan, "cFlightplan");
                                ((Flightplan)cFlightplan).getLandingpoint().updateFromUAV(plane);
                            } catch (AirplaneCacheEmptyException e1) {
                            }

                            setYaw(yaw);
                            // both cm here, so no conversion needed
                            setAltitudeBreakout((int)Math.round(alt));
                            setAltitude((int)Math.round(alt) + DEFAULT_FULL3D_LANDING_ALT_CM);
                            setLatLon(LatLon.fromDegrees(lat, lon));
                            try {
                                setLastAutoLandingRefStartPosLat(plane.getAirplaneCache().getStartLatBaro());
                                setLastAutoLandingRefStartPosLon(plane.getAirplaneCache().getStartLonBaro());
                                setLastAutoLandingGroundLevelMeter(alt / 100.);
                                Debug.getLog()
                                    .config(
                                        "set spot landing successfully: yaw="
                                            + yaw
                                            + " alt="
                                            + alt
                                            + "cm pos:"
                                            + getLatLon()
                                            + " refPos: lat="
                                            + plane.getAirplaneCache().getStartLatBaro()
                                            + "  lon="
                                            + plane.getAirplaneCache().getStartLonBaro()
                                            + " on FP "
                                            + getFlightplan());
                                hintService.showHint(
                                    languageHelper.getString(
                                        "com.intel.missioncontrol.ui.planning.landing.StartingLandingView.hintAskUavDone"),
                                    50);
                            } catch (AirplaneCacheEmptyException e) {
                                Debug.getLog()
                                    .log(
                                        Level.SEVERE,
                                        "plane starting position is not avaliable, auto landing mode will not work",
                                        e);
                            }
                        }
                    });
            }
        }

        @Override
        public synchronized void recv_positionOrientation(PositionOrientationData po) {
            count++;
            sumAlt += po.altitude;
            sumLat += po.lat;
            sumLon += po.lon;
            yaw = po.yaw;
        }

    }

    public void updateFromUAV(IAirplane plane) throws AirplaneCacheEmptyException {
        /** TODO implement for IDL */
    }

    public static final String getLandingModeI18N(LandingModes mode) {
        return languageHelper.getString(KEY_MODE + "." + mode);
    }

    public static final boolean isModeUserLevelOK(LandingModes mode, IPlatformDescription platformDescription) {
        switch (mode) {
        case DESC_CIRCLE:
            return true;
        case DESC_STAYAIRBORNE:
            return true;
        case DESC_HOLDYAW:
            return platformDescription.isInCopterMode();
        case DESC_PARACHUTE:
            return false;
        case DESC_FULL3d:
            // DONT check avaliable airplance chache stuff here, since otherwise loading of flightplans with wrong mode
            // would silently fail
            return platformDescription.getAPtype().canLinearClimbOnLine()
                && !platformDescription.isInCopterMode(); // && GlobalSettings.isInternalGui();
        default:
            return false;
        }
    }
    /*

        public static final boolean isModeUserLevelOK(LandingModes mode, IPlatformDescription plattform) {
            if (plattform.isInCopterMode()){//FALCON
                switch (mode) {
                    case DESC_CIRCLE:
                        return true;
                    case DESC_STAYAIRBORNE:
                        return true;
                    case DESC_HOLDYAW:
                    case DESC_PARACHUTE:
                        return false;
                    case DESC_FULL3d:
                        return false;
                    default:
                        return false;
                }
            } else {//Fixed wing
                switch (mode) {
                    case DESC_CIRCLE:
                        return true;
                    case DESC_STAYAIRBORNE:
                        return true;
                    case DESC_HOLDYAW:
                    case DESC_PARACHUTE:
                        return false;
                    case DESC_FULL3d:
                        return true;
                    default:
                        return false;
                }
            }
        }


        public static LandingModes[] getAvaliableLandingModes(IAirplane plane) {
            Vector<LandingModes> modes = new Vector<>();
            for (LandingModes mode : LandingModes.values()) {
                if (isModeUserLevelOK(mode, plane)) {
                    modes.add(mode);
                }
            }
            LandingModes[] arr = new LandingModes[modes.size()];
            for (int i = 0; i != modes.size(); i++) {
                arr[i] = modes.get(i);
            }
            return arr;
        }
    */

    @Override
    public Position getPosition() {
        if (mode == LandingModes.DESC_FULL3d) {
            return new Position(getLatLon(), getAltBreakoutWithinM());
        } else {
            return new Position(getLatLon(), getAltInMAboveFPRefPoint());
        }
    }

    public void setMode(LandingModes mode) {
        if (this.mode != mode) {
            this.mode = mode;
            notifyStatementChanged(false);
        }
    }

    @Override
    protected void notifyStatementChanged() {
        notifyStatementChanged(true);
    }

    private void notifyStatementChanged(boolean resetAutoLandRef) {
        super.notifyStatementChanged();

        if (resetAutoLandRef) {
            resetLastAutoLandingRefStartPos();
        }
    }

    private void resetLastAutoLandingRefStartPos() {
        setLastAutoLandingRefStartPosLat(0);
        setLastAutoLandingRefStartPosLon(0); // make this invalid!
    }

    private void updatePreApproach() {
        if (mode != LandingModes.DESC_FULL3d) {
            return;
        }

        CPreApproach pre = getPreApproach();
        CWaypointLoop breakupPath = getBreakupPath();
        CWaypoint wpBreakup1 = getBreakupWaypoint1();
        CWaypoint wpCross = getCrosswindWaypoint();
        CWaypoint wpDown = getDownwindWaypoint();
        CWaypoint wpBreakup2 = getBreakupWaypoint2();
        CWaypoint wpBreakup3 = getBreakupWaypoint3();
        if (pre == null
                || breakupPath == null
                || wpBreakup1 == null
                || wpCross == null
                || wpDown == null
                || wpBreakup2 == null
                || wpBreakup3 == null) {
            // toggling the mode will delete and reconstruct the landing approach stuff
            LandingModes old = mode;
            setMode(LandingModes.DESC_STAYAIRBORNE);
            setMode(old);

            pre = getPreApproach();
            breakupPath = getBreakupPath();
            wpBreakup1 = getBreakupWaypoint1();
            wpCross = getCrosswindWaypoint();
            wpDown = getDownwindWaypoint();
            wpBreakup2 = getBreakupWaypoint2();
            wpBreakup3 = getBreakupWaypoint3();
        }

        if (pre == null
                || breakupPath == null
                || wpBreakup1 == null
                || wpCross == null
                || wpDown == null
                || wpBreakup2 == null
                || wpBreakup3 == null) {
            return;
        }

        // System.out.println("\n\npre:"+pre);
        LatLon ref = getLatLon();
        // System.out.println("landP:" + ref);
        // keep in mind that the internal alt numbers are in cm
        Angle direction = Angle.fromDegrees(yaw + 180).normalizedLongitude();
        double dist = (altitude - altitudeBreakout) / 100. / Math.tan(Math.toRadians(landingAngleDeg));
        // System.out.println("dist:"+dist);
        Angle disAngle = Angle.fromRadians(dist / globe.getRadiusAt(ref));
        // System.out.println("disAngle:"+disAngle);
        LatLon prePos = LatLon.greatCircleEndPosition(ref, direction, disAngle);

        // System.out.println("prePos:"+prePos);
        pre.setLatLon(prePos.getLatitude().degrees, prePos.getLongitude().degrees);
        pre.setAltWithinCM(altitude);

        breakupPath.setIgnore(true);
        breakupPath.setCount(1);
        breakupPath.setTime(0);

        CFlightplan cflightplan = getFlightplan();
        Ensure.notNull(cflightplan, "cflightplan");
        int altBreakup = Math.max(cflightplan.getEventList().getAltWithinCM(), altitude);
        wpBreakup1.setLatLon(getLat(), getLon());
        wpBreakup1.setAltWithinCM(altBreakup);
        wpBreakup1.setAssertAltitude(AltAssertModes.jump);
        wpBreakup1.setIgnore(false);
        wpBreakup1.setSpeedMode(SpeedMode.normal);

        dist =
            cflightplan
                    .getHardwareConfiguration()
                    .getPlatformDescription()
                    .getTurnRadius()
                    .convertTo(Unit.METER)
                    .getValue()
                    .doubleValue()
                * 2;
        disAngle = Angle.fromRadians(dist / globe.getRadiusAt(ref));
        LatLon crossPos = LatLon.greatCircleEndPosition(prePos, direction, disAngle);
        // System.out.println("crossPos:"+crossPos);

        wpCross.setLatLon(crossPos.getLatitude().degrees, crossPos.getLongitude().degrees);
        wpCross.setAltWithinCM(altitude);
        wpCross.setAssertAltitude(AltAssertModes.unasserted);
        wpCross.setIgnore(false);
        wpCross.setSpeedMode(SpeedMode.slow);

        PreviousOfTypeVisitor vis = new PreviousOfTypeVisitor(wpDown, IFlightplanPositionReferenced.class);
        vis.setSkipIgnoredPaths(true);
        vis.startVisit(cflightplan);
        if (vis.prevObj != null) {
            IFlightplanPositionReferenced prev = (IFlightplanPositionReferenced)vis.prevObj;
            LatLon prevPos = LatLon.fromDegrees(prev.getLat(), prev.getLon());
            Angle headingToLanding =
                LatLon.ellipsoidalForwardAzimuth(
                    prevPos, crossPos, Earth.WGS84_EQUATORIAL_RADIUS, Earth.WGS84_POLAR_RADIUS);
            headingToLanding = headingToLanding.subtract(direction.subtract(Angle.POS180)).normalizedLongitude();
            if (headingToLanding.degrees < 0) {
                headingToLanding = Angle.NEG90;
            } else {
                headingToLanding = Angle.POS90;
            }

            direction = direction.add(headingToLanding).normalizedLongitude();
        } else {
            direction = direction.addDegrees(90).normalizedLongitude();
        }

        LatLon downPos = LatLon.greatCircleEndPosition(crossPos, direction, disAngle);
        wpDown.setLatLon(downPos.getLatitude().degrees, downPos.getLongitude().degrees);
        wpDown.setAltWithinCM(altitude);
        wpDown.setAssertAltitude(AltAssertModes.jump);
        wpDown.setIgnore(false);
        wpDown.setSpeedMode(SpeedMode.normal);
        wpDown.setAssertYawOn(true);
        wpDown.setAssertYaw(
            direction.degrees
                + 90
                + 10); // 10 degrees additionally to make sure he is allready behind the devision point
        // and is not accidentally trying to make another turn in the wrong direction

        wpBreakup2.setLatLon(downPos.getLatitude().degrees, downPos.getLongitude().degrees);
        wpBreakup2.setAltWithinCM(altBreakup);
        wpBreakup2.setAssertAltitude(AltAssertModes.unasserted);
        wpBreakup2.setIgnore(false);
        wpBreakup2.setSpeedMode(SpeedMode.normal);

        wpBreakup3.setLatLon(downPos.getLatitude().degrees, downPos.getLongitude().degrees);
        wpBreakup3.setAltWithinCM(altitude);
        wpBreakup3.setAssertAltitude(AltAssertModes.jump);
        wpBreakup3.setIgnore(false);
        wpBreakup3.setSpeedMode(SpeedMode.slow);
        wpBreakup3.setAssertYawOn(true);
        wpBreakup3.setAssertYaw(
            direction.degrees + 90 + 10); // 10 degrees additionally to make sure he is allready behind the devision
        // point and is not accidentally trying to make another turn in the wrong
        // direction

        // System.out.println("ready");

    }

    public boolean isLastAutoLandingRefStartPosValid(IAirplane plane) throws AirplaneCacheEmptyException {
        Debug.getLog()
            .config(
                "check: refPosLastAvg: lat="
                    + lastAutoLandingRefStartPosLat
                    + " lon="
                    + lastAutoLandingRefStartPosLon
                    + "  currentRef: lat="
                    + plane.getAirplaneCache().getStartLatBaro()
                    + "  lon="
                    + plane.getAirplaneCache().getStartLonBaro());
        return (Math.abs(plane.getAirplaneCache().getStartLatBaro() - lastAutoLandingRefStartPosLat) < 1e-10
            && Math.abs(plane.getAirplaneCache().getStartLonBaro() - lastAutoLandingRefStartPosLon)
                < 1e-10); // roughly more precise
        // than 1mm
    }

    public boolean isEmpty() {
        return lat == 0.0d && lon == 0.0d;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (o == null) {
            return false;
        }

        if (o instanceof LandingPoint) {
            LandingPoint landingpoint = (LandingPoint)o;
            return lat == landingpoint.lat
                && lon == landingpoint.lon
                && mode == landingpoint.mode
                && yaw == landingpoint.yaw
                && (altitude == landingpoint.altitude || !mode.isLandingAltitudeRelevant())
                && (altitudeBreakout == landingpoint.altitudeBreakout || !mode.isLandingAltitudeBreakoutRelevant())
                && id == landingpoint.id
                && landingAngleDeg == landingpoint.landingAngleDeg
                && lastAutoLandingRefStartPosLat == landingpoint.lastAutoLandingRefStartPosLat
                && lastAutoLandingRefStartPosLon == landingpoint.lastAutoLandingRefStartPosLon
                && lastAutoLandingGroundLevelMeter == landingpoint.lastAutoLandingGroundLevelMeter;
        } else {
            return false;
        }
    }

    @Override
    public IFlightplanRelatedObject getCopy() {
        return new LandingPoint(this);
    }

    /**
     * returns true if this is a fixed planned spot where the UAV will fly. one fuzzy situation is flying to actual
     * takeoff location and staying airborne their... this is considered as FALSE, since at planning time the point isnt
     * given jet.. it will only be inserted while sending a flight plan to the UAV
     *
     * @return
     */
    public boolean isPreplanned() {
        CFlightplan fp = getFlightplan();
        if (fp == null) {
            return true;
        }

        return !fp.getHardwareConfiguration().getPlatformDescription().isInCopterMode()
            || getMode() == LandingModes.DESC_CIRCLE;
    }

    @Override
    public void setActive(boolean active) {}

    public boolean isActive() {
        CFlightplan fp = getFlightplan();

        Ensure.notNull(fp);

        /*if (fp.isMeta()) {//this would cause a stack overflow recursion
            return false;
        }*/

        if (MathHelper.isDifferenceTiny(lat, 0) && MathHelper.isDifferenceTiny(lon, 0)) {
            return false;
        }

        switch (getMode()) {
        case DESC_CIRCLE:
            return true;
        case DESC_STAYAIRBORNE:

            // copters will stay airborne on last waypoint, fixedwing will go to startprocedure location==same as
            // landing but stay on alt
            if (fp.getHardwareConfiguration().getPlatformDescription().isInCopterMode()) return false;
            return true;

        case DESC_FULL3d:
            return true;
        case DESC_HOLDYAW:
            // copters will use this for auto landing on Same as start
            if (!fp.getHardwareConfiguration().getPlatformDescription().isInCopterMode()) return false;

            return true;
        default:
            return false;
        }
    }
}
