/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.flightplan;

import static eu.mavinci.core.flightplan.CPicArea.FacadeScanningSide.left;

import com.intel.missioncontrol.hardware.IPlatformDescription;
import com.intel.missioncontrol.helper.Ensure;
import com.intel.missioncontrol.map.elevation.IEgmModel;
import com.intel.missioncontrol.map.elevation.IElevationModel;
import com.intel.missioncontrol.measure.Unit;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import eu.mavinci.core.flightplan.visitors.ContainsTypeVisitor;
import eu.mavinci.core.flightplan.visitors.IFlightplanVisitor;
import eu.mavinci.core.helper.MinMaxPair;
import eu.mavinci.core.helper.Pair;
import eu.mavinci.core.obfuscation.IKeepAll;
import eu.mavinci.desktop.gui.doublepanel.calculator.AltitudeGsdCalculator;
import eu.mavinci.desktop.helper.MathHelper;
import eu.mavinci.flightplan.Flightplan;
import eu.mavinci.flightplan.Point;
import eu.mavinci.flightplan.ReferencePoint;
import eu.mavinci.flightplan.WindmillData;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;

public abstract class CPicArea extends AReentryContainer implements IMuteable, IRecalculateable {

    private final IElevationModel elevationModel =
        DependencyInjector.getInstance().getInstanceOf(IElevationModel.class);
    private final IEgmModel egmModel = DependencyInjector.getInstance().getInstanceOf(IEgmModel.class);

    public static final double DEF_GSD = 0.001;
    public static final double MIN_GSD = 0.0002;
    public static final double MAX_GSD = 0.5;

    public static final double DEF_OVERLAP_IN_FLIGHT = 85;
    public static final double DEF_OVERLAP_IN_FLIGHT_MIN = 75;
    public static final double DEF_OVERLAP_PARALLEL = 65;

    public static final int MIN_CORRIDOR_WIDTH_METER = 1;
    public static final int MAX_CORRIDOR_WIDTH_METER = 2000;
    public static final int DEF_CORRIDOR_WIDTH_METER = 100;

    public static final double MIN_CROP_HEIGHT_METER = 0;
    public static final double MAX_CROP_HEIGHT_METER = 1000;

    public static final double MIN_OBJ_DISTANCE_METER = 1;
    public static final double MAX_OBJ_DISTANCE_METER = 1000;

    public static final int MIN_CORRIDOR_MIN_LINES = 1;
    public static final int MAX_CORRIDOR_MIN_LINES = 100;

    public static final int CIRCLE_MIN = 1;
    public static final int CIRCLE_MAX = 1000;

    private static final double DELTA = 1;
    public static final int DIST_MIN_DEFAULT = 1000;
    protected CPicAreaCorners corners;

    // distance to the object
    protected double alt = -1;
    // minimum distance to the object that waypoints that computed points have
    protected double distMinComputed = DIST_MIN_DEFAULT;

    protected double corridorWidthInMeter = DEF_CORRIDOR_WIDTH_METER;

    protected PlanType planType = PlanType.POLYGON;

    protected double cropHeightMin; // some magic numbers --- should be 0 by default ---TODO change in the template
    protected double cropHeightMax; // some magic numbers

    protected double minObjectDistance = 5;

    protected double maxObjectDistance = 1000;

    protected double minGroundDistance = 10;

    protected boolean addCeiling = true;

    // moved here from CPhotoSettings means the direction of the flight lines
    protected double yaw = 0; // in grad

    protected double maxYawRollChange = 15;
    protected double maxPitchChange = 45;

    protected final AltitudeGsdCalculator altitudeGsdCalculator = new AltitudeGsdCalculator();

    protected int corridorMinLines = 3;

    protected double gsd = DEF_GSD; // in m
    protected double overlapInFlight = DEF_OVERLAP_IN_FLIGHT; // in %
    protected double overlapInFlightMin = DEF_OVERLAP_IN_FLIGHT_MIN; // in %
    protected double overlapParallel = DEF_OVERLAP_PARALLEL; // in %

    protected String name = ""; // name of this AOI // not stored inside templates

    protected boolean wasRecalconce;

    // all parameters related to 3D objects
    protected String modelFilePath = ""; // path to a OBJ file for copter3D planning

    public abstract LatLon getCenterShifted();

    public abstract void updateModelReferencePoint();

    public double getRestrictionHeightAboveWgs84(
            LatLon point, double originalHeight, RestrictedAreaHeightReferenceTypes heightRef) {
        if (point != null) {
            double topHeight;

            if (heightRef == RestrictedAreaHeightReferenceTypes.ABOVE_GROUNDLEVEL) {
                double groundHeight = elevationModel.getElevationAsGoodAsPossible(point);
                topHeight = originalHeight + groundHeight;
            } else {
                topHeight = originalHeight - egmModel.getEGM96Offset(point);
            }

            return topHeight;
        }

        return 0;
    }

    public enum ModelSourceTypes implements IKeepAll {
        MODEL_FILE,
        TERRAIN
    }

    protected ModelSourceTypes modelSource = ModelSourceTypes.MODEL_FILE;
    protected double modelScale = 1;
    protected ReferencePoint modelReferencePoint = new ReferencePoint(this);

    public enum ModelAxisAlignment implements IKeepAll {
        MIN,
        CENTER,
        MAX,
        UNCHANGED
    }

    protected ModelAxisAlignment modelAxisAlignmentX = ModelAxisAlignment.UNCHANGED;
    protected ModelAxisAlignment modelAxisAlignmentY = ModelAxisAlignment.UNCHANGED;
    protected ModelAxisAlignment modelAxisAlignmentZ = ModelAxisAlignment.UNCHANGED;

    public enum ModelAxis implements IKeepAll {
        Xminus(-1, 0),
        Yminus(-1, 1),
        Zminus(-1, 2),
        Xplus(1, 0),
        Yplus(1, 1),
        Zplus(1, 2);

        int sign;
        int axis;

        ModelAxis(int sign, int axis) {
            this.sign = sign;
            this.axis = axis;
        }

        public int getSign() {
            return sign;
        }

        public int getAxis() {
            return axis;
        }
    }

    protected double modelAxisOffsetX = 0;
    protected double modelAxisOffsetY = 0;
    protected double modelAxisOffsetZ = 0;

    protected Vector<Pair<ModelAxis, ModelAxis>> modelAxisTransformations = new Vector<>();

    public enum FacadeScanningSide {
        left,
        right
    };

    protected FacadeScanningSide facadeScanningSide = left;
    protected double objectHeight = 50;

    protected boolean enableCropHeightMin = true;
    protected boolean enableCropHeightMax = true;

    protected boolean circleLeftTrueRightFalse; // unknown
    protected int circleCount;

    public enum ScanDirectionsTypes implements IKeepAll {
        towardLaning,
        fromStarting,
        cornerXminYmin,
        cornerXminYmax,
        cornerXmaxYmin,
        cornerXmaxYmax,
        left,
        right,
        custom;

        public boolean isAvaliable(PlanType planType) {
            if (!planType.needsHeights()) {
                switch (this) {
                case towardLaning:
                case fromStarting:
                case cornerXminYmin:
                case cornerXminYmax:
                case cornerXmaxYmin:
                case cornerXmaxYmax:
                    return true;
                default:
                    return false;
                }
            } else if (planType == PlanType.FACADE) {
                switch (this) {
                case left:
                case right:
                case towardLaning:
                case fromStarting:
                    return true;
                default:
                    return false;
                }
            } else if (planType == PlanType.TOWER) {
                switch (this) {
                case towardLaning:
                case fromStarting:
                case custom:
                    return true;
                default:
                    return false;
                }
            } else {
                switch (this) {
                case towardLaning:
                case fromStarting:
                    return true;
                default:
                    return false;
                }
            }
        }
    }

