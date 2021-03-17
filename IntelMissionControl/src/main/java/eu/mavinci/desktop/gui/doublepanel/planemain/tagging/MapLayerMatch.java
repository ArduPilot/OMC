/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.planemain.tagging;

import com.google.common.base.Strings;
import com.intel.missioncontrol.hardware.IHardwareConfiguration;
import com.intel.missioncontrol.helper.Expect;
import eu.mavinci.core.flightplan.CPhotoLogLine;
import eu.mavinci.desktop.gui.doublepanel.camerasettings.CameraHelper;
import eu.mavinci.desktop.gui.doublepanel.mapmanager.IResourceFileReferenced;
import eu.mavinci.desktop.gui.doublepanel.planemain.tree.maplayers.MapLayer;
import eu.mavinci.desktop.gui.doublepanel.planemain.wwd.AerialPinholeKnownImage;
import eu.mavinci.desktop.gui.doublepanel.planemain.wwd.FuzzinessData;
import eu.mavinci.desktop.helper.ImageMask;
import eu.mavinci.geo.ILatLonReferenced;
import eu.mavinci.geo.IPositionReferenced;
import eu.mavinci.geo.ISectorReferenced;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec4;
import java.io.File;
import java.util.OptionalDouble;
import org.apache.commons.io.FilenameUtils;

