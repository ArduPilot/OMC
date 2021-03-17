/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.planemain.tagging;

import com.intel.missioncontrol.INotificationObject;
import com.intel.missioncontrol.hardware.IGenericCameraConfiguration;
import com.intel.missioncontrol.hardware.IHardwareConfiguration;
import com.intel.missioncontrol.hardware.IHardwareConfigurationManager;
import com.intel.missioncontrol.helper.Ensure;
import com.intel.missioncontrol.map.worldwind.IWWGlobes;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import eu.mavinci.desktop.gui.doublepanel.planemain.tree.maplayers.IMapLayer;
import eu.mavinci.desktop.gui.doublepanel.planemain.wwd.ComputeCornerData;
import eu.mavinci.desktop.helper.FileHelper;
import eu.mavinci.desktop.helper.ImageHelper;
import eu.mavinci.desktop.helper.ImageMask;
import eu.mavinci.desktop.helper.MFileFilter;
import eu.mavinci.desktop.helper.MathHelper;
import eu.mavinci.desktop.main.debug.Debug;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import org.asyncfx.concurrent.Dispatcher;
import eu.mavinci.WeakRunnable;

public class MapLayerMatching extends AMapLayerMatching implements ComputeCornerData.IAerialPinholeImageContext {

    MapLayerPicAreas areas;
    MapLayerRTKPosition rtkLayer;
    MapLayerDatasetTrack track;

    protected double connectorAvgLat;
    protected double connectorAvgLon;
    protected double connectorAvgAltWGS84;
    protected boolean
        connectorAvgPosAvaliable; // dont initialize this exmplizitly with something, since this will be overwrite from
    // file
    // loaded values

    private boolean
        rtkPosAvaliable; // DO NOT SET THIS FALSE EXPLICIT, since it will overwrite values since this data is loaded
    // inside the
    // constructor before this assignment.. will be implicitly false anyway!! = false;
    double rtkLat;
    double rtkLon;
    double rtkAltMSL;
    double rtkGeoidSep;
    double rtkAvgTime; // if <0: rtk base not set, if ==0: position manually entered, if >0: averaging time [secs]
    double rtkTimestamp; // timestamp when the position was created with averaging

    double realLat;
    double realLon;
    double realAltWgs84;
    double realAntennaAlt;
    double geoidOffset;

    public static final int minMaskImages = 5;
    public static final int maxMaskImages = 20;
    public static final int typMaskImagesSpacing = 20;

    String[] bandNames;

    boolean maskComputationStarted;

    ImageMask maskNarrow;
    ImageMask maskWide;

    Vec4 rtkOffsetCached = null;

    public static final String DEFAULT_FILENAME = "dataset." + MFileFilter.ptgFilter.getExtension();

    protected final IHardwareConfigurationManager hardwareConfigurationManager =
        DependencyInjector.getInstance().getInstanceOf(IHardwareConfigurationManager.class);
    private static final Globe globe =
        DependencyInjector.getInstance().getInstanceOf(IWWGlobes.class).getDefaultGlobe();
    protected IHardwareConfiguration hardwareConfiguration;

    private final INotificationObject.ChangeListener hwConfigListener =
        new INotificationObject.ChangeListener() {
            @Override
            public void propertyChange(INotificationObject.ChangeEvent propertyChangeEvent) {
                // trigger this to mark layer as changed
                setChanged(true);
                if (!getPicsLayer().layer.isMute()) {
                    getPicsLayer().layer.fireImageLayerChanged();
                }

                getCoverage().updateCameraCorners();
            }
        };

    public MapLayerMatching(IHardwareConfiguration hardwareConfiguration) {
        super(null, true);
        this.hardwareConfiguration = hardwareConfiguration;
        this.hardwareConfiguration.addListener(new INotificationObject.WeakChangeListener(hwConfigListener));
        try {
            postConstruction();
        } catch (Exception e) {
            // since exceptions only coming from file loading, they should be rare, so lets wrap them in an
            // RuntimeException
            throw new RuntimeException(e);
        }

        Ensure.notNull(this.hardwareConfiguration, "hardwareConfiguration");
    }