    protected ScanDirectionsTypes scanDirection = ScanDirectionsTypes.fromStarting;

    public enum StartCaptureTypes implements IKeepAll {
        endOfLine,
        insideLine
    }

    protected StartCaptureTypes startCapture = StartCaptureTypes.endOfLine;

    public enum JumpPatternTypes {
        lineByLine,
        interleaving
    }

    protected JumpPatternTypes jumpPattern = JumpPatternTypes.interleaving;

    public enum VerticalScanPatternTypes {
        leftRight,
        upDown,
        segmentWiseLeftRight
    };

    protected VerticalScanPatternTypes verticalScanPattern = VerticalScanPatternTypes.upDown;

    public enum StartCaptureVerticallyTypes implements IKeepAll {
        up,
        down
    }

    protected StartCaptureVerticallyTypes startCaptureVertically = StartCaptureVerticallyTypes.down;

    protected boolean onlySingleDirection = false;

    protected boolean cameraTiltToggleEnable = false;
    protected double cameraTiltToggleDegrees = 5;
    protected double cameraPitchOffsetDegrees = 0;

    protected boolean cameraRollToggleEnable = false;
    protected double cameraRollToggleDegrees = 5;
    protected double cameraRollOffsetDegrees = 0;

    public enum RestrictedAreaHeightReferenceTypes implements IKeepAll {
        ABOVE_SEALEVEL,
        ABOVE_GROUNDLEVEL;
    }

    public static final double RESTRICTION_MIN = 0;
    public static final double RESTRICTION_MAX = 10000;

    protected double restrictionFloor = 0;
    protected boolean restrictionFloorEnabled = true;
    protected RestrictedAreaHeightReferenceTypes restrictionFloorRef =
        RestrictedAreaHeightReferenceTypes.ABOVE_SEALEVEL;
    protected double restrictionCeiling = 100;
    protected double restrictionEgmOffset;
    protected boolean restrictionCeilingEnabled = true;
    protected RestrictedAreaHeightReferenceTypes restrictionCeilingRef =
        RestrictedAreaHeightReferenceTypes.ABOVE_SEALEVEL;

    protected double pitchOffsetLineBegin = 0;

    protected boolean isSync = false;

    public WindmillData windmill = new WindmillData(); // null;

    protected CPicArea(IFlightplanContainer parent) {
        super(parent);
        corners = FlightplanFactory.getFactory().newPicAreaCorners(this);
        altitudeGsdCalculator.setFlightPlan(getFlightplan());
    }

    protected CPicArea(int id, IFlightplanContainer parent) {
        super(id, parent);
        corners = FlightplanFactory.getFactory().newPicAreaCorners(this);
        altitudeGsdCalculator.setFlightPlan(getFlightplan());
    }

    protected CPicArea(
            int id,
            IFlightplanContainer parent,
            double gsd,
            double overlapInFlight,
            double overlapInFlightMin,
            double overlapParallel) {
        this(id, parent);
        this.gsd = gsd;
        this.overlapInFlight = overlapInFlight;
        this.overlapParallel = overlapParallel;
        this.overlapInFlightMin = overlapInFlightMin;
        altitudeGsdCalculator.setGsd(gsd);
    }

    protected CPicArea(
            IFlightplanContainer parent,
            double gsd,
            double overlapInFlight,
            double overlapInFlightMin,
            double overlapParallel) {
        this(parent);
        this.gsd = gsd;
        this.overlapInFlight = overlapInFlight;
        this.overlapParallel = overlapParallel;
        this.overlapInFlightMin = overlapInFlightMin;
        altitudeGsdCalculator.setGsd(gsd);
    }

    public AltitudeGsdCalculator getAltitudeGsdCalculator() {
        return altitudeGsdCalculator;
    }

    public double getMaxPitchChange() {
        return maxPitchChange;
    }

    public double getMaxYawRollChange() {
        return maxYawRollChange;
    }

    public boolean setMaxPitchChange(double maxPitchChange) {
        maxPitchChange = MathHelper.intoRange(maxPitchChange, 5, 360);
        if (this.maxPitchChange == maxPitchChange) {
            return false;
        }

        this.maxPitchChange = maxPitchChange;
        flightplanStatementChanged(this);
        return true;
    }

    public boolean setMaxYawRollChange(double maxYawRollChange) {
        maxYawRollChange = MathHelper.intoRange(maxYawRollChange, 5, 360);
        if (this.maxYawRollChange == maxYawRollChange) {
            return false;
        }

        this.maxYawRollChange = maxYawRollChange;
        flightplanStatementChanged(this);
        return true;
    }

    public double getMinObjectDistance() {
        return minObjectDistance;
    }

    public boolean setMinObjectDistance(double minObjectDistance) {
        minObjectDistance = MathHelper.intoRange(minObjectDistance, MIN_OBJ_DISTANCE_METER, MAX_OBJ_DISTANCE_METER);
        if (this.minObjectDistance == minObjectDistance) {
            return false;
        }

        if (maxObjectDistance < minObjectDistance) {
            maxObjectDistance = minObjectDistance;
        }

        this.minObjectDistance = minObjectDistance;
        flightplanStatementChanged(this);
        return true;
    }

    public double getMaxObjectDistance() {
        return maxObjectDistance;
    }

    public boolean setMaxObjectDistance(double maxObjectDistance) {
        maxObjectDistance = MathHelper.intoRange(maxObjectDistance, MIN_OBJ_DISTANCE_METER, MAX_OBJ_DISTANCE_METER);
        if (this.maxObjectDistance == maxObjectDistance) {
            return false;
        }

        if (minObjectDistance > maxObjectDistance) {
            minObjectDistance = maxObjectDistance;
        }

        this.maxObjectDistance = maxObjectDistance;
        flightplanStatementChanged(this);
        return true;
    }

    public double getMinGroundDistance() {
        return minGroundDistance;
    }

    public boolean setMinGroundDistance(double minGroundDistance) {
        if (this.minGroundDistance == minGroundDistance) {
            return false;
        }

        if (minGroundDistance > objectHeight) {
            minGroundDistance = objectHeight;
        }

        IPlatformDescription plat = getPlatformDescription();
        Ensure.notNull(plat, "Platform Description");
        double minGround = plat.getMinGroundDistance().convertTo(Unit.METER).getValue().doubleValue();

        if (minGroundDistance < minGround) {
            minGroundDistance = minGround;
        }

        this.minGroundDistance = minGroundDistance;
        flightplanStatementChanged(this);
        return true;
    }

    public double getYaw() {
        if (planType == PlanType.WINDMILL) {
            yaw = windmill.getHubYaw();
        }
        // normalize -0.0==+0.0 for primitives but NOT for Double or Float objects...
        if (yaw == 0.0d) {
            yaw = 0.0d; // workaround for existing problems!
        }

        return yaw;
    }

    public boolean setYaw(double yaw) {
        while (yaw < 0) {
            yaw += 360;
        }

        while (yaw >= 360) {
            yaw -= 360;
        }
        // normalize -0.0==+0.0 for primitives but NOT for Double or Float objects...
        if (yaw == 0.0d) {
            yaw = 0.0d;
        }

        if (planType == PlanType.WINDMILL) {
            this.yaw = windmill.getHubYaw();
        }

        if (this.yaw == yaw) {
            return false;
        }

        this.yaw = yaw;
        windmill.setHubYaw(yaw);
        flightplanStatementChanged(this);
        return true;
    }

