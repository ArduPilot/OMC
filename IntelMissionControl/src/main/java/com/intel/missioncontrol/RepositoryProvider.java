/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.intel.missioncontrol.common.IPathProvider;
import com.intel.missioncontrol.persistence.Repository;
import com.intel.missioncontrol.persistence.local.LocalRepository;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import java.nio.file.Path;

public class RepositoryProvider implements Provider<Repository> {
    @Inject
    IPathProvider pathProvider;

    @Override
    public Repository get() {
        Path projectsDirectoryPath = pathProvider.getProjectsDirectory();
        return new LocalRepository(projectsDirectoryPath);
    }
}
