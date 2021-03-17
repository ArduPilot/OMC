/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project;

import com.intel.missioncontrol.project.serialization.DeserializationContext;

public class Dataset extends AbstractDataset {

    public Dataset() {
        super();
    }

    public Dataset(IDataset source) {
        super(source);
    }

    public Dataset(DeserializationContext context) {
        super(context);
    }

}
