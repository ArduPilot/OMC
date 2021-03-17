/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.flightplan;

import com.intel.missioncontrol.hardware.IGenericCameraConfiguration;
import com.intel.missioncontrol.hardware.IGenericCameraDescription;
import com.intel.missioncontrol.hardware.IHardwareConfiguration;
import com.intel.missioncontrol.hardware.IHardwareConfigurationManager;
import com.intel.missioncontrol.helper.Ensure;
import com.intel.missioncontrol.mission.ReferencePointType;
import eu.mavinci.core.flightplan.FMLReader.Tokens;
import eu.mavinci.core.helper.StringHelper;
import eu.mavinci.core.plane.AirplaneEventActions;
import eu.mavinci.desktop.main.debug.Debug;
import eu.mavinci.flightplan.LandingPoint;
import eu.mavinci.flightplan.PicArea;
import eu.mavinci.flightplan.Point;
import eu.mavinci.flightplan.ReferencePoint;
import eu.mavinci.flightplan.Takeoff;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class FMLHandler extends DefaultHandler implements Tokens {
    public static final String JUMP_OVER = "JumpOver";
    private static final int MAX_NUM_TRIES =
        100; // for tries to update the elevation model (if it is not yet precise enough)
    private final IHardwareConfigurationManager hardwareConfigurationManager;

    protected Stack<IFlightplanContainer> outerContainer = new Stack<>();
    private IGenericCameraDescription lastCamera;
    protected CFlightplan plan;
    private IHardwareConfiguration tempHardwareConfig;

    private IFlightplanNodeBody lastBodyParentNode;
    private CDump lastDump;
    protected StringBuffer sbuf = new StringBuffer();

    FMLHandler(CFlightplan plan, IHardwareConfigurationManager hardwareConfigurationManager) {
        super();
        this.plan = plan;
        this.hardwareConfigurationManager = hardwareConfigurationManager;
        this.tempHardwareConfig = hardwareConfigurationManager.getImmutableDefault().deepCopy();
    }

    @Override
    public void startDocument() {
        outerContainer.push(plan);
    }

    @Override
    public void endDocument() throws SAXException {
        plan.hardwareConfiguration.initializeFrom(tempHardwareConfig);
    }

    boolean inBody = false;
    boolean inHead = false;
    boolean bodyToLoad = false;

    @Override
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
        qName = localName;
        // lastWaypoint = null;
        lastDump = null;

        // first clear the character buffer
        sbuf.delete(0, sbuf.length());

        // if (GlobalSettings.system == OpSys.Android) {
        // qName = localName;
        // }
        // System.out.println("namespaceURI:"+namespaceURI+" localName:"+localName+" qName:"+qName+" atts:"+atts);

        if (qName.equals(HEAD)) {
            inHead = true;
            inBody = false;
        } else if (qName.equals(BODY)) {
            inHead = false;
            inBody = true;
        }

        // legacy support
        if (qName.equals(ORIGIN)) {
            if (inBody) {
                initRefPoint((CPicArea)outerContainer.peek());
            } else {
                // legacy case
                // if in the head and not 0 then take this value as a new takeoff --- additionally set R to the takeoff
                try {
                    double lon = Double.parseDouble(atts.getValue(LON));
                    double lat = Double.parseDouble(atts.getValue(LAT));
                    double alt = Double.parseDouble(atts.getValue(ALT));

                    double yaw = Double.parseDouble(atts.getValue(YAW));
                    boolean isDefined = Boolean.parseBoolean(atts.getValue(ISDEFINED));
                    boolean hasAlt = Boolean.parseBoolean(atts.getValue(HAS_ALT));
                    boolean hasYaw = Boolean.parseBoolean(atts.getValue(HAS_YAW));

                    IFlightplanContainer container = outerContainer.peek();
                    if (container instanceof CFlightplan && isDefined) {
                        ReferencePoint referencePoint = ((CFlightplan)container).getRefPoint();
                        Takeoff takeoff = ((CFlightplan)container).getTakeoff();

                        takeoff.setValues(lat, lon, alt, 0, 0, yaw, isDefined, hasAlt, hasYaw, false, 0);
                        double absAlt = takeoff.updateAltitudeWgs84();
                        int counter = 0;
                        while (takeoff.updateAltitudeWgs84() == absAlt && counter < MAX_NUM_TRIES) {
                            counter++;
                        }

                        referencePoint.setValues(((CFlightplan)container).getTakeoff());
                        referencePoint.setIsAuto(true);
                        referencePoint.setRefPointType(ReferencePointType.TAKEOFF);
                    }

                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        } else if (qName.equals(REF_POINT)) {
            try {
                double lon = Double.parseDouble(atts.getValue(LON));
                double lat = Double.parseDouble(atts.getValue(LAT));
                double alt = Double.parseDouble(atts.getValue(ALT));
                double altWgs84 = 0;
                double geoidSep = 0;
                ReferencePointType type = ReferencePointType.VERTEX;
                int refPointOptionIndex = 0;
                try {
                    altWgs84 = Double.parseDouble(atts.getValue(ALT_WGS));
                    geoidSep = Double.parseDouble(atts.getValue(GEOID_SEP));
                    type = ReferencePointType.valueOf(atts.getValue(TYPE));
                    refPointOptionIndex = Integer.valueOf(atts.getValue(INDEX));
                } catch (Exception e) {
                    Debug.getLog()
                        .log(Level.WARNING, "Old log format, origin´s WGS84 altitude was not read from the log");
                }

                double yaw = Double.parseDouble(atts.getValue(YAW));
                boolean isDefined = Boolean.parseBoolean(atts.getValue(ISDEFINED));

                boolean hasAlt = Boolean.parseBoolean(atts.getValue(HAS_ALT));

                boolean hasYaw = Boolean.parseBoolean(atts.getValue(HAS_YAW));

                String isAutoStr = atts.getValue(IS_AUTO);
                String elevationStr = atts.getValue(ELEVATION);
                double elevation = 0;
                // in case of the new format of the mission reading the elevation value
                if (elevationStr != null) {
                    elevation = Double.parseDouble(elevationStr);
                }

                boolean isAuto = StringHelper.isNullOrBlank(isAutoStr) ? true : Boolean.parseBoolean(isAutoStr);

                IFlightplanContainer container = outerContainer.peek();
                ReferencePoint origin = null;
                if (container instanceof CFlightplan) {
                    origin = ((CFlightplan)container).refPoint;
                } else if (container instanceof CPicArea) {
                    origin = ((CPicArea)container).modelReferencePoint;
                }

                if (origin != null) {
                    if (lat != 0 && lon != 0) {
                        origin.setMute(true);
                        origin.setValues(
                            lat, lon, alt, altWgs84, geoidSep, yaw, isDefined, hasAlt, hasYaw, isAuto, elevation);
                        origin.setRefPointType(type);
                        origin.setRefPointOptionIndex(refPointOptionIndex);

                        // updating abs altitude only in case if the legacy one was not defined
                        if (isDefined && altWgs84 == 0) {
                            double absAlt = origin.updateAltitudeWgs84();
                            int counter = 0;
                            while (origin.updateAltitudeWgs84() == absAlt && counter < MAX_NUM_TRIES) {
                                counter++;
                            }
                        }
                    } else {
                        if (container instanceof PicArea) {
                            // edge case (legacy compatibility) - when ref point inside picArea is
                            origin.setValues(plan.getRefPoint());
                        }
                    }

                    origin.setSilentUnmute();
                }

            } catch (Throwable t) {
            }
        } else if (qName.equals(TAKEOFF)) {
            try {
                double lon = Double.parseDouble(atts.getValue(LON));
                double lat = Double.parseDouble(atts.getValue(LAT));
                double alt = Double.parseDouble(atts.getValue(ALT));
                double altWgs84 = 0;
                double geoidSep = 0;
                try {
                    altWgs84 = Double.parseDouble(atts.getValue(ALT_WGS));
                    geoidSep = Double.parseDouble(atts.getValue(GEOID_SEP));

                } catch (Exception e) {
                    Debug.getLog()
                        .log(Level.WARNING, "Old log format, origin´s WGS84 altitude was not read from the log");
                }

                double yaw = Double.parseDouble(atts.getValue(YAW));
                boolean isDefined = Boolean.parseBoolean(atts.getValue(ISDEFINED));

                boolean hasAlt = Boolean.parseBoolean(atts.getValue(HAS_ALT));

                boolean hasYaw = Boolean.parseBoolean(atts.getValue(HAS_YAW));

                String isAutoStr = atts.getValue(IS_AUTO);
                String elevationStr = atts.getValue(ELEVATION);
                double elevation = 0;
                // in case of the new format of the mission reading the elevation value
                if (elevationStr != null) {
                    elevation = Double.parseDouble(elevationStr);
                }

                boolean isAuto = StringHelper.isNullOrBlank(isAutoStr) ? true : Boolean.parseBoolean(isAutoStr);

                IFlightplanContainer container = outerContainer.peek();
                Takeoff takeoff;
                if (container instanceof CFlightplan) {
                    CFlightplan flightPlan = (CFlightplan)container;
                    takeoff = flightPlan.takeoff;
                    takeoff.setMute(true);
                    takeoff.setValues(
                        lat, lon, alt, altWgs84, geoidSep, yaw, isDefined, hasAlt, hasYaw, isAuto, elevation);
                    if (isDefined && altWgs84 == 0 && !(lat == 0 && lon == 0)) {
                        double absAlt = takeoff.updateAltitudeWgs84();
                        int counter = 0;
                        while (takeoff.updateAltitudeWgs84() == absAlt && counter < MAX_NUM_TRIES) {
                            counter++;
                        }
                    }

                    takeoff.setAltInMAboveFPRefPoint(
                        flightPlan.getTakeofftAltWgs84WithElevation() - flightPlan.getRefPointAltWgs84WithElevation());
                    takeoff.setSilentUnmute();

                    if (plan.landingpoint.isDefaultPosition()) {
                        if (takeoff.getLatLon().equals(LatLon.ZERO)) {
                            takeoff.setValues(
                                lat, lon, alt, altWgs84, geoidSep, yaw, isDefined, hasAlt, hasYaw, true, elevation);
                        }

                        plan.landingpoint.setLatLon(takeoff.getLatLon());
                        plan.landingpoint.setMode(LandingModes.LAND_AT_TAKEOFF);
                        plan.landingpoint.setAltInMAboveFPRefPoint(LandingPoint.DEFAULT_ALTITUDEINMETER);
                        plan.landingpoint.setId(ReentryPoint.INVALID_REENTRYPOINT);
                        plan.landingpoint.setLandAutomatically(false);
                    }

                    if (plan.landingpoint != null) plan.landingpoint.doSubRecalculationStage2();
                }

            } catch (Throwable t) {
            }
        } else if (inHead) {
            if (qName.equals(LANDINGPOINT)) {
                plan.landingpoint.setLatLon(
                    Double.parseDouble(atts.getValue(LAT)), Double.parseDouble(atts.getValue(LON)));
                try {
                    plan.landingpoint.setMode(LandingModes.valueOf(atts.getValue(MODE)));
                } catch (Exception e) {
                }

                try {
                    plan.landingpoint.setAltInMAboveFPRefPoint(Double.parseDouble(atts.getValue(ALT_NEW)));
                } catch (Throwable t) {
                }

                try {
                    plan.landingpoint.setId(Integer.parseInt(atts.getValue(ID)));
                } catch (Throwable t) {
                    plan.landingpoint.setId(ReentryPoint.INVALID_REENTRYPOINT);
                }

                try {
                    plan.landingpoint.setLandAutomatically(Boolean.parseBoolean(atts.getValue(IS_AUTO)));
                } catch (Throwable t) {
                }
            } else if (qName.equals(CAMERA_DESCRIPTION)) {
                String cameraId = atts.getValue(CAMERA_ID);
                String slotIdx = atts.getValue(SLOT_IDX);
                String payloadIdx = atts.getValue(PAYLOAD_IDX);
                String lensId = atts.getValue(LENS_ID);
                try {
                    IGenericCameraConfiguration camera =
                        hardwareConfigurationManager.getCameraConfiguration(cameraId, lensId);
                    int slotI = Integer.parseInt(slotIdx);
                    int payloadI = Integer.parseInt(payloadIdx);
                    if (slotI == 0) {
                        tempHardwareConfig.setPrimaryPayload(camera);
                    } else {
                        tempHardwareConfig.getPayloadMount(slotI).setPayload(payloadI, camera);
                    }

                    // that sounds very dangerous but hardwareConfiguration in the mission has to be initialized
                    // before picAreas...
                    // so again everything is very stateful
                    plan.hardwareConfiguration.initializeFrom(tempHardwareConfig);

                } catch (Exception e) {
                    Debug.getLog()
                        .log(
                            Level.WARNING,
                            "could not parse payload description while reading the mission FML. cameraId:"
                                + cameraId
                                + " slotIdx:"
                                + slotIdx
                                + " payloadIdx:"
                                + payloadIdx
                                + " lensId:"
                                + lensId,
                            e);
                }
            } else if (qName.equals(NAME)) {
                try {
                    plan.name = atts.getValue(NAME);

                } catch (Throwable t) {
                }
            } else if (qName.equals(NOTES)) {
                try {
                    plan.notes = atts.getValue(NOTES);

                } catch (Throwable t) {
                }
            } else if (qName.equals(PLATFORM_DESCRIPTION)) {
                try {
                    String id = atts.getValue(PLATFORM_ID);
                    tempHardwareConfig.setPlatformDescription(hardwareConfigurationManager.getPlatformDescription(id));
                } catch (Throwable t) {
                    Debug.getLog().log(Level.WARNING, "error loading platform HW description", t);
                }
            } else if (qName.equals(BASED_ON_TEMPLATE)) {
                plan.basedOnTemplate = atts.getValue(NAME);
            } else if (qName.equals(RECALCULATE_ON_EVERY_CHANGE)) {
                plan.recalculateOnEveryChange = Boolean.parseBoolean(atts.getValue(NAME));
            } else if (qName.equals(ENABLE_JUMP_OVER_WAYPOINTS)) {
                plan.enableJumpOverWaypoints = Boolean.parseBoolean(atts.getValue(NAME));
            } else if (qName.equals(OBSTACLE_AVOIDANCE)) {
                plan.obstacleAvoidanceEnabled = Boolean.parseBoolean(atts.getValue(NAME));
            } else if (qName.equals(PICAREA_TEMPLATES)) {
                plan.picAreaTemplates = new ArrayList<>();
            } else if (qName.equals(PIC_AREA)) {
                IFlightplanContainer parent = plan;
                CPicArea area = handlePicArea(atts, parent);

                plan.picAreaTemplates.add(area);
                outerContainer.push(area);
            } else if (qName.equals(PHOTOSETTINGS)) {
                plan.photoSettings.maxRoll = Double.parseDouble(atts.getValue(MAX_ROLL));
                plan.photoSettings.maxNick = Double.parseDouble(atts.getValue(MAX_NICK));
                plan.photoSettings.mintimeinterval = Double.parseDouble(atts.getValue(MIN_TIME_INTERVAL));
                try {
                    plan.photoSettings.altAdjustMode = AltitudeAdjustModes.valueOf(atts.getValue(ALTITUDE_ADJUST_MODE));
                } catch (Throwable t) {
                }

                try {
                    plan.photoSettings.isMultiFP = Boolean.parseBoolean(atts.getValue(IS_MULTI_FP));
                } catch (Throwable t) {
                }

                try {
                    plan.photoSettings.maxGroundSpeedMPSec =
                        Double.parseDouble(atts.getValue(MAX_GROUND_SPEED_KMH)) / 3.6;
                } catch (Throwable t) {
                }

                try {
                    plan.photoSettings.maxGroundSpeedAutomatic =
                        FlightplanSpeedModes.valueOf(atts.getValue(MAX_GROUND_SPEED_AUTOMATIC));
                } catch (Throwable t) {
                }

                try {
                    plan.photoSettings.stoppingAtWaypoints = Boolean.parseBoolean(atts.getValue(STOP_AT_WAYPOINTS));
                } catch (Throwable t) {
                }

                try {
                    plan.photoSettings.gsdTolerance = Double.parseDouble(atts.getValue(GSD_TOLERANCE));
                } catch (Throwable t) {
                }
            } else if (qName.equals(LEARNINGMODE)) {
                plan.isLearningmode = true;
            } else if (qName.equals(EVENT_ACTIONS)) {
                CEventList eventList = plan.eventList;
                while (eventList.sizeOfFlightplanContainer() > 0) {
                    eventList.removeFromFlightplanContainer(0);
                }

                try {
                    eventList.setAltWithinCM(Integer.parseInt(atts.getValue(SAFETY_ALT)));
                } catch (Exception e) {
                }

                try {
                    eventList.setAutoComputeSafetyHeight(Boolean.parseBoolean(atts.getValue(IS_AUTO)));
                } catch (Exception e) {
                }

                outerContainer.push(eventList);
            } else if (qName.equals(EVENT)) {
                CEventList eventList = plan.eventList;
                CEvent event = FlightplanFactory.getFactory().newCEvent(eventList, atts.getValue(NAME));
                event.delay = Integer.parseInt(atts.getValue(DELAY));
                try {
                    event.hasLevel = true;
                    event.level = Integer.parseInt(atts.getValue(LEVEL));
                } catch (Throwable t) {
                    event.hasLevel = false;
                }

                event.recover = Integer.parseInt(atts.getValue(RECOVER)) == 1;
                event.action = AirplaneEventActions.values()[Integer.parseInt(atts.getValue(ACTION))];
                try {
                    eventList.addToFlightplanContainer(event);
                } catch (Exception e) {
                    throw new SAXException(e);
                }
            } else if (qName.equals(START_ALT)) {
                // these values are stored now inside the origin class

                /*plan.startAltWgs84 = Double.parseDouble(atts.getValue(ALT));
                try {
                    plan.startGeoidSep = Double.parseDouble(atts.getValue(GEOID_SEP));
                } catch (Exception e) {
                    plan.startGeoidSep = 0;
                }*/
            }
        }

        if (inBody) {
            if (qName.equals(WAYPOINT)) {
                int radius = 0;
                String tmp = atts.getValue(RADIUS);
                if (tmp != null) {
                    radius = Integer.parseInt(tmp);
                }

                try {
                    CWaypoint wp =
                        FlightplanFactory.getFactory()
                            .newCWaypoint(
                                Double.parseDouble(atts.getValue(LON)),
                                Double.parseDouble(atts.getValue(LAT)),
                                Integer.parseInt(atts.getValue(ALT)),
                                AltAssertModes.parse(atts.getValue(ASSERTALTITUDE)),
                                radius,
                                "",
                                Integer.parseInt(atts.getValue(ID)),
                                outerContainer.peek());
                    lastBodyParentNode = wp;
                    try {
                        wp.setCamYaw(Double.parseDouble(atts.getValue(CAM_YAW)));
                    } catch (Exception e) {
                    }

                    try {
                        wp.setCamPitch(Double.parseDouble(atts.getValue(CAM_PITCH)));
                    } catch (Exception e) {
                    }

                    try {
                        wp.setCamRoll(Double.parseDouble(atts.getValue(CAM_ROLL)));
                    } catch (Exception e) {
                    }

                    try {
                        wp.ignore = Boolean.parseBoolean(atts.getValue(IGNORE));
                    } catch (Exception e) {
                    }

                    try {
                        wp.stopHereTimeCopter = Double.parseDouble(atts.getValue(STOP_AT_WAYPOINTS));
                    } catch (Exception e) {
                    }

                    try {
                        wp.isBeginFlightline = Boolean.parseBoolean(atts.getValue(BEGING_FLIGHTLINE));
                    } catch (Exception e) {
                    }

                    try {
                        wp.triggerImageHereCopterMode = Boolean.parseBoolean(atts.getValue(PHOTO));
                    } catch (Exception e) {
                    }

                    try {
                        wp.targetDistance = Double.parseDouble(atts.getValue(TARGET_DISTANCE));
                    } catch (Exception e) {
                    }

                    try {
                        if (atts.getValue(ASSERT_YAW) != null) {
                            wp.assertYaw = Double.parseDouble(atts.getValue(ASSERT_YAW));
                            wp.assertYawOn = true;
                        }
                    } catch (Exception e) {
                    }

                    try {
                        wp.speedMode = SpeedMode.valueOf(atts.getValue(SPEED_MODE));
                    } catch (Exception e) {
                    }

                    try {
                        wp.speedMpSec = Double.parseDouble(atts.getValue(SPEED_MPS));
                    } catch (Exception e) {
                    }

                    outerContainer.peek().addToFlightplanContainer(wp);
                } catch (Exception e) {
                    throw new SAXException(e);
                }

                bodyToLoad = true;
            } else if (qName.equals(DUMP)) {
                try {
                    lastDump = FlightplanFactory.getFactory().newCDump(CDump.DEFAULT_BODY);
                    outerContainer.peek().addToFlightplanContainer(lastDump);
                } catch (Exception e) {
                    throw new SAXException(e);
                }
            } else if (qName.equals(PHOTO)) {
                boolean power = false;
                boolean isWPmode = false;
                if (ON.equals(atts.getValue(POWER))) {
                    power = true;
                }

                double distance = Double.parseDouble(atts.getValue(DISTANCE));
                double distanceMax = Double.parseDouble(atts.getValue(DISTANCE_MAX));
                if (Boolean.parseBoolean(atts.getValue(PHOTO_WPmode))) {
                    isWPmode = true;
                }

                try {
                    CPhoto photo =
                        FlightplanFactory.getFactory()
                            .newCPhoto(
                                power,
                                distance,
                                distanceMax,
                                Integer.parseInt(atts.getValue(ID)),
                                outerContainer.peek());
                    photo.triggerOnlyOnWaypoints = isWPmode;
                    outerContainer.peek().addToFlightplanContainer(photo);
                } catch (Exception e) {
                    throw new SAXException(e);
                }
            } else if (qName.equals(LOOP)) {
                CWaypointLoop loop =
                    FlightplanFactory.getFactory()
                        .newCWaypointLoop(
                            Integer.parseInt(atts.getValue(COUNTER)),
                            Integer.parseInt(atts.getValue(TIME)),
                            Integer.parseInt(atts.getValue(ID)),
                            outerContainer.peek());
                lastBodyParentNode = loop;
                try {
                    loop.ignore = Boolean.parseBoolean(atts.getValue(IGNORE));
                } catch (Exception e) {
                }

                try {
                    outerContainer.peek().addToFlightplanContainer(loop);
                } catch (Exception e) {
                    throw new SAXException(e);
                }

                outerContainer.push(loop);
            } else if (qName.equals(PIC_AREA)) {
                IFlightplanContainer parent = outerContainer.peek();
                CPicArea area = handlePicArea(atts, parent);
                area.setMute(true);
                try {
                    outerContainer.peek().addToFlightplanContainer(area);
                } catch (Exception e) {
                    throw new SAXException(e);
                }

                outerContainer.push(area);
            } else if (qName.equals(CORNERS)) {
                CPicAreaCorners corners =
                    FlightplanFactory.getFactory().newPicAreaCorners((CPicArea)outerContainer.peek());
                CPicArea area = (CPicArea)outerContainer.peek();
                area.corners = corners;
                corners.setMute(true);
                corners.setParent(area);
                outerContainer.push(corners);
            } else if (qName.equals(SWAP)) {
                try {
                    CPicArea.ModelAxis first = CPicArea.ModelAxis.valueOf(atts.getValue(FIRST));
                    CPicArea.ModelAxis second = CPicArea.ModelAxis.valueOf(atts.getValue(SECOND));
                    CPicArea area = (CPicArea)outerContainer.peek();
                    area.modelAddSwap(first, second);
                } catch (Throwable t) {
                }
            } else if (qName.equals(POINT)) {
                // TODO Jan
                try {
                    double lat = Double.parseDouble(atts.getValue(LAT));
                    double lon = Double.parseDouble(atts.getValue(LON));
                    var p = new Point(outerContainer.peek(), lat, lon);
                    try {
                        p.setPitch(Double.parseDouble(atts.getValue(CAM_PITCH)));
                        p.setYaw(Double.parseDouble(atts.getValue(CAM_YAW)));
                        p.setAltitude(Double.parseDouble(atts.getValue(ALT)));
                        p.setTriggerImage(Boolean.parseBoolean(atts.getValue(TRIGGER_IMAGE)));
                        p.setNote(atts.getValue(NOTES));
                        p.setTarget(Boolean.parseBoolean(atts.getValue(TARGET)));

                        p.setGsdMeter(Double.parseDouble(atts.getValue(GSD)));
                        p.setDistanceMeter(Double.parseDouble(atts.getValue(DISTANCE)));
                        p.setFrameDiagonaleMeter(Double.parseDouble(atts.getValue(FRAME_DIAG)));

                        // this line has to be last loading, since it enables automatic computation
                        var distanceSource =
                            Point.DistanceSource.valueOf(atts.getValue(DISTANCE_SOURCE).toUpperCase(Locale.ENGLISH));
                        p.setDistanceSource(distanceSource);
                    } catch (Exception e) {
                        Debug.getLog().log(Level.WARNING, "parsing failed", e);
                    }

                    outerContainer.peek().addToFlightplanContainer(p);
                } catch (Exception e) {
                    throw new SAXException(e);
                }
            }
        }
    }

    private void initRefPoint(CPicArea area) {
        var fp = area.getFlightplan();
        Ensure.notNull(fp);

        ReferencePoint point = area.getFlightplan().getRefPoint();
        if (!point.isDefined()) {
            // default first corner in case if refPointOption is null
            // for legacy flightplans for example

            Point tmp = fp.getFirstElement(Point.class);
            if (tmp != null) {
                point.setPosition(new Position(tmp.getLatLon().getLatitude(), tmp.getLatLon().getLongitude(), 0));
            }
        }

        Takeoff takeoff = fp.getTakeoff();
        if (!takeoff.isDefined()) {
            LatLon latlon = ((PicArea)area).getCenterShiftedInOtherDirection();
            takeoff.setPosition(new Position(latlon.getLatitude(), latlon.getLongitude(), 0));
        }

        point = area.getModelReferencePoint();
        if (!point.isDefined()) {
            area.updateModelReferencePoint();
        }
    }

    private CPicArea handlePicArea(Attributes atts, IFlightplanContainer parent) throws SAXException {
        double gsd = Double.parseDouble(atts.getValue(GSD));
        double overlapInFlight = Double.parseDouble(atts.getValue(OVERLAP_IN_FLIGHT));
        double overlapInFlightMin = Double.parseDouble(atts.getValue(OVERLAP_IN_FLIGHT_MIN));
        double overlapParallel = Double.parseDouble(atts.getValue(OVERLAP_PARALLEL));
        CPicArea area =
            FlightplanFactory.getFactory()
                .newPicArea(
                    Integer.parseInt(atts.getValue(ID)),
                    parent,
                    gsd,
                    overlapInFlight,
                    overlapInFlightMin,
                    overlapParallel);
        area.setMute(true);

        setDouble(atts, GSD, area::setGsd);
        set(atts, PLAN_TYPE, area::setPlanType, PlanType::valueOf);
        setInt(atts, CORRIDOR_MIN_LINES, area::setCorridorMinLines);
        setDouble(atts, CORRIDOR_WIDTH, area::setCorridorWidthInMeter);
        area.setName(atts.getValue(NAME));
        area.setModelFilePath(atts.getValue(MODEL_FILE_PATH));
        setDouble(atts, MODEL_SCALE, area::setModelScale);
        setDouble(atts, MIN_GROUND_DISTANCE, area::setMinGroundDistance);
        setDouble(atts, MIN_OBJECT_DISTANCE, area::setMinObjectDistance);
        setDouble(atts, MAX_OBJECT_DISTANCE, area::setMaxObjectDistance);
        set(atts, FACADE_SCANNING_SIDE, area::setFacadeScanningSide, CPicArea.FacadeScanningSide::valueOf);
        setBool(atts, ENABLE_CROP_HEIGHT_MAX, area::setEnableCropHeightMax);
        setBool(atts, ENABLE_CROP_HEIGHT_MIN, area::setEnableCropHeightMin);

        // next 4 lines (object heights and add ceiling) have to be in a special order, to not cause wrong loading
        setDouble(atts, OBJECT_HEIGHT, area::setObjectHeight);
        setDouble(atts, OBJECT_HEIGHT_MAX, area::setCropHeightMax);
        setDouble(atts, OBJECT_HEIGHT_MIN, area::setCropHeightMin);
        setBool(atts, ADD_CEILING, area::setAddCeiling);

        setBool(atts, CIRCLE_LEFT_TRUE_RIGHT_FALSE, area::setCircleLeftTrueRightFalse);
        setInt(atts, CIRCLE_COUNT, area::setCircleCount);
        set(atts, SCAN_DIRECTION, area::setScanDirection, CPicArea.ScanDirectionsTypes::valueOf);
        set(atts, START_CAPTURE, area::setStartCapture, CPicArea.StartCaptureTypes::valueOf);
        set(atts, JUMP_PATTERN, area::setJumpPattern, CPicArea.JumpPatternTypes::valueOf);
        set(atts, VERTICAL_SCAN_PATTERN, area::setVerticalScanPattern, CPicArea.VerticalScanPatternTypes::valueOf);
        set(
            atts,
            START_CAPTURE_VERTICALLY,
            area::setStartCaptureVertically,
            CPicArea.StartCaptureVerticallyTypes::valueOf);
        setBool(atts, ONLY_SINGLE_DIRECTION, area::setOnlySingleDirection);
        setDouble(atts, ONLY_SINGLE_DIRECTION_ANGLE, area::setYaw);
        setBool(atts, CAMERA_TILT_TOGGLE_ENABLE, area::setCameraTiltToggleEnable);
        setDouble(atts, CAMERA_TILT_TOGGLE_DEGREES, area::setCameraTiltToggleDegrees);
        setDouble(atts, CAMERA_PITCH_OFFSET_DEGREES, area::setCameraPitchOffsetDegrees);
        setBool(atts, CAMERA_ROLL_TOGGLE_ENABLE, area::setCameraRollToggleEnable);
        setDouble(atts, CAMERA_ROLL_TOGGLE_DEGREES, area::setCameraRollToggleDegrees);
        setDouble(atts, CAMERA_ROLL_OFFSET_DEGREES, area::setCameraRollOffsetDegrees);
        setDouble(atts, MAX_PITCH_CHANGE, area::setMaxPitchChange);
        setDouble(atts, MAX_YAW_ROLL_CHANGE, area::setMaxYawRollChange);

        setDouble(atts, RESTRICTION_CEILING, area::setRestrictionCeiling);
        setDouble(atts, RESTRICTION_FLOOR, area::setRestrictionFloor);
        setBool(atts, RESTRICTION_CEILING_ENABLED, area::setRestrictionCeilingEnabled);
        setBool(atts, RESTRICTION_FLOOR_ENABLED, area::setRestrictionFloorEnabled);
        set(
            atts,
            RESTRICTION_CEILING_REF,
            area::setRestrictionCeilingRef,
            CPicArea.RestrictedAreaHeightReferenceTypes::valueOf);
        set(
            atts,
            RESTRICTION_FLOOR_REF,
            area::setRestrictionFloorRef,
            CPicArea.RestrictedAreaHeightReferenceTypes::valueOf);

        set(atts, MODEL_SOURCE_TYPE, area::setModelSource, CPicArea.ModelSourceTypes::valueOf);
        set(atts, MODEL_AXIS_ALIGNMENT_X, area::setModelAxisAlignmentX, CPicArea.ModelAxisAlignment::valueOf);
        set(atts, MODEL_AXIS_ALIGNMENT_Y, area::setModelAxisAlignmentY, CPicArea.ModelAxisAlignment::valueOf);
        set(atts, MODEL_AXIS_ALIGNMENT_Z, area::setModelAxisAlignmentZ, CPicArea.ModelAxisAlignment::valueOf);
        setDouble(atts, MODEL_AXIS_OFFSET_X, area::setModelAxisOffsetX);
        setDouble(atts, MODEL_AXIS_OFFSET_Y, area::setModelAxisOffsetY);
        setDouble(atts, MODEL_AXIS_OFFSET_Z, area::setModelAxisOffsetZ);

        setDouble(atts, PITCH_OFFSET_LINE_BEGIN, area::setPitchOffsetLineBegin);

        setDouble(atts, HUB_DIAMETER, area::setWindmillHubDiameter);
        setDouble(atts, HUB_LENGTH, area::setWindmillHubLength);
        setInt(atts, NUMBER_OF_BLADES, area::setWindmillNumberOfBlades);
        setDouble(atts, BLADE_LENGTH, area::setWindmillBladeLength);
        setDouble(atts, BLADE_DIAMETER, area::setWindmillBladeDiameter);
        setDouble(atts, BLADE_THIN_RADIUS, area::setWindmillBladeThinRadius);
        setDouble(atts, BLADE_PITCH, area::setWindmillBladePitch);
        setDouble(atts, BLADE_START_ROTATION, area::setWindmillBladeStartRotation);
        setDouble(atts, BLADE_COVER_LENGTH, area::setWindmillBladeCoverLength);

        return area;
    }

    void setInt(Attributes atts, String key, Consumer<Integer> consumer) {
        set(atts, key, consumer, Integer::parseInt);
    }

    void setDouble(Attributes atts, String key, Consumer<Double> consumer) {
        set(atts, key, consumer, Double::parseDouble);
    }

    void setBool(Attributes atts, String key, Consumer<Boolean> consumer) {
        set(atts, key, consumer, Boolean::parseBoolean);
    }

    <T> void set(Attributes atts, String key, Consumer<T> consumer, Function<String, T> parser) {
        String val = null;
        try {
            val = atts.getValue(key);
            if (val != null) {
                consumer.accept(parser.apply(val));
            }
        } catch (Exception e) {
            Debug.getLog().log(Level.FINE, "trying to parse key:" + key + " val:" + val + " failed", e);
        }
    }

    @Override
    public void endElement(String namespaceURI, String localName, String qName) {
        // System.out.println("namespaceURI:"+namespaceURI+" localName:"+localName+" qName:"+qName);
        qName = localName;

        if (qName.equals(WAYPOINT)) {
            if (lastBodyParentNode != null && bodyToLoad) {
                lastBodyParentNode.setBody(sbuf.toString());
            }
            // legacy adapter - if a flightplan had jumpOver waypoints then set a flag tot true
            if (lastBodyParentNode.equals(JUMP_OVER)) {
                plan.enableJumpOverWaypoints = true;
            }

            bodyToLoad = false;
            lastBodyParentNode = null;
        } else if (qName.equals(DUMP)) {
            if (lastDump != null) {
                lastDump.setBody(sbuf.toString());
            }
        } else if (qName.equals(LOOP)) {
            outerContainer.pop();
        } else if (qName.equals(CORNERS)) {
            // Do not process PICAREA_TEMPLATES which are in Head
            if (inBody) {
                outerContainer.pop();
            }
        } else if (qName.equals(ROW)) {
            outerContainer.pop();
        } else if (qName.equals(EVENT_ACTIONS)) {
            outerContainer.pop();
        } else if (qName.equals(PIC_AREA)) {
            CPicArea area = (CPicArea)outerContainer.pop();
            area.getCorners().setMute(false);
            area.setMute(false);
        } else if (qName.equals(BODY)) {
            inBody = false;
            inHead = false;
        } else if (qName.equals(HEAD)) {
            inBody = false;
            inHead = false;
            plan.eventList.fixEvents();
        } else if (qName.equals(WBODY)) {
            if (lastBodyParentNode != null) {
                lastBodyParentNode.setBody(sbuf.toString());
            }

            bodyToLoad = false;
        }
        // lastWaypoint = null;
        lastDump = null;
    }

    public CFlightplan getFlightplan() {
        if (plan.getLandingpoint().getId() == ReentryPoint.INVALID_REENTRYPOINT) {
            plan.getLandingpoint().reassignIDs();
        }

        return plan;
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        sbuf.append(ch, start, length);
    }
}
