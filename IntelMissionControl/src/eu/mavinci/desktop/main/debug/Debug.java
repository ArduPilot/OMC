/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.main.debug;

import eu.mavinci.core.desktop.main.debug.DebugLogListenerFactory;
import eu.mavinci.core.plane.ICAirplane;
import eu.mavinci.core.plane.listeners.IAirplaneListenerAll;
import eu.mavinci.core.plane.listeners.IAirplaneListenerGuiClose;
import eu.mavinci.core.plane.management.INewConnectionCallback;
import eu.mavinci.desktop.main.core.Application;
import eu.mavinci.desktop.main.core.IAppListener;
import eu.mavinci.plane.management.Airport;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Debug implements IAppListener {

    public static final Level WARNING = new MLevel("WARNING", 350);

    protected static final Logger log = Logger.getLogger("eu.mavinci");

    public static String KEY = "eu.mavinci.desktop.main.debug.Debug";
    public static String KEY_NO_OF_REPORTS_CHANGED = "DebugReportsNoChanged";
    public static final long bootTime = System.currentTimeMillis();

    public static long getUptimeMSec() {
        return System.currentTimeMillis() - bootTime;
    }

    private static boolean isInitIMC;

    public static void startThreadPrinter() {
        if (isInitIMC) {
            return;
        }

        isInitIMC = true;

        new StackPrinter();

        Debug.getLog().log(Level.FINE, "Stack trace printer started");
    }

    public static void setThreadErrorHandler() {
        UncaughtExceptionHandler handler =
            new UncaughtExceptionHandler() {

                @Override
                public void uncaughtException(Thread t, Throwable e) {
                    if (e instanceof ThreadDeath) {
                        return;
                    }

                    Level lev = Level.WARNING;
                    if (e instanceof NullPointerException
                            && e.getStackTrace()[0]
                                .getClassName()
                                .equals("org.jvnet.substance.utils.RolloverButtonListener")) {
                        lev = Level.FINE;
                    }

                    Debug.getLog().log(lev, "Exception in thread \"" + t.getName() + "\"", e);
                }
            };

        Thread.setDefaultUncaughtExceptionHandler(handler);

        Debug.getLog().log(Level.FINE, "thread error handler initialized");
    }

    public static void init() {
        // defaultPopupper = new DebugPopupListener();
        newConnectionListener = new NewConnectionListener();
        planes = new ArrayList<PlaneDebugListener>();

        // collect all exceptions from UI and other threads
        UncaughtExceptionHandler handler =
            new UncaughtExceptionHandler() {

                @Override
                public void uncaughtException(Thread t, Throwable e) {
                    if (e instanceof ThreadDeath) {
                        return;
                    }

                    Level lev = Level.WARNING;
                    if (e instanceof NullPointerException
                            && e.getStackTrace()[0]
                                .getClassName()
                                .equals("org.jvnet.substance.utils.RolloverButtonListener")) {
                        lev = Level.FINE;
                    }

                    Debug.getLog().log(lev, "Exception in thread \"" + t.getName() + "\"", e);
                }
            };

        Thread.setDefaultUncaughtExceptionHandler(handler);

        new StackPrinter();
    }

    /** @return the log */
    public static Logger getLog() {
        return log;
    }

    public static void printStackTrace(Object... args) {
        if (args != null) {
            int i = 0;
            for (Object o : args) {
                System.err.println("arg" + i + ":" + o);
                i++;
            }
        }

        (new Exception()).printStackTrace();
    }

    private static class StackPrinter extends Thread {

        public StackPrinter() {
            super("Stack Trace Printer Thread");
            setPriority(Thread.MIN_PRIORITY);
            this.start();
        }

        boolean guiClosing = false;

        public static Thread getThreadById(long id) {
            Thread currentThread = Thread.currentThread();
            ThreadGroup threadGroup = getRootThreadGroup(currentThread);
            int allActiveThreads = threadGroup.activeCount();
            Thread[] allThreads = new Thread[allActiveThreads];
            threadGroup.enumerate(allThreads);

            for (int i = 0; i < allThreads.length; i++) {
                Thread thread = allThreads[i];
                if (id == thread.getId()) {
                    return thread;
                }
            }

            return null;
        }

        private static ThreadGroup getRootThreadGroup(Thread thread) {
            ThreadGroup rootGroup = thread.getThreadGroup();
            while (true) {
                ThreadGroup parentGroup = rootGroup.getParent();
                if (parentGroup == null) {
                    break;
                }

                rootGroup = parentGroup;
            }

            return rootGroup;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(30 * 1000);
            } catch (InterruptedException e1) {
            }

            while (!guiClosing) {
                String s = "";
                s += "System Usage:\n";
                s += "HeapMemoryUsage:\t" + ManagementFactory.getMemoryMXBean().getHeapMemoryUsage() + "\n";
                s += "NonHeapMemoryUsage:\t" + ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage() + "\n";
                s +=
                    "ObjectPendingFinalizationCount:\t"
                        + ManagementFactory.getMemoryMXBean().getObjectPendingFinalizationCount()
                        + "\n";
                s +=
                    "SystemLoadAverage:\t" + ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage() + "\n";
                s += "All Stacktraces:\n==============\n";
                ThreadInfo[] threads;
                try {
                    threads = ManagementFactory.getThreadMXBean().dumpAllThreads(false, false);
                } catch (Exception e) {
                    Debug.getLog().log(Level.CONFIG, "problem dumping threads", e);
                    continue;
                }

                for (ThreadInfo t : threads) {
                    s += t.toString() + "\n";
                    StackTraceElement[] stack = t.getStackTrace();
                    for (StackTraceElement e : stack) {
                        s += "  " + e.toString() + "\n";
                    }

                    s += "==============\n\n";
                }

                Debug.getLog().log(Level.FINER, s);

                // deadlock detection and dumping
                ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
                long[] threadIds = threadBean.findDeadlockedThreads();
                int deadlockedThreads = threadIds != null ? threadIds.length : 0;
                boolean forceClose = false;
                if (deadlockedThreads > 0) {
                    Debug.getLog()
                        .log(Level.SEVERE, "DEADLOCKS DETECTED! Number of deadlocked threads: " + deadlockedThreads);
                    s = "";
                    for (long id : threadIds) {
                        ThreadInfo t = threadBean.getThreadInfo(id);
                        s += t.toString() + "\n";
                        StackTraceElement[] stack = t.getStackTrace();
                        for (StackTraceElement e : stack) {
                            s += "  " + e.toString() + "\n";
                        }

                        Thread deadlockThread = getThreadById(id);
                        if (deadlockThread != null) {
                            Debug.getLog().log(Level.SEVERE, "Try interrupt deadlock Thread: " + deadlockThread);
                            deadlockThread.interrupt();
                        } else {
                            Debug.getLog().log(Level.SEVERE, "could not get Thread by id" + id);
                            forceClose = true;
                        }

                        s += "==============\n\n";
                    }

                    Debug.getLog().log(Level.SEVERE, "DeadlockThreads:\n" + s);

                    if (forceClose) {
                        Debug.getLog()
                            .log(Level.SEVERE, "Force Application close since not all reads could be interrupted");
                        Application.closeAppForce();
                    }
                }

                try {
                    Thread.sleep(20 * 1000);
                } catch (InterruptedException e) {
                }
            }
        }

    }

    public static Vector<Throwable> earlyErrors = new Vector<Throwable>();

    @SuppressWarnings("unused")
    private static NewConnectionListener newConnectionListener;

    private static class NewConnectionListener implements INewConnectionCallback {
        public NewConnectionListener() {
            Airport.getInstance().addNewConnectionListener(this);
        }

        @Override
        public void newTcpConnectionArchieved(ICAirplane plane) {
            planes.add(new PlaneDebugListener(plane));
            Debug.getLog().log(Level.FINE, "new Airplane registerd in Airport");
        }
    }

    private static ArrayList<PlaneDebugListener> planes; // = new ArrayList<PlaneDebugListener>();

    private static class PlaneDebugListener implements IAirplaneListenerGuiClose {
        @SuppressWarnings("unused")
        private IAirplaneListenerAll debugWriter; // to prevent garbage collection of it

        public PlaneDebugListener(ICAirplane plane) {
            debugWriter = DebugLogListenerFactory.createStreamHandle(plane);
            plane.addListener(this);
        }

        @Override
        public void guiClose() {
            planes.remove(this);
        }

        @Override
        public boolean guiCloseRequest() {
            return true;
        }

        @Override
        public void storeToSessionNow() {}

    }

    private static Integer returnVal;

    @Override
    public boolean appRequestClosing() {
        return true;
    }

    @Override
    public void appIsClosing() {}

    @Override
    public void guiReadyLoaded() {
        // TODO Auto-generated method stub

    }

    public static class MLevel extends Level {

        public MLevel(String name, int value) {
            super(name, value);
        }

        private static final long serialVersionUID = -4174565869827938201L;

    }

    public static void reportIssueAppearance() {
        IssuesAppearanceCounter.INSTANCE.reportIssue();
    }

    public static void resetReportedIssues() {
        IssuesAppearanceCounter.INSTANCE.reset();
    }

    public static long reportedIssuesCount() {
        return IssuesAppearanceCounter.INSTANCE.issuesAppearanceCount.longValue();
    }

    public static boolean hasReportedIssues() {
        return IssuesAppearanceCounter.INSTANCE.hasReportedIssues();
    }

    private enum IssuesAppearanceCounter {
        INSTANCE;
        // TODO check if last run crashed, in past: zombieLockFileDetected
        private AtomicLong issuesAppearanceCount = new AtomicLong();

        void reportIssue() {
            issuesAppearanceCount.incrementAndGet();
        }

        boolean hasReportedIssues() {
            return issuesAppearanceCount.get() > 0;
        }

        void reset() {
            issuesAppearanceCount.set(0);
        }

    }

}
