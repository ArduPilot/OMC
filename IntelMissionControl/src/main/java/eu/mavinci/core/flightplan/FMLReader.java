/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.flightplan;

import com.intel.missioncontrol.hardware.IHardwareConfigurationManager;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import com.intel.missioncontrol.utils.IVersionProvider;
import eu.mavinci.core.main.OsTypes;
import eu.mavinci.core.xml.MEntryResolver;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

// SAXParser p = SAXParserFactory.newInstance().newSAXParser();
// DefaultHandler handler = new XMLHanler();
// p.parse(new ByteArrayInputStream(xml.getBytes()), handler);
//
// where XMLHandler is simple implementation that just log which methods are invoked.
// For this xml it prints only from startDocument method.
//
//
// But today I've changed to this code:
//
// SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
// XMLReader xr = parser.getXMLReader();
// xr.setContentHandler(handler);
// xr.parse(new InputSource(new StringReader(xml)));
//
// and it works. To be honest I don't know why.

public class FMLReader {

    public CFlightplan readFML(
            CFlightplan plan, InputStream is, IHardwareConfigurationManager hardwareConfigurationManager)
            throws SAXException, IOException, ParserConfigurationException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        // if (GlobalSettings.system != OpSys.Android) { // avoid a lot of
        // validation in android
        // factory.setNamespaceAware(false);
        // }
        factory.setNamespaceAware(true);
        // factory.setValidating(GlobalSettings.system.isDesktop());
        factory.setValidating(false);

        SAXParser saxParser;
        MEntryResolver res = MEntryResolver.resolver;

