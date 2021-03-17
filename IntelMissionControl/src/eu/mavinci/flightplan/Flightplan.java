/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.flightplan;

import com.intel.missioncontrol.collections.ArraySet;
import com.intel.missioncontrol.hardware.IGenericCameraConfiguration;
import com.intel.missioncontrol.hardware.IHardwareConfiguration;
import com.intel.missioncontrol.hardware.IPlatformDescription;
import com.intel.missioncontrol.helper.DoubleHelper;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.map.elevation.IElevationModel;
import com.intel.missioncontrol.map.elevation.MinMaxTrackDistanceAndAbsolute;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.mission.ReferencePointType;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import eu.mavinci.core.desktop.listener.WeakListenerList;
import eu.mavinci.core.flightplan.AltAssertModes;
import eu.mavinci.core.flightplan.CEventList;
import eu.mavinci.core.flightplan.CFlightplan;
import eu.mavinci.core.flightplan.CPicArea;
import eu.mavinci.core.flightplan.CWaypoint;
import eu.mavinci.core.flightplan.FMLReader;
import eu.mavinci.core.flightplan.IFlightplanContainer;
import eu.mavinci.core.flightplan.IFlightplanDeactivateable;
import eu.mavinci.core.flightplan.IFlightplanPositionReferenced;
import eu.mavinci.core.flightplan.IFlightplanRelatedObject;
import eu.mavinci.core.flightplan.IFlightplanStatement;
import eu.mavinci.core.flightplan.IRecalculateable;
import eu.mavinci.core.flightplan.KMLReader;
import eu.mavinci.core.flightplan.PlanType;
import eu.mavinci.core.flightplan.visitors.CollectsTypeVisitor;
import eu.mavinci.core.flightplan.visitors.ExtractPicAreasVisitor;
import eu.mavinci.core.flightplan.visitors.FirstLastOfTypeVisitor;
import eu.mavinci.core.flightplan.visitors.ReassignIdsVisitor;
import eu.mavinci.core.helper.MinMaxPair;
import eu.mavinci.desktop.gui.doublepanel.mapmanager.IResourceFileReferenced;
import eu.mavinci.desktop.gui.doublepanel.planemain.tree.maplayers.IMapLayerListener;
import eu.mavinci.desktop.gui.doublepanel.planemain.tree.maplayers.MapLayer;
import eu.mavinci.desktop.gui.doublepanel.planemain.wwd.FPcoveragePreview;
import eu.mavinci.desktop.helper.FileHelper;
import eu.mavinci.desktop.helper.MFileFilter;
import eu.mavinci.desktop.main.debug.Debug;
import eu.mavinci.flightplan.computation.AutoFPhelper;
import eu.mavinci.flightplan.computation.FPsim;
import eu.mavinci.flightplan.computation.FastPositionTransformationProvider;
import eu.mavinci.flightplan.computation.LocalTransformationProvider;
import eu.mavinci.flightplan.visitors.SectorVisitor;
import eu.mavinci.geo.CountryDetector;
import eu.mavinci.geo.IPositionReferenced;
import eu.mavinci.geo.ISectorReferenced;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Line;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.util.WWMath;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.OptionalDouble;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javax.swing.JFileChooser;
import org.apache.commons.lang3.NotImplementedException;

public class Flightplan extends CFlightplan implements ISectorReferenced, IResourceFileReferenced {

    public static final String KEY = "eu.mavinci.flightplan.Flightplan";
    public static final String KEY_UNNAMED_FILE = KEY + ".unnamedFP";

    private final String jumpOver =
        DependencyInjector.getInstance()
            .getInstanceOf(ILanguageHelper.class)
            .getString("com.intel.missioncontrol.ui.sidepane.planning.EditWaypointsView.jumpOver");

    protected WeakListenerList<IMapLayerListener> mapListeners = new WeakListenerList<>("mapListeners");
    private boolean allAoisSizeValid = true;

    public boolean allAoisSizeValid() {
        return allAoisSizeValid;
    }

    public void setAllAoisSizeValid(boolean allAoisSizeValid) {
        if (this.allAoisSizeValid == allAoisSizeValid) {
            return;
        }

        this.allAoisSizeValid = allAoisSizeValid;
        flightplanStatementChanged(this);
    }

    private final IElevationModel elevationModel =
        DependencyInjector.getInstance().getInstanceOf(IElevationModel.class);

    FastPositionTransformationProvider fastPositionTransformationProvider;

    public Flightplan() {
        super();
        loadingDone = true;
    }

    public Flightplan(File file) throws InvalidFlightPlanFileException {
        this();
        open(file);
        loadingDone = true;
    }

    public void open(File file) throws InvalidFlightPlanFileException {
        if (!FileHelper.canRead(file, null)) {
            throw new InvalidFlightPlanFileException("Cannot open flight plan:  " + file);
        }

        Debug.getLog().fine("try to load Flightplan from File: " + file.getAbsolutePath());

        if (file.getName().toLowerCase().endsWith(".kml")) {
            openKml(file);
        } else if (file.getName().toLowerCase().endsWith(".fml")) {
            openFml(file);
        } else {
            throw new InvalidFlightPlanFileException("Unsupported flight plan type " + file.getName());
        }

        fastPositionTransformationProvider = new FastPositionTransformationProvider(Math.toRadians(refPoint.lon));
    }

    private void openKml(File file) throws InvalidFlightPlanFileException {
        KMLReader reader = new KMLReader(CWaypoint.DEFAULT_ALT_WITHIN_M);
        openFlightPlan(
            file,
            () -> {
                try {
                    return reader.readKML(Flightplan.this, file);
                } catch (Exception e) {
                    throw new IllegalStateException("Unable to read kml file", e);
                }
            });
    }

    private void openFml(File file) throws InvalidFlightPlanFileException {
        FMLReader reader = new FMLReader();
        openFlightPlan(
            file,
            () -> {
                try {
                    return reader.readFML(Flightplan.this, file, getHardwareConfigurationManager());
                } catch (Exception e) {
                    throw new IllegalStateException("Unable to read fml file", e);
                }
            });
    }

    private void openFlightPlan(File file, Supplier<CFlightplan> fpLoader) throws InvalidFlightPlanFileException {
        setMute(true);
        reset();
        setFile(file);
        try {
            fpLoader.get();
        } catch (Exception e) {
            throw new InvalidFlightPlanFileException(String.format("%s cannot be loaded", file.getAbsolutePath()), e);
        }

        isChanged = false;
        isEmpty = false;
        setSilentUnmute();
        flightplanStatementChanged(this, false);
    }

