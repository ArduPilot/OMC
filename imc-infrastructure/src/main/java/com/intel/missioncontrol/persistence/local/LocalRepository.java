/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.persistence.local;

import com.google.inject.Inject;
import com.intel.missioncontrol.common.IPathProvider;
import com.intel.missioncontrol.persistence.PersistenceException;
import com.intel.missioncontrol.persistence.Query;
import com.intel.missioncontrol.persistence.Repository;
import com.intel.missioncontrol.persistence.ResourceResolver;
import com.intel.missioncontrol.project.Project;
import com.intel.missioncontrol.project.ProjectSnapshot;
import com.intel.missioncontrol.project.RepositoryType;
import com.intel.missioncontrol.project.ResourceReference;
import com.intel.missioncontrol.serialization.JsonSerializer;
import com.intel.missioncontrol.serialization.SerializationException;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.asyncfx.beans.property.AsyncObjectProperty;
import org.asyncfx.collections.ArraySet;
import org.asyncfx.concurrent.Dispatcher;
import org.asyncfx.concurrent.Future;
import org.asyncfx.concurrent.Futures;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalRepository implements Repository {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalRepository.class);
    private static final String PROJECT_FILENAME = "project.json";

    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final DirectoryWatcher directoryWatcher;

    private final List<ChangeListener> listeners = new ArrayList<>();
    private final Path rootPath;

    @Inject
    public LocalRepository(IPathProvider pathProvider) {
        this(pathProvider.getProjectsDirectory());
    }

    public LocalRepository(Path path) {
        this.rootPath = path;
        try {
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
        } catch (IOException ex) {
            throw PersistenceException.fromException(ex);
        }

        directoryWatcher =
            new DirectoryWatcher(
                rootPath,
                true,
                new DirectoryWatcher.Handler() {
                    @Override
                    public void fileAdded(Path path) {
                        handleProjectChangeDetected(path);
                    }

                    @Override
                    public void fileDeleted(Path path) {}

                    @Override
                    public void fileModified(Path path) {
                        handleProjectChangeDetected(path);
                    }
                });
    }

    @Override
    public RepositoryType getRepositoryType() {
        return RepositoryType.INSIGHT;
    }

    @Override
    public void addListener(ChangeListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    @Override
    public void removeListener(ChangeListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    @Override
    public Future<Project> createAsync(String name) {
        UUID uuid = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

        return Futures.successful(
            new Project(
                new ProjectSnapshot(
                    uuid,
                    name,
                    RepositoryType.LOCAL,
                    now,
                    now,
                    null,
                    null,
                    new ArrayList<>(),
                    new ArrayList<>(),
                    new ArrayList<>())));
    }

    @Override
    public Future<Void> deleteAsync(UUID id) {
        return Dispatcher.background().runLaterAsync((Runnable)Futures::successful);
    }

    @Override
    public Future<Set<Project>> queryAsync(Query query) {
        return Dispatcher.background()
            .getLaterAsync(
                () -> {
                    Set<Project> projects = new ArraySet<>();

                    try (DirectoryStream<Path> subdirs = Files.newDirectoryStream(rootPath)) {
                        for (Path subdir : subdirs) {
                            Path projectFile = subdir.resolve(PROJECT_FILENAME);
                            if (!Files.exists(projectFile)) {
                                continue;
                            }

                            try (InputStream stream =
                                new BufferedInputStream(new FileInputStream(projectFile.toFile()))) {
                                JsonSerializer serializer = new JsonSerializer();
                                serializer.setPrettyPrint(true);
                                serializer.setPreserveReferences(true);
                                Project project = serializer.deserialize(stream, Project.class);

                                if (query.satisfies(project)) {
                                    ((AsyncObjectProperty<Repository>)project.repositoryProperty()).set(this);
                                    projects.add(project);
                                }
                            } catch (SerializationException ignored) {
                            }
                        }
                    } catch (Exception ex) {
                        throw PersistenceException.fromException(ex);
                    }

                    return projects;
                });
    }

    @Override
    public Future<Void> saveAsync(Project project) {
        return Dispatcher.background()
            .runLaterAsync(
                () -> {
                    try {
                        var uuid = project.getId();
                        Path projectDir = rootPath.resolve(uuid.toString());

                        if (!Files.exists(projectDir)) {
                            Files.createDirectory(projectDir);
                        }

                        try (OutputStream stream =
                            new BufferedOutputStream(
                                new FileOutputStream(projectDir.resolve(PROJECT_FILENAME).toFile()))) {
                            JsonSerializer serializer = new JsonSerializer();
                            serializer.setPrettyPrint(true);
                            serializer.setPreserveReferences(true);
                            serializer.serialize(project, stream);
                        }
                    } catch (Exception ex) {
                        throw PersistenceException.fromException(ex);
                    }
                });
    }

    @Override
    public ResourceResolver getResourceResolver() {
        return null;
    }

    private ResourceResolver createResourceResolver(Path directory) {
        return new ResourceResolver() {
            @Override
            public Future<InputStream> openInputStreamAsync(ResourceReference reference) {
                Path path = directory.resolve(reference.getName());
                if (Files.isRegularFile(path) && Files.exists(path)) {
                    try {
                        return Futures.successful(new BufferedInputStream(new FileInputStream(path.toFile())));
                    } catch (Throwable throwable) {
                        return Futures.failed(throwable);
                    }
                }

                return Futures.failed(new FileNotFoundException(reference.getName()));
            }

            @Override
            public Future<OutputStream> openOutputStreamAsync(ResourceReference reference) {
                Path path = directory.resolve(reference.getName());
                try {
                    Files.createDirectories(path);
                    return Futures.successful(new BufferedOutputStream(new FileOutputStream(path.toFile())));
                } catch (Throwable throwable) {
                    return Futures.failed(throwable);
                }
            }
        };
    }

    private void handleProjectChangeDetected(Path path) {
        Path relativePath = rootPath.relativize(path);
        boolean isProjectFile =
            relativePath.getNameCount() == 2 && relativePath.getName(1).toString().equals(PROJECT_FILENAME);

        if (isProjectFile) {
            try (InputStream stream = new BufferedInputStream(new FileInputStream(path.toFile()))) {
                JsonSerializer serializer = new JsonSerializer();
                Project project = serializer.deserialize(stream, Project.class);
                notifyChangeListeners(project);
            } catch (IOException | SerializationException ex) {
                LOGGER.info("Reading project file '" + path + "' failed with the following error: " + ex.getMessage());
            }
        }
    }

    private void notifyChangeListeners(Project project) {
        synchronized (listeners) {
            for (ChangeListener listener : listeners) {
                listener.changed(project);
            }
        }
    }

}