    // TODO should be used instead of one in the flightplan -- each description
    // corresponds to the type of PicArea
    // is selected once when picarea is created

    public double getCropHeightMin() {
        return cropHeightMin;
    }

    public boolean setCropHeightMin(double crop) {
        crop = MathHelper.intoRange(crop, MIN_CROP_HEIGHT_METER, MAX_CROP_HEIGHT_METER);
        if (crop > cropHeightMax) {
            crop = cropHeightMax;
        }

        this.cropHeightMin = crop;
        flightplanStatementChanged(this);
        return true;
    }

    public double getCropHeightMax() {
        if (planType == PlanType.WINDMILL) {
            cropHeightMax = windmill.getTowerHeight();
        }

        return cropHeightMax;
    }

    public boolean setCropHeightMax(double cropHeightMax) {
        if (cropHeightMin > cropHeightMax) {
            cropHeightMin = cropHeightMax;
        }

        cropHeightMax = MathHelper.intoRange(cropHeightMax, MIN_CROP_HEIGHT_METER, MAX_CROP_HEIGHT_METER);

        if (planType == PlanType.WINDMILL) {
            this.cropHeightMax = windmill.getTowerHeight();
        }

        if (this.cropHeightMax == cropHeightMax) {
            return false;
        }

        if (cropHeightMax > objectHeight) {
            objectHeight = cropHeightMax;
        }

        if (objectHeight - cropHeightMax > DELTA) {
            setAddCeiling(false);
        }

        this.cropHeightMax = cropHeightMax;
        windmill.setTowerHeight(cropHeightMax);
        flightplanStatementChanged(this);
        return true;
    }

    public boolean isAddCeiling() {
        return addCeiling;
    }

    public boolean setAddCeiling(boolean addCealing) {
        if (this.addCeiling == addCealing) {
            return false;
        }

        this.addCeiling = addCealing;
        flightplanStatementChanged(this);
        return true;
    }

    public PlanType getPlanType() {
        return planType;
    }

    public boolean setPlanType(PlanType planType) {
        if (this.planType == planType) {
            return false;
        }

        CFlightplan cFlightplan = getFlightplan();
        this.planType = planType;
        if (!planType.canDoMultiPlans() && cFlightplan != null && cFlightplan.getPhotoSettings().isMultiFP()) {
            cFlightplan.getPhotoSettings().setMultiFP(false);
        }

        flightplanStatementChanged(this);
        return true;
    }

    public double getCorridorWidthInMeter() {
        if (planType == PlanType.WINDMILL) {
            corridorWidthInMeter = windmill.getTowerRadius();
        }

        return corridorWidthInMeter;
    }

    public boolean setCorridorWidthInMeter(double corridorWidthInMeter) {
        if (planType == PlanType.WINDMILL) {
            this.corridorWidthInMeter = windmill.getTowerRadius();
        }

        if (this.corridorWidthInMeter == corridorWidthInMeter) {
            return false;
        }

        if (corridorWidthInMeter < MIN_CORRIDOR_WIDTH_METER) {
            corridorWidthInMeter = MIN_CORRIDOR_WIDTH_METER;
        } else if (corridorWidthInMeter > MAX_CORRIDOR_WIDTH_METER) {
            corridorWidthInMeter = MAX_CORRIDOR_WIDTH_METER;
        }

        this.corridorWidthInMeter = corridorWidthInMeter;
        windmill.setTowerRadius(corridorWidthInMeter);
        flightplanStatementChanged(this);
        return true;
    }

    public int getCorridorMinLines() {
        if (planType == PlanType.SPIRAL) {
            return Math.max(corridorMinLines, 3);
        }

        return corridorMinLines;
    }

    public boolean setCorridorMinLines(int corridorMinLines) {
        corridorMinLines = MathHelper.intoRange(corridorMinLines, MIN_CORRIDOR_MIN_LINES, MAX_CORRIDOR_MIN_LINES);
        if (this.corridorMinLines == corridorMinLines) {
            return false;
        }

        this.corridorMinLines = corridorMinLines;
        flightplanStatementChanged(this);
        return true;
    }

    //
    // windmill interface functions
    //
    // the following connections are already made:
    // windmill.towerHeight -- this.cropHeightMax
    // windmill.tower.Radius -- this.corridorWidthInMeter
    // windmill.distanceFromBlade -- this.alt
    // windmill.hubYaw -- this.yaw
    //

    public double getWindmillHubDiameter() {
        return windmill.hubRadius * 2;
    }

    public boolean setWindmillHubDiameter(double hubDiameter) {
        if (windmill.setHubRadius(hubDiameter / 2)) {
            flightplanStatementChanged(this);
            return true;
        }

        return false;
    }

    public double getWindmillHubLength() {
        return windmill.hubHalfLength * 2;
    }

    public boolean setWindmillHubLength(double hubLength) {
        if (windmill.setHubHalfLength(hubLength / 2)) {
            flightplanStatementChanged(this);
            return true;
        }

        return false;
    }

    public int getWindmillNumberOfBlades() {
        return windmill.numberOfBlades;
    }

    public boolean setWindmillNumberOfBlades(int numberOfBlades) {
        if (windmill.setNumberOfBlades(numberOfBlades)) {
            flightplanStatementChanged(this);
            return true;
        }

        return false;
    }

    public double getWindmillBladeLength() {
        return windmill.bladeLength;
    }

    public boolean setWindmillBladeLength(double bladeLength) {
        if (windmill.setBladeLength(bladeLength)) {
            flightplanStatementChanged(this);
            return true;
        }

        return false;
    }

    public double getWindmillBladeDiameter() {
        return windmill.bladeRadius * 2;
    }

    public boolean setWindmillBladeDiameter(double bladeDiameter) {
        if (windmill.setBladeRadius(bladeDiameter / 2)) {
            flightplanStatementChanged(this);
            return true;
        }

        return false;
    }

    public double getWindmillBladeThinRadius() {
        return windmill.bladeThinRadius;
    }

    public boolean setWindmillBladeThinRadius(double bladeThinRadius) {
        if (windmill.setBladeThinRadius(bladeThinRadius)) {
            flightplanStatementChanged(this);
            return true;
        }

        return false;
    }

    public double getWindmillBladePitch() {
        return windmill.bladePitch;
    }

    public boolean setWindmillBladePitch(double bladePitch) {
        if (windmill.setBladePitch(bladePitch)) {
            flightplanStatementChanged(this);
            return true;
        }

        return false;
    }

    public double getWindmillBladeStartRotation() {
        return windmill.bladeStartRotation;
    }

    public boolean setWindmillBladeStartRotation(double bladeStartRotation) {
        if (windmill.setBladeStartRotation(bladeStartRotation)) {
            flightplanStatementChanged(this);
            return true;
        }

        return false;
    }

    public double getWindmillBladeCoverLength() {
        return (windmill.bladeLength - windmill.bladeStartLength);
    }

    public boolean setWindmillBladeCoverLength(double bladeCoverLength) {
        if (windmill.setBladeStartLength(windmill.bladeLength - bladeCoverLength)) {
            flightplanStatementChanged(this);
            return true;
        }

        return false;
    }

    public CPicAreaCorners getCorners() {
        return corners;
    }

