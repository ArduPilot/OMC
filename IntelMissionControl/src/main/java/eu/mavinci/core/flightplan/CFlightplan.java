/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.flightplan;

import com.intel.missioncontrol.INotificationObject;
import com.intel.missioncontrol.hardware.IHardwareConfiguration;
import com.intel.missioncontrol.hardware.IHardwareConfigurationManager;
import com.intel.missioncontrol.helper.Ensure;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import eu.mavinci.core.desktop.listener.WeakListenerList;
import eu.mavinci.core.flightplan.visitors.DistanceVisitor;
import eu.mavinci.core.flightplan.visitors.ExtractByIdVisitor;
import eu.mavinci.core.flightplan.visitors.IFlightplanVisitor;
import eu.mavinci.core.flightplan.visitors.MaxIdVisitor;
import eu.mavinci.core.flightplan.visitors.NextWaypointVisitor;
import eu.mavinci.core.flightplan.visitors.PreviousWaypointVisitor;
import eu.mavinci.core.helper.StringHelper;
import eu.mavinci.core.helper.VectorNonEqual;
import eu.mavinci.core.plane.CAirplaneCache;
import eu.mavinci.core.plane.ICAirplane;
import eu.mavinci.desktop.main.debug.Debug;
import eu.mavinci.flightplan.LandingPoint;
import eu.mavinci.flightplan.Point;
import eu.mavinci.flightplan.ReferencePoint;
import eu.mavinci.flightplan.Takeoff;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.TreeMap;
import java.util.logging.Level;
import org.apache.commons.lang3.StringUtils;

