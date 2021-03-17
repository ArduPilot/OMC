/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.flightplan;

import com.intel.missioncontrol.hardware.IGenericCameraConfiguration;
import com.intel.missioncontrol.hardware.IHardwareConfiguration;
import com.intel.missioncontrol.hardware.IPayloadConfiguration;
import com.intel.missioncontrol.hardware.IPayloadMountConfiguration;
import com.intel.missioncontrol.hardware.IPlatformDescription;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import eu.mavinci.core.helper.Pair;
import eu.mavinci.core.licence.ILicenceManager;
import eu.mavinci.core.xml.XMLWriter;
import eu.mavinci.flightplan.Dump;
import eu.mavinci.flightplan.LandingPoint;
import eu.mavinci.flightplan.Point;
import eu.mavinci.flightplan.ReferencePoint;
import eu.mavinci.flightplan.Takeoff;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

public class CFMLWriter {

    public interface Tokens extends FMLReader.Tokens {

        public static final String FLIGHTPLANML_HEADER =
            "<!DOCTYPE flightplanml PUBLIC \"-//EU//MAVINCI//XML\" \"http://www.mavinci.eu/xml/flightplanml.dtd\">\n"
                + "<?xml-stylesheet type=\"text/xsl\" href=\"flightplanml.xsl\" ?>\n"
                + "<flightplan>\n";

    }

    public void writeFlightplan(CFlightplan plan, File file) throws IOException {
        try (OutputStream out = new PrintStream(file, StandardCharsets.UTF_8.name())) {
            writeFlightplan(plan, out, null);
        }
    }

    public String flightplanToXML(CFlightplan plan) throws IOException {
        return flightplanToXML(plan, null);
    }