    public boolean setDefaultsFromMasterPicArea(CPicArea area) {
        boolean changed = false;
        setMute(true);
        try {
            // intentionall we are not calling setName(...) here
            // since this feels more natural to the user

            if (setGsd(area.getGsd())) {
                changed = true;
            }

            if (setOverlapInFlight(area.getOverlapInFlight())) {
                changed = true;
            }

            if (setOverlapInFlightMin(area.getOverlapInFlightMin())) {
                changed = true;
            }

            if (setOverlapParallel(area.getOverlapParallel())) {
                changed = true;
            }

            if (setCorridorMinLines(area.getCorridorMinLines())) {
                changed = true;
            }

            if (setCorridorWidthInMeter(area.getCorridorWidthInMeter())) {
                changed = true;
            }

            if (setPlanType(area.getPlanType())) {
                changed = true;
            }

            if (setCropHeightMax(area.getCropHeightMax())) {
                changed = true;
            }

            if (setCropHeightMin(area.getCropHeightMin())) {
                changed = true;
            }

            if (setMinObjectDistance(area.getMinObjectDistance())) {
                changed = true;
            }

            if (setMaxObjectDistance(area.getMaxObjectDistance())) {
                changed = true;
            }

            if (setMinGroundDistance(area.getMinGroundDistance())) {
                changed = true;
            }

            if (setAddCeiling(area.isAddCeiling())) {
                changed = true;
            }

            if (setAlt(area.getAlt())) {
                changed = true;
            }

            if (setObjectHeight(area.getObjectHeight())) {
                changed = true;
            }

            if (setYaw(area.getYaw())) {
                changed = true;
            }

            if (setFacadeScanningSide(area.getFacadeScanningSide())) {
                changed = true;
            }

            if (setModelAxisTransformations(area.getModelAxisTransformations())) {
                changed = true;
            }

            if (setModelFilePath(area.getModelFilePath())) {
                changed = true;
            }

            if (getModelReferencePoint().setValues(area.getModelReferencePoint())) {
                changed = true;
            }

            if (setModelScale(area.getModelScale())) {
                changed = true;
            }

            if (setModelAxisAlignmentX(area.getModelAxisAlignmentX())) {
                changed = true;
            }

            if (setModelAxisAlignmentY(area.getModelAxisAlignmentY())) {
                changed = true;
            }

            if (setModelAxisAlignmentZ(area.getModelAxisAlignmentZ())) {
                changed = true;
            }

            if (setModelAxisOffsetX(area.getModelAxisOffsetX())) {
                changed = true;
            }

            if (setModelAxisOffsetY(area.getModelAxisOffsetY())) {
                changed = true;
            }

            if (setModelAxisOffsetZ(area.getModelAxisOffsetZ())) {
                changed = true;
            }

            if (setModelSource(area.getModelSource())) {
                changed = true;
            }

            if (setScanDirection(area.getScanDirection())) {
                changed = true;
            }

            if (setStartCapture(area.getStartCapture())) {
                changed = true;
            }

            if (setStartCaptureVertically(area.getStartCaptureVertically())) {
                changed = true;
            }

            if (setVerticalScanPattern(area.getVerticalScanPattern())) {
                changed = true;
            }

            if (setRestrictionCeiling(area.getRestrictionCeiling())) {
                changed = true;
            }

            if (setRestrictionFloor(area.getRestrictionFloor())) {
                changed = true;
            }

            if (setRestrictionCeilingEnabled(area.isRestrictionCeilingEnabled())) {
                changed = true;
            }

            if (setRestrictionCeilingRef(area.getRestrictionCeilingRef())) {
                changed = true;
            }

            if (setRestrictionFloorEnabled(area.isRestrictionFloorEnabled())) {
                changed = true;
            }

            if (setRestrictionFloorRef(area.getRestrictionFloorRef())) {
                changed = true;
            }

            if (setPitchOffsetLineBegin(area.getPitchOffsetLineBegin())) {
                changed = true;
            }

            if (setOnlySingleDirection(area.isOnlySingleDirection())) {
                changed = true;
            }

            if (setCameraPitchOffsetDegrees(area.getCameraPitchOffsetDegrees())) {
                changed = true;
            }

            if (setCameraRollOffsetDegrees(area.getCameraRollOffsetDegrees())) {
                changed = true;
            }

            if (setCameraRollToggleDegrees(area.getCameraRollToggleDegrees())) {
                changed = true;
            }

            if (setCameraRollToggleEnable(area.isCameraRollToggleEnable())) {
                changed = true;
            }

            if (setCameraTiltToggleDegrees(area.getCameraTiltToggleDegrees())) {
                changed = true;
            }

            if (setCameraTiltToggleEnable(area.isCameraTiltToggleEnable())) {
                changed = true;
            }

            if (setCircleCount(area.getCircleCount())) {
                changed = true;
            }

            if (setCircleLeftTrueRightFalse(area.isCircleLeftTrueRightFalse())) {
                changed = true;
            }

            if (setMaxYawRollChange(area.getMaxYawRollChange())) {
                changed = true;
            }

            if (setMaxPitchChange(area.getMaxPitchChange())) {
                changed = true;
            }

            if (setWindmillBladeCoverLength(area.getWindmillBladeCoverLength())) {
                changed = true;
            }

            if (setWindmillBladeDiameter(area.getWindmillBladeDiameter())) {
                changed = true;
            }

            if (setWindmillBladeLength(area.getWindmillBladeLength())) {
                changed = true;
            }

            if (setWindmillBladePitch(area.getWindmillBladePitch())) {
                changed = true;
            }

            if (setWindmillBladeStartRotation(area.getWindmillBladeStartRotation())) {
                changed = true;
            }

            if (setWindmillBladeThinRadius(area.getWindmillBladeThinRadius())) {
                changed = true;
            }

            if (setWindmillHubDiameter(area.getWindmillHubDiameter())) {
                changed = true;
            }

            if (setWindmillHubLength(area.getWindmillHubLength())) {
                changed = true;
            }

            if (setWindmillNumberOfBlades(area.getWindmillNumberOfBlades())) {
                changed = true;
            }

            if (setEnableCropHeightMax(area.isEnableCropHeightMax())) {
                changed = true;
            }

            if (setEnableCropHeightMin(area.isEnableCropHeightMin())) {
                changed = true;
            }

            if (setJumpPattern(area.getJumpPattern())) {
                changed = true;
            }

        } finally {
            setSilentUnmute();
        }

        if (changed) {
            flightplanStatementChanged(this);
        }

        return changed;
    }

    public double getGsd() {
        return gsd;
    }

    public boolean setGsd(double gsd) {
        // CDebug.printStackTrace("new GSD:"+gsd);
        if (gsd >= MAX_GSD) {
            gsd = MAX_GSD;
        } else if (gsd <= MIN_GSD) {
            gsd = MIN_GSD;
        }

        CFlightplan flightplan = getFlightplan();
        if (this.gsd != gsd && flightplan != null) {
            this.gsd = gsd;
            altitudeGsdCalculator.setGsd(gsd);
            this.alt = altitudeGsdCalculator.getAlt();
            flightplanStatementChanged(this);
            return true;
        }

        return false;
    }

    public double getOverlapInFlight() {
        return overlapInFlight;
    }

    public boolean setOverlapInFlight(double overlapInFlight) {
        overlapInFlight = MathHelper.intoRange(overlapInFlight, 0, 100);
        if (this.overlapInFlight != overlapInFlight) {
            this.overlapInFlight = overlapInFlight;
            flightplanStatementChanged(this);
            return true;
        }

        return false;
    }

