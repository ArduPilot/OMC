/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project;

import com.intel.missioncontrol.Awaiter;
import com.intel.missioncontrol.project.persistence.insight.InsightRepository;
import java.io.File;
import java.time.Duration;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.asyncfx.AsyncFX;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class InsightTest {

    private static InsightRepository insightRepository;

    @BeforeAll
    static void init() {
        AsyncFX.setVerifyPropertyAccess(false);
        File localFolder = new File("");
        String parent = localFolder.getAbsolutePath();
        String name = parent + "/insightCache";
        insightRepository = new InsightRepository(new File(name).toPath());
    }

    @AfterAll
    static void teardown() {
        AsyncFX.setVerifyPropertyAccess(true);
    }

    @Test
    public void run_Multiple_Calls_Concurrently() {
        ThreadPoolExecutor exec = (ThreadPoolExecutor)Executors.newFixedThreadPool(100);
        Awaiter awaiter = new Awaiter();
        for (int i = 0; i < 100; i++) {
            exec.execute(
                () -> {
                    try {
                        ProjectSnapshot project =
                            insightRepository.create("Hello project!").get(1000, TimeUnit.MILLISECONDS);
                        insightRepository.delete(project.getId()).get(1000, TimeUnit.MILLISECONDS);
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

        awaiter.await(100, Duration.ofMinutes(5));
    }

    @Test
    public void delete_All_Test_Projects() throws InterruptedException, ExecutionException, TimeoutException {
        Set<ProjectSnapshot> snapshots = insightRepository.query(null).get(1000, TimeUnit.MILLISECONDS);
        Iterator<ProjectSnapshot> it = snapshots.iterator();
        while (it.hasNext()) {
            insightRepository.delete(it.next().getId()).get(1000, TimeUnit.MILLISECONDS);
        }
    }

    @Test
    public void create_Project_Save_Update_Delete() throws ExecutionException, InterruptedException {
        insightRepository
            .create("Hello project 1")
            .thenAccept(
                projectSnapshot -> {
                    Set<ProjectSnapshot> projects = null;
                    try {
                        projects = insightRepository.query(null).get(1000, TimeUnit.MILLISECONDS);
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

                    Project project = new Project(projectSnapshot);
                    project.nameProperty().setValue("Hello project 2");

                    try {
                        insightRepository.save(new ProjectSnapshot(project)).get(1000, TimeUnit.MILLISECONDS);
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
}
