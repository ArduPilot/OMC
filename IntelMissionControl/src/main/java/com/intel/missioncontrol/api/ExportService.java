/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.api;

import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.intel.insight.InsightContext;
import com.intel.insight.api.PhotogrammetryService;
import com.intel.insight.datastructures.Camera;
import com.intel.insight.datastructures.Coordinates;
import com.intel.insight.datastructures.Geometries;
import com.intel.insight.datastructures.IUploadProgress;
import com.intel.insight.datastructures.Inspection;
import com.intel.insight.datastructures.Photo;
import com.intel.insight.datastructures.PhotoGeometry;
import com.intel.insight.datastructures.PhotoUpload;
import com.intel.insight.datastructures.ProcessSettings;
import com.intel.insight.datastructures.Survey;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.StaticInjector;
import com.intel.missioncontrol.hardware.IGenericCameraConfiguration;
import com.intel.missioncontrol.hardware.IGenericCameraDescription;
import com.intel.missioncontrol.hardware.IHardwareConfiguration;
import com.intel.missioncontrol.hardware.ILensDescription;
import com.intel.missioncontrol.hardware.IPlatformDescription;
import com.intel.missioncontrol.helper.Expect;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.map.worldwind.IWWGlobes;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.mission.Matching;
import com.intel.missioncontrol.settings.ExpertSettings;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.settings.PathSettings;
import com.intel.missioncontrol.ui.dialogs.IDialogService;
import com.intel.missioncontrol.ui.notifications.Toast;
import com.intel.missioncontrol.ui.notifications.ToastType;
import com.intel.missioncontrol.ui.sidepane.analysis.AnalysisSettings;
import com.intel.missioncontrol.ui.sidepane.analysis.ExportTypes;
import com.intel.missioncontrol.utils.IBackgroundTaskManager;
import com.intel.missioncontrol.utils.IVersionProvider;
import eu.mavinci.core.flightplan.CPhotoLogLine;
import eu.mavinci.core.flightplan.IFlightplanStatement;
import eu.mavinci.core.flightplan.camera.GPStype;
import eu.mavinci.core.flightplan.camera.LensTypes;
import eu.mavinci.core.helper.StringHelper;
import eu.mavinci.core.licence.ILicenceManager;
import eu.mavinci.core.plane.protocol.Base64;
import eu.mavinci.core.plane.protocol.ProtocolTokens;
import eu.mavinci.core.plane.sendableobjects.OrientationData;
import eu.mavinci.core.xml.XMLWriter;
import eu.mavinci.desktop.gui.doublepanel.camerasettings.CameraHelper;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.AMapLayerMatching;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.ExifInfos;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.MapLayerMatch;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.MapLayerPicArea;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.PhotoFile;
import eu.mavinci.desktop.gui.doublepanel.planemain.tree.maplayers.IMapLayer;
import eu.mavinci.desktop.helper.FileHelper;
import eu.mavinci.desktop.helper.ImageHelper;
import eu.mavinci.desktop.helper.ImageMask;
import eu.mavinci.desktop.helper.MFileFilter;
import eu.mavinci.desktop.helper.MathHelper;
import eu.mavinci.desktop.helper.ProcessHelper;
import eu.mavinci.desktop.helper.WinRegistryQuery;
import eu.mavinci.desktop.helper.gdal.ISrsManager;
import eu.mavinci.desktop.helper.gdal.MSpatialReference;
import eu.mavinci.desktop.helper.gdal.SRStransformCacheEntry;
import eu.mavinci.desktop.main.debug.Debug;
import eu.mavinci.flightplan.Point;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.render.SurfacePolygon;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;
import java.util.Vector;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.asyncfx.concurrent.Dispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExportService implements IExportService {
    private static final Logger LOG = LoggerFactory.getLogger(ExportService.class);

    public static final String DEFAULT_CSV_NAME = "exported_metadata";
    public static final String PIX4D_WEBSITE_URL = "http://pix4d.mavinci.de";

    private final IApplicationContext applicationContext;
    private final ILanguageHelper languageHelper;
    private final PathSettings pathSettings;
    private final GeneralSettings generalSettings;
    private final AnalysisSettings analysisSettings;
    private final ExpertSettings expertSettings;
    private final ILicenceManager licenceManager;
    private final IVersionProvider versionProvider;
    private final ISrsManager srsManager;
    private final IWWGlobes globes;
    private final IDialogService dialogService;
    private Map<ExportTypes, Function<Matching, String>> targetObtainFunctions;

    @Inject
    public ExportService(
            IApplicationContext applicationContext,
            ISettingsManager settingsManager,
            ILanguageHelper languageHelper,
            ILicenceManager licenceManager,
            IVersionProvider versionProvider,
            ISrsManager srsManager,
            IWWGlobes globes,
            IDialogService dialogService) {
        this.licenceManager = licenceManager;
        this.applicationContext = applicationContext;
        this.versionProvider = versionProvider;
        pathSettings = settingsManager.getSection(PathSettings.class);
        generalSettings = settingsManager.getSection(GeneralSettings.class);
        analysisSettings = settingsManager.getSection(AnalysisSettings.class);
        expertSettings = settingsManager.getSection(ExpertSettings.class);
        this.languageHelper = languageHelper;
        this.srsManager = srsManager;
        this.globes = globes;
        this.dialogService = dialogService;
        targetObtainFunctions = new EnumMap<>(ExportTypes.class);
        targetObtainFunctions.put(ExportTypes.CSV, matching -> getCsvFile(matching).getPath());
        targetObtainFunctions.put(ExportTypes.PIX4D_DESKTOP, matching -> getPix4dFile(matching).getPath());
        targetObtainFunctions.put(ExportTypes.AGISOFT_PHOTOSCAN, matching -> getAgiSoftFile(matching).getPath());
        targetObtainFunctions.put(ExportTypes.AGISOFT_METASHAPE, matching -> getAgiSoftFile(matching).getPath());
        targetObtainFunctions.put(ExportTypes.CONTEXT_CAPTURE, matching -> getContextCaptureFile(matching).getPath());
    }

    public boolean isNotExportedAsCsv(Matching matching) {
        return !getCsvFile(matching).exists();
    }

    @Override
    public void exportAsCsv(Matching matching, MSpatialReference srs) throws IOException {
        File target;

        target = getCsvFile(matching);
        createOrRewrite(target);
        PrintStream out;
        out = new PrintStream(target, ProtocolTokens.encoding);

        out.print(licenceManager.getExportHeader());
        out.print("#XYZ_WKT: " + srs.getWkt().replace("\n", "").replace("\r", "") + " \r\n");
        out.print(
            "valid number filename gps_timestamp(sec_since_1970) gps_lon(deg) gps_lat(deg) altitiude(mOVERwgs84) yaw(deg) pitch(deg) roll(deg) gyro_yaw(deg/s) gyro_pitch(deg/s) gyro_roll(deg/s) altitude_GPS(geoid)(m) altitude_ellipsoid(m) groundspeed(m/s) lineNumber isCrossline isMainDirection folder cellNumber gpsHeading exif_timestamp(sec_since_1970) omega(deg) phi(deg) kappa(deg) gps_timestamp_float(float_sec_since_1970) GPS_Quality_Indicator_int GPS_Quality_Indicator_name X_WKT Y_WKT Z_WKT isOrthogonalLine lineNumber refinementID\r\n");

        int i = 0;

        final IHardwareConfiguration hardwareConfig = matching.getHardwareConfiguration();
        final List<MapLayerMatch> matches =
            Matching.sortMatches(matching, AMapLayerMatching.comparatorMatchesLineOrder);

        ArrayList<Position> ps = new ArrayList<>();
        for (MapLayerMatch match : matches) {
            Position p = match.getShiftedPositionExport(hardwareConfig);
            ps.add(p);
        }
        // batch gdal transformation
        List<SRStransformCacheEntry> vs = null;
        try {
            vs = srs.fromWgs84(ps);
        } catch (Exception e1) {
            Debug.getLog().log(Level.WARNING, "problems to transform coordinates in Matching-CVS export to SRS " + srs);
        }

        for (MapLayerMatch match : matches) {
            CPhotoLogLine line = match.getPhotoLogLine();
            Position p = ps.get(i);
            File imgFile = match.getResourceFile();
            PhotoFile photo = match.getCurPhotoFile();
            OrientationData o = CameraHelper.getCorrectedOrientation(line, hardwareConfig);
            OrientationData oRates = CameraHelper.getCorrectedRotationRates(line, hardwareConfig);
            Matrix m = CameraHelper.getCorrectedStateTransform(line, hardwareConfig).getTranspose();
            double[] ret = MathHelper.transformationToOmegaPhiKappa(m);

            long timestampExif = (long)photo.getExif().timestamp;
            if (timestampExif == -1) {
                try {
                    photo.getExif().timestamp = timestampExif = (long)(new ExifInfos(imgFile).timestamp);
                } catch (IOException e) {
                    Debug.getLog().log(Level.WARNING, "could not extract exif timestamp", e);
                }
            }

            SRStransformCacheEntry v = null;
            try {
                Expect.notNull(vs, "vs");
                v = vs.get(i);
            } catch (Exception e1) {
            }

            out.print(
                (match.isExportPassFilter() ? "1 " : "0 ")
                    + (i + 1)
                    + " "
                    + imgFile.getName()
                    + " "
                    + (long)(line.getTimestamp())
                    + " "
                    + p.longitude.degrees
                    + " "
                    + p.latitude.degrees
                    + " "
                    + p.elevation
                    + " "
                    + o.yaw
                    + " "
                    + o.pitch
                    + " "
                    + (o.roll * -1)
                    + " "
                    + oRates.yaw
                    + " "
                    + oRates.pitch
                    + " "
                    + (oRates.roll * -1)
                    + " "
                    + p.elevation
                    + " "
                    + (line.gps_ellipsoid_cm / 100.)
                    + " "
                    + (line.groundSpeed_cms / 100.)
                    + " "
                    + line.getLineNumberPure()
                    + " "
                    + (!line.isOnMainLine() ? "1 " : "0 ")
                    + (line.isForwardLine() ? "1 " : "0 ")
                    + FileHelper.getRelativePath(imgFile.getParentFile(), target, FileHelper.pathSeperatorExport)
                    + " "
                    + line.getCellNumber()
                    + " "
                    + line.heading
                    + " "
                    + timestampExif
                    + " "
                    + ret[0]
                    + " "
                    + ret[1]
                    + " "
                    + ret[2]
                    + " "
                    + line.getTimestamp()
                    + " "
                    + (line.fixType == null ? "-1 null" : line.fixType.ordinal() + " " + line.fixType)
                    + " "
                    + (v == null ? "0 0 0" : (v.x + " " + v.y + " " + v.z))
                    + (line.isOrthogonalLine() ? "1 " : "0 ")
                    + " "
                    + (line.lineNumber)
                    + " "
                    + line.getRefinementID()
                    + "\r\n");
            i++;
        }

        out.close();

        final File fInterest = new File(target.getParentFile(), "exported_RegionOfInteres.txt");
        writeMatchingMetaFile(fInterest, matching);
    }

    public boolean isNotExportedAsPix4d(Matching matching) {
        return !getPix4dFile(matching).exists();
    }

    @SuppressWarnings("Duplicates")
    public void exportAsPix4d(Matching currentMatching, MSpatialReference srs) throws Exception {
        if (!currentMatching.assureNonRaw()) {
            throw new Exception("could not export RAW datasets");
        }

        File target = getPix4dFile(currentMatching);
        String projectName = getPix4dProjectName(target, currentMatching);
        createOrRewrite(target);

        // TODO: this information should ne taken from matching
        final IHardwareConfiguration hardwareConfig = currentMatching.getHardwareConfiguration();
        final IPlatformDescription platformDesc = hardwareConfig.getPlatformDescription();
        final IGenericCameraConfiguration cameraConfig =
            hardwareConfig.getPrimaryPayload(IGenericCameraConfiguration.class);
        final IGenericCameraDescription cameraDesc = cameraConfig.getDescription();
        final ILensDescription lensDesc = cameraConfig.getLens().getDescription();

        PrintWriter out = null;
        out = new PrintWriter(target, "UTF-8");

        XMLWriter xml = new XMLWriter(out);
        xml.begin("<pix4uav>\n", 2);
        xml.comment(licenceManager.getExportHeaderCore());
        // xml.start("pix4uav");

        xml.tag("id", "value", "mavinci");

        String wkt = srs.getWkt();
        xml.tag("WKT", "value", wkt);
        xml.tag("WKTGCP", "value", wkt);
        xml.tag("WKTImages", "value", wkt);

        String camName =
            cameraDesc.getExifModels().isEmpty() ? cameraDesc.getName() : cameraDesc.getExifModels().get(0);

        String camID =
            camName + "_" + lensDesc.getFocalLength() + "_" + cameraDesc.getCcdResX() + "x" + cameraDesc.getCcdResY();

        String[] attr = new String[] {"cameraId", "id"};
        String[] vals = new String[] {camID, camID};
        xml.start("camera", attr, vals);
        xml.tag("index", "value", "0");

        xml.end();

        xml.tag("projectName", "value", projectName);

        String eMail = generalSettings.emailsInSupportProperty().get();
        if (eMail == null) {
            eMail = "user@unknown.www";
        }

        xml.tag("userName", "value", eMail);

        xml.tag("webService", "value", PIX4D_WEBSITE_URL);

        File workspace;
        if (versionProvider.getSystem().isLinux()) {
            workspace = new File(System.getProperty("user.home") + "/.wine/drive_c/pix4dWork/");
            xml.tag("workspace", "path", workspace.getAbsolutePath());
        } else {
            workspace = target.getParentFile();
            xml.tag(
                "workspace",
                "path",
                workspace.getAbsolutePath().replaceAll(Pattern.quote(File.separator), Matcher.quoteReplacement("\\")));
        }

        xml.tag("groundAltitude", "value", "" + currentMatching.getEstimatedStartingElevationInMoverWgs84(true));

        double resolution = 0.5; // default in m

        try {
            if (MSpatialReference.WGS84_ID.equals(srs.id)) {
                final String[] attrCorner = new String[] {"lat", "lng"};
                final String[] attrZminMax = new String[] {"zMin", "zMax"};

                for (MapLayerPicArea picArea : currentMatching.getVisiblePicAreas()) {
                    if (resolution > picArea.getGSD()) {
                        resolution = picArea.getGSD();
                    }

                    xml.start("area", "name", "denseRoi");
                    Vector<LatLon> ps = new Vector<LatLon>(picArea.sizeOfFlightplanContainer());
                    for (IFlightplanStatement o : picArea) {
                        ps.add(((Point)o).getLatLon());
                    }

                    for (LatLon latLon : ps) {
                        vals =
                            new String[] {
                                Double.toString(latLon.getLatitude().degrees),
                                Double.toString(latLon.getLongitude().degrees)
                            };
                        xml.tag("geoCoord2D", attrCorner, vals);
                    }

                    vals =
                        new String[] {
                            Double.toString(Double.MAX_VALUE), Double.toString(-Double.MAX_VALUE),
                        };
                    xml.tag("z", attrZminMax, vals);
                    xml.end();
                    xml.start("area", "name", "orthoRoi");
                    for (LatLon latLon : ps) {
                        vals =
                            new String[] {
                                Double.toString(latLon.getLatitude().degrees),
                                Double.toString(latLon.getLongitude().degrees)
                            };
                        xml.tag("geoCoord2D", attrCorner, vals);
                    }

                    vals =
                        new String[] {
                            Double.toString(Double.MAX_VALUE), Double.toString(-Double.MAX_VALUE),
                        };
                    xml.tag("z", attrZminMax, vals);
                    xml.end();
                }
            } else {
                final String[] attrCorner = new String[] {"lat", "lng", "x", "y"};
                final String[] attrZminMax = new String[] {"zMin", "zMax"};

                for (MapLayerPicArea picArea : currentMatching.getVisiblePicAreas()) {
                    if (resolution > picArea.getGSD()) {
                        resolution = picArea.getGSD();
                    }

                    xml.start("area", "name", "denseRoi");
                    Vector<LatLon> ps = new Vector<LatLon>(picArea.sizeOfFlightplanContainer());
                    for (IFlightplanStatement point : picArea) {
                        ps.add(((Point)point).getLatLon());
                    }

                    List<SRStransformCacheEntry> vs = srs.fromWgs84(ps);
                    for (int i = 0; i != ps.size(); i++) {
                        LatLon latLon = ps.get(i);
                        SRStransformCacheEntry v = vs.get(i);
                        vals =
                            new String[] {
                                Double.toString(latLon.getLatitude().degrees),
                                Double.toString(latLon.getLongitude().degrees),
                                Double.toString(v.x),
                                Double.toString(v.y),
                            };
                        xml.tag("geoCoord2D", attrCorner, vals);
                    }

                    vals =
                        new String[] {
                            Double.toString(Double.MAX_VALUE), Double.toString(-Double.MAX_VALUE),
                        };
                    xml.tag("z", attrZminMax, vals);
                    xml.end();
                    xml.start("area", "name", "orthoRoi");
                    for (int i = 0; i != ps.size(); i++) {
                        LatLon latLon = ps.get(i);
                        SRStransformCacheEntry v = vs.get(i);
                        vals =
                            new String[] {
                                Double.toString(latLon.getLatitude().degrees),
                                Double.toString(latLon.getLongitude().degrees),
                                Double.toString(v.x),
                                Double.toString(v.y),
                            };
                        xml.tag("geoCoord2D", attrCorner, vals);
                    }

                    xml.end();
                }
            }

            if (resolution > 0.30) {
                resolution = 0.30; // cm maximal TODO FIXME MAKE configureable!
            }

            xml.tag("orthoresolution", "value", "" + (resolution * 100));

            final String[] attrGps = new String[] {"alt", "lat", "lng"};
            final String[] attrXYZ = new String[] {"x", "y", "z"};
            final String[] attrOr = new String[] {"yaw", "pitch", "roll", "perpCamera"};
            ArrayList<Position> ps = new ArrayList<>();
            for (IMapLayer layer : currentMatching.getPictures()) {
                if (layer instanceof MapLayerMatch) {
                    MapLayerMatch match = (MapLayerMatch)layer;
                    Position p = match.getShiftedPositionExport(hardwareConfig);
                    ps.add(p);
                }
            }

            List<SRStransformCacheEntry> vs = srs.fromWgs84(ps);
            int i = 0;
            for (IMapLayer layer : currentMatching.getPictures()) {
                if (layer instanceof MapLayerMatch) {
                    MapLayerMatch match = (MapLayerMatch)layer;
                    CPhotoLogLine line = match.getPhotoLogLine();
                    SRStransformCacheEntry v = vs.get(i);
                    Position p = ps.get(i);
                    i++;
                    File imgFile = match.getResourceFile();
                    Expect.notNull(imgFile);
                    OrientationData o = CameraHelper.getCorrectedOrientation(line, 90, hardwareConfig);
                    String path = imgFile.getAbsolutePath();

                    path = path.replaceAll(Pattern.quote(File.separator), Matcher.quoteReplacement("\\"));

                    attr = new String[] {"path", "type", "enabled"};
                    vals =
                        new String[] {
                            path, cameraDesc.getBandNamesSplit()[0].toLowerCase(), match.isExportPassFilter() + ""
                        };

                    xml.start("image", attr, vals);

                    attr = new String[] {"id", "index"};
                    vals = new String[] {camID, "0"};

                    xml.tag("camera", attr, vals);
                    xml.tag("exifID", "value", camID);

                    if (MSpatialReference.WGS84_ID.equals(srs.id)) {
                        String[] valsGps =
                            new String[] {
                                Double.toString(p.elevation),
                                Double.toString(p.latitude.degrees),
                                Double.toString(p.longitude.degrees)
                            };
                        xml.tag("gps", attrGps, valsGps);
                    } else {
                        String[] valsXYZ =
                            new String[] {
                                Double.toString(v.x), Double.toString(v.y), Double.toString(v.z),
                            };
                        xml.tag("xyz", attrXYZ, valsXYZ);
                    }

                    String[] valsOri =
                        new String[] {
                            Double.toString(o.yaw), Double.toString(o.pitch), Double.toString(o.roll * -1), "true"
                        };
                    xml.tag("ori", attrOr, valsOri);

                    if (line.fixType != null) {
                        xml.tag("toleranceXY", "value", "" + line.fixType.getXYaccuracy());

                        xml.tag("toleranceZ", "value", "" + line.fixType.getZaccuracy());
                    }

                    xml.end();
                }
            }
        } finally {
            xml.finish("</pix4uav>\n");
            out.close();
        }
    }

    public void openInPix4d(Matching matching) {
        File pix4dFile = getPix4dFile(matching);
        Dispatcher dispatcher = Dispatcher.background();
        dispatcher.run(
            () -> {
                if (pathSettings.pix4DPathProperty().get() != null) {
                    try {
                        String[] args = getPix4dArgArray(pix4dFile.getAbsolutePath());
                        Debug.getLog().fine("run-pix4d:" + Arrays.asList(args).toString());
                        ProcessHelper.exec(args, null, pix4dFile.getParentFile());
                    } catch (IOException e) {
                        LOG.warn("opening pix4d failed", e);
                        applicationContext.addToast(
                            Toast.createDefaultFailed(
                                languageHelper,
                                languageHelper.getString(
                                    "com.intel.missioncontrol.ui.analysis.AnalysisViewModel.export.generic.complete")));
                    }
                } else {
                    try {
                        FileHelper.openFile(getPix4dFile(matching).getParentFile());
                    } catch (IOException e) {
                        LOG.warn("opening pix4d file in explorer failed", e);
                    }
                }
            });
    }

    @Override
    public boolean intelInsightUpload(
            Matching matching, MSpatialReference srs, IUploadProgress uploadProgress, boolean pix4DProcessing)
            throws Exception {
        if (!analysisSettings.getInsightLoggedIn()) {
            return false;
        }

        Sector boundingSector = matching.getSector();
        LatLon[] boundingBox = boundingSector.getCorners();

        List<MapLayerMatch> matches = Matching.sortMatches(matching, AMapLayerMatching.comparatorMatchesLineOrder);

        var hardwareConfig = matching.getHardwareConfiguration();
        IGenericCameraConfiguration cam = hardwareConfig.getPrimaryPayload(IGenericCameraConfiguration.class);

        Survey survey = new Survey();
        survey.setName(matching.getName());
        survey.setAddProjectToUsers(true);
        Geometries geom = new Geometries();
        geom.setType("GeometryCollection");

        GsonBuilder builder = new GsonBuilder();
        var gson = builder.create();
        var coordinates =
            gson.fromJson(
                "{\n"
                    + "        \"type\": \"Polygon\",\n"
                    + "        \"coordinates\": [\n"
                    + "          [\n"
                    + "            [\n"
                    + "              "
                    + boundingBox[0].longitude.degrees
                    + ",\n"
                    + "              "
                    + boundingBox[0].latitude.degrees
                    + "\n"
                    + "            ],\n"
                    + "            [\n"
                    + "              "
                    + boundingBox[1].longitude.degrees
                    + ",\n"
                    + "              "
                    + boundingBox[1].latitude.degrees
                    + "\n"
                    + "            ],\n"
                    + "            [\n"
                    + "              "
                    + boundingBox[2].longitude.degrees
                    + ",\n"
                    + "              "
                    + boundingBox[2].latitude.degrees
                    + "\n"
                    + "            ],\n"
                    + "            [\n"
                    + "              "
                    + boundingBox[3].longitude.degrees
                    + ",\n"
                    + "              "
                    + boundingBox[3].latitude.degrees
                    + "\n"
                    + "            ],\n"
                    + "            [\n"
                    + "              "
                    + boundingBox[0].longitude.degrees
                    + ",\n"
                    + "              "
                    + boundingBox[0].latitude.degrees
                    + "\n"
                    + "            ]\n"
                    + "          ]\n"
                    + "        ]\n"
                    + "      }",
                Coordinates.class);
        geom.getGeometries().add(coordinates);
        survey.setGeometry(geom);

        Camera camera = new Camera();
        camera.setFocalLength(
            (int)cam.getLens().getDescription().getFocalLength().convertTo(Unit.MILLIMETER).getValue().doubleValue());
        camera.setAspectRatio(
            cam.getDescription().getCcdWidth().convertTo(Unit.MILLIMETER).getValue().doubleValue()
                / cam.getDescription().getCcdHeight().convertTo(Unit.MILLIMETER).getValue().doubleValue());
        camera.setWidth(cam.getDescription().getCcdResX());
        camera.setHeight(cam.getDescription().getCcdResY());
        camera.setModel(cam.getDescription().getExifModels().get(0));
        survey.getCameras().add(camera);
        SurfacePolygon polygon = new SurfacePolygon(Arrays.asList(boundingBox));

        survey.setArea(polygon.getArea(globes.getDefaultGlobe())); // the area covered by the sector
        var processSettings = new ProcessSettings();
        processSettings.setMapType("");
        var inspection = new Inspection();
        inspection.setVideo(false);
        processSettings.setInspection(inspection);
        DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH).withZone(ZoneId.of("UTC"));
        survey.setSurveyDate(
            formatter.format(
                new Date(Math.round(1000 * matches.get(0).getPhotoLogLine().getTimestamp()))
                    .toInstant())); // "2018-04-12T00:00:00.000Z");
        survey.setNumberOfPhotos(matches.size());
        survey.setIndustry("Geospatial");
        survey.setProcessSettings(processSettings);

        var context = new InsightContext();
        context.authWithInsight(analysisSettings.getInsightUsername(), analysisSettings.getInsightPassword());
        System.out.println(context.oauthResponse.getAccessToken());
        var createdProject = context.createNewProject(survey);

        ArrayList<Position> ps = new ArrayList<>();
        for (MapLayerMatch match : matches) {
            Position p = match.getShiftedPositionExport(hardwareConfig);
            ps.add(p);
        }
        // batch gdal transformation
        List<SRStransformCacheEntry> vs = srs.fromWgs84(ps);

        PhotoUpload photoUpload = new PhotoUpload();
        photoUpload.setFlight(createdProject.getFlight().getId());
        photoUpload.setMission(createdProject.getMission().getId());
        photoUpload.setProject(createdProject.getProject().getId());

        List<Photo> photos = new ArrayList<>();

        String vertCS = srs.getWktOnlyVertical();
        String horCS = srs.getWktWithoutVertical();

        int i = -1;
        for (MapLayerMatch match : matches) {
            i++;
            if (match.isExportPassFilter()) {
                CPhotoLogLine line = match.getPhotoLogLine();
                Position p = ps.get(i);
                SRStransformCacheEntry v = vs.get(i);
                File imgFile = match.getResourceFile();
                PhotoFile photoFile = match.getCurPhotoFile();
                Matrix m = CameraHelper.getCorrectedStateTransform(line, hardwareConfig).getTranspose();
                double[] rollPitchYaw = MathHelper.transformationToRollPitchYaw(m);

                ExifInfos exif = photoFile.getExif();

                var photo = new Photo();
                photo.setAltitude(v.z);
                photo.setUtc(formatter.format(new Date(Math.round(1000 * line.getTimestamp())).toInstant()));
                photo.setShutter(exif.exposureSec);
                photo.setSharpened(false);
                photo.setStatus("uploading");
                photo.setRtc(4); // ????
                photo.setCamera(createdProject.getCameras().get(0).getId());
                photo.setWidth(cam.getDescription().getCcdResX());
                photo.setHeight(cam.getDescription().getCcdResY());
                PhotoGeometry photoGeom = new PhotoGeometry();
                List<Double> coord = new ArrayList<>();
                coord.add(v.x);
                coord.add(v.y);
                photoGeom.setCoordinates(coord);
                photo.setGeometry(photoGeom);
                // photo.setVerticalSrsWkt(srs.getWkt());
                // TODO: the vertical srs uses a different syntax
                photo.setVerticalSrsWkt(
                    "VERT_CS[\\\"EGM96 geoid (meters)\\\",VERT_DATUM[\\\"EGM96 geoid\\\",2005,EXTENSION[\\\"PROJ4_GRIDS\\\",\\\"egm96_15.gtx\\\"],AUTHORITY[\\\"EPSG\\\",\\\"5171\\\"]],UNIT[\\\"metre\\\",1,AUTHORITY[\\\"EPSG\\\",\\\"9001\\\"]],AXIS[\\\"Up\\\",UP]]");
                photo.setHorizontalSrsWkt(srs.getWkt());
                // SONY_UMC-R10C_24_5c4ffb256bb2a051df3bdf35_2018-04-12T09:56:53.000Z
                photo.setUploadId(
                    createdProject.getCameras().get(0).getId()
                        + createdProject.getMission().getId()
                        + "2018-05-29T10:07:25.000Z"); // TODO from timestamp
                photo.setFlight(createdProject.getFlight().getId()); // TODO
                // photo.setPhi(rollPitchYaw[0]);
                // photo.setPsi(rollPitchYaw[2]);
                // photo.setTheta(rollPitchYaw[1]);
                /*{
                    "seq": "5c529fac3869981964933bd7",
                        "altitude": 159.63428174878555,
                        "ground_footprint": {},
                    "UTC": "2018-05-29T10:07:25.000Z",
                        "shutter": 12.5,
                        "sharpened": false,
                        "status": "uploading",
                        "RTC": 4,
                        "camera": "5c4061b494eed129d59678ab",
                        "width": 7360,
                        "height": 4912,
                        "geometry": {
                    "type": "Point",
                            "coordinates": [
                    23.685973158360568,
                            61.51935263322394
                ]
                },
                    "phi": 0,
                        "psi": 45.17,
                        "theta": 0,
                        "vertical_srs_wkt": "VERT_CS[\"EGM96 geoid (meters)\",VERT_DATUM[\"EGM96 geoid\",2005,EXTENSION[\"PROJ4_GRIDS\",\"egm96_15.gtx\"],AUTHORITY[\"EPSG\",\"5171\"]],UNIT[\"metre\",1,AUTHORITY[\"EPSG\",\"9001\"]],AXIS[\"Up\",UP]]",
                        "horizontal_srs_wkt": "GEOGCS[\"WGS 84\",DATUM[\"WGS_1984\",SPHEROID[\"WGS 84\",6378137,298.257223563,AUTHORITY[\"EPSG\",\"7030\"]],TOWGS84[0,0,0,0,0,0,0],AUTHORITY[\"EPSG\",\"6326\"]],PRIMEM[\"Greenwich\",0,AUTHORITY[\"EPSG\",\"8901\"]],UNIT[\"degree\",0.0174532925199433,AUTHORITY[\"EPSG\",\"9122\"]],AUTHORITY[\"EPSG\",\"4326\"]]",
                        "upload_id": "SONY_ILCE-7R_35_5c529fac3869981964933bd7_2018-05-29T10:07:25.000Z",
                        "flight": "5c6bf5a9a39853197efc7208"
                },*/
                // match.getResourceFile().getAbsolutePath();
                photo.setSeq(match.getResourceFile().getPath());
                photo.setVerticalSrsWkt(vertCS);
                photo.setHorizontalSrsWkt(horCS);
                photo.setUploadId(
                    createdProject.getCameras().get(0).getId()
                        + createdProject.getMission().getId()
                        + formatter.format(new Date(Math.round(1000 * line.getTimestamp())).toInstant()));
                photo.setFlight(createdProject.getFlight().getId());
                photo.setPhi(rollPitchYaw[2]);
                photo.setPsi(rollPitchYaw[0]);
                photo.setTheta(rollPitchYaw[1]);
                photos.add(photo);
            }
        }

        photoUpload.setPhotos(photos);

        context.preparePhotoUpload(gson, photoUpload, uploadProgress);

        if (pix4DProcessing) {
            var photoService = new PhotogrammetryService(context);
            survey.setProcessSettings(photoService.createPix4DProcessSettings());

            var p =
                photoService.createPhotogrammetryRequest(
                    createdProject.getMission().getId(), createdProject.getFlight().getId());
            photoService.requestPhotogrammetry(p);
        }

        return true;
    }

    @Override
    public void openIntelInsightAccount(Matching matching) {
        try {
            // FileHelper.openFileOrURL("https://insightplatform.intel.com");
            var context = new InsightContext();
            FileHelper.openFileOrURL(context.getTargetHost());
        } catch (IOException e) {
            LOG.error("cant open insight URL in browser", e);
        }
    }

    @Override
    public boolean noInsightUploadRunning(Matching matching) {
        return true; // TODO IMPLEENT ME
    }

    private String[] getPix4dArgArray(String absFilePath) {
        String pix4dPath = pathSettings.getPix4DPath().toString().toLowerCase();
        if (pix4dPath.endsWith("pix4dmapper.exe")) {
            return new String[] {pix4dPath, absFilePath};
        } else if (versionProvider.getSystem().isMac()) {
            if (pix4dPath.contains("pix4dmapper") && pix4dPath.contains(".app")) {
                return new String[] {pix4dPath + "/Contents/MacOS/Pix4Dmapper", absFilePath};
            } else {
                return new String[] {pix4dPath, "-xml", absFilePath, "-gui"};
            }
        } else {
            return new String[] {pix4dPath, "-xml", absFilePath, "-gui"};
        }
    }

    private File getPix4dFile(Matching currentMatching) {
        return getFile(currentMatching, "pix4d_export." + MFileFilter.pix4dMatchingExportFilter.getExtension());
    }

    private File getCsvFile(Matching matching) {
        return getFile(matching, DEFAULT_CSV_NAME + "." + MFileFilter.csvMatchingExportFilter.getExtension());
    }

    private File getFile(Matching matching, String filename) {
        File target;
        if (matching == null || matching.getResourceFile() == null) {
            throw new IllegalStateException("Matching has no data to export");
        }

        target = new File(matching.getResourceFile().getParentFile(), filename);
        return target;
    }

    private void createOrRewrite(File target) throws IOException {
        if (target.exists()) {
            if (!target.delete()) {
                Debug.getLog().log(Level.WARNING, "Cannot delete previous file");
            }
        }

        if (!target.createNewFile()) {
            Debug.getLog().log(Level.WARNING, "Cannot create new file");
        }
    }

    private Double writeMatchingMetaFile(final File fInterest, Matching currentMatching) throws IOException {
        // System.out.println("fInterest:"+fInterest);
        PrintStream outInteres;
        if (fInterest.exists()) {
            if (!fInterest.delete()) {
                Debug.getLog().log(Level.WARNING, "Cannot delete previous region of interest file");
            }
        }

        if (!fInterest.createNewFile()) {
            Debug.getLog().log(Level.SEVERE, "Cannot create region of interest file");
        }

        outInteres = new PrintStream(fInterest, ProtocolTokens.encoding);

        outInteres.print(licenceManager.getExportHeader());
        double area = 0;
        for (MapLayerPicArea picArea : currentMatching.getVisiblePicAreas()) {
            area += picArea.getArea();
            outInteres.print("\r\n");
            outInteres.print("\r\n");
            outInteres.print("GSD: " + (picArea.getGSD() * 100.) + " cm\r\n");
            outInteres.print("area: " + picArea.getArea() + " m^2\r\n");
            outInteres.print("Sector: " + picArea.getSector() + "\r\n");
            outInteres.print("lat(deg)\tlon(deg)\r\n");
            for (LatLon latLon : picArea.getCorners()) {
                outInteres.print(latLon.latitude.degrees + "\t" + latLon.longitude.degrees + "\r\n");
            }
        }

        outInteres.close();
        return area;
    }

    private String getPix4dProjectName(File target, Matching currentMatching) {
        String projectName;
        projectName = MFileFilter.pix4dMatchingExportFilter.removeExtension(target.getName());
        projectName = applicationContext.getCurrentMission().getDirectory() + "_" + projectName;
        projectName = projectName.toLowerCase().replaceAll("[^a-zA-Z0-9_]", Matcher.quoteReplacement(""));
        return projectName;
    }

    @Override
    public boolean isNotExportedToAgiSoftPhotoScan(Matching matching) {
        return !getAgiSoftFile(matching).exists();
    }

    private File getAgiSoftFile(Matching matching) {
        AgiSoftVersion version = detectAgiSoftVersion();
        MFileFilter exportFormat;

        if (version.version != null
                && (version.version.startsWith("1.2")
                    || version.version.startsWith("1.3")
                    || version.version.startsWith("1.4")
                    || version.version.startsWith("1.5"))) {
            exportFormat = MFileFilter.psxMatchingExportFilter;
        } else {
            exportFormat = MFileFilter.pszMatchingExportFilter;
        }

        File target =
            new File(
                matching.getResourceFile().getParentFile(),
                matching.getResourceFile().getParentFile().getName() + "." + exportFormat.getExtension());
        String fileName = target.getName();
        fileName = fileName.replaceAll(Pattern.quote(" "), Matcher.quoteReplacement("_"));
        target = new File(target.getParentFile(), fileName);
        return target;
    }

    @Override
    public void openInAgiSoft(Matching matching) throws IOException {
        File agiSoftProj = getAgiSoftFile(matching);
        executeAgiSoftFile(agiSoftProj);
    }

    @Override
    public void writeDebugAgisoftScript() throws Exception {
        if (pathSettings.getAgiSoftPhotoScanPath() == null) {
            throw new Exception(
                "no PhotoScan / Metashape installation path known. Could not start PhotoScan / Metashape.");
        }

        AgiSoftVersion version = detectAgiSoftVersion();
        String resource = "TestAccuracy";
        String resourceInit = "TestAccuracy.py";
        String libResource = "eu/mavinci/agisoft/" + resource + ".py";

        // i18n python code
        try (InputStream inputLibResource = ClassLoader.getSystemResourceAsStream(libResource)) {
            String pythonCode = i18nAgisoftScript(version, resource, inputLibResource);

            if (versionProvider.getSystem().isWindows()) {
                File scriptFolder = getAgisoftScriptsFolder();

                if (!scriptFolder.mkdirs() && !scriptFolder.isDirectory()) {
                    LOG.warn("Unable to create directories for scripts. Path - {}", scriptFolder.getAbsolutePath());
                }

                File script = new File(scriptFolder, "TestAccuracy.py");

                try (FileOutputStream fs = new FileOutputStream(script);
                    OutputStreamWriter os = new OutputStreamWriter(fs, "UTF-8")) {
                    os.write(pythonCode);
                    os.close();
                    LOG.trace("written Photoscan / Metashape python code to {}", script);
                }

                FileHelper.writeResourceToFile(
                    "eu/mavinci/agisoft/" + resourceInit, new File(scriptFolder, "TestAccuracy.py"));
                LOG.info(
                    "Debug / Metashape Photoscan script "
                        + resourceInit
                        + "was copied into the Photoscan / Metashape folder "
                        + scriptFolder);
            } else {
                LOG.error("OS other than Windows are not yet supported");
            }
        }
    }

    @Override
    public void exportToAgiSoftPhotoScan(Matching currentMatching, MSpatialReference srs) throws Exception {
        AgiSoftVersion version = detectAgiSoftVersion();
        boolean usePhotoScanLevelArmProcessing;
        MFileFilter exportFormat;

        if (version.version != null
                && (version.version.startsWith("1.2")
                    || version.version.startsWith("1.3")
                    || version.version.startsWith("1.4")
                    || version.version.startsWith("1.5"))) {
            usePhotoScanLevelArmProcessing = expertSettings.getExportUseAgisoftLevelArmOptimisation();
            exportFormat = MFileFilter.psxMatchingExportFilter;
        } else {
            usePhotoScanLevelArmProcessing = false;
            exportFormat = MFileFilter.pszMatchingExportFilter;
        }

        File target = getAgiSoftFile(currentMatching);

        MSpatialReference srsWithoutVertical = srs;
        try {
            String wkt = srs.getWktWithoutVertical();
            if (!wkt.equals(srs.getWkt())) {
                srsWithoutVertical =
                    new MSpatialReference(
                        "99" + srs.id, srs.name + "_nonVert", wkt, srsManager, globes.getDefaultGlobe());
            }
        } catch (Exception e) {
            LOG.warn("could not remove vertical component from SRS", e);
        }

        final IHardwareConfiguration hardwareConfig = currentMatching.getHardwareConfiguration();
        final IPlatformDescription platformDesc = hardwareConfig.getPlatformDescription();
        final IGenericCameraConfiguration cameraConfig =
            hardwareConfig.getPrimaryPayload(IGenericCameraConfiguration.class);
        final IGenericCameraDescription cameraDesc = cameraConfig.getDescription();
        final ILensDescription lensDesc = cameraConfig.getLens().getDescription();

        if (platformDesc.isJpgMetadataLocationInCameraFrame()) {
            usePhotoScanLevelArmProcessing = false;
        }

        File fCal = new File(cameraDesc.getAgisoftCalibFile());

        boolean useMasks = lensDesc.getLensType() == LensTypes.FISH_EYE;

        List<MapLayerMatch> matches =
            Matching.sortMatches(currentMatching, AMapLayerMatching.comparatorMatchesLineOrder);
        // determine best GPS in dataset:

        final GPStype gpsType;
        if (currentMatching.rtkFixCountProperty().get() > 0 || currentMatching.rtkFloatCountProperty().get() > 0) {
            gpsType = GPStype.DGPS_RTK;
        } else if (currentMatching.diffGpsFixCountProperty().get() > 0) {
            gpsType = GPStype.DGPS;
        } else {
            gpsType = GPStype.GPS;
        }

        String[] args;
        String[] vals;
        int i;
        if (version.version == null
                || (version.version.startsWith("1.2")
                    || version.version.startsWith("1.3")
                    || version.version.startsWith("1.4")
                    || version.version.startsWith("1.5"))) {
            double gpsAccuracyXY = 0;
            double gpsAccuracyZ = 0;
            int gpsAccuracyCnt = 0;
            ArrayList<Position> ps = new ArrayList<>();
            for (MapLayerMatch match : matches) {
                if (!match.isExportPassFilter()) {
                    continue;
                }

                Position p = match.getShiftedPositionExport(hardwareConfig, !usePhotoScanLevelArmProcessing);
                ps.add(p);

                CPhotoLogLine line = match.getPhotoLogLine();
                if (line.fixType != null) {
                    gpsAccuracyXY += line.fixType.getXYaccuracy();
                    gpsAccuracyZ += line.fixType.getZaccuracy();
                    gpsAccuracyCnt++;
                }
            }
            // batch gdal transformation
            List<SRStransformCacheEntry> vs = srsWithoutVertical.fromWgs84(ps);

            if (gpsAccuracyCnt == 0) {
                gpsAccuracyXY = 2;
                gpsAccuracyZ = 3;
            } else {
                gpsAccuracyXY /= gpsAccuracyCnt;
                gpsAccuracyZ /= gpsAccuracyCnt;
            }

            double resolution = currentMatching.estimateGsd();
            double groundLevel = currentMatching.getEstimatedStartingElevationInMoverWgs84(true);

            double[] rpyCamOrientation =
                MathHelper.transformationToRollPitchYaw(CameraHelper.getCameraJustageTransform(90, hardwareConfig));
            while (rpyCamOrientation[2] < 0) {
                rpyCamOrientation[2] += 360;
            }

            while (rpyCamOrientation[2] >= 360) {
                rpyCamOrientation[2] -= 360;
            }

            if (!target.delete()) {
                LOG.warn("Unable to delete file {}", target.getAbsolutePath());
            }

            File folderProject = new File(exportFormat.removeExtension(target) + ".files");
            if (!FileHelper.deleteDir(languageHelper, folderProject, true)) {
                throw new IOException("could not delete folder: " + folderProject);
            }

            if (!folderProject.mkdir()) {
                throw new IOException("could not create folder: " + folderProject);
            }

            File fileProject = new File(folderProject, "project.zip");
            File fileProjectInner = new File(folderProject, "doc.xml");

            File folderChunk = new File(folderProject, "0");
            if (!folderChunk.mkdir()) {
                throw new IOException("could not create folder: " + folderChunk);
            }

            File fileChunk = new File(folderChunk, "chunk.zip");
            File fileChunkInner = new File(folderChunk, "doc.xml");

            File folderFrame = new File(folderChunk, "0");
            if (!folderFrame.mkdir()) {
                throw new IOException("could not create folder: " + folderFrame);
            }

            File fileFrame = new File(folderFrame, "frame.zip");
            File fileFrameInner = new File(folderFrame, "doc.xml");

            File folderThumps = new File(folderFrame, "thumbnails");
            if (!folderThumps.mkdir()) {
                throw new IOException("could not create folder: " + folderThumps);
            }

            File fileThumps = new File(folderThumps, "thumbnails.zip");
            File fileThumpsInner = new File(folderThumps, "doc.xml");

            List<MapLayerPicArea> visiblePicAreas = currentMatching.getVisiblePicAreas();
            boolean cropToAOI = !visiblePicAreas.isEmpty();

            // #################################################################
            // shapes.zip
            if (cropToAOI) {
                File folderShapes = new File(folderFrame, "shapes");
                if (!folderShapes.mkdir()) {
                    throw new IOException("could not create folder: " + folderShapes);
                }

                File fileShapes = new File(folderShapes, "shapes.zip");
                File fileShapesInner = new File(folderShapes, "doc.xml");

                PrintWriter out = new PrintWriter(fileShapesInner, "UTF-8");
                XMLWriter xml = new XMLWriter(out);
                xml.begin("", 2);
                xml.comment(licenceManager.getExportHeaderCore());
                xml.start("shapes", "version", "1.2.0");
                xml.start("params");
                xml.contentTag("crs", srsWithoutVertical.getWkt());
                xml.end(); // params

                xml.start("shapes", "next_id", visiblePicAreas.size() + "");
                i = 0;
                for (MapLayerPicArea picArea : visiblePicAreas) {
                    args = new String[] {"id", "type", "boundary"};
                    vals = new String[] {i + "", "polygon", "true"};
                    xml.start("shape", args, vals);
                    StringBuilder vertices = new StringBuilder();
                    boolean first = true;

                    // batch gdal transformation
                    List<SRStransformCacheEntry> vsCorners = srsWithoutVertical.fromWgs84(picArea.getCorners());
                    for (SRStransformCacheEntry corner : vsCorners) {
                        if (first) {
                            first = false;
                        } else {
                            vertices.append(" ");
                        }

                        vertices.append(corner.x).append(" ").append(corner.y);
                    }

                    xml.contentTag("vertices", vertices.toString());
                    xml.end(); // shape
                    i++;
                }

                xml.end(); // shapes
                xml.end(); // shapes (yes this has to be twice)

                xml.finish("");
                out.close();

                FileHelper.zipSingleFile(fileShapesInner, fileShapes);
            }
            // ##############################################
            // *psx
            PrintWriter out = new PrintWriter(target, "UTF-8");
            XMLWriter xml = new XMLWriter(out);
            xml.begin("", 2);
            xml.comment(licenceManager.getExportHeaderCore());
            args = new String[] {"version", "path"};
            vals = new String[] {"1.2.0", "{projectname}.files/project.zip"};

            xml.tag("document", args, vals);
            xml.finish("");
            out.close();

            // ##############################################
            // project.zip

            out = new PrintWriter(fileProjectInner, "UTF-8");
            xml = new XMLWriter(out);
            xml.begin("", 2);
            xml.comment(licenceManager.getExportHeaderCore());
            xml.start("document", "version", "1.2.0");

            xml.start("meta");

            args = new String[] {"name", "value"};

            vals = new String[] {"mavinci.camAccuracy", gpsAccuracyXY + ""};
            xml.tag("property", args, vals);

            vals = new String[] {"mavinci.camAccuracyZ", gpsAccuracyZ + ""};
            xml.tag("property", args, vals);

            vals = new String[] {"mavinci.wkt", srs.getWkt()};
            xml.tag("property", args, vals);

            vals = new String[] {"mavinci.wktName", srs.toString()};
            xml.tag("property", args, vals);

            vals = new String[] {"mavinci.useInternalWKT", "1"};
            xml.tag("property", args, vals);

            vals = new String[] {"mavinci.align.quality", gpsType == GPStype.GPS ? "medium" : "high"};
            xml.tag("property", args, vals);
            vals = new String[] {"mavinci.dense.quality", "medium"};
            xml.tag("property", args, vals);
            vals = new String[] {"mavinci.mesh.quality", gpsType == GPStype.GPS ? "medium" : "high"};
            xml.tag("property", args, vals);

            vals = new String[] {"mavinci.dense.enable", "1"};
            xml.tag("property", args, vals);
            vals = new String[] {"mavinci.mesh.enable", "1"};
            xml.tag("property", args, vals);

            vals = new String[] {"mavinci.exportHeaderCore", licenceManager.getExportHeaderCore()};
            xml.tag("property", args, vals);

            vals = new String[] {"mavinci.gpsType", gpsType.toString()};
            xml.tag("property", args, vals);

            vals = new String[] {"mavinci.ortho.enable", "1"};
            xml.tag("property", args, vals);
            vals = new String[] {"mavinci.ortho.gsd", resolution + ""};
            xml.tag("property", args, vals);
            vals = new String[] {"mavinci.ortho.enableColCorrection", "0"};
            xml.tag("property", args, vals);

            vals = new String[] {"mavinci.dsm.enable", "1"};
            xml.tag("property", args, vals);
            vals = new String[] {"mavinci.dsm.gsd", (2 * resolution) + ""};
            xml.tag("property", args, vals);

            // deprecated, but still here for compatibility with old plugins:

            vals = new String[] {"mavinci.quality", gpsType == GPStype.GPS ? "medium" : "high"};
            xml.tag("property", args, vals);

            vals = new String[] {"mavinci.resolution", resolution + ""};
            xml.tag("property", args, vals);

            vals = new String[] {"mavinci.enableColCorrection", "0"};
            xml.tag("property", args, vals);

            vals = new String[] {"mavinci.zScale", gpsType == GPStype.GPS ? "1.0" : "0.1"};
            xml.tag("property", args, vals);

            vals = new String[] {"mavinci.useMasks", useMasks ? "1" : "0"};
            xml.tag("property", args, vals);

            String matchingName =
                (currentMatching.getResourceFile() != null
                    ? currentMatching.getResourceFile().getParentFile().getName()
                    : "Multimatching");
            vals =
                new String[] {
                    "mavinci.description",
                    languageHelper.getString(
                        "eu.mavinci.desktop.gui.doublepanel.planemain.ActionManager"
                            + ".actMatchingExportAgisoftProject"
                            + ".report.description",
                        pathSettings.getProjectFolder(),
                        matchingName)
                };
            xml.tag("property", args, vals);

            vals =
                new String[] {
                    "mavinci.title",
                    languageHelper.getString(
                        "eu.mavinci.desktop.gui.doublepanel.planemain.ActionManager"
                            + ".actMatchingExportAgisoftProject"
                            + ".report.title")
                };
            xml.tag("property", args, vals);

            if (usePhotoScanLevelArmProcessing) {
                vals = new String[] {"mavinci.cam.roll", rpyCamOrientation[0] + ""};
                xml.tag("property", args, vals);
                vals = new String[] {"mavinci.cam.pitch", rpyCamOrientation[1] + ""};
                xml.tag("property", args, vals);
                vals = new String[] {"mavinci.cam.yaw", rpyCamOrientation[2] + ""};
                xml.tag("property", args, vals);

                vals =
                    new String[] {
                        "mavinci.cam.x",
                        "" + (cameraDesc.getOffsetToRightWing().convertTo(Unit.METER).getValue().doubleValue())
                    };
                xml.tag("property", args, vals);
                vals =
                    new String[] {
                        "mavinci.cam.y",
                        "" + (-cameraDesc.getOffsetToTail().convertTo(Unit.METER).getValue().doubleValue())
                    };
                xml.tag("property", args, vals);
                vals =
                    new String[] {
                        "mavinci.cam.z",
                        "" + (cameraDesc.getOffsetToSky().convertTo(Unit.METER).getValue().doubleValue())
                    };
                xml.tag("property", args, vals);
            }

            xml.end(); // meta

            args = new String[] {"next_id", "active_id"};
            vals = new String[] {"1", "0"};
            xml.start("chunks", args, vals);

            args = new String[] {"id", "path"};
            vals = new String[] {"0", "0/chunk.zip"};
            xml.tag("chunk", args, vals);
            xml.end(); // chunks

            xml.end(); // document

            xml.finish("");
            out.close();

            FileHelper.zipSingleFile(fileProjectInner, fileProject);

            // #################################################################
            // chunk.zip
            out = new PrintWriter(fileChunkInner, "UTF-8");
            xml = new XMLWriter(out);
            xml.begin("", 2);
            xml.comment(licenceManager.getExportHeaderCore());

            args = new String[] {"label", "enabled"};
            vals = new String[] {"Chunk 1", "true"};
            xml.start("chunk", args, vals);

            xml.start("sensors", "next_id", "1");
            if (currentMatching.getNumberOfImagesPerPosition() > 1) {
                args = new String[] {"id", "label"};
                vals = new String[] {"0", cameraDesc.getName()};
                xml.start("sensor", args, vals);

                for (String band : currentMatching.getBandNames()) {
                    args = new String[] {"type"};
                    vals = new String[] {useMasks ? "fisheye" : "frame"};
                    xml.start("plane", args, vals);

                    xml.start("bands");
                    xml.tag("band", "label", band);
                    xml.end(); // bands

                    args = new String[] {"width", "height"};
                    vals =
                        new String[] {
                            Integer.toString(cameraDesc.getCcdResX()), Integer.toString(cameraDesc.getCcdResY())
                        };
                    xml.tag("resolution", args, vals);

                    args = new String[] {"name", "value"};

                    vals =
                        new String[] {
                            "pixel_width",
                            ""
                                + (cameraDesc.getCcdWidth().convertTo(Unit.MILLIMETER).getValue().doubleValue()
                                    / cameraDesc.getCcdResX())
                        };
                    xml.tag("property", args, vals);

                    vals =
                        new String[] {
                            "pixel_height",
                            ""
                                + (cameraDesc.getCcdHeight().convertTo(Unit.MILLIMETER).getValue().doubleValue()
                                    / cameraDesc.getCcdResY())
                        };
                    xml.tag("property", args, vals);

                    vals =
                        new String[] {
                            "focal_length",
                            "" + lensDesc.getFocalLength().convertTo(Unit.MILLIMETER).getValue().doubleValue()
                        };
                    xml.tag("property", args, vals);

                    vals = new String[] {"fixed", "false"};
                    xml.tag("property", args, vals);

                    File fCalBand = new File(fCal.getAbsoluteFile() + "_" + band);
                    if (fCalBand.exists()) {
                        String cal = FileHelper.readFileAsString(fCalBand);
                        cal = cal.replaceFirst(Pattern.quote("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"), "");
                        xml.println(cal);
                    }

                    xml.end(); // plane
                }
            } else {
                args = new String[] {"id", "label", "type"};
                vals = new String[] {"0", cameraDesc.getName(), useMasks ? "fisheye" : "frame"};
                xml.start("sensor", args, vals);

                args = new String[] {"width", "height"};
                vals =
                    new String[] {Integer.toString(cameraDesc.getCcdResX()), Integer.toString(cameraDesc.getCcdResY())};
                xml.tag("resolution", args, vals);

                args = new String[] {"name", "value"};

                vals =
                    new String[] {
                        "pixel_width",
                        ""
                            + (cameraDesc.getCcdWidth().convertTo(Unit.MILLIMETER).getValue().doubleValue()
                                / cameraDesc.getCcdResX())
                    };
                xml.tag("property", args, vals);

                vals =
                    new String[] {
                        "pixel_height",
                        ""
                            + (cameraDesc.getCcdHeight().convertTo(Unit.MILLIMETER).getValue().doubleValue()
                                / cameraDesc.getCcdResY())
                    };
                xml.tag("property", args, vals);

                vals =
                    new String[] {
                        "focal_length",
                        "" + lensDesc.getFocalLength().convertTo(Unit.MILLIMETER).getValue().doubleValue()
                    };
                xml.tag("property", args, vals);

                vals = new String[] {"fixed", "false"};
                xml.tag("property", args, vals);

                if (fCal.exists()) {
                    String cal = FileHelper.readFileAsString(fCal);
                    cal = cal.replaceFirst(Pattern.quote("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"), "");
                    xml.println(cal);
                }
            }

            if (usePhotoScanLevelArmProcessing) {
                xml.start("antenna");
                /*
                 * X/Y/Z specify antenna offset relative to the camera. Z axis points backwards, X axis points to the right, Y axis
                 * points up (as seen by the camera). For example, if antenna is placed above the camera, the offset will be
                 * something like (0, 0, d) Yaw/Pitch/Roll specify antenna orientation relative to the camera (camera axes defined
                 * as above). In typical configuration when a flight direction is up (as seen by the camera), Yaw/Pitch/Roll angles
                 * are zero. If you are unsure about Yaw/Pitch/Roll angles, you can use the following trick: - Uncheck Fix GPS/INS
                 * offset - Set Yaw/Pitch/Roll accuracy to a large value (like 200 degrees) - Optimize After optimization you should
                 * see automatically calculated compensation angles in the adjusted fields. >> whats zero? Zero X/Y/Z means that
                 * antenna is placed at the camera projection center. Zero Yaw/Pitch/Roll means that antenna has the same
                 * orientation as camera.
                 */
                args =
                    new String[] {"x", "y", "z", "yaw", "pitch", "roll", "sx", "sy", "sz", "syaw", "spitch", "sroll"};
                // the next vector is specified in a way to NOT be transformed into camera system by any additional
                // rotation if the
                // exportet rotation vector is also zero
                vals =
                    new String[] {
                        "" + (cameraDesc.getOffsetToRightWing().convertTo(Unit.METER).getValue().doubleValue()),
                        "" + (-cameraDesc.getOffsetToTail().convertTo(Unit.METER).getValue().doubleValue()),
                        "" + cameraDesc.getOffsetToSky().convertTo(Unit.METER).getValue().doubleValue(),
                        "" + rpyCamOrientation[2],
                        "" + rpyCamOrientation[1],
                        "" + rpyCamOrientation[0],
                        "0.001",
                        "0.001",
                        "0.001",
                        "10",
                        "5",
                        "5"
                    }; // error in shift in meter, error in
                // orientation in deg
                xml.tag("reference", args, vals);

                args = new String[] {"name", "value"};
                vals = new String[] {"fixed", "false"};
                xml.tag("property", args, vals);

                xml.end(); // antenna
            }

            xml.end(); // sensor
            xml.end(); // sensors

            args = new String[] {"next_id", "next_group_id"};
            vals = new String[] {vs.size() + "", "0"};
            xml.start("cameras", args, vals);

            i = 0;
            for (MapLayerMatch match : matches) {
                if (!match.isExportPassFilter()) {
                    continue;
                }

                File img = match.getResourceFile();

                args = new String[] {"id", "label", "sensor_id", "enabled"};
                vals =
                    new String[] {
                        Integer.toString(i), img.getName(), "0", match.isExportPassFilter() ? "true" : "false"
                    };
                xml.start("camera", args, vals);

                xml.contentTag("orientation", "1");

                CPhotoLogLine line = match.getPhotoLogLine();
                double actPitch;
                double actRoll;
                double actYaw;
                if (usePhotoScanLevelArmProcessing) {
                    // 90deg offset is stored inside the levelArm Offset
                    double time = CameraHelper.computePlgDataDelay(line, hardwareConfig) / 1000;
                    actPitch = line.cameraPitch + line.cameraPitchRate * time;
                    actRoll = -(line.cameraRoll + line.cameraRollRate * time);
                    actYaw = line.cameraYaw + line.cameraYawRate * time;
                } else {
                    OrientationData o = CameraHelper.getCorrectedOrientation(line, 90, hardwareConfig);
                    actPitch = o.pitch;
                    actRoll = -o.roll;
                    actYaw = o.yaw;
                }

                args = new String[] {"x", "y", "z", "yaw", "pitch", "roll", "enabled"};

                SRStransformCacheEntry v = vs.get(i);

                vals =
                    new String[] {
                        Double.toString(v.x),
                        Double.toString(v.y),
                        Double.toString(v.z),
                        Double.toString(actYaw),
                        Double.toString(actPitch),
                        Double.toString(actRoll),
                        "true"
                    };
                xml.tag("reference", args, vals);

                xml.end(); // camera
                i++;
            }

            xml.end(); // cameras

            xml.start("frames", "next_id", "1");
            args = new String[] {"id", "path"};
            vals = new String[] {"0", "0/frame.zip"};
            xml.tag("frame", args, vals);
            xml.end(); // frames

            xml.contentTag("reference", srsWithoutVertical.getWkt());

            xml.start("settings");

            vals = new String[] {"accuracy_cameras", "" + gpsAccuracyXY};
            args = new String[] {"name", "value"};
            xml.tag("property", args, vals);

            vals = new String[] {"accuracy_cameras_ypr", "10"};
            xml.tag("property", args, vals);

            vals = new String[] {"accuracy_tiepoints", "0.5"};
            xml.tag("property", args, vals);

            vals = new String[] {"accuracy_markers", "0.02"};
            xml.tag("property", args, vals);

            vals = new String[] {"accuracy_projections", "0.1"};
            xml.tag("property", args, vals);

            xml.end(); // settings

            // <settings> //inside chunk.zip
            // <property name="accuracy_tiepoints" value="666"/>
            // <property name="accuracy_cameras" value="111"/>
            // <property name="accuracy_cameras_ypr" value="222"/>
            // <property name="accuracy_markers" value="333"/>
            // <property name="accuracy_scalebars" value="444"/>
            // <property name="accuracy_projections" value="555"/>
            // </settings>

            xml.start("meta");
            args = new String[] {"name", "value"};
            vals = new String[] {"ground_altitude", "" + groundLevel};
            xml.tag("property", args, vals);
            xml.end(); // meta

            xml.end(); // chunk

            xml.finish("");
            out.close();

            FileHelper.zipSingleFile(fileChunkInner, fileChunk);

            // #################################################################
            // frame.zip
            out = new PrintWriter(fileFrameInner, "UTF-8");
            xml = new XMLWriter(out);
            xml.begin("", 2);
            xml.comment(licenceManager.getExportHeaderCore());
            xml.start("frame");

            xml.start("cameras");
            i = 0;
            for (MapLayerMatch match : matches) {
                if (!match.isExportPassFilter()) {
                    continue;
                }

                xml.start("camera", "camera_id", Integer.toString(i));

                if (currentMatching.getNumberOfImagesPerPosition() > 1) {
                    for (int p = 0; p != currentMatching.getNumberOfImagesPerPosition(); p++) {
                        File img = match.getResourceCube().photoFiles[p].getFile();
                        args = new String[] {"plane", "path"};
                        vals = new String[] {"" + p, FileHelper.getRelativePath(img, fileFrame, "\\")};
                        xml.start("photo", args, vals);

                        xml.start("meta");

                        args = new String[] {"name", "value"};

                        vals =
                            new String[] {
                                "Exif/FocalLength",
                                Double.toString(
                                    lensDesc.getFocalLength().convertTo(Unit.MILLIMETER).getValue().doubleValue())
                            };
                        xml.tag("property", args, vals);

                        vals =
                            new String[] {
                                "Exif/FocalLengthIn35mmFilm",
                                Double.toString(CameraHelper.getFocalLength35mm(hardwareConfig))
                            };
                        xml.tag("property", args, vals);

                        vals = new String[] {"Exif/Make", "MAVinci"};
                        xml.tag("property", args, vals);

                        vals = new String[] {"Exif/Model", cameraDesc.getName()};
                        xml.tag("property", args, vals);

                        vals = new String[] {"Exif/Orientation", "1"};
                        xml.tag("property", args, vals);

                        vals = new String[] {"File/ImageHeight", Integer.toString(cameraDesc.getCcdResY())};
                        xml.tag("property", args, vals);

                        vals = new String[] {"File/ImageWidth", Integer.toString(cameraDesc.getCcdResX())};
                        xml.tag("property", args, vals);

                        CPhotoLogLine line = match.getPhotoLogLine();
                        vals =
                            new String[] {
                                "MAVinci/gpsFixType/No", line.fixType == null ? "-1" : line.fixType.ordinal() + ""
                            };
                        xml.tag("property", args, vals);
                        vals =
                            new String[] {
                                "MAVinci/gpsFixType/Name", line.fixType == null ? "null" : line.fixType.toString()
                            };
                        xml.tag("property", args, vals);

                        xml.end(); // meta
                        xml.end(); // photo
                    }
                } else {
                    File img = match.getResourceFile();
                    xml.start("photo", "path", FileHelper.getRelativePath(img, fileFrame, "\\"));

                    xml.start("meta");

                    args = new String[] {"name", "value"};

                    vals =
                        new String[] {
                            "Exif/FocalLength",
                            Double.toString(
                                lensDesc.getFocalLength().convertTo(Unit.MILLIMETER).getValue().doubleValue())
                        };
                    xml.tag("property", args, vals);

                    vals =
                        new String[] {
                            "Exif/FocalLengthIn35mmFilm",
                            Double.toString(CameraHelper.getFocalLength35mm(hardwareConfig))
                        };
                    xml.tag("property", args, vals);

                    vals = new String[] {"Exif/Make", "MAVinci"};
                    xml.tag("property", args, vals);

                    vals = new String[] {"Exif/Model", cameraDesc.getName()};
                    xml.tag("property", args, vals);

                    vals = new String[] {"Exif/Orientation", "1"};
                    xml.tag("property", args, vals);

                    vals = new String[] {"File/ImageHeight", Integer.toString(cameraDesc.getCcdResY())};
                    xml.tag("property", args, vals);

                    vals = new String[] {"File/ImageWidth", Integer.toString(cameraDesc.getCcdResX())};
                    xml.tag("property", args, vals);

                    CPhotoLogLine line = match.getPhotoLogLine();
                    vals =
                        new String[] {
                            "MAVinci/gpsFixType/No", line.fixType == null ? "-1" : line.fixType.ordinal() + ""
                        };
                    xml.tag("property", args, vals);
                    vals =
                        new String[] {
                            "MAVinci/gpsFixType/Name", line.fixType == null ? "null" : line.fixType.toString()
                        };
                    xml.tag("property", args, vals);

                    xml.end(); // meta
                    xml.end(); // photo
                }

                xml.end(); // camera
                i++;
            }

            xml.end(); // cameras

            xml.tag("thumbnails", "path", "thumbnails/thumbnails.zip");

            if (cropToAOI) {
                xml.tag("shapes", "path", "shapes/shapes.zip");
            }

            if (useMasks) {
                xml.tag("masks", "path", "masks/masks.zip");
            }

            xml.end(); // frame
            xml.finish("");
            out.close();

            FileHelper.zipSingleFile(fileFrameInner, fileFrame);

            // #################################################################
            // thumpnails.zip
            out = new PrintWriter(fileThumpsInner, "UTF-8");
            xml = new XMLWriter(out);
            xml.begin("", 2);
            xml.comment(licenceManager.getExportHeaderCore());
            xml.start("thumbnails", "version", "1.2.0");
            Vector<File> filesThumb = new Vector<File>();

            i = 0;
            for (MapLayerMatch match : matches) {
                if (!match.isExportPassFilter()) {
                    continue;
                }

                File thumpFile = match.getCurPhotoFile().getThumpFile();

                args = new String[] {"camera_id", "path"};
                vals = new String[] {i + "", thumpFile.getName()};
                xml.tag("thumbnail", args, vals);

                // its actually not necessary to generate the priviews...
                // photoscan will do it on its own when it needs the images when needed (lazy)
                // but photoscan loads faster if we provide the images ;-)
                if (thumpFile.exists()) {
                    filesThumb.add(thumpFile);
                }

                i++;
            }

            xml.end(); // thumpnails
            xml.finish("");
            out.close();
            // FileHelper.zipSingleFile(fileThumpsInner,fileThumps);
            filesThumb.add(fileThumpsInner);
            FileHelper.zipManyFiles(fileThumps, filesThumb);

            // System.out.println("useMasks:"+useMasks);
            // export mask??
            if (useMasks) {
                File folderMasks = new File(folderFrame, "masks");
                if (!folderMasks.mkdir()) {
                    throw new IOException("could not create folder: " + folderMasks);
                }

                Vector<File> files = new Vector<File>();
                File fileMasks = new File(folderMasks, "masks.zip");
                File fileMasksInner = new File(folderMasks, "doc.xml");

                out = new PrintWriter(fileMasksInner, "UTF-8");
                xml = new XMLWriter(out);
                xml.begin("", 2);
                xml.comment(licenceManager.getExportHeaderCore());
                xml.start("masks", "version", "1.2.0");

                i = 0;
                for (MapLayerMatch match : matches) {
                    if (!match.isExportPassFilter()) {
                        continue;
                    }

                    ImageMask imageMask = match.getMaskWide();
                    if (imageMask == null) {
                        throw new IOException("please wait until mask is computed and repo export");
                    }
                    // System.out.println("writing masks stuff:" + i);
                    File maskTarget = new File(folderMasks, "c" + i + ".png");
                    FileHelper.copyFile(imageMask.getFile(), maskTarget);
                    args = new String[] {"camera_id", "path"};
                    vals = new String[] {i + "", maskTarget.getName()};
                    xml.tag("mask", args, vals);
                    files.add(maskTarget);
                    i++;
                }

                xml.end(); // masks
                xml.finish("");
                out.close();
                files.add(fileMasksInner);
                FileHelper.zipManyFiles(fileMasks, files);

                for (File f : files) {
                    if (!f.delete()) {
                        LOG.warn("Unable to delete {}", f.getAbsolutePath());
                    }
                }
            }

            // install geotiff
            File geoid = srs.getGeoidFile();
            if (geoid != null) {
                FileHelper.copyFile(
                    geoid,
                    new File(
                        new File(pathSettings.getAgiSoftPhotoScanPath().getParent().toFile(), "geoids"),
                        geoid.getName()),
                    true,
                    true);
            }
        } else {
            // Photoscan 1.1 or below
            File folder = target.getParentFile();
            PrintWriter out;
            File innerTarget = new File(target.getParentFile(), "doc.xml");
            if (target.exists()) {
                if (!target.delete()) {
                    LOG.warn("Unable to delete {}", target.getAbsolutePath());
                }
            }

            if (innerTarget.exists()) {
                if (!innerTarget.delete()) {
                    LOG.warn("Unable to delete {}", innerTarget.getAbsolutePath());
                }
            }

            out = new PrintWriter(innerTarget, "UTF-8");

            XMLWriter xml = new XMLWriter(out);
            xml.begin("<document version=\"1.0.0\">\n", 2);
            xml.comment(licenceManager.getExportHeaderCore());
            args = new String[] {"label", "active", "enabled"};
            vals = new String[] {"Chunk 1", "true", "true"};
            xml.start("chunk", args, vals);

            xml.start("sensors");
            args = new String[] {"id", "label", "type"};
            vals = new String[] {"0", cameraDesc.getName(), "frame"};
            xml.start("sensor", args, vals);

            args = new String[] {"width", "height"};
            vals = new String[] {Integer.toString(cameraDesc.getCcdResX()), Integer.toString(cameraDesc.getCcdResY())};
            xml.tag("resolution", args, vals);

            args = new String[] {"name", "value"};

            vals =
                new String[] {
                    "pixel_width",
                    ""
                        + (cameraDesc.getCcdWidth().convertTo(Unit.MILLIMETER).getValue().doubleValue()
                            / cameraDesc.getCcdResX())
                };
            xml.tag("property", args, vals);

            vals =
                new String[] {
                    "pixel_height",
                    ""
                        + (cameraDesc.getCcdHeight().convertTo(Unit.MILLIMETER).getValue().doubleValue()
                            / cameraDesc.getCcdResY())
                };
            xml.tag("property", args, vals);

            vals =
                new String[] {
                    "focal_length", "" + lensDesc.getFocalLength().convertTo(Unit.MILLIMETER).getValue().doubleValue()
                };
            xml.tag("property", args, vals);

            vals = new String[] {"fixed", "false"};
            xml.tag("property", args, vals);

            if (fCal.exists()) {
                String cal = FileHelper.readFileAsString(fCal);
                cal = cal.replaceFirst(Pattern.quote("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"), "");
                xml.println(cal);
            }

            xml.end(); // sensor
            xml.end(); // sensors

            xml.start("cameras");

            i = 0;
            for (MapLayerMatch match : matches) {
                if (!match.isExportPassFilter()) {
                    continue;
                }

                File img = match.getResourceFile();

                args = new String[] {"id", "label", "sensor_id", "enabled"};
                vals =
                    new String[] {
                        Integer.toString(i), img.getName(), "0", match.isExportPassFilter() ? "true" : "false"
                    };
                xml.start("camera", args, vals);

                args = new String[] {"width", "height"};
                vals =
                    new String[] {Integer.toString(cameraDesc.getCcdResX()), Integer.toString(cameraDesc.getCcdResY())};
                xml.tag("resolution", args, vals);

                xml.start("frames");
                xml.start("frame");
                args = new String[] {"path"};
                vals = new String[] {FileHelper.getRelativePath(img, folder, "\\")};
                xml.start("image", args, vals);
                xml.start("meta");

                args = new String[] {"name", "value"};
                vals = new String[] {"Exif/Make", "MAVinci"};
                xml.tag("property", args, vals);

                vals = new String[] {"Exif/Model", cameraDesc.getName()};
                xml.tag("property", args, vals);

                vals =
                    new String[] {
                        "Exif/FocalLength",
                        Double.toString(lensDesc.getFocalLength().convertTo(Unit.MILLIMETER).getValue().doubleValue())
                    };
                xml.tag("property", args, vals);

                vals = new String[] {"Exif/Orientation", "1"};
                xml.tag("property", args, vals);

                vals =
                    new String[] {
                        "Exif/FocalLengthIn35mmFilm", Double.toString(CameraHelper.getFocalLength35mm(hardwareConfig))
                    };
                xml.tag("property", args, vals);

                CPhotoLogLine line = match.getPhotoLogLine();
                vals =
                    new String[] {"MAVinci/gpsFixType/No", line.fixType == null ? "-1" : line.fixType.ordinal() + ""};
                xml.tag("property", args, vals);
                vals =
                    new String[] {"MAVinci/gpsFixType/Name", line.fixType == null ? "null" : line.fixType.toString()};
                xml.tag("property", args, vals);

                xml.end(); // meta
                xml.end(); // image
                xml.end(); // frame
                xml.end(); // frames

                xml.end(); // camera
                i++;
            }

            xml.end(); // cameras

            xml.start("ground_control");
            xml.contentTag("reference", srsWithoutVertical.getWkt());
            i = 0;
            double gpsAccuracy = 0;
            int gpsAccuracyCnt = 0;
            ArrayList<Position> ps = new ArrayList<>();
            for (MapLayerMatch match : matches) {
                if (!match.isExportPassFilter()) {
                    continue;
                }

                Position p = match.getShiftedPositionExport(hardwareConfig);
                ps.add(p);
            }
            // batch gdal transformation
            List<SRStransformCacheEntry> vs = srsWithoutVertical.fromWgs84(ps);

            for (MapLayerMatch match : matches) {
                if (!match.isExportPassFilter()) {
                    continue;
                }

                CPhotoLogLine line = match.getPhotoLogLine();
                OrientationData o = CameraHelper.getCorrectedOrientation(line, 90, hardwareConfig);

                args = new String[] {"camera_id", "enabled", "x", "y", "z", "yaw", "pitch", "roll"};

                SRStransformCacheEntry v = vs.get(i);

                vals =
                    new String[] {
                        Integer.toString(i),
                        String.valueOf(match.isExportPassFilter()),
                        Double.toString(v.x),
                        Double.toString(v.y),
                        Double.toString(v.z),
                        Double.toString(o.yaw),
                        Double.toString(o.pitch),
                        Double.toString(-1 * o.roll)
                    };
                xml.tag("record", args, vals);
                if (line.fixType != null) {
                    gpsAccuracy += line.fixType.getXYaccuracy();
                    gpsAccuracyCnt++;
                }

                i++;
            }

            if (gpsAccuracyCnt == 0) {
                gpsAccuracy = 2;
            } else {
                gpsAccuracy /= gpsAccuracyCnt;
            }

            xml.start("settings");

            args = new String[] {"name", "value"};
            vals = new String[] {"accuracy_cameras", gpsAccuracy + ""};
            xml.tag("property", args, vals);

            vals =
                new String[] {
                    "accuracy_tiepoints", "4"
                }; // tie point accuracy has an internal other meaning in photoscan <= 1.1.x
            xml.tag("property", args, vals);

            vals = new String[] {"accuracy_markers", "0.02"};
            xml.tag("property", args, vals);

            vals = new String[] {"accuracy_projections", "0.1"};
            xml.tag("property", args, vals);

            xml.end(); // settings

            xml.end(); // ground_control

            xml.contentTag(
                "transform",
                "1.0000000000000000e+00 0.0000000000000000e+00 "
                    + "0.0000000000000000e+00 4.1140940365649885e+06 "
                    + "0.0000000000000000e+00 1.0000000000000000e+00 "
                    + "0.0000000000000000e+00 6.2524933623027836e+05 "
                    + "0.0000000000000000e+00 0.0000000000000000e+00 "
                    + "1.0000000000000000e+00 4.8177628453036696e+06 "
                    + "0.0000000000000000e+00 0.0000000000000000e+00 "
                    + "0.0000000000000000e+00 1.0000000000000000e+00");
            xml.contentTag("reference", srsWithoutVertical.getWkt());

            xml.end(); // chunk

            double resolution = currentMatching.estimateGsd();

            // LatLon center = currentMatching.getSector().getCentroid();

            // write metadata for MAVinci processing script
            xml.start("meta");
            args = new String[] {"name", "value"};

            vals = new String[] {"mavinci.camAccuracy", gpsAccuracy + ""};
            xml.tag("property", args, vals);

            vals = new String[] {"mavinci.wkt", srs.getWkt()};
            xml.tag("property", args, vals);

            vals = new String[] {"mavinci.wktName", srs.toString()};
            xml.tag("property", args, vals);

            vals = new String[] {"mavinci.useInternalWKT", "1"};
            xml.tag("property", args, vals);

            vals = new String[] {"mavinci.align.quality", gpsType == GPStype.GPS ? "medium" : "high"};
            xml.tag("property", args, vals);
            // vals = new String[] { "mavinci.dense.quality", gpsType ==GPStype.GPS ? "medium" : "high" };
            vals = new String[] {"mavinci.dense.quality", "medium"};
            xml.tag("property", args, vals);
            vals = new String[] {"mavinci.mesh.quality", gpsType == GPStype.GPS ? "medium" : "high"};
            xml.tag("property", args, vals);

            vals = new String[] {"mavinci.dense.enable", "1"};
            xml.tag("property", args, vals);
            vals = new String[] {"mavinci.mesh.enable", "1"};
            xml.tag("property", args, vals);

            vals = new String[] {"mavinci.exportHeaderCore", licenceManager.getExportHeaderCore()};
            xml.tag("property", args, vals);

            vals = new String[] {"mavinci.gpsType", gpsType.toString()};
            xml.tag("property", args, vals);

            vals = new String[] {"mavinci.ortho.enable", "1"};
            xml.tag("property", args, vals);
            vals = new String[] {"mavinci.ortho.gsd", resolution + ""};
            xml.tag("property", args, vals);
            vals = new String[] {"mavinci.ortho.enableColCorrection", "0"};
            xml.tag("property", args, vals);

            vals = new String[] {"mavinci.dsm.enable", "1"};
            xml.tag("property", args, vals);
            vals = new String[] {"mavinci.dsm.gsd", (2 * resolution) + ""};
            xml.tag("property", args, vals);

            // depricated, but still here for compatibility with old plugins:

            vals = new String[] {"mavinci.quality", gpsType == GPStype.GPS ? "medium" : "high"};
            xml.tag("property", args, vals);

            vals = new String[] {"mavinci.resolution", resolution + ""};
            xml.tag("property", args, vals);

            vals = new String[] {"mavinci.enableColCorrection", "0"};
            xml.tag("property", args, vals);

            xml.end(); // meta

            xml.finish("</document>\n");
            out.close();

            FileHelper.zipSingleFile(innerTarget, target);
        }

        startAgiSoft(target);
    }

    @Override
    public boolean isWriteExifDataAllowed(Matching matching) {
        if (matching == null) {
            return false;
        }

        return !matching.getPictures().isEmpty();
    }

    @Override
    public void writeExifData(
            Consumer<IBackgroundTaskManager.ProgressStageFirer> callback,
            BooleanSupplier cancelIndicator,
            Matching matching)
            throws Exception {
        final IHardwareConfiguration hardwareConfiguration = matching.getHardwareConfiguration();

        List<IMapLayer> pictures = matching.getPictures();
        IBackgroundTaskManager.ProgressStageFirer firer =
            new IBackgroundTaskManager.ProgressStageFirer(pictures.size());
        final AtomicInteger counter = new AtomicInteger(0);
        for (IMapLayer layer : pictures) {
            if (cancelIndicator.getAsBoolean()) {
                return;
            }

            MapLayerMatch picture = (MapLayerMatch)layer;
            for (PhotoFile pf : picture.getResourceCube()) {
                if (cancelIndicator.getAsBoolean()) {
                    return;
                }

                if (pf.getMatch().isExportPassFilter()) {
                    pf.writeMetadata(hardwareConfiguration);
                }
            }

            firer.setCurrentStage(counter.getAndAdd(1));
            callback.accept(firer);
        }
    }

    @Override
    public Function<Matching, String> getTagretFilePath(ExportTypes type) {
        return targetObtainFunctions.get(type);
    }

    private void startAgiSoft(File target) throws IOException {
        if (pathSettings.getAgiSoftPhotoScanPath() == null) {
            throw new IOException(
                "no PhotoScan / Metashape installation path known. Could not start PhotoScan / Metashape.");
        }

        AgiSoftVersion version = detectAgiSoftVersion();

        String resource;
        String resourceInit = "IntelInit.py";
        if (version.version == null) {
            LOG.warn("Exporting Agisoft Plugin 1.0");
            resource = "IntelLib_1-0";
        } else if (version.version.startsWith("1.1")) {
            LOG.warn("Exporting Agisoft Plugin 1.1");
            resource = "IntelLib_1-1";
        } else if (version.version.startsWith("1.2")) {
            LOG.warn("Exporting Agisoft Plugin 1.2");
            resource = "IntelLib_1-2";
        } else if (version.version.startsWith("1.3")) {
            LOG.warn("Exporting Agisoft Plugin 1.3");
            resource = "IntelLib_1-3";
            resourceInit = "IntelInit_1-3.py";
        } else if (version.version.startsWith("1.4")) {
            LOG.warn("Exporting Agisoft Plugin 1.4");
            resource = "IntelLib_1-4";
            resourceInit = "IntelInit_1-4.py";
        } else {
            LOG.warn("Exporting Agisoft Plugin 1.5");
            resource = "IntelLib_1-5";
            resourceInit = "IntelInit_1-5.py";
        }

        resource += expertSettings.getExportPhotoScanTesting();
        String libResource = "eu/mavinci/agisoft/" + resource + ".py";

        // i18n python code
        try (InputStream inputLibResource = ClassLoader.getSystemResourceAsStream(libResource)) {
            String pythonCode = i18nAgisoftScript(version, resource, inputLibResource);

            if (versionProvider.getSystem().isWindows()) {
                installScripts(getAgisoftScriptsFolder(), pythonCode, resourceInit);
                installScripts(getAgisoftTopconScriptsFolder(), pythonCode, resourceInit);
            } else if (versionProvider.getSystem().isMac()) {
                installScripts(
                    new File(
                        System.getProperty("user.home"), "Library/Application Support/Agisoft/PhotoScan Pro/scripts/"),
                    pythonCode,
                    resourceInit);
                installScripts(
                    new File(
                        System.getProperty("user.home"),
                        "Library/Application Support/Agisoft/Photogrammetric Kit for TOPCON/scripts/"),
                    pythonCode,
                    resourceInit);
            } else {
                installScripts(
                    new File(System.getProperty("user.home"), ".local/share/data/Agisoft/PhotoScan Pro/scripts/"),
                    pythonCode,
                    resourceInit);
                installScripts(
                    new File(
                        System.getProperty("user.home"),
                        ".local/share/data/Agisoft/Photogrammetric Kit for TOPCON/scripts/"),
                    pythonCode,
                    resourceInit);
            }
        }
    }

    private File getAgisoftTopconScriptsFolder() {
        return new File(WinRegistryQuery.getLocalAppData(), "Agisoft\\Photogrammetric Kit for TOPCON\\scripts\\");
    }

    private File getAgisoftScriptsFolder() {
        if (pathSettings.agiSoftIsMetashape()) {
            return new File(WinRegistryQuery.getLocalAppData(), "Agisoft\\Metashape Pro\\scripts\\");
        } else {
            return new File(WinRegistryQuery.getLocalAppData(), "Agisoft\\PhotoScan Pro\\scripts\\");
        }
    }

    private String i18nAgisoftScript(AgiSoftVersion version, String resource, InputStream inputLibResource)
            throws IOException {
        String pythonCode = StringHelper.readStreamUTF8(inputLibResource);
        Pattern i18nPattern = Pattern.compile("\\[\\[(.*?)\\]\\]");

        Matcher m = i18nPattern.matcher(pythonCode);
        TreeSet<String> keys = new TreeSet<String>();
        while (m.find()) {
            String s = m.group(1);
            keys.add(s);
        }

        for (String key : keys) {
            String str = StringHelper.escapeSpecialChars(languageHelper.getString("agisoft." + key));
            str = str.replaceAll(Pattern.quote("_"), Matcher.quoteReplacement("&"));
            pythonCode = pythonCode.replaceAll(Pattern.quote("[[" + key + "]]"), Matcher.quoteReplacement(str));
        }
        // magic to make line ends always like in windows
        pythonCode = pythonCode.replaceAll("\\r\\n", "\n");
        pythonCode = pythonCode.replaceAll("\\r", "\n");
        pythonCode = pythonCode.replaceAll("\\n", "\r\n");

        pythonCode =
            pythonCode.replaceAll(
                Pattern.quote("{{mavinci.release}}"),
                Matcher.quoteReplacement(versionProvider.getBuildCommitTimeAsLong() + ""));
        String header = licenceManager.getExportHeaderCore();
        header = header.replaceAll("\"", "").replaceAll("'", "");
        pythonCode =
            pythonCode.replaceAll(
                Pattern.quote("{{mavinci.exportHeader}}"),
                Matcher.quoteReplacement(
                    header
                        + " exported for PhotoScan / Metashape VERSION:"
                        + version.version
                        + " build:"
                        + version.build
                        + " resourceFile:"
                        + resource));
        return pythonCode;
    }

    private void executeAgiSoftFile(File target) throws IOException {
        if (versionProvider.getSystem().isMac()) {
            String agisoftBinary = "/Contents/MacOS/PhotoScanPro";
            File agisoftPath = new File(pathSettings.getAgiSoftPhotoScanPath() + agisoftBinary);
            if (!agisoftPath.exists()) {
                agisoftBinary = "/Contents/MacOS/APK";
                agisoftPath = new File(pathSettings.getAgiSoftPhotoScanPath() + agisoftBinary);
            }

            if (!agisoftPath.exists()) {
                agisoftBinary = "";
            }

            ProcessHelper.exec(
                new String[] {pathSettings.getAgiSoftPhotoScanPath() + agisoftBinary, target.getAbsolutePath()});
        } else {
            LOG.trace("Execute {} {}", pathSettings.getAgiSoftPhotoScanPath(), target.getAbsolutePath());
            ProcessHelper.exec(
                new String[] {pathSettings.getAgiSoftPhotoScanPath().toString(), target.getAbsolutePath()});
        }
    }

    private void installScripts(File scriptFolder, String pythonCode, String resourceInit) throws IOException {
        if (!scriptFolder.mkdirs() && !scriptFolder.isDirectory()) {
            LOG.warn("Unable to create directories for scripts. Path - {}", scriptFolder.getAbsolutePath());
        }
        // drop old files
        File scriptOld = new File(scriptFolder, "MAVinci-Menue.py"); // "-" chars give problems on manually importing
        if (scriptOld.exists()) {
            if (!scriptOld.delete()) {
                LOG.warn("Unable to delete script file {}", scriptOld.getAbsolutePath());
            }
        }

        scriptOld = new File(scriptFolder, "MAVinciMenue.py"); // "-" chars give problems on manually importing
        if (scriptOld.exists()) {
            if (!scriptOld.delete()) {
                LOG.warn("Unable to delete script file {}", scriptOld.getAbsolutePath());
            }
        }

        File script = new File(scriptFolder, "IntelLib.py");

        try (FileOutputStream fs = new FileOutputStream(script);
            OutputStreamWriter os = new OutputStreamWriter(fs, "UTF-8")) {
            os.write(pythonCode);
            os.close();
            LOG.trace("written Photoscan / Metashape python code to {}", script);
        }

        FileHelper.writeResourceToFile("eu/mavinci/agisoft/" + resourceInit, new File(scriptFolder, "IntelInit.py"));
        LOG.trace("written Photoscan / Metashape python code done. folder {}", scriptFolder);
    }

    private AgiSoftVersion detectAgiSoftVersion() {
        AgiSoftVersion version = new AgiSoftVersion();
        try {
            if (pathSettings.getAgiSoftPhotoScanPath() == null) {
                return version;
            }

            Process process;
            if (versionProvider.getSystem().isMac()) {
                String agiSoftBinary = "/Contents/MacOS/PhotoScanPro";
                File agiSoftPath = new File(pathSettings.getAgiSoftPhotoScanPath() + agiSoftBinary);
                if (!agiSoftPath.exists()) {
                    agiSoftBinary = "/Contents/MacOS/APK";
                    agiSoftPath = new File(pathSettings.getAgiSoftPhotoScanPath() + agiSoftBinary);
                }

                if (!agiSoftPath.exists()) {
                    agiSoftBinary = "";
                }

                process =
                    ProcessHelper.exec(new String[] {pathSettings.getAgiSoftPhotoScanPath() + agiSoftBinary, "--help"});
            } else {
                process =
                    ProcessHelper.exec(new String[] {pathSettings.getAgiSoftPhotoScanPath().toString(), "--help"});
            }

            try (OutputStream stdin = process.getOutputStream();
                InputStream stderr = process.getErrorStream();
                InputStream stdout = process.getInputStream();
                BufferedReader agiSoftStdOutput =
                    new BufferedReader(new InputStreamReader(stdout, StandardCharsets.UTF_8));
                BufferedReader agiSoftStdErr =
                    new BufferedReader(new InputStreamReader(stderr, StandardCharsets.UTF_8))) {
                stdin.close();
                String line;
                // clean up if any output in stdout
                while ((line = agiSoftStdOutput.readLine()) != null) {
                    LOG.trace("[agisoft-out] {}", line);
                    line = line.toLowerCase();
                    if (line.contains("version") && !line.contains("--version")) {
                        String[] parts = line.split(" ");
                        for (int i = 0; i < parts.length - 1; i++) {
                            if ("version".equals(parts[i])) {
                                version.version = parts[i + 1];
                            } else if ("build".equals(parts[i])) {
                                version.build = Integer.parseInt(parts[i + 1]);
                            }
                        }
                    }
                }

                if ((line = agiSoftStdErr.readLine()) != null) {
                    throw new AgiSoftException("[agisoft-error]" + line);
                }
            }

        } catch (AgiSoftException e) {
            if (version.version == null || version.build == -1) {
                LOG.warn("could not extract Agisoft PhotoScan / Metashape version information", e);
            } else {
                LOG.warn("Agisoft PhotoScan / Metashape reported an error:", e);
            }
        } catch (Exception e) {
            LOG.warn("could not extract Agisoft PhotoScan / Metashape version information", e);
        }

        LOG.trace("Agisoft VERSION: {}  build: {}", version.version, version.build);
        return version;
    }

    private static class AgiSoftException extends Exception {
        AgiSoftException(String string) {
            super(string);
        }
    }

    private static class AgiSoftVersion {
        String version = null;
        int build = -1;

        @Override
        public String toString() {
            return version + " " + build;
        }
    }

    public boolean isNotExportedToContextCapture(Matching matching) {
        return !getContextCaptureFile(matching).exists();
    }

    private File getContextCaptureFile(Matching matching) {
        File target =
            new File(
                matching.getResourceFile().getParentFile(),
                matching.getResourceFile().getParentFile().getName()
                    + "."
                    + MFileFilter.bentleyXMLMatchingExportFilter.getExtension());

        String fileName = target.getName();
        fileName = fileName.replaceAll(Pattern.quote(" "), Matcher.quoteReplacement("_"));
        target = new File(target.getParentFile(), fileName);

        return target;
    }

    public void exportToContextCapture(Matching currentMatching, MSpatialReference srs) throws Exception {
        if (!currentMatching.assureNonRaw()) {
            throw new Exception("could not export RAW datasets");
        }

        boolean useLevelArmProcessing = false;

        boolean exportBentleyAngles = expertSettings.getExportBentleyAngles();
        Debug.getLog().log(Level.CONFIG, "Bentley Rotation written: " + exportBentleyAngles);

        File target = getContextCaptureFile(currentMatching);

        MSpatialReference srsNonVert = srs;
        try {
            String wkt = srs.getWktWithoutVertical();
            if (!wkt.equals(srs.getWkt())) {
                srsNonVert =
                    new MSpatialReference(
                        "99" + srs.id, srs.name + "_nonVert", wkt, srsManager, globes.getDefaultGlobe());
            }
        } catch (Exception e) {
            Debug.getLog().log(Level.WARNING, "could not remove vertical component from SRS", e);
        }

        // TODO get from matching
        final IHardwareConfiguration hardwareConfiguration = currentMatching.getHardwareConfiguration();
        final IGenericCameraConfiguration cameraConfig =
            hardwareConfiguration.getPrimaryPayload(IGenericCameraConfiguration.class);
        final IGenericCameraDescription cameraDesc = cameraConfig.getDescription();
        final ILensDescription lensDesc = cameraConfig.getLens().getDescription();

        List<MapLayerMatch> matches =
            Matching.sortMatches(currentMatching, AMapLayerMatching.comparatorMatchesLineOrder);

        ArrayList<Position> ps = new ArrayList<>();
        for (MapLayerMatch match : matches) {
            if (!match.isExportPassFilter()) {
                continue;
            }

            Position p = match.getShiftedPositionExport(hardwareConfiguration, !useLevelArmProcessing);
            ps.add(p);
        }
        // batch gdal transformation
        List<SRStransformCacheEntry> vs = srsNonVert.fromWgs84(ps);

        if (target.exists()) {
            if (!target.delete()) {
                LOG.warn("Unable to remove file {}", target.getAbsolutePath());
            }
        }

        PrintWriter out = new PrintWriter(target, "UTF-8");
        XMLWriter xml = new XMLWriter(out);
        xml.begin("", 2);
        xml.comment(licenceManager.getExportHeaderCore());

        xml.start("BlocksExchange", "version", "2.1");

        /// SpatialReferenceSystems

        xml.start("SpatialReferenceSystems");
        xml.start("SRS");

        String srsId = "0"; // TODO consider several systems
        xml.contentTag("Id", srsId);

        xml.contentTag("Name", srs.name);

        xml.contentTag("Definition", srs.getSR().ExportToPrettyWkt());

        xml.end(); // SRS
        xml.end(); // SpatialReferenceSystems
        /////////////////////////

        File folder;
        if (currentMatching.getResourceFile() != null) {
            folder = currentMatching.getResourceFile().getParentFile();
            xml.contentTag("BaseImagePath", folder.getAbsolutePath() + "/images/");
        } else {
            folder = target.getParentFile();
            xml.contentTag("BaseImagePath", folder.getAbsolutePath() + "/");
        }

        xml.start("Block");
        xml.contentTag("Name", folder.getName());
        xml.contentTag("Description", folder.getName());
        xml.contentTag("Type", "Aerial");
        xml.contentTag("SRSId", srsId);

        xml.start("Photogroups");

        xml.start("Photogroup");

        xml.contentTag("Name", cameraDesc.getName());

        xml.start("ImageDimensions");
        xml.contentTag("Width", Integer.toString(cameraDesc.getCcdResX()));
        xml.contentTag("Height", Integer.toString(cameraDesc.getCcdResY()));
        xml.end(); // ImageDimensions

        xml.contentTag("CameraModelType", lensDesc.getLensType() == LensTypes.FISH_EYE ? "Fisheye" : "Perspective");
        xml.contentTag("Description", folder.getName());
        xml.contentTag(
            "SensorSize",
            Double.toString(cameraDesc.getCcdWidth().convertTo(Unit.MILLIMETER).getValue().doubleValue()));
        xml.contentTag(
            "FocalLength",
            Double.toString(lensDesc.getFocalLength().convertTo(Unit.MILLIMETER).getValue().doubleValue()));
        // <!-- Possible values: XRightYDown (default), XRightYUp (more frequent in photogrammetry), XLeftYDown,
        // XLeftYUp,
        // XDownYRight, XDownYLeft, XUpYRight, XUpYLeft -->
        xml.contentTag("CameraOrientation", "XDownYRight");

        int i = 0;

        for (MapLayerMatch match : matches) {
            if (!match.isExportPassFilter()) {
                continue;
            }

            File img = match.getResourceFile();
            Expect.notNull(img, "img");
            xml.start("Photo");
            xml.contentTag("Id", Integer.toString(i));
            xml.contentTag("ImagePath", img.getAbsolutePath());

            ImageMask imageMask = match.getMaskWide();
            if (imageMask != null) {
                File maskFile = imageMask.getFile();
                File tifFile = new File(maskFile.getParentFile(), "mask.tif");

                // transform png to tiff
                if (!tifFile.exists()) {
                    ImageHelper.writeTiffWithoutGdal(ImageHelper.loadImage(maskFile), tifFile);
                }

                xml.contentTag("MaskPath", tifFile.getAbsolutePath());
            }

            xml.start("Pose");

            if (exportBentleyAngles) {
                xml.start("Rotation");

                CPhotoLogLine line = match.getPhotoLogLine();

                Matrix m = CameraHelper.getCorrectedStateTransform(line, 0, 0, hardwareConfiguration).getTranspose();

                xml.contentTag("M_00", Double.toString(m.m11));
                xml.contentTag("M_01", Double.toString(m.m12));
                xml.contentTag("M_02", Double.toString(m.m13));

                xml.contentTag("M_10", Double.toString(m.m21));
                xml.contentTag("M_11", Double.toString(m.m22));
                xml.contentTag("M_12", Double.toString(m.m23));

                xml.contentTag("M_20", Double.toString(m.m31));
                xml.contentTag("M_21", Double.toString(m.m32));
                xml.contentTag("M_22", Double.toString(m.m33));

                xml.end(); // Rotation
            }

            xml.start("Center");

            SRStransformCacheEntry v = vs.get(i);
            xml.contentTag("x", Double.toString(v.x));
            xml.contentTag("y", Double.toString(v.y));
            xml.contentTag("z", Double.toString(v.z));

            xml.end(); // Center
            xml.end(); // Pose
            xml.end(); // Photo
            i++;
        }

        xml.end(); // Photogroup

        xml.end(); // Photogroups

        xml.end(); // Block

        xml.end(); // BlocksExchange

        xml.finish("");
        out.close();

        /// FINISH

    }

    @Override
    public void openInContextCapture(Matching matching) {
        File target = getContextCaptureFile(matching);
        Dispatcher dispatcher = Dispatcher.background();
        dispatcher.run(
            () -> {
                if (pathSettings.contextCapturePathProperty().get() != null) {
                    try {
                        runBentley(target);
                    } catch (Exception e) {
                        applicationContext.addToast(
                            Toast.createDefaultFailed(languageHelper, "Context Capture opening"));
                    }
                } else {
                    try {
                        FileHelper.openFile(getContextCaptureFile(matching).getParentFile());
                    } catch (IOException e) {
                        applicationContext.addToast(
                            Toast.createDefaultFailed(
                                languageHelper, "opening ContextCapture file in explorer failed"));
                    }
                }
            });
    }

    private static AtomicInteger gobblerThreadCounter = new AtomicInteger(0);
    private void runBentley(File target) throws Exception {
        int threadCount = gobblerThreadCounter.incrementAndGet();
        /*
         * Class for redirecting the stdout of the called batch and python files to Java's stdout
         */
        class StreamGobbler extends Thread {

            private final InputStream inputStream;
            private final String gobblerType;

            private StreamGobbler(InputStream inputStream, String gobblerType) {
                super("StreamGobbler-" + threadCount + "-" + target.getName() + "-" + gobblerType);
                this.inputStream = inputStream;
                this.gobblerType = gobblerType;
            }

            @Override
            public void run() {
                try {
                    BufferedReader reader =
                        new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(gobblerType + ">>>" + line);
                    }
                } catch (IOException e) {
                    LOG.error("Error reading " + gobblerType, e);
                }
            }
        }

        if (pathSettings.getContextCapturePath() == null) {
            throw new Exception(
                languageHelper.getString(
                    "eu.mavinci.desktop.gui.doublepanel.planemain.ActionManager" + ".bentleyPathIsNull"));
        }

        File baseFolder = pathSettings.getProjectFolder().toFile();
        File bentleyBase = new File(baseFolder.getParent() + "/bentley/");
        if (!bentleyBase.exists()) {
            if (!bentleyBase.mkdir()) {
                throw new Exception("Unable to create bentley base folder " + bentleyBase.getAbsolutePath());
            }
        }

        File bat = new File(bentleyBase.getAbsolutePath(), "startBentleyPython.bat");
        File py = new File(bentleyBase.getAbsolutePath(), "loadMavinciBlock.py");

        FileHelper.writeResourceToFile("eu/mavinci/bentley/loadMavinciBlock.py", py);
        FileHelper.writeResourceToFile("eu/mavinci/bentley/startBentleyPython.bat", bat);

        String parent = pathSettings.getContextCapturePath().getParent().getParent().toString();
        Debug.getLog()
            .log(
                Level.FINE,
                "Execute "
                    + "\""
                    + bat.getPath()
                    + "\" "
                    + "\""
                    + py.getPath()
                    + "\" "
                    + "\""
                    + target.getAbsolutePath()
                    + "\" "
                    + "\""
                    + target.getParent()
                    + "\" "
                    + "\""
                    + parent
                    + "\"");
        Process proc = null;

        LOG.trace("start token exchange");

        proc =
            Runtime.getRuntime()
                .exec(
                    "\""
                        + bat.getPath()
                        + "\" "
                        + "\""
                        + py.getPath()
                        + "\" "
                        + "\""
                        + target.getAbsolutePath()
                        + "\" "
                        + "\""
                        + target.getParent()
                        + "\" "
                        + "\""
                        + parent
                        + "\" "
                        + "\""
                        + 6014
                        + "\"");

        LOG.trace("running");
        LOG.trace(
            "\""
                + bat.getPath()
                + "\" "
                + "\""
                + py.getPath()
                + "\" "
                + "\""
                + target.getAbsolutePath()
                + "\" "
                + "\""
                + target.getParent()
                + "\" "
                + "\""
                + parent
                + "\" "
                + "\""
                + 6014
                + "\"");

        StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream(), "STDOUT");
        outputGobbler.start();

        StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(), "STDERR");
        errorGobbler.start();

        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            Debug.getLog().log(Level.SEVERE, "Waiting for token exchange interrupted.", e);
        }

        BentleyAuthenticator.exchangeToken();
        try {
            proc.waitFor();
        } catch (InterruptedException e) {
            Debug.getLog().log(Level.SEVERE, "Waiting for token exchange interrupted.", e);
        }
    }

    private static boolean assureNonRaw(AMapLayerMatching matching) {
        for (IMapLayer layer : matching.getPictures()) {
            if (layer instanceof MapLayerMatch) {
                MapLayerMatch match = (MapLayerMatch)layer;

                File input = match.getResourceFile();
                if (MFileFilter.rawFilterNonTiff.accept(input)) {
                    StaticInjector.getInstance(IApplicationContext.class)
                        .addToast(
                            Toast.of(ToastType.ALERT)
                                .setText(
                                    StaticInjector.getInstance(ILanguageHelper.class)
                                        .getString(
                                            "eu.mavinci.desktop.gui.doublepanel.planemain.ActionManager"
                                                + ".matchingContainsRaw",
                                            match.getMatching().getName()))
                                .setShowIcon(true)
                                .create());
                    return false;
                }
            }
        }

        return true;
    }

    static class BentleyAuthenticator {
        static String privateKey =
            "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBALEG46SmSFbLgx0V9APwatYA8cLu\n"
                + "lTw/w0R37DQK+uu8aCmHUwUtjGaW/7UI6WImjojpKhNm2LbTeIHCKddLHjM1FkaJD1couKL8WZta\n"
                + "MhTBI8JGT4gb1KRnUzl8Yu/S1Hd1KK0OBdDr8OKV+g85urV5PYZ9M123ZjXJZZwj9whDAgElAoGB\n"
                + "AJ3jjMNIMq5GzuKXChFZ4r7eP2irRtTe7G1/tv379I0P0oXnZbiCkfqiWa9NIy4GtnocEMUyNuhA\n"
                + "EYh8vYiqx+b9SVmsC9AXGsfmaMFeUMefpuMrxbE3+h4oRVz9S3Td8rNRrvUrtuJuYXqhw/FBX/Eg\n"
                + "KTziRm5ro3VAHwxItW7tAkEA7mKiPvbT7dN9de4atisVDRWG9KqmXsDqafFoW2zj3UDgSLNXdmnL\n"
                + "J0msKQBW0wnwTES2ORrBO8VPDb7HrO5CIQJBAL4blRTU9wH1qj5DoBN94dKBqTfWvfKttzUUC8bn\n"
                + "QxvBiEz+vtMz0ZZ3+oFMkqFFMvGjyhAXN/Ntgkz4YdfvxeMCQQCUL3mcwvJxPkcR9OAsDPFo/4s+\n"
                + "JOP1tjDaEprRDFZLRAEKmP8Ea0bu6JtzbundIdqQRmNvmwJ/GclbkkTFcYMNAkAZsLNIARp14u1+\n"
                + "CSN4QXGLJkdMvCeBqMW7EIv4SMPhJ/2pijV2fJ/IOboReQzlXGDUi8FcHtA1pwPFNlJpSepPAkAW\n"
                + "QTT5utkqsCE8G/k7t5JCUSBB25gMP7YV0vnJ6BYTDtTkk3EeeSgcNB4vkZANZYrvQAZ9XCBG/WoN\n"
                + "fccJry3p"
                + "";
        static byte[] signedToken = null;
        static byte[] token = null;

        static int port = 6014;

        static void exchangeToken()
                throws IOException, NoSuchAlgorithmException, SignatureException, InvalidKeyException,
                        InvalidKeySpecException {
            Socket s = null;
            s = new Socket();
            int port = BentleyAuthenticator.port;
            s.connect(new InetSocketAddress("127.0.0.1", port));
            DataOutputStream out = new DataOutputStream(s.getOutputStream());
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));

            LOG.info("Connected to socket, waiting for token");
            String token;
            if ((token = in.readLine()) != null) {
                BentleyAuthenticator.token = token.getBytes(StandardCharsets.UTF_8);

                LOG.info("Received token:");
                LOG.info(token);

                PrivateKey privateKey = BentleyAuthenticator.loadPrivateKey();
                if (privateKey == null) {
                    throw new NullPointerException("could not load private key.");
                }

                byte[] signedToken = BentleyAuthenticator.signBentleyToken(token, privateKey);

                byte[] encode = java.util.Base64.getEncoder().encode(signedToken);
                out.write(encode, 0, encode.length);
            } else {
                throw new NullPointerException("Could not read token");
            }

            if (s != null) {
                s.close();
            }
        }

        private static PrivateKey loadPrivateKey()
                throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {
            return loadPrivateKey(BentleyAuthenticator.privateKey);
        }

        private static PrivateKey loadPrivateKey(String key)
                throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {

            // The private key should be in dem format, the following removes
            // the tags so that we can load it into java

            StringBuilder pkcs8Lines = new StringBuilder();
            BufferedReader rdr = new BufferedReader(new StringReader(key));
            String line;
            while ((line = rdr.readLine()) != null) {
                pkcs8Lines.append(line);
            }

            String pkcs8Pem = pkcs8Lines.toString();
            pkcs8Pem = pkcs8Pem.replace("-----BEGIN RSA PRIVATE KEY-----", "");
            pkcs8Pem = pkcs8Pem.replace("-----END RSA PRIVATE KEY-----", "");
            pkcs8Pem = pkcs8Pem.replaceAll("\\s+", "");

            LOG.info(pkcs8Pem);
            LOG.info(
                new String(
                    java.util.Base64.getDecoder().decode(pkcs8Pem.getBytes(StandardCharsets.UTF_8)),
                    StandardCharsets.UTF_8));

            // This may have to be activated to initialize the internal keystore
            // com.sun.org.apache.xml.internal.security.Init.init();

            byte[] clearKey = Base64.decode(pkcs8Pem, 1);

            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(clearKey);
            KeyFactory fact = null;
            fact = KeyFactory.getInstance("RSA");

            PrivateKey privateKey = null;
            privateKey = fact.generatePrivate(keySpec);

            Arrays.fill(clearKey, (byte)0);

            return privateKey;
        }

        private static byte[] signBentleyToken(String token, PrivateKey privateKey)
                throws IOException, NoSuchAlgorithmException, SignatureException, InvalidKeyException,
                        InvalidKeySpecException {
            Signature instance = null;

            if (privateKey == null) {
                privateKey = BentleyAuthenticator.loadPrivateKey();
            }

            instance = Signature.getInstance("SHA1withRSA");
            instance.initSign(privateKey);
            instance.update(token.getBytes("UTF-8"));

            byte[] signedToken = null;

            signedToken = instance.sign();

            BentleyAuthenticator.signedToken = signedToken;

            return BentleyAuthenticator.signedToken;
        }
    }
}
