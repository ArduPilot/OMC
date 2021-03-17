/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.connector;

import java.util.UUID;

public interface ConnectorIdGenerator {
    UUID generateNextId();
}