    public MapLayerMatching(File matchingFolder, IHardwareConfiguration hardwareConfiguration) throws Exception {
        super(matchingFolder, false);
        this.hardwareConfiguration = hardwareConfiguration;
        this.hardwareConfiguration.addListener(new INotificationObject.WeakChangeListener(hwConfigListener));
        postConstruction();
        Ensure.notNull(this.hardwareConfiguration, "hardwareConfiguration");
    }

    public IGenericCameraConfiguration getCameraConfiguration() {
        return hardwareConfiguration.getPrimaryPayload(IGenericCameraConfiguration.class);
    }

    public void updateHardwareConfiguration(IHardwareConfiguration hardwareConfiguration) {
        this.hardwareConfiguration.initializeFrom(hardwareConfiguration);
    }

    public IHardwareConfiguration getHardwareConfiguration() {
        return hardwareConfiguration;
    }

    @Override
    protected void postConstruction() throws Exception {
        // System.out.println("post contruction MapLayerMatching" + toString());
        if (bandNames == null) {
            bandNames = new String[] {"rgb"};
        }

        areas = new MapLayerPicAreas(this);
        addMapLayer(areas);

        track = new MapLayerDatasetTrack(this);
        addMapLayer(track);

        if (!createNew) {
            load();
        }

        super.postConstruction();
    }

    public MapLayerDatasetTrack getTrackLayer() {
        return track;
    }

    /**
     * checks if the exif meta data fits to the selected hardware desription returns a i18n string in case something is
     * wrong, and NULL if everything is ok
     */
    public String checkExif() {
        if (!getPictures().isEmpty()) {
            MapLayerMatch match = (MapLayerMatch)getPictures().get(getPictures().size() - 1);
            return (match.getCurPhotoFile() != null
                ? match.getCurPhotoFile().isCameraMatchingThis(getCameraConfiguration(), true)
                : null);
        }

        return null;
    }

    @Override
    public void load() throws Exception {
        File base = getMatchingFolder();

        File old = new File(base, "flightplans");
        old.renameTo(new File(base, FOLDER_NAME_FLIGHTPLANS_SUBFOLDER));

        old = new File(base, "images");
        old.renameTo(new File(base, FOLDER_NAME_PICS_SUBFOLDER));

        old = new File(base, "thumbs");
        old.renameTo(new File(base, PhotoFile.FOLDER_PREVIEW_IMG));

        try {
            setMute(true);
            MatchingDataReader reader = new MatchingDataReader();
            reader.readMatchingData(this, getResourceFile(), hardwareConfigurationManager);
        } finally {
            setMute(false);
            mapLayerStructureChanged(this);
            rtkShiftChanged();
            setChanged(false);
            Dispatcher.background().runLater(new WeakRunnable(runnableRecompute), Duration.ofMillis(5000));
        }
    }

    Runnable runnableRecompute =
        new Runnable() {

            @Override
            public void run() {
                MapLayerMatching.this.recompute();
            }
        };

    public void saveResourceFile() {
        // assure folders exist
        if (getMatchingFolder() == null) {
            throw new NullPointerException("getMatchingFolder()");
        } else {
            getImagesFolder().mkdirs();
            getPreviewImagesFolder().mkdirs();
            getFlightplanFolder().mkdirs();
        }

        MatchingDataWriter writer = new MatchingDataWriter();
        try {
            writer.writeMatchingData(this, getResourceFile());
            setChanged(false);
        } catch (IOException e) {
            Debug.getLog().log(Level.WARNING, "Problems writing Matching to File", e);
        }
    }

    @Override
    public File getResourceFile() {
        return new File(getMatchingFolder(), DEFAULT_FILENAME);
    }

    @Override
    public String getName() {
        return matchingFolder == null ? "null" : matchingFolder.getName();
    }