    public double getOverlapInFlightMin() {
        return overlapInFlightMin;
    }

    public boolean setOverlapInFlightMin(double overlapInFlightMin) {
        overlapInFlightMin = MathHelper.intoRange(overlapInFlightMin, 0, 100);
        if (this.overlapInFlightMin != overlapInFlightMin) {
            this.overlapInFlightMin = overlapInFlightMin;
            flightplanStatementChanged(this);
            return true;
        }

        return false;
    }

    public double getOverlapParallel() {
        return overlapParallel;
    }

    public boolean setOverlapParallel(double overlapParallel) {
        overlapParallel = MathHelper.intoRange(overlapParallel, 0, 100);
        if (this.overlapParallel != overlapParallel) {
            this.overlapParallel = overlapParallel;
            flightplanStatementChanged(this);
            return true;
        }

        return false;
    }

    public boolean isAddableToFlightplanContainer(IFlightplanStatement statement) {
        if (SKIP_IS_ADDABLE_ASSERTION_CHECKS) return true;

        if (sizeOfFlightplanContainer() >= getMaxSize()) {
            return false;
        }

        if (statement instanceof CPicAreaCorners) {
            return false;
        }

        if (statement instanceof Point) {
            return false;
        }

        CFlightplan cFlightplan = getFlightplan();

        ContainsTypeVisitor vis = new ContainsTypeVisitor(CPicArea.class);
        vis.startVisit(statement);
        return !vis.found;
    }

    @Override
    public int getMaxSize() {
        if (SKIP_IS_ADDABLE_ASSERTION_CHECKS) return super.getMaxSize();
        // why does this matter if we have rows or not?
        return super.getMaxSize();
    }

    @Override
    public boolean isAddableToFlightplanContainer(Class<? extends IFlightplanStatement> cls) {
        if (SKIP_IS_ADDABLE_ASSERTION_CHECKS) return true;

        if (!super.isAddableToFlightplanContainer(cls)) {
            return false;
        }

        return !CFlightplan.class.isAssignableFrom(cls)
            && !CPicArea.class.isAssignableFrom(cls);
    }

    public boolean setAlt(double alt) {
        if (planType == PlanType.WINDMILL) {
            this.alt = windmill.getDistanceFromBlade();
        }
        // lets assume that the alt value that was set is already valid in respect of the resulting GSD
        CFlightplan flightPlan = getFlightplan();
        if (this.alt != alt && flightPlan != null) {
            this.alt = alt;
            altitudeGsdCalculator.setAlt(alt);
            this.gsd = altitudeGsdCalculator.getGsd();
            windmill.setDistanceFromBlade(alt);
            flightplanStatementChanged(this);
            return true;
        }

        return false;
    }

    public double getAlt() {
        if (planType == PlanType.WINDMILL) {
            alt = windmill.getDistanceFromBlade();
        }

        if (alt > 0) {
            return alt;
        } else {
            alt = altitudeGsdCalculator.getAlt();
            if (alt > 0) {
                return alt;
            }
        }

        for (int i = 0; i != sizeOfFlightplanContainer(); i++) {
            IFlightplanStatement o = getFromFlightplanContainer(i);
            if (o instanceof IFlightplanPositionReferenced) {
                IFlightplanPositionReferenced posRef = (IFlightplanPositionReferenced)o;
                return posRef.getAltInMAboveFPRefPoint();
            }
        }

        return 0;
    }

    /* this method was used in a way assuming this height is a save travel height... but its not, just assume a tower from which only the lower part is mapped..
    public double getMaxAlt() {
        double maxAlt = Double.NEGATIVE_INFINITY;

        for (int i = 0; i != sizeOfFlightplanContainer(); i++) {
            IFlightplanStatement o = getFromFlightplanContainer(i);
            if (o instanceof IFlightplanPositionReferenced) {
                IFlightplanPositionReferenced posRef = (IFlightplanPositionReferenced)o;
                maxAlt = Math.max(maxAlt, posRef.getAltInMAboveFPRefPoint());
            }
        }

        return maxAlt;
    }*/

    public double getDistMinComputed() {
        return distMinComputed;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (o == null) {
            return false;
        }

        if (o instanceof CPicArea) {
            CPicArea pa = (CPicArea)o;
            // System.out.println("picArea-Equals elements:"+elements.equals(pa.elements));
            // System.out.println("picArea-Equals corners:"+corners.equals(pa.corners));
            return elements.equals(pa.elements)
                && corners.equals(pa.corners)
                && pa.gsd == gsd
                && pa.overlapInFlight == overlapInFlight
                && pa.overlapInFlightMin == overlapInFlightMin
                && pa.overlapParallel == overlapParallel
                && pa.corridorMinLines == corridorMinLines
                && pa.corridorWidthInMeter == corridorWidthInMeter
                && pa.planType == planType
                && pa.addCeiling == addCeiling
                && pa.cropHeightMax == cropHeightMax
                && pa.cropHeightMin == cropHeightMin
                && pa.minObjectDistance == minObjectDistance
                && pa.maxObjectDistance == maxObjectDistance
                && pa.minGroundDistance == minGroundDistance
                && pa.name.equals(name)
                && pa.modelFilePath.equals(modelFilePath)
                && pa.modelSource == modelSource
                && pa.modelAxisAlignmentX == modelAxisAlignmentX
                && pa.modelAxisOffsetX == modelAxisOffsetX
                && pa.modelAxisAlignmentY == modelAxisAlignmentY
                && pa.modelAxisOffsetY == modelAxisOffsetY
                && pa.modelAxisAlignmentZ == modelAxisAlignmentZ
                && pa.modelAxisOffsetZ == modelAxisOffsetZ
                && pa.modelScale == modelScale
                && pa.modelReferencePoint.equals(modelReferencePoint)
                && pa.modelAxisTransformations.equals(modelAxisTransformations)
                && pa.facadeScanningSide == facadeScanningSide
                && pa.objectHeight == objectHeight
                && pa.enableCropHeightMax == enableCropHeightMax
                && pa.enableCropHeightMin == enableCropHeightMin
                && pa.circleLeftTrueRightFalse == circleLeftTrueRightFalse
                && pa.circleCount == circleCount
                && pa.scanDirection == scanDirection
                && pa.startCapture == startCapture
                && pa.jumpPattern == jumpPattern
                && pa.verticalScanPattern == verticalScanPattern
                && pa.startCaptureVertically == startCaptureVertically
                && pa.onlySingleDirection == onlySingleDirection
                && pa.cameraTiltToggleEnable == cameraTiltToggleEnable
                && pa.cameraTiltToggleDegrees == cameraTiltToggleDegrees
                && pa.cameraPitchOffsetDegrees == cameraPitchOffsetDegrees
                && pa.cameraRollToggleEnable == cameraRollToggleEnable
                && pa.cameraRollToggleDegrees == cameraRollToggleDegrees
                && pa.cameraRollOffsetDegrees == cameraRollOffsetDegrees
                && pa.maxYawRollChange == maxYawRollChange
                && pa.maxPitchChange == maxPitchChange
                && pa.restrictionCeilingEnabled == restrictionCeilingEnabled
                && pa.restrictionCeiling == restrictionCeiling
                && pa.restrictionCeilingRef == restrictionCeilingRef
                && pa.restrictionFloorEnabled == restrictionFloorEnabled
                && pa.restrictionFloor == restrictionFloor
                && pa.restrictionFloorRef == restrictionFloorRef
                && pa.pitchOffsetLineBegin == pitchOffsetLineBegin
                && pa.windmill.equals(windmill);
        }

        return false;
    }

