/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project;

import com.intel.missioncontrol.project.persistence.ResourceResolver;
import com.intel.missioncontrol.project.serialization.DeserializationContext;

public class Project extends AbstractProject {

    public Project() {
        super();
    }

    public Project(IProject other) {
        super(other);
    }

    public Project(DeserializationContext context) {
        super(context);
    }

    public void setResolver(ResourceResolver resolver) {

    }

}
