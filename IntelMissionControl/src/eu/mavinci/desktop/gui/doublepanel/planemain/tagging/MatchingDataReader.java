/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.planemain.tagging;

import com.intel.missioncontrol.api.ExportService;
import com.intel.missioncontrol.hardware.IHardwareConfigurationManager;
import com.intel.missioncontrol.utils.IVersionProvider;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import eu.mavinci.core.xml.MEntryResolver;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

public class MatchingDataReader {
    private static final Logger LOG = LoggerFactory.getLogger(ExportService.class);

    public AMapLayerMatching readMatchingData  (
            MapLayerMatching data, File filename, IHardwareConfigurationManager hardwareConfigurationManager)
            throws SAXException, IOException {
        InputStream is = null;
        MEntryResolver res = MEntryResolver.resolver;
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            // factory.setValidating(false);
            factory.setValidating(true);

            if (!filename.getParentFile().exists()) {
                if (!filename.getParentFile().mkdir()) {
                    LOG.warn("can not write Matching Data");
                    throw new IOException("can not write Matching Data");
                }
            }
            if (!filename.exists() && !filename.createNewFile()) {
                LOG.warn("can not write Matching Data");
                throw new IOException("can not write Matching Data");
            }
            is = new FileInputStream(filename);
            SAXParser saxParser;

            try {
                saxParser = factory.newSAXParser();

                MatchingDataHandler handler =
                    new MatchingDataHandler(data, filename.getParentFile(), hardwareConfigurationManager);

                XMLReader xr = saxParser.getXMLReader();
                xr.setContentHandler(handler);
                xr.setEntityResolver(res);
                xr.setErrorHandler(handler);
                if (!DependencyInjector.getInstance()
                        .getInstanceOf(IVersionProvider.class)
                        .getSystem()
                        .isAndroid()) { // avoid warning in
                    // android
                    xr.setDTDHandler(handler); // not supported in android
                }

                xr.parse(new InputSource(is));
                // saxParser.parse(is, handler);

                return handler.data;
            } catch (ParserConfigurationException e) {
                LOG.warn("cant read Matching Data", e);
                return null;
            } catch (SAXParseException e) {
                LOG.warn("error while reading invalid Matching Data", e);
                // that wasn't a valid flightplan!

                // try again without validating DTD

                return null;
            }
        } finally {
            if (is != null) {
                is.close();
            }

            if (res != null) {
                res.closeResource();
            }
        }
    }

    public interface Tokens {
        public static final String ID = "id";
        public static final String MATCHING = "matching";
        public static final String ALT_FILTER = "altFilter";
        public static final String ANGLE_FILTER = "angleFilter";
        public static final String ANGLE_SPEED_FILTER = "angleSpeedFilter";
        public static final String MIN = "min";
        public static final String MAX = "max";
        public static final String CAMERA_PITCH = "pitch";
        public static final String CAMERA_ROLL = "roll";
        public static final String CAMERA_YAW = "yaw";
        public static final String PLANE_YAW = "plane_yaw";
        public static final String PLANE_ROLL = "plane_roll";
        public static final String PLANE_PITCH = "plane_pitch";

        public static final String FUZZYNESS_FILTER = "fuzzynessFilter";
        public static final String PIXEL = "pixel";

        public static final String USE_ALL = "useAll";
        public static final String DIRECTION_FILTER = "directionFilter";
        public static final String ONLY_MAIN_LINES = "onlyMainLines";
        public static final String ONLY_IN_PIC_AREA = "onlyInPicArea";
        public static final String ONLY_ONE_DIRECTION = "onlyOneDirection";
        public static final String DONT_PERFORATE_LINES = "dontPerforateLines";

        public static final String MATCH = "match";
        public static final String MMATCH = "mmatch";
        public static final String FILE = "file";

        public static final String COVERAGE = "coverage";
        public static final String RESOLUTION = "resolution";

        public static final String PICAREA = "picarea";
        public static final String GSD = "gsd";
        public static final String POINT = "point";

        public static final String NUMBER = "number";
        public static final String LINE_NUMBER = "lineNumber";
        public static final String LON = "lon";
        public static final String LAT = "lat";
        public static final String ALT = "alt";
        public static final String GROUNDSPEED = "groundspeed";
        public static final String ROLLRATE = "rollrate";
        public static final String PITCHRATE = "pitchrate";
        public static final String YAWRATE = "yawrate";
        public static final String ENABLED = "enabled";

        public static final String ALTFILTER = "altfilter";
        public static final String ROLLFILTER = "rollfilter";
        public static final String PITCHFILTER = "pitchfilter";
        public static final String YAWFILTER = "yawfilter";
        public static final String VALUE = "value";
        public static final String SPREAD = "spread";

        public static final String BANDS = "Bands";
        public static final String BAND = "Band";
        public static final String NAME = "Name";
        public static final String CURRENT = "current";

        public static final String TIMESTAMP = "timestamp";
        public static final String TIMESTAMP_EXIF = "timestamp_exif";
        public static final String DELAY_TYPE = "delay_type";

        public static final String GPS_ELLIPSOID = "gps_ellipsoid";
        public static final String GPS_ALTITUDE = "gps_altitude";
        public static final String HEADING = "heading";

        public static final String FOCALLENGTH_EXIF = "focallength_exif";
        public static final String EXPOSURETIME_EXIF = "exposuretime_exif";
        public static final String APERTURE_EXIF = "aperture_exif";
        public static final String ISO_EXIF = "iso_exif";
        public static final String MODEL_EXIF = "model_exif";

        public static final String TIME_SINCE_LAST_FIX = "time_since_last_fix"; // in ms

        public static final String CONN_POS = "connectorposition";

        public static final String RTK_AVG_TIME = "rtk_avg_time";
        public static final String REAL_LON = "real_lon";
        public static final String REAL_LAT = "real_lat";
        public static final String REAL_ALTWGS84 = "real_altwgs84";
        public static final String REAL_ANTENNA_ALT = "real_antenna_alt";
        public static final String GEOID_OFFSET_EXPOT = "geoid_offset_export";

        public static final String AVG_LON = "avg_lon";
        public static final String AVG_LAT = "avg_lat";
        public static final String AVG_ALTWGS84 = "avg_altwgs84";

        public static final String FIX_TYPE = "fix_type";

        public static final String LAT_TAKEOFF = "lat_takeoff";
        public static final String LON_TAKEOFF = "lon_takeoff";
        public static final String HEIGHT_TAKEOFF = "height_takeoff";

        public static final String CAMERA_DESCRIPTION = "camera_description";
        public static final String PLATFORM_DESCRIPTION = "platform_description";
        public static final String LENS_DESCRIPTION = "lens_description";
        public static final String PICAREA_TEMPLATES = "picarea_templates";
        public static final String BASED_ON_TEMPLATE = "based_on_template";
        public static final String SLOT = "slot";

        public static final String VIEW = "view";

        public static final String PROJECTION_TYPE = "projectionType";
        public static final String PROJECTION_DISTANCE = "projectionDistance";
        public static final String ELEVATION_OFFSET = "elevationOffset";

        public static final String BAND_NO = "bandNo";
        public static final String CONFIRMED_AS_CORRECT = "confirmedAsCorrect";
        public static final String LOCATION_TYPE = "locationType";

        public static final String SHOW_PREVIEW = "showPreview";
        public static final String SHOW_LOCATIONS = "showLocations";
        public static final String SHOW_AREA_FILTERS = "showAreaFilters";
        public static final String SHOW_RTK_LOCATIONS = "showRtkLocation";
        public static final String SHOW_TRACK = "showTrack";
        public static final String SHOW_COVERAGE = "showCoverage";
    }
}