    public void reOpen() throws InvalidFlightPlanFileException {
        open(getFile());
    }

    public void saveAs(File baseFolder) {
        File flightPlanLocation = promtUserForFlightplanFile(baseFolder);
        if (flightPlanLocation == null) {
            return;
        }

        saveToLocation(flightPlanLocation);
    }

    public void saveToLocation(File flightPlanLocation) {
        saveAsDirectly(flightPlanLocation);
    }

    private File promtUserForFlightplanFile(File baseFolder) {
        JFileChooser chooser = new JFileChooser(baseFolder);
        chooser.setMultiSelectionEnabled(false);
        chooser.setFileFilter(MFileFilter.fmlFilter);

        int result = chooser.showSaveDialog(null);

        if (result != JFileChooser.APPROVE_OPTION) {
            return null;
        }

        File file = chooser.getSelectedFile();
        file = FileHelper.validateFileName(file, MFileFilter.fmlFilter);
        if (!FileHelper.askForOverwrite(file, null)) {
            return null;
        }

        return file;
    }

    public void save(File baseFolder) {
        if (getFile() != null) {
            saveAsDirectly(getFile());
            return;
        } else {
            saveAs(baseFolder);
        }
    }

    public String toASM() {
        FMLWriter writer = new FMLWriter();
        try {
            return writer.flightplanToASM(this);
        } catch (IOException e) {
            Debug.getLog().log(Level.WARNING, "Flightplan ASM conversion Problems", e);
            return null;
        }
    }

    SectorVisitor secVis = null;

    void startSecVis() {
        if (secVis != null) {
            return;
        }

        secVis = new SectorVisitor(this);
        Sector s = secVis.getSector();
        maxElev = secVis.getMaxElev();
        if (s != null) {
            LocalTransformationProvider trafo =
                new LocalTransformationProvider(new Position(s.getCentroid(), 0), Angle.ZERO, 0, 0, true);
            // for panorama flight plans the area covered by the points is basically zero, this
            // means the cam will sit exactly on the points, but actally we will then not getting them
            // rendered on the screen
            // => mitigation: if area is too small, add a height offset
            if (AutoFPhelper.computeArea(
                        Arrays.stream(s.getCorners()).map(c -> trafo.transformToLocal(c)).collect(Collectors.toList()))
                    < 100) { // smaller 100m²
                if (maxElev.isPresent()) {
                    maxElev = OptionalDouble.of(maxElev.getAsDouble() + 25); // add 25m
                }
            }
        }
    }

    @Override
    public void flightplanStatementAdded(IFlightplanRelatedObject statement) {
        secVis = null;
        super.flightplanStatementAdded(statement);
    }

    @Override
    public void flightplanStatementChanged(IFlightplanRelatedObject statement) {
        secVis = null;
        super.flightplanStatementChanged(statement);
    }

    @Override
    public void flightplanStatementRemove(int i, IFlightplanRelatedObject statement) {
        secVis = null;
        super.flightplanStatementRemove(i, statement);
    }

    @Override
    public void flightplanStatementStructureChanged(IFlightplanRelatedObject statement) {
        secVis = null;
        super.flightplanStatementStructureChanged(statement);
    }

    @Override
    public Sector getSector() {
        startSecVis();
        return secVis.getSector();
    }

    OptionalDouble maxElev;

    @Override
    public OptionalDouble getMaxElev() {
        startSecVis();
        return maxElev;
    }

    @Override
    public OptionalDouble getMinElev() {
        startSecVis();
        return secVis.getMinElev();
    }

    protected Flightplan(Flightplan source) {
        super(source.hardwareConfiguration.deepCopy());
        this.setFile(null);
        this.isLearningmode = source.isLearningmode;

        this.landingpoint = new LandingPoint(source.landingpoint);
        this.landingpoint.setParent(this);

        this.refPoint = new ReferencePoint(source.refPoint);
        this.refPoint.setParent(this);

        this.takeoff = new Takeoff(source.takeoff);
        this.takeoff.setParent(this);
        // these objects are effectively immutable (except flightPlanDescription but it will disappear -- so no sense to
        // clone)
        this.picAreaTemplates = source.picAreaTemplates;

        this.photoSettings = source.photoSettings.clone();
        this.photoSettings.setParent(this);

        this.eventList = (CEventList)source.eventList.getCopy();
        this.eventList.setParent(this);

        for (IFlightplanStatement statement : source.elements) {
            IFlightplanRelatedObject statementCopy = statement.getCopy();
            this.elements.add((IFlightplanStatement)statementCopy);
            statementCopy.setParent(this);
        }

        ExtractPicAreasVisitor vis = new ExtractPicAreasVisitor();
        vis.startVisit(this);
        boolean validSizeAOIs = true;
        for (CPicArea picArea : vis.picAreas) {
            picArea.setupTransform();
            picArea.computeFlightlinesWithLastPlaneSilent();
            picArea.reinit();
            if (!((PicArea)picArea).validSizeAOI || !((PicArea)picArea).validAOI) {
                validSizeAOIs = false;
            }
        }

        allAoisSizeValid = validSizeAOIs;
        this.isEmpty = source.isEmpty;
        this.isChanged = true;
        this.basedOnTemplate = source.basedOnTemplate;
        this.name = source.name;
        this.recalculateOnEveryChange = source.recalculateOnEveryChange;
        loadingDone = true;
        this.enableJumpOverWaypoints = source.enableJumpOverWaypoints;
    }

    @Override
    public Flightplan getCopy() {
        return new Flightplan(this);
    }

    @Override
    public Flightplan getFlightplan() {
        return (Flightplan)super.getFlightplan();
    }

    @Override
    public LandingPoint getLandingpoint() {
        return super.getLandingpoint();
    }

    @Override
    public PhotoSettings getPhotoSettings() {
        return (PhotoSettings)super.getPhotoSettings();
    }

    @Override
    public EventList getEventList() {
        return (EventList)eventList;
    }

    @Override
    protected void saveAsDirectly(File file) {
        super.saveAsDirectly(file);
    }

    public ReferencePointType getRefPointType() {
        return refPoint.getRefPointType();
    }

    public void setRefPointType(ReferencePointType type) {
        refPoint.setRefPointType(type);
    }

