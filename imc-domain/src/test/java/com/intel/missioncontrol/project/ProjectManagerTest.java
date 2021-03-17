/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project;

import com.intel.missioncontrol.UUIDHelper;
import com.intel.missioncontrol.persistence.ProjectOutdatedException;
import com.intel.missioncontrol.persistence.Query;
import com.intel.missioncontrol.persistence.Repository;
import com.intel.missioncontrol.persistence.ResourceResolver;
import com.intel.missioncontrol.project.property.MergeStrategy;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.asyncfx.Awaiter;
import org.asyncfx.TestBase;
import org.asyncfx.beans.property.PropertyHelper;
import org.asyncfx.collections.LockedSet;
import org.asyncfx.concurrent.Dispatcher;
import org.asyncfx.concurrent.Future;
import org.asyncfx.concurrent.Futures;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ProjectManagerTest extends TestBase {

    private static class TestRepository implements Repository {
        private final RepositoryType repositoryType;
        private final Set<Project> projects;

        TestRepository(RepositoryType repositoryType, Set<Project> projects) {
            this.repositoryType = repositoryType;
            this.projects = projects;
        }

        @Override
        public void addListener(ChangeListener listener) {}

        @Override
        public void removeListener(ChangeListener listener) {}

        @Override
        public Future<Set<Project>> queryAsync(Query query) {
            return Dispatcher.background()
                .runLaterAsync(() -> {}, Duration.ofMillis(100))
                .thenGet(() -> projects.stream().filter(query::satisfies).collect(Collectors.toSet()));
        }

        @Override
        public Future<Project> createAsync(String name) {
            return null;
        }

        @Override
        public Future<Void> saveAsync(Project project) {
            return null;
        }

        @Override
        public Future<Void> deleteAsync(UUID id) {
            return null;
        }

        @Override
        public RepositoryType getRepositoryType() {
            return repositoryType;
        }

        @Override
        public ResourceResolver getResourceResolver() {
            return null;
        }
    }

    @Test
    void ProjectManager_Queries_Two_Repositories_And_Aggregates_Projects() {
        var awaiter = new Awaiter();

        List<Project> localProjects =
            List.of(
                new Project(ProjectHelper.createRandomProject(UUIDHelper.getUUID(0), RepositoryType.LOCAL)),
                new Project(ProjectHelper.createRandomProject(UUIDHelper.getUUID(1), RepositoryType.LOCAL)),
                new Project(ProjectHelper.createRandomProject(UUIDHelper.getUUID(2), RepositoryType.LOCAL)));

        List<Project> cloudProjects =
            List.of(
                new Project(ProjectHelper.createRandomProject(UUIDHelper.getUUID(3), RepositoryType.INSIGHT)),
                new Project(ProjectHelper.createRandomProject(UUIDHelper.getUUID(4), RepositoryType.INSIGHT)),
                new Project(ProjectHelper.createRandomProject(UUIDHelper.getUUID(5), RepositoryType.INSIGHT)));

        var projectManager =
            new ProjectManager(
                new TestRepository(RepositoryType.LOCAL, new HashSet<>(localProjects)),
                new TestRepository(RepositoryType.INSIGHT, new HashSet<>(cloudProjects)));

        projectManager.refreshAsync().whenDone(awaiter::signal);
        awaiter.await(1);

        try (LockedSet<Project> projects = projectManager.availableProjectsProperty().lock()) {
            var it = projects.iterator();
            Assertions.assertEquals(it.next(), localProjects.get(0));
            Assertions.assertEquals(it.next(), localProjects.get(1));
            Assertions.assertEquals(it.next(), localProjects.get(2));
            Assertions.assertEquals(it.next(), cloudProjects.get(0));
            Assertions.assertEquals(it.next(), cloudProjects.get(1));
            Assertions.assertEquals(it.next(), cloudProjects.get(2));
        }
    }

    @Test
    void ProjectManager_Merges_Conflicting_Projects_And_Records_Conflict() {
        var awaiter = new Awaiter();

        var p0 = new Project(ProjectHelper.createRandomProject(UUIDHelper.getUUID(0), RepositoryType.LOCAL));
        var p1 = new Project(ProjectHelper.createRandomProject(UUIDHelper.getUUID(1), RepositoryType.LOCAL));
        var p2 = new Project(ProjectHelper.createRandomProject(UUIDHelper.getUUID(2), RepositoryType.LOCAL));

        var p0a = new Project(p0);
        p0a.nameProperty().set("Changed 1");
        List<Project> localProjects = List.of(p0a, p1, p2);

        var p0b = new Project(p0);
        p0b.nameProperty().set("Changed 2");
        List<Project> cloudProjects = List.of(p0b, p1, p2);

        var projectManager =
            new ProjectManager(
                new TestRepository(RepositoryType.LOCAL, new HashSet<>(localProjects)),
                new TestRepository(RepositoryType.INSIGHT, new HashSet<>(cloudProjects)));

        projectManager.refreshAsync().whenDone(awaiter::signal);
        awaiter.await(1, Duration.ofSeconds(10));

        try (LockedSet<Project> projects = projectManager.availableProjectsProperty().lock()) {
            var it = projects.iterator();
            p0 = it.next();
            p1 = it.next();
            p2 = it.next();

            Assertions.assertEquals(1, p0.mergeConflictsProperty().size());
            Assertions.assertEquals(0, p1.mergeConflictsProperty().size());
            Assertions.assertEquals(0, p2.mergeConflictsProperty().size());

            // Project p0 might be equal to p0a or p0b randomly depending on which repo was faster to complete a query

            Assertions.assertEquals(p1, localProjects.get(1));
            Assertions.assertEquals(p2, localProjects.get(2));
            Assertions.assertEquals(p1, cloudProjects.get(1));
            Assertions.assertEquals(p2, cloudProjects.get(2));
        }
    }

    @Test
    void Changed_Project_Is_Saved_To_Repository() {
        var awaiter = new Awaiter();

        var p0 = new Project(ProjectHelper.createRandomProject(UUIDHelper.getUUID(0), RepositoryType.LOCAL));
        var p1 = new Project(ProjectHelper.createRandomProject(UUIDHelper.getUUID(1), RepositoryType.LOCAL));
        var p2 = new Project(ProjectHelper.createRandomProject(UUIDHelper.getUUID(2), RepositoryType.LOCAL));

        var projectManager =
            new ProjectManager(
                new TestRepository(RepositoryType.LOCAL, Set.of(p0, p1, p2)) {
                    @Override
                    public Future<Void> saveAsync(Project project) {
                        awaiter.signal();
                        return Futures.successful();
                    }
                },
                new TestRepository(RepositoryType.INSIGHT, new HashSet<>()));

        projectManager.refreshAsync().getUnchecked();

        try (LockedSet<Project> projects = projectManager.availableProjectsProperty().lock()) {
            projectManager.currentProjectProperty().set(projects.iterator().next());
        }

        Project currentProject = projectManager.currentProjectProperty().get();

        PropertyHelper.setValueSafe(currentProject.nameProperty(), "New project name");

        // We need to wait here for the minimum period to elapse, otherwise ProjectManager will drop the change event.
        sleep((int)ProjectManager.CHANGE_DETECTION_PERIOD.toMillis() + 10);

        PropertyHelper.setValueSafe(currentProject.missionsProperty().get(0).nameProperty(), "New mission name");

        awaiter.await(2, Duration.ofSeconds(10000));
    }

    @Test
    void test_conflict_resolution_using_our_version_callback_works() {
        var awaiter = new Awaiter();
        var p0 = new Project(ProjectHelper.createRandomProject(UUIDHelper.getUUID(0), RepositoryType.LOCAL));
        var p1 = new Project(ProjectHelper.createRandomProject(UUIDHelper.getUUID(1), RepositoryType.LOCAL));
        var p2 = new Project(ProjectHelper.createRandomProject(UUIDHelper.getUUID(2), RepositoryType.LOCAL));

        var projectManager =
            new ProjectManager(
                new TestRepository(RepositoryType.LOCAL, Set.of(p0, p1, p2)) {
                    @Override
                    public Future<Void> saveAsync(Project project) {
                        awaiter.signal();
                        return Futures.successful();
                    }
                },
                new TestRepository(RepositoryType.LOCAL, new HashSet<>()));

        projectManager.registerCallback(
            conflictEntries -> {
                awaiter.signal();
                return new MergeStrategy.KeepOurs();
            });

        projectManager.refreshAsync().getUnchecked();
        PropertyHelper.setValueSafe(p0.nameProperty(), "New project name external");

        try (LockedSet<Project> projects = projectManager.availableProjectsProperty().lock()) {
            projectManager.currentProjectProperty().set(projects.iterator().next());
        }

        Project currentProject = projectManager.currentProjectProperty().get();

        PropertyHelper.setValueSafe(currentProject.nameProperty(), "New project name");

        // We need to wait here for the minimum period to elapse, otherwise ProjectManager will drop the change event.
        sleep((int)ProjectManager.CHANGE_DETECTION_PERIOD.toMillis() + 10);

        awaiter.await(2, Duration.ofSeconds(1000));

        Assertions.assertEquals("New project name", currentProject.nameProperty().get());
    }

    @Test
    void test_conflict_resolution_using_their_version_callback_works() {
        var awaiter = new Awaiter();
        var p0 = new Project(ProjectHelper.createRandomProject(UUIDHelper.getUUID(0), RepositoryType.LOCAL));
        var p1 = new Project(ProjectHelper.createRandomProject(UUIDHelper.getUUID(1), RepositoryType.LOCAL));
        var p2 = new Project(ProjectHelper.createRandomProject(UUIDHelper.getUUID(2), RepositoryType.LOCAL));

        var projectManager =
            new ProjectManager(
                new TestRepository(RepositoryType.LOCAL, Set.of(p0, p1, p2)) {
                    @Override
                    public Future<Void> saveAsync(Project project) {
                        return Futures.successful();
                    }
                },
                new TestRepository(RepositoryType.LOCAL, new HashSet<>()));

        projectManager.registerCallback(conflictEntries -> new MergeStrategy.KeepTheirs());

        projectManager.refreshAsync().getUnchecked();
        PropertyHelper.setValueSafe(p0.nameProperty(), "New project name external");

        try (LockedSet<Project> projects = projectManager.availableProjectsProperty().lock()) {
            projectManager.currentProjectProperty().set(projects.iterator().next());
        }

        projectManager.refreshAsync().getUnchecked();

        Project currentProject = projectManager.currentProjectProperty().get();

        projectManager
            .currentProjectProperty()
            .get()
            .nameProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue.equals("New project name external")) {
                        awaiter.signal();
                    }
                });

        PropertyHelper.setValueSafe(currentProject.nameProperty(), "New project name");

        // We need to wait here for the minimum period to elapse, otherwise ProjectManager will drop the change event.
        sleep((int)ProjectManager.CHANGE_DETECTION_PERIOD.toMillis() + 10);

        awaiter.await(1, Duration.ofSeconds(1000));

        Assertions.assertEquals("New project name external", currentProject.nameProperty().get());
    }

    @Test
    void test_conflict_resolution_using_their_version_plus_changes_in_the_current_project_callback_works() {
        var awaiter = new Awaiter();
        var p0 = new Project(ProjectHelper.createRandomProject(UUIDHelper.getUUID(0), RepositoryType.LOCAL));
        var p1 = new Project(ProjectHelper.createRandomProject(UUIDHelper.getUUID(1), RepositoryType.LOCAL));
        var p2 = new Project(ProjectHelper.createRandomProject(UUIDHelper.getUUID(2), RepositoryType.LOCAL));

        var projectManager =
            new ProjectManager(
                new TestRepository(RepositoryType.LOCAL, Set.of(p0, p1, p2)) {
                    @Override
                    public Future<Void> saveAsync(Project project) {
                        awaiter.signal();
                        return Futures.successful();
                    }
                },
                new TestRepository(RepositoryType.LOCAL, new HashSet<>()));

        projectManager.registerCallback(
            conflictEntries -> {
                // change that comes in the meantime
                PropertyHelper.setValueSafe(
                    projectManager.currentProjectProperty().get().nameProperty(), "Yet another name");
                return new MergeStrategy.KeepOurs();
            });

        projectManager.refreshAsync().getUnchecked();
        PropertyHelper.setValueSafe(p0.nameProperty(), "New project name external");

        try (LockedSet<Project> projects = projectManager.availableProjectsProperty().lock()) {
            projectManager.currentProjectProperty().set(projects.iterator().next());
        }

        projectManager.refreshAsync().getUnchecked();

        Project currentProject = projectManager.currentProjectProperty().get();

        projectManager
            .currentProjectProperty()
            .get()
            .nameProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    // if (newValue.equals("New project name external")) {
                    awaiter.signal();
                    // }
                });

        PropertyHelper.setValueSafe(currentProject.nameProperty(), "New project name");

        // We need to wait here for the minimum period to elapse, otherwise ProjectManager will drop the change event.
        sleep((int)ProjectManager.CHANGE_DETECTION_PERIOD.toMillis() + 10);

        awaiter.await(4, Duration.ofSeconds(1000));

        Assertions.assertEquals("Yet another name", currentProject.nameProperty().get());
    }

    @Test
    void test_conflict_resolution_using_their_version_callback_and_ProjectOutdatedException_from_server_works() {
        var awaiter = new Awaiter();
        var p0 = new Project(ProjectHelper.createRandomProject(UUIDHelper.getUUID(0), RepositoryType.LOCAL));
        var p1 = new Project(ProjectHelper.createRandomProject(UUIDHelper.getUUID(1), RepositoryType.LOCAL));
        var p2 = new Project(ProjectHelper.createRandomProject(UUIDHelper.getUUID(2), RepositoryType.LOCAL));

        var projectManager =
            new ProjectManager(
                new TestRepository(RepositoryType.LOCAL, Set.of(p0, p1, p2)) {
                    @Override
                    public Future<Void> saveAsync(Project project) {
                        awaiter.signal();
                        return Futures.failed(new ProjectOutdatedException(new ProjectSnapshot(project)));
                    }
                },
                new TestRepository(RepositoryType.LOCAL, new HashSet<>()));

        projectManager.registerCallback(conflictEntries -> new MergeStrategy.KeepTheirs());

        projectManager.refreshAsync().getUnchecked();
        PropertyHelper.setValueSafe(p0.nameProperty(), "New project name external");

        try (LockedSet<Project> projects = projectManager.availableProjectsProperty().lock()) {
            projectManager.currentProjectProperty().set(projects.iterator().next());
        }

        projectManager.refreshAsync().getUnchecked();

        Project currentProject = projectManager.currentProjectProperty().get();

        PropertyHelper.setValueSafe(currentProject.nameProperty(), "New project name");

        // We need to wait here for the minimum period to elapse, otherwise ProjectManager will drop the change event.
        sleep((int)ProjectManager.CHANGE_DETECTION_PERIOD.toMillis() + 10);

        // Infinite amount of times (let's say 5 times for now) sync procedure will go through retrieving a project from
        // the repo, trying to merge with the current project and saving back to the repo
        awaiter.await(5, Duration.ofSeconds(10000));

        Assertions.assertEquals("New project name", currentProject.nameProperty().get());
    }

}
