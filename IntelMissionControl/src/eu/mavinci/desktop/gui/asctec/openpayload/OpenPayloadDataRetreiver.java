/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.asctec.openpayload;

import com.intel.missioncontrol.concurrent.Dispatcher;
import com.intel.missioncontrol.hardware.IGenericCameraConfiguration;
import com.intel.missioncontrol.hardware.IHardwareConfiguration;
import com.intel.missioncontrol.hardware.IHardwareConfigurationManager;
import com.intel.missioncontrol.helper.Ensure;
import com.intel.missioncontrol.map.worldwind.IWWGlobes;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.AMapLayerMatching;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.ExifInfos;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.MapLayerMatch;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.MapLayerMatching;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.PhotoCube;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.PhotoFile;
import eu.mavinci.desktop.helper.FileHelper;
import eu.mavinci.desktop.helper.MFileFilter;
import eu.mavinci.desktop.main.debug.Debug;
import eu.mavinci.flightplan.PhotoLogLine;
import eu.mavinci.flightplan.ReferencePoint;
import eu.mavinci.flightplan.Takeoff;
import eu.mavinci.plane.IAirplane;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Earth;
import gov.nasa.worldwind.globes.Globe;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TreeMap;
import java.util.logging.Level;

public class OpenPayloadDataRetreiver {

    static int BROADCAST_PORT = 12345;
    static boolean stopBroadcastListening;
    static boolean isBroadcastListening;
    static boolean oneBroadcastReceived;
    private static final Globe globe =
        DependencyInjector.getInstance().getInstanceOf(IWWGlobes.class).getDefaultGlobe();

    public static void createMatching(
            final IAirplane plane, IHardwareConfigurationManager hardwareConfigurationManager) {
        Runnable run =
            new Runnable() {
                public void run() {
                    Debug.getLog().log(Level.FINE, "start broadcast Listening.");
                    stopBroadcastListening = false;
                    isBroadcastListening = true;
                    DatagramSocket dsocket = null;
                    try {

                        // Create a socket to listen on the Port.
                        try {
                            dsocket = new DatagramSocket(null);
                            dsocket.setReuseAddress(true);
                            dsocket.setSoTimeout(300); // max time the socket is blocked
                            dsocket.setBroadcast(true);
                            dsocket.bind(new InetSocketAddress(BROADCAST_PORT));
                            //
                        } catch (BindException e) {
                            Debug.getLog().log(Level.WARNING, "Error binding broadcast Listening.");
                            isBroadcastListening = false;
                            return;
                        } catch (SocketException e) {
                            Debug.getLog().log(Level.SEVERE, "Could not open Backend broadcast listener", e);
                            isBroadcastListening = false;
                            return;
                        }

                        // Create a buffer to read datagrams into. If a
                        // packet is larger than this buffer, the
                        // excess will simply be discarded!
                        byte[] buffer = new byte[1024 * 10];

                        // Create a packet to receive data into the buffer
                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                        Debug.getLog().log(Level.CONFIG, "Open Backend broadcast listener on Port: " + BROADCAST_PORT);

                        // System.out.println("open port");
                        // Now loop forever, waiting to receive packets and printing
                        // them.
                        int retry = 0;
                        while (true) {
                            // System.out.println("head:"+System.currentTimeMillis());
                            if (stopBroadcastListening) {
                                dsocket.close();
                                isBroadcastListening = false;
                                return;
                            }
                            // Wait to receive a datagram
                            try {
                                dsocket.receive(packet);
                                oneBroadcastReceived = true;
                            } catch (SocketTimeoutException e) {
                                Debug.getLog().info("Backend broadcast listener timeout");
                                continue;
                            } catch (IOException e) {
                                Debug.getLog()
                                    .log(
                                        Level.SEVERE,
                                        "Could not read from openPayload broadcast listener socket. close connection",
                                        e);
                                dsocket.close();
                                isBroadcastListening = false;
                                return;
                            }

                            // Convert the contents to a string, and display them
                            String msg = new String(buffer, 0, packet.getLength());
                            InetSocketAddress sender = (InetSocketAddress)packet.getSocketAddress();
                            // ##############################
                            // WARNING!! ATTENTION
                            // if someone tries to extract the hostname via sender.getHostname() ... this will totally
                            // delay everything ON
                            // WINDOWS ONLY!! and break everything down!
                            // please make shure that no one in the WHOLDE APPLICATION code does this! getHostString()
                            // is THE SOLUTION
                            // #############################
                            @SuppressWarnings("checkstyle:variabledeclarationusagedistance")
                            long time = System.currentTimeMillis();
                            // System.out.println(""+System.currentTimeMillis()+ sender + msg);
                            Debug.getLog()
                                .log(Level.FINER, "Receive Broadcast from " + sender.getHostString() + ":" + msg);
                            if (createMatching(new OpenPayloadBroadcast(msg), plane, hardwareConfigurationManager)) {
                                return;
                            }

                            retry++;
                            if (retry > 3) {
                                return;
                            }

                            time = System.currentTimeMillis() - time;

                            // Reset the length of the packet before reusing it.
                            packet.setLength(buffer.length);
                        }
                    } catch (Throwable t) {
                        Debug.getLog()
                            .log(
                                Level.SEVERE,
                                "Could not read from Backend broadcast listener socket. close connection",
                                t);
                        isBroadcastListening = false;
                    } finally {
                        if (dsocket != null) {
                            try {
                                dsocket.close();
                            } catch (Exception expected) {
                            }
                        }
                    }

                    isBroadcastListening = false;
                }
            };

        Dispatcher.post(run);
    }

