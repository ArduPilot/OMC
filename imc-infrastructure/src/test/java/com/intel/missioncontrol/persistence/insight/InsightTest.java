/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.persistence.insight;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.intel.missioncontrol.common.IPathProvider;
import com.intel.missioncontrol.persistence.Query;
import com.intel.missioncontrol.project.Dataset;
import com.intel.missioncontrol.project.FlightPlan;
import com.intel.missioncontrol.project.Mission;
import com.intel.missioncontrol.project.PolygonGoal;
import com.intel.missioncontrol.project.Project;
import com.intel.missioncontrol.project.property.MergeStrategy;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.asyncfx.AsyncFX;
import org.asyncfx.Awaiter;
import org.asyncfx.concurrent.Futures;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class InsightTest {

    private static InsightRepository insightRepository;

    @TempDir
    static Path tempDir;

    @BeforeAll
    static void init() {
        AsyncFX.setVerifyPropertyAccess(false);
        insightRepository =
            new InsightRepository(
                new IPathProvider() {
                    @Override
                    public Path getSettingsDirectory() {
                        return null;
                    }

                    @Override
                    public Path getProjectsDirectory() {
                        return null;
                    }

                    @Override
                    public Path getProfilingDirectory() {
                        return null;
                    }

                    @Override
                    public Path getUserHomeDirectory() {
                        return null;
                    }

                    @Override
                    public Path getErrorUploadDirectory() {
                        return null;
                    }

                    @Override
                    public Path getUpdatesDirectory() {
                        return null;
                    }

                    @Override
                    public Path getSettingsFile() {
                        return null;
                    }

                    @Override
                    public Path getLegacySettingsFile() {
                        return null;
                    }

                    @Override
                    public Path getCacheDirectory() {
                        return tempDir.resolve("insightCache");
                    }

                    @Override
                    public Path getGazetteerCacheDirectory() {
                        return null;
                    }

                    @Override
                    public Path getLogDirectory() {
                        return null;
                    }

                    @Override
                    public Path getExternalRtkConfigDirectory() {
                        return null;
                    }

                    @Override
                    public Path getCameraDescriptionsDirectory() {
                        return null;
                    }

                    @Override
                    public Path getLensDescriptionsDirectory() {
                        return null;
                    }

                    @Override
                    public Path getPlatformDescriptionsDirectory() {
                        return null;
                    }

                    @Override
                    public Path getTemplatesDirectory() {
                        return null;
                    }

                    @Override
                    public Path getLicenseSettingsFile() {
                        return null;
                    }

                    @Override
                    public Path getGeoidDirectory() {
                        return null;
                    }

                    @Override
                    public Path getLocalAirspacesFolder() {
                        return null;
                    }

                    @Override
                    public Path getWebviewCacheFolder() {
                        return null;
                    }
                });
    }

    @AfterAll
    static void teardown() {
        AsyncFX.setVerifyPropertyAccess(true);
    }

    @Test
    void run_Multiple_Calls_Concurrently() {
        ThreadPoolExecutor exec = (ThreadPoolExecutor)Executors.newFixedThreadPool(10);
        Awaiter awaiter = new Awaiter();
        for (int i = 0; i < 10; i++) {
            exec.execute(
                () -> {
                    try {
                        Project project =
                            insightRepository.createAsync("Hello project!").get(1000, TimeUnit.MILLISECONDS);
                        insightRepository.deleteAsync(project.getId()).get(1000, TimeUnit.MILLISECONDS);
                        awaiter.signal();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (TimeoutException e) {
                        e.printStackTrace();
                    }
                });
        }

        awaiter.await(10, Duration.ofMinutes(5));
    }

    @Test
    void delete_All_Test_Projects() throws InterruptedException, ExecutionException, TimeoutException {
        Set<Project> snapshots = insightRepository.queryAsync(new Query.All()).get(1000, TimeUnit.MILLISECONDS);
        Iterator<Project> it = snapshots.iterator();
        while (it.hasNext()) {
            insightRepository.deleteAsync(it.next().getId()).get(1000, TimeUnit.MILLISECONDS);
        }
    }

    @Test
    void create_Project_Save_Update_Delete() throws ExecutionException, InterruptedException {
        insightRepository
            .createAsync("Hello project 1")
            .thenAccept(
                project -> {
                    Set<Project> projects = null;
                    try {
                        projects = insightRepository.queryAsync(new Query.All()).get(1000, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (TimeoutException e) {
                        e.printStackTrace();
                    }

                    Assertions.assertNotEquals(0, projects.size());
                    Assertions.assertNotEquals(
                        0, projects.stream().filter(ps -> ps.getName().equals("Hello project 1")).count());

                    project.nameProperty().setValue("Hello project 2");

                    try {
                        insightRepository.saveAsync(project).get(1000, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (TimeoutException e) {
                        e.printStackTrace();
                    }

                    try {
                        projects = insightRepository.queryAsync(new Query.All()).get(1000, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (TimeoutException e) {
                        e.printStackTrace();
                    }

                    Assertions.assertNotEquals(0, projects.size());
                    Assertions.assertNotEquals(
                        0, projects.stream().filter(ps -> ps.getName().equals("Hello project 2")).count());
                })
            .get();
    }

    @Test
    void save_To_Insight_And_Read_Back_Project_With_Goals_And_FlightPlans_Successfully()
            throws ExecutionException, InterruptedException, TimeoutException {
        insightRepository
            .createAsync("The goodest project")
            .thenAccept(
                project -> {
                    project.getUser().nameProperty().setValue(InsightConfig.getInstance().getInsightUsername());

                    // add mission
                    Mission mission = new Mission();
                    mission.nameProperty().setValue("The goodest mission");
                    project.getMissions().add(mission);

                    // add goal
                    PolygonGoal polygonGoal = new PolygonGoal();
                    polygonGoal.nameProperty().setValue("The goodest polygon");
                    mission.placeablesProperty().add(polygonGoal);

                    // add flight plan
                    FlightPlan flightPlan = new FlightPlan();
                    flightPlan.nameProperty().setValue("The goodest flight plan");
                    mission.flightPlansProperty().add(flightPlan);

                    // add dataset
                    Dataset dataset = new Dataset();
                    dataset.nameProperty().setValue("The goodest dataset");
                    project.getDatasets().add(dataset);

                    try {
                        insightRepository
                            .saveAsync(project)
                            .thenApplyAsync(
                                aVoid -> {
                                    try {
                                        Set<Project> projects =
                                            insightRepository
                                                .queryAsync(new Query.All())
                                                .get(1000, TimeUnit.MILLISECONDS);
                                        return Futures.successful(
                                            projects.stream()
                                                .filter(ps -> ps.getName().equals("The goodest project"))
                                                .findFirst()
                                                .get());
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    } catch (ExecutionException e) {
                                        e.printStackTrace();
                                    } catch (TimeoutException e) {
                                        e.printStackTrace();
                                    }

                                    return Futures.successful(null);
                                })
                            .thenAccept(
                                projectSnapshot1 -> {
                                    var strategy = new MergeStrategy.DryRun();

                                    assertEquals(
                                        new Project(projectSnapshot1).nameProperty().get(),
                                        project.nameProperty().get());
                                    assertEquals(
                                        new Project(projectSnapshot1).missionsProperty().get(0).nameProperty().get(),
                                        project.missionsProperty().get(0).nameProperty().get());
                                    assertEquals(
                                        new Project(projectSnapshot1)
                                            .missionsProperty()
                                            .get(0)
                                            .flightPlansProperty()
                                            .get(0)
                                            .nameProperty()
                                            .get(),
                                        project.missionsProperty()
                                            .get(0)
                                            .flightPlansProperty()
                                            .get(0)
                                            .nameProperty()
                                            .get());
                                    project.merge(projectSnapshot1, strategy);
                                    assertEquals(0, strategy.getConflicts().size());
                                })
                            .get(1000, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (TimeoutException e) {
                        e.printStackTrace();
                    }
                })
            .get(1000, TimeUnit.MILLISECONDS);
    }
}
