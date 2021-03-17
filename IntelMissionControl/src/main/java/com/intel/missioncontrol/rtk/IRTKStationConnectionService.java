/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.rtk;

import org.asyncfx.beans.property.ReadOnlyAsyncObjectProperty;

public interface IRTKStationConnectionService {

    ReadOnlyAsyncObjectProperty<IRTKStation> getRTKStation();
}