    public boolean applyVisitorPost(IFlightplanVisitor visitor, boolean skipIgnoredPaths) {
        if (corners.applyFpVisitor(visitor, skipIgnoredPaths)) {
            return true;
        }

        return super.applyVisitorPost(visitor, skipIgnoredPaths);
    }

    public boolean applyVisitorFlatPost(IFlightplanVisitor visitor) {
        if (visitor.visit(corners)) {
            return true;
        }

        return super.applyVisitorFlatPost(visitor);
    }

    public void setParent(IFlightplanContainer container) {
        super.setParent(container);
        reinit();

        if (getFlightplan() != null) {
            altitudeGsdCalculator.setFlightPlan(getFlightplan());
            altitudeGsdCalculator.setGsd(gsd);
        }
    }

    public void reinit() {
        setupTransform();
        resetCaches();
    }

    public void setupTransform() {}

    public void resetCaches() {}

    public void computeFlightlinesWithLastPlaneSilent() {}

    private boolean mute;

    public void setMute(boolean mute) {
        // if (mute==false) Debug.printStackTrace(mute);
        getCorners().setMute(mute);
        if (mute == this.mute) {
            return;
        }

        this.mute = mute;
        if (!mute) {
            flightplanStatementChanged(this);
        }
    }

    /*
    public void unmuteWithSync() {
        setMute(true);
        resetCaches();
        this.isSync = true;
        setSilentUnmute();
        // dont call this function on this class, because than isSync becomes false again
        super.flightplanStatementStructureChanged(this);
        super.flightplanStatementChanged(this);
        getFlightplan().informValuesChangeLiseners(getFlightplan()); // maybe it was switched FROM or TO TERRAIN
        // getFlightplan().informStructureChangeListeners();
    }*/

    public boolean wasRecalconce() {
        return wasRecalconce;
    }

    public boolean isMute() {
        return mute;
    }

    public void setSilentUnmute() {
        // Debug.printStackTrace("filent to false");
        getCorners().setSilentUnmute();
        this.mute = false;
    }

    protected IPlatformDescription getPlatformDescription() {
        CFlightplan cFlightplan = getFlightplan();
        Ensure.notNull(cFlightplan, "cFlightplan");
        return cFlightplan.getHardwareConfiguration().getPlatformDescription();
    }

    public boolean setModelAxisOffsetX(double modelAxisOffsetX) {
        if (this.modelAxisOffsetX == modelAxisOffsetX) return false;
        this.modelAxisOffsetX = modelAxisOffsetX;
        flightplanStatementChanged(this);
        return true;
    }

    public double getModelAxisOffsetX() {
        return modelAxisOffsetX;
    }

    public boolean setModelAxisOffsetY(double modelAxisOffsetY) {
        if (this.modelAxisOffsetY == modelAxisOffsetY) return false;
        this.modelAxisOffsetY = modelAxisOffsetY;
        flightplanStatementChanged(this);
        return true;
    }

    public double getModelAxisOffsetY() {
        return modelAxisOffsetY;
    }

    public boolean setModelAxisOffsetZ(double modelAxisOffsetZ) {
        if (this.modelAxisOffsetZ == modelAxisOffsetZ) return false;
        this.modelAxisOffsetZ = modelAxisOffsetZ;
        flightplanStatementChanged(this);
        return true;
    }

    public double getModelAxisOffsetZ() {
        return modelAxisOffsetZ;
    }

    public void modelAddSwap(ModelAxis first, ModelAxis second) {
        modelAxisTransformations.add(new Pair<>(first, second));
        flightplanStatementChanged(this);
    }

    // protected double maxObstacleHeight;

    public String getName() {
        return name;
    }

    public boolean setName(String name) {
        if (name == null) {
            name = "";
        }

        if (name.equals(this.name)) return false;
        this.name = name;
        flightplanStatementChanged(this);
        return true;
    }

    public String getModelFilePath() {
        return modelFilePath;
    }

    public boolean setModelFilePath(String modelFilePath) {
        if (modelFilePath.equals(this.modelFilePath)) return false;
        this.modelFilePath = modelFilePath;
        flightplanStatementChanged(this);
        return true;
    }

    public ModelSourceTypes getModelSource() {
        return modelSource;
    }

    public boolean setModelSource(ModelSourceTypes modelSource) {
        if (this.modelSource == modelSource) return false;
        this.modelSource = modelSource;
        flightplanStatementChanged(this);
        return true;
    }

    public double getModelScale() {
        return modelScale;
    }

    public boolean setModelScale(double modelScale) {
        modelScale = MathHelper.intoRange(modelScale, 0.001, 1000);
        if (this.modelScale == modelScale) return false;
        this.modelScale = modelScale;
        flightplanStatementChanged(this);
        return true;
    }

    public ReferencePoint getModelReferencePoint() {
        return modelReferencePoint;
    }

    public ModelAxisAlignment getModelAxisAlignmentX() {
        return modelAxisAlignmentX;
    }

    public boolean setModelAxisAlignmentX(ModelAxisAlignment modelAxisAlignmentX) {
        if (this.modelAxisAlignmentX == modelAxisAlignmentX) return false;
        this.modelAxisAlignmentX = modelAxisAlignmentX;
        flightplanStatementChanged(this);
        return true;
    }

    public ModelAxisAlignment getModelAxisAlignmentY() {
        return modelAxisAlignmentY;
    }

    public boolean setModelAxisAlignmentY(ModelAxisAlignment modelAxisAlignmentY) {
        if (this.modelAxisAlignmentY == modelAxisAlignmentY) return false;
        this.modelAxisAlignmentY = modelAxisAlignmentY;
        flightplanStatementChanged(this);
        return true;
    }

    public ModelAxisAlignment getModelAxisAlignmentZ() {
        return modelAxisAlignmentZ;
    }

    public boolean setModelAxisAlignmentZ(ModelAxisAlignment modelAxisAlignmentZ) {
        if (this.modelAxisAlignmentZ == modelAxisAlignmentZ) return false;
        this.modelAxisAlignmentZ = modelAxisAlignmentZ;
        flightplanStatementChanged(this);
        return true;
    }

    public Collection<Pair<ModelAxis, ModelAxis>> getModelAxisTransformations() {
        return new ArrayList<>(modelAxisTransformations);
    }

    public boolean setModelAxisTransformations(
            Collection<? extends Pair<ModelAxis, ModelAxis>> modelAxisTransformations) {
        if (modelAxisTransformations.equals(this.modelAxisTransformations)) return false;
        this.modelAxisTransformations.clear();
        this.modelAxisTransformations.addAll(modelAxisTransformations);
        flightplanStatementChanged(this);
        return true;
    }

    public Matrix getModelAxisTransformationMatrix() {
        Matrix m = Matrix.fromScale(getModelScale());
        for (Pair<ModelAxis, ModelAxis> swap : modelAxisTransformations) {
            Vec4[] axis = new Vec4[] {Vec4.UNIT_X, Vec4.UNIT_Y, Vec4.UNIT_Z};
            Vec4 tmp = axis[swap.first.axis];
            axis[swap.first.axis] = axis[swap.second.axis];
            axis[swap.second.axis] = tmp;
            int sign = swap.first.sign * swap.second.sign;
            if (sign < 0) {
                axis[swap.first.axis] = axis[swap.first.axis].getNegative3();
                axis[swap.second.axis] = axis[swap.second.axis].getNegative3();
            }

            Matrix sm =
                new Matrix(
                    axis[0].x, axis[1].x, axis[2].x, 0.0, axis[0].y, axis[1].y, axis[2].y, 0.0, axis[0].z, axis[1].z,
                    axis[2].z, 0.0, 0.0, 0.0, 0.0, 1.0);
            m = sm.multiply(m);
        }

        return m;
    }