public abstract class CFlightplan extends AFlightplanContainer
        implements IFlightplanContainer, Cloneable, IMuteable, IRecalculateable {

    private static IHardwareConfigurationManager hardwareConfigurationManager;

    protected static synchronized IHardwareConfigurationManager getHardwareConfigurationManager() {
        if (hardwareConfigurationManager == null) {
            hardwareConfigurationManager =
                DependencyInjector.getInstance().getInstanceOf(IHardwareConfigurationManager.class);
        }

        return hardwareConfigurationManager;
    }

    protected LandingPoint landingpoint;
    protected CPhotoSettings photoSettings;
    protected CEventList eventList;

    private final INotificationObject.ChangeListener hardwareChangeListener =
        event -> doFlightplanCalculationIfAutoRecalcIsActive();

    /**
     * Reference point of the flightplan "R". In the past was always a takeoff, now could be any point. Used for
     * calculating absolute heights of the elements using their relative heights to this point
     */

    // TODO maybe would make sense to store startAltWgs84 inside the origin ??
    // TODO disconnect it from the takeoff, connect to the new "R"
    protected ReferencePoint refPoint;

    // The takeoff is a position and elevation of the planned takeoff of the drone,
    // it should not be mixed with refPoint (which is used for referencing the heights in the flightplan )
    protected Takeoff takeoff;

    protected String basedOnTemplate;

    protected String name;

    protected String id;

    protected boolean recalculateOnEveryChange = true;

    protected String notes;

    protected final IHardwareConfiguration hardwareConfiguration;

    // first template is the default one for the polygon
    protected List<CPicArea> picAreaTemplates = new ArrayList<>();
    protected boolean isLearningmode = false;

    private File file;

    // absolute height of the origin. All the absolute heights of the elements inside the flightplan are equal to the
    // sum of startAltWgs84 and their setAltInMAboveFPRefPoint,
    // provided by IFlightplanPositionReferenced interface
    // protected double startAltWgs84 = 0;
    // protected double startGeoidSep = 0;

    /**
     * this is the assumed reference height for the ground
     *
     * @return
     */
    public double getRefPointAltWgs84WithElevation() {
        return refPoint.getAltitudeWgs84() + refPoint.getElevation();
    }

    public double getTakeofftAltWgs84WithElevation() {
        return takeoff.getAltitudeWgs84() + takeoff.getElevation();
    }

    public double getStartGeoidSep() {
        return refPoint.getGeoidSeparation();
    }

    /*public void setStartAlt(double startAlt, double startGeoidSep) {
        if (this.startAltWgs84 == startAlt && this.startGeoidSep == startGeoidSep) {
            return;
        }

        this.startAltWgs84 = startAlt;
        this.startGeoidSep = startGeoidSep;
        flightplanStatementChanged(this);
    }*/

    protected WeakListenerList<IFlightplanChangeListener> listeners =
        new WeakListenerList<IFlightplanChangeListener>("FlightplanListeners");

    protected boolean mute;

    public void setMute(boolean mute) {
        if (mute == this.mute) {
            return;
        }

        this.mute = mute;
        if (!mute) {
            flightplanStatementStructureChanged(this);
        }
    }

    protected boolean loadingDone;

    public void setSilentUnmute() {
        this.mute = false;
    }

    public boolean isMute() {
        return mute;
    }

    public boolean isLearningMode() {
        return isLearningmode;
    }

    public void setLearningmode(boolean isLearningmode) {
        if (isLearningmode == this.isLearningmode) {
            return;
        }

        this.isLearningmode = isLearningmode;
        flightplanStatementChanged(this);
    }

    protected boolean isEmpty = true;

    // changed in corparsion to saved version
    protected boolean isChanged = true;

    protected boolean enableJumpOverWaypoints;

    public boolean isChanged() {
        return isChanged;
    }

    public boolean isEmpty() {
        return isEmpty;
    }

    public void clear() {
        reset();
    }

    public CFlightplan() {
        this(getHardwareConfigurationManager().getImmutableDefault());
    }

    protected CFlightplan(IHardwareConfiguration hardwareConfiguration) {
        this.hardwareConfiguration = hardwareConfiguration;
        hardwareConfiguration.addListener(new INotificationObject.WeakChangeListener(hardwareChangeListener));
        refPoint = new ReferencePoint(this);
        refPoint.setIsAuto(true);
        takeoff = new Takeoff(this);
        landingpoint = new LandingPoint(this);
        photoSettings = FlightplanFactory.getFactory().newCPhotoSettings(this);
        eventList = FlightplanFactory.getFactory().newCEventList(this);
        landingpoint.reassignIDs();
        reset();
    }

    private boolean isOnAirFlightplan;

    public boolean isOnAirFlightplan() {
        return isOnAirFlightplan;
    }

    public void setIsOnAirFlightplan(boolean isOnAirFlightplan) {
        if (this.isOnAirFlightplan == isOnAirFlightplan) return;
        this.isOnAirFlightplan = isOnAirFlightplan;
        flightplanStatementChanged(this);
    }

    public boolean isOnAirRelatedLocalFlightplan(ICAirplane plane) {
        return plane.getFPmanager().onAirRelatedLocalFP == this;
    }

    public boolean isOnAirFlightplanOrRelatedLocalFlightplan(ICAirplane plane) {
        return isOnAirFlightplan() || isOnAirRelatedLocalFlightplan(plane);
    }

    public void addFPChangeListener(IFlightplanChangeListener listener) {
        listeners.add(listener);
    }

    public void removeFPChangeListener(IFlightplanChangeListener listener) {
        listeners.remove(listener);
    }

    public void fromXML(String input) {
        // System.out.println("input:"+input);
        FMLReader reader = new FMLReader();
        CFlightplan fp = FlightplanFactory.getFactory().newFlightplan();
        if (!input.equals("")) {
            InputStream is = new ByteArrayInputStream(input.getBytes());
            try {
                reader.readFML(fp, is, getHardwareConfigurationManager());
            } catch (Exception e) {
                Debug.getLog().log(Level.WARNING, "Flightplan XML reading Problems", e);
            }
        }

        // only overwrite this flightplan, if nessesary! -> so we still know if
        // it is the same file from harddisk
        if (!this.equals(fp)) {
            setMute(true);
            reset();

            this.elements = fp.elements;
            for (IFlightplanStatement elem : elements) {
                elem.setParent(this);
            }

            this.landingpoint = fp.landingpoint;
            this.landingpoint.setParent(this);

            this.refPoint = fp.refPoint;
            this.refPoint.setParent(this);

            this.takeoff = fp.takeoff;
            this.takeoff.setParent(this);

            this.basedOnTemplate = fp.basedOnTemplate;
            this.name = fp.name;
            this.id = fp.id;
            this.notes = fp.notes;
            this.recalculateOnEveryChange = fp.recalculateOnEveryChange;
            this.hardwareConfiguration.initializeFrom(fp.hardwareConfiguration);
            this.picAreaTemplates = fp.picAreaTemplates;

            this.isLearningmode = fp.isLearningmode;

            this.photoSettings = fp.photoSettings;
            this.photoSettings.setParent(this);

            this.eventList = fp.eventList;
            this.eventList.setParent(this);
            this.enableJumpOverWaypoints = fp.enableJumpOverWaypoints;

            setSilentUnmute();
            isEmpty = false;
            flightplanStatementStructureChanged(this);
        }
    }

    @Override
    public boolean isAddableToFlightplanContainer(Class<? extends IFlightplanStatement> cls) {
        if (SKIP_IS_ADDABLE_ASSERTION_CHECKS) return true;

        if (!super.isAddableToFlightplanContainer(cls)) {
            return false;
        }

        // System.out.println(!CFlightplan.class.isAssignableFrom(cls) + " "+ !CPoint.class.isAssignableFrom(cls) + " "+
        // !IHasStart.class.isAssignableFrom(cls));
        return !CFlightplan.class.isAssignableFrom(cls)
            && !Point.class.isAssignableFrom(cls)
            && !IHasStart.class.isAssignableFrom(cls);
    }

    @Override
    public boolean isAddableToFlightplanContainer(IFlightplanStatement statement) {
        if (SKIP_IS_ADDABLE_ASSERTION_CHECKS) return true;

        if (!super.isAddableToFlightplanContainer(statement)) {
            return false;
        }
        // if (statement instanceof CPreApproach){
        // for ( IFlightplanStatement tmp : this){
        // if (tmp instanceof CPreApproach) {
        // return false;
        // }
        // }
        // }
        return true;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getInternalName() {
        if (getFile() == null) {
            // System.out.println(System.currentTimeMillis() + " curFile:null");
            Debug.getLog().log(Level.SEVERE, "CFlightplan: no name");
            return "empty";
        }
        // System.out.println(System.currentTimeMillis() + " curFile:" +
        // file.toString());
        return getFile().getName();
    }

    protected void reset() {
        boolean wasMute = isMute();
        setMute(true);
        setFile(null);
        elements = new VectorNonEqual<IFlightplanStatement>();
        refPoint.setDefined(false);
        refPoint.setLatLon(0, 0);
        refPoint.setAltInMAboveFPRefPoint(0);
        refPoint.setHasAlt(false);
        refPoint.setHasYaw(false);
        refPoint.setYaw(0);
        refPoint.resetAltitudes();
        takeoff.setDefined(false);
        takeoff.setLatLon(0, 0);
        takeoff.setAltInMAboveFPRefPoint(0);
        takeoff.setHasAlt(false);
        takeoff.setHasYaw(false);
        takeoff.setYaw(0);
        takeoff.resetAltitudes();
        landingpoint.setLatLon(0, 0);
        isLearningmode = false;
        photoSettings.resetToDefaults();

        eventList.resetToDefaults();

        // endMode = FlightplanEndModes.DESCENDING;
        if (!wasMute) setSilentUnmute();
        isEmpty = true;
        flightplanStatementStructureChanged(this);
        // System.gc(); //remove elements from tree which are now dangeling
    }

    protected void saveAsDirectly(File file) {
        CFMLWriter writer = new CFMLWriter();
        try {
            writer.writeFlightplan(this, file);
        } catch (IOException e) {
            Debug.getLog().log(Level.WARNING, "Flightplan Save Problems", e);
        }

        isChanged = false;
        // CDebug.printStackTrace("set unchanged:",this);
        flightplanStatementChanged(this, false);
        setFile(file);
    }

    public LandingPoint getLandingpoint() {
        return landingpoint;
    }

    public CPhotoSettings getPhotoSettings() {
        return photoSettings;
    }

    public CEventList getEventList() {
        return eventList;
    }

    public String toXML() {
        CFMLWriter writer = new CFMLWriter();
        try {
            return writer.flightplanToXML(this, null);
        } catch (IOException e) {
            Debug.getLog().log(Level.WARNING, "Flightplan toXML conversion Problems", e);
            throw new IllegalStateException("Flightplan toXML conversion Problems", e);
        }
    }

    public String toXMLwithHash() {
        String hash = StringHelper.getHashXML(toXML());
        CFMLWriter writer = new CFMLWriter();
        try {
            return writer.flightplanToXML(this, hash);
        } catch (IOException e) {
            Debug.getLog().log(Level.WARNING, "Flightplan toXMLwithHash conversion Problems", e);
            throw new IllegalStateException("Flightplan toXMLwithHash conversion Problems: ", e);
        }
    }

    public String toString() {
        return (isChanged ? "*" : "")
            + "FP: "
            + getInternalName()
            + " ("
            + StringHelper.lengthToIngName(getLengthInMeterCached(), -3, false)
            + ")";
    }

    public int getUnusedId() {
        int max = getMaxUsedId();
        if (max >= ReentryPointID.maxValidID) {
            throw new FlightplanFullException();
        }

        return getMaxUsedId() + 1;
    }

    public int getMaxUsedId() {
        MaxIdVisitor vis = new MaxIdVisitor();
        vis.startVisit(this);
        return vis.max;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true; // shortcut
        }

        if (o == null) {
            return false;
        }

        if (o instanceof CFlightplan) {
            CFlightplan fp = (CFlightplan)o;
            // CDebug.printStackTrace();
            // System.out.println("fpElements-equals:"+elements.equals(fp.elements));
            // System.out.println("landingpoint-equals:"+landingpoint.equals(fp.landingpoint));
            // System.out.println("photoSettings-equals:"+photoSettings.equals(fp.photoSettings));
            return isLearningmode == fp.isLearningmode
                && elements.equals(fp.elements)
                && refPoint.equals(fp.refPoint)
                && landingpoint.equals(fp.landingpoint)
                && photoSettings.equals(fp.photoSettings)
                && eventList.equals(fp.eventList)
                && fp.hardwareConfiguration.equals(hardwareConfiguration)
                && picAreaTemplates.equals(fp.picAreaTemplates)
                && Objects.equals(file, fp.file)
                && Objects.equals(name, fp.getName())
                && Objects.equals(notes, fp.notes)
                && enableJumpOverWaypoints == fp.enableJumpOverWaypoints;
        }

        return false;
    }

    public CFlightplan getFlightplan() {
        return this;
    }

    public void flightplanStatementChanged(IFlightplanRelatedObject statement) {
        flightplanStatementChanged(statement, true);
    }

    public void flightplanStatementChanged(IFlightplanRelatedObject statement, boolean setChanged) {
        if (!loadingDone || mute) {
            return;
        }

        if (setChanged) {
            setChangedSilent();
        }

        isEmpty = false;

        if (statement instanceof CPhotoSettings
                || statement instanceof CEventList
                || statement instanceof LandingPoint
                || statement instanceof CPicArea) {
            if (doFlightplanCalculationIfAutoRecalcIsActive()) {
                return;
            }
        } else if (statement instanceof Point || statement instanceof CPicAreaCorners) {
            if (doFlightplanCalculationIfAutoRecalcIsActive()) {
                return;
            }
        }

        for (IFlightplanChangeListener listener : listeners) {
            if (listener == null) {
                continue;
            }

            listener.flightplanValuesChanged(statement);
        }
    }

    public void flightplanStatementStructureChanged(IFlightplanRelatedObject statement) {
        if (!loadingDone || mute) {
            return;
        }

        setChangedSilent();
        isEmpty = false;
        if (statement instanceof CPicAreaCorners) {
            if (doFlightplanCalculationIfAutoRecalcIsActive()) {
                return;
            }
        }

        for (IFlightplanChangeListener listener : listeners) {
            if (listener == null) {
                continue;
            }

            listener.flightplanStructureChanged(statement);
        }
    }

    public void flightplanStatementAdded(IFlightplanRelatedObject statement) {
        if (!loadingDone || mute) {
            return;
        }

        setChangedSilent();
        isEmpty = false;
        if (statement instanceof Point) {
            if (doFlightplanCalculationIfAutoRecalcIsActive()) {
                return;
            }
        } else if (statement.getParent() == this) {
            if (doFlightplanCalculationIfAutoRecalcIsActive()) {
                return;
            }
        }

        for (IFlightplanChangeListener listener : listeners) {
            if (listener == null) {
                continue;
            }

            listener.flightplanElementAdded(this, statement);
        }
    }

    public void flightplanStatementRemove(int i, IFlightplanRelatedObject statement) {
        if (!loadingDone || mute) {
            return;
        }

        setChangedSilent();
        isEmpty = false;
        if (statement instanceof Point) {
            if (doFlightplanCalculationIfAutoRecalcIsActive()) {
                return;
            }
        } else if (statement != null && statement.getParent() == this) {
            if (doFlightplanCalculationIfAutoRecalcIsActive()) {
                return;
            }
        }

        for (IFlightplanChangeListener listener : listeners) {
            if (listener == null) {
                continue;
            }

            listener.flightplanElementRemoved(this, i, statement);
        }
    }

    public ExtractByIdVisitor getStatementById(int id) {
        ExtractByIdVisitor vis = new ExtractByIdVisitor(id);
        vis.startVisit(this);
        return vis;
    }

    public void reassignIDs() {}

    public String getBasedOnTemplate() {
        return basedOnTemplate;
    }

    public void setBasedOnTemplate(String basedOnTemplate) {
        this.basedOnTemplate = basedOnTemplate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        if (StringUtils.equals(notes, this.notes)) {
            return;
        }

        this.notes = notes;
        flightplanStatementChanged(this);
    }

    public String getTitle() {
        return getInternalName();
    }

    public String getTooltip() {
        return "";
    }

    public static double getDistanceInMeter(IFlightplanPositionReferenced wp1, IFlightplanLatLonReferenced wp2) {
        return CAirplaneCache.distanceMeters(
            wp1.getLat(),
            wp1.getLon(),
            wp1.getAltInMAboveFPRefPoint(),
            wp2.getLat(),
            wp2.getLon(),
            wp1.getAltInMAboveFPRefPoint()); // its intentional to take both in the same altitude
    }

    public static double getDistanceInMeter(IFlightplanPositionReferenced wp1, IFlightplanPositionReferenced wp2) {
        return CAirplaneCache.distanceMeters(
            wp1.getLat(),
            wp1.getLon(),
            wp1.getAltInMAboveFPRefPoint(),
            wp2.getLat(),
            wp2.getLon(),
            wp2.getAltInMAboveFPRefPoint());
    }

    double lengthInMeter = -1;
    double timeInSec = -1;
    int picCount = -1;
    protected TreeMap<Integer, Double> progressMapDistance = null;
    protected TreeMap<Integer, IFlightplanPositionReferenced> posMap = null;

    public void resetDistanceVisitorCache() {
        synchronized (getLockingObject()) {
            lengthInMeter = -1;
            picCount = -1;
            timeInSec = -1;
            progressMapDistance = null;
            posMap = null;
        }
    }

    private void applyDistanceVisitor() {
        DistanceVisitor vis = new DistanceVisitor(getHardwareConfiguration().getPlatformDescription());
        vis.startVisit(this);
        lengthInMeter = vis.distance;
        picCount = vis.picCount;
        timeInSec = vis.timeInSec;
        progressMapDistance = vis.progressMapDistance;
        posMap = vis.posMap;
    }

    public double getLengthInMeterCached() {
        return lengthInMeter;
    }

    public double getTimeInSecCached() {
        return timeInSec;
    }

    public int getPicCountCached() {
        return picCount;
    }

    public TreeMap<Integer, Double> getProgressMapDistance() {
        synchronized (getLockingObject()) {
            if (progressMapDistance == null) {
                applyDistanceVisitor();
            }

            return progressMapDistance;
        }
    }

    public double getLengthInMeter() {
        synchronized (getLockingObject()) {
            if (lengthInMeter < 0) {
                applyDistanceVisitor();
            }

            return lengthInMeter;
        }
    }

    public int getPicCount() {
        synchronized (getLockingObject()) {
            if (picCount < 0) {
                applyDistanceVisitor();
            }

            return picCount;
        }
    }

    public double getTimeInSec() {
        synchronized (getLockingObject()) {
            if (timeInSec < 0) {
                applyDistanceVisitor();
            }

            return timeInSec;
        }
    }

    public double getProgressCachedInM(int reentrypointID, double lat, double lon, double alt) {
        TreeMap<Integer, IFlightplanPositionReferenced> posMapLocal = null;
        TreeMap<Integer, Double> progressMapDistanceLocal = null;
        synchronized (getLockingObject()) {
            posMapLocal = posMap;
            progressMapDistanceLocal = progressMapDistance;
        }

        if (posMapLocal == null || progressMapDistanceLocal == null) {
            return -1;
        }

        Double prog = progressMapDistanceLocal.get(reentrypointID);
        if (prog != null) {
            IFlightplanPositionReferenced pos = posMapLocal.get(reentrypointID);
            // System.out.println();
            // System.out.println("prog before change:"+prog);
            // System.out.println("pos="+ pos + " curLat="+lat+" lon="+lon + " alt="+alt + "
            // reentryID="+reentrypointID);
            if (pos != null) {
                prog -=
                    CAirplaneCache.distanceMeters(
                        pos.getLat(), pos.getLon(), pos.getAltInMAboveFPRefPoint(), lat, lon, alt);
                if (prog < 0) {
                    prog = 0d;
                }
            }
            // System.out.println("newprog="+prog);
            return prog;
        } else {
            // System.out.println("prog=null");
            return -2;
        }
    }

    /**
     * getting next CWaypoint from the position of fpStatement in the flightplan if fpStatement is allready a CWaypoint,
     * itself will NOT be returned, but the next one
     *
     * @param fpStatement
     * @return
     */
    public CWaypoint getNextWaypoint(IFlightplanStatement fpStatement) {
        NextWaypointVisitor visitor = new NextWaypointVisitor(fpStatement);
        visitor.setSkipIgnoredPaths(true);
        visitor.startVisit(this);
        return visitor.nextWaypoint;
    }

    /**
     * getting previous CWaypoint from the position of fpStatement in the flightplan if fpStatement is allready a
     * CWaypoint, ITSELF will be returned, but the next one note: this behaviour is needed for geting the coordinates
     * where e.g. a CPhototag will be activated.
     *
     * @param fpStatement
     * @return
     */
    public CWaypoint getApplyingWaypoint(IFlightplanStatement fpStatement) {
        if (fpStatement instanceof CWaypoint) {
            CWaypoint wp = (CWaypoint)fpStatement;
            return wp;
        }

        PreviousWaypointVisitor visitor = new PreviousWaypointVisitor(fpStatement);
        visitor.setSkipIgnoredPaths(true);
        visitor.startVisit(this);
        return visitor.prevWaypoint;
    }

    /**
     * getting previous CWaypoint from the position of fpStatement in the flightplan if fpStatement is allready a
     * CWaypoint, itself will NOT be returned, but the previous
     *
     * @param fpStatement
     * @return
     */
    public CWaypoint getPreviousWaypoint(IFlightplanStatement fpStatement) {
        PreviousWaypointVisitor visitor = new PreviousWaypointVisitor(fpStatement);
        visitor.setSkipIgnoredPaths(true);
        visitor.startVisit(this);
        return visitor.prevWaypoint;
    }

    @Override
    public boolean applyVisitorAdditionalMembers(IFlightplanVisitor visitor) {
        if (visitor.visit(photoSettings)) {
            return true;
        }

        if (visitor.visit(refPoint)) {
            return true;
        }

        if (visitor.visit(takeoff)) {
            return true;
        }

        if (visitor.visit(landingpoint)) {
            return true;
        }

        return visitor.visit(eventList);
    }

    protected boolean setChanged() {
        if (setChangedSilent()) {
            flightplanStatementChanged(this);
            return true;
        }

        return false;
    }

    protected boolean setChangedSilent() {
        synchronized (getLockingObject()) {
            resetDistanceVisitorCache();
            if (!isChanged) {
                isChanged = true;
                return true;
            }
        }

        return false;
    }

    public void setUnchanged() {
        isChanged = false;
        flightplanStatementChanged(this, false);
    }

    public IFlightplanContainer getParent() {
        return null;
    }

    public void setParent(IFlightplanContainer container) {}

    public Object getLockingObject() {
        return this;
    }

    public ReferencePoint getRefPoint() {
        return refPoint;
    }

    public Position getRefPointPosition() {
        return refPoint.getPosition();
    }

    public void setRefPointPosition(Position p) {
        refPoint.setPosition(p);
    }

    public Position getTakeoffPosition() {
        return takeoff.getPosition();
    }

    public void setTakeoffPosition(LatLon p) {
        takeoff.setPosition(p);
    }

    public Position getLandingPosition() {
        return landingpoint.getPosition();
    }

    public void setLandingPosition(Position p) {
        landingpoint.setLatLon(p);
        landingpoint.setAltInMAboveFPRefPoint(p.getAltitude());
    }

    public IHardwareConfiguration getHardwareConfiguration() {
        return hardwareConfiguration;
    }

    public void updateHardwareConfiguration(IHardwareConfiguration hardwareConfiguration) {
        this.hardwareConfiguration.initializeFrom(hardwareConfiguration);
    }

    public CPicArea getPicAreaTemplate(PlanType type) {
        for (CPicArea picArea : picAreaTemplates) {
            if (picArea.getPlanType().equals(type)) {
                return picArea;
            }
        }
        // the default one - polygon
        if (picAreaTemplates.size() == 0) {
            return null;
        } else {
            // copy from default template, and then change the type...
            // this is bettern then not able to create the new type at all
            CPicArea picArea = picAreaTemplates.get(0);
            CPicArea picAreaNew = (CPicArea)picArea.getCopy();
            picAreaNew.setMute(true);
            picAreaNew.setParent(picArea.getParent());
            picAreaNew.setPlanType(type);
            picAreaNew.setSilentUnmute();
            picAreaTemplates.add(picAreaNew);
            return picAreaNew;
        }
    }

    public void updatePicAreaTemplate(PlanType type, CPicArea picArea) {
        CPicArea oldPicArea = getPicAreaTemplate(type);
        int index = picAreaTemplates.indexOf(oldPicArea);
        if (index == -1) {
            picAreaTemplates.add(picArea);
        } else {
            picAreaTemplates.remove(index);
            picAreaTemplates.add(index, picArea);
        }
    }

    /**
     * Clones the appropriate picArea template, adds it to the flightplan (maybe not successfully) and returns this
     * newly created object
     */
    public CPicArea addNewPicArea(PlanType type) {
        Ensure.notNull(type, "type");
        CPicArea templatePicArea = getPicAreaTemplate(type);
        Ensure.notNull(templatePicArea, "templatePicArea");
        CPicArea newPicArea = (CPicArea)templatePicArea.getCopy();
        try {
            // since templates sometimes contain wrong altitude update it, GSD is the guiding value
            newPicArea.setGsd(templatePicArea.getGsd());
            this.addToFlightplanContainer(newPicArea);
        } catch (FlightplanContainerFullException e) {
            throw new RuntimeException("no space left in flight plan to add another AOI", e);
        } catch (FlightplanContainerWrongAddingException e) {
            throw new RuntimeException("cant add AOI to flight plan because of wrong type", e);
        }

        return newPicArea;
    }

    public boolean getRecalculateOnEveryChange() {
        return recalculateOnEveryChange;
    }

    public void setRecalculateOnEveryChange(boolean recalculateOnEveryChange) {
        if (this.recalculateOnEveryChange == recalculateOnEveryChange) {
            return;
        }

        this.recalculateOnEveryChange = recalculateOnEveryChange;
        if (!doFlightplanCalculationIfAutoRecalcIsActive()) {
            flightplanStatementChanged(this);
        }
    }

    public List<CPicArea> getPicAreaTemplates() {
        return picAreaTemplates;
    }

    protected boolean muteAutoRecalc;

    public boolean willAutoRecalc() {
        return !mute && !muteAutoRecalc && getRecalculateOnEveryChange();
    }

    public boolean setMuteAutoRecalc(boolean mute) {
        muteAutoRecalc = mute;
        return doFlightplanCalculationIfAutoRecalcIsActive();
    }

    public boolean isMuteAutoRecalc() {
        return muteAutoRecalc;
    }

    /**
     * in case auto flightplan computation is currently enabled, this method will start it.
     *
     * @return true if a change event was thrown
     */
    public boolean doFlightplanCalculationIfAutoRecalcIsActive() {
        changeTimestamp = System.currentTimeMillis();
        if (willAutoRecalc()) {
            doFlightplanCalculation();
            return true;
        }

        return false;
    }

    public boolean doFlightplanCalculation() {
        // gets implemented in derived class
        return false;
    }

    protected long updateTimestamp;

    public long getUpdateTimestamp() {
        return updateTimestamp;
    }

    protected long changeTimestamp;

    public long getChangeTimestamp() {
        return changeTimestamp;
    }

    public Takeoff getTakeoff() {
        return takeoff;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
