/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.planemain.tagging;

import com.google.common.base.Strings;
import eu.mavinci.core.flightplan.CPhotoLogLine;
import eu.mavinci.core.flightplan.CPicArea;
import eu.mavinci.core.flightplan.GPSFixType;
import eu.mavinci.core.flightplan.GpsFixTypeCounter;
import eu.mavinci.core.helper.MinMaxPair;
import eu.mavinci.desktop.cir.Histogram;
import eu.mavinci.desktop.gui.doublepanel.mapmanager.IResourceFileReferenced;
import eu.mavinci.desktop.gui.doublepanel.planemain.tree.maplayers.IMapLayer;
import eu.mavinci.desktop.gui.doublepanel.planemain.tree.maplayers.MapLayerSectorReferenced;
import eu.mavinci.desktop.gui.doublepanel.planemain.wwd.AerialPinholeImage;
import eu.mavinci.desktop.gui.doublepanel.planemain.wwd.ComputeCornerData;
import eu.mavinci.desktop.helper.IRecomputeRunnable;
import eu.mavinci.desktop.helper.MFileFilter;
import eu.mavinci.desktop.helper.MathHelper;
import eu.mavinci.desktop.helper.Recomputer;
import eu.mavinci.desktop.main.debug.Debug;
import gov.nasa.worldwind.geom.LatLon;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import org.xml.sax.SAXException;