        try {
            saxParser = factory.newSAXParser();

            FMLHandler handler = new FMLHandler(plan, hardwareConfigurationManager);

            XMLReader xr = saxParser.getXMLReader();
            xr.setContentHandler(handler);
            xr.setEntityResolver(res);
            xr.setErrorHandler(handler);
            OsTypes system = DependencyInjector.getInstance().getInstanceOf(IVersionProvider.class).getSystem();
            if (!system.isAndroid()) { // avoid warning in
                // android
                xr.setDTDHandler(handler); // not supported in android
            }

            xr.parse(new InputSource(is));
            // saxParser.parse(is, handler);

            return handler.getFlightplan();

        } finally {
            if (res != null) {
                res.closeResource();
            }
        }
    }

    public CFlightplan readFML(
            CFlightplan plan, File filename, IHardwareConfigurationManager hardwareConfigurationManager)
            throws SAXException, IOException, ParserConfigurationException {
        try (InputStream is = new FileInputStream(filename)) {
            return readFML(plan, is, hardwareConfigurationManager);
        }
    }

    public interface Tokens {
        public static final String ID = "id";
        public static final String ID_LANDING_BEGIN = "idLandingBegin";
        public static final String FLIGHTPLAN = "flightplan";
        public static final String HEAD = "head";
        public static final String BODY = "body";
        public static final String NOTES = "notes";
        public static final String DUMP = "dump";
        public static final String STARTPROCEDURE = "startprocedure";
        public static final String LANDINGPOINT = "landingpoint";
        public static final String REF_POINT = "refPoint";
        public static final String TAKEOFF = "takeoff";
        public static final String ORIGIN = "origin";
        public static final String MODE = "mode";
        public static final String WAYPOINT = "waypoint";
        public static final String PHOTO = "photo";
        public static final String BEGING_FLIGHTLINE = "begingFlightline";
        public static final String PHOTO_WPmode = "triggerOnWaypointsOnly";
        public static final String PHOTOSETTINGS = "photosettings";
        public static final String LOOP = "loop";
        public static final String LON = "lon";
        public static final String LAT = "lat";
        public static final String ALT = "alt";
        public static final String ALT_NEW = "altNew";
        public static final String ALT_WGS = "altWgs";
        public static final String TARGET_DISTANCE = "targetDistance";
        public static final String GEOID_SEP = "geoidOffset";
        public static final String ALT_INTERNAL = "altInternal";
        public static final String ALT_BREAKOUT = "altBreakout";
        public static final String LANDING_ANGLE_DEG = "landingAngleDeg";
        public static final String START_ALT = "startAltWgs84";
        public static final String ASSERTALTITUDE = "assertaltitude";
        public static final String RADIUS = "radius";
        public static final String FALSE = "false";
        public static final String TRUE = "true";
        public static final String POWER = "power";
        public static final String ON = "on";
        public static final String OFF = "off";
        public static final String COUNTER = "counter";
        public static final String TIME = "time";
        public static final String MAX_ROLL = "maxroll";
        public static final String MAX_NICK = "maxnick";
        public static final String MIN_TIME_INTERVAL = "mintimeinterval";
        public static final String DISTANCE = "distance";
        public static final String FRAME_DIAG = "frameDiag";
        public static final String DISTANCE_MAX = "distanceMax";
        public static final String ONLY_ONE_DIRECTION = "onlyonedirection";
        public static final String ALTITUDE_ADJUST_MODE = "altitudeAdjustMode";
        public static final String IS_MULTI_FP = "isMultiFP";
        public static final String MAX_GROUND_SPEED_KMH = "maxGroundSpeedKMH";
        public static final String MAX_GROUND_SPEED_AUTOMATIC = "maxGroundSpeedAutomatic";
        public static final String TARGET = "target";


        public static final String lastAutoLandingRefStartPosLat = "lastAutoLandingRefStartPosLat";
        public static final String lastAutoLandingRefStartPosLon = "lastAutoLandingRefStartPosLon";
        public static final String lastAutoLandingGroundLevelMeter = "lastAutoLandingGroundLevelMeter";

        public static final String GSD = "gsd";
        public static final String GSD_TOLERANCE = "gsdTolerance";
        public static final String OVERLAP_IN_FLIGHT = "overlapInFlight";
        public static final String OVERLAP_IN_FLIGHT_MIN = "overlapInFlightMin";
        public static final String OVERLAP_PARALLEL = "overlapParallel";
        public static final String PIC_AREA = "picarea";
        public static final String CORNERS = "corners";
        public static final String POINT = "point";

        public static final String WIND = "wind";
        public static final String YAW = "yaw";
        public static final String PITCH = "yaw";
        public static final String SPEED = "speed";
        public static final String ISDEFINED = "isDefined";
        public static final String HAS_YAW = "hasYaw";
        public static final String HAS_ALT = "hasAlt";
        public static final String IS_AUTO = "isAuto";
        public static final String ELEVATION = "elevation";
        public static final String STOP_AT_WAYPOINTS = "stopAtWaypoints";
        public static final String TYPE = "type";
        public static final String INDEX = "index";

        public static final String IGNORE = "ignore";
        public static final String ACTIVE = "active";
        public static final String ROW = "row";
        public static final String CELL = "cell";
        public static final String START = "start";
        public static final String START_CORNER = "start_corner";

        public static final String LEARNINGMODE = "learningmode";

        public static final String EVENT_ACTIONS = "eventActions";
        public static final String EVENT = "event";
        public static final String SAFETY_ALT = "safetyAlt";

        public static final String NAME = "name";

        public static final String TRIGGER_IMAGE = "triggerImage";
        public static final String DISTANCE_SOURCE = "distanceSource";

        public static final String SLOT_IDX = "slotIdx";
        public static final String PAYLOAD_IDX = "payloadIdx";

        public static final String ACTION = "action";
        public static final String DELAY = "delay";
        public static final String RECOVER = "recover";
        public static final String LEVEL = "level";

        public static final String WBODY = "wbody";
        public static final String PREAPPROACH = "preApproach";

        public static final String PLAN_TYPE = "planType";
        public static final String CORRIDOR_WIDTH = "corridorWidth";
        public static final String CORRIDOR_MIN_LINES = "corridorMinLines";

        public static final String CAM_ROLL = "camRoll";
        public static final String CAM_PITCH = "camPitch";
        public static final String CAM_YAW = "camYaw";

        public static final String ASSERT_YAW = "assertYaw";
        public static final String SPEED_MODE = "speedMode";
        public static final String SPEED_MPS = "speedMpSec";

        public static final String ADD_CEILING = "addCeiling";
        public static final String OBJECT_HEIGHT_MIN = "cropHeightMin";
        public static final String OBJECT_HEIGHT_MAX = "cropHeightMax";

        public static final String CROP_HEIGHT_MAX = "cropHeightMax";
        public static final String CROP_HEIGHT_MIN = "cropHeightMin";
        public static final String MODEL_FILE_PATH = "modelFilePath";

        public static final String MODEL_SCALE = "modelScale";
        public static final String MODEL_ORIGIN = "modelOrign";
        public static final String MODEL_AXIS_TRANSFORMATION = "modelAxisTransformations";
        public static final String SWAP = "swap";
        public static final String FIRST = "first";
        public static final String SECOND = "second";

        public static final String MODEL_SOURCE_TYPE = "modelSourceType";
        public static final String MODEL_AXIS_ALIGNMENT_X = "modelAxisAlignmentX";
        public static final String MODEL_AXIS_ALIGNMENT_Y = "modelAxisAlignmentY";
        public static final String MODEL_AXIS_ALIGNMENT_Z = "modelAxisAlignmentZ";
        public static final String MODEL_AXIS_OFFSET_X = "modelAxisOffsetX";
        public static final String MODEL_AXIS_OFFSET_Y = "modelAxisOffsetY";
        public static final String MODEL_AXIS_OFFSET_Z = "modelAxisOffsetZ";

        // public static final String MAX_OBSTACLE_HEIGHT = "maxObstacleHeight";
        public static final String FACADE_SCANNING_SIDE = "facadeScanningSide";
        public static final String OBJECT_HEIGHT = "objectHeight";

        public static final String ENABLE_CROP_HEIGHT_MAX = "enableCropHeightMax";
        public static final String ENABLE_CROP_HEIGHT_MIN = "enableCropHeightMin";
        public static final String MIN_GROUND_DISTANCE = "minGroundDistance";
        public static final String MIN_OBJECT_DISTANCE = "minObjectDistance";
        public static final String MAX_OBJECT_DISTANCE = "maxObjectDistance";
        public static final String CIRCLE_LEFT_TRUE_RIGHT_FALSE = "circleLeftTrueRightFalse";

        public static final String CIRCLE_COUNT = "circleCount";
        public static final String SCAN_DIRECTION = "scanDirection";
        public static final String START_CAPTURE = "startCapture";

        public static final String JUMP_PATTERN = "jumpPattern";
        public static final String VERTICAL_SCAN_PATTERN = "verticalScanPattern";
        public static final String START_CAPTURE_VERTICALLY = "startCaptureVertically";

        public static final String ONLY_SINGLE_DIRECTION = "onlySingleDirection";
        public static final String ONLY_SINGLE_DIRECTION_ANGLE = "onlySingleDirectionAngle";

        public static final String CAMERA_TILT_TOGGLE_ENABLE = "cameraTiltToggleEnable";
        public static final String CAMERA_TILT_TOGGLE_DEGREES = "cameraTiltToggleDegrees";
        public static final String CAMERA_PITCH_OFFSET_DEGREES = "cameraPitchOffsetDegrees";

        public static final String CAMERA_ROLL_TOGGLE_ENABLE = "cameraRollToggleEnable";
        public static final String CAMERA_ROLL_TOGGLE_DEGREES = "cameraRollToggleDegrees";
        public static final String CAMERA_ROLL_OFFSET_DEGREES = "cameraRollOffsetDegrees";

        public static final String MAX_YAW_ROLL_CHANGE = "maxYawRollChange";
        public static final String MAX_PITCH_CHANGE = "maxPitchChange";
        ////////////////////////////////////////////////////////////////
        // new fields from old camera
        ///////////////////////////////////////////////////////////////
        public static final String PLATFORM_DESCRIPTION = "platform_description";
        public static final String CAMERA_DESCRIPTION = "camera_description";
        public static final String LENS_DESCRIPTION = "lens_description";
        public static final String CAMERA_ID = "camera_id";
        public static final String PLATFORM_ID = "platform_id";
        public static final String LENS_ID = "lens_id";
        public static final String PICAREA_TEMPLATES = "picarea_templates";
        public static final String BASED_ON_TEMPLATE = "based_on_template";

        public static final String RECALCULATE_ON_EVERY_CHANGE = "recalculate_on_every_change";

        public static final String RESTRICTION_FLOOR = "restrictionFloor";
        public static final String RESTRICTION_FLOOR_ENABLED = "restrictionFloorEnabled";
        public static final String RESTRICTION_FLOOR_REF = "restrictionFloorRef";
        public static final String RESTRICTION_CEILING = "restrictionCeiling";
        public static final String RESTRICTION_CEILING_ENABLED = "restrictionCeilingEnabled";
        public static final String RESTRICTION_CEILING_REF = "restrictionCeilingRef";

        public static final String PITCH_OFFSET_LINE_BEGIN = "pitchOffsetLineBegin";

        public static final String ENABLE_JUMP_OVER_WAYPOINTS = "enableJumpOverWaypoints";

        public static final String HUB_DIAMETER = "hubDiameter";
        public static final String HUB_LENGTH = "hubLength";
        public static final String NUMBER_OF_BLADES = "numberOfBlades";
        public static final String BLADE_LENGTH = "bladeLength";
        public static final String BLADE_DIAMETER = "bladeDiameter";
        public static final String BLADE_THIN_RADIUS = "bladeThinRadius";
        public static final String BLADE_PITCH = "bladePitch";
        public static final String BLADE_START_ROTATION = "bladeStartRotation";
        public static final String BLADE_COVER_LENGTH = "bladeCoverLength";

    }
}
