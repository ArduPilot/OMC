/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.planemain.tagging;

import eu.mavinci.core.flightplan.CPhotoLogLine;
import eu.mavinci.core.flightplan.camera.GPStype;
import eu.mavinci.core.helper.MinMaxPair;
import eu.mavinci.desktop.main.debug.Debug;
import java.util.Vector;

public class TaggingAlgorithmLossyLog extends TaggingAlgorithmA {

    private Vector<PhotoCube> photosVec = new Vector<>();

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

        logsUnmatched.clear();
        photosMatched.clear();
        photosUnmatched.clear();

        dtPair.reset();
        for (CPhotoLogLine log : logs) {
            int i = log.imageNumber; // -1;
            // System.out.println("img No:"+i);
            if (photosVec.size() <= i) {
                logsUnmatched.add(log);
            } else {
                PhotoCube img = photosVec.get(i);
                img.logTmp = log;
                photosMatched.add(img);
                double diff = log.getTimestamp() - img.getTimestamp();
                dtPair.update(diff);
            }
        }

        photosUnmatched.addAll(photos);
        photosUnmatched.removeAll(photosMatched);

        if (dtPair.size() > dtVarietyMax) {
            throw new TaggingException(
                "Create matching error: time variance is " + dtPair + " ( " + dtPair.size() + ") > " + dtVarietyMax,
                "Could not find matching between logfile and images in Folder since the time variance is "
                    + dtPair
                    + " ( "
                    + dtPair.size()
                    + ") > "
                    + dtVarietyMax);
        }

        for (PhotoCube p : photosMatched) {
            newNumbers.add(p.logTmp.imageNumber);
        }
    }

    @Override
    public String getAlgName() {
        return "LossyLog";
    }
}
