/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.asctec;

import com.intel.missioncontrol.utils.IVersionProvider;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import eu.mavinci.core.flightplan.GPSFixType;
import eu.mavinci.core.flightplan.PhotoLogLineType;
import eu.mavinci.core.obfuscation.IKeepAll;
import eu.mavinci.desktop.main.debug.Debug;
import eu.mavinci.flightplan.PhotoLogLine;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.globes.Earth;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public interface FalconLogLib extends Library, IKeepAll, IFalconSDLog {
    FalconLogLib INSTANCE = initLib();

    static FalconLogLib initLib() {
        IVersionProvider versionProvider = DependencyInjector.getInstance().getInstanceOf(IVersionProvider.class);
        if (versionProvider.getSystem().isLinux()) {
            if (versionProvider.isEclipseLaunched()) {
                System.setProperty("jna.library.path", new File("").getAbsolutePath() + "/lib/");
            } else {
                String path = versionProvider.getInstallDir().getParent() + "/lib/";
                Debug.getLog().log(Level.INFO, "Installation/lib PATH: " + path);
                NativeLibrary.addSearchPath("libFalconLog", path);
                System.setProperty("jna.library.path", path);
                Debug.getLog().log(Level.INFO, "JNA PATH: " + System.getProperty("jna.library.path"));
            }
            // if linux platform and lib name is libFalconLog.so
            return (FalconLogLib)Native.loadLibrary("FalconLog", FalconLogLib.class);
        } else if (versionProvider.getSystem().isWindows()) {
            if (versionProvider.is64Bit()) {
                if (versionProvider.isEclipseLaunched()) {
                    System.setProperty("jna.library.path", new File("").getAbsolutePath() + "/lib64/");
                } else {
                    String path = versionProvider.getInstallDir().getAbsolutePath() + "\\lib64\\";
                    Debug.getLog().log(Level.INFO, "Installation/lib PATH: " + path);
                    NativeLibrary.addSearchPath("libFalconLog", path);
                    System.setProperty("jna.library.path", path);
                    System.setProperty("java.library.path", System.getProperty("java.library.path") + ";" + path);
                }
            } else {
                String path = versionProvider.getInstallDir().getAbsolutePath() + "\\lib32\\";
                NativeLibrary.addSearchPath("libFalconLog", path);
                System.setProperty("jna.library.path", path);
            }

            Debug.getLog().log(Level.INFO, "JAVA PATH 2: " + System.getProperty("java.library.path"));
            Debug.getLog().log(Level.INFO, "JNA PATH: " + System.getProperty("jna.library.path"));
            // if windows platform and lib name is libFalconLog.dll
            return (FalconLogLib)Native.loadLibrary("libFalconLog", FalconLogLib.class);
        } else {
            String path = versionProvider.getInstallDir().getAbsolutePath() + "/lib/";
            Debug.getLog().log(Level.INFO, "Installation/lib PATH: " + path);
            NativeLibrary.addSearchPath("libFalconLog", path);
            System.setProperty("jna.library.path", path);
            System.setProperty("java.library.path", System.getProperty("java.library.path") + ":" + path);
            // if mac platform and lib name is libFalconLog.dylib
            return (FalconLogLib)Native.loadLibrary("FalconLog", FalconLogLib.class);
        }
    }

    static List<PhotoLogLine> getPhotoLogLines(File path, AntennaInformation info, IntByReference num) {
        // because this method is called from LogFileHelper's background job and CreateDatasetTask background job...
        synchronized (FalconLogLib.class) {
            List<PhotoLogLine> logLines = new ArrayList<>();
            try {
                @SuppressWarnings("checkstyle:localvariablename")
                IntByReference nElements = new IntByReference();
                PointerByReference structPointerByReference = new PointerByReference();

                FalconLogLib.INSTANCE.get_PhotoTags(
                    path.getAbsolutePath(), nElements, structPointerByReference, info, num);
                int numElements = nElements.getValue();
                if (numElements == 0) {
                    return new ArrayList<>();
                }

                Pointer structPointerValue = structPointerByReference.getValue();
                PhotoTagStructure struct = new PhotoTagStructure(structPointerValue);
                struct.read();

                PhotoTagStructure[] structs = (PhotoTagStructure[])struct.toArray(numElements);
                logLines = photoTags2LogLines(structs, isAntennaDefined(info));

                FalconLogLib.INSTANCE.free_struct(structPointerValue, numElements);

            } catch (Error e) {
                Debug.getLog().log(Level.SEVERE, "AscTec log file is corruped. Please, try once again", e);
            }

            return logLines;
        }
    }

    static boolean isAntennaDefined(AntennaInformation info) {
        return info.lat != 0 || info.lon != 0;
    }

    public static List<PhotoLogLine> photoTags2LogLines(PhotoTagStructure[] structs, boolean antennaDefined) {
        ArrayList<PhotoLogLine> lines = new ArrayList<>();
        int num = 0;
        LatLon lastLatLon = null;
        PhotoLogLine lastLine = null;
        for (PhotoTagStructure tag : structs) {
            double timestampUnix = tag.timestamp;
            PhotoLogLine line = new PhotoLogLine(timestampUnix, null);
            line.alt = (int)(tag.relH * 100);
            // line.alt_ultrasonic=alt_ultrasonic;
            line.gps_altitude_cm = tag.height * 100;
            // TODO FIXME, get TRUE GPS heading and ground speed from log, otherwise RTK shifting wont work!
            line.gps_ellipsoid_cm = 0;
            // line.groundSpeed_cms=groundSpeed_cms;
            // line.heading= tag.yaw;
            // some estimate...
            LatLon latLon = LatLon.fromDegrees(line.lat, line.lon);
            if (lastLatLon != null) {
                line.heading = LatLon.greatCircleAzimuth(lastLatLon, latLon).degrees;
                double dt = line.getTimestamp() - lastLine.getTimestamp();
                if (dt > 0) {
                    line.groundSpeed_cms =
                        (int)
                            Math.round(
                                100
                                    * (LatLon.ellipsoidalDistance(
                                        lastLatLon, latLon, Earth.WGS84_EQUATORIAL_RADIUS, Earth.WGS84_POLAR_RADIUS))
                                    / dt);
                } else {
                    line.groundSpeed_cms = 0;
                }
            }

            lastLatLon = latLon;
            lastLine = line;
            line.imageNumber = num;
            num++;
            line.lat = tag.lat;
            // line.lineNumber=lineNumber;
            line.lon = tag.lon;
            line.cameraPitch = tag.pitch_cam + 90;
            line.cameraRoll = tag.roll_cam;
            line.cameraYaw = tag.yaw_cam;

            line.planePitch = tag.pitch_plane;
            line.planeRoll = tag.roll_plane;
            line.planeYaw = tag.yaw_plane;

            line.latTakeoff = tag.lat0;
            line.lonTakeoff = tag.lon0;
            line.gps_altitude_takeoff_cm = tag.height0 * 100;

            // TODO FIXME read form log if this was a feedback or trigger signal, and if this was RTK or not!!

            line.type = PhotoLogLineType.FLASH;
            line.fixType = antennaDefined ? GPSFixType.rtkFixedBL : GPSFixType.gpsFix;

            // line.type = PhotoLogLineType.FLASH;
            // line.fixType = GPSFixType.rtkFixedBL;

            lines.add(line);
        }

        if (num > 1) {
            lines.get(0).heading = lines.get(1).heading;
            lines.get(0).groundSpeed_cms = lines.get(1).groundSpeed_cms;
        }

        return lines;
    }

    public static void main(String[] args) {
        AntennaInformation info = new AntennaInformation();
        IntByReference num = new IntByReference();
        List<PhotoLogLine> photologLines = FalconLogLib.getPhotoLogLines(new File("E:\\ASCTEC\\1"), info, num);
    }

}
