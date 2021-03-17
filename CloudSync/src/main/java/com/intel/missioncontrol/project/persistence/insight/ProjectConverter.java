/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.persistence.insight;

import com.google.common.collect.BiMap;
import com.intel.missioncontrol.project.DatasetSnapshot;
import com.intel.missioncontrol.project.FlightPlanSnapshot;
import com.intel.missioncontrol.project.GoalSnapshot;
import com.intel.missioncontrol.project.IGoal;
import com.intel.missioncontrol.project.MissionSnapshot;
import com.intel.missioncontrol.project.ProjectSnapshot;
import com.intel.missioncontrol.project.RepositoryType;
import com.intel.missioncontrol.project.UserSnapshot;
import com.intel.missioncontrol.project.insight.Annotation;
import com.intel.missioncontrol.project.insight.Flight;
import com.intel.missioncontrol.project.insight.FlightPlan;
import com.intel.missioncontrol.project.insight.Mission;
import com.intel.missioncontrol.project.insight.ModificationUser;
import com.intel.missioncontrol.project.insight.Project;
import com.intel.missioncontrol.project.insight.RealBbox;
import com.intel.missioncontrol.project.insight.User;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec4;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProjectConverter {
    //private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectConverter.class);
    public static final String EPSG_4326 = "EPSG:4326";
    // date format matching insight's definition
    //private final JsonParser parser = new JsonParser();
    //private final IHardwareConfigurationManager hwConfigManager;
    //private final HWConfigurationSerializer hwConfigurationSerializer;
    private final BiMap<UUID, String> uuidInsightIdMap;
    private final BiMap<String, UUID> insightIdUuidMap;
    private final Map<String, Integer> insightIdVersionMap;

    public ProjectConverter(BiMap<UUID, String> uuidInsightIdMap, Map<String, Integer> insightIdVersionMap) {
        this.uuidInsightIdMap = uuidInsightIdMap;
        this.insightIdUuidMap = uuidInsightIdMap.inverse();
        this.insightIdVersionMap = insightIdVersionMap;
    }

    public ProjectSnapshot convertProjectFromInsight(
            Project insightProject) {
        UUID imcId = insightIdUuidMap.get(insightProject.getId());
        if(imcId == null){
            imcId = UUID.randomUUID();
            insightIdUuidMap.put(insightProject.getId(), imcId);
        }
        ProjectSnapshot imcProject =
                new ProjectSnapshot(
                        imcId,
                        insightProject.getName(),
                        RepositoryType.INSIGHT,
                        ZonedDateTime.parse(insightProject.getCreated()).toOffsetDateTime(),
                        ZonedDateTime.parse(insightProject.getModificationDate()).toOffsetDateTime(),
                        fromInsightObject(insightProject.getUser()),
                        insightProject.getEpsg(),
                        new ArrayList<>(),
                        new ArrayList<>(),
                        new ArrayList<>());

        var list = insightProject.getMissionList();
        for (int i = 0; i < list.size(); i++) {
            var mission = list.get(i);
            var m = fromInsightObject(mission);
            imcProject.getMissions().add(m);
            var fpList = mission.getFlightPlanList();
            for (int j = 0; j < fpList.size(); j++) {
                var fp = fromInsightObject(fpList.get(j));
                m.getFlightPlans().add(fp);
            }

            var aoisList = mission.getAnnotationList();
            for (int j = 0; j < aoisList.size(); j++) {
                GoalSnapshot goal = fromInsightObject(aoisList.get(j));
                m.getGoals().add(goal);
            }
        }

        for (var flight : insightProject.getFlightList()) {
            imcProject.getFlights().add(fromInsightObject(flight));
        }

        return imcProject;
    }

    private FlightPlanSnapshot fromInsightObject(FlightPlan insightFlightPlan) {
        UUID imcId = insightIdUuidMap.get(insightFlightPlan.getId());

        if(imcId == null){
            imcId = UUID.randomUUID();
            insightIdUuidMap.put(insightFlightPlan.getId(), imcId);
        }
        return null;
    }

    private GoalSnapshot fromInsightObject(Annotation annotation) {
        UUID imcId = insightIdUuidMap.get(annotation.getId());

        if(imcId == null){
            imcId = UUID.randomUUID();
            insightIdUuidMap.put(annotation.getId(), imcId);
        }
        return null;
    }

    private DatasetSnapshot fromInsightObject(Flight flight) {
        UUID imcId = insightIdUuidMap.get(flight.getId());

        if(imcId == null){
            imcId = UUID.randomUUID();
            insightIdUuidMap.put(flight.getId(), imcId);
        }
        return null;
    }

    public Project toInsightProject(
            ProjectSnapshot imcProject) {
        var project = new Project();
        String insightId = uuidInsightIdMap.get(imcProject.getId());
        project.setId(insightId);
        User user = toInsightObject(imcProject.getUser());
        project.setUser(user);
       /* if (imcProject.getSector() != null) {
            var box = new RealBbox();
            var coords =
                    Arrays.stream(imcProjectInfo.getSector().asDegreesArray()).boxed().collect(Collectors.toList());

            box.setType("Polygon");
            box.setBbox(coords);
            project.setRealBbox(box);
        }*/

        project.setName(imcProject.getName());
        project.setV(insightIdVersionMap.get(insightId));
        //project.setCompanyId(imcProjectInfo.getCompanyId());
        //project.setIndustry(imcProjectInfo.getIndustry());
        if (imcProject.getSrsEpsg() != null) {
            project.setEpsg(imcProject.getSrsEpsg());
        } else {
            project.setEpsg(EPSG_4326); // srsManager.getDefault().id);
        }

        return project;
    }

    private ModificationUser toModUser(UserSnapshot user) {
        var modUser = new ModificationUser();
        modUser.setDisplayName(user.getName());
        String insightId = uuidInsightIdMap.get(user.getId());
        modUser.setId(insightId);
        return modUser;
    }

    private User toInsightObject(UserSnapshot user) {
        var insightUser = new User();
        insightUser.setDisplayName(user.getName());
        String insightId = uuidInsightIdMap.get(user.getId());
        insightUser.setId(insightId);
        return insightUser;
    }

    private Sector fromInsightObject(RealBbox realBbox) {
        if (realBbox != null) {
            // Insight! minlon minlat maxlon maxlat
            var bbox = realBbox.getBbox();
            if (bbox.size() == 4) {
                // IMC minLat maxLat minLon maxLon
                return new Sector(Sector.fromDegrees(bbox.get(1), bbox.get(3), bbox.get(0), bbox.get(2)));
            }
        }

        return null;
    }

    private UserSnapshot fromInsightObject(User user) {
        if (user == null) {
            return null;
        }
        UUID uuid = insightIdUuidMap.get(user.getId());
        if(uuid == null){
            uuid = UUID.randomUUID();
            insightIdUuidMap.put(user.getId(), uuid);
        }
        var imcUser = new UserSnapshot(uuid, user.getDisplayName());
        return imcUser;

    }

    protected UserSnapshot fromInsightObject(ModificationUser user) {
        if (user == null) {
            return null;
        }
        UUID uuid = insightIdUuidMap.get(user.getId());
        if(uuid == null){
            uuid = UUID.randomUUID();
            insightIdUuidMap.put(user.getId(), uuid);
        }
        var imcUser = new UserSnapshot(uuid, user.getDisplayName());
        return imcUser;
    }


    public Mission toInsightObject(MissionSnapshot imcMission) {
        var mission = new Mission();
        String insightId = uuidInsightIdMap.get(imcMission.getId());
        mission.setId(insightId);
        mission.setName(imcMission.getName());
        mission.setV(insightIdVersionMap.get(insightId));
        //mission.setTerrainAdjustmentMode(imcMission.getAltitudeAdjustModes().name());
        double originLat = imcMission.getOriginLat();
        double originLon = imcMission.getOriginLon();
        mission.setReferencePoint(
                new ArrayList<>() {
                    {
                        add(originLat);
                        add(originLon);
                    }
                });
         /*mission.setHwConfig(
                hwConfigurationSerializer
                        .serialize(imcMission.getHardwareConfiguration(), IHardwareConfiguration.class, null)
                        .toString());
        mission.setCameras(imcMission.getCameraIds());*/
        return mission;
    }

    /*public Camera toInsightObject(IHardwareConfiguration hwConfig) {
        IGenericCameraConfiguration cameraConfig =
                hwConfig.getPrimaryPayload(com.intel.missioncontrol.hardware.IGenericCameraConfiguration.class);
        final IGenericCameraDescription cameraDesc = cameraConfig.getDescription();
        final ILensDescription lensDescription = cameraConfig.getLens().getDescription();

        var camera = new Camera();
        camera.setFocalLength(lensDescription.getFocalLength().convertTo(Unit.MILLIMETER).getValue().intValue());
        var resX = cameraDesc.getCcdResX();
        var resY = cameraDesc.getCcdResY();
        var w = cameraDesc.getCcdWidth().convertTo(Unit.MILLIMETER).getValue().doubleValue();
        var h = cameraDesc.getCcdHeight().convertTo(Unit.MILLIMETER).getValue().doubleValue();
        camera.setHeight(resX);
        camera.setWidth(resY);
        camera.setAspectRatio(w / h);
        camera.setModel(cameraDesc.getName());

        return camera;
    }
*/
    public MissionSnapshot fromInsightObject(Mission mission) {
       /* IHardwareConfiguration hardwareConfiguration = null;

        if (mission.getHwConfig() != null) {
            try {
                hardwareConfiguration =
                        hwConfigurationSerializer.deserialize(
                                parser.parse(mission.getHwConfig()).getAsJsonObject(), IHardwareConfiguration.class, null);
            } catch (Exception e) {
                // parse error
            }
        }

        if (hardwareConfiguration == null) {
            hardwareConfiguration = hwConfigManager.getImmutableDefault();
        }
*/
        var ref = mission.getReferencePoint();
        Vec4 point;
        if (ref != null && ref.size() == 2) {
            point = new Vec4(ref.get(0), ref.get(1));
        } else {
            point = new Vec4(0, 0);
        }

        UUID imcId = insightIdUuidMap.get(mission.getId());
        MissionSnapshot
                imcMission =
                new MissionSnapshot(
                        imcId,
                        mission.getName(),
                        point.x,
                        point.y,
                        new ArrayList<>(),
                        new ArrayList<>(),
                        0.0, 0.0, true, true);

        return imcMission;
    }

    public Annotation toInsightObject(IGoal goal) {
        return null;
    }

    public FlightPlan toInsightObject(FlightPlanSnapshot fp) {
        return null;
    }
/*
    public FlightPlan toInsightObject(FlightPlanSnapshot imcFlightPlan) {
        var fp = new FlightPlan();
        fp.setId(imcFlightPlan.getId());
        fp.setName(imcFlightPlan.getName());
        fp.setV(imcFlightPlan.getVersion());
        fp.setType("SURVEY");
        fp.setTakeoff(toInsightObject(imcFlightPlan.getTakeOffPoint()));
        fp.setLanding(toInsightObject(imcFlightPlan.getLandingPoint()));
        fp.setHwConfig(
                hwConfigurationSerializer
                        .serialize(imcFlightPlan.getHardwareConfiguration(), IHardwareConfiguration.class, null)
                        .toString());
        fp.setCamera(toInsightObject(imcFlightPlan.getHardwareConfiguration()));
        fp.setSafetyAltitude(imcFlightPlan.getSafetyAltitude());
        fp.setMaxGroundSpeed(imcFlightPlan.getMaxGroundSpeed());

        // TODO naming ?
        fp.setSafetyProcedures(imcFlightPlan.getEmergencyProcedures());

        fp.getWaypoints()
                .addAll(
                        imcFlightPlan
                                .getWaypoints()
                                .stream()
                                .map(waypoint -> toInsightObject(waypoint))
                                .collect(Collectors.toList()));

        return fp;
    }

    public FlightPlanSnapshot fromInsightObject(FlightPlan flightPlan) {
        IHardwareConfiguration hardwareConfiguration = null;

        if (flightPlan.getHwConfig() != null) {
            try {
                hardwareConfiguration =
                        hwConfigurationSerializer.deserialize(
                                parser.parse(flightPlan.getHwConfig()).getAsJsonObject(), IHardwareConfiguration.class, null);
            } catch (Exception e) {
                // parse error
            }
        }

        if (hardwareConfiguration == null) {
            hardwareConfiguration = hwConfigManager.getImmutableDefault();
        }

        FlightPlanSnapshot imcFlightPlan = null;
        try {
            imcFlightPlan =
                    new FlightPlanSnapshot(
                            flightPlan.getId(),
                            flightPlan.getV(),
                            flightPlan.getModified() != null ? dateFormat.parse(flightPlan.getModified()) : new Date(0),
                            flightPlan.getCreated() != null ? dateFormat.parse(flightPlan.getCreated()) : new Date(0),
                            flightPlan.getName(),
                            hardwareConfiguration,
                            fromInsightObject(flightPlan.getTakeoff()),
                            fromInsightObject(flightPlan.getLanding()),
                            LandingModes.LAND_AT_TAKEOFF, // TODO
                            0.0, // TODO
                            0.0, // TODO
                            0.0, // TODO
                            false, // TODO
                            false, // TODO
                            null, // TODO
                            flightPlan.getMaxGroundSpeed() != null ? flightPlan.getMaxGroundSpeed() : 0.0, // TODO
                            flightPlan.getSafetyAltitude() != null ? flightPlan.getSafetyAltitude() : 0.0, // TODO
                            AirplaneEventActions.returnToStart, // TODO
                            Duration.ZERO, // TODO
                            AirplaneEventActions.returnToStart, // TODO
                            Duration.ZERO, // TODO
                            AirplaneEventActions.returnToStart, // TODO
                            AirplaneEventActions.returnToStart, // TODO
                            Duration.ZERO); // TODO
        } catch (ParseException e) {
            e.printStackTrace();
        }

        imcFlightPlan
                .getWaypoints()
                .addAll(
                        flightPlan
                                .getWaypoints()
                                .stream()
                                .map(waypoint -> fromInsightObject(waypoint))
                                .collect(Collectors.toList()));
        return imcFlightPlan;
    }

    public Waypoint toInsightObject(WaypointSnapshot wp) {
        var waypoint = new Waypoint();
        waypoint.setTargetPoint(toInsightObject(wp.getTargetPoint()));
        waypoint.setDronePoint(toInsightObject(wp.getDronePoint()));
        waypoint.setWarning(wp.getWarning());
        return waypoint;
    }

    public WaypointSnapshot fromInsightObject(Waypoint waypoint) {
        var wp =
                new WaypointSnapshot(
                        fromInsightObject(waypoint.getTargetPoint()),
                        fromInsightObject(waypoint.getDronePoint()),
                        waypoint.getWarning(),
                        false, // TODO
                        false, // TODO
                        0, // TODO
                        0.0, // TODO
                        0.0, // TODO
                        AltAssertModes.linear, // TODO
                        0.0, // TODO
                        "", // TODO
                        waypoint.getRoll(), // TODO
                        waypoint.getPitch(), // TODO
                        waypoint.getYaw(), // TODO
                        false, // TODO
                        false, // TODO
                        0.0, // TODO
                        SpeedMode.normal, // TODO
                        waypoint.getSpeed(), // TODO
                        0.0, // TODO
                        false, // TODO
                        false, // TODO
                        false, // TODO
                        false, // TODO
                        false, // TODO
                        0.0 // TODO
                );

        return wp;
    }

    public ArrayList<Double> toInsightObject(PointSnapshot p) {
        return new ArrayList<>() {
            {
                add(p.getX());
                add(p.getY());
                add(p.getZ());
            }
        };
    }

    public PointSnapshot fromInsightObject(List<Double> coordinates) {
        if (coordinates.size() == 3) {
            return new PointSnapshot(coordinates.get(0), coordinates.get(1), coordinates.get(2));
        } else return new PointSnapshot(0, 0, 0);
    }

    public GoalSnapshot fromInsightObject(Annotation annotation) {
        var geom = new AreaOfInterestGeometrySnapshot(new ReferencePointSnapshot(0, 0, 0, 0), null, null, 0, 0, 0);

        // TODO
        *//* geom.referencePointProperty().set(
        fromInsightObject(annotation.getFeature().getGeometry().getReferencePoint()));*//*

        var coords = annotation.getFeature().getGeometry().getCoordinates();

        for (var polygone : coords) {
            // GeoJSON polygone have first and last point identical and twice in each list --> IMC dont like that,
            // so lets drop last point
            if (polygone.size() > 1) {
                for (var corner : polygone.subList(0, polygone.size() - 1)) {
                    PointSnapshot p = new PointSnapshot(corner.get(1), corner.get(0), 0.0);

                    geom.getCorners().add(p);
                }
            }
        }

        var annotationParameters = annotation.getParameters();
        AreaOfInterestSnapshot aoi = null;
        try {
            aoi =
                    new AreaOfInterestSnapshot(
                            annotation.getId(),
                            annotation.getV(),
                            annotationParameters.getName(),
                            geom,
                            AreaOfInterestType.valueOf(annotationParameters.getType().name()),
                            annotationParameters.getGsd(),
                            annotationParameters.getDistance(),
                            0, // TODO
                            0, // TODO
                            annotationParameters.getYaw(),
                            0, // TODO
                            annotationParameters.getMinGroundDistance(),
                            IAreaOfInterest.JumpPatternTypes.interleaving, // TODO
                            IAreaOfInterest.ScanDirectionsTypes.towardLaning, // TODO
                            IAreaOfInterest.StartCaptureTypes.insideLine, // TODO
                            annotationParameters.getSingleDirection(),
                            false, // TODO
                            0, // TODO
                            0, // TODO
                            false, // TODO
                            0, // TODO
                            0, // TODO
                            annotationParameters.getMaxYawRollChange(),
                            annotationParameters.getMaxPitchChange(),
                            annotationParameters.getForwardOverlap(),
                            annotationParameters.getForwardOverlapMin(),
                            annotationParameters.getLateralOverlap(),
                            annotationParameters.getLateralOverlapMin(),
                            0, // TODO
                            IAreaOfInterest.RotationDirection.RIGHT, // TODO annotationParameters.getRotationDirection(),
                            0, // TODO
                            annotation.getModifiedDate() != null ? dateFormat.parse(annotation.getModifiedDate()) : new Date(0),
                            annotation.getCreatedDate() != null ? dateFormat.parse(annotation.getCreatedDate()) : new Date(0));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return aoi;
    }

    public Annotation toInsightObject(AreaOfInterestSnapshot imcAoi) {
        var annotation = new Annotation();
        annotation.setId(imcAoi.getId());
        annotation.setV(imcAoi.getVersion());
        Geometry geom = new Geometry();
        geom.setType("Polygon");
        List<List<List<Double>>> coordinates = new ArrayList<>();
        geom.setCoordinates(coordinates);
        List<List<Double>> aoi = new ArrayList<>();
        coordinates.add(aoi);
        var lock = imcAoi.getAreaOfInterestGeometry().getCorners();
        lock.forEach(
                p -> {
                    List<Double> point = new ArrayList<>();
                    point.add(p.getY());
                    point.add(p.getX());
                    // point.add(p.zProperty().get());
                    aoi.add(point);
                });

        // adding first point again aka. GeoJSON polygon
        List<Double> point = new ArrayList<>();
        point.add(lock.get(0).getY());
        point.add(lock.get(0).getX());
        // point.add(p.zProperty().get());
        aoi.add(point);

        var feature = new Feature();
        feature.setType("Feature");
        feature.setGeometry(geom);
        annotation.setFeature(feature);

        var target = new Target();
        target.setType("2d");
        annotation.setTarget(target);

        var annotationParameters = new AnnotationParameters();
        // annotationParameters.setAnnotationId(areaOfInterestGeometryProperty().get().getInsightId());
        annotationParameters.setName(imcAoi.getName());
        annotationParameters.setGsd(imcAoi.getGsd());
        annotationParameters.setDistance(imcAoi.getDistance());
        annotationParameters.setForwardOverlapMin(imcAoi.getForwardOverlapMin());
        annotationParameters.setForwardOverlap(imcAoi.getForwardOverlap());
        annotationParameters.setLateralOverlapMin(imcAoi.getLateralOverlapMin());
        annotationParameters.setLateralOverlap(imcAoi.getLateralOverlap());
        annotation.setParameters(annotationParameters);

        return annotation;
    }

    public Flight toInsightObject(FlightSnapshot imcFlight) {
        var flight = new Flight();
        flight.setId(imcFlight.getId());
        flight.setV(imcFlight.getVersion());
        flight.getCameras().addAll(imcFlight.getPhotos().stream().collect(Collectors.toList()));
        flight.getLogs().addAll(imcFlight.getLogs().stream().collect(Collectors.toList()));

        flight.setSurveyDate(dateFormat.format(imcFlight.getCreationDate()));
        flight.setName(imcFlight.getName());

        return flight;
    }

    public DatasetSnapshot fromInsightObject(Flight flight) {
        *//*IHardwareConfiguration hardwareConfiguration = null;

        if (flight.getHwConfig() != null) {
            try {
                hardwareConfiguration =
                        hwConfigurationSerializer.deserialize(
                                parser.parse(flight.getHwConfig()).getAsJsonObject(), IHardwareConfiguration.class, null);
            } catch (Exception e) {
                // parse error
            }
        }*//*

        FlightSnapshot imcFlight = null;
        try {
            imcFlight =
                    new FlightSnapshot(
                            flight.getId(),
                            flight.getV(),
                            flight.getName(),
                            flight.getSurveyDate() != null ? dateFormat.parse(flight.getSurveyDate()) : new Date(0),
                            flight.getSurveyDate() != null ? dateFormat.parse(flight.getSurveyDate()) : new Date(0),
                            hardwareConfiguration,
                            MatchingStatus.NEW, // TODO
                            0, // TODO
                            0, // TODO
                            0l,
                            "", // TODO
                            0l, // TODO
                            0l, // TODO
                            0l, // TODO
                            0l, // TODO
                            "", // TODO
                            false, // TODO
                            false, // TODO
                            false, // TODO
                            false, // TODO
                            0d, // TODO
                            0d, // TODO
                            false, // TODO
                            0d, // TODO
                            0d, // TODO
                            false, // TODO
                            0d, // TODO
                            0d, // TODO
                            null, // TODO
                            null, // TODO
                            null, // TODO
                            null, // TODO
                            null, // TODO
                            null, // TODO
                            null, // TODO
                            null, // TODO
                            null, // TODO
                            null, // TODO
                            null, // TODO
                            null, // TODO
                            null, // TODO
                            null, // TODO
                            null, // TODO
                            null, // TODO
                            null, // TODO
                            null, // TODO
                            null // TODO
                    );
        } catch (ParseException e) {
            e.printStackTrace();
        }

        imcFlight.getPhotos().addAll(flight.getCameras());
        imcFlight.getLogs().addAll(flight.getLogs());
        return imcFlight;
    }*/
}