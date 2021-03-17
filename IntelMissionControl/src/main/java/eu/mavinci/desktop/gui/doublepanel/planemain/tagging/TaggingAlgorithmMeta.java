/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.planemain.tagging;

import eu.mavinci.core.flightplan.CPhotoLogLine;
import eu.mavinci.core.flightplan.PhotoLogLineType;
import eu.mavinci.core.flightplan.camera.GPStype;
import eu.mavinci.core.helper.MinMaxPair;
import eu.mavinci.desktop.helper.InterruptedByUserException;
import eu.mavinci.desktop.main.debug.Debug;
import java.io.File;
import java.util.TreeSet;
import java.util.Vector;
import java.util.logging.Level;

public class TaggingAlgorithmMeta implements ITaggingAlgorithm {

    // TaggingAlgorithmA [] algs = {new TaggingAlgorithmGreedyNoLogsMissing(), new
    // TaggingAlgorithmGreedyLogsAndImagesMissing(), new TaggingAlgorithmGreedyNoLogsMissing()};
    TaggingAlgorithmA[] algs = {
        new TaggingAlgorithmGreedyNoLogsMissing(),
        new TaggingAlgorithmGreedyLogsAndImagesMissing(),
        new TaggingAlgorithmLossyLog(),
        new TaggingAlgorithmGreedyNoLogsMissing()
    };

    protected int bestI = 0;

    public TaggingAlgorithmMeta() {}

    @Override
    public boolean checkLogfileDownload() {
        return algs[0].checkLogfileDownload();
    }

    @Override
    public void clearLogfiles() {
        algs[0].clearLogfiles();
    }

    @Override
    public void loadLogfile(File logFile, boolean allowMultiFlashTags) throws Exception {
        algs[0].loadLogfile(logFile, allowMultiFlashTags);
    }

    @Override
    public void loadPictures(
            File picFolder,
            boolean clear,
            int noFilesPerCube,
            boolean generatePreview,
            ProgressCallbackImgLoading progressCallbackImgLoading)
            throws Exception {
        algs[0].loadPictures(picFolder, clear, noFilesPerCube, generatePreview, progressCallbackImgLoading);
    }

    @Override
    public void loadPicturesFromAppLog(
            File logFile, boolean generatePreview, ProgressCallbackImgLoading progressCallbackImgLoading)
            throws Exception {
        algs[0].loadPicturesFromAppLog(logFile, generatePreview, progressCallbackImgLoading);
    }

    @Override
    public void loadPictures(
            MapLayerMatching matching, boolean generatePreview, ProgressCallbackImgLoading progressCallbackImgLoading)
            throws Exception {
        algs[0].loadPictures(matching, generatePreview, progressCallbackImgLoading);
    }

