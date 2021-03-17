/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.persistence;

import com.intel.missioncontrol.project.ProjectSnapshot;
import com.intel.missioncontrol.project.RepositoryType;
import com.intel.missioncontrol.project.ResourceReference;
import com.intel.missioncontrol.project.serialization.JsonSerializer;
import com.intel.missioncontrol.project.serialization.SerializationException;
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
import org.asyncfx.collections.ArraySet;
import org.asyncfx.concurrent.Dispatcher;
import org.asyncfx.concurrent.Future;
import org.asyncfx.concurrent.Futures;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FolderRepository implements Repository {

    private static final Logger LOGGER = LoggerFactory.getLogger(FolderRepository.class);
    private static final String PROJECT_FILENAME = "project.json";

    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final DirectoryWatcher directoryWatcher;

    private final List<ChangeListener> listeners = new ArrayList<>();
    private final Path rootPath;

    public FolderRepository(Path rootPath) {
        try {
            if (!Files.exists(rootPath)) {
                Files.createDirectories(rootPath);
            }
        } catch (IOException ex) {
            throw PersistenceException.fromException(ex);
        }

        this.rootPath = rootPath;

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
    public Future<ProjectSnapshot> create(String name) {
        UUID uuid = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

        return Futures.successful(
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
                new ArrayList<>()));
    }

    @Override
    public Future<Void> delete(UUID id) {
        return Dispatcher.background().runLaterAsync((Runnable)Futures::successful);
    }

    @Override
    public Future<Set<ProjectSnapshot>> query(Query<ProjectSnapshot> query) {
        return Dispatcher.background()
            .getLaterAsync(
                () -> {
                    Set<ProjectSnapshot> projects = new ArraySet<>();

                    try (DirectoryStream<Path> subdirs = Files.newDirectoryStream(rootPath)) {
                        for (Path subdir : subdirs) {
                            Path projectFile = subdir.resolve(PROJECT_FILENAME);
                            if (!Files.exists(projectFile)) {
                                continue;
                            }

                            try (InputStream stream =
                                new BufferedInputStream(new FileInputStream(projectFile.toFile()))) {
                                JsonSerializer serializer = new JsonSerializer(true);
                                ProjectSnapshot project = serializer.deserialize(stream, ProjectSnapshot.class);

                                if (query.satisfies(project)) {
                                    List<ResourceReference> resources = project.getResources();
                                    if (!resources.isEmpty()) {
                                        ResourceResolver resourceResolver = createResourceResolver(subdir);
                                        for (ResourceReference resourceRef : resources) {
                                            resourceRef.setResolver(resourceResolver);
                                        }
                                    }

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
    public Future<Void> save(ProjectSnapshot project) {
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
                            JsonSerializer serializer = new JsonSerializer(true);
                            serializer.serialize(null, stream);
                        }
                    } catch (Exception ex) {
                        throw PersistenceException.fromException(ex);
                    }
                });
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
                ProjectSnapshot project = serializer.deserialize(stream, ProjectSnapshot.class);
                notifyChangeListeners(project);
            } catch (IOException | SerializationException ex) {
                LOGGER.info("Reading project file '" + path + "' failed with the following error: " + ex.getMessage());
            }
        }
    }

    private void notifyChangeListeners(ProjectSnapshot project) {
        synchronized (listeners) {
            for (ChangeListener listener : listeners) {
                listener.changed(project);
            }
        }
    }

}