    public static boolean createMatching(
            OpenPayloadBroadcast msgParsed,
            IAirplane plane,
            IHardwareConfigurationManager hardwareConfigurationManager) {
        // make use of the message -> create new matching
        try {
            String matchingName;
            if (plane.getFPmanager().getOnAirRelatedLocalFP() != null
                    && plane.getFPmanager().getOnAirRelatedLocalFP().getFile() != null) {
                matchingName =
                    MFileFilter.fmlFilter.removeExtension(
                        plane.getFPmanager().getOnAirRelatedLocalFP().getFile().getName());
            } else {
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
                matchingName = df.format(Calendar.getInstance().getTime());
            }

            matchingName += "_" + msgParsed.getMissionName();

            final File matchingFolder = new File(plane.getMatchingsFolder(), matchingName);
            if (!FileHelper.askForOverwrite(matchingFolder, null)) {
                return false;
            }

            matchingFolder.mkdirs();

            // copy flightplans
            File fpFolder = new File(matchingFolder, AMapLayerMatching.FOLDER_NAME_FLIGHTPLANS_SUBFOLDER);
            fpFolder.mkdirs();
            File imagesFolder = new File(matchingFolder, AMapLayerMatching.FOLDER_NAME_PICS_SUBFOLDER);
            imagesFolder.mkdirs();
            try {
                // for (File source : fps) {
                // FileHelper.copyFile(source, new File(fpFolder,
                // source.getName()));
                // }
                String strxml = plane.getFPmanager().getOnAirFlightplan().toXML();
                Ensure.notNull(strxml, "strxml");
                FileHelper.writeStringToFile(strxml, new File(fpFolder, "onAirFlightplan.fml"));
            } catch (IOException e2) {
                Debug.getLog().log(Level.SEVERE, "Unable to copy Flightplan", e2);
                return false;
            }

            TreeMap<String, Long> listing = msgParsed.downloadFiles(imagesFolder);
            if (listing == null) {
                return false;
            }

            System.out.println("DONE DOWNLOADNIG FileNo:" + listing.size());
            File[] listJPG =
                imagesFolder.listFiles(
                    new FilenameFilter() {

                        @Override
                        public boolean accept(File dir, String name) {
                            return !name.endsWith(".jpg");
                        }
                    });

            Ensure.notNull(listJPG, "listJPG");
            // moving non images into matching folder, since FTP download placed them wrong
            for (File f : listJPG) {
                f.renameTo(new File(matchingFolder, f.getName()));
            }

            return createMatching(matchingFolder, plane, hardwareConfigurationManager);
        } catch (Exception e) {
            Debug.getLog().log(Level.WARNING, "Could not compute received message from openPayload broadcast", e);
            return false;
        }
    }

    private static double parseDoubleSafely(String str) { // should this be moved to more generic helper class?
        Ensure.notNull(str, "OpenPayloadDataRetreiver - null input");
        double result = 0;
        try {
            result = Double.parseDouble(str);
        } catch (NullPointerException np) {
            Debug.getLog().log(Level.WARNING, "OpenPayloadDataRetreiver - parsing of double failed: " + str + " " + np);
        } catch (NumberFormatException nf) {
            Debug.getLog().log(Level.WARNING, "OpenPayloadDataRetreiver - parsing of double failed: " + str + " " + nf);
        }

        return result;
    }