    @Override
    public File getMatchingFolder() {
        return matchingFolder;
    }

    public File getImagesFolder() {
        if (getMatchingFolder() == null) {
            throw new NullPointerException("getMatchingFolder()");
        }

        return new File(getMatchingFolder(), FOLDER_NAME_PICS_SUBFOLDER);
    }

    public File getPreviewImagesFolder() {
        if (getMatchingFolder() == null) {
            throw new NullPointerException("getMatchingFolder()");
        }

        return new File(getMatchingFolder(), PhotoFile.FOLDER_PREVIEW_IMG);
    }

    public File getFlightplanFolder() {
        if (getMatchingFolder() == null) {
            throw new NullPointerException("getMatchingFolder()");
        }

        return new File(getMatchingFolder(), FOLDER_NAME_FLIGHTPLANS_SUBFOLDER);
    }

    public MapLayerPicAreas getPicAreasLayer() {
        Ensure.notNull(areas, "areas");
        return areas;
    }

    @Override
    public List<MapLayerPicArea> getVisiblePicAreas() {
        ArrayList<MapLayerPicArea> list = new ArrayList<>();
        if (!getPicAreasLayer().isCropCoverage()) {
            return list;
        }

        if (useAll || !onlyInPicArea) {
            return list;
        }

        for (IMapLayer layer : getPicAreasLayer().getLayers()) {
            if (!layer.isVisible()) {
                continue;
            }

            if (layer instanceof MapLayerPicArea) {
                MapLayerPicArea picArea = (MapLayerPicArea)layer;
                list.add(picArea);
            }
        }

        return list;
    }

    @Override
    public List<MapLayerPicArea> getPicAreas() {
        ArrayList<MapLayerPicArea> list = new ArrayList<>();
        for (IMapLayer layer : getPicAreasLayer().getLayers()) {
            if (layer instanceof MapLayerPicArea) {
                MapLayerPicArea picArea = (MapLayerPicArea)layer;
                list.add(picArea);
            }
        }

        return list;
    }

    @Override
    public void cloneMyValuesTo(MapLayerMatching other) throws Exception {
        other.onlyInPicArea = onlyInPicArea;
        other.cover.resolution = cover.resolution;

        other.rtkAltMSL = rtkAltMSL;
        other.rtkAvgTime = rtkAvgTime;
        other.rtkGeoidSep = rtkGeoidSep;
        other.rtkLat = rtkLat;
        other.rtkLon = rtkLon;
        other.rtkTimestamp = rtkTimestamp;

        other.realLat = realLat;
        other.realLon = realLon;
        other.realAltWgs84 = realAltWgs84;
        other.realAntennaAlt = realAntennaAlt;
        other.geoidOffset = geoidOffset;

        other.connectorAvgAltWGS84 = connectorAvgAltWGS84;
        other.connectorAvgLat = connectorAvgLat;
        other.connectorAvgLon = connectorAvgLon;
        other.connectorAvgPosAvaliable = connectorAvgPosAvaliable;

        other.bandNames = bandNames;
        other.currentBandNo = currentBandNo;

        if (isRTKposAvaiable()) {
            other.setRTKAvaiable();
        }

        for (IMapLayer layer : getPicAreasLayer().getLayers()) {
            if (layer instanceof MapLayerPicArea) {
                MapLayerPicArea picArea = (MapLayerPicArea)layer;
                other.getPicAreasLayer().addToFlightplanContainer(picArea.clone(other));
            }
        }

        other.filterChanged();
    }

    public void setOnlyInPicArea(boolean onlyInPicArea) {
        super.setOnlyInPicArea(onlyInPicArea);
        if (onlyInPicArea) {
            // ensure that they are shown as well
            getPicAreasLayer().setVisible(true);
        }
    }

    public boolean isConnectorAvgPosAvaliable() {
        return connectorAvgPosAvaliable;
    }

    public double getConnectorAvgLat() {
        return connectorAvgLat;
    }

