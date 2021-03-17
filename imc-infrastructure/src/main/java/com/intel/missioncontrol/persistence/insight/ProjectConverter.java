/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.persistence.insight;

import com.google.common.collect.BiMap;
import com.intel.missioncontrol.geometry.Vec2;
import com.intel.missioncontrol.geospatial.LatLon;
import com.intel.missioncontrol.persistence.insight.primitives.Annotation;
import com.intel.missioncontrol.persistence.insight.primitives.AnnotationParameters;
import com.intel.missioncontrol.persistence.insight.primitives.AreaOfInterestType;
import com.intel.missioncontrol.persistence.insight.primitives.Feature;
import com.intel.missioncontrol.persistence.insight.primitives.Flight;
import com.intel.missioncontrol.persistence.insight.primitives.FlightPlan;
import com.intel.missioncontrol.persistence.insight.primitives.Geometry;
import com.intel.missioncontrol.persistence.insight.primitives.Mission;
import com.intel.missioncontrol.persistence.insight.primitives.ModificationUser;
import com.intel.missioncontrol.persistence.insight.primitives.Project;
import com.intel.missioncontrol.persistence.insight.primitives.Target;
import com.intel.missioncontrol.persistence.insight.primitives.User;
import com.intel.missioncontrol.project.DatasetSnapshot;
import com.intel.missioncontrol.project.FlightPlanSnapshot;
import com.intel.missioncontrol.project.GoalSnapshot;
import com.intel.missioncontrol.project.GoalType;
import com.intel.missioncontrol.project.MissionSnapshot;
import com.intel.missioncontrol.project.PolygonGoalSnapshot;
import com.intel.missioncontrol.project.ProjectSnapshot;
import com.intel.missioncontrol.project.RepositoryType;
import com.intel.missioncontrol.project.UserSnapshot;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProjectConverter {
    // private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectConverter.class);
    public static final String EPSG_4326 = "EPSG:4326";
    // date format matching insight's definition
    // private final JsonParser parser = new JsonParser();
    // private final IHardwareConfigurationManager hwConfigManager;
    // private final HWConfigurationSerializer hwConfigurationSerializer;
    private final BiMap<UUID, String> uuidInsightIdMap;
    private final BiMap<String, UUID> insightIdUuidMap;
    private final Map<String, Integer> insightIdVersionMap;

    public ProjectConverter(BiMap<UUID, String> uuidInsightIdMap, Map<String, Integer> insightIdVersionMap) {
        this.uuidInsightIdMap = uuidInsightIdMap;
        this.insightIdUuidMap = uuidInsightIdMap.inverse();
        this.insightIdVersionMap = insightIdVersionMap;
    }

    public ProjectSnapshot convertProjectFromInsight(Project insightProject) {
        UUID imcId = insightIdUuidMap.get(insightProject.getId());
        if (imcId == null) {
            imcId = UUID.randomUUID();
            insightIdUuidMap.put(insightProject.getId(), imcId);
        }

        insightIdVersionMap.put(insightProject.getId(), insightProject.getV());

        ProjectSnapshot imcProject =
            new ProjectSnapshot(
                imcId,
                insightProject.getName(),
                RepositoryType.INSIGHT,
                ZonedDateTime.parse(insightProject.getCreated()).toOffsetDateTime(),
                ZonedDateTime.parse(insightProject.getModificationDate()).toOffsetDateTime(),
                convertUserFromInsight(insightProject.getUser()),
                insightProject.getEpsg(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>());

        var list = insightProject.getMissionList();
        for (int i = 0; i < list.size(); i++) {
            var mission = list.get(i);
            var m = convertMissionFromInsight(mission);
            imcProject.getMissions().add(m);
            var fpList = mission.getFlightPlanList();
            for (int j = 0; j < fpList.size(); j++) {
                var fp = convertFlightPlanFromInsight(fpList.get(j));
                m.getFlightPlans().add(fp);
            }

            var aoisList = mission.getAnnotationList();
            for (int j = 0; j < aoisList.size(); j++) {
                GoalSnapshot goal = convertGoalFromInsight(aoisList.get(j));
                m.getPlaceables().add(goal);
            }
        }

        for (var flight : insightProject.getFlightList()) {
            imcProject.getDatasets().add(convertDatasetFromInsight(flight));
        }

        return imcProject;
    }

    private FlightPlanSnapshot convertFlightPlanFromInsight(FlightPlan flightPlan) {
        UUID imcId = insightIdUuidMap.get(flightPlan.getId());

        if (imcId == null) {
            imcId = UUID.randomUUID();
            insightIdUuidMap.put(flightPlan.getId(), imcId);
        }

        insightIdVersionMap.put(flightPlan.getId(), flightPlan.getV());

        /*IHardwareConfiguration hardwareConfiguration = null;

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
        */
        FlightPlanSnapshot imcFlightPlan = null;

        imcFlightPlan =
            new FlightPlanSnapshot(
                imcId,
                flightPlan.getName(),
                ZonedDateTime.parse(flightPlan.getModified()).toOffsetDateTime(),
                null,
                null,
                null,
                0.0,
                0.0,
                0.0,
                true,
                true,
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                new ArrayList<>()); // TODO

        /*
        imcFlightPlan
                .getWaypoints()
                .addAll(
                        flightPlan
                                .getWaypoints()
                                .stream()
                                .map(waypoint -> fromInsightObject(waypoint))
                                .collect(Collectors.toList()));*/
        return imcFlightPlan;
    }

    private GoalSnapshot convertGoalFromInsight(Annotation annotation) {
        UUID imcId = insightIdUuidMap.get(annotation.getId());

        if (imcId == null) {
            imcId = UUID.randomUUID();
            insightIdUuidMap.put(annotation.getId(), imcId);
        }

        insightIdVersionMap.put(annotation.getId(), annotation.getV());

        var coords = annotation.getFeature().getGeometry().getCoordinates();
        for (var polygone : coords) {
            // GeoJSON polygone have first and last point identical and twice in each list --> IMC dont like that,
            // so lets drop last point
            if (polygone.size() > 1) {
                for (var corner : polygone.subList(0, polygone.size() - 1)) {
                    // PointSnapshot p = new PointSnapshot(corner.get(1), corner.get(0), 0.0);
                    // geom.getCorners().add(p);
                }
            }
        }

        AnnotationParameters annotationParameters = annotation.getParameters();
        if (annotationParameters.getType().equals(AreaOfInterestType.POLYGON)) {
            PolygonGoalSnapshot snapshot =
                new PolygonGoalSnapshot(
                    imcId,
                    null,
                    0.0,
                    null,
                    annotationParameters.getName(),
                    0.0,
                    0.0,
                    0.0,
                    0.0,
                    0.0,
                    0.0,
                    0.0,
                    false,
                    false,
                    0.0,
                    0.0,
                    false,
                    0.0,
                    0.0,
                    0.0,
                    0.0,
                    0.0,
                    0.0,
                    0.0,
                    0.0,
                    0.0,
                    0.0,
                    0.0,
                    null,
                    0,
                    GoalType.POLYGON,
                    null);
            return snapshot;
        }

        return null;
    }

    private DatasetSnapshot convertDatasetFromInsight(Flight flight) {
        UUID imcId = insightIdUuidMap.get(flight.getId());

        if (imcId == null) {
            imcId = UUID.randomUUID();
            insightIdUuidMap.put(flight.getId(), imcId);
        }

        insightIdVersionMap.put(flight.getId(), flight.getV());

        return null;
    }

    public Project convertProjectToInsight(ProjectSnapshot imcProject) {
        var project = new Project();
        String insightId = uuidInsightIdMap.get(imcProject.getId());
        project.setId(insightId);
        User user = convertUserToInsight(imcProject.getUser());
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
        Integer version = insightIdVersionMap.get(insightId);
        project.setMy_version(version == null ? 0 : version);
        // project.setCompanyId(imcProjectInfo.getCompanyId());
        // project.setIndustry(imcProjectInfo.getIndustry());
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

    private User convertUserToInsight(UserSnapshot user) {
        var insightUser = new User();
        insightUser.setDisplayName(user.getName());
        String insightId = uuidInsightIdMap.get(user.getId());
        insightUser.setId(insightId);
        return insightUser;
    }

    private UserSnapshot convertUserFromInsight(User user) {
        if (user == null) {
            return null;
        }

        UUID uuid = insightIdUuidMap.get(user.getId());
        if (uuid == null) {
            uuid = UUID.randomUUID();
            insightIdUuidMap.put(user.getId(), uuid);
        }

        var imcUser = new UserSnapshot(uuid, user.getDisplayName());
        return imcUser;
    }

    protected UserSnapshot convertUserFromInsight(ModificationUser user) {
        if (user == null) {
            return null;
        }

        UUID uuid = insightIdUuidMap.get(user.getId());
        if (uuid == null) {
            uuid = UUID.randomUUID();
            insightIdUuidMap.put(user.getId(), uuid);
        }

        var imcUser = new UserSnapshot(uuid, user.getDisplayName());
        return imcUser;
    }

    public Mission convertMissionToInsight(MissionSnapshot imcMission) {
        var mission = new Mission();
        String insightId = uuidInsightIdMap.get(imcMission.getId());
        mission.setId(insightId);
        mission.setName(imcMission.getName());
        Integer version = insightIdVersionMap.get(insightId);
        mission.setMy_version(version == null ? 0 : version);
        // mission.setTerrainAdjustmentMode(imcMission.getAltitudeAdjustModes().name());
        double originLat = imcMission.getOrigin().getLatitude();
        double originLon = imcMission.getOrigin().getLongitude();
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
    public MissionSnapshot convertMissionFromInsight(Mission mission) {
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
        LatLon point;
        if (ref != null && ref.size() == 2) {
            point = LatLon.fromDegrees(ref.get(0), ref.get(1));
        } else {
            point = LatLon.fromDegrees(0, 0);
        }

        UUID imcId = insightIdUuidMap.get(mission.getId());
        if (imcId == null) {
            imcId = UUID.randomUUID();
            insightIdUuidMap.put(mission.getId(), imcId);
        }

        MissionSnapshot imcMission =
            new MissionSnapshot(
                imcId,
                mission.getName(),
                point,
                "EPSG:3395",
                new ArrayList<>(),
                new ArrayList<>(),
                0.0,
                0.0,
                true,
                true,
                null);

        return imcMission;
    }

    public Annotation convertGoalToInsight(GoalSnapshot goal) {
        var annotation = new Annotation();
        String insightId = uuidInsightIdMap.get(goal.getId());
        annotation.setId(insightId);
        Integer version = insightIdVersionMap.get(insightId);
        annotation.setMy_version(version == null ? 0 : version);
        Geometry geom = new Geometry();
        geom.setType("Polygon");
        /*List<List<List<Double>>> coordinates = new ArrayList<>();
                geom.setCoordinates(coordinates);
                List<List<Double>> aoi = new ArrayList<>();
                coordinates.add(aoi);
                var lock = goal.getCorners();
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
        */
        var feature = new Feature();
        feature.setType("Feature");
        feature.setGeometry(geom);
        annotation.setFeature(feature);

        var target = new Target();
        target.setType("2d");
        annotation.setTarget(target);

        var annotationParameters = new AnnotationParameters();
        // annotationParameters.setAnnotationId(areaOfInterestGeometryProperty().get().getInsightId());
        annotationParameters.setName(goal.getName());
        annotationParameters.setGsd(goal.getGsd());
        annotationParameters.setDistance(goal.getDistance());
        annotationParameters.setForwardOverlapMin(goal.getForwardOverlapMin());
        annotationParameters.setForwardOverlap(goal.getForwardOverlap());
        annotationParameters.setLateralOverlapMin(goal.getLateralOverlapMin());
        annotationParameters.setLateralOverlap(goal.getLateralOverlap());
        annotation.setParameters(annotationParameters);

        return annotation;
    }

    public FlightPlan convertFlightPlanToInsight(FlightPlanSnapshot flightPlan) {
        var fp = new FlightPlan();
        String insightId = uuidInsightIdMap.get(flightPlan.getId());
        fp.setId(insightId);
        Integer version = insightIdVersionMap.get(insightId);
        fp.setMy_version(version == null ? 0 : version);
        fp.setName(flightPlan.getName());
        fp.setType("SURVEY");
        // fp.setTakeoff(toInsightObject(flightPlan.getTakeOffPoint()));
        // fp.setLanding(toInsightObject(flightPlan.getLandingPoint()));
        /*fp.setHwConfig(
                hwConfigurationSerializer
                        .serialize(flightPlan.getHardwareConfiguration(), IHardwareConfiguration.class, null)
                        .toString());
        fp.setCamera(toInsightObject(flightPlan.getHardwareConfiguration()));*/
        fp.setSafetyAltitude(flightPlan.getSafetyAltitude());
        fp.setMaxGroundSpeed(flightPlan.getMaxGroundSpeed());

        // TODO naming ?
        // fp.setSafetyProcedures(flightPlan.getEmergencyProcedures());

        /* fp.getWaypoints()
        .addAll(
                flightPlan
                        .getWaypoints()
                        .stream()
                        .map(waypoint -> toInsightObject(waypoint))
                        .collect(Collectors.toList()));*/

        return fp;
    }
}