public abstract class AMapLayerMatching extends MapLayerSectorReferenced
        implements IResourceFileReferenced, IMatchingRelated {

    File matchingFolder;

    private static final double STARTING_POS_PICTURES_ALT_THRESHOLD = 4;

    public static boolean comparisonContractViolationFound = false;

    public static final String KEY = "eu.mavinci.desktop.gui.doublepanel.planemain.tagging.MapLayerMatching";
    public static final String DEFAULT_FILENAME = "dataset." + MFileFilter.ptgFilter.getExtension();

    public static final String FOLDER_NAME_PICS_SUBFOLDER = "Images";
    public static final String FOLDER_NAME_FLIGHTPLANS_SUBFOLDER = "Flight Plans";

    public static final double MAX_PIXEL_FUXINESS = 20; // °/s

    protected abstract void load() throws IOException, SAXException, Exception;

    MapLayerCoverageMatching cover;
    MapLayerPics pics;

    boolean createNew;

    int currentBandNo = 0;

    protected double maxPixelFuzzyness = 2;

    protected boolean altitudeAGLEnabled = false;
    protected boolean rollEnabled = false;
    protected boolean pitchEnabled = false;
    protected boolean yawEnabled = false;
    protected double altitudeValue = 0;
    protected double rollValue = 0;
    protected double pitchValue = 0;
    protected double yawValue = 0;
    protected double altitudeSpread = 0;
    protected double rollSpread = 0;
    protected double pitchSpread = 0;
    protected double yawSpread = 0;

    protected boolean onlyInPicArea = true;
    protected boolean useAll = false;

    private ProjectionType projectionType = ProjectionType.INSPECTIONS_3D;

    private double projectionDistance = 5;

    private double elevationOffset;

    private boolean confirmAsCorrect;

    private LocationType locationType = LocationType.ASSUMED;

    private int countFiltered;
    private long totalSizeFilteredBytes;
    private String filteredFileType = "";
    private final GpsFixTypeCounter gpsFixTypeCounter = new GpsFixTypeCounter();

    boolean elevationDataAvaliable = true; // this value shuld be threadsafe
    private boolean elevationDataAvaliableTmp = true; // is only used for storing a non ready value

    private boolean isChanged = false;
    private Double estimateStaringElevationInM = null;

    private IRecomputeRunnable recompRunFiltering =
        new IRecomputeRunnable() {

            boolean filterResultChanged;

            boolean notFirstRun;

            @Override
            public void run() {
                long start = System.currentTimeMillis();
                try {
                    elevationDataAvaliableTmp = true;
                    for (IMapLayer subLayer : getPicsLayer().getLayers()) {
                        if (subLayer instanceof MapLayerMatch) {
                            MapLayerMatch match = (MapLayerMatch)subLayer;
                            if (applyFilter(match)) {
                                filterResultChanged = true;
                            }
                        }
                    }

                    elevationDataAvaliable = elevationDataAvaliableTmp;
                    if (!notFirstRun) {
                        filterResultChanged = true;
                        notFirstRun = true;
                    }

                    if (!filterResultChanged) {
                        return;
                    }

                    gpsFixTypeCounter.clear();
                    countFiltered = 0;
                    totalSizeFilteredBytes = 0L;
                    filteredFileType = "";

                    for (IMapLayer subLayer : getPicsLayer().getLayers()) {
                        if (subLayer instanceof MapLayerMatch) {
                            MapLayerMatch match = (MapLayerMatch)subLayer;
                            if (!match.isPassFilter) {
                                continue;
                            }

                            countFiltered++;
                            GPSFixType fix = match.getPhotoLogLine().fixType;
                            gpsFixTypeCounter.increment(fix);

                            totalSizeFilteredBytes += match.getResourceFileSizeBytes();

                            if (Strings.isNullOrEmpty(filteredFileType)) {
                                filteredFileType = match.getResourceFileType();
                            }
                        }
                    }

                    cover.recomputeCoverage();
                    getPicsLayer().getWWLayer().resetVisibility();
                } finally {
                    Debug.getLog()
                        .info(
                            "Image filtering recalc done. "
                                + countFiltered
                                + "/"
                                + getPicsLayer().getLayers().size()
                                + "   "
                                + AMapLayerMatching.this
                                + ". It took "
                                + (System.currentTimeMillis() - start) / 1000.
                                + " sec");
                }
            }

            @Override
            public void runLaterOnUIThread() {
                if (filterResultChanged) {
                    mapLayerValuesChanged(AMapLayerMatching.this);
                }
            }
        };

    private Recomputer recomp = new Recomputer(recompRunFiltering);

    public AMapLayerMatching(File matchingFolder, boolean createNew) {
        super(true);
        this.matchingFolder = matchingFolder;
        this.createNew = createNew;

        cover = new MapLayerCoverageMatching(this);
        addMapLayer(cover);

        pics = new MapLayerPics(this, false);
        addMapLayer(pics);
    }

    protected void postConstruction() throws Exception {
        recompute();
        if (pics.isShowingImages()) {
            pics.generatePreview();
        }
    }

    public MapLayerCoverageMatching getCoverage() {
        return cover;
    }

    public abstract int getNumberOfImagesPerPosition();

    public abstract String[] getBandNames();

    public int getCurrentBandNo() {
        return currentBandNo;
    }

    public void setCurrentBandNo(int currentBandNo) {
        currentBandNo = MathHelper.intoRange(currentBandNo, 0, getNumberOfImagesPerPosition());
        if (this.currentBandNo == currentBandNo) {
            return;
        }

        this.currentBandNo = currentBandNo;
        mapLayerValuesChanged(this);
        pics.layer.fireImageLayerChanged(); // different preview image content
        setChanged(true);
    }

    @Override
    public void dispose() {
        recompRunFiltering = null;
        super.dispose();
    }

    public ProjectionType getProjectionType() {
        return projectionType;
    }

    public void setProjectionType(ProjectionType projectionType) {
        if (this.projectionType == projectionType) {
            return;
        }

        this.projectionType = projectionType;
        mapLayerValuesChanged(this);
        pics.layer.fireImageLayerChanged();
        cover
            .recomputeCoverage(); // .mapLayerVisibilityChanged(cover,cover.isVisibleIncludingParent()); //it's implicit
        // visibility changes since its hidden in 2d preview mode
        setChanged(true);
    }

    public double getProjectionDistance() {
        return projectionDistance;
    }

    public void setProjectionDistance(double projectionDistance) {
        if (projectionDistance < 0.1) projectionDistance = .1;
        if (projectionDistance > 100) projectionDistance = 100;
        if (this.projectionDistance == projectionDistance) return;
        this.projectionDistance = projectionDistance;
        mapLayerValuesChanged(this);
        if (!getPicsLayer().layer.isMute()) {
            getCoverage().updateCameraCorners();
        }

        setChanged(true);
    }

    public boolean isConfirmAsCorrect() {
        return confirmAsCorrect;
    }

    public void setConfirmAsCorrect(boolean confirmAsCorrect) {
        if (this.confirmAsCorrect == confirmAsCorrect) return;
        this.confirmAsCorrect = confirmAsCorrect;
        mapLayerValuesChanged(this);
        setChanged(true);
    }

    public LocationType getLocationType() {
        return locationType;
    }

    public void setLocationType(LocationType locationType) {
        if (this.locationType == locationType) return;
        this.locationType = locationType;
        mapLayerValuesChanged(this);
        setChanged(true);
    }

    public boolean isUsingAll() {
        return useAll;
    }

    public void setUseAll(boolean useAll) {
        // Debug.printStackTrace(this.useAll, useAll);
        if (this.useAll == useAll) {
            return;
        }

        this.useAll = useAll;
        filterChanged();
    }

    public double getMaxPixelFuzzyness() {
        return maxPixelFuzzyness;
    }

    public void setMaxPixelFuzzyness(double maxPixelFuzzyness) {
        // System.out.println("maxFuzz old: "+ this.maxPixelFuzzyness + " new:"+maxPixelFuzzyness + " at:"+this);
        maxPixelFuzzyness = Math.min(maxPixelFuzzyness, MAX_PIXEL_FUXINESS);
        if (maxPixelFuzzyness < 0) {
            maxPixelFuzzyness = 0;
        }

        if (this.maxPixelFuzzyness != maxPixelFuzzyness) {
            this.maxPixelFuzzyness = maxPixelFuzzyness;
            filterChanged();
        }
    }

    public boolean getAltitudeAGLEnabled() {
        return altitudeAGLEnabled;
    }

    public void setAltitudeAGLEnabled(boolean altitudeAGLEnabled) {
        if (this.altitudeAGLEnabled != altitudeAGLEnabled) {
            this.altitudeAGLEnabled = altitudeAGLEnabled;
            filterChanged();
        }
    }

    public boolean getRollEnabled() {
        return rollEnabled;
    }

    public void setRollEnabled(boolean rollEnabled) {
        if (this.rollEnabled != rollEnabled) {
            this.rollEnabled = rollEnabled;
            filterChanged();
        }
    }

    public boolean getPitchEnabled() {
        return pitchEnabled;
    }

    public void setPitchEnabled(boolean pitchEnabled) {
        if (this.pitchEnabled != pitchEnabled) {
            this.pitchEnabled = pitchEnabled;
            filterChanged();
        }
    }

    public boolean getYawEnabled() {
        return yawEnabled;
    }

    public void setYawEnabled(boolean yawEnabled) {
        if (this.yawEnabled != yawEnabled) {
            this.yawEnabled = yawEnabled;
            filterChanged();
        }
    }

    public double getAltitudeValue() {
        return altitudeValue;
    }

    public void setAltitudeValue(double altitudeValue) {
        if (this.altitudeValue != altitudeValue) {
            this.altitudeValue = altitudeValue;
            filterChanged();
        }
    }

    public double getRollValue() {
        return rollValue;
    }

    public void setRollValue(double rollValue) {
        if (this.rollValue != rollValue) {
            this.rollValue = rollValue;
            filterChanged();
        }
    }

    public double getPitchValue() {
        return pitchValue;
    }

    public void setPitchValue(double pitchValue) {
        if (this.pitchValue != pitchValue) {
            this.pitchValue = pitchValue;
            filterChanged();
        }
    }

    public double getYawValue() {
        return yawValue;
    }

    public void setYawValue(double yawValue) {
        if (this.yawValue != yawValue) {
            this.yawValue = yawValue;
            filterChanged();
        }
    }

    public double getAltitudeSpread() {
        return altitudeSpread;
    }

    public void setAltitudeSpread(double altitudeSpread) {
        if (this.altitudeSpread != altitudeSpread) {
            this.altitudeSpread = altitudeSpread;
            filterChanged();
        }
    }

    public double getRollSpread() {
        return rollSpread;
    }

    public void setRollSpread(double rollSpread) {
        if (this.rollSpread != rollSpread) {
            this.rollSpread = rollSpread;
            filterChanged();
        }
    }

    public double getPitchSpread() {
        return pitchSpread;
    }

    public void setPitchSpread(double pitchSpread) {
        if (this.pitchSpread != pitchSpread) {
            this.pitchSpread = pitchSpread;
            filterChanged();
        }
    }

    public double getYawSpread() {
        return yawSpread;
    }

    public void setYawSpread(double yawSpread) {
        if (this.yawSpread != yawSpread) {
            this.yawSpread = yawSpread;
            filterChanged();
        }
    }

    public boolean isOnlyInPicArea() {
        return onlyInPicArea;
    }

    public void setOnlyInPicArea(boolean onlyInPicArea) {
        if (this.onlyInPicArea != onlyInPicArea) {
            this.onlyInPicArea = onlyInPicArea;
            filterChanged();
        }
    }

    public int getCountFiltered() {
        return countFiltered;
    }

    public long getTotalSizeFilteredBytes() {
        return totalSizeFilteredBytes;
    }

    public String getFilteredFileType() {
        return filteredFileType;
    }

    public long getCountFilteredRTK() {
        return getGpsFixTypeCount(GPSFixType.rtkFixedBL);
    }

    public long getGpsFixTypeCount(GPSFixType type) {
        return gpsFixTypeCounter.getCount(type);
    }

    public void guessGoodFilters() {
        MinMaxPair minMaxAlt = new MinMaxPair();
        int cnt = 0;
        double xYaw = 0;
        double yYaw = 0;
        double xRoll = 0;
        double yRoll = 0;
        double xPitch = 0;
        double yPitch = 0;
        final double altStep = 0.5;
        final double degStep = 1;
        final double cutPercentil = 0.05;

        for (IMapLayer layer : getPictures()) {
            if (layer instanceof MapLayerMatch) {
                MapLayerMatch match = (MapLayerMatch)layer;
                CPhotoLogLine line = match.getPhotoLogLine();
                ComputeCornerData computeCornerData = match.img.getComputeCornerData();
                double alt =
                    (computeCornerData != null && computeCornerData.getAltOverCenter() != null)
                        ? computeCornerData.getAltOverCenter()
                        : match.line.getAltInM();
                minMaxAlt.update(alt);
                cnt++;
                xYaw += Math.sin(Math.toRadians(match.line.cameraYaw));
                yYaw += Math.cos(Math.toRadians(match.line.cameraYaw));
                xRoll += Math.sin(Math.toRadians(match.line.cameraRoll));
                yRoll += Math.cos(Math.toRadians(match.line.cameraRoll));
                xPitch += Math.sin(Math.toRadians(match.line.cameraPitch));
                yPitch += Math.cos(Math.toRadians(match.line.cameraPitch));
            }
        }

        double yawAvg = Math.toDegrees(Math.atan2(xYaw, yYaw));
        double rollAvg = Math.toDegrees(Math.atan2(xRoll, yRoll));
        double pitchAvg = Math.toDegrees(Math.atan2(xPitch, yPitch));

        Histogram histRoll = new Histogram(-180 - degStep / 2, 180 + degStep / 2, degStep, false);
        Histogram histPitch = new Histogram(-180 - degStep / 2, 180 + degStep / 2, degStep, false);
        Histogram histYaw = new Histogram(-180 - degStep / 2, 180 + degStep / 2, degStep, false);
        Histogram histAlt = new Histogram(minMaxAlt.min - altStep / 2, minMaxAlt.max + altStep / 2, altStep, false);

        for (IMapLayer layer : getPictures()) {
            if (layer instanceof MapLayerMatch) {
                MapLayerMatch match = (MapLayerMatch)layer;
                CPhotoLogLine line = match.getPhotoLogLine();
                ComputeCornerData computeCornerData = match.img.getComputeCornerData();
                double alt =
                    (computeCornerData != null && computeCornerData.getAltOverCenter() != null)
                        ? computeCornerData.getAltOverCenter()
                        : match.line.getAltInM();
                histAlt.count(alt);

                double yaw = line.cameraYaw - yawAvg;
                while (yaw < 180) {
                    yaw += 360;
                }

                while (yaw >= 180) {
                    yaw -= 360;
                }

                histYaw.count(yaw);
                double roll = line.cameraRoll - rollAvg;
                while (roll < 180) {
                    roll += 360;
                }

                while (roll >= 180) {
                    roll -= 360;
                }

                histRoll.count(roll);
                double pitch = line.cameraPitch - pitchAvg;
                while (pitch < 180) {
                    pitch += 360;
                }

                while (pitch >= 180) {
                    pitch -= 360;
                }

                histPitch.count(pitch);
            }
        }

        altitudeAGLEnabled = false;
        altitudeValue = histAlt.getCenter(histAlt.getPercentileBin(0.5));
        altitudeSpread =
            degStep
                + Math.max(
                    altitudeValue - histAlt.getCenter(histAlt.getPercentileBin(cutPercentil)),
                    histAlt.getCenter(histAlt.getPercentileBin(1 - cutPercentil)) - altitudeValue);

        rollEnabled = false;
        rollValue = histRoll.getCenter(histRoll.getPercentileBin(0.5));
        rollSpread =
            degStep
                + Math.max(
                    rollValue - histRoll.getCenter(histRoll.getPercentileBin(cutPercentil)),
                    histRoll.getCenter(histRoll.getPercentileBin(1 - cutPercentil)) - rollValue);
        rollValue += rollAvg;
        while (rollValue < -180) {
            rollValue += 360;
        }

        while (rollValue >= 180) {
            rollValue -= 360;
        }

        pitchEnabled = false;
        pitchValue = histPitch.getCenter(histPitch.getPercentileBin(0.5));
        pitchSpread =
            degStep
                + Math.max(
                    pitchValue - histPitch.getCenter(histPitch.getPercentileBin(cutPercentil)),
                    histPitch.getCenter(histPitch.getPercentileBin(1 - cutPercentil)) - pitchValue);
        pitchValue += pitchAvg;
        while (pitchValue < -180) {
            pitchValue += 360;
        }

        while (pitchValue >= 180) {
            pitchValue -= 360;
        }

        yawEnabled = false;
        yawValue = histYaw.getCenter(histYaw.getPercentileBin(0.5));
        yawSpread =
            degStep
                + Math.max(
                    yawValue - histYaw.getCenter(histYaw.getPercentileBin(cutPercentil)),
                    histYaw.getCenter(histYaw.getPercentileBin(1 - cutPercentil)) - yawValue);
        yawValue += yawAvg;

        while (yawValue < 0) {
            yawValue += 360;
        }

        while (yawValue >= 360) {
            yawValue -= 360;
        }

        setProjectionType(
            Math.abs(pitchValue) + pitchSpread > 50 ? ProjectionType.INSPECTIONS_3D : ProjectionType.SURVEYS_2D);

        // disable filter, if no picarea is avaliable, to see at least something
        setOnlyInPicArea(getProjectionType() == ProjectionType.SURVEYS_2D && !getPicAreas().isEmpty());

        filterChanged();
    }

    void filterChanged() {
        setChanged(true);
        // System.out.println("trigger all changes");
        recompute();
    }

    public void recompute() {
        if (recompRunFiltering == null) {
            return;
        }
        // Debug.printStackTrace(this);
        recomp.tryStartRecomp();
        // do the preview recomputation AFTER the filter changes are applied to the matches!!
        // if (!isMute()) {
        // getPicsLayer().getWWLayer().resetVisibility();
        // }
    }

    /**
     * Applys all LOCAL filters to a match so dontPerforateLines has to be applied in a second run
     *
     * @param match
     * @return if filter value was changed
     */
    private boolean applyFilter(MapLayerMatch match) {
        if (useAll) {
            match.setPassFilter(true);
            return true;
        }

        AerialPinholeImage img = match.img;

        if (img == null) {
            return true;
        }

        ComputeCornerData computeCornerData = img.getComputeCornerData();

        /*if (GlobalSettings.userLevel == GuiLevels.DEBUG) {
            FuzzinessData fuzzinessData = match.getFuzzinessBlocking();
            if (fuzzinessData != null && fuzzinessData.getInPixel() == -1) {
                elevationDataAvaliableTmp = false; // maybe I could remove this line later?
            }
        }*/

        if (computeCornerData == null || !computeCornerData.isElevationDataReady()) {
            elevationDataAvaliableTmp = false;
        }

        double alt =
            (computeCornerData != null && computeCornerData.getAltOverCenter() != null)
                ? computeCornerData.getAltOverCenter()
                : match.line.getAltInM();
        // System.out.println(match.getResourceFile() + " -> "+alt + " - " + match.line.alt/100f);

        boolean isPass =
            // (GlobalSettings.userLevel != GuiLevels.DEBUG || match.getFuzzyness().inPixel <= maxPixelFuzzyness) &&
            // && (!onlyMainLines || match.line.isOnMainLine()) && (!onlySingleDirection || match.line.isForwardLine()
            Math.abs(match.line.lat) > 1E-300 // falcon without valid GPS
                && isAltAGLPass(alt)
                && isRollPass(match)
                && isPitchPass(match)
                && isYawPass(match);
        if (isPass && onlyInPicArea && computeCornerData != null && !getVisiblePicAreas().isEmpty()) {
            isPass = false;
            for (MapLayerPicArea picArea : getVisiblePicAreas()) {
                if (picArea.intersectsWith(
                        computeCornerData.getGroundProjectedCorners(), computeCornerData.getSector())) {
                    isPass = true;
                    break;
                }
            }
        }

        // System.out.println("apply Filter on " + match + " pass="+isPass);
        return match.setPassFilter(isPass);
    }

    public abstract List<MapLayerPicArea> getPicAreas();

    public abstract String getName();

    public abstract File getMatchingFolder();

    public abstract List<MapLayerPicArea> getVisiblePicAreas();

    public boolean isChanged() {
        return isChanged;
    }

    public synchronized void setChanged(boolean isChanged) {
        if (isChanged != this.isChanged) {
            // if (isChanged) Debug.printStackTrace(isChanged);
            this.isChanged = isChanged;
            mapLayerValuesChanged(this);
        }
    }

    public void generatePreview() {
        getPicsLayer().generatePreview();
    }

    public List<IMapLayer> getPictures() {
        return getPicsLayer().getLayers();
    }

    public int getPicturesCount() {
        List<IMapLayer> pictures = getPictures();

        if (pictures == null) {
            return 0;
        }

        return pictures.size();
    }

    @Override
    public AMapLayerMatching getMatching() {
        return this;
    }

    synchronized void resetEstimatedStartingElevation() {
        estimateStaringElevationInM = null;
    }

    public synchronized double getEstimatedStartingElevationInMoverWGS84() {
        return getEstimatedStartingElevationInMoverWGS84(false);
    }

    public synchronized double getEstimatedStartingElevationInMoverWGS84(boolean addEGMOffsetForExport) {
        if (estimateStaringElevationInM == null) {
            try {
                // compute mean gps alt of all staring points
                double startGpsAlt = 0;
                int countAlt = 0;
                for (IMapLayer layer : getPictures()) {
                    if (layer instanceof MapLayerMatch) {
                        MapLayerMatch match = (MapLayerMatch)layer;
                        CPhotoLogLine line = match.getPhotoLogLine();
                        // Position p = c.shiftPosition(line);
                        startGpsAlt += line.gps_altitude_cm + line.gps_ellipsoid_cm - line.alt;
                        if (addEGMOffsetForExport) {
                            startGpsAlt += match.getMatching().getGeoidOffset();
                        }

                        countAlt++;
                    }
                }

                if (countAlt == 0) {
                    throw new Exception("No images found to export in selected matchings!");
                }

                startGpsAlt /= countAlt;
                startGpsAlt /= 100; // cm -> m
                estimateStaringElevationInM = startGpsAlt;

            } catch (Exception e) {
                Debug.getLog().log(Level.CONFIG, "could not estimate Start Elev", e);
                estimateStaringElevationInM = null;
            }

            if (estimateStaringElevationInM == null) {
                return 0;
            }
        }

        return estimateStaringElevationInM;
    }

    public synchronized LatLon getStartingPosition() {
        for (IMapLayer layer : getPictures()) {
            if (layer instanceof MapLayerMatch) {
                MapLayerMatch first = (MapLayerMatch)layer;
                if (first.getPhotoLogLine().alt <= STARTING_POS_PICTURES_ALT_THRESHOLD * 100) {
                    return first.getLatLon();
                }

                break;
            }
        }

        return null;
    }

    public static final Comparator<MapLayerMatch> comparatorMatchesFlyingOrder =
        new Comparator<MapLayerMatch>() {

            @Override
            public int compare(MapLayerMatch o1, MapLayerMatch o2) {
                if (o1 == o2) {
                    return 0;
                }

                CPhotoLogLine l1 = o1.getPhotoLogLine();
                CPhotoLogLine l2 = o2.getPhotoLogLine();

                // System.out.println("l1:"+l1 + " l2:"+l2);
                // to keep independent flights not interleaved
                if (o1.getMatching() != o2.getMatching()) {
                    // System.out.println("A:"+o1.getMatching().getResourceFile().getParentFile().getName().compareTo(o2.getMatching().getResourceFile().getParentFile().getName()));
                    return saveCompare(o1, o2, o1.getMatching().getName().compareTo(o2.getMatching().getName()));
                }
                // System.out.println("C:"+(l1.imageNumber - l2.imageNumber));
                return saveCompare(o1, o2, l1.imageNumber - l2.imageNumber);
            }

            private int saveCompare(Object o1, Object o2, int compare) {
                // this makes problems on twice loaded data, since they ARE equal even if they are different objects
                // if (compare == 0) {
                //// System.out.println("saving to 0");
                // return o1.hashCode() - o2.hashCode();
                // }
                return compare;
            }

        };

    public static final Comparator<MapLayerMatch> comparatorMatchesLineOrder =
        new Comparator<MapLayerMatch>() {

            @Override
            public int compare(MapLayerMatch o1, MapLayerMatch o2) {
                if (o1 == o2) {
                    return 0;
                }

                CPhotoLogLine l1 = o1.getPhotoLogLine();
                CPhotoLogLine l2 = o2.getPhotoLogLine();
                // System.out.println("\nl1:"+l1 + " ("+ o1.getMatching().getResourceFile().getParentFile().getName() +
                // ") \nl2:"+l2+ " ("+
                // o2.getMatching().getResourceFile().getParentFile().getName() + ")");
                // if the matches are from different matchings and not both are multiFP generated, dont interleave the
                // mathcings!!
                if (!l1.isMultiFP() || !l2.isMultiFP()) {
                    if (!l1.isMultiFP() && !l2.isMultiFP()) {
                        // beide nicht auf auf multiflightline
                        if (o1.getMatching() != o2.getMatching()) {
                            // System.out.println("A:"+o1.getMatching().getResourceFile().getParentFile().getName().compareTo(o2.getMatching().getResourceFile().getParentFile().getName()));
                            return saveCompare(
                                o1, o2, o1.getMatching().getName().compareTo(o2.getMatching().getName()));
                        } else {
                            // sort by image number inside same matching
                            // System.out.println("A1:"+(l1.imageNumber - l2.imageNumber));
                            return saveCompare(o1, o2, l1.imageNumber - l2.imageNumber);
                        }
                    } else {
                        // non multi FP stuff first
                        if (!l1.isMultiFP()) {
                            // System.out.println("A2:-1");
                            return -1;
                        } else {
                            // System.out.println("A2:1");
                            return 1;
                        }
                    }
                }

                // first sort linewise
                if (l1.getLineNumberPure() != l2.getLineNumberPure()) {
                    // System.out.println("B:"+(l1.getLineNumberPure() - l2.getLineNumberPure()));
                    return saveCompare(o1, o2, l1.getLineNumberPure() - l2.getLineNumberPure());
                }

                // same main line, but different matching?
                // if (o1.getMatching() != o2.getMatching() && l1.getCellNumber() != l2.getCellNumber()) {
                if (l1.getCellNumber() != l2.getCellNumber()) {
                    // System.out.println("C:"+(l1.getCellNumber() - l2.getCellNumber()));
                    return saveCompare(o1, o2, l1.getCellNumber() - l2.getCellNumber());
                }

                // sort linewise, seperate crosslines
                if (l1.getIDPureWithoutCell() != l2.getIDPureWithoutCell()) {
                    // System.out.println("D:"+(l1.getIDPureWithoutCell() - l2.getIDPureWithoutCell()));
                    return saveCompare(o1, o2, l1.getIDPureWithoutCell() - l2.getIDPureWithoutCell());
                }

                // inside lines sort in forward direction! so maybe reverse the line!
                if (l1.isForwardLine() && l2.isForwardLine()) { // I have to ask both, otherwise it is not symmetric!
                    if (l1.imageNumber == l2.imageNumber) {
                        // System.out.println("E1:"+(o1.getResourceFile().getName().compareTo(o2.getResourceFile().getName())));
                        // System.out.println("E1:0");
                        // most likely the same image in different matchings, e.g. once in the source matching and once
                        // in a sparsed one
                        return 0;
                        // return saveCompare(o1, o2,
                        // o1.getResourceFile().getName().compareTo(o2.getResourceFile().getName()));
                    }
                    // System.out.println("E2:"+(l1.imageNumber - l2.imageNumber));
                    return saveCompare(o1, o2, l1.imageNumber - l2.imageNumber);
                } else {
                    // System.out.println("F:"+(l2.imageNumber - l1.imageNumber));
                    return saveCompare(o1, o2, l2.imageNumber - l1.imageNumber);
                }
            }

            private int saveCompare(Object o1, Object o2, int compare) {
                // this makes problems on twice loaded data, since they ARE equal even if they are different objects
                // if (compare == 0) {
                // //this COULD be problematic and cause bugs with triangle equiation...
                // System.out.println("saving to 0");
                // Debug.printStackTrace();
                // return o1.hashCode() - o2.hashCode();
                // }
                return compare;
            }
        };

    public static Vector<Vector<MapLayerMatch>> reorderMatches(
            Collection<AMapLayerMatching> all_matchings, int minEnabledPicsPerLine) {
        return reorderMatches(all_matchings, minEnabledPicsPerLine, comparatorMatchesLineOrder);
    }

    public static Vector<MapLayerMatch> sortMatches(AMapLayerMatching matching, Comparator<MapLayerMatch> imageOrder) {
        Vector<AMapLayerMatching> all_matchings = new Vector<>();
        all_matchings.add(matching);
        return sortMatches(all_matchings, imageOrder);
    }

    public static Vector<MapLayerMatch> sortMatches(
            Collection<AMapLayerMatching> all_matchings, Comparator<MapLayerMatch> imageOrder) {
        Vector<MapLayerMatch> matches = new Vector<>();
        // int fileNo = 0;
        for (AMapLayerMatching matching : all_matchings) {
            // fileNo++;
            for (IMapLayer layer : matching.getPictures()) {
                if (layer instanceof MapLayerMatch) {
                    MapLayerMatch match = (MapLayerMatch)layer;
                    // match.fileNo = fileNo;
                    matches.add(match);
                }
            }
        }

        try {
            Collections.sort(matches, imageOrder);
        } catch (Throwable t) {
            comparisonContractViolationFound = true;
            Debug.getLog().log(Level.SEVERE, "could not sort matches", t);
        }

        return matches;
    }

    /**
     * Gets a list of matches whitch also could come from multiple flights, and reorder it by distinguished lines non
     * Forward lines are reversed if revertBackwardLines is true
     */
    public static Vector<Vector<MapLayerMatch>> reorderMatches(
            Collection<AMapLayerMatching> all_matchings,
            int minEnabledPicsPerLine,
            Comparator<MapLayerMatch> imageOrder) {
        Vector<MapLayerMatch> matches = sortMatches(all_matchings, imageOrder);

        Vector<Vector<MapLayerMatch>> lines = new Vector<>();
        Vector<MapLayerMatch> currentLine = new Vector<>();
        int lastReentryPoint = -1;
        // int lastFileNo = -1;
        int noOfEnabled = 0;
        for (MapLayerMatch match : matches) {
            CPhotoLogLine line = match.getPhotoLogLine();
            if (line.getLineNumberPure() != lastReentryPoint) { // || match.fileNo != lastFileNo){
                if (noOfEnabled >= minEnabledPicsPerLine) {
                    lines.add(currentLine);
                }

                currentLine = new Vector<>();
                lastReentryPoint = line.getLineNumberPure();
                // lastFileNo = match.fileNo;
                noOfEnabled = 0;
            }

            currentLine.add(match);
            if (match.isPassFilter()) {
                noOfEnabled++;
            }
        }

        if (noOfEnabled >= minEnabledPicsPerLine) {
            lines.add(currentLine);
        }

        // if (revertBackwardLines){
        // for (Vector<MapLayerMatch> line : lines) {
        // if (!line.get(0).getPhotoLogLine().isForwardLine()) Collections.reverse(line);
        // }
        // }

        return lines;
    }

    public MapLayerPics getPicsLayer() {
        return pics;
    }

    @Override
    public void mapLayerVisibilityChanged(IMapLayer layer, boolean newVisibility) {
        if (layer instanceof MapLayerPicArea) {
            setChanged(true);
            recompute();
        } else if (layer instanceof MapLayerMatch) {
        } else if (layer == this) {
        } else {
            setChanged(true);
        }

        super.mapLayerVisibilityChanged(layer, newVisibility);
    }

    @Override
    public void mapLayerStructureChanged(IMapLayer layer) {
        if (layer instanceof MapLayerPicArea || layer instanceof MapLayerPicAreas) {
            setChanged(true);
            resetEstimatedStartingElevation();
            recompute();
        }

        super.mapLayerStructureChanged(layer);
    }

    @Override
    public void mapLayerValuesChanged(IMapLayer layer) {
        if (layer instanceof MapLayerPicArea) {
            setChanged(true);
            recompute();
        } else if (layer instanceof MapLayerPics) {
            setChanged(true);
        }

        super.mapLayerValuesChanged(layer);
    }

    @Override
    public void childMapLayerInserted(int i, IMapLayer layer) {
        if (layer instanceof MapLayerMatch) {
            resetEstimatedStartingElevation();
            if (!isMute()) {
                setChanged(true);
                filterChanged();
            }
        }

        super.childMapLayerInserted(i, layer);
    }

    @Override
    public void childMapLayerRemoved(int i, IMapLayer layer) {
        if (layer instanceof MapLayerMatch) {
            resetEstimatedStartingElevation();
            if (!isMute()) {
                setChanged(true);
                filterChanged();
            }
        } else if (layer instanceof MapLayerPicArea) {
            setChanged(true);
            recompute();
        }

        super.childMapLayerRemoved(i, layer);
    }

    @Override
    public void setMute(boolean mute) {
        getPicsLayer().setMute(mute);
        boolean oldMute = this.mute;
        super.setMute(mute);
        if (!mute && oldMute) {
            setChanged(true);
            filterChanged();
        }
    }

    @Override
    public void setSilentUnmute() {
        getPicsLayer().setSilentUnmute();
        super.setSilentUnmute();
    }

    public abstract void cloneMyValuesTo(MapLayerMatching other) throws Exception;

    public double estimateGsd() {
        double resolution = Double.POSITIVE_INFINITY;
        for (MapLayerPicArea picArea : getVisiblePicAreas()) {
            if (resolution > picArea.getGSD()) {
                resolution = picArea.getGSD();
            }
        }

        if (resolution == Double.POSITIVE_INFINITY) {
            for (MapLayerPicArea picArea : getPicAreas()) {
                if (resolution > picArea.getGSD()) {
                    resolution = picArea.getGSD();
                }
            }
        }

        if (resolution == Double.POSITIVE_INFINITY) {
            resolution = CPicArea.DEF_GSD;
        }

        return resolution;
    }

    public void setFile(File matchingFolder) throws IOException {
        if (this.matchingFolder != null) {
            updateImagesPath(this.matchingFolder.toPath(), matchingFolder.toPath());
        }

        this.matchingFolder = matchingFolder;
    }

    private void updateImagesPath(Path oldMatchingPath, Path newMatchingPath) {
        getPictures()
            .stream()
            .filter(MapLayerMatch.class::isInstance)
            .map(MapLayerMatch.class::cast)
            .map(m -> m.photoCube.photoFiles)
            .flatMap(Arrays::stream)
            .forEach(
                photo ->
                    photo.file = replaceParentPath(photo.file.toPath(), oldMatchingPath, newMatchingPath).toFile());
    }

    public static Path replaceParentPath(Path oldPhotoPath, Path oldMatchingPath, Path newMatchingPath) {
        if (oldPhotoPath.startsWith(oldMatchingPath)) {
            Path relativePath = oldMatchingPath.relativize(oldPhotoPath);
            return newMatchingPath.resolve(relativePath);
        } else {
            return oldPhotoPath;
        }
    }

    private boolean isAltAGLPass(double alt) {
        if (!altitudeAGLEnabled) {
            return true;
        }

        return Math.abs(this.altitudeValue - alt) <= this.altitudeSpread;
    }

    private boolean isRollPass(MapLayerMatch match) {
        if (!rollEnabled) {
            return true;
        }

        double rollNormalized = this.rollValue - match.line.cameraRoll;
        while (rollNormalized < -180) {
            rollNormalized += 360;
        }

        while (rollNormalized >= 180) {
            rollNormalized -= 360;
        }

        return Math.abs(rollNormalized) <= this.rollSpread;
    }

    private boolean isPitchPass(MapLayerMatch match) {
        if (!pitchEnabled) {
            return true;
        }

        return Math.abs(this.pitchValue - match.line.cameraPitch) <= this.pitchSpread;
    }

    private boolean isYawPass(MapLayerMatch match) {
        if (!yawEnabled) {
            return true;
        }

        double yawNormalized = this.yawValue - match.line.cameraYaw;
        while (yawNormalized < -180) {
            yawNormalized += 360;
        }

        while (yawNormalized >= 180) {
            yawNormalized -= 360;
        }

        return Math.abs(yawNormalized) <= this.yawSpread;
    }

    public void setElevationOffset(double elevationOffset) {
        if (this.elevationOffset == elevationOffset) return;
        this.elevationOffset = elevationOffset;
        mapLayerValuesChanged(this);
        if (!getPicsLayer().layer.isMute()) {
            getCoverage().updateCameraCorners();
        }

        setChanged(true);
    }

    public double getElevationOffset() {
        return elevationOffset;
    }

    public void rename(String newName) throws IOException {
        File newFolder = new File(matchingFolder.getParentFile(), newName);
        if (newFolder.equals(matchingFolder)) {
            return;
        }

        if (newFolder.exists()) {
            throw new IOException("File already exist:" + newFolder);
        }

        boolean result = matchingFolder.renameTo(newFolder);
        if (!result) {
            throw new IOException("Can't rename dataset to " + newFolder);
        }

        setFile(newFolder);
        mapLayerValuesChanged(this);
    }
}