    public double getConnectorAvgLon() {
        return connectorAvgLon;
    }

    public double getConnectorAvgAltWGS84() {
        return connectorAvgAltWGS84;
    }

    public double getRtkLat() {
        return rtkLat;
    }

    public double getRtkLon() {
        return rtkLon;
    }

    public double getRtkAltMSL() {
        return rtkAltMSL;
    }

    public double getRtkGeoidSep() {
        return rtkGeoidSep;
    }

    public double getRtkAvgTime() {
        return rtkAvgTime;
    }

    public double getRtkTimestamp() {
        return rtkTimestamp;
    }

    public double getRealLat() {
        return realLat;
    }

    public double getRealLon() {
        return realLon;
    }

    public double getRealAltWgs84() {
        return realAltWgs84;
    }

    public double getRealAntennaAlt() {
        return realAntennaAlt;
    }

    public void setRealPos(Position pos, double realAntennaAlt) {
        setRealPos(pos.getLatitude().degrees, pos.getLongitude().degrees, pos.getElevation(), realAntennaAlt);
    }

    public void setRealPos(double lat, double lon, double altWgs84) {
        if (lat == this.realLat && lon == this.realLon && altWgs84 == this.realAltWgs84) {
            return;
        }

        this.realLat = lat;
        this.realLon = lon;
        this.realAltWgs84 = altWgs84;
        resetRTKoffsetCache();
        // mapLayerValuesChanged(this);
        rtkShiftChanged();
    }

    public void setRealPos(double lat, double lon, double altWgs84, double realAntennaAlt) {
        if (lat == this.realLat
                && lon == this.realLon
                && altWgs84 == this.realAltWgs84
                && realAntennaAlt == this.realAntennaAlt) {
            return;
        }

        this.realLat = lat;
        this.realLon = lon;
        this.realAltWgs84 = altWgs84;
        this.realAntennaAlt = realAntennaAlt;
        resetRTKoffsetCache();
        // mapLayerValuesChanged(this);
        rtkShiftChanged();
    }

    @Override
    public int getNumberOfImagesPerPosition() {
        Ensure.notNull(bandNames, "bandNames");
        return bandNames.length;
    }

    @Override
    public String[] getBandNames() {
        return bandNames;
    }

    public void setRealLat(double lat) {
        if (lat == this.realLat) {
            return;
        }

        this.realLat = lat;
        resetRTKoffsetCache();
        // mapLayerValuesChanged(this);
        rtkShiftChanged();
    }

    public void setRealLon(double lon) {
        if (lon == this.realLon) {
            return;
        }

        this.realLon = lon;
        resetRTKoffsetCache();
        // mapLayerValuesChanged(this);
        rtkShiftChanged();
    }

    public void setRealAltWgs84(double altWgs84) {
        if (altWgs84 == this.realAltWgs84) {
            return;
        }

        this.realAltWgs84 = altWgs84;
        resetRTKoffsetCache();
        // mapLayerValuesChanged(this);
        rtkShiftChanged();
    }

    public void setRealAntennaAlt(double realAntennaAlt) {
        if (realAntennaAlt == this.realAntennaAlt) {
            return;
        }

        this.realAntennaAlt = realAntennaAlt;
        resetRTKoffsetCache();
        rtkShiftChanged();
    }

    protected void rtkShiftChanged() {
        getCoverage().updateCameraCorners(); // will update the corners async... filter change will be triggered when
        // done!
        mapLayerValuesChanged(this);
    }

    public Position getRTKPosition() {
        return Position.fromDegrees(rtkLat, rtkLon, rtkAltMSL + rtkGeoidSep);
    }

    public Position getRealPosition() {
        if (getLocationType() == LocationType.ASSUMED) {
            return getRTKPosition();
        }

        return Position.fromDegrees(realLat, realLon, realAltWgs84 + realAntennaAlt);
    }

    public Position getRealPositionWitoutAntennaShift() {
        if (getLocationType() == LocationType.ASSUMED) {
            return getRTKPosition();
        }

        return Position.fromDegrees(realLat, realLon, realAltWgs84);
    }

