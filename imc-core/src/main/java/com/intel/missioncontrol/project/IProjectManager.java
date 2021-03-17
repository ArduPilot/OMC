/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project;

import com.intel.missioncontrol.persistence.Repository;
import com.intel.missioncontrol.project.property.MergeStrategy;
import java.util.List;
import java.util.function.Function;
import org.asyncfx.beans.property.AsyncObjectProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncListProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncSetProperty;
import org.asyncfx.concurrent.Future;

public interface IProjectManager {

    ReadOnlyAsyncListProperty<Repository> repositoriesProperty();

    ReadOnlyAsyncSetProperty<Project> availableProjectsProperty();

    AsyncObjectProperty<Project> currentProjectProperty();

    Future<Void> refreshAsync();

    void addRepository(Repository repository);

    Future<Void> synchronizeAsync(Project project);

    Future<Void> moveAsync(Project project, Repository repository);

    void registerCallback(Function<List<? extends MergeStrategy.ConflictEntry>, MergeStrategy> callback);

}
