/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.widgets;

import eu.mavinci.core.helper.IterativeEstimater;
import eu.mavinci.core.helper.StringHelper;
import eu.mavinci.desktop.main.debug.Debug;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Window;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;

/** This version of the progressmonitor is usable also from out of thread which are not the UI Thread!! */
public class MProgressMonitor extends ProgressMonitor implements IMProgressMonitor {

    public long tmpLon = 0;

    public MProgressMonitor(Component parentComponent, Object message, String note, int min, int max) {
        super(parentComponent, message, note, min, max);
        // Debug.printStackTrace(message);
        this.max = max;
        this.min = min;
        setMillisToDecideToPopup(0);
        setMillisToPopup(0);
        setProgress(min);
        constructionTime = System.currentTimeMillis();
        SwingUtilities.invokeLater(
            new Runnable() {
                @Override
                public void run() {
                    AccessibleContext context = getAccessibleContext();
                    if (context == null) {
                        return;
                    }

                    Accessible parent = context.getAccessibleParent();
                    if (parent instanceof Window) {
                        Window window = (Window)parent;
                        window.setAlwaysOnTop(true);
                        Dimension d = window.getSize();
                        d.width *= 2;
                        window.setSize(d);
                    }
                }
            });
    }

    boolean isShowEstimation = true;

    public boolean isShowEstimation() {
        return isShowEstimation;
    }

    public void setShowEstimation(boolean isShowing) {
        if (isShowEstimation == isShowing) {
            return;
        }

        isShowEstimation = isShowing;
        setProgress(currentNV);
    }

    long constructionTime;
    long lastTime = -1;
    int lastProgress = -1;
    IterativeEstimater estimator = new IterativeEstimater(20);
    long beforeLastEstimatorPush = 0;
    double lastEstimation = 0;
    static final long ESTIMATOR_UPDATE_INTERVAL = 300;

    @Override
    public void setProgressNote(final String note, final int progress) {
        if (orgNote.equals(note) && currentNV == progress) {
            return;
        }

        estimateProg(progress);
        final String addNoteFin = addNote;
        orgNote = note;
        final int progressF = checkProgress(progress);
        SwingUtilities.invokeLater(
            new Runnable() {
                @Override
                public void run() {
                    if (addNoteFin != null) {
                        MProgressMonitor.this.setNoteOrg(orgNote + addNoteFin);
                    } else {
                        MProgressMonitor.this.setNoteOrg(note);
                    }

                    MProgressMonitor.this.setProgressOrg(progressF);
                }
            });
    }

    int max;
    int min;

    @Override
    public int getMaximum() {
        return max;
    }

    @Override
    public void setMaximum(final int m) {
        this.max =
            m; // so it is right now avaliable, and not only after the runnable is executed, what could take some time
        SwingUtilities.invokeLater(
            new Runnable() {
                @Override
                public void run() {
                    MProgressMonitor.this.setMaximumOrg(m);
                }
            });
    }

    private void setMaximumOrg(int m) {
        super.setMaximum(m);
    }

    @Override
    public void setMinimum(final int m) {
        SwingUtilities.invokeLater(
            new Runnable() {
                @Override
                public void run() {
                    MProgressMonitor.this.setMinimumOrg(m);
                }
            });
    }

    private void setMinimumOrg(int m) {
        super.setMinimum(m);
    }

    @Override
    public void setNote(final String note) {
        orgNote = note;
        SwingUtilities.invokeLater(
            new Runnable() {
                @Override
                public void run() {
                    MProgressMonitor.this.setNoteOrg(note);
                }
            });
    }

    protected String orgNote = "";
    String addNote = null;

    private void setNoteOrg(String note) {
        super.setNote(note.trim());
    }