    public int getRefPointOptionIndex() {
        return refPoint.getRefPointOptionIndex();
    }

    public void setRefPointOptionIndex(int idx) {
        refPoint.setRefPointOptionIndex(idx);
    }

    public boolean enableJumpOverWaypoints() {
        return enableJumpOverWaypoints;
    }

    public void setEnableJumpOverWaypoints(boolean enableJumpOverWaypoints) {
        if (this.enableJumpOverWaypoints == enableJumpOverWaypoints) {
            return;
        }

        this.enableJumpOverWaypoints = enableJumpOverWaypoints;
        if (!doFlightplanCalculationIfAutoRecalcIsAvtive()) {
            flightplanStatementChanged(this);
        }
    }

    @Override
    public String getInternalName() {
        if (getFile() == null) {
            return DependencyInjector.getInstance().getInstanceOf(ILanguageHelper.class).getString(KEY_UNNAMED_FILE);
        }

        String name = getFile().getName();

        if (isChanged) {
            name += "*";
        }

        return name;
    }

    @Override
    public boolean isAddableToFlightplanContainer(Class<? extends IFlightplanStatement> cls) {
        // System.out.println("addable to flightplancontainer:" + super.isAddableToFlightplanContainer(cls));
        return super.isAddableToFlightplanContainer(cls) && !MapLayer.class.isAssignableFrom(cls);
    }

    @Override
    public File getResourceFile() {
        return getFile();
    }

    FPsim fpSim;
    Object simLock = new Object();

    public FPsim getFPsim() {
        synchronized (simLock) {
            if (fpSim == null) fpSim = new FPsim(this);
        }

        return fpSim;
    }

    private FPcoveragePreview fPcoveragePreview;
    private Object fPcoveragePreviewLock = new Object();

    public FPcoveragePreview getFPcoverage() {
        synchronized (fPcoveragePreviewLock) {
            if (fPcoveragePreview == null) fPcoveragePreview = new FPcoveragePreview(this);
        }

        return fPcoveragePreview;
    }

    public Position getFirstWaypointPosition(boolean includeAltitude) {
        PicArea firstPicArea = getFirstElement(PicArea.class);

        if (firstPicArea == null) {
            return null;
        }

        Waypoint firstWaypoint = firstPicArea.getFirstElement(Waypoint.class);

        if (firstWaypoint == null) {
            return null;
        }

        double altitude = ((includeAltitude) ? (firstWaypoint.getAltInMAboveFPRefPoint()) : (0.0));
        return Position.fromDegrees(firstWaypoint.getLat(), firstWaypoint.getLon(), altitude);
    }

    @Override
    public boolean doSubRecalculationStage1() {
        if (!CountryDetector.instance.allowProceed(getSector())) {
            return false;
        }

        if (!getHardwareConfiguration().hasPrimaryPayload(IGenericCameraConfiguration.class)) {
            return false;
        }

        return true;
    }

    @Override
    public boolean doSubRecalculationStage2() {
        return true;
    }

    // does the line from pointA to pointB intersect the plane containing lastCorner, thisCorner, and a corner at
    // height?
    private Vec4 lineSegmentIntersectPlaneSegment(
            Vec4 linep1, Vec4 linep2, Vec4 planep1, Vec4 planep2, PicArea picArea, double clearance) {
        // make plane from thisCorner, lastCorner, and another corner at height
        Vec4 planep3 = new Vec4(planep1.x, planep1.y, planep1.z + 10);
        Vec4 normal =
            new Vec4(
                (planep2.y - planep1.y) * (planep3.z - planep1.z) - (planep2.z - planep1.z) * (planep3.y - planep1.y),
                -(planep2.x - planep1.x) * (planep3.z - planep1.z) + (planep2.z - planep1.z) * (planep3.x - planep1.x),
                (planep2.x - planep1.x) * (planep3.y - planep1.y) - (planep2.y - planep1.y) * (planep3.x - planep1.x));

        // make line direction from points
        Vec4 lineDir = linep2.subtract3(linep1);

        // find intersection of plane and line from pointA to pointB, if any
        double ldotn = lineDir.dot3(normal);
        double lp0dotn = (planep1.subtract3(linep1)).dot3(normal);
        if (DoubleHelper.areClose(ldotn, 0)) {
            if (DoubleHelper.areClose(lp0dotn, 0)) {
                return linep1;
            }
        }

        // if height of intersection point is below height, return true
        double intersectd = lp0dotn / ldotn;
        if (intersectd < 0 || 1 < intersectd) {
            // given definition of lineDir, line segment ends defined by this range on intersectd
            return null;
        }

        Vec4 intersect = (lineDir.multiply3(intersectd)).add3(linep1);

        if (new MinMaxPair(planep1.x, planep2.x).contains(intersect.x)
                && new MinMaxPair(planep1.y, planep2.y).contains(intersect.y)
                && picArea.getRestrictionIntervalInFpHeights(
                        fastPositionTransformationProvider.cheapToGlobalRelHeights(intersect), clearance)
                    .contains(intersect.z)) {
            return intersect;
        }

        return null;
    }

    // check if point too close to plane segment
    private Vec4 pointTooCloseToPlaneSegment(
            Vec4 linep1,
            Vec4 planep1,
            Vec4 planep2,
            Vec4 normal,
            PicArea picArea,
            double clearanceXY,
            double clearanceZ) {
        Vec4 pt2plane1 = linep1.subtract3(planep1);
        double vdotn1 = pt2plane1.dot3(normal);
        if (Math.abs(vdotn1) <= clearanceXY) {
            Vec4 proj1 = linep1.add3(normal.multiply3(vdotn1)); // nearest point on plane to linep1
            if (new MinMaxPair(planep1.x, planep2.x).contains(proj1.x)
                    && new MinMaxPair(planep1.y, planep2.y).contains(proj1.y)
                    && picArea.getRestrictionIntervalInFpHeights(
                            fastPositionTransformationProvider.cheapToGlobalRelHeights(proj1), clearanceZ)
                        .contains(proj1.z)) {
                return proj1;
            }
        }

        return null;
    }

