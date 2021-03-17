/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.persistence.insight;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.intel.missioncontrol.common.IPathProvider;
import com.intel.missioncontrol.persistence.OperationFailedException;
import com.intel.missioncontrol.persistence.PersistenceException;
import com.intel.missioncontrol.persistence.ProjectOutdatedException;
import com.intel.missioncontrol.persistence.Query;
import com.intel.missioncontrol.persistence.Repository;
import com.intel.missioncontrol.persistence.ResourceResolver;
import com.intel.missioncontrol.persistence.insight.primitives.Annotation;
import com.intel.missioncontrol.persistence.insight.primitives.FlightPlan;
import com.intel.missioncontrol.persistence.insight.primitives.Mission;
import com.intel.missioncontrol.persistence.insight.primitives.Project;
import com.intel.missioncontrol.persistence.insight.primitives.Projects;
import com.intel.missioncontrol.persistence.local.LocalRepository;
import com.intel.missioncontrol.project.GoalSnapshot;
import com.intel.missioncontrol.project.MissionSnapshot;
import com.intel.missioncontrol.project.ProjectSnapshot;
import com.intel.missioncontrol.project.RepositoryType;
import com.intel.missioncontrol.project.UserSnapshot;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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

    private final LocalRepository insightCache;
    private BiMap<UUID, String> uuidInsightIdMap = Maps.synchronizedBiMap(HashBiMap.create());
    private BiMap<String, UUID> insightIdUuidMap = uuidInsightIdMap.inverse();
    private Map<String, Integer> insightIdVersionMap = Collections.synchronizedMap(new HashMap<>());
    private UserSnapshot user = new UserSnapshot(UUID.randomUUID(), "");
    private final ProjectConverter projectConverter;

    @Inject
    public InsightRepository(IPathProvider pathProvider) {
        Path insightCachePath = pathProvider.getCacheDirectory().resolve("insightCache");
        try {
            if (!Files.exists(insightCachePath)) {
                Files.createDirectories(insightCachePath);
            }
        } catch (IOException ex) {
            throw PersistenceException.fromException(ex);
        }

        insightCache = new LocalRepository(insightCachePath);
        insightContext = new InsightContext();
        insightDao = new InsightDaoImpl(insightContext);
        projectConverter = new ProjectConverter(uuidInsightIdMap, insightIdVersionMap);
    }

    @Override
    public RepositoryType getRepositoryType() {
        return RepositoryType.INSIGHT;
    }

    @Override
    public ResourceResolver getResourceResolver() {
        return null;
    }

    @Override
    public void addListener(ChangeListener listener) {}

    @Override
    public void removeListener(ChangeListener listener) {}

    @Override
    public Future<com.intel.missioncontrol.project.Project> createAsync(String name) {
        // performs post operation
        // calling POST first time ---- getting id back
        UUID uuid = UUID.randomUUID();
        ProjectSnapshot projectSnapshot =
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
        var project = new com.intel.missioncontrol.project.Project(projectSnapshot);
        return insightContext
            .logInAsync()
            .thenApplyAsync(value -> saveAsync(project).thenApplyAsync(value2 -> Futures.successful(project)));
    }

    @Override
    public Future<Void> saveAsync(com.intel.missioncontrol.project.Project project) {
        return insightContext
            .logInAsync()
            .thenApplyAsync(
                v ->
                    Dispatcher.background()
                        .getLaterAsync(
                            () -> {
                                saveProjectInsight(new ProjectSnapshot(project));

                                return project;
                            })
                        .thenFinallyApplyAsync(
                            (value, exception) -> {
                                if (exception != null) {
                                    var exceptions = exception.getThrowables();
                                    for (Throwable t : exceptions) {
                                        if (t instanceof ProjectOutdatedException) {
                                            return Futures.failed(t);
                                        } else if (t instanceof PersistenceException) {
                                            return insightCache.saveAsync(project);
                                        }
                                    }
                                }

                                try {
                                    return insightCache.saveAsync(project);
                                } catch (Exception e) {
                                    return Futures.failed(new PersistenceException(InsightRepository.class.getName()));
                                }
                            }));
    }

    private void saveProjectInsight(ProjectSnapshot projectSnapshot) {
        // converting IMC project to Insight Project
        Project insightProject = projectConverter.convertProjectToInsight(projectSnapshot);
        String projectInsightId = insightProject.getId();
        // creating or updating project
        if (projectInsightId == null) {
            Project createdProject = insightDao.createProject(insightProject);
            uuidInsightIdMap.put(projectSnapshot.getId(), createdProject.getId());
            uuidInsightIdMap.put(projectSnapshot.getUser().getId(), createdProject.getUser().getId());
            insightIdVersionMap.put(createdProject.getId(), createdProject.getV());
            projectInsightId = createdProject.getId();
        } else {
            try {
                Project updatedProject = insightDao.updateProject(insightProject);
                insightIdVersionMap.put(updatedProject.getId(), updatedProject.getV());
            } catch (OperationFailedException e) {
                // project is outdated () --- has to repeat get + merge
                ProjectSnapshot repositoryProject =
                    projectConverter.convertProjectFromInsight(insightDao.searchProjectById(projectInsightId));
                throw new ProjectOutdatedException(repositoryProject);
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
            Mission insightMission = projectConverter.convertMissionToInsight(missionSnapshot);
            String missionInsightId = insightMission.getId();
            if (missionInsightId == null) {
                Mission createdMission = insightDao.createMissionByProjectId(projectInsightId, insightMission);
                createdMission = insightDao.searchMissionById(createdMission.getId());
                uuidInsightIdMap.put(missionSnapshot.getId(), createdMission.getId());
                insightIdVersionMap.put(createdMission.getId(), createdMission.getV());
                missionInsightId = createdMission.getId();
            } else {
                try {
                    Mission updatedMission = insightDao.updateMission(insightMission);
                    insightIdVersionMap.put(updatedMission.getId(), updatedMission.getV());
                } catch (OperationFailedException e) {
                    // mission is outdated --- has to repeat get + merge
                    ProjectSnapshot repositoryProject =
                        projectConverter.convertProjectFromInsight(insightDao.searchProjectById(projectInsightId));
                    throw new ProjectOutdatedException(repositoryProject);
                }
            }

            for (var goal : missionSnapshot.getPlaceables()) {
                Annotation insightGoal = projectConverter.convertGoalToInsight((GoalSnapshot)goal);
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
                    } catch (OperationFailedException e) {
                        // goal is outdated --- has to repeat get + merge
                        ProjectSnapshot repositoryProject =
                            projectConverter.convertProjectFromInsight(insightDao.searchProjectById(projectInsightId));
                        throw new ProjectOutdatedException(repositoryProject);
                    }
                }
            }

            for (var fp : missionSnapshot.getFlightPlans()) {
                FlightPlan insightFlightPlan = projectConverter.convertFlightPlanToInsight(fp);
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
                    } catch (OperationFailedException e) {
                        // flight plan is outdated --- has to repeat get + merge
                        ProjectSnapshot repositoryProject =
                            projectConverter.convertProjectFromInsight(insightDao.searchProjectById(projectInsightId));
                        throw new ProjectOutdatedException(repositoryProject);
                    }
                }
            }
        }
    }

    @Override
    public Future<Void> deleteAsync(UUID id) {
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
    public Future<Set<com.intel.missioncontrol.project.Project>> queryAsync(Query query) {
        return insightContext
            .logInAsync()
            .thenApplyAsync(
                v -> {
                    Projects projects = insightDao.searchAllProjects();
                    Set<com.intel.missioncontrol.project.Project> projectsSet =
                        projects.getProjects()
                            .stream()
                            .map(
                                project -> {
                                    doInsightMagicToComposeAProject(project);
                                    return projectConverter.convertProjectFromInsight(project);
                                })
                            .filter(query::satisfies)
                            .map(com.intel.missioncontrol.project.Project::new)
                            .collect(Collectors.toSet());

                    return Futures.successful(projectsSet);
                })
            .thenFinallyApplyAsync(
                (value, exception) -> {
                    //in case of temporary repository failure like being offline should return a cached value
                    if ((exception != null) || value.size() == 0) {
                        return insightCache.queryAsync(query);
                    }

                    return Futures.successful(value);
                });
    }

    private void doInsightMagicToComposeAProject(Project project) {
        String projectId = project.getId();

        var missions = insightDao.searchMissionsByProjectId(projectId).getMissions();
        missions.forEach(
            mission -> {
                var aois = insightDao.searchAllAnnotationsInMission(mission.getId(), projectId);
                var flighPlans = insightDao.searchAllFlightPlansInMission(mission.getId(), projectId);
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
    }

}
