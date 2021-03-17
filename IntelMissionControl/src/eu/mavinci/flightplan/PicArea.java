/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.flightplan;

import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.concurrent.Dispatcher;
import com.intel.missioncontrol.hardware.IGenericCameraConfiguration;
import com.intel.missioncontrol.hardware.IGenericCameraDescription;
import com.intel.missioncontrol.hardware.IHardwareConfiguration;
import com.intel.missioncontrol.hardware.ILensDescription;
import com.intel.missioncontrol.hardware.IPlatformDescription;
import com.intel.missioncontrol.helper.DoubleHelper;
import com.intel.missioncontrol.helper.Ensure;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.map.elevation.IEgmModel;
import com.intel.missioncontrol.map.elevation.IElevationModel;
import com.intel.missioncontrol.map.worldwind.IWWGlobes;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.ui.notifications.Toast;
import com.intel.missioncontrol.ui.notifications.ToastType;
import com.intel.missioncontrol.utils.IBackgroundTaskManager;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import eu.mavinci.core.flightplan.AFlightplanContainer;
import eu.mavinci.core.flightplan.AltAssertModes;
import eu.mavinci.core.flightplan.AltitudeAdjustModes;
import eu.mavinci.core.flightplan.CFlightplan;
import eu.mavinci.core.flightplan.CPhotoSettings;
import eu.mavinci.core.flightplan.CPicArea;
import eu.mavinci.core.flightplan.CPicAreaCorners;
import eu.mavinci.core.flightplan.FlightplanContainerFullException;
import eu.mavinci.core.flightplan.FlightplanContainerWrongAddingException;
import eu.mavinci.core.flightplan.FlightplanSpeedModes;
import eu.mavinci.core.flightplan.IFlightplanContainer;
import eu.mavinci.core.flightplan.IFlightplanPositionReferenced;
import eu.mavinci.core.flightplan.IFlightplanRelatedObject;
import eu.mavinci.core.flightplan.IFlightplanStatement;
import eu.mavinci.core.flightplan.Orientation;
import eu.mavinci.core.flightplan.PlanType;
import eu.mavinci.core.flightplan.ReentryPointID;
import eu.mavinci.core.flightplan.visitors.NextOfTypeVisitor;
import eu.mavinci.core.flightplan.visitors.PreviousOfTypeVisitor;
import eu.mavinci.core.flightplan.visitors.UnsusedIdVisitor;
import eu.mavinci.core.helper.MinMaxPair;
import eu.mavinci.core.helper.StringHelper;
import eu.mavinci.core.helper.VectorNonEqual;
import eu.mavinci.desktop.gui.doublepanel.camerasettings.CameraHelper;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.MapLayerPicArea;
import eu.mavinci.desktop.gui.doublepanel.planemain.tree.maplayers.MapLayer;
import eu.mavinci.desktop.gui.wwext.LatLongInterpolationUtils;
import eu.mavinci.desktop.helper.IObjectiveFuntion;
import eu.mavinci.desktop.helper.MathHelper;
import eu.mavinci.desktop.main.debug.Debug;
import eu.mavinci.desktop.main.debug.profiling.requests.OptimizePicAreaRequest;
import eu.mavinci.flightplan.computation.AutoFPhelper;
import eu.mavinci.flightplan.computation.CorridorHelper;
import eu.mavinci.flightplan.computation.FlightLine;
import eu.mavinci.flightplan.computation.FlightplanVertex;
import eu.mavinci.flightplan.computation.LocalTransformationProvider;
import eu.mavinci.flightplan.computation.objectSurface.AllPointsResult;
import eu.mavinci.flightplan.computation.objectSurface.ObjectFlightplanAlg;
import eu.mavinci.flightplan.computation.objectSurface.VoxelGrid;
import eu.mavinci.flightplan.visitors.LineOfSightVisitor;
import eu.mavinci.flightplan.visitors.SectorVisitor;
import eu.mavinci.geo.CountryDetector;
import eu.mavinci.geo.ILatLonReferenced;
import eu.mavinci.geo.IPositionReferenced;
import eu.mavinci.geo.ISectorReferenced;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.OptionalDouble;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.checkerframework.checker.nullness.qual.Nullable;

/** Represents an Area of Interest and also provides methods for calculating flight plans. */
public class PicArea extends CPicArea implements ISectorReferenced, ITransformationProvider {

    public static final String KEY = "eu.mavinci.flightplan.PicArea";
    public static final String KEY_TO_STRING = KEY + ".toString";

    // public final static double assureCircAlt = 100; //meter

    private static final double minRadiusSpiral = 150; // unknown
    private static final double crossTrackToleranceRelToStripeWidth = 0.1; // unknown

    private final SectorVisitor secVis; // unknown
    private final AtomicBoolean needsRecomputeSector = new AtomicBoolean(false); // unknown

    protected Vector<Vec4> cornerList; // corners of AOI
    Vector<LatLon> corLatLon = null; // corners of AOI in global coordinates

    protected Vector<Vector<Vec4>> polygonsLocal = new Vector<Vector<Vec4>>(); // unknown
    protected LatLon refLatLon; // LatLon of reference point?
    protected double lengthY; // unknown
    protected double lengthX; // unknown

    double optimalSpeedThisAoi = Double.POSITIVE_INFINITY;
    Vector<LatLon> hull; // hull of what? the aoi

    // unknown. what do these transform? what is the difference between tansform4 and 3?
    Matrix transform4 = null;
    Matrix transform4Inv = null;
    Matrix transform3 = null;
    Matrix transform3Inv = null;

    double area = -1;
    double corridorLength = -1;
    // double alt;
    double sizeInFlight;
    double sizeInFlightEff;
    double sizeInFlightEffMax;
    double sizeParallelFlightEff;

    // public final static double overshoot = 50;//in meter
    double overshootParallelFlight;
    int idForInitPhoto;
    int nextFreeLineNo;
    double maxYenlarged;
    double maxXenlarged;
    double minYenlarged;
    double minXenlarged;

    // double camOvershoot;
    Vector<FlightLine> flightLines;
    double maxFlightLength;
    double lengthEstimation; // unknown, length of what? all waypoints?
    double overshootInnerLinesEnd; // unknown, does it interact with overshootParallelFlight?
    double overshootTotalLinesEnd; // unknown, does it interact with overshootParallelFlight?
    double centrencyParallelFlight;
    double centrencyInFlight;
    double sizeParallelFlight; // unknown, distance between parallel flight lines?
    CorridorHelper corridorHelper;
    double motionBlurrEst;
    double lastAcceptedReducedOverlapInFlightDirection =
        Double.POSITIVE_INFINITY; // unknown, hints at potential repeated calculations
    double lastAcceptedReducedOverlapInFlightDirectionMin =
        Double.POSITIVE_INFINITY; // unknown, hints at potential repeated calculations
    RecomputeErrors lastError;

    private final ILanguageHelper languageHelper =
        DependencyInjector.getInstance().getInstanceOf(ILanguageHelper.class);
    private final IElevationModel elevationModel =
        DependencyInjector.getInstance().getInstanceOf(IElevationModel.class);
    private final Globe globe = DependencyInjector.getInstance().getInstanceOf(IWWGlobes.class).getDefaultGlobe();
    private final IEgmModel egmModel = DependencyInjector.getInstance().getInstanceOf(IEgmModel.class);

    private double ADD_TOP_LINE_THRESHOLD;
    private double minY; // unknown, min of what?
    private double minX; // unknown, max of what?
    private double overlapInFlightMaxPossible;
    private boolean checkOverlapPossibleWarning;
    public boolean validSizeAOI = isValidSizeAOI();
    public boolean validAOI = true;

