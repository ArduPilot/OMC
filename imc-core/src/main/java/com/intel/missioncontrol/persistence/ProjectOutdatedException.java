/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.persistence;

import com.intel.missioncontrol.project.ProjectSnapshot;

public class ProjectOutdatedException extends RuntimeException {
    private ProjectSnapshot repositoryProject;


    public ProjectOutdatedException(ProjectSnapshot repositoryProject) {
        this.repositoryProject = repositoryProject;
    }

    public ProjectSnapshot getRepositoryProject() {
        return repositoryProject;
    }
}
