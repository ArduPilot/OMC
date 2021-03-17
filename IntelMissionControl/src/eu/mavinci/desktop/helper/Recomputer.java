/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.helper;

import com.intel.missioncontrol.concurrent.Dispatcher;
import com.intel.missioncontrol.helper.Ensure;
import eu.mavinci.core.desktop.listener.WeakListenerList;
import eu.mavinci.desktop.main.debug.Debug;
import java.lang.ref.WeakReference;
import java.util.TimerTask;
import java.util.logging.Level;
import javafx.util.Duration;

public class Recomputer implements IRecomputerListenerManager {

    protected WeakListenerList<IRecomputeListener> listener =
        new WeakListenerList<IRecomputeListener>("RecomputeListener");

    protected WeakReference<Runnable> r;
    protected Class<?> runnableClass;
    long noTrigger = 0;
    long noRuns = 0;
    Runnable persistentRunnableRef =
        null; // this is used to intentially prevent garbage collection of runnable if needed

    protected final Object waitMutex = new Object();

    protected void init(Runnable r) {
        this.r = new WeakReference<Runnable>(r);
        runnableClass = r.getClass();
    }

    protected void initPersistent(Runnable r) {
        init(r);
        persistentRunnableRef = r;
    }

    protected Recomputer() {}

    public Recomputer(Runnable r) {
        init(r);
    }

    private boolean isRunningRecompute = false;
    private boolean waitingForRunningRecompute = false;

    protected long getDelayBeforeStart() {
        return 100;
    }

    protected Runnable recomputeWrapper =
        new Runnable() {

            @Override
            public void run() {
                long sleepTime = getDelayBeforeStart();
                if (sleepTime > 0) {
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                    }
                }

                long thisRun = noRuns;
                noRuns++;
                long start = System.currentTimeMillis();
                try {
                    Runnable run = r.get();
                    if (run == null) {
                        Debug.getLog()
                            .log(
                                Level.INFO,
                                "stop recomputeCoverage due to GC of runnable:  "
                                    + thisRun
                                    + " "
                                    + Recomputer.this.toString());
                        return;
                    }

                    try {
                        // System.out.println("perform RUN:" + Recomputer.this.toString());
                        // since we haven't REALLY started jet, reset the waiting state here..
                        // especially if the recomputer was started twice within some milliseconds, this could save
                        // 50% of computations!
                        waitingForRunningRecompute = false;
                        run.run();
                    } catch (Throwable t) {
                        t.printStackTrace();
                        Debug.getLog()
                            .log(Level.SEVERE, "Problems perform run in Runnable  " + Recomputer.this.toString(), t);
                    }

                    if (run instanceof IRecomputeRunnable) {
                        final IRecomputeRunnable rUI = (IRecomputeRunnable)run;
                        try {
                            // its crucial that we wait here, otherwise we might do another computation round before
                            // this post is fullfilled, and then make this post for both of them directly after each
                            // other
                            Dispatcher.runOnUI(rUI::runLaterOnUIThread);
                        } catch (Exception e) {
                            Debug.getLog()
                                .log(
                                    Level.WARNING,
                                    "Problems running Recomputable-Later on UI Thread:"
                                        + rUI
                                        + "  "
                                        + Recomputer.this.toString(),
                                    e);
                        }
                    }

                    run = null;
                    // long t = System.currentTimeMillis();
                    // System.gc();//now we have a tiny timeframe to get rid of the runnable... GC will most likely miss
                    // this, if it is not
                    // triggered here!
                    // System.out.println("gc took: " + (System.currentTimeMillis()-t));
                } finally {
                    endRecomp(thisRun);
                    Debug.getLog()
                        .log(
                            Level.INFO,
                            "end RUN: "
                                + thisRun
                                + " "
                                + Recomputer.this.toString()
                                + ". \nIt took "
                                + (System.currentTimeMillis() - start) / 1000.
                                + " sec");
                }
            }

        };

    public void maybeStartAgainIfNotDoneYet(long delayMs) {
        final long oldRun = noRuns;
        Dispatcher.schedule(
            new TimerTask() {
                @Override
                public void run() {
                    if (noRuns > oldRun) {
                        // if for whatever reason we already running again, we can skip this old rerun request
                        // since in case this new run also encounters issues, it will itself shedule a new run...
                        return;
                    }

                    tryStartRecomp();
                }
            },
            Duration.millis(delayMs));
    }

    public boolean tryStartRecomp() {
        // System.out.printcln("isRunningRecompute;:"+isRunningRecompute + " "+this.toString());
        // Debug.printStackTrace(this, isRunningRecompute, waitingForRunningRecompute, noTrigger);
        synchronized (this) {
            noTrigger++;
            if (isRunningRecompute) {
                waitingForRunningRecompute = true;
                return false;
            }

            isRunningRecompute = true;
        }

        execute();
        return true;
    }

    protected void execute() {
        Dispatcher.post(recomputeWrapper);
    }

    public void endRecomp(long runNo) {
        boolean waitingForRunningRecompute;
        synchronized (this) {
            waitingForRunningRecompute = this.waitingForRunningRecompute;
            isRunningRecompute = false;
            if (this.waitingForRunningRecompute) {
                this.waitingForRunningRecompute = false;
                tryStartRecomp();
            }
        }
        // dont put this into the end block, otherwise the waiting for the waitMutex would block also through
        // synchronization on this that new starting trickers become blocking
        synchronized (waitMutex) {
            waitMutex.notifyAll();
        }

        for (IRecomputeListener l : listener) {
            // this could not be in any synchronize block, otherwise start triggers are blocked!
            // listeners have to synchronize on their own to prevent raice conditions
            try {
                Ensure.notNull(l, "l");
                l.recomputeReady(this, waitingForRunningRecompute, runNo);
            } catch (Exception e) {
                Debug.getLog().log(Level.WARNING, "cant propagate recomp ready message", e);
            }
        }
    }

    public synchronized boolean isRunningRecompute() {
        return isRunningRecompute;
    }

    @Override
    public String toString() {
        return super.toString() + " " + runnableClass + " -> " + r.get();
    }

    @Override
    public void addRecomputeListener(IRecomputeListener l) {
        listener.add(l);
    }

    @Override
    public void addRecomputeListenerAtBegin(IRecomputeListener l) {
        listener.addAtBegin(l);
    }

    @Override
    public void removeRecomputeListener(IRecomputeListener l) {
        listener.remove(l);
    }

}
