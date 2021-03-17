/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project;

import com.intel.missioncontrol.persistence.Repository;
import com.intel.missioncontrol.persistence.insight.InsightRepository;
import com.intel.missioncontrol.persistence.local.LocalRepository;
import com.intel.missioncontrol.project.property.MergeStrategy;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import org.asyncfx.TestBase;
import org.asyncfx.concurrent.Dispatcher;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CloudSyncTest extends TestBase {

    private static Path folderRepoPath;
    private static Path insightCachePath;

    private static Repository localRepository;
    private static Repository insightRepository;

    @TempDir
    static Path tempDir;

    @BeforeAll
    static void beforeAll() {
        folderRepoPath = tempDir.resolve("folderRepository");
        insightCachePath = tempDir.resolve("insightCache");
        localRepository = new LocalRepository(folderRepoPath);
        insightRepository = new InsightRepository(insightCachePath);
    }

    // create a local project
    @Test
    void creating_Local_Project_Test() {
        IProjectManager projectManager = new ProjectManager(localRepository, insightRepository);
        Project project =
            projectManager.createAsync("New local project", localRepository).getUnchecked(5000, TimeUnit.MILLISECONDS);
        Dispatcher.platform()
            .runLaterAsync(
                () -> {
                    project.missionsProperty().add(new Mission());
                    project.missionsProperty().add(new Mission());
                })
            .getUnchecked(1000, TimeUnit.MILLISECONDS);
        projectManager.synchronizeAsync(project, new MergeStrategy.KeepOurs()).getUnchecked(5000, TimeUnit.MILLISECONDS);
        Assertions.assertEquals("New local project", project.nameProperty().get());
    }

    // create an Insight project (if offline - create in the cache and upload it when online)
    @Test
    void creating_Insight_Project_Test() {
        IProjectManager projectManager = new ProjectManager(localRepository, insightRepository);
        Project project =
            projectManager
                .createAsync("New insight project", insightRepository)
                .getUnchecked(5000, TimeUnit.MILLISECONDS);
        Assertions.assertEquals("New insight project", project.nameProperty().get());
    }

    // update a list of projects
    @Test
    void updating_List_Of_All_Projects_Test() {
        IProjectManager projectManager = new ProjectManager(localRepository, insightRepository);
        projectManager.refreshAsync().getUnchecked(5000, TimeUnit.MILLISECONDS);
        var listOfProjects = projectManager.availableProjectsProperty();
        Assertions.assertNotEquals(0, listOfProjects.size());
    }

    // open a project from the list
    @Test
    void open_And_Sync_Project_Test() throws InterruptedException {
        IProjectManager projectManager = new ProjectManager(localRepository, insightRepository);
        projectManager.refreshAsync().getUnchecked(5000, TimeUnit.MILLISECONDS);
        var listOfProjects = projectManager.availableProjectsProperty();
        var project = listOfProjects.get().lock().iterator().next();
        Assertions.assertNotEquals(null, project.getName());

        Dispatcher.platform().run(() -> project.nameProperty().set("Hello sync 2"));

        Thread.sleep(20000);

        projectManager.synchronizeAsync(project, new MergeStrategy.KeepOurs()).getUnchecked(5000, TimeUnit.MILLISECONDS);
        Assertions.assertEquals("Hello sync 2", project.getName());
    }

    @Test
    void open_And_Sync_Project_With_External_Changes_Test() throws InterruptedException {
        IProjectManager projectManager = new ProjectManager(localRepository, insightRepository);
        projectManager.refreshAsync().getUnchecked(5000, TimeUnit.MILLISECONDS);
        var listOfProjects = projectManager.availableProjectsProperty();
        var project = listOfProjects.get().lock().iterator().next();
        Assertions.assertNotEquals(null, project.getName());

        Dispatcher.platform().run(() -> project.nameProperty().set("Hello sync 2"));

        Thread.sleep(20000);

        projectManager.synchronizeAsync(project, new MergeStrategy.KeepTheirs()).getUnchecked(5000, TimeUnit.MILLISECONDS);
        Assertions.assertNotEquals("Hello sync 2", project.getName());
    }

}