public class MapLayerMatch extends MapLayer
        implements ISectorReferenced,
            IResourceFileReferenced,
            IMatchingRelated,
            ILatLonReferenced,
            IPositionReferenced {

    public static final String KEY = "eu.mavinci.desktop.gui.doublepanel.planemain.tagging.MapLayerMatch";

    AerialPinholeKnownImage img;

    // public int fileNo = 0;
    MapLayerMatching matching;
    String niceFilename;

    // public MapLayerMatch(File picFile, String logLine, MapLayerMatching matching) throws Throwable{
    // this(picFile,new PhotoLogLine(logLine), matching);
    // }

    public AerialPinholeKnownImage getScreenImage() {
        return img;
    }

    public MapLayerMatch(PhotoCube cube, CPhotoLogLine line, MapLayerMatching matching) {
        this(cube, matching);
        this.line = line;
        img = new AerialPinholeKnownImage(this);
    }

    private MapLayerMatch(PhotoCube cube, MapLayerMatching matching) {
        super(false);
        this.photoCube = cube;
        this.matching = matching;
        if (cube.photoFiles.length > 0 && cube.photoFiles[0] != null) {
            adjustName(false);
        }
    }

    public MapLayerMatch cloneMatch(MapLayerMatching newParentMatching) throws Throwable {
        MapLayerMatch clone = new MapLayerMatch(photoCube, line, newParentMatching);
        return clone;
    }
    /**
     * adjust the nice name of this layer.
     *
     * @param fireChange should a change event be triggered in case of a change
     * @return is the nice layer name was changed
     */
    boolean adjustName(boolean fireChange) {
        String niceFilename = null;
        if (getMatching() instanceof MapLayerMatching) {
            File resourceFile = getResourceFile();
            if (resourceFile != null) {
                niceFilename = resourceFile.getName();
            }
        }

        if (niceFilename != null && !niceFilename.equals(this.niceFilename)) {
            this.niceFilename = niceFilename;
            if (fireChange) {
                mapLayerValuesChanged(this);
            }

            return true;
        }

        return false;
    }

    boolean isPassFilter = true;

    public boolean isPassFilter() {
        return isPassFilter;
    }

    public boolean setPassFilter(boolean pass) {
        boolean changed = adjustName(false);
        boolean filterChanged = false;
        if (pass != isPassFilter) {
            if (img != null) {
                img.setEnabled(pass);
            }

            isPassFilter = pass;
            changed = true;
            filterChanged = true;
        }

        if (changed) {
            mapLayerValuesChanged(this);
        }

        return filterChanged;
    }

    protected CPhotoLogLine line;

    public CPhotoLogLine getPhotoLogLine() {
        return line;
    }

    PhotoCube photoCube;

    @Override
    public boolean isVisible() {
        return true;
    }

    @Override
    public void setVisible(boolean isVisible) {
        var curPhotoFile = getCurPhotoFile();
        Expect.notNull(curPhotoFile);
        if (!curPhotoFile.thumpFileExists() && isLoading) {
            // TODO Marco, this looks unintentional, maybe there is an else missing? :)
            isVisible = false;
        }

        isVisible = true;

        super.setVisible(isVisible);
        // img.setShowImage(isVisible);
    }

    public void generatePreview() throws Exception {
        for (PhotoFile pf : photoCube) {
            pf.generateThumpFile();
        }
    }

    @Override
    public OptionalDouble getMaxElev() {
        return img.getMaxElev();
    }

    @Override
    public OptionalDouble getMinElev() {
        return img.getMinElev();
    }

    @Override
    public Sector getSector() {
        if (!isPassFilter()) {
            return null;
        }

        return img.getSector();
    }

    @Override
    public File getResourceFile() {
        PhotoFile photoFile = getCurPhotoFile();
        return photoFile != null ? photoFile.getFile() : null;
    }

    public long getResourceFileSizeBytes() {
        try {
            File f = getResourceFile();
            return f != null ? f.length() : 0L;
        } catch (Exception ex) {
            ex.printStackTrace();
            return 0L;
        }
    }

    public String getResourceFileType() {
        try {
            File file = getResourceFile();
            if (file == null) {
                return "";
            }

            String extension = FilenameUtils.getExtension(file.getPath());

            if (Strings.isNullOrEmpty(extension)) {
                return "";
            }

            return extension.toUpperCase();
        } catch (Exception ex) {
            ex.printStackTrace();
            return "";
        }
    }

    public PhotoFile getCurPhotoFile() {
        return photoCube.photoFiles.length > 0 && photoCube.photoFiles.length > matching.getCurrentBandNo()
            ? photoCube.photoFiles[matching.getCurrentBandNo()]
            : null;
    }

    @Override
    public String toString() {
        PhotoFile photoFile = getCurPhotoFile();
        if (photoFile == null) return null;
        return photoFile.file.getName();
    }

    public PhotoCube getResourceCube() {
        return photoCube;
    }

    public void setResourceCube(PhotoCube cube) {
        if (photoCube.equals(cube)) {
            return;
        }

        photoCube = cube;

        adjustName(true);
        mapLayerValuesChanged(this);
    }

    public FuzzinessData getFuzzinessBlocking() {
        final var curPhotoFile = getCurPhotoFile();
        Expect.notNull(curPhotoFile);
        return img.getFuzzinessBlocking(curPhotoFile.getExif().exposureSec);
    }

    @Override
    public MapLayerMatching getMatching() {
        return matching;
    }

    @Override
    public LatLon getLatLon() {
        return LatLon.fromDegrees(line.lat, line.lon);
    }

    @Override
    public Position getPosition() {
        return Position.fromDegrees(
            line.lat, line.lon, line.alt / 100. + matching.getEstimatedStartingElevationInMoverWGS84());
    }

    public Vec4 getRtkOffset() {
        return matching.getRtkOffset();
    }

    public ImageMask getMaskNarrow() {
        return matching.getMaskNarrow();
    }

    public ImageMask getMaskWide() {
        return matching.getMaskWide();
    }

    public boolean needsMask() {
        return matching.needsMask();
    }

    public Position getShiftedPosition(IHardwareConfiguration hardwareConfiguration, boolean enableLevelArm) {
        return CameraHelper.shiftPosition(
            line,
            getMatching().getEstimatedStartingElevationInMoverWGS84(),
            getRtkOffset(),
            0,
            enableLevelArm,
            hardwareConfiguration);
    }

    /** shifted / corrected position of this camera including geoid offset */
    public Position getShiftedPositionExport(IHardwareConfiguration hardwareConfiguration) {
        return getShiftedPosition(hardwareConfiguration, true);
    }

    public Position getShiftedPositionExport(IHardwareConfiguration hardwareConfiguration, boolean enableLevelArm) {
        Position p = getShiftedPosition(hardwareConfiguration, enableLevelArm);
        return new Position(p, p.elevation + getMatching().getGeoidOffset());
    }

}
