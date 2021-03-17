/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project;

import com.intel.missioncontrol.serialization.CompositeDeserializationContext;

public class Dataset extends AbstractDataset {

    public Dataset() {
        super();
    }

    public Dataset(Dataset source) {
        super(source);
    }

    public Dataset(DatasetSnapshot source) {
        super(source);
    }

    public Dataset(CompositeDeserializationContext context) {
        super(context);
    }

}
