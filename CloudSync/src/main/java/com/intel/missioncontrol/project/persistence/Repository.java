/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.persistence;

import com.intel.missioncontrol.project.ProjectSnapshot;
import java.util.Set;
import java.util.UUID;
import org.asyncfx.concurrent.Future;

public interface Repository {

    interface ChangeListener {
        void changed(ProjectSnapshot project);
    }

    void addListener(ChangeListener listener);

    void removeListener(ChangeListener listener);

    Future<Set<ProjectSnapshot>> query(Query<ProjectSnapshot> query);

    Future<ProjectSnapshot> create(String name);

    /** Saves changes to the repository, might fail if the current project version is outdated */
    Future<Void> save(ProjectSnapshot project);

    Future<Void> delete(UUID id);

}
