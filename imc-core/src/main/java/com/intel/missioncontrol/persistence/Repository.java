/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.persistence;

import com.intel.missioncontrol.project.Project;
import com.intel.missioncontrol.project.RepositoryType;
import java.util.Set;
import java.util.UUID;
import org.asyncfx.concurrent.Future;

public interface Repository {

    interface ChangeListener {
        void changed(Project project);
    }

    RepositoryType getRepositoryType();

    ResourceResolver getResourceResolver();

    void addListener(ChangeListener listener);

    void removeListener(ChangeListener listener);

    Future<Set<Project>> queryAsync(Query query);

    Future<Project> createAsync(String name);

    /** Saves changes to the repository, might fail if the current project version is outdated */
    Future<Void> saveAsync(Project project);

    Future<Void> deleteAsync(UUID id);

}