    @Override
    public void optimizeMatching(
            double dtVarietyMax,
            GPStype gpsType,
            MinMaxPair clockPrecision,
            ProgressCallbackOptimizing progressCallbackOptimizing)
            throws Exception {
        bestI = 0;
        int maxMatched = 0;
        double bestVariance = Double.POSITIVE_INFINITY;
        // first, test if matching is possible with photo3 tags only, if so. take result!
        int cntPhoto3 = 0;
        for (CPhotoLogLine line : getLogsAll()) {
            if (line.type == PhotoLogLineType.FLASH) {
                cntPhoto3++;
            }
        }

        Debug.getLog()
            .info(
                "Start Meta Matching optimisation with "
                    + cntPhoto3
                    + " DGPS triggers and "
                    + getLogsAll().size()
                    + " triggers in total for "
                    + getPhotosAll().size()
                    + " photos.");
        if (cntPhoto3 != 0 && gpsType != GPStype.GPS
                || (cntPhoto3 > 0 && cntPhoto3 < getLogsAll().size() && cntPhoto3 <= getPhotosAll().size())) {
            TreeSet<CPhotoLogLine> removedLines = new TreeSet<CPhotoLogLine>();
            for (Object o : getLogsAll().toArray()) {
                CPhotoLogLine line = (CPhotoLogLine)o;
                if (line.type != PhotoLogLineType.FLASH) {
                    if (getLogsAll().remove(line)) {
                        removedLines.add(line);
                    }
                }
            }

            try {
                Debug.getLog().info("Testing only Photo3 tags with algorithm " + algs[0].getAlgName());
                algs[0].optimizeMatching(dtVarietyMax, gpsType, clockPrecision, progressCallbackOptimizing);

                getLogsAll().addAll(removedLines);
                getLogsUnmatched().addAll(removedLines);

                bestI = 0;
                maxMatched = algs[0].getPhotosMatched().size();
                bestVariance = algs[0].getMinMaxPair().size();
                return; // if the first alg which is able to get a total matching, is the winner!
            } catch (InterruptedByUserException e) {
                throw e;
            } catch (Exception e) {
                Debug.getLog().log(Level.INFO, "algorithm i=" + 0 + " with only photo3 lines (DGPS) failed", e);
                // getLogsAll().addAll(removedLines); //dont revert this here, since the next algorithm will also try
                // only photo3 tags
                // throw e; //FIXME REMOVE THIS
            }

            try {
                Debug.getLog()
                    .fine(
                        "Testing only Photo3 tags with algorithm "
                            + algs[0].getAlgName()
                            + " and 1 second more allowed jitter");
                algs[0].optimizeMatching(dtVarietyMax + 1, gpsType, clockPrecision, progressCallbackOptimizing);

                getLogsAll().addAll(removedLines);
                getLogsUnmatched().addAll(removedLines);

                bestI = 0;
                maxMatched = algs[0].getPhotosMatched().size();
                bestVariance = algs[0].getMinMaxPair().size();
                return; // if the first alg which is able to get a total matching, is the winner!
            } catch (InterruptedByUserException e) {
                throw e;
            } catch (Exception e) {
                Debug.getLog()
                    .log(
                        Level.CONFIG,
                        "algorithm i=" + 0 + " with only photo3 lines (DGPS) and one second more jitter failed",
                        e);
                getLogsAll().addAll(removedLines);
                // throw e; //FIXME REMOVE THIS
            }

            // cleanup to make other algs start with org data
            // getLogsAll().addAll(removedLines);
        }

        boolean haveToRestore = false;
        for (int i = 0; i != algs.length - 1; i++) {
            if (i > 0) {
                // insert loaded date!
                algs[i].getPhotosAll().clear();
                algs[i].getPhotosAll().addAll(getPhotosAll());
                algs[i].getLogsAll().clear();
                algs[i].getLogsAll().addAll(getLogsAll());
            }

            try {
                Debug.getLog().info("Testing with algorithm " + algs[i].getAlgName());
                algs[i].optimizeMatching(dtVarietyMax, gpsType, clockPrecision, progressCallbackOptimizing);

                if (algs[i].getPhotosMatched().size() == algs[i].getPhotosAll().size()) {
                    bestI = i;
                    maxMatched = algs[i].getPhotosMatched().size();
                    bestVariance = algs[i].getMinMaxPair().size();
                    haveToRestore = false;
                    break; // if the first alg which is able to get a total matching, is the winner!
                } else if (algs[i].getPhotosMatched().size() > maxMatched
                        || (algs[i].getPhotosMatched().size() == maxMatched
                            && bestVariance > algs[i].getMinMaxPair().size())) { // greater
                    // without
                    // equal is
                    // important!
                    bestI = i;
                    maxMatched = algs[i].getPhotosMatched().size();
                    bestVariance = algs[i].getMinMaxPair().size();
                    haveToRestore = false;
                } else {
                    haveToRestore = true;
                }

                Debug.getLog().info("matches:" + algs[i].getPhotosMatched().size() + "  jitter:" + bestVariance);

            } catch (InterruptedByUserException e) {
                throw e;
            } catch (Exception e) {
                haveToRestore = true;
                Debug.getLog().log(Level.INFO, "algorithm i=" + i + " failed, try next one", e);
            }
        }
        // restore result of best alg
        if (haveToRestore) {
            haveToRestore = false;
            Debug.getLog().log(Level.INFO, "Restoring result of algorithm i=" + bestI);
            int i = bestI;
            if (i > 0) {
                // insert loaded date!
                algs[i].getPhotosAll().clear();
                algs[i].getPhotosAll().addAll(getPhotosAll());
                algs[i].getLogsAll().clear();
                algs[i].getLogsAll().addAll(getLogsAll());
            }

            try {
                Debug.getLog().info("Testing with algorithm " + algs[i].getAlgName());
                algs[i].optimizeMatching(dtVarietyMax, gpsType, clockPrecision, progressCallbackOptimizing);
                Debug.getLog().info("matches:" + algs[i].getPhotosMatched().size() + "  jitter:" + bestVariance);

            } catch (InterruptedByUserException e) {
                throw e;
            } catch (Exception e) {
                Debug.getLog().log(Level.INFO, "algorithm i=" + i + " failed, try next one", e);
            }
        }

        if (bestI != 0 && getPhotosUnmatched().size() > 0) {
            // retrying old alg, but with sparsing of images similar to bestI
            int i = algs.length - 1;
            algs[i].getPhotosAll().clear();
            algs[i].getPhotosAll().addAll(getPhotosAll());
            algs[i].getLogsAll().clear();
            algs[i].getLogsAll().addAll(getLogsAll());
            PhotoCube last = null;
            boolean changed = false;
            TreeSet<PhotoCube> removedPhotos = new TreeSet<>();
            // try to esimate burst or head/tail-errors remove them from the data, and optimize again with old alg
            for (PhotoCube p : getPhotosUnmatched()) {
                if (last != null && last.noInFolderTmp + 1 == p.noInFolderTmp) {
                    if (algs[i].getPhotosAll().remove(p)) {
                        removedPhotos.add(p);
                    }

                    if (algs[i].getPhotosAll().remove(last)) {
                        removedPhotos.add(last);
                    }

                    changed = true;
                    // System.out.println("removing " + p + " and: "+ last);
                }

                last = p;
                if (p == getPhotosAll().first() || p == getPhotosAll().last()) {
                    if (algs[i].getPhotosAll().remove(p)) {
                        removedPhotos.add(p);
                        // System.out.println("removing header/footer: " + p);
                    }

                    changed = true;
                }
            }

            if (changed) {
                // System.out.println("retrying the old alg with removed images");
                Debug.getLog()
                    .info(
                        "Restesting with algorithm "
                            + algs[i].getAlgName()
                            + " to improve order of result "
                            + algs[bestI].getAlgName());
                try {
                    Debug.getLog().info("Testing with algorithm " + algs[i].getAlgName());
                    algs[i].optimizeMatching(dtVarietyMax, gpsType, clockPrecision, progressCallbackOptimizing);
                    algs[i].getPhotosAll().addAll(removedPhotos);
                    algs[i].getPhotosUnmatched().addAll(removedPhotos);
                    if (algs[i].getPhotosMatched().size()
                            >= maxMatched) { // here equal is ok, because the ordering is mostly better in the
                        // old alg
                        bestI = i;
                        // System.out.println("the old Alg. improves result! " +algs[i].getPhotosMatched().size() + " ->
                        // " + maxMatched);
                        maxMatched = algs[i].getPhotosMatched().size();
                    } else {
                        haveToRestore = true;
                    }
                } catch (InterruptedByUserException e) {
                    throw e;
                } catch (Exception e) {
                    haveToRestore = true;
                    Debug.getLog().log(Level.INFO, "algorithm i=" + i + " failed, try next one", e);
                }
            }
        }
        // restore result of best alg
        if (haveToRestore) {
            haveToRestore = false;
            Debug.getLog().log(Level.INFO, "Restoring result of algorithm i=" + bestI);
            int i = bestI;
            if (i > 0) {
                // insert loaded date!
                algs[i].getPhotosAll().clear();
                algs[i].getPhotosAll().addAll(getPhotosAll());
                algs[i].getLogsAll().clear();
                algs[i].getLogsAll().addAll(getLogsAll());
            }

            try {
                Debug.getLog().info("Testing with algorithm " + algs[i].getAlgName());
                algs[i].optimizeMatching(dtVarietyMax, gpsType, clockPrecision, progressCallbackOptimizing);
                Debug.getLog().info("matches:" + algs[i].getPhotosMatched().size() + "  jitter:" + bestVariance);

            } catch (InterruptedByUserException e) {
                throw e;
            } catch (Exception e) {
                Debug.getLog().log(Level.INFO, "algorithm i=" + i + " failed, try next one", e);
            }
        }

        if (maxMatched < getPhotosAll().size() * ACCEPT_MATCHED_PHOTO_RATIO) {
            throw new TaggingException(
                "Create matching error: Less than the "
                    + (ACCEPT_MATCHED_PHOTO_RATIO * 100)
                    + "% of the images are matched!",
                "Less than the "
                    + (ACCEPT_MATCHED_PHOTO_RATIO * 100)
                    + "% of the images are matched! Could not find matching between logfile and images in Folder. Probably they do not belong together!");
        }

        Debug.getLog().info("Algorithm bestI=" + bestI + "  " + algs[bestI].getAlgName() + " wins!");
    }