    public FacadeScanningSide getFacadeScanningSide() {
        return facadeScanningSide;
    }

    public boolean setFacadeScanningSide(FacadeScanningSide facadeScanningSide) {
        if (this.facadeScanningSide == facadeScanningSide) return false;
        this.facadeScanningSide = facadeScanningSide;
        flightplanStatementChanged(this);
        return true;
    }

    public double getObjectHeight() {
        return objectHeight;
    }

    public double getObjectHeightRelativeToRefPoint() {
        return objectHeight;
    }

    public boolean setObjectHeight(double objectHeight) {
        objectHeight = MathHelper.intoRange(objectHeight, MIN_CROP_HEIGHT_METER, MAX_CROP_HEIGHT_METER);
        if (this.objectHeight == objectHeight) return false;
        boolean isMute = isMute();
        setMute(true); // muting otherwise recomputation is triggered twice
        if (this.objectHeight - cropHeightMax < DELTA) {
            this.objectHeight = objectHeight;
            setCropHeightMax(objectHeight);
        } else {
            if (objectHeight < cropHeightMax) {
                this.objectHeight = objectHeight;
                setCropHeightMax(objectHeight);
            }
        }

        if (!isMute) {
            setSilentUnmute();
        }

        if (objectHeight < minGroundDistance) {
            // TODO should change here min ground dist ????
        }

        this.objectHeight = objectHeight;
        flightplanStatementChanged(this);
        return true;
    }

    public boolean isEnableCropHeightMin() {
        return enableCropHeightMin;
    }

    public boolean setEnableCropHeightMin(boolean enableCropHeightMin) {
        if (this.enableCropHeightMin == enableCropHeightMin) return false;
        this.enableCropHeightMin = enableCropHeightMin;
        flightplanStatementChanged(this);
        return true;
    }

    public boolean isEnableCropHeightMax() {
        return enableCropHeightMax;
    }

    public boolean setEnableCropHeightMax(boolean enableCropHeightMax) {
        if (this.enableCropHeightMax == enableCropHeightMax) return false;
        this.enableCropHeightMax = enableCropHeightMax;
        flightplanStatementChanged(this);
        return true;
    }

    public boolean isCircleLeftTrueRightFalse() {
        return circleLeftTrueRightFalse;
    }

    public boolean setCircleLeftTrueRightFalse(boolean circleLeftTrueRightFalse) {
        if (this.circleLeftTrueRightFalse == circleLeftTrueRightFalse) return false;
        this.circleLeftTrueRightFalse = circleLeftTrueRightFalse;
        flightplanStatementChanged(this);
        return true;
    }

    public int getCircleCount() {
        return circleCount;
    }

    public boolean setCircleCount(int circleCount) {
        circleCount = MathHelper.intoRange(circleCount, CIRCLE_MIN, CIRCLE_MAX);
        if (this.circleCount == circleCount) return false;
        this.circleCount = circleCount;
        flightplanStatementChanged(this);
        return true;
    }

    public ScanDirectionsTypes getScanDirection() {
        return scanDirection;
    }

    public boolean setScanDirection(ScanDirectionsTypes scanDirection) {
        if (this.scanDirection == scanDirection) return false;
        this.scanDirection = scanDirection;
        if (scanDirection == ScanDirectionsTypes.towardLaning) {
            var fp = getFlightplan();
            Ensure.notNull(fp);
            ((Flightplan)fp).setEnableJumpOverWaypoints(true);
        }

        flightplanStatementChanged(this);
        return true;
    }

    public StartCaptureTypes getStartCapture() {
        return startCapture;
    }

    public boolean setStartCapture(StartCaptureTypes startCapture) {
        if (this.startCapture == startCapture) return false;
        this.startCapture = startCapture;
        flightplanStatementChanged(this);
        return true;
    }

    public JumpPatternTypes getJumpPattern() {
        return jumpPattern;
    }

    public boolean setJumpPattern(JumpPatternTypes jumpPattern) {
        if (this.jumpPattern == jumpPattern) return false;
        this.jumpPattern = jumpPattern;
        flightplanStatementChanged(this);
        return true;
    }

    public VerticalScanPatternTypes getVerticalScanPattern() {
        return verticalScanPattern;
    }

    public boolean setVerticalScanPattern(VerticalScanPatternTypes verticalScanPattern) {
        if (this.verticalScanPattern == verticalScanPattern) return false;
        this.verticalScanPattern = verticalScanPattern;
        flightplanStatementChanged(this);
        return true;
    }

    public StartCaptureVerticallyTypes getStartCaptureVertically() {
        return startCaptureVertically;
    }

    public boolean setStartCaptureVertically(StartCaptureVerticallyTypes startCaptureVertically) {
        if (this.startCaptureVertically == startCaptureVertically) return false;
        this.startCaptureVertically = startCaptureVertically;
        flightplanStatementChanged(this);
        return true;
    }

    public boolean isOnlySingleDirection() {
        return onlySingleDirection;
    }

    public boolean setOnlySingleDirection(boolean onlySingleDirection) {
        if (this.onlySingleDirection == onlySingleDirection) return false;
        this.onlySingleDirection = onlySingleDirection;
        flightplanStatementChanged(this);
        return true;
    }

    public boolean isCameraTiltToggleEnable() {
        return cameraTiltToggleEnable;
    }

    public boolean setCameraTiltToggleEnable(boolean cameraTiltToggleEnable) {
        if (this.cameraTiltToggleEnable == cameraTiltToggleEnable) return false;
        this.cameraTiltToggleEnable = cameraTiltToggleEnable;
        flightplanStatementChanged(this);
        return true;
    }

    public double getCameraTiltToggleDegrees() {
        return cameraTiltToggleDegrees;
    }

    public boolean setCameraTiltToggleDegrees(double cameraTiltToggleDegrees) {
        cameraTiltToggleDegrees = MathHelper.intoRange(cameraTiltToggleDegrees, 0, 90);
        if (this.cameraTiltToggleDegrees == cameraTiltToggleDegrees) return false;
        this.cameraTiltToggleDegrees = cameraTiltToggleDegrees;
        flightplanStatementChanged(this);
        return true;
    }

    public double getCameraPitchOffsetDegrees() {
        return cameraPitchOffsetDegrees;
    }

    public boolean setCameraPitchOffsetDegrees(double cameraPitchOffsetDegrees) {
        cameraPitchOffsetDegrees = MathHelper.intoRange(cameraPitchOffsetDegrees, -180, 180);
        if (this.cameraPitchOffsetDegrees == cameraPitchOffsetDegrees) return false;
        this.cameraPitchOffsetDegrees = cameraPitchOffsetDegrees;
        flightplanStatementChanged(this);
        return true;
    }

    public boolean isCameraRollToggleEnable() {
        return cameraRollToggleEnable;
    }

    public boolean setCameraRollToggleEnable(boolean cameraRollToggleEnable) {
        if (this.cameraRollToggleEnable == cameraRollToggleEnable) return false;
        this.cameraRollToggleEnable = cameraRollToggleEnable;
        flightplanStatementChanged(this);
        return true;
    }

    public double getCameraRollToggleDegrees() {
        return cameraRollToggleDegrees;
    }

