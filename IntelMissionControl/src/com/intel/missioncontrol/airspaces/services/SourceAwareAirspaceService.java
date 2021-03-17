/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.airspaces.services;

import com.intel.missioncontrol.airspaces.sources.AirspaceSource;

interface SourceAwareAirspaceService {
    AirspaceSource getSource();
}
