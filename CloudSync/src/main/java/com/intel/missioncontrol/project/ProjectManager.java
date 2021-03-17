/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project;

import com.intel.missioncontrol.NotImplementedException;
import com.intel.missioncontrol.project.persistence.FolderRepository;
import com.intel.missioncontrol.project.persistence.Repository;
import com.intel.missioncontrol.project.persistence.insight.InsightRepository;
import com.intel.missioncontrol.project.property.MergeStrategy;
import com.intel.missioncontrol.project.property.PlatformSynchronizationContext;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.UUID;
import org.asyncfx.beans.property.AsyncListProperty;
import org.asyncfx.beans.property.PropertyMetadata;
import org.asyncfx.beans.property.SimpleAsyncListProperty;
import org.asyncfx.collections.AsyncObservableList;
import org.asyncfx.collections.FXAsyncCollections;
import org.asyncfx.concurrent.Dispatcher;
import org.asyncfx.concurrent.Future;
import org.asyncfx.concurrent.Strand;

public class ProjectManager implements IProjectManager {
    private final Strand strand = new Strand();
    private InsightRepository insightRepository;
    private FolderRepository folderRepository;
    private AsyncListProperty<Project> availableProjects =
        new SimpleAsyncListProperty<>(
            this,
            new PropertyMetadata.Builder<AsyncObservableList<Project>>()
                .initialValue(FXAsyncCollections.observableArrayList())
                .synchronizationContext(PlatformSynchronizationContext.getInstance())
                .create());

    public ProjectManager(Path rootPath, Path insightCachePath) {
        insightRepository = new InsightRepository(insightCachePath);
        folderRepository = new FolderRepository(rootPath);
    }

    private Repository getProjectRepository(RepositoryType type) {
        switch (type) {
        case LOCAL:
            return folderRepository;
        case INSIGHT:
            return insightRepository;
        default:
            throw new IllegalArgumentException("type");
        }
    }

    @Override
    public AsyncListProperty<Project> availableProjectsProperty() {
        return availableProjects;
    }

    @Override
    public Future<Void> refreshAsync() {
        return null;
        // return MergeHelper.merge();
        /*LinkedHashSet<ProjectSnapshot> updatedProjects = new LinkedHashSet<>();
        return folderRepository
            .query()
            .thenApplyAsync(
                set -> {
                    updatedProjects.addAll(set);
                    return insightRepository.query();
                })
            .thenAcceptAsync(
                value -> {
                    updatedProjects.addAll(value);

                    Dispatcher dispatcher = Dispatcher.platform();
                    return dispatcher.runLaterAsync(
                        () -> {
                            try (var lockedList = availableProjects.lock()) {
                                MergeHelper.updateTargetFromSource(
                                    updatedProjects,
                                    lockedList,
                                    (s, t) -> s.getId().equals(t.getId()),
                                    (project, projectSnapshot) -> project.merge(projectSnapshot, MergeStrategy.NONE),
                                    Project::new);
                            }
                        });
                });*/
    }

    @Override
    public Future<Project> create(String name, RepositoryType type) {
        /*return getProjectRepository(type)
        .create(name)
        .thenApplyAsync(p -> Dispatcher.platform().getLaterAsync(() -> new ImmutableProject(p)));*/
        return null;
    }

    @Override
    public Future<Project> loadExternal(Path path) {
        /*return new FolderRepository(path.getParent())
        .read(UUID.fromString(path.getFileName().toString()))
        .thenApplyAsync(p -> Dispatcher.platform().getLaterAsync(() -> new Project(p)));*/
        return null;
    }

    @Override
    public Future<Void> synchronize(Project project, MergeStrategy strategy) {
        /*return getProjectRepository(project.getRepositoryType())
        .read(project.getId())
        // .thenApplyAsync(p -> Dispatcher.platform().runLaterAsync(() -> project.deepMerge(p, strategy)))
        .thenAcceptAsync(
            value -> getProjectRepository(project.getRepositoryType()).save(new ProjectSnapshot(project)));*/
        return null;
    }

    @Override
    public Future<Void> delete(UUID info) {
        return null;
    }

    @Override
    public Future<Void> cloudify(Project project) {
        throw new NotImplementedException();
        /*return insightRepository
        .create(project.getName())
        .thenApplyAsync(
            p -> Dispatcher.platform().runLaterAsync(() -> project.merge(p, MergeStrategy.KEEP_LOCAL, null)))
        .thenApplyAsync(
            value -> {
                folderRepository.delete(project.getId());
                return Futures.successful(value);
            });*/
    }

    @Override
    public Future<Void> groundify(Project project) {
        return null;
    }
}
