/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.planemain.tagging;

import com.intel.missioncontrol.hardware.IGenericCameraConfiguration;
import com.intel.missioncontrol.hardware.IHardwareConfigurationManager;
import eu.mavinci.core.flightplan.CFMLWriter;
import eu.mavinci.core.flightplan.GPSFixType;
import eu.mavinci.core.flightplan.PhotoLogLineType;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.MatchingDataReader.Tokens;
import eu.mavinci.desktop.helper.FileHelper;
import eu.mavinci.desktop.main.debug.Debug;
import eu.mavinci.flightplan.PhotoLogLine;
import eu.mavinci.flightplan.Point;
import java.io.File;
import java.util.Date;
import java.util.Vector;
import java.util.logging.Level;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class MatchingDataHandler extends DefaultHandler implements Tokens {
    private final IHardwareConfigurationManager hardwareConfigurationManager;

    protected MapLayerMatching data;

    protected StringBuffer sbuf = new StringBuffer();

    File basePath;

    MatchingDataHandler(
            MapLayerMatching data, File basePath, IHardwareConfigurationManager hardwareConfigurationManager) {
        super();
        this.hardwareConfigurationManager = hardwareConfigurationManager;
        this.basePath = basePath;
        this.data = data;
    }

    public void startDocument() {
        while (data.getPicsLayer().sizeMapLayer() > 0) {
            data.getPicsLayer().removeMapLayer(0);
        }

        while (data.getPicAreasLayer().sizeMapLayer() > 0) {
            data.getPicAreasLayer().removeMapLayer(0);
        }
    }

    public void endDocument() throws SAXException {}

    MapLayerPicArea curPicArea;
    boolean inBandDefinition;
    MapLayerMatch curMatch;
    int curBandNo;
    ExifInfos lastExif;
    Vector<String> bandNames = new Vector<String>();

    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
        qName = localName;

        // first clear the character buffer
        sbuf.delete(0, sbuf.length());

        if (qName.equals(ALTFILTER)) {
            data.altitudeAGLEnabled = Boolean.parseBoolean(atts.getValue(ENABLED));
            data.altitudeValue = Double.parseDouble(atts.getValue(VALUE));
            data.altitudeSpread = Double.parseDouble(atts.getValue(SPREAD));
        } else if (qName.equals(ROLLFILTER)) {
            data.rollEnabled = Boolean.parseBoolean(atts.getValue(ENABLED));
            data.rollValue = Double.parseDouble(atts.getValue(VALUE));
            data.rollSpread = Double.parseDouble(atts.getValue(SPREAD));
        } else if (qName.equals(PITCHFILTER)) {
            data.pitchEnabled = Boolean.parseBoolean(atts.getValue(ENABLED));
            data.pitchValue = Double.parseDouble(atts.getValue(VALUE));
            data.pitchSpread = Double.parseDouble(atts.getValue(SPREAD));
        } else if (qName.equals(YAWFILTER)) {
            data.yawEnabled = Boolean.parseBoolean(atts.getValue(ENABLED));
            data.yawValue = Double.parseDouble(atts.getValue(VALUE));
            data.yawSpread = Double.parseDouble(atts.getValue(SPREAD));
        } else if (qName.equals(FUZZYNESS_FILTER)) {
            data.maxPixelFuzzyness = Double.parseDouble(atts.getValue(PIXEL));
        } else if (qName.equals(VIEW)) {
            try {
                data.setProjectionType(ProjectionType.valueOf(atts.getValue(PROJECTION_TYPE)));
                data.setProjectionDistance(Double.parseDouble(atts.getValue(PROJECTION_DISTANCE)));
                String elevationOffset = atts.getValue(ELEVATION_OFFSET);
                data.setElevationOffset(elevationOffset == null ? 0 : Double.parseDouble(elevationOffset));
                data.setCurrentBandNo(Integer.parseInt(atts.getValue(BAND_NO)));
                data.setConfirmAsCorrect(Boolean.valueOf(atts.getValue(CONFIRMED_AS_CORRECT)));
                data.setLocationType(LocationType.valueOf(atts.getValue(LOCATION_TYPE)));

                data.getPicsLayer()
                    .setShowOnlyOutlines(!Boolean.parseBoolean(atts.getValue(MatchingDataWriter.Tokens.SHOW_PREVIEW)));
                data.getPicsLayer()
                    .setShowImageLocations(
                        Boolean.parseBoolean(atts.getValue(MatchingDataWriter.Tokens.SHOW_LOCATIONS)));
                data.getPicAreasLayer()
                    .setVisible(Boolean.parseBoolean(atts.getValue(MatchingDataWriter.Tokens.SHOW_AREA_FILTERS)));
                if (data.getMayLayerRTKPosition() != null) {
                    data.getMayLayerRTKPosition()
                        .setVisible(Boolean.parseBoolean(atts.getValue(MatchingDataWriter.Tokens.SHOW_RTK_LOCATIONS)));
                }

                data.getTrackLayer()
                    .setVisible(Boolean.parseBoolean(atts.getValue(MatchingDataWriter.Tokens.SHOW_TRACK)));
                data.getCoverage()
                    .setVisible(Boolean.parseBoolean(atts.getValue(MatchingDataWriter.Tokens.SHOW_COVERAGE)));
            } catch (Exception e) {
                Debug.getLog()
                    .log(
                        Level.WARNING,
                        "Some projection/location/view data was not parsed for the matching "
                            + data.getMatchingFolder(),
                        e);
            }
        } else if (qName.equals(USE_ALL)) {
            data.useAll = Boolean.parseBoolean(atts.getValue(ENABLED));
        } else if (qName.equals(DIRECTION_FILTER)) {
            try {
                data.onlyInPicArea = Boolean.parseBoolean(atts.getValue(ONLY_IN_PIC_AREA));
            } catch (Exception e) {
            }
        } else if (qName.equals(COVERAGE)) {
            data.cover.resolution = Double.parseDouble(atts.getValue(RESOLUTION));
        } else if (qName.equals(MATCH) || qName.equals(MMATCH)) {
            PhotoLogLine line = new PhotoLogLine(data.getHardwareConfiguration());
            line.imageNumber = Integer.parseInt(atts.getValue(NUMBER));
            line.lineNumber = Integer.parseInt(atts.getValue(LINE_NUMBER));
            line.lat = Double.parseDouble(atts.getValue(LAT));
            line.lon = Double.parseDouble(atts.getValue(LON));
            line.alt = (int)Double.parseDouble(atts.getValue(ALT));
            line.groundSpeed_cms = (int)Double.parseDouble(atts.getValue(GROUNDSPEED));
            line.cameraRollRate = Double.parseDouble(atts.getValue(ROLLRATE));
            line.cameraPitchRate = Double.parseDouble(atts.getValue(PITCHRATE));
            line.cameraYawRate = Double.parseDouble(atts.getValue(YAWRATE));
            line.cameraRoll = Double.parseDouble(atts.getValue(CAMERA_ROLL));
            line.cameraPitch = Double.parseDouble(atts.getValue(CAMERA_PITCH));
            line.cameraYaw = Double.parseDouble(atts.getValue(CAMERA_YAW));
            try {
                line.planeRoll = Double.parseDouble(atts.getValue(PLANE_ROLL));
            } catch (Exception e) {
            }

            try {
                line.planePitch = Double.parseDouble(atts.getValue(PLANE_PITCH));
            } catch (Exception e) {
            }

            try {
                line.planeYaw = Double.parseDouble(atts.getValue(PLANE_YAW));
            } catch (Exception e) {
                line.planeYaw = line.cameraYaw;
            }

            line.setTimestampRaw(Double.parseDouble(atts.getValue(TIMESTAMP)));
            line.gps_altitude_cm = Double.parseDouble(atts.getValue(GPS_ALTITUDE));
            line.gps_ellipsoid_cm = Double.parseDouble(atts.getValue(GPS_ELLIPSOID));
            line.heading = Double.parseDouble(atts.getValue(HEADING));
            line.fixType = GPSFixType.parseMeta(atts.getValue(FIX_TYPE), line.fixType);
            try {
                line.lonTakeoff = Double.parseDouble(atts.getValue(LON_TAKEOFF));
            } catch (Exception e) {
            }

            try {
                line.latTakeoff = Double.parseDouble(atts.getValue(LAT_TAKEOFF));
            } catch (Exception e) {
            }

            try {
                line.gps_altitude_takeoff_cm = Double.parseDouble(atts.getValue(HEIGHT_TAKEOFF));
            } catch (Exception e) {
            }

            try {
                line.time_since_last_fix = Integer.parseInt(atts.getValue(TIME_SINCE_LAST_FIX));
            } catch (Exception e) {
            }

            try {
                line.type = PhotoLogLineType.valueOf(atts.getValue(DELAY_TYPE));
            } catch (Exception e) {
            }

            lastExif = new ExifInfos();
            try {
                lastExif.timestamp = Double.parseDouble(atts.getValue(TIMESTAMP_EXIF));
                lastExif.datetime = new Date(Math.round(lastExif.timestamp * 1000));
            } catch (Exception e) {
            }

            try {
                lastExif.focalLengthMM = Double.parseDouble(atts.getValue(FOCALLENGTH_EXIF));
            } catch (Exception e) {
            }

            try {
                lastExif.exposureSec = Double.parseDouble(atts.getValue(EXPOSURETIME_EXIF));
            } catch (Exception e) {
            }

            try {
                lastExif.model = atts.getValue(MODEL_EXIF);
            } catch (Exception e) {
            }

            try {
                lastExif.aperture = Double.parseDouble(atts.getValue(APERTURE_EXIF));
            } catch (Exception e) {
                lastExif.aperture = -2; // mark as never read out in a previous version of Intel Mission Control
            }

            try {
                lastExif.iso = Double.parseDouble(atts.getValue(ISO_EXIF));
            } catch (Exception e) {
            }

            curBandNo = 0;
            try {
                curMatch = new MapLayerMatch(new PhotoCube(data.bandNames.length), line, data);
            } catch (Throwable e) {
                Debug.getLog().log(Level.WARNING, "could not load match:" + line, e);
            }
        } else if (qName.equals(PICAREA)) {
            curPicArea = new MapLayerPicArea(data);
            try {
                curPicArea.setGSD(Double.parseDouble(atts.getValue(GSD)));
            } catch (Exception e) {
            }

            try {
                curPicArea.setName(atts.getValue(NAME));
            } catch (Exception e) {
            }

            try {
                curPicArea.setVisible(Boolean.parseBoolean(atts.getValue(ENABLED)));
            } catch (Exception e) {
            }

            data.getPicAreasLayer().addMapLayer(curPicArea);
        } else if (qName.equals(POINT)) {
            double lat = Double.parseDouble(atts.getValue(LAT));
            double lon = Double.parseDouble(atts.getValue(LON));
            Point point = new Point(curPicArea, lat, lon);
            try {
                curPicArea.addToFlightplanContainer(point);
            } catch (Exception e) {
                throw new SAXException("Problem adding new PicArea Corner", e);
            }
        } else if (qName.equals(CONN_POS)) {
            data.rtkLat = Double.parseDouble(atts.getValue(LAT));
            data.rtkLon = Double.parseDouble(atts.getValue(LON));
            data.rtkAltMSL = Double.parseDouble(atts.getValue(GPS_ALTITUDE));
            data.rtkGeoidSep = Double.parseDouble(atts.getValue(GPS_ELLIPSOID));
            data.rtkTimestamp = Double.parseDouble(atts.getValue(TIMESTAMP));
            data.rtkAvgTime = Double.parseDouble(atts.getValue(RTK_AVG_TIME));

            data.realLat = Double.parseDouble(atts.getValue(REAL_LAT));
            data.realLon = Double.parseDouble(atts.getValue(REAL_LON));
            data.realAltWgs84 = Double.parseDouble(atts.getValue(REAL_ALTWGS84));
            try {
                data.realAntennaAlt = Double.parseDouble(atts.getValue(REAL_ANTENNA_ALT));
            } catch (Exception e) {
            }

            try {
                data.connectorAvgAltWGS84 = Double.parseDouble(atts.getValue(AVG_ALTWGS84));
                data.connectorAvgLat = Double.parseDouble(atts.getValue(AVG_LAT));
                data.connectorAvgLon = Double.parseDouble(atts.getValue(AVG_LON));
                data.connectorAvgPosAvaliable = true;
            } catch (Exception e) {
            }

            try {
                data.geoidOffset = Double.parseDouble(atts.getValue(GEOID_OFFSET_EXPOT));
            } catch (Exception e) {
            }

            data.setRTKAvaiable();
        } else if (qName.equals(Tokens.BANDS)) {
            bandNames.clear();
            try {
                data.currentBandNo = Integer.parseInt(atts.getValue(CURRENT));
            } catch (Exception e) {
            }

            inBandDefinition = true;
        } else if (qName.equals(Tokens.BAND)) {
            if (inBandDefinition) {
                bandNames.addElement(atts.getValue(NAME));
            } else {
                lastExif = new ExifInfos();
                try {
                    lastExif.timestamp = Double.parseDouble(atts.getValue(TIMESTAMP_EXIF));
                    lastExif.datetime = new Date(Math.round(lastExif.timestamp * 1000));
                } catch (Exception e) {
                }

                try {
                    lastExif.focalLengthMM = Double.parseDouble(atts.getValue(FOCALLENGTH_EXIF));
                } catch (Exception e) {
                }

                try {
                    lastExif.exposureSec = Double.parseDouble(atts.getValue(EXPOSURETIME_EXIF));
                } catch (Exception e) {
                }

                try {
                    lastExif.model = atts.getValue(MODEL_EXIF);
                } catch (Exception e) {
                }

                try {
                    lastExif.aperture = Double.parseDouble(atts.getValue(APERTURE_EXIF));
                } catch (Exception e) {
                }

                try {
                    lastExif.iso = Double.parseDouble(atts.getValue(ISO_EXIF));
                } catch (Exception e) {
                }
            }
        } else if (qName.equals(CFMLWriter.Tokens.CAMERA_DESCRIPTION)) {
            String cameraId = atts.getValue(CFMLWriter.Tokens.CAMERA_ID);
            String slotIdx = atts.getValue(CFMLWriter.Tokens.SLOT_IDX);
            String payloadIdx = atts.getValue(CFMLWriter.Tokens.PAYLOAD_IDX);
            String lensId = atts.getValue(CFMLWriter.Tokens.LENS_ID);
            try {
                IGenericCameraConfiguration camera =
                    hardwareConfigurationManager.getCameraConfiguration(cameraId, lensId);
                int slotI = Integer.parseInt(slotIdx);
                int payloadI = Integer.parseInt(payloadIdx);
                data.getHardwareConfiguration().setPrimaryPayload(camera);
                data.getHardwareConfiguration().getPayloadMount(slotI).setPayload(payloadI, camera);
            } catch (Exception e) {
                Debug.getLog()
                    .log(
                        Level.WARNING,
                        "could not parse payload description while reading the matching. cameraId:"
                            + cameraId
                            + " slotIdx:"
                            + slotIdx
                            + " payloadIdx:"
                            + payloadIdx
                            + " lensId:"
                            + lensId,
                        e);
            }
        } else if (qName.equals(CFMLWriter.Tokens.PLATFORM_DESCRIPTION)) {
            try {
                String id = atts.getValue(CFMLWriter.Tokens.PLATFORM_ID);
                data.updateHardwareConfiguration(hardwareConfigurationManager.getHardwareConfiguration(id));
            } catch (Exception e) {
                Debug.getLog().log(Level.WARNING, "could not parse platform description while reading the matching", e);
            }
        }
    }

    public void endElement(String namespaceURI, String localName, String qName) {
        qName = localName;

        if (qName.equals(MATCH)) {
            if (data.bandNames.length == 1) {
                String path = sbuf.toString();
                File f = null;
                try {
                    f = FileHelper.unmakeRelativePath(basePath, path);
                    curMatch.photoCube.photoFiles[curBandNo] = new PhotoFile(curMatch, f, lastExif);
                } catch (Throwable e) {
                    Debug.getLog()
                        .log(Level.WARNING, "could not set image for matching:" + f + " at no:" + curBandNo, e);
                }

                curBandNo++;
            }

            try {
                data.getPicsLayer().addMapLayer(curMatch);
            } catch (Throwable e) {
                Debug.getLog().log(Level.WARNING, "could not add match:" + curMatch, e);
            }

            curMatch = null;
        } else if (qName.equals(BAND) && !inBandDefinition) {
            String path = sbuf.toString();
            File f = null;
            try {
                f = FileHelper.unmakeRelativePath(basePath, path);
                curMatch.photoCube.photoFiles[curBandNo] = new PhotoFile(curMatch, f, lastExif);
            } catch (Throwable e) {
                Debug.getLog().log(Level.WARNING, "could not set image for matching:" + f + " at no:" + curBandNo, e);
            }

            curBandNo++;
        } else if (qName.equals(MMATCH)) {
            try {
                data.getPicsLayer().addMapLayer(curMatch);
            } catch (Throwable e) {
                Debug.getLog().log(Level.WARNING, "could not add match:" + curMatch, e);
            }

            curMatch = null;
        } else if (qName.equals(PICAREA)) {
            curPicArea = null;
        } else if (qName.equals(Tokens.BANDS)) {
            data.bandNames = bandNames.toArray(new String[bandNames.size()]);
            inBandDefinition = false;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        sbuf.append(ch, start, length);
    }
}
