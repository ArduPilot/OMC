/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.planemain.tagging;

import eu.mavinci.core.flightplan.CPhotoLogLine;
import eu.mavinci.core.flightplan.camera.GPStype;
import eu.mavinci.core.helper.MinMaxPair;
import eu.mavinci.desktop.helper.InterruptedByUserException;
import eu.mavinci.desktop.main.debug.Debug;
import java.io.File;
import java.util.Vector;

public class TaggingAlgorithmGreedyLogsAndImagesMissing extends TaggingAlgorithmA {

    private Vector<CPhotoLogLine> logsVec = new Vector<CPhotoLogLine>();

    private Vector<PhotoCube> photosVec = new Vector<PhotoCube>();

    public static final double MAX_IMG_LOSS_REALTIVE = 0.5;
    public static final int MAX_IMG_LOSS_ABS_MIN = 3;

    public static final double MAX_LOG_LOSS_REALTIVE = MAX_IMG_LOSS_REALTIVE;
    public static final int MAX_LOG_LOSS_ABS_MIN = MAX_IMG_LOSS_ABS_MIN;

    @Override
    public void optimizeMatching(
            double dtVarietyMax,
            GPStype gpsType,
            MinMaxPair clockPrecision,
            ProgressCallbackOptimizing progressCallbackOptimizing)
            throws Exception {
        Debug.getLog().fine("performing optimisation with dtVarietyMax=" + dtVarietyMax);
        // lets assume their are more log entrys than pictures
        newNumbers.clear();

        photosVec.clear();
        photosVec.addAll(photos);

        logsVec.clear();
        logsVec.addAll(logs);

        int maxImgLoss =
            (int)
                Math.min(
                    Math.ceil(Math.max(photos.size() * MAX_IMG_LOSS_REALTIVE, MAX_IMG_LOSS_ABS_MIN)), photos.size());
        int maxLogLoss =
            (int)Math.min(Math.ceil(Math.max(logs.size() * MAX_LOG_LOSS_REALTIVE, MAX_LOG_LOSS_ABS_MIN)), logs.size());
        // maxImgLoss = 1; //FIXME REMOVE ME

        // double shiftAtMinDt=shift.min - TINY;
        int lowestMissingMatches = Integer.MAX_VALUE;
        int noRemoveImgAtMin = 0;
        int noRemoveLogAtMin = 0;

        // try every shift
        // Debug.getLog().info("shift=" + shift + " step=" + STEP_SIZE + " maxImgLoss="+maxImgLoss+" (" +
        // getPhotosAll().size()+" of "
        // +logs.size() + ")");

        if (logs.size() == getPhotosAll().size()) { // if there is only one mathing.. just try this!
            Debug.getLog()
                .config("matching unique, try with skipImg=" + noRemoveImgAtMin + ", skipLog=" + noRemoveLogAtMin);

            lowestMissingMatches = applyShift(noRemoveImgAtMin, noRemoveLogAtMin, dtVarietyMax, lowestMissingMatches);
            // System.out.println("new Minimum lowestMissingMatches:" + lowestMissingMatches + " @
            // noRemoveImg="+noRemoveImgAtMin + "
            // noRemoveLog="+noRemoveLogAtMin);
        }

        // if (lowestMissingMatches == Integer.MAX_VALUE){
        if (lowestMissingMatches != 0) {

            //            MProgressMonitor mon = new MProgressMonitor(null, Language.getString(KEY +
            // ".monOptimize.title"),
            //                Language.getString(KEY + ".monOptimize.initNote"), -1, maxImgLoss * maxLogLoss);
            //            try {
            int step = 0;
            long maxStep = maxImgLoss * maxLogLoss;
            for (int noRemoveImg = 0; noRemoveImg != maxImgLoss; noRemoveImg++) {
                for (int noRemoveLog = 0; noRemoveLog != maxLogLoss; noRemoveLog++) {
                    // for(double curShift=shift.min; curShift<=shift.max; curShift+=STEP_SIZE){

                    if (progressCallbackOptimizing != null) {
                        if (progressCallbackOptimizing.isCanceled()) {
                            throw new InterruptedByUserException();
                        }

                        progressCallbackOptimizing.progress(getAlgName(), step, maxStep);
                        step++;
                    }

                    int lowestMissingMatchesTmp =
                        applyShift(noRemoveImg, noRemoveLog, dtVarietyMax, lowestMissingMatches);
                    if (lowestMissingMatchesTmp < lowestMissingMatches) {
                        noRemoveImgAtMin = noRemoveImg;
                        noRemoveLogAtMin = noRemoveLog;
                        lowestMissingMatches = lowestMissingMatchesTmp;
                        // System.out.println("new Minimum lowestMissingMatches:" + lowestMissingMatches + " @
                        // noRemoveImg="+noRemoveImg
                        // + " noRemoveLog="+noRemoveLog);
                    }
                }
            }

            if (lowestMissingMatches == Integer.MAX_VALUE) {
                throw new Exception(
                    "Could not find matching between logfile and images in Folder. Probably they do not belong together!");
            }

            Debug.getLog()
                .config(
                    "opt:"
                        + lowestMissingMatches
                        + "  @  skipImg="
                        + noRemoveImgAtMin
                        + ", skipLog="
                        + noRemoveLogAtMin);

            // verbose = true;
            lowestMissingMatches = applyShift(noRemoveImgAtMin, noRemoveLogAtMin, dtVarietyMax, lowestMissingMatches);
        }

        if (lowestMissingMatches == Integer.MAX_VALUE) {
            throw new TaggingException(
                "Create matching error: no matching between logfile and images in folder.",
                "Could not find matching between logfile and images in Folder. Probably they do not belong together!");
        }

        for (PhotoCube p : photosMatched) {
            newNumbers.add(p.logTmp.imageNumber);
        }
    }