    public String flightplanToXML(CFlightplan plan, String hash) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        writeFlightplan(plan, out, hash);
        // System.out.println(out.toString());
        return out.toString();
    }

    public void writeFlightplan(CFlightplan plan, OutputStream os) throws IOException {
        writeFlightplan(plan, os, null);
    }

    String hash = null;

    public void writeFlightplan(CFlightplan plan, OutputStream os, String hash) throws IOException {
        if (hash != null) {
            hash = hash.toUpperCase();
        }

        this.hash = hash;

        // XMLWriter xml = new GXMLWriter(new PrintWriter(os)); //I dont use this, because it wrongly encodes umlaute in
        // tag-bodies
        XMLWriter xml = new XMLWriter(new PrintWriter(os));
        xml.begin(Tokens.FLIGHTPLANML_HEADER, 2);
        xml.comment(DependencyInjector.getInstance().getInstanceOf(ILicenceManager.class).getExportHeaderCore());

        xml.start(Tokens.HEAD);
        if (hash != null) {
            write(new Dump(Dump.prefixFPhash + hash), xml);
            // } else {
            // write(new CDump("dummy"),xml); //without this, we will not get an almost blank line if we trying to
            // remove the dump on text
            // base, but NOTHING on this place -> hashes would be different!!
        }

        write(plan.photoSettings, xml);
        if (plan.basedOnTemplate != null) {
            write(plan.basedOnTemplate, xml);
        }

        xml.tag(Tokens.RECALCULATE_ON_EVERY_CHANGE, Tokens.NAME, Boolean.toString(plan.recalculateOnEveryChange));
        write(plan.eventList, xml);
        if (plan.isLearningmode) {
            xml.tagEmpty(Tokens.LEARNINGMODE);
        }

        // has to be done before landing point to make landing point readin maybe fix missing EGM offset in file
        String[] attr = new String[] {Tokens.ALT, Tokens.GEOID_SEP};
        String[] vals =
            new String[] {
                Double.toString(plan.getRefPointAltWgs84WithElevation()), Double.toString(plan.getStartGeoidSep())
            };
        xml.tag(Tokens.START_ALT, attr, vals);

        if (plan.getName() != null) {
            xml.tag(Tokens.NAME, Tokens.NAME, plan.getName());
        } else {
            if (plan.getFile() != null) {
                xml.tag(Tokens.NAME, Tokens.NAME, plan.getFile().getName());
            }
        }

        write(plan.landingpoint, xml);
        write(plan.refPoint, xml);
        write(plan.takeoff, xml);

        write(plan.getHardwareConfiguration(), xml);
        // write(plan.cameraDescription, xml);
        write(plan.picAreaTemplates, xml);
        // write(plan.flightPlanDescription, xml);
        xml.tag(Tokens.ENABLE_JUMP_OVER_WAYPOINTS, Tokens.NAME, Boolean.toString(plan.enableJumpOverWaypoints));

        xml.end(); // head

        xml.start(Tokens.BODY);
        for (IFlightplanStatement point : plan.elements) {
            writeSwitch(point, xml);
        }

        xml.end(); // body

        // finish writing file
        xml.finish("</" + Tokens.FLIGHTPLAN + ">\n");
    }
    //
    //    private void write(IFlightPlanDescription flightPlanDescription, XMLWriter xml) {
    //        String[] attr = new String[] { Tokens.NAME};
    //        String[] vals = new String[] { cameraDescription.getName()};
    //        xml.tag(Tokens.CAMERA_DESCRIPTION, attr, vals);
    //    }

    private void write(List<CPicArea> picAreaTemplates, XMLWriter xml) throws IOException {
        xml.start(Tokens.PICAREA_TEMPLATES);
        for (CPicArea picArea : picAreaTemplates) {
            write(picArea, xml);
        }

        xml.end();
    }

    private void write(IHardwareConfiguration hardwareConfiguration, XMLWriter xml) {
        IPlatformDescription platformDescription = hardwareConfiguration.getPlatformDescription();
        String[] attr = new String[] {Tokens.PLATFORM_ID};
        String[] vals = new String[] {platformDescription.getId()};
        xml.start(Tokens.PLATFORM_DESCRIPTION, attr, vals);
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
        xml.tag(Tokens.CAMERA_DESCRIPTION, attr, vals);
    }

    private void write(String templateName, XMLWriter xml) {
        xml.start(Tokens.BASED_ON_TEMPLATE, new String[] {Tokens.NAME}, new String[] {templateName});
        xml.end();
    }

    private void write(CEventList eventList, XMLWriter xml) {
        xml.start(
            Tokens.EVENT_ACTIONS,
            new String[] {Tokens.SAFETY_ALT, Tokens.IS_AUTO},
            new String[] {
                Integer.toString(eventList.safetyAltitude_CM), Boolean.toString(eventList.isAutoComputingSafetyHeight())
            });
        for (IFlightplanStatement st : eventList.elements) {
            CEvent event = (CEvent)st;
            write(event, xml);
        }

        xml.end();
    }

    private void write(CEvent event, XMLWriter xml) {
        if (event.hasLevel) {
            String[] attr = new String[] {Tokens.NAME, Tokens.ACTION, Tokens.DELAY, Tokens.RECOVER, Tokens.LEVEL};
            String[] vals =
                new String[] {
                    event.getName(),
                    Integer.toString(event.action.ordinal()),
                    Integer.toString(event.delay),
                    (event.recover ? "1" : "0"),
                    Integer.toString(event.level)
                };
            xml.tag(Tokens.EVENT, attr, vals);
        } else {
            String[] attr = new String[] {Tokens.NAME, Tokens.ACTION, Tokens.DELAY, Tokens.RECOVER};
            String[] vals =
                new String[] {
                    event.getName(),
                    Integer.toString(event.action.ordinal()),
                    Integer.toString(event.delay),
                    (event.recover ? "1" : "0")
                };
            xml.tag(Tokens.EVENT, attr, vals);
        }
    }

    private void write(LandingPoint lp, XMLWriter xml) {
        String[] attr =
            new String[] {
                Tokens.ID,
                Tokens.LON,
                Tokens.LAT,
                Tokens.MODE,
                Tokens.ALT,
                Tokens.ALT_INTERNAL,
                Tokens.YAW,
                Tokens.ALT_BREAKOUT,
                Tokens.LANDING_ANGLE_DEG,
                Tokens.lastAutoLandingRefStartPosLat,
                Tokens.lastAutoLandingRefStartPosLon,
                Tokens.lastAutoLandingGroundLevelMeter,
                Tokens.ID_LANDING_BEGIN
            };
        String[] vals =
            new String[] {
                Integer.toString(lp.getId()),
                Double.toString(lp.getLon()),
                Double.toString(lp.getLat()),
                Integer.toString(lp.getMode().ordinal()),
                Integer.toString(lp.getAltWithinCM()),
                Integer.toString(lp.getAltitude()),
                Double.toString(lp.getYaw()),
                Integer.toString(lp.getAltBreakoutWithinCM()),
                Double.toString(lp.getLandingAngleDeg()),
                Double.toString(lp.getLastAutoLandingRefStartPosLat()),
                Double.toString(lp.getLastAutoLandingRefStartPosLon()),
                Double.toString(lp.getLastAutoLandingGroundLevelMeter()),
                Integer.toString(lp.getIdLandingBegin())
            };
        xml.tag(Tokens.LANDINGPOINT, attr, vals);
    }

    private void write(ReferencePoint point, XMLWriter xml) {
        Vector<String> attrV = new Vector<>();
        Vector<String> valsV = new Vector<>();

        attrV.addAll(
            Arrays.asList(
                new String[] {
                    Tokens.LON,
                    Tokens.LAT,
                    Tokens.ALT,
                    Tokens.ALT_WGS,
                    Tokens.GEOID_SEP,
                    Tokens.YAW,
                    Tokens.ISDEFINED,
                    Tokens.HAS_ALT,
                    Tokens.HAS_YAW,
                    Tokens.IS_AUTO,
                    Tokens.ELEVATION,
                    Tokens.TYPE,
                    Tokens.INDEX
                }));
        valsV.addAll(
            Arrays.asList(
                new String[] {
                    Double.toString(point.getLon()),
                    Double.toString(point.getLat()),
                    Double.toString(point.getAltInMAboveFPRefPoint()),
                    Double.toString(point.getAltitudeWgs84()),
                    Double.toString(point.getGeoidSeparation()),
                    Double.toString(point.getYaw()),
                    Boolean.toString(point.isDefined()),
                    Boolean.toString(point.hasAlt()),
                    Boolean.toString(point.hasYaw()),
                    Boolean.toString(point.isAuto()),
                    Double.toString(point.getElevation()),
                    point.getRefPointType().toString(),
                    Integer.toString(point.getRefPointOptionIndex())
                }));

        String[] attr = attrV.toArray(new String[attrV.size()]);
        String[] vals = valsV.toArray(new String[valsV.size()]);

        xml.tag(Tokens.REF_POINT, attr, vals);
    }

    private void write(Takeoff point, XMLWriter xml) {
        Vector<String> attrV = new Vector<>();
        Vector<String> valsV = new Vector<>();

        attrV.addAll(
            Arrays.asList(
                new String[] {
                    Tokens.LON,
                    Tokens.LAT,
                    Tokens.ALT,
                    Tokens.ALT_WGS,
                    Tokens.GEOID_SEP,
                    Tokens.YAW,
                    Tokens.ISDEFINED,
                    Tokens.HAS_ALT,
                    Tokens.HAS_YAW,
                    Tokens.IS_AUTO,
                    Tokens.ELEVATION
                }));
        valsV.addAll(
            Arrays.asList(
                new String[] {
                    Double.toString(point.getLon()),
                    Double.toString(point.getLat()),
                    Double.toString(point.getAltInMAboveFPRefPoint()),
                    Double.toString(point.getAltitudeWgs84()),
                    Double.toString(point.getGeoidSeparation()),
                    Double.toString(point.getYaw()),
                    Boolean.toString(point.isDefined()),
                    Boolean.toString(point.hasAlt()),
                    Boolean.toString(point.hasYaw()),
                    Boolean.toString(point.isAuto()),
                    Double.toString(point.getElevation())
                }));

        String[] attr = attrV.toArray(new String[attrV.size()]);
        String[] vals = valsV.toArray(new String[valsV.size()]);

        xml.tag(Tokens.TAKEOFF, attr, vals);
    }

    private void write(CWaypoint point, XMLWriter xml) {
        Vector<String> attrV = new Vector<>();
        Vector<String> valsV = new Vector<>();

        attrV.addAll(
            Arrays.asList(
                new String[] {
                    Tokens.LON,
                    Tokens.LAT,
                    Tokens.ALT,
                    Tokens.RADIUS,
                    Tokens.ID,
                    Tokens.ASSERTALTITUDE,
                    Tokens.IGNORE,
                    Tokens.SPEED_MODE,
                    Tokens.SPEED_MPS,
                    Tokens.STOP_AT_WAYPOINTS,
                    Tokens.PHOTO,
                    Tokens.BEGING_FLIGHTLINE,
                    Tokens.TARGET_DISTANCE
                }));
        valsV.addAll(
            Arrays.asList(
                new String[] {
                    Double.toString(point.lon),
                    Double.toString(point.lat),
                    Integer.toString(point.alt),
                    Integer.toString(point.radius),
                    Integer.toString(point.id),
                    point.assertAltitude.toString(),
                    Boolean.toString(point.ignore),
                    point.speedMode.toString(),
                    Double.toString(point.speedMpSec),
                    Double.toString(point.stopHereTimeCopter),
                    Boolean.toString(point.triggerImageHereCopterMode),
                    Boolean.toString(point.isBeginFlightline),
                    Double.toString(point.targetDistance)
                }));

        Orientation o = point.getOrientation();
        // if (o.isRollDefined()){
        attrV.add(Tokens.CAM_ROLL);
        valsV.add(Double.toString(o.getRoll()));
        // }
        // if (o.isPitchDefined()){
        attrV.add(Tokens.CAM_PITCH);
        valsV.add(Double.toString(o.getPitch()));
        // }
        // if (o.isYawDefined()){
        attrV.add(Tokens.CAM_YAW);
        valsV.add(Double.toString(o.getYaw()));
        // }
        if (point.assertYawOn) {
            attrV.add(Tokens.ASSERT_YAW);
            valsV.add(Double.toString(point.assertYaw));
        }

        String[] attr = attrV.toArray(new String[attrV.size()]);
        String[] vals = valsV.toArray(new String[valsV.size()]);

        // System.out.println("write waypoint");
        if (point.getBody().isEmpty()) {
            xml.tag(Tokens.WAYPOINT, attr, vals);
        } else {
            xml.start(Tokens.WAYPOINT, attr, vals);
            xml.contentTag(Tokens.WBODY, point.getBody());
            xml.end();
        }
    }

    private void write(CPreApproach point, XMLWriter xml) {
        String[] attr = new String[] {Tokens.LON, Tokens.LAT, Tokens.ALT, Tokens.ID};
        String[] vals =
            new String[] {
                Double.toString(point.lon),
                Double.toString(point.lat),
                Integer.toString(point.alt),
                Integer.toString(point.id)
            };
        xml.tag(Tokens.PREAPPROACH, attr, vals);
    }

    private void write(CDump dump, XMLWriter xml) {
        xml.contentTag(Tokens.DUMP, dump.getBody());
    }

    private void write(Point point, XMLWriter xml) {
        String[] attr = new String[] {Tokens.LON, Tokens.LAT};
        String[] vals = new String[] {Double.toString(point.getLon()), Double.toString(point.getLat())};
        xml.tag(Tokens.POINT, attr, vals);
    }

    private void write(CStartProcedure start, XMLWriter xml) {
        if (start.hasOwnAltitude) {
            String[] attr = new String[] {Tokens.ID, Tokens.ALT, Tokens.ACTIVE};
            String[] vals =
                new String[] {
                    Integer.toString(start.id), Integer.toString(start.altitude), Boolean.toString(start.isActive)
                };
            xml.tag(Tokens.STARTPROCEDURE, attr, vals);
        } else {
            String[] attr = new String[] {Tokens.ID, Tokens.ACTIVE};
            String[] vals = new String[] {Integer.toString(start.id), Boolean.toString(start.isActive)};
            xml.tag(Tokens.STARTPROCEDURE, attr, vals);
        }
    }

    private void write(CPhoto photo, XMLWriter xml) {
        String power;
        if (photo.powerOn) {
            power = "on";
        } else {
            power = "off";
        }

        String[] attr =
            new String[] {Tokens.POWER, Tokens.DISTANCE, Tokens.DISTANCE_MAX, Tokens.ID, Tokens.PHOTO_WPmode};
        String[] vals =
            new String[] {
                power,
                Double.toString(photo.distance),
                Double.toString(photo.distanceMax),
                Integer.toString(photo.id),
                Boolean.toString(photo.triggerOnlyOnWaypoints)
            };
        xml.tag(Tokens.PHOTO, attr, vals);

        if (photo.powerOn && hash != null) {
            write(new Dump(Dump.prefixFPhash + hash), xml);
        }
    }

    private void write(CPhotoSettings photoSettings, XMLWriter xml) {
        String[] attr =
            new String[] {
                Tokens.MAX_ROLL,
                Tokens.MAX_NICK,
                Tokens.MIN_TIME_INTERVAL,
                Tokens.ALTITUDE_ADJUST_MODE,
                Tokens.IS_MULTI_FP,
                Tokens.MAX_GROUND_SPEED_KMH,
                Tokens.MAX_GROUND_SPEED_AUTOMATIC,
                Tokens.STOP_AT_WAYPOINTS,
                Tokens.GSD_TOLERANCE
            };
        String[] vals =
            new String[] {
                Double.toString(photoSettings.maxRoll),
                Double.toString(photoSettings.maxNick),
                Double.toString(photoSettings.mintimeinterval),
                photoSettings.altAdjustMode.toString(),
                Boolean.toString(photoSettings.isMultiFP),
                Double.toString(photoSettings.maxGroundSpeedMPSec * 3.6),
                photoSettings.maxGroundSpeedAutomatic.toString(),
                Boolean.toString(photoSettings.stoppingAtWaypoints),
                Double.toString(photoSettings.gsdTolerance),
            };
        xml.tag(Tokens.PHOTOSETTINGS, attr, vals);
    }

    private void write(CWaypointLoop loop, XMLWriter xml) throws IOException {
        String[] attr = new String[] {Tokens.COUNTER, Tokens.TIME, Tokens.ID, Tokens.IGNORE};
        String[] vals =
            new String[] {
                Integer.toString(loop.count),
                Integer.toString(loop.time),
                Integer.toString(loop.id),
                Boolean.toString(loop.ignore)
            };
        xml.start(Tokens.LOOP, attr, vals);
        if (!loop.getBody().isEmpty()) {
            xml.contentTag(Tokens.WBODY, loop.getBody());
        }

        for (IFlightplanStatement point : loop.elements) {
            writeSwitch(point, xml);
        }

        xml.end();
    }

    /*






       public static final String ONLY_SINGLE_DIRECTION   = "onlySingleDirection";
       public static final String ONLY_SINGLE_DIRECTION_ANGLE   = "onlySingleDirectionAngle";

       public static final String CAMERA_TILT_TOGGLE_ENABLE  = "cameraTiltToggleEnable";
       public static final String CAMERA_TILT_TOGGLE_DEGREES  = "cameraTiltToggleDegrees";
    */
    private void write(CPicArea area, XMLWriter xml) throws IOException {
        String[] attr =
            new String[] {
                Tokens.ID,
                Tokens.GSD,
                Tokens.OVERLAP_IN_FLIGHT,
                Tokens.OVERLAP_IN_FLIGHT_MIN,
                Tokens.OVERLAP_PARALLEL,
                Tokens.ALT,
                Tokens.PLAN_TYPE,
                Tokens.CORRIDOR_MIN_LINES,
                Tokens.CORRIDOR_WIDTH,
                Tokens.ADD_CEILING,
                Tokens.OBJECT_HEIGHT_MIN,
                Tokens.OBJECT_HEIGHT_MAX,
                //////////////////////////////////////////////////// new fields
                Tokens.NAME,
                Tokens.MODEL_FILE_PATH,
                Tokens.MODEL_SCALE,
                Tokens.MODEL_SOURCE_TYPE,
                Tokens.MODEL_AXIS_ALIGNMENT_X,
                Tokens.MODEL_AXIS_ALIGNMENT_Y,
                Tokens.MODEL_AXIS_ALIGNMENT_Z,
                Tokens.MODEL_AXIS_OFFSET_X,
                Tokens.MODEL_AXIS_OFFSET_Y,
                Tokens.MODEL_AXIS_OFFSET_Z,
                Tokens.MIN_GROUND_DISTANCE,
                Tokens.MIN_OBJECT_DISTANCE,
                Tokens.MAX_OBJECT_DISTANCE,
                Tokens.FACADE_SCANNING_SIDE,
                Tokens.OBJECT_HEIGHT,
                Tokens.ENABLE_CROP_HEIGHT_MAX,
                Tokens.ENABLE_CROP_HEIGHT_MIN,
                Tokens.CIRCLE_LEFT_TRUE_RIGHT_FALSE,
                Tokens.CIRCLE_COUNT,
                Tokens.SCAN_DIRECTION,
                Tokens.START_CAPTURE,
                Tokens.JUMP_PATTERN,
                Tokens.VERTICAL_SCAN_PATTERN,
                Tokens.START_CAPTURE_VERTICALLY,
                Tokens.ONLY_SINGLE_DIRECTION,
                Tokens.ONLY_SINGLE_DIRECTION_ANGLE,
                Tokens.CAMERA_TILT_TOGGLE_ENABLE,
                Tokens.CAMERA_TILT_TOGGLE_DEGREES,
                Tokens.CAMERA_PITCH_OFFSET_DEGREES,
                Tokens.CAMERA_ROLL_TOGGLE_ENABLE,
                Tokens.CAMERA_ROLL_TOGGLE_DEGREES,
                Tokens.CAMERA_ROLL_OFFSET_DEGREES,
                Tokens.MAX_YAW_ROLL_CHANGE,
                Tokens.MAX_PITCH_CHANGE,
                Tokens.RESTRICTION_CEILING,
                Tokens.RESTRICTION_CEILING_ENABLED,
                Tokens.RESTRICTION_CEILING_REF,
                Tokens.RESTRICTION_FLOOR,
                Tokens.RESTRICTION_FLOOR_ENABLED,
                Tokens.RESTRICTION_FLOOR_REF,
                Tokens.PITCH_OFFSET_LINE_BEGIN,
                //////////////////////////////////////////////////// new windmill fields
                Tokens.HUB_DIAMETER,
                Tokens.HUB_LENGTH,
                Tokens.NUMBER_OF_BLADES,
                Tokens.BLADE_LENGTH,
                Tokens.BLADE_DIAMETER,
                Tokens.BLADE_THIN_RADIUS,
                Tokens.BLADE_PITCH,
                Tokens.BLADE_START_ROTATION,
                Tokens.BLADE_COVER_LENGTH,
            };

        String[] vals =
            new String[] {
                Integer.toString(area.id),
                Double.toString(area.gsd),
                Double.toString(area.overlapInFlight),
                Double.toString(area.overlapInFlightMin),
                Double.toString(area.overlapParallel),
                Double.toString(area.alt),
                area.planType.toString(),
                Integer.toString(area.corridorMinLines),
                Double.toString(area.corridorWidthInMeter),
                Boolean.toString(area.addCeiling),
                Double.toString(area.cropHeightMin),
                Double.toString(area.cropHeightMax),

                ///////////////////////////////////////////////// new fields
                area.getName(),
                area.getModelFilePath(),
                Double.toString(area.getModelScale()),
                area.getModelSource().name(),
                area.getModelAxisAlignmentX().name(),
                area.getModelAxisAlignmentY().name(),
                area.getModelAxisAlignmentZ().name(),
                String.valueOf(area.getModelAxisOffsetX()),
                String.valueOf(area.getModelAxisOffsetY()),
                String.valueOf(area.getModelAxisOffsetZ()),
                String.valueOf(area.getMinGroundDistance()),
                String.valueOf(area.getMinObjectDistance()),
                String.valueOf(area.getMaxObjectDistance()),
                String.valueOf(area.getFacadeScanningSide()),
                String.valueOf(area.getObjectHeight()),
                String.valueOf(area.enableCropHeightMax),
                String.valueOf(area.enableCropHeightMin),
                String.valueOf(area.circleLeftTrueRightFalse),
                String.valueOf(area.circleCount),
                String.valueOf(area.scanDirection),
                String.valueOf(area.startCapture),
                String.valueOf(area.jumpPattern),
                String.valueOf(area.verticalScanPattern),
                String.valueOf(area.startCaptureVertically),
                String.valueOf(area.onlySingleDirection),
                String.valueOf(area.getYaw()),
                String.valueOf(area.cameraTiltToggleEnable),
                String.valueOf(area.cameraTiltToggleDegrees),
                String.valueOf(area.cameraPitchOffsetDegrees),
                String.valueOf(area.cameraRollToggleEnable),
                String.valueOf(area.cameraRollToggleDegrees),
                String.valueOf(area.cameraRollOffsetDegrees),
                String.valueOf(area.maxYawRollChange),
                String.valueOf(area.maxPitchChange),
                String.valueOf(area.restrictionCeiling),
                String.valueOf(area.restrictionCeilingEnabled),
                String.valueOf(area.restrictionCeilingRef),
                String.valueOf(area.restrictionFloor),
                String.valueOf(area.restrictionFloorEnabled),
                String.valueOf(area.restrictionFloorRef),
                String.valueOf(area.pitchOffsetLineBegin),

                //////////////////////////////////////////////////// new windmill fields
                String.valueOf(area.getWindmillHubDiameter()),
                String.valueOf(area.getWindmillHubLength()),
                String.valueOf(area.getWindmillNumberOfBlades()),
                String.valueOf(area.getWindmillBladeLength()),
                String.valueOf(area.getWindmillBladeDiameter()),
                String.valueOf(area.getWindmillBladeThinRadius()),
                String.valueOf(area.getWindmillBladePitch()),
                String.valueOf(area.getWindmillBladeStartRotation()),
                String.valueOf(area.getWindmillBladeCoverLength()),
            };

        xml.start(Tokens.PIC_AREA, attr, vals);
        write(area.corners, xml);
        write(area.getModelReferencePoint(), xml);
        write(area.getModelAxisTransformations(), xml);
        for (IFlightplanStatement point : area.elements) {
            writeSwitch(point, xml);
        }

        xml.end();
    }

    private void write(
            Collection<Pair<CPicArea.ModelAxis, CPicArea.ModelAxis>> modelAxisTransformations, XMLWriter xml) {
        xml.start(Tokens.MODEL_AXIS_TRANSFORMATION);
        String[] attr = new String[] {Tokens.FIRST, Tokens.SECOND};
        for (Pair<CPicArea.ModelAxis, CPicArea.ModelAxis> swap : modelAxisTransformations) {
            String[] vals = new String[] {String.valueOf(swap.first), String.valueOf(swap.second)};
            xml.tag(Tokens.SWAP, attr, vals);
        }

        xml.end();
    }

    private void write(CPicAreaCorners loop, XMLWriter xml) throws IOException {
        xml.start(Tokens.CORNERS);
        for (IFlightplanStatement point : loop.elements) {
            writeSwitch(point, xml);
        }

        xml.end();
    }

    private void writeSwitch(IFlightplanRelatedObject point, XMLWriter xml) throws IOException {
        if (point instanceof CWaypoint) {
            CWaypoint new_name = (CWaypoint)point;
            write(new_name, xml);
        } else if (point instanceof CPhoto) {
            CPhoto new_name = (CPhoto)point;
            write(new_name, xml);
        } else if (point instanceof CWaypointLoop) {
            CWaypointLoop new_name = (CWaypointLoop)point;
            write(new_name, xml);
        } else if (point instanceof Point) {
            Point new_name = (Point)point;
            write(new_name, xml);
        } else if (point instanceof CStartProcedure) {
            CStartProcedure new_name = (CStartProcedure)point;
            write(new_name, xml);
        } else if (point instanceof CPicArea) {
            CPicArea new_name = (CPicArea)point;
            write(new_name, xml);
        } else if (point instanceof CPicAreaCorners) {
            CPicAreaCorners new_name = (CPicAreaCorners)point;
            write(new_name, xml);
        } else if (point instanceof LandingPoint) {
            LandingPoint new_name = (LandingPoint)point;
            write(new_name, xml);
        } else if (point instanceof CPhotoSettings) {
            CPhotoSettings photoSettings = (CPhotoSettings)point;
            write(photoSettings, xml);
        } else if (point instanceof CDump) {
            CDump dump = (CDump)point;
            write(dump, xml);
        } else if (point instanceof CPreApproach) {
            CPreApproach dump = (CPreApproach)point;
            write(dump, xml);
        } else {
            throw new IOException("No rule for XML-writing from: " + point + "<" + point.getClass() + ">");
        }
    }

}
