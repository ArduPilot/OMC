/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project;

import com.intel.missioncontrol.project.property.MergeStrategy;
import org.asyncfx.beans.property.AsyncListProperty;
import org.asyncfx.concurrent.Future;
import java.nio.file.Path;
import java.util.UUID;

public interface IProjectManager {

    AsyncListProperty<Project> availableProjectsProperty();

    // updates the list of available projects: add new projects from the repos and updates status of existing ones
    Future<Void> refreshAsync();

    Future<Project> create(String name, RepositoryType type);

    Future<Project> loadExternal(Path path);

    Future<Void> synchronize(Project project, MergeStrategy strategy);

    Future<Void> delete(UUID info);

    // deletes from FS original location
    Future<Void> cloudify(Project project);

    // it is deleted from insight, also from cache and only stored in the local FS repo
    Future<Void> groundify(Project project);
}
