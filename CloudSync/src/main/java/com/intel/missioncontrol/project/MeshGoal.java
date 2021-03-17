/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project;

import com.intel.missioncontrol.project.serialization.DeserializationContext;

public class MeshGoal extends AbstractMeshGoal {

    public MeshGoal() {}

    public MeshGoal(IMeshGoal source) {
        super(source);
    }

    public MeshGoal(DeserializationContext context) {
        super(context);
    }

}