    public Position getRealPositionWitoutAntennaShiftIgnoringLocationType() {
        return Position.fromDegrees(realLat, realLon, realAltWgs84);
    }

    public boolean isRTKposAvaiable() {
        // System.out.println("is Avaliable on: " + this.hashCode() + " -> " + rtkPosAvaliable);
        return rtkPosAvaliable;
    }

    /**
     * in case this is a RTK dataset, return the RTK layer
     *
     * @return
     */
    public MapLayerRTKPosition getMayLayerRTKPosition() {
        return rtkLayer;
    }

    public void setRTKAvaiable() {
        if (rtkPosAvaliable) {
            return;
        }

        rtkPosAvaliable = true;
        // System.out.println("set Avaliable on: " + this.hashCode());
        rtkLayer = new MapLayerRTKPosition(this);
        addMapLayer(rtkLayer);
        rtkShiftChanged();
    }

    public void setRTKUnavaiable() {
        if (!rtkPosAvaliable) {
            return;
        }

        rtkPosAvaliable = false;
        // System.out.println("set Avaliable on: " + this.hashCode());
        if (rtkLayer != null) {
            removeMapLayer(rtkLayer);
            rtkLayer = null;
        }

        rtkShiftChanged();
    }

    public void setRTKAvaiable(ITaggingAlgorithm alg) {
        if (!alg.isRtkPosAvaliable()) {
            setRTKUnavaiable();
            return;
        }

        rtkAltMSL = alg.getRtkAltMSL();
        rtkAvgTime = alg.getRtkAvgTime();
        rtkGeoidSep = alg.getRtkGeoidSep();
        rtkLat = alg.getRtkLat();
        rtkLon = alg.getRtkLon();
        rtkTimestamp = alg.getRtkTimestamp();

        realLat = rtkLat;
        realLon = rtkLon;
        realAltWgs84 = rtkAltMSL + rtkGeoidSep;
        realAntennaAlt = 0;

        connectorAvgLat = alg.getConnectorAvgLat();
        connectorAvgLon = alg.getConnectorAvgLon();
        connectorAvgAltWGS84 = alg.getConnectorAvgAltWGS84();
        connectorAvgPosAvaliable = alg.isConnectorAvgPosAvaliable();

        setRTKAvaiable();
    }

    protected synchronized void resetRTKoffsetCache() {
        rtkOffsetCached = null;
    }

    public synchronized Vec4 getRtkOffset() {
        if (getLocationType() == LocationType.ASSUMED) {
            return Vec4.ZERO;
        }
        // LocationType.MANUAL....
        if (rtkOffsetCached == null) {
            Vec4 vReal = globe.computePointFromPosition(getRealPosition());
            Vec4 vRTK = globe.computePointFromPosition(getRTKPosition());

            rtkOffsetCached = vReal.subtract3(vRTK);
        }

        return rtkOffsetCached;
    }

    @Override
    public double getStartingElevationOverWgs84WithOffset() {
        return getStartingElevationOverWgs84() - getElevationOffset();
    }

    public double getGeoidOffset() {
        return geoidOffset;
    }

    public void setGeoidOffset(double geoidOffset) {
        if (this.geoidOffset == geoidOffset) {
            return;
        }

        this.geoidOffset = geoidOffset;

        // trigger this to mark layer as changed
        setChanged(true);

        // don't fire them, since this will NOT be change anything in preview
        // resetRTKoffsetCache();
        // rtkShiftChanged();
    }

    public synchronized boolean needsMask() {
        return hardwareConfiguration
            .getPrimaryPayload(IGenericCameraConfiguration.class)
            .getLens()
            .getDescription()
            .getLensType()
            .needsMask();
    }