    // are any of the line end points too close to the plane segment? checking clearance and height
    private Vec4 lineEndPointsTooCloseToPlaneSegment(
            Vec4 linep1,
            Vec4 linep2,
            Vec4 planep1,
            Vec4 planep2,
            PicArea picArea,
            double clearanceXY,
            double clearanceZ) {
        // make plane from thisCorner, lastCorner, and another corner at height
        Vec4 planep3 = new Vec4(planep1.x, planep1.y, planep1.z + 10);
        Vec4 normal =
            new Vec4(
                (planep2.y - planep1.y) * (planep3.z - planep1.z) - (planep2.z - planep1.z) * (planep3.y - planep1.y),
                -(planep2.x - planep1.x) * (planep3.z - planep1.z) + (planep2.z - planep1.z) * (planep3.x - planep1.x),
                (planep2.x - planep1.x) * (planep3.y - planep1.y) - (planep2.y - planep1.y) * (planep3.x - planep1.x));

        normal = normal.normalize3();

        // test end point 1
        Vec4 intersect1 =
            pointTooCloseToPlaneSegment(linep1, planep1, planep2, normal, picArea, clearanceXY, clearanceZ);
        if (intersect1 != null) {
            return intersect1;
        }

        // test end point 2
        Vec4 intersect2 =
            pointTooCloseToPlaneSegment(linep2, planep1, planep2, normal, picArea, clearanceXY, clearanceZ);
        if (intersect2 != null) {
            return intersect2;
        }

        return null;
    }

    private ArrayList<Vec4> extendLineForClearance(Vec4 a, Vec4 b, double clearance) {
        // extend the line from a to b by clearance in both directions
        ArrayList<Vec4> extensions = new ArrayList<Vec4>();
        extensions.add(a);
        extensions.add(b);
        if (clearance <= 0) {
            return extensions;
        }

        Vec4 lineDir = b.subtract3(a);
        lineDir = lineDir.normalize3();

        Vec4 newa = a.subtract3(lineDir.multiply3(clearance));
        Vec4 newb = b.add3(lineDir.multiply3(clearance));
        extensions.set(0, newa);
        extensions.set(1, newb);

        return extensions;
    }

    public Collection<PicArea> firstCollisionLineWithAOI(Position start, Position end, Vector<PicArea> picAreas) {
        if (fastPositionTransformationProvider == null) {
            throw new NotImplementedException("uninitialized centralLongitudeRad");
        }

        ConnectingFlightLine connectingFlightLine = new ConnectingFlightLine(null, null, start, end, null, null);
        ArraySet<PicArea> geoFences = new ArraySet<>();
        ArraySet<PicArea> collisions = new ArraySet<>();
        for (PicArea picArea : picAreas) {
            if (picArea.getPlanType().isGeofence()) {
                geoFences.add(picArea);
            } else if (isCollisionLineWithAOI(connectingFlightLine, picArea, 0)) {
                collisions.add(picArea);
            }
        }

        if (!geoFences.isEmpty()) {
            boolean foundFence = false;
            for (PicArea picArea : geoFences) {
                if (isCollisionLineWithAOI(connectingFlightLine, picArea, 0)) {
                    foundFence = true;
                    break;
                }
            }

            if (!foundFence) {
                geoFences.addAll(collisions);
                return geoFences;
            }
        }

        return collisions;
    }

    private boolean checkIntersectionPathCylinder(
            Vec4 pointA, Vec4 pointB, double cylHeight, double cylRadius, double cylThinRadius) {
        // assumes test cylinder is centered at (0,0,0) and extends in the z-axis direction, thinRadius is y direction
        // check if both end points above z-range
        if (cylHeight < pointA.z && cylHeight < pointB.z) {
            return false;
        }
        // check if both end points below z-range
        if (pointA.z < 0 && pointB.z < 0) {
            return false;
        }

        // check if line end pointA is in (elliptical) cylinder
        double cylRadius2 = cylRadius * cylRadius;
        double cylThinRadius2 = cylThinRadius * cylThinRadius;
        double c1 = (pointA.x * pointA.x) / cylThinRadius2 + (pointA.y * pointA.y) / cylRadius2;
        if (c1 <= 1 && 0 <= pointA.z && pointA.z <= cylHeight) {
            return true;
        }
        // check if line end pointB is in (elliptical) cylinder
        c1 = (pointB.x * pointB.x) / cylThinRadius2 + (pointB.y * pointB.y) / cylRadius2;
        if (c1 <= 1 && 0 <= pointB.z && pointB.z <= cylHeight) {
            return true;
        }

        // now neither line end point is within cylinder and some heights in z range or pass through it
        Line line = Line.fromSegment(pointA, pointB);
        Vec4 lindir = line.getDirection();

        // check for z-plane intersections with line
        if (!DoubleHelper.areClose(lindir.z, 0)) { // z slope not 0
            // check intersection points in t range and within ellipse
            double tp1 = (cylHeight - pointA.z) / lindir.z; // line intersect z == cylHeight plane
            if (0 <= tp1 && tp1 <= 1) {
                Vec4 zint = line.getPointAt(tp1);
                double cp1 = (zint.x * zint.x) / cylThinRadius2 + (zint.y * zint.y) / cylRadius2;
                if (cp1 <= 1) {
                    return true;
                }
            }

            tp1 = (0 - pointA.z) / lindir.z; // line intersect z == 0 poane
            if (0 <= tp1 && tp1 <= 1) {
                Vec4 zint = line.getPointAt(tp1);
                double cp1 = (zint.x * zint.x) / cylThinRadius2 + (zint.y * zint.y) / cylRadius2;
                if (cp1 <= 1) {
                    return true;
                }
            }
        }

        // check for elliptical cylinder intersections with line
        double a = cylRadius2 * lindir.x * lindir.x + cylThinRadius2 * lindir.y * lindir.y;
        if (DoubleHelper.areClose(a, 0)) {
            return (c1 <= 1); // vertical line, inside or not
        }

        double b = 2 * (cylRadius2 * lindir.x * pointA.x + cylThinRadius2 * lindir.y * pointA.y);
        double c =
            cylRadius2 * pointA.x * pointA.x + cylThinRadius2 * pointA.y * pointA.y - cylThinRadius2 * cylRadius2;
        double radical = b * b - 4 * a * c;
        if (radical < 0) {
            return false; // skew line case
        }

        radical = Math.sqrt(radical);
        // check intersection points in t range and z range
        double t = (-b + radical) / (2 * a);
        if (0 <= t && t <= 1) {
            double zc = pointA.z + t * lindir.z;
            if (0 <= zc && zc <= cylHeight) {
                return true;
            }
        }

        t = (-b - radical) / (2 * a);
        if (0 <= t && t <= 1) {
            double zc = pointA.z + t * lindir.z;
            if (0 <= zc && zc <= cylHeight) {
                return true;
            }
        }

        return false;
    }

