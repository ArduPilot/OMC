/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.persistence.insight;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import com.intel.missioncontrol.project.MissionSnapshot;
import com.intel.missioncontrol.project.ProjectSnapshot;
import com.intel.missioncontrol.project.RepositoryType;
import com.intel.missioncontrol.project.UserSnapshot;
import com.intel.missioncontrol.project.insight.Annotation;
import com.intel.missioncontrol.project.insight.FlightPlan;
import com.intel.missioncontrol.project.insight.Mission;
import com.intel.missioncontrol.project.insight.Project;
import com.intel.missioncontrol.project.insight.Projects;
import com.intel.missioncontrol.project.persistence.FolderRepository;
import com.intel.missioncontrol.project.persistence.InvalidFormatException;
import com.intel.missioncontrol.project.persistence.PersistenceException;
import com.intel.missioncontrol.project.persistence.ProjectOutdatedException;
import com.intel.missioncontrol.project.persistence.Query;
import com.intel.missioncontrol.project.persistence.Repository;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.asyncfx.concurrent.Dispatcher;
import org.asyncfx.concurrent.Future;
import org.asyncfx.concurrent.Futures;

public class InsightRepository implements Repository {

    private final InsightContext insightContext;
    private final InsightDaoImpl insightDao;

    private final FolderRepository insightCache;
    private BiMap<UUID, String> uuidInsightIdMap = Maps.synchronizedBiMap(HashBiMap.create());
    private BiMap<String, UUID> insightIdUuidMap = uuidInsightIdMap.inverse();
    private Map<String, Integer> insightIdVersionMap = Collections.synchronizedMap(new HashMap<>());
    private UserSnapshot user = new UserSnapshot(UUID.randomUUID(), "");
    private final ProjectConverter projectConverter;

    public InsightRepository(Path cachePath) {
        insightCache = new FolderRepository(cachePath);
        insightContext = new InsightContext();
        insightDao = new InsightDaoImpl(insightContext);
        projectConverter = new ProjectConverter(uuidInsightIdMap, insightIdVersionMap);
    }

    @Override
    public void addListener(ChangeListener listener) {}

    @Override
    public void removeListener(ChangeListener listener) {}

    @Override
    public Future<ProjectSnapshot> create(String name) {
        // performs post operation
        // calling POST first time ---- getting id back
        UUID uuid = UUID.randomUUID();
        ProjectSnapshot project =
            new ProjectSnapshot(
                uuid,
                name,
                RepositoryType.INSIGHT,
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                user,
                "EPSG:4326",
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>());
        return insightContext
            .logInAsync()
            .thenApplyAsync(value -> save(project).thenApplyAsync(value2 -> Futures.successful(project)));
    }

    @Override
    public Future<Void> save(ProjectSnapshot projectSnapshot) {
        return insightContext
            .logInAsync()
            .thenApplyAsync(
                v ->
                    Dispatcher.background()
                        .getLaterAsync(
                            () -> {
                                UUID uuid = projectSnapshot.getId();
                                saveProjectInsight(projectSnapshot);
                                return projectSnapshot;
                            })
                        .thenApplyAsync(
                            value -> {
                                try {
                                    // keep local means override cache
                                    return insightCache.save(projectSnapshot);
                                } catch (Exception e) {
                                    throw new PersistenceException(InsightRepository.class.getName());
                                }
                            }));
    }

    private void saveProjectInsight(ProjectSnapshot projectSnapshot) {
        // converting IMC project to Insight Project
        Project insightProject = projectConverter.toInsightProject(projectSnapshot);
        String projectInsightId = insightProject.getId();
        // creating or updating project
        if (projectInsightId == null) {
            Project createdProject = insightDao.createProject(insightProject);
            uuidInsightIdMap.put(projectSnapshot.getId(), createdProject.getId());
            insightIdVersionMap.put(createdProject.getId(), createdProject.getV());
            projectInsightId = createdProject.getId();
        } else {
            try {
                Project updatedProject = insightDao.updateProject(insightProject);
                insightIdVersionMap.put(updatedProject.getId(), updatedProject.getV());
            } catch (InvalidFormatException e) {
                // project is outdated () --- has to repeat get + merge
                throw new ProjectOutdatedException();
            }
        }

        for (MissionSnapshot missionSnapshot : projectSnapshot.getMissions()) {
            /*  if (mission.getCameras().isEmpty()) {
                    cameraResp = insightDao.createCamera(cameraResp);
                    insightMission.getCameras().add(cameraResp.getId());
                } else {
                    // TODO else - update
                }
            */
            Mission insightMission = projectConverter.toInsightObject(missionSnapshot);
            String missionInsightId = insightMission.getId();
            if (missionInsightId == null) {
                Mission createdMission = insightDao.createMissionByProjectId(projectInsightId, insightMission);
                createdMission = insightDao.searchMissionById(createdMission.getId());
                uuidInsightIdMap.put(missionSnapshot.getId(), createdMission.getId());
                insightIdVersionMap.put(createdMission.getId(), createdMission.getV());
                // ??? why do i need to update mission immediately ???
                insightMission.setV(createdMission.getV());
                createdMission = insightDao.updateMission(insightMission);
                /////////////
                insightIdVersionMap.put(createdMission.getId(), createdMission.getV());
                missionInsightId = createdMission.getId();
            } else {
                try {
                    Mission updatedMission = insightDao.updateMission(insightMission);
                    insightIdVersionMap.put(updatedMission.getId(), updatedMission.getV());
                } catch (InvalidFormatException e) {
                    // mission is outdated --- has to repeat get + merge
                    throw new ProjectOutdatedException();
                }
            }

            for (var goal : missionSnapshot.getGoals()) {
                Annotation insightGoal = projectConverter.toInsightObject(goal);
                insightGoal.setProjectId(projectInsightId);
                insightGoal.setMissionId(missionInsightId);

                String goalInsightId = insightGoal.getId();
                if (goalInsightId == null) {
                    // create a new Goal in insight
                    Annotation createdAnnotation = insightDao.createAnnotation(insightGoal);
                    uuidInsightIdMap.put(goal.getId(), createdAnnotation.getId());
                    insightIdVersionMap.put(createdAnnotation.getId(), createdAnnotation.getV());
                } else {
                    try {
                        Annotation updatedAnnotation = insightDao.updateAnnotation(insightGoal);
                        insightIdVersionMap.put(updatedAnnotation.getId(), updatedAnnotation.getV());
                    } catch (InvalidFormatException e) {
                        // goal is outdated --- has to repeat get + merge
                        throw new ProjectOutdatedException();
                    }
                }
            }

            for (var fp : missionSnapshot.getFlightPlans()) {
                FlightPlan insightFlightPlan = projectConverter.toInsightObject(fp);
                insightFlightPlan.setProjectId(projectInsightId);
                insightFlightPlan.setMissionId(missionInsightId);

                var insightFlightPlanId = insightFlightPlan.getId();
                if (insightFlightPlanId == null) {
                    FlightPlan createdFlightPlan = insightDao.createFlightPlan(insightFlightPlan);
                    uuidInsightIdMap.put(fp.getId(), createdFlightPlan.getId());
                    insightIdVersionMap.put(createdFlightPlan.getId(), createdFlightPlan.getV());
                } else {
                    try {
                        FlightPlan updatedFlightPlan = insightDao.updateFlightPlan(insightFlightPlan);
                        insightIdVersionMap.put(updatedFlightPlan.getId(), updatedFlightPlan.getV());
                    } catch (InvalidFormatException e) {
                        // flight plan is outdated --- has to repeat get + merge
                        throw new ProjectOutdatedException();
                    }
                }
            }
        }
    }

