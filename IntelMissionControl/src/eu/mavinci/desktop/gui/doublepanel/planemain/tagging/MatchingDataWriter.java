/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.planemain.tagging;

import com.intel.missioncontrol.hardware.IGenericCameraConfiguration;
import com.intel.missioncontrol.hardware.IHardwareConfiguration;
import com.intel.missioncontrol.hardware.IPayloadConfiguration;
import com.intel.missioncontrol.hardware.IPayloadMountConfiguration;
import com.intel.missioncontrol.hardware.IPlatformDescription;
import com.intel.missioncontrol.helper.Ensure;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import eu.mavinci.core.flightplan.CFMLWriter;
import eu.mavinci.core.flightplan.IFlightplanStatement;
import eu.mavinci.core.licence.ILicenceManager;
import eu.mavinci.core.xml.XMLWriter;
import eu.mavinci.desktop.gui.doublepanel.planemain.tree.maplayers.IMapLayer;
import eu.mavinci.desktop.helper.FileHelper;
import eu.mavinci.flightplan.Point;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MatchingDataWriter {

    private static final Logger LOGGER = LoggerFactory.getLogger(MatchingDataWriter.class);

    public interface Tokens extends MatchingDataReader.Tokens {

        public static final String MATCHING_HEADER =
            "<!DOCTYPE matching PUBLIC \"-//EU//MAVINCI//XML\" \"http://www.mavinci.eu/xml/matching.dtd\">\n"
                + "<matching>\n";

    }

    private File target = null;

    public void writeMatchingData(MapLayerMatching data, File file) throws IOException {
        LOGGER.info("Saving matching data");
        this.target = file;
        File tempMatchingFile = new File(file.getAbsolutePath() + ".tmp");
        writeMatchingData(data, new PrintStream(tempMatchingFile, "UTF-8"));
        FileHelper.move(tempMatchingFile, file);
        LOGGER.info("Stored matching data at file {}", file);
    }

    public String writeMatchingData(MapLayerMatching data) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        writeMatchingData(data, out);
        // System.out.println(out.toString());
        return out.toString();
    }

    public void writeMatchingData(MapLayerMatching data, OutputStream os) throws IOException {

        // XMLWriter xml = new GXMLWriter(new PrintWriter(os)); //I dont use this, because it wrongly encodes umlaute in
        // tag-bodies
        XMLWriter xml = new XMLWriter(new PrintWriter(os));
        xml.begin(Tokens.MATCHING_HEADER, 2);
        xml.comment(DependencyInjector.getInstance().getInstanceOf(ILicenceManager.class).getExportHeaderCore());

        String[] attr;
        String[] vals;

        attr = new String[] {Tokens.ENABLED};
        vals = new String[] {Boolean.toString(data.useAll)};
        xml.tag(Tokens.USE_ALL, attr, vals);

        /////////////////
        attr = new String[] {Tokens.ENABLED, Tokens.VALUE, Tokens.SPREAD};
        vals =
            new String[] {
                Boolean.toString(data.altitudeAGLEnabled),
                Double.toString(data.altitudeValue),
                Double.toString(data.altitudeSpread)
            };
        xml.tag(Tokens.ALTFILTER, attr, vals);

        attr = new String[] {Tokens.ENABLED, Tokens.VALUE, Tokens.SPREAD};
        vals =
            new String[] {
                Boolean.toString(data.rollEnabled), Double.toString(data.rollValue), Double.toString(data.rollSpread)
            };
        xml.tag(Tokens.ROLLFILTER, attr, vals);

        attr = new String[] {Tokens.ENABLED, Tokens.VALUE, Tokens.SPREAD};
        vals =
            new String[] {
                Boolean.toString(data.pitchEnabled), Double.toString(data.pitchValue), Double.toString(data.pitchSpread)
            };
        xml.tag(Tokens.PITCHFILTER, attr, vals);

        attr = new String[] {Tokens.ENABLED, Tokens.VALUE, Tokens.SPREAD};
        vals =
            new String[] {
                Boolean.toString(data.yawEnabled), Double.toString(data.yawValue), Double.toString(data.yawSpread)
            };
        xml.tag(Tokens.YAWFILTER, attr, vals);
        ////////////////

        attr = new String[] {Tokens.ONLY_IN_PIC_AREA};
        vals = new String[] {Boolean.toString(data.onlyInPicArea)};
        xml.tag(Tokens.DIRECTION_FILTER, attr, vals);

        attr = new String[] {Tokens.RESOLUTION};
        vals = new String[] {Double.toString(data.cover.resolution)};
        xml.tag(Tokens.COVERAGE, attr, vals);

        attr = new String[] {Tokens.CURRENT};
        vals = new String[] {Integer.toString(data.currentBandNo)};
        xml.start(Tokens.BANDS, attr, vals);

        attr = new String[] {Tokens.NAME};
        String[] bandNames = data.getBandNames();
        if (bandNames != null) {
            for (String name : bandNames) {
                vals = new String[] {name};
                xml.tag(Tokens.BAND, attr, vals);
            }
        }

        xml.end(); // BANDS

        attr = new String[] {Tokens.PIXEL};
        vals = new String[] {Double.toString(data.maxPixelFuzzyness)};
        xml.tag(Tokens.FUZZYNESS_FILTER, attr, vals);

        attr =
            new String[] {
                Tokens.PROJECTION_TYPE,
                Tokens.PROJECTION_DISTANCE,
                Tokens.ELEVATION_OFFSET,
                Tokens.BAND_NO,
                Tokens.CONFIRMED_AS_CORRECT,
                Tokens.LOCATION_TYPE,
                Tokens.SHOW_PREVIEW,
                Tokens.SHOW_LOCATIONS,
                Tokens.SHOW_AREA_FILTERS,
                Tokens.SHOW_RTK_LOCATIONS,
                Tokens.SHOW_TRACK,
                Tokens.SHOW_COVERAGE
            };
        vals =
            new String[] {
                data.getProjectionType().toString(),
                Double.toString(data.getProjectionDistance()),
                Double.toString(data.getElevationOffset()),
                Integer.toString(data.getCurrentBandNo()),
                Boolean.toString(data.isConfirmAsCorrect()),
                data.getLocationType().toString(),
                Boolean.toString(!data.getPicsLayer().isShowOnlyOutlines()),
                Boolean.toString(data.getPicsLayer().isShowImageLocations()),
                Boolean.toString(data.getPicAreasLayer().isVisible()),
                Boolean.toString(
                    data.getMayLayerRTKPosition() != null ? data.getMayLayerRTKPosition().isVisible() : false),
                Boolean.toString(data.getTrackLayer().isVisible()),
                Boolean.toString(data.getCoverage().isVisible())
            };

        xml.tag(Tokens.VIEW, attr, vals);

        write(data.getHardwareConfiguration(), xml);

        if (data.isRTKposAvaiable()) {
            if (data.connectorAvgPosAvaliable) {
                attr =
                    new String[] {
                        Tokens.LAT, Tokens.LON, Tokens.GPS_ALTITUDE, Tokens.GPS_ELLIPSOID, Tokens.TIMESTAMP,
                            Tokens.RTK_AVG_TIME,
                        Tokens.REAL_LAT, Tokens.REAL_LON, Tokens.REAL_ALTWGS84, Tokens.REAL_ANTENNA_ALT,
                            Tokens.AVG_ALTWGS84, Tokens.AVG_LAT,
                        Tokens.AVG_LON, Tokens.GEOID_OFFSET_EXPOT
                    };

                vals =
                    new String[] {
                        Double.toString(data.rtkLat), Double.toString(data.rtkLon), Double.toString(data.rtkAltMSL),
                        Double.toString(data.rtkGeoidSep), Double.toString(data.rtkTimestamp),
                            Double.toString(data.rtkAvgTime),
                        Double.toString(data.realLat), Double.toString(data.realLon),
                            Double.toString(data.realAltWgs84),
                        Double.toString(data.realAntennaAlt), Double.toString(data.connectorAvgAltWGS84),
                            Double.toString(data.connectorAvgLat),
                        Double.toString(data.connectorAvgLon), Double.toString(data.geoidOffset)
                    };
            } else {
                attr =
                    new String[] {
                        Tokens.LAT,
                        Tokens.LON,
                        Tokens.GPS_ALTITUDE,
                        Tokens.GPS_ELLIPSOID,
                        Tokens.TIMESTAMP,
                        Tokens.RTK_AVG_TIME,
                        Tokens.REAL_LAT,
                        Tokens.REAL_LON,
                        Tokens.REAL_ALTWGS84,
                        Tokens.REAL_ANTENNA_ALT,
                        Tokens.GEOID_OFFSET_EXPOT
                    };

                vals =
                    new String[] {
                        Double.toString(data.rtkLat), Double.toString(data.rtkLon), Double.toString(data.rtkAltMSL),
                        Double.toString(data.rtkGeoidSep), Double.toString(data.rtkTimestamp),
                            Double.toString(data.rtkAvgTime),
                        Double.toString(data.realLat), Double.toString(data.realLon),
                            Double.toString(data.realAltWgs84),
                        Double.toString(data.realAntennaAlt), Double.toString(data.geoidOffset)
                    };
            }

            xml.tag(Tokens.CONN_POS, attr, vals);
            // System.out.println("SUCCEEDED!");
        }

        File basePath = null;
        if (target != null) {
            basePath = target.getParentFile();
            // System.out.println("basePath="+basePath);
        }

        for (IMapLayer layer : data.getPicAreasLayer().getLayers()) {
            if (layer instanceof MapLayerPicArea) {
                final MapLayerPicArea picArea = (MapLayerPicArea)layer;
                attr = new String[] {Tokens.GSD, Tokens.NAME, Tokens.ENABLED};
                vals =
                    new String[] {
                        Double.toString(picArea.getGSD()), picArea.getName(), Boolean.toString(picArea.isVisible())
                    };
                xml.start(Tokens.PICAREA, attr, vals);
                for (IFlightplanStatement fpObj : picArea) {
                    Point point = (Point)fpObj;
                    attr = new String[] {Tokens.LAT, Tokens.LON};
                    vals = new String[] {Double.toString(point.getLat()), Double.toString(point.getLon())};
                    xml.tag(Tokens.POINT, attr, vals);
                }

                xml.end();
            }
        }

        Ensure.notNull(basePath, "basePath");
        for (IMapLayer layer : data.getPictures()) {
            if (layer instanceof MapLayerMatch) {
                final MapLayerMatch match = (MapLayerMatch)layer;

                if (data.bandNames != null && data.bandNames.length > 1) {
                    attr =
                        new String[] {
                            Tokens.NUMBER,
                            Tokens.LINE_NUMBER,
                            Tokens.LAT,
                            Tokens.LON,
                            Tokens.ALT,
                            Tokens.GROUNDSPEED,
                            Tokens.ROLLRATE,
                            Tokens.PITCHRATE,
                            Tokens.YAWRATE,
                            Tokens.CAMERA_ROLL,
                            Tokens.CAMERA_PITCH,
                            Tokens.CAMERA_YAW,
                            Tokens.PLANE_ROLL,
                            Tokens.PLANE_PITCH,
                            Tokens.PLANE_YAW,
                            Tokens.TIMESTAMP,
                            Tokens.GPS_ELLIPSOID,
                            Tokens.GPS_ALTITUDE,
                            Tokens.HEADING,
                            Tokens.TIME_SINCE_LAST_FIX,
                            Tokens.DELAY_TYPE,
                            Tokens.FIX_TYPE,
                            Tokens.LAT_TAKEOFF,
                            Tokens.LON_TAKEOFF,
                            Tokens.HEIGHT_TAKEOFF
                        };

                    vals =
                        new String[] {
                            Integer.toString(match.line.imageNumber),
                            Integer.toString(match.line.lineNumber),
                            Double.toString(match.line.lat),
                            Double.toString(match.line.lon),
                            Integer.toString(match.line.alt),
                            Integer.toString(match.line.groundSpeed_cms),
                            Double.toString(match.line.cameraRollRate),
                            Double.toString(match.line.cameraPitchRate),
                            Double.toString(match.line.cameraYawRate),
                            Double.toString(match.line.cameraRoll),
                            Double.toString(match.line.cameraPitch),
                            Double.toString(match.line.cameraYaw),
                            Double.toString(match.line.planeRoll),
                            Double.toString(match.line.planePitch),
                            Double.toString(match.line.planeYaw),
                            Double.toString(match.line.getTimestampRaw()),
                            Double.toString(match.line.gps_ellipsoid_cm),
                            Double.toString(match.line.gps_altitude_cm),
                            Double.toString(match.line.heading),
                            Integer.toString(match.line.time_since_last_fix),
                            match.line.type.name(),
                            (match.line.fixType != null ? match.line.fixType.ordinal() + "" : ""),
                            Double.toString(match.line.latTakeoff),
                            Double.toString(match.line.lonTakeoff),
                            Double.toString(match.line.gps_altitude_takeoff_cm),
                        };

                    // String path = match.picFile.getAbsolutePath();
                    // // System.out.println("path="+path);
                    // if (basePath != null && path.startsWith(basePath)) {
                    // path = path.replaceFirst(Pattern.quote(basePath), ".");
                    // // System.out.println("replacedPath="+path);
                    // }
                    xml.start(Tokens.MMATCH, attr, vals);
                    for (PhotoFile photo : match.photoCube) {
                        String path = FileHelper.makeRelativePathSysIndep(basePath, photo.getFile());
                        attr =
                            new String[] {
                                Tokens.TIMESTAMP_EXIF,
                                Tokens.FOCALLENGTH_EXIF,
                                Tokens.EXPOSURETIME_EXIF,
                                Tokens.MODEL_EXIF,
                                Tokens.APERTURE_EXIF,
                                Tokens.ISO_EXIF
                            };

                        vals =
                            new String[] {
                                Double.toString(photo.exif.timestamp), Double.toString(photo.exif.focalLengthMM),
                                Double.toString(photo.exif.exposureSec),
                                    (photo.exif.model != null ? photo.exif.model : ""),
                                Double.toString(photo.exif.aperture), Double.toString(photo.exif.iso)
                            };
                        xml.contentTag(Tokens.BAND, attr, vals, path);
                    }

                    xml.end(); // MMATCH
                } else {
                    PhotoFile photo = match.photoCube.photoFiles[0];
                    attr =
                        new String[] {
                            Tokens.NUMBER,
                            Tokens.LINE_NUMBER,
                            Tokens.LAT,
                            Tokens.LON,
                            Tokens.ALT,
                            Tokens.GROUNDSPEED,
                            Tokens.ROLLRATE,
                            Tokens.PITCHRATE,
                            Tokens.YAWRATE,
                            Tokens.CAMERA_ROLL,
                            Tokens.CAMERA_PITCH,
                            Tokens.CAMERA_YAW,
                            Tokens.PLANE_ROLL,
                            Tokens.PLANE_PITCH,
                            Tokens.PLANE_YAW,
                            Tokens.TIMESTAMP,
                            Tokens.GPS_ELLIPSOID,
                            Tokens.GPS_ALTITUDE,
                            Tokens.HEADING,
                            Tokens.TIME_SINCE_LAST_FIX,
                            Tokens.FIX_TYPE,
                            Tokens.DELAY_TYPE,
                            Tokens.TIMESTAMP_EXIF,
                            Tokens.FOCALLENGTH_EXIF,
                            Tokens.EXPOSURETIME_EXIF,
                            Tokens.MODEL_EXIF,
                            Tokens.APERTURE_EXIF,
                            Tokens.ISO_EXIF,
                            Tokens.LAT_TAKEOFF,
                            Tokens.LON_TAKEOFF,
                            Tokens.HEIGHT_TAKEOFF,
                        };

                    vals =
                        new String[] {
                            Integer.toString(match.line.imageNumber),
                            Integer.toString(match.line.lineNumber),
                            Double.toString(match.line.lat),
                            Double.toString(match.line.lon),
                            Integer.toString(match.line.alt),
                            Integer.toString(match.line.groundSpeed_cms),
                            Double.toString(match.line.cameraRollRate),
                            Double.toString(match.line.cameraPitchRate),
                            Double.toString(match.line.cameraYawRate),
                            Double.toString(match.line.cameraRoll),
                            Double.toString(match.line.cameraPitch),
                            Double.toString(match.line.cameraYaw),
                            Double.toString(match.line.planeRoll),
                            Double.toString(match.line.planePitch),
                            Double.toString(match.line.planeYaw),
                            Double.toString(match.line.getTimestampRaw()),
                            Double.toString(match.line.gps_ellipsoid_cm),
                            Double.toString(match.line.gps_altitude_cm),
                            Double.toString(match.line.heading),
                            Integer.toString(match.line.time_since_last_fix),
                            (match.line.fixType != null ? match.line.fixType.ordinal() + "" : ""),
                            match.line.type.name(),
                            Double.toString(photo.exif.timestamp),
                            Double.toString(photo.exif.focalLengthMM),
                            Double.toString(photo.exif.exposureSec),
                            (photo.exif.model != null ? photo.exif.model : ""),
                            Double.toString(photo.exif.aperture),
                            Double.toString(photo.exif.iso),
                            Double.toString(match.line.latTakeoff),
                            Double.toString(match.line.lonTakeoff),
                            Double.toString(match.line.gps_altitude_takeoff_cm),
                        };

                    String path = FileHelper.makeRelativePathSysIndep(basePath, photo.file);
                    // String path = match.picFile.getAbsolutePath();
                    // // System.out.println("path="+path);
                    // if (basePath != null && path.startsWith(basePath)) {
                    // path = path.replaceFirst(Pattern.quote(basePath), ".");
                    // // System.out.println("replacedPath="+path);
                    // }
                    xml.contentTag(Tokens.MATCH, attr, vals, path);
                }
            }
        }

        // finish writing file
        xml.finish("</" + Tokens.MATCHING + ">\n");
    }

    private void write(IHardwareConfiguration hardwareConfiguration, XMLWriter xml) {
        IPlatformDescription platformDescription = hardwareConfiguration.getPlatformDescription();
        String[] attr = new String[] {CFMLWriter.Tokens.PLATFORM_ID};
        String[] vals = new String[] {platformDescription.getId()};
        xml.start(CFMLWriter.Tokens.PLATFORM_DESCRIPTION, attr, vals);
        for (int slotIdx = 0; slotIdx < hardwareConfiguration.getPayloadMounts().length; slotIdx++) {
            IPayloadMountConfiguration payloadMountConfiguration = hardwareConfiguration.getPayloadMounts()[slotIdx];
            for (int payloadIdx = 0; payloadIdx < payloadMountConfiguration.getPayloads().size(); payloadIdx++) {
                IPayloadConfiguration payload = payloadMountConfiguration.getPayloads().get(payloadIdx);
                if (payload instanceof IGenericCameraConfiguration) {
                    write((IGenericCameraConfiguration)payload, slotIdx, payloadIdx, xml);
                } else {
                    // implement in the future write method for other cameras with different lenses
                }
            }
        }

        xml.end();
    }

    private void write(IGenericCameraConfiguration cameraDescription, int slotIdx, int payloadIdx, XMLWriter xml) {
        String[] attr =
            new String[] {
                CFMLWriter.Tokens.CAMERA_ID,
                CFMLWriter.Tokens.SLOT_IDX,
                CFMLWriter.Tokens.PAYLOAD_IDX,
                CFMLWriter.Tokens.LENS_ID
            };
        String[] vals =
            new String[] {
                cameraDescription.getDescription().getId(),
                String.valueOf(slotIdx),
                String.valueOf(payloadIdx),
                cameraDescription.getLens().getDescription().getId()
            };
        xml.tag(CFMLWriter.Tokens.CAMERA_DESCRIPTION, attr, vals);
    }
}