    private boolean setupWindmillTransforms(PicArea picArea, WindmillData windmill, double clearance) {
        Vector<LatLon> corLatLon = picArea.getCornersVec();
        if (corLatLon.size() != 1) {
            return false;
        }

        // build transformation to center location
        Vec4 centerBot = fastPositionTransformationProvider.cheapToLocalRelHeights(corLatLon.get(0));
        Vec4 centTranslation = centerBot.multiply3(-1);
        windmill.centerTransform = Matrix.fromTranslation(centTranslation);

        // build transformations for hub
        Angle spin1 = Angle.fromDegrees(90 - windmill.hubYaw);
        Matrix hubRotation1 = Matrix.fromAxisAngle(spin1, 0, 0, 1);
        hubRotation1 = hubRotation1.multiply(windmill.centerTransform);
        Vec4 hubTranslationX = new Vec4(0, 0, -(windmill.towerHeight + windmill.hubRadius), 0);
        Matrix hubTransX = Matrix.fromTranslation(hubTranslationX);
        hubTransX = hubTransX.multiply(hubRotation1);

        Matrix hubRotation2 = Matrix.fromAxisAngle(Angle.fromDegrees(-90), 0, 1, 0);
        hubRotation2 = hubRotation2.multiply(hubTransX);
        Vec4 hubTranslation2 = new Vec4(0, 0, windmill.hubHalfLength + clearance, 0);
        windmill.hubTransform = Matrix.fromTranslation(hubTranslation2);
        windmill.hubTransform = windmill.hubTransform.multiply(hubRotation2);

        // build transformations for blades
        Vec4 bladeTrans1 = new Vec4(windmill.hubHalfLength - windmill.bladeRadius, 0, 0, 0);
        Matrix bladeTransX = Matrix.fromTranslation(bladeTrans1);
        bladeTransX = bladeTransX.multiply(hubTransX);

        double bladeRotationDegs = windmill.bladeStartRotation;
        double bladeRotationStep = 360 / windmill.numberOfBlades;
        windmill.bladeTransforms.clear();

        for (int bladeNum = 0; bladeNum < windmill.numberOfBlades; bladeNum++) {
            Angle blade1spin = Angle.fromDegrees(Angle.normalizedDegrees(bladeRotationDegs));
            Matrix bladeRotation = Matrix.fromAxisAngle(blade1spin, 1, 0, 0);
            bladeRotation = bladeRotation.multiply(bladeTransX);
            Vec4 bladeTrans3 = new Vec4(0, 0, -windmill.hubRadius, 0);
            Matrix bladeTrans = Matrix.fromTranslation(bladeTrans3);
            bladeTrans = bladeTrans.multiply(bladeRotation);
            Angle bladePitch = Angle.fromDegrees(Angle.normalizedDegrees(windmill.bladePitch));
            Matrix bladeTwist = Matrix.fromAxisAngle(bladePitch, 0, 0, 1);
            bladeTwist = bladeTwist.multiply(bladeTrans);

            windmill.bladeTransforms.add(bladeTwist);

            bladeRotationDegs -= bladeRotationStep;
        }

        return true; // all setup completed
    }

    private boolean isCollisionWithWindmill(ConnectingFlightLine connection, PicArea picArea, double safetyMargin) {
        // get windmill parameters
        WindmillData windmill = picArea.windmill;

        // get points for testing
        Vec4 pointA = fastPositionTransformationProvider.cheapToLocalRelHeights(connection.startPos);
        Vec4 pointB = fastPositionTransformationProvider.cheapToLocalRelHeights(connection.endPos);

        // check if intersection with unlocked windmill

        double clearance = picArea.getMinObjectDistance() + safetyMargin;
        if (windmill.centerTransform == null) {
            if (!setupWindmillTransforms(picArea, windmill, clearance)) {
                return false;
            }
        }

        Matrix centTrans = windmill.centerTransform;

        // TODO: this is used to fly over unlocked windmill, can probably do better (fly around locked windmill)
        MinMaxPair mmpClearance = new MinMaxPair(Double.NEGATIVE_INFINITY, windmill.totalHeight + clearance);

        double cylHeight =
            picArea.getObjectHeightRelativeToRefPoint()
                + windmill.totalHeight
                - windmill.towerHeight
                + clearance; // current AOI height is tower height
        double cylRadius = windmill.unlockedRadius + clearance;

        Vec4 pointATrans = pointA.transformBy4(centTrans);
        Vec4 pointBTrans = pointB.transformBy4(centTrans);

        if (!checkIntersectionPathCylinder(pointATrans, pointBTrans, cylHeight, cylRadius, cylRadius)) {
            // System.out.println("no intersect unlocked WM");
            return false;
        }

        // the following assumes there was a hit with the unlocked version of the windmill
        if (!windmill.locked) {
            // System.out.println("intersect unlocked WM");
            connection.clearObstacleHeight.enlarge(mmpClearance);
            return true;
        }

        // line intersects with unlocked windmill space; check composite parts of windmill

        // check if intersection with windmill tower
        cylHeight = windmill.towerHeight + clearance;
        cylRadius = windmill.towerRadius + clearance;

        if (checkIntersectionPathCylinder(pointATrans, pointBTrans, cylHeight, cylRadius, cylRadius)) {
            // System.out.println("intersect WM tower");
            connection.clearObstacleHeight.enlarge(mmpClearance);
            return true;
        }

        // check if intersection with windmill hub/nacelle
        cylHeight = (windmill.hubHalfLength + clearance) * 2;
        cylRadius = windmill.hubRadius + clearance;

        Matrix hubTrans = windmill.hubTransform;

        pointATrans = pointA.transformBy4(hubTrans);
        pointBTrans = pointB.transformBy4(hubTrans);
        if (checkIntersectionPathCylinder(pointATrans, pointBTrans, cylHeight, cylRadius, cylRadius)) {
            // System.out.println("intersect WM hub");
            connection.clearObstacleHeight.enlarge(mmpClearance);
            return true;
        }

        // check if intersection with windmill blades
        cylHeight = windmill.bladeLength + clearance;
        cylRadius = windmill.bladeRadius + clearance;
        double cylThinRadius = windmill.bladeThinRadius + clearance;

        for (int bladeNum = 0; bladeNum < windmill.numberOfBlades; bladeNum++) {
            Matrix bladeTrans = windmill.bladeTransforms.get(bladeNum);

            pointATrans = pointA.transformBy4(bladeTrans);
            pointBTrans = pointB.transformBy4(bladeTrans);

            if (checkIntersectionPathCylinder(pointATrans, pointBTrans, cylHeight, cylRadius, cylThinRadius)) {
                // System.out.println("intersect WM blade " + bladeNum);
                connection.clearObstacleHeight.enlarge(mmpClearance);
                return true;
            }
        }

        // no intersection with windmill components
        // System.out.println("no intersect WM");
        return false;
    }