    private void estimateProg(int nv) {
        long thisTime = System.currentTimeMillis();
        if (nv <= getMinimum()) {
            return; // drop 0 progress infos for estimation
        }

        if (nv < lastProgress) { // reset everything on backward steps
            lastTime = -1;
            estimator.reset();
            beforeLastEstimatorPush = 0;
            lastEstimation = 0;
        }

        if (lastTime == -1) { // take first REAL progress as reference. this removes additional delay on startup of
            // progressMon
            lastTime = thisTime;
            lastProgress = nv;
            return;
        }

        if (lastTime == thisTime) {
            return; // shortcut
        }

        final int diffProg = nv - lastProgress;
        final long diffTime = thisTime - lastTime;
        final long usedTime = thisTime - constructionTime;
        double endTime = -1;
        if (diffProg > 0 && diffTime >= 100) { // at least 100ms progress, otherwise prediction will not be good enough
            final double estToGo = ((double)((getMaximum() - nv) * diffTime)) / diffProg;
            // System.out.println("max="+getMaximum() + " nv="+nv + " diffTime="+diffTime + " diffProg="+diffProg);
            // System.out.println("estToGo:"+estToGo);
            endTime = estToGo + thisTime;
            // System.out.println("endTime="+endTime);
            if (thisTime - beforeLastEstimatorPush < ESTIMATOR_UPDATE_INTERVAL) {
                estimator.setMean(lastEstimation);
                estimator.pushValue(endTime);
                // System.out.println("tiny update");
            } else {
                beforeLastEstimatorPush = thisTime;
                estimator.pushValue(endTime);
                lastEstimation = estimator.getMean();
                // System.out.println("big update");
            }
            // System.out.println("estToGo:"+(estimator.getMean()-thisTime));
            // System.out.println("esimation:"+estimator.getMean()+"\n");

            lastProgress = nv;
            lastTime = thisTime;
        }

        if (estimator.getCurrentCount() > 0) {
            final double toGo = estimator.getMean() - thisTime;
            if (toGo < 0) {
                estimator.reset();
                if (endTime > 0) {
                    estimator.pushValue(endTime);
                }
            }
        }

        if (estimator.getCurrentCount() > 0) {
            if (isShowEstimation()) {
                addNote =
                    "  "
                        + StringHelper.secToShortDHMS(usedTime / 1000.)
                        + " ("
                        + StringHelper.secToShortDHMS((estimator.getMean() - thisTime) / 1000.)
                        + ")";
            } else {
                addNote = null;
            }
        }
        // System.out.println("diffTime"+diffTime + " usedTime"+usedTime+" diffProg"+diffProg+ " addNote"+addNote);
    }

    protected int currentNV;

    @Override
    public void setProgress(final int nv) {
        if (currentNV == nv) {
            return;
        }

        currentNV = nv;
        estimateProg(nv);
        final String addNoteFin = addNote;
        final int progressF = checkProgress(nv);
        SwingUtilities.invokeLater(
            new Runnable() {
                @Override
                public void run() {
                    if (addNoteFin != null) {
                        MProgressMonitor.this.setNoteOrg(orgNote + addNoteFin);
                    }

                    MProgressMonitor.this.setProgressOrg(progressF);
                }
            });
    }

    public int getProgress() {
        return currentNV;
    }

    private int checkProgress(int nv) {
        if (nv > getMaximum()) {
            Debug.getLog()
                .log(Debug.WARNING, "progress " + nv + " biggern than allowed:" + getMaximum(), new Exception());
            return getMaximum();
        }

        if (nv < min) {
            Debug.getLog().log(Debug.WARNING, "progress " + nv + " smaller than allowed:" + min, new Exception());
            return min;
        }

        return nv;
    }

    private void setProgressOrg(int nv) {
        super.setProgress(nv);
    }

    @Override
    public void close() {
        SwingUtilities.invokeLater(
            new Runnable() {
                @Override
                public void run() {
                    MProgressMonitor.this.closeOrg();
                }
            });
    }

    private void closeOrg() {
        super.close();
    }
}