    public synchronized ImageMask getMaskNarrow() {
        if (!needsMask()) {
            return null;
        }

        if (maskNarrow != null) {
            return maskNarrow;
        }

        File maskNarrowFile = new File(getMatchingFolder(), "maskNarrow.png");
        if (maskNarrowFile.exists()) {
            try {
                maskNarrow = new ImageMask(maskNarrowFile);
            } catch (IOException e) {
                Debug.getLog().log(Debug.WARNING, "problems loading narrow maskfile", e);
            }

            return maskNarrow;
        }

        computeMasks();
        return maskNarrow;
    }

    public synchronized ImageMask getMaskWide() {
        if (!needsMask()) {
            return null;
        }

        if (maskWide != null) {
            return maskWide;
        }

        File maskWideFile = new File(getMatchingFolder(), "maskWide.png");
        if (maskWideFile.exists()) {
            try {
                maskWide = new ImageMask(maskWideFile);
            } catch (IOException e) {
                Debug.getLog().log(Debug.WARNING, "problems loading wide maskfile", e);
            }

            return maskWide;
        }

        computeMasks();
        return maskWide;
    }

    private synchronized void computeMasks() {
        if (maskComputationStarted) {
            return;
        }

        maskComputationStarted = true;
        Dispatcher dispatcher = Dispatcher.background();
        dispatcher.run(
            new Runnable() {

                @Override
                public void run() {
                    File tmpMaskNarrow =
                        new File(getMatchingFolder(), "maskNarrow." + System.currentTimeMillis() + ".png");
                    File tmpMaskWide = new File(getMatchingFolder(), "maskWide." + System.currentTimeMillis() + ".png");

                    try {
                        int imgCount = getPictures().size();
                        int numImgs =
                            MathHelper.intoRange(imgCount / typMaskImagesSpacing, minMaskImages, maxMaskImages);
                        if (numImgs > imgCount) {
                            numImgs = imgCount;
                        }

                        int realSpacing = imgCount / numImgs;
                        Vector<File> inImgsSparse = new Vector<>();
                        for (int i = realSpacing; i < imgCount; i += realSpacing) {
                            inImgsSparse.addElement(((MapLayerMatch)(getPictures().get(i))).getResourceFile());
                        }

                        File maskNarrowFile = new File(getMatchingFolder(), "maskNarrow.png");
                        File maskWideFile = new File(getMatchingFolder(), "maskWide.png");

                        // System.out.println("inImgsSparse:"+inImgsSparse.size());

                        if (!ImageHelper.createMask(inImgsSparse, tmpMaskNarrow, tmpMaskWide, getName())) {
                            // maskComputationStarted = false; //dont do this, otherwise it will immediately restart
                            return;
                        }

                        FileHelper.move(tmpMaskNarrow, maskNarrowFile);
                        FileHelper.move(tmpMaskWide, maskWideFile);

                        ImageMask maskNarrow = new ImageMask(maskNarrowFile);
                        ImageMask maskWide = new ImageMask(maskWideFile);

                        synchronized (MapLayerMatching.this) {
                            MapLayerMatching.this.maskNarrow = maskNarrow;
                            MapLayerMatching.this.maskWide = maskWide;
                        }

                        // tell everyone that this layer has changed, so maybe we can get a new preview
                        Dispatcher platform = Dispatcher.platform();
                        platform.runLater(
                            new Runnable() {

                                @Override
                                public void run() {
                                    // mapLayerValuesChanged(MapLayerMatching.this);
                                    // its enough to recomputeCoverage the preview image
                                    getPicsLayer().getWWLayer().resetVisibility();
                                }

                            });

                    } catch (IOException e) {
                        Debug.getLog().log(Level.SEVERE, "could not compute masks", e);
                        return;
                    }
                }

            });
    }

    public void setBandNames(String[] bandNamesSplit) {
        if (Arrays.equals(bandNamesSplit, bandNames)) {
            return;
        }

        this.bandNames = bandNamesSplit;
        mapLayerValuesChanged(this);
    }

    public Double getStartingElevationOverWgs84() {
        return getEstimatedStartingElevationInMoverWGS84();
    }

}
