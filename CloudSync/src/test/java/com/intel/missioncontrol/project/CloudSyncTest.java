/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project;

import com.intel.missioncontrol.TestBase;
import com.intel.missioncontrol.config.CloudSync;
import com.intel.missioncontrol.project.property.MergeStrategy;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import org.asyncfx.concurrent.Dispatcher;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class CloudSyncTest extends TestBase {

    private static Path folderRepoPath;
    private static Path insightCachePath;
    private static CloudSync cloudSync;

    @BeforeAll
    public static void beforeAll() {
        Path rootPath = Paths.get(new File("").getAbsolutePath());
        folderRepoPath = rootPath.resolve("folderRepository");
        insightCachePath = rootPath.resolve("insightCache");
        cloudSync = new CloudSync();
    }

    // create a local project
    @Test
    public void creating_Local_Project_Test() {
        IProjectManager projectManager = new ProjectManager(folderRepoPath, insightCachePath);
        Project project =
            projectManager.create("New local project", RepositoryType.LOCAL).getUnchecked(5000, TimeUnit.MILLISECONDS);
        Dispatcher.platform()
            .runLaterAsync(
                () -> {
                    project.missionsProperty().add(new Mission());
                    project.missionsProperty().add(new Mission());
                })
            .getUnchecked(1000, TimeUnit.MILLISECONDS);
        projectManager
            .synchronize(project, new MergeStrategy.KeepOurs())
            .getUnchecked(5000, TimeUnit.MILLISECONDS);
        Assertions.assertEquals("New local project", project.nameProperty().get());
    }

    // create an Insight project (if offline - create in the cache and upload it when online)
    @Test
    public void creating_Insight_Project_Test() {
        IProjectManager projectManager = new ProjectManager(folderRepoPath, insightCachePath);
        Project project =
            projectManager
                .create("New insight project", RepositoryType.INSIGHT)
                .getUnchecked(5000, TimeUnit.MILLISECONDS);
        Assertions.assertEquals("New insight project", project.nameProperty().get());
    }

    // update a list of projects
    @Test
    public void updating_List_Of_All_Projects_Test() {
        IProjectManager projectManager = new ProjectManager(folderRepoPath, insightCachePath);
        projectManager.refreshAsync().getUnchecked(5000, TimeUnit.MILLISECONDS);
        var listOfProjects = projectManager.availableProjectsProperty();
        Assertions.assertNotEquals(0, listOfProjects.size());
    }

    // open a project from the list
    @Test
    public void open_And_Sync_Project_Test() throws InterruptedException {
        IProjectManager projectManager = new ProjectManager(folderRepoPath, insightCachePath);
        projectManager.refreshAsync().getUnchecked(5000, TimeUnit.MILLISECONDS);
        var listOfProjects = projectManager.availableProjectsProperty();
        var project = listOfProjects.get().lock().iterator().next();
        Assertions.assertNotEquals(null, project.getName());

        Dispatcher.platform().run(() -> project.nameProperty().set("Hello sync 2"));

        Thread.sleep(20000);

        projectManager
            .synchronize(project, new MergeStrategy.KeepOurs())
            .getUnchecked(5000, TimeUnit.MILLISECONDS);
        Assertions.assertEquals("Hello sync 2", project.getName());
    }

    @Test
    public void open_And_Sync_Project_With_External_Changes_Test() throws InterruptedException {
        IProjectManager projectManager = new ProjectManager(folderRepoPath, insightCachePath);
        projectManager.refreshAsync().getUnchecked(5000, TimeUnit.MILLISECONDS);
        var listOfProjects = projectManager.availableProjectsProperty();
        var project = listOfProjects.get().lock().iterator().next();
        Assertions.assertNotEquals(null, project.getName());

        Dispatcher.platform().run(() -> project.nameProperty().set("Hello sync 2"));

        Thread.sleep(20000);

        projectManager
            .synchronize(project, new MergeStrategy.KeepTheirs())
            .getUnchecked(5000, TimeUnit.MILLISECONDS);
        Assertions.assertNotEquals("Hello sync 2", project.getName());
    }
}