    public static boolean createMatching(
            File matchingFolder, IAirplane plane, IHardwareConfigurationManager hardwareConfigurationManager) {
        File imagesFolder = new File(matchingFolder, AMapLayerMatching.FOLDER_NAME_PICS_SUBFOLDER);

        try {
            // creating matching:
            final File matchingFile = new File(matchingFolder, AMapLayerMatching.DEFAULT_FILENAME);
            matchingFile.delete(); // otherwise data might be appended to existing matching

            // TODO when create a matching need to select the flightplan that was used --- not the plane!!
            IHardwareConfiguration hardwareConfiguration = plane.getHardwareConfiguration().deepCopy();
            MapLayerMatching matching = new MapLayerMatching(matchingFile, hardwareConfiguration);

            matching.setBandNames(
                plane.getHardwareConfiguration()
                    .getPrimaryPayload(IGenericCameraConfiguration.class)
                    .getDescription()
                    .getBandNamesSplit());

            Takeoff origin = plane.getFPmanager().getOnAirFlightplan().getTakeoff();
            // TODO FIXME highly important to get the altitude into this!!!! otherwise objects will be under the terrain
            // very often
            // Origin origin = (Origin) FlightplanFactory.getFactory().newOrigin(null, 49, 10, 1000, 0, true);
            Matrix m =
                (globe.computeSurfaceOrientationAtPosition(
                        Angle.fromDegrees(origin.getLat()),
                        Angle.fromDegrees(origin.getLon()),
                        origin.getAltInMAboveFPRefPoint()))
                    .multiply(Matrix.fromRotationY(Angle.fromDegrees(origin.getYaw())));

            // csv parsing:
            File csvFile = new File(matchingFolder, "geotags.csv");
            int lineCount = 0;
            try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    lineCount = lineCount + 1;
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) {
                        continue;
                    }
                    // could also check this otherways:
                    // String[] parts = line.split(";");
                    // if (parts.length < 8) continue;
                    //
                    // but then the wrongly formatted line would just be omitted

                    String[] parts = line.split(";");
                    if (parts.length < 8)
                        throw new NullPointerException(
                            "geotags.csv (at line"
                                + lineCount
                                + ") doesn't contain enough data in columns to split into variables to do arithmetics");
                    String imgName = parts[6];

                    File imgFile = new File(imagesFolder, imgName);
                    if (!imgFile.exists()) {
                        Debug.getLog().log(Level.INFO, "file not found for creating a openPayload dataset: " + imgFile);
                        continue;
                    }

                    PhotoFile photoFile = new PhotoFile(imgFile, new ExifInfos(), 0);
                    PhotoCube photoCube = new PhotoCube(new PhotoFile[] {photoFile});
                    PhotoLogLine plg = new PhotoLogLine(plane.getHardwareConfiguration());

                    double x = -parseDoubleSafely(parts[0]);
                    double y = -parseDoubleSafely(parts[1]);
                    double z = (int)Math.round(parseDoubleSafely(parts[2]));
                    if (Math.abs(x) > 1000 || Math.abs(y) > 1000 || Math.abs(z) > 1000) {
                        Debug.getLog().log(Level.INFO, "location is out of bound for a openPayload image: " + line);
                        continue;
                    }

                    Vec4 vec = new Vec4(x, y, z);
                    Vec4 ecef = vec.transformBy4(m);
                    Position pos = globe.computePositionFromPoint(ecef);

                    plg.lat = pos.latitude.degrees;
                    plg.lon = pos.longitude.degrees;
                    plg.alt = (int)Math.round(100 * pos.elevation);
                    plg.setTimestampRaw(parseDoubleSafely(parts[7]) / 1000);
                    plg.cameraRoll = -0;

                    try {
                        plg.cameraPitch = parseDoubleSafely(parts[3]) + 90;
                    } catch (NumberFormatException e) {
                        Debug.getLog().log(Level.WARNING, "geotags.csv could not parse cameraPitch double: " + e);
                    } catch (ArithmeticException e) {
                        // could be overflowed
                        e.printStackTrace();
                    }

                    plg.cameraYaw = (-parseDoubleSafely(parts[4]) - 90);
                    if (plg.cameraYaw < 0) {
                        plg.cameraYaw += 360;
                    }

                    try {
                        MapLayerMatch match = new MapLayerMatch(photoCube, plg, matching);
                        photoCube.setMatch(match);
                        matching.getPicsLayer().addMapLayer(match);
                    } catch (Throwable e1) {
                        Debug.getLog().log(Level.WARNING, "can't create singe match", e1);
                    }
                }
            } catch (Exception e) {
                Debug.getLog().log(Level.WARNING, "Issue found: ", e);
            }

            matching.getCoverage().updateCameraCorners();
            matching.guessGoodFilters();
            matching.saveResourceFile();

            Dispatcher.postToUI(
                new Runnable() {

                    @Override
                    public void run() {
                        String resString =
                            FileHelper.makeRelativePathSysIndep(plane.getMatchingsFolder(), matchingFile);
                        System.out.println(
                            "" + plane.getMatchingsFolder() + "    " + matchingFile + "   ->  " + resString);

                        // TODO IMPLEMENT ME
                        /*
                        ((DPanPlaneMainController)controller).matchingManager.add(resString);
                        AMapLayerMatching matching =
                            (AMapLayerMatching)
                                ((DPanPlaneMainController)controller)
                                    .matchingLayer.getSublayerByResourceString(resString);

                        if (matching != null) {
                            matching.setVisible(true);
                            controller.setSelectionAsync(matching);
                        }*/
                    }
                });

            return true;
        } catch (Exception e) {
            Debug.getLog().log(Level.WARNING, "Could not compute received message from openPayload broadcast", e);
            return false;
        }
    }
    // */
    // GlobalSettings.system = OsTypes.Win;
    // FileHelper.openFile(localFolder);

    public static void main(String[] args) throws FileNotFoundException, IOException {
        // String jsonLine = new String("{\"general\": { \"payloadType\": \"20\",\"systemType\":
        // \"Falcon8+\",\"lastMission\": {\"ftp\":
        // [{\"address\": \"ftp://192.168.0.10\", \"user\": \"anonymous\", \"pass\": \"anonymous\", \"dir\":
        // \"missions/00011\"},]}}}");
        // //create ObjectMapper instance
        // JsonElement jelement = new JsonParser().parse(jsonLine);
        // JsonObject jobject = jelement.getAsJsonObject();
        //
        // jobject = jobject.getAsJsonObject("general");
        // System.out.println(jobject);
        //
        // jobject = jobject.getAsJsonObject("lastMission");
        // JsonArray jarray = jobject.getAsJsonArray("ftp");
        // jobject = jarray.get(0).getAsJsonObject();
        // System.out.println(jobject);
        // String result = jobject.get("translatedText").toString();

        Globe globe = new Earth();
        ReferencePoint origin = new ReferencePoint(49.248771667, 8.638145447, 0, 0);
        // TODO FIXME highly important to get the altitude into this!!!! otherwise objects will be under the terrain
        // very often
        // Origin origin = (Origin) FlightplanFactory.getFactory().newOrigin(null, 49, 10, 1000, 0, true);
        Matrix m =
            (globe.computeSurfaceOrientationAtPosition(
                    Angle.fromDegrees(origin.getLat()),
                    Angle.fromDegrees(origin.getLon()),
                    origin.getAltInMAboveFPRefPoint()))
                .multiply(Matrix.fromRotationY(Angle.fromDegrees(origin.getYaw())));

        String path =
            "C:\\Users\\ekorotko\\Documents\\Intel Mission Control\\sessions\\AUVSI_Demo\\matchings\\2017-04-07_13-53-32_00010";
        // csv parsing:
        File csvFile = new File(path, "geotags.csv");
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                String[] parts = line.split(";");
                String imgName = parts[6];

                File imgFile = new File(path + "\\" + "images", imgName);
                if (!imgFile.exists()) {
                    Debug.getLog().log(Level.INFO, "file not found for creating a openPayload dataset: " + imgFile);
                    continue;
                }
                // PhotoFile photoFile = new PhotoFile(imgFile, new ExifInfos(), 0);
                // PhotoCube photoCube = new PhotoCube(new PhotoFile[] { photoFile });
                // PhotoLogLine plg = new PhotoLogLine(new );

                double x = -parseDoubleSafely(parts[0]);
                double y = -parseDoubleSafely(parts[1]);
                double z = (int)Math.round(100 * parseDoubleSafely(parts[2]));
                if (Math.abs(x) > 1000 || Math.abs(y) > 1000 || Math.abs(z) > 1000) {
                    Debug.getLog().log(Level.INFO, "location is out of bound for a openPayload image: " + line);
                    continue;
                }

                Vec4 vec = new Vec4(x, y, z);
                Vec4 ecef = vec.transformBy4(m);
                Position pos = globe.computePositionFromPoint(ecef);

                System.out.println(
                    "Pic data: "
                        + pos.latitude.degrees
                        + ", "
                        + pos.longitude.degrees
                        + ", "
                        + (int)Math.round(pos.elevation));

                /*
                 * plg.setTimestampRaw(Double.parseDouble(parts[7])/1000); plg.roll = -0; plg.pitch =
                 * Double.parseDouble(parts[3])+ 90; plg.yaw = Double.parseDouble(parts[4])+ 180;
                 */

            }
        }
    }
}