    public boolean setCameraRollToggleDegrees(double cameraRollToggleDegrees) {
        cameraRollToggleDegrees = MathHelper.intoRange(cameraRollToggleDegrees, 0, 90);
        if (this.cameraRollToggleDegrees == cameraRollToggleDegrees) return false;
        this.cameraRollToggleDegrees = cameraRollToggleDegrees;
        flightplanStatementChanged(this);
        return true;
    }

    public double getCameraRollOffsetDegrees() {
        return cameraRollOffsetDegrees;
    }

    public boolean setCameraRollOffsetDegrees(double cameraRollOffsetDegrees) {
        cameraRollOffsetDegrees = MathHelper.intoRange(cameraRollOffsetDegrees, -180, 180);
        if (this.cameraRollOffsetDegrees == cameraRollOffsetDegrees) return false;
        this.cameraRollOffsetDegrees = cameraRollOffsetDegrees;
        flightplanStatementChanged(this);
        return true;
    }

    public double getRestrictionFloor() {
        return restrictionFloor;
    }

    public boolean setRestrictionFloor(double restrictionFloor) {
        restrictionFloor = MathHelper.intoRange(restrictionFloor, RESTRICTION_MIN, RESTRICTION_MAX);
        if (this.restrictionFloor == restrictionFloor) return false;
        this.restrictionFloor = restrictionFloor;
        this.objectHeight = restrictionCeiling - restrictionFloor;
        flightplanStatementChanged(this);
        return true;
    }

    public boolean setRestrictionCeilingEnabled(boolean restrictionCeilingEnabled) {
        if (this.restrictionCeilingEnabled == restrictionCeilingEnabled) return false;
        this.restrictionCeilingEnabled = restrictionCeilingEnabled;
        flightplanStatementChanged(this);
        return true;
    }

    public boolean isRestrictionCeilingEnabled() {
        return restrictionCeilingEnabled;
    }

    public boolean setRestrictionFloorEnabled(boolean restrictionFloorEnabled) {
        if (this.restrictionFloorEnabled == restrictionFloorEnabled) return false;
        this.restrictionFloorEnabled = restrictionFloorEnabled;
        flightplanStatementChanged(this);
        return true;
    }

    public boolean isRestrictionFloorEnabled() {
        return restrictionFloorEnabled;
    }

    public boolean setRestrictionFloorRef(RestrictedAreaHeightReferenceTypes restrictionFloorRef) {
        if (restrictionFloorRef == null) return false;
        if (this.restrictionFloorRef == restrictionFloorRef) return false;
        this.restrictionFloorRef = restrictionFloorRef;
        flightplanStatementChanged(this);
        return true;
    }

    public RestrictedAreaHeightReferenceTypes getRestrictionFloorRef() {
        return restrictionFloorRef;
    }

    public boolean setRestrictionCeilingRef(RestrictedAreaHeightReferenceTypes restrictionCeilingRef) {
        if (restrictionCeilingRef == null) return false;
        if (this.restrictionCeilingRef == restrictionCeilingRef) return false;
        this.restrictionCeilingRef = restrictionCeilingRef;
        flightplanStatementChanged(this);
        return true;
    }

    public RestrictedAreaHeightReferenceTypes getRestrictionCeilingRef() {
        return restrictionCeilingRef;
    }

    public double getRestrictionCeiling() {
        return restrictionCeiling;
    }

    public boolean setRestrictionCeiling(double restrictionCeiling) {
        restrictionCeiling = MathHelper.intoRange(restrictionCeiling, RESTRICTION_MIN, RESTRICTION_MAX);
        if (this.restrictionCeiling == restrictionCeiling) return false;
        this.restrictionCeiling = restrictionCeiling;
        this.objectHeight = restrictionCeiling - restrictionFloor;
        flightplanStatementChanged(this);
        return true;
    }

    public MinMaxPair getRestrictionIntervalInFpHeights(Position pos, double clearance) {
        if (planType.isNoFlyZone() || planType.isGeofence()) {
            double minHeight;
            double maxHeight;
            if (isRestrictionFloorEnabled()) {
                if (getRestrictionFloorRef() == RestrictedAreaHeightReferenceTypes.ABOVE_SEALEVEL) {
                    minHeight =
                        getRestrictionHeightAboveWgs84(pos, getRestrictionFloor(), getRestrictionFloorRef())
                            - getFlightplan().getRefPointAltWgs84WithElevation();
                } else {
                    // above ground
                    minHeight =
                        getRestrictionHeightAboveWgs84(pos, getRestrictionFloor(), getRestrictionFloorRef())
                            - getFlightplan().getRefPointAltWgs84WithElevation();
                }
            } else {
                minHeight = Double.NEGATIVE_INFINITY;
            }

            if (isRestrictionCeilingEnabled()) {
                if (getRestrictionCeilingRef() == RestrictedAreaHeightReferenceTypes.ABOVE_SEALEVEL) {
                    maxHeight =
                        getRestrictionHeightAboveWgs84(pos, getRestrictionCeiling(), getRestrictionCeilingRef())
                            - getFlightplan().getRefPointAltWgs84WithElevation();
                } else {
                    // above ground
                    maxHeight =
                        getRestrictionHeightAboveWgs84(pos, getRestrictionCeiling(), getRestrictionCeilingRef())
                            - getFlightplan().getRefPointAltWgs84WithElevation();
                }
            } else {
                maxHeight = Double.POSITIVE_INFINITY;
            }

            return MinMaxPair.fromMinMax(minHeight - clearance, maxHeight + clearance);
        } else if (planType == PlanType.TOWER
                || planType == PlanType.WINDMILL
                || planType == PlanType.FACADE
                || planType == PlanType.BUILDING) {
            return new MinMaxPair(Double.NEGATIVE_INFINITY, getObjectHeightRelativeToRefPoint() + clearance);
        }

        return null;
    }

    public double getPitchOffsetLineBegin() {
        return pitchOffsetLineBegin;
    }

    public boolean setPitchOffsetLineBegin(double pitchOffsetLineBegin) {
        pitchOffsetLineBegin = MathHelper.intoRange(pitchOffsetLineBegin, -90, 90);
        if (this.pitchOffsetLineBegin == pitchOffsetLineBegin) return false;
        this.pitchOffsetLineBegin = pitchOffsetLineBegin;
        flightplanStatementChanged(this);
        return true;
    }

    public boolean isSync() {
        return isSync;
    }

    /**
     * set if this picarea is currently in a auto computed state
     *
     * @param isSync
     */
    public void setSync(boolean isSync) {
        this.isSync = isSync;
        if (mute) {
            return;
        }

        if (!isSync) {
            reinit();
        }
    }

    public void flightplanStatementAdded(IFlightplanRelatedObject statement) {
        if (mute) {
            return;
        }

        setSync(false);
        super.flightplanStatementAdded(statement);
    }

    public void flightplanStatementChanged(IFlightplanRelatedObject statement) {
        if (mute) {
            return;
        }

        setSync(false);
        super.flightplanStatementChanged(statement);
    }

    public void flightplanStatementStructureChanged(IFlightplanRelatedObject statement) {
        if (mute) {
            return;
        }

        setSync(false);
        super.flightplanStatementChanged(statement);
    }

    public void flightplanStatementRemove(int i, IFlightplanRelatedObject statement) {
        if (mute) {
            return;
        }

        setSync(false);
        super.flightplanStatementRemove(i, statement);
    }

    protected void fireSyncedChange() {
        setSync(true);
        super.flightplanStatementStructureChanged(this);
    }

}