    public static final double ACCEPT_MATCHED_PHOTO_RATIO = 0.6;

    @Override
    public boolean wasOptimal() {
        return algs[0].oldNumbers.equals(algs[bestI].newNumbers);
    }

    @Override
    public TreeSet<CPhotoLogLine> getLogsAll() {
        return algs[0].getLogsAll();
    }

    @Override
    public TreeSet<PhotoCube> getPhotosMatched() {
        return algs[bestI].getPhotosMatched();
    }

    @Override
    public TreeSet<CPhotoLogLine> getLogsUnmatched() {
        return algs[bestI].getLogsUnmatched();
    }

    @Override
    public TreeSet<PhotoCube> getPhotosAll() {
        return algs[0].getPhotosAll();
    }

    @Override
    public TreeSet<PhotoCube> getPhotosUnmatched() {
        return algs[bestI].getPhotosUnmatched();
    }

    @Override
    public double getShift() {
        return algs[bestI].getShift();
    }

    @Override
    public MinMaxPair getMinMaxPair() {
        return algs[bestI].getMinMaxPair();
    }

    @Override
    public File getLogFile() {
        return algs[0].getLogFile();
    }

    @Override
    public String getAlgName() {
        return "Meta + " + algs[bestI].getAlgName();
    }

    @Override
    public Vector<File> getFPs(File missionFolder) {
        return algs[0].getFPs(missionFolder);
    }