    // during computation of the flightplan, we have to add some extra margin, otherwise we might find issues during
    // checking because of numerical errors
    public static final double JUMP_SAFETY_MARGIN = 1;

    private boolean isCollisionLineWithAOI(ConnectingFlightLine connection, PicArea picArea, double safetyMargin) {
        PlanType pt = picArea.getPlanType();
        if (pt != PlanType.BUILDING
                && pt != PlanType.FACADE
                && pt != PlanType.TOWER
                && pt != PlanType.WINDMILL
                && pt != PlanType.NO_FLY_ZONE_CIRC
                && pt != PlanType.NO_FLY_ZONE_POLY
                && pt != PlanType.GEOFENCE_CIRC
                && pt != PlanType.GEOFENCE_POLY) {
            return false;
        }

        // special case for now, composite AOI type
        if (pt == PlanType.WINDMILL) {
            return isCollisionWithWindmill(connection, picArea, safetyMargin);
        }

        double clearanceXY = 0;
        double clearanceZ = 1;

        // so the problem described in IMC-1796 was due to the height variable
        // when the object is no-fly zone or a geo-fence, it´s height should be casted here to the point R
        double height;
        if (!pt.hasWaypoints()) {
            height =
                picArea.getRestrictionHeightAboveWgs84(
                        picArea.getCenter(), picArea.getRestrictionCeiling(), picArea.getRestrictionCeilingRef())
                    - getRefPointAltWgs84WithElevation();
        } else {
            clearanceXY = clearanceZ = picArea.getMinObjectDistance() + safetyMargin;
            height = picArea.getObjectHeightRelativeToRefPoint() + clearanceZ;
        }

        // get points for testing
        Vector<LatLon> corLatLon = picArea.getHull();
        if (corLatLon == null || corLatLon.size() == 0) {
            return false;
        }

        ArrayList<Vec4> corners = new ArrayList<Vec4>();
        for (LatLon latLon : corLatLon) {
            Vec4 v = fastPositionTransformationProvider.cheapToLocalRelHeights(latLon);
            corners.add(v);
        }

        Vec4 pointA = fastPositionTransformationProvider.cheapToLocalRelHeights(connection.startPos);
        Vec4 pointB = fastPositionTransformationProvider.cheapToLocalRelHeights(connection.endPos);

        // if line always higher than AOI, no collision
        if (!pt.hasWaypoints()) {
            if (pointA.z > height && pointB.z > height) {
                return false;
            }
        }

        // check each face of AOI for intersection with input connection line, only need one to intersect
        // (assuming intersection with a corner is still an intersection)
        Vec4 lastCorner = corners.get(corners.size() - 1);
        if (!pt.isClosedPolygone() && !pt.isCircular()) {
            // don't wrap e.g. facades -- don't close loop automatically
            lastCorner = null;
        }

        for (Vec4 thisCorner : corners) {
            if (lastCorner != null) {
                // extend lastCorner to thisCorner line by clearance in both directions
                ArrayList<Vec4> extensions = extendLineForClearance(lastCorner, thisCorner, clearanceXY);

                // if line goes through plane below height, definite collision
                Vec4 intersect1 =
                    lineSegmentIntersectPlaneSegment(
                        pointA, pointB, extensions.get(0), extensions.get(1), picArea, clearanceZ);
                if (intersect1 != null) {
                    connection.clearObstacleHeight.enlarge(
                        picArea.getRestrictionIntervalInFpHeights(
                            fastPositionTransformationProvider.cheapToGlobalRelHeights(intersect1), clearanceZ));
                    return true;
                }

                // if line end points too close to plane below height, definite collision
                Vec4 intersect2 =
                    lineEndPointsTooCloseToPlaneSegment(
                        pointA, pointB, extensions.get(0), extensions.get(1), picArea, clearanceXY, clearanceZ);
                if (intersect2 != null) {
                    connection.clearObstacleHeight.enlarge(
                        picArea.getRestrictionIntervalInFpHeights(
                            fastPositionTransformationProvider.cheapToGlobalRelHeights(intersect2), clearanceZ));
                    return true;
                }
            }

            lastCorner = thisCorner;
        }

        // check points below height to see if they are internal to AOI
        if (!pt.isClosedPolygone() && !pt.isCircular()) {
            return false;
        }

        ArrayList<LatLon> corLatLonLoop = new ArrayList<LatLon>(corLatLon);
        corLatLonLoop.add(corLatLon.get(0));
        MinMaxPair heightStart = picArea.getRestrictionIntervalInFpHeights(connection.startPos, clearanceZ);
        if (heightStart.contains(pointA.z) && WWMath.isLocationInside(connection.startPos, corLatLonLoop)) {
            connection.clearObstacleHeight.enlarge(heightStart);
            return true;
        }

        MinMaxPair heightEnd = picArea.getRestrictionIntervalInFpHeights(connection.endPos, clearanceZ);
        if (heightEnd.contains(pointB.z) && WWMath.isLocationInside(connection.endPos, corLatLonLoop)) {
            connection.clearObstacleHeight.enlarge(heightEnd);
            return true;
        }

        return false;
    }

    // class to represent info for connecting flight lines between AOIs
    private static class ConnectingFlightLine {
        private final IFlightplanPositionReferenced startPoint; // starting point of line
        private final IFlightplanPositionReferenced endPoint; // ending point of line

        private final Position startPos; // starting point of line
        private final Position endPos; // ending point of line

        private final IFlightplanContainer startContainer; // picArea of starting waypoint
        private final IFlightplanContainer endContainer; // picArea of ending waypoint

        private final MinMaxPair clearObstacleHeight = new MinMaxPair(); // height to clear highest obstacle

