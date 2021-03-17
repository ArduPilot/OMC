/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.planemain.tagging;

import com.intel.missioncontrol.helper.Ensure;
import com.intel.missioncontrol.mission.MissionConstants;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.settings.OperationLevel;
import com.sun.jna.ptr.IntByReference;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import eu.mavinci.core.flightplan.CDump;
import eu.mavinci.core.flightplan.CPhotoLogLine;
import eu.mavinci.core.flightplan.PhotoLogLineType;
import eu.mavinci.core.helper.MinMaxPair;
import eu.mavinci.core.plane.CAirplane;
import eu.mavinci.core.plane.listeners.IAirplaneListenerLogReplay;
import eu.mavinci.core.plane.listeners.IAirplaneListenerPhoto;
import eu.mavinci.core.plane.sendableobjects.PhotoData;
import eu.mavinci.desktop.gui.asctec.AntennaInformation;
import eu.mavinci.desktop.gui.asctec.FalconLogLib;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.jsonGeotags.Geotag;
import eu.mavinci.desktop.gui.doublepanel.planemain.tree.maplayers.IMapLayer;
import eu.mavinci.desktop.helper.FileHelper;
import eu.mavinci.desktop.helper.InterruptedByUserException;
import eu.mavinci.desktop.helper.MFileFilter;
import eu.mavinci.desktop.helper.MathHelper;
import eu.mavinci.desktop.main.debug.Debug;
import eu.mavinci.plane.logfile.LogReaderVLG;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class TaggingAlgorithmA implements ITaggingAlgorithm {

    public static final String KEY = "eu.mavinci.desktop.gui.doublepanel.planemain.tagging.TaggingAlgorithm";

    public static final String UNMATCHED_FOLDER = "unmatched";

    public static boolean verbose = false;

    public static void syso(String s) {
        if (verbose) {
            System.out.println(s);
        }
    }

    public static ITaggingAlgorithm createNewDefaultTaggingAlgorithm() {
        return new TaggingAlgorithmMeta();
    }

    protected MinMaxPair dtPair = new MinMaxPair();

    protected TreeSet<String> fpHashes = new TreeSet<String>();
    protected File logFile = null;
    protected TreeSet<CPhotoLogLine> logs = new TreeSet<CPhotoLogLine>();

    protected TreeSet<CPhotoLogLine> logsUnmatched = new TreeSet<CPhotoLogLine>();

    protected boolean logConfusing = false;
    protected boolean logWasCorrupted = false;

    protected Vector<Integer> newNumbers = new Vector<Integer>();

    protected Vector<Integer> oldNumbers = new Vector<Integer>();

    protected TreeSet<PhotoCube> photos = new TreeSet<PhotoCube>();
    protected TreeSet<PhotoCube> photosMatched = new TreeSet<PhotoCube>();

    protected TreeSet<PhotoCube> photosUnmatched = new TreeSet<PhotoCube>();
    protected TreeSet<PhotoCube> photosCalib = new TreeSet<PhotoCube>();

    public File picFolder;

    protected boolean rtkPosAvaliable = false;
    protected double rtkLat;
    protected double rtkLon;
    protected double rtkAltMSL;
    protected double rtkGeoidSep;
    protected double rtkAvgTime =
        -1; // if ==-1: rtk base not set, -2 external source like ntrip or third party base, if ==0: position
    // manually entered, if >0: averaging time [secs]
    protected double rtkTimestamp; // timestamp when the position was created with averaging

    protected double connectorAvgLat = 0;
    protected double connectorAvgLon = 0;
    protected double connectorAvgAltWGS84 = 0;
    protected boolean connectorAvgPosAvaliable = false;

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

    @Override
    public TreeSet<PhotoCube> getPhotosCalib() {
        return photosCalib;
    }

    public boolean isRtkPosAvaliable() {
        return rtkPosAvaliable;
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
        return rtkAvgTime; // if <0: rtk base not set, if ==0: position manually entered, if >0: averaging time [secs]
    }

    public double getRtkTimestamp() {
        return rtkTimestamp; // timestamp when the position was created with averaging
    }

    @Override
    public boolean isLogfileConfusing() {
        return logConfusing;
    }

    @Override
    public boolean checkLogfileDownload() {
        if (logWasCorrupted) {
            return false;
        }

        int lastID = -1;
        for (CPhotoLogLine line : logs) {
            if (lastID == -1) {
                lastID = line.imageNumber;
            } else {
                if (line.imageNumber - lastID != 1) {
                    Debug.getLog()
                        .config("wrong step found in image sequence on imageNo:" + lastID + "->" + line.imageNumber);
                    System.out.println("problems with plg");
                    return false;
                }

                lastID = line.imageNumber;
            }
        }

        return true;
    }

    protected void dumpMatching() {
        for (PhotoCube photo : photosMatched) {
            System.out.println(photo); // photo.file + "\t"+photo.log.number);
        }
    }

    public static Comparator<File> autoSaveFpSorter =
        new Comparator<File>() {

            @Override
            public int compare(File o1, File o2) {
                // typical auto save filenames:
                // ladehof27mmTest_2015-10-29_11-42-17.fml
                // 2015-10-30_10-28-59.fml
                // last 23 chars are a timestamp...
                // just sort reversly for this, to get newest first!
                String n1 = o1.getName();
                if (n1.length() > 23) {
                    n1 = n1.substring(n1.length() - 23);
                }

                String n2 = o2.getName();
                if (n2.length() > 23) {
                    n2 = n2.substring(n2.length() - 23);
                }

                return -n1.compareTo(n2);
            }

        };

    public static Comparator<File> stdFpSorter =
        new Comparator<File>() {

            @Override
            public int compare(File o1, File o2) {
                long timeDiff = o1.lastModified() - o2.lastModified();
                return -(int)timeDiff;
            }

        };

    @Override
    public Vector<File> getFPs(File missionBaseFolder) {
        Vector<File> f = new Vector<File>();
        if (fpHashes.isEmpty()) {
            return f;
        }
        // taking care for sorting, since hashes could occour multiple times the same!
        File[] s1 =
            MissionConstants.getFlightplanFolder(missionBaseFolder)
                .listFiles(MFileFilter.fmlFilter.getWithoutFolders());
        Arrays.sort(s1, stdFpSorter); // make newest ones first

        File[] s2 =
            MissionConstants.getFlightplanAutosaveFolder(missionBaseFolder)
                .listFiles(MFileFilter.fmlFilter.getWithoutFolders());
        Arrays.sort(s2, autoSaveFpSorter); // make newest ones first

        LinkedList<File> all = new LinkedList<File>(Arrays.asList(s2));
        // first auto saved FPs, after that non auto saved
        all.addAll(Arrays.asList(s1));
        // System.out.println("List of FPS:" + all);
        // System.out.println("List of Hashes:"+fpHashes);

        for (File a : all) {
            String hash;
            try {
                hash = FileHelper.getHashXMLwithoutHashDumpAndComments(a).toUpperCase();
            } catch (IOException e) {
                continue;
            }
            // System.out.println("FP:"+a + " -> " + hash);
            if (fpHashes.contains(hash)) {
                fpHashes.remove(hash);
                f.add(a);
            }
        }

        return f;
    }

    public File getLogFile() {
        return logFile;
    }

    @Override
    public TreeSet<CPhotoLogLine> getLogsAll() {
        return logs;
    }

    @Override
    public TreeSet<CPhotoLogLine> getLogsUnmatched() {
        return logsUnmatched;
    }

    @Override
    public MinMaxPair getMinMaxPair() {
        return dtPair;
    }

    @Override
    public TreeSet<PhotoCube> getPhotosAll() {
        return photos;
    }

    @Override
    public TreeSet<PhotoCube> getPhotosMatched() {
        return photosMatched;
    }

    @Override
    public TreeSet<PhotoCube> getPhotosUnmatched() {
        return photosUnmatched;
    }

    @Override
    public double getShift() {
        return dtPair.min;
    }

    @Override
    public void clearLogfiles() {
        logWasCorrupted = false;
        logConfusing = false;
        this.logFile = null;
        logs.clear();
        getLogsUnmatched().clear();
        fpHashes.clear();
        rtkPosAvaliable = false;
        this.rtkLat = 0;
        this.rtkLon = 0;
        this.rtkAltMSL = 0;
        this.rtkGeoidSep = 0;
        this.rtkAvgTime = -1;
        this.rtkTimestamp = 0;
    }

    boolean isVLGreading = false;

    IAirplaneListenerPhoto photoListener =
        new IAirplaneListenerPhoto() {
            @Override
            public void recv_photo(PhotoData photo) {
                System.out.println("read photo:" + photo);
                logs.add(new CPhotoLogLine(photo));
            }
        };

    IAirplaneListenerLogReplay replayListener =
        new IAirplaneListenerLogReplay() {

            @Override
            public void replayStopped(boolean stopped) {
                // System.out.println("replay stopped:"+stopped);
                if (stopped) {
                    isVLGreading = false;
                }
            }

            @Override
            public void replaySkipPhase(boolean isSkipping) {}

            @Override
            public void replayPaused(boolean paused) {}

            @Override
            public void replayFinished() {
                // System.out.println("replay finish");
                isVLGreading = false;
            }

            @Override
            public void elapsedSimTime(double secs, double secsTotal) {
                // System.out.println("replac " + secs +" of " + secsTotal);
            }
        };

    @Override
    public void loadLogfile(File logFile, boolean allowMultiFlashTags) throws Exception {
        // Debug.printStackTrace(logFile,allowMultiFlashTags);
        logWasCorrupted = false;
        logConfusing = false;

        connectorAvgLat = 0;
        connectorAvgLon = 0;
        connectorAvgAltWGS84 = 0;
        connectorAvgPosAvaliable = false;
        int sumConAvg = 0;

        double connectorTrueLat = 0;
        double connectorTrueLon = 0;
        double connectorTrueAltWGS84 = 0;
        int sumConTrue = 0;
        boolean connectorTrueUnique = true;
        double typicalGPSclockOffset = Double.NaN;

        try {
            if (MFileFilter.ascTecLogFolder.acceptTrinityLog(logFile)) {
                AntennaInformation info = new AntennaInformation();
                IntByReference num = new IntByReference();
                logs.addAll(FalconLogLib.getPhotoLogLines(logFile, info, num));

                if (num.getValue() > 0) {
                    this.rtkLat = info.lat;
                    this.rtkLon = info.lon;
                    Debug.getLog().info("Antenna height is " + info.height);
                    this.rtkAltMSL = info.height;
                    this.rtkGeoidSep =
                        0; // actually the lib already returns WGS84 height, so 0 is correct here! (same with sirius)
                    this.rtkPosAvaliable = true;
                }
            } else if (MFileFilter.photoJsonFilter.accept(logFile.getName())) {
                try (InputStream stream = new FileInputStream(logFile);
                    BufferedReader br = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                    String strLine;
                    while ((strLine = br.readLine()) != null) {
                        strLine = strLine.trim();
                        if (strLine.startsWith("[")) {
                            strLine = strLine.substring(1, strLine.length());
                        }

                        if (strLine.endsWith(",") || strLine.endsWith("]")) {
                            strLine = strLine.substring(0, strLine.length() - 1);
                        }

                        strLine = strLine.trim();
                        if (strLine.isEmpty()) {
                            continue;
                        }

                        logs.add(new CPhotoLogLine(Geotag.fromJson(strLine)));
                    }
                } catch (Exception e) {
                    Debug.getLog().log(Level.WARNING, "cant parse logfile", e);
                }
            } else if (MFileFilter.vlgFilter.accept(logFile.getName())
                    || MFileFilter.vlgZipFilter.accept(logFile.getName())) {
                CAirplane plane = new CAirplane();
                plane.addListener(photoListener);
                plane.addListener(replayListener);

                LogReaderVLG reader = new LogReaderVLG(plane, logFile);
                isVLGreading = true;
                reader.dispatchEventsInUIthread = false;
                reader.startSimulation(Float.POSITIVE_INFINITY);
                while (isVLGreading) {
                    Thread.sleep(10);
                }
            } else if (MFileFilter.photoLogFilter.accept(logFile.getName())) {
                try (FileInputStream in = new FileInputStream(logFile);
                    BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
                    TreeMap<Integer, CPhotoLogLine> logsMap = new TreeMap<Integer, CPhotoLogLine>();

                    String strLine;
                    // Read File Line By Line

                    int countImprovedLines = 0;
                    double sumTimeDifference = 0;
                    boolean hasSomePhoto3Tag = false;

                    while ((strLine = br.readLine()) != null) {
                        strLine = strLine.toUpperCase();
                        if (CPhotoLogLine.isMaybeParseableLine(strLine)) {
                            try {
                                CPhotoLogLine plg = new CPhotoLogLine(strLine);

                                if (plg.type == PhotoLogLineType.FLASH) {
                                    hasSomePhoto3Tag = true;
                                }

                                CPhotoLogLine otherPlg = logsMap.get(plg.imageNumber);
                                if (otherPlg != null) {
                                    if (Double.isNaN(typicalGPSclockOffset)) {
                                        typicalGPSclockOffset = plg.getTimestamp() - otherPlg.getTimestamp();
                                        Debug.getLog()
                                            .log(Level.FINE, "typicalGPSclockOffset=" + typicalGPSclockOffset);
                                    }
                                    // maybe fix leapsecond offset occuring during flight
                                    double offset =
                                        plg.getTimestamp() - otherPlg.getTimestamp() - typicalGPSclockOffset;
                                    //
                                    // more than 1/2 day offset...
                                    // if a flight goes over midnight, the photo3 timestamps may have strange almost one
                                    // day backward jumps for
                                    // some images
                                    // since the GPS only delivers sec since start of the day, and if the AP assumes a
                                    // different day than the
                                    // GPS, this leads to errors...
                                    int days = (int)Math.round(((double)(offset)) / (24 * 60 * 60));
                                    days = MathHelper.intoRange(days, -1, 1);
                                    days *= 24 * 60 * 60;
                                    int maxleap = 6;
                                    offset =
                                        MathHelper.intoRange(
                                            Math.round(offset),
                                            -maxleap + days,
                                            maxleap + days); // max. 6 leap seconds,
                                    // also neg. could be ok

                                    // if (offset!=0) {
                                    plg.setTimestampRaw(plg.getTimestampRaw() - offset - typicalGPSclockOffset);
                                    if (Math.abs(offset) > 24 * 60 * 60 / 2) {
                                        Debug.getLog()
                                            .log(Level.FINE, "detecting over midnight flight offset=" + offset);
                                    }

                                    Debug.getLog()
                                        .log(
                                            Level.FINE,
                                            "reducing plg timestamp by offset="
                                                + offset
                                                + " plus typOffset:"
                                                + typicalGPSclockOffset);
                                    // }

                                    if (plg.isBetterThan(otherPlg)) {

                                        // MM: remove this check, since sometimes B110 timestamps and AP clock is VERY
                                        // out of sync! (don'T know
                                        // reason jet)
                                        // && Math.abs(plg.getTimestamp()
                                        // - otherPlg.getTimestamp()) < 1000) {

                                        // replacing worse one
                                        // System.out.println("replacing: " +
                                        // otherPlg + " -> " + plg);
                                        if (plg.type == PhotoLogLineType.FLASH) {
                                            countImprovedLines++;
                                            sumTimeDifference += plg.getTimestamp() - otherPlg.getTimestamp();
                                            // System.out.println(" timeDiff:"+(plg.getTimestamp()
                                            // - otherPlg.getTimestamp()));
                                        }

                                        logs.remove(otherPlg);
                                        logsMap.put(plg.imageNumber, plg);
                                        logs.add(plg);
                                    } else if (plg.type == PhotoLogLineType.FLASH) { // not better, but equal
                                        otherPlg.hasSeenAnotherFlashPlg = true;
                                        if (allowMultiFlashTags) {
                                            // int sOld = logs.size();

                                            logsMap.put(plg.imageNumber, plg);
                                            logs.add(plg); // multiple photo3 tags are important for rikola

                                            // System.out.println("add regardless id was known before! "
                                            // + otherPlg + " (from " + plg+") "+(logs.size()-sOld));
                                        } else {
                                            Debug.getLog()
                                                .log(
                                                    Level.WARNING,
                                                    "mupltiple feedbacks for the same image no: "
                                                        + plg.imageNumber
                                                        + " received");
                                            // System.out.println("upgrading to FLASH triggering: "
                                            // + otherPlg + " (from " + plg+")");
                                        }
                                    } else {
                                        // skip this log entry since it is not
                                        // better
                                        // System.out.println("skip " + plg);
                                    }
                                } else {
                                    // adding new one
                                    logsMap.put(plg.imageNumber, plg);
                                    logs.add(plg);
                                    // System.out.println("add normal");
                                }

                            } catch (Throwable e1) {
                                // was not a log entry?
                                Debug.getLog()
                                    .log(
                                        Debug.WARNING,
                                        "problems reading photolog-line:" + strLine + "\nIn File" + logFile,
                                        e1);
                                logWasCorrupted = true;
                            }
                        } else if (strLine.startsWith(CDump.prefixLogLine.toUpperCase())) {
                            // System.out.println("hashLine="+strLine);
                            String[] parts = strLine.split(";");
                            if (parts.length == 3 && parts[2].startsWith(CDump.prefixFPhash.toUpperCase())) {
                                // System.out.println("added!"+parts[2].substring(CDump.prefixFPhash.length()));
                                fpHashes.add(parts[2].substring(CDump.prefixFPhash.length()));
                            }
                        } else if (strLine.startsWith("$CONNPOS")) {
                            // System.out.println("hashLine="+strLine);
                            String[] parts = strLine.split(";");
                            if (parts.length >= 11) {
                                try {
                                    Double connectorLat = Double.parseDouble(parts[1]);
                                    Double connectorLon = Double.parseDouble(parts[2]);
                                    Double connectorAltMSL = Double.parseDouble(parts[3]);
                                    Double connectorGeoidSep = Double.parseDouble(parts[4]);

                                    Double rtkLat = Double.parseDouble(parts[5]);
                                    Double rtkLon = Double.parseDouble(parts[6]);
                                    Double rtkAltMSL = Double.parseDouble(parts[7]);
                                    Double rtkGeoidSep = Double.parseDouble(parts[8]);
                                    Double rtkAvgTime = Double.parseDouble(parts[9]);
                                    Double rtkTimestamp = Double.parseDouble(parts[10]);

                                    if (this.rtkAvgTime != -1) {
                                        if (this.rtkAvgTime == -2 && rtkAvgTime == -2) {
                                            // everything is fine, both external
                                        } else if (this.rtkAvgTime > 0 && rtkAvgTime > 0) {
                                            // everything is fine,both self avg
                                        } else if (this.rtkAvgTime == 0 && rtkAvgTime == 0) {
                                            // everything is fine,both manual
                                        } else {
                                            logConfusing = true;
                                            Debug.getLog()
                                                .log(
                                                    Debug.WARNING,
                                                    "ambiguously RTK baseposition mode detected: " + strLine);
                                        }
                                    }

                                    if (0 != connectorLat && 0 != connectorLon) {
                                        try {
                                            connectorAvgLat += connectorLat;
                                            connectorAvgLon += connectorLon;
                                            connectorAvgAltWGS84 += connectorAltMSL + connectorGeoidSep;
                                            sumConAvg++;
                                        } catch (ArithmeticException ae) {
                                            Debug.getLog()
                                                .log(Level.WARNING, "Arithmetic problem, possible overflow " + ae);
                                        }
                                    }

                                    if (rtkAvgTime == -1) {
                                        // skip this! nothing defined jet
                                    } else if (rtkAvgTime == -2) {
                                        // use external data source
                                        if (sumConTrue == 0) {
                                            this.rtkLat = rtkLat;
                                            this.rtkLon = rtkLon;
                                            this.rtkAltMSL = rtkAltMSL;
                                            this.rtkGeoidSep = rtkGeoidSep;
                                            this.rtkAvgTime = -2;
                                        } else if (this.rtkLat != rtkLat
                                                || this.rtkLon != rtkLon
                                                || this.rtkAltMSL != rtkAltMSL
                                                || this.rtkGeoidSep != rtkGeoidSep
                                                || this.rtkAvgTime != -2) {
                                            connectorTrueUnique = false;
                                        }

                                        rtkPosAvaliable = true;
                                        sumConTrue++;
                                        connectorTrueLat += rtkLat;
                                        connectorTrueLon += rtkLon;
                                        connectorTrueAltWGS84 += rtkAltMSL + rtkGeoidSep;
                                    } else {
                                        // internal pro connector source. maybe manually entered position (avgTime==0)

                                        if (rtkPosAvaliable
                                                && !logs
                                                    .isEmpty()) { // moving base is ok as long no images are triggered
                                            // jet
                                            if (this.rtkLat != rtkLat
                                                    || this.rtkLon != rtkLon
                                                    || this.rtkAltMSL != rtkAltMSL
                                                    || this.rtkGeoidSep != rtkGeoidSep
                                                    || this.rtkAvgTime != rtkAvgTime
                                                    || this.rtkTimestamp != rtkTimestamp) {
                                                logConfusing = true;

                                                this.rtkLat = rtkLat;
                                                this.rtkLon = rtkLon;
                                                this.rtkAltMSL = rtkAltMSL;
                                                this.rtkGeoidSep = rtkGeoidSep;
                                                this.rtkAvgTime = rtkAvgTime;
                                                this.rtkTimestamp = rtkTimestamp;

                                                Debug.getLog()
                                                    .log(
                                                        Level.SEVERE,
                                                        "ambiguously RTK baseposition detected. If seem you have changed base position while recording images: "
                                                            + strLine);
                                            }
                                        } else {
                                            rtkPosAvaliable = true;
                                            this.rtkLat = rtkLat;
                                            this.rtkLon = rtkLon;
                                            this.rtkAltMSL = rtkAltMSL;
                                            this.rtkGeoidSep = rtkGeoidSep;
                                            this.rtkAvgTime = rtkAvgTime;
                                            this.rtkTimestamp = rtkTimestamp;
                                        }
                                    }

                                } catch (NumberFormatException e) {
                                    Debug.getLog().log(Level.WARNING, "parsing double problem" + e);
                                }
                            }
                        } else {
                            if (!strLine.isEmpty() && !strLine.startsWith(CPhotoLogLine.LINE_LOG_PREFIX)) {
                                logWasCorrupted = true;
                                Debug.getLog().log(Level.FINE, "broken logfile entry:" + strLine);
                            }
                            // System.out.println("unmatched line:"+strLine);
                        }
                    }

                    // this is some magic code to shift timestamps of non flash
                    // tiggered images, to reduce time jitter
                    if (hasSomePhoto3Tag && countImprovedLines > 0) {
                        Debug.getLog()
                            .fine(
                                "Shifting some timestamps by: sumTimeDifference:"
                                    + sumTimeDifference
                                    + " countImprovedLines:"
                                    + countImprovedLines
                                    + " shift:"
                                    + (sumTimeDifference / countImprovedLines));
                        sumTimeDifference /= countImprovedLines;
                        // System.out.println("sumTimeDifference:"+sumTimeDifference
                        // +" countImprovedLines:"+countImprovedLines);
                        for (CPhotoLogLine plg : logs) {
                            if (plg.type != PhotoLogLineType.FLASH) {
                                Debug.getLog()
                                    .fine(
                                        "shifting timestamp: plg:"
                                            + plg
                                            + " -> "
                                            + (plg.getTimestamp() + sumTimeDifference));
                                plg.setTimestampRaw(plg.getTimestamp() + sumTimeDifference);
                                // System.out.println(" -> "+plg.getTimestamp() );
                            }
                        }
                    }

                } catch (FileNotFoundException e2) {
                    Debug.getLog().log(Level.WARNING, "File not found " + e2);
                }
            }
        } catch (IOException e1) {
            Debug.getLog().log(Level.WARNING, "problems reading photolog: " + logFile, e1);
            logWasCorrupted = true;
        }

        if (sumConAvg >= 1) {
            connectorAvgAltWGS84 /= sumConAvg;
            connectorAvgLat /= sumConAvg;
            connectorAvgLon /= sumConAvg;
            connectorAvgPosAvaliable = true;
        }

        if (rtkAvgTime == -2 && connectorTrueUnique == false) {
            rtkLon = connectorTrueLat / sumConTrue;
            rtkLon = connectorTrueLon / sumConTrue;
            rtkAltMSL = connectorTrueAltWGS84 / sumConTrue;
            rtkGeoidSep = 0;
        }

        if (logs.size() == 0) {
            logWasCorrupted = true;
            throw new Exception("No (valid) Photo-Logentrys found in file " + logFile);
        }

        if (logConfusing) {
            Debug.getLog()
                .log(
                    Debug.WARNING,
                    "Logfiles "
                        + logFile
                        + " has confusing content. If this repeats, please report *.plg files to MAVinci support.");
        }

        /*System.out.println("----------");
        int k= 0;
        for (CPhotoLogLine log : logs){
            k++;
            System.out.println(k+"\t"+log.getTimestamp() + "\t"+ log.lat + "\t"+log.lon+"\t"+(log.gps_altitude_cm/100f));
        }
        System.out.println("----------");*/

        Debug.getLog().config("Logfiles loaded. Now " + logs.size() + " lines are avaliable in total");
    }

    @Override
    public void loadPictures(
            File picFolder,
            boolean clear,
            int noFilesPerCube,
            boolean generatePreview,
            ProgressCallbackImgLoading progressCallbackImgLoading)
            throws Exception {
        this.picFolder = picFolder;
        File[] photoFiles = picFolder.listFiles(MFileFilter.jpegFilter.getWithoutFolders());
        Ensure.notNull(photoFiles, "TaggingAlgorithmA : photoFiles");
        Arrays.sort(
            photoFiles); // especially for multi band stuff its important to get the bands always in the same order
        // System.out.println("folder="+picFolder+
        // "allFiles:"+Arrays.asList(picFolder.listFiles()));
        // System.out.println("folder="+picFolder+
        // "photosRaw:"+Arrays.asList(PhotoCubes));
        //        mon.setMaximum(photoFiles.length);
        if (clear) {
            getPhotosAll().clear();
            photosMatched.clear();
            photosUnmatched.clear();
            oldNumbers.clear();
        }

        int prog = 0;
        int i = 0;
        // boolean hasWarnedClock=false;
        PhotoCube curPhotoCube = null;
        int curFillLevel = 0;
        for (File f : photoFiles) {
            if (progressCallbackImgLoading != null) {
                if (progressCallbackImgLoading.isCanceled()) {
                    throw new InterruptedByUserException();
                }

                progressCallbackImgLoading.progress(f, prog, photoFiles.length);
            }

            prog++;
            if (DependencyInjector.getInstance()
                            .getInstanceOf(ISettingsManager.class)
                            .getSection(GeneralSettings.class)
                            .getOperationLevel()
                        != OperationLevel.DEBUG
                    && f.getName().toLowerCase().startsWith(PhotoFile.PREFIX_PREVIEW_IMG)) {
                continue;
            }

            if (!f.exists() || f.length() == 0) {
                System.out.println("img corrupted");
                Debug.getLog().severe("This image-file seems to be corrupted " + f.getAbsolutePath());
                continue;
            }

            if (curPhotoCube == null) {
                curFillLevel = 0;
                curPhotoCube = new PhotoCube(noFilesPerCube);
                curPhotoCube.noInFolderTmp = i;
                i++;
            }

            PhotoFile curPhotoFile = new PhotoFile(f);
            if (generatePreview) {
                curPhotoFile.generateThumpFile();
            }

            if (!curPhotoFile.isTimestampValid()) {
                Debug.getLog().severe("The exif's timestamp seems to be invalid in  " + f.getAbsolutePath());
                continue;
            }

            curPhotoCube.photoFiles[curFillLevel] = curPhotoFile;
            curFillLevel++;
            Debug.getLog()
                .fine(
                    "Load file no "
                        + prog
                        + " ("
                        + getPhotosAll().size()
                        + ") : "
                        + curPhotoFile
                        + " into cube no:"
                        + (i)
                        + " at band No "
                        + curFillLevel);

            if (curFillLevel >= noFilesPerCube) {
                getPhotosAll().add(curPhotoCube);
                curPhotoCube = null;
            }
        }

        if (curPhotoCube != null) {
            getPhotosAll().add(curPhotoCube);
        }

        /*System.out.println("----------");
        int k = 0;
        for (PhotoCube photo : getPhotosAll()){
            k++;
            System.out.println(k+"\t" + photo.getTimestamp() + "\t"+ photo.photoFiles[0].file);
        }
        System.out.println("----------");*/

        sortoutCalibPhotos();
        Debug.getLog().config(picFolder + " -> no imgages=" + getPhotosAll().size());
        if (getPhotosAll().size() == 0
                && DependencyInjector.getInstance()
                        .getInstanceOf(ISettingsManager.class)
                        .getSection(GeneralSettings.class)
                        .getOperationLevel()
                    == OperationLevel.USER) {
            throw new Exception("No JPEG-Photos found in Folder " + picFolder);
        }
    }

    protected void sortoutCalibPhotos() {
        for (Iterator<PhotoCube> it = getPhotosAll().iterator(); it.hasNext(); ) {
            PhotoCube cube = it.next();
            PhotoFile photo = cube.photoFiles[0];
            ExifInfos exif = photo.getExif();
            if (exif.meteringMode == 4 && exif.model.equals("RedEdge")) { // maybe
                it.remove();
                photosCalib.add(cube);
            }
        }

        Debug.getLog().info(picFolder + " -> no imgages done in calibration mode=" + getPhotosCalib().size());
    }

    @Override
    public void loadPictures(
            MapLayerMatching matching, boolean generatePreview, ProgressCallbackImgLoading progressCallbackImgLoading)
            throws Exception {
        // System.out.println("folder="+picFolder+
        // "allFiles:"+Arrays.asList(picFolder.listFiles()));
        // System.out.println("folder="+picFolder+
        // "photosRaw:"+Arrays.asList(PhotoCubes));
        File unmatchedFolder = new File(matching.getImagesFolder(), TaggingAlgorithmA.UNMATCHED_FOLDER);
        File[] photoFilesUnmatched = null;
        int count = matching.getPicsLayer().getLayers().size();
        if (unmatchedFolder.exists()) {
            photoFilesUnmatched = unmatchedFolder.listFiles(MFileFilter.jpegFilter.getWithoutFolders());
            Ensure.notNull(photoFilesUnmatched, "photoFilesUnmatched");
            Arrays.sort(
                photoFilesUnmatched); // especially for multi band stuff its important to get the bands always in
            // the same order
            count += photoFilesUnmatched.length;
        }

        this.picFolder = null;
        getPhotosAll().clear();
        photosMatched.clear();
        photosUnmatched.clear();
        oldNumbers.clear();

        int i = 0;
        for (IMapLayer layer : matching.getPicsLayer().getLayers()) {
            MapLayerMatch match = (MapLayerMatch)layer;
            PhotoCube pf = match.getResourceCube();
            if (progressCallbackImgLoading != null) {
                if (progressCallbackImgLoading.isCanceled()) {
                    throw new InterruptedByUserException();
                }

                progressCallbackImgLoading.progress(pf.photoFiles[0].file, i, count);
            }

            pf.noInFolderTmp = i;
            i++;
            getPhotosAll().add(pf);
            oldNumbers.add(match.getPhotoLogLine().imageNumber);
        }

        PhotoCube curPhotoCube = null;
        int curFillLevel = 0;
        int noFilesPerCube = matching.getNumberOfImagesPerPosition();
        if (photoFilesUnmatched != null) {
            for (File f : photoFilesUnmatched) {
                if (DependencyInjector.getInstance()
                                .getInstanceOf(ISettingsManager.class)
                                .getSection(GeneralSettings.class)
                                .getOperationLevel()
                            != OperationLevel.DEBUG
                        && f.getName().toLowerCase().startsWith(PhotoFile.PREFIX_PREVIEW_IMG)) {
                    continue;
                }

                if (!f.exists() || f.length() == 0) {
                    System.out.println("img corrupted");
                    Debug.getLog().severe("This image-file seems to be corrupted " + f.getAbsolutePath());
                    continue;
                }

                if (progressCallbackImgLoading != null) {
                    if (progressCallbackImgLoading.isCanceled()) {
                        throw new InterruptedByUserException();
                    }
                }

                if (curPhotoCube == null) {
                    curFillLevel = 0;
                    curPhotoCube = new PhotoCube(noFilesPerCube);
                    curPhotoCube.noInFolderTmp = i;
                    i++;
                }

                PhotoFile curPhotoFile = new PhotoFile(f);
                if (generatePreview) {
                    curPhotoFile.generateThumpFile();
                }

                curPhotoCube.photoFiles[curFillLevel] = curPhotoFile;
                curFillLevel++;
                Debug.getLog()
                    .fine("Load file: " + curPhotoFile + " into cube no:" + (i) + " at band No " + curFillLevel);

                if (curFillLevel >= noFilesPerCube) {
                    getPhotosAll().add(curPhotoCube);
                    curPhotoCube = null;
                }
            }

            if (curPhotoCube != null) {
                getPhotosAll().add(curPhotoCube);
            }
        }

        if (getPhotosAll().size() == 0) {
            throw new Exception("No JPEG-Photos found in Folder " + picFolder);
        }

        sortoutCalibPhotos();
    }

    @Override
    public void loadPicturesFromAppLog(
            File logFile, boolean generatePreview, ProgressCallbackImgLoading progressCallbackImgLoading)
            throws Exception {
        this.picFolder = null;
        getPhotosAll().clear();
        photosMatched.clear();
        photosUnmatched.clear();
        oldNumbers.clear();
        try (FileInputStream in = new FileInputStream(logFile);
            BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
            String strLine;
            // Read File Line By Line
            int maxNo = -1;
            int i = 0;
            try {
                while ((strLine = br.readLine()) != null) {
                    if (progressCallbackImgLoading != null && progressCallbackImgLoading.isCanceled()) {
                        throw new InterruptedByUserException();
                    }
                    // strLine = strLine.toUpperCase();
                    // System.out.println(strLine);
                    if (strLine.startsWith("INFO: Load Photo no ") || strLine.startsWith("FINE: Load Photo no ")) {
                        try {
                            int posDoppelpunkt = strLine.indexOf(":", 5);
                            // System.out.println("posDoppelP:"+posDoppelpunkt);
                            String noStr = strLine.substring(20, posDoppelpunkt);
                            int no = Integer.parseInt(noStr);
                            if (no > maxNo) {
                                maxNo = no;
                                int posAt = strLine.indexOf("@");
                                int posL = strLine.indexOf("<", posAt);
                                String path = strLine.substring(posDoppelpunkt + 2, posAt);
                                String timeStamp = strLine.substring(posAt + 1, posL);
                                ExifInfos exif = new ExifInfos();
                                exif.timestamp =
                                    Double.parseDouble(
                                        timeStamp.replaceAll(Pattern.quote(","), Matcher.quoteReplacement(".")));
                                PhotoCube p = new PhotoCube(new File(path), exif);
                                if (generatePreview) {
                                    p.photoFiles[0].generateThumpFile();
                                }

                                p.noInFolderTmp = i;
                                i++;
                                // System.out.println("add:"+p);
                                getPhotosAll().add(p);
                            } else {
                                // skipping, in case of duplicated entrysys in log
                            }
                        } catch (Throwable e1) {
                            // was not a log entry?
                            // e1.printStackTrace();
                            Debug.getLog().log(Level.WARNING, "problems reading applog-line:" + strLine, e1);
                        }
                    }
                }
            } catch (IOException e1) {

                Debug.getLog().log(Level.WARNING, "problems reading applog", e1);
            }

        } catch (FileNotFoundException e2) {
            e2.printStackTrace();
        }

        if (getPhotosAll().size() == 0) {
            throw new Exception("No JPEG-Photos found in Applog-file " + logFile);
        }
    }

    @Override
    public boolean wasOptimal() {
        return oldNumbers.equals(newNumbers);
    }

    @Override
    public RikolaTaskfile loadPicturesFromTaskFile(
            File taskFile,
            File bandOrderFile,
            boolean generatePreview,
            ProgressCallbackImgLoading progressCallbackImgLoading)
            throws Exception {
        getPhotosAll().clear();
        photosMatched.clear();
        photosUnmatched.clear();
        oldNumbers.clear();

        RikolaTaskfile tf = new RikolaTaskfile();
        tf.taskFile = taskFile;
        tf.bandOrderFile = bandOrderFile;
        try (FileInputStream in = new FileInputStream(taskFile);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            FileInputStream in2 = new FileInputStream(bandOrderFile);
            BufferedReader br2 = new BufferedReader(new InputStreamReader(in2))) {
            String strLine;
            double lastDelay = 0;
            while ((strLine = br.readLine()) != null) {
                if (progressCallbackImgLoading != null && progressCallbackImgLoading.isCanceled()) {
                    throw new InterruptedByUserException();
                }

                if (strLine.trim().startsWith("%")) {
                    continue;
                }
                // System.out.println(strLine);
                String[] parts = strLine.trim().split(Pattern.quote(","));
                if (parts == null) {
                    continue;
                }

                if (parts.length >= 14 || parts[0].endsWith(".DAT")) {
                    // System.out.println("detected!");

                    // if this is a trigger with a lot of bands, skip the auxillary files
                    String part = parts[11];
                    if (part == null) {
                        part = "";
                    }

                    part = part.trim();
                    if (!(part.equals("0") || part.equals("1"))) {
                        continue;
                    }

                    try {
                        double imgDelay = Double.parseDouble(parts[1]) / 1000.;
                        if (imgDelay < lastDelay) {
                            Debug.getLog()
                                .log(
                                    Level.WARNING,
                                    "taskfile contains negative time jumps. please correct it, and restart matching process: "
                                        + strLine);
                            getPhotosAll().clear();
                            return tf;
                        }

                        lastDelay = imgDelay;
                        ExifInfos exif = new ExifInfos();
                        exif.timestamp = imgDelay;
                        PhotoCube p = new PhotoCube(new PhotoFile(new File(parts[0]), exif, -1));

                        if (generatePreview) {
                            p.photoFiles[0].generateThumpFile();
                        }

                        getPhotosAll().add(p);
                    } catch (Exception e) {
                        Debug.getLog().log(Level.WARNING, "could not create image from taskfile: " + strLine, e);
                    }

                    continue;
                }

                if (parts.length >= 8 || parts[0].startsWith("C")) {
                    try {
                        tf.exposuretime.add(Double.parseDouble(parts[5]));
                    } catch (Exception e) {
                        Debug.getLog().log(Level.WARNING, "could not parse exposure time: " + strLine, e);
                    }

                    continue;
                }
            }

            while ((strLine = br2.readLine()) != null) {
                strLine = strLine.trim();
                if (strLine.startsWith("%") || strLine.isEmpty()) {
                    continue;
                }
                // double wl = Double.parseDouble(strLine);
                tf.bandOrder.add(strLine);
            }

        } catch (IOException e) {
            Debug.getLog()
                .log(Level.WARNING, "could not load photos from taskfile " + taskFile + "  " + bandOrderFile, e);
        }

        Debug.getLog().config("Loaded " + getPhotosAll().size() + " photos");
        return tf;
    }
}
