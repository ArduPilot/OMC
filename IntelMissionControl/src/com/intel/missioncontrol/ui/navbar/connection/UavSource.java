/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.connection;

import java.util.List;

public interface UavSource {
    List<UnmannedAerialVehicle> listUavs();
}