        ConnectingFlightLine(
                IFlightplanPositionReferenced startPoint,
                IFlightplanPositionReferenced endPoint,
                Position startPos,
                Position endPos,
                IFlightplanContainer startPicArea,
                IFlightplanContainer endPicArea) {
            this.startPoint = startPoint;
            this.endPoint = endPoint;
            this.startPos = startPos;
            this.endPos = endPos;
            this.startContainer = startPicArea;
            this.endContainer = endPicArea;
        }

        @Override
        public String toString() {
            return "line:"
                + startPoint
                + "("
                + startPos
                + ")@"
                + startContainer
                + " => "
                + endPoint
                + "("
                + endPos
                + ")@"
                + endContainer
                + "  height:"
                + clearObstacleHeight;
        }
    }

    // add a waypoint to jump over an AOI obstacle
    // "clearHeight" and position.elevation are the heights above FP.R
    private void addWaypointToJumpOver(
            double clearHeight, Position position, IFlightplanContainer addToContainer, boolean addToEnd) {
        clearHeight += JUMP_SAFETY_MARGIN;
        try {
            // the following `if` was changed to the opposite way because it doesn't work the other way around..
            // TODO explain why was it different before
            if (position.getElevation() <= clearHeight) {
                // add new connector WP to existing list of WPs
                Waypoint newWp =
                    new Waypoint(
                        position.getLongitude().degrees,
                        position.getLatitude().degrees,
                        (float)clearHeight,
                        AltAssertModes.jump,
                        0,
                        jumpOver,
                        0,
                        null);
                newWp.setStopHereTimeCopter(1);
                newWp.setSpeedMpSec(getPhotoSettings().getMaxGroundSpeedMPSec());
                if (addToEnd) {
                    addToContainer.addToFlightplanContainer(newWp);
                } else {
                    addToContainer.addToFlightplanContainer(0, newWp);
                }
            }

        } catch (Exception e) {
            Debug.getLog().log(Level.WARNING, "problem adding path over obstacles between AOIs", e);
        }
    }

    public static final double MIN_HEIGHT_OVER_TAKEOFF_TO_START_MISSION_FALCON = 10;

    // find connections between AOIs and check/warn if they intersect any AOIs
    private void checkCollisionsBetweenAOIConnections(CollectsTypeVisitor<IRecalculateable> collectsTypeVisitor) {
        // collect line segments connecting AOIs
        ArrayList<ConnectingFlightLine> connectors = new ArrayList<ConnectingFlightLine>();
        // TODO here should be landing lastWp
        IFlightplanPositionReferenced lastWp = getTakeoff();
        Position lastPos =
            new Position(
                getTakeoff().getPosition(),
                getTakeoff().getAltInMAboveFPRefPoint() + MIN_HEIGHT_OVER_TAKEOFF_TO_START_MISSION_FALCON);
        IFlightplanContainer lastContainer = null;

        for (IRecalculateable calc : collectsTypeVisitor.matches) {
            if (calc == this) {
                continue;
            }

            if (calc instanceof IFlightplanDeactivateable) {
                IFlightplanDeactivateable act = (IFlightplanDeactivateable)calc;
                if (!act.isActive()) {
                    continue;
                }
            }

            if (calc instanceof IFlightplanContainer) {
                IFlightplanContainer container = (IFlightplanContainer)calc;
                FirstLastOfTypeVisitor vis = new FirstLastOfTypeVisitor(IPositionReferenced.class);
                vis.startVisit(container);
                if (vis.first == null) {
                    continue;
                }

                IFlightplanPositionReferenced nextWp = (IFlightplanPositionReferenced)vis.first;
                IPositionReferenced nextWpPosRef = (IPositionReferenced)vis.first;
                Position nextPos = nextWpPosRef.getPosition();
                IFlightplanContainer nextContainer = container;

                if (lastPos != null && !lastPos.equals(nextPos)) {
                    // new line segment is from lastWp of last picArea to firstWp of this picArea
                    connectors.add(
                        new ConnectingFlightLine(lastWp, nextWp, lastPos, nextPos, lastContainer, nextContainer));
                }

                lastWp = (IFlightplanPositionReferenced)vis.last;
                IPositionReferenced lastWpPosRef = (IPositionReferenced)vis.last;
                lastPos = lastWpPosRef.getPosition();
                lastContainer = container;
            }
        }

        if (getLandingpoint().isActive()) {
            IFlightplanPositionReferenced nextWp = getLandingpoint();
            Position nextPos = getLandingpoint().getPosition();
            IFlightplanContainer nextContainer = null;

            if (lastPos != null && !lastPos.equals(nextPos)) {
                connectors.add(
                    new ConnectingFlightLine(lastWp, nextWp, lastPos, nextPos, lastContainer, nextContainer));
            }
        }

        IHardwareConfiguration hardwareConfiguration = this.getHardwareConfiguration();
        IPlatformDescription platformDescription = hardwareConfiguration.getPlatformDescription();
        double minGroundDistInM =
            platformDescription.getMinGroundDistance().convertTo(Unit.METER).getValue().doubleValue();

        // check line segments between AOIs, warn if any intersect any AOIs
        for (ConnectingFlightLine connection : connectors) {
            // check collision with terrain

            // making relative flight plan elements absolute
            Position absStartPos =
                new Position(connection.startPos, connection.startPos.elevation + getRefPointAltWgs84WithElevation());
            Position absEndPos =
                new Position(connection.endPos, connection.endPos.elevation + getRefPointAltWgs84WithElevation());

            // EarthElevationModel operates with absolute values
            MinMaxTrackDistanceAndAbsolute heights =
                elevationModel.computeMinMaxTrackDistanceAndAbsolute(absStartPos, absEndPos);

            heights.minMaxDistanceToGround.shift(-minGroundDistInM - IElevationModel.TINY_GROUND_ELEVATION);

            if (heights.minMaxDistanceToGround.min < 0) {
                heights.minMaxGroundHeight.shift(-getRefPointAltWgs84WithElevation()); // get it relative to R
                heights.minMaxGroundHeight.shift(
                    minGroundDistInM + IElevationModel.TINY_GROUND_ELEVATION); // add additional safety margin
                connection.clearObstacleHeight.enlarge(heights.minMaxGroundHeight);
            }

            for (IRecalculateable calc : collectsTypeVisitor.matches) {
                if (calc instanceof PicArea) {
                    PicArea picArea = (PicArea)calc;
                    if (picArea.getPlanType().isGeofence()) {
                        continue;
                    }
                    // check collisions with AOIs
                    isCollisionLineWithAOI(connection, picArea, JUMP_SAFETY_MARGIN);
                }
            }
        }

        // add waypoints to jump over AOI obstacles
        for (ConnectingFlightLine connection : connectors) {
            if (!connection.clearObstacleHeight.isValid()) {
                // no changes necessary, no collision found on this path
                continue;
            }

            if (connection.clearObstacleHeight.max >= Double.POSITIVE_INFINITY) {
                Debug.getLog().log(Level.WARNING, "Cannot fly over obstacle.");
                throw new NotImplementedException("obstacle has infinite height and we cant fly over it");
            }

            Debug.getLog().log(Level.INFO, "Adding waypoints to avoid detected collisions in connecting flights.");

            if (enableJumpOverWaypoints) {
                if (connection.startContainer != null) {
                    if (connection.endContainer != null) {
                        addWaypointToJumpOver(
                            connection.clearObstacleHeight.max, connection.startPos, connection.startContainer, true);
                        addWaypointToJumpOver(
                            connection.clearObstacleHeight.max, connection.endPos, connection.endContainer, false);
                    } else {
                        addWaypointToJumpOver(
                            connection.clearObstacleHeight.max, connection.startPos, connection.startContainer, true);
                        addWaypointToJumpOver(
                            connection.clearObstacleHeight.max, connection.endPos, connection.startContainer, true);
                    }
                } else {
                    if (connection.endContainer != null) {
                        // here adding start and end is intentionally swapped since both are added at the begin, and we
                        // will
                        // otherwise end up with the wrong order
                        addWaypointToJumpOver(
                            connection.clearObstacleHeight.max, connection.endPos, connection.endContainer, false);
                        addWaypointToJumpOver(
                            connection.clearObstacleHeight.max, connection.startPos, connection.endContainer, false);
                    } else {
                        // TODO at the moment this case isnt happening, but if it would... we ahve to add it
                        // somewhere...
                        // justadding it to the flightplan would mean we are never deleting them again at the moment
                        throw new NotImplementedException("we have no place to add the flying over line to");
                    }
                }
            }
        }
    }

