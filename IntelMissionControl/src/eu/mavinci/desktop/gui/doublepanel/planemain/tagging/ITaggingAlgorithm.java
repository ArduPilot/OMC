/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.planemain.tagging;

import eu.mavinci.core.flightplan.CPhotoLogLine;
import eu.mavinci.core.flightplan.camera.GPStype;
import eu.mavinci.core.helper.MinMaxPair;
import java.io.File;
import java.util.TreeSet;
import java.util.Vector;

public interface ITaggingAlgorithm {

    public static interface ProgressCallbackImgLoading {
        public void progress(File image, long no, long total);

        public boolean isCanceled();
    }

    public static interface ProgressCallbackOptimizing {
        public void progress(String algName, long no, long total);

        public boolean isCanceled();
    }

    /**
     * Check if the logfile lines have always successive numbers
     *
     * @return
     */
    public abstract boolean checkLogfileDownload();

    public abstract boolean isLogfileConfusing();

    public abstract void clearLogfiles();

    public abstract void loadLogfile(File logFile, boolean allowMultiFlashTags) throws Exception;

    public abstract void loadPictures(
            File picFolder,
            boolean clear,
            int noFilesPerCube,
            boolean generatePreview,
            ProgressCallbackImgLoading progressCallbackImgLoading)
            throws Exception;

    public abstract void loadPicturesFromAppLog(
            File logFile, boolean generatePreview, ProgressCallbackImgLoading progressCallbackImgLoading)
            throws Exception;

    public abstract void loadPictures(
            MapLayerMatching matching, boolean generatePreview, ProgressCallbackImgLoading progressCallbackImgLoading)
            throws Exception;

    public abstract RikolaTaskfile loadPicturesFromTaskFile(
            File taskFile,
            File bandOrderFile,
            boolean generatePreview,
            ProgressCallbackImgLoading progressCallbackImgLoading)
            throws Exception;

    public abstract void optimizeMatching(
            double dtVarietyMax,
            GPStype gpsType,
            MinMaxPair clockPrecision,
            ProgressCallbackOptimizing progressCallbackOptimizing)
            throws Exception;

    public abstract boolean wasOptimal();

    public abstract Vector<File> getFPs(File missionBaseFolder);

    public abstract TreeSet<CPhotoLogLine> getLogsAll();

    public abstract TreeSet<PhotoCube> getPhotosMatched();

    public abstract TreeSet<CPhotoLogLine> getLogsUnmatched();

    public abstract TreeSet<PhotoCube> getPhotosAll();

    public abstract TreeSet<PhotoCube> getPhotosUnmatched();

    /**
     * calibration images like for rededge. this images are NOT included in any of the other sets and will be sorted
     * directly on loading automatically
     *
     * @return
     */
    public abstract TreeSet<PhotoCube> getPhotosCalib();

    public abstract double getShift();

    public abstract MinMaxPair getMinMaxPair();

    public File getLogFile();

    public String getAlgName();

    public boolean isRtkPosAvaliable();

    public double getRtkLat();

    public double getRtkLon();

    public double getRtkAltMSL();

    public double getRtkGeoidSep();

    public double
            getRtkAvgTime(); // if <0: rtk base not set, if ==0: position manually entered, if >0: averaging time [secs]

    public double getRtkTimestamp(); // timestamp when the position was created with averaging

    public boolean isConnectorAvgPosAvaliable();

    public double getConnectorAvgLat();

    public double getConnectorAvgLon();

    public double getConnectorAvgAltWGS84();

}
