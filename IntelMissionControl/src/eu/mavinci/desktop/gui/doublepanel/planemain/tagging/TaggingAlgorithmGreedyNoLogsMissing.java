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
import java.util.Iterator;

/**
 * this algorithm assumes there are only images missing, but no images too many (==log entries missing) in this case he
 * is fast and delivers nice results
 */
public class TaggingAlgorithmGreedyNoLogsMissing extends TaggingAlgorithmA {

    public static final double STEP_SIZE = 0.001;
    public static final double LARGING_SEARCH_INTERVAL = 1;

    double shiftAtMinDt;

    public void optimizeMatching(
            double dtVarietyMax,
            GPStype gpsType,
            MinMaxPair clockPrecision,
            ProgressCallbackOptimizing progressCallbackOptimizing)
            throws Exception {
        Debug.getLog().fine("performing optimisation with dtVarietyMax=" + dtVarietyMax);
        // lets assume their are more log entrys than pictures
        newNumbers.clear();

        if (logs.size() < photos.size()) {
            // verbose = true;
            syso(photos.toString());
            throw new Exception(
                "more Images ("
                    + photos.size()
                    + ") found than log entrys ("
                    + logs.size()
                    + "). Something is very wrong!");
        }

        if (getPhotosAll().first().getTimestamp() <= 0) {
            Debug.getLog()
                .warning(
                    "Workaround in tagging: Since image number is equal to triggering, match images even Camera clock has an error!");
            System.out.println(
                "Workaround in tagging: Since image number is equal to triggering, match images even Camera clock has an error!");
            dtVarietyMax = Double.POSITIVE_INFINITY;
            int toDel = logs.size() - getPhotosAll().size();
            if (toDel > 0) {
                int step = getPhotosAll().size() / toDel;
                for (int i = 0; i != toDel; ++i) {
                    int x = step / 2 + i * step;
                    int xi = 0;
                    for (CPhotoLogLine log : logs) {
                        System.out.println("+++");
                        if (xi == x) {
                            logs.remove(log);
                            break;
                        }

                        xi++;
                    }
                }
            } else if (toDel < 0) {
                toDel -= toDel;
                int step = getLogsAll().size() / toDel;
                for (int i = 0; i != toDel; ++i) {
                    int x = step / 2 + i * step;
                    int xi = 0;
                    for (PhotoCube p : getPhotosAll()) {
                        System.out.println("---");
                        if (xi == x) {
                            getPhotosAll().remove(p);
                            break;
                        }

                        xi++;
                    }
                }
            }
        }

        // TODO maybe min and max are mixed up...
        // System.out.println(photos);
        // System.out.println(logs);

        dtPair.reset();

        // double minShift = Math.floor(logs.last().timestamp-photos.last().timestamp-5);
        // double maxShift = Math.ceil(logs.first().timestamp-photos.first().timestamp+5);

        dtPair.update(logs.first().getTimestamp() - photos.first().getTimestamp());
        dtPair.update(logs.last().getTimestamp() - photos.last().getTimestamp());

        // if (clockPrecision!=null){
        // MinMaxPair tmp =dtPair.intersect(clockPrecision);
        // Debug.getLog().config("intersect clock prevision " + clockPrecision + " with default dt search range " +
        // dtPair + " -> "+ tmp);
        // dtPair = tmp;
        // dtPair=clockPrecision;
        // }

        dtPair.enlarge(LARGING_SEARCH_INTERVAL);

        double minDt = Double.POSITIVE_INFINITY;
        shiftAtMinDt = dtPair.min;
        // try every shift
        Debug.getLog()
            .config("shift " + dtPair + " step-size:" + STEP_SIZE + " (" + photos.size() + " of " + logs.size() + ")");

        if (logs.size() == photos.size()) { // if there is only one mathing.. just use this!
            shiftAtMinDt = dtPair.min;
            // dtVarietyMax = Double.POSITIVE_INFINITY;
            Debug.getLog().config("matching unique, use minimal shift:" + dtPair.min);
        } else {
            int steps = (int)Math.ceil(dtPair.size() / STEP_SIZE);
            if (steps > 1000) {
                steps *= 1.001; // try compensate potential numerical erros by this
            }
            //            MProgressMonitor mon = new MProgressMonitor(null, Language.getString(KEY +
            // ".monOptimize.title"),
            //                Language.getString(KEY + ".monOptimize.initNote"), -1, steps);
            int step = 0;
            for (double shift = dtPair.min; shift <= dtPair.max; shift += STEP_SIZE) {
                if (progressCallbackOptimizing != null) {
                    if (progressCallbackOptimizing.isCanceled()) {
                        throw new InterruptedByUserException();
                    }

                    progressCallbackOptimizing.progress(getAlgName(), step, steps);
                    step++;
                }

                double dt = applyShift(shift, dtVarietyMax, minDt);
                // System.out.println("---");
                // System.out.println(shift +"->"+dt);
                // dumpMatching();
                if (dt < minDt) {
                    shiftAtMinDt = shift;
                    minDt = dt;
                }
            }

            if (minDt == Double.POSITIVE_INFINITY) {
                throw new TaggingException(
                    "Create matching error: no matching between logfile and images in folder.",
                    "Could not find matching between logfile and images in Folder. Probably they do not belong together!");
            }
            // take best one
            Debug.getLog().fine("opt:" + shiftAtMinDt + "->" + minDt);
        }

        double dt = applyShift(shiftAtMinDt, dtVarietyMax, Double.POSITIVE_INFINITY);
        if (dt == Double.POSITIVE_INFINITY) {
            throw new TaggingException(
                "Create matching error: no matching between logfile and images in folder.",
                "Could not find matching between logfile and images in Folder. Probably they do not belong together!");
        }

        for (PhotoCube p : photosMatched) {
            newNumbers.add(p.logTmp.imageNumber);
        }
    }