    public PicArea(IFlightplanContainer parent) {
        super(parent);
        DependencyInjector.getInstance()
            .getInstanceOf(ISettingsManager.class)
            .getSection(GeneralSettings.class)
            .operationLevelProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    flightplanStatementChanged(this);
                });
        setupTransform();
        secVis = new SectorVisitor(this);
    }

    public PicArea(int id, IFlightplanContainer parent) {
        super(id, parent);
        DependencyInjector.getInstance()
            .getInstanceOf(ISettingsManager.class)
            .getSection(GeneralSettings.class)
            .operationLevelProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    flightplanStatementChanged(this);
                });
        setupTransform();
        secVis = new SectorVisitor(this);
    }

    public PicArea(PicArea source) {
        super(source.id, null);

        for (IFlightplanStatement statement : source.elements) {
            IFlightplanRelatedObject statementCopy = statement.getCopy();
            this.elements.add((IFlightplanStatement)statementCopy);
            statementCopy.setParent(this);
        }

        this.corners = (CPicAreaCorners)source.corners.getCopy();
        this.corners.setParent(this);

        this.yaw = source.yaw;
        this.gsd = source.gsd;
        this.alt = source.alt;
        this.overlapInFlight = source.overlapInFlight;
        this.overlapInFlightMin = source.overlapInFlightMin;
        this.overlapParallel = source.overlapParallel;
        this.corridorMinLines = source.corridorMinLines;
        this.corridorWidthInMeter = source.corridorWidthInMeter;
        this.planType = source.planType;
        this.addCeiling = source.addCeiling;
        this.cropHeightMax = source.cropHeightMax;
        this.cropHeightMin = source.cropHeightMin;
        this.name = source.name;
        this.modelFilePath = source.modelFilePath;
        this.modelAxisAlignmentX = source.modelAxisAlignmentX;
        this.modelAxisAlignmentY = source.modelAxisAlignmentY;
        this.modelAxisAlignmentZ = source.modelAxisAlignmentZ;
        this.modelAxisOffsetX = source.modelAxisOffsetX;
        this.modelAxisOffsetY = source.modelAxisOffsetY;
        this.modelAxisOffsetZ = source.modelAxisOffsetZ;
        this.modelSource = source.modelSource;
        this.modelScale = source.modelScale;
        this.modelReferencePoint = (ReferencePoint)source.modelReferencePoint.getCopy();
        this.modelReferencePoint.setParent(this);
        this.modelAxisTransformations.addAll(source.modelAxisTransformations);
        this.minGroundDistance = source.minGroundDistance;
        this.minObjectDistance = source.minObjectDistance;
        this.maxObjectDistance = source.maxObjectDistance;
        this.facadeScanningSide = source.facadeScanningSide;
        this.objectHeight = source.objectHeight;
        this.enableCropHeightMax = source.enableCropHeightMax;
        this.enableCropHeightMin = source.enableCropHeightMin;
        this.circleLeftTrueRightFalse = source.circleLeftTrueRightFalse;
        this.circleCount = source.circleCount;
        this.scanDirection = source.scanDirection;
        this.startCapture = source.startCapture;
        this.jumpPattern = source.jumpPattern;
        this.verticalScanPattern = source.verticalScanPattern;
        this.startCaptureVertically = source.startCaptureVertically;
        this.onlySingleDirection = source.onlySingleDirection;
        this.cameraTiltToggleEnable = source.cameraTiltToggleEnable;
        this.cameraTiltToggleDegrees = source.cameraTiltToggleDegrees;
        this.cameraPitchOffsetDegrees = source.cameraPitchOffsetDegrees;
        this.cameraRollToggleEnable = source.cameraRollToggleEnable;
        this.cameraRollToggleDegrees = source.cameraRollToggleDegrees;
        this.cameraRollOffsetDegrees = source.cameraRollOffsetDegrees;
        this.maxYawRollChange = source.maxYawRollChange;
        this.maxPitchChange = source.maxPitchChange;
        this.restrictionCeilingEnabled = source.restrictionCeilingEnabled;
        this.restrictionCeiling = source.restrictionCeiling;
        this.restrictionCeilingRef = source.restrictionCeilingRef;
        this.restrictionFloorEnabled = source.restrictionFloorEnabled;
        this.restrictionFloor = source.restrictionFloor;
        this.restrictionFloorRef = source.restrictionFloorRef;
        this.pitchOffsetLineBegin = source.pitchOffsetLineBegin;

        this.windmill = new WindmillData(source.windmill);

        secVis = new SectorVisitor(this);
    }

    public PicArea(
            int id,
            IFlightplanContainer parent,
            double gsd,
            double overlapInFlight,
            double overlapInFlightMin,
            double overlapParallel) {
        super(id, parent, gsd, overlapInFlight, overlapInFlightMin, overlapParallel);
        setupTransform();
        secVis = new SectorVisitor(this);
    }

    public PicArea(
            IFlightplanContainer parent,
            double gsd,
            double overlapInFlight,
            double overlapInFlightMin,
            double overlapParallel) {
        super(parent, gsd, overlapInFlight, overlapInFlightMin, overlapParallel);
        setupTransform();
        secVis = new SectorVisitor(this);
    }

    @Override
    public boolean isAddableToFlightplanContainer(Class<? extends IFlightplanStatement> cls) {
        return super.isAddableToFlightplanContainer(cls) && !MapLayer.class.isAssignableFrom(cls);
    }

    @Override
    public boolean isAddableToFlightplanContainer(IFlightplanStatement statement) {
        return super.isAddableToFlightplanContainer(statement)
            && !MapLayer.class.isAssignableFrom(statement.getClass());
    }

    public double getOptimalSpeedThisAoi() {
        return optimalSpeedThisAoi;
    }

    public synchronized Vector<LatLon> getCornersVec() {
        if (corLatLon == null) {
            corLatLon =
                new Vector<LatLon>(
                    corners.sizeOfFlightplanContainer()); // workaround to prevent further nullPointer problems
            for (IFlightplanStatement corner : corners) {
                LatLon latLon = ((Point)corner).getLatLon();
                corLatLon.add(latLon);
            }
        }

        return corLatLon;
    }

    public double getCorridorWidthInMeterEffective() {
        /*double width = maxXenlarged - minXenlarged - 2 * overshootParallelFlight;
        if (MathHelper.isValid(width)) {
            return Math.max(width, corridorWidthInMeter);
        }*/

        return corridorWidthInMeter;
    }

    private Vector<LatLon> vecVec4toLatLon(Vector<Vec4> points) {
        Vector<LatLon> cornersS = new Vector<>();
        for (Vec4 v : points) {
            cornersS.addElement(transformToGlobe(v));
        }

        return cornersS;
    }

    @Override
    public double getCorridorWidthInMeter() {
        if (getPlanType() == PlanType.FACADE) {
            return 2;
        } else {
            return super.getCorridorWidthInMeter();
        }
    }

    public synchronized Vector<LatLon> getHull() {
        // System.out.println("Hull:"+hull);
        if (hull == null) {
            if (cornerList == null) {
                setupTransformNotSpreading();
            }

            Vector<Vec4> corVec = this.cornerList;

            // System.out.println("cornerList:"+cornerList);
            if (corVec == null) {
                hull = new Vector<>();
                area = 0;
                return hull;
            }

            switch (planType) {
            case CORRIDOR:
                if (corVec.size() < 2) {
                    area = 0;
                    hull = getCornersVec();
                    corridorLength = 0;
                } else {
                    Vector<Vec4> hullV =
                        corridorHelper.getHull(
                            getCorridorWidthInMeterEffective() / 2, -getCorridorWidthInMeterEffective() / 2);
                    Ensure.notNull(hullV, "hullV");
                    area = AutoFPhelper.computeArea(hullV);
                    hull = vecVec4toLatLon(hullV);

                    double corridorLength = 0;
                    Vec4 last = corVec.get(0);
                    for (int i = 1; i < corVec.size(); i++) {
                        Vec4 next = corVec.get(i);
                        corridorLength += next.distanceTo2(last);
                        last = next;
                    }

                    this.corridorLength = corridorLength;
                }

                break;
            case STAR:
            case TOWER:
            case WINDMILL:
            case POINT_OF_INTEREST:
            case PANORAMA:
            case NO_FLY_ZONE_CIRC:
            case GEOFENCE_CIRC:
                Vector<Vec4> cornersVS = new Vector<>();
                for (double yaw = 0; yaw < 360; yaw += 5) {
                    double yawRad = Math.toRadians(yaw);
                    Vec4 v =
                        new Vec4(
                            getCorridorWidthInMeter() * Math.sin(yawRad), getCorridorWidthInMeter() * Math.cos(yawRad));
                    cornersVS.add(v);
                }

                area = getCorridorWidthInMeter() * 2 * Math.PI;
                area *= cropHeightMax - cropHeightMin;

                if (addCeiling) {
                    area += getCorridorWidthInMeter() * getCorridorWidthInMeter() * Math.PI;
                }

                hull = vecVec4toLatLon(cornersVS);
                break;
            case SPIRAL:
                {
                    double a = sizeParallelFlightEff / (Math.PI * 2);
                    if (!isOnlySingleDirection()) {
                        a *= 2; // only every second line
                    }

                    double radius = minRadiusSpiral;
                    double yawRad = minRadiusSpiral / a;
                    double maxYawStepInit =
                        2 * Math.acos(1 - sizeParallelFlightEff * crossTrackToleranceRelToStripeWidth / radius);
                    double yawOffset = -yawRad - maxYawStepInit; // -Math.PI/2;
                    double maxRadius =
                        Math.max(
                            getCorridorWidthInMeter(),
                            minRadiusSpiral + sizeParallelFlightEff * (getCorridorMinLines() - 0.5));
                    double crossTrackToleranceRelToStripeWidth = 0.1;
                    Vector<Vec4> cornersV = new Vector<>();

                    double yawMax = 0;
                    while (radius <= maxRadius) {
                        radius = yawRad * a;
                        yawMax = yawRad;
                        double maxYawStep =
                            2 * Math.acos(1 - sizeParallelFlightEff * crossTrackToleranceRelToStripeWidth / radius);
                        yawRad += maxYawStep;
                    }

                    double pathLen = a / 2 * yawMax * yawMax;
                    pathLen -= overshootTotalLinesEnd;
                    yawMax = Math.sqrt(2 * pathLen / a);

                    radius = minRadiusSpiral;
                    yawRad = minRadiusSpiral / a;
                    crossTrackToleranceRelToStripeWidth = 0.01;

                    while (radius <= maxRadius && yawRad <= yawMax) {
                        radius = yawRad * a;
                        if (yawRad >= (yawMax - 2 * Math.PI)) {
                            Vec4 v =
                                new Vec4(
                                    (radius - overshootParallelFlight) * Math.sin(yawRad + yawOffset),
                                    (radius - overshootParallelFlight) * Math.cos(yawRad + yawOffset));
                            cornersV.add(v);
                        }

                        // double dist = a/2*yawRad*yawRad;
                        // System.out.println("yaw="+yawRad+ " "+radius);
                        double maxYawStep =
                            2 * Math.acos(1 - sizeParallelFlightEff * crossTrackToleranceRelToStripeWidth / radius);
                        yawRad += maxYawStep;
                    }

                    area = AutoFPhelper.computeArea(cornersV);
                    hull = vecVec4toLatLon(cornersV);
                    break;
                }
            case SEARCH:
                {
                    Vector<Vec4> cornersV = new Vector<>();
                    double radius = getCorridorWidthInMeter();
                    cornersV.add(new Vec4(radius, radius));
                    cornersV.add(new Vec4(-radius, radius));
                    cornersV.add(new Vec4(-radius, -radius));
                    cornersV.add(new Vec4(radius, -radius));

                    area = radius * 2;
                    area = area * area;
                    hull = vecVec4toLatLon(cornersV);
                    break;
                }
            case FACADE:
                area = 0;
                if (corVec.size() < 2) {
                    hull = getCornersVec();
                    this.corridorLength = 0;
                } else {
                    Vec4 last = corVec.firstElement();
                    Ensure.notNull(last, "last");
                    for (int i = 1; i < corVec.size(); i++) {
                        Vec4 cur = corVec.get(i);
                        area += last.distanceTo3(cur);
                        last = cur;
                    }

                    area *= cropHeightMax - cropHeightMin;

                    Vector<Vec4> hullvector =
                        corridorHelper.getHull(
                            getFacadeScanningSide() == FacadeScanningSide.right
                                ? -getCorridorWidthInMeter() / 2
                                : -CorridorHelper.MINIMAL_POSSIBLE_SHIFT,
                            getFacadeScanningSide() == FacadeScanningSide.left
                                ? getCorridorWidthInMeter() / 2
                                : CorridorHelper.MINIMAL_POSSIBLE_SHIFT);
                    Ensure.notNull(hullvector, "hullvector");
                    hull = vecVec4toLatLon(hullvector);

                    double corridorLength = 0;
                    Vec4 lastP = corVec.get(0);
                    for (int i = 1; i < corVec.size(); i++) {
                        Vec4 next = corVec.get(i);
                        corridorLength += next.distanceTo2(lastP);
                        lastP = next;
                    }

                    this.corridorLength = corridorLength;
                }

                break;
            case BUILDING:
                area = 0;
                Vec4 last = corVec.lastElement();
                for (int i = 0; i < corVec.size(); i++) {
                    Vec4 cur = corVec.get(i);
                    area += last.distanceTo3(cur);
                    last = cur;
                }

                area *= cropHeightMax - cropHeightMin;
                if (addCeiling) {
                    area += AutoFPhelper.computeArea(corVec);
                }

                hull = getCornersVec();
                break;
            default:
                // System.out.println("cornerList:"+cornerList);
                area = AutoFPhelper.computeArea(corVec);
                // System.out.println("getCornersVec()"+getCornersVec());
                hull = getCornersVec();
            }
        }

        return hull;
    }

    public synchronized Vector<LatLon> getHull(LatLon extraPoint) {
        if (extraPoint == null) {
            return getHull();
        }

        // System.out.println("extra:"+extraPoint);
        PicArea picArea = this.getCopy();
        picArea.setMute(true);
        // picArea.setParent(getFlightplan());

        try {
            picArea.getCorners()
                .addToFlightplanContainer(new Point(extraPoint.latitude.degrees, extraPoint.longitude.degrees));
        } catch (Exception e) {
            Debug.getLog().log(Level.WARNING, "cant simulate another added point", e);
            extraPoint = null;
        }
        // picArea.setupTransformNotSpreading();
        // picArea.computeFlightLinesNotSpreading(true);
        return picArea.getHull();
    }

    @Override
    public Sector getSector() {
        if (needsRecomputeSector.getAndSet(false)) {
            secVis.startVisit(this);
        }

        return secVis.getSector();
    }

    @Override
    public OptionalDouble getMaxElev() {
        if (needsRecomputeSector.getAndSet(false)) {
            secVis.startVisit(this);
        }

        return secVis.getMaxElev();
    }

    @Override
    public OptionalDouble getMinElev() {
        if (needsRecomputeSector.getAndSet(false)) {
            secVis.startVisit(this);
        }

        return secVis.getMinElev();
    }

    public PicAreaCorners getCorners() {
        return (PicAreaCorners)corners;
    }

    /** Size of the target area in m^2. */
    public synchronized double getArea() {
        if (area < 0) {
            getHull();
        }

        return area;
    }

    /** get the length of an corridor center line in m. */
    public synchronized double getCorridorLength() {
        if (corridorLength < 0) {
            getHull();
        }

        return corridorLength;
    }

    public Vec4 transformToLocalNonShift(LatLon latLon) {
        return globe.computePointFromLocation(latLon).transformBy4(transform4).transformBy3(transform3);
    }

    @Override
    public Vec4 transformToLocal(LatLon latLon) {
        return transformToLocalNonShift(latLon).subtract3(minX, minY, 0);
    }

    @Override
    public Vec4 transformToLocalInclAlt(Position pos) {
        return globe.computePointFromPosition(pos)
            .transformBy4(transform4)
            .transformBy3(transform3)
            .subtract3(minX, minY, 0);
    }

    @Override
    public Vec4 compensateCamCentrency(Vec4 vec, boolean isForward, boolean isRot90) {
        if (centrencyParallelFlight == 0 && centrencyInFlight == 0) {
            return vec;
        }

        int sign = isForward ? -1 : 1;
        if (isRot90) {
            return new Vec4(vec.x + sign * centrencyParallelFlight, vec.y + sign * centrencyInFlight);
        } else {
            return new Vec4(vec.x + sign * centrencyInFlight, vec.y + sign * centrencyParallelFlight);
        }
    }

    public double compensateCamCentrency(double parallelCoordinate, boolean isForward) {
        if (centrencyParallelFlight == 0) {
            return parallelCoordinate;
        }

        int sign = isForward ? -1 : 1;
        return parallelCoordinate + sign * centrencyParallelFlight;
    }

    @Override
    public Position transformToGlobe(Vec4 vec) {
        Ensure.notNull(vec, "vec");

        return globe.computePositionFromPoint(
            vec.add3(minX, minY, 0).transformBy3(transform3Inv).transformBy4(transform4Inv));
    }

    @Override
    public Vec4 transformToGlobalNorthing(Vec4 vec) {
        return vec.transformBy3(transform3Inv);
    }

    @Override
    public Vec4 transformToLocal(Vec4 vec) {
        return vec.transformBy3(transform3);
    }

    public boolean canTransform() {
        return transform4 != null;
    }

    public boolean setupTransformNotSpreading() {
        // Debug.printStackTrace();
        // Debug.printStackTrace("setup trafo");
        PicAreaCorners corners = getCorners();
        // Flightplan fp = getFlightplan();
        hull = null;
        corLatLon = null;
        area = -1;
        if (corners == null) { // || fp == null) {
            return false;
        }

        Vector<LatLon> corLatLon = getCornersVec();
        Sector s = corners.getSector();
        if (corners.sizeOfFlightplanContainer() == 0 || s == null) {
            transform4 = null;
            transform4Inv = null;
            transform3 = null;
            transform3Inv = null;

            cornerList = null;
            refLatLon = null;

            lengthY = 0;
            minY = 0;
            lengthX = 0;
            minX = 0;
            // renderable = new SurfacePolygonWithUserData(corLatLon);
            area = -1;
            return false;
        }

        if (planType == PlanType.COPTER3D) {
            refLatLon = modelReferencePoint.getLatLon();
        } else if (planType.getMinCorners() == 1) {
            refLatLon = corLatLon.firstElement();
        } else {
            refLatLon = s.getCentroid();
        }

        transform4Inv = globe.computeModelCoordinateOriginTransform(new Position(refLatLon, 0));
        transform4 = transform4Inv.getInverse();

        if (planType == PlanType.WINDMILL) {
            transform3 = Matrix.fromRotationZ(Angle.fromDegrees(-90)); // yaw has different meaning for WINDMILL
        } else {
            transform3 = Matrix.fromRotationZ(Angle.fromDegrees(getYaw() - 90));
        }

        transform3Inv = transform3.getInverse();

        Vector<Vec4> corVec = new Vector<Vec4>();
        Vector<Vec4> corVecNonShift = new Vector<Vec4>();
        lengthY = Double.NEGATIVE_INFINITY;
        minY = Double.POSITIVE_INFINITY;
        lengthX = Double.NEGATIVE_INFINITY;
        minX = Double.POSITIVE_INFINITY;

        // System.out.println("init transform");
        for (LatLon latLon : corLatLon) {
            Vec4 v = transformToLocalNonShift(latLon);
            corVecNonShift.add(v);
            double y = v.y;
            double x = v.x;
            lengthY = Math.max(lengthY, y);
            minY = Math.min(minY, y);
            lengthX = Math.max(lengthX, x);
            minX = Math.min(minX, x);
            // System.out.println("min y"+minY + " x"+minX + " len y" + lengthY + " x"+lengthX);
            if (planType.getMinCorners() == 1) {
                break;
            }
        }

        if (getPlanType() != PlanType.COPTER3D) {
            lengthX -= minX;
            lengthY -= minY;
            for (Vec4 v : corVecNonShift) {
                corVec.add(v.subtract3(minX, minY, 0));
            }
        } else {
            minX = 0;
            minY = 0;
        }

        // System.out.println("cornerList created" + cornerList);
        polygonsLocal.clear();
        polygonsLocal.add(corVec);
        /*if (planType == PlanType.CORRIDOR) {
            corridorHelper = new CorridorHelper(cornerList);
        } else {
            corridorHelper = null;
        }*/
        corridorHelper = new CorridorHelper(corVec, planType == PlanType.BUILDING);
        // System.out.println("end trafo");
        this.cornerList = corVec;

        return true;
    }

    @Override
    public double getHeightOffsetToGlobal() {
        return 0;
    }

    public void setupTransform() {
        if (!setupTransformNotSpreading()) {
            return;
        }
    }

    public double getSizeInFlightEff() {
        return sizeInFlightEff;
    }

    public double getSizeParallelFlightEff() {
        return sizeParallelFlightEff;
    }

    public void setSizeParallelFlightEff(double sizeParallelFlightEff) {
        this.sizeParallelFlightEff = sizeParallelFlightEff;
    }

    public Vector<FlightLine> getFlightLines() {
        return flightLines;
    }

    public void computeFlightlinesWithLastPlaneSilent() {
        computeFlightLines(true);
    }

    public double getMotionBlurrEst() {
        return motionBlurrEst;
    }

    public boolean computeFlightLinesNotSpreading(boolean silent) {
        // System.out.println("computeFlightLinesNotSpreading:" + silent);
        this.distMinComputed = DIST_MIN_DEFAULT;
        int minCorners = planType.getMinCorners();
        if (getCorners().sizeOfFlightplanContainer() < minCorners) {
            if (!silent) {
                lastError = RecomputeErrors.MSG_TOO_VIEW_CORNERS;
            }

            return false;
        }

        Flightplan flightplan = getFlightplan();
        Ensure.notNull(flightplan, "flightplan");
        // if this is a individual FP in a cell, crop the bounds,to the cell bounds, to reduce the overshoot

        MinMaxPair minMaxBoundsX = null;
        MinMaxPair minMaxBoundsY = null;

        PhotoSettings photoSettings = flightplan.getPhotoSettings();
        IHardwareConfiguration hardwareConfiguration = flightplan.getHardwareConfiguration();
        IGenericCameraConfiguration cameraConfiguration =
            hardwareConfiguration.getPrimaryPayload(IGenericCameraConfiguration.class);
        IGenericCameraDescription cameraDescription = cameraConfiguration.getDescription();
        IPlatformDescription platformDescription = hardwareConfiguration.getPlatformDescription();
        validSizeAOI = isValidSizeAOI();
        if (!validSizeAOI) {
            return false;
        } else if (!validAOI && isEmpty() && !getPlanType().hasWaypoints()) {
            lastError = RecomputeErrors.MSG_NO_WAYPOINTS;
        }

        double footprintShapeOvershoot;
        if (planType == PlanType.TARGET_POINTS) {
            altitudeGsdCalculator.setGsd(1);
        }

        altitudeGsdCalculator.recalculate();
        alt = altitudeGsdCalculator.getAlt();
        gsd = altitudeGsdCalculator.getGsd();

        double[] effectiveFootprint = CameraHelper.getSizeInFlight(alt, hardwareConfiguration);
        if (effectiveFootprint.length == 0) {
            if (!silent) {
                lastError = RecomputeErrors.MSG_CAM_NOT_MATCHABLE;
            }

            return false;
        }

        centrencyParallelFlight = effectiveFootprint[0]; // unknown, what is centrency?
        centrencyInFlight = effectiveFootprint[1]; // unknown, what is centrency?
        sizeParallelFlight = effectiveFootprint[2]; // unknown, size of what?
        sizeInFlight = effectiveFootprint[3]; // unknown, size of what?
        footprintShapeOvershoot = Math.abs(effectiveFootprint[4]); // unknown
        double pixelEnlargingCenter = effectiveFootprint[6]; // unknown, never used

        // this code is gone to the AltitudeGsdCalculator

        // unknown why this happens in different directions?
        centrencyParallelFlight -=
            cameraDescription.getOffsetToRightWing().convertTo(Unit.METER).getValue().doubleValue();
        centrencyInFlight += cameraDescription.getOffsetToTail().convertTo(Unit.METER).getValue().doubleValue();

        // unsure what it does, but probably somehow compensate for motion blur
        if (photoSettings.getMaxGroundSpeedAutomatic().isAutomaticallyAdjusting()) {
            compensateForMotionBlur(photoSettings, sizeInFlight);
        }

        // unknown intent, what does fuzzy mean?
        altitudeGsdCalculator.recalculateFuzzy(photoSettings.getMaxGroundSpeedMPSec());
        motionBlurrEst = altitudeGsdCalculator.getFuzzyTotal();

        if (!checkOverlapPossible(silent, platformDescription, sizeInFlight)) {
            // dont give computation up, this would be a silent faliure!
            // return false;
        }

        sizeInFlightEff =
            (1. - getOverlapInFlight() / 100.)
                * sizeInFlight; // recompute,since overlap might have changed // todo: recompute mentioned but there is
        // no recompute performed
        sizeInFlightEffMax = (1. - getOverlapInFlightMin() / 100.) * sizeInFlight;
        sizeParallelFlightEff = (1. - getOverlapParallel() / 100.) * sizeParallelFlight;

        // double overlapInFlight = sizeInFlight - sizeInFlightEff;
        // double overlapInFlightMin = sizeInFlight - sizeInFlightEffMax; //overlapInFlightMin < overlapInFlight

        // where does the -50 come from? with a 50 per-cent overlap, there is no overshoot?
        overshootParallelFlight = (getOverlapParallel() - 50) / 100. * sizeParallelFlight;

        // unknown intent
        double overshoot = platformDescription.getOvershoot().convertTo(Unit.METER).getValue().doubleValue();
        overshootInnerLinesEnd =
            (platformDescription.planIndividualImagePositions() ? 0 : overshoot) - sizeInFlightEff / 2; // camera
        // default
        // because of
        // curve radius
        // if (shiftAltitudes){
        // overshootInnerLinesEnd += Math.max(0, (camera.getTurnRadius() - sizeParallelFlightEff)); //intentionally
        // without times 2
        // }

        overshootTotalLinesEnd = (platformDescription.planIndividualImagePositions() ? 0 : overshoot);
        if (platformDescription.planIndividualImagePositions()) {
            // no additional offset needed
        } else if (!platformDescription.getAPtype().makesImagesBeforeAndAfterCorners()) {
            overshootTotalLinesEnd +=
                sizeInFlightEffMax / 2; // REMOVED because cam triggers directly before every curve now. OLD: because
            // maybe we can only trigger integer number of images, so make it one more, it
            // will get floored
            // todo: removed mentioned, but nothing removed?
        } else {
            overshootTotalLinesEnd +=
                platformDescription.getPlaneSpeed().convertTo(Unit.METER_PER_SECOND).getValue().doubleValue()
                    * cameraConfiguration
                        .getLens()
                        .getDescription()
                        .getMinRepTime()
                        .convertTo(Unit.SECOND)
                        .getValue()
                        .doubleValue()
                    / 2; // to make shure, that he has enough time
            // to shutter the last image on the spot
            // where we expect it
        }

        // why the distinction?
        if (platformDescription.isInCopterMode()) {
            overshootTotalLinesEnd +=
                (getOverlapInFlight() - 50) / 100. * sizeInFlight; // to make the innver picArea area everywhere
        } else {
            overshootTotalLinesEnd +=
                (getOverlapInFlightMin() - 50) / 100. * sizeInFlight; // to make the innver picArea area everywhere
        }
        // covered by the same number of images. /2 since
        // this can be put half half on both ends
        overshootTotalLinesEnd += footprintShapeOvershoot; // if footprint is not rectangular;

        // System.out.println("getOverlapInFlightMin:" + getOverlapInFlightMin());
        // System.out.println("overshootInnerLinesEnd=" + overshootInnerLinesEnd);
        // System.out.println("sizeInFlight=" + sizeInFlight);
        // System.out.println("overlapInFlight="+overlapInFlight);
        // System.out.println("overlapInFlightMin="+overlapInFlightMin);
        // System.out.println("sizeInFlightEffMax="+sizeInFlightEffMax);
        // System.out.println("sizeInFlightEff="+sizeInFlightEff);
        // System.out.println("footprintShapeOvershoot=" + footprintShapeOvershoot);
        //
        // System.out.println("overshootTotalLinesEnd="+overshootTotalLinesEnd);

        // System.out.println("sizeInFlightEffMinPossible="+sizeInFlightEffMinPossible);
        // System.out.println("sizeInFlight="+sizeInFlight);
        // System.out.println("overlapInFlightMaxPossible="+overlapInFlightMaxPossible);

        // double repTimeWithWind = sizeInFlightEff / (camera.getPlaneSpeed());
        // double repTimeAgainstWind = sizeXeff / ((camera.getPlaneSpeed()-photoSettings.getSpeed()));

        // System.out.println("sizeInFlight"+sizeInFlight);
        // System.out.println("sizeParallel"+sizeParallelFlight);
        // System.out.println("sizeInFlightEff"+sizeInFlightEff);
        // System.out.println("sizeParallelFlightEff"+sizeParallelFlightEff);
        // System.out.println("repTimeWithWind" + repTimeWithWind);
        // System.out.println("onlyInOneDirection" + onlyInOneDirection);

        // FirstPicAreaVisitor vis = new FirstPicAreaVisitor();
        // vis.startVisit(getFlightplan());

        UnsusedIdVisitor vis = new UnsusedIdVisitor(this);
        vis.startVisit(flightplan);
        vis.minMaxId.update(0); // make sure it's valid
        int firstFreeID = Math.max((int)vis.minMaxId.max + 1, -ReentryPointID.MAXIMAL_ALLOWED_NEGATIVE_ID);
        int firstFreeLineNo = vis.minMaxLineNo.isValid() ? (int)vis.minMaxLineNo.max + 1 : 0;

        if (firstFreeID >= ReentryPointID.maxValidID && firstFreeLineNo >= ReentryPointID.maxNoLines) {
            if (!silent) {
                Debug.getLog()
                    .severe(
                        "normal ids are already as large as autoMultiFP IDs. try to restart with an new empty flightplan!");
                lastError = RecomputeErrors.MSG_TOO_MANY_POINTS;
            }

            return false;
        }

        // int firstFreeID = getFlightplan().getUnusedId();
        idForInitPhoto =
            firstFreeID; // platz für init phot on und +1-3 ggf. für assert Hinweg alt, und für alte AP compatibilitäts
        // Photo
        // On stuff
        // firstFreeID = ReentryPointID.createNextPureID(firstFreeID+12);

        // Vector<Vec4> points = new Vector<Vec4>();
        flightLines = new Vector<FlightLine>();
        // Vector<Integer> ids = new Vector<Integer>();
        // Vector<Vec4> cornerList = new Vector<Vec4>();
        // cornerList.addAll(this.cornerList);
        // cornerList.add(this.cornerList.get(0));
        lengthEstimation = 0;

        if (!planType.canDoMultiPlans() && flightplan.getPhotoSettings().isMultiFP()) {
            flightplan.getPhotoSettings().setMultiFP(false);
        }

        int numberLines;
        int lineNo;

        switch (planType) {
        case STAR:
            { // scoped
                minYenlarged = -getCorridorWidthInMeter();
                maxYenlarged = +getCorridorWidthInMeter();

                minXenlarged = -getCorridorWidthInMeter();
                maxXenlarged = +getCorridorWidthInMeter();

                numberLines = 1;
                Vector<Vec4> points = new Vector<>();
                for (double yaw = 0; yaw < 180; yaw += Math.max(getYaw(), 1)) {
                    double yawRad = Math.toRadians(yaw);
                    Vec4 v =
                        new Vec4(
                            getCorridorWidthInMeter() * Math.sin(yawRad), getCorridorWidthInMeter() * Math.cos(yawRad));
                    points.add(v);
                    points.add(v.getNegative3());
                    points.add(v);
                }

                if (points.size() > 0) {
                    lineNo = 1;
                    FlightLine fl = new FlightLine(points, lineNo, sizeParallelFlightEff, this, true);
                    lineNo++;
                    flightLines.add(fl);
                    lengthEstimation += fl.getLength();
                }
            }
            // return false;
            break;
        case SPIRAL:
            { // scoped
                numberLines = 1;

                // double trackStep=30;
                // double yawStep=Math.toRadians(15);
                // double lastYaw=-1;

                Vector<Vector<Vec4>> polygons = new Vector<>();
                Vector<Vec4> pointsBase = new Vector<>();

                double maxRadius =
                    Math.max(
                        getCorridorWidthInMeter(),
                        minRadiusSpiral + sizeParallelFlightEff * (getCorridorMinLines() - 0.5));
                // int lastCircNo=0;

                minYenlarged = -maxRadius;
                maxYenlarged = +maxRadius;

                minXenlarged = -maxRadius;
                maxXenlarged = +maxRadius;

                lineNo = 1;
                // double dist;
                double a = sizeParallelFlightEff / (Math.PI * 2);
                if (!isOnlySingleDirection()) {
                    a *= 2; // only every second line
                }

                MinMaxPair minMaxInnerY = new MinMaxPair();
                // this constant describes the allowed cross track arror due to sampling of the Archimedean spiral
                // relatively to the typical distance of the stripes

                double radius = minRadiusSpiral;
                double yawRad = minRadiusSpiral / a;
                double maxYawStepInit =
                    2 * Math.acos(1 - sizeParallelFlightEff * crossTrackToleranceRelToStripeWidth / radius);
                double yawOffset = -yawRad - maxYawStepInit; // -Math.PI/2;
                while (radius <= maxRadius) {
                    radius = yawRad * a;
                    pointsBase.add(
                        new Vec4(radius * Math.sin(yawRad + yawOffset), radius * Math.cos(yawRad + yawOffset)));
                    // double dist = a/2*yawRad*yawRad;

                    double maxYawStep =
                        2 * Math.acos(1 - sizeParallelFlightEff * crossTrackToleranceRelToStripeWidth / radius);
                    yawRad += maxYawStep;
                }

                if (pointsBase.size() > 0) {
                    minMaxInnerY.update(pointsBase.get(0).y);
                    FlightLine fl = new FlightLine(pointsBase, lineNo, 0, this, true);
                    lineNo++;
                    flightLines.add(fl);
                    lengthEstimation += fl.getLength();
                    polygons.add(pointsBase);
                    pointsBase = new Vector<>();
                }

                double outerYaw = yawRad - Math.PI;

                yawRad = minRadiusSpiral / a;
                while (yawRad < outerYaw) {
                    radius = yawRad * a;
                    pointsBase.add(
                        new Vec4(
                            radius * Math.sin(yawRad + Math.PI + yawOffset),
                            radius * Math.cos(yawRad + Math.PI + yawOffset)));
                    // double dist = a/2*yawRad*yawRad;
                    // System.out.println("rad:" + radius + " " + yawRad);
                    double maxYawStep =
                        2 * Math.acos(1 - sizeParallelFlightEff * crossTrackToleranceRelToStripeWidth / radius);
                    yawRad += maxYawStep;
                }

                if (pointsBase.size() > 0) {
                    minMaxInnerY.update(pointsBase.get(0).y);
                    if (!isOnlySingleDirection()) {
                        FlightLine fl = new FlightLine(pointsBase, lineNo, sizeParallelFlightEff, this, true);
                        lineNo++;
                        flightLines.add(fl);
                        lengthEstimation += fl.getLength();
                        polygons.add(pointsBase);
                    }
                }

                numberLines =
                    (int)
                        Math.max(
                            Math.ceil(minMaxInnerY.size() / sizeParallelFlightEff),
                            Math.round(sizeParallelFlight / sizeParallelFlightEff));

                double offset = ((numberLines - 1) * sizeParallelFlightEff - minMaxInnerY.size()) / 2;
                double currentY = minMaxInnerY.max + offset;

                for (int i = 0; i != numberLines; i++) {
                    // System.out.println("i="+i);
                    // System.out.println("currentY;"+currentY);
                    // scan center of the line, and on neighbor lines
                    MinMaxPair minMaxX =
                        AutoFPhelper.getFlightlineIntersectionsX(polygonsLocal, currentY, sizeParallelFlightEff);
                    // scan on center between this and neigbot line. eg. on tiny AOI with only two lines in total, we
                    // would otherwise missto interset the AOI
                    minMaxX.enlarge(
                        AutoFPhelper.getFlightlineIntersectionsX(polygonsLocal, currentY, sizeParallelFlightEff / 2));

                    // System.out.println("FINAL: minX=" + minX + "\tmaxX="+maxX);
                    // System.out.println(minMaxPairsX.first + " " + minMaxPairsX.second);

                    if (minMaxX.isValid()) {
                        minMaxX.enlarge(-sizeParallelFlightEff / 2);
                        minMaxX.enlarge(overshootInnerLinesEnd);

                        // System.out.printf("currentY=%f X=%s\n", currentY, minMaxX);
                        FlightLine fl = new FlightLine(currentY, minMaxX, lineNo, this, false);
                        lineNo++;

                        flightLines.add(fl);
                        // firstFreeID+=4;

                        lengthEstimation += minMaxX.size();
                    }

                    currentY -= sizeParallelFlightEff;
                }

                if (flightLines.isEmpty()) {
                    lastError = RecomputeErrors.MSG_AREA_TOO_SMALL;
                    Debug.getLog()
                        .log(
                            Debug.WARNING,
                            "Could not compute flightplan for picture Area. Maybe it is too small to be rastered");
                    return false;
                }

                // lengthEstimation += AutoFPhelper.getDiameterMultiPol(polygonsLocal) + 2*alt;//way to and from
                // landingpoint
                // lengthEstimation += (numberLines-1)*sizeParallelFlightEff * FlightLine.computeJumpStep(camera,
                // sizeParallelFlightEff);
            }

            break;
        case SEARCH:
            { // scoped
                numberLines = 1;

                Vector<Vec4> corners = new Vector<>();
                int ySign = circleLeftTrueRightFalse ? 1 : -1;
                lineNo = 1;

                corners.add(new Vec4(0, 0));

                int circleCountMax =
                    Math.max(
                        1,
                        (int)Math.ceil((corridorWidthInMeter - 0.5 * sizeParallelFlightEff) / sizeParallelFlightEff));

                for (int circleCount = 1; circleCount <= circleCountMax; circleCount++) {
                    double radius = circleCount * sizeParallelFlightEff;
                    corners.add(new Vec4(radius, -ySign * (radius - sizeParallelFlightEff)));
                    corners.add(new Vec4(radius, ySign * radius));
                    corners.add(new Vec4(-radius, ySign * radius));
                    corners.add(new Vec4(-radius, -ySign * radius));
                    lengthEstimation += 8 * radius - (2 * sizeParallelFlightEff);
                }

                double radius = circleCountMax * sizeParallelFlightEff;
                corners.add(new Vec4(radius, -ySign * radius));
                lengthEstimation += 2 * radius;

                FlightLine fl = new FlightLine(corners, lineNo, 0, this, false);
                flightLines.add(fl);
            }

            break;
        case CORRIDOR:

            // make sure corridorHelper is not null
            Ensure.notNull(corridorHelper, "corridorHelper");

            numberLines =
                (int)
                    Math.max(
                        getCorridorMinLines(),
                        1
                            + Math.ceil(
                                (2 * overshootParallelFlight + getCorridorWidthInMeter()) / sizeParallelFlightEff));

            minYenlarged = 0;
            maxYenlarged = corridorHelper.getCenterLength();

            maxXenlarged = sizeParallelFlightEff * (numberLines - 1) / 2.;
            minXenlarged = -maxXenlarged;

            for (lineNo = 0; lineNo != numberLines; lineNo++) {
                // minus sign to reverse the line order, as sortIntoRows would expect this
                double shift = -(sizeParallelFlightEff * (lineNo - (numberLines - 1) / 2.));
                Vector<Vec4> line = corridorHelper.getShifted(shift);

                FlightLine fl = new FlightLine(line, lineNo, shift - minXenlarged, this, false);
                fl.enlarge(overshootTotalLinesEnd + centrencyInFlight, overshootTotalLinesEnd + centrencyInFlight);
                flightLines.add(fl);
                // firstFreeID+=4;
                lengthEstimation += fl.getLength();
            }

            if (numberLines % 2 == 1) {
                lengthEstimation += corridorHelper.getCenterLength();
            }

            lengthEstimation +=
                AutoFPhelper.getDiameterMultiPol(polygonsLocal) + 2 * alt; // way to and from landingpoint
            lengthEstimation +=
                (numberLines - 1)
                    * sizeParallelFlightEff
                    * FlightLine.computeJumpStep(platformDescription, sizeParallelFlightEff);

            break;

        case POLYGON:
        case CITY:
            {
                Boolean x = generateCityWayPoints(null, minMaxBoundsX, minMaxBoundsY, platformDescription);
                if (x != null) {
                    return x;
                }

                break;
            }
        case BUILDING:
        case FACADE:
            {
                boolean circleLeftTrueRightFalse = this.circleLeftTrueRightFalse;
                if (startCaptureVertically.equals(StartCaptureVerticallyTypes.up)) {
                    circleLeftTrueRightFalse = !circleLeftTrueRightFalse;
                }

                ADD_TOP_LINE_THRESHOLD = 0.3 * sizeInFlightEff;

                lineNo = 0;
                // corridorHelper = new CorridorHelper(this.cornerList, planType == PlanType.BUILDING);

                Vector<Vec4> pointsAdd = null;
                Vector<Orientation> directionsAdd = null;
                Vec4 lastLowerLevel = null;

                Vector<Vec4> pointsBase = new Vector<>();
                Vector<Orientation> directionsBase = new Vector<>();

                Vector<Vec4> pathSafetyDist;
                Vector<Vec4> polyNoGo;
                double sign = 1;
                // System.out.println("facadeScanningSide:"+facadeScanningSide);

                Vector<Vec4> corVecEnlarged = new Vector<>(cornerList.size());
                corVecEnlarged.addAll(cornerList);

                if (planType == PlanType.BUILDING) {
                    if (AutoFPhelper.computeAreaWithSign(cornerList) > 0) {
                        sign = -1;
                    }

                    polyNoGo = corridorHelper.getShifted(sign * (minObjectDistance + 1.2));
                    pathSafetyDist = polyNoGo;
                } else { // FACADE
                    setYaw(0);
                    if (facadeScanningSide == FacadeScanningSide.left) {
                        sign = -1;
                    }

                    FlightLine.enlargePolygone(
                        corVecEnlarged,
                        overshootParallelFlight - centrencyParallelFlight,
                        overshootParallelFlight + centrencyParallelFlight);

                    pathSafetyDist = new Vector<>();
                    pathSafetyDist.addAll(corridorHelper.getShifted(sign * (minObjectDistance + 1.2)));
                    FlightLine.enlargePolygone(
                        pathSafetyDist,
                        overshootParallelFlight - centrencyParallelFlight + alt,
                        overshootParallelFlight + centrencyParallelFlight + alt);

                    polyNoGo = corridorHelper.getShifted(-sign * 2 * CorridorHelper.MINIMAL_POSSIBLE_SHIFT);
                    FlightLine.enlargePolygone(polyNoGo, alt, alt);
                    Collections.reverse(polyNoGo);
                    polyNoGo.addAll(pathSafetyDist);
                }

                Ensure.notNull(pathSafetyDist, "pathSafetyDist");
                Ensure.notNull(polyNoGo, "polyNoGo");

                double altMin =
                    Math.max(
                        Math.max(cropHeightMin, minGroundDistance),
                        platformDescription.getMinGroundDistance().convertTo(Unit.METER).getValue().doubleValue());

                double minCropToMinGroundDist = minGroundDistance - cropHeightMin;

                ArrayList<ArrayList<TemporaryWaypoint>> waypoints =
                    generateWaypoints(
                        corVecEnlarged,
                        polyNoGo,
                        pathSafetyDist,
                        sign,
                        altMin,
                        minCropToMinGroundDist,
                        alt,
                        circleLeftTrueRightFalse);
                makeFlightLines(waypoints, getVerticalScanPattern(), planType);

                if (addCeiling) {
                    pointsAdd = new Vector<>();
                    directionsAdd = new Vector<>();

                    lastLowerLevel = flightLines.lastElement().getCorners().lastElement();

                    pointsBase.clear();
                    directionsBase.clear();

                    // first the circles on the roof edge
                    boolean reverse = !(circleLeftTrueRightFalse == (sign > 0));
                    double signOrg = sign;
                    if (reverse) {
                        sign *= -1;
                    }

                    int tiltSteps = (int)Math.ceil((90 + cameraPitchOffsetDegrees) / maxPitchChange);
                    double tiltStepsDeg = (90 + cameraPitchOffsetDegrees) / tiltSteps;
                    if (planType == PlanType.FACADE) {
                        tiltSteps++;
                    }

                    for (int s = 1; s < tiltSteps; s++) {
                        double tiltDeg = s * tiltStepsDeg;
                        double tiltRad = Math.toRadians(tiltDeg);

                        double curAlt = cropHeightMax + alt * Math.sin(tiltRad);
                        Vector<Vec4> path = corridorHelper.getShifted(signOrg * alt * Math.cos(tiltRad));
                        if (planType == PlanType.FACADE) {
                            FlightLine.enlargePolygone(
                                path,
                                overshootParallelFlight - centrencyParallelFlight,
                                overshootParallelFlight + centrencyParallelFlight);
                        }

                        if (reverse) {
                            Collections.reverse(path);
                        }

                        Ensure.notNull(path, "path");
                        // roof line looking 45°
                        Vec4 last = planType == PlanType.FACADE ? path.get(1) : path.get(path.size() - 2);
                        Vec4 next = path.firstElement();
                        Vec4 diffLast = next.subtract3(last).normalize3();
                        if (planType == PlanType.FACADE) {
                            diffLast = diffLast.multiply3(-1);
                        }

                        for (int i = 1; i < path.size(); i++) {
                            last = next;
                            next = path.get(i);
                            Vec4 diff = next.subtract3(last);
                            double len = diff.getLength3();
                            if (len == 0) continue;
                            diff = diff.multiply3(1 / len);
                            double yaw = Math.toDegrees(Math.atan2(diff.x, diff.y)) + getYaw() - 90 + 90 * sign;

                            Vec4 v = last;
                            v = new Vec4(v.x, v.y, curAlt);
                            Vec4 diffAvg = diffLast.add3(diff);
                            double yawAvg =
                                Math.toDegrees(Math.atan2(diffAvg.x, diffAvg.y)) + getYaw() - 90 + 90 * sign;
                            Orientation o = new Orientation(0, 90 - tiltDeg, yawAvg);
                            pointsBase.addElement(v);
                            directionsBase.add(o);

                            diffLast = diff;
                            int noImg = (int)Math.ceil(len / sizeParallelFlightEff);
                            double step = len / noImg;
                            if (planType == PlanType.FACADE && i == path.size() - 1) {
                                noImg++;
                            }

                            double dist = step;
                            for (int n = 1; n < noImg; n++) {
                                v = last.add3(diff.multiply3(dist));
                                v = new Vec4(v.x, v.y, curAlt);
                                o = new Orientation(0, 90 - tiltDeg, yaw);
                                pointsBase.addElement(v);
                                directionsBase.add(o);
                                dist += step;
                            }
                        }

                        // find element which is as closest to lastLowerLevel
                        if (planType == PlanType.FACADE) {
                            if (lastLowerLevel.distanceTo3(pointsBase.firstElement())
                                    > lastLowerLevel.distanceTo3(pointsBase.lastElement())) {
                                Collections.reverse(pointsBase);
                                Collections.reverse(directionsBase);
                            }

                            FlightLine fl = new FlightLine(pointsBase, directionsBase, alt, lineNo++, this);
                            flightLines.add(fl);
                            lastLowerLevel = fl.getCorners().lastElement();
                        } else {
                            double minDist = Double.POSITIVE_INFINITY;
                            int bestI = -1;
                            for (int i = 0; i != pointsBase.size(); i++) {
                                double dist = lastLowerLevel.distanceTo3(pointsBase.get(i));
                                if (dist < minDist) {
                                    minDist = dist;
                                    bestI = i;
                                }
                            }

                            for (int i = 0; i != pointsBase.size(); i++) {
                                int k = (-i + bestI) % pointsBase.size();
                                if (k < 0) {
                                    k += pointsBase.size();
                                }

                                pointsAdd.add(pointsBase.get(k));
                                directionsAdd.add(directionsBase.get(k));
                            }

                            FlightLine fl = new FlightLine(pointsAdd, directionsAdd, alt, lineNo++, this);
                            flightLines.add(fl);
                        }

                        pointsBase.clear();
                        directionsBase.clear();
                        pointsAdd.clear();
                        directionsAdd.clear();
                    }

                    double curAlt = cropHeightMax + alt;

                    // rasterize roof
                    if (planType != PlanType.FACADE) {
                        numberLines = (int)Math.round(this.lengthY / sizeParallelFlightEff);
                        if (numberLines < 1) {
                            numberLines = 1;
                        }
                        // System.out.println("numLines:" +numberLines+ " lengthY:"+lengthY);
                        double stepY = this.lengthY / numberLines;

                        double currentY = 0;

                        for (lineNo = 0; lineNo <= numberLines; lineNo++) {
                            // System.out.println("============ line No:"+lineNo + " curY:"+currentY);
                            MinMaxPair minMaxX =
                                AutoFPhelper.getFlightlineIntersectionsX(polygonsLocal, currentY, stepY / 2);
                            // System.out.println("minMaxX:"+minMaxX);
                            if (minMaxX.isValid()) {
                                int numImgs = (int)Math.ceil(minMaxX.size() / sizeInFlightEff);
                                if (numImgs < 1) {
                                    numImgs = 1;
                                }

                                double stepX = minMaxX.size() / numImgs;
                                sign = lineNo % 2 == 0 ? 1 : -1;
                                // System.out.println("numImgs:"+numImgs );
                                double currentX = sign < 0 ? minMaxX.min : minMaxX.max;
                                for (int imgNo = 0; imgNo <= numImgs; imgNo++) {

                                    // System.out.println("curX: " +currentX + " stepX"+stepX);
                                    Vec4 v = new Vec4(currentX, currentY, curAlt);
                                    // v= v.transformBy3(m);
                                    Orientation o = new Orientation(0, 0, getYaw() - 90 - 90 * sign);
                                    // System.out.println("V:"+v + " o:"+o);
                                    pointsBase.addElement(v);
                                    directionsBase.add(o);
                                    currentX -= stepX * sign;
                                    // break;
                                }
                            }

                            currentY += stepY;
                        }

                        FlightLine fl = new FlightLine(pointsBase, directionsBase, alt, lineNo++, this);
                        flightLines.add(fl);
                        pointsBase.clear();
                        directionsBase.clear();
                    }
                }
                // FlightLine.mirrowAll(flightLines);
                // Collections.reverse(flightLines);

                break;
            }
        case POINT_OF_INTEREST:
        case PANORAMA:
            {
                double radius = cameraDescription.getOffsetToTail().convertTo(Unit.METER).getValue().doubleValue();
                int lines;
                double yawOffset;
                if (planType == PlanType.POINT_OF_INTEREST) {
                    radius += alt + corridorWidthInMeter;
                    lines =
                        (int)
                            Math.max(
                                Math.ceil(2 * Math.PI / (sizeParallelFlightEff / corridorWidthInMeter)),
                                getCorridorMinLines());
                    yawOffset = Math.PI / 2.0;
                } else { // PANORAMA
                    double openingAngleRad =
                        2
                            * Math.tan(
                                hardwareConfiguration
                                        .getPrimaryPayload(IGenericCameraConfiguration.class)
                                        .getDescription()
                                        .getCcdWidth()
                                        .convertTo(Unit.MILLIMETER)
                                        .getValue()
                                        .doubleValue()
                                    / 2
                                    / hardwareConfiguration
                                        .getPrimaryPayload(IGenericCameraConfiguration.class)
                                        .getLens()
                                        .getDescription()
                                        .getFocalLength()
                                        .convertTo(Unit.MILLIMETER)
                                        .getValue()
                                        .doubleValue());
                    openingAngleRad *= 1 - (getOverlapParallel() / 100.);
                    lines = (int)Math.ceil(2 * Math.PI / openingAngleRad);
                    yawOffset = -Math.PI / 2.0;
                }

                double step = 2 * Math.PI / lines;

                Vector<Vec4> pointsBase = new Vector<>();
                Vector<Orientation> directionsBase = new Vector<>();
                double curAlt =
                    Math.max(
                        cropHeightMax,
                        platformDescription.getMinGroundDistance().convertTo(Unit.METER).getValue().doubleValue());
                for (int k = 0; k < lines; k++) {
                    double yawRad = yawOffset + step * k * (circleLeftTrueRightFalse ? -1 : 1);
                    Vec4 v = new Vec4(radius * Math.sin(yawRad), radius * Math.cos(yawRad), curAlt);
                    Orientation o = new Orientation(0, 90, Math.toDegrees(yawRad) + getYaw() + 90);
                    // System.out.println("V:" + v + " o:" + o);
                    pointsBase.addElement(v);
                    directionsBase.add(o);
                }

                FlightLine fl = new FlightLine(pointsBase, directionsBase, radius, 0, this);

                flightLines.add(fl);
                // System.out.println("flightlines:" + flightLines);
                break;
            }
        case TOWER:
            {
                boolean circleLeftTrueRightFalse = this.circleLeftTrueRightFalse;
                if (startCaptureVertically.equals(StartCaptureVerticallyTypes.up)) {
                    circleLeftTrueRightFalse = !circleLeftTrueRightFalse;
                }

                double altMin =
                    Math.max(
                        platformDescription.getMinGroundDistance().convertTo(Unit.METER).getValue().doubleValue(),
                        Math.max(cropHeightMin, minGroundDistance));

                int lines =
                    (int)
                        Math.max(
                            Math.ceil(2 * Math.PI / (sizeParallelFlightEff / corridorWidthInMeter)),
                            getCorridorMinLines());
                lines = Math.max(lines, (int)Math.ceil(360 / maxYawRollChange));
                double step = 2 * Math.PI / lines;
                lineNo = 0;
                Vector<Vec4> pointsBase = new Vector<>();
                Vector<Orientation> directionsBase = new Vector<>();

                double radius =
                    alt
                        + corridorWidthInMeter
                        + cameraDescription.getOffsetToTail().convertTo(Unit.METER).getValue().doubleValue();
                double minCropToMinGroundDist = minGroundDistance - cropHeightMin;

                ArrayList<ArrayList<TemporaryWaypoint>> waypoints = new ArrayList<>();
                generateWaypointsTower(
                    waypoints, minCropToMinGroundDist, altMin, step, radius, lines, circleLeftTrueRightFalse);
                makeFlightLines(waypoints, getVerticalScanPattern(), planType);

                if (addCeiling) {
                    int tiltSteps = (int)Math.ceil((90 + cameraPitchOffsetDegrees) / maxPitchChange);
                    double tiltStepsDeg = (90 + cameraPitchOffsetDegrees) / tiltSteps;
                    for (int i = 1; i < tiltSteps; i++) {
                        Vec4 last = flightLines.lastElement().getCorners().lastElement();
                        double yawRadStart = -Math.atan2(-last.y, -last.x);

                        double tiltDeg = i * tiltStepsDeg;
                        double tiltRad = Math.toRadians(tiltDeg);

                        lines =
                            (int)
                                Math.max(
                                    Math.ceil(2 * Math.PI / (sizeParallelFlightEff / corridorWidthInMeter)),
                                    getCorridorMinLines());
                        lines = Math.max(lines, (int)Math.ceil(360 / maxYawRollChange));
                        step = 2 * Math.PI / lines;

                        radius = corridorWidthInMeter + alt * Math.cos(tiltRad);
                        double curAlt = cropHeightMax + alt * Math.sin(tiltRad);
                        for (int k = 0; k < lines; k++) {
                            double yawRad = yawRadStart + step * k * (circleLeftTrueRightFalse ? -1 : 1);
                            Vec4 v = new Vec4(radius * -Math.cos(yawRad), radius * Math.sin(yawRad), curAlt);
                            Orientation o = new Orientation(0, 90 - tiltDeg, Math.toDegrees(yawRad) + getYaw());
                            // System.out.println("V:"+v + " o:"+o);
                            pointsBase.addElement(v);
                            directionsBase.add(o);
                        }

                        FlightLine fl = new FlightLine(pointsBase, directionsBase, alt, lineNo++, this);
                        if (planType.useStartCaptureVertically()) {
                            fl.resetDerrivates();
                        }

                        flightLines.add(fl);
                        pointsBase.clear();
                        directionsBase.clear();
                    }

                    // rasterize top polygone
                    double curAlt = cropHeightMax + alt;

                    Vec4 last = flightLines.lastElement().getCorners().lastElement();
                    double yawRadStart = Math.atan2(-last.y, last.x) + Math.PI;

                    if (corridorWidthInMeter * 2 < sizeInFlightEff) {
                        // single point on top:
                        Vec4 v = new Vec4(0, 0, curAlt);
                        Orientation o = new Orientation(0, 0, Math.toDegrees(yawRadStart) + getYaw());
                        // System.out.println("V:"+v + " o:"+o);
                        pointsBase.addElement(v);
                        directionsBase.add(o);
                    } else {
                        yawRadStart -= Math.PI / 2.;

                        // this is line 0
                        Vec4 v =
                            new Vec4(
                                corridorWidthInMeter * Math.sin(yawRadStart),
                                corridorWidthInMeter * Math.cos(yawRadStart),
                                curAlt);
                        Orientation o = new Orientation(0, 0, Math.toDegrees(yawRadStart) + getYaw() + 90);
                        // System.out.println("V:"+v + " o:"+o);
                        pointsBase.addElement(v);
                        directionsBase.add(o);

                        numberLines =
                            (int)Math.round(2 * (corridorWidthInMeter - sizeInFlightEff) / sizeParallelFlightEff);
                        // System.out.println("numLines:"+numberLines);
                        double stepY = 2 * (corridorWidthInMeter - sizeInFlightEff) / numberLines;

                        double currentY = -corridorWidthInMeter + sizeInFlightEff;
                        if (numberLines <= 0) {
                            currentY = 0;
                            numberLines = 0;
                        }

                        // System.out.println();
                        Matrix m = Matrix.fromRotationZ(Angle.fromRadians(-yawRadStart));

                        for (lineNo = 0; lineNo <= numberLines; lineNo++) {

                            // System.out.println("curY: " +currentY + " stepY"+stepY);

                            double currentX =
                                Math.sqrt(corridorWidthInMeter * corridorWidthInMeter - currentY * currentY);
                            // System.out.println("width:"+currentX);
                            int numImgs = (int)Math.ceil(2 * currentX / sizeInFlightEff);
                            if (numImgs < 1) {
                                numImgs = 1;
                            }

                            double stepX = 2 * currentX / numImgs;
                            int sign = lineNo % 2 == 0 ? 1 : -1;
                            // System.out.println("numImgs:"+numImgs );
                            for (int imgNo = 0; imgNo <= numImgs; imgNo++) {

                                // System.out.println("curX: " +currentX + " stepX"+stepX);
                                v = new Vec4(-currentX * sign, -currentY, curAlt);
                                v = v.transformBy3(m);
                                o = new Orientation(0, 0, Math.toDegrees(yawRadStart) + getYaw() - 90 + 90 * sign);
                                // System.out.println("V:" + v + " o:" + o);
                                pointsBase.addElement(v);
                                directionsBase.add(o);
                                currentX -= stepX;
                                // break;
                            }

                            currentY += stepY;
                        }

                        // this is the last line
                        v =
                            new Vec4(
                                -corridorWidthInMeter * Math.sin(yawRadStart),
                                -corridorWidthInMeter * Math.cos(yawRadStart),
                                curAlt);
                        o = new Orientation(0, 0, Math.toDegrees(yawRadStart) + getYaw() + 90);
                        // System.out.println("V:"+v + " o:"+o);
                        pointsBase.addElement(v);
                        directionsBase.add(o);
                    }

                    FlightLine fl = new FlightLine(pointsBase, directionsBase, alt, lineNo++, this);
                    flightLines.add(fl);
                }

                // FlightLine.mirrowAll(flightLines);
                // Collections.reverse(flightLines);

                break;
            }

        case WINDMILL:
            {
                boolean circleLeftTrueRightFalse = this.circleLeftTrueRightFalse;
                if (startCaptureVertically.equals(StartCaptureVerticallyTypes.up)) {
                    circleLeftTrueRightFalse = !circleLeftTrueRightFalse;
                }
                // WINDMILL parameters, fixed for now, need ui/etc to change
                //                if (windmill == null) {
                //                    windmill = new WindmillData();
                //                } else {
                //                    System.out.println("reusing existing WindmillData");
                //                }

                // TODO: these should come from UI for windmill eventually
                //                windmill.setTowerHeight(cropHeightMax);
                //                windmill.setTowerRadius(corridorWidthInMeter);
                //                windmill.setDistanceFromBlade(alt);

                // make waypoints and lines (flightplan) for WINDMILL blades
                // treat blades as tilted cylinders

                // next few variables are for flightlines
                int lines = 4; // number of lines around a blade
                double step = 2 * Math.PI / lines; // angular steps around blades
                double radius = // radius of flight lines around blades
                    windmill.distanceFromBlade
                        + windmill.bladeRadius
                        + cameraDescription.getOffsetToTail().convertTo(Unit.METER).getValue().doubleValue();
                double thinRadius = radius - windmill.bladeRadius + windmill.bladeThinRadius;
                double startAlt = windmill.hubRadius + windmill.bladeStartLength;
                double stopAlt = windmill.hubRadius + windmill.bladeLength;

                // build flight path for all blades
                double bladeRotationStep = 360 / windmill.numberOfBlades;
                double bladeRotationDegs = windmill.bladeStartRotation;

                for (int i = 0; i < windmill.numberOfBlades; i++) {
                    AddBladeWaypoints(
                        startAlt,
                        stopAlt,
                        step,
                        radius,
                        thinRadius,
                        lines,
                        windmill.hubRadius,
                        windmill.hubHalfLength,
                        windmill.hubYaw,
                        windmill.towerHeight,
                        bladeRotationDegs,
                        windmill.bladeRadius,
                        windmill.bladePitch,
                        windmill.numberOfBlades);

                    bladeRotationDegs += bladeRotationStep;
                }

                break;
            }
        case COPTER3D:
            // computing flightlines here is way too expensive!
            break;
        case TARGET_POINTS:
            // generate waypoints directly!
            break;
        default:
            break;
        }

        return true;
    }

    @Nullable
    public Boolean generateCityWayPoints(
            Vector<Vector<Vec4>> cellBounds,
            MinMaxPair minMaxBoundsX,
            MinMaxPair minMaxBoundsY,
            IPlatformDescription platformDescription) {
        int numberLines;
        int lineNo; // System.out.println("minXorg = " + minX + " lengthX=" + lengthX);
        // System.out.println("minYorg = " + minY + " lengthY=" + lengthY);

        // todo: this looks weird, why is either minY or overshootPArallelFlight negative? coordinates given in
        // uav ref frame?
        double minY = -overshootParallelFlight;
        double maxY = this.lengthY + overshootParallelFlight;
        if (minMaxBoundsY != null) {
            minMaxBoundsY.enlarge(sizeParallelFlight / 2); // assures that their is a line close after the splitting..
            minY = Math.max(minY, minMaxBoundsY.min);
            maxY = Math.min(maxY, minMaxBoundsY.max);
        }

        numberLines =
            (int)
                Math.max(
                    Math.ceil((maxY - minY) / sizeParallelFlightEff), // might be that max/minY is min/max of aoi?
                    Math.round(sizeParallelFlight / sizeParallelFlightEff));

        double offset = ((numberLines - 1) * sizeParallelFlightEff - (maxY - minY)) / 2;
        maxYenlarged = maxY + offset;
        double currentY = maxYenlarged;

        for (lineNo = 0; lineNo != numberLines; lineNo++) {
            // System.out.println("i="+i);
            // System.out.println("currentY;"+currentY);

            // scan center of the line, and on neighbor lines
            MinMaxPair minMaxX =
                AutoFPhelper.getFlightlineIntersectionsX(polygonsLocal, currentY, sizeParallelFlightEff);
            // scan on center between this and neigbot line. eg. on tiny AOI with only two lines in total, we
            // would otherwise missto interset the AOI
            minMaxX.enlarge(
                AutoFPhelper.getFlightlineIntersectionsX(polygonsLocal, currentY, sizeParallelFlightEff / 2));

            if (minMaxX.isValid()) {
                minMaxX.enlarge(overshootTotalLinesEnd);
                if (cellBounds != null) {
                    MinMaxPair minMaxXBounds =
                        AutoFPhelper.getFlightlineIntersectionsX(cellBounds, currentY, sizeParallelFlightEff);

                    minMaxXBounds.enlarge(overshootInnerLinesEnd);
                    minMaxX.shrink(minMaxXBounds);
                }

                // System.out.printf("currentY=%f X=%s\n", currentY, minMaxX);
                FlightLine fl = new FlightLine(currentY, minMaxX, lineNo, this, false);
                flightLines.add(fl);
                // firstFreeID+=4;

                lengthEstimation += minMaxX.size();
            }

            currentY -= sizeParallelFlightEff;
        }

        if (flightLines.isEmpty()) {
            lastError = RecomputeErrors.MSG_AREA_TOO_SMALL;
            Debug.getLog()
                .log(Level.INFO, "Could not compute flightplan for picture Area. Maybe it is too small to be rastered");
            return false;
        }

        if (isOnlySingleDirection()) {
            lengthEstimation *= 2;
        }

        lengthEstimation += AutoFPhelper.getDiameterMultiPol(polygonsLocal) + 2 * alt; // way to and from landingpoint
        lengthEstimation +=
            (numberLines - 1)
                * sizeParallelFlightEff
                * FlightLine.computeJumpStep(platformDescription, sizeParallelFlightEff);
        minYenlarged = currentY + sizeParallelFlightEff;

        if (planType == PlanType.POLYGON) {
            return true;
        }

        double minX = -overshootParallelFlight;
        double maxX = this.lengthX + overshootParallelFlight;
        if (minMaxBoundsX != null) {
            minMaxBoundsX.enlarge(sizeParallelFlight / 2); // assures that their is a line close after the splitting..
            minX = Math.max(minX, minMaxBoundsX.min);
            maxX = Math.min(maxX, minMaxBoundsX.max);
        }

        numberLines =
            (int)
                Math.max(
                    Math.ceil((maxX - minX) / sizeParallelFlightEff),
                    Math.round(sizeParallelFlight / sizeParallelFlightEff));

        offset = ((numberLines - 1) * sizeParallelFlightEff - (maxX - minX)) / 2;
        maxXenlarged = maxX + offset;
        double currentX = maxXenlarged;

        // System.out.println("cornerList"+cornerList);
        // System.out.println("numberLines" + numberLines);
        // System.out.println("offset"+offset);
        // System.out.println("minMaxY:" + minY + " " + maxY);

        double lengthEstimation2 = 0;

        // start lineNo count from 0 since this lines are rotated and will be distinguised that way
        for (lineNo = 0; lineNo != numberLines; lineNo++) {
            // System.out.println("i="+i);
            // System.out.println("currentX;"+currentX);
            // scan center of the line, and on neighbor lines
            MinMaxPair minMaxY =
                AutoFPhelper.getFlightlineIntersectionsY(polygonsLocal, currentX, sizeParallelFlightEff);
            // scan on center between this and neigbot line. eg. on tiny AOI with only two lines in total, we
            // would otherwise missto interset the AOI
            minMaxY.enlarge(
                AutoFPhelper.getFlightlineIntersectionsY(polygonsLocal, currentX, sizeParallelFlightEff / 2));

            if (minMaxY.isValid()) {
                minMaxY.enlarge(overshootTotalLinesEnd);
                if (cellBounds != null) {
                    MinMaxPair minMaxYBounds =
                        AutoFPhelper.getFlightlineIntersectionsY(cellBounds, currentX, sizeParallelFlightEff);
                    minMaxYBounds.enlarge(overshootInnerLinesEnd);
                    minMaxY.shrink(minMaxYBounds);
                }

                // System.out.printf("currentY=%f X=%s\n", currentY, minMaxX);
                FlightLine fl = new FlightLine(currentX, minMaxY, lineNo, this, true);

                flightLines.add(fl);
                // firstFreeID+=4;

                lengthEstimation2 += minMaxY.size();
            }

            currentX -= sizeParallelFlightEff;
        }

        if (flightLines.isEmpty()) {
            lastError = RecomputeErrors.MSG_AREA_TOO_SMALL;
            Debug.getLog()
                .log(Level.INFO, "Could not compute flightplan for picture Area. Maybe it is too small to be rastered");
            return false;
        }

        if (isOnlySingleDirection()) {
            lengthEstimation2 *= 2;
        }
        // lengthEstimation += AutoFPhelper.getDiameterMultiPol(polygonsLocal) + 2*alt;//way to and from
        // landingpoint
        lengthEstimation2 +=
            (numberLines - 1)
                * sizeParallelFlightEff
                * FlightLine.computeJumpStep(platformDescription, sizeParallelFlightEff);
        minXenlarged = currentX + sizeParallelFlightEff;

        lengthEstimation += lengthEstimation2;
        return null;
    }

    public void compensateForMotionBlur(PhotoSettings photoSettings, double sizeInFlight) {
        // compensate for motion blur
        double maxPossiblePlaneSpeedMpSBlurr = altitudeGsdCalculator.computeMaxGroundSpeedMpS();
        double maxPossiblePlaneSpeedMpS =
                photoSettings
                        .getFlightplan()
                        .getHardwareConfiguration()
                        .getPlatformDescription()
                        .getMaxPlaneSpeed()
                        .convertTo(Unit.METER_PER_SECOND)
                        .getValue()
                        .doubleValue();
        // compensate for forward overlap isues

        // compute max possible plane speed
        sizeInFlightEff = (1. - getOverlapInFlight() / 100.) * sizeInFlight;
        double maxPossiblePlaneSpeedMpSOverlap = sizeInFlightEff / photoSettings.getMinTimeInterval();

        if (photoSettings.getMaxGroundSpeedAutomatic() == FlightplanSpeedModes.AUTOMATIC_DYNAMIC) {
            this.optimalSpeedThisAoi = Math.min(maxPossiblePlaneSpeedMpSBlurr, maxPossiblePlaneSpeedMpS);
            if (photoSettings.getMaxGroundSpeedMPSec() == maxPossiblePlaneSpeedMpS) {
                photoSettings.setMaxGroundSpeedMPSec(this.optimalSpeedThisAoi);
            } else {
                if (photoSettings.getMaxGroundSpeedMPSec() < this.optimalSpeedThisAoi) {
                    photoSettings.setMaxGroundSpeedMPSec(this.optimalSpeedThisAoi);
                }
            }
        } else {
            this.optimalSpeedThisAoi =
                Math.min(
                        Math.min(maxPossiblePlaneSpeedMpSBlurr, maxPossiblePlaneSpeedMpSOverlap), maxPossiblePlaneSpeedMpS);
            if (photoSettings.getMaxGroundSpeedMPSec() == maxPossiblePlaneSpeedMpS) {
                photoSettings.setMaxGroundSpeedMPSec(this.optimalSpeedThisAoi);
            } else {
                if (photoSettings.getMaxGroundSpeedMPSec() < this.optimalSpeedThisAoi) {
                    photoSettings.setMaxGroundSpeedMPSec(this.optimalSpeedThisAoi);
                }
            }
        }
    }

    private void AddBladeWaypoints(
            double startAlt,
            double stopAlt,
            double step,
            double radius,
            double thinRadius,
            int lines,
            double windmillHubRadius,
            double windmillHubHalfLength,
            double windmillYaw,
            double windmillTowerHeight,
            double bladeRotationDegs,
            double windmillBladeRadius,
            double windmillBladePitch,
            int windmillNumberOfBlades) {

        // blade waypoints are for waypoint locations in flight plan
        List<List<TemporaryWaypoint>> bladeWaypoints = new ArrayList<>();
        generateWaypointsWindmillBlade(
            bladeWaypoints,
            startAlt,
            stopAlt,
            step,
            radius,
            thinRadius,
            lines,
            windmillHubRadius,
            windmillNumberOfBlades);
        // center waypoints are for setting direction to point to for orientation
        List<List<TemporaryWaypoint>> centerWaypoints = new ArrayList<>();
        generateWaypointsWindmillBlade(
            centerWaypoints, startAlt, stopAlt, 0, 0, 0, lines, windmillHubRadius, windmillNumberOfBlades);

        // blade is attached to hub, not beyond the end of the hub
        Matrix bladeTransform =
            getBladeTransform(
                windmillBladePitch,
                windmillHubHalfLength - windmillBladeRadius,
                windmillYaw,
                windmillTowerHeight + windmillHubRadius,
                bladeRotationDegs);
        Matrix bladeTransformNoTwist =
            getBladeTransform(
                0,
                windmillHubHalfLength - windmillBladeRadius,
                windmillYaw,
                windmillTowerHeight + windmillHubRadius,
                bladeRotationDegs);

        transformWaypoints(bladeWaypoints, bladeTransform, bladeTransformNoTwist);
        transformWaypoints(centerWaypoints, bladeTransform, bladeTransformNoTwist);

        // now fix orientations
        fixOrientations(bladeWaypoints, centerWaypoints);

        makeFlightLinesAsIs(bladeWaypoints, getVerticalScanPattern(), planType);
    }

    public Matrix getBladeTransform(
            double windmillBladePitch,
            double windmillHubHalfLength,
            double windmillYaw,
            double windmillTowerHeight,
            double bladeRotationDegs) {
        // set b;ade pitch
        Angle bladeTwisting = Angle.fromDegrees(windmillBladePitch);
        Matrix bladeTwist = Matrix.fromAxisAngle(bladeTwisting, 0, 0, 1);
        // rotate blade
        Angle bladeSpin = Angle.fromDegrees(bladeRotationDegs);
        Matrix bladeRotation = Matrix.fromAxisAngle(bladeSpin, 1, 0, 0);
        bladeRotation = bladeRotation.multiply(bladeTwist);
        // translate for hub offset
        Vec4 bladeTranslation = new Vec4(windmillHubHalfLength, 0, 0, 0);
        Matrix bladeTrans = Matrix.fromTranslation(bladeTranslation);
        Matrix bladeTransform = bladeTrans.multiply(bladeRotation);
        // rotate tower
        Angle yawSpin = Angle.fromDegrees(windmillYaw);
        Matrix bladeRotation2 = Matrix.fromAxisAngle(yawSpin, 0, 0, 1);
        bladeRotation2 = bladeRotation2.multiply(bladeTransform);
        // translate for tower height
        Vec4 bladeTranslation2 = new Vec4(0, 0, windmillTowerHeight, 0);
        Matrix bladeTrans2 = Matrix.fromTranslation(bladeTranslation2);
        Matrix bladeTransform2 = bladeTrans2.multiply(bladeRotation2);

        return (bladeTransform2);
    }

    // set orientations of waypoints to point correctly to blade centers
    private void fixOrientations(
            List<List<TemporaryWaypoint>> bladeWaypoints, List<List<TemporaryWaypoint>> centerWaypoints) {

        // find and assign directions for waypoints
        for (int ic = 0; ic < bladeWaypoints.size(); ic++) {
            List<TemporaryWaypoint> column = bladeWaypoints.get(ic);
            List<TemporaryWaypoint> centerColumn = centerWaypoints.get(ic);

            for (int i = 0; i < column.size(); i++) {
                TemporaryWaypoint t = column.get(i);
                Vec4 wp = t.positionOnDrone;
                Vec4 cp = centerColumn.get(i).positionOnDrone;
                Vec4 offset = cp.subtract3(wp);
                // this combination, x and y order and +- 90, is how got it to work
                double yaw = Math.atan2(offset.x, offset.y);
                yaw = (180 * yaw / Math.PI) - 90;
                double xy = Math.sqrt(offset.x * offset.x + offset.y * offset.y);
                double pitch = Math.atan2(offset.z, xy);
                pitch = (180 * pitch / Math.PI) + 90;
                t.orientation = new Orientation(0, pitch, yaw);
            }
        }
    }

    private void transformWaypoints(
            List<List<TemporaryWaypoint>> waypoints, Matrix bladeTransform, Matrix bladeTransformNoTwist) {

        // apply transformation to all waypoints
        for (int icol = 0; icol < waypoints.size(); icol++) {
            List<TemporaryWaypoint> column = waypoints.get(icol);
            if (icol == waypoints.size() - 1) {
                // special column for hub pictures
                for (TemporaryWaypoint t : column) {
                    t.positionOnDrone = t.positionOnDrone.transformBy4(bladeTransformNoTwist);
                }
            } else {
                for (TemporaryWaypoint t : column) {
                    t.positionOnDrone = t.positionOnDrone.transformBy4(bladeTransform);
                }
            }
        }
    }

    private void generateWaypointsWindmillBlade(
            List<List<TemporaryWaypoint>> waypoints,
            double altMin,
            double altMax,
            double step,
            double radius,
            double thinRadius,
            int lines,
            double windmillHubRadius,
            int windmillNumberOfBlades) {
        // function does double duty generating blade waypoints and blade center locations for setting orientations
        int linecount = (DoubleHelper.areClose(radius, 0)) ? 1 : lines;

        for (int k = 0; k < lines; k++) {
            double yawRad = k * step * (circleLeftTrueRightFalse ? -1 : 1);
            ArrayList<TemporaryWaypoint> column = new ArrayList<>();
            Orientation o = new Orientation(0, 90, Math.toDegrees(yawRad + Math.PI / 2) + getYaw());
            Vec4 v = new Vec4(radius * Math.sin(yawRad), thinRadius * Math.cos(yawRad), altMin);
            column.add(new TemporaryWaypoint(v, o, null));

            for (double alt = altMin + sizeInFlightEff; alt <= altMax; alt += sizeInFlightEff) {
                v = new Vec4(v.x, v.y, alt);
                column.add(new TemporaryWaypoint(v, o, null));
            }

            // special case for blade end photo
            if (k == 0) {
                if (DoubleHelper.areClose(radius, 0)) {
                    v = new Vec4(0, 0, altMax);
                    column.add(new TemporaryWaypoint(v, o, null)); // point to blade end center
                    column.add(new TemporaryWaypoint(v, o, null));
                    column.add(new TemporaryWaypoint(v, o, null));
                } else {
                    double height = altMax + alt; // alt is aka windmillDistanceFromBlade
                    v = new Vec4(v.x, v.y, height);
                    column.add(new TemporaryWaypoint(v, o, null)); // position without picture
                    v = new Vec4(0, 0, height);
                    column.add(new TemporaryWaypoint(v, o, null)); // want picture here
                    yawRad = step * (circleLeftTrueRightFalse ? -1 : 1);
                    v = new Vec4(radius * Math.sin(yawRad), thinRadius * Math.cos(yawRad), height);
                    column.add(new TemporaryWaypoint(v, o, null)); // position without picture
                }
            } else { // special case for waypoints between columns
                if (DoubleHelper.areClose(radius, 0)) {
                    v = new Vec4(0, 0, altMin);
                    column.add(new TemporaryWaypoint(v, o, null)); // point to blade center
                    v = new Vec4(0, 0, altMax);
                    column.add(new TemporaryWaypoint(v, o, null)); // point to blade center
                } else {
                    yawRad = (k + 0.5) * step * (circleLeftTrueRightFalse ? -1 : 1);
                    v = new Vec4(radius * Math.sin(yawRad), thinRadius * Math.cos(yawRad), altMin);
                    column.add(new TemporaryWaypoint(v, o, null)); // position without picture
                    v = new Vec4(radius * Math.sin(yawRad), thinRadius * Math.cos(yawRad), altMax);
                    column.add(new TemporaryWaypoint(v, o, null)); // position without picture
                }
            }

            waypoints.add(column);
        }

        // also want pictures of hub from front, need to move to there
        // add another column (waypoint list) to move in front of blade and take hub pictures
        ArrayList<TemporaryWaypoint> column = new ArrayList<>();

        double eradius = radius * 1.2; // expanded radius meant to avoid blade, not photo it
        double yawRad =
            (linecount - 1) * step * (circleLeftTrueRightFalse ? -1 : 1); // start where would be with full radius
        Orientation o = new Orientation(0, 90, Math.toDegrees(yawRad + Math.PI / 2) + getYaw());
        Vec4 v = new Vec4(eradius * Math.sin(yawRad), eradius * Math.cos(yawRad), altMin);
        column.add(new TemporaryWaypoint(v, o, null)); // position without picture

        // move to front of blade
        int numlines = Math.max(lines / 2, 1);
        for (int k = 0; k < numlines; k++) {
            yawRad = k * step * (circleLeftTrueRightFalse ? -1 : 1); // TODO: go in direction away from tower
            o = new Orientation(0, 90, Math.toDegrees(yawRad + Math.PI / 2) + getYaw());
            v = new Vec4(eradius * Math.sin(yawRad), eradius * Math.cos(yawRad), altMin);
            column.add(new TemporaryWaypoint(v, o, null)); // position without picture
        }

        // take more pictures to include the base of blades (near hub)
        yawRad = (numlines - 1) * step * (circleLeftTrueRightFalse ? -1 : 1);
        o = new Orientation(0, 90, Math.toDegrees(yawRad + Math.PI / 2) + getYaw());
        v = new Vec4(radius * Math.sin(yawRad), radius * Math.cos(yawRad), windmillHubRadius);
        column.add(new TemporaryWaypoint(v, o, null));

        // between blades near hub
        if (DoubleHelper.areClose(radius, 0)) {
            v = new Vec4(radius * Math.sin(yawRad), radius * Math.cos(yawRad), 0);
        } else {
            double offset = windmillHubRadius * Math.tan(Math.PI / windmillNumberOfBlades);
            v = new Vec4(radius * Math.sin(yawRad), radius * Math.cos(yawRad) - offset, windmillHubRadius);
        }

        column.add(new TemporaryWaypoint(v, o, null));

        // add position to avoid blade whatever it's pitch
        yawRad = Math.PI;
        o = new Orientation(0, 90, Math.toDegrees(yawRad + Math.PI / 2) + getYaw());
        if (DoubleHelper.areClose(radius, 0)) {
            v = new Vec4(radius * Math.sin(yawRad), radius * Math.cos(yawRad), 0);
        } else {
            v = new Vec4(eradius * Math.sin(yawRad), eradius * Math.cos(yawRad), windmillHubRadius);
        }

        column.add(new TemporaryWaypoint(v, o, null)); // position without picture

        waypoints.add(column);
    }

    private void makeFlightLinesAsIs(
            List<List<TemporaryWaypoint>> columns, VerticalScanPatternTypes verticalScanPattern, PlanType planType) {
        if (columns == null || columns.size() == 0) {
            return;
        }

        int lineNo = 0;
        Vector<Vec4> pointsBase = new Vector<>();
        Vector<Orientation> directionsBase = new Vector<>();

        int i = 0;
        for (List<TemporaryWaypoint> column : columns) {
            TemporaryWaypoint savetmin = column.get(column.size() - 2);
            TemporaryWaypoint savetmax = column.get(column.size() - 1);
            for (TemporaryWaypoint t : column) {
                pointsBase.add(t.positionOnDrone);
                directionsBase.add(t.orientation);
            }

            // this adds the waypoint to avoid blade collision going to next column
            if (i == 1) {
                pointsBase.remove(pointsBase.size() - 1);
                directionsBase.remove(directionsBase.size() - 1);
                pointsBase.remove(pointsBase.size() - 1);
                directionsBase.remove(directionsBase.size() - 1);
                pointsBase.add(0, savetmin.positionOnDrone);
                directionsBase.add(0, savetmin.orientation);
            } else if (i == 2) {
                pointsBase.remove(pointsBase.size() - 1);
                directionsBase.remove(directionsBase.size() - 1);
                pointsBase.remove(pointsBase.size() - 1);
                directionsBase.remove(directionsBase.size() - 1);
                pointsBase.add(savetmax.positionOnDrone);
                directionsBase.add(savetmax.orientation);
            } else if (i == 3) {
                pointsBase.remove(pointsBase.size() - 1);
                directionsBase.remove(directionsBase.size() - 1);
                pointsBase.remove(pointsBase.size() - 1);
                directionsBase.remove(directionsBase.size() - 1);
            }

            if (i % 2 == 1) {
                Collections.reverse(pointsBase);
                Collections.reverse(directionsBase);
            }

            i++;
            FlightLine fl = new FlightLine(pointsBase, directionsBase, alt, lineNo++, this);
            flightLines.add(fl);
            pointsBase.clear();
            directionsBase.clear();
        }
    }

    private void generateWaypointsTower(
            ArrayList<ArrayList<TemporaryWaypoint>> waypoints,
            double minCropToMinGroundDist,
            double altMin,
            double step,
            double radius,
            int lines,
            boolean circleLeftTrueRightFalse) {
        for (int k = 0; k < lines; k++) {
            double yawRad = Math.PI / 2 + k * step * (circleLeftTrueRightFalse ? -1 : 1);
            ArrayList<TemporaryWaypoint> column = new ArrayList<>();
            Orientation o = new Orientation(0, 90, Math.toDegrees(yawRad + Math.PI / 2) + getYaw());
            Vec4 v = new Vec4(radius * Math.sin(yawRad), radius * Math.cos(yawRad), 0);

            v = new Vec4(v.x, v.y, altMin);
            column.add(new TemporaryWaypoint(v, o, null));

            // TODO extract method (repeating a part from generateWaypoints method)
            if (minCropToMinGroundDist > 0) {
                // number of additional steps needed to cover the bottom part
                int steps = (int)Math.ceil(minCropToMinGroundDist / sizeInFlightEff);
                double stepSize = sizeInFlightEff / (steps + 1);
                for (double j = 0; j < steps; j++) {
                    double height = minGroundDistance + stepSize * (j + 1);
                    // angle of the waypoint to target a facade point
                    double pitch = Math.toDegrees(Math.atan2(alt, height - (cropHeightMin + sizeInFlightEff * j)));
                    double pitchRad = pitch * Math.PI / 180.0;
                    // shift that need to be applied to a waypoint towards the object to preserve the resolution
                    double shift = alt * (1.0 / Math.sin(pitchRad) - 1);
                    if (j == 0) {
                        this.distMinComputed = alt - shift;
                    }

                    double direction = (o.getYaw() - getYaw()) * Math.PI / 180.0;
                    // TODO: maybe check if x and y have correct cos and sin :)
                    double vx = v.x + Math.cos(direction) * shift;
                    double vy = v.y - Math.sin(direction) * shift;
                    Vec4 v1 = new Vec4(vx, vy, height);

                    Orientation o_add = new Orientation(0, pitch, o.getYaw());
                    column.add(new TemporaryWaypoint(v1, o_add, null));
                }
            }

            for (double alt = altMin + sizeInFlightEff; alt <= cropHeightMax; alt += sizeInFlightEff) {
                v = new Vec4(v.x, v.y, alt);
                column.add(new TemporaryWaypoint(v, o, null));
            }

            waypoints.add(column);
        }
    }

    private void makeFlightLines(
            ArrayList<ArrayList<TemporaryWaypoint>> columnsOrig,
            VerticalScanPatternTypes verticalScanPattern,
            PlanType planType) {
        List<ArrayList<TemporaryWaypoint>> columns = sortColumns(columnsOrig);

        if (columns == null || columns.size() == 0) {
            return;
        }

        int lineNo = 0;
        Vector<Vec4> pointsBase = new Vector<>();
        Vector<Orientation> directionsBase = new Vector<>();

        if (verticalScanPattern.equals(VerticalScanPatternTypes.upDown)) {
            int i = 0;
            for (ArrayList<TemporaryWaypoint> column : columns) {
                for (TemporaryWaypoint t : column) {
                    pointsBase.add(t.positionOnDrone);
                    directionsBase.add(t.orientation);
                }

                if (i % 2 == 0) {
                    Collections.reverse(pointsBase);
                    Collections.reverse(directionsBase);
                }

                i++;
                FlightLine fl = new FlightLine(pointsBase, directionsBase, alt, lineNo++, this);
                flightLines.add(fl);
                pointsBase.clear();
                directionsBase.clear();
            }
        } else if (verticalScanPattern.equals(VerticalScanPatternTypes.leftRight)) {
            for (int i = 0; i < columns.get(0).size(); i++) {
                for (ArrayList<TemporaryWaypoint> c : columns) {
                    pointsBase.add(c.get(i).positionOnDrone);
                    directionsBase.add(c.get(i).orientation);
                }

                if (i % 2 == 1 && planType == PlanType.FACADE) {
                    Collections.reverse(pointsBase);
                    Collections.reverse(directionsBase);
                }

                FlightLine fl = new FlightLine(pointsBase, directionsBase, alt, lineNo++, this);
                flightLines.add(fl);
                pointsBase.clear();
                directionsBase.clear();
            }
        }
    }

    private List<ArrayList<TemporaryWaypoint>> sortColumns(ArrayList<ArrayList<TemporaryWaypoint>> columns) {
        ScanDirectionsTypes type = getScanDirection();
        List<ArrayList<TemporaryWaypoint>> sorted = new ArrayList<>();
        Flightplan flightplan = getFlightplan();
        // erticalScanPattern.equals(VerticalScanPatternTypes.leftRight)

        if (type.equals(ScanDirectionsTypes.fromStarting) || type.equals(ScanDirectionsTypes.towardLaning)) {
            Position position = null;

            if (type.equals(ScanDirectionsTypes.fromStarting)) {
                // compute previous positionOnDrone before picArea
                PreviousOfTypeVisitor vis2 = new PreviousOfTypeVisitor(this, IPositionReferenced.class, Point.class);
                vis2.setSkipIgnoredPaths(true);
                vis2.startVisit(flightplan);
                IPositionReferenced wp = (IPositionReferenced)vis2.prevObj;
                if (wp == null || isNullIsland(wp.getLatLon()) || wp == getFlightplan().getRefPoint()) {
                    ReferencePoint origin = flightplan.getTakeoff();
                    position = new Position(Angle.fromDegrees(origin.getLat()), Angle.fromDegrees(origin.getLon()), 0);
                } else {
                    position = wp.getPosition();
                }
            } else {
                // compute next positionOnDrone after picArea
                NextOfTypeVisitor vis = new NextOfTypeVisitor(this, IPositionReferenced.class, Point.class);
                vis.setSkipIgnoredPaths(true);
                vis.startVisit(flightplan);
                IPositionReferenced wp = (IPositionReferenced)vis.nextObj;
                if (wp == null || isNullIsland(wp.getPosition()) || wp == getFlightplan().getRefPoint()) {
                    if (!flightplan.getLandingpoint().isEmpty()) {
                        wp = flightplan.getLandingpoint();
                    } else {
                        wp = flightplan.getTakeoff();
                    }

                    position = new Position(wp.getLatLon(), 0);
                } else {
                    position = wp.getPosition();
                }
            }

            Vec4 vec = globe.computePointFromPosition(position);

            // get index of the nearest column to the point of interest
            int idx =
                IntStream.range(0, columns.size())
                    .reduce(
                        (a, b) ->
                            computeDistance(columns.get(a).get(0).positionOnDrone, vec)
                                    < computeDistance(columns.get(b).get(0).positionOnDrone, vec)
                                ? a
                                : b)
                    .getAsInt();

            if (planType.equals(PlanType.BUILDING)
                    || planType.equals(PlanType.TOWER)
                    || planType.equals(PlanType.WINDMILL)) {

                // cyclic sift of columns
                return IntStream.range(idx, columns.size() + idx)
                    .mapToObj((i) -> columns.get((i % columns.size())))
                    .collect(Collectors.toList());
            } else if (planType.equals(PlanType.FACADE)) {
                // non cyclic order, but first from the column to the right and come back and from the column to the
                // left
                /*return IntStream.range(idx, columns.size() + idx)
                .mapToObj((i) -> columns.get((i < columns.size() ? i : (idx - i % columns.size() - 1))))
                .collect(Collectors.toList());*/
                if (idx <= columns.size() / 2 && type.equals(ScanDirectionsTypes.fromStarting)
                        || idx > columns.size() / 2 && type.equals(ScanDirectionsTypes.towardLaning)) {
                    // do nothing start from the left
                    return columns;
                } else {
                    // start from the right
                    Collections.reverse(columns);
                    return columns;
                }
            }
        } else if (scanDirection.equals(ScanDirectionsTypes.left) && facadeScanningSide == FacadeScanningSide.left
                || scanDirection.equals(ScanDirectionsTypes.right) && facadeScanningSide == FacadeScanningSide.right) {
            return columns;
        } else if (scanDirection.equals(ScanDirectionsTypes.custom)) {
            return columns;
        } else {
            Collections.reverse(columns);
            return columns;
        }

        return sorted;
    }

    private boolean isNullIsland(LatLon position) {
        return position == null || position.getLatitude().degrees == 0 || position.getLongitude().degrees == 0;
    }

    private double computeDistance(Vec4 local, Vec4 v2) {
        Position p1 = transformToGlobe(local);
        Vec4 v1 = globe.computePointFromPosition(p1);

        return v1.distanceTo3(v2);
    }

    // put some safety maring here for numerical stability
    private final int MAX_NUM_PICS = 10000;

    public boolean isValidSizeAOI() {
        if (!getPlanType().hasWaypoints() || getArea() == 0 || getAlt() <= 0) {
            return true;
        }

        int num = estimatePicsNum();

        if (num > MAX_NUM_PICS) {
            PicArea.this.elements.clear();
            Debug.getLog()
                .log(
                    Level.WARNING,
                    "Selected area is too large for plan generation. Please decrease area size or increase resolution: "
                        + this);
            return false;
        }

        return true;
    }

    private int estimatePicsNum() {
        // since selected HW might have changed, better recalc always
        altitudeGsdCalculator.recalculate();
        double alt = altitudeGsdCalculator.getAlt();
        double area = getArea();
        double overlapW = getOverlapInFlight();
        double overlapH = getOverlapParallel();
        Flightplan flightplan = getFlightplan();
        double numPicsEst = 0;
        if (flightplan != null) {
            IHardwareConfiguration hardwareConfiguration = flightplan.getHardwareConfiguration();
            IGenericCameraConfiguration cameraConfiguration =
                hardwareConfiguration.getPrimaryPayload(IGenericCameraConfiguration.class);
            IGenericCameraDescription cameraDescription = cameraConfiguration.getDescription();
            ILensDescription lensDescription = cameraConfiguration.getLens().getDescription();
            double focalLength = lensDescription.getFocalLength().convertTo(Unit.MILLIMETER).getValue().doubleValue();
            double ccdWidth = cameraDescription.getCcdWidth().convertTo(Unit.MILLIMETER).getValue().doubleValue();
            double ccdHeight = cameraDescription.getCcdHeight().convertTo(Unit.MILLIMETER).getValue().doubleValue();

            double height = alt * ccdHeight * ((100 - overlapH) / 100.0) / focalLength;
            double width = alt * ccdWidth * ((100 - overlapW) / 100.0) / focalLength;

            numPicsEst = area / (height * width);
        }

        return (int)numPicsEst;
    }

    private class TemporaryWaypoint {
        private Vec4 positionOnDrone;
        private Vec4 positionOnObject;
        private Orientation orientation;

        public TemporaryWaypoint(Vec4 positionOnDrone, Orientation orientation, Vec4 positionOnObject) {
            this.positionOnDrone = positionOnDrone;
            this.orientation = orientation;
            this.positionOnObject = positionOnObject;
        }

        void setPointOnObjectChangeDirection(Vec4 pointOnObject) {
            Vec4 diff2 = positionOnDrone.subtract3(pointOnObject);
            yaw = Math.toDegrees(Math.atan2(diff2.x, diff2.y)) + 90 - getYaw();
            orientation.setYaw(yaw);
        }

        @Override
        public String toString() {
            return "positionOnDrone:"
                + positionOnDrone
                + "\tpositionOnObject:"
                + positionOnObject
                + "\torientation:"
                + orientation;
        }
    }

    /**
     * returns ArrayList<TemporaryWaypoint> waypoints - containing all the waypoints in one list in column-by-column
     * order (with ascending height)
     *
     * <p>does not change flightlines
     */
    private ArrayList<ArrayList<TemporaryWaypoint>> generateWaypoints(
            Vector<Vec4> path,
            Vector<Vec4> polyNoGo,
            Vector<Vec4> pathSafetyDist,
            double sign,
            double altMin,
            double minCropToMinGroundDist,
            double distanceToObj,
            boolean circleLeftTrueRightFalse) {
        if (planType.isClosedPolygone()) {
            if (circleLeftTrueRightFalse == (sign > 0)) {
                Collections.reverse(path);
                Collections.reverse(polyNoGo);
                Collections.reverse(pathSafetyDist);
                sign *= -1;
            }
        }

        int maxPath = planType.isClosedPolygone() ? path.size() - 1 : path.size();
        Vec4 last = path.get(path.size() - 2);
        Vec4 next = path.firstElement();
        Vec4 diffLast = planType == PlanType.FACADE ? null : next.subtract3(last).normalize3();

        // compute baseline of path, maybe with too high angle changes, but with OK overlap
        ArrayList<TemporaryWaypoint> waypoints = new ArrayList();
        for (int i = 1; i < maxPath; i++) {
            // for each segment of the path
            last = next;
            next = path.get(i);
            Vec4 diff = next.subtract3(last);
            double len = diff.getLength3();
            if (len == 0) continue;

            diff = diff.multiply3(1 / len);
            double yawStraight = Math.toDegrees(Math.atan2(diff.x, diff.y)) - 90 + 90 * sign;

            Vec4 vOnObject = last;
            Vec4 diffAvg = diffLast == null ? diff : diffLast.add3(diff);
            double yawAvg = Math.toDegrees(Math.atan2(diffAvg.x, diffAvg.y)) - 90 + 90 * sign;
            double yawLastDiff = diffLast == null ? 0 : diffLast.angleBetween3(diff).degrees;

            Vec4 nextnext = path.get(Math.min(i + 1, path.size() - 1));
            Vec4 diffnext = nextnext.subtract3(next);
            double lennext = diffnext.getLength3();
            if (lennext > 0) {
                diffnext = diffnext.multiply3(1 / lennext);
            }

            double yawNextDiff = diff.angleBetween3(diffnext).degrees;

            vOnObject = new Vec4(vOnObject.x, vOnObject.y, 0);

            diffLast = diff;
            int noSubRows = (int)Math.ceil(len / sizeParallelFlightEff);
            double step = len / noSubRows;

            int max =
                noSubRows
                    + ((!planType.isClosedPolygone() && i == path.size() - 1) || yawNextDiff > maxYawRollChange
                        ? 1
                        : 0);

            double dist = 0;

            for (int n = 0; n < max; n++) {
                // adding subsegments, since typically a path segment is wide and will carry more then one image
                vOnObject = last.add3(diff.multiply3(dist));
                double yaw = n == 0 && yawLastDiff <= maxYawRollChange ? yawAvg : yawStraight;
                Orientation o = new Orientation(0, 90, yaw);

                double yawRad = Math.toRadians(yaw);
                // todo: check coordinate system used :)
                Vec4 camDist = new Vec4(Math.cos(yawRad), -Math.sin(yawRad));
                Vec4 vOnDrone = vOnObject.add3(camDist.multiply3(-distanceToObj));

                if (AutoFPhelper.isDroneInsideFaceadeOrBehind(
                        polyNoGo, pathSafetyDist, planType.isClosedPolygone(), vOnDrone, vOnObject)) {
                    // search closest legal point, and dont care if distanceToObject might get violated
                    vOnDrone =
                        AutoFPhelper.closestOnPolygone(
                            polyNoGo, pathSafetyDist, vOnDrone, vOnObject, planType.isClosedPolygone());
                    if (vOnDrone == null) {
                        dist += step;
                        continue;
                    }

                    Vec4 diff2 = vOnDrone.subtract3(vOnObject);
                    yaw = Math.toDegrees(Math.atan2(diff2.x, diff2.y)) + 90;
                    o.setYaw(yaw);
                }

                vOnDrone = new Vec4(vOnDrone.x, vOnDrone.y, altMin);
                vOnObject = new Vec4(vOnObject.x, vOnObject.y, altMin);
                waypoints.add(new TemporaryWaypoint(vOnDrone, o, vOnObject));

                dist += step;
            }
        }

        // add additional points for ensuring maximal angle change at corners
        ArrayList<TemporaryWaypoint> waypointsRefined = new ArrayList();
        TemporaryWaypoint lastWP =
            planType.isClosedPolygone() && !waypoints.isEmpty() ? waypoints.get(waypoints.size() - 1) : null;
        for (TemporaryWaypoint tmp : waypoints) {
            if (lastWP != null) {
                double yawDiff = tmp.orientation.getYaw() - lastWP.orientation.getYaw();
                while (yawDiff > 180) {
                    yawDiff -= 360;
                }

                while (yawDiff < -180) {
                    yawDiff += 360;
                }

                double yawDiffAbs = Math.abs(yawDiff);
                if (yawDiffAbs > maxYawRollChange) {
                    // refine line
                    int steps = (int)Math.ceil(yawDiffAbs / maxYawRollChange);
                    double yawStep = yawDiff / steps;
                    Vec4 posStep = tmp.positionOnObject.subtract3(lastWP.positionOnObject).divide3(steps);

                    for (int i = 1; i < steps; i++) {
                        // add intermediate points, make sure angles and object position is as desired, so drone flies
                        // curves
                        Orientation o = new Orientation(0, 90, lastWP.orientation.getYaw() + i * yawStep);
                        Vec4 positionOnObject = lastWP.positionOnObject.add3(posStep.multiply3(i));
                        double yawRad = Math.toRadians(o.getYaw());
                        // todo: check coordinate system used :)
                        Vec4 camDist = new Vec4(Math.cos(yawRad), -Math.sin(yawRad));
                        Vec4 vOnDrone = positionOnObject.add3(camDist.multiply3(-distanceToObj));

                        if (AutoFPhelper.isDroneInsideFaceadeOrBehind(
                                polyNoGo, pathSafetyDist, planType.isClosedPolygone(), vOnDrone, positionOnObject)) {
                            // search closest legal point, and dont care if distanceToObject might get violated
                            vOnDrone =
                                AutoFPhelper.closestOnPolygone(
                                    polyNoGo, pathSafetyDist, vOnDrone, positionOnObject, planType.isClosedPolygone());
                            if (vOnDrone == null) {
                                continue;
                            }

                            Vec4 diff2 = vOnDrone.subtract3(positionOnObject);
                            double yaw = Math.toDegrees(Math.atan2(diff2.x, diff2.y)) + 90;
                            o.setYaw(yaw);
                        }

                        vOnDrone = new Vec4(vOnDrone.x, vOnDrone.y, altMin);
                        positionOnObject = new Vec4(positionOnObject.x, positionOnObject.y, altMin);
                        TemporaryWaypoint insert = new TemporaryWaypoint(vOnDrone, o, positionOnObject);
                        waypointsRefined.add(insert);
                    }
                }
            }

            waypointsRefined.add(tmp);
            lastWP = tmp;
        }

        // multiply those points into z-direction
        ArrayList<ArrayList<TemporaryWaypoint>> waypointsAll = new ArrayList();
        for (TemporaryWaypoint tmp : waypointsRefined) {
            ArrayList<TemporaryWaypoint> column = new ArrayList<>();
            /*
            Add bottom waypoint
             */
            column.add(tmp);

            Vec4 v = tmp.positionOnDrone;
            double yaw =
                tmp.orientation.getYaw()
                    + getYaw(); // so far all rotations have been in local coordinate frame which is rotated relatively
            // to the world
            tmp.orientation.setYaw(yaw);

            /**
             * Add if needed (in the situation when minCropHeight is lower than minGroundDistance) waypoints between the
             * bottom one and the one on top of it to cover the bottom part of the building
             */
            if (minCropToMinGroundDist > 0) {
                // number of additional steps needed to cover the bottom part
                int steps = (int)Math.ceil(minCropToMinGroundDist / sizeInFlightEff);
                double stepSize = sizeInFlightEff / (steps + 1);
                for (double j = 0; j < steps; j++) {
                    double height = minGroundDistance + stepSize * (j + 1);
                    // angle of the waypoint to target a facade point
                    double pitchRad = Math.atan2(alt, height - (cropHeightMin + sizeInFlightEff * j));
                    double pitch = Math.toDegrees(pitchRad);
                    // shift that need to be applied to a waypoint towards the object to preserve the resolution
                    double shift = alt * (1 - Math.sin(pitchRad));
                    if (j == 0) {
                        this.distMinComputed = alt - shift;
                    }

                    double direction = (yaw - getYaw()) * Math.PI / 180.0;
                    // todo: check coordinate system used
                    double vx = v.x + Math.cos(direction) * shift;
                    double vy = v.y - Math.sin(direction) * shift;
                    Vec4 v1 = new Vec4(vx, vy, height);

                    Orientation oAvg_add = new Orientation(0, pitch, yaw);
                    column.add(new TemporaryWaypoint(v1, oAvg_add, null));
                }
            }

            double altMax = cropHeightMax;
            if (!isAddCeiling()) {
                altMax += overshootTotalLinesEnd;
            }

            double dAlt = altMax - altMin - sizeInFlightEff;
            int stepsToClimb = (int)Math.max(0, Math.ceil(dAlt / sizeInFlightEff));
            double stepAlt = Math.max(0, dAlt / stepsToClimb);

            for (int k = 0; k <= stepsToClimb; k++) {
                double alt = altMin + sizeInFlightEff + k * stepAlt;
                v = new Vec4(v.x, v.y, alt);
                column.add(new TemporaryWaypoint(v, tmp.orientation, null));
            }

            waypointsAll.add(column);
        }

        return waypointsAll;
    }

    public boolean isCheckOverlapPossibleWarning() {
        return checkOverlapPossibleWarning;
    }

    public void reduceOverlap() {
        this.lastAcceptedReducedOverlapInFlightDirection = Math.min(overlapInFlight, getOverlapInFlightMaxPossible());
        this.lastAcceptedReducedOverlapInFlightDirectionMin =
            Math.min(getOverlapInFlightMin(), getOverlapInFlightMaxPossible());

        try {
            setMute(true);
            setOverlapInFlight(this.lastAcceptedReducedOverlapInFlightDirection);
            setOverlapInFlightMin(this.lastAcceptedReducedOverlapInFlightDirectionMin);
        } finally {
            setMute(false);
        }
    }

    private boolean checkOverlapPossible(
            boolean silent, IPlatformDescription platformDescription, double sizeInFlight) {
        Flightplan flightplan = getFlightplan();
        Ensure.notNull(flightplan, "flightplan");
        // Debug.printStackTrace("silent",silent, this.hashCode());
        PhotoSettings photoSettings = (PhotoSettings)flightplan.getPhotoSettings();

        if (platformDescription.isInCopterMode()
                && photoSettings.getMaxGroundSpeedAutomatic().isAutomaticallyAdjusting()) {
            checkOverlapPossibleWarning = false;
            return true;
        }

        if (platformDescription.isInCopterMode()
                && photoSettings.getMaxGroundSpeedAutomatic() == FlightplanSpeedModes.MANUAL_CONSTANT
                && photoSettings.isStoppingAtWaypoints()) {
            checkOverlapPossibleWarning = false;
            return true;
        }

        // check if the camera could this trigger, and maybe adjust overlap
        double sizeInFlightEffMinPossible = photoSettings.getMaxGroundSpeedMPSec() * photoSettings.getMinTimeInterval();
        overlapInFlightMaxPossible = 100. * (1 - sizeInFlightEffMinPossible / sizeInFlight);

        double overlapInFlight = getOverlapInFlight();

        double reducedOverlapInFlightDirection = Math.min(overlapInFlight, getOverlapInFlightMaxPossible());
        double reducedOverlapInFlightDirectionMin = Math.min(getOverlapInFlightMin(), getOverlapInFlightMaxPossible());

        // Debug.printStackTrace(silent, sizeInFlight, overlapInFlight, overlapInFlightMaxPossible);
        if (overlapInFlight > getOverlapInFlightMaxPossible()) {
            if (this.lastAcceptedReducedOverlapInFlightDirection <= reducedOverlapInFlightDirection
                    && this.lastAcceptedReducedOverlapInFlightDirectionMin <= reducedOverlapInFlightDirectionMin) {
                checkOverlapPossibleWarning = false;
            } else {
                checkOverlapPossibleWarning = true;
                return false;
            }

            try {
                setMute(true);
                setOverlapInFlight(reducedOverlapInFlightDirection);
                setOverlapInFlightMin(reducedOverlapInFlightDirectionMin);
            } finally {
                setMute(false);
            }
        } else {
            checkOverlapPossibleWarning = false;
        }

        return true;
    }

    /*boolean wasRecalconce = false;

    public boolean wasRecalconce() {
        return wasRecalconce;
    }
    */

    public boolean computeFlightLines(boolean silent) {
        if (!computeFlightLinesNotSpreading(silent)) {
            return false;
        }

        return true;
    }

    /**
     * ask if nessesary, if FP should be split and store result in member var
     *
     * @return false, if calculation should be cancelt
     */
    public boolean askForSplitting() {
        // Debug.printStackTrace("ask for splitting");
        return true; // TODO FIXME remove this for productive version
    }

    @Override
    public boolean doSubRecalculationStage2() {
        boolean result = doRecalculation(true);
        updateModelReferencePoint();
        return result;
    }

    @Override
    public boolean doSubRecalculationStage1() {
        return true;
    }

    @Override
    public void flightplanStatementAdded(IFlightplanRelatedObject statement) {
        needsRecomputeSector.set(true);
        getCorners().setNeedsRecomputeSector(true);
        super.flightplanStatementAdded(statement);
    }

    @Override
    public void flightplanStatementChanged(IFlightplanRelatedObject statement) {
        if (isMute()) {
            return;
        }

        if (needsRecomputeSector == null) {
            // Yes... this happens...
            // Reason:
            //          The initializer of PicArea calls the initializer of CPicArea
            //          The initializer CPicArea does stuff and calls the base CPicArea::flightplanStatementChanged
            //          Since this was overridden this function here is called before the PicArea initializer finished,
            // hence needsRecomputeSector is not initialized the first time this function is called
            // How to avoid:
            //          Do _not_ call non-final methods in the initializer
            return;
        }

        needsRecomputeSector.set(true);
        getCorners().setNeedsRecomputeSector(true);
        super.flightplanStatementChanged(statement);
    }

    @Override
    public void flightplanStatementStructureChanged(IFlightplanRelatedObject statement) {
        needsRecomputeSector.set(true);
        getCorners().setNeedsRecomputeSector(true);
        super.flightplanStatementStructureChanged(statement);
    }

    @Override
    public void flightplanStatementRemove(int i, IFlightplanRelatedObject statement) {
        needsRecomputeSector.set(true);
        getCorners().setNeedsRecomputeSector(true);
        super.flightplanStatementRemove(i, statement);
    }

    public boolean doRecalculation(boolean silent) {
        if (!planType.doAutoComputation()) {
            return true;
        }

        try {
            if (!CountryDetector.instance.allowProceed(getSector())) {
                return false;
            }

            boolean reaskForSplitting = false;
            boolean reaskForTerrain = false;
            Flightplan flightplan = getFlightplan();
            Ensure.notNull(flightplan, "flightplan");
            IHardwareConfiguration hardwareConfiguration = flightplan.getHardwareConfiguration();

            if (!hardwareConfiguration.hasPrimaryPayload(IGenericCameraConfiguration.class)) {
                return false;
            }

            lastError = null;
            validAOI = true;
            boolean shouldAutoSync = true;

            IPlatformDescription platformDesc = hardwareConfiguration.getPlatformDescription();
            IGenericCameraConfiguration cameraConfig =
                hardwareConfiguration.getPrimaryPayload(IGenericCameraConfiguration.class);

            // somehow dirty fix to make sure camera copter mode, and altitude mode and AOI filling mode are in sync
            if (!getPlanType()
                    .isSelectable(
                        platformDesc.isInCopterMode(),
                        platformDesc.getMinWaypointSeparation().getValue().doubleValue() == 0)) {
                lastError = RecomputeErrors.MSG_AOT_TYPE_NOT_SUPPORTED;
                return false;
            }

            if (getPlanType().getNeededAltMode() != null) {
                flightplan.getPhotoSettings().setAltitudeAdjustMode(getPlanType().getNeededAltMode());
            }

            AltitudeAdjustModes shiftAltitudes = flightplan.getPhotoSettings().getAltitudeAdjustMode();

            // update transformation
            setupTransform();

            if (!computeFlightLines(silent)) {
                shouldAutoSync = false;
                return false;
            }

            if (reaskForSplitting && !askForSplitting()) {
                shouldAutoSync = false;
                return false;
            }

            // System.out.println("recalc 3");

            double overshootShift = overshootTotalLinesEnd - overshootInnerLinesEnd;

            if (planType == PlanType.COPTER3D) {
                IBackgroundTaskManager.BackgroundTask task =
                    new IBackgroundTaskManager.BackgroundTask("objFlightplanCompute") {
                        @Override
                        protected Void call() throws Exception {
                            updateProgress(0, 1000);
                            updateMessage("init");
                            if (isCancelled()) {
                                return null;
                            }

                            flightLines.clear();
                            PreviousOfTypeVisitor visPrev =
                                new PreviousOfTypeVisitor(PicArea.this, IFlightplanPositionReferenced.class);
                            visPrev.setSkipIgnoredPaths(true);
                            visPrev.startVisit(flightplan);
                            Vec4 prevVec = null;
                            if (visPrev.prevObj != null) {
                                IFlightplanPositionReferenced prevObj = (IFlightplanPositionReferenced)visPrev.prevObj;
                                prevVec = transformToLocal(LatLon.fromDegrees(prevObj.getLat(), prevObj.getLon()));
                                prevVec =
                                    new Vec4(
                                        prevVec.x,
                                        prevVec.y,
                                        prevObj.getAltInMAboveFPRefPoint()
                                            + flightplan.getRefPointAltWgs84WithElevation());
                            }

                            NextOfTypeVisitor visNext =
                                new NextOfTypeVisitor(PicArea.this, IFlightplanPositionReferenced.class);
                            visNext.setSkipIgnoredPaths(true);
                            visNext.startVisit(flightplan);
                            Vec4 nextVec = null;
                            if (visNext.nextObj != null) {
                                IFlightplanPositionReferenced nextObj = (IFlightplanPositionReferenced)visNext.nextObj;
                                nextVec = transformToLocal(LatLon.fromDegrees(nextObj.getLat(), nextObj.getLon()));
                                nextVec =
                                    new Vec4(
                                        nextVec.x,
                                        nextVec.y,
                                        nextObj.getAltInMAboveFPRefPoint()
                                            + flightplan.getRefPointAltWgs84WithElevation());
                            }

                            LocalTransformationProvider trafo;
                            if (getModelSource() == ModelSourceTypes.TERRAIN) {
                                getFlightplan().getRefPoint().updateAltitudeWgs84();
                                trafo =
                                    new LocalTransformationProvider(
                                        new Position(
                                            getFlightplan().getRefPoint().getLatLon(),
                                            getFlightplan().getRefPoint().getAltInMAboveFPRefPoint()
                                                + getFlightplan().getRefPoint().getAltitudeWgs84()),
                                        Angle.fromDegrees(90 + getYaw()),
                                        0,
                                        0,
                                        true);
                            } else {
                                modelReferencePoint.updateAltitudeWgs84();
                                trafo =
                                    new LocalTransformationProvider(
                                        new Position(
                                            modelReferencePoint.getLatLon(),
                                            modelReferencePoint.getAltInMAboveFPRefPoint()
                                                + modelReferencePoint.getAltitudeWgs84()),
                                        Angle.fromDegrees(90 + getYaw()),
                                        0,
                                        0,
                                        true);
                            }

                            Vector<Vec4> corvecLocalModelSystem = new Vector<>();
                            for (LatLon latLon : getCornersVec()) {
                                corvecLocalModelSystem.add(trafo.transformToLocal(latLon));
                            }

                            AllPointsResult result =
                                ObjectFlightplanAlg.computeObjectCoverageFlight(
                                    corvecLocalModelSystem,
                                    sizeParallelFlightEff,
                                    sizeInFlightEff,
                                    alt,
                                    prevVec,
                                    nextVec,
                                    PicArea.this,
                                    trafo,
                                    this);
                            if (isCancelled()) {
                                return null;
                            }

                            updateProgress(ObjectFlightplanAlg.maxProgress - 1, ObjectFlightplanAlg.maxProgress);
                            updateMessage("store waypoints");

                            int lineNo = 0;
                            for (Vector<FlightplanVertex> sub : result.allSubClouds) {
                                FlightLine fl =
                                    new FlightLine(sub, lineNo, trafo, flightplan.getRefPointAltWgs84WithElevation());
                                flightLines.add(fl);
                                lineNo++;
                            }

                            Dispatcher.postToUI(
                                () -> {
                                    try {
                                        flightlinesToWaypoints(
                                            flightLines, flightplan, platformDesc, cameraConfig, shiftAltitudes);
                                    } catch (FlightplanContainerFullException e) {
                                        PicArea.this.elements.clear();
                                        lastError = RecomputeErrors.MSG_TOO_MANY_POINTS;
                                        Debug.getLog()
                                            .log(
                                                Debug.WARNING,
                                                "Flight plan will contain too many points, please reduce size: " + this,
                                                e);
                                        validAOI = false;
                                    } catch (Exception e) {
                                        PicArea.this.elements.clear();
                                        lastError = RecomputeErrors.MSG_EXCEPTION;
                                        Debug.getLog()
                                            .log(
                                                Level.SEVERE,
                                                "Something went wrong during recalculation of picture Area: " + this,
                                                e);
                                    }
                                });
                            return null;
                        }
                    };

                DependencyInjector.getInstance().getInstanceOf(IBackgroundTaskManager.class).submitTask(task);
            } else if (planType == PlanType.TARGET_POINTS) {
                setGsd(1); // to simplify math for field of view below..

                while (sizeOfFlightplanContainer() > 0) {
                    removeFromFlightplanContainer(0);
                }

                Photo photo = new Photo(true, sizeInFlightEff * 100, sizeInFlightEffMax * 100, 0, null);
                photo.setTriggerOnlyOnWaypoints(true);
                addToFlightplanContainer(photo);

                CFlightplan fp = getFlightplan();
                CPhotoSettings photoSettings = fp.getPhotoSettings();
                FlightplanSpeedModes speedMode = photoSettings.getMaxGroundSpeedAutomatic();
                boolean forceTriggerIndividualImages =
                    photoSettings.isStoppingAtWaypoints() && speedMode == FlightplanSpeedModes.MANUAL_CONSTANT;
                // TODO.. in this case we dont have to wait sooo long until image is stored, we could wait shorter?
                double minTimeInterval = forceTriggerIndividualImages ? photoSettings.getMinTimeInterval() * 1000 : 0;

                try (BufferedReader br = new BufferedReader(new FileReader(new File(getModelFilePath())))) {
                    String line;
                    int totalCount = 0;
                    double totalAlt = 0;
                    while ((line = br.readLine()) != null) {
                        try {
                            line = line.trim();
                            if (line.startsWith("#") || line.isEmpty()) continue;

                            String[] parts = line.split(Pattern.quote(";"));
                            if (parts == null || parts.length < 11 || parts[0].trim().isEmpty()) continue;
                            String name = parts[0] + " " + parts[1];
                            double lat = Double.parseDouble(parts[3].replace(",", "."));
                            double lon = Double.parseDouble(parts[2].replace(",", "."));
                            // double heightOverMsl = Double.parseDouble(parts[4].replace(",", "."));
                            double heightOverTerrain = Double.parseDouble(parts[5].replace(",", ".")) * 0.3048;
                            double heading = Double.parseDouble(parts[6].replace(",", "."));
                            double pitch = Double.parseDouble(parts[7].replace(",", ".")) + 90;
                            double fovHoriz = Double.parseDouble(parts[9].replace(",", ".")) * 0.3048;
                            double fovVert = Double.parseDouble(parts[10].replace(",", ".")) * 0.3048;
                            // double terrainHeightInput = heightOverTerrain - heightOverMsl;

                            LatLon latLonImgCenter = LatLon.fromDegrees(lat, lon);
                            double terrainHeightIMC = elevationModel.getElevationAsGoodAsPossible(latLonImgCenter);

                            Position posImgCenter =
                                new Position(
                                    latLonImgCenter,
                                    terrainHeightIMC
                                        + heightOverTerrain
                                        + -getFlightplan().getRefPointAltWgs84WithElevation());
                            Orientation o = new Orientation(0, pitch, heading);

                            double camTargetAlt = alt * Math.max(fovHoriz / sizeParallelFlight, fovVert / sizeInFlight);
                            totalAlt += camTargetAlt;
                            totalCount++;
                            Vec4 direction = new Vec4(0, 0, camTargetAlt);
                            double roll = 0;
                            Matrix m = MathHelper.getRollPitchYawTransformationMAVinicAngles(roll, pitch, heading);
                            direction = direction.transformBy4(m.getInverse());
                            LocalTransformationProvider localTransformation =
                                new LocalTransformationProvider(posImgCenter, Angle.ZERO, 0, 0, true);
                            Position posDrone = localTransformation.transformToGlobe(direction);

                            Waypoint wp =
                                new Waypoint(
                                    posDrone.longitude.degrees,
                                    posDrone.latitude.degrees,
                                    posDrone.elevation,
                                    AltAssertModes.linear,
                                    0,
                                    name,
                                    0,
                                    null);
                            wp.setTargetDistance(camTargetAlt);
                            wp.setTriggerImageHereCopterMode(true);
                            wp.setStopHereTimeCopter(minTimeInterval);
                            wp.setSpeedMpSec(optimalSpeedThisAoi);
                            wp.setOrientation(o);
                            if (totalCount == 1) {
                                wp.setBeginFlightline(true);
                            }

                            addToFlightplanContainer(wp);
                        } catch (Exception e) {
                            Debug.getLog().log(Level.INFO, "cant parse line:" + line, e);
                        }
                    }

                    if (totalCount > 0) {
                        totalAlt /= totalCount;
                        // setAlt(totalAlt);
                    }

                } catch (Exception e) {
                    lastError = RecomputeErrors.MSG_EXCEPTION;
                    Debug.getLog().log(Level.WARNING, "cant compute target points", e);
                }
            } else {
                flightlinesToWaypoints(flightLines, flightplan, platformDesc, cameraConfig, shiftAltitudes);
            }
        } catch (FlightplanContainerFullException e) {
            this.elements.clear();
            lastError = RecomputeErrors.MSG_TOO_MANY_POINTS;
            Debug.getLog()
                .log(Debug.WARNING, "Flight plan will contain too many points, please reduce size: " + this, e);
            validAOI = false;
        } catch (Exception e) {
            this.elements.clear();
            lastError = RecomputeErrors.MSG_EXCEPTION;
            Debug.getLog().log(Level.SEVERE, "Something went wrong during recalculation of picture Area: " + this, e);
        }

        return true;
    }

    private void flightlinesToWaypoints(
            Vector<FlightLine> flightLines,
            Flightplan flightplan,
            IPlatformDescription platformDesc,
            IGenericCameraConfiguration cameraConfig,
            AltitudeAdjustModes shiftAltitudes)
            throws FlightplanContainerFullException, FlightplanContainerWrongAddingException {
        DummyContainer dummyContainer = new DummyContainer();
        try {
            setMute(true);
            if (!flightLines.isEmpty()) {

                // compute next positionOnDrone after picArea

                // if landing for example is missing - than in case of the "ScanDirectionsTypes.towardLaning" for a
                // single AOI
                // we will have to mock it up with reversing the ScanDirectionsTypes.fromStarting scenario by using the
                // noNextElement
                boolean noNextElement = false;
                NextOfTypeVisitor vis = new NextOfTypeVisitor(this, ILatLonReferenced.class, Point.class);
                vis.setSkipIgnoredPaths(true);
                vis.startVisit(flightplan);
                ILatLonReferenced wp = (ILatLonReferenced)vis.nextObj;
                if (wp == null || isNullIsland(wp.getLatLon()) || wp == getFlightplan().getRefPoint()) {
                    if (!flightplan.getLandingpoint().isEmpty()) {
                        wp = flightplan.getLandingpoint();
                    } else {
                        wp = flightplan.getTakeoff();
                        noNextElement = true;
                    }
                }

                LatLon nextLatLon = wp.getLatLon();
                // System.out.println("nextLatLon="+nextLatLon + " nextWP:" + wp);
                Vec4 nextVec = null;
                if (nextLatLon != null) {
                    nextVec = transformToLocal(nextLatLon);
                }
                // compute previous positionOnDrone before picArea
                PreviousOfTypeVisitor vis2 = new PreviousOfTypeVisitor(this, ILatLonReferenced.class, Point.class);
                vis2.setSkipIgnoredPaths(true);
                vis2.startVisit(flightplan);
                wp = (ILatLonReferenced)vis2.prevObj;
                if (wp == null || isNullIsland(wp.getLatLon()) || wp == getFlightplan().getRefPoint()) {
                    wp = flightplan.getTakeoff();
                }

                LatLon previousLatLon = wp.getLatLon();
                // System.out.println("previousLatLon="+previousLatLon + " previousWP:" + wp);
                Vec4 previousVec = null;
                if (previousLatLon != null) {
                    previousVec = transformToLocal(previousLatLon);
                }

                // reorders flight lines and points due to the startCaptureVertically parameter

                // because ceiling is always added in the end of the flight lines list
                // why addCeiling is true for a facade ??
                if (startCaptureVertically.equals(StartCaptureVerticallyTypes.up)
                        && (addCeiling
                                && (planType == PlanType.BUILDING
                                    || planType == PlanType.TOWER
                                    || planType == PlanType.WINDMILL)
                            || verticalScanPattern.equals(VerticalScanPatternTypes.leftRight))) {
                    Collections.reverse(flightLines);
                    FlightLine.reverseAllInsideLines(flightLines);
                } else if (planType.useStartCaptureVertically()) {
                    boolean startsAtTop = startsAtTheTop(flightLines);
                    if (((startCaptureVertically.equals(StartCaptureVerticallyTypes.up) && !startsAtTop))
                            || ((startCaptureVertically.equals(StartCaptureVerticallyTypes.down) && startsAtTop))) {
                        Vector<FlightLine> other = FlightLine.deepCopy(flightLines);
                        for (FlightLine fl : other) {
                            if (!MathHelper.isDifferenceTiny(
                                    fl.getCorners().firstElement().z, fl.getCorners().lastElement().z)) {
                                fl.reverseAllCollections();
                            }
                        }

                        flightLines = other;
                    }
                } else {
                    if (nextLatLon != null && planType.shouldPermuteLines()) {
                        // since I am reversing / mirrowing some parts of the flightLines list all the time in the
                        // permuting / rectangularing process, its not predictable which initial permutation / mirrowing
                        // would give the best result. since we have only 4 possible combinations, just try all of them
                        // ;-)

                        Vector<FlightLine> other;
                        ArrayList<Vector<FlightLine>> options = new ArrayList<>(4);

                        if (this.scanDirection == ScanDirectionsTypes.towardLaning
                                || this.scanDirection == ScanDirectionsTypes.fromStarting) {
                            // these find the corner with the shortest distance to the start

                            options.add(FlightLine.deepCopy(flightLines));

                            other = FlightLine.deepCopy(flightLines);
                            FlightLine.mirrowAll(other);
                            options.add(other);

                            other = FlightLine.deepCopy(flightLines);
                            Collections.reverse(other);
                            other = FlightLine.switchBlocks(other);
                            options.add(other);

                            other = FlightLine.deepCopy(other);
                            FlightLine.mirrowAll(other);
                            options.add(other);
                        } else {
                            // these specify explicitly which corner to start with

                            if (this.scanDirection == ScanDirectionsTypes.cornerXminYmin) { // bottom left
                                options.add(FlightLine.deepCopy(flightLines));
                            } else if (this.scanDirection == ScanDirectionsTypes.cornerXminYmax) { // top left
                                other = FlightLine.deepCopy(flightLines);
                                FlightLine.mirrowAll(other);
                                options.add(other);
                            } else if (this.scanDirection == ScanDirectionsTypes.cornerXmaxYmin) { // bottom right
                                other = FlightLine.deepCopy(flightLines);
                                Collections.reverse(other);
                                other = FlightLine.switchBlocks(other);
                                options.add(other);
                            } else if (this.scanDirection == ScanDirectionsTypes.cornerXmaxYmax) { // top right
                                other = FlightLine.deepCopy(flightLines);
                                Collections.reverse(other);
                                other = FlightLine.switchBlocks(other);
                                FlightLine.mirrowAll(other);
                                options.add(other);
                            }
                        }

                        double bestDist = Double.POSITIVE_INFINITY;
                        // int i = 0;
                        // int bestK = -1;
                        for (Vector<FlightLine> flightLinesTmp : options) {

                            // permute lines locally, so turnradius is preserved
                            flightLinesTmp =
                                FlightLine.permuteLines(
                                    platformDesc,
                                    sizeParallelFlightEff,
                                    flightLinesTmp,
                                    shiftAltitudes,
                                    isOnlySingleDirection());

                            // make each turn a 90° turn... extend shorter outer lines if nessesary
                            flightLinesTmp =
                                FlightLine.assureTurnRadius(
                                    this,
                                    isOnlySingleDirection(),
                                    flightLinesTmp,
                                    shiftAltitudes,
                                    platformDesc,
                                    null,
                                    planType);

                            double dist;
                            if (scanDirection == ScanDirectionsTypes.towardLaning) {
                                dist = nextVec.distanceTo3(flightLinesTmp.lastElement().getCorners().lastElement());
                            } else { // if (this.scanDirection == ScanDirectionsTypes.fromStarting){
                                // actually we use this for all other cases, since in that situations we anyway have
                                // only one option..
                                dist =
                                    previousVec.distanceTo3(flightLinesTmp.firstElement().getCorners().firstElement());
                            }
                            // System.out.println(dist);

                            // if we want to be closest to landing but there is no landing - then be furthest from the
                            // takeoff
                            if (dist < bestDist) {
                                bestDist = dist;
                                flightLines.clear();
                                flightLines.addAll(flightLinesTmp);
                                // System.out.println("best i:" + i);
                                // bestK = i;
                            }

                            // i++;
                        }
                    }
                }

                AutoFPhelper.fillContainer(
                    flightLines,
                    alt,
                    sizeInFlightEff,
                    sizeInFlightEffMax,
                    sizeParallelFlightEff,
                    idForInitPhoto,
                    nextFreeLineNo,
                    -1,
                    -1,
                    isOnlySingleDirection(),
                    flightplan.getPhotoSettings().getGsdTolerance(),
                    dummyContainer,
                    shiftAltitudes,
                    platformDesc,
                    cameraConfig,
                    getYaw(),
                    cameraTiltToggleEnable,
                    cameraTiltToggleDegrees,
                    optimalSpeedThisAoi,
                    cameraPitchOffsetDegrees,
                    cameraRollToggleEnable,
                    cameraRollToggleDegrees,
                    cameraRollOffsetDegrees,
                    pitchOffsetLineBegin,
                    planType.keepPointOnTargetContantOnRotations());

                // transform4 = transform4.getInverse();
            }

            wasRecalconce = true;
        } finally {
            // atomic sync of changes!
            this.elements = dummyContainer.getElements();
            setSilentUnmute();
            fireSyncedChange();
        }
    }

    public VoxelGrid voxelGridTmp;

    private boolean startsAtTheTop(Vector<FlightLine> flightLines) {
        if (flightLines.size() > 0) {
            FlightLine line0 = flightLines.get(0);
            Vec4 point0;
            Vec4 pointN;
            if (verticalScanPattern.equals(VerticalScanPatternTypes.upDown)) {
                List<Vec4> points = line0.getCorners();
                if (points.size() > 1) {
                    point0 = points.get(0);
                    pointN = points.get(points.size() - 1);
                } else {
                    return false;
                }
            } else {
                FlightLine lineN = flightLines.get(flightLines.size() - 1);
                point0 = line0.getCorners().get(0);
                pointN = lineN.getCorners().get(0);
            }

            if (point0.z - pointN.z > 0) {
                return true;
            } else {
                return false;
            }
        }

        return false;
    }

    @Override
    public Flightplan getFlightplan() {
        return (Flightplan)super.getFlightplan();
    }

    @Override
    public PicArea getCopy() {
        return new PicArea(this);
    }

    public RecomputeErrors getLastError() {
        return lastError;
    }

    public void optimizeArea(double maxAspectRatio) {
        lastError = null;
        if (!planType.canOptimizeCorners()) {
            return;
        }

        Flightplan flightplan = getFlightplan();
        Ensure.notNull(flightplan, "flightplan");

        IHardwareConfiguration hardwareConfig = flightplan.getHardwareConfiguration();
        IPlatformDescription platformDesc = flightplan.getHardwareConfiguration().getPlatformDescription();

        if (planType == PlanType.SPIRAL) {
            double radiusInit = sizeParallelFlightEff;

            final double maxtime = platformDesc.getMaxFlightTime().convertTo(Unit.MINUTE).getValue().doubleValue() * 60;
            try {
                setMute(true);

                getCorners()
                    .addToFlightplanContainer(
                        new Point(
                            getCorners(),
                            flightplan.getLandingpoint().getLat(),
                            flightplan.getLandingpoint().getLon()));
                // compute flight time
                corLatLon = null;
                setupTransformNotSpreading();

                // fallback to fixed ratio approach
                MinMaxPair timeP =
                    MathHelper.findRootNestedIntervals(
                        new IObjectiveFuntion() {

                            @Override
                            public double getValue(double val) throws Exception {
                                double time = estimateTimeSpiral(val);
                                // System.out.println("radius:" +val + "->"+ time + " .. "+maxtime);
                                return time - maxtime;
                            }
                        },
                        radiusInit,
                        platformDesc.getMaxLineOfSight().convertTo(Unit.METER).getValue().doubleValue(),
                        1.1,
                        0.001,
                        80);
                double radiusBest = timeP.min;
                LineOfSightVisitor vis = new LineOfSightVisitor(flightplan, new Position(getCornersVec().get(0), 0));
                vis.startVisit(flightplan);
                double time = estimateTimeSpiral(radiusBest);
                double bestArea = getArea();

                DependencyInjector.getInstance()
                    .getInstanceOf(IApplicationContext.class)
                    .addToast(
                        Toast.of(ToastType.INFO)
                            .setText(
                                languageHelper.getString(
                                    KEY + ".optimalAreaRadiusMsg",
                                    StringHelper.secToShortDHMS(time),
                                    StringHelper.areaToIngName(bestArea, -3, false),
                                    StringHelper.lengthToIngName(radiusBest, -3, false)))
                            .create());

            } catch (Exception e) {
                Debug.getLog().log(Level.WARNING, "could not maximize AOI area", e);
            } finally {
                setMute(false);
            }

            return;
        }

        OptimizePicAreaRequest request = new OptimizePicAreaRequest(this);
        // print too much
        // Debug.profiler.requestStarting(request);

        try {
            setMute(true);

            // in case this FP was not recomputed yet, make sure transformations are avaliable
            setupTransformNotSpreading();
            // this provides some camera footprint details
            computeFlightLinesNotSpreading(true);

            final Position pilotPoint =
                elevationModel.getPositionOverGround(
                    LatLon.fromDegrees(flightplan.getLandingpoint().getLat(), flightplan.getLandingpoint().getLon()));

            alt = altitudeGsdCalculator.getAlt();

            double[] effectiveFootprint = CameraHelper.getSizeInFlight(alt, hardwareConfig);
            if (effectiveFootprint.length == 0) {
                lastError = RecomputeErrors.MSG_CAM_NOT_MATCHABLE;
                DependencyInjector.getInstance()
                    .getInstanceOf(IApplicationContext.class)
                    .addToast(
                        Toast.of(ToastType.INFO)
                            .setText(languageHelper.getString(KEY + ".msgcamnotmatchable"))
                            .create());
                return;
            }

            sizeParallelFlightEff = (1. - getOverlapParallel() / 100.) * sizeParallelFlight;

            sizeInFlight = effectiveFootprint[3];

            if (!checkOverlapPossible(false, platformDesc, sizeInFlight)) {
                return;
            }

            sizeInFlightEff = (1. - getOverlapInFlight() / 100.) * sizeInFlight;

            flightplan.getPhotoSettings().setMultiFP(false);

            final double maxtime = platformDesc.getMaxFlightTime().convertTo(Unit.MINUTE).getValue().doubleValue() * 60;

            double x =
                maxtime * platformDesc.getMaxPlaneSpeed().convertTo(Unit.METER_PER_SECOND).getValue().doubleValue() / 8;
            x = Math.min(x, platformDesc.getMaxLineOfSight().convertTo(Unit.METER).getValue().doubleValue());
            double y = sizeParallelFlightEff; // this will most likely generate 3 flightlines-> minimum
            double yInit = y;
            double optimalX = x;
            double optimalY = y;
            double bestArea = -1;

            if (maxAspectRatio > 1) {
                // System.out.println("init maxAspect:" + maxAspectRatio);

                // find step where new line is added
                final double timeInitF = estimateTime(x, y, null);
                // if (1==1) return;
                // System.out.println("timeInitF:"+timeInitF +" x"+x+ " y"+y);
                final double xF = x;
                MinMaxPair minMaxY =
                    MathHelper.findRootNestedIntervals(
                        new IObjectiveFuntion() {

                            @Override
                            public double getValue(double val) throws Exception {
                                return estimateTime(xF, val, null) - timeInitF - 1;
                            }
                        },
                        y,
                        (y + sizeParallelFlightEff / 2.) / y,
                        0.01,
                        50);

                yInit = minMaxY.min;
                y = yInit;
                // System.out.println("Y " + y );

                while (true) {
                    // System.out.println("while");
                    final double yF = y;
                    MinMaxPair timeP =
                        MathHelper.findRootNestedIntervals(
                            new IObjectiveFuntion() {

                                @Override
                                public double getValue(double val) throws Exception {
                                    return estimateTime(val, yF, pilotPoint) - maxtime;
                                }
                            },
                            y,
                            2,
                            0.01,
                            80);
                    x = timeP.min;
                    double area = x * y * 4;
                    System.out.println(
                        "x:" + x + " y:" + y + " -> time:" + estimateTime(x, y, pilotPoint) + " area:" + area);
                    LineOfSightVisitor vis = new LineOfSightVisitor(flightplan, pilotPoint);
                    vis.startVisit(flightplan);
                    if (area > bestArea
                            && x / y <= maxAspectRatio
                            && y / x <= maxAspectRatio
                            && vis.isValid(platformDesc)) {
                        optimalX = x;
                        optimalY = y;
                        bestArea = area;
                        // System.out.println("new Opt");
                    }

                    y += sizeParallelFlightEff;

                    if (y > maxAspectRatio * x) {
                        break;
                    }

                    if (y > platformDesc.getMaxLineOfSight().convertTo(Unit.METER).getValue().doubleValue()) {
                        break;
                    }
                }
                // System.out.println("OPTIMAL: x:"+optimalX + " y:"+optimalY+" -> area:"+bestArea);

            }

            if (bestArea < 0) {
                // fallback to fixed ratio approach
                MinMaxPair timeP =
                    MathHelper.findRootNestedIntervals(
                        new IObjectiveFuntion() {

                            @Override
                            public double getValue(double val) throws Exception {
                                return estimateTime(val * maxAspectRatio, val, pilotPoint) - maxtime;
                            }
                        },
                        yInit,
                        2,
                        0.01,
                        80);
                y = timeP.min;
                x = y * maxAspectRatio;
                double area = x * y * 4;
                LineOfSightVisitor vis = new LineOfSightVisitor(flightplan, pilotPoint);
                vis.startVisit(flightplan);
                // System.out.println("x:"+x + " y:"+y+" -> time:"+estimateTime(x, y,pilotPoint)+" area:"+area);
                if (area > bestArea
                        && x / y <= maxAspectRatio
                        && y / x <= maxAspectRatio
                        && vis.isValid(platformDesc)) {
                    optimalX = x;
                    optimalY = y;
                    bestArea = area;
                    // System.out.println("new Opt");
                }
            }

            double time = estimateTime(optimalX, optimalY, pilotPoint);

            if (bestArea < 0) {
                throw new Exception("No solution found");
            }

            DependencyInjector.getInstance()
                .getInstanceOf(IApplicationContext.class)
                .addToast(
                    Toast.of(ToastType.INFO)
                        .setText(
                            languageHelper.getString(
                                KEY + ".optimalAreaMsg",
                                StringHelper.secToShortDHMS(time),
                                StringHelper.areaToIngName(bestArea, -3, false),
                                optimalX / optimalY,
                                maxAspectRatio))
                        .create());
        } catch (Exception e) {
            Debug.getLog().log(Level.WARNING, "could not maximize AOI area", e);
        } finally {
            setMute(false);
        }
    }

    private double estimateTimeSpiral(double radius) throws Exception {
        setMute(true);

        // compute flight time
        corLatLon = null;
        corridorWidthInMeter = radius;

        setMute(true);
        Flightplan flightplan = getFlightplan();
        Ensure.notNull(flightplan, "flightplan");
        flightplan.doFlightplanCalculation();

        IPlatformDescription platformDesc = flightplan.getHardwareConfiguration().getPlatformDescription();
        double speed = platformDesc.getMaxPlaneSpeed().convertTo(Unit.METER_PER_SECOND).getValue().doubleValue();
        flightplan.resetDistanceVisitorCache();
        LineOfSightVisitor vis = new LineOfSightVisitor(flightplan, new Position(getCornersVec().get(0), 0));
        vis.startVisit(flightplan);

        if (!vis.isValid(platformDesc)) {
            return (vis.getMaxDistance2d()
                        - platformDesc.getMaxLineOfSight().convertTo(Unit.METER).getValue().doubleValue())
                    / speed
                + platformDesc.getMaxFlightTime().convertTo(Unit.MINUTE).getValue().doubleValue() * 60 * 10000;
        }

        return flightplan.getTimeInSec();
    }

    private double estimateTime(double xHalfWidth, double yHalfWidth, Position pilotPoint) throws Exception {
        setMute(true);

        System.out.println("xHalfWidth:" + xHalfWidth + " yHalfWidth:" + yHalfWidth + "  pilotPoint:" + pilotPoint);
        Flightplan flightplan = getFlightplan();
        Ensure.notNull(flightplan, "flightplan");

        LocalTransformationProvider trafo =
            new LocalTransformationProvider(getFlightplan().getTakeoffPosition(), Angle.ZERO, 0, 0, true);
        // cleanup

        // set square corners
        Vec4[] vec =
            new Vec4[] {
                new Vec4(xHalfWidth, yHalfWidth),
                new Vec4(-xHalfWidth, yHalfWidth),
                new Vec4(-xHalfWidth, -yHalfWidth),
                new Vec4(xHalfWidth, -yHalfWidth)
            };
        for (Vec4 v : vec) {
            LatLon latLon = trafo.transformToGlobe(v);
            // System.out.println("V:" +v+ " -> "+latLon);
            getCorners()
                .addToFlightplanContainer(
                    new Point(getCorners(), latLon.getLatitude().degrees, latLon.getLongitude().degrees));
        }
        // compute flight time
        flightplan.doFlightplanCalculation();
        setMute(true);
        IPlatformDescription platformDescription = flightplan.getHardwareConfiguration().getPlatformDescription();
        double speed = platformDescription.getMaxPlaneSpeed().convertTo(Unit.METER_PER_SECOND).getValue().doubleValue();
        flightplan.resetDistanceVisitorCache();
        if (pilotPoint != null) {
            LineOfSightVisitor vis = new LineOfSightVisitor(flightplan, pilotPoint);
            vis.startVisit(flightplan);

            if (!vis.isValid(platformDescription)) {
                return (vis.getMaxDistance2d()
                            - platformDescription.getMaxLineOfSight().convertTo(Unit.METER).getValue().doubleValue())
                        / speed
                    + platformDescription.getMaxFlightTime().convertTo(Unit.MINUTE).getValue().doubleValue()
                        * 60
                        * 10000;
            }
        }

        return flightplan.getTimeInSec();
    }

    public LatLon getCenter() {
        if (!getPlanType().isClosedPolygone() && corridorHelper != null && corridorHelper.isValid()) {
            MinMaxPair minMaxParallel = new MinMaxPair();
            Vec4 center = corridorHelper.getShiftedPoint(0, getCornersVec().size() <= 2 ? 0.75 : 0.5);
            return transformToGlobe(center);
        } else {
            return MapLayerPicArea.meanPoint(getCornersVec());
        }
    }

    /**
     * to get some meaningful takeoff estimation which does not depend on the order of the flightlines for 3D objects
     * such as Building, Facade, Tower
     *
     * @return LatLon of the estimated point
     */
    public LatLon getCenterShiftedInOtherDirection() {
        LatLon center;
        int sign = 1;
        if (planType.equals(PlanType.BUILDING)) {
            if (cornerList != null && AutoFPhelper.computeAreaWithSign(cornerList) > 0) {
                sign = -1;
            }
        } else if (planType.equals(PlanType.FACADE)) {
            if (getFacadeScanningSide() == FacadeScanningSide.left) {
                sign = -1;
            }
        } else if (planType.equals(PlanType.TOWER) || planType.equals(PlanType.WINDMILL)) {
            double shift = alt + corridorWidthInMeter;
            if (transform3Inv != null) {
                center = transformToGlobe(new Vec4(0, shift, 0));
                return center;
            }
        } else {
            center = MapLayerPicArea.meanPoint(getCornersVec());
            return center;
        }

        if (corridorHelper != null && corridorHelper.isValid()) {
            Vec4 shifted = corridorHelper.getShiftedPoint(sign * alt, -1);
            center = transformToGlobe(shifted);
        } else {
            center = MapLayerPicArea.meanPoint(getCornersVec());
        }

        return center;
    }

    @Override
    public LatLon getCenterShifted() {
        LatLon newCenter = getCenter();
        if (newCenter != null) {
            if (getCorners().sizeOfFlightplanContainer() > 0) {
                LatLon corner1 = getCorners().getFromFlightplanContainer(0).getLatLon();
                LatLongInterpolationUtils.LatLongPairInterpolator interpolation =
                    LatLongInterpolationUtils.makeFastInterpolatorIfSafe(corner1, newCenter);
                newCenter = interpolation.interpolate(.8);
            }

            return newCenter;
        }

        return null;
    }

    @Override
    public void updateModelReferencePoint() {
        ReferencePoint newCenter = getFlightplan().getRefPoint();
        if (newCenter != null && !newCenter.equals(modelReferencePoint.getLatLon())) {
            modelReferencePoint.setValues(newCenter);
        }
    }

    public LatLon getNorthernmostVertexPosition() {
        LatLon northermostVertex = null;
        PicAreaCorners corners = getCorners();
        for (int i = 0; i < corners.sizeOfFlightplanContainer(); i++) {
            Point pointFromFlightplanContainer = corners.getPointFromFlightplanContainer(i);
            if (northermostVertex == null) {
                northermostVertex = pointFromFlightplanContainer.getLatLon();
            }

            if (northermostVertex.getLatitude().degrees < pointFromFlightplanContainer.getLat()) {
                northermostVertex = pointFromFlightplanContainer.getLatLon();
            } else if (northermostVertex.getLatitude().degrees == pointFromFlightplanContainer.getLat()) {
                if (northermostVertex.getLongitude().degrees < pointFromFlightplanContainer.getLon()) {
                    northermostVertex = pointFromFlightplanContainer.getLatLon();
                }
            }
        }

        return northermostVertex;
    }

    public double getLength() {
        return corridorHelper != null && planType == PlanType.CORRIDOR ? corridorHelper.getCenterLength() : 0;
    }

    @Override
    public Angle transformYawToLocal(Angle yaw) {
        return yaw.subtract(
            Angle.fromDegrees(getYaw() - 90)); // TODO FIXME.. I dont know if it has to be add or substract
    }

    @Override
    public Angle transformYawToGlobal(Angle yaw) {
        return yaw.add(Angle.fromDegrees(getYaw() - 90)); // TODO FIXME.. I dont know if it has to be add or substract
    }

    public double getOverlapInFlightMaxPossible() {
        return overlapInFlightMaxPossible;
    }

    public static enum RecomputeErrors {
        MSG_TOO_VIEW_CORNERS,
        MSG_CAM_NOT_MATCHABLE,
        MSG_AREA_TOO_SMALL,
        MSG_TOO_MANY_POINTS,
        MSG_FLAT_DE_MNOT_COMPATIBLE,
        MSG_EXCEPTION,
        MSG_AOT_TYPE_NOT_SUPPORTED,
        MSG_NO_WAYPOINTS;
    }

    private class DummyContainer extends AFlightplanContainer {

        @Override
        public void reassignIDs() {}

        @Override
        public CFlightplan getFlightplan() {
            return PicArea.this.getFlightplan();
        }

        @Override
        public IFlightplanRelatedObject getCopy() {
            return null;
        }

        @Override
        public IFlightplanContainer getParent() {
            return null;
        }

        @Override
        public void setParent(IFlightplanContainer container) {}

        @Override
        public void flightplanStatementChanged(IFlightplanRelatedObject statement) {}

        @Override
        public void flightplanStatementStructureChanged(IFlightplanRelatedObject statement) {}

        @Override
        public void flightplanStatementAdded(IFlightplanRelatedObject statement) {}

        @Override
        public void flightplanStatementRemove(int i, IFlightplanRelatedObject statement) {}

        VectorNonEqual<IFlightplanStatement> getElements() {
            return elements;
        }

        protected void updateParent(IFlightplanStatement statement) {
            statement.setParent(PicArea.this);
        }
    }

    public double getRestrictionEgmOffset() {
        if (getCenter() != null) {
            return egmModel.getEGM96Offset(getCenter());
        }

        return 0;
    }

}