    protected int applyShift(int noRemoveImg, int noRemoveLog, double dtVarietyMax, int lowestMissingMatches) {
        int missingMatches = noRemoveImg + noRemoveLog;
        int imgToGo = photos.size() - noRemoveImg;
        int logToGo = logs.size() - noRemoveLog;
        int bestCaseMissmatches = missingMatches + Math.abs(imgToGo - logToGo);
        if (bestCaseMissmatches >= lowestMissingMatches) {
            return bestCaseMissmatches;
        }

        int logIdx = noRemoveLog;
        int imgIdx = noRemoveImg;

        // syso("shift "+shift);
        logsUnmatched.clear();
        logsUnmatched.addAll(logs);
        photosMatched.clear();
        photosUnmatched.clear();
        photosUnmatched.addAll(photos);

        dtPair.reset();

        CPhotoLogLine log = logsVec.get(logIdx);
        PhotoCube img = photosVec.get(imgIdx);

        double diff = log.getTimestamp() - img.getTimestamp();
        boolean valid = dtPair.size(diff) <= dtVarietyMax;
        syso("init diff=" + diff + " valid=" + valid);

        do {
            syso("\n-----\n");
            syso("log[" + logIdx + "]=" + log);
            syso("img[" + imgIdx + "]=" + img);

            // wenn der akt match nich valide ist, versuche nächsten logeintrag solange es dadruch qualitativ besser
            // werden würde
            if (!valid) {
                syso("not valid, try to scroll logs");
                int logIdxNext = logIdx + 1;
                boolean validLast = false;
                while (logIdxNext < logs.size() && !validLast) {
                    CPhotoLogLine logNext = logsVec.get(logIdxNext);
                    double diffNext = logNext.getTimestamp() - img.getTimestamp();
                    boolean validNext = dtPair.size(diffNext) <= dtVarietyMax;
                    validLast = validNext;
                    logIdxNext++;
                }

                if (validLast) {
                    syso("logs scrolled by " + (logIdxNext - 1 - logIdx));
                    logIdx = logIdxNext - 1;
                    log = logsVec.get(logIdx);
                    diff = log.getTimestamp() - img.getTimestamp();
                    valid = true;
                }
            }

            // wenn der akt match nich valide ist, versuche nächstes photo solange es dadruch qualitativ besser werden
            // würde
            if (!valid) {
                syso("not valid, try to scroll imgs");
                int imgIdxNext = imgIdx + 1;
                boolean validLast = false;
                while (imgIdxNext < photos.size() && !validLast) {
                    PhotoCube imgNext = photosVec.get(imgIdxNext);

                    double diffNext = log.getTimestamp() - imgNext.getTimestamp();

                    boolean validNext = dtPair.size(diffNext) <= dtVarietyMax;
                    validLast = validNext;

                    imgIdxNext++;
                }

                if (validLast) {
                    syso("logs scrolled by " + (imgIdxNext - 1 - imgIdx));
                    imgIdx = imgIdxNext - 1;
                    img = photosVec.get(imgIdx);
                    diff = log.getTimestamp() - img.getTimestamp();
                    valid = true;
                }
            }

            if (!valid) {
                syso("breaking, still not valid!!");
                return Integer.MAX_VALUE;
            }

            // acept this match
            img.logTmp = log;
            logsUnmatched.remove(log);
            photosMatched.add(img);
            photosUnmatched.remove(img);

            // update QM tracking
            dtPair.update(diff);
            syso("curDtPair=" + dtPair);

            // scroll to next
            logIdx++;
            imgIdx++;
            if (logIdx >= logs.size() || imgIdx >= photos.size()) {
                break;
            }

            img = photosVec.get(imgIdx);
            log = logsVec.get(logIdx);

            diff = log.getTimestamp() - img.getTimestamp();
            valid = dtPair.size(diff) <= dtVarietyMax;

            imgToGo = photos.size() - imgIdx;
            logToGo = logs.size() - logIdx;
            bestCaseMissmatches = missingMatches + Math.abs(imgToGo - logToGo);
        } while (bestCaseMissmatches < lowestMissingMatches);

        // adding missing images panelty, and make dt positive
        missingMatches = photosUnmatched.size() + logsUnmatched.size();
        syso("#unmatched:  photos = " + photosUnmatched.size() + "   logs=" + logsUnmatched.size());
        syso("diffs:" + dtPair + " size=" + dtPair.size());

        return missingMatches;
    }

    @Override
    public String getAlgName() {
        return "MinDeviationAllImgages";
    }

    public static void main(String[] args) throws Exception {
        ITaggingAlgorithm alg = createNewDefaultTaggingAlgorithm();
        alg.loadLogfile(
            new File("/home/marco/mavinci/rikiola/test1/2014_06_12-rikolatest1/ftp/photo-Thu-12-Jun-2014_16-31-19.plg"),
            true);
        System.out.println("log in total: " + alg.getLogsAll().size());
    }
}