    @Override
    public double getShift() {
        return shiftAtMinDt;
    }

    protected double applyShift(double shift, double dtVarietyMax, double maxValidQuality) {
        syso("shift " + shift);
        logsUnmatched.clear();
        logsUnmatched.addAll(logs);
        photosMatched.clear();
        photosUnmatched.clear();
        photosUnmatched.addAll(photos);

        double dt = 0;
        MinMaxPair dtPair = new MinMaxPair();
        // seach best
        Iterator<CPhotoLogLine> itLog = logs.iterator();
        Iterator<PhotoCube> itPhoto = photos.iterator();

        CPhotoLogLine lastLog;
        CPhotoLogLine nextLog = itLog.next();
        double lastDiff;
        int maxToSkip = logs.size() - photos.size();
        while (itPhoto.hasNext()) {
            PhotoCube photo = itPhoto.next();
            double time = shift + photo.getTimestamp();
            double nextDiff = nextLog.getTimestamp() - time;

            syso("");
            syso(photo.photoFiles[0].file + " exifTimestamp:" + photo.getTimestamp() + " ShiftedTime:" + time);
            // syso("1:"+itLog.hasNext());
            if (!itLog.hasNext()
                    || dtPair.sizeValid()
                        > dtVarietyMax // this line speeds up the testcase below by times 4, without changing
                    // the results
                    || Math.abs(dt)
                        >= maxValidQuality) { // this line is speeding up by more than times 10, in case of very
                // dtVarietyMax values
                photo.logTmp = nextLog;
                logsUnmatched.remove(nextLog);
                photosMatched.add(photo);
                photosUnmatched.remove(photo);
                dt += nextDiff;
                if (itPhoto.hasNext()) {
                    syso("ERR");
                    do {
                        itPhoto.next().logTmp = null;
                        dt = Double.POSITIVE_INFINITY;
                    } while (itPhoto.hasNext());
                }

                break;
            }

            maxToSkip++;
            do {
                lastLog = nextLog;
                lastDiff = nextDiff;
                // syso("2:"+itLog.hasNext());
                nextLog = itLog.next();
                nextDiff = nextLog.getTimestamp() - time;
                maxToSkip--;
                // syso(""+nextDiff);
                syso("maxToSkip" + maxToSkip);

                // System.out.println(nextDiff);
            } while (maxToSkip > 0 && itLog.hasNext() && Math.abs(nextDiff) < Math.abs(lastDiff));
            photo.logTmp = lastLog; // das letzte bevor die diff wieder länger wird
            logsUnmatched.remove(lastLog);
            photosMatched.add(photo);
            photosUnmatched.remove(photo);

            syso("" + lastDiff + " -> " + lastLog.imageNumber);
            dt += lastDiff; // *lastDiff;
            dtPair.update(lastDiff);
            // syso("3:"+itLog.hasNext());
        }
        // System.out.println("diffs:" + dtPair + " size=" + dtPair.size());
        if (dtPair.size() > dtVarietyMax) {
            return Double.POSITIVE_INFINITY;
        }

        return Math.abs(dt);
    }

    @Override
    public String getAlgName() {
        return "GreedyWithinVariance";
    }

}