    private final AtomicBoolean insideRecalculation = new AtomicBoolean();

    public boolean doFlightplanCalculation() {
        // System.out.println("DO RECALC"+insideRecalculation);
        // Debug.printStackTrace();

        if (!insideRecalculation.compareAndSet(false, true)) {
            return true;
        }

        CollectsTypeVisitor<IRecalculateable> collectsTypeVisitor = new CollectsTypeVisitor<>(IRecalculateable.class);
        collectsTypeVisitor.startVisit(this);

        try {
            setMute(true);

            fastPositionTransformationProvider = new FastPositionTransformationProvider(Math.toRadians(refPoint.lon));

            for (int i = 0; i < 2; i++) {
                for (IRecalculateable calc : collectsTypeVisitor.matches) {
                    if (!calc.doSubRecalculationStage1()) {
                        break;
                    }
                }

                for (IRecalculateable calc : collectsTypeVisitor.matches) {
                    if (!calc.doSubRecalculationStage2()) {
                        break;
                    }
                }

                if (refPoint.isDefined()) {
                    // if origin isnt defined jet, we need another pass!
                    break;
                }
            }

            MinMaxPair flightAlt = new MinMaxPair();
            Boolean validSizeAOIs = true;
            for (IRecalculateable calc : collectsTypeVisitor.matches) {
                if (calc instanceof CPicArea) {
                    CPicArea picArea = (CPicArea)calc;
                    flightAlt.update(picArea.getAlt());
                    if (!((PicArea)picArea).validSizeAOI || !((PicArea)picArea).validAOI) {
                        validSizeAOIs = false;
                    }
                }
            }

            allAoisSizeValid = validSizeAOIs;
            checkCollisionsBetweenAOIConnections(collectsTypeVisitor);

            for (IRecalculateable calc : collectsTypeVisitor.matches) {
                if (calc instanceof CPicArea) {
                    CPicArea picArea = (CPicArea)calc;
                    picArea.setMute(true);
                    picArea.getCorners().setMute(true);
                }
            }

            if (getHardwareConfiguration().getPlatformDescription().isInCopterMode()) {
                FlightplanPostprocessor fpPostProcessor = new FlightplanPostprocessor(this);

                fpPostProcessor.setWaypointSpeedSettings();

                if (getHardwareConfiguration().getPlatformDescription().getInsertPhantomWaypoints()) {
                    fpPostProcessor.addPhantomWaypoints();
                }

                if (getHardwareConfiguration().getPrimaryPayload().getDescription().getEnforcePitchRange()) {
                    fpPostProcessor.enforcePitchRange();
                }

                if (getHardwareConfiguration()
                            .getPlatformDescription()
                            .getMinWaypointSeparation()
                            .convertTo(Unit.METER)
                            .getValue()
                            .doubleValue()
                        > 0.0) {
                    fpPostProcessor.removeCloseWaypoints();
                }
            }

            if (getEventList().isAutoComputingSafetyHeight()) {
                // set safety altitude
                if (photoSettings.getAltitudeAdjustMode().usesAbsoluteHeights()) {
                    Sector sector = getSector();
                    if (sector != null) {
                        getEventList()
                            .setAltWithinM(
                                elevationModel.getMaxElevation(sector).max
                                    + getHardwareConfiguration()
                                        .getPlatformDescription()
                                        .getMinGroundDistance()
                                        .convertTo(Unit.METER)
                                        .getValue()
                                        .doubleValue()
                                    + IElevationModel.TINY_GROUND_ELEVATION
                                    - getRefPointAltWgs84WithElevation());
                    }
                } else if (flightAlt.isValid()) {
                    getEventList().setAltWithinM(flightAlt.max);
                }
            }

            // cleanup ID mess
            ReassignIdsVisitor vis = new ReassignIdsVisitor(true);
            vis.startVisit(this); // this is silently unmuting the flight plan
            setMute(true); // otherwise the setMute(false) in the bottom wouldnt be effective..

        } finally {
            updateTimestamp = System.currentTimeMillis();
            for (IRecalculateable calc : collectsTypeVisitor.matches) {
                if (calc instanceof CPicArea) {
                    CPicArea picArea = (CPicArea)calc;
                    picArea.getCorners().setMute(false);
                    picArea.setMute(false);
                }
            }

            setMute(false);
            insideRecalculation.set(false);
        }

        return true;
    }
}
