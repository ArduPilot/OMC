/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.analysis.tasks;

public enum CreateDatasetSubTasks {
    START(10),
    PARSE_LOG_FILES(2000),
    COPY_FLIGHT_PLANS(300),
    COPY_LOG_FILES(300),
    COPY_AUX_FILE(300),
    COPY_IMAGES(60000),
    LOAD_IMAGES(30000),
    OPTIMIZE_DATASET(10000),
    CREATE_LAYERS(100),
    MOVE_UNMATCHED_IMAGES(1000),
    IMPORT_FLIGHT_PLANS(300),
    CALCULATE_LAYERS(300),
    SAVE_LAYERS(300),
    ERASE_SD(300),
    GENERATE_THUMBFILES(60000);

    final long duration;

    CreateDatasetSubTasks(long duration) {
        this.duration = duration;
    }
}