    @Override
    public boolean isRtkPosAvaliable() {
        return algs[0].isRtkPosAvaliable();
    }

    @Override
    public double getRtkLat() {
        return algs[0].getRtkLat();
    }

    @Override
    public double getRtkLon() {
        return algs[0].getRtkLon();
    }

    @Override
    public double getRtkAltMSL() {
        return algs[0].getRtkAltMSL();
    }

    @Override
    public double getRtkGeoidSep() {
        return algs[0].getRtkGeoidSep();
    }

    @Override
    public double getRtkAvgTime() {
        return algs[0].getRtkAvgTime();
    }

    @Override
    public double getRtkTimestamp() {
        return algs[0].getRtkTimestamp();
    }

    @Override
    public boolean isConnectorAvgPosAvaliable() {
        return algs[0].isConnectorAvgPosAvaliable();
    }

    @Override
    public double getConnectorAvgLat() {
        return algs[0].getConnectorAvgLat();
    }

    @Override
    public double getConnectorAvgLon() {
        return algs[0].getConnectorAvgLon();
    }

    @Override
    public double getConnectorAvgAltWGS84() {
        return algs[0].getConnectorAvgAltWGS84();
    }

    @Override
    public RikolaTaskfile loadPicturesFromTaskFile(
            File taskFile,
            File bandOrderFile,
            boolean generatePreview,
            ProgressCallbackImgLoading progressCallbackImgLoading)
            throws Exception {
        return algs[0].loadPicturesFromTaskFile(taskFile, bandOrderFile, generatePreview, progressCallbackImgLoading);
    }

    @Override
    public boolean isLogfileConfusing() {
        return algs[0].isLogfileConfusing();
    }

    @Override
    public TreeSet<PhotoCube> getPhotosCalib() {
        return algs[0].getPhotosCalib();
    }

}