    @Override
    public Future<Void> delete(UUID id) {
        return insightContext
            .logInAsync()
            .thenAcceptAsync(
                value -> {
                    String insightId = uuidInsightIdMap.get(id);
                    if (insightId != null) {
                        insightDao.deleteProject(insightId);
                    }

                    return Futures.successful();
                });
    }

    @Override
    public Future<Set<ProjectSnapshot>> query(Query<ProjectSnapshot> query) {
        return insightContext
            .logInAsync()
            .thenApplyAsync(
                v -> {
                    Projects projects = insightDao.searchAllProjects();

                    Set<ProjectSnapshot> projectSnapshots =
                        new HashSet<>(
                            projects.getProjects()
                                .stream()
                                .map(
                                    project -> {
                                        String projectId = project.getId();

                                        var missions = insightDao.searchMissionsByProjectId(projectId).getMissions();
                                        missions.forEach(
                                            mission -> {
                                                var aois =
                                                    insightDao.searchAllAnnotationsInMission(
                                                        mission.getId(), projectId);
                                                var flighPlans =
                                                    insightDao.searchAllFlightPlansInMission(
                                                        mission.getId(), projectId);
                                                mission.setAnnotationList(aois.getAnnotations());
                                                mission.setFlightPlanList(flighPlans.getFlightPlans());
                                            });

                                        project.setMissionList(missions);

                                        var flights = insightDao.searchFlightsByProjectId(projectId).getFlights();
                                        flights.forEach(
                                            mission -> {
                                                /*
                                                Request photos for example
                                                 */
                                            });

                                        project.setFlightList(flights);
                                        ProjectSnapshot projectSnapshot =
                                            projectConverter.convertProjectFromInsight(project);
                                        return projectSnapshot;
                                    })
                                .collect(Collectors.toList()));
                    return Futures.successful(projectSnapshots);
                });
    }

    // TODO: read is implemented by Query.ById
    // private read and save are used in the public update method, because both can cause a conflict - update takes care
    /*@Override
    public Future<ProjectSnapshot> read(UUID uuid) {
        return insightContext
            .logInAsync()
            .thenApplyAsync(
                v ->
                    Dispatcher.background()
                        .getLaterAsync(
                            () -> {
                                String insightId = uuidInsightIdMap.get(uuid);
                                if (insightId != null) {
                                    Project readProject = null;
                                    try {
                                        readProject = insightDao.searchProjectById(insightId);
                                    } catch (PersistenceException e) {
                                        return null;
                                    }

                                    insightIdVersionMap.put(insightId, readProject.getV());
                                    ProjectSnapshot snapshot = null;

                                    snapshot =
                                        new ProjectSnapshot(
                                            uuid,
                                            readProject.getName(),
                                            RepositoryType.INSIGHT,
                                            OffsetDateTime.now(),
                                            OffsetDateTime.now(),
                                            user,
                                            "EPSG:4326",
                                            new ArrayList<>(),
                                            new ArrayList<>(),
                                            new ArrayList<>());

                                    return snapshot;
                                }

                                return null;
                            })
                        .thenApplyAsync(
                            value -> {
                                if (value == null) {
                                    try {
                                        // keep local means override cache
                                        return insightCache.read(uuid);
                                    } catch (Exception e) {
                                        throw new PersistenceException(InsightRepository.class.getName());
                                    }
                                } else {
                                    try {
                                        // keep local means override cache
                                        return insightCache
                                            .save(value)
                                            .thenApplyAsync(value1 -> Futures.successful(value));
                                    } catch (Exception e) {
                                        throw new PersistenceException(InsightRepository.class.getName());
                                    }
                                }
                            }));
    }*/

}
